package com.easymusic.mappers;

import com.easymusic.entity.po.SysDict;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据库操作接口
 */
public interface SysDictMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据DictId更新
     */
    Integer updateByDictId(@Param("bean") T t, @Param("dictId") Integer dictId);


    /**
     * 根据DictId删除
     */
    Integer deleteByDictId(@Param("dictId") Integer dictId);


    /**
     * 根据DictId获取对象
     */
    T selectByDictId(@Param("dictId") Integer dictId);


    void changeSort(@Param("sysDictList") List<SysDict> sysDictList);
}
