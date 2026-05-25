package com.easymusic.entity.query;



/**
 * 系统字典参数
 */
public class SysDictQuery extends BaseParam {


	/**
	 * 字典ID
	 */
	private Integer dictId;

	/**
	 * 字典编号
	 */
	private String dictCode;

	private String dictCodeFuzzy;

	/**
	 * 父级字典ID
	 */
	private String dictPcode;

	private String dictPcodeFuzzy;

	/**
	 * 字典值
	 */
	private String dictValue;

	private String dictValueFuzzy;

	/**
	 * 字典描述
	 */
	private String dictDesc;

	private String dictDescFuzzy;

	/**
	 * 排序号
	 */
	private Integer sort;


	public void setDictId(Integer dictId){
		this.dictId = dictId;
	}

	public Integer getDictId(){
		return this.dictId;
	}

	public void setDictCode(String dictCode){
		this.dictCode = dictCode;
	}

	public String getDictCode(){
		return this.dictCode;
	}

	public void setDictCodeFuzzy(String dictCodeFuzzy){
		this.dictCodeFuzzy = dictCodeFuzzy;
	}

	public String getDictCodeFuzzy(){
		return this.dictCodeFuzzy;
	}

	public void setDictPcode(String dictPcode){
		this.dictPcode = dictPcode;
	}

	public String getDictPcode(){
		return this.dictPcode;
	}

	public void setDictPcodeFuzzy(String dictPcodeFuzzy){
		this.dictPcodeFuzzy = dictPcodeFuzzy;
	}

	public String getDictPcodeFuzzy(){
		return this.dictPcodeFuzzy;
	}

	public void setDictValue(String dictValue){
		this.dictValue = dictValue;
	}

	public String getDictValue(){
		return this.dictValue;
	}

	public void setDictValueFuzzy(String dictValueFuzzy){
		this.dictValueFuzzy = dictValueFuzzy;
	}

	public String getDictValueFuzzy(){
		return this.dictValueFuzzy;
	}

	public void setDictDesc(String dictDesc){
		this.dictDesc = dictDesc;
	}

	public String getDictDesc(){
		return this.dictDesc;
	}

	public void setDictDescFuzzy(String dictDescFuzzy){
		this.dictDescFuzzy = dictDescFuzzy;
	}

	public String getDictDescFuzzy(){
		return this.dictDescFuzzy;
	}

	public void setSort(Integer sort){
		this.sort = sort;
	}

	public Integer getSort(){
		return this.sort;
	}

}
