package com.easymusic.netty;

import com.easymusic.entity.po.ImMessage;
import com.easymusic.service.ImMessageService;
import com.easymusic.service.RecommendAgentService;
import com.easymusic.utils.JsonUtils;
import com.easymusic.spring.SpringContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final ChannelManager channelManager;
    private final ImMessageService imMessageService;
    private final RecommendAgentService recommendAgentService;
    
    // 记录连接最后发起推荐请求的序列号，解决高并发下的乱序覆盖问题
    private final java.util.Map<String, Long> requestSequences = new java.util.concurrent.ConcurrentHashMap<>();

    public WebSocketHandler(ChannelManager channelManager, ImMessageService imMessageService, RecommendAgentService recommendAgentService) {
        this.channelManager = channelManager;
        this.imMessageService = imMessageService;
        this.recommendAgentService = recommendAgentService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String payload = msg.text();
        String userId = ctx.channel().attr(ChannelManager.USER_ID_KEY).get();

        if (userId == null) {
            log.warn("Channel read from unauthenticated connection. Closing channel.");
            ctx.close();
            return;
        }

        try {
            WsRequest request = JsonUtils.convertJson2Obj(payload, WsRequest.class);
            if (request == null || request.getAction() == null) {
                sendError(ctx, "Invalid Request Format");
                return;
            }

            log.info("Received Action: {} from User: {}", request.getAction(), userId);

            switch (request.getAction().toUpperCase()) {
                case "PING":
                    ctx.writeAndFlush(new TextWebSocketFrame("{\"action\":\"PONG\"}"));
                    break;

                case "CHAT":
                    if (request.getReceiverId() == null || request.getContent() == null) {
                        sendError(ctx, "Missing receiverId or content for CHAT");
                        return;
                    }
                    ImMessage chatMsg = new ImMessage();
                    chatMsg.setSenderId(userId);
                    chatMsg.setReceiverId(request.getReceiverId());
                    chatMsg.setMsgType("CHAT");
                    chatMsg.setContent(request.getContent());
                    
                    // 通过 IM 消息路由服务发送消息
                    imMessageService.processSendMessage(chatMsg);
                    
                    // 回显给发送者表示发送成功
                    ctx.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(chatMsg)));
                    break;

                case "REVIEW":
                    if (request.getReceiverId() == null || request.getContent() == null) {
                        sendError(ctx, "Missing musicId (receiverId) or content for REVIEW");
                        return;
                    }
                    ImMessage reviewMsg = new ImMessage();
                    reviewMsg.setSenderId(userId);
                    reviewMsg.setReceiverId(request.getReceiverId());
                    reviewMsg.setMsgType("REVIEW");
                    reviewMsg.setContent(request.getContent());
                    
                    // 广播点评
                    imMessageService.processSendMessage(reviewMsg);
                    break;

                case "JOIN_ROOM":
                    if (request.getReceiverId() == null) {
                        sendError(ctx, "Missing musicId (receiverId) for JOIN_ROOM");
                        return;
                    }
                    channelManager.joinRoom(request.getReceiverId(), ctx.channel());
                    ctx.writeAndFlush(new TextWebSocketFrame("{\"action\":\"JOINED_ROOM\",\"receiverId\":\"" + request.getReceiverId() + "\"}"));
                    break;

                case "LEAVE_ROOM":
                    if (request.getReceiverId() == null) {
                        sendError(ctx, "Missing musicId (receiverId) for LEAVE_ROOM");
                        return;
                    }
                    channelManager.leaveRoom(request.getReceiverId(), ctx.channel());
                    ctx.writeAndFlush(new TextWebSocketFrame("{\"action\":\"LEFT_ROOM\",\"receiverId\":\"" + request.getReceiverId() + "\"}"));
                    break;

                case "TRIGGER_RECOMMEND":
                    String currentInput = "";
                    if (request.getData() != null && request.getData().getCurrentInput() != null) {
                        currentInput = request.getData().getCurrentInput();
                    }
                    triggerRecommendation(ctx, userId, currentInput);
                    break;

                default:
                    sendError(ctx, "Unsupported Action: " + request.getAction());
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to process WebSocket text message", e);
            sendError(ctx, "Server processing error");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            String userId = ctx.channel().attr(ChannelManager.USER_ID_KEY).get();
            if (userId != null) {
                // 1. 注册通道和Redis路由
                channelManager.registerUser(userId, ctx.channel());
                // 2. 触发离线消息推送
                imMessageService.pushOfflineMessages(userId);
            } else {
                log.warn("Handshake complete without userId. Closing channel.");
                ctx.close();
            }
        } else if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleEvent = (IdleStateEvent) evt;
            if (idleEvent.state() == io.netty.handler.timeout.IdleState.READER_IDLE) {
                // 60秒无心跳数据，剔除空闲连接
                log.info("Read idle timeout reached. Evicting idle connection.");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelManager.deregisterUser(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("WebSocket channel exception", cause);
        ctx.close();
    }

    private void sendError(ChannelHandlerContext ctx, String errMsg) {
        ctx.writeAndFlush(new TextWebSocketFrame("{\"action\":\"ERROR\",\"content\":\"" + errMsg + "\"}"));
    }

    private void triggerRecommendation(ChannelHandlerContext ctx, String userId, String currentInput) {
        long currentSeq = requestSequences.compute(userId, (k, v) -> v == null ? 1L : v + 1);
        io.netty.channel.Channel channel = ctx.channel();

        // 绑定最新的请求序列号到 Channel 属性中，供回传时做幂等/时效性校验
        channel.attr(ChannelManager.ACTIVE_SEQ_KEY).set(currentSeq);

        try {
            org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate =
                    (org.springframework.amqp.rabbit.core.RabbitTemplate) SpringContext.getBean("rabbitTemplate");

            com.easymusic.entity.dto.AiRecommendTaskDTO taskDto = new com.easymusic.entity.dto.AiRecommendTaskDTO();
            taskDto.setUserId(userId);
            taskDto.setCurrentInput(currentInput);
            taskDto.setNodeAddress(channelManager.getNodeAddress());
            taskDto.setRequestSeq(currentSeq);

            // 异步投递推荐任务到 MQ
            rabbitTemplate.convertAndSend(
                    com.easymusic.config.RabbitConfig.AI_RECOMMEND_TASK_QUEUE,
                    com.easymusic.utils.JsonUtils.convertObj2Json(taskDto)
            );
            log.info("[WebSocketHandler] Dispatched AI recommendation task to MQ for user {} (seq: {})", userId, currentSeq);

        } catch (Exception e) {
            log.error("[WebSocketHandler] Failed to dispatch AI recommendation task to MQ for user {}", userId, e);
            com.alibaba.fastjson2.JSONObject msg = new com.alibaba.fastjson2.JSONObject();
            msg.put("type", "RECOMMEND_ERROR");
            msg.put("content", "Dispatched recommendation failed: " + e.getMessage());
            ctx.writeAndFlush(new TextWebSocketFrame(msg.toJSONString()));
        }
    }

    private void sendMessageSafely(io.netty.channel.Channel channel, String userId, long seq, String message) {
        try {
            Long activeSeq = requestSequences.get(userId);
            if (activeSeq != null && activeSeq == seq && channel.isActive()) {
                channel.writeAndFlush(new TextWebSocketFrame(message));
            } else {
                log.debug("Discarding Netty message for user {} due to sequence mismatch or closed channel. Active seq: {}, msg seq: {}", userId, activeSeq, seq);
            }
        } catch (Exception e) {
            log.error("Failed to send Netty recommendation message to user {}", userId, e);
        }
    }

    /**
     * WebSocket 请求实体定义
     */
    public static class WsRequest {
        private String action;
        private String receiverId;
        private String content;
        private WsRequestData data;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(String receiverId) {
            this.receiverId = receiverId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public WsRequestData getData() {
            return data;
        }

        public void setData(WsRequestData data) {
            this.data = data;
        }
    }

    public static class WsRequestData {
        private String currentInput;

        public String getCurrentInput() {
            return currentInput;
        }

        public void setCurrentInput(String currentInput) {
            this.currentInput = currentInput;
        }
    }
}
