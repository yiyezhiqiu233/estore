package com.estore.dao;


import com.estore.object.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static junit.framework.TestCase.assertNull;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-context.xml")
@SpringBootTest
@Transactional
public class OrderDAOTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private SqlSessionFactoryBean sqlSessionFactory;
	@Autowired
	OrderDAO orderDAO;

	Order o1=new Order();

	private void init(){
		o1.setAddress("来安路686");
		o1.setTotalPrice(Float.valueOf(10));
		o1.setTelephone("11021218989");
		o1.setReceiver("猫");
		o1.setUserId(0);
		o1.setStatus("已提交");

		o1.setOrderId(-1);
	}

	private void cmpOrder(Order a,Order b){
		if(null==a){
			assertNull(b);
		}
		assertEquals(a.getOrderId(),b.getOrderId());
		assertEquals(a.getAddress(),b.getAddress());
		assertEquals(a.getReceiver(),b.getReceiver());
		assertEquals(a.getStatus(),b.getStatus());
		assertEquals(a.getTelephone(),b.getTelephone());
		assertEquals(a.getTotalPrice(),b.getTotalPrice());
		assertEquals(a.getUserId(),b.getUserId());
	}

	@Test
	public void testDAOExist() throws Exception {
		assertNotNull(sqlSessionFactory);
		assertNotNull(orderDAO);
	}

	@Test
	public void testInsertAndQueryOrder() throws Exception{
		init();
		int res=orderDAO.insertOrder(o1);
		assertEquals(res,1);
		assertNotEquals(o1.getOrderId(),-1);

		Order _o1=orderDAO.queryOrderByOrderId(o1.getOrderId());
		assertNotNull(_o1);
		cmpOrder(o1,_o1);
	}
}
