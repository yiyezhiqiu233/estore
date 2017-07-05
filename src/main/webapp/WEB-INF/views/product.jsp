<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="C" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
	<title>${product.name}</title>
	<link rel="stylesheet" href="<c:url value="/js/lib/bootstrap/css/bootstrap.min.css"/>">
	<script type="text/javascript" src="<c:url value="/js/lib/jquery-3.2.1.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/lib/bootstrap/js/bootstrap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/product.js"/>"></script>
</head>
<body onload="max_amount=${product.total};" style="text-align: center">

<ul class="nav nav-tabs">
	<li role="presentation"><a href="/user/${user.username}/profile">${user.username}</a></li>
	<li role="presentation"><a href="/user/${user.username}">商场</a></li>
	<li role="presentation"><a href="/user/${user.username}/orders">订单</a></li>
	<li><a href="<c:url value="/logout"/>">退出</a></li>

	<li role="presentation" class="dropdown">
		<a class="dropdown-toggle" data-toggle="dropdown" href="#" role="button" aria-haspopup="true"
		   aria-expanded="false">
			购物车<span class="caret"></span>
		</a>
		<ul class="dropdown-menu" id="cart">
			<c:forEach var="p" items="${cart.productHashMap}">
				<div class="btn-group btn-group-justified">
					<a class="input-group-addon" href="/product/${p.key.productId}">${p.key.name}
						×${p.value.intValue()}</a>
					<a class="input-group-addon" onclick="removeProductFromCart(${p.key.productId})">删除</a>
				</div>
			</c:forEach>
			<div class="btn-group-justified">
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

<input id="productId" value="${product.productId}" hidden title="">
<c:if test="${product.onSale=='false'}">
	<div style="height: 32px">
		<label class="alert alert-danger">该商品暂时下架了！</label>
	</div>
</c:if>

<table style="vertical-align: middle">
	<tr>
		<td style="height: 512px;width:512px;">
			<div id class="thumbnail" style="max-width: 508px; max-height: 508px;">
				<img src="${product.picPath}" onerror="this.src='/upload/blank.jpg'"
					 style="max-width: 500px;max-height: 500px;"/>
			</div>
		</td>
		<td style="vertical-align: top;padding-left: 24px;padding-top: 24px;">
			<div><h2>${product.name}</h2></div>
			<br/><br/>
			<div>
				<!--price-->
				<span class="label label-warning" style="font-size: x-large;"><small
						style="color: black">价格</small> ￥${product.price}</span><br/><br/>
				<!--total-->
				<span class="label" style="font-size: x-large;color: #122b40;"><small
						style="color: black">剩余</small>  ${product.total}件</span><br/><br/>
				<br/>
				<!--buy many-->
				<div class="input-group">
							<span class="input-group-btn"><button class="btn btn-default" type="button"
																  onclick="minus()">-</button></span>
					<input type="text" id="amount" class="form-control" value="0" onkeyup="checkNum();"
						   title=""/>
					<span class="input-group-btn"><button class="btn btn-default" type="button"
														  onclick="plus()">+</button></span>

					<!--buy this/add this-->
					<span class="input-group-btn">
								<button class="form-control" style="background-color: #f0ad4e"
										onclick="addProductToCartAndBuyNow()">+立即购买</button>
							</span>
					<span class="input-group-btn">
								<button class="form-control" style="background-color: #f0ad4e"
										onclick="addProductToCart()">加入购物车</button>
							</span>
				</div>
			</div>
			<br/><br/>
			<div><span class="label label-default">商品描述</span><br/><br/>
				${product.description}
			</div>
		</td>
	</tr>
</table>

</body>
</html>