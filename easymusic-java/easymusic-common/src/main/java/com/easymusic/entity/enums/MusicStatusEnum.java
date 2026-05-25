package com.easymusic.entity.enums;


public enum MusicStatusEnum {
    CREATING(0, "音乐生成中"),
    CREATED(1, "音乐生成完成"),
    CRAETE_FAIL(2, "音乐生成失败");

    private Integer status;
    private String desc;

    MusicStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
