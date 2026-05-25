package com.easymusic.netty;

import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.redis.RedisComponent;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class HandshakeAuthHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final RedisComponent redisComponent;

    public HandshakeAuthHandler(RedisComponent redisComponent) {
        this.redisComponent = redisComponent;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // 只拦截 WebSocket 握手请求
        if (!req.decoderResult().isSuccess() || !"websocket".equalsIgnoreCase(req.headers().get(HttpHeaderNames.UPGRADE))) {
            ctx.fireChannelRead(req.retain());
            return;
        }

        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        String path = decoder.path();

        if (!"/ws".equals(path)) {
            sendErrorResponse(ctx, HttpResponseStatus.BAD_REQUEST, "Invalid WebSocket Endpoint");
            return;
        }

        // 提取 Token 参数进行鉴权
        Map<String, List<String>> parameters = decoder.parameters();
        List<String> tokens = parameters.get("token");
        if (tokens == null || tokens.isEmpty()) {
            sendErrorResponse(ctx, HttpResponseStatus.UNAUTHORIZED, "Missing Token");
            return;
        }

        String token = tokens.get(0);
        TokenUserInfoDTO userInfo = redisComponent.getTokenUserInfoDto(token);
        if (userInfo == null) {
            sendErrorResponse(ctx, HttpResponseStatus.UNAUTHORIZED, "Invalid or Expired Token");
            return;
        }

        // 保存用户ID到 Channel 属性中，供后续 Handler 使用
        ctx.channel().attr(ChannelManager.USER_ID_KEY).set(userInfo.getUserId());

        // 重写 URI 为 /ws，去除 QueryString，从而使 Netty 自带的 WebSocketServerProtocolHandler 能够精确匹配路由进行握手升级
        req.setUri(path);

        log.info("WebSocket Handshake authenticated successfully. User: {}", userInfo.getUserId());

        // 传递给下一个 Handler (WebSocketServerProtocolHandler)
        ctx.fireChannelRead(req.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception in HandshakeAuthHandler", cause);
        ctx.close();
    }

    private void sendErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String message) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(message, CharsetUtil.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        log.warn("WebSocket Handshake authentication failed: {} - status: {}", message, status);
    }
}
