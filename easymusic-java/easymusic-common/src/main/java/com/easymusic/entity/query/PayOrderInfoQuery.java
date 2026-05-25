package com.easymusic.entity.query;

import java.math.BigDecimal;


/**
 * 支付订单信息参数
 */
public class PayOrderInfoQuery extends BaseParam {


    /**
     * 支付了行
     */
    private String orderId;

    private String orderIdFuzzy;

    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 商品ID
     */
    private String productId;

    private String productIdFuzzy;

    /**
     * 商品名称
     */
    private String productName;

    private String productNameFuzzy;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 支付通道订单ID
     */
    private String channelOrderId;

    private String channelOrderIdFuzzy;

    /**
     * 订单创建时间
     */
    private String createTime;

    private String createTimeStart;

    private String createTimeEnd;

    /**
     * 支付时间
     */
    private String payTime;

    private String payTimeStart;

    private String payTimeEnd;

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

    private String payInfoFuzzy;

    /**
     * 支付类型
     */
    private Integer payType;

    private Boolean queryUser;

    private String nickNameFuzzy;

    public Boolean getQueryUser() {
        return queryUser;
    }

    public void setQueryUser(Boolean queryUser) {
        this.queryUser = queryUser;
    }

    public String getNickNameFuzzy() {
        return nickNameFuzzy;
    }

    public void setNickNameFuzzy(String nickNameFuzzy) {
        this.nickNameFuzzy = nickNameFuzzy;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderIdFuzzy(String orderIdFuzzy) {
        this.orderIdFuzzy = orderIdFuzzy;
    }

    public String getOrderIdFuzzy() {
        return this.orderIdFuzzy;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserIdFuzzy(String userIdFuzzy) {
        this.userIdFuzzy = userIdFuzzy;
    }

    public String getUserIdFuzzy() {
        return this.userIdFuzzy;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return this.productId;
    }

    public void setProductIdFuzzy(String productIdFuzzy) {
        this.productIdFuzzy = productIdFuzzy;
    }

    public String getProductIdFuzzy() {
        return this.productIdFuzzy;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductNameFuzzy(String productNameFuzzy) {
        this.productNameFuzzy = productNameFuzzy;
    }

    public String getProductNameFuzzy() {
        return this.productNameFuzzy;
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

    public void setChannelOrderIdFuzzy(String channelOrderIdFuzzy) {
        this.channelOrderIdFuzzy = channelOrderIdFuzzy;
    }

    public String getChannelOrderIdFuzzy() {
        return this.channelOrderIdFuzzy;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public void setCreateTimeStart(String createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public String getCreateTimeStart() {
        return this.createTimeStart;
    }

    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public String getCreateTimeEnd() {
        return this.createTimeEnd;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getPayTime() {
        return this.payTime;
    }

    public void setPayTimeStart(String payTimeStart) {
        this.payTimeStart = payTimeStart;
    }

    public String getPayTimeStart() {
        return this.payTimeStart;
    }

    public void setPayTimeEnd(String payTimeEnd) {
        this.payTimeEnd = payTimeEnd;
    }

    public String getPayTimeEnd() {
        return this.payTimeEnd;
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

    public void setPayInfoFuzzy(String payInfoFuzzy) {
        this.payInfoFuzzy = payInfoFuzzy;
    }

    public String getPayInfoFuzzy() {
        return this.payInfoFuzzy;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public Integer getPayType() {
        return this.payType;
    }

}
