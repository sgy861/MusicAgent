package com.easymusic.entity.dto;

public class MusicSettingDTO {
    private String musicGener;
    private String musicEmotion;
    private String musicSex;

    public String getMusicGener() {
        return musicGener;
    }

    public void setMusicGener(String musicGener) {
        this.musicGener = musicGener;
    }

    public String getMusicEmotion() {
        return musicEmotion;
    }

    public void setMusicEmotion(String musicEmotion) {
        this.musicEmotion = musicEmotion;
    }

    public String getMusicSex() {
        return musicSex;
    }

    public void setMusicSex(String musicSex) {
        this.musicSex = musicSex;
    }
}
