package com.estore.object.enums;

public enum UserType {
	//admin
	ADMIN(0),
	//supplier
	SUPPLIER(1),
	//normal user
	NORMAL(2);

	private int type;

	UserType(int type) {
		this.type = type;
	}

	public static UserType valueOf(int type) {
		switch (type) {
			case 0:
				return ADMIN;
			case 1:
				return SUPPLIER;
			case 2:
				return NORMAL;
			default:
				return null;
		}
	}

	public int toInt() {
		return type;
	}
}