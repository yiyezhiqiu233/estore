package com.estore.service;

import com.estore.object.Address;
import com.estore.object.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public interface UserService {
	User userLogin(String username, String password) throws Exception;

	List<User> listAllUsers() throws Exception;

	User getUserById(int id) throws Exception;

	boolean bUsernameExists(String username);

	User updateUsernameById(int id, String username) throws Exception;

	User updatePasswordById(int id, String password) throws Exception;

	boolean deleteUserById(int id);

	User insertUser(String username, String password) throws Exception;

	String validateUsername(User m_u);

	String validatePassword(User m_u);

	String validateUser(User m_u);

	List<Address> getUserAddresses(int userId);

	User updateUserInfo(User user, String address, String recevier, String telephone) throws Exception;
}
