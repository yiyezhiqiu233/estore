
function plus2(e, total, price) {
	var i = new Number(e.value) + 1;
	e.value = i;
	total.innerText = (new Number(e.value) * new Number(price)).toFixed(1);
	calcTotalPrice()
}

function minus2(e, total, price) {
	var i = new Number(e.value) - 1;
	if (i < 1) {
		e.value = 1;
	} else {
		e.value = i;
	}
	total.innerText = (new Number(e.value) * new Number(price)).toFixed(1);
	calcTotalPrice()
}

function calcTotalPrice() {
	var amounts = document.getElementsByName("total_prices");
	var total_price = new Number(0);
	for (var i = 0; i < amounts.length; i++) {
		total_price +=
			new Number(amounts[i].innerText);
	}
	document.getElementById('totalPrice').innerText = total_price.toFixed(2);
}

function submitOrder() {
	var inputs = {};

	inputs['receiver'] = document.getElementById('receiver').value;
	inputs['address'] = document.getElementById('new_address').value;
	inputs['telephone'] = document.getElementById('telephone').value;
	inputs['password'] = document.getElementById('password').value;

	//update amounts
	_pro_amount={};
	for (var i = 0; i < _products.length; i++) {
		var id = _products[i]
		var amount = document.getElementById('amount_' + id)
		amount=amount.value
		_pro_amount[id]=amount
	}
	inputs['products'] = _pro_amount

	$.ajax({
		async: false,
		url: '/buy',
		type: 'POST',
		data: JSON.stringify(inputs),
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: function (msg) {
			alert(msg.err + msg.acc);
			if (msg.stat == 'GOTO') {
				window.location.href = msg.goto;
			}
		}
	});
}

function removeProductFromTable(row, id) {
	for(var i=0;i<_products.length;i++){
		if(_products[i]==id){
			for(var j=i;j<_products.length-1;j++)
				_products[j]=_products[j+1];
			break;
		}
	}
	_products.length-=1;
	var i = row.parentNode.parentNode.rowIndex;
	document.getElementById("table_products").deleteRow(i);
	calcTotalPrice();
}


function checkPhoneNum(e) {
	e.value = e.value.replace(/\D/g, '');
	var i = new Number(e.value.substr(0, 11));
	e.value = i;
}