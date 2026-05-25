package com.easymusic.consumer;

import com.easymusic.config.RabbitConfig;
import com.easymusic.entity.constants.Constants;
import com.easymusic.redis.RedisUtils;
import com.easymusic.service.UserPreferenceService;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class UserBehaviorConsumer {

    @Resource
    private UserPreferenceService userPreferenceService;

    @Resource
    private RedisUtils<Object> redisUtils;

    @RabbitListener(queues = RabbitConfig.USER_BEHAVIOR_UPDATE_QUEUE)
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            String userId = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("接收到用户行为延迟更新消息，userId: {}", userId);

            String redisKey = Constants.REDIS_KEY_USER_PROFILE_DIRTY + userId;
            Object isDirtyObj = redisUtils.get(redisKey);
            boolean isDirty = false;
            if (isDirtyObj != null) {
                if (isDirtyObj instanceof Boolean) {
                    isDirty = (Boolean) isDirtyObj;
                } else {
                    isDirty = Boolean.parseBoolean(isDirtyObj.toString());
                }
            }

            if (isDirty) {
                log.info("用户画像处于脏状态，开始触发增量更新，userId: {}", userId);
                userPreferenceService.updateUserProfile(userId);
                // 更新完成后清除脏标记
                redisUtils.delete(redisKey);
                log.info("用户画像增量更新完成，已清除脏标记，userId: {}", userId);
            } else {
                log.info("用户画像未处于脏状态，忽略此次更新，userId: {}", userId);
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理用户行为延迟更新消息失败", e);
            // 异常时不重新入队，避免死循环，直接Ack或者Nack丢弃
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
