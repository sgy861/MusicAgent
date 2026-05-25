package com.easymusic.entity.enums;


public enum MusicActionTypeEnum {
    GOOD(1, "点赞");

    private Integer type;
    private String desc;

    MusicActionTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
