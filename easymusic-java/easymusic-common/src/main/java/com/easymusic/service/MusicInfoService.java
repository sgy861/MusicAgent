package com.easymusic.service;

import com.easymusic.entity.dto.MusicCreationResultDTO;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * 音乐信息 业务接口
 */
public interface MusicInfoService {

    /**
     * 根据条件查询列表
     */
    List<MusicInfo> findListByParam(MusicInfoQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(MusicInfoQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<MusicInfo> findListByPage(MusicInfoQuery param);

    /**
     * 新增
     */
    Integer add(MusicInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<MusicInfo> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<MusicInfo> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(MusicInfo bean, MusicInfoQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(MusicInfoQuery param);

    /**
     * 根据MusicId查询对象
     */
    MusicInfo getMusicInfoByMusicId(String musicId);


    /**
     * 根据MusicId修改
     */
    Integer updateMusicInfoByMusicId(MusicInfo bean, String musicId);


    /**
     * 根据MusicId删除
     */
    Integer deleteMusicInfoByMusicId(String musicId);


    /**
     * 根据TaskId查询对象
     */
    MusicInfo getMusicInfoByTaskId(String taskId);


    /**
     * 根据TaskId修改
     */
    Integer updateMusicInfoByTaskId(MusicInfo bean, String taskId);


    /**
     * 根据TaskId删除
     */
    Integer deleteMusicInfoByTaskId(String taskId);

    void musicCreateNotify(Integer musicType, String body);

    String updateCover(MultipartFile cover, String userId, String musicId);

    void updateMusicCount(String musicId);

    void musicCreated(MusicCreationResultDTO resultDTO);
}