package com.easymusic.entity.query;

import java.util.List;

/**
 * 音乐信息参数
 */
public class MusicInfoQuery extends BaseParam {


    /**
     * 音乐ID
     */
    private String musicId;

    private String musicIdFuzzy;

    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 任务ID
     */
    private String taskId;

    private String taskIdFuzzy;

    /**
     * 创作ID
     */
    private String creationId;

    private String creationIdFuzzy;

    /**
     * 标题
     */
    private String musicTitle;

    private String musicTitleFuzzy;

    /**
     * 封面
     */
    private String cover;

    private String coverFuzzy;

    /**
     * 音乐地址
     */
    private String audioPath;

    private String audioPathFuzzy;

    /**
     * 持续时间
     */
    private Integer duration;

    /**
     * 歌词
     */
    private String lyrics;

    private String lyricsFuzzy;

    /**
     * 播放数量
     */
    private Integer playCount;

    /**
     * 点赞数
     */
    private Integer goodCount;

    /**
     * 0:未推荐 1:已推荐
     */
    private Integer commendType;

    /**
     * 创建时间
     */
    private String createTime;

    private String createTimeStart;

    private String createTimeEnd;

    /**
     * 0:生成音乐中 1:生成完毕
     */
    private Integer musicStatus;

    /**
     * 音乐类型 0:音乐 1:纯音乐
     */
    private Integer musicType;

    private Boolean queryUser;

    private List<String> musicIdList;

    private String currentUserId;

    private Boolean queryLikeMusic;

    private String likeUserId;

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public String getMusicId() {
        return this.musicId;
    }

    public void setMusicIdFuzzy(String musicIdFuzzy) {
        this.musicIdFuzzy = musicIdFuzzy;
    }

    public String getMusicIdFuzzy() {
        return this.musicIdFuzzy;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserIdFuzzy(String userIdFuzzy) {
        this.userIdFuzzy = userIdFuzzy;
    }

    public String getUserIdFuzzy() {
        return this.userIdFuzzy;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return this.taskId;
    }

    public void setTaskIdFuzzy(String taskIdFuzzy) {
        this.taskIdFuzzy = taskIdFuzzy;
    }

    public String getTaskIdFuzzy() {
        return this.taskIdFuzzy;
    }

    public void setCreationId(String creationId) {
        this.creationId = creationId;
    }

    public String getCreationId() {
        return this.creationId;
    }

    public void setCreationIdFuzzy(String creationIdFuzzy) {
        this.creationIdFuzzy = creationIdFuzzy;
    }

    public String getCreationIdFuzzy() {
        return this.creationIdFuzzy;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public String getMusicTitle() {
        return this.musicTitle;
    }

    public void setMusicTitleFuzzy(String musicTitleFuzzy) {
        this.musicTitleFuzzy = musicTitleFuzzy;
    }

    public String getMusicTitleFuzzy() {
        return this.musicTitleFuzzy;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCover() {
        return this.cover;
    }

    public void setCoverFuzzy(String coverFuzzy) {
        this.coverFuzzy = coverFuzzy;
    }

    public String getCoverFuzzy() {
        return this.coverFuzzy;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getAudioPath() {
        return this.audioPath;
    }

    public void setAudioPathFuzzy(String audioPathFuzzy) {
        this.audioPathFuzzy = audioPathFuzzy;
    }

    public String getAudioPathFuzzy() {
        return this.audioPathFuzzy;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getDuration() {
        return this.duration;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getLyrics() {
        return this.lyrics;
    }

    public void setLyricsFuzzy(String lyricsFuzzy) {
        this.lyricsFuzzy = lyricsFuzzy;
    }

    public String getLyricsFuzzy() {
        return this.lyricsFuzzy;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public Integer getPlayCount() {
        return this.playCount;
    }

    public void setGoodCount(Integer goodCount) {
        this.goodCount = goodCount;
    }

    public Integer getGoodCount() {
        return this.goodCount;
    }

    public void setCommendType(Integer commendType) {
        this.commendType = commendType;
    }

    public Integer getCommendType() {
        return this.commendType;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public void setCreateTimeStart(String createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public String getCreateTimeStart() {
        return this.createTimeStart;
    }

    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public String getCreateTimeEnd() {
        return this.createTimeEnd;
    }

    public void setMusicStatus(Integer musicStatus) {
        this.musicStatus = musicStatus;
    }

    public Integer getMusicStatus() {
        return this.musicStatus;
    }

    public void setMusicType(Integer musicType) {
        this.musicType = musicType;
    }

    public Integer getMusicType() {
        return this.musicType;
    }

    public Boolean getQueryUser() {
        return queryUser;
    }

    public void setQueryUser(Boolean queryUser) {
        this.queryUser = queryUser;
    }

    public List<String> getMusicIdList() {
        return musicIdList;
    }

    public void setMusicIdList(List<String> musicIdList) {
        this.musicIdList = musicIdList;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public Boolean getQueryLikeMusic() {
        return queryLikeMusic;
    }

    public void setQueryLikeMusic(Boolean queryLikeMusic) {
        this.queryLikeMusic = queryLikeMusic;
    }

    public String getLikeUserId() {
        return likeUserId;
    }

    public void setLikeUserId(String likeUserId) {
        this.likeUserId = likeUserId;
    }
}
