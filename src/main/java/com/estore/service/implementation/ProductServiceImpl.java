package com.estore.service.implementation;

import com.estore.dao.ProductDAO;
import com.estore.object.Product;
import com.estore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service("productService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class ProductServiceImpl implements ProductService {
	private final ProductDAO productDAO;
	private final LocalValidatorFactoryBean validator;

	private static String[] defaultValidationProperties = {"name", "price", "total", "onSale"};

	@Autowired
	public ProductServiceImpl(ProductDAO productDAO, LocalValidatorFactoryBean validator) {
		this.productDAO = productDAO;
		this.validator = validator;
	}

	public List<Product> listAllProducts() throws Exception {
		List<Product> productList = productDAO.queryAllProducts();
		if (null == productList) throw new Exception("ProductService故障:无法获取商品列表.");
		return productList;
	}

	public List<Product> listAllOnsaleProducts() throws Exception {
		List<Product> products = new ArrayList<Product>();
		try {
			for (Product p : listAllProducts()) {
				if (p.isOnSale()) {
					products.add(p);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return products;
	}

	@Override
	public List<Product> listAllOnsaleProducts(String keyword) throws Exception {
		if (null == keyword || keyword.equals("")) {
			return listAllOnsaleProducts();
		}
		List<Product> productList = searchOnSaleProducts(keyword);
		if (null == productList) {
			throw new Exception("搜索失败.");
		}
		return productList;
	}

	public Product getProductById(int id) {
		return productDAO.queryProductById(id);
	}

	public Product updateProduct(Product product) throws Exception {
		Product old = productDAO.queryProductById(product.getProductId());
		if (null == old) throw new Exception("商品不存在.");

		String errMsg = validateProduct(product, defaultValidationProperties);
		if (null != errMsg && !errMsg.equals("")) throw new Exception(errMsg);

		productDAO.updateProduct(
				product.getProductId(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getTotal(),
				product.isOnSale(),
				product.getPicPath()
		);
		product = productDAO.queryProductById(product.getProductId());
		if (null == product) throw new Exception("商品不存在.");
		return product;
	}

	public boolean deleteProductById(int id) {
		productDAO.deleteProductById(id);
		return (null == productDAO.queryProductById(id));
	}

	public Product addProduct(Product p) throws Exception {
		if (null == p) throw new Exception("商品不存在.");
		if (null == p.getPicPath())
			p.setPicPath("");

		String errMsg = validateProduct(p, defaultValidationProperties);
		if (null != errMsg && !errMsg.equals("")) throw new Exception(errMsg);

		int result = productDAO.insertProduct(p);
		if (result != 1) throw new Exception("商品不存在.");
		p = productDAO.queryProductById(p.getProductId());
		if (null == p) throw new Exception("商品不存在.");
		return p;
	}

	public String validateProduct(Product m_product, String[] properties) {
		if (null == m_product || null == properties || properties.length < 1)
			return null;

		Set<ConstraintViolation<Product>> errors = null;
		for (String property : properties) {
			if (null == errors)
				errors = validator.getValidator().validateProperty(m_product, property);
			else
				errors.addAll(validator.getValidator().validateProperty(m_product, property));
		}

		StringBuilder errMsg = new StringBuilder();
		if (errors != null) {
			for (ConstraintViolation<Product> err : errors) {
				errMsg.append(err.getMessage());
			}
		}
		return errMsg.toString();
	}

	public List<Product> searchOnSaleProducts(String keyword) throws Exception {
		if (null == keyword || keyword.equals("")) {
			return listAllOnsaleProducts();
		}
		List<Product> productList = productDAO.queryLikeOnSaleProducts(keyword);
		if (null == productList) throw new Exception("ProductService故障:无法搜索 " + keyword + ".");
		return productList;
	}
}
