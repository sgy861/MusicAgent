package com.easymusic.service;

import com.easymusic.entity.enums.UserIntegralRecordTypeEnum;
import com.easymusic.entity.po.UserIntegralRecord;
import com.easymusic.entity.query.UserIntegralRecordQuery;
import com.easymusic.entity.vo.PaginationResultVO;

import java.math.BigDecimal;
import java.util.List;


/**
 * 用户积分记录信息 业务接口
 */
public interface UserIntegralRecordService {

    /**
     * 根据条件查询列表
     */
    List<UserIntegralRecord> findListByParam(UserIntegralRecordQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(UserIntegralRecordQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<UserIntegralRecord> findListByPage(UserIntegralRecordQuery param);

    /**
     * 新增
     */
    Integer add(UserIntegralRecord bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserIntegralRecord> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<UserIntegralRecord> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(UserIntegralRecord bean, UserIntegralRecordQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(UserIntegralRecordQuery param);

    /**
     * 根据RecordId查询对象
     */
    UserIntegralRecord getUserIntegralRecordByRecordId(Integer recordId);


    /**
     * 根据RecordId修改
     */
    Integer updateUserIntegralRecordByRecordId(UserIntegralRecord bean, Integer recordId);


    /**
     * 根据RecordId删除
     */
    Integer deleteUserIntegralRecordByRecordId(Integer recordId);

    void changeUserIntegral(UserIntegralRecordTypeEnum recordTypeEnum, String businessId, String userId, Integer changeIntegral, BigDecimal amount);
}