package com.estore.utils;

import org.springframework.util.DigestUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CommonFileUtil {
	public static String fileMd5(String filePath) {
		InputStream in = null;
		try {
			in = new FileInputStream(filePath);
			return DigestUtils.md5DigestAsHex(in); // Spring 自带的 MD5 工具
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}
}