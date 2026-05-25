package com.easymusic.mappers;

import org.apache.ibatis.annotations.Param;

/**
 *  数据库操作接口
 */
public interface MusicInfoActionMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ActionId更新
	 */
	 Integer updateByActionId(@Param("bean") T t,@Param("actionId") Integer actionId);


	/**
	 * 根据ActionId删除
	 */
	 Integer deleteByActionId(@Param("actionId") Integer actionId);


	/**
	 * 根据ActionId获取对象
	 */
	 T selectByActionId(@Param("actionId") Integer actionId);


	/**
	 * 根据MusicIdAndUserId更新
	 */
	 Integer updateByMusicIdAndUserId(@Param("bean") T t,@Param("musicId") String musicId,@Param("userId") String userId);


	/**
	 * 根据MusicIdAndUserId删除
	 */
	 Integer deleteByMusicIdAndUserId(@Param("musicId") String musicId,@Param("userId") String userId);


	/**
	 * 根据MusicIdAndUserId获取对象
	 */
	 T selectByMusicIdAndUserId(@Param("musicId") String musicId,@Param("userId") String userId);


}
