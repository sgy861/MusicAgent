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
import com.easymusic.entity.po.MusicCreation;
import com.easymusic.entity.po.SysDict;
import com.easymusic.entity.query.MusicCreationQuery;
import com.easymusic.mappers.MusicCreationMapper;
import java.util.Optional;
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
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
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

    @Resource
    private MusicCreationMapper<MusicCreation, MusicCreationQuery> musicCreationMapper;

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
        }

        MusicInfoQuery musicInfoQuery = new MusicInfoQuery();
        musicInfoQuery.setTaskId(resultDTO.getTaskId());
        musicInfoQuery.setMusicStatus(MusicStatusEnum.CREATING.getStatus());

        MusicInfo musicInfo = this.musicInfoMapper.selectByTaskId(resultDTO.getTaskId());
        if (musicInfo == null) {
            throw new BusinessException("音乐不存在");
        }

        Integer changeCount = this.musicInfoMapper.updateByParam(updateInfo, musicInfoQuery);
        if (changeCount == 0) {
            throw new BusinessException("更新音乐状态失败");
        }

        // --- State Machine & Quota Confirm/Cancel ---
        MusicCreation musicCreation = this.musicCreationMapper.selectByCreationId(musicInfo.getCreationId());
        if (musicCreation != null) {
            MusicTypeEnum musicTypeEnum = MusicTypeEnum.getByType(musicCreation.getMusicType());
            List<SysDict> sysDictSubList = redisComponent.getDictSubList(musicTypeEnum.getDictCode());
            Optional<SysDict> dictInfo = sysDictSubList.stream()
                    .filter(value -> value.getDictCode().equals(musicCreation.getModel()))
                    .findFirst();
            int integral = 0;
            if (dictInfo.isPresent()) {
                integral = Integer.parseInt(dictInfo.get().getDictValue());
            }

            // Check if this creation task is not finalized yet
            if (AiTaskStatusEnum.AI_SUBMITTED.getStatus().equals(musicCreation.getTaskStatus()) 
                    || AiTaskStatusEnum.AI_PROCESSING.getStatus().equals(musicCreation.getTaskStatus())
                    || AiTaskStatusEnum.QUOTA_FROZEN.getStatus().equals(musicCreation.getTaskStatus())) {
                
                if (resultDTO.getCreateSuccess()) {
                    // Update state to COMPLETED using conditional update (optimistic lock)
                    MusicCreation updateCreation = new MusicCreation();
                    updateCreation.setTaskStatus(AiTaskStatusEnum.COMPLETED.getStatus());
                    updateCreation.setUpdateTime(new Date());
                    
                    MusicCreationQuery query = new MusicCreationQuery();
                    query.setCreationId(musicCreation.getCreationId());
                    query.setTaskStatus(musicCreation.getTaskStatus()); // Must match current status
                    
                    Integer rows = musicCreationMapper.updateByParam(updateCreation, query);
                    if (rows > 0) {
                        // Success: Confirm freeze and deduct database points!
                        userIntegralRecordService.confirmFreeze(musicCreation.getCreationId(), musicCreation.getUserId(), integral);
                        userIntegralRecordService.changeUserIntegral(UserIntegralRecordTypeEnum.CREATE_MUSIC, 
                                musicCreation.getCreationId(), musicCreation.getUserId(), -integral, null);
                        log.info("[FSM] MusicCreation {} succeeded. Confirmed freeze of {} points.", 
                                musicCreation.getCreationId(), integral);
                    } else {
                        log.warn("[FSM] MusicCreation {} status was already finalized by another thread, skipping confirm.", 
                                musicCreation.getCreationId());
                    }
                } else {
                    // Failure of this task. Check if all tasks under this creation failed.
                    MusicInfoQuery query = new MusicInfoQuery();
                    query.setCreationId(musicCreation.getCreationId());
                    List<MusicInfo> siblingMusicInfos = this.musicInfoMapper.selectList(query);
                    
                    boolean allFailed = true;
                    boolean anySucceeded = false;
                    for (MusicInfo info : siblingMusicInfos) {
                        if (MusicStatusEnum.CREATING.getStatus().equals(info.getMusicStatus())) {
                            allFailed = false; // still running
                        } else if (MusicStatusEnum.CREATED.getStatus().equals(info.getMusicStatus())) {
                            anySucceeded = true;
                        }
                    }
                    
                    if (allFailed && !anySucceeded) {
                        // All sibling tasks failed! Cancel the freeze and refund points (no DB deduct happened).
                        // Conditional update to FAILED
                        MusicCreation updateCreation = new MusicCreation();
                        updateCreation.setTaskStatus(AiTaskStatusEnum.FAILED.getStatus());
                        updateCreation.setUpdateTime(new Date());
                        
                        MusicCreationQuery mcQuery = new MusicCreationQuery();
                        mcQuery.setCreationId(musicCreation.getCreationId());
                        mcQuery.setTaskStatus(musicCreation.getTaskStatus());
                        
                        Integer rows = musicCreationMapper.updateByParam(updateCreation, mcQuery);
                        if (rows > 0) {
                            userIntegralRecordService.cancelFreeze(musicCreation.getCreationId(), musicCreation.getUserId(), integral);
                            log.info("[FSM] MusicCreation {} completely failed. Cancelled freeze of {} points.", 
                                    musicCreation.getCreationId(), integral);
                        } else {
                            log.warn("[FSM] MusicCreation {} status was already finalized, skipping cancel.", 
                                    musicCreation.getCreationId());
                        }
                    } else if (anySucceeded) {
                        // Some succeeded, but this task failed. Update state to COMPLETED conditionally.
                        MusicCreation updateCreation = new MusicCreation();
                        updateCreation.setTaskStatus(AiTaskStatusEnum.COMPLETED.getStatus());
                        updateCreation.setUpdateTime(new Date());
                        
                        MusicCreationQuery mcQuery = new MusicCreationQuery();
                        mcQuery.setCreationId(musicCreation.getCreationId());
                        mcQuery.setTaskStatus(musicCreation.getTaskStatus());
                        
                        musicCreationMapper.updateByParam(updateCreation, mcQuery);
                        log.info("[FSM] MusicCreation {} task failed but at least one task succeeded. State -> COMPLETED.", 
                                musicCreation.getCreationId());
                    } else {
                        // Still has other tasks in progress. Update status to AI_PROCESSING conditionally.
                        MusicCreation updateCreation = new MusicCreation();
                        updateCreation.setTaskStatus(AiTaskStatusEnum.AI_PROCESSING.getStatus());
                        updateCreation.setUpdateTime(new Date());
                        
                        MusicCreationQuery mcQuery = new MusicCreationQuery();
                        mcQuery.setCreationId(musicCreation.getCreationId());
                        mcQuery.setTaskStatus(musicCreation.getTaskStatus());
                        
                        musicCreationMapper.updateByParam(updateCreation, mcQuery);
                        log.info("[FSM] MusicCreation {} task failed, but other tasks are still running. State -> AI_PROCESSING.", 
                                musicCreation.getCreationId());
                    }
                }
            }
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
}