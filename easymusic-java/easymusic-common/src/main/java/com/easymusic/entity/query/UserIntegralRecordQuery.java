package com.easymusic.entity.query;

import java.math.BigDecimal;


/**
 * 用户积分记录信息参数
 */
public class UserIntegralRecordQuery extends BaseParam {


    /**
     * 自增ID
     */
    private Integer recordId;

    /**
     * 用户ID
     */
    private String userId;

    private String userIdFuzzy;

    /**
     * 积分
     */
    private Integer changeIntegral;

    /**
     * 创建时间
     */
    private String createTime;

    private String createTimeStart;

    private String createTimeEnd;

    /**
     * 记录类型 0:创作失败退回 1:创作消耗 2:充值 3:系统赠送
     */
    private Integer recordType;

    /**
     * 业务ID
     */
    private String businessId;

    private String businessIdFuzzy;

    /**
     * 充值金额
     */
    private BigDecimal amount;


    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public Integer getRecordId() {
        return this.recordId;
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

    public void setChangeIntegral(Integer changeIntegral) {
        this.changeIntegral = changeIntegral;
    }

    public Integer getChangeIntegral() {
        return this.changeIntegral;
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

    public void setRecordType(Integer recordType) {
        this.recordType = recordType;
    }

    public Integer getRecordType() {
        return this.recordType;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessId() {
        return this.businessId;
    }

    public void setBusinessIdFuzzy(String businessIdFuzzy) {
        this.businessIdFuzzy = businessIdFuzzy;
    }

    public String getBusinessIdFuzzy() {
        return this.businessIdFuzzy;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

}
