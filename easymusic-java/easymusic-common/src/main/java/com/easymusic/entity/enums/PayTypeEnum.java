package com.easymusic.entity.enums;


public enum PayTypeEnum {
    LAOLUO("laoluo", "使用老罗封装的接口"),
    WECHAT("wechat", "使用微信官方");

    private String payType;
    private String desc;

    PayTypeEnum(String payType, String desc) {
        this.payType = payType;
        this.desc = desc;
    }

    public String getPayType() {
        return payType;
    }

    public String getDesc() {
        return desc;
    }
}
