package com.estore.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ModelAndView exception(Exception e) {
		Map<String, String> map = new HashMap<String, String>();

		map.put("stat", "ERROR");
		map.put("err", "上传文件大小超过限制.");
		map.put("acc", "");
		map.put("goto", "");

		return new ModelAndView(new MappingJackson2JsonView(), map);
	}
}