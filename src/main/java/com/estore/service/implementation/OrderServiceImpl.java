package com.estore.service.implementation;

import com.estore.dao.ItemDAO;
import com.estore.dao.OrderDAO;
import com.estore.object.Item;
import com.estore.object.Order;
import com.estore.object.Product;
import com.estore.object.User;
import com.estore.object.enums.UserType;
import com.estore.service.OrderService;
import com.estore.service.ProductService;
import com.estore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service("orderService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class OrderServiceImpl implements OrderService {
	private final OrderDAO orderDAO;
	private final ItemDAO itemDAO;
	private final ProductService productService;
	private final UserService userService;

	@Autowired
	public OrderServiceImpl(OrderDAO orderDAO, ItemDAO itemDAO, ProductService productService, UserService userService) {
		this.orderDAO = orderDAO;
		this.itemDAO = itemDAO;
		this.productService = productService;
		this.userService = userService;
	}

	public void generateNewOrder(
			int userId,
			HashMap<Product, Integer> products,
			String receiver,
			String address,
			String telephone
	) throws Exception {
		//verify user existence
		User user = userService.getUserById(userId);
		if (null == user) {
			throw new Exception("用户不存在.");
		} else if (user.getUserType() != UserType.NORMAL) {
			throw new Exception("用户无权限购买商品.");
		}

		if (receiver.length() > 16) throw new Exception("收货人 名字过长.");
		if (telephone.length() > 16) throw new Exception("联系方式 过长.");
		if (address.length() > 255) throw new Exception("收货地址 名字过长.");

		//verify products and totalPrice
		Float totalPrice = 0f;
		for (Product p : products.keySet()) {
			if (null == p) {
				throw new Exception("商品不存在.");
			}
			Product _p = productService.getProductById(p.getProductId());
			if (null == _p) {
				throw new Exception("商品不存在.");
			} else if (!p.getPrice().equals(_p.getPrice())) {
				throw new Exception("商品价格已改变");
			}
			if (!p.isOnSale()) {
				throw new Exception("商品已下架.");
			}
			Integer amount = products.get(p);
			if (null == amount) {
				throw new Exception("数量信息丢失.");
			}
			if (_p.getTotal() < amount) {
				throw new Exception("商品 " + _p.getName() + " 库存不足.");
			}
			_p.setTotal(_p.getTotal() - amount);
			_p = productService.updateProduct(_p);
			if (null == _p) {
				throw new Exception("ProductService出错,无法购买商品 " + p.getName() + ".");
			}
			totalPrice += amount * p.getPrice();
		}

		//insert order first
		Order order = new Order(
				userId,
				totalPrice,
				receiver,
				address,
				telephone,
				"已提交");
		int result = orderDAO.insertOrder(order);
		if (1 != result) {
			throw new Exception("订单创建失败.");
		}

		int orderId = order.getOrderId();
		//insert items
		for (Product p : products.keySet()) {
			Item item = new Item(orderId, p.getProductId(),
					p.getPrice(), products.get(p), p.getName());

			result = itemDAO.insertItem(item);
			if (1 != result) {
				throw new Exception("订单项创建失败.");
			}
		}
	}

	public List<Order> getAllOrdersOfUser(int userId) throws Exception {
		List<Order> orders = orderDAO.queryOrderByUserId(userId);
		if (null == orders) {
			throw new Exception("无法获取用户订单.");
		}
		return orders;
	}

	@Override
	public List<Order> getAllOrders() throws Exception {
		List<Order> orderList = orderDAO.queryAllOrders();
		if (null == orderList) throw new Exception("OrderService故障:无法获取订单数据.");
		return orderList;
	}

	@Override
	public Order getOrderByOrderId(int orderId) throws Exception {
		Order order = orderDAO.queryOrderByOrderId(orderId);
		if (null == order) {
			throw new Exception("订单不存在.");
		}
		return order;
	}

	@Override
	public List<Item> getAllItemsOfOrder(int orderId) throws Exception {
		List<Item> items = itemDAO.queryItemByOrderId(orderId);
		if (null == items) {
			throw new Exception("未查询到对应订单项.");
		}
		return items;
	}

	@Override
	public void confirmOrder(Order order) throws Exception {
		if (null == order) {
			throw new Exception("订单不存在.");
		}
		//订单是否有变化
		Order order1 = getOrderByOrderId(order.getOrderId());
		if (!order.equals(order1)) {
			throw new Exception("订单信息有变化.请刷新页面");
		}
		//订单是否未处理
		if (!order.getStatus().equals("已提交"))
			throw new Exception("订单已处理.");
		//更新
		try {
			int result = orderDAO.updateOrderStatusByOrderId(order.getOrderId(), "已确认");
			if (result != 1) {
				throw new Exception("订单状态更新失败.");
			}
		} catch (Exception ex) {
			throw new Exception("更新订单失败.");
		}
		//重新获取订单
		order = orderDAO.queryOrderByOrderId(order.getOrderId());
		if (null == order) {
			throw new Exception("订单获取失败.");
		}
	}

	@Override
	public void rejectOrder(Order order) throws Exception {
		if (null == order) {
			throw new Exception("订单不存在.");
		}
		//订单是否有变化
		Order order1 = getOrderByOrderId(order.getOrderId());
		if (!order.equals(order1)) {
			throw new Exception("订单信息有变化.请刷新页面");
		}
		//订单是否未处理
		if (!order.getStatus().equals("已提交"))
			throw new Exception("订单已处理.");
		//更新
		try {
			int result = orderDAO.updateOrderStatusByOrderId(order.getOrderId(), "已拒绝");
			if (result != 1) {
				throw new Exception("订单状态更新失败.");
			}
		} catch (Exception ex) {
			throw new Exception("更新订单失败.");
		}
		//update product amount
		List<Item> items = getAllItemsOfOrder(order.getOrderId());
		for (Item item : items) {
			Product p = productService.getProductById(item.getProductId());
			p.setTotal(p.getTotal() + item.getAmount());
			productService.updateProduct(p);
		}
		//重新获取订单
		order = getOrderByOrderId(order.getOrderId());
		if (null == order) {
			throw new Exception("订单获取失败.");
		}
	}
}
