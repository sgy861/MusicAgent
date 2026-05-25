package com.easymusic.entity.enums;


import java.util.Arrays;
import java.util.Optional;

public enum ModelType4MusicEnum {
    V3("V3", "TemPolor v3", "tianpuyueApi", "生成最长120s的带人声音乐(0.2元/首)"),
    V3_5("V3.5", "TemPolor v3.5", "tianpuyueApi", "生成最长270s的带人声音乐(0.3元/首)");

    private String modelId;
    private String modelCode;
    private String apiCode;
    private String desc;

    ModelType4MusicEnum(String modelId, String modelCode, String apiCode, String desc) {
        this.modelId = modelId;
        this.modelCode = modelCode;
        this.apiCode = apiCode;
        this.desc = desc;
    }

    public static ModelType4MusicEnum getById(String id) {
        Optional<ModelType4MusicEnum> typeEnum = Arrays.stream(ModelType4MusicEnum.values()).filter(value -> value.getModelId().equals(id)).findFirst();
        return typeEnum == null ? null : typeEnum.get();
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

    public String getApiCode() {
        return apiCode;
    }
}
