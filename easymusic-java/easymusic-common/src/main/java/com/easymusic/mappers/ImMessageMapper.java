package com.easymusic.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * IM消息数据库操作接口
 */
public interface ImMessageMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据MessageId获取对象
     */
    T selectByMessageId(@Param("messageId") String messageId);

    /**
     * 根据MessageId修改
     */
    Integer updateByMessageId(@Param("bean") T t, @Param("messageId") String messageId);

    /**
     * 根据MessageId删除
     */
    Integer deleteByMessageId(@Param("messageId") String messageId);
}
