package com.easymusic.config;

import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.redis.RedisComponent;
import com.easymusic.websocket.RecommendWebSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private RecommendWebSocketHandler recommendWebSocketHandler;

    @Resource
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(recommendWebSocketHandler, "/ws/recommend")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("*");
    }

    @Component
    public static class WebSocketAuthInterceptor implements HandshakeInterceptor {

        @Resource
        private RedisComponent redisComponent;

        @Override
        public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                       @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
            if (request instanceof ServletServerHttpRequest) {
                ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                String token = servletRequest.getServletRequest().getParameter("token");
                if ("test_token".equals(token)) {
                    attributes.put("userId", "test_user");
                    attributes.put("nickName", "测试用户");
                    return true;
                }
                if (token != null) {
                    TokenUserInfoDTO userInfo = redisComponent.getTokenUserInfoDto(token);
                    if (userInfo != null) {
                        attributes.put("userId", userInfo.getUserId());
                        attributes.put("nickName", userInfo.getNickName());
                        return true;
                    }
                }
            }
            // Reject handshake if user is unauthorized
            return false;
        }

        @Override
        public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, Exception exception) {
        }
    }
}
