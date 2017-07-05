<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="tagform" uri="http://www.springframework.org/tags/form" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="utf-8"%>

<%
	request.setCharacterEncoding("utf-8");
%>
<html>
<head>
	<title>商场</title>
	<link rel="stylesheet" type="text/css" href="/css/default.css">
	<link rel="stylesheet" href="/js/bootstrap/css/bootstrap.min.css">
	<script type="text/javascript" src="/js/jquery-3.2.1.min.js"></script>
	<script type="text/javascript" src="/js/bootstrap/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="/js/product.js"></script>
</head>
<body>

<ul class="nav nav-tabs">
	<li role="presentation"><a href="/user/${user.username}/profile">${user.username}</a></li>
	<li role="presentation" class="active"><a href="/user/${user.username}">商店</a></li>
	<li role="presentation"><a href="/user/${user.username}/orders">订单</a></li>
	<li><a href="/logout">退出</a></li>

	<li role="presentation" class="dropdown">
		<a class="dropdown-toggle" data-toggle="dropdown" href="#" role="button" aria-haspopup="true"
		   aria-expanded="false">
			购物车<span class="caret"></span>
		</a>
		<ul class="dropdown-menu" id="cart">
			<c:forEach var="p" items="${cart.productHashMap}">
				<div class="btn-group btn-group-justified" role="group">
					<a class="input-group-addon" href="/product/${p.key.productId}">${p.key.name}
						×${p.value.intValue()}</a>
					<a class="input-group-addon" onclick="removeProductFromCart(${p.key.productId})">删除</a>
				</div>
			</c:forEach>
			<div class="btn-group-justified" role="group">
				<li class="btn-group" role="group">
					<button type="button" class="btn btn-default" onclick="clearCart()">清空</button>
				</li>
				<li class="btn-group" role="group">
					<button type="button" class="btn btn-default" onclick="trySubmitCart()">结算</button>
				</li>
			</div>
		</ul>
	</li>
</ul>
<div class="alert alert-warning" id="message" style="display: none"></div>

<form method="post" id="search_form">
	<div class="input-group">
		<input type="text" class="form-control" name="keyword"
		<c:if test="${empty keyword}">
			   placeholder="搜索商品...">
		</c:if>
		<c:if test="${not empty keyword}">
			placeholder="${keyword}">
		</c:if>
		<span class="input-group-addon" id="basic-addon1"
			  onclick="document.getElementById('search_form').submit()"><span class="glyphicon glyphicon-search"></span></span>
	</div>
</form>

<div id="top_product" class="tab-pane" align="center">
	<c:if test="${empty productList}">没有搜索到指定商品.</c:if>
	<div class="row">
		<c:forEach var="product" items="${productList}">
			<div class="col-sm-4">
				<a href="/product/${product.productId}" class="thumbnail">
					<img src="${product.picPath}" onerror="this.src='/upload/blank.jpg'">
						${product.name} ￥<fmt:formatNumber pattern=".00" value="${product.price}"></fmt:formatNumber>
				</a>
			</div>
		</c:forEach>
	</div>
</div>

</body>
</html>