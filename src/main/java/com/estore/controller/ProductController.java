package com.estore.controller;

import com.estore.object.Address;
import com.estore.object.Cart;
import com.estore.object.Product;
import com.estore.object.User;
import com.estore.object.enums.UserType;
import com.estore.service.OrderService;
import com.estore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes({"user", "cart"})
public class ProductController extends BaseController {
	@Autowired
	private ProductService productService;
	@Autowired
	private OrderService orderService;

	@GetMapping(value = "/product/{productId}")
	public String showProduct(
			@PathVariable int productId,
			@ModelAttribute User user,
			@ModelAttribute Cart cart,
			Model model) {
		initMsg();
		if (null == user || user.getUserType() != UserType.NORMAL)
			return "not_found";

		Product p = productService.getProductById(productId);
		if (null == p) {
			return "not_found";
		} else {
			model.addAttribute("product", p);
			model.addAttribute("cart", cart);
			model.addAttribute("user", user);
			return "product";
		}
	}

	@RequestMapping(value = "/cart/addToCart")
	@ResponseBody
	public ModelAndView addProductToCart(
			@ModelAttribute User user,
			@ModelAttribute Cart cart,
			@RequestBody int[] inputs,
			Model model) {
		initMsg();
		if (null == user || user.getUserType() != UserType.NORMAL) {
			errMsg = "权限不足.";
		} else {
			if (inputs.length != 2) {
				errMsg = "POST数据出错.";
			} else {
				int productId = inputs[0];
				int amount = inputs[1];
				Product p = productService.getProductById(productId);
				if (null == p) {
					errMsg = "商品不存在.";
				} else {
					//check amount
					if (amount < 1) amount = 1;
					//add
					if (cart.getProductHashMap().containsKey(p)) {
						amount += cart.getProductHashMap().get(p);
					}
					cart.getProductHashMap().put(p, amount);

					accMsg += "添加成功.";
				}
			}
		}
		return generateModelAndViewByMsg();
	}


	@PostMapping(value = "/cart/removeFromCart")
	@ResponseBody
	public ModelAndView removeProductFromCart(
			@ModelAttribute User user,
			@ModelAttribute Cart cart,
			@RequestBody int productId,
			Model model) {
		initMsg();
		if (null == user || user.getUserType() != UserType.NORMAL) {
			errMsg = "权限不足.";
		} else {

			Product p = productService.getProductById(productId);
			if (null == p) {
				errMsg += "商品不存在.";
			} else {
				if (null == cart) {
					cart = new Cart();
				}

				if (cart.getProductHashMap().containsKey(p)) {
					cart.getProductHashMap().remove(p);
				}
			}
			model.addAttribute("product", p);
			accMsg += "删除成功.";
		}
		return generateModelAndViewByMsg();
	}

	@RequestMapping(value = "/cart/clearCart")
	@ResponseBody
	public ModelAndView clearProductCart(
			@ModelAttribute User user,
			@ModelAttribute Cart cart,
			Model model) {
		initMsg();
		if (null == user || user.getUserType() != UserType.NORMAL) {
			errMsg += "权限不足.";
		} else {
			model.addAttribute("cart", new Cart());
			accMsg += "清空成功.";
		}
		return generateModelAndViewByMsg();
	}

	@RequestMapping(value = "/cart/submitCart")
	@ResponseBody
	public ModelAndView submitProductCart(
			@ModelAttribute User user,
			@ModelAttribute Cart cart,
			Model model) {
		initMsg();
		if (null == user || user.getUserType() != UserType.NORMAL) {
			errMsg += "权限不足.";
		} else {
			if (null == cart.getProductHashMap() ||
					cart.getProductHashMap().size() < 1) {
				errMsg += "购物车为空.";
			} else {
				gotoMsg = "/buy";
			}
		}
		return generateModelAndViewByMsg();
	}

	@GetMapping(value = "/buy")
	public String showOrder(
			@ModelAttribute User user,
			@ModelAttribute Cart cart,
			RedirectAttributes model) {
		initMsg();
		if (null == user || user.getUserType() != UserType.NORMAL) {
			return "not_found";
		} else {
			if (null == cart.getProductHashMap() || cart.getProductHashMap().size() < 1) {
				model.addFlashAttribute("message", "购物车为空.");
				return "redirect:/user/" + user.getUsername();
			} else {
				List<Address> addresses = userService.getUserAddresses(user.getUserId());
				model.addAttribute("addresses", addresses);
				return "buy";
			}
		}
	}

	@PostMapping(value = "/buy")
	@ResponseBody
	public ModelAndView submitOrder(
			@ModelAttribute User user,
			@ModelAttribute Cart cart,
			@RequestBody Map map,
			RedirectAttributes model) {
		initMsg();
		if (null == user || user.getUserType() != UserType.NORMAL) {
			errMsg = "权限不足.";
		} else {
			String receiver = (String)map.get("receiver");
			String address = (String)map.get("address");
			String telephone = (String)map.get("telephone");
			String password = (String)map.get("password");

			if (null == receiver || "" == receiver) {
				errMsg += "请输入收货人.";
			} else if (null == address || "" == address) {
				errMsg += "请输入地址.";
			} else if (null == telephone || "" == telephone) {
				errMsg += "请输入联系电话.";
			} else if (null == password || "" == password) {
				errMsg += "请输入密码.";
			}else {
				try {
					//validate password
					user = userService.userLogin(user.getUsername(), password);
				}catch (Throwable e) {
					errMsg += "UserService出错:"+e.getMessage();
					return generateModelAndViewByMsg();
				}

				//get products
				HashMap<Product,Integer> productList=new HashMap<Product, Integer>();
				if(null==map.get("products")){
					errMsg+="产品信息缺失.";
					return generateModelAndViewByMsg();
				}
				HashMap<String,String>hm=(HashMap<String,String>)map.get("products");
				if(null==hm){
					errMsg+="产品信息缺失.";
					return generateModelAndViewByMsg();
				}
				if(hm.size()<1){
					errMsg+="未选择购买任何产品.";
					return generateModelAndViewByMsg();
				}
				for(String _productId:hm.keySet()){
					int productId=Integer.valueOf(_productId);
					Product p=productService.getProductById(productId);
					if(null==p){
						errMsg+="产品信息错误.";
						return generateModelAndViewByMsg();
					}
					String _amount=hm.get(_productId);
					Integer amount=Integer.valueOf(_amount);
					if(p.getTotal()<amount){
						errMsg+="商品 "+p.getName()+" 库存不足.";
						return generateModelAndViewByMsg();
					}
					productList.put(p,amount);
					//remove from cart
					if(cart.getProductHashMap().containsKey(p)){
						cart.getProductHashMap().remove(p);
					}
				}

				try {
					orderService.generateNewOrder(
							user.getUserId(), productList,
							receiver, address, telephone);
				} catch (Throwable e) {
					errMsg += e.getMessage();
					return generateModelAndViewByMsg();
				}
				accMsg+="订单提交成功.";
				gotoMsg="/user/"+user.getUsername()+"/orders";
			}
		}
		return generateModelAndViewByMsg();
	}
}
