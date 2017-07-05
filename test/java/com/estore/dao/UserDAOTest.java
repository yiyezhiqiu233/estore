package com.estore.dao;

import com.estore.object.User;
import com.estore.object.enums.UserType;
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

import static junit.framework.TestCase.*;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-context.xml")
@SpringBootTest
@Transactional
public class UserDAOTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private SqlSessionFactoryBean sqlSessionFactory;
	@Autowired
	UserDAO userDAO;

	@Test
	public void testDAOExist() throws Exception {
		assertNotNull(sqlSessionFactory);
		assertNotNull(userDAO);
	}

	@Test
	public void testUpdateUsernameById() throws Exception {
		String username="test_123";
		String password="ppp";
		String salt="783abd92";
		int re=userDAO.insertUser(username,password,salt,2);
		assertEquals(re,1);

		User u=userDAO.queryUserByUsername(username);
		assertNotNull(u);

		userDAO.updateUsernameById(u.getUserId(), "TEST_123");
		User u2 = userDAO.queryUserById(u.getUserId());
		assertNotNull(u2);
		assertEquals(u2.getUsername(),"TEST_123");
	}

	@Test
	public void testInsertUser() throws Exception {
		userDAO.insertUser("u1001", "p1001", "", 2);
		User u1001 = userDAO.queryUserByUsername("u1001");
		assertNotNull(u1001);
		assertEquals(u1001.getUsername(), "u1001");
		assertEquals(u1001.getPassword(), "p1001");
		assertEquals(u1001.getUserType(), UserType.NORMAL);
	}

	@Test
	public void testDeleteUser() throws Exception {
		userDAO.insertUser("u1001", "p1001", "", 2);
		User u1001 = userDAO.queryUserByUsername("u1001");
		userDAO.deleteUserById(u1001.getUserId());
		u1001 = userDAO.queryUserByUsername("u1001");
		assertNull(u1001);
	}
}