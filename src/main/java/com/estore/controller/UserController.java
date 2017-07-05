package com.estore.controller;

import com.estore.object.*;
import com.estore.object.enums.UserType;
import com.estore.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


@Controller
@SessionAttributes({"user", "cart"})
public class UserController extends BaseController {
	@Autowired
	OrderService orderService;

	@GetMapping(value = {"/", "/login"})
	public String showLoginForm(Model model) {
		if (!model.containsAttribute("user"))
			model.addAttribute("user", new User(-1, "", ""));
		if (!model.containsAttribute("cart"))
			model.addAttribute("cart", new Cart());
		return "loginForm";
	}

	@RequestMapping(value = "/logout")
	public String logoutAndLogin(HttpSession session, Model model) {
		session.invalidate();
		if(model.containsAttribute("user")) model.asMap().remove("user");
		if(model.containsAttribute("cart")) model.asMap().remove("cart");
		return "redirect:/login";
	}

	@PostMapping(value = "/login")
	public String processLogin(@Valid User user, BindingResult errors, RedirectAttributes model) {
		if (errors.hasErrors()) {
			return "loginForm";
		}

		try {
			user = userService.userLogin(user.getUsername(), user.getPassword());
		} catch (Exception e) {
			errors.addError(
					new FieldError(
							"user",
							"password",
							e.getMessage()
					));
			return "loginForm";
		}
		model.addAttribute("username", user.getUsername());
		model.addFlashAttribute("user", user);
		model.addFlashAttribute("cart", new Cart());
		return "redirect:/user/{username}";
	}

	@RequestMapping(value = "/user/{username}", method = GET)
	public String showDefaultUserPage(
			@PathVariable String username,
			@ModelAttribute User user,
			@ModelAttribute Cart cart,
			Model model) {
		if (null != user &&
				username == user.getUsername() &&
				null != user.getUserType() &&
				user.getUserType() == UserType.NORMAL) {
			List<Product> productList2 = null;
			try {
				productList2 = productService.listAllOnsaleProducts();
			} catch (Exception e) {
				e.printStackTrace();
			}
			model.addAttribute("productList", productList2);
			return "shop";
		}
		return "not_found";
	}


	@RequestMapping(value = "/user/{username}", method = POST)
	public String searchResults(
			@PathVariable String username,
			@ModelAttribute User user,
			@ModelAttribute Cart cart,
			@RequestParam String keyword,
			Model model) throws UnsupportedEncodingException {
		if (null != user && username == user.getUsername() &&
				null != user.getUserType() && user.getUserType() == UserType.NORMAL) {

			if (null != keyword)
				keyword = new String(keyword.getBytes("ISO-8859-1"), "UTF-8");
			List<Product> productList2 = null;
			try {
				productList2 = productService.listAllOnsaleProducts(keyword);
			} catch (Exception e) {
				errMsg += e.getMessage();
				return "not_found";
			}
			model.addAttribute("keyword", keyword);
			model.addAttribute("productList", productList2);
			return "shop";
		}

		return "not_found";
	}

	@RequestMapping(value = "/user/{username}/profile", method = GET)
	public String showDefaultUserPage(
			@PathVariable String username,
			@ModelAttribute User user,
			Model model) {
		if (null == user || user.getUserType() != UserType.NORMAL)
			return "not_found";

		return "profile";
	}

	@RequestMapping(value = "/user/{username}/orders{filter}")
	public String showOrders(
			@PathVariable String username,
			@PathVariable(required = false) String filter,
			@ModelAttribute User user,
			@RequestParam(required = false) String post_order_id,
			Model model) {
		if (null == user || user.getUserType() != UserType.NORMAL)
			return "not_found";

		try {
			List<Order> all_orders = orderService.getAllOrdersOfUser(user.getUserId());
			List<Order> filtered_orders = new ArrayList<Order>();
			//if search order id
			if (null != post_order_id) {
				try {
					Integer orderId = Integer.valueOf(post_order_id);
					boolean found = false;
					for (Order order : all_orders) {
						if (orderId.equals(order.getOrderId())) {
							found = true;
							filtered_orders.add(order);
						}
					}
				} catch (Exception ex) {
					model.addAttribute("message", "无效订单号.");
				}
			} else {
				//filter
				if (null == filter || filter.equals("")) {
					filtered_orders = all_orders;
					model.addAttribute("filter", "");
				} else {
					for (Order order : all_orders) {
						if (order.getStatus().equals(filter)) {
							filtered_orders.add(order);
						}
					}
					model.addAttribute("filter", filter);
				}
			}

			model.addAttribute("orderList", filtered_orders);

			for (Order order : filtered_orders) {
				List<Item> items = orderService.getAllItemsOfOrder(order.getOrderId());
				model.addAttribute("itemList_in_order_" + order.getOrderId(),
						items);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "not_found";
		}
		return "order";
	}


	@RequestMapping(value = "/user/{username}/editPassword")
	@ResponseBody
	public ModelAndView editPassword(
			Model model,
			@PathVariable String username,
			@ModelAttribute User user,
			@RequestBody Map map) {
		initMsg();
		if (null == user || !username.equals(user.getUsername())) {
			errMsg += "用户校验失败.";
			gotoMsg = "/logout";
		} else {
			User u = null;
			try {
				u = userService.getUserById(user.getUserId());
			} catch (Exception e) {
				errMsg += "用户校验失败.";
				gotoMsg = "/logout";
				return generateModelAndViewByMsg();
			}
			EDIT_PWD:
			try {
				String old_pwd = (String) map.get("old_password");
				String new_pwd = (String) map.get("new_password1");
				String new_pwd2 = (String) map.get("new_password2");

				//password old
				if (null == old_pwd || "" == old_pwd) {
					errMsg += "旧密码为空.";
					break EDIT_PWD;
				}
				//password1
				if (null == new_pwd || "" == new_pwd) {
					errMsg += "新密码为空.";
					break EDIT_PWD;
				}
				//password2
				if (null == new_pwd2 || "" == new_pwd2) {
					errMsg += "重输密码为空.";
					break EDIT_PWD;
				}
				if (!new_pwd.equals(new_pwd2)) {
					errMsg += "两次输入密码不一致.";
					break EDIT_PWD;
				}
				//validate password
				User temp = checkAndUpdatePassword(user, old_pwd, new_pwd);
				if (null != temp) {
					//更新user
					model.addAttribute("user", temp);
				}
			} catch (Exception e) {
				errMsg+=e.getMessage();
			}
		}
		return generateModelAndViewByMsg();
	}

	@RequestMapping(value = "/user/{username}/updateInfo")
	@ResponseBody
	public ModelAndView updateInfo(
			Model model,
			@PathVariable String username,
			@ModelAttribute User user,
			@RequestBody Map map) {
		initMsg();
		if (null == user || !username.equals(user.getUsername())) {
			errMsg += "用户校验失败.";
			gotoMsg = "/logout";
		} else {
			User u = null;
			try {
				u = userService.getUserById(user.getUserId());
			} catch (Exception e) {
				errMsg += "用户校验失败.";
				gotoMsg = "/logout";
				return generateModelAndViewByMsg();
			}
			UPDATE_INFO:
			try {
				String address = (String) map.get("address");
				String receiver = (String) map.get("receiver");
				String telephone = (String) map.get("telephone");

				//password old
				if (null == address) {
					errMsg += "地址信息丢失.";
					break UPDATE_INFO;
				}
				//password1
				if (null == receiver) {
					errMsg += "收货人信息丢失.";
					break UPDATE_INFO;
				}
				//password2
				if (null == telephone) {
					errMsg += "联系电话信息丢失.";
					break UPDATE_INFO;
				}
				if (address.equals(user.getDefaultAddress()) &&
						telephone.equals(user.getDefaultTelephone()) &&
						receiver.equals(user.getDefaultReceiver())) {
					accMsg += "用户信息未变更.";
				} else {
					//validate password
					User temp = userService.updateUserInfo(user, address, receiver, telephone);
					if (null != temp) {
						accMsg += "用户信息已更新.";
						//更新user
						model.addAttribute("user", temp);
					}
				}
			} catch (Exception ex) {
				System.out.println(ex);
			}
		}
		return generateModelAndViewByMsg();
	}
}