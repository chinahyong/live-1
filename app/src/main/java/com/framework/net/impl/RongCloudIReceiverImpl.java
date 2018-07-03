package com.framework.net.impl;

import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.model.HttpConstants;
import com.bixin.bixin.library.util.EvtLog;

import org.json.JSONException;
import org.json.JSONObject;

import com.framework.net.AEntity;
import com.framework.net.IReceiverListener;

public class RongCloudIReceiverImpl implements IReceiverListener {
	private static final String TAG = "RongCloudIReceiverImpl";
	private CallbackDataHandle mCallbackDataHandle;
	private String resultCode;

	public RongCloudIReceiverImpl(CallbackDataHandle callback) {
		mCallbackDataHandle = callback;
	}

	@Override
	public void onReceive(AEntity entity) {
		EvtLog.e(TAG, "RongCloudIReceiverImpl result:" + entity.receiveData);
		//请求网络接口成功，有返回数据
		if (HttpConstants.SENT_STATUS_SUCCESS.equals(entity.sentStatus)) {
			if (entity.receiveData != null) {
				try {
					JSONObject json = new JSONObject(entity.receiveData);
					resultCode = json.getString("code");
					// 服务器返回码 正确
					if ("200".equals(resultCode)) {
						if (mCallbackDataHandle != null) {
							mCallbackDataHandle.onCallback(true, HttpConstants.SENT_STATUS_SUCCESS, "",
									json);
						}
						return;
					}
					if (mCallbackDataHandle != null) {
						mCallbackDataHandle.onCallback(false, resultCode, "", null);
					}
					return;
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}
		//网络连接错误
		if (mCallbackDataHandle != null) {
			mCallbackDataHandle.onCallback(false, entity.sentStatus, Constants.NETWORK_FAIL, null);
		}

	}
}
