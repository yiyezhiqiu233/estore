package com.estore.dao;


import com.estore.object.Item;
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
public class ItemDAOTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private SqlSessionFactoryBean sqlSessionFactory;
	@Autowired
	ItemDAO itemDAO;

	Item i0=new Item();

	void init(){
		i0.setAmount(5);
		i0.setOrderId(87139872);
		i0.setPrice(new Float(100));
		i0.setProductId(33);
		i0.setProductName("HIHI123");
	}

	void cmpItem(Item a,Item b){
		if(null==a){
			assertNull(b);
		}
		assertEquals(a.getAmount(),b.getAmount());
		assertEquals(a.getItemId(),b.getItemId());
		assertEquals(a.getOrderId(),b.getOrderId());
		assertEquals(a.getPrice(),b.getPrice());
		assertEquals(a.getProductId(),b.getProductId());
	}

	@Test
	public void testDAOExist() throws Exception {
		assertNotNull(sqlSessionFactory);
		assertNotNull(itemDAO);
	}

	@Test
	public void testInsertAmdQueryItem() throws Exception{
		init();
		int res=itemDAO.insertItem(i0);
		assertEquals(res,1);

		List<Item> _i0=itemDAO.queryItemByOrderId(i0.getOrderId());
		cmpItem(i0,_i0.get(0));
	}
}
