package com.easymusic.task;

import com.easymusic.entity.po.MusicCreation;
import com.easymusic.entity.po.MusicInfo;
import com.easymusic.entity.po.SysDict;
import com.easymusic.entity.enums.AiTaskStatusEnum;
import com.easymusic.entity.enums.MusicStatusEnum;
import com.easymusic.entity.enums.MusicTypeEnum;
import com.easymusic.entity.query.MusicCreationQuery;
import com.easymusic.entity.query.MusicInfoQuery;
import com.easymusic.mappers.MusicCreationMapper;
import com.easymusic.mappers.MusicInfoMapper;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.UserIntegralRecordService;
import com.easymusic.utils.DateUtil;
import com.easymusic.entity.enums.DateTimePatternEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

/**
 * AI 任务状态机补偿定时任务。
 * 针对超过 5 分钟仍处于中间状态（QUOTA_FROZEN, AI_SUBMITTED, AI_PROCESSING）的 stuck 任务进行兜底清理，
 * 释放 Redis 冻结配额，并将任务状态置为 FAILED。
 */
@Component
@Slf4j
public class AiTaskCompensationJob {

    @Resource
    private MusicCreationMapper<MusicCreation, MusicCreationQuery> musicCreationMapper;

    @Resource
    private MusicInfoMapper<MusicInfo, MusicInfoQuery> musicInfoMapper;

    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 每30秒扫描一次
     */
    @Scheduled(fixedDelay = 30000)
    public void compensateStuckTasks() {
        try {
            // 1. 扫描处于中间状态的 MySQL stuck 任务
            Date fiveMinsAgo = new Date(System.currentTimeMillis() - 5 * 60 * 1000);
            String fiveMinsAgoStr = DateUtil.format(fiveMinsAgo, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern());

            List<Integer> stuckStatuses = Arrays.asList(
                    AiTaskStatusEnum.QUOTA_FROZEN.getStatus(),
                    AiTaskStatusEnum.AI_SUBMITTED.getStatus(),
                    AiTaskStatusEnum.AI_PROCESSING.getStatus()
            );

            for (Integer status : stuckStatuses) {
                MusicCreationQuery query = new MusicCreationQuery();
                query.setTaskStatus(status);
                query.setUpdateTimeEnd(fiveMinsAgoStr);
                List<MusicCreation> stuckCreations = musicCreationMapper.selectList(query);
                if (stuckCreations == null || stuckCreations.isEmpty()) {
                    continue;
                }

                for (MusicCreation creation : stuckCreations) {
                    log.warn("[AiTaskCompensationJob] Found stuck MusicCreation {} in status {} (last updated at {}). Compensating...",
                            creation.getCreationId(), status, creation.getUpdateTime());
                    compensateStuckCreation(creation, status);
                }
            }

            // 2. 扫描 Redis 中的孤儿冻结明细键（由于 JVM 崩溃导致 MySQL 无记录，但 Redis 冻结了）
            cleanOrphanRedisFreezes();

        } catch (Exception e) {
            log.error("[AiTaskCompensationJob] Error occurred during stuck tasks compensation", e);
        }
    }

    private void compensateStuckCreation(MusicCreation creation, Integer oldStatus) {
        try {
            // 获取积分单价
            MusicTypeEnum musicTypeEnum = MusicTypeEnum.getByType(creation.getMusicType());
            List<SysDict> sysDictSubList = redisComponent.getDictSubList(musicTypeEnum.getDictCode());
            Optional<SysDict> dictInfo = sysDictSubList.stream()
                    .filter(value -> value.getDictCode().equals(creation.getModel()))
                    .findFirst();
            int integral = 0;
            if (dictInfo.isPresent()) {
                integral = Integer.parseInt(dictInfo.get().getDictValue());
            }

            // 1. 更新 MusicCreation 状态为 FAILED (使用条件乐观锁更新)
            MusicCreation updateCreation = new MusicCreation();
            updateCreation.setTaskStatus(AiTaskStatusEnum.FAILED.getStatus());
            updateCreation.setUpdateTime(new Date());

            MusicCreationQuery mcQuery = new MusicCreationQuery();
            mcQuery.setCreationId(creation.getCreationId());
            mcQuery.setTaskStatus(oldStatus);

            Integer rows = musicCreationMapper.updateByParam(updateCreation, mcQuery);
            if (rows > 0) {
                // 2. 释放 Redis 冻结配额 (Cancel)
                userIntegralRecordService.cancelFreeze(creation.getCreationId(), creation.getUserId(), integral);

                // 3. 将其关联的所有未完成 MusicInfo 子任务强制置为失败
                MusicInfoQuery infoQuery = new MusicInfoQuery();
                infoQuery.setCreationId(creation.getCreationId());
                infoQuery.setMusicStatus(MusicStatusEnum.CREATING.getStatus());

                MusicInfo updateInfo = new MusicInfo();
                updateInfo.setMusicStatus(MusicStatusEnum.CRAETE_FAIL.getStatus());
                musicInfoMapper.updateByParam(updateInfo, infoQuery);

                log.info("[AiTaskCompensationJob] Successfully compensated stuck MusicCreation {} for user {}. Status updated to FAILED, {} points returned.",
                        creation.getCreationId(), creation.getUserId(), integral);
            } else {
                log.warn("[AiTaskCompensationJob] MusicCreation {} was already completed or updated by another thread. Skipping compensation.", creation.getCreationId());
            }
        } catch (Exception e) {
            log.error("[AiTaskCompensationJob] Failed to compensate stuck MusicCreation: {}", creation.getCreationId(), e);
        }
    }

    private void cleanOrphanRedisFreezes() {
        try {
            String pattern = "easymusic:quota:freeze:detail:*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                return;
            }
            long now = System.currentTimeMillis();
            for (String key : keys) {
                // key 格式: easymusic:quota:freeze:detail:<creationId>
                String creationId = key.substring("easymusic:quota:freeze:detail:".length());
                Object valueObj = redisTemplate.opsForValue().get(key);
                if (valueObj == null) {
                    continue;
                }
                String value = valueObj.toString();
                // value 格式: userId:amount:timestamp
                String[] parts = value.split(":");
                if (parts.length < 3) {
                    continue;
                }
                String userId = parts[0];
                int amount = Integer.parseInt(parts[1]);
                long timestamp = Long.parseLong(parts[2]);

                // 超过 5 分钟的孤儿冻结，进行清理检查
                if (now - timestamp > 5 * 60 * 1000) {
                    log.warn("[AiTaskCompensationJob] Found Redis freeze detail matching key {} that is older than 5 minutes. Checking MySQL...", key);
                    MusicCreation mc = musicCreationMapper.selectByCreationId(creationId);
                    if (mc == null) {
                        // MySQL 无此记录，判定为 JVM 崩溃在提交前导致。直接清理 Redis 冻结！
                        log.error("[AiTaskCompensationJob] JVM crash detected. MySQL record not found for creationId {}. Cleaning up Redis quota freeze of {} points for user {}", 
                                creationId, amount, userId);
                        userIntegralRecordService.cancelFreeze(creationId, userId, amount);
                    } else if (AiTaskStatusEnum.FAILED.getStatus().equals(mc.getTaskStatus())) {
                        // MySQL 存在但已经是 FAILED 状态，安全兜底清理 detail key
                        log.info("[AiTaskCompensationJob] MySQL record for creationId {} is already FAILED. Cleaning up Redis detail key.", creationId);
                        redisTemplate.delete(key);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[AiTaskCompensationJob] Failed to clean orphan Redis freezes", e);
        }
    }
}
