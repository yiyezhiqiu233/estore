package com.estore.dao;

import com.estore.object.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UserDAO {
	User queryUserByUsernameAndPassword(String username, String password);

	List<User> queryAllUsers();

	User queryUserById(int id);

	User queryUserByUsername(String username);

	int updateUsernameById(int id, String username);

	int updatePasswordById(int id, String salted_password, String salt);

	int deleteUserById(int id);

	int insertUser(String username, String password, String salt, int userType);

	int updateUserInfo(User user);
}