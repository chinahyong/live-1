/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016 Shengjie Sim Sun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.tinker.android.patchserver;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

import tv.live.bx.library.file.load.AsyLoadTask;
import tv.live.bx.library.file.load.LoadTask;
import tv.live.bx.library.file.load.LoadingListener;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tinker.android.app.BuildInfo;

import org.json.JSONObject;

import java.io.File;


/**
 * Created by zhangshaowen on 16/11/3.
 */

public class TinkerServerManager {
	private static final String TAG = "Tinker.ServerManager";
	private static final String TINKER_PATCH_VERSION_CFG = "tinker_patch_version_cfg";
	private static final String TINKER_PATCH_VERSION = "tinker_patch_version";
	private static final String TINKER_CACHE_DIR = "tinker_patch";
	public static final String TINKER_TEMP_FILE_SUBIFX = ".tmp";

	private Context mContext;
	private String mAppVersion;
	private Tinker mTinker;
	private static volatile TinkerServerManager client;
	private PatchRequestCallback mCallback;

	public TinkerServerManager(Context context, Tinker tinker) {
		this.mContext = context;
		this.mAppVersion = BuildInfo.VERSION_NAME;
		this.mTinker = tinker;
		this.mCallback = new SamplePatchRequestCallback();
	}

	public static TinkerServerManager get() {
		return client;
	}

	public static TinkerServerManager init(Context context, Tinker tinker) {
		if (client == null) {
			synchronized (TinkerServerManager.class) {
				if (client == null) {
					client = new TinkerServerManager(context, tinker);
				}
			}
		}
		return client;
	}

	public Context getContext() {
		return mContext;
	}

	public String getAppVersion() {
		return mAppVersion;
	}

	public Tinker getTinker() {
		return mTinker;
	}

	/**
	 * 补丁包信息
	 *
	 * @param patchObject
	 */
	public void update(JSONObject patchObject) {
		if (mCallback == null) {
			throw new RuntimeException("callback can't be null");
		}
		if (!mCallback.beforePatchRequest()) {
			return;
		}
		try {
			if (patchObject.optBoolean("isRollback")) {
				mCallback.onPatchRollback();
				return;
			}
			final Integer newVersion = patchObject.getInt("patchVersion");
			final int currentVersion = getCurrentTinkerPatchVersion();

			if (newVersion > currentVersion) {
				LoadingListener loadingListener = new LoadingListener() {
					@Override
					public void onLoadingProcess(long total, long current) {

					}

					@Override
					public void onLoadingSuccess(String targetFilePath) {
						mCallback.onPatchUpgrade(new File(targetFilePath), newVersion, currentVersion);
					}

					@Override
					public void onLoadingFailure(Throwable e) {
						mCallback.onPatchDownloadFail((Exception) e, newVersion, currentVersion);
					}
				};
				String cacheFileAbsPath = getServerFile(mContext, mAppVersion, String.valueOf(newVersion)).getAbsolutePath();
				LoadTask loadTask = new AsyLoadTask();
				loadTask.loadTask(patchObject.getString("url"), cacheFileAbsPath, loadingListener);
			} else {
				TinkerLog.i(TAG, "Needn't update, newPatchVersion is: " + newVersion);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clearTinkerPatchCfg() {
		SharedPreferences sp = mContext.getSharedPreferences(
				TINKER_PATCH_VERSION_CFG, Context.MODE_PRIVATE
		);
		sp.edit().clear().commit();
	}

	public void updateTinkerPatchVersion(int patchVersion) {
		SharedPreferences sp = mContext.getSharedPreferences(
				TINKER_PATCH_VERSION_CFG, Context.MODE_PRIVATE
		);
		sp.edit().putInt(TINKER_PATCH_VERSION + "_" + mAppVersion, patchVersion).commit();
	}

	public int getCurrentTinkerPatchVersion() {
		SharedPreferences sp = mContext.getSharedPreferences(
				TINKER_PATCH_VERSION_CFG, Context.MODE_PRIVATE
		);
		return sp.getInt(TINKER_PATCH_VERSION + "_" + mAppVersion, 0);
	}


	public static File getServerDirectory(Context context) {
		ApplicationInfo applicationInfo = context.getApplicationInfo();
		if (applicationInfo == null) {
			// Looks like running on a test Context, so just return without patching.
			return null;
		}
		return new File(applicationInfo.dataDir, TINKER_CACHE_DIR);
	}

	public static File getServerFile(Context context, String appVersion, String currentVersion) {
		return new File(getServerDirectory(context), appVersion + "_" + currentVersion + ".apk");
	}
}
