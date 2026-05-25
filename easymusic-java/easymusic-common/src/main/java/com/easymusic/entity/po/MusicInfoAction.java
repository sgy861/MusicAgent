package com.easymusic.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 音乐操作
 */
public class MusicInfoAction implements Serializable {


	/**
	 * 操作ID
	 */
	private Integer actionId;

	/**
	 * 音乐ID
	 */
	private String musicId;

	/**
	 * 音乐用户ID
	 */
	private String musicUserId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 操作类型1:点赞
	 */
	private Integer actionType;


	public void setActionId(Integer actionId){
		this.actionId = actionId;
	}

	public Integer getActionId(){
		return this.actionId;
	}

	public void setMusicId(String musicId){
		this.musicId = musicId;
	}

	public String getMusicId(){
		return this.musicId;
	}

	public void setMusicUserId(String musicUserId){
		this.musicUserId = musicUserId;
	}

	public String getMusicUserId(){
		return this.musicUserId;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setActionType(Integer actionType){
		this.actionType = actionType;
	}

	public Integer getActionType(){
		return this.actionType;
	}

	@Override
	public String toString (){
		return "操作ID:"+(actionId == null ? "空" : actionId)+"，音乐ID:"+(musicId == null ? "空" : musicId)+"，音乐用户ID:"+(musicUserId == null ? "空" : musicUserId)+"，用户ID:"+(userId == null ? "空" : userId)+"，操作类型1:点赞:"+(actionType == null ? "空" : actionType);
	}
}
