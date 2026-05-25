package com.easymusic.task;

import com.easymusic.service.LocalMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LocalMessageRetryTask {

    @Resource
    private LocalMessageService localMessageService;

    // Run every 10 seconds
    @Scheduled(fixedDelay = 10000)
    public void retrySendMessages() {
        try {
            localMessageService.retryUnsentMessages();
        } catch (Exception e) {
            log.error("Error retrying local messages", e);
        }
    }
}
