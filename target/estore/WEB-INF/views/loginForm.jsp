<%@ taglib uri="http://www.springframework.org/tags/form" prefix="tagform" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="tag" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page session="false" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
	<title>登录</title>
	<link rel="stylesheet" href="<c:url value="/js/lib/bootstrap/css/bootstrap.min.css"/>">
	<script type="text/javascript" src="<c:url value="/js/lib/jquery-3.2.1.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/lib/bootstrap/js/bootstrap.min.js"/>"></script>
</head>
<body align="center">
<br/>
<h1>登录</h1>
<br/>
<div style="margin-left: auto;margin-right: auto;max-width: 384px;">
	<tagform:form method="POST" modelAttribute="user" action="/login" cssClass="form-group" id="login_form">
		<tagform:errors path="*" element="div" cssClass="alert alert-warning"/>
		<div class="input-group">
			<span class="input-group-addon">用户名:</span>
			<tagform:input cssClass="form-control" path="username" autocomplete="off"/>
		</div>

		<div class="input-group">
			<span class="input-group-addon">密码:</span>
			<tagform:input cssClass="form-control" path="password" type="password" autocomplete="off"/>
		</div>
		<input type="submit" class="form-control" value="登录">
	</tagform:form>
</div>
</body>
</html>
