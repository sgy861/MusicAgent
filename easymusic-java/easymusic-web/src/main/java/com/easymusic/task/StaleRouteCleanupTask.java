package com.easymusic.task;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 宕机路由自动清理任务。
 * 每15秒扫描所有存有在线用户的节点路由 Key。
 * 若发现某节点心跳 Key 已过期，则判定该节点意外宕机/被杀，批量清理该节点下所有残留用户的在线路由。
 */
@Component
@Slf4j
public class StaleRouteCleanupTask {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String IM_NODE_USERS_PREFIX = "im:node:users:";
    private static final String IM_HEARTBEAT_PREFIX = "im:heartbeat:";
    private static final String IM_ROUTE_KEY_PREFIX = "im:route:";

    @Scheduled(fixedDelay = 15000)
    public void cleanStaleRoutes() {
        try {
            // 扫描所有已在 Redis 注册的在线用户节点集合 Key
            Set<String> nodeKeys = redisTemplate.keys(IM_NODE_USERS_PREFIX + "*");
            if (nodeKeys == null || nodeKeys.isEmpty()) {
                return;
            }

            for (String nodeUsersKey : nodeKeys) {
                // 截取出节点地址
                String nodeAddress = nodeUsersKey.substring(IM_NODE_USERS_PREFIX.length());

                // 检查该节点心跳 Key 是否存在
                String heartbeatKey = IM_HEARTBEAT_PREFIX + nodeAddress;
                Boolean isAlive = redisTemplate.hasKey(heartbeatKey);

                if (Boolean.FALSE.equals(isAlive)) {
                    log.warn("[StaleRouteCleanupTask] Node {} is detected as offline (heartbeat lost). Starting cleanup...", nodeAddress);

                    // 获取该宕机节点上所有的在线用户 ID
                    Set<Object> userIds = redisTemplate.opsForSet().members(nodeUsersKey);
                    if (userIds != null && !userIds.isEmpty()) {
                        log.info("[StaleRouteCleanupTask] Cleaning {} stale routes for dead node {}", userIds.size(), nodeAddress);
                        
                        // 管道化批量删除，降低 Redis 交互 RTT
                        redisTemplate.executePipelined(new org.springframework.data.redis.core.SessionCallback<Object>() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public <K, V> Object execute(org.springframework.data.redis.core.RedisOperations<K, V> operations) {
                                for (Object userId : userIds) {
                                    operations.delete((K) (IM_ROUTE_KEY_PREFIX + userId));
                                }
                                operations.delete((K) nodeUsersKey);
                                return null;
                            }
                        });
                    } else {
                        // 集合为空，直接删除该集合 Key
                        redisTemplate.delete(nodeUsersKey);
                    }
                    log.info("[StaleRouteCleanupTask] Stale routes cleanup completed for node {}", nodeAddress);
                }
            }
        } catch (Exception e) {
            log.error("[StaleRouteCleanupTask] Error occurred during stale routes cleanup", e);
        }
    }
}
