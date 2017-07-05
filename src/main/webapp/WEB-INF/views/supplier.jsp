<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="tagform" uri="http://www.springframework.org/tags/form" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
	<title>供应商</title>
	<link rel="stylesheet" href="<c:url value="/js/lib/bootstrap/css/bootstrap.min.css"/>">
	<script type="text/javascript" src="<c:url value="/js/lib/jquery-3.2.1.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/lib/bootstrap/js/bootstrap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/user.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/supplier.js"/>"></script>
</head>
<body>

<ul class="nav nav-tabs">
	<li role="presentation"><a href="#top_setting" data-toggle="tab">供应商-supplier</a></li>
	<li role="presentation" class="active"><a href="#top_product" data-toggle="tab">产品</a></li>
	<li role="presentation"><a href="#top_order" data-toggle="tab">订单</a></li>
	<li><a href="<c:url value="/logout"/>">退出</a></li>
</ul>

<div class="tab-content">
	<div id="top_product" class="tab-pane fade in active" align="center">
		<br/>
		<a class="btn btn-default" href="<c:url value="/user/supplier/newProduct"/>">添加新产品</a>
		<button type="button" class="btn btn-default" onclick="deleteSelectedProducts();">删除所选产品</button>
		<button type="button" class="btn btn-default" onclick="selectAllProducts();">全选</button>
		<button type="button" class="btn btn-default" onclick="revertSelectedProducts();">反选</button>

		<table class="table">
			<tr>
				<th></th>
				<th>ID</th>
				<th>名称</th>
				<th>价格</th>
				<th>剩余数量</th>
				<th>上架</th>
				<th>更新</th>
				<th>详细</th>
			</tr>
			<c:forEach var="product" items="${productList}">
				<tr>
					<td><input type="checkbox" id="ck_product_${product.productId}"
							   name="ck_product_${product.productId}" title=""/></td>
					<td><input class="form-control" id="productId_${product.productId}" value="${product.productId}"
							   readonly title=""/></td>
					<td><input class="form-control" id="name_${product.productId}" value="${product.name}" title=""/>
					</td>
					<td><input class="form-control" id="price_${product.productId}" value="${product.price}" title=""/>
					</td>
					<td><input class="form-control" id="total_${product.productId}" value="${product.total}" title=""/>
					</td>
					<td>
						<c:choose>
							<c:when test="${product.onSale==true}">
								<input type="checkbox" id="on_sale_${product.productId}" value="${product.onSale}"
									   checked="checked" title=""/>
							</c:when>
							<c:otherwise>
								<input type="checkbox" id="on_sale_${product.productId}" value="${product.onSale}"
									   title=""/>
							</c:otherwise>
						</c:choose>
					</td>
					<td><input type="button" class="btn btn-default" onclick="updateProduct(${product.productId})"
							   value="update"></td>
					<td>
						<li role="presentation" class="input-group"><a
								href="/user/supplier/product/${product.productId}">详细信息</a></li>
					</td>
					<td><input id="description_${product.productId}" value="${product.description}" hidden title="">
					</td>
				</tr>
			</c:forEach>
		</table>
		<br/>
	</div>
	<div id="top_order" class="tab-pane fade">
		<h3>未处理订单</h3>
		<!--unprocessed first-->
		<c:if test="${empty unprocessed_orders}">
			没有未处理的订单
		</c:if>
		<c:if test="${not empty unprocessed_orders}">
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
				<c:forEach var="order" items="${unprocessed_orders}">
					<c:set var='itemList' value="itemList_in_order_${order.orderId}" scope="page"/>
					<tr style="background-color: #ecf8f9">
						<th><fmt:formatNumber value="${order.orderId}" pattern="0000000000000"/></th>
						<th><fmt:formatDate value="${order.time}" pattern="yyyy年MM月dd日 HH:mm"/></th>
						<td>${order.receiver}</td>
						<td>${order.telephone}</td>
						<td>${order.address}</td>
						<th>${order.totalPrice}</th>
						<th>
							<button><span class="glyphicon glyphicon-ok"
										  onclick='processOrder("${order.orderId}","confirm")'></span></button>
							<button class="glyphicon glyphicon-remove"
									onclick='processOrder("${order.orderId}","reject")'></button>
						</th>
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
										<td>${item.productName}</td>
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

		<h3>已处理订单</h3>
		<!--processed-->
		<c:if test="${empty processed_orders}">
			没有已处理的订单
		</c:if>
		<c:if test="${not empty processed_orders}">
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
				<c:forEach var="order" items="${processed_orders}">
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
										<td>${item.productName}</td>
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
	<div id="top_setting" class="tab-pane fade" align="center">
		<br/>
		<table class="table">
			<tr>
				<td align="right">旧密码:</td>
				<td><input class="form-control" type="password" id="old_password" autocomplete="off" title=""/></td>
			</tr>
			<tr>
				<td align="right">新密码:</td>
				<td><input class="form-control" type="password" id="new_password" autocomplete="off" title=""/></td>
			</tr>
			<tr>
				<td align="right">重输密码:</td>
				<td><input class="form-control" type="password" id="new_password2" autocomplete="off" title=""/></td>
			</tr>
			<tr>
				<td align="right"></td>
				<td><input type="button" class="btn btn-default" id="edit_pwd" value="更改密码"
						   onclick='editPassword("supplier")'/>
				</td>
			</tr>
		</table>
	</div>
</div>


</body>
</html>