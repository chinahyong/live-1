/**
 *
 */

package cn.efeizao.feizao.fmk.appupdate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.efeizao.bx.R;

import java.io.File;
import java.util.UUID;

/**
 * Title: 默认的下载观察者 (类或者接口名称) Description: XXXX (简单对此类或接口的名字进行描述) Copyright:
 *
 * @version 1.0
 */
public class DefaultUpdateObserver extends AUpdateObserver {
	private String appName;
	private String packageName;
	private int notification_id = UUID.randomUUID().hashCode();
	private NotificationManager nm;
	private Notification notification;
	private Context context;
	private String downloadUrl;
	private boolean isRedownload;
	private ActivityCallBack activityCallBack;
	// public static final String s_key =
	// "cn.richinfo.fmk.appupdate.ActivityCallBack";

	public static final int STATE_DOWNING = 0;

	private String action;

	public static final int STATE_ERROR = 1;

	public static final int STATE_FINISH = 2;

	/**
	 * @param context
	 * @param cls
	 * @param packageName 用于监听安装成功后的处理
	 * @param appName     应用名称
	 * @param res_icon    应用图标
	 * @param tickText    在状态栏的提示语
	 */
	public DefaultUpdateObserver(Context context, Class<?> cls, String packageName, String appName, Bitmap res_icon,
								 String tickText, String downloadUrl, boolean isRedownload, String action, ActivityCallBack activityCallBack) {
		this.context = context;
		this.packageName = packageName;
		this.appName = appName;
		this.downloadUrl = downloadUrl;
		this.isRedownload = isRedownload;
		this.action = action;
		this.activityCallBack = activityCallBack;
		nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(context);
		builder.setSmallIcon(R.drawable.icon_logo)
				.setContentTitle(tickText)
				.setWhen(System.currentTimeMillis())
				.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT));
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			notification = builder.getNotification();
		} else {
			notification = builder.build();
		}
		// notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.contentView = new RemoteViews(context.getPackageName(), R.layout.notification);
		notification.contentView.setImageViewBitmap(R.id.image, res_icon);
		// Intent notificationIntent = new Intent(context, cls);
		// PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
		// notificationIntent, 0);
		// notification.contentIntent = contentIntent;
	}

	@Override
	protected void start() {
		Log.d("app-update", "start");
		// AppInstall.getInstance(context).registerReceiver();
		nm.cancel(notification_id);
		notification.contentView.setTextViewText(R.id.tv_down_title, String.format("开始下载:%s", appName));
		notification.contentView.setProgressBar(R.id.pb, 100, 0, false);
		nm.notify(notification_id, notification);
	}

	@Override
	protected void downloading(int perent) {
		Log.d("app-update", "downloading");
		notification.contentView.setTextViewText(R.id.tv_down_title, String.format("正在下载:%s", appName));
		// notification.contentView.setInt(R.id.pb, "setVisibility",
		// View.VISIBLE);
		notification.contentView.setProgressBar(R.id.pb, 100, perent, false);
		notification.contentView.setTextViewText(R.id.tv_percent, String.format("%s%%", perent));
		activityCallBack.update(STATE_DOWNING, perent);
		nm.notify(notification_id, notification);
	}

	@Override
	protected void finish(File file) {
		Log.d("app-update", "finish");
		notification.contentView.setTextViewText(R.id.tv_down_title, String.format("下载完成:%s", appName));
		// notification.contentView.setInt(R.id.pb, "setVisibility",
		// View.VISIBLE);
		notification.contentView.setProgressBar(R.id.pb, 100, 100, false);
		notification.contentView.setTextViewText(R.id.tv_percent, String.format("%s%%", 100));
		// nm.notify(notification_id, notification);
		AppInstall.getInstance(context, file, packageName).startInstall();

		// 点击通知栏即安装应用 by huangzhen
		Uri uri = Uri.fromFile(file);
		Intent installIntent = new Intent(Intent.ACTION_VIEW);
		installIntent.setDataAndType(uri, "application/vnd.android.package-archive");
		PendingIntent updatePendingIntent = PendingIntent.getActivity(context, 0, installIntent, 0);
//		notification.setLatestEventInfo(context, appName, "下载完成,点击安装", updatePendingIntent);
		activityCallBack.update(STATE_FINISH, 100);
		nm.notify(notification_id, notification);
	}

	@Override
	protected void error(int errorCode) {
		Log.d("app-update", "error");
		// AppInstall.getInstance(context).registerReceiver();
		// Intent intent = new Intent();
		// intent.setAction("cn.abel.action.broadcast");
		// this.context.sendBroadcast(intent);

		Log.i("liaoguang", "error==下载超时 =====" + errorCode);

		Intent intent = new Intent();
		intent.setAction(action/* "cn.richinfo.subscribe.appupdate" */);

		Log.i("liaoguang", "error==下载超时2 =====" + errorCode);
		activityCallBack.update(STATE_ERROR, 0);
		Log.i("liaoguang", "error==下载超时3 =====" + errorCode);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 108, intent, 0);
		notification.contentIntent = pendingIntent;

		if (errorCode == 1) {
			Log.i("liaoguang", "下载超时,请确认网络连接是否正常,然后点此继续下");
			notification.contentView.setTextViewText(R.id.tv_down_title,
					String.format("下载超时,请确认网络连接是否正常,然后点此继续下载:%s", appName));

		} else if (errorCode == 2) {
			notification.contentView.setTextViewText(R.id.tv_down_title,
					String.format("SD卡内存不足,请确保有足够空间后,再点此下载:%s", appName));
		} else {

			notification.contentView.setTextViewText(R.id.tv_down_title, String.format("下载失败,点此继续下载:%s", appName));
		}

		nm.notify(notification_id, notification);
		// AppInstall.getInstance(context).registerReceiver();
	}

	@Override
	protected void noMiniSD() {
		notification.contentView.setTextViewText(R.id.tv_down_title, String.format("无存储卡，无法下载应用:%s", appName));
		nm.notify(notification_id, notification);
	}

	@Override
	protected void noFinish() {
	}

	@Override
	protected String getDownloadUrl() {
		return downloadUrl;
	}

	@Override
	protected boolean isRedownloadIfExists() {
		return isRedownload;
	}

}
