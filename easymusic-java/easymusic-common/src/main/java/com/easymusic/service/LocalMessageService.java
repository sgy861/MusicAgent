package com.easymusic.service;

import com.easymusic.entity.po.LocalMessage;

public interface LocalMessageService {
    String createAndSaveMessage(String queueName, String exchangeName, String routingKey, Object content);
    void publishMessage(String messageId);
    void publishMessage(LocalMessage localMessage);
    void retryUnsentMessages();
}
