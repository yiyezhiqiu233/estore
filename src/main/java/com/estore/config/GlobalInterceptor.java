package com.estore.config;

import com.estore.object.User;
import com.estore.object.enums.UserType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class GlobalInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(
			HttpServletRequest request,
			HttpServletResponse response,
			Object handler) throws Exception {
		String requestURI = request.getRequestURI();
		if (requestURI.indexOf("/user") == 0) {
			User user = getUser(request);
			if (null == user) return false;
			if (requestURI.indexOf("/user/" + user.getUsername()) != 0)
				return false;
		} else if (requestURI.indexOf("/buy") == 0 ||
				requestURI.indexOf("/product/") == 0 ||
				requestURI.indexOf("/cart/") == 0
				) {
			User user = getUser(request);
			if (null == user || user.getUserType() == null||user.getUserType() != UserType.NORMAL) return false;
		}
		return true;
	}

	private User getUser(HttpServletRequest request) {
		HttpSession session = request.getSession();
		return (User) session.getAttribute("user");
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

	}
}
