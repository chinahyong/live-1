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

import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tinker.android.util.Utils;

import java.io.File;


public class SamplePatchRequestCallback extends DefaultPatchRequestCallback {
	private static final String TAG = "Tinker.SampleRequestCallback";

	@Override
	public boolean beforePatchRequest() {
		boolean result = super.beforePatchRequest();
		if (result) {
			TinkerServerManager client = TinkerServerManager.get();
			Tinker tinker = client.getTinker();
			if (!tinker.isMainProcess()) {
				TinkerLog.e(TAG, "beforePatchRequest, only request on the main process");
				return false;
			}
			if (Utils.isGooglePlay()) {
				TinkerLog.e(TAG, "beforePatchRequest, google play channel, return false");
				return false;
			}
		}

		return result;
	}

	@Override
	public void onPatchRollback() {
		TinkerLog.w(TAG, "onPatchRollback");
		TinkerServerManager tinkerServerManager = TinkerServerManager.get();
		if (!tinkerServerManager.getTinker().isTinkerLoaded()) {
			TinkerLog.w(TAG, "onPatchRollback, tinker is not loaded, just return");
			return;
		}

		rollbackPatchDirectly();
	}

	@Override
	public void onPatchDownloadFail(Exception e, Integer newVersion, Integer currentVersion) {
		super.onPatchDownloadFail(e, newVersion, currentVersion);
	}

	@Override
	public void onPatchSyncFail(Exception e) {
		super.onPatchSyncFail(e);
	}

	@Override
	public boolean onPatchUpgrade(File file, Integer newVersion, Integer currentVersion) {
		boolean result = super.onPatchUpgrade(file, newVersion, currentVersion);
		if (result) {
			TinkerServerManager tinkerServerManager = TinkerServerManager.get();
			tinkerServerManager.updateTinkerPatchVersion(newVersion);
		}
		return result;
	}

	/**
	 * 重写，回滚后清除补丁patch包，但不重启
	 */
	@Override
	public void rollbackPatchDirectly() {
		TinkerServerManager client = TinkerServerManager.get();
		final Tinker tinker = client.getTinker();
		client.clearTinkerPatchCfg();
		//restart now
		tinker.cleanPatch();
	}
}
