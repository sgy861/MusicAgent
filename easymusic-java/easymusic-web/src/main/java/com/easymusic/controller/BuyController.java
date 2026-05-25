package com.easymusic.controller;

import com.easymusic.annotation.GlobalInterceptor;
import com.easymusic.entity.dto.PayInfoDTO;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.enums.ProductOnSaleTypeEnum;
import com.easymusic.entity.po.ProductInfo;
import com.easymusic.entity.query.ProductInfoQuery;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.PayOrderInfoService;
import com.easymusic.service.ProductInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/buy")
public class BuyController extends ABaseController {

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private PayOrderInfoService payOrderInfoService;

    @Resource
    private RedisComponent redisComponent;


    @RequestMapping("/loadProduct")
    @GlobalInterceptor
    public ResponseVO loadProduct() {
        ProductInfoQuery productInfoQuery = new ProductInfoQuery();
        productInfoQuery.setOrderBy("p.sort asc");
        productInfoQuery.setOnsaleType(ProductOnSaleTypeEnum.ON_SALE.getType());
        List<ProductInfo> productInfoList = productInfoService.findListByParam(productInfoQuery);
        return getSuccessResponseVO(productInfoList);
    }


    @RequestMapping("/getPayInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getPayInfo(@NotEmpty String productId, @NotNull Integer payType) {
        PayInfoDTO payInfoDTO = payOrderInfoService.getPayInfo(getTokenUserInfo(null), productId, payType);
        return getSuccessResponseVO(payInfoDTO);
    }

    /**
     * 校验支付
     *
     * @param orderId
     * @return
     */
    @RequestMapping("/checkPayOrder")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO checkPayOrder(@NotEmpty String orderId) {
        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo(null);
        Integer integral = payOrderInfoService.checkPay(tokenUserInfoDTO.getUserId(), orderId);
        if (integral == null) {
            return getSuccessResponseVO(null);
        }
        tokenUserInfoDTO.setIntegral(integral);
        return getSuccessResponseVO(tokenUserInfoDTO);
    }

    @RequestMapping("/havePay")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO havePay(@NotEmpty String orderId) {
        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo(null);
        Integer integral = payOrderInfoService.havePay(tokenUserInfoDTO.getUserId(), orderId);
        if (integral == null) {
            return getSuccessResponseVO(null);
        }
        tokenUserInfoDTO.setIntegral(integral);
        return getSuccessResponseVO(tokenUserInfoDTO);
    }

    @RequestMapping(value = "/buyByPayCode")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO buyByPayCode(@NotEmpty String checkCodeKey,
                                   @NotEmpty String checkCode,
                                   @NotEmpty String payCode,
                                   @NotEmpty String productId) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo(null);
            payOrderInfoService.buyByPayCode(productId, payCode, tokenUserInfoDTO.getUserId());
            return getSuccessResponseVO(null);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }
}
