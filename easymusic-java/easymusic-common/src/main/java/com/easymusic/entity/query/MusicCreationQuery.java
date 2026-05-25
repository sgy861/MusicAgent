package com.easymusic.entity.query;

import java.util.Date;


/**
 * 音乐创作信息参数
 */
public class MusicCreationQuery extends BaseParam {


	/**
	 * 创作ID
	 */
	private String creationId;

	private String creationIdFuzzy;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 提示词
	 */
	private String prompt;

	private String promptFuzzy;

	/**
	 * 歌词
	 */
	private String lyrics;

	private String lyricsFuzzy;

	/**
	 * 模型
	 */
	private String model;

	private String modelFuzzy;

	/**
	 * 音乐类型 0:音乐 1:纯音乐
	 */
	private Integer musicType;

	/**
	 * 模式 0:简单模式 1:专家模式
	 */
	private Integer modeType;

	/**
	 * 设置信息
	 */
	private String settings;

	private String settingsFuzzy;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setCreationId(String creationId){
		this.creationId = creationId;
	}

	public String getCreationId(){
		return this.creationId;
	}

	public void setCreationIdFuzzy(String creationIdFuzzy){
		this.creationIdFuzzy = creationIdFuzzy;
	}

	public String getCreationIdFuzzy(){
		return this.creationIdFuzzy;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setUserIdFuzzy(String userIdFuzzy){
		this.userIdFuzzy = userIdFuzzy;
	}

	public String getUserIdFuzzy(){
		return this.userIdFuzzy;
	}

	public void setPrompt(String prompt){
		this.prompt = prompt;
	}

	public String getPrompt(){
		return this.prompt;
	}

	public void setPromptFuzzy(String promptFuzzy){
		this.promptFuzzy = promptFuzzy;
	}

	public String getPromptFuzzy(){
		return this.promptFuzzy;
	}

	public void setLyrics(String lyrics){
		this.lyrics = lyrics;
	}

	public String getLyrics(){
		return this.lyrics;
	}

	public void setLyricsFuzzy(String lyricsFuzzy){
		this.lyricsFuzzy = lyricsFuzzy;
	}

	public String getLyricsFuzzy(){
		return this.lyricsFuzzy;
	}

	public void setModel(String model){
		this.model = model;
	}

	public String getModel(){
		return this.model;
	}

	public void setModelFuzzy(String modelFuzzy){
		this.modelFuzzy = modelFuzzy;
	}

	public String getModelFuzzy(){
		return this.modelFuzzy;
	}

	public void setMusicType(Integer musicType){
		this.musicType = musicType;
	}

	public Integer getMusicType(){
		return this.musicType;
	}

	public void setModeType(Integer modeType){
		this.modeType = modeType;
	}

	public Integer getModeType(){
		return this.modeType;
	}

	public void setSettings(String settings){
		this.settings = settings;
	}

	public String getSettings(){
		return this.settings;
	}

	public void setSettingsFuzzy(String settingsFuzzy){
		this.settingsFuzzy = settingsFuzzy;
	}

	public String getSettingsFuzzy(){
		return this.settingsFuzzy;
	}

	public void setCreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getCreateTime(){
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

}
