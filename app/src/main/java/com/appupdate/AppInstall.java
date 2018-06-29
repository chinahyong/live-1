package com.appupdate;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * Title: XXXX (类或者接口名称) Description: XXXX (简单对此类或接口的名字进行描述) Copyright:
 * @version 1.0
 */
public class AppInstall {
	private static final AppInstall instance = new AppInstall();
	private File file;
	private Context context;
	private String packageName;
	private InstallBroadcastReceiver receiver = new InstallBroadcastReceiver();
	private String apkMimeType = MimeTypeMap.getSingleton()
			.getMimeTypeFromExtension("apk");

	private AppInstall() {
	}

	public static final AppInstall getInstance(Context context, File file,
			String packageName) {
		instance.receiver.file = file;
		instance.file = file;
		instance.context = context;
		instance.packageName = packageName;
		instance.receiver.packageName = instance.packageName;
		return instance;
	}

	//
	// public static final AppInstall getInstance(Context context,
	// String packageName, String appName, int res_icon, String tickText,
	// String downloadUrl, boolean isRedownload) {
	//
	// return instance;
	// }
	//
	// public static final AppInstall getInstance(Context context) {
	// instance.context = context;
	// return instance;
	// }

	public void startInstall() {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.fromFile(file), apkMimeType);
		context.startActivity(i);
		// IntentFilter intentFilter = new IntentFilter();
		// intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		// intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		// intentFilter.addDataScheme("package");
		// context.registerReceiver(receiver, intentFilter);
	}

	public static class InstallBroadcastReceiver extends BroadcastReceiver {
		private File file;
		private String packageName;

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if ((action.equals(Intent.ACTION_PACKAGE_ADDED) || action
					.equals(Intent.ACTION_PACKAGE_REPLACED))
					&& intent.getDataString().indexOf(packageName) > -1) {
				boolean delete = file.delete();
				Log.d("app-update",
						String.format("delete file:%s:%s",
								file.getAbsolutePath(), delete));
				context.unregisterReceiver(this);
			} else {
				Log.i("liaoguang", "action====" + action);
//				AppUpdate download = AppUpdate.getInstance();
//				download.startUpdate();
			}

		}
	}

//	public void registerReceiver() {
//
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction("cn.abel.action.broadcast");
//		this.context.registerReceiver(receiver, intentFilter);
//
//	}

	public void unregisterReceiver() {

		this.context.unregisterReceiver(receiver);
	}
}
