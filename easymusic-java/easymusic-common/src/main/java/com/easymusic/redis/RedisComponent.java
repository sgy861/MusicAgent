package com.easymusic.redis;

import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.MusicTaskDTO;
import com.easymusic.entity.dto.TokenUserInfo4AdminDTO;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.po.SysDict;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    public String saveCheckCode(String code) {
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, Constants.REDIS_KEY_EXPIRES_ONE_MIN * 10);
        return checkCodeKey;
    }

    public String getCheckCode(String checkCodeKey) {
        return (String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    public void cleanCheckCode(String checkCodeKey) {
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    /**
     * 后台登录
     *
     * @param token
     * @return
     */
    public TokenUserInfo4AdminDTO getTokenUserInfoDto4Admin(String token) {
        TokenUserInfo4AdminDTO tokenUserInfo4AdminDTO = (TokenUserInfo4AdminDTO) redisUtils.get(Constants.REDIS_KEY_TOKEN_ADMIN_USER + token);
        return tokenUserInfo4AdminDTO;
    }

    public void saveTokenUserInfo4AdminDto(TokenUserInfo4AdminDTO tokenUserInfoDto) {
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_ADMIN_USER + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_DAY * 2);
    }

    public void cleanUserToken4Admin(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_ADMIN_USER + token);
    }


    /**
     * 获取token信息
     *
     * @param token
     * @return
     */
    public TokenUserInfoDTO getTokenUserInfoDto(String token) {
        TokenUserInfoDTO tokenUserInfoDto = (TokenUserInfoDTO) redisUtils.get(Constants.REDIS_KEY_TOKEN_WEB_USER + token);
        return tokenUserInfoDto;
    }

    public void saveTokenUserInfoDto(TokenUserInfoDTO tokenUserInfoDto) {
        redisUtils.setex(Constants.REDIS_KEY_TOKEN_WEB_USER + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_DAY * 2);
    }

    public void cleanUserToken(String token) {
        redisUtils.delete(Constants.REDIS_KEY_TOKEN_WEB_USER + token);
    }

    public void addMusicCreateTask(MusicTaskDTO musicTaskDto) {
        long executeTime = System.currentTimeMillis() + 30 * 1000;
        redisUtils.zsetAdd(Constants.REDIS_KEY_MUSIC_CREATE_QUEUE, musicTaskDto, executeTime);
    }

    public Set<MusicTaskDTO> getMusicTaskDto() {
        return redisUtils.zsetRangeByScore(Constants.REDIS_KEY_MUSIC_CREATE_QUEUE, 0, System.currentTimeMillis());
    }

    public Long removeMusicTaskDto(MusicTaskDTO taskDto) {
        return redisUtils.zsetAddRemove(Constants.REDIS_KEY_MUSIC_CREATE_QUEUE, taskDto);
    }

    public void saveDict(String dictPcode, List<SysDict> sysDictList) {
        redisUtils.hset(Constants.REDIS_KEY_SYS_DICT, dictPcode, sysDictList);
    }

    public List<SysDict> getDictSubList(String dictPcode) {
        return (List<SysDict>) redisUtils.hget(Constants.REDIS_KEY_SYS_DICT, dictPcode);
    }

    public Map<String, List<SysDict>> getAllDict() {
        return (Map<String, List<SysDict>>) redisUtils.entries(Constants.REDIS_KEY_SYS_DICT);
    }

    public void addOrder2DelayQueue(Integer delayMin, String orderId) {
        long executeTime = System.currentTimeMillis() + delayMin * 60 * 1000;
        redisUtils.zsetAdd(Constants.REDIS_KEY_ORDER_DELAY_QUEUE, orderId, executeTime);
    }

    public Set<String> getTimeOutOrder() {
        return redisUtils.zsetRangeByScore(Constants.REDIS_KEY_ORDER_DELAY_QUEUE, 0, System.currentTimeMillis());
    }

    public Long removeTimeOutOrder(String orderId) {
        return redisUtils.zsetAddRemove(Constants.REDIS_KEY_ORDER_DELAY_QUEUE, orderId);
    }

    public void cacheHavePayOrder(String orderId) {
        redisUtils.setex(Constants.REDIS_KEY_ORDER_HAVE_PAY + orderId, orderId, Constants.REDIS_KEY_EXPIRES_ONE_MIN);
    }

    public String getHavePayOrder(String orderId) {
        return (String) redisUtils.get(Constants.REDIS_KEY_ORDER_HAVE_PAY + orderId);
    }
}
