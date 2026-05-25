package com.easymusic.entity.enums;


import java.util.Arrays;
import java.util.Optional;

public enum ModelType4PureMusicEnum {
    V3("V3", "TemPolor i3", "tianpuyueApi", "生成最长120s的纯音乐(0.2元/首)"),
    V3_5("V3.5", "TemPolor i3.5", "tianpuyueApi", "生成最长270s的纯音乐(0.3元/首)");

    private String modelId;
    private String modelCode;
    private String apiCode;
    private String desc;

    ModelType4PureMusicEnum(String modelId, String modelCode, String apiCode, String desc) {
        this.modelId = modelId;
        this.modelCode = modelCode;
        this.apiCode = apiCode;
        this.desc = desc;
    }

    public static ModelType4PureMusicEnum getById(String id) {
        Optional<ModelType4PureMusicEnum> typeEnum = Arrays.stream(ModelType4PureMusicEnum.values()).filter(value -> value.getModelId().equals(id)).findFirst();
        return typeEnum.get();
    }

    public String getApiCode() {
        return apiCode;
    }

    public String getModelId() {
        return modelId;
    }

    public String getModelCode() {
        return modelCode;
    }

    public String getDesc() {
        return desc;
    }

}
