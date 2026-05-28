package com.easymusic.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.easymusic.config.RabbitConfig;
import com.easymusic.entity.dto.AiRecommendTaskDTO;
import com.easymusic.entity.po.ImMessage;
import com.easymusic.service.RecommendAgentService;
import com.easymusic.service.RecommendationStreamCallback;
import com.easymusic.utils.JsonUtils;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 异步 AI 推荐任务消费者。
 * 接收来自 Netty 网关投递的推荐任务，在公共后台线程中执行耗时的 Agent 推理，
 * 并通过 RabbitMQ 回传队列实时将流式 Token（Think、Result、Error）投递到对应的 Netty 网关实例。
 */
@Component
@Slf4j
public class AiRecommendTaskConsumer {

    @Resource
    private RecommendAgentService recommendAgentService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.AI_RECOMMEND_TASK_QUEUE)
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("[AiRecommendTaskConsumer] Received task: {}", body);
            AiRecommendTaskDTO taskDto = JsonUtils.convertJson2Obj(body, AiRecommendTaskDTO.class);

            if (taskDto == null || taskDto.getUserId() == null) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            String userId = taskDto.getUserId();
            String currentInput = taskDto.getCurrentInput();
            String nodeAddress = taskDto.getNodeAddress();
            Long seq = taskDto.getRequestSeq();

            recommendAgentService.generateRecommendationStream(userId, currentInput, new RecommendationStreamCallback() {
                @Override
                public void onStart() {
                    sendResultToNode(nodeAddress, userId, seq, "{\"type\":\"RECOMMEND_START\"}");
                }

                @Override
                public void onThink(String token) {
                    JSONObject msg = new JSONObject();
                    msg.put("type", "RECOMMEND_THINK");
                    msg.put("content", token);
                    sendResultToNode(nodeAddress, userId, seq, msg.toJSONString());
                }

                @Override
                public void onResult(String jsonResult) {
                    sendResultToNode(nodeAddress, userId, seq, jsonResult);
                }

                @Override
                public void onError(Throwable throwable) {
                    JSONObject msg = new JSONObject();
                    msg.put("type", "RECOMMEND_ERROR");
                    msg.put("content", throwable.getMessage());
                    sendResultToNode(nodeAddress, userId, seq, msg.toJSONString());
                }
            });

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[AiRecommendTaskConsumer] Failed to process AI recommendation task", e);
            // 发生错误时拒绝且不重回队列，避免死循环毒包
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void sendResultToNode(String nodeAddress, String userId, Long seq, String payload) {
        if (nodeAddress == null) {
            log.warn("[AiRecommendTaskConsumer] Cannot send result, nodeAddress is null");
            return;
        }

        // 用 ImMessage 的壳包装推荐结果
        ImMessage imMessage = new ImMessage();
        imMessage.setMsgType("RECOMMEND");
        imMessage.setReceiverId(userId);

        // 在 content 中放 JSON（包含 seq 与 payload）
        JSONObject contentObj = new JSONObject();
        contentObj.put("seq", seq);
        contentObj.put("payload", payload);
        imMessage.setContent(contentObj.toJSONString());

        // 投递到 direct 交换机，路由键为对应的 Netty 节点地址
        rabbitTemplate.convertAndSend(
                RabbitConfig.AI_RECOMMEND_RESULT_EXCHANGE,
                nodeAddress,
                JsonUtils.convertObj2Json(imMessage)
        );
    }
}
