package com.lib.common.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Create By Amin
 */
public class ToastUtil {

	public static Toast toast;
	public static boolean toastSwitch = true;

	/**
	 * 顯示Toast.short
	 */
	@UiThread
	public static void showToast(@NonNull Context context,@Nullable int toastId) {
		if (null == toast) {
			toast = Toast.makeText(context, toastId, Toast.LENGTH_SHORT);
		} else {
			show(toastId, Toast.LENGTH_SHORT);
		}
		show();
	}

	/**
	 * Toast.short
	 */
	@UiThread
	public static void showToast(@NonNull Context context,@Nullable String msg) {
		if (TextUtils.isEmpty(msg)) {
			return;
		}
		if (null == toast) {
			toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		} else {
			show(msg, Toast.LENGTH_SHORT);
		}
		show();
	}

	/**
	 * Toast 指定時間
	 */
	@UiThread
	public static void showToast(@NonNull Context context,@Nullable String msg, int duration) {
		if (TextUtils.isEmpty(msg)) {
			return;
		}
		if (null == toast) {
			toast = Toast.makeText(context, msg, duration > 0 ? duration : Toast.LENGTH_SHORT);
		} else {
			show(msg, duration > 0 ? duration : Toast.LENGTH_SHORT);
		}
		show();
	}

	/**
	 * Toast 指定時間
	 */
	@UiThread
	public static void showToast(@NonNull Context context,@Nullable int toastid, int duration) {
		if (null == toast) {
			toast = Toast.makeText(context, toastid, duration > 0 ? duration : Toast.LENGTH_SHORT);
		} else {
			show(toastid, duration > 0 ? duration : Toast.LENGTH_SHORT);
		}
		show();
	}

	/**
	 * 展示Toast.Long
	 */
	@UiThread
	public static void showLong(@NonNull Context context,@Nullable String msg) {
		if (TextUtils.isEmpty(msg)) {
			return;
		}
		if (null == toast) {
			toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		} else {
			show(msg, Toast.LENGTH_LONG);
		}
		show();
	}

	/**
	 * 展示Toast.Long
	 */
	@UiThread
	public static void showLong(@NonNull Context context,@Nullable int toastid) {
		if (null == toast) {
			toast = Toast.makeText(context, toastid, Toast.LENGTH_LONG);
		} else {
			show(toastid, Toast.LENGTH_LONG);
		}
		show();
	}

	/**
	 * toast不为空，设置内容时间
	 */
	private static void show(@Nullable int toastId, int duration) {
		toast.setText(toastId);
		toast.setDuration(duration);
	}

	/**
	 * toast不為空，更改內容，時間
	 */
	private static void show(@Nullable String msg, int duration) {
		if (TextUtils.isEmpty(msg)) {
			return;
		}
		toast.setText(msg);
		toast.setDuration(duration);
	}

	/**
	 * 顯示toast
	 */
	private static void show() {
		if (toastSwitch) {
			toast.show();
		}
	}

	/**
	 * 關閉toast
	 */
	public static void cancel() {
		if (null != toast) {
			toast.cancel();
			toast = null;
		}
	}
}
