package com.estore.object;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;

@Component
public class Order {
	private int orderId;
	private int userId;
	private Float totalPrice;
	private String receiver;
	private String address;
	private String telephone;
	private String status;
	private Timestamp time;

	public Order(){}

	public Order(
			//auto gen:int orderId,
			int userId,
			Float totalPrice,
			String receiver,
			String address,
			String telephone,
			String status
	){
		this.userId=userId;
		this.totalPrice=totalPrice;
		this.receiver=receiver;
		this.address=address;
		this.telephone=telephone;
		this.status=status;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(orderId)
				.append(userId)
				.append(totalPrice)
				.append(address)
				.append(telephone)
				.append(receiver)
				.append(status)
				.append(time)
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
		Order other = (Order) obj;
		return this.hashCode()==other.hashCode();
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public Float getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(Float totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}
}
