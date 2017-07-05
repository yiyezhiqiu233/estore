package com.estore.utils;

import java.security.MessageDigest;
import java.util.Random;

public class PasswordEncoder {
	private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	private static final String random_base = "~`!@#$%^&**()-_=+[]{};:\'\",./<>?ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	private static final String algorithm = "SHA-256";

	public static String encodePassword(String password, String salt) {
		try {
			//SHA256(pswd)
			MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
			messageDigest.update(password.getBytes());
			String hash1 = getFormattedText(messageDigest.digest());
			hash1 += salt;
			messageDigest = MessageDigest.getInstance(algorithm);
			messageDigest.update(hash1.getBytes());
			return getFormattedText(messageDigest.digest());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String getFormattedText(byte[] bytes) {
		int len = bytes.length;
		StringBuilder buf = new StringBuilder(len * 2);
		// 把密文转换成十六进制的字符串形式
		for (byte aByte : bytes) {
			buf.append(HEX_DIGITS[(aByte >> 4) & 0x0f]);
			buf.append(HEX_DIGITS[aByte & 0x0f]);
		}
		return buf.toString();
	}

	public static String getRandomString(int length) {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(random_base.length());
			sb.append(random_base.charAt(number));
		}
		return sb.toString();
	}
}
