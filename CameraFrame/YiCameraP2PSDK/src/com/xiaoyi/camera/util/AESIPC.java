package com.xiaoyi.camera.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AESIPC {

	public static void main(String[] args) {

		// String content = "EVN9UYNEJ2AR9N3U";
		// String password = "EVN9UYNEJ2AR9N3U";
		String content = "1234567890123456";
		String password = "1234567890123456";
		// byte[] encryptResult = encrypt(content, password);//加密
		byte[] encryptResult = encrypt(content, password);// 加密
		byte[] decryptResult = decrypt(encryptResult, password);// 解密
		System.out.println("解密后：" + new String(decryptResult));

		/* 容易出错的地方，请看下面代码 */
		System.out.println("***********************************************");
		try {
			String encryptResultStr = byte2hex(encryptResult);

			System.out.println("============：" + encryptResultStr);
			// byte[] ss = AES.hex2byte("aeef1e3f6b6367c49b3864e983242c75");
			// byte[] decryptResult = decrypt(ss,password);
			// System.out.println("解密后：" + new String(decryptResult));

		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		 * 则，系统会报出如下异常：javax.crypto.IllegalBlockSizeException: Input length must
		 * be multiple of 16 when decrypting with padded cipher at
		 * com.sun.crypto.provider.SunJCE_f.b(DashoA13*..) at
		 * com.sun.crypto.provider.SunJCE_f.b(DashoA13*..) at
		 * com.sun.crypto.provider.AESCipher.engineDoFinal(DashoA13*..) at
		 * javax.crypto.Cipher.doFinal(DashoA13*..) at
		 * cn.com.songjy.test.ASCHelper.decrypt(ASCHelper.java:131) at
		 * cn.com.songjy.test.ASCHelper.main(ASCHelper.java:58)
		 */
		/*
		 * 这主要是因为加密后的byte数组是不能强制转换成字符串的, 换言之,字符串和byte数组在这种情况下不是互逆的,
		 * 要避免这种情况，我们需要做一些修订，可以考虑将二进制数据转换成十六进制表示,
		 * 主要有两个方法:将二进制转换成16进制(见方法parseByte2HexStr
		 * )或是将16进制转换为二进制(见方法parseHexStr2Byte)
		 */

		/* 然后，我们再修订以上测试代码 */
		// System.out.println("***********************************************");
		// String encryptResultStr = parseByte2HexStr(encryptResult);
		// System.out.println("加密后：" + encryptResultStr);
		// byte[] decryptFrom = parseHexStr2Byte(encryptResultStr);
		// decryptResult = decrypt(decryptFrom,password);//解码
		// System.out.println("解密后：" + new String(decryptResult));
	}

	/**
	 * 另外一种加密方式--这种加密方式有两种限制 1、密钥必须是16位的 2、待加密内容的长度必须是16的倍数，如果不是16的倍数，就会出如下异常
	 * javax.crypto.IllegalBlockSizeException: Input length not multiple of 16
	 * bytes at com.sun.crypto.provider.SunJCE_f.a(DashoA13*..) at
	 * com.sun.crypto.provider.SunJCE_f.b(DashoA13*..) at
	 * com.sun.crypto.provider.SunJCE_f.b(DashoA13*..) at
	 * com.sun.crypto.provider.AESCipher.engineDoFinal(DashoA13*..) at
	 * javax.crypto.Cipher.doFinal(DashoA13*..) 要解决如上异常，可以通过补全传入加密内容等方式进行避免。
	 * 
	 * @method encrypt2
	 * @param content
	 *            需要加密的内容
	 * @param password
	 *            加密密码
	 * @return
	 * @throws
	 * @since v1.0
	 */
	public static byte[] encrypt(String content, String password) {
		try {
			if (cp == null || !password.equals(cp.password)) {
				cp = getCipher(password);
			}
			Cipher cipher = cp.cipher;
			byte[] byteContent = content.getBytes("ASCII");
			byte[] result = cipher.doFinal(byteContent);
			return result; // 加密
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static CipherPair getCipher(String password) {
		Cipher cipher = null;
		try {
			byte[] raw = password.getBytes("ASCII");
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			cipher = Cipher.getInstance("AES/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new CipherPair(password, cipher);
	}

	static class CipherPair {
		public String password;
		public Cipher cipher;

		public CipherPair(String password, Cipher cipher) {
			super();
			this.password = password;
			this.cipher = cipher;
		}

	}

	private static CipherPair cp = null;

	/**
	 * 解密
	 * 
	 * @method decrypt
	 * @param content
	 *            待解密内容
	 * @param password
	 *            解密密钥
	 * @return
	 * @throws
	 * @since v1.0
	 */
	public static byte[] decrypt(byte[] content, String password) {
		try {
			if (cp == null || !password.equals(cp.password)) {
				cp = getCipher(password);
			}
			Cipher cipher = cp.cipher;
			byte[] original = cipher.doFinal(content);
			return original;
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// fail, return copy of content
		if(content != null){
			byte[] copy = new byte[content.length];
			System.arraycopy(content, 0, copy, 0, content.length);
			return copy;
		}else{
			return null;
		}

	}

	/**
	 * 将二进制转换成16进制
	 * 
	 * @method parseByte2HexStr
	 * @param buf
	 * @return
	 * @throws
	 * @since v1.0
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 将16进制转换为二进制
	 * 
	 * @method parseHexStr2Byte
	 * @param hexStr
	 * @return
	 * @throws
	 * @since v1.0
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
					16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	/** 字节数组转成16进制字符串 **/
	public static String byte2hex(byte[] b) { // 一个字节的数，
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs;
	}
}
