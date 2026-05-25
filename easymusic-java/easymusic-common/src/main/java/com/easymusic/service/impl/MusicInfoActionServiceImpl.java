package com.easymusic.service.impl;

import com.easymusic.entity.enums.MusicActionTypeEnum;
import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.po.MusicInfoAction;
import com.easymusic.entity.query.MusicInfoActionQuery;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.mappers.MusicInfoActionMapper;
import com.easymusic.mappers.MusicInfoMapper;
import com.easymusic.service.MusicInfoActionService;
import com.easymusic.utils.StringTools;
import com.easymusic.config.RabbitConfig;
import com.easymusic.redis.RedisUtils;
import com.easymusic.entity.constants.Constants;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 业务接口实现
 */
@Service("musicInfoActionService")
public class MusicInfoActionServiceImpl implements MusicInfoActionService {

    @Resource
    private MusicInfoActionMapper<MusicInfoAction, MusicInfoActionQuery> musicInfoActionMapper;

    @Resource
    private MusicInfoMapper<MusicInfo, MusicInfoQuery> musicInfoMapper;

    @Resource
    private RedisUtils<Object> redisUtils;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<MusicInfoAction> findListByParam(MusicInfoActionQuery param) {
        return this.musicInfoActionMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(MusicInfoActionQuery param) {
        return this.musicInfoActionMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<MusicInfoAction> findListByPage(MusicInfoActionQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<MusicInfoAction> list = this.findListByParam(param);
        PaginationResultVO<MusicInfoAction> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(MusicInfoAction bean) {
        return this.musicInfoActionMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<MusicInfoAction> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.musicInfoActionMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<MusicInfoAction> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.musicInfoActionMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(MusicInfoAction bean, MusicInfoActionQuery param) {
        StringTools.checkParam(param);
        return this.musicInfoActionMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(MusicInfoActionQuery param) {
        StringTools.checkParam(param);
        return this.musicInfoActionMapper.deleteByParam(param);
    }

    /**
     * 根据ActionId获取对象
     */
    @Override
    public MusicInfoAction getMusicInfoActionByActionId(Integer actionId) {
        return this.musicInfoActionMapper.selectByActionId(actionId);
    }

    /**
     * 根据ActionId修改
     */
    @Override
    public Integer updateMusicInfoActionByActionId(MusicInfoAction bean, Integer actionId) {
        return this.musicInfoActionMapper.updateByActionId(bean, actionId);
    }

    /**
     * 根据ActionId删除
     */
    @Override
    public Integer deleteMusicInfoActionByActionId(Integer actionId) {
        return this.musicInfoActionMapper.deleteByActionId(actionId);
    }

    /**
     * 根据MusicIdAndUserId获取对象
     */
    @Override
    public MusicInfoAction getMusicInfoActionByMusicIdAndUserId(String musicId, String userId) {
        return this.musicInfoActionMapper.selectByMusicIdAndUserId(musicId, userId);
    }

    /**
     * 根据MusicIdAndUserId修改
     */
    @Override
    public Integer updateMusicInfoActionByMusicIdAndUserId(MusicInfoAction bean, String musicId, String userId) {
        return this.musicInfoActionMapper.updateByMusicIdAndUserId(bean, musicId, userId);
    }

    /**
     * 根据MusicIdAndUserId删除
     */
    @Override
    public Integer deleteMusicInfoActionByMusicIdAndUserId(String musicId, String userId) {
        return this.musicInfoActionMapper.deleteByMusicIdAndUserId(musicId, userId);
    }

    @Override
    public void doGood(String musicId, String userId) {
        MusicInfoAction musicInfoAction = this.musicInfoActionMapper.selectByMusicIdAndUserId(musicId, userId);
        boolean changed = false;
        if (musicInfoAction != null) {
            this.musicInfoActionMapper.deleteByMusicIdAndUserId(musicId, userId);
            changed = true;
        } else {
            MusicInfo musicInfo = this.musicInfoMapper.selectByMusicId(musicId);
            if (musicInfo != null) {
                musicInfoAction = new MusicInfoAction();
                musicInfoAction.setMusicId(musicId);
                musicInfoAction.setMusicUserId(musicInfo.getUserId());
                musicInfoAction.setActionType(MusicActionTypeEnum.GOOD.getType());
                musicInfoAction.setUserId(userId);
                this.musicInfoActionMapper.insert(musicInfoAction);
                changed = true;
            }
        }

        if (changed) {
            try {
                // 设置画像脏数据标记，并向延迟队列投递更新消息，用于30秒防抖合并处理
                redisUtils.setex(Constants.REDIS_KEY_USER_PROFILE_DIRTY + userId, true, Constants.REDIS_KEY_EXPIRES_DAY);
                rabbitTemplate.convertAndSend(RabbitConfig.USER_BEHAVIOR_DELAY_QUEUE, userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}