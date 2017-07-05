function editPassword(username) {
	var inputs = {};

	var in_old_p = document.getElementById("old_password").value;
	var in_new_p = document.getElementById("new_password").value;
	var in_new_p2 = document.getElementById("new_password2").value;

	inputs['old_password']=in_old_p;
	inputs['new_password1']=in_new_p;
	inputs['new_password2']=in_new_p2;

	$.ajax({
		async: false,
		url: '/user/'+username+'/editPassword',
		type: 'POST',
		data: JSON.stringify(inputs),
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: function (result) {
			if ((result.err+result.acc) != "")
				alert(result.err+result.acc);
			if(result.goto!="")
				window.location.href=result.goto;
		}
	});
}

function updateInfo(username) {
	var inputs = {};

	var receiver = document.getElementById("receiver").value;
	var address = document.getElementById("address").value;
	var telephone = document.getElementById("telephone").value;

	inputs['receiver']=receiver;
	inputs['address']=address;
	inputs['telephone']=telephone;

	$.ajax({
		async: false,
		url: '/user/'+username+'/updateInfo',
		type: 'POST',
		data: JSON.stringify(inputs),
		dataType: 'json',
		contentType: 'application/json;charset=UTF-8',
		success: function (result) {
			if ((result.err+result.acc) != "")
				alert(result.err+result.acc);
			if(result.goto!="")
				window.location.href=result.goto;
		}
	});
}