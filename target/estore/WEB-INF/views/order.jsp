<jsp:useBean id="user" scope="session" type="com.estore.object.User"/>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="tagform" uri="http://www.springframework.org/tags/form" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
	<title>商场</title>
	<link rel="stylesheet" href="<c:url value="/js/lib/bootstrap/css/bootstrap.min.css"/>">
	<script type="text/javascript" src="<c:url value="/js/lib/jquery-3.2.1.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/lib/bootstrap/js/bootstrap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/product.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/order.js"/>"></script>
</head>
<body>

<ul class="nav nav-tabs">
	<li role="presentation"><a href="/user/${user.username}/profile">${user.username}</a></li>
	<li role="presentation"><a href="/user/${user.username}">商店</a></li>
	<li role="presentation" class="active"><a href="">订单</a></li>
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
				<li class="btn-group" >
					<button type="button" class="btn btn-default" onclick="clearCart()">清空</button>
				</li>
				<li class="btn-group" >
					<button type="button" class="btn btn-default" onclick="trySubmitCart()">结算</button>
				</li>
			</div>
		</ul>
	</li>
</ul>
<form method="post">
	<div class="input-group">
		<input type="text" id="post_order_id" name="post_order_id" class="form-control"
			   onchange="checkNum()" placeholder="输入订单号">
		<span class="input-group-btn">
        	<button class="btn btn-default" type="submit">搜索</button>
		</span>
	</div>
</form>
<div class="alert alert-warning" id="message" style="display: none;">${message}</div>

<nav class="navbar navbar-default">
	<ul class="nav navbar-nav">
		<li <c:if test='${filter==""}'>class="active"</c:if>><a href='/user/${user.username}/orders'>全部</a></li>
	</ul>
	<ul class="nav navbar-nav">
		<li <c:if test='${filter=="已提交"}'>class="active"</c:if>><a href='/user/${user.username}/orders已提交'>已提交</a></li>
	</ul>
	<ul class="nav navbar-nav">
		<li <c:if test='${filter=="已确认"}'>class="active"</c:if>><a href='/user/${user.username}/orders已确认'>已确认</a></li>
	</ul>
	<ul class="nav navbar-nav">
		<li <c:if test='${filter=="已拒绝"}'>class="active"</c:if>><a href='/user/${user.username}/orders已拒绝'>已拒绝</a></li>
	</ul>
</nav>

<div id="top_order" class="tab-pane">
	<c:if test="${empty orderList}">
		没有符合条件的订单.
	</c:if>
	<c:if test="${not empty orderList}">
		<table class="table">
			<tr>
				<th><h4 style="color: #2e6da4;">订单号</h4></th>
				<th><h4 style="color: #2e6da4;">下单时间</h4></th>
				<th><h4 style="color: #2e6da4;">收货人</h4></th>
				<th><h4 style="color: #2e6da4;">电话</h4></th>
				<th><h4 style="color: #2e6da4;">地址</h4></th>
				<th><h4 style="color: #2e6da4;">总价</h4></th>
				<th><h4 style="color: #2e6da4;">状态</h4></th>
			</tr>
			<c:forEach var="order" items="${orderList}">
				<c:set var='itemList' value="itemList_in_order_${order.orderId}" scope="page"/>
				<tr style="background-color: #ecf8f9">
					<th><fmt:formatNumber value="${order.orderId}" pattern="0000000000000"/></th>
					<th><fmt:formatDate value="${order.time}" pattern="yyyy年MM月dd日 HH:mm"/></th>
					<td>${order.receiver}</td>
					<td>${order.telephone}</td>
					<td>${order.address}</td>
					<th>${order.totalPrice}</th>
					<th>${order.status}</th>
				</tr>
				<tr>
					<td></td>
					<td colspan="6">
						<table class="table">
							<tr>
								<th>商品</th>
								<th>单价</th>
								<th>数量</th>
								<th>总价</th>
							</tr>
							<c:forEach var="item" items='${requestScope[itemList]}'>
								<tr>
									<td><a href="/product/${item.productId}">${item.productName}</a></td>
									<td>${item.price}</td>
									<td>${item.amount}</td>
									<td>${item.price * item.amount}</td>
								</tr>
							</c:forEach>
						</table>
					</td>
				</tr>
			</c:forEach>
		</table>
	</c:if>
</div>
</body>
</html>