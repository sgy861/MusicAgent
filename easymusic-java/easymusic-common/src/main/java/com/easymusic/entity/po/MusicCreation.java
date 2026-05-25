package com.easymusic.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.easymusic.entity.enums.DateTimePatternEnum;
import com.easymusic.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 音乐创作信息
 */
public class MusicCreation implements Serializable {


	/**
	 * 创作ID
	 */
	private String creationId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 提示词
	 */
	private String prompt;

	/**
	 * 歌词
	 */
	private String lyrics;

	/**
	 * 模型
	 */
	private String model;

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

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public void setCreationId(String creationId){
		this.creationId = creationId;
	}

	public String getCreationId(){
		return this.creationId;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setPrompt(String prompt){
		this.prompt = prompt;
	}

	public String getPrompt(){
		return this.prompt;
	}

	public void setLyrics(String lyrics){
		this.lyrics = lyrics;
	}

	public String getLyrics(){
		return this.lyrics;
	}

	public void setModel(String model){
		this.model = model;
	}

	public String getModel(){
		return this.model;
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

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "创作ID:"+(creationId == null ? "空" : creationId)+"，用户ID:"+(userId == null ? "空" : userId)+"，提示词:"+(prompt == null ? "空" : prompt)+"，歌词:"+(lyrics == null ? "空" : lyrics)+"，模型:"+(model == null ? "空" : model)+"，音乐类型 0:音乐 1:纯音乐:"+(musicType == null ? "空" : musicType)+"，模式 0:简单模式 1:专家模式:"+(modeType == null ? "空" : modeType)+"，设置信息:"+(settings == null ? "空" : settings)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
