package com.easymusic.entity.po;

import java.io.Serializable;


/**
 * 系统字典
 */
public class SysDict implements Serializable {


    /**
     * 字典ID
     */
    private Integer dictId;

    /**
     * 字典编号
     */
    private String dictCode;

    /**
     * 父级字典ID
     */
    private String dictPcode;

    /**
     * 字典值
     */
    private String dictValue;

    /**
     * 字典描述
     */
    private String dictDesc;

    /**
     * 排序号
     */
    private Integer sort;


    public void setDictId(Integer dictId) {
        this.dictId = dictId;
    }

    public Integer getDictId() {
        return this.dictId;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getDictCode() {
        return this.dictCode;
    }

    public void setDictPcode(String dictPcode) {
        this.dictPcode = dictPcode;
    }

    public String getDictPcode() {
        return this.dictPcode;
    }

    public void setDictValue(String dictValue) {
        this.dictValue = dictValue;
    }

    public String getDictValue() {
        return this.dictValue;
    }

    public void setDictDesc(String dictDesc) {
        this.dictDesc = dictDesc;
    }

    public String getDictDesc() {
        return this.dictDesc;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getSort() {
        return this.sort;
    }

    @Override
    public String toString() {
        return "字典ID:" + (dictId == null ? "空" : dictId) + "，字典编号:" + (dictCode == null ? "空" : dictCode) + "，父级字典ID:" + (dictPcode == null ? "空" : dictPcode) +
                "，字典值:" + (dictValue == null ? "空" : dictValue) + "，字典描述:" + (dictDesc == null ? "空" : dictDesc) + "，排序号:" + (sort == null ? "空" : sort);
    }
}
