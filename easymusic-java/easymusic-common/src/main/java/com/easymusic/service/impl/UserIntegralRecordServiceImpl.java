package com.easymusic.service.impl;

import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.enums.UserIntegralRecordTypeEnum;
import com.easymusic.entity.po.UserInfo;
import com.easymusic.entity.po.UserIntegralRecord;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.entity.query.UserInfoQuery;
import com.easymusic.entity.query.UserIntegralRecordQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.mappers.UserInfoMapper;
import com.easymusic.mappers.UserIntegralRecordMapper;
import com.easymusic.service.UserIntegralRecordService;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * 用户积分记录信息 业务接口实现
 */
@Service("userIntegralRecordService")
public class UserIntegralRecordServiceImpl implements UserIntegralRecordService {

    private static final Logger log = LoggerFactory.getLogger(UserIntegralRecordServiceImpl.class);

    private static final String DEDUCT_LUA_SCRIPT =
            "if redis.call('exists', KEYS[1]) == 0 then " +
            "  return -1; " +
            "end; " +
            "local current = tonumber(redis.call('get', KEYS[1])); " +
            "local deduct = tonumber(ARGV[1]); " +
            "if current < deduct then " +
            "  return 0; " +
            "end; " +
            "redis.call('decrby', KEYS[1], deduct); " +
            "return 1; ";

    private final RedisScript<Long> deductScript = new DefaultRedisScript<>(DEDUCT_LUA_SCRIPT, Long.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserIntegralRecordMapper<UserIntegralRecord, UserIntegralRecordQuery> userIntegralRecordMapper;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserIntegralRecord> findListByParam(UserIntegralRecordQuery param) {
        return this.userIntegralRecordMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserIntegralRecordQuery param) {
        return this.userIntegralRecordMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserIntegralRecord> findListByPage(UserIntegralRecordQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserIntegralRecord> list = this.findListByParam(param);
        PaginationResultVO<UserIntegralRecord> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserIntegralRecord bean) {
        return this.userIntegralRecordMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserIntegralRecord> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userIntegralRecordMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserIntegralRecord> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userIntegralRecordMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserIntegralRecord bean, UserIntegralRecordQuery param) {
        StringTools.checkParam(param);
        return this.userIntegralRecordMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserIntegralRecordQuery param) {
        StringTools.checkParam(param);
        return this.userIntegralRecordMapper.deleteByParam(param);
    }

    /**
     * 根据RecordId获取对象
     */
    @Override
    public UserIntegralRecord getUserIntegralRecordByRecordId(Integer recordId) {
        return this.userIntegralRecordMapper.selectByRecordId(recordId);
    }

    /**
     * 根据RecordId修改
     */
    @Override
    public Integer updateUserIntegralRecordByRecordId(UserIntegralRecord bean, Integer recordId) {
        return this.userIntegralRecordMapper.updateByRecordId(bean, recordId);
    }

    /**
     * 根据RecordId删除
     */
    @Override
    public Integer deleteUserIntegralRecordByRecordId(Integer recordId) {
        return this.userIntegralRecordMapper.deleteByRecordId(recordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeUserIntegral(UserIntegralRecordTypeEnum recordTypeEnum, String businessId, String userId,
                                   Integer changeIntegral, BigDecimal amount) {
        Integer updateCount = this.userInfoMapper.changeUserIntegral(userId, changeIntegral);
        if (updateCount == 0) {
            throw new BusinessException("用户积分不足");
        }
        UserIntegralRecord records = new UserIntegralRecord();
        records.setChangeIntegral(changeIntegral);
        records.setUserId(userId);
        records.setCreateTime(new Date());
        records.setBusinessId(businessId);
        records.setRecordType(recordTypeEnum.getType());
        records.setAmount(amount);
        this.userIntegralRecordMapper.insert(records);
    }

    @Override
    public boolean preDeductUserQuota(String userId, int amount) {
        String key = com.easymusic.entity.constants.Constants.REDIS_KEY_USER_QUOTA + userId;
        Long result = redisTemplate.execute(deductScript, java.util.Collections.singletonList(key), String.valueOf(amount));
        
        if (result == null || result == -1) {
            log.info("Quota cache miss for user: {}. Loading from database.", userId);
            UserInfo userInfo = userInfoMapper.selectByUserId(userId);
            if (userInfo == null) {
                throw new BusinessException("用户不存在");
            }
            int currentIntegral = userInfo.getIntegral();
            redisTemplate.opsForValue().set(key, currentIntegral, 24, java.util.concurrent.TimeUnit.HOURS);
            
            result = redisTemplate.execute(deductScript, java.util.Collections.singletonList(key), String.valueOf(amount));
        }

        if (result != null && result == 1) {
            log.info("Successfully pre-deducted quota of {} for user: {} in Redis", amount, userId);
            return true;
        } else {
            log.warn("Failed to pre-deduct quota of {} for user: {} (insufficient quota)", amount, userId);
            throw new BusinessException("用户积分不足");
        }
    }

    @Override
    public void rebateUserQuota(String userId, int amount) {
        String key = com.easymusic.entity.constants.Constants.REDIS_KEY_USER_QUOTA + userId;
        Boolean hasKey = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(hasKey)) {
            redisTemplate.opsForValue().increment(key, amount);
            log.info("Rebated quota of {} for user: {} in Redis", amount, userId);
        }
    }
}