<jsp:useBean id="user" scope="session" type="com.estore.object.User"/>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="C" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
	<title><c:out value="{product.name}"/></title>
	<link rel="stylesheet" href="<c:url value="/js/lib/bootstrap/css/bootstrap.min.css"/>">
	<script type="text/javascript" src="<c:url value="/js/lib/jquery-3.2.1.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/lib/bootstrap/js/bootstrap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/product.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/buy.js"/>"></script>
</head>
<body>

<ul class="nav nav-tabs">
	<li role="presentation"><a href="/user/${user.username}/profile">${user.username}</a></li>
	<li role="presentation"><a href="/user/${user.username}">产品</a></li>
	<li role="presentation"><a href="/user/${user.username}/orders">订单</a></li>
	<li><a href="<c:url value="/logout"/>">退出</a></li>

	<li role="presentation" class="dropdown">
		<a class="dropdown-toggle" data-toggle="dropdown" href="#" role="button" aria-haspopup="true"
		   aria-expanded="false">
			购物车<span class="caret"></span>
		</a>
		<ul class="dropdown-menu" id="cart">
			<jsp:useBean id="cart" scope="session" type="com.estore.object.Cart"/>
			<c:forEach var="p" items="${cart.productHashMap}">
				<div class="btn-group btn-group-justified" role="group">
					<a class="input-group-addon" href="/product/${p.key.productId}">${p.key.name}
						×${p.value.intValue()}</a>
					<a class="input-group-addon" onclick="removeProductFromCart(${p.key.productId})">删除</a>
				</div>
			</c:forEach>
			<div class="btn-group-justified" role="group">
				<li class="btn-group">
					<button type="button" class="btn btn-default" onclick="clearCart()">清空</button>
				</li>
				<li class="btn-group">
					<button type="button" class="btn btn-default" onclick="trySubmitCart()">结算</button>
				</li>
			</div>
		</ul>
	</li>
</ul>
<div class="alert alert-warning" id="message" style="display: none;"></div>

<table class="table" id="table_products">
	<tr>
		<th>商品名</th>
		<th>数量</th>
		<th>单价</th>
		<th>合计</th>
		<th></th>
	</tr>
	<jsp:useBean id="cart" scope="session" type="com.estore.object.Cart"/>
	<c:forEach var="p" items="${cart.productHashMap}">
		<tr>
			<td>
				<p hidden>${p.key.productId}</p>
					${p.key.name}
			</td>
			<td>
				<div class="input-group">
					<span class="input-group-btn"><button class="btn btn-default" type="button"
														  onclick="minus2(document.getElementById('amount_${p.key.productId}'),document.getElementById('totalPrice_${p.key.productId}'),${p.key.price})">-</button></span>
					<input type="text" name="amount" id="amount_${p.key.productId}" class="form-control"
						   value="${p.value}"
						   onkeyup="this.value=this.value.replace(/\D/g,'');" title=""/>
					<span class="input-group-btn"><button class="btn btn-default" type="button"
														  onclick="plus2(document.getElementById('amount_${p.key.productId}'),document.getElementById('totalPrice_${p.key.productId}'),${p.key.price})">+</button></span>
				</div>
			</td>
			<td>
					${p.key.price}
			</td>
			<td>
				<label id="totalPrice_${p.key.productId}">${p.key.price*p.value}</label>
			</td>
			<td>
				<button class="btn btn-warning" onclick="removeProductFromTable(this,${p.key.productId})">暂不购买</button>
			</td>
		</tr>
	</c:forEach>
	<tr>
		<th colspan="3">合计</th>
		<td colspan="2"><p id="totalPrice">总计</p></td>
	</tr>
</table>

收货人:
<input id="receiver" class="form-control" value="${user.defaultReceiver}" title="">

联系方式:
<input id="telephone" class="form-control" value="${user.defaultTelephone}" onchange="checkPhoneNum(this)" title="">

收货地址:

<!--
<div class="input-group">
	<jsp:useBean id="addresses" scope="session" type="java.util.List"/>
	<c:forEach var="address" items="${addresses}">
		<span class="input-group">
			<input type="radio" name="address" id=${address}" value="${address}">
		</span>
		${address}
	</c:forEach>
	<span class="input-group-addon">
        <input type="radio" id="ratio_new_address" name="address">
	</span>

	<input id="new_address" type="text" class="form-control" placeholder="新地址...">
</div>
-->
<input id="new_address" type="text" class="form-control" placeholder="新地址..." value="${user.defaultAddress}">

<br/>
输入密码:
<div align="center">
	<input class="form-control" id="password" type="password" autocomplete="off" title="">
</div>

<div align="right" style="padding: 24px;">
	<button class="btn btn-success" onclick="submitOrder()">提交订单</button>
</div>

<script defer="defer">
	var _products = [];
	calcTotalPrice();
	<c:forEach var="p" items="${cart.productHashMap}">
	_products.push(${p.key.productId});
	</c:forEach>
</script>
</body>
</html>