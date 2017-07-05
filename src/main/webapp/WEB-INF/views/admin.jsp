<%@ taglib prefix="tagform" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
	<title>管理员</title>
	<link rel="stylesheet" href="<c:url value="/js/lib/bootstrap/css/bootstrap.min.css"/>">
	<script type="text/javascript" src="<c:url value="/js/lib/jquery-3.2.1.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/lib/bootstrap/js/bootstrap.min.js"/>"></script>
	<script type="text/javascript" src="<c:url value="/js/admin.js"/>"></script>
</head>
<body style="text-align: center">
<a class="navbar navbar-nav" href="<c:url value="/logout"/>">退出登录</a>
<h1 align="center">管理员-admin</h1>
<br/>

<div class="btn-group" role="group">
	<button class="btn btn-default" onclick="deleteSelected()">批量删除</button>
	<button class="btn btn-default" onclick="selectAll()">全选</button>
	<button class="btn btn-default" onclick="revertSelected()">反选</button>
</div>

<table class="table">
	<tr>
		<th></th>
		<th>ID</th>
		<th>类型</th>
		<th>用户名</th>
		<th>新密码</th>
		<th>重输</th>
		<th>extra</th>
		<th></th>
	</tr>

	<c:forEach var="m_user" items="${userList}">
		<tr align="center">
			<form method="post" autocomplete="off" class="form-group">
				<c:choose>
					<c:when test="${m_user.userType=='ADMIN'}">
						<td>
							<label for="ck_${m_user.userId}"></label><input type="checkbox" id="ck_${m_user.userId}"
																			name="ck_${m_user.userId}"
																			class="form-control" disabled="disabled"/>
						</td>
						<td><input class="form-control-static" style="border: none" name="m_userId"
								   value="${m_user.userId}" readonly title=""/></td>
						<td>管理员</td>
						<td><input class="form-control" style="text-align: center;" name="m_username"
								   value="${m_user.username}" readonly title=""/>
						</td>
					</c:when>
					<c:when test="${m_user.userType=='SUPPLIER'}">
						<td>
							<input type="checkbox" id="ck_${m_user.userId}" name="ck_${m_user.userId}"
								   class="form-control" disabled="disabled"/>
						</td>
						<td><input class="form-control-static" style="border: none" name="m_userId"
								   value="${m_user.userId}" readonly title=""/></td>
						<td>供应商</td>
						<td><input class="form-control" style="text-align: center;" name="m_username"
								   value="${m_user.username}" readonly title=""/>
						</td>
					</c:when>
					<c:otherwise>
						<td>
							<input type="checkbox" id="ck_${m_user.userId}"
								   name="ck_${m_user.userId}"/>
						</td>
						<td><input class="form-control-static" style="border: none" name="m_userId"
								   value="${m_user.userId}" readonly title=""/></td>
						<td>用户</td>
						<td><input class="form-control" style="text-align: center;" name="m_username"
								   value="${m_user.username}" title=""/></td>
					</c:otherwise>
				</c:choose>
				<td><input class="form-control" name="m_password1" type="password" autocomplete="off" title=""/></td>
				<td><input class="form-control" name="m_password2" type="password" autocomplete="off" title=""/></td>

				<td><input class="btn btn-default" type="submit" value="update"></td>
				<td>
					<c:if test="${m_user_id==m_user.userId}">
						<c:if test="${err!=''}">
							<c:out value="${err}"/>
						</c:if>
						<c:if test="${acc!=''}">
							<c:out value="${acc}"/>
						</c:if>
					</c:if>
				</td>
			</form>
		</tr>
	</c:forEach>


	<tr>
		<td colspan="3" align="right">添加用户</td>
		<td><input class="form-control" style="text-align: center;" id="new_username" autocomplete="off" title=""/></td>
		<td><input class="form-control" style="text-align: center;" type="password" id="new_password1"
				   autocomplete="off" title=""/></td>
		<td><input class="form-control" style="text-align: center;" type="password" id="new_password2"
				   autocomplete="off" title=""/></td>
		<td>
			<input type="button" class="btn btn-default" onclick="addNewUser()" value="添加用户"/>
		</td>
		<td></td>
	</tr>
</table>


</body>
</html>