package com.estore.dao;

import com.estore.object.Item;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ItemDAO {
	int insertItem(Item item);
	List<Item> queryItemByOrderId(int orderId);
}
