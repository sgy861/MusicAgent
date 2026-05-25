package com.easymusic.controller;

import com.easymusic.entity.enums.ResponseCodeEnum;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class AGlobalExceptionHandlerController extends ABaseController {
    @ExceptionHandler(value = Exception.class)
    Object handleException(Exception e, HttpServletRequest request) {
        log.error("请求错误，请求地址{},错误信息:", request.getRequestURL(), e);
        ResponseVO responseVO = new ResponseVO();
        //404
        if (e instanceof NoHandlerFoundException || e instanceof NoResourceFoundException) {
            responseVO.setCode(ResponseCodeEnum.CODE_404.getCode());
            responseVO.setInfo(ResponseCodeEnum.CODE_404.getMsg());
            responseVO.setStatus(STATUC_ERROR);
        } else if (e instanceof BusinessException) {
            //业务错误
            BusinessException biz = (BusinessException) e;
            responseVO.setCode(biz.getCode() == null ? ResponseCodeEnum.CODE_600.getCode() : biz.getCode());
            responseVO.setInfo(biz.getMessage());
            responseVO.setStatus(STATUC_ERROR);
        } else if (e instanceof ConstraintViolationException || e instanceof BindException || e instanceof MethodArgumentTypeMismatchException) {
            //参数类型错误
            responseVO.setCode(ResponseCodeEnum.CODE_600.getCode());
            responseVO.setInfo(ResponseCodeEnum.CODE_600.getMsg());
            responseVO.setStatus(STATUC_ERROR);
        } else if (e instanceof DuplicateKeyException) {
            //主键冲突
            responseVO.setCode(ResponseCodeEnum.CODE_601.getCode());
            responseVO.setInfo(ResponseCodeEnum.CODE_601.getMsg());
            responseVO.setStatus(STATUC_ERROR);
        } else {
            responseVO.setCode(ResponseCodeEnum.CODE_500.getCode());
            responseVO.setInfo(ResponseCodeEnum.CODE_500.getMsg());
            responseVO.setStatus(STATUC_ERROR);
        }
        return responseVO;
    }
}
