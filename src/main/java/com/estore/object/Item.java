package com.estore.object;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.stereotype.Component;

@Component
public class Item {
	private int itemId;
	private int orderId;
	private int productId;
	private Float price;
	private int amount;
	private String productName;

	public Item(){}
	public Item(
			int orderId,
			int productId,
			Float price,
			int amount,
			String productName
	){
		this.orderId=orderId;
		this.productId=productId;
		this.price=price;
		this.amount=amount;
		this.productName=productName;
	}


	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(itemId)
				.append(orderId)
				.append(price)
				.append(amount)
				.append(productId)
				.append(productName)
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
		Item other = (Item) obj;
		return this.hashCode()==other.hashCode();
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getProductId() {
		return productId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
}
