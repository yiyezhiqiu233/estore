<%@ taglib uri="http://www.springframework.org/tags/form" prefix="tagform" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="tag" %>
<%@ page session="false" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<link rel="stylesheet" type="text/css" href="/css/default.css">
<link rel="stylesheet" type="text/css" href="/js/bootstrap/css/bootstrap.min.css">
<head>
	<title>登录</title>
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
