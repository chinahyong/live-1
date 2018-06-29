package com.update;

import android.content.Context;
import android.graphics.Bitmap;

import com.appupdate.AppUpdate;
import com.appupdate.ActivityCallBack;

/***
 * Title: AppUpdateManager.java Description: 应用更新管理类 Copyright: Copyright (c)
 * 2008
 * @CreateDate 2014-4-3 下午3:48:56
 * @version 1.0
 */
public class AppUpdateManager {

	// 升级action
	public static final String ACTION_UPDATE = "cn.richinfo.android.calendar.appupdate";

	public Context context;

	// 开始下载
	public void beginToDownload(Context context, String packageName, String appName, Bitmap res_icon, String tickText,
			String downloadUrl, ActivityCallBack activityCallBack) {
		AppUpdate download = AppUpdate.getInstance(context, packageName, appName, res_icon, tickText, downloadUrl,
				activityCallBack);
		download.startUpdate();
	}
}
