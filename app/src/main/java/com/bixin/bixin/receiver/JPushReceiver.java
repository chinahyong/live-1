package com.bixin.bixin.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.lonzh.lib.network.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.framework.net.impl.CallbackDataHandle;
import cn.jpush.android.api.JPushInterface;
import com.bixin.bixin.App;
import tv.live.bx.R;
import com.bixin.bixin.activities.ShareDialogActivity;
import com.bixin.bixin.activities.WebViewActivity;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.config.AppConfig;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.PackageUtil;
import com.bixin.bixin.live.activities.LiveMediaPlayerActivity;
import com.bixin.bixin.util.ActivityJumpUtil;

/**
 * 自定义接收器
 * <p/>
 * 如果不定义这个 Receiver，则： 1) 默认用户会打开主界面 2) 接收不到自定义消息
 */
public class JPushReceiver extends BroadcastReceiver {
	private static final String TAG = "JPush";
	private static long TimeJustVibrate = 0;// 是否刚刚震动过
	private static final String TASK_TOP = "com.efeizao.feizao.live.activities.LiveCameraStreamActivity";

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle == null) {
			return;
		}
		EvtLog.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));

		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			EvtLog.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
			// send the Registration Id to your server...
			String sessionId = HttpSession.getInstance(App.mContext).getCookie("PHPSESSID");
			// 存在sessionId 但 app初始化jpush未登记成功
			if (!TextUtils.isEmpty(sessionId) && AppConfig.getInstance().jpushRegisted && !TextUtils.isEmpty(regId)) {
				BusinessUtils.reportRegisterId(App.mContext, new CallbackDataHandle() {
					@Override
					public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
						if (success) {
							// 此参数如果有数据，在JPushReceiver不需要再执行上报
							AppConfig.getInstance().updateJpushRegistrationStatus(true);
						}
					}
				}, regId);
			}

		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
			EvtLog.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
			processCustomMessage(context, bundle);
			notificationReceiverHandle(context, bundle);// 暂时没有用
		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
			EvtLog.d(TAG, "[MyReceiver] 接收到推送下来的通知");
			int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
			EvtLog.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
			notificationReceiverHandle(context, bundle);
		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
			EvtLog.d(TAG, "[MyReceiver] 用户点击打开了通知");
			JPushInterface.reportNotificationOpened(context, bundle.getString(JPushInterface.EXTRA_MSG_ID));
			// 通知打开处理类
			notificationOnClickHandle(context, bundle);

		} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
			EvtLog.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
			// 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity，
			// 打开一个网页等..

		} else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
			boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
			EvtLog.w(TAG, "[MyReceiver]" + intent.getAction() + " connected state change to " + connected);
		} else {
			EvtLog.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
		}
	}

	/**
	 * 处理接受到的消息
	 */
	private void notificationReceiverHandle(Context context, Bundle bundle) {
		try {
			/** 通知栏实际数据存储在JPushInterface.EXTRA_EXTRA字段 */
			String notifiContent = bundle.getString(JPushInterface.EXTRA_EXTRA);
			// 数据必须是JsonObject对象
			JSONObject object = new JSONObject(notifiContent);
			// 根据不同数据类型，处理方式不一样
			String type = object.getString("type");
			if (Constants.NOTIFICATION_TYPE_ROOM.equals(type)) {
				// 在这里可以自己写代码去定义用户点击后的行为

			} else if (Constants.NOTIFICATION_TYPE_ACTIVITY.equals(type)) {
				// 在这里可以自己写代码去定义用户点击后的行为
			} else if (Constants.NOTIFICATION_TYPE_POST.equals(type)) {

			} else if (Constants.NOTIFICATION_TYPE_ME_MESSAGE.equals(type)) {
				Intent intent = new Intent();
				intent.setAction(MessageReceiver.ACTION_MESSAGE_INFO);
				intent.putExtra(MessageReceiver.MESSAGE_TYPE, Constants.NOTIFICATION_TYPE_ME_MESSAGE);
				App.mContext.sendBroadcast(intent);
			}
			// 其他暂时没有

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	/**
	 * 处理通知栏点击操作
	 */
	private void notificationOnClickHandle(Context context, Bundle bundle) {
		if (PackageUtil.getTaskTopFlag(context, TASK_TOP))
			return;
		try {
			/** 通知栏实际数据存储在JPushInterface.EXTRA_EXTRA字段 */
			String notifiContent = bundle.getString(JPushInterface.EXTRA_EXTRA);
			// 数据必须是JsonObject对象
			JSONObject object = new JSONObject(notifiContent);
			// 根据不同数据类型，处理方式不一样
			String type = object.getString("type");
			if (Constants.NOTIFICATION_TYPE_ROOM.equals(type)) {
				// 在这里可以自己写代码去定义用户点击后的行为
				Map<String, String> lmItem = new HashMap<String, String>();
				lmItem.put("rid", object.getString("rid"));
				lmItem.put(LiveMediaPlayerActivity.MEDIA_PLAY_URL,
						object.optString(LiveMediaPlayerActivity.MEDIA_PLAY_URL));
				if (object.optBoolean("private")) {
					ActivityJumpUtil.toPrivateLiveMediaPlayerActivity(context, lmItem);
				} else {
					ActivityJumpUtil.toLiveMediaPlayerActivity(context, lmItem);
				}
			} else if (Constants.NOTIFICATION_TYPE_ACTIVITY.equals(type)) {
				// 在这里可以自己写代码去定义用户点击后的行为
				Intent clickIntent = new Intent();
				clickIntent.setClass(context, WebViewActivity.class);
				clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				clickIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				Map<String, String> lmItem = new HashMap<String, String>();
				lmItem.put("url", object.optString("url"));
				lmItem.put("title", object.optString("title"));

				lmItem.put(ShareDialogActivity.Share_Content, object.optString(ShareDialogActivity.Share_Content));
				lmItem.put(ShareDialogActivity.Share_Img, object.optString(ShareDialogActivity.Share_Img));
				lmItem.put(ShareDialogActivity.Share_Title, object.optString(ShareDialogActivity.Share_Title));
				lmItem.put(ShareDialogActivity.Share_Url, object.optString(ShareDialogActivity.Share_Url));

				clickIntent.putExtra(WebViewActivity.WEB_INFO, (Serializable) lmItem);
				context.startActivity(clickIntent);
			}
			// 其他暂时没有

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	// send msg to MainActivity
	private void processCustomMessage(Context context, Bundle bundle) {
		String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
		try {
			JSONObject msgJson = new JSONObject(message);
			String msg = msgJson.getString("msg");
			showNotification(context, App.mContext.getResources().getString(R.string.app_name), msg, true);
		} catch (JSONException e) {

		}

	}

	// 打印所有的 intent extra 数据
	private String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			} else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}

	/**
	 * 在状态栏显示通知
	 */
	@SuppressWarnings("deprecation")
	private void showNotification(Context context, String title, String content, boolean isVibrate) {
		if (System.currentTimeMillis() - TimeJustVibrate < 5000) {
			isVibrate = false;
		}

		// 创建一个NotificationManager的引用
		final NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		// 定义Notification的各种属性
		Notification notification;
		Notification.Builder builder = new Notification.Builder(context);
		builder.setSmallIcon(R.drawable.icon_logo)
				.setContentTitle(title)
				.setContentText(content)
				.setWhen(System.currentTimeMillis())
				.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0));
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			notification = builder.getNotification();
		} else {
			notification = builder.build();
		}
		notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
		notification.flags |= Notification.FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;

		int defaults = 0;
		if (isVibrate)
			defaults |= Notification.DEFAULT_VIBRATE;// 震动
		notification.defaults = defaults;

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, notification);
		TimeJustVibrate = System.currentTimeMillis();
	}
}
