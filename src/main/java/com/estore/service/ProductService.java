package com.estore.service;

import com.estore.object.Product;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public interface ProductService {
	List<Product> listAllProducts() throws Exception;

	List<Product> listAllOnsaleProducts() throws Exception;

	List<Product> listAllOnsaleProducts(String keyword) throws Exception;

	Product getProductById(int id);

	Product updateProduct(Product product) throws Exception;

	boolean deleteProductById(int id);

	Product addProduct(Product p) throws Exception;

	String validateProduct(Product m_product, String[] properties);

	public List<Product> searchOnSaleProducts(String keyword) throws Exception;
}
