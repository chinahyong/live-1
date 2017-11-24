package tv.live.bx.library.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * 
 * Title: ImeUtil.java
 * Description: 软键盘的工具类
 * Copyright: Copyright (c) 2008
 * @author lin.xr 2013-7-3 上午11:47:37
 * @version 1.0
 */
public class ImeUtil {
	private static final String TAG = "ImeUtil";

	/**
	 * 隐藏软键盘
	 * 
	 * @param act 
	 */
	public static void hideSoftInput(Activity act) {
		try {
			if (act == null) {
				return;
			}
			final View v = act.getWindow().peekDecorView();
			if (v != null && v.getWindowToken() != null) {
				InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
				// method 1
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				// method 2
				// imm.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(),
				// InputMethodManager.HIDE_NOT_ALWAYS);
			}
		} catch (Exception e) {
			EvtLog.w(TAG, e);
		}
	}

	/**
	 * 显示软键盘
	 * 
	 * @param context 
	 */
	public static void showSoftInput(Context context) {
		try {
			InputMethodManager m = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
			EvtLog.w(TAG, e);
		}
	}

	/**
	 * 软键盘是否显示
	 * 
	 * @param context 
	 * @return boolean
	 */
	public static boolean isSoftInputShow(Context context) {
		try {
			InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			return imm.isActive();
		} catch (Exception e) {
			EvtLog.w(TAG, e);
		}
		return false;
	}
}