package com.easymusic.mappers;

import com.easymusic.entity.po.ProductInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据库操作接口
 */
public interface ProductInfoMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据ProductId更新
     */
    Integer updateByProductId(@Param("bean") T t, @Param("productId") String productId);


    /**
     * 根据ProductId删除
     */
    Integer deleteByProductId(@Param("productId") String productId);


    /**
     * 根据ProductId获取对象
     */
    T selectByProductId(@Param("productId") String productId);


    void changeProductSort(@Param("productList") List<ProductInfo> productInfoList);
}
