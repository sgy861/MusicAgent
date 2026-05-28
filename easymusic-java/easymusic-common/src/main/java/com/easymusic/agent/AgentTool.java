package com.easymusic.agent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个方法为 Agent 可调用的工具。
 * ReActEngine 在运行时会通过反射扫描所有被此注解标记的方法，
 * 并将其注册到工具清单中，供大模型进行 Function Calling 决策。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentTool {

    /**
     * 工具的唯一名称，LLM 通过此名称来调用工具。
     * 命名规范：使用 camelCase，如 "checkQuota"、"searchSimilarMusic"。
     */
    String name();

    /**
     * 工具的功能描述，用于在 System Prompt 中告知 LLM 此工具的用途。
     * 描述应当简洁明了，帮助 LLM 判断何时该调用此工具。
     */
    String description();

    /**
     * 工具参数的 JSON Schema 描述（可选）。
     * 如果不指定，引擎会通过反射自动推断方法参数。
     */
    String parameterSchema() default "";
}
