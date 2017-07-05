function submitPicture(_url) {
	form = document.getElementById("PictureSubmitForm");
	var formData = new FormData(form);
	$.ajax(
		{
			async: false,
			url: _url,
			type: "POST",
			data: formData,
			mimeType: "multipart/form-data",
			dataType:'json',
			contentType: false,
			cache: false,
			processData: false,
			success: function (result) {
				if(result.err!="")
					alert(result.err);
				if (result.stat == 'GOTO') {
					window.location.href = result.goto;
				} else {
					location.reload(true);
				}
			},
			error: function (result) {
				alert(result.err);
				result = $.parseJSON(result);
				alert(result.err + result.acc);
				alert('ERR.');
			}
		});
}


function showUploadPicture() {
	var r = new FileReader();
	f = document.getElementById('picture').files[0];
	r.readAsDataURL(f);
	r.onload = function (e) {
		document.getElementById('showUpPic').src = this.result;
	};
}