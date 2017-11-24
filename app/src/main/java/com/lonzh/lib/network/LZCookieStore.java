package com.lonzh.lib.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import tv.live.bx.common.Consts;
import tv.live.bx.library.util.EvtLog;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LZCookieStore implements CookieStore {
	private static final String COOKIE_SP_NAME = "cookie";
	private Context moContext;

	public LZCookieStore(Context poContext) {
		moContext = poContext;
	}

	@Override
	public void addCookie(Cookie poCookie) {
		if (poCookie instanceof BasicClientCookie) {
			EvtLog.e(COOKIE_SP_NAME, "addCookie:xxxxx" + poCookie.toString());
			((BasicClientCookie) poCookie).setDomain(Consts.BASE_DOMAIN);
		}
		EvtLog.e(COOKIE_SP_NAME, "addCookie:" + poCookie.toString());
		String lsName = poCookie.getName();
		SharedPreferences loCfg = moContext.getSharedPreferences(
				COOKIE_SP_NAME, 0);
		Editor loEditor = loCfg.edit();
		if (poCookie.isExpired(new Date()))
			loEditor.remove(lsName);
		else {
			loEditor.putString(lsName, encodeCookie(new SerilizableCookie(
					poCookie)));
		}
		loEditor.commit();
	}

	@Override
	public void clear() {
		EvtLog.e(COOKIE_SP_NAME, "clear:");
		SharedPreferences loCfg = moContext.getSharedPreferences(
				COOKIE_SP_NAME, 0);
		Editor loEditor = loCfg.edit();
		loEditor.clear();
		loEditor.commit();
	}

	@Override
	public boolean clearExpired(Date poDate) {
		EvtLog.e(COOKIE_SP_NAME, "clearExpired:" + poDate.toString());
		boolean lbClearAny = false;
		SharedPreferences loCfg = moContext.getSharedPreferences(
				COOKIE_SP_NAME, 0);
		Editor loEditor = loCfg.edit();
		Map<String, ?> lmCookies = loCfg.getAll();
		Iterator<String> loIterator = lmCookies.keySet().iterator();
		while (loIterator.hasNext()) {
			String lsCookieName = loIterator.next();
			String lsHexCookie = (String) lmCookies.get(lsCookieName);
			Cookie loCookie = decodeCookie(lsHexCookie);
			if (loCookie.isExpired(new Date())) {
				loEditor.remove(lsCookieName);
				lbClearAny = true;
			}
		}
		loEditor.commit();

		return lbClearAny;
	}

	@Override
	public List<Cookie> getCookies() {
		SharedPreferences loCfg = moContext.getSharedPreferences(
				COOKIE_SP_NAME, 0);
		Map<String, ?> lmCookies = loCfg.getAll();
		EvtLog.e(COOKIE_SP_NAME, "getCookies before:" + lmCookies.size());
		List<Cookie> llCookies = new ArrayList<>();
		Iterator<String> loIterator = lmCookies.keySet().iterator();
		while (loIterator.hasNext()) {
			String lsCookieName = loIterator.next();
			String lsHexCookie = (String) lmCookies.get(lsCookieName);
			Cookie loCookie = decodeCookie(lsHexCookie);
			EvtLog.e(COOKIE_SP_NAME, "getCookies: xxx cookie" + loCookie.toString());
			llCookies.add(loCookie);
		}
		return llCookies;
	}

	public String getCookie(String psKey) {
		EvtLog.e(COOKIE_SP_NAME, "getCookies psKey:" + psKey);
		SharedPreferences loCfg = moContext.getSharedPreferences(
				COOKIE_SP_NAME, 0);
		Map<String, ?> lmCookies = loCfg.getAll();
		Iterator<String> loIterator = lmCookies.keySet().iterator();
		while (loIterator.hasNext()) {
			String lsCookieName = loIterator.next();
			String lsHexCookie = (String) lmCookies.get(lsCookieName);
			Cookie loCookie = decodeCookie(lsHexCookie);
			if (loCookie.getName().equals(psKey))
				return loCookie.getValue();
		}
		return null;
	}

	public Cookie getCookieObj(String psKey) {
		EvtLog.e(COOKIE_SP_NAME, "getCookieObj psKey:" + psKey);
		SharedPreferences loCfg = moContext.getSharedPreferences(
				COOKIE_SP_NAME, 0);
		Map<String, ?> lmCookies = loCfg.getAll();
		Iterator<String> loIterator = lmCookies.keySet().iterator();
		while (loIterator.hasNext()) {
			String lsCookieName = loIterator.next();
			String lsHexCookie = (String) lmCookies.get(lsCookieName);
			Cookie loCookie = decodeCookie(lsHexCookie);
			if (loCookie.getName().equals(psKey))
				return loCookie;
		}
		return null;
	}

	/**
	 * Serializes Cookie object into String
	 *
	 * @param cookie cookie to be encoded, can be null
	 * @return cookie encoded as String
	 */
	protected String encodeCookie(SerilizableCookie cookie) {
		if (cookie == null)
			return null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(os);
			outputStream.writeObject(cookie);
		} catch (Exception e) {
			return null;
		}

		return byteArrayToHexString(os.toByteArray());
	}

	/**
	 * Returns cookie decoded from cookie string
	 *
	 * @param cookieString string of cookie as returned from http request
	 * @return decoded cookie or null if exception occured
	 */
	protected Cookie decodeCookie(String cookieString) {
		byte[] bytes = hexStringToByteArray(cookieString);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				bytes);
		Cookie cookie = null;
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(
					byteArrayInputStream);
			cookie = ((SerilizableCookie) objectInputStream.readObject())
					.getCookie();
		} catch (Exception exception) {
			Log.d("shit", "decodeCookie failed", exception);
		}

		return cookie;
	}

	/**
	 * Using some super basic byte array <-> hex conversions so we don't have to
	 * rely on any large Base64 libraries. Can be overridden if you like!
	 *
	 * @param bytes byte array to be converted
	 * @return string containing hex values
	 */
	protected String byteArrayToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte element : bytes) {
			int v = element & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase(Locale.US);
	}

	/**
	 * Converts hex values from strings to byte arra
	 *
	 * @param hexString string of hex-encoded values
	 * @return decoded byte array
	 */
	protected byte[] hexStringToByteArray(String hexString) {
		int len = hexString.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
					.digit(hexString.charAt(i + 1), 16));
		}
		return data;
	}

	class SerilizableCookie implements Serializable {
		private static final long serialVersionUID = -8545794043590851264L;

		private transient Cookie moCookie;
		private transient BasicClientCookie moClientCookie;

		public SerilizableCookie(Cookie poCookie) {
			moCookie = poCookie;
		}

		public Cookie getCookie() {
			return moClientCookie == null ? moCookie : moClientCookie;
		}

		private void writeObject(ObjectOutputStream poOut) throws IOException {
			poOut.writeObject(moCookie.getName());
			poOut.writeObject(moCookie.getValue());
			poOut.writeObject(moCookie.getComment());
			poOut.writeObject(moCookie.getDomain());
			poOut.writeObject(moCookie.getExpiryDate());
			poOut.writeObject(moCookie.getPath());
			poOut.writeInt(moCookie.getVersion());
			poOut.writeBoolean(moCookie.isSecure());
		}

		private void readObject(ObjectInputStream poIn)
				throws ClassNotFoundException,
				IOException {
			String lsName = (String) poIn.readObject();
			String lsValue = (String) poIn.readObject();
			moClientCookie = new BasicClientCookie(lsName, lsValue);
			moClientCookie.setComment((String) poIn.readObject());
			moClientCookie.setDomain((String) poIn.readObject());
			moClientCookie.setExpiryDate((Date) poIn.readObject());
			moClientCookie.setPath((String) poIn.readObject());
			moClientCookie.setVersion(poIn.readInt());
			moClientCookie.setSecure(poIn.readBoolean());
		}
	}

	/**
	 * 更新cookie的域名
	 *
	 * @param context
	 */
	public static void updateCookieDomain(Context context) {
		LZCookieStore cookieStore = new LZCookieStore(context);
		List<Cookie> cookies = cookieStore.getCookies();
		for (Cookie cookie : cookies) {
			cookieStore.addCookie(cookie);
		}
	}

	/**
	 * webview设置cookie
	 *
	 * @param context
	 */
	public static void synCookies(Context context) {
		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.removeAllCookie();// 移除
		Cookie loSessionCookie = HttpSession.getInstance(context).getCookieObj(
				"PHPSESSID");
		Cookie loUidCookie = HttpSession.getInstance(context).getCookieObj(
				"uid");
		if (loSessionCookie != null) {
			String lsSession = loSessionCookie.getName() + "="
					+ loSessionCookie.getValue();
			cookieManager.setCookie(Consts.BASE_M_URL_SERVER, lsSession);
		}
		if (loUidCookie != null) {
			String lsUid = loUidCookie.getName() + "=" + loUidCookie.getValue();
			cookieManager.setCookie(Consts.BASE_M_URL_SERVER, lsUid);
		}
		CookieSyncManager.getInstance().sync();
	}

}
