package com.easymusic.service;

import com.easymusic.entity.po.UserPreferenceProfile;

public interface UserPreferenceService {

    /**
     * 更新用户偏好画像 (基于点赞行为、LLM 提炼、BGE 本地向量化)
     */
    void updateUserProfile(String userId);

    /**
     * 获取用户偏好画像
     */
    UserPreferenceProfile getUserProfile(String userId);
}
