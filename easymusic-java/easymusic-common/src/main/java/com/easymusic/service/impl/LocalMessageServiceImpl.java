package com.easymusic.service.impl;

import com.easymusic.entity.po.LocalMessage;
import com.easymusic.mappers.LocalMessageMapper;
import com.easymusic.service.LocalMessageService;
import com.easymusic.utils.JsonUtils;
import com.easymusic.utils.StringTools;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Date;
import java.util.List;

@Service("localMessageService")
@Slf4j
public class LocalMessageServiceImpl implements LocalMessageService {

    @Resource
    private LocalMessageMapper localMessageMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        // 配置 ConfirmCallback：当消息成功发送到 Broker（Exchange）时触发
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData != null) {
                String messageId = correlationData.getId();
                log.info("RabbitMQ publisher confirm callback: messageId={}, ack={}, cause={}", messageId, ack, cause);
                if (ack) {
                    // 发送成功，标记本地消息状态为 1 (SUCCESS)
                    LocalMessage update = new LocalMessage();
                    update.setStatus(1);
                    update.setUpdateTime(new Date());
                    localMessageMapper.updateByMessageId(update, messageId);
                } else {
                    // 发送失败，标记本地消息状态为 2 (FAIL)
                    log.warn("RabbitMQ NACKed message: messageId={}, cause={}", messageId, cause);
                    LocalMessage update = new LocalMessage();
                    update.setStatus(2);
                    update.setUpdateTime(new Date());
                    localMessageMapper.updateByMessageId(update, messageId);
                }
            }
        });

        // 配置 ReturnsCallback：当消息被 Exchange 路由，但由于路由键不匹配等原因未投递到任何 Queue 时触发
        rabbitTemplate.setReturnsCallback(returned -> {
            String messageId = returned.getMessage().getMessageProperties().getCorrelationId();
            log.warn("RabbitMQ returned message: messageId={}, replyCode={}, replyText={}, exchange={}, routingKey={}",
                    messageId, returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey());
            if (messageId != null) {
                // 路由失败，标记本地消息状态为 2 (FAIL)
                LocalMessage update = new LocalMessage();
                update.setStatus(2);
                update.setUpdateTime(new Date());
                localMessageMapper.updateByMessageId(update, messageId);
            }
        });
    }

    @Override
    public String createAndSaveMessage(String queueName, String exchangeName, String routingKey, Object content) {
        String messageId = StringTools.getRandomString(20);
        LocalMessage localMessage = new LocalMessage();
        localMessage.setMessageId(messageId);
        localMessage.setQueueName(queueName);
        localMessage.setExchangeName(exchangeName);
        localMessage.setRoutingKey(routingKey);
        localMessage.setMessageContent(JsonUtils.convertObj2Json(content));
        localMessage.setStatus(0); // Sending
        localMessage.setRetryCount(0);
        Date now = new Date();
        localMessage.setCreateTime(now);
        localMessage.setUpdateTime(now);

        localMessageMapper.insert(localMessage);

        // Register transaction synchronization to publish to RabbitMQ after commit
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishMessage(localMessage);
                }
            });
        } else {
            // If not in a transaction, publish immediately
            publishMessage(localMessage);
        }

        return messageId;
    }

    @Override
    public void publishMessage(String messageId) {
        LocalMessage localMessage = localMessageMapper.selectByMessageId(messageId);
        if (localMessage != null) {
            publishMessage(localMessage);
        }
    }

    @Override
    public void publishMessage(LocalMessage localMessage) {
        try {
            log.info("Publishing message to RMQ, messageId: {}", localMessage.getMessageId());
            CorrelationData correlationData = new CorrelationData(localMessage.getMessageId());
            
            // 立即在数据库中更新 update_time，并递增 retry_count，防范在 10s 内被定时重试任务并发重复提取发送
            LocalMessage update = new LocalMessage();
            update.setStatus(0); // 标记回发送中
            update.setRetryCount(localMessage.getRetryCount() + 1);
            update.setUpdateTime(new Date());
            localMessageMapper.updateByMessageId(update, localMessage.getMessageId());

            if (localMessage.getExchangeName() != null && !localMessage.getExchangeName().isEmpty()) {
                rabbitTemplate.convertAndSend(localMessage.getExchangeName(), localMessage.getRoutingKey(), localMessage.getMessageContent(), correlationData);
            } else {
                rabbitTemplate.convertAndSend(localMessage.getQueueName(), (Object) localMessage.getMessageContent(), correlationData);
            }
            log.info("Message sent to rabbitTemplate, awaiting async confirmation. messageId: {}", localMessage.getMessageId());
        } catch (Exception e) {
            log.error("Failed to send message to RabbitMQ, messageId: " + localMessage.getMessageId(), e);
            LocalMessage update = new LocalMessage();
            update.setStatus(2); // FAIL
            update.setUpdateTime(new Date());
            localMessageMapper.updateByMessageId(update, localMessage.getMessageId());
        }
    }

    @Override
    public void retryUnsentMessages() {
        // Retry limit is 5
        List<LocalMessage> list = localMessageMapper.selectUnsentMessages(5);
        if (list == null || list.isEmpty()) {
            return;
        }
        log.info("Found {} unsent/failed local messages to retry", list.size());
        for (LocalMessage message : list) {
            publishMessage(message);
        }
    }
}
