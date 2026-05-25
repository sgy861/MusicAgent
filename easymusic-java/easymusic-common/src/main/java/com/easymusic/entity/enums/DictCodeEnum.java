package com.easymusic.entity.enums;


public enum DictCodeEnum {
    MUSIC_PROMPT("music_prompt", "音乐提示词"),
    MUSIC_PURE_PROMPT("music_pure_prompt", "纯音乐提示词"),
    MUSIC_GENRE("music_genre", "音乐曲风"),
    MUSIC_EMOTION("music_emotion", "音乐情绪"),
    MUSIC_SEX("music_sex", "音乐人声");

    private String code;
    private String desc;

    DictCodeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
