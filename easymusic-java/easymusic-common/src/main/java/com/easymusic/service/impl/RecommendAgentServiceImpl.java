package com.easymusic.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.easymusic.agent.ReActEngine;
import com.easymusic.entity.po.UserPreferenceProfile;
import com.easymusic.service.MilvusService;
import com.easymusic.service.MilvusService.MusicCreationSearchResult;
import com.easymusic.service.RecommendAgentService;
import com.easymusic.service.UserPreferenceService;
import com.easymusic.service.RecommendationStreamCallback;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("recommendAgentService")
public class RecommendAgentServiceImpl implements RecommendAgentService {

    private static final Logger log = LoggerFactory.getLogger(RecommendAgentServiceImpl.class);

    @Resource
    @Lazy
    private UserPreferenceService userPreferenceService;

    @Resource
    @Lazy
    private MilvusService milvusService;

    @Resource
    private ChatLanguageModel chatLanguageModel;

    @Resource
    private StreamingChatLanguageModel streamingChatLanguageModel;

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private ReActEngine reActEngine;

    @Override
    public String generateRecommendation(String userId, String currentInput) {
        log.info("Generating personalized recommendations for userId: {}, currentInput: '{}'", userId, currentInput);
        try {
            // 1. 获取用户偏好描述
            UserPreferenceProfile profile = userPreferenceService.getUserProfile(userId);
            String preferenceText = null;
            float[] vector = null;

            if (profile != null) {
                preferenceText = profile.getPreferenceText();
                vector = profile.getVectorAsFloatArray();
            }

            if (preferenceText == null || preferenceText.trim().isEmpty()) {
                preferenceText = "该用户目前还没有点赞记录，推荐流行乐、轻快舒缓的轻音乐风格，偏好中等速度和温馨的氛围。";
            }

            // 如果向量不存在（例如新用户或历史数据没有生成好），则对偏好描述进行向量化
            if (vector == null) {
                try {
                    log.info("Generating preference vector on-the-fly for userId: {}", userId);
                    vector = embeddingModel.embed(preferenceText).content().vector();
                } catch (Exception e) {
                    log.error("Failed to generate vector for preference text", e);
                }
            }

            // 2. RAG 从 Milvus 检索相似的用户优秀创作数据
            List<MusicCreationSearchResult> similarCreations = null;
            if (vector != null) {
                try {
                    log.info("Searching Milvus for top 3 similar creations...");
                    similarCreations = milvusService.searchSimilarCreations(vector, 3);
                } catch (Exception e) {
                    log.error("Failed to search similar creations from Milvus", e);
                }
            }

            // 3. 构建 Prompt 并调用 Kimi LLM
            String systemPrompt = "你是一个专业的 AI 音乐生成风格参考推荐 Agent。\n" +
                    "请结合【用户的偏好特征】、【用户相似创作参考】以及【用户当前草稿】，为用户生成 3 个最符合其口味且富有创意的定制化音乐创作风格选项。\n" +
                    "注意：\n" +
                    "1. 输出必须为合法的 JSON 格式，不要包含任何额外的自然语言解释、注释或包裹字符（除下面要求的 JSON 结构之外）。\n" +
                    "2. 推荐结果要与用户的偏好高度相关。如果用户当前输入了部分提示词或歌词草稿，应优先基于其草稿进行风格拓展。\n" +
                    "3. suggestedSettings 的值必须符合以下规则：\n" +
                    "   - musicType: 0 (代表有人声歌曲), 1 (代表纯音乐)\n" +
                    "   - musicGener: 音乐流派标签 (如: 流行, 摇滚, 民谣, 爵士, 电子, 轻音乐 等)\n" +
                    "   - musicEmotion: 情感/氛围标签 (如: 轻快, 忧伤, 温馨, 动感, 宁静, 悲伤 等)\n" +
                    "   - musicSex: 人声类型 (如: 女声, 男声, 无)\n" +
                    "   - model: 默认为 \"chirp-v3-5\"\n" +
                    "\n" +
                    "请严格按照以下 JSON 格式输出：\n" +
                    "{\n" +
                    "  \"recommendations\": [\n" +
                    "    {\n" +
                    "      \"title\": \"风格一名称\",\n" +
                    "      \"promptTags\": \"英文逗号分隔的风格提示词标签，如: pop, acoustic guitar, female vocals, slow\",\n" +
                    "      \"lyricTheme\": \"简短的歌词建议主题，如: 友情、夏日清晨、微风\",\n" +
                    "      \"suggestedSettings\": {\n" +
                    "        \"musicType\": 0,\n" +
                    "        \"musicGener\": \"流行\",\n" +
                    "        \"musicEmotion\": \"轻快\",\n" +
                    "        \"musicSex\": \"女声\",\n" +
                    "        \"model\": \"chirp-v3-5\"\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            StringBuilder sb = new StringBuilder();
            sb.append("【用户的偏好特征】：\n").append(preferenceText).append("\n\n");
            sb.append("【用户相似创作参考】：\n");
            if (similarCreations == null || similarCreations.isEmpty()) {
                sb.append("（暂无相似创作参考）\n");
            } else {
                int idx = 1;
                for (MusicCreationSearchResult res : similarCreations) {
                    sb.append("- 参考 ").append(idx++).append(":\n")
                            .append("  提示词: ").append(res.getPrompt()).append("\n")
                            .append("  配置: ").append(res.getSettings()).append("\n");
                }
            }
            sb.append("\n【用户当前草稿】：\n").append(currentInput == null || currentInput.trim().isEmpty() ? "（无）" : currentInput);

            String userPrompt = sb.toString();
            log.info("Calling Kimi chat model for recommendations...");
            String rawResponse = chatLanguageModel.generate(systemPrompt + "\n" + userPrompt);
            log.debug("Raw response from Kimi: {}", rawResponse);

            if (rawResponse != null) {
                String cleanJson = rawResponse.trim();
                if (cleanJson.startsWith("```")) {
                    cleanJson = cleanJson.replaceAll("```json|```", "").trim();
                }

                JSONObject parsed = JSON.parseObject(cleanJson);
                JSONArray recommendations = parsed.getJSONArray("recommendations");
                if (recommendations != null && !recommendations.isEmpty()) {
                    JSONObject finalResponse = new JSONObject();
                    finalResponse.put("type", "RECOMMEND_RESULT");
                    finalResponse.put("recommendations", recommendations);
                    return finalResponse.toJSONString();
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate personalized recommendation, using fallback", e);
        }
        return getFallbackRecommendationJson();
    }

    private String getFallbackRecommendationJson() {
        JSONObject response = new JSONObject();
        response.put("type", "RECOMMEND_RESULT");

        JSONArray arr = new JSONArray();

        JSONObject item1 = new JSONObject();
        item1.put("title", "清新流行乐");
        item1.put("promptTags", "pop, acoustic guitar, light drums, upbeat, female vocals");
        item1.put("lyricTheme", "青春、阳光、微风");
        JSONObject settings1 = new JSONObject();
        settings1.put("musicType", 0);
        settings1.put("musicGener", "流行");
        settings1.put("musicEmotion", "轻快");
        settings1.put("musicSex", "女声");
        settings1.put("model", "chirp-v3-5");
        item1.put("suggestedSettings", settings1);
        arr.add(item1);

        JSONObject item2 = new JSONObject();
        item2.put("title", "舒缓轻音乐");
        item2.put("promptTags", "ambient, piano, soft strings, relaxing, slow tempo, instrumental");
        item2.put("lyricTheme", "宁静、夜空、静思");
        JSONObject settings2 = new JSONObject();
        settings2.put("musicType", 1);
        settings2.put("musicGener", "轻音乐");
        settings2.put("musicEmotion", "温馨");
        settings2.put("musicSex", "无");
        settings2.put("model", "chirp-v3-5");
        item2.put("suggestedSettings", settings2);
        arr.add(item2);

        JSONObject item3 = new JSONObject();
        item3.put("title", "感性流行歌");
        item3.put("promptTags", "ballad, piano, warm electric guitar, emotional, warm male vocals");
        item3.put("lyricTheme", "思念、温暖、故事");
        JSONObject settings3 = new JSONObject();
        settings3.put("musicType", 0);
        settings3.put("musicGener", "流行");
        settings3.put("musicEmotion", "温馨");
        settings3.put("musicSex", "男声");
        settings3.put("model", "chirp-v3-5");
        item3.put("suggestedSettings", settings3);
        arr.add(item3);

        response.put("recommendations", arr);
        return response.toJSONString();
    }

    @Override
    public void generateRecommendationStream(String userId, String currentInput, RecommendationStreamCallback callback) {
        log.info("Generating stream recommendation (ReAct Agent mode) for userId: {}, currentInput: '{}'", userId, currentInput);
        callback.onStart();
        try {
            // ==== ReAct Agent 模式 ====
            // 与旧版 Pipeline 的核心区别：
            //   旧版：代码硬编码 "拉画像 → 向量化 → Milvus检索 → 拼Prompt → LLM"
            //   新版：LLM 自主决策是否调用 getUserPreference / searchSimilarMusic / checkQuota

            String systemPrompt = buildAgentSystemPrompt();
            String userPrompt = buildAgentUserPrompt(userId, currentInput);

            // 调用 ReAct 循环引擎
            String jsonResult = reActEngine.executeReActLoop(systemPrompt, userPrompt, userId, callback);

            // 解析 Agent 返回的 JSON 结果
            String finalResponseJson = null;
            if (jsonResult != null && !jsonResult.isEmpty()) {
                try {
                    JSONObject parsed = JSON.parseObject(jsonResult);
                    JSONArray recommendations = parsed.getJSONArray("recommendations");
                    if (recommendations != null && !recommendations.isEmpty()) {
                        JSONObject finalResponse = new JSONObject();
                        finalResponse.put("type", "RECOMMEND_RESULT");
                        finalResponse.put("recommendations", recommendations);
                        finalResponseJson = finalResponse.toJSONString();
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse ReAct JSON result, falling back. Raw JSON: {}", jsonResult, e);
                }
            }

            if (finalResponseJson == null) {
                log.warn("ReAct Agent failed to produce valid result, falling back to static pipeline");
                callback.onThink("\n⚠️ Agent 智能推理未产生有效结果，切换到静态推荐模式...\n");
                // Fallback: 退化到旧版静态 Pipeline
                finalResponseJson = generateRecommendationFallbackPipeline(userId, currentInput, callback);
            }

            callback.onResult(finalResponseJson);

        } catch (Exception e) {
            log.error("Failed to initiate stream recommendation", e);
            callback.onError(e);
        }
    }

    /**
     * 构建 Agent 模式的 System Prompt。
     * 与旧版的区别：不再包含硬编码的上下文数据，而是告诉 LLM 可以通过工具自主获取数据。
     */
    private String buildAgentSystemPrompt() {
        return "你是一个专业的 AI 音乐生成风格参考推荐 Agent，具备自主推理和工具调用能力。\n" +
                "\n" +
                "你的工作流程：\n" +
                "1. 首先使用 <think>...</think> 标签输出你的推理思考过程。\n" +
                "2. 在思考过程中，你可以主动调用工具来获取用户画像、检索相似创作、查询配额等信息。\n" +
                "3. 根据收集到的信息，为用户生成 3 个最符合其口味且富有创意的定制化音乐创作风格选项。\n" +
                "4. 最终推荐结果必须使用 <json>...</json> 标签包裹，格式如下：\n" +
                "{\n" +
                "  \"recommendations\": [\n" +
                "    {\n" +
                "      \"title\": \"风格名称\",\n" +
                "      \"promptTags\": \"英文逗号分隔的风格提示词标签，如: pop, acoustic guitar, female vocals\",\n" +
                "      \"lyricTheme\": \"简短的歌词建议主题\",\n" +
                "      \"suggestedSettings\": {\n" +
                "        \"musicType\": 0,\n" +
                "        \"musicGener\": \"流行\",\n" +
                "        \"musicEmotion\": \"轻快\",\n" +
                "        \"musicSex\": \"女声\",\n" +
                "        \"model\": \"chirp-v3-5\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "\n" +
                "suggestedSettings 规则：\n" +
                "- musicType: 0(有人声歌曲) 或 1(纯音乐)\n" +
                "- musicGener: 流行/摇滚/民谣/爵士/电子/轻音乐 等\n" +
                "- musicEmotion: 轻快/忧伤/温馨/动感/宁静/悲伤 等\n" +
                "- musicSex: 女声/男声/无\n" +
                "- model: 默认 \"chirp-v3-5\"";
    }

    /**
     * 构建 Agent 模式的 User Prompt。
     * 只提供 userId 和当前草稿，让 Agent 自主决定是否需要调用工具获取更多信息。
     */
    private String buildAgentUserPrompt(String userId, String currentInput) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户ID：").append(userId).append("\n");
        sb.append("用户当前草稿：").append(currentInput == null || currentInput.trim().isEmpty() ? "（无）" : currentInput).append("\n\n");
        sb.append("请先调用 getUserPreference 工具获取该用户的偏好画像，然后根据画像和草稿内容调用 searchSimilarMusic 检索相似创作参考，最后生成推荐。");
        return sb.toString();
    }

    /**
     * 静态 Pipeline 兜底推荐（旧版逻辑）。
     * 当 ReAct Agent 模式失败时退化到此路径，保证推荐功能可用。
     */
    private String generateRecommendationFallbackPipeline(String userId, String currentInput, RecommendationStreamCallback callback) {
        try {
            // 1. 获取用户偏好描述
            UserPreferenceProfile profile = userPreferenceService.getUserProfile(userId);
            String preferenceText = null;
            float[] vector = null;

            if (profile != null) {
                preferenceText = profile.getPreferenceText();
                vector = profile.getVectorAsFloatArray();
            }

            if (preferenceText == null || preferenceText.trim().isEmpty()) {
                preferenceText = "该用户目前还没有点赞记录，推荐流行乐、轻快舒缓的轻音乐风格，偏好中等速度和温馨的氛围。";
            }

            if (vector == null) {
                try {
                    vector = embeddingModel.embed(preferenceText).content().vector();
                } catch (Exception e) {
                    log.error("Fallback: Failed to generate vector", e);
                }
            }

            // 2. RAG 检索
            List<MusicCreationSearchResult> similarCreations = null;
            if (vector != null) {
                try {
                    similarCreations = milvusService.searchSimilarCreations(vector, 3);
                } catch (Exception e) {
                    log.error("Fallback: Failed to search Milvus", e);
                }
            }

            // 3. 构建 Prompt
            StringBuilder sb = new StringBuilder();
            sb.append("【用户的偏好特征】：\n").append(preferenceText).append("\n\n");
            sb.append("【用户相似创作参考】：\n");
            if (similarCreations == null || similarCreations.isEmpty()) {
                sb.append("（暂无相似创作参考）\n");
            } else {
                int idx = 1;
                for (MusicCreationSearchResult res : similarCreations) {
                    sb.append("- 参考 ").append(idx++).append(":\n")
                            .append("  提示词: ").append(res.getPrompt()).append("\n")
                            .append("  配置: ").append(res.getSettings()).append("\n");
                }
            }
            sb.append("\n【用户当前草稿】：\n").append(currentInput == null || currentInput.trim().isEmpty() ? "（无）" : currentInput);

            String rawResponse = chatLanguageModel.generate(buildAgentSystemPrompt() + "\n" + sb.toString());
            if (rawResponse != null) {
                String cleanJson = rawResponse.trim();
                if (cleanJson.contains("<json>")) {
                    int start = cleanJson.indexOf("<json>") + 6;
                    int end = cleanJson.contains("</json>") ? cleanJson.indexOf("</json>") : cleanJson.length();
                    cleanJson = cleanJson.substring(start, end).trim();
                }
                if (cleanJson.startsWith("```")) {
                    cleanJson = cleanJson.replaceAll("```json|```", "").trim();
                }
                JSONObject parsed = JSON.parseObject(cleanJson);
                JSONArray recommendations = parsed.getJSONArray("recommendations");
                if (recommendations != null && !recommendations.isEmpty()) {
                    JSONObject finalResponse = new JSONObject();
                    finalResponse.put("type", "RECOMMEND_RESULT");
                    finalResponse.put("recommendations", recommendations);
                    return finalResponse.toJSONString();
                }
            }
        } catch (Exception e) {
            log.error("Fallback pipeline also failed", e);
        }
        return getFallbackRecommendationJson();
    }

    private static class RecommendationStreamParser {
        private enum State { INIT, THINKING, JSON_ACCUMULATING }
        private State state = State.INIT;
        private final StringBuilder buffer = new StringBuilder();
        private final StringBuilder jsonBuffer = new StringBuilder();
        private final RecommendationStreamCallback callback;

        public RecommendationStreamParser(RecommendationStreamCallback callback) {
            this.callback = callback;
        }

        public void onToken(String token) {
            buffer.append(token);
            String currentText = buffer.toString();

            if (state == State.INIT) {
                if (currentText.contains("<think>")) {
                    state = State.THINKING;
                    int index = currentText.indexOf("<think>");
                    String content = currentText.substring(index + 7);
                    if (!content.isEmpty()) {
                        callback.onThink(content);
                    }
                    buffer.setLength(0);
                } else if (currentText.length() > 50 && !currentText.contains("<think>")) {
                    // Fallback if model fails to output <think>
                    state = State.THINKING;
                    callback.onThink(currentText);
                    buffer.setLength(0);
                }
            } else if (state == State.THINKING) {
                if (currentText.contains("</think>")) {
                    state = State.JSON_ACCUMULATING;
                    int index = currentText.indexOf("</think>");
                    String content = currentText.substring(0, index);
                    if (!content.isEmpty()) {
                        callback.onThink(content);
                    }
                    String rest = currentText.substring(index + 8);
                    jsonBuffer.append(rest);
                    buffer.setLength(0);
                } else {
                    callback.onThink(token);
                    buffer.setLength(0);
                }
            } else if (state == State.JSON_ACCUMULATING) {
                jsonBuffer.append(token);
            }
        }

        public String getJsonContent() {
            String result = jsonBuffer.toString().trim();
            if (result.contains("<json>")) {
                int start = result.indexOf("<json>") + 6;
                int end = result.contains("</json>") ? result.indexOf("</json>") : result.length();
                if (end > start) {
                    return result.substring(start, end).trim();
                }
            }
            if (result.startsWith("```")) {
                result = result.replaceAll("(?s)^```(?:json)?|```$", "").trim();
            }
            return result;
        }
    }
}
