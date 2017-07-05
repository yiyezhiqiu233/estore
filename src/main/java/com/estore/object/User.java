package com.estore.object;

import com.estore.object.enums.UserType;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@Component
public class User {
	private int userId;

	//TODO:min max
	@NotNull
	@Size(min = 2, max = 16, message = "{username.size}")
	@Pattern(regexp = "[(a-zA-Z0-9_)]*", message = "{username.contain_quotation_mark}")
	private String username;

	//TODO:min-max
	@NotNull
	@Size(min = 2, max = 32, message = "{password.size}")
	@Pattern(regexp = "[(a-zA-Z0-9_~!@#$%^&*-+=;:,./<>?)]*", message = "{password.contain_quotation_mark}")
	private String password;

	private UserType userType;

	private String salt;

	private String defaultAddress;
	private String defaultTelephone;
	private String defaultReceiver;

	public User() {
	}

	public User(Integer userId, String username, String password) {
		this.userId = userId;
		this.username = username;
		this.password = password;
	}

	public int getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UserType getUserType() {
		return this.userType;
	}

	public void setUserType(int userType) {
		this.userType = UserType.valueOf(userType);
	}

	public String getDefaultAddress() {
		return defaultAddress;
	}

	public void setDefaultAddress(String defaultAddress) {
		this.defaultAddress = defaultAddress;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(userId)
				.append(username)
				.append(password)
				.append(userType)
				.append(defaultAddress)
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		return this.hashCode() == other.hashCode();
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getDefaultTelephone() {
		return defaultTelephone;
	}

	public void setDefaultTelephone(String defaultTelephone) {
		this.defaultTelephone = defaultTelephone;
	}

	public String getDefaultReceiver() {
		return defaultReceiver;
	}

	public void setDefaultReceiver(String defaultReceiver) {
		this.defaultReceiver = defaultReceiver;
	}
}
