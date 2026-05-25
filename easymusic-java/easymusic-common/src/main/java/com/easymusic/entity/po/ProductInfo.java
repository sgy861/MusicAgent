package com.easymusic.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import com.easymusic.entity.enums.DateTimePatternEnum;
import com.easymusic.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 商品信息
 */
public class ProductInfo implements Serializable {


	/**
	 * 商品ID
	 */
	private String productId;

	/**
	 * 商品名称
	 */
	private String productName;

	/**
	 * 封面
	 */
	private String cover;

	/**
	 * 价格
	 */
	private BigDecimal price;

	/**
	 * 商品描述
	 */
	private String productDescription;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

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

	public void setProductName(String productName){
		this.productName = productName;
	}

	public String getProductName(){
		return this.productName;
	}

	public void setCover(String cover){
		this.cover = cover;
	}

	public String getCover(){
		return this.cover;
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

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
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

	@Override
	public String toString (){
		return "商品ID:"+(productId == null ? "空" : productId)+"，商品名称:"+(productName == null ? "空" : productName)+"，封面:"+(cover == null ? "空" : cover)+"，价格:"+(price == null ? "空" : price)+"，商品描述:"+(productDescription == null ? "空" : productDescription)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，上架类型:"+(onsaleType == null ? "空" : onsaleType)+"，购买积分:"+(integral == null ? "空" : integral)+"，排序号:"+(sort == null ? "空" : sort);
	}
}
