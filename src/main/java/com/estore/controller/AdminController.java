package com.estore.controller;

import com.estore.object.User;
import com.estore.object.enums.UserType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


@Controller
@SessionAttributes("user")
public class AdminController extends BaseController {

	@RequestMapping(value = "/user/admin", method = GET)
	public String showDefaultUserPage(
			@ModelAttribute User user,
			Model model) {
		if (null != user && null != user.getUserType() &&
				user.getUserType() == UserType.ADMIN) {
			model.addAttribute("m_user_id", -1);
			model.addAttribute("err", "");
			model.addAttribute("acc", "");
			List<User> userList = null;
			try {
				userList = userService.listAllUsers();
			} catch (Exception e) {
				e.printStackTrace();
				return "not_found";
			}
			model.addAttribute("userList", userList);
			return "admin";
		}
		return "not_found";
	}

	@RequestMapping(value = "/user/admin", method = POST)
	public String adminManagement(
			@ModelAttribute User user,
			Model model,
			@RequestParam int m_userId,
			@RequestParam String m_username,
			@RequestParam String m_password1,
			@RequestParam String m_password2) {
		initMsg();
		if (null == user || null == user.getUserType() || user.getUserType() != UserType.ADMIN)
			return "not_found";

		MANAGE:
		try {
			//userExists
			User m_u = userService.getUserById(m_userId);

			//change username
			if (!m_username.equals(m_u.getUsername())) {
				m_u = userService.updateUsernameById(m_u.getUserId(), m_username);
				accMsg += "用户名已更改为:" + m_u.getUsername() + ".";
			}

			//change password
			if ((null != m_password1 && null != m_password2) &&
					("" != m_password1 || "" != m_password2)) {
				if ("" == m_password1) {
					errMsg += "新密码为空.";
				} else if ("" == m_password2) {
					errMsg += "重输密码为空.";
				} else if (!m_password1.equals(m_password2)) {
					errMsg += "密码不一致.";
				} else {
					//real update password here
					m_u = userService.updatePasswordById(m_u.getUserId(), m_password1);
					accMsg += "密码已更改.";
				}
			}
			//if admin,update user
			if (m_u.getUserType() == UserType.ADMIN) {
				m_u = userService.getUserById(m_u.getUserId());
				if (null != m_u) {
					model.addAttribute("user", m_u);
				}
			}
		} catch (Exception e) {
			errMsg = e.getMessage();
		} finally {
			//add messages
			model.addAttribute("m_user_id", m_userId);
			model.addAttribute("err", errMsg);
			if (accMsg.equals("") && errMsg.equals(""))
				accMsg = "没有更改项.";
			model.addAttribute("acc", accMsg);

			//show all users again
			List<User> userList = null;
			try {
				userList = userService.listAllUsers();
			} catch (Exception e) {
				return "/error";
			}
			model.addAttribute("userList", userList);
			return "admin";
		}
	}

	@RequestMapping(value = "/user/admin/addNewUser")
	public ModelAndView addNewUser(
			@ModelAttribute User user,
			@RequestBody Map map) {
		initMsg();
		if (null == user || user.getUserType() != UserType.ADMIN)
			return null;

		errMsg = "";
		accMsg = "";
		ADD_USER:
		try {
			String m_username = (String) map.get("username");
			String m_password1 = (String) map.get("password");
			String m_password2 = (String) map.get("password2");

			//password1
			if (null == m_username || "" == m_username) {
				errMsg += "用户名为空.";
				break ADD_USER;
			}
			//password1
			if (null == m_password1 || "" == m_password1) {
				errMsg += "新密码为空.";
				break ADD_USER;
			}
			//password2
			if (null == m_password2 || "" == m_password2) {
				errMsg += "重输密码为空.";
				break ADD_USER;
			}
			if (!m_password1.equals(m_password2)) {
				errMsg += "两次输入密码不一致.";
				break ADD_USER;
			}
			User m_u = userService.insertUser(m_username, m_password1);
			accMsg += "新建用户:" + m_u.getUsername() + "成功.";
		} catch (Exception e) {
			errMsg += e.getMessage();
		}
		return generateModelAndViewByMsg();
	}

	@RequestMapping(value = "/user/admin/deleteSelected")
	@ResponseBody
	public ModelAndView deleteSelectedUsers(
			@ModelAttribute User user,
			@RequestBody int[] idList) {
		initMsg();
		if (null == user || user.getUserType() != UserType.ADMIN)
			return null;

		List<Integer> deleted = new ArrayList<Integer>();
		List<Integer> notDeleted = new ArrayList<Integer>();
		for (int id : idList) {
			if (userService.deleteUserById(id)) {
				deleted.add(id);
			} else {
				notDeleted.add(id);
			}
		}

		if (!deleted.isEmpty()) {
			accMsg += "已成功删除的用户:id=" + deleted.toString();
		}
		if (!notDeleted.isEmpty()) {
			errMsg += "未成功删除用户:id=" + notDeleted.toString();
		}
		if (accMsg.equals("") && errMsg.equals("")) {
			errMsg = "未识别的操作.";
		}
		return generateModelAndViewByMsg();
	}
}