package com.easymusic.agent;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.easymusic.service.RecommendationStreamCallback;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ReAct（Reasoning + Acting）循环引擎。
 *
 * <p>核心工作流程：
 * <ol>
 *   <li>将用户输入 + 工具清单 + System Prompt 组装为初始消息</li>
 *   <li>调用 LLM（流式），解析输出中是否包含 tool_call 指令</li>
 *   <li>若包含工具调用 → 执行工具 → 将结果追加到对话上下文 → 再次调用 LLM</li>
 *   <li>若不包含工具调用 → 解析最终推荐结果（<json>...</json> 标签）</li>
 *   <li>设置最大循环次数（MAX_ITERATIONS = 5）防止无限循环</li>
 * </ol>
 *
 * <p>与传统 Pipeline 的区别：
 * <ul>
 *   <li>Pipeline：代码硬编码调用顺序 → 拉画像 → 向量化 → 检索 → 拼 Prompt → LLM</li>
 *   <li>ReAct：LLM 自主决策是否调用工具、调用哪个工具、调用几次</li>
 * </ul>
 */
@Component
@Slf4j
public class ReActEngine {

    private static final int MAX_ITERATIONS = 5;
    private static final long LLM_TIMEOUT_SECONDS = 120;

    /**
     * 匹配 LLM 输出中的工具调用块：```tool_call\n{...}\n```
     */
    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile(
            "```tool_call\\s*\\n(\\{.*?})\\s*\\n```", Pattern.DOTALL);

    /**
     * 匹配最终 JSON 结果块：<json>...</json>
     */
    private static final Pattern JSON_RESULT_PATTERN = Pattern.compile(
            "<json>(.*?)</json>", Pattern.DOTALL);

    @Resource
    private AgentToolRegistry toolRegistry;

    @Resource
    private StreamingChatLanguageModel streamingChatLanguageModel;

    /**
     * 执行 ReAct 循环，支持流式输出。
     *
     * @param systemPrompt 系统提示词（包含工具清单和输出格式要求）
     * @param userPrompt   用户输入（包含用户当前草稿等上下文）
     * @param userId       当前活跃用户ID，用于越权安全拦截校验
     * @param callback     流式回调（CoT 思考链 + 最终结果）
     * @return 最终推荐结果的 JSON 字符串，若失败返回 null
     */
    public String executeReActLoop(String systemPrompt, String userPrompt, String userId, RecommendationStreamCallback callback) {
        log.info("[ReActEngine] Starting ReAct loop...");

        // 构建完整的 Prompt，包含工具清单
        String toolsPrompt = toolRegistry.generateToolsPrompt();
        String fullSystemPrompt = systemPrompt + "\n\n" + toolsPrompt;

        // 维护对话历史，用于多轮工具调用时的上下文累积
        StringBuilder conversationHistory = new StringBuilder();
        conversationHistory.append(fullSystemPrompt).append("\n").append(userPrompt);

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            log.info("[ReActEngine] Iteration {}/{}", iteration + 1, MAX_ITERATIONS);

            // 通知前端当前 Agent 正在思考的阶段
            if (iteration > 0) {
                callback.onThink("\n\n🔄 [Agent 第 " + (iteration + 1) + " 轮推理]\n");
            }

            // 调用 LLM（同步等待完整响应，内部仍然流式推送 CoT token）
            String llmResponse = callLlmAndStreamThinking(conversationHistory.toString(), callback);

            if (llmResponse == null) {
                log.error("[ReActEngine] LLM returned null response at iteration {}", iteration + 1);
                return null;
            }

            // 检查是否包含工具调用
            Matcher toolCallMatcher = TOOL_CALL_PATTERN.matcher(llmResponse);
            if (toolCallMatcher.find()) {
                String toolCallJson = toolCallMatcher.group(1);
                log.info("[ReActEngine] Detected tool call: {}", toolCallJson);

                try {
                    JSONObject toolCall = JSON.parseObject(toolCallJson);
                    String toolName = toolCall.getString("tool");
                    JSONObject toolArgs = toolCall.getJSONObject("args");
                    if (toolArgs == null) {
                        toolArgs = new JSONObject();
                    }

                    // 通知前端正在调用工具
                    callback.onThink("\n🔧 调用工具: **" + toolName + "**(" + toolArgs.toJSONString() + ")\n");

                    // 执行工具
                    String toolResult = toolRegistry.executeTool(toolName, toolArgs, userId);
                    log.info("[ReActEngine] Tool '{}' returned: {}", toolName, toolResult);

                    // 通知前端工具返回结果
                    callback.onThink("📋 工具返回: " + summarizeToolResult(toolResult) + "\n");

                    // 将工具调用及结果追加到对话历史，继续下一轮推理
                    conversationHistory.append("\n\n")
                            .append("[工具调用] ").append(toolName).append("(").append(toolArgs.toJSONString()).append(")")
                            .append("\n[工具结果] ").append(toolResult)
                            .append("\n\n请根据上述工具返回结果继续推理。如果你已经获得了足够的信息，请直接输出最终推荐结果（使用 <json>...</json> 标签包裹）。否则继续调用其他工具。");

                } catch (Exception e) {
                    log.error("[ReActEngine] Failed to parse or execute tool call: {}", toolCallJson, e);
                    callback.onThink("\n⚠️ 工具调用失败: " + e.getMessage() + "\n");
                    // 工具调用失败时，追加错误信息让 LLM 尝试其他方式
                    conversationHistory.append("\n\n[工具调用失败] ")
                            .append(e.getMessage())
                            .append("\n请换一种方式继续推理，或直接基于已有信息输出推荐结果。");
                }
            } else {
                // 没有工具调用，检查是否有最终结果
                Matcher jsonMatcher = JSON_RESULT_PATTERN.matcher(llmResponse);
                if (jsonMatcher.find()) {
                    String jsonResult = jsonMatcher.group(1).trim();
                    log.info("[ReActEngine] Final JSON result extracted at iteration {}", iteration + 1);
                    return jsonResult;
                }

                // 尝试直接解析（某些 LLM 可能不严格遵循 <json> 标签）
                String fallbackJson = tryExtractJson(llmResponse);
                if (fallbackJson != null) {
                    log.info("[ReActEngine] Fallback JSON extracted at iteration {}", iteration + 1);
                    return fallbackJson;
                }

                log.warn("[ReActEngine] No tool call and no JSON result found at iteration {}. LLM response: {}",
                        iteration + 1, llmResponse.substring(0, Math.min(200, llmResponse.length())));

                // 追加提示让 LLM 输出最终结果
                conversationHistory.append("\n\n")
                        .append("你没有调用工具也没有输出最终结果。请立即输出最终的推荐结果，使用 <json>...</json> 标签包裹。");
            }
        }

        log.warn("[ReActEngine] Max iterations ({}) reached without final result", MAX_ITERATIONS);
        return null;
    }

    /**
     * 调用 LLM 并流式推送思考链 Token。
     * 同步等待 LLM 完成（通过 CountDownLatch），但实时将 <think> 标签内的内容推送给前端。
     */
    private String callLlmAndStreamThinking(String prompt, RecommendationStreamCallback callback) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> fullResponseRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        StringBuilder responseBuilder = new StringBuilder();

        // 状态机解析：支持推理模型（有 <think>）和常规模型（无 <think>，20字符内自动 fallback）
        // 0 = 初始判断, 1 = 推理模型思考中, 2 = 普通模型流式输出中, 3 = 推理模型思考结束（收集最终结果，屏蔽JSON回传）
        final int[] state = {0};

        streamingChatLanguageModel.generate(prompt, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                responseBuilder.append(token);
                String current = responseBuilder.toString();

                if (state[0] == 0) {
                    if (current.contains("<think>")) {
                        state[0] = 1;
                        int idx = current.indexOf("<think>") + 7;
                        String afterTag = current.substring(idx);
                        if (!afterTag.isEmpty()) {
                            callback.onThink(afterTag);
                        }
                    } else if (current.length() > 20) {
                        // 20个字符仍没有 <think> 标签，判定为常规模型，补推并开启直接流式推送
                        state[0] = 2;
                        callback.onThink(current);
                    }
                } else if (state[0] == 1) {
                    // 检查是否结束思考
                    if (current.contains("</think>")) {
                        state[0] = 3;
                        int tokenCloseIdx = token.indexOf("</think>");
                        if (tokenCloseIdx > 0) {
                            callback.onThink(token.substring(0, tokenCloseIdx));
                        }
                    } else {
                        callback.onThink(token);
                    }
                } else if (state[0] == 2) {
                    // 普通模型，直接推送
                    callback.onThink(token);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                if (state[0] == 0) {
                    // 极短响应兜底
                    callback.onThink(responseBuilder.toString());
                }
                fullResponseRef.set(responseBuilder.toString());
                latch.countDown();
            }

            @Override
            public void onError(Throwable error) {
                errorRef.set(error);
                latch.countDown();
            }
        });

        try {
            boolean completed = latch.await(LLM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                log.error("[ReActEngine] LLM call timed out after {}s", LLM_TIMEOUT_SECONDS);
                return null;
            }
            if (errorRef.get() != null) {
                log.error("[ReActEngine] LLM call failed", errorRef.get());
                return null;
            }
            return fullResponseRef.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[ReActEngine] Interrupted while waiting for LLM response", e);
            return null;
        }
    }

    /**
     * 尝试从 LLM 响应中提取 JSON（兜底逻辑）
     */
    private String tryExtractJson(String response) {
        try {
            // 尝试 ```json ... ``` 格式
            Pattern codeBlockPattern = Pattern.compile("```(?:json)?\\s*\\n(\\{.*?})\\s*\\n```", Pattern.DOTALL);
            Matcher matcher = codeBlockPattern.matcher(response);
            if (matcher.find()) {
                String json = matcher.group(1).trim();
                JSON.parseObject(json); // 验证 JSON 合法性
                return json;
            }

            // 尝试直接找 { "recommendations": [...] }
            int braceStart = response.indexOf("{\"recommendations\"");
            if (braceStart == -1) {
                braceStart = response.indexOf("{ \"recommendations\"");
            }
            if (braceStart >= 0) {
                String candidate = response.substring(braceStart);
                // 找到匹配的 }
                int depth = 0;
                for (int i = 0; i < candidate.length(); i++) {
                    if (candidate.charAt(i) == '{') depth++;
                    if (candidate.charAt(i) == '}') depth--;
                    if (depth == 0) {
                        String json = candidate.substring(0, i + 1);
                        JSON.parseObject(json); // 验证
                        return json;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[ReActEngine] Fallback JSON extraction failed", e);
        }
        return null;
    }

    /**
     * 将工具返回结果摘要化（避免在 CoT 推送中暴露过多数据）
     */
    private String summarizeToolResult(String result) {
        if (result == null) return "null";
        if (result.length() <= 200) return result;
        return result.substring(0, 200) + "... (共 " + result.length() + " 字符)";
    }
}
