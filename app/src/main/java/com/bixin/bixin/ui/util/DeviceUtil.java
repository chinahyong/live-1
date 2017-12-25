package com.bixin.bixin.ui.util;

import android.os.Build;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by Live on 2017/4/5.
 */

public class DeviceUtil {
	// 检测MIUI
	private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
	private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
	private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

	/**
	 * 判断是否为小米手机
	 * 参考：https://www.zhihu.com/question/22102139/answer/24834510
	 *
	 * @return
	 */
	public static boolean isMIUI() {
		try {
			final BuildProperties prop = BuildProperties.newInstance();
			return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
					|| prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
					|| prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 判断是否为魅族手机
	 *
	 * @return
	 */
	public static boolean isFlyme() {
		try {
			Method method = Build.class.getMethod("hasSmartBar");
			return method != null;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return false;
		}
	}
}
