package com.easymusic.netty;

import com.easymusic.entity.po.ImMessage;
import com.easymusic.service.ImMessageService;
import com.easymusic.utils.JsonUtils;
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

    public WebSocketHandler(ChannelManager channelManager, ImMessageService imMessageService) {
        this.channelManager = channelManager;
        this.imMessageService = imMessageService;
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

    /**
     * WebSocket 请求实体定义
     */
    public static class WsRequest {
        private String action;
        private String receiverId;
        private String content;

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
    }
}
