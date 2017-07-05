package com.estore.service;

import com.estore.object.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring-context.xml")
@SpringBootTest
@Transactional
public class UserServiceTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private UserService userService;

	@Test
	public void testUserService() {
		assertNotNull(userService);
	}

	@Test
	public void testUserLogin() {
		try {
			User user = userService.userLogin("admin", "admin");
			assertNotNull(user);
			assertEquals(user.getUsername(),"admin");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
