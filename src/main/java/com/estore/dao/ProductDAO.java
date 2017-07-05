package com.estore.dao;

import com.estore.object.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ProductDAO {
	List<Product> queryAllProducts();

	Product queryProductById(int id);

	void updateProduct(int id, String name,
					   String description, Float price,
					   Integer total, Boolean onSale,
					   String picPath);

	void deleteProductById(int id);

	int insertProduct(Product p);

	List<Product> queryLikeOnSaleProducts(String keyword);
}