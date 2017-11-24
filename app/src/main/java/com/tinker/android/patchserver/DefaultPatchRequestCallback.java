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

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.lib.util.TinkerServiceInternals;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;
import com.tencent.tinker.loader.shareutil.ShareSecurityCheck;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.File;


public class DefaultPatchRequestCallback implements PatchRequestCallback {
	private static final String TAG = "Tinker.RequestCallback";

	public static final String TINKER_DOWNLOAD_FAIL_TIMES = "tinker_download_fail";
	public static final int TINKER_DOWNLOAD_FAIL_MAX_TIMES = 3;
	/**
	 * 下载补丁时异常
	 */
	public static final int ERROR_DOWNLOAD_FAIL = -1;
	/**
	 * 检测下载补丁的签名时异常
	 */
	public static final int ERROR_DOWNLOAD_CHECK_FAIL = -2;
	/**
	 * 补丁在patchListener检测时异常
	 */
	public static final int ERROR_LISTENER_CHECK_FAIL = -3;
	/**
	 * 补丁合成异常
	 */
	public static final int ERROR_PATCH_FAIL = -4;
	/**
	 * 补丁加载异常
	 */
	public static final int ERROR_LOAD_FAIL = -5;

	@Override
	public boolean beforePatchRequest() {
		TinkerServerManager tinkerServerManager = TinkerServerManager.get();
		if (TinkerServiceInternals.isTinkerPatchServiceRunning(tinkerServerManager.getContext())) {
			TinkerLog.e(TAG, "tinker service is running");
			return false;
		}
		return true;
	}

	@Override
	public boolean onPatchUpgrade(File file, Integer newVersion, Integer currentVersion) {
		TinkerLog.w(TAG, "onPatchUpgrade, file:%s, newVersion:%d, currentVersion:%d",
				file.getPath(), newVersion, currentVersion);
		TinkerServerManager tinkerServerManager = TinkerServerManager.get();
		Context context = tinkerServerManager.getContext();

		ShareSecurityCheck securityCheck = new ShareSecurityCheck(context);
		if (!securityCheck.verifyPatchMetaSignature(file)) {
			TinkerLog.e(TAG, "onPatchUpgrade, signature check fail. file: %s, version:%d", file.getPath(), newVersion);
			SharePatchFileUtil.safeDeleteFile(file);
			return false;
		}
		tryPatchFile(file, newVersion);
		return true;
	}

	private void tryPatchFile(File patchFile, Integer newVersion) {
		TinkerServerManager tinkerServerManager = TinkerServerManager.get();
		Context context = tinkerServerManager.getContext();
		TinkerLog.w(TAG, "tryPatchFile onReceiveUpgradePatch patchFile :" + patchFile.getAbsolutePath());
		//try install
		TinkerInstaller.onReceiveUpgradePatch(context, patchFile.getAbsolutePath());
	}

	@Override
	public void onPatchDownloadFail(Exception e, Integer newVersion, Integer currentVersion) {
		TinkerLog.w(TAG, "onPatchDownloadFail e:" + e);
	}

	@Override
	public void onPatchSyncFail(Exception e) {
		TinkerLog.w(TAG, "onPatchSyncFail error:" + e);
		TinkerLog.printErrStackTrace(TAG, e, "onPatchSyncFail stack:");
	}

	@Override
	public void onPatchRollback() {
		TinkerLog.w(TAG, "onPatchRollback");
		rollbackPatchDirectly();
	}

	public void rollbackPatchDirectly() {
		TinkerServerManager tinkerServerManager = TinkerServerManager.get();
		Context context = tinkerServerManager.getContext();
		final Tinker tinker = tinkerServerManager.getTinker();
		//restart now
		tinker.cleanPatch();
		ShareTinkerInternals.killAllOtherProcess(context);
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public void updatePatchConditions() {
	}

}
