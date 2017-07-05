package com.estore.controller;

import com.estore.object.Item;
import com.estore.object.Order;
import com.estore.object.Product;
import com.estore.object.User;
import com.estore.object.enums.UserType;
import com.estore.service.OrderService;
import com.estore.utils.CommonFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@SessionAttributes("user")
public class SupplierController extends BaseController {
	@Autowired
	OrderService orderService;


	@RequestMapping(value = "/user/supplier", method = GET)
	public String showDefaultUserPage(
			@ModelAttribute User user,
			Model model) {
		if (null != user && null != user.getUserType() &&
				user.getUserType() == UserType.SUPPLIER) {
			try {
				//获取商品列表
				List<Product> productList = productService.listAllProducts();
				model.addAttribute("productList", productList);
				//获取订单
				List<Order> all_orders = orderService.getAllOrders();
				List<Order> unprocessed_orders = new ArrayList<Order>();
				List<Order> processed_orders = new ArrayList<Order>();
				//获取订单项
				for (Order order : all_orders) {
					if (order.getStatus().equals("已提交")) {
						unprocessed_orders.add(order);
					} else {
						processed_orders.add(order);
					}
				}
				model.addAttribute("processed_orders", processed_orders);
				model.addAttribute("unprocessed_orders", unprocessed_orders);

				for (Order order : all_orders) {
					List<Item> items = orderService.getAllItemsOfOrder(order.getOrderId());
					model.addAttribute("itemList_in_order_" + order.getOrderId(),
							items);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return "supplier";

		}
		return "not_found";
	}

	@PostMapping(value = "/user/supplier/updateProduct")
	public ModelAndView updateProduct(
			@ModelAttribute User user,
			@RequestBody Product product) {
		initMsg();
		if (null == user || user.getUserType() != UserType.SUPPLIER)
			return null;

		boolean bNewProduct = (product.getProductId() == -1);

		Product oldProduct = null;
		if (!bNewProduct) {
			oldProduct = productService.getProductById(product.getProductId());
			if (null == oldProduct) {
				errMsg += "产品不存在.";
				return generateModelAndViewByMsg();
			}
		} else {
			oldProduct = product;
		}
		if (bNewProduct) {
			if (null == product.getPicPath()) {
				product.setPicPath("");
			}
			try {
				product = productService.addProduct(product);
			} catch (Exception e) {
				errMsg += e.getMessage();
			}
			accMsg += "添加产品成功.";
			gotoMsg = "/user/supplier/product/" + product.getProductId();
		} else {
			//name
			oldProduct.setName(product.getName());
			//price
			oldProduct.setPrice(product.getPrice());
			//total
			oldProduct.setTotal(product.getTotal());
			//on_sale
			oldProduct.setOnSale(product.isOnSale());
			//description
			oldProduct.setDescription(product.getDescription());

			//update product
			try {
				product = productService.updateProduct(oldProduct);
			} catch (Exception e) {
				errMsg += e.getMessage();
			}
			accMsg += "产品信息更新成功.";
		}

		return generateModelAndViewByMsg();
	}

	@RequestMapping(value = "/user/supplier/product/{id}", method = GET)
	public String showProductDetails(
			@PathVariable int id,
			@ModelAttribute User user,
			Model model) {
		initMsg();
		if (null == user || user.getUserType() != UserType.SUPPLIER)
			return "not_found";

		Product product = productService.getProductById(id);
		if (null == product) {
			return "not_found";
		}
		model.addAttribute("action", "update");
		model.addAttribute("product", product);
		return "product_details";
	}

	@RequestMapping(value = "/user/supplier/newProduct", method = GET)
	public String showProductDetails(
			@ModelAttribute User user,
			Model model) {
		initMsg();
		if (null == user || user.getUserType() != UserType.SUPPLIER)
			return "not_found";

		model.addAttribute("action", "add");
		Product p = new Product();
		try {
			productService.addProduct(p);
		} catch (Exception e) {
			return "/error";
		}
		model.addAttribute("product", p);
		return "product_details";
	}

	@RequestMapping(value = "/user/supplier/product/{id}", method = POST)
	@ResponseBody
	public ModelAndView handleFileUpload(
			@PathVariable int id,
			@ModelAttribute User user,
			@RequestParam("file") MultipartFile file,
			HttpServletRequest request,
			Model model) {
		initMsg();
		if (null == user || user.getUserType() != UserType.SUPPLIER)
			return null;

		accMsg = errMsg = "";
		boolean bNewProduct = (id == -1);

		if (!file.isEmpty()) {
			Product p = null;
			if (!bNewProduct) {    //not add
				p = productService.getProductById(id);
				if (null == p) {
					errMsg += "产品信息不存在.";
					return generateModelAndViewByMsg();
				}
			}

			String tempFilePath = request.getSession().getServletContext().getRealPath("/upload/")
					+ "_temp";
			File tempFile = new File(tempFilePath);
			try {
				// 转存文件
				file.transferTo(tempFile);
			} catch (Exception e) {
				e.printStackTrace();
				errMsg += "文件已存在.";
			}
			String fileMd5 = CommonFileUtil.fileMd5(tempFilePath);

			String destFilePath = request.getSession().getServletContext().getRealPath("/upload/")
					+ fileMd5;
			File destFile = new File(destFilePath);
			tempFile.renameTo(destFile);

			if (!destFile.exists()) {
				errMsg += "文件上传出错.";
			}

			if (bNewProduct) {//add new
				p = new Product();
				p.setPicPath("/upload/" + fileMd5);
				try {
					p = productService.addProduct(p);
					accMsg += "添加产品成功.文件上传成功.";
					gotoMsg = "/user/supplier/product/" + p.getProductId();
					model.addAttribute("product", p);
				} catch (Exception e) {
					errMsg += e.getMessage();
				}
			} else {
				p.setPicPath("/upload/" + fileMd5);
				try {
					p = productService.updateProduct(p);
					accMsg += "添加产品成功.文件上传成功.";
					model.addAttribute("product", p);
				} catch (Exception e) {
					errMsg += e.getMessage();
				}
			}
		} else {
			errMsg += "未上传任何文件.";
			model.addAttribute("product", productService.getProductById(id));
		}
		return generateModelAndViewByMsg();
	}


	@RequestMapping(value = "/user/supplier/deleteSelected")
	@ResponseBody
	public ModelAndView deleteSelectedProducts(
			@ModelAttribute User user,
			@RequestBody int[] idList) {
		initMsg();
		if (null == user || user.getUserType() != UserType.SUPPLIER)
			return null;

		List<Integer> deleted = new ArrayList<Integer>();
		List<Integer> notDeleted = new ArrayList<Integer>();
		for (int id : idList) {
			if (productService.deleteProductById(id)) {
				deleted.add(id);
			} else {
				notDeleted.add(id);
			}
		}

		if (!deleted.isEmpty()) {
			accMsg += "已成功删除的产品:id=" + deleted.toString();
		}
		if (!notDeleted.isEmpty()) {
			errMsg += "未成功删除的产品:id=" + notDeleted.toString();
		}
		if (accMsg.equals("") && errMsg.equals("")) {
			errMsg = "未识别的操作.";
		}
		return generateModelAndViewByMsg();
	}

	@RequestMapping(value = "/user/supplier/processOrder")
	@ResponseBody
	public ModelAndView processOrder(
			@ModelAttribute User user,
			@RequestBody Map map) {
		initMsg();
		if (null == user || user.getUserType() != UserType.SUPPLIER)
			return null;

		String _orderId = (String) map.get("orderId");
		String operation = (String) map.get("operation");
		if (null == _orderId || null == operation) {
			errMsg += "提交参数不完整.";
		} else {
			Integer orderId = 0;
			Order order = null;
			try {
				orderId = Integer.valueOf(_orderId);
				order = orderService.getOrderByOrderId(orderId);
			} catch (Exception ex) {
				errMsg += "订单号错误或订单不存在.";
			}

			if (operation.equals("confirm")) {
				try {
					orderService.confirmOrder(order);
					accMsg += "订单已确认.";
				} catch (Exception e) {
					errMsg += e.getMessage();
				}
			} else if (operation.equals("reject")) {
				try {
					orderService.rejectOrder(order);
					accMsg += "订单已拒绝.";
				} catch (Exception ex) {
					errMsg += ex.getMessage();
				}
			} else {
				errMsg += "未知操作.";
			}
		}
		return generateModelAndViewByMsg();
	}
}
