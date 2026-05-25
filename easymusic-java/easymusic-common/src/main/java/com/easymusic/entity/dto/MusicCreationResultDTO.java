package com.easymusic.entity.dto;

import java.util.List;

public class MusicCreationResultDTO {
    private String taskId;
    private String title;
    private Integer duration;
    private String audioUrl;
    private String audioHiUrl;
    private List<Lyrics> lyricsList;
    private Boolean createSuccess;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getAudioHiUrl() {
        return audioHiUrl;
    }

    public void setAudioHiUrl(String audioHiUrl) {
        this.audioHiUrl = audioHiUrl;
    }

    public List<Lyrics> getLyricsList() {
        return lyricsList;
    }

    public void setLyricsList(List<Lyrics> lyricsList) {
        this.lyricsList = lyricsList;
    }

    public Boolean getCreateSuccess() {
        return createSuccess;
    }

    public void setCreateSuccess(Boolean createSuccess) {
        this.createSuccess = createSuccess;
    }

    public class Lyrics {
        private double start;
        private double end;
        private String text;

        public double getStart() {
            return start;
        }

        public void setStart(double start) {
            this.start = start;
        }

        public double getEnd() {
            return end;
        }

        public void setEnd(double end) {
            this.end = end;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

}
