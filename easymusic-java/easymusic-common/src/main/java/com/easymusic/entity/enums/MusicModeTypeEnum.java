package com.easymusic.entity.enums;


import java.util.Arrays;
import java.util.Optional;

public enum MusicModeTypeEnum {
    SIMPLE(0, "简单模式)"),
    ADVANCED(1, "高级模式");

    private Integer modeType;
    private String desc;

    MusicModeTypeEnum(Integer modeType, String desc) {
        this.modeType = modeType;
        this.desc = desc;
    }

    public static MusicModeTypeEnum getByType(Integer type) {
        Optional<MusicModeTypeEnum> typeEnum = Arrays.stream(MusicModeTypeEnum.values()).filter(value -> value.getModeType().equals(type)).findFirst();
        return typeEnum == null ? null : typeEnum.get();
    }

    public Integer getModeType() {
        return modeType;
    }

    public String getDesc() {
        return desc;
    }
}
