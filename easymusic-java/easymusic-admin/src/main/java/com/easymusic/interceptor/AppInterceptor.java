package com.easymusic.interceptor;

import com.easymusic.entity.dto.TokenUserInfo4AdminDTO;
import com.easymusic.entity.enums.ResponseCodeEnum;
import com.easymusic.exception.BusinessException;
import com.easymusic.redis.RedisComponent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class AppInterceptor implements HandlerInterceptor {

    private final static String URL_ACCOUNT = "/account";
    private final static String URL_FILE = "/file";

    @Resource
    private RedisComponent redisComponent;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (null == handler) {
            return false;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        return checkLogin();
    }

    //校验登录
    private boolean checkLogin() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String url = request.getRequestURI();
        if (url.indexOf(URL_ACCOUNT) != -1 || url.indexOf(URL_FILE) != -1) {
            return true;
        }
        String token = request.getHeader("token");
        TokenUserInfo4AdminDTO tokenUserInfoDto = redisComponent.getTokenUserInfoDto4Admin(token);
        if (System.getProperty("dev") != null) {
            tokenUserInfoDto = new TokenUserInfo4AdminDTO();
            tokenUserInfoDto.setAccount("admin");
            tokenUserInfoDto.setToken(token);
            redisComponent.saveTokenUserInfo4AdminDto(tokenUserInfoDto);
        }
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}