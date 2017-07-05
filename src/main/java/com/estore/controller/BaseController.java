package com.estore.controller;

import com.estore.object.User;
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
	protected @Autowired
	UserService userService;
	protected @Autowired
	ProductService productService;

	protected String errMsg = "";
	protected String accMsg = "";
	protected String gotoMsg = "";

	//only validate password
	protected boolean validateUserPassword(User m_u, String m_password) {
		//validate password
		m_u.setPassword(m_password);

		errMsg = userService.validatePassword(m_u);

		if (!errMsg.equals("")) {
			return false;
		} else {
			return true;
		}
	}

	protected User checkAndUpdatePassword(User user, String old_pwd, String new_pwd) {
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

	protected void initMsg() {
		errMsg = accMsg = gotoMsg = "";
	}

	protected ModelAndView generateModelAndViewByMsg() {
		Map map = new HashMap();
		String status = "";
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

		ModelAndView mv = new ModelAndView(new MappingJackson2JsonView(), map);
		return mv;
	}


	private String getExceptionMessage(
			Throwable throwable,
			Integer statusCode) {
		if (throwable != null) {
			return Throwables.getRootCause(throwable).getMessage();
		}
		HttpStatus httpStatus;
		httpStatus = HttpStatus.valueOf(statusCode);
		return httpStatus.getReasonPhrase();
	}
}
