package com.estore.dao;

import com.estore.object.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface OrderDAO {
	int insertOrder(Order order);
	List<Order> queryOrderByUserId(int userId);
	Order queryOrderByOrderId(int orderId);
	List<Order> queryAllOrders();
	int updateOrderStatusByOrderId(int orderId,String status);
}
