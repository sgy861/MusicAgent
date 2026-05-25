package com.easymusic.controller;

import com.easymusic.annotation.GlobalInterceptor;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.po.UserInfo;
import com.easymusic.entity.vo.CheckCodeVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.UserInfoService;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    /**
     * 验证码
     */
    @RequestMapping(value = "/checkCode")
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = redisComponent.saveCheckCode(code);
        String checkCodeBase64 = captcha.toBase64();
        CheckCodeVO checkCodeVO = new CheckCodeVO();
        checkCodeVO.setCheckCode(checkCodeBase64);
        checkCodeVO.setCheckCodeKey(checkCodeKey);
        return getSuccessResponseVO(checkCodeVO);
    }

    @RequestMapping(value = "/register")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty String password,
                               @NotEmpty String nickName,
                               @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            userInfoService.register(email, nickName, password);
            return getSuccessResponseVO(null);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @RequestMapping(value = "/login")
    public ResponseVO login(@NotEmpty String checkCodeKey,
                            @NotEmpty @Email String email,
                            @NotEmpty String password,
                            @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            TokenUserInfoDTO tokenUserInfoDTO = userInfoService.login(email, password);
            return getSuccessResponseVO(tokenUserInfoDTO);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @RequestMapping(value = "/logout")
    public ResponseVO logout() {
        TokenUserInfoDTO tokenUserInfoDto = getTokenUserInfo(null);
        if (tokenUserInfoDto == null) {
            return getSuccessResponseVO(null);
        }
        redisComponent.cleanUserToken(tokenUserInfoDto.getToken());
        return getSuccessResponseVO(null);
    }

    @RequestMapping(value = "/updateUserInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO updateUserInfo(MultipartFile avatar, @NotEmpty @Size(max = 20) String nickName) {
        TokenUserInfoDTO tokenUserInfoDto = getTokenUserInfo(null);
        userInfoService.updateUserInfo(tokenUserInfoDto, avatar, nickName);
        return getSuccessResponseVO(tokenUserInfoDto);
    }

    @RequestMapping(value = "/updatePassword")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO updatePassword(@NotEmpty String oldPassword, @NotEmpty String password) {
        TokenUserInfoDTO tokenUserInfoDto = getTokenUserInfo(null);
        userInfoService.updatePassword(tokenUserInfoDto.getUserId(), oldPassword, password);
        return getSuccessResponseVO(null);
    }

    @RequestMapping(value = "/getLoginInfo")
    public ResponseVO getLoginInfo() {
        TokenUserInfoDTO tokenUserInfoDto = getTokenUserInfo(null);
        if (tokenUserInfoDto == null) {
            return getSuccessResponseVO(null);
        }
        //续期token
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
        UserInfo userInfo = this.userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
        tokenUserInfoDto.setIntegral(userInfo.getIntegral());
        return getSuccessResponseVO(tokenUserInfoDto);
    }
}
