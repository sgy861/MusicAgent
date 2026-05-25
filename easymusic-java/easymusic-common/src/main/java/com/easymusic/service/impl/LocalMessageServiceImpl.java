package com.easymusic.service.impl;

import com.easymusic.entity.po.LocalMessage;
import com.easymusic.mappers.LocalMessageMapper;
import com.easymusic.service.LocalMessageService;
import com.easymusic.utils.JsonUtils;
import com.easymusic.utils.StringTools;
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
            if (localMessage.getExchangeName() != null && !localMessage.getExchangeName().isEmpty()) {
                rabbitTemplate.convertAndSend(localMessage.getExchangeName(), localMessage.getRoutingKey(), localMessage.getMessageContent(), correlationData);
            } else {
                rabbitTemplate.convertAndSend(localMessage.getQueueName(), (Object) localMessage.getMessageContent(), correlationData);
            }
            
            LocalMessage update = new LocalMessage();
            update.setStatus(1); // SUCCESS
            update.setUpdateTime(new Date());
            localMessageMapper.updateByMessageId(update, localMessage.getMessageId());
            log.info("Message sent successfully, messageId: {}", localMessage.getMessageId());
        } catch (Exception e) {
            log.error("Failed to send message to RabbitMQ, messageId: " + localMessage.getMessageId(), e);
            LocalMessage update = new LocalMessage();
            update.setStatus(2); // FAIL
            update.setRetryCount(localMessage.getRetryCount() + 1);
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
