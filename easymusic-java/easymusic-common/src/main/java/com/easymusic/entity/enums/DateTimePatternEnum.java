package com.easymusic.entity.enums;


public enum DateTimePatternEnum {
    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"), YYYY_MM_DDTHH_MM_SS("yyyy-MM-dd'T'HH:mm:ssXXX"), YYYY_MM_DD("yyyy-MM-dd"), YYYYMM("yyyyMM"), YYYYMMDDHHMMSS(
            "yyyyMMddHHmmss");

    private String pattern;

    DateTimePatternEnum(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
