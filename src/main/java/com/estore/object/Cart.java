package com.estore.object;


import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class Cart {
	//productId->amount
	private HashMap<Product, Integer> productHashMap;

	public Cart() {
		productHashMap = new HashMap<Product, Integer>();
	}

	public HashMap<Product, Integer> getProductHashMap() {
		return productHashMap;
	}

	public void setProductHashMap(HashMap<Product, Integer> productHashMap) {
		this.productHashMap = productHashMap;
	}
}
