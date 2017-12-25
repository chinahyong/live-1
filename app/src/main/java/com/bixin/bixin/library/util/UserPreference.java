package com.bixin.bixin.library.util;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Title: XXXX (类或者接口名称) Description: XXXX (简单对此类或接口的名字进行描述) Copyright:
 * @version 1.0
 */
public class UserPreference {

	/**
	 * 用户Preference文件
	 */
	public static final String PREFERENCE_USER = "preference_user";

	/*
	 * 应用升级包的地址
	 */
	public static final String PREFERENCE_UPDATEAPK_FILEPATH = "preference_updateapk_filepath";

	private static final SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(PREFERENCE_USER,
				Context.MODE_PRIVATE);
	}

	private static void setValue(String key, String value, Context context) {
		SharedPreferences preferences = getSharedPreferences(context);
		preferences.edit().putString(key, value).commit();
	}

	private static String getValue(String key, Context context) {
		SharedPreferences preferences = getSharedPreferences(context);
		return preferences.getString(key, "");
	}

	public static final void setUpdateApkPath(String path, Context context) {
		setValue(PREFERENCE_UPDATEAPK_FILEPATH, path, context);
	}

	public static final String getUpdateApkPath(Context context) {
		String path = getValue(PREFERENCE_UPDATEAPK_FILEPATH, context);
		return path;
	}

	public static final void delUpdateApk(Context context) {
		String path = getUpdateApkPath(context);
		if (path != null && !"".equals(path)) {
			File apkFile = new File(path);
			if (apkFile != null && apkFile.exists()) {
				apkFile.delete();
			}

			setUpdateApkPath("", context);
		}
	}

}
