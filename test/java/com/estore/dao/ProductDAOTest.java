package com.estore.dao;

import com.estore.object.Product;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-context.xml")
@SpringBootTest
@Transactional
public class ProductDAOTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private ProductDAO productDAO;
	@Autowired
	private SqlSessionFactoryBean sqlSessionFactory;

	@Test
	public void testUserDAOExist() throws Exception {
		assertNotNull(sqlSessionFactory);
		assertNotNull(productDAO);
	}

	@Test
	public void testQueryAllProducts() {
		List<Product> products = productDAO.queryAllProducts();
		assertNotNull(products);
	}

	@Test
	public void testQueryProduct1() {
		Product p = new Product();
		p.setTotal(3);
		p.setPrice(100f);
		productDAO.insertProduct(p);
		Product product = productDAO.queryProductById(p.getProductId());
		assertNotNull(product);
		assertEquals(p.getTotal(), product.getTotal());
		assertEquals(p.getPrice(), product.getPrice());
	}

	@Test
	public void testDeleteProductById() {
		Product p = new Product();
		p.setTotal(3);
		p.setPrice(100f);
		productDAO.insertProduct(p);

		productDAO.deleteProductById(p.getProductId());
		p = productDAO.queryProductById(1);
		assertNull(p);
	}

	@Test
	public void testInsertProduct() {
		Product p = new Product();
		p.setTotal(3);
		int id = productDAO.insertProduct(p);
		assertEquals(id, 1);
	}

	@Test
	public void testUpdateProduct1() {
		Product p = new Product();
		p.setTotal(3);
		p.setPrice(100f);
		productDAO.insertProduct(p);


		Product product = productDAO.queryProductById(p.getProductId());
		assertNotNull(product);
		product.setName("drink");
		product.setDescription("I Love It");
		product.setPrice(5f);
		product.setTotal(100);
		product.setOnSale(false);
		product.setPicPath("39daffc2ef8ce6fb7a795e13f8bdf1c0");
		productDAO.updateProduct(product.getProductId(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getTotal(),
				product.isOnSale(),
				product.getPicPath()
		);
		Product product1 = productDAO.queryProductById(p.getProductId());
		assertNotNull(product1);
		assertEquals(product.getName(), product1.getName());
		assertEquals(product.getDescription(), product1.getDescription());
		assertEquals(product.getPrice(), product1.getPrice());
		assertEquals(product.getTotal(), product1.getTotal());
		assertEquals(product.isOnSale(), product1.isOnSale());
		assertEquals(product.getPicPath(), product1.getPicPath());
	}

}