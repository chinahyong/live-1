package tv.live.bx.library.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import tv.live.bx.FeizaoApp;
import tv.live.bx.common.Constants;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * 应用工具类.
 *
 * @author
 */
public class PackageUtil {

	private static final String TAG = "PackageUtil";
	private static final String DEVICE_ID = "Unknow";

	public static final int FROYO = 8;
	public static final int GINGERBREAD = 9;
	public static final int HONEYCOMB = 11;
	public static final int ICECREAMSANDWICH = 14;

	/**
	 * 获取应用程序的版本号
	 *
	 * @return 版本号
	 * @throws NameNotFoundException 找不到改版本号的异常信息
	 */
	public static int getVersionCode() {
		int verCode = 0;
		try {
			verCode = FeizaoApp.mConctext.getPackageManager().getPackageInfo(FeizaoApp.mConctext.getPackageName(), 0).versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return verCode;
	}

	/**
	 * 获取应用程序的外部版本号
	 *
	 * @return 外部版本号
	 * @throws NameNotFoundException 找不到信息的异常
	 */
	public static String getVersionName() {
		String versionName = "1.0";
		try {
			versionName = FeizaoApp.mConctext.getPackageManager().getPackageInfo(FeizaoApp.mConctext.getPackageName(),
					0).versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionName;
	}

	/**
	 * 获取MAC地址
	 *
	 * @return 返回MAC地址
	 */
	public static String getLocalMacAddress() {
		WifiManager wifi = (WifiManager) FeizaoApp.mConctext.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();

		return info.getMacAddress();
	}

	/**
	 * 获取 string.xml 文件定义的字符串
	 *
	 * @param resourceId 资源id
	 * @return 返回 string.xml 文件定义的字符串
	 */
	public static String getString(int resourceId) {
		Resources res = FeizaoApp.mConctext.getResources();
		return res.getString(resourceId);
	}

	/**
	 * 获取 color.xml 颜色定义
	 *
	 * @param resourceId 资源id
	 * @return 返回 color.xml 文件定义的内容
	 */
	public static int getColor(int colorId) {
		Resources res = FeizaoApp.mConctext.getResources();
		return res.getColor(colorId);
	}

	/**
	 * @return 获得手机端终端标识
	 */
	public static String getTerminalSign() {
		String tvDevice = null;
		TelephonyManager tm = (TelephonyManager) FeizaoApp.mConctext.getSystemService(Context.TELEPHONY_SERVICE);
		tvDevice = tm.getDeviceId();
		if (tvDevice == null) {
			tvDevice = getLocalMacAddress();
		}

		if (tvDevice == null) {
			tvDevice = DEVICE_ID;
		}

		EvtLog.d(TAG, "唯一终端标识号：" + tvDevice);
		return tvDevice;
	}

	/**
	 * @return 获得手机型号
	 */
	public static String getDeviceType() {
		String deviceType = android.os.Build.MODEL;
		return deviceType;
	}

	/**
	 * @return 获得操作系统版本号
	 */

	public static String getSysVersion() {
		String sysVersion = android.os.Build.VERSION.RELEASE;
		return sysVersion;
	}

	/**
	 * 读取manifest.xml中application标签下的配置项，如果不存在，则返回空字符串
	 *
	 * @param key
	 * @return
	 */
	public static String getConfigObject(String key) {
		String val = "";
		try {
			ApplicationInfo appInfo = FeizaoApp.mConctext.getPackageManager().getApplicationInfo(
					FeizaoApp.mConctext.getPackageName(), PackageManager.GET_META_DATA);
			Object obj = appInfo.metaData.get(key);
			if (obj == null) {
				EvtLog.e(TAG, "please set config value for " + key + " in manifest.xml first");
			} else {
				val = obj.toString();
			}
		} catch (Exception e) {
			EvtLog.w(TAG, e);
		}
		return val;
	}

	/**
	 * 读取manifest.xml中application标签下的配置项，如果不存在，则返回空字符串
	 *
	 * @param key 键名
	 * @return 返回字符串
	 */
	public static String getConfigString(String key) {
		String val = "";
		try {
			ApplicationInfo appInfo = FeizaoApp.mConctext.getPackageManager().getApplicationInfo(
					FeizaoApp.mConctext.getPackageName(), PackageManager.GET_META_DATA);
			val = appInfo.metaData.getString(key);
			if (val == null) {
				EvtLog.e(TAG, "please set config value for " + key + " in manifest.xml first");
			}
		} catch (Exception e) {
			EvtLog.w(TAG, e);
		}
		return val;
	}

	/**
	 * 读取manifest.xml中application标签下的配置项
	 *
	 * @param key 键名
	 * @return 返回字符串
	 */
	public static int getConfigInt(String key) {
		int val = 0;
		try {
			ApplicationInfo appInfo = FeizaoApp.mConctext.getPackageManager().getApplicationInfo(
					FeizaoApp.mConctext.getPackageName(), PackageManager.GET_META_DATA);
			val = appInfo.metaData.getInt(key);
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		}
		return val;
	}

	/**
	 * 读取manifest.xml中application标签下的配置项
	 *
	 * @param key 键名
	 * @return 返回字符串
	 */
	public static boolean getConfigBoolean(String key) {
		boolean val = false;
		try {
			ApplicationInfo appInfo = FeizaoApp.mConctext.getPackageManager().getApplicationInfo(
					FeizaoApp.mConctext.getPackageName(), PackageManager.GET_META_DATA);
			val = appInfo.metaData.getBoolean(key);
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		}
		return val;
	}

	/**
	 * 指定的activity所属的应用，是否是当前手机的顶级
	 *
	 * @param context activity界面或者application
	 * @return 如果是，返回true；否则返回false
	 */
	public static boolean isTopApplication(Context context) {
		if (context == null) {
			return false;
		}

		try {
			String packageName = context.getPackageName();
			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
			if (tasksInfo.size() > 0) {
				// 应用程序位于堆栈的顶层
				if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
					return true;
				}
			}
		} catch (Exception e) {
			// 什么都不做
			EvtLog.w(TAG, e);
		}
		return false;
	}

	/**
	 * 判断APP是否已经打开
	 *
	 * @param context activity界面或者application
	 * @return true表示已经打开 false表示没有打开
	 */
	public static boolean isAppOpen(Context context) {
		ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> activitList = mActivityManager.getRunningTasks(30);
		for (ActivityManager.RunningTaskInfo info : activitList) {
			EvtLog.e(TAG, "context.getPackageName():" + context.getPackageName());
			EvtLog.e(TAG, "info.topActivity.getPackageName():" + info.topActivity.getPackageName());
			EvtLog.e(TAG, "info.topActivity.getClassName():" + info.topActivity.getClassName());
			if (context.getPackageName().equals(info.topActivity.getPackageName())) {
				EvtLog.d(TAG, "接收闹钟   存在后台进程");
				return true;
			}
		}
		EvtLog.d(TAG, "接收闹钟   不存在运行activity, 不唤醒服务.");
		return false;
	}

	/**
	 * 动态获取资源id
	 *
	 * @param context activity界面或者application
	 * @param name    资源名
	 * @param defType 资源所属的类 drawable, id, string, layout等
	 * @return 资源id
	 */
	public static int getIdentifier(Context context, String name, String defType) {
		return context.getResources().getIdentifier(name, defType, context.getPackageName());
	}

	/**
	 * Check if ActionBar is available.
	 *
	 * @return
	 */
	public static boolean hasActionBar() {
		return Build.VERSION.SDK_INT >= HONEYCOMB;
	}

	/**
	 * sdk版本号
	 *
	 * @return 版本号
	 */
	public static int getSDKVersion() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 * 获取屏幕高度
	 *
	 * @param context
	 * @return
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static int getScreenHeight(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point point = new Point();
		try {
			display.getSize(point);
		} catch (java.lang.NoSuchMethodError ignore) {
			// Older device
			point.y = display.getHeight();
		}
		return point.y;
	}

	/**
	 * 获取屏幕宽度
	 *
	 * @param context
	 * @return
	 */
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point point = new Point();
		try {
			display.getSize(point);
		} catch (java.lang.NoSuchMethodError ignore) {
			// Older device
			point.x = display.getWidth();
		}
		return point.x;
	}

	/**
	 * 获取状态栏高度
	 *
	 * @param context
	 * @return
	 */
	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 38;// 默认为38，貌似大部分是这样的

		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = context.getResources().getDimensionPixelSize(x);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return sbar;
	}

	/**
	 * 获取是否为debug mode
	 *
	 * @param conext 程序中的Activity， Application的实例都可以
	 */
	public static boolean isDebugMode(Context context) {
		if (context == null) {
			EvtLog.e("Utils::isDebugMode", "context is null");
			return false;
		}
		ApplicationInfo info = context.getApplicationInfo();
		return (0 != ((info.flags) & ApplicationInfo.FLAG_DEBUGGABLE));
	}

	/**
	 * 另：几个常用的Package命令：
	 * <p/>
	 * 新浪微博（编辑界面）：com.sina.weibo com.sina.weibo.EditActivity
	 * <p/>
	 * 腾讯微博（编辑界面）：com.tencent.WBlog com.tencent.WBlog.activity.MicroblogInput
	 * <p/>
	 * 微信： com.tencent.mm com.tencent.mm.ui.LauncherUI QQ: com.tencent.mobileqq
	 * com.tencent.mobileqq.activity.HomeActivity
	 */
	public static void startComponentActivity(Activity mContext, String packageName, String activityName) {
		Intent intent = new Intent();
		ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(cmp);
		mContext.startActivity(intent);
	}

	/**
	 * 以打开微信为例，前提需要知道打开应用的包名，一般一个发布版本的应用，包名不会轻易改变的，但是，打开QQ就要注意了，
	 * 毕竟QQ的发布版本有不下于4个版本。
	 */
	public static void startComponentActivity(Activity mContext, String packageName) {
		Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
		mContext.startActivity(intent);
	}

	/**
	 * 以打开微信为例，前提需要知道打开应用的包名，一般一个发布版本的应用，包名不会轻易改变的，但是，打开QQ就要注意了，
	 * 毕竟QQ的发布版本有不下于4个版本。
	 */
	public static void startWeiXin(Activity mContext) {
		Intent intent = mContext.getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
		mContext.startActivity(intent);
	}

	/**
	 * isGuojiangPackage:(是否为果酱的应用). <br/>
	 *
	 * @return true 是；false 否
	 */
	public static boolean isGuojiangPackage() {
		return Constants.PACKAGE_ID.endsWith("0");
	}

	/**
	 * 判断某个权限是否被允许(仅适用于系统本身禁止权限判断，不包括第三方助手)
	 *
	 * @param permission
	 * @return
	 */
	public static boolean checkPermission(Context context, String permission) {
		boolean result = false;
		if (Build.VERSION.SDK_INT >= 23) {
			try {
				Class<?> clazz = Class.forName("android.content.Context");
				Method method = clazz.getMethod("checkSelfPermission", String.class);
				int rest = (Integer) method.invoke(context, permission);
				result = rest == PackageManager.PERMISSION_GRANTED;
			} catch (Exception e) {
				result = false;
			}
		} else {
			PackageManager pm = context.getPackageManager();
			if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
				result = true;
			}
		}
		return result;
	}

	public static String getDeviceInfo(Context context) {
		try {
			org.json.JSONObject json = new org.json.JSONObject();
			android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			String device_id = null;
			if (checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
				device_id = tm.getDeviceId();
			}
			String mac = null;
			FileReader fstream = null;
			try {
				fstream = new FileReader("/sys/class/net/wlan0/address");
			} catch (FileNotFoundException e) {
				fstream = new FileReader("/sys/class/net/eth0/address");
			}
			BufferedReader in = null;
			if (fstream != null) {
				try {
					in = new BufferedReader(fstream, 1024);
					mac = in.readLine();
				} catch (IOException e) {
				} finally {
					if (fstream != null) {
						try {
							fstream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
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
	 * 获取App UID
	 *
	 * @return
	 */
	public static String getUid(Activity context) {
		String uid = "";
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ApplicationInfo appInfo = context.getApplicationInfo();
		List<RunningAppProcessInfo> run = am.getRunningAppProcesses();
		for (RunningAppProcessInfo runningProcess : run) {
			if ((runningProcess.processName != null) && runningProcess.processName.equals(appInfo.processName)) {
				return uid = String.valueOf(runningProcess.uid);
			}
		}
		return uid;
	}

	/**
	 * 判断activityNames activity是否在栈顶
	 *
	 * @param context
	 * @param activityNames
	 * @return
	 */
	public static boolean getTaskTopFlag(Context context, String... activityNames) {
		//获取栈顶activity
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(2).get(0).topActivity;
		if (cn != null) {
			//判断栈顶activity是否为主播侧，如果是返回不跳转
			for (String str : activityNames) {
				EvtLog.i(TAG, "getTaskTopFlag activityNames:" + str);
				if (cn.getClassName().equals(str)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取当前所在进程名称
	 */
	public static String getCurProcessName(Context context) {

		int pid = android.os.Process.myPid();

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
				.getRunningAppProcesses()) {

			if (appProcess.pid == pid) {
				return appProcess.processName;
			}
		}
		return null;
	}

	/**
	 * 获取app签名md5值
	 */
	public static String getSignMd5(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
			Signature[] signs = packageInfo.signatures;
			Signature sign = signs[0];
			String signStr = encryptionMD5(sign.toByteArray());
			return signStr;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}


	/**
	 * 解析签名
	 *
	 * @param signature
	 */
	public static void parseSignature(byte[] signature) {
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));

			String pubKey = cert.getPublicKey().toString();
			String signNumber = cert.getSerialNumber().toString();
			System.out.println("signName:" + cert.getSigAlgName());
			System.out.println("pubKey:" + pubKey);
			System.out.println("signNumber:" + signNumber);
			System.out.println("subjectDN:" + cert.getSubjectDN().toString());
		} catch (CertificateException e) {
			e.printStackTrace();
		}
	}

	/**
	 * MD5加密
	 *
	 * @param signature 签名内容
	 * @return signature的MD5值
	 */
	public static String encryptionMD5(byte[] signature) {
		MessageDigest messageDigest = null;
		StringBuffer md5StrBuff = new StringBuffer();
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(signature);
			byte[] byteArray = messageDigest.digest();
			for (int i = 0; i < byteArray.length; i++) {
				if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
					md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
				} else {
					md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return md5StrBuff.toString();
	}

}
