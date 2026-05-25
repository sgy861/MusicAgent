package com.easymusic.entity.po;

import com.easymusic.entity.enums.DateTimePatternEnum;
import com.easymusic.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 支付码
 */
public class PayCodeInfo implements Serializable {


    /**
     * 支付码
     */
    private String payCode;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 使用用户ID
     */
    private String useUserId;

    /**
     * 使用时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date useTime;

    /**
     * 状态 0:待使用 1:已使用
     */
    private Integer status;

    private String nickName;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setPayCode(String payCode) {
        this.payCode = payCode;
    }

    public String getPayCode() {
        return this.payCode;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setUseUserId(String useUserId) {
        this.useUserId = useUserId;
    }

    public String getUseUserId() {
        return this.useUserId;
    }

    public void setUseTime(Date useTime) {
        this.useTime = useTime;
    }

    public Date getUseTime() {
        return this.useTime;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

    @Override
    public String toString() {
        return "支付码:" + (payCode == null ? "空" : payCode) + "，金额:" + (amount == null ? "空" : amount) + "，创建时间:" + (createTime == null ? "空" :
                DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，使用用户ID:" + (useUserId == null ? "空" : useUserId) + "，使用时间:" + (useTime == null ? "空" : DateUtil.format(useTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + "，状态 0:待使用 1:已使用:" + (status == null ? "空" : status);
    }
}
