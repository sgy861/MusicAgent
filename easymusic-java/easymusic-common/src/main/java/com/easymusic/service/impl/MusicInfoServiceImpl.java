package com.easymusic.service.impl;

import com.easymusic.api.MusicCreateApi;
import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.MusicCreationResultDTO;
import com.easymusic.entity.dto.MusicTaskDTO;
import com.easymusic.entity.enums.*;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.po.UserInfo;
import com.easymusic.entity.po.UserIntegralRecord;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.entity.query.UserInfoQuery;
import com.easymusic.entity.query.UserIntegralRecordQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.mappers.MusicInfoMapper;
import com.easymusic.mappers.UserInfoMapper;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.MusicInfoService;
import com.easymusic.service.UserIntegralRecordService;
import com.easymusic.spring.SpringContext;
import com.easymusic.utils.FileUtils;
import com.easymusic.utils.JsonUtils;
import com.easymusic.utils.StringTools;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;


/**
 * 业务接口实现
 */
@Service("musicInfoService")
@Slf4j
public class MusicInfoServiceImpl implements MusicInfoService {

    @Resource
    private MusicInfoMapper<MusicInfo, MusicInfoQuery> musicInfoMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private FileUtils fileUtils;

    @Resource
    private AppConfig appConfig;

    @Resource
    @Lazy
    private MusicInfoService musicInfoService;


    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<MusicInfo> findListByParam(MusicInfoQuery param) {
        return this.musicInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(MusicInfoQuery param) {
        return this.musicInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<MusicInfo> findListByPage(MusicInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<MusicInfo> list = this.findListByParam(param);
        PaginationResultVO<MusicInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(MusicInfo bean) {
        return this.musicInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<MusicInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.musicInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<MusicInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.musicInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(MusicInfo bean, MusicInfoQuery param) {
        StringTools.checkParam(param);
        return this.musicInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(MusicInfoQuery param) {
        StringTools.checkParam(param);
        return this.musicInfoMapper.deleteByParam(param);
    }

    /**
     * 根据MusicId获取对象
     */
    @Override
    public MusicInfo getMusicInfoByMusicId(String musicId) {
        MusicInfo musicInfo = this.musicInfoMapper.selectByMusicId(musicId);
        if (null != musicInfo) {
            UserInfo userInfo = this.userInfoMapper.selectByUserId(musicInfo.getUserId());
            musicInfo.setNickName(userInfo.getNickName());
        }
        return musicInfo;
    }

    /**
     * 根据MusicId修改
     */
    @Override
    public Integer updateMusicInfoByMusicId(MusicInfo bean, String musicId) {
        return this.musicInfoMapper.updateByMusicId(bean, musicId);
    }

    /**
     * 根据MusicId删除
     */
    @Override
    public Integer deleteMusicInfoByMusicId(String musicId) {
        return this.musicInfoMapper.deleteByMusicId(musicId);
    }

    /**
     * 根据TaskId获取对象
     */
    @Override
    public MusicInfo getMusicInfoByTaskId(String taskId) {
        return this.musicInfoMapper.selectByTaskId(taskId);
    }

    /**
     * 根据TaskId修改
     */
    @Override
    public Integer updateMusicInfoByTaskId(MusicInfo bean, String taskId) {
        return this.musicInfoMapper.updateByTaskId(bean, taskId);
    }

    /**
     * 根据TaskId删除
     */
    @Override
    public Integer deleteMusicInfoByTaskId(String taskId) {
        return this.musicInfoMapper.deleteByTaskId(taskId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void musicCreated(MusicCreationResultDTO resultDTO) {
        MusicInfo updateInfo = new MusicInfo();
        if (resultDTO.getCreateSuccess()) {
            updateInfo.setMusicTitle(resultDTO.getTitle());
            updateInfo.setDuration(resultDTO.getDuration());
            String lyrics = JsonUtils.convertObj2Json(resultDTO.getLyricsList());
            updateInfo.setLyrics(lyrics);
            updateInfo.setMusicStatus(MusicStatusEnum.CREATED.getStatus());
            String audioPath = fileUtils.downloadFile(resultDTO.getAudioUrl(), Constants.AUDIO_SUFFIX);
            updateInfo.setAudioPath(audioPath);
        } else {
            updateInfo.setMusicStatus(MusicStatusEnum.CRAETE_FAIL.getStatus());
            //退还用户积分
            MusicInfo musicInfo = this.musicInfoMapper.selectByTaskId(resultDTO.getTaskId());
            if (musicInfo == null) {
                throw new BusinessException("音乐不存在");
            }
            UserIntegralRecordQuery recordQuery = new UserIntegralRecordQuery();
            recordQuery.setUserId(musicInfo.getUserId());
            recordQuery.setBusinessId(musicInfo.getCreationId());
            List<UserIntegralRecord> list = this.userIntegralRecordService.findListByParam(recordQuery);
            UserIntegralRecord record = list.get(0);
            userIntegralRecordService.changeUserIntegral(UserIntegralRecordTypeEnum.CREATE_MUSIC_BACK, musicInfo.getCreationId(), musicInfo.getUserId(),
                    -record.getChangeIntegral(), null);
        }
        MusicInfoQuery musicInfoQuery = new MusicInfoQuery();
        musicInfoQuery.setTaskId(resultDTO.getTaskId());
        musicInfoQuery.setMusicStatus(MusicStatusEnum.CREATING.getStatus());
        Integer changeCount = this.musicInfoMapper.updateByParam(updateInfo, musicInfoQuery);
        if (changeCount == 0) {
            throw new BusinessException("更新音乐状态失败");
        }
    }

    @Override
    public void musicCreateNotify(Integer musicType, String body) {
        String apiCode = MusicTypeEnum.MUSIC.getType().equals(musicType) ? ModelType4MusicEnum.V3.getApiCode() : ModelType4PureMusicEnum.V3.getApiCode();
        MusicCreateApi musicCreateApi = (MusicCreateApi) SpringContext.getBean(apiCode);
        MusicCreationResultDTO resultDTO = musicCreateApi.createMusicNotify(musicType, body);
        if (resultDTO == null) {
            return;
        }
        musicInfoService.musicCreated(resultDTO);
    }

    @Override
    public String updateCover(MultipartFile cover, String userId, String musicId) {
        MusicInfo musicInfo = this.musicInfoMapper.selectByMusicId(musicId);
        if (musicInfo == null || !musicInfo.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String suffix = StringTools.getFileSuffix(cover.getOriginalFilename());
        String fileName = musicId + suffix;
        String coverPath = fileUtils.uploadFile(cover, null, fileName) + "&" + System.currentTimeMillis();
        MusicInfo updateInfo = new MusicInfo();
        updateInfo.setCover(coverPath);
        musicInfoMapper.updateByMusicId(updateInfo, musicId);
        return coverPath;
    }

    @Override
    public void updateMusicCount(String musicId) {
        this.musicInfoMapper.updateMusicCount(musicId);
    }

    /**
     * 轮训查询订单，针对无法使用回调的情况  实际开发中，会使用支回调来处理音乐生成情况
     * 比如本地开发，或者没有线上服务器的情况 API服务没法回调到你本地的服务器上，采用轮询查询处理。
     * 【已弃用】目前已重构为使用 RabbitMQ 延迟检查队列和死信队列进行长任务监控与超时处理。
     */
    @PostConstruct
    public void getMusicFromQueue() {
        // 已停用原 Redis 轮询逻辑，改用 RabbitMQ 延迟检查
        log.info("Redis long task check polling is deprecated. Using RabbitMQ now.");
    }
}