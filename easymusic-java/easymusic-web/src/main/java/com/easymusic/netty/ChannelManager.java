package com.easymusic.netty;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ChannelManager {

    public static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("userId");
    public static final AttributeKey<Long> ACTIVE_SEQ_KEY = AttributeKey.valueOf("activeSeq");

    private final Map<String, Channel> userChannels = new ConcurrentHashMap<>();
    private final Map<String, ChannelGroup> roomChannels = new ConcurrentHashMap<>();

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private String nodeAddress;

    public static final String IM_HEARTBEAT_PREFIX = "im:heartbeat:";
    private static final String IM_ROUTE_KEY_PREFIX = "im:route:";
    private static final String IM_NODE_USERS_PREFIX = "im:node:users:";

    public void renewHeartbeat() {
        if (nodeAddress != null) {
            redisTemplate.opsForValue().set(
                IM_HEARTBEAT_PREFIX + nodeAddress, "1", 15, TimeUnit.SECONDS);
        }
    }

    public void initNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
        log.info("ChannelManager initialized with nodeAddress: {}", nodeAddress);
        renewHeartbeat(); // 注册时立即写入一次心跳
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    /**
     * 注册用户通道与路由表
     */
    public void registerUser(String userId, Channel channel) {
        // 绑定属性到Channel
        channel.attr(USER_ID_KEY).set(userId);
        userChannels.put(userId, channel);

        if (nodeAddress != null) {
            String routeKey = IM_ROUTE_KEY_PREFIX + userId;
            String nodeUsersKey = IM_NODE_USERS_PREFIX + nodeAddress;

            // 设置路由（有效期12小时，用于安全兜底）
            redisTemplate.opsForValue().set(routeKey, nodeAddress, 12, TimeUnit.HOURS);
            // 将用户ID存入节点在线用户集合
            redisTemplate.opsForSet().add(nodeUsersKey, userId);

            log.info("User {} connected to node {}. Registered route in Redis.", userId, nodeAddress);
        } else {
            log.warn("Node address not initialized, user route not registered in Redis.");
        }
    }

    /**
     * 注销用户通道与路由表
     */
    public void deregisterUser(Channel channel) {
        String userId = channel.attr(USER_ID_KEY).get();
        if (userId == null) {
            return;
        }

        // 仅在当前Channel为当前活跃连接时才注销，防止重复连接覆盖后，前一个连接断开误删新路由
        Channel activeChannel = userChannels.get(userId);
        if (activeChannel == channel) {
            userChannels.remove(userId);
            if (nodeAddress != null) {
                String routeKey = IM_ROUTE_KEY_PREFIX + userId;
                String nodeUsersKey = IM_NODE_USERS_PREFIX + nodeAddress;

                redisTemplate.delete(routeKey);
                redisTemplate.opsForSet().remove(nodeUsersKey, userId);
                log.info("User {} disconnected from node {}. Deregistered route in Redis.", userId, nodeAddress);
            }
        }

        // 从所有的Room ChannelGroup中移除该Channel
        for (ChannelGroup group : roomChannels.values()) {
            group.remove(channel);
        }
    }

    /**
     * 获取在线用户的Channel
     */
    public Channel getUserChannel(String userId) {
        return userChannels.get(userId);
    }

    /**
     * 加入点评房间 (musicId)
     */
    public void joinRoom(String musicId, Channel channel) {
        ChannelGroup group = roomChannels.computeIfAbsent(musicId, 
            k -> new DefaultChannelGroup(GlobalEventExecutor.INSTANCE));
        group.add(channel);
        log.info("Channel for user {} joined room {}", channel.attr(USER_ID_KEY).get(), musicId);
    }

    /**
     * 离开点评房间 (musicId)
     */
    public void leaveRoom(String musicId, Channel channel) {
        ChannelGroup group = roomChannels.get(musicId);
        if (group != null) {
            group.remove(channel);
            log.info("Channel for user {} left room {}", channel.attr(USER_ID_KEY).get(), musicId);
            if (group.isEmpty()) {
                roomChannels.remove(musicId);
            }
        }
    }

    /**
     * 获取点评房间的所有ChannelGroup
     */
    public ChannelGroup getRoomGroup(String musicId) {
        return roomChannels.get(musicId);
    }

    /**
     * 清理本节点所有Redis在线用户缓存（用于宕机或重启后的自愈）
     */
    public void cleanNodeRoutes() {
        if (nodeAddress == null) {
            return;
        }
        String nodeUsersKey = IM_NODE_USERS_PREFIX + nodeAddress;
        try {
            Long size = redisTemplate.opsForSet().size(nodeUsersKey);
            if (size != null && size > 0) {
                log.info("Cleaning up {} stale user routes for node {} from Redis.", size, nodeAddress);
                while (true) {
                    Object userIdObj = redisTemplate.opsForSet().pop(nodeUsersKey);
                    if (userIdObj == null) {
                        break;
                    }
                    String userId = (String) userIdObj;
                    redisTemplate.delete(IM_ROUTE_KEY_PREFIX + userId);
                }
            }
            redisTemplate.delete(nodeUsersKey);
            log.info("Stale routes cleanup for node {} completed.", nodeAddress);
        } catch (Exception e) {
            log.error("Failed to clean node routes from Redis", e);
        }
    }
}
