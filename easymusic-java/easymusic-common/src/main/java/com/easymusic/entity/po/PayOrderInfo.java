package com.easymusic.entity.po;

import com.easymusic.entity.enums.DateTimePatternEnum;
import com.easymusic.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 支付订单信息
 */
public class PayOrderInfo implements Serializable {


    /**
     * 支付了行
     */
    private String orderId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 支付通道订单ID
     */
    private String channelOrderId;

    /**
     * 订单创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /**
     * 0:待支付 1:支付完成
     */
    private Integer status;

    /**
     * 购买积分
     */
    private Integer integral;

    /**
     * 支付信息
     */
    private String payInfo;

    /**
     * 支付类型
     */
    private Integer payType;

    private String nickName;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return this.productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setChannelOrderId(String channelOrderId) {
        this.channelOrderId = channelOrderId;
    }

    public String getChannelOrderId() {
        return this.channelOrderId;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public Date getPayTime() {
        return this.payTime;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setIntegral(Integer integral) {
        this.integral = integral;
    }

    public Integer getIntegral() {
        return this.integral;
    }

    public void setPayInfo(String payInfo) {
        this.payInfo = payInfo;
    }

    public String getPayInfo() {
        return this.payInfo;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Integer getPayType() {
        return this.payType;
    }

    @Override
    public String toString() {
        return "支付了行:" + (orderId == null ? "空" : orderId) + "，用户ID:" + (userId == null ? "空" : userId) + "，商品ID:" + (productId == null ? "空" : productId) + "，商品名称:" + (productName == null ? "空" : productName) + "，金额:" + (amount == null ? "空" : amount) + "，支付通道订单ID:" + (channelOrderId == null ? "空" : channelOrderId) + "，订单创建时间:" + (createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，支付时间:" + (payTime == null ? "空" : DateUtil.format(payTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，0:待支付 1:支付完成:" + (status == null ? "空" : status) + "，购买积分:" + (integral == null ? "空" : integral) + "，支付信息:" + (payInfo == null ? "空" : payInfo) + "，支付类型:" + (payType == null ? "空" : payType);
    }
}
