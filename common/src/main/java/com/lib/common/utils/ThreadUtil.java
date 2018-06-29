package com.lib.common.utils;

import android.os.Looper;

/**
 * Create by Admin
 */

public class ThreadUtil {
	/**
	 * 是否是UI線程
	 *
	 * @return
	 */
	public static boolean isRunOnUiThread() {
		return Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId();
	}
}
