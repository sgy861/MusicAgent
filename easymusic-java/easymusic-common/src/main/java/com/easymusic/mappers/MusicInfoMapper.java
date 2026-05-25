package com.easymusic.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 音乐信息 数据库操作接口
 */
public interface MusicInfoMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据MusicId更新
     */
    Integer updateByMusicId(@Param("bean") T t, @Param("musicId") String musicId);


    /**
     * 根据MusicId删除
     */
    Integer deleteByMusicId(@Param("musicId") String musicId);


    /**
     * 根据MusicId获取对象
     */
    T selectByMusicId(@Param("musicId") String musicId);


    /**
     * 根据TaskId更新
     */
    Integer updateByTaskId(@Param("bean") T t, @Param("taskId") String taskId);


    /**
     * 根据TaskId删除
     */
    Integer deleteByTaskId(@Param("taskId") String taskId);


    /**
     * 根据TaskId获取对象
     */
    T selectByTaskId(@Param("taskId") String taskId);

    void updateMusicCount(@Param("musicId") String musicId);
}
