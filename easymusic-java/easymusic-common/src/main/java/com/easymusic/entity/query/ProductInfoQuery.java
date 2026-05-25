package com.easymusic.entity.query;

import java.math.BigDecimal;
import java.util.Date;


/**
 * 商品信息参数
 */
public class ProductInfoQuery extends BaseParam {


	/**
	 * 商品ID
	 */
	private String productId;

	private String productIdFuzzy;

	/**
	 * 商品名称
	 */
	private String productName;

	private String productNameFuzzy;

	/**
	 * 封面
	 */
	private String cover;

	private String coverFuzzy;

	/**
	 * 价格
	 */
	private BigDecimal price;

	/**
	 * 商品描述
	 */
	private String productDescription;

	private String productDescriptionFuzzy;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 上架类型
	 */
	private Integer onsaleType;

	/**
	 * 购买积分
	 */
	private Integer integral;

	/**
	 * 排序号
	 */
	private Integer sort;


	public void setProductId(String productId){
		this.productId = productId;
	}

	public String getProductId(){
		return this.productId;
	}

	public void setProductIdFuzzy(String productIdFuzzy){
		this.productIdFuzzy = productIdFuzzy;
	}

	public String getProductIdFuzzy(){
		return this.productIdFuzzy;
	}

	public void setProductName(String productName){
		this.productName = productName;
	}

	public String getProductName(){
		return this.productName;
	}

	public void setProductNameFuzzy(String productNameFuzzy){
		this.productNameFuzzy = productNameFuzzy;
	}

	public String getProductNameFuzzy(){
		return this.productNameFuzzy;
	}

	public void setCover(String cover){
		this.cover = cover;
	}

	public String getCover(){
		return this.cover;
	}

	public void setCoverFuzzy(String coverFuzzy){
		this.coverFuzzy = coverFuzzy;
	}

	public String getCoverFuzzy(){
		return this.coverFuzzy;
	}

	public void setPrice(BigDecimal price){
		this.price = price;
	}

	public BigDecimal getPrice(){
		return this.price;
	}

	public void setProductDescription(String productDescription){
		this.productDescription = productDescription;
	}

	public String getProductDescription(){
		return this.productDescription;
	}

	public void setProductDescriptionFuzzy(String productDescriptionFuzzy){
		this.productDescriptionFuzzy = productDescriptionFuzzy;
	}

	public String getProductDescriptionFuzzy(){
		return this.productDescriptionFuzzy;
	}

	public void setCreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getCreateTime(){
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

	public void setOnsaleType(Integer onsaleType){
		this.onsaleType = onsaleType;
	}

	public Integer getOnsaleType(){
		return this.onsaleType;
	}

	public void setIntegral(Integer integral){
		this.integral = integral;
	}

	public Integer getIntegral(){
		return this.integral;
	}

	public void setSort(Integer sort){
		this.sort = sort;
	}

	public Integer getSort(){
		return this.sort;
	}

}
