package com.estore.service.implementation;

import com.estore.dao.AddressDAO;
import com.estore.dao.UserDAO;
import com.estore.object.Address;
import com.estore.object.User;
import com.estore.object.enums.UserType;
import com.estore.service.UserService;
import com.estore.utils.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;

@Service("userService")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class UserServiceImpl implements UserService {
	private final UserDAO userDAO;
	private final AddressDAO addressDAO;
	private final LocalValidatorFactoryBean validator;

	@Autowired
	public UserServiceImpl(UserDAO userDAO, AddressDAO addressDAO, LocalValidatorFactoryBean validator) {
		this.userDAO = userDAO;
		this.addressDAO = addressDAO;
		this.validator = validator;
	}

	public User userLogin(String username, String password) throws Exception {
		User user = new User();
		user.setUsername(username);
		user.setPassword(password);
		String errMsg = validateUser(user);
		if (errMsg != null && !errMsg.equals("")) throw new Exception(errMsg);
		user = userDAO.queryUserByUsername(username);
		if (null == user) throw new Exception("用户名或密码错误.");
		password = PasswordEncoder.encodePassword(password, user.getSalt());
		user = userDAO.queryUserByUsernameAndPassword(username, password);
		if (null == user) throw new Exception("用户名或密码错误.");
		return user;
	}

	public List<User> listAllUsers() throws Exception {
		List<User> userList = userDAO.queryAllUsers();
		if (null == userList) throw new Exception("UserService故障:无法获取用户列表.");
		return userList;
	}

	public User getUserById(int id) throws Exception {
		User user = userDAO.queryUserById(id);
		if (null == user) throw new Exception("用户不存在.");
		return user;
	}

	public boolean bUsernameExists(String username) {
		User user = userDAO.queryUserByUsername(username);
		return (null != user);
	}

	public User updateUsernameById(int id, String username) throws Exception {
		//用户名已存在
		boolean bAlreadyExists = bUsernameExists(username);
		if (bAlreadyExists) throw new Exception("用户名已占用,未更改.");

		//UserId是否存在
		User u = userDAO.queryUserById(id);
		if (u == null) throw new Exception("用户不存在.");

		u.setUsername(username);
		//validate username
		String errMsg = validateUsername(u);
		if (errMsg != null && !errMsg.equals("")) {
			throw new Exception(errMsg);
		}

		//update
		int result = userDAO.updateUsernameById(id, username);
		if (result != 1) throw new Exception("无法更新用户名.");

		u = userDAO.queryUserById(id);
		if (null == u) throw new Exception("用户不存在.");
		return u;
	}

	public User updatePasswordById(int id, String password) throws Exception {
		//UserId是否存在
		User u = userDAO.queryUserById(id);
		if (u == null) throw new Exception("用户不存在.");

		u.setPassword(password);

		String errMsg = validatePassword(u);
		if (errMsg != null && !errMsg.equals("")) throw new Exception(errMsg);

		String salt = PasswordEncoder.getRandomString(32);
		password = PasswordEncoder.encodePassword(password, salt);
		int result = userDAO.updatePasswordById(id, password, salt);
		if (result != 1) throw new Exception("密码更新失败.");
		u = userDAO.queryUserById(id);
		if (null == u) throw new Exception("用户不存在.");
		return u;
	}

	public User updatePassword(String username, String password) throws Exception {
		User u = userLogin(username, password);
		u = updatePasswordById(u.getUserId(), password);
		return u;
	}

	public boolean deleteUserById(int id) {
		//exists?
		User u = userDAO.queryUserById(id);
		//can not del admin/supplier
		if (null == u
				|| u.getUserType() == UserType.ADMIN
				|| u.getUserType() == UserType.SUPPLIER)
			return false;
		//delete
		int result = userDAO.deleteUserById(id);
		if (result != 1) return false;
		//exists?
		u = userDAO.queryUserById(id);
		return (null == u);
	}

	public User insertUser(String username, String password) throws Exception {
		//用户名已存在
		User u = userDAO.queryUserByUsername(username);
		if (null != u) throw new Exception("用户名已存在.");

		User user = new User();
		user.setUsername(username);
		user.setPassword(password);

		String errMsg = validateUser(user);
		if (null != errMsg && !errMsg.equals("")) {
			throw new Exception(errMsg);
		}

		//insert
		String salt = PasswordEncoder.getRandomString(32);
		password = PasswordEncoder.encodePassword(password, salt);
		int result = userDAO.insertUser(username, password, salt, UserType.NORMAL.toInt());
		if (result != 1) throw new Exception("新建用户失败.");
		user = userDAO.queryUserByUsername(username);
		if (null == user) throw new Exception("新建用户失败.");
		return user;
	}

	public String validateUsername(User m_u) {
		Set<ConstraintViolation<User>> errors = validator.getValidator().validateProperty(m_u, "username");

		StringBuilder errMsg = new StringBuilder();
		for (ConstraintViolation<User> err : errors) {
			errMsg.append(err.getMessage());
		}
		return errMsg.toString();
	}

	public String validatePassword(User m_u) {
		Set<ConstraintViolation<User>> errors = validator.getValidator().validateProperty(m_u, "password");

		StringBuilder errMsg = new StringBuilder();
		for (ConstraintViolation<User> err : errors) {
			errMsg.append(err.getMessage());
		}
		return errMsg.toString();
	}

	public String validateUser(User m_u) {
		Set<ConstraintViolation<User>> errors_username = validator.getValidator().validateProperty(m_u, "username");
		Set<ConstraintViolation<User>> errors_password = validator.getValidator().validateProperty(m_u, "password");

		errors_username.addAll(errors_password);
		StringBuilder errMsg = new StringBuilder();
		for (ConstraintViolation<User> err : errors_username) {
			errMsg.append(err.getMessage());
		}
		return errMsg.toString();
	}

	public List<Address> getUserAddresses(int userId) {
		return addressDAO.queryAllAddressesOfUser(userId);
	}

	@Override
	public User updateUserInfo(User user, String address, String recevier, String telephone) throws Exception {
		User u = getUserById(user.getUserId());
		if (!user.equals(u)) {
			throw new Exception("用户信息已改变,请重新登录.");
		}
		user.setDefaultAddress(address);
		user.setDefaultReceiver(recevier);
		user.setDefaultTelephone(telephone);
		int result = userDAO.updateUserInfo(user);
		if (result != 1) {
			throw new Exception("用户信息更新失败.");
		}
		user = getUserById(user.getUserId());
		if (null == user) throw new Exception("获取用户信息失败.");
		return user;
	}
}
