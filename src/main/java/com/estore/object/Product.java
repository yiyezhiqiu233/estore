package com.estore.object;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.stereotype.Component;

import javax.validation.constraints.*;
import java.sql.Timestamp;
import java.util.Date;

@Component
public class Product {
	private int productId;
	private Timestamp createTime;

	@NotNull(message = "产品名称为空.")
	@Size(min = 1, message = "产品名称为空.")
	private String name;

	@Pattern(regexp = "[^\'\"]*", message = "描述包含引号.")
	private String description;

	@NotNull(message = "价格不能为空.")
	private Float price;

	@NotNull(message = "剩余数量不能为空.")
	@DecimalMax("1000000000")
	@DecimalMin("0")
	private Integer total;

	private boolean onSale;

	private String picPath;

	public Product() {
		productId = -1;
		name = "未命名";
		description = "";
		price = Float.valueOf(0);
		total = 99;
		onSale = true;
		picPath = "";
		createTime = new Timestamp(new Date().getTime());
	}

	public Product(Integer productId,
				   String name,
				   String description,
				   Float price,
				   Integer total,
				   Boolean onSale,
				   Timestamp createTime,
				   String picPath) {
		this.productId = productId;
		this.name = name;
		this.description = description;
		this.price = price;
		this.total = total;
		this.onSale = onSale;
		this.createTime = createTime;
		this.picPath = picPath;
	}


	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(productId)
				.append(name)
				.append(description)
				.append(price)
				.append(total)
				.append(onSale)
				.append(createTime)
				.append(picPath)
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Product other = (Product) obj;
		return this.hashCode()==other.hashCode();
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public boolean isOnSale() {
		return onSale;
	}

	public void setOnSale(boolean onSale) {
		this.onSale = onSale;
	}

	public String getPicPath() {
		return picPath;
	}

	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}
}
