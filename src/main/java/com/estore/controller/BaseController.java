package com.estore.controller;

import com.estore.object.User;
import com.estore.service.OrderService;
import com.estore.service.ProductService;
import com.estore.service.UserService;
import com.google.common.base.Throwables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.HashMap;
import java.util.Map;

@Controller
public class BaseController {
	final UserService userService;
	final ProductService productService;
	final OrderService orderService;

	String errMsg = "";
	String accMsg = "";
	String gotoMsg = "";

	@Autowired
	public BaseController(UserService userService, ProductService productService, OrderService orderService) {
		this.userService = userService;
		this.productService = productService;
		this.orderService = orderService;
	}

	//only validate password
	private boolean validateUserPassword(User m_u, String m_password) {
		//validate password
		m_u.setPassword(m_password);

		errMsg = userService.validatePassword(m_u);

		return errMsg.equals("");
	}

	User checkAndUpdatePassword(User user, String old_pwd, String new_pwd) {
		User temp = new User();
		//validate new password
		if (!validateUserPassword(temp, new_pwd)) {
			return null;
		}
		//verify old password
		try {
			User u = userService.userLogin(user.getUsername(), old_pwd);
		} catch (Exception ex) {
			errMsg += "旧密码不正确.";
			return null;
		}
		//try update
		try {
			temp = userService.updatePasswordById(user.getUserId(), new_pwd);
		} catch (Exception e) {
			errMsg += e.getMessage();
		}
		if (null == temp) {
			errMsg += "UserService出错.";
			return null;
		} else {
			accMsg += "密码已更改.";
			return temp;
		}
	}

	void initMsg() {
		errMsg = accMsg = gotoMsg = "";
	}

	ModelAndView generateModelAndViewByMsg() {
		Map<String, String> map = new HashMap<String, String>();
		String status;
		if (!gotoMsg.equals("")) {
			status = "GOTO";
		} else if (errMsg.equals("") && accMsg.equals("")) {
			status = "REMAIN";
		} else if (!errMsg.equals("")) {
			status = "ERROR";
		} else {
			status = "ACCEPT";
		}
		map.put("stat", status);
		map.put("err", errMsg);
		map.put("acc", accMsg);
		map.put("goto", gotoMsg);

		return new ModelAndView(new MappingJackson2JsonView(), map);
	}
}
