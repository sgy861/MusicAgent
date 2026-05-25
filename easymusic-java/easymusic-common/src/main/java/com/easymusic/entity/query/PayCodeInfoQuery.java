package com.easymusic.entity.query;

import java.math.BigDecimal;


/**
 * 支付码参数
 */
public class PayCodeInfoQuery extends BaseParam {


    /**
     * 支付码
     */
    private String payCode;

    private String payCodeFuzzy;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 创建时间
     */
    private String createTime;

    private String createTimeStart;

    private String createTimeEnd;

    /**
     * 使用用户ID
     */
    private String useUserId;

    private String useUserIdFuzzy;

    /**
     * 使用时间
     */
    private String useTime;

    private String useTimeStart;

    private String useTimeEnd;

    /**
     * 状态 0:待使用 1:已使用
     */
    private Integer status;

    private Boolean queryUser;

    public Boolean getQueryUser() {
        return queryUser;
    }

    public void setQueryUser(Boolean queryUser) {
        this.queryUser = queryUser;
    }

    public void setPayCode(String payCode) {
        this.payCode = payCode;
    }

    public String getPayCode() {
        return this.payCode;
    }

    public void setPayCodeFuzzy(String payCodeFuzzy) {
        this.payCodeFuzzy = payCodeFuzzy;
    }

    public String getPayCodeFuzzy() {
        return this.payCodeFuzzy;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return this.amount;
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

    public void setUseUserId(String useUserId) {
        this.useUserId = useUserId;
    }

    public String getUseUserId() {
        return this.useUserId;
    }

    public void setUseUserIdFuzzy(String useUserIdFuzzy) {
        this.useUserIdFuzzy = useUserIdFuzzy;
    }

    public String getUseUserIdFuzzy() {
        return this.useUserIdFuzzy;
    }

    public void setUseTime(String useTime) {
        this.useTime = useTime;
    }

    public String getUseTime() {
        return this.useTime;
    }

    public void setUseTimeStart(String useTimeStart) {
        this.useTimeStart = useTimeStart;
    }

    public String getUseTimeStart() {
        return this.useTimeStart;
    }

    public void setUseTimeEnd(String useTimeEnd) {
        this.useTimeEnd = useTimeEnd;
    }

    public String getUseTimeEnd() {
        return this.useTimeEnd;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

}
