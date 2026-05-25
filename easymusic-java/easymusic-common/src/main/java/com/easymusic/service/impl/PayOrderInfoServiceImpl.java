package com.easymusic.service.impl;

import com.easymusic.api.PayChannelService;
import com.easymusic.entity.config.AppConfig;
import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.dto.PayInfoDTO;
import com.easymusic.entity.dto.PayOrderNotifyDTO;
import com.easymusic.entity.dto.TokenUserInfoDTO;
import com.easymusic.entity.enums.*;
import com.easymusic.entity.po.PayCodeInfo;
import com.easymusic.entity.po.PayOrderInfo;
import com.easymusic.entity.po.ProductInfo;
import com.easymusic.entity.po.UserInfo;
import com.easymusic.entity.query.PayCodeInfoQuery;
import com.easymusic.entity.query.PayOrderInfoQuery;
import com.easymusic.entity.query.ProductInfoQuery;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.mappers.PayCodeInfoMapper;
import com.easymusic.mappers.PayOrderInfoMapper;
import com.easymusic.mappers.ProductInfoMapper;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.PayOrderInfoService;
import com.easymusic.service.UserInfoService;
import com.easymusic.service.UserIntegralRecordService;
import com.easymusic.spring.SpringContext;
import com.easymusic.utils.DateUtil;
import com.easymusic.utils.StringTools;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 业务接口实现
 */
@Service("payOrderInfoService")
@Slf4j
public class PayOrderInfoServiceImpl implements PayOrderInfoService {


    @Resource
    private PayOrderInfoMapper<PayOrderInfo, PayOrderInfoQuery> payOrderInfoMapper;

    @Resource
    private ProductInfoMapper<ProductInfo, ProductInfoQuery> productInfoMapper;

    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private AppConfig appConfig;

    @Resource
    private PayCodeInfoMapper<PayCodeInfo, PayCodeInfoQuery> payCodeInfoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<PayOrderInfo> findListByParam(PayOrderInfoQuery param) {
        return this.payOrderInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(PayOrderInfoQuery param) {
        return this.payOrderInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<PayOrderInfo> findListByPage(PayOrderInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<PayOrderInfo> list = this.findListByParam(param);
        PaginationResultVO<PayOrderInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(PayOrderInfo bean) {
        return this.payOrderInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<PayOrderInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.payOrderInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<PayOrderInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.payOrderInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(PayOrderInfo bean, PayOrderInfoQuery param) {
        StringTools.checkParam(param);
        return this.payOrderInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(PayOrderInfoQuery param) {
        StringTools.checkParam(param);
        return this.payOrderInfoMapper.deleteByParam(param);
    }

    /**
     * 根据OrderId获取对象
     */
    @Override
    public PayOrderInfo getPayOrderInfoByOrderId(String orderId) {
        return this.payOrderInfoMapper.selectByOrderId(orderId);
    }

    /**
     * 根据OrderId修改
     */
    @Override
    public Integer updatePayOrderInfoByOrderId(PayOrderInfo bean, String orderId) {
        return this.payOrderInfoMapper.updateByOrderId(bean, orderId);
    }

    /**
     * 根据OrderId删除
     */
    @Override
    public Integer deletePayOrderInfoByOrderId(String orderId) {
        return this.payOrderInfoMapper.deleteByOrderId(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayInfoDTO getPayInfo(TokenUserInfoDTO tokenUserInfoDTO, String productId, Integer payType) {
        ProductInfo productInfo = this.productInfoMapper.selectByProductId(productId);
        if (null == productInfo || !ProductOnSaleTypeEnum.ON_SALE.getType().equals(productInfo.getOnsaleType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        PayOrderTypeEnum payTypeEnum = PayOrderTypeEnum.getByType(payType);
        if (payTypeEnum == null || PayOrderTypeEnum.PAY_CODE == payTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        PayChannelService payChannelService = (PayChannelService) SpringContext.getBean(payTypeEnum.getBeanName());
        String orderId = getOrderId();
        String payUrl = payChannelService.getPayUrl(orderId, productInfo.getPrice(), productInfo.getProductName());
        PayOrderInfo payOrderInfo = new PayOrderInfo();
        payOrderInfo.setOrderId(orderId);
        payOrderInfo.setCreateTime(new Date());
        payOrderInfo.setIntegral(productInfo.getIntegral());
        payOrderInfo.setUserId(tokenUserInfoDTO.getUserId());
        payOrderInfo.setAmount(productInfo.getPrice());
        payOrderInfo.setProductId(productInfo.getProductId());
        payOrderInfo.setProductName(productInfo.getProductName());
        payOrderInfo.setStatus(PayOrderStatusEnum.NO_PAY.getStatus());
        payOrderInfo.setPayType(payType);
        payOrderInfo.setPayInfo(payUrl);
        this.payOrderInfoMapper.insert(payOrderInfo);
        //将订单放入延时队列
        redisComponent.addOrder2DelayQueue(Constants.ORDER_TIMEOUT_MIN + 1, orderId);

        PayInfoDTO payInfoDTO = new PayInfoDTO();
        payInfoDTO.setPayUrl(payUrl);
        payInfoDTO.setOrderId(orderId);
        return payInfoDTO;
    }

    private static String getOrderId() {
        return DateUtil.format(new Date(), DateTimePatternEnum.YYYYMMDDHHMMSS.getPattern()) + StringTools.getRandomString(Constants.LENGTH_14).toUpperCase();
    }

    @PostConstruct
    public void consumeDelayOrder() {
        ExecutorServiceSingletonEnum.INSTANCE.getExecutorService().execute(() -> {
            while (true) {
                try {
                    Set<String> queueOrderList = redisComponent.getTimeOutOrder();
                    if (queueOrderList == null || queueOrderList.isEmpty()) {
                        Thread.sleep(10000);
                        continue;
                    }
                    for (String orderId : queueOrderList) {
                        if (redisComponent.removeTimeOutOrder(orderId) > 0) {
                            PayOrderInfo payOrderInfo = payOrderInfoMapper.selectByOrderId(orderId);
                            if (PayOrderStatusEnum.HAVE_PAY.getStatus().equals(payOrderInfo.getStatus())) {
                                continue;
                            }
                            PayOrderInfo updateInfo = new PayOrderInfo();
                            updateInfo.setStatus(PayOrderStatusEnum.TIME_OUT.getStatus());

                            PayOrderInfoQuery orderInfoQuery = new PayOrderInfoQuery();
                            orderInfoQuery.setStatus(PayOrderStatusEnum.NO_PAY.getStatus());
                            orderInfoQuery.setOrderId(orderId);
                            payOrderInfoMapper.updateByParam(updateInfo, orderInfoQuery);
                        }
                    }
                } catch (Exception e) {
                    log.error("处理超时订单失败", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payNotify(Integer payType, Map<String, Object> params, String body) {
        PayOrderTypeEnum payTypeEnum = PayOrderTypeEnum.getByType(payType);
        PayChannelService payChannelService = (PayChannelService) SpringContext.getBean(payTypeEnum.getBeanName());
        PayOrderNotifyDTO payOrderInfoDto = payChannelService.checkPayNotify(params, body);

        PayOrderInfo payOrderInfo = this.payOrderInfoMapper.selectByOrderId(payOrderInfoDto.getOrderId());
        if (null == payOrderInfo) {
            throw new BusinessException("支付回调通知失败，支付订单" + payOrderInfoDto.getOrderId() + "不存在");
        }
        payOrderSuccess(payOrderInfo, payOrderInfoDto.getChannelOrderId());
    }

    private void payOrderSuccess(PayOrderInfo payOrderInfo, String channelOrderId) {
        if (PayOrderStatusEnum.HAVE_PAY.getStatus().equals(payOrderInfo.getStatus())) {
            return;
        }
        PayOrderInfo updateInfo = new PayOrderInfo();
        updateInfo.setStatus(PayOrderStatusEnum.HAVE_PAY.getStatus());
        updateInfo.setPayTime(new Date());
        updateInfo.setChannelOrderId(channelOrderId);

        PayOrderInfoQuery payOrderInfoQuery = new PayOrderInfoQuery();
        payOrderInfoQuery.setOrderId(payOrderInfo.getOrderId());
        payOrderInfoQuery.setStatus(PayOrderStatusEnum.NO_PAY.getStatus());
        Integer updateCount = payOrderInfoMapper.updateByParam(updateInfo, payOrderInfoQuery);
        if (updateCount == 0) {
            throw new BusinessException("更新支付订单状态失败");
        }
        //增加用户积分
        userIntegralRecordService.changeUserIntegral(UserIntegralRecordTypeEnum.RECHARGE, payOrderInfo.getOrderId(), payOrderInfo.getUserId(),
                payOrderInfo.getIntegral(), payOrderInfo.getAmount());

        //将已支付的订单放入缓存
        redisComponent.cacheHavePayOrder(payOrderInfo.getOrderId());
    }

    @Override
    public Integer checkPay(String userId, String orderId) {
        String havePayOrderId = redisComponent.getHavePayOrder(orderId);
        if (StringTools.isEmpty(havePayOrderId)) {
            return null;
        }
        UserInfo userInfo = this.userInfoService.getUserInfoByUserId(userId);
        return userInfo.getIntegral();
    }

    @Override
    public Integer havePay(String userId, String orderId) {
        PayOrderInfo payOrderInfo = getPayOrderInfoByOrderId(orderId);
        if (!PayOrderStatusEnum.HAVE_PAY.getStatus().equals(payOrderInfo.getStatus())) {
            return null;
        }
        UserInfo userInfo = this.userInfoService.getUserInfoByUserId(userId);
        return userInfo.getIntegral();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buyByPayCode(String productId, String payCode, String userId) {
        PayCodeInfo payCodeInfo = payCodeInfoMapper.selectByPayCode(payCode);
        if (null == payCodeInfo) {
            throw new BusinessException("支付码不正确或者已过期或者已使用");
        }
        //已使用
        if (PayCodeStatusEnum.USED.getStatus().equals(payCodeInfo.getStatus())
                || System.currentTimeMillis() - payCodeInfo.getCreateTime().getTime() > 1000 * 60 * 30) {
            throw new BusinessException("支付码不正确或者已过期或者已使用");
        }

        ProductInfo productInfo = this.productInfoMapper.selectByProductId(productId);
        if (productInfo == null || !ProductOnSaleTypeEnum.ON_SALE.getType().equals(productInfo.getOnsaleType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (productInfo.getPrice().compareTo(payCodeInfo.getAmount()) != 0) {
            throw new BusinessException("支付码金额与商品金额不匹配");
        }

        Date curDate = new Date();
        String orderId = getOrderId();
        PayOrderInfo payOrderInfo = new PayOrderInfo();
        payOrderInfo.setOrderId(orderId);
        payOrderInfo.setCreateTime(curDate);
        payOrderInfo.setPayTime(curDate);
        payOrderInfo.setIntegral(productInfo.getIntegral());
        payOrderInfo.setUserId(userId);
        payOrderInfo.setAmount(productInfo.getPrice());
        payOrderInfo.setProductId(productInfo.getProductId());
        payOrderInfo.setProductName(productInfo.getProductName());
        payOrderInfo.setStatus(PayOrderStatusEnum.HAVE_PAY.getStatus());
        payOrderInfo.setPayType(PayOrderTypeEnum.PAY_CODE.getType());
        this.payOrderInfoMapper.insert(payOrderInfo);

        //更新支付码
        PayCodeInfo updateInfo = new PayCodeInfo();
        updateInfo.setStatus(PayCodeStatusEnum.USED.getStatus());
        updateInfo.setUseUserId(userId);
        updateInfo.setUseTime(curDate);
        PayCodeInfoQuery payCodeInfoQuery = new PayCodeInfoQuery();
        payCodeInfoQuery.setPayCode(payCode);
        payCodeInfoQuery.setStatus(PayCodeStatusEnum.NO_USE.getStatus());
        Integer updateCount = payCodeInfoMapper.updateByParam(updateInfo, payCodeInfoQuery);
        if (updateCount == 0) {
            throw new BusinessException("支付码支付失败，请联系老罗");
        }

        userIntegralRecordService.changeUserIntegral(UserIntegralRecordTypeEnum.RECHARGE, payOrderInfo.getOrderId(), payOrderInfo.getUserId(),
                payOrderInfo.getIntegral(), payCodeInfo.getAmount());
    }

    /**
     * 轮训查询订单，针对无法使用回调的情况  实际开发中，会使用支付回调来处理订单支付逻辑，这里是解决没法使用回调的情况
     * 比如本地开发，或者没有线上服务器的情况 微信的服务器没法回调到你本地的服务器上，采用轮询查询处理。
     */
    @PostConstruct
    public void checkPayOrder() {
        if (!appConfig.getAutoCheckPay()) {
            return;
        }
        ExecutorServiceSingletonEnum.INSTANCE.getExecutorService().execute(() -> {
            while (true) {
                try {
                    PayOrderInfoQuery payOrderInfoQuery = new PayOrderInfoQuery();
                    payOrderInfoQuery.setStatus(PayOrderStatusEnum.NO_PAY.getStatus());
                    List<PayOrderInfo> payOrderInfoList = payOrderInfoMapper.selectList(payOrderInfoQuery);
                    for (PayOrderInfo payOrderInfo : payOrderInfoList) {
                        PayOrderTypeEnum payTypeEnum = PayOrderTypeEnum.getByType(payOrderInfo.getPayType());
                        PayChannelService payChannelService = (PayChannelService) SpringContext.getBean(payTypeEnum.getBeanName());
                        PayOrderNotifyDTO payOrderNotifyDTO = payChannelService.queryOrder(payOrderInfo.getOrderId());
                        if (payOrderNotifyDTO == null) {
                            continue;
                        }
                        payOrderSuccess(payOrderInfo, payOrderNotifyDTO.getChannelOrderId());
                    }
                    Thread.sleep(10000);
                } catch (Exception e) {
                    log.error("查询支付订单失败", e);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }
}