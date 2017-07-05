<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="tagform" uri="http://www.springframework.org/tags/form" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
	<title>${user.username}--用户信息</title>
	<link rel="stylesheet" type="text/css" href="/css/default.css">
	<link rel="stylesheet" href="/js/bootstrap/css/bootstrap.min.css">
	<script type="text/javascript" src="/js/jquery-3.2.1.min.js"></script>
	<script type="text/javascript" src="/js/bootstrap/js/bootstrap.min.js"></script>
	<script type="text/javascript" src="/js/product.js"></script>
	<script type="text/javascript" src="/js/user.js"></script>
</head>
<body>

<ul class="nav nav-tabs">
	<li role="presentation" class="active"><a href="/user/${user.username}/profile">${user.username}</a></li>
	<li role="presentation"><a href="/user/${user.username}">商店</a></li>
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

<div class="tab-pane" align="center">
	<br/>
	<table class="table">
		<tr>
			<td align="right">旧密码:</td>
			<td><input class="form-control" type="password" id="old_password" autocomplete="off"/></td>
			<td></td>
		</tr>
		<tr>
			<td align="right">新密码:</td>
			<td><input class="form-control" type="password" id="new_password" autocomplete="off"/></td>
			<td></td>
		</tr>
		<tr>
			<td align="right">重输密码:</td>
			<td><input class="form-control" type="password" id="new_password2" autocomplete="off"/></td>
			<td></td>
		</tr>
		<tr>
			<td align="right"></td>
			<td><input type="button" class="btn btn-default" id="edit_pwd" value="更改密码"
					   onclick='editPassword("${user.username}")'/>
			<td></td>
			</td>
		</tr>
	</table>
	<br/>
	<table class="table">
		<tr>
			<td align="right">默认收货人:</td>
			<td><input class="form-control" type="text" id="receiver" autocomplete="off"
					   value="${user.defaultReceiver}"/></td>
			<td></td>
		</tr>
		<tr>
			<td align="right">默认联系电话:</td>
			<td><input class="form-control" type="text" id="telephone" autocomplete="off"
					   value="${user.defaultTelephone}"/></td>
			<td></td>
		</tr>
		<tr>
			<td align="right">默认收货地址:</td>
			<td><input class="form-control" type="text" id="address" autocomplete="off"
					   value="${user.defaultAddress}"/></td>
			<td></td>
		</tr>
		<tr>
			<td align="right"></td>
			<td><input type="button" class="btn btn-default" id="update_info" value="更新信息"
					   onclick='updateInfo("${user.username}")'/>
			<td></td>
			</td>
		</tr>
	</table>
</div>

</body>
</html>