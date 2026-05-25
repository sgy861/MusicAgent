package com.easymusic.service;

import java.util.List;

public interface MilvusService {

    /**
     * 初始化集合和索引，并加载集合
     */
    void initCollection();

    /**
     * 将 MySQL 中未同步的历史创作同步到 Milvus 中
     */
    void syncExistingCreations();

    /**
     * 插入单条创作向量数据
     */
    void insertCreation(String creationId, String userId, String prompt, String settings, float[] vector);

    /**
     * 搜索最相似的创作
     */
    List<MusicCreationSearchResult> searchSimilarCreations(float[] vector, int topK);

    /**
     * 相似度检索返回结构
     */
    class MusicCreationSearchResult {
        private String creationId;
        private String userId;
        private String prompt;
        private String settings;
        private Float score;

        public String getCreationId() {
            return creationId;
        }

        public void setCreationId(String creationId) {
            this.creationId = creationId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getSettings() {
            return settings;
        }

        public void setSettings(String settings) {
            this.settings = settings;
        }

        public Float getScore() {
            return score;
        }

        public void setScore(Float score) {
            this.score = score;
        }
    }
}
