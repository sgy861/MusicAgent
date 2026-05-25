package com.easymusic.mappers;

import org.apache.ibatis.annotations.Param;

/**
 *  数据库操作接口
 */
public interface MusicCreationMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据CreationId更新
	 */
	 Integer updateByCreationId(@Param("bean") T t,@Param("creationId") String creationId);


	/**
	 * 根据CreationId删除
	 */
	 Integer deleteByCreationId(@Param("creationId") String creationId);


	/**
	 * 根据CreationId获取对象
	 */
	 T selectByCreationId(@Param("creationId") String creationId);


}
