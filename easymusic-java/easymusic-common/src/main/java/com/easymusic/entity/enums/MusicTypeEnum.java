package com.easymusic.entity.enums;


import java.util.Arrays;
import java.util.Optional;

public enum MusicTypeEnum {
    MUSIC(0, "music_model", "音乐"),
    PURE(1, "music_model_pure", "纯音乐");

    private Integer type;
    private String dictCode;
    private String desc;

    MusicTypeEnum(Integer type, String dictCode, String desc) {
        this.type = type;
        this.dictCode = dictCode;
        this.desc = desc;
    }

    public static MusicTypeEnum getByType(Integer type) {
        Optional<MusicTypeEnum> musicTypeEnum = Arrays.stream(MusicTypeEnum.values())
                .filter(value -> value.getType().equals(type)).findFirst();
        return musicTypeEnum == null ? null : musicTypeEnum.get();
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public String getDictCode() {
        return dictCode;
    }
}
