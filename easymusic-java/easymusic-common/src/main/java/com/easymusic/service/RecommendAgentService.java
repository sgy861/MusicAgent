package com.easymusic.service;

public interface RecommendAgentService {
    /**
     * 根据用户ID和当前初步输入（草稿），结合RAG召回相似歌曲生成个性化推荐风格
     *
     * @param userId       用户ID
     * @param currentInput 创作输入框当前的草稿内容
     * @return JSON 格式的推荐结果字符串
     */
    String generateRecommendation(String userId, String currentInput);
}
