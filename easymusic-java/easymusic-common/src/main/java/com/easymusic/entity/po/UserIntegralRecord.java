package com.easymusic.entity.po;

import com.easymusic.entity.enums.DateTimePatternEnum;
import com.easymusic.entity.enums.UserIntegralRecordTypeEnum;
import com.easymusic.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 用户积分记录信息
 */
public class UserIntegralRecord implements Serializable {


    /**
     * 自增ID
     */
    private Integer recordId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 积分
     */
    private Integer changeIntegral;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 记录类型 0:创作失败退回 1:创作消耗 2:充值 3:系统赠送
     */
    private Integer recordType;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 充值金额
     */
    private BigDecimal amount;

    private String recordTypeName;

    public String getRecordTypeName() {
        UserIntegralRecordTypeEnum recordTypeEnum = UserIntegralRecordTypeEnum.getByType(recordType);
        return recordTypeEnum == null ? "" : recordTypeEnum.getDesc();
    }

    public void setRecordTypeName(String recordTypeName) {
        this.recordTypeName = recordTypeName;
    }

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

    public void setChangeIntegral(Integer changeIntegral) {
        this.changeIntegral = changeIntegral;
    }

    public Integer getChangeIntegral() {
        return this.changeIntegral;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return this.createTime;
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

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    @Override
    public String toString() {
        return "自增ID:" + (recordId == null ? "空" : recordId) + "，用户ID:" + (userId == null ? "空" : userId) + "，积分:" + (changeIntegral == null ? "空" : changeIntegral) +
                "，创建时间:" + (createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，记录类型 0:创作失败退回 1:创作消耗 2:充值" +
                " 3:系统赠送:" + (recordType == null ? "空" : recordType) + "，业务ID:" + (businessId == null ? "空" : businessId) + "，充值金额:" + (amount == null ? "空" : amount);
    }
}
