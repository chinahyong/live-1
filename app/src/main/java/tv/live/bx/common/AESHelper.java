package tv.live.bx.common;

import android.text.TextUtils;
import android.util.Base64;

import tv.live.bx.library.util.EvtLog;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Live on 2017/2/15.
 * AES 加密 /解密帮助类
 */

public class AESHelper {
	// 加密固定字符串：TXEIdN + 时间戳
	public static String mDate = String.valueOf(System.currentTimeMillis());
	public static String key = "V4ujGjsNUl6RvgjvgD6m91";    //System.currentTimeMillis() / 1000 + "V4ujGjsNUl6RvgjvgD6m91"
	private static final String IV = "0000000000000000";
	private static final String CBC_PKCS7_PADDING = "AES/CBC/PKCS7Padding";//AES是加密方式 CBC是工作模式 PKCS7Padding是填充模式

	/**
	 * 创建密钥
	 **/
	private static SecretKeySpec getRawKey(String key) {
		byte[] data = null;
		StringBuffer sb = new StringBuffer(32);
		sb.append(key);
		while (sb.length() < 32) {
			sb.append("0");
		}
		if (sb.length() > 32) {
			sb.setLength(32);
		}
		try {
			data = sb.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new SecretKeySpec(data, "AES");
	}


	private static IvParameterSpec createIV(String password) {
		byte[] data = null;
		if (password == null) {
			password = "";
		}
		StringBuffer sb = new StringBuffer(16);
		sb.append(password);
		while (sb.length() < 16) {
			sb.append("0");
		}
		if (sb.length() > 16) {
			sb.setLength(16);
		}


		try {
			data = sb.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new IvParameterSpec(data);
	}

	/*
	 * 加密
     */
	public static String encrypt(String cleartext, String date) {
		mDate = date;
		if (TextUtils.isEmpty(cleartext)) {
			return cleartext;
		}
		try {
			byte[] result = encrypt(cleartext.getBytes());
			//Base64Encoder.encode(result)
			String data = Base64.encodeToString(result, Base64.NO_WRAP);
			EvtLog.e("AES", "key:" + key + "  encrypt result:" + data);
			EvtLog.e("AES", "key:" + key + "  encrypt result:" + Base64.encodeToString(result, Base64.DEFAULT));
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	* 加密
	*/
	private static byte[] encrypt(byte[] clear) throws Exception {
		SecretKeySpec skeySpec = getRawKey(mDate + key);
		Cipher cipher = Cipher.getInstance(CBC_PKCS7_PADDING);
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, createIV(IV));
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	/*
	 * 解密
     */
	public static String decrypt(String key, String encrypted) {
		if (TextUtils.isEmpty(encrypted)) {
			return encrypted;
		}
		try {
//			byte[] enc = Base64Decoder.decodeToBytes(encrypted);
			byte[] enc = Base64.decode(encrypted, Base64.NO_WRAP);
			byte[] result = decrypt(key, enc);
			EvtLog.e("AES", "key:" + key + "  decrypt result:" + new String(result));
			return new String(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * 解密
	 */
	private static byte[] decrypt(String key, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = getRawKey(key);
		Cipher cipher = Cipher.getInstance(CBC_PKCS7_PADDING);
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, createIV(IV));
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}
}
