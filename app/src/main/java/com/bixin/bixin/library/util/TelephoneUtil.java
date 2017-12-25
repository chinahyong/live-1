package com.bixin.bixin.library.util;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.bixin.bixin.App;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

public class TelephoneUtil {
	private static final String TAG = "TelephoneUtil";
	private static PowerManager.WakeLock wakeLock = null;
	private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
	private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

	public static boolean isWifiEnable(Context context) {
		ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conManager != null && conManager.getActiveNetworkInfo() != null
				&& conManager.getActiveNetworkInfo().isAvailable()) {
			if (conManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断是否打开网络连接
	 *
	 * @return
	 */
	public static boolean isNetworkAvailable() {
		Context context = App.mContext;
		if (context == null) {
			return false;
		}
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}
		NetworkInfo[] info = null;
		try {
			info = cm.getAllNetworkInfo();
		} catch (Exception e) {
			EvtLog.w(TAG, e);
		}
		if (info != null) {
			for (int i = 0; i < info.length; i++) {
				if (info[i].getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isGPSAvailable() {
		boolean result;
		LocationManager locationManager = (LocationManager) App.mContext
				.getSystemService(Context.LOCATION_SERVICE);
		result = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		EvtLog.d(TAG, "result:" + result);

		return result;
	}

	/**
	 * 判断是否锁屏状态
	 *
	 * @param context 上下文
	 * @return true 锁屏 flase 非锁屏
	 */
	public static boolean isScreenLocked(Context context) {
		KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		if (pm.isScreenOn() == false) {
			return true;
		} else {
			return mKeyguardManager.inKeyguardRestrictedInputMode();
		}
	}

	/**
	 * 唤醒屏幕,保持长亮
	 */
	public static void acquireWakeLock(Context context) {
		EvtLog.d("PowerManager", "Acquiring wake lock");
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		if (wakeLock == null) {
			wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
		}

		wakeLock.acquire();
	}

	/**
	 * 释放屏幕长亮锁
	 */
	public static void releaseWakeLock() {
		EvtLog.d("PowerManager", "release wake lock");
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	/**
	 * 打开网络设置界面
	 */
	public static void openWifiSetting(Context mContext) {
		Intent intent = null;
		// TODO Auto-generated method stub
		if (android.os.Build.VERSION.SDK_INT > 10) {
			// 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
			intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		} else {
			intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		}
	}

	/**
	 * 获取设备唯一序列号 MD5(IMEI + DEVICE ID + ANDROID ID + WIFI MAC)
	 *
	 * @param context
	 * @return
	 */
	public static String getDeviceId(Context context) {
		// 1 compute IMEI
		TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String m_szImei = TelephonyMgr.getDeviceId(); // Requires
		// READ_PHONE_STATE

		// 2 compute DEVICE ID
		String m_szDevIDShort = "35"
				+ // we make this look like a valid IMEI
				Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.CPU_ABI.length() % 10
				+ Build.DEVICE.length() % 10 + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
				+ Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 + Build.MODEL.length() % 10
				+ Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
				+ Build.USER.length() % 10; // 13 digits
		// 3 android ID - unreliable
		String m_szAndroidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

		// 4 wifi manager, read MAC address - requires
		// android.permission.ACCESS_WIFI_STATE or comes as null
		WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();

		/* // 5 Bluetooth MAC address android.permission.BLUETOOTH required
		 * BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth
		 * adapter m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		 * String m_szBTMAC = m_BluetoothAdapter.getAddress(); */

		// 6 SUM THE IDs
		String m_szLongID = m_szImei + m_szDevIDShort + m_szAndroidID + m_szWLANMAC;
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
		byte p_md5Data[] = m.digest();

		String m_szUniqueID = new String();
		for (int i = 0; i < p_md5Data.length; i++) {
			int b = (0xFF & p_md5Data[i]);
			// if it is a single digit, make sure it have 0 in front (proper
			// padding)
			if (b <= 0xF)
				m_szUniqueID += "0";
			// add number to string
			m_szUniqueID += Integer.toHexString(b);
		}
		m_szUniqueID = m_szUniqueID.toUpperCase();

		return m_szUniqueID;
	}

	/**
	 * @param context 上下文
	 * @return 设备的IMEI号码
	 */
	public static String getDeviceImei(Context context) {
		TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId = TelephonyMgr.getDeviceId();
		if (TextUtils.isEmpty(deviceId)) {
			return "";
		} else {
			return deviceId;
		}
	}

	/**
	 * @return 设备的Model描述
	 */
	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	/**
	 * @return 设备的系统版本
	 */
	public static String getAndridVersion() {
		return Build.VERSION.RELEASE;
	}

	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	/**
	 * @return 手机Sim是否可用
	 */
	public static boolean isSimAvailable(Context context) {
		TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return TelephonyMgr.getSimState() == TelephonyManager.SIM_STATE_READY;
	}

	/**
	 * 返回是否包含smartBar
	 *
	 * @return
	 */
	public static boolean hasSmartBar() {
		try {
			// 新型号可用反射调用Build.hasSmartBar()
			Method method = Class.forName("android.os.Build").getMethod("hasSmartBar");
			return ((Boolean) method.invoke(null)).booleanValue();
		} catch (Exception e) {
		}

		// 反射不到Build.hasSmartBar()，则用Build.DEVICE判断
		if (Build.DEVICE.equals("mx2")) {
			return true;
		} else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) {
			return false;
		}

		return false;
	}

	/**
	 * 是否安装指定包名的应用
	 *
	 * @param context     上下文
	 * @param packageName 包名
	 * @return
	 */
	public static boolean haveInstallApp(Context context, String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return false;
		}
		try {
			context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * 判断是否在主线程
	 *
	 * @return
	 */
	public static boolean isRunOnUiThread() {
		return Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId();
	}

	/**
	 * 获取屏幕信息
	 */
	public static DisplayMetrics getDisplayMetrics() {
		DisplayMetrics dm = new DisplayMetrics();
		dm = App.mContext.getResources().getDisplayMetrics();
		float density = dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
		EvtLog.d(TAG + " DisplayMetrics", "density=" + density + "; dm.widthPixels=" + dm.widthPixels);
		return dm;
	}

	// 此方法只是关闭软键盘
	public static void hideSoftInput(Activity activity) {
		InputMethodManager loImm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		loImm.hideSoftInputFromWindow(activity.getWindow().peekDecorView().getApplicationWindowToken(), 0);
	}

	/**
	 * 设置是否全屏
	 *
	 * @param flag 是否全部，true为设置全屏，false为取消全屏
	 */
	public static void setIsFullScreen(Activity context, boolean flag) {
		if (flag) {
			context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			WindowManager.LayoutParams attrs = context.getWindow().getAttributes();
			attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
			context.getWindow().setAttributes(attrs);
		}
	}

	/**
	 * umeng获取设备信息
	 */
	public static String getDeviceInfo(Context context) {
		try {
			org.json.JSONObject json = new org.json.JSONObject();
			android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			String device_id = tm.getDeviceId();

			android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);

			String mac = wifi.getConnectionInfo().getMacAddress();
			json.put("mac", mac);

			if (TextUtils.isEmpty(device_id)) {
				device_id = mac;
			}

			if (TextUtils.isEmpty(device_id)) {
				device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
			}

			json.put("device_id", device_id);

			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 复制到剪切板
	 *
	 * @param context
	 * @param str
	 */
	public static void copyClip(Context context, String str) {
		ClipboardManager myClipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData myClip = ClipData.newPlainText("text", str);
		myClipboard.setPrimaryClip(myClip);
	}

	/**
	 * 是否打开通知权限
	 *
	 * @param context
	 * @return
	 */
	public static boolean isNotificationAvailable(Context context) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
//			NotificationManagerCompat manager = NotificationManagerCompat.from(context);
//			boolean isOpened = manager.areNotificationsEnabled();
//			return isOpened;
//		}
			AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
			ApplicationInfo applicationInfo = context.getApplicationInfo();
			int uid = applicationInfo.uid;
			String pack = context.getApplicationContext().getPackageName();
			Class clazz = null;
			try {
				clazz = Class.forName(appOpsManager.getClass().getName());
				Method checkUpNoThrowMethod = clazz.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);

				Field opPostNotificationValue = clazz.getField(OP_POST_NOTIFICATION);
				int value = (Integer) opPostNotificationValue.get(Integer.class);
				return ((Integer) checkUpNoThrowMethod.invoke(appOpsManager, value, uid, pack) == AppOpsManager.MODE_ALLOWED);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * 打开系统设置面板
	 *
	 * @param context
	 */
	public static void startNotificationManager(Context context) {
		// 进入设置系统应用权限界面
		Intent localIntent = new Intent();
		localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (Build.VERSION.SDK_INT >= 9) {
			localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
			localIntent.setData(Uri.fromParts("package", App.mContext.getPackageName(), null));
		} else if (Build.VERSION.SDK_INT <= 8) {
			localIntent.setAction(Intent.ACTION_VIEW);
			localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			localIntent.putExtra("com.android.settings.ApplicationPkgName", App.mContext.getPackageName());
		}
		context.startActivity(localIntent);
	}

	private static final String marshmallowMacAddress = "02:00:00:00:00:00";
	private static final String fileAddressMac = "/sys/class/net/wlan0/address";

	/**
	 * 获取mac地址
	 *
	 * @param context
	 * @return
	 */
	public static String getMacAddress(Context context) {
		WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifiMan.getConnectionInfo();

		if (wifiInf != null && marshmallowMacAddress.equals(wifiInf.getMacAddress())) {
			String result = null;
			try {
				result = getMacAddressByInterface();
				if (result != null) {
					return result;
				} else {
					result = getAddressMacByFile(wifiMan);
					return result;
				}
			} catch (IOException e) {
				EvtLog.e("MobileAccess", "Erreur lecture propriete Adresse MAC");
			} catch (Exception e) {
				EvtLog.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
			}
		} else {
			if (wifiInf != null && wifiInf.getMacAddress() != null) {
				return wifiInf.getMacAddress();
			} else {
				return "";
			}
		}
		return marshmallowMacAddress;
	}

	private static String getMacAddressByInterface() {
		try {
			List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface nif : all) {
				if (nif.getName().equalsIgnoreCase("wlan0")) {
					byte[] macBytes = nif.getHardwareAddress();
					if (macBytes == null) {
						return "";
					}

					StringBuilder res1 = new StringBuilder();
					for (byte b : macBytes) {
						res1.append(String.format("%02X:", b));
					}

					if (res1.length() > 0) {
						res1.deleteCharAt(res1.length() - 1);
					}
					return res1.toString();
				}
			}

		} catch (Exception e) {
			EvtLog.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
		}
		return null;
	}

	private static String getAddressMacByFile(WifiManager wifiMan) throws Exception {
		String ret;
		int wifiState = wifiMan.getWifiState();

		wifiMan.setWifiEnabled(true);
		File fl = new File(fileAddressMac);
		FileInputStream fin = new FileInputStream(fl);
		ret = crunchifyGetStringFromStream(fin);
		fin.close();

		boolean enabled = WifiManager.WIFI_STATE_ENABLED == wifiState;
		wifiMan.setWifiEnabled(enabled);
		return ret;
	}

	private static String crunchifyGetStringFromStream(InputStream crunchifyStream) throws IOException {
		if (crunchifyStream != null) {
			Writer crunchifyWriter = new StringWriter();

			char[] crunchifyBuffer = new char[2048];
			try {
				Reader crunchifyReader = new BufferedReader(new InputStreamReader(crunchifyStream, "UTF-8"));
				int counter;
				while ((counter = crunchifyReader.read(crunchifyBuffer)) != -1) {
					crunchifyWriter.write(crunchifyBuffer, 0, counter);
				}
			} finally {
				crunchifyStream.close();
			}
			return crunchifyWriter.toString();
		} else {
			return "No Contents";
		}
	}
}
