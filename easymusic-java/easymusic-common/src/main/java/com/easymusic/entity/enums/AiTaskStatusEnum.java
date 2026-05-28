package com.easymusic.entity.enums;

public enum AiTaskStatusEnum {
    INIT(0, "初始化"),
    QUOTA_FROZEN(1, "配额已冻结"),
    AI_SUBMITTED(2, "AI 任务已提交"),
    AI_PROCESSING(3, "AI 任务处理中"),
    COMPLETED(4, "已完成"),
    FAILED(5, "已失败");

    private final Integer status;
    private final String desc;

    AiTaskStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static AiTaskStatusEnum getByStatus(Integer status) {
        if (status == null) {
            return null;
        }
        for (AiTaskStatusEnum value : AiTaskStatusEnum.values()) {
            if (value.getStatus().equals(status)) {
                return value;
            }
        }
        return null;
    }
}
