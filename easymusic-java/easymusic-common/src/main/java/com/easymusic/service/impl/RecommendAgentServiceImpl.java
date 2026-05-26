package com.easymusic.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
        log.info("Generating stream recommendation for userId: {}, currentInput: '{}'", userId, currentInput);
        callback.onStart();
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

            // 3. 构建 Prompt 并调用 Kimi LLM Stream
            String systemPrompt = "你是一个专业的 AI 音乐生成风格参考推荐 Agent。\n" +
                    "请结合【用户的偏好特征】、【用户相似创作参考】以及【用户当前草稿】，为用户生成 3 个最符合其口味且富有创意的定制化音乐创作风格选项。\n" +
                    "注意：\n" +
                    "1. 你必须在回复的开头首先输出你的思考推理过程（思考当前用户草稿的意图、适合什么流派、人声搭配什么情绪最合适，以及相似创作能带来什么启发等）。思考过程必须使用 <think> 与 </think> 标签包裹。\n" +
                    "2. 在思考过程结束后，紧接着输出推荐的 JSON 结果。JSON 结果必须使用 <json> 与 </json> 标签包裹。\n" +
                    "3. 推荐结果要与用户的偏好高度相关。如果用户当前输入了部分提示词或歌词草稿，应优先基于其草稿进行风格拓展。\n" +
                    "4. suggestedSettings 的值必须符合以下规则：\n" +
                    "   - musicType: 0 (代表有人声歌曲), 1 (代表纯音乐)\n" +
                    "   - musicGener: 音乐流派标签 (如: 流行, 摇滚, 民谣, 爵士, 电子, 轻音乐 等)\n" +
                    "   - musicEmotion: 情感/氛围标签 (如: 轻快, 忧伤, 温馨, 动感, 宁静, 悲伤 等)\n" +
                    "   - musicSex: 人声类型 (如: 女声, 男声, 无)\n" +
                    "   - model: 默认为 \"chirp-v3-5\"\n" +
                    "\n" +
                    "请严格按照以下 JSON 格式输出到 <json>...</json> 标签中：\n" +
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
            String fullPrompt = systemPrompt + "\n" + userPrompt;

            RecommendationStreamParser parser = new RecommendationStreamParser(callback);

            log.info("Starting LLM stream generation...");
            streamingChatLanguageModel.generate(fullPrompt, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    parser.onToken(token);
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    String cleanedJson = parser.getJsonContent();
                    String finalResponseJson = null;
                    try {
                        if (cleanedJson != null && !cleanedJson.isEmpty()) {
                            JSONObject parsed = JSON.parseObject(cleanedJson);
                            JSONArray recommendations = parsed.getJSONArray("recommendations");
                            if (recommendations != null && !recommendations.isEmpty()) {
                                JSONObject finalResponse = new JSONObject();
                                finalResponse.put("type", "RECOMMEND_RESULT");
                                finalResponse.put("recommendations", recommendations);
                                finalResponseJson = finalResponse.toJSONString();
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse streamed JSON content, falling back. Cleaned JSON: {}", cleanedJson, e);
                    }

                    if (finalResponseJson == null) {
                        finalResponseJson = getFallbackRecommendationJson();
                    }

                    callback.onResult(finalResponseJson);
                }

                @Override
                public void onError(Throwable error) {
                    log.error("LLM streaming failed", error);
                    callback.onError(error);
                }
            });

        } catch (Exception e) {
            log.error("Failed to initiate stream recommendation", e);
            callback.onError(e);
        }
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
