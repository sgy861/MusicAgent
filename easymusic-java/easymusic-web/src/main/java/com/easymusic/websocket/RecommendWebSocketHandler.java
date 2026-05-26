package com.easymusic.websocket;

import com.alibaba.fastjson2.JSONObject;
import com.easymusic.service.RecommendationStreamCallback;
import com.easymusic.utils.JsonUtils;
import com.easymusic.service.RecommendAgentService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@Component
public class RecommendWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(RecommendWebSocketHandler.class);

    // Keep active sessions by userId
    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    // Keep request sequences to drop stale streaming tokens
    private static final Map<String, Long> REQUEST_SEQUENCES = new ConcurrentHashMap<>();

    @Resource
    @Lazy
    private RecommendAgentService recommendAgentService;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            SESSIONS.put(userId, session);
            log.info("WebSocket connection established for user: {}", userId);
            // Trigger initial recommendations upon connection
            triggerRecommendation(userId, "", session);
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        String payload = message.getPayload();
        try {
            WsRequest request = JsonUtils.convertJson2Obj(payload, WsRequest.class);
            if (request != null && "TRIGGER_RECOMMEND".equals(request.getAction())) {
                String currentInput = "";
                if (request.getData() != null && request.getData().getCurrentInput() != null) {
                    currentInput = request.getData().getCurrentInput();
                }
                triggerRecommendation(userId, currentInput, session);
            }
        } catch (Exception e) {
            log.error("Failed to process WebSocket text message", e);
        }
    }

    private void triggerRecommendation(String userId, String currentInput, WebSocketSession session) {
        // Increment and get request sequence ID for this user
        long currentSeq = REQUEST_SEQUENCES.compute(userId, (k, v) -> v == null ? 1L : v + 1);

        ForkJoinPool.commonPool().submit(() -> {
            try {
                if (!session.isOpen()) {
                    return;
                }

                recommendAgentService.generateRecommendationStream(userId, currentInput, new RecommendationStreamCallback() {
                    @Override
                    public void onStart() {
                        sendMessageSafely(session, userId, currentSeq, "{\"type\":\"RECOMMEND_START\"}");
                    }

                    @Override
                    public void onThink(String token) {
                        JSONObject msg = new JSONObject();
                        msg.put("type", "RECOMMEND_THINK");
                        msg.put("content", token);
                        sendMessageSafely(session, userId, currentSeq, msg.toJSONString());
                    }

                    @Override
                    public void onResult(String jsonResult) {
                        sendMessageSafely(session, userId, currentSeq, jsonResult);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        JSONObject msg = new JSONObject();
                        msg.put("type", "RECOMMEND_ERROR");
                        msg.put("content", throwable.getMessage());
                        sendMessageSafely(session, userId, currentSeq, msg.toJSONString());
                    }
                });

            } catch (Exception e) {
                log.error("Failed to execute stream recommendation for user: {}", userId, e);
                JSONObject msg = new JSONObject();
                msg.put("type", "RECOMMEND_ERROR");
                msg.put("content", e.getMessage());
                sendMessageSafely(session, userId, currentSeq, msg.toJSONString());
            }
        });
    }

    private void sendMessageSafely(WebSocketSession session, String userId, long seq, String message) {
        try {
            Long activeSeq = REQUEST_SEQUENCES.get(userId);
            if (activeSeq != null && activeSeq == seq && session.isOpen()) {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(message));
                    }
                }
            } else {
                log.debug("Discarding message for user {} due to sequence mismatch or closed session. Active seq: {}, msg seq: {}", userId, activeSeq, seq);
            }
        } catch (Exception e) {
            log.error("Failed to send message to user {} over websocket", userId, e);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            SESSIONS.remove(userId);
            REQUEST_SEQUENCES.remove(userId);
            log.info("WebSocket connection closed for user: {}", userId);
        }
    }

    // --- Inner classes for WebSocket frames ---

    public static class WsRequest {
        private String action;
        private WsRequestData data;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
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
