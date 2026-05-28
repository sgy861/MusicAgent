package com.easymusic.task;

import com.easymusic.netty.ChannelManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 节点定时心跳任务。
 * 每10秒向 Redis 中更新当前节点的心跳 Key（TTL=15s），用于证明当前节点存活。
 */
@Component
@Slf4j
public class NodeHeartbeatTask {

    @Resource
    private ChannelManager channelManager;

    @Scheduled(fixedDelay = 10000)
    public void heartbeat() {
        try {
            channelManager.renewHeartbeat();
        } catch (Exception e) {
            log.error("[NodeHeartbeatTask] Failed to renew node heartbeat", e);
        }
    }
}
