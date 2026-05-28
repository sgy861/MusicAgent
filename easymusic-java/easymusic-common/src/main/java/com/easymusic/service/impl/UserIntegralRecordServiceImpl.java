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

    private static final String FREEZE_LUA_SCRIPT =
            "if redis.call('exists', KEYS[1]) == 0 then " +
            "  return -1; " +
            "end; " +
            "local available = tonumber(redis.call('get', KEYS[1])); " +
            "local deduct = tonumber(ARGV[1]); " +
            "if available < deduct then " +
            "  return 0; " +
            "end; " +
            "redis.call('decrby', KEYS[1], deduct); " +
            "redis.call('incrby', KEYS[2], deduct); " +
            "return 1; ";

    private static final String CONFIRM_LUA_SCRIPT =
            "local frozen = tonumber(redis.call('get', KEYS[1]) or 0); " +
            "local amount = tonumber(ARGV[1]); " +
            "if frozen >= amount then " +
            "  redis.call('decrby', KEYS[1], amount); " +
            "end; " +
            "return 1; ";

    private static final String CANCEL_LUA_SCRIPT =
            "local amount = tonumber(ARGV[1]); " +
            "redis.call('incrby', KEYS[1], amount); " +
            "local frozen = tonumber(redis.call('get', KEYS[2]) or 0); " +
            "if frozen >= amount then " +
            "  redis.call('decrby', KEYS[2], amount); " +
            "end; " +
            "return 1; ";

    private final RedisScript<Long> deductScript = new DefaultRedisScript<>(DEDUCT_LUA_SCRIPT, Long.class);
    private final RedisScript<Long> freezeScript = new DefaultRedisScript<>(FREEZE_LUA_SCRIPT, Long.class);
    private final RedisScript<Long> confirmScript = new DefaultRedisScript<>(CONFIRM_LUA_SCRIPT, Long.class);
    private final RedisScript<Long> cancelScript = new DefaultRedisScript<>(CANCEL_LUA_SCRIPT, Long.class);

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

    @Override
    public boolean freezeQuota(String creationId, String userId, int amount) {
        String availableKey = com.easymusic.entity.constants.Constants.REDIS_KEY_USER_QUOTA + userId;
        String frozenKey = com.easymusic.entity.constants.Constants.REDIS_KEY_USER_QUOTA_FROZEN + userId;
        
        Long result = redisTemplate.execute(freezeScript, 
                java.util.Arrays.asList(availableKey, frozenKey), 
                String.valueOf(amount));
        
        if (result == null || result == -1) {
            log.info("Quota cache miss for user: {} during freeze. Loading from database.", userId);
            UserInfo userInfo = userInfoMapper.selectByUserId(userId);
            if (userInfo == null) {
                throw new BusinessException("用户不存在");
            }
            int currentIntegral = userInfo.getIntegral();
            redisTemplate.opsForValue().set(availableKey, currentIntegral, 24, java.util.concurrent.TimeUnit.HOURS);
            
            result = redisTemplate.execute(freezeScript, 
                    java.util.Arrays.asList(availableKey, frozenKey), 
                    String.valueOf(amount));
        }

        if (result != null && result == 1) {
            // Write detail key to Redis with 1 hour TTL
            String detailKey = "easymusic:quota:freeze:detail:" + creationId;
            String detailValue = userId + ":" + amount + ":" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(detailKey, detailValue, 1, java.util.concurrent.TimeUnit.HOURS);
            log.info("Successfully froze quota of {} for user: {} in Redis (creationId: {})", amount, userId, creationId);
            return true;
        } else {
            log.warn("Failed to freeze quota of {} for user: {} (insufficient quota)", amount, userId);
            throw new BusinessException("用户积分不足");
        }
    }

    @Override
    public void confirmFreeze(String creationId, String userId, int amount) {
        String frozenKey = com.easymusic.entity.constants.Constants.REDIS_KEY_USER_QUOTA_FROZEN + userId;
        String detailKey = "easymusic:quota:freeze:detail:" + creationId;
        
        redisTemplate.execute(confirmScript, 
                java.util.Collections.singletonList(frozenKey), 
                String.valueOf(amount));
        
        redisTemplate.delete(detailKey);
        log.info("Confirmed quota freeze of {} for user: {} in Redis (creationId: {})", amount, userId, creationId);
    }

    @Override
    public void cancelFreeze(String creationId, String userId, int amount) {
        String detailKey = "easymusic:quota:freeze:detail:" + creationId;
        // Idempotency: only cancel if the detail key exists
        Boolean exists = redisTemplate.hasKey(detailKey);
        if (Boolean.FALSE.equals(exists)) {
            log.info("Quota freeze for creationId: {} has already been processed or does not exist. Skipping cancelFreeze.", creationId);
            return;
        }

        String availableKey = com.easymusic.entity.constants.Constants.REDIS_KEY_USER_QUOTA + userId;
        String frozenKey = com.easymusic.entity.constants.Constants.REDIS_KEY_USER_QUOTA_FROZEN + userId;
        
        redisTemplate.execute(cancelScript, 
                java.util.Arrays.asList(availableKey, frozenKey), 
                String.valueOf(amount));
        
        redisTemplate.delete(detailKey);
        log.info("Cancelled quota freeze of {} (rebated to available) for user: {} in Redis (creationId: {})", amount, userId, creationId);
    }
}