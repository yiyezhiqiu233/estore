package com.estore.service;

import com.estore.object.Item;
import com.estore.object.Order;
import com.estore.object.Product;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public interface OrderService {
	Order generateNewOrder(
			int userId,
			HashMap<Product, Integer> products,
			String receiver,
			String address,
			String telephone)
			throws Exception;

	List<Order> getAllOrdersOfUser(int userId) throws Exception;

	List<Order> getAllOrders() throws Exception;

	Order getOrderByOrderId(int orderId) throws Exception;

	List<Item> getAllItemsOfOrder(int orderId) throws Exception;

	void confirmOrder(Order order) throws Exception;

	void rejectOrder(Order order) throws Exception;
}
