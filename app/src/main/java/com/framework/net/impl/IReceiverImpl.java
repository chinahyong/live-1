package com.framework.net.impl;

import com.bixin.bixin.common.model.HttpConstants;
import com.framework.net.AEntity;
import com.framework.net.IReceiverListener;

import org.json.JSONException;
import org.json.JSONObject;
import com.bixin.bixin.App;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.config.AppConfig;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.util.ActivityJumpUtil;

public class IReceiverImpl implements IReceiverListener {
	private static final String TAG = "IReceiverImpl";
	private CallbackDataHandle mCallbackDataHandle;
	private String resultCode;
	private String resultMsg;

	public IReceiverImpl(CallbackDataHandle callback) {
		mCallbackDataHandle = callback;
	}

	@Override
	public void onReceive(AEntity entity) {
		EvtLog.e(TAG, "IReceiverImpl result:" + entity.receiveData);
		//请求网络接口成功，有返回数据
		if (HttpConstants.SENT_STATUS_SUCCESS.equals(entity.sentStatus)) {
			if (entity.receiveData != null) {
				try {
					JSONObject json = new JSONObject(entity.receiveData);
					resultCode = json.getString(HttpConstants.SERVER_RESULT_CODE);
					resultMsg = json.getString(HttpConstants.SERVER_RESULT_MSG);
					Object resultData = json.opt(HttpConstants.SERVER_RESULT_DATA);
					// 服务器返回码 正确
					if (HttpConstants.SENT_STATUS_SUCCESS.equals(resultCode)) {
						if (mCallbackDataHandle != null) {
							mCallbackDataHandle.onCallback(true, HttpConstants.SENT_STATUS_SUCCESS, resultMsg,
									resultData);
						}
						return;
					}
					//如果未登录，跳转登录页面
					if (HttpConstants.SENT_STATUS_NEED_LOGIN.equals(resultCode)) {
						// 设置未登录
						AppConfig.getInstance().updateLoginStatus(false);
						ActivityJumpUtil.toLoginActivity(App.mContext, true);
					}
					if (mCallbackDataHandle != null) {
						mCallbackDataHandle.onCallback(false, resultCode, resultMsg, resultData);
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
