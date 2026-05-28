package com.easymusic.agent;

import com.alibaba.fastjson2.JSONObject;
import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.po.UserInfo;
import com.easymusic.entity.po.UserPreferenceProfile;
import com.easymusic.entity.query.UserInfoQuery;
import com.easymusic.mappers.UserInfoMapper;
import com.easymusic.service.MilvusService;
import com.easymusic.service.MilvusService.MusicCreationSearchResult;
import com.easymusic.service.UserPreferenceService;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Agent 工具实现类。
 * 包含所有可被 ReAct Agent 通过 Function Calling 动态调用的后端能力。
 *
 * <p>每个工具方法需满足：
 * <ul>
 *   <li>被 {@link AgentTool} 注解标记</li>
 *   <li>参数类型为基本类型或 String（便于 JSON 反序列化）</li>
 *   <li>返回值为 String 或可 JSON 序列化的对象</li>
 * </ul>
 */
@Component
@Slf4j
public class AgentToolProvider {

    @Resource
    @Lazy
    private UserPreferenceService userPreferenceService;

    @Resource
    @Lazy
    private MilvusService milvusService;

    @Resource
    @Lazy
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 查询用户当前剩余音乐生成配额。
     * Agent 可在用户询问"我还能生成几首歌"或决定是否推荐付费模型时调用此工具。
     *
     * @param userId 用户ID
     * @return 包含 available（可用配额）字段的 JSON 字符串
     */
    @AgentTool(
            name = "checkQuota",
            description = "查询用户当前剩余的音乐生成配额（积分）。当用户询问额度、或你需要判断是否推荐高积分消耗模型时，调用此工具。"
    )
    public String checkQuota(String userId) {
        log.info("[AgentTool:checkQuota] Checking quota for userId: {}", userId);
        try {
            // 先尝试从 Redis 缓存读取
            String redisKey = Constants.REDIS_KEY_USER_QUOTA + userId;
            Object cached = redisTemplate.opsForValue().get(redisKey);
            if (cached != null) {
                int available = Integer.parseInt(cached.toString());
                JSONObject result = new JSONObject();
                result.put("userId", userId);
                result.put("available", available);
                result.put("source", "cache");
                return result.toJSONString();
            }

            // 缓存未命中，查询数据库
            UserInfo userInfo = userInfoMapper.selectByUserId(userId);
            if (userInfo == null) {
                return "{\"error\": \"用户不存在\", \"userId\": \"" + userId + "\"}";
            }
            int integral = userInfo.getIntegral();
            JSONObject result = new JSONObject();
            result.put("userId", userId);
            result.put("available", integral);
            result.put("source", "database");
            return result.toJSONString();
        } catch (Exception e) {
            log.error("[AgentTool:checkQuota] Failed for userId: {}", userId, e);
            return "{\"error\": \"查询配额失败: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 根据风格描述在 Milvus 向量库中检索相似的优秀创作。
     * Agent 可在需要参考已有创作进行推荐时调用此工具。
     *
     * @param query 自然语言的风格描述（如"动感电子舞曲"、"轻柔钢琴曲"）
     * @param topK  返回最相似的前 K 条结果
     * @return 包含相似创作列表的 JSON 字符串
     */
    @AgentTool(
            name = "searchSimilarMusic",
            description = "根据音乐风格描述，在向量数据库中检索最相似的历史优秀创作。当你需要参考已有创作来生成推荐时调用。参数: query(风格描述), topK(返回条数,默认3)"
    )
    public String searchSimilarMusic(String query, int topK) {
        log.info("[AgentTool:searchSimilarMusic] query='{}', topK={}", query, topK);
        try {
            if (topK <= 0 || topK > 10) {
                topK = 3;
            }

            // 将文本描述向量化
            float[] vector = embeddingModel.embed(query).content().vector();

            // 在 Milvus 中检索
            List<MusicCreationSearchResult> results = milvusService.searchSimilarCreations(vector, topK);

            if (results == null || results.isEmpty()) {
                return "{\"results\": [], \"message\": \"未找到相似创作\"}";
            }

            List<Map<String, Object>> resultList = new ArrayList<>();
            for (MusicCreationSearchResult r : results) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("creationId", r.getCreationId());
                item.put("prompt", r.getPrompt());
                item.put("settings", r.getSettings());
                item.put("score", r.getScore());
                resultList.add(item);
            }

            JSONObject response = new JSONObject();
            response.put("results", resultList);
            response.put("totalFound", results.size());
            return response.toJSONString();
        } catch (Exception e) {
            log.error("[AgentTool:searchSimilarMusic] Failed for query: {}", query, e);
            return "{\"error\": \"向量检索失败: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 获取用户的音乐偏好画像描述。
     * Agent 可在需要了解用户口味偏好时调用此工具。
     *
     * @param userId 用户ID
     * @return 包含用户偏好描述的 JSON 字符串
     */
    @AgentTool(
            name = "getUserPreference",
            description = "获取用户的音乐偏好画像描述。当你需要了解用户的听歌口味、偏好流派、情绪倾向时调用此工具。"
    )
    public String getUserPreference(String userId) {
        log.info("[AgentTool:getUserPreference] Getting preference for userId: {}", userId);
        try {
            UserPreferenceProfile profile = userPreferenceService.getUserProfile(userId);
            JSONObject result = new JSONObject();
            result.put("userId", userId);

            if (profile != null && profile.getPreferenceText() != null
                    && !profile.getPreferenceText().trim().isEmpty()
                    && !profile.getPreferenceText().contains("该用户目前还没有点赞记录")) {
                result.put("hasProfile", true);
                result.put("preferenceText", profile.getPreferenceText());
                result.put("lastUpdateTime", profile.getUpdateTime() != null ? profile.getUpdateTime().toString() : "unknown");
            } else {
                result.put("hasProfile", false);
                result.put("preferenceText", "该用户目前还没有足够的行为数据生成个性化画像。建议推荐流行乐、轻快舒缓的轻音乐风格。");
            }

            return result.toJSONString();
        } catch (Exception e) {
            log.error("[AgentTool:getUserPreference] Failed for userId: {}", userId, e);
            return "{\"error\": \"获取用户画像失败: " + e.getMessage() + "\"}";
        }
    }
}
