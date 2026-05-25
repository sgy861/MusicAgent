package com.easymusic.controller;

import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.TokenUserInfo4AdminDTO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.redis.RedisComponent;
import com.easymusic.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 验证码
     */
    @RequestMapping(value = "/checkCode")
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = redisComponent.saveCheckCode(code);
        String checkCodeBase64 = captcha.toBase64();
        Map<String, String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);
        return getSuccessResponseVO(result);
    }

    @RequestMapping(value = "/login")
    public ResponseVO login(@NotEmpty String account,
                            @NotEmpty String password,
                            @NotEmpty String checkCode,
                            @NotEmpty String checkCodeKey) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            if (!account.equals(appConfig.getAdminAccount()) || !password.equals(StringTools.encodeByMD5(appConfig.getAdminPassword()))) {
                throw new BusinessException("账号或者密码错误");
            }
            TokenUserInfo4AdminDTO adminDTO = new TokenUserInfo4AdminDTO();
            adminDTO.setAccount(account);
            adminDTO.setToken(StringTools.getRandomString(Constants.LENGTH_30));
            redisComponent.saveTokenUserInfo4AdminDto(adminDTO);
            return getSuccessResponseVO(adminDTO.getToken());
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @RequestMapping(value = "/logout")
    public ResponseVO logout() {
        TokenUserInfo4AdminDTO tokenUserInfo4AdminDTO = getTokenUserInfo(null);
        if (tokenUserInfo4AdminDTO == null) {
            return getSuccessResponseVO(null);
        }
        redisComponent.cleanUserToken4Admin(tokenUserInfo4AdminDTO.getToken());
        return getSuccessResponseVO(null);
    }
}