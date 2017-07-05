function updateProduct(id) {
	var product = {};

	product['productId'] = id;
	product['name'] = document.getElementById("name_" + id.toString()).value;
	product['price'] = document.getElementById("price_" + id.toString()).value;
	product['total'] = document.getElementById("total_" + id.toString()).value;
	product['onSale'] = document.getElementById("on_sale_" + id.toString()).checked;
	product['description'] = document.getElementById("description_" + id.toString()).value;

	$.ajax({
		async: false,
		url: '/user/supplier/updateProduct',
		type: 'POST',
		data: JSON.stringify(product),
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: function (msg) {
			alert(msg.err + msg.acc);
			if (msg.stat === 'GOTO') {
				window.location.href = msg.goto;
			} else {
				location.reload(true);
			}
		}
	});
}

function deleteSelectedProducts() {
	var id_list = [];

	var inputs = document.getElementsByTagName("input");
	for (var i = 0; i < inputs.length; i++) {
		if (inputs[i].type === 'checkbox' && !inputs[i].disabled
			&& (inputs[i].id.indexOf('ck_product') >= 0) && inputs[i].checked) {
			id_list.push(parseInt(inputs[i].id.substr(11)));
		}
	}
	if (id_list.length < 1) {
		alert('未选中任何产品.');
		return;
	}

	if (!window.confirm("确认删除下列产品吗? id=[" + id_list.toString() + "]")) {
		return;
	}
	$.ajax({
		async: false,
		url: '/user/supplier/deleteSelected',
		type: 'POST',
		data: JSON.stringify(id_list),
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: function (result) {
			alert(result.acc + result.err);
			location.reload(true);
		}
	});
}

function selectAllProducts() {
	var inputs = document.getElementsByTagName("input");
	for (var i = 0; i < inputs.length; i++) {
		if (inputs[i].type === 'checkbox' && !inputs[i].disabled
			&& (inputs[i].id.indexOf('ck_product') >= 0)) {
			inputs[i].checked = true;
		}
	}
}

function revertSelectedProducts() {
	var inputs = document.getElementsByTagName("input");
	for (var i = 0; i < inputs.length; i++) {
		if (inputs[i].type === 'checkbox' && !inputs[i].disabled
			&& (inputs[i].id.indexOf('ck_product') >= 0)) {
			inputs[i].checked = !inputs[i].checked;
		}
	}
}

function processOrder(orderId, op) {
	var inputs = {};
	inputs['orderId'] = orderId;
	inputs['operation'] = op;

	$.ajax({
		async: false,
		url: '/user/supplier/processOrder',
		type: 'POST',
		data: JSON.stringify(inputs),
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: function (msg) {
			alert(msg.err + msg.acc);
			location.reload(true);
		}
	});
}