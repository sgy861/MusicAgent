package com.easymusic.entity.enums;


public enum MusicSettingEnum {
    MUSIC_GENER("musicGener", "曲风"),
    MUSIC_EMOTION("musicEmotion", "情绪"),
    MUSIC_SEX("musicSex", "音色");

    private String keyCode;
    private String typeDesc;

    MusicSettingEnum(String keyCode, String typeDesc) {
        this.keyCode = keyCode;
        this.typeDesc = typeDesc;
    }

    public String getKeyCode() {
        return keyCode;
    }

    public String getTypeDesc() {
        return typeDesc;
    }
}
