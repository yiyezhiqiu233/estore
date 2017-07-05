function addNewUser() {
	var inputs = {};

	var in_username = document.getElementById("new_username");
	var n_username = in_username.value;
	var in_password = document.getElementById("new_password1");
	var n_password = in_password.value;
	var in_password2 = document.getElementById("new_password2");
	var n_password2 = in_password2.value;

	inputs['username']=n_username;
	inputs['password']=n_password;
	inputs['password2']=n_password2;

	$.ajax({
		async: false,
		url: 'admin/addNewUser',
		type: 'POST',
		data: JSON.stringify(inputs),
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: function (result) {
			//没有error则return
			if (result.err == "")
				return;
			alert(result.err);
		}
	});
	location.reload(true);
}

function deleteSelected() {
	var id_list = new Array();

	var inputs = document.getElementsByTagName("input");
	for (var i = 0; i < inputs.length; i++) {
		if (inputs[i].type == 'checkbox' && !inputs[i].disabled) {
			if (inputs[i].checked) {
				id_list.push(parseInt(inputs[i].id.substr(3)));
			}
		}
	}
	if (id_list.length < 1) {
		alert('未选中任何用户.');
		return;
	}

	if (!window.confirm("确认删除下列用户吗? id=[" + id_list.toString() + "]")) {
		return;
	}
	$.ajax({
		async: false,
		url: 'admin/deleteSelected',
		type: 'POST',
		data: JSON.stringify(id_list),
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: function (result) {
			alert(result.acc + result.err);
		}
	});
	location.reload(true);
}

function selectAll() {
	var inputs = document.getElementsByTagName("input");
	for (var i = 0; i < inputs.length; i++) {
		if (inputs[i].type == 'checkbox' && !inputs[i].disabled) {
			inputs[i].checked = true;
		}
	}
}

function revertSelected() {
	var inputs = document.getElementsByTagName("input");
	for (var i = 0; i < inputs.length; i++) {
		if (inputs[i].type == 'checkbox' && !inputs[i].disabled) {
			inputs[i].checked = !inputs[i].checked;
		}
	}
}