package com.easymusic.entity.enums;


import java.util.Arrays;
import java.util.Optional;

public enum PayOrderTypeEnum {

    PAY_CODE(0, "", "付款码支付"),
    PAY_WECHAT(1, "payChannel4Wechat", "微信支付");


    private Integer type;
    private String beanName;
    private String desc;

    PayOrderTypeEnum(Integer type, String beanName, String desc) {
        this.type = type;
        this.beanName = beanName;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public String getBeanName() {
        return beanName;
    }

    public static PayOrderTypeEnum getByType(Integer type) {
        Optional<PayOrderTypeEnum> typeEnum = Arrays.stream(PayOrderTypeEnum.values()).filter(value -> value.getType().equals(type)).findFirst();
        return typeEnum == null ? null : typeEnum.get();
    }
}
