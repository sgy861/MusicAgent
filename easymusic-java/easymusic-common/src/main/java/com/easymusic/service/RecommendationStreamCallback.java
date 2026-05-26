package com.easymusic.service;

public interface RecommendationStreamCallback {
    void onStart();
    void onThink(String token);
    void onResult(String jsonResult);
    void onError(Throwable throwable);
}
