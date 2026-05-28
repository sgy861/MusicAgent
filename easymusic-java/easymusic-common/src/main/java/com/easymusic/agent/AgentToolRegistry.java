package com.easymusic.agent;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Agent 工具注册中心。
 * 应用启动时自动扫描所有 Spring Bean 中被 {@link AgentTool} 注解标记的方法，
 * 构建工具清单（名称 → 描述 → 参数 Schema → 执行句柄），供 {@link ReActEngine} 使用。
 *
 * <p>职责：
 * <ul>
 *   <li>工具发现与注册（启动时一次性扫描）</li>
 *   <li>工具执行路由（根据名称安全地反射调用）</li>
 *   <li>生成工具清单的 JSON Schema（注入到 System Prompt 中供 LLM 感知）</li>
 * </ul>
 */
@Component
@Slf4j
public class AgentToolRegistry {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 已注册工具的内部表示
     */
    public static class RegisteredTool {
        private final String name;
        private final String description;
        private final Method method;
        private final Object bean;
        private final List<ToolParameter> parameters;

        public RegisteredTool(String name, String description, Method method, Object bean, List<ToolParameter> parameters) {
            this.name = name;
            this.description = description;
            this.method = method;
            this.bean = bean;
            this.parameters = parameters;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public Method getMethod() { return method; }
        public Object getBean() { return bean; }
        public List<ToolParameter> getParameters() { return parameters; }
    }

    public static class ToolParameter {
        private final String name;
        private final Class<?> type;

        public ToolParameter(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }

        public String getName() { return name; }
        public Class<?> getType() { return type; }
    }

    private final Map<String, RegisteredTool> toolMap = new ConcurrentHashMap<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * 启动时扫描所有 Spring Bean，注册被 @AgentTool 标记的方法
     */
    @EventListener(ContextRefreshedEvent.class)
    public void init(ContextRefreshedEvent event) {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        ApplicationContext ctx = event.getApplicationContext();
        String[] beanNames = ctx.getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            Object bean = ctx.getBean(beanName);
            Class<?> clazz = bean.getClass();
            // 处理 Spring CGLIB 代理
            if (clazz.getName().contains("$$")) {
                clazz = clazz.getSuperclass();
            }
            for (Method method : clazz.getDeclaredMethods()) {
                AgentTool annotation = method.getAnnotation(AgentTool.class);
                if (annotation != null) {
                    List<ToolParameter> params = new ArrayList<>();
                    for (Parameter param : method.getParameters()) {
                        params.add(new ToolParameter(param.getName(), param.getType()));
                    }
                    RegisteredTool tool = new RegisteredTool(
                            annotation.name(),
                            annotation.description(),
                            method,
                            bean,
                            params
                    );
                    toolMap.put(annotation.name(), tool);
                    log.info("[AgentToolRegistry] Registered tool: {} -> {}.{}() | params: {}",
                            annotation.name(), clazz.getSimpleName(), method.getName(),
                            params.stream().map(p -> p.getName() + ":" + p.getType().getSimpleName()).toList());
                }
            }
        }
        log.info("[AgentToolRegistry] Total registered tools: {}", toolMap.size());
    }

    /**
     * 执行工具调用。根据工具名称和参数 JSON 进行安全的反射调用。
     *
     * @param toolName     工具名称
     * @param argsJson     参数 JSON 对象（key = 参数名，value = 参数值）
     * @param actualUserId 经鉴权认证的当前活跃用户ID，用于安全重写以防横向越权
     * @return 工具执行结果（序列化为 JSON 字符串）
     */
    public String executeTool(String toolName, JSONObject argsJson, String actualUserId) {
        RegisteredTool tool = toolMap.get(toolName);
        if (tool == null) {
            return "{\"error\": \"Unknown tool: " + toolName + "\"}";
        }

        try {
            Method method = tool.getMethod();
            List<ToolParameter> params = tool.getParameters();
            Object[] args = new Object[params.size()];

            for (int i = 0; i < params.size(); i++) {
                ToolParameter param = params.get(i);
                
                // 安全防线：如果入参名为 userId，强制使用 actualUserId 覆盖，阻断 prompt 注入越权
                if ("userId".equals(param.getName())) {
                    args[i] = actualUserId;
                    continue;
                }

                Object value = argsJson.get(param.getName());
                // 类型适配
                if (value != null) {
                    if (param.getType() == int.class || param.getType() == Integer.class) {
                        args[i] = argsJson.getIntValue(param.getName());
                    } else if (param.getType() == long.class || param.getType() == Long.class) {
                        args[i] = argsJson.getLongValue(param.getName());
                    } else if (param.getType() == String.class) {
                        args[i] = argsJson.getString(param.getName());
                    } else if (param.getType() == float.class || param.getType() == Float.class) {
                        args[i] = argsJson.getFloatValue(param.getName());
                    } else if (param.getType() == double.class || param.getType() == Double.class) {
                        args[i] = argsJson.getDoubleValue(param.getName());
                    } else if (param.getType() == boolean.class || param.getType() == Boolean.class) {
                        args[i] = argsJson.getBooleanValue(param.getName());
                    } else {
                        args[i] = argsJson.getObject(param.getName(), param.getType());
                    }
                }
            }

            method.setAccessible(true);
            Object result = method.invoke(tool.getBean(), args);
            if (result == null) {
                return "{\"result\": null}";
            }
            if (result instanceof String) {
                return (String) result;
            }
            return JSONObject.toJSONString(result);
        } catch (Exception e) {
            log.error("[AgentToolRegistry] Failed to execute tool: {}", toolName, e);
            return "{\"error\": \"Tool execution failed: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 生成工具清单的 Prompt 描述文本。
     * 此文本将被注入到 System Prompt 中，让 LLM 感知可用的工具集合。
     */
    public String generateToolsPrompt() {
        if (toolMap.isEmpty()) {
            return "当前没有可用的工具。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("你可以使用以下工具来辅助你的推理决策。要调用工具，请在你的回复中输出如下格式的 JSON 块：\n");
        sb.append("```tool_call\n{\"tool\": \"工具名称\", \"args\": {\"参数名\": \"参数值\"}}\n```\n\n");
        sb.append("可用工具列表：\n");

        for (RegisteredTool tool : toolMap.values()) {
            sb.append("- **").append(tool.getName()).append("**: ").append(tool.getDescription()).append("\n");
            if (!tool.getParameters().isEmpty()) {
                sb.append("  参数：");
                StringJoiner joiner = new StringJoiner(", ");
                for (ToolParameter param : tool.getParameters()) {
                    joiner.add(param.getName() + " (" + param.getType().getSimpleName() + ")");
                }
                sb.append(joiner).append("\n");
            }
        }

        sb.append("\n重要规则：\n");
        sb.append("1. 每次你只能调用一个工具。\n");
        sb.append("2. 调用工具后，你会收到工具的返回结果，然后你可以继续推理或调用下一个工具。\n");
        sb.append("3. 当你不再需要调用工具时，直接输出最终的推荐结果。\n");
        sb.append("4. 最终推荐结果必须使用 <json>...</json> 标签包裹。\n");

        return sb.toString();
    }

    /**
     * 判断是否有注册的工具
     */
    public boolean hasTools() {
        return !toolMap.isEmpty();
    }

    /**
     * 获取所有注册的工具名称
     */
    public Set<String> getToolNames() {
        return Collections.unmodifiableSet(toolMap.keySet());
    }
}
