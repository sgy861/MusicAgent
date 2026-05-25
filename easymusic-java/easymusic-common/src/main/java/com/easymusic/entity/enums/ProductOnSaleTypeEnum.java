package com.easymusic.entity.enums;


public enum ProductOnSaleTypeEnum {
    OFF_SALE(0, "下架"), ON_SALE(1, "上架");

    private Integer type;

    private String desc;

    ProductOnSaleTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
