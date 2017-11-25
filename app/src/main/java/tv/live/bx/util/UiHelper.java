package tv.live.bx.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

import cn.efeizao.feizao.ui.dialog.ConfirmDialgBuilder;
import cn.efeizao.feizao.ui.dialog.CustomDialogBuilder;
import cn.efeizao.feizao.ui.dialog.LiveStatusCustomDialogBuilder;
import cn.efeizao.feizao.ui.dialog.PermissionDialogBuilder;
import cn.jpush.android.api.JPushInterface;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.library.util.TelephoneUtil;

/**
 * Title: UiHelper.java Description: ui操作工具类 Copyright: Copyright (c) 2008
 *
 * @version 1.0
 * @CreateDate 2013-11-12 下午6:29:52
 */
public class UiHelper {
	private static final String TAG = "UiHelper";

	private static Toast mToast = Toast.makeText(FeizaoApp.mConctext, "", Toast.LENGTH_SHORT);

	private static Typeface mSTHeitiLightTf = null;

	public static void showToast(Context context, int toastId) {
		if (!isRunOnUiThread()) {
			return;
		}
		mToast.setText(toastId);
		mToast.show();
	}

	public static void showToast(Context context, String msg) {
		if (!isRunOnUiThread()) {
			return;
		}
		mToast.setText(msg);
		mToast.show();
	}

	public static void showShortToast(Context context, int toastId) {
		if (!isRunOnUiThread()) {
			return;
		}
		mToast.setText(toastId);
		mToast.show();
	}

	public static void showShortToast(Context context, String msg) {
		if (!isRunOnUiThread()) {
			return;
		}
		mToast.setText(msg);
		mToast.show();
	}

	public static void showToastLongTime(Context context, String formatStr) {
		if (!isRunOnUiThread()) {
			return;
		}
		mToast.setText(formatStr);
		mToast.setDuration(Toast.LENGTH_LONG);
		mToast.show();
	}

	public static boolean isRunOnUiThread() {
		return Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId();
	}

	/* 含有一个Handler对象，利用此类实现延迟加载 */
	static class HandlerHolder {
		public static Handler sGlobalUiHandler = new Handler(Looper.getMainLooper());
	}

	/**
	 * 获取与主线程Looper关联的Handler
	 *
	 * @return
	 */
	public static Handler obtainUiHandler() {
		return HandlerHolder.sGlobalUiHandler;
	}

	/**
	 * 设置对话框是否自动关闭
	 *
	 * @param closable 是否可关闭
	 */
	public static void setDialogClosable(Dialog dialog, boolean closable) {
		try {
			Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, closable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取系统状态栏的高度
	 */
	public static float getStatusBarHeight(Context context) {
		Resources resources = context.getResources();
		int statusBarIdentifier = resources.getIdentifier("status_bar_height", "dimen", "android");
		if (0 != statusBarIdentifier) {
			return resources.getDimension(statusBarIdentifier);
		}
		return 0;
	}

	/**
	 * 显示一个确认对话框，默认为“确定”、“取消”按钮，无标题
	 * @param context
	 * @param msg
	 * @param positiveListener
	 * @return
	 */
	// public static Dialog showConfirmDialog(Context context, int titleRes, int
	// msg, int positiveRes, int negativeRes,
	// DialogInterface.OnClickListener positiveListener,
	// DialogInterface.OnClickListener negativeListener) {
	// Dialog dialog = new
	// ConfirmDialgBuilder(context).setMessage(msg).setTitle(titleRes)
	// .setPositiveButton(positiveRes,
	// positiveListener).setNegativeButton(negativeRes, negativeListener)
	// .showDialog();
	// return dialog;
	// }

	/**
	 * 显示一个确认对话框，默认为“确定”、“取消”按钮，无标题
	 *
	 * @param context
	 * @param msg
	 * @param positiveListener
	 * @return
	 */
	public static Dialog showConfirmDialog(Context context, int msg, int positiveRes, int negativeRes,
										   DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
		Dialog dialog = new ConfirmDialgBuilder(context).setMessage(msg)
				.setPositiveButton(positiveRes, positiveListener).setNegativeButton(negativeRes, negativeListener)
				.showDialog();
		return dialog;
	}

	/**
	 * 显示一个确认对话框，默认为“确定”、“取消”按钮，无标题
	 *
	 * @param context
	 * @param @String
	 * @param positiveListener
	 * @return
	 */
	public static Dialog showConfirmDialog(Context context, String msg, int positiveRes, int negativeRes,
										   DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
		Dialog dialog = new ConfirmDialgBuilder(context).setMessage(msg)
				.setPositiveButton(positiveRes, positiveListener).setNegativeButton(negativeRes, negativeListener)
				.showDialog();
		return dialog;
	}

	/**
	 * 显示一个单确认对话框，无标题
	 *
	 * @param context
	 * @return
	 */
	public static Dialog showSingleConfirmDialog(Context context, String content, View.OnClickListener onClickListener) {
		CustomDialogBuilder dialogBuilder = new CustomDialogBuilder(context, R.layout.dialog_single_confirm_layout);
		((TextView) dialogBuilder.findViewById(R.id.message)).setText(content);
		dialogBuilder.setOnClickListener(R.id.item_determine, onClickListener);
		Dialog dialog = dialogBuilder.showDialog();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	/**
	 * 从view 得到图片
	 *
	 * @param view
	 * @return
	 */
	public static Bitmap getBitmapFromView(View view) {
		view.destroyDrawingCache();
		view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
				View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.setDrawingCacheEnabled(true);
		Bitmap bitmap = view.getDrawingCache(true);
		return bitmap;
	}

	/**
	 * 显示无权限对话框，无标题
	 *
	 * @return
	 */
	public static Dialog showPermissionDialog(Context context, int question, int deviceName, int permissionName) {
		Dialog dialog = new PermissionDialogBuilder(context).setQuestionTitle(question).setExceptionTitle(deviceName)
				.setMethod1(permissionName).showDialog();
		return dialog;
	}

	/**
	 * 显示无权限对话框，无标题
	 *
	 * @return
	 */
	public static Dialog showPermissionDialog(Context context, String question, String deviceName, String permissionName) {
		Dialog dialog = new PermissionDialogBuilder(context).setQuestionTitle(question).setExceptionTitle(deviceName)
				.setMethod1(permissionName).showDialog();
		return dialog;
	}

	/**
	 * 显示自定义对话框，无标题
	 *
	 * @return
	 */
	public static Dialog showLiveStatusCustomDialogBuilder(Context context, String error) {
		Dialog dialog = new LiveStatusCustomDialogBuilder(context, error).showDialog();
		return dialog;
	}

	/**
	 * 显示notificaiton打开提醒
	 *
	 * @param context
	 */
	public static void showNotificationDialog(final Context context) {
		// 如果JPush推送关闭，则打开
		if (JPushInterface.isPushStopped(context)) {
			JPushInterface.resumePush(context);
		}
		// 如果没有开启通知权限
		if (!TelephoneUtil.isNotificationAvailable(context)) {
			UiHelper.showConfirmDialog(context, R.string.setting_open_notification_msg, R.string.settings,
					R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//跳转到系统设置
							TelephoneUtil.startNotificationManager(context);
						}
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
		}
	}

	/**
	 * 显示一个单确认对话框，无标题
	 *
	 * @param context
	 * @return
	 */
	public static Dialog showLiveNeedFocusDialog(Context context, View.OnClickListener onClickListener) {
		final CustomDialogBuilder dialogBuilder = new CustomDialogBuilder(context, R.layout.dialog_need_focus);
		dialogBuilder.setOnClickListener(R.id.tv_left, onClickListener);
		dialogBuilder.setOnClickListener(R.id.tv_right, onClickListener);
		Dialog dialog = dialogBuilder.showDialog();
		return dialog;
	}

}
