package com.easymusic.entity.query;



/**
 * 音乐操作参数
 */
public class MusicInfoActionQuery extends BaseParam {


	/**
	 * 操作ID
	 */
	private Integer actionId;

	/**
	 * 音乐ID
	 */
	private String musicId;

	private String musicIdFuzzy;

	/**
	 * 音乐用户ID
	 */
	private String musicUserId;

	private String musicUserIdFuzzy;

	/**
	 * 用户ID
	 */
	private String userId;

	private String userIdFuzzy;

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

	public void setMusicIdFuzzy(String musicIdFuzzy){
		this.musicIdFuzzy = musicIdFuzzy;
	}

	public String getMusicIdFuzzy(){
		return this.musicIdFuzzy;
	}

	public void setMusicUserId(String musicUserId){
		this.musicUserId = musicUserId;
	}

	public String getMusicUserId(){
		return this.musicUserId;
	}

	public void setMusicUserIdFuzzy(String musicUserIdFuzzy){
		this.musicUserIdFuzzy = musicUserIdFuzzy;
	}

	public String getMusicUserIdFuzzy(){
		return this.musicUserIdFuzzy;
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

	public void setActionType(Integer actionType){
		this.actionType = actionType;
	}

	public Integer getActionType(){
		return this.actionType;
	}

}
