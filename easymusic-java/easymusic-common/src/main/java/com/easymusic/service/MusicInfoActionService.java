package com.easymusic.service;

import com.easymusic.entity.po.MusicInfoAction;
import com.easymusic.entity.query.MusicInfoActionQuery;
import com.easymusic.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 业务接口
 */
public interface MusicInfoActionService {

    /**
     * 根据条件查询列表
     */
    List<MusicInfoAction> findListByParam(MusicInfoActionQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(MusicInfoActionQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<MusicInfoAction> findListByPage(MusicInfoActionQuery param);

    /**
     * 新增
     */
    Integer add(MusicInfoAction bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<MusicInfoAction> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<MusicInfoAction> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(MusicInfoAction bean, MusicInfoActionQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(MusicInfoActionQuery param);

    /**
     * 根据ActionId查询对象
     */
    MusicInfoAction getMusicInfoActionByActionId(Integer actionId);


    /**
     * 根据ActionId修改
     */
    Integer updateMusicInfoActionByActionId(MusicInfoAction bean, Integer actionId);


    /**
     * 根据ActionId删除
     */
    Integer deleteMusicInfoActionByActionId(Integer actionId);


    /**
     * 根据MusicIdAndUserId查询对象
     */
    MusicInfoAction getMusicInfoActionByMusicIdAndUserId(String musicId, String userId);


    /**
     * 根据MusicIdAndUserId修改
     */
    Integer updateMusicInfoActionByMusicIdAndUserId(MusicInfoAction bean, String musicId, String userId);


    /**
     * 根据MusicIdAndUserId删除
     */
    Integer deleteMusicInfoActionByMusicIdAndUserId(String musicId, String userId);

    void doGood(String musicId, String userId);
}