var max_amount;

function buyProductDirectly() {
	amount = document.getElementById("amount").value;
	alert(amount);

	$.ajax({
		async: false,
		url: 'test',
		type: 'POST',
		data: JSON.stringify(amount),
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

function addProductToCart() {
	var idAndAmount = new Array();
	id = document.getElementById("productId").value;
	amount = document.getElementById("amount").value;

	idAndAmount.push(id);
	idAndAmount.push(amount);
	$.ajax({
		async: false,
		url: '/cart/addToCart',
		type: 'POST',
		data: JSON.stringify(idAndAmount),
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: function (result) {
			//没有error则reload
			if (result.err == "") {
				location.reload(true);
			}
			else {
				alert(result.err);
			}
		}
	});
}

function addProductToCartAndBuyNow() {
	var idAndAmount = new Array();
	id = document.getElementById("productId").value;
	amount = document.getElementById("amount").value;

	idAndAmount.push(id);
	idAndAmount.push(amount);
	$.ajax({
		async: false,
		url: '/cart/addToCart',
		type: 'POST',
		data: JSON.stringify(idAndAmount),
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: function (result) {
			//没有error则return
			if (result.err == "") {
				window.location.href = "/buy";
			}
			else {
				alert(result.err);
			}
		}
	});
}

function removeProductFromCart(id) {
	$.ajax({
		async: false,
		url: '/cart/removeFromCart',
		type: 'POST',
		dataType: 'json',
		data: JSON.stringify(id),
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

function clearCart() {
	$.ajax({
		async: false,
		url: '/cart/clearCart',
		type: 'POST',
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: function (msg) {
			if (msg.stat == 'GOTO') {
				window.location.href = msg.goto;
				return;
			}
			else if (msg.err == "")
				return;
			location.reload(true);
		}
	});
}

function trySubmitCart() {
	$.ajax({
		async: false,
		url: '/cart/submitCart',
		type: 'POST',
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: function (msg) {
			if (msg.stat == "GOTO") {
				window.location.href = msg.goto;
				return;
			}
			else if (msg.err == "")
				return;
			document.getElementById("message").style.display = 'block';
			document.getElementById("message").innerText = msg.err;
		}
	});
}

function checkNum() {
	e = document.getElementById("amount");
	e.value = e.value.replace(/\D/g, '');
	var i = new Number(e.value);
	if (i > max_amount) {
		e.value = max_amount;
	} else if (i < 0) {
		e.value = 0;
	} else {
		e.value = i;
	}
}

function plus() {
	e = document.getElementById("amount");
	var i = new Number(e.value) + 1;
	if (i > max_amount) {
		e.value = max_amount;
	} else {
		e.value = i;
	}
}
function minus() {
	e = document.getElementById("amount");
	var i = new Number(e.value) - 1;
	if (i < 0) {
		e.value = 0;
	} else {
		e.value = i;
	}
}