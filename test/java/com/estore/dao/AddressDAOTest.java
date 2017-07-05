package com.estore.dao;

import com.estore.object.Address;
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-context.xml")
@SpringBootTest
@Transactional
public class AddressDAOTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private SqlSessionFactoryBean sqlSessionFactory;
	@Autowired
	AddressDAO addressDAO;

	String a1="\"中山市xx路线\"";
	String a2="hihi";

	@Test
	public void testDAOExist() throws Exception {
		assertNotNull(sqlSessionFactory);
		assertNotNull(addressDAO);
	}

	@Test
	public void testInsertAndQueryUserAddress(){
		Address A1=new Address();
		A1.setUserId(0);
		A1.setAddress(a1);
		int res=addressDAO.insertUserAddress(A1);
		assertEquals(res,1);
		List<Address> addresses=addressDAO.queryAllAddressesOfUser(0);
		assertNotNull(addresses);
		assertNotEquals(addresses.size(),0);
		boolean found=false;
		for(Address a:addresses){
			if(a.getAddress().equals(a1)&&a.getUserId()==0&&a.getAddressId()==A1.getAddressId())
				found=true;
		}
		assertEquals(found,true);
	}

	@Test
	public void testDeleteAddress(){
		Address A1=new Address();
		A1.setUserId(0);
		A1.setAddress(a1);
		int res=addressDAO.insertUserAddress(A1);
		assertEquals(res,1);

		addressDAO.deleteAddress(A1.getAddressId());


		List<Address> addresses=addressDAO.queryAllAddressesOfUser(0);
		assertNotNull(addresses);
		boolean found=false;
		for(Address a:addresses){
			if(a.getAddress().equals(a1)&&a.getUserId()==0&&a.getAddressId()==A1.getAddressId())
				found=true;
		}
		assertEquals(found,false);
	}

	@Test
	public void testUpdateUserAddress(){
		Address A1=new Address();
		A1.setUserId(0);
		A1.setAddress(a1);
		int res=addressDAO.insertUserAddress(A1);
		assertEquals(res,1);

		addressDAO.updateUserAddress(A1.getAddressId(),a2);

		List<Address> addresses=addressDAO.queryAllAddressesOfUser(0);
		assertNotNull(addresses);
		boolean found1=false;
		boolean found2=false;
		for(Address a:addresses){
			if(a.getAddress().equals(a1)&&a.getUserId()==0&&a.getAddressId()==A1.getAddressId())
				found1=true;
			if(a.getAddress().equals(a2)&&a.getUserId()==0&&a.getAddressId()==A1.getAddressId())
				found2=true;
		}
		assertEquals(found1,false);
		assertEquals(found2,true);
	}
}