<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
	<title>${product.name}</title>
	<link rel="stylesheet" type="text/css" href="/css/default.css">
	<link rel="stylesheet" href="/js/bootstrap/css/bootstrap.min.css">
	<script src="/js/jquery-3.2.1.min.js"></script>
	<script src="/js/supplier.js"></script>
	<script src="/js/picture_submit.js"></script>
</head>
<body style="padding-top: 56px;">

<nav class="navbar navbar-default navbar-fixed-top">
	<div class="navbar-header">
		<a href="/user/supplier/" class="btn btn-link">返回</a>
		<a class="btn btn-lg" onclick="updateProduct(${product.productId})">
			<c:choose>
				<c:when test="${action=='add'}">添加</c:when>
				<c:when test="${action=='update'}">更新</c:when>
				<c:otherwise></c:otherwise>
			</c:choose> ${product.name} 产品信息
		</a>
		<c:if test="${action=='update'}">
			<a class="btn btn-lg" href="/user/supplier/newProduct">添加更多产品</a>
		</c:if>
	</div>
</nav>

<div align="center">
	<table class="table" style="vertical-align: middle">
		<tr>
			<td align="right" style="vertical-align: middle">预览图</td>
			<td style="width: 256px;">
				<form class="form-group" id="PictureSubmitForm" method="POST" enctype="multipart/form-data">
					<div id class="thumbnail" style="max-width: 256px; max-height: 256px;">
						<label>
							<img id="showUpPic" src="${product.picPath}" style="max-width: 244px; max-height: 244px;"
								 onerror="this.src='/upload/blank.jpg'">
							<input id="picture" type="file" name="file" accept="image/*"
								   onchange="submitPicture('/user/supplier/product/'+${product.productId});"
								   class="hidden"/>
						</label>
					</div>
				</form>
			</td>
		</tr>
		</tr>
		<tr>
			<td align="right" style="vertical-align: middle">名称</td>
			<td>
				<input class="form-control" id="name_${product.productId}" value="${product.name}"
					   style="width: 384px;"/>
			</td>
		</tr>
		<tr>
			<td align="right" style="vertical-align: middle">价格</td>
			<td>
				<input class="form-control" id="price_${product.productId}" value="${product.price}"
					   style="width: 384px;"/>
			</td>
		</tr>
		<tr>
			<td align="right" style="vertical-align: middle">剩余量</td>
			<td>
				<input class="form-control" id="total_${product.productId}" value="${product.total}"
					   style="width: 384px;"/>
			</td>
		</tr>
		<tr>
			<td align="right" style="vertical-align: middle">是否上架</td>
			<td>
				<c:choose>
					<c:when test="${product.onSale==true}">
						<input type="checkbox" id="on_sale_${product.productId}" value="${product.onSale}"
							   checked="checked"/>
					</c:when>
					<c:otherwise>
						<input type="checkbox" id="on_sale_${product.productId}" value="${product.onSale}"/>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
		<tr>
			<td align="right" style="vertical-align: middle">描述</td>
			<td>
				<textarea class="form-control" id="description_${product.productId}"
						  style="width: 384px; height: 256px; text-align: left">${product.description}</textarea>
			</td>
		</tr>
	</table>
</div>
</body>
</html>