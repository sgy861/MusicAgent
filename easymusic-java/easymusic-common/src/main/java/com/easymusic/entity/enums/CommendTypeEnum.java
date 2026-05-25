package com.easymusic.entity.enums;


public enum CommendTypeEnum {
    NOT_COMMEND(0, "未推荐"), COMMEND(1, "已推荐");

    private Integer type;

    private String desc;

    CommendTypeEnum(Integer type, String desc) {
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
