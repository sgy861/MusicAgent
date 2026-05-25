package com.easymusic.service.impl;

import com.easymusic.entity.po.MusicCreation;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.po.MusicInfoAction;
import com.easymusic.entity.po.UserPreferenceProfile;
import com.easymusic.entity.query.MusicCreationQuery;
import com.easymusic.entity.query.MusicInfoActionQuery;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.mappers.MusicCreationMapper;
import com.easymusic.mappers.MusicInfoActionMapper;
import com.easymusic.mappers.MusicInfoMapper;
import com.easymusic.mappers.UserPreferenceProfileMapper;
import com.easymusic.service.UserPreferenceService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("userPreferenceService")
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private static final Logger log = LoggerFactory.getLogger(UserPreferenceServiceImpl.class);

    @Resource
    @Lazy
    private UserPreferenceProfileMapper<UserPreferenceProfile, Object> profileMapper;

    @Resource
    @Lazy
    private MusicInfoActionMapper<MusicInfoAction, MusicInfoActionQuery> musicInfoActionMapper;

    @Resource
    @Lazy
    private MusicInfoMapper<MusicInfo, MusicInfoQuery> musicInfoMapper;

    @Resource
    @Lazy
    private MusicCreationMapper<MusicCreation, MusicCreationQuery> musicCreationMapper;

    @Resource
    private ChatLanguageModel chatLanguageModel;

    @Resource
    private EmbeddingModel embeddingModel;

    @Override
    public void updateUserProfile(String userId) {
        log.info("Starting user preference profile update for userId: {}", userId);
        try {
            // 1. Get existing profile to check if we can do incremental update
            UserPreferenceProfile existingProfile = null;
            try {
                existingProfile = profileMapper.selectByUserId(userId);
            } catch (Exception e) {
                log.warn("No existing profile found or failed to query for userId: {}", userId);
            }

            // 2. Query recently liked actions by the user
            MusicInfoActionQuery actionQuery = new MusicInfoActionQuery();
            actionQuery.setUserId(userId);
            actionQuery.setActionType(1); // 1: Good / Like
            actionQuery.setOrderBy("action_id desc");
            actionQuery.setSimplePage(new com.easymusic.entity.query.SimplePage(0, 20));
            List<MusicInfoAction> actions = musicInfoActionMapper.selectList(actionQuery);

            String preferenceText = null;
            Integer lastActionId = 0;
            boolean needsUpdate = false;
            List<MusicInfoAction> targetActions = null;

            if (actions == null || actions.isEmpty()) {
                log.info("No liked actions found for userId: {}.", userId);
                if (existingProfile == null) {
                    preferenceText = "该用户目前还没有点赞记录，推荐流行乐、轻快舒缓的轻音乐风格，偏好中等速度和温馨的氛围。";
                    lastActionId = 0;
                    needsUpdate = true;
                } else {
                    log.info("Profile already exists for userId: {} and no likes. Skipping.", userId);
                    return;
                }
            } else {
                int maxActionId = actions.get(0).getActionId();

                if (existingProfile == null) {
                    // Initial generation: use up to the latest 5 liked creations as the seed
                    log.info("No existing profile for userId: {}. Performing initial generation.", userId);
                    targetActions = actions.subList(0, Math.min(actions.size(), 5));
                    needsUpdate = true;
                    lastActionId = maxActionId;
                } else if (existingProfile.getLastActionId() == null || existingProfile.getLastActionId() == 0) {
                    // Profile exists but lastActionId is not initialized (migration step)
                    if (existingProfile.getPreferenceText() != null && !existingProfile.getPreferenceText().trim().isEmpty() 
                            && !existingProfile.getPreferenceText().contains("该用户目前还没有点赞记录")) {
                        // The existing profile is valid, so we assume all current actions are already processed.
                        // We initialize the lastActionId directly without calling LLM.
                        existingProfile.setLastActionId(maxActionId);
                        existingProfile.setUpdateTime(new Date());
                        profileMapper.insertOrUpdate(existingProfile);
                        log.info("Initialized lastActionId to {} for existing profile of userId: {} (no LLM call)", maxActionId, userId);
                        return;
                    } else {
                        // Profile is empty or placeholder. Perform initial generation.
                        log.info("Placeholder profile for userId: {}. Performing initial generation.", userId);
                        targetActions = actions.subList(0, Math.min(actions.size(), 5));
                        needsUpdate = true;
                        lastActionId = maxActionId;
                    }
                } else {
                    // Profile exists and lastActionId is valid. Filter for new likes (incremental delta).
                    int prevLastActionId = existingProfile.getLastActionId();
                    targetActions = actions.stream()
                            .filter(a -> a.getActionId() > prevLastActionId)
                            .collect(Collectors.toList());

                    if (targetActions.isEmpty()) {
                        log.info("No new liked actions found since lastActionId: {} for userId: {}. Skipping.", prevLastActionId, userId);
                        return;
                    }

                    log.info("Found {} new liked actions for incremental update for userId: {}", targetActions.size(), userId);
                    needsUpdate = true;
                    lastActionId = maxActionId;
                }
            }

            if (needsUpdate) {
                if (targetActions != null && !targetActions.isEmpty()) {
                    // Compile music details for the target actions
                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    for (MusicInfoAction action : targetActions) {
                        MusicInfo musicInfo = musicInfoMapper.selectByMusicId(action.getMusicId());
                        if (musicInfo != null && musicInfo.getCreationId() != null) {
                            MusicCreation creation = musicCreationMapper.selectByCreationId(musicInfo.getCreationId());
                            if (creation != null && creation.getPrompt() != null) {
                                count++;
                                sb.append("- 音乐").append(count).append(": 标题: ")
                                        .append(musicInfo.getMusicTitle())
                                        .append(" | 提示词: ").append(creation.getPrompt())
                                        .append(" | 类型: ").append(creation.getMusicType() == 1 ? "纯音乐" : "歌曲")
                                        .append("\n");
                            }
                        }
                    }

                    if (count == 0) {
                        log.info("No corresponding creation prompts found for new liked actions of userId: {}.", userId);
                        if (existingProfile != null) {
                            // If profile exists, keep it as is but update the lastActionId to skip these in future
                            existingProfile.setLastActionId(lastActionId);
                            existingProfile.setUpdateTime(new Date());
                            profileMapper.insertOrUpdate(existingProfile);
                            log.info("Updated lastActionId to {} for userId: {} (no prompts found)", lastActionId, userId);
                            return;
                        } else {
                            preferenceText = "该用户目前还没有点赞记录，推荐流行乐、轻快舒缓的轻音乐风格，偏好中等速度和温馨的氛围。";
                            lastActionId = 0; // reset to 0 so next attempt will try again
                        }
                    } else {
                        String systemPrompt;
                        String userPrompt;

                        if (existingProfile != null && existingProfile.getPreferenceText() != null 
                                && !existingProfile.getPreferenceText().contains("该用户目前还没有点赞记录")) {
                            // Incremental merge prompt
                            systemPrompt = "你是一个专业的音乐偏好分析专家。你将收到用户【原有的偏好画像描述】以及【新增的点赞歌曲特征】。\n" +
                                    "你的任务是把这些新增的点赞歌曲风格特征巧妙地融合到原有的偏好画像中，生成一段全新的、融合后的用户偏好描述。\n" +
                                    "要求：\n" +
                                    "1. 对画像进行微调和增量修改，既要保留原有偏好的核心特征，也要体现出新点赞曲风的引入。如果新曲风与原有画像不同，应合理地描述用户音乐品味的多样化，而不是完全重写或完全覆盖原有的核心喜好。\n" +
                                    "2. 回答必须非常精简，字数控制在150字以内。\n" +
                                    "3. 直接输出融合后的偏好描述段落，不要包含任何前言、总结或“已更新”等解释性废话。";

                            userPrompt = "【原有的偏好画像描述】：\n" + existingProfile.getPreferenceText() + 
                                    "\n\n【新增的点赞歌曲特征】：\n" + sb.toString();
                        } else {
                            // Initial profile generation prompt
                            systemPrompt = "你是一个专业的音乐偏好分析专家。请根据提供的用户点赞歌曲及其生成提示词列表，精炼地分析并总结该用户的核心音乐喜好特点（偏好哪种流派、乐器、节奏、歌词主题等）。\n" +
                                    "注意：回答必须非常精简，字数控制在150字以内，直接输出偏好描述段落，不要包含任何前言或总结性字眼。";

                            userPrompt = "用户点赞的歌曲数据如下：\n" + sb.toString();
                        }

                        log.info("Calling Kimi for preference profile updates for userId: {}...", userId);
                        String rawSummary = chatLanguageModel.generate(systemPrompt + "\n" + userPrompt);
                        preferenceText = rawSummary != null ? rawSummary.trim() : "";
                        log.info("Kimi response for userId {}: {}", userId, preferenceText);
                    }
                }

                if (preferenceText != null && !preferenceText.isEmpty()) {
                    // 5. Vectorize text with BGE small Chinese model (dimension = 512)
                    log.info("Vectorizing preference text for userId: {}...", userId);
                    Embedding embedding = embeddingModel.embed(preferenceText).content();
                    float[] vector = embedding.vector();

                    // 6. Store / Upsert profile in MySQL
                    UserPreferenceProfile profile = new UserPreferenceProfile();
                    profile.setUserId(userId);
                    profile.setPreferenceText(preferenceText);
                    profile.setVectorFromFloatArray(vector);
                    profile.setLastActionId(lastActionId);
                    profile.setLastBehaviorTime(new Date());
                    profile.setUpdateTime(new Date());

                    profileMapper.insertOrUpdate(profile);
                    log.info("User preference profile updated successfully in MySQL for userId: {} with lastActionId: {}", userId, lastActionId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to update user preference profile for userId: {}", userId, e);
        }
    }

    @Override
    public UserPreferenceProfile getUserProfile(String userId) {
        try {
            return profileMapper.selectByUserId(userId);
        } catch (Exception e) {
            log.error("Failed to select user preference profile for userId: {}", userId, e);
            return null;
        }
    }
}
