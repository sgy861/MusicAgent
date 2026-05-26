package com.easymusic.controller;

import com.easymusic.entity.enums.UserIntegralRecordTypeEnum;
import com.easymusic.entity.po.UserInfo;
import com.easymusic.entity.query.UserInfoQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.UserInfoService;
import com.easymusic.service.UserIntegralRecordService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.redis.core.RedisTemplate;

@RestController
@RequestMapping("/user")
@Slf4j
@Validated
public class UserInfoController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @RequestMapping("/loadUser")
    public ResponseVO loadUser(UserInfoQuery userInfoQuery) {
        userInfoQuery.setOrderBy("u.create_time desc");
        PaginationResultVO resultVO = userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/changeUserStatus")
    public ResponseVO changeUserStatus(@NotEmpty String userId, @NotNull Integer status) {
        UserInfo updateInfo = new UserInfo();
        updateInfo.setStatus(status);
        userInfoService.updateUserInfoByUserId(updateInfo, userId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/changeIntegral")
    public ResponseVO changeIntegral(@NotEmpty String userId, @NotNull Integer integral) {
        userIntegralRecordService.changeUserIntegral(integral < 0 ? UserIntegralRecordTypeEnum.ADMIN_DEDUCT : UserIntegralRecordTypeEnum.ADMIN_ADD, null, userId,
                integral, null);
        // 清除 Redis 中的配额缓存，强制从数据库重新加载以保持一致性
        String key = com.easymusic.entity.constants.Constants.REDIS_KEY_USER_QUOTA + userId;
        redisTemplate.delete(key);
        log.info("Evicted quota cache key: {} due to admin manual adjustment of {}", key, integral);
        return getSuccessResponseVO(null);
    }
}
