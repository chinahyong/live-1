package com.bixin.bixin.ui.util;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;

import java.lang.reflect.Method;

/**
 * Created by Live on 2017/4/5.
 */

public class AlertWindowUtil {
	public static final String TAG = "AlertWindowUtil";

	/**
	 * 4.4 以上可以直接判断准确
	 * <p>
	 * 4.4 以下非MIUI直接返回true
	 * <p>
	 * 4.4 以下MIUI 可 判断 上一次打开app 时 是否开启了悬浮窗权限
	 *
	 * @param context
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static boolean isFloatWindowOpAllowed(Context context) {
		final int version = Build.VERSION.SDK_INT;

		if (!DeviceUtil.isFlyme() && !DeviceUtil.isMIUI()) {
			return true;
		}
		if (version >= Build.VERSION_CODES.KITKAT) {
			return checkOp(context, 24);  //看AppOpsManager.java //AppOpsManager.OP_SYSTEM_ALERT_WINDOW
		} else {
			if (DeviceUtil.isMIUI()) {
				return (context.getApplicationInfo().flags & 1 << 27) == 1 << 27;
			} else {
				return true;
			}
		}

	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static boolean checkOp(Context context, int op) {
		final int version = Build.VERSION.SDK_INT;

		if (version >= Build.VERSION_CODES.KITKAT) {
			AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
			try {
				Class managerClass = manager.getClass();
				Method method = managerClass.getDeclaredMethod("checkOp", int.class, int.class, String.class);
				int isAllowNum = (Integer) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());

				return AppOpsManager.MODE_ALLOWED == isAllowNum;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
