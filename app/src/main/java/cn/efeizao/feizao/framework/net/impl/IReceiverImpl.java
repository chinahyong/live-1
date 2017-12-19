package cn.efeizao.feizao.framework.net.impl;

import cn.efeizao.feizao.framework.net.AEntity;
import cn.efeizao.feizao.framework.net.IReceiverListener;
import cn.efeizao.feizao.framework.net.NetConstants;
import org.json.JSONException;
import org.json.JSONObject;
import tv.live.bx.FeizaoApp;
import tv.live.bx.common.Constants;
import tv.live.bx.config.AppConfig;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.util.ActivityJumpUtil;

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
		if (NetConstants.SENT_STATUS_SUCCESS.equals(entity.sentStatus)) {
			if (entity.receiveData != null) {
				try {
					JSONObject json = new JSONObject(entity.receiveData);
					resultCode = json.getString(NetConstants.SERVER_RESULT_CODE);
					resultMsg = json.getString(NetConstants.SERVER_RESULT_MSG);
					Object resultData = json.opt(NetConstants.SERVER_RESULT_DATA);
					// 服务器返回码 正确
					if (NetConstants.SENT_STATUS_SUCCESS.equals(resultCode)) {
						if (mCallbackDataHandle != null) {
							mCallbackDataHandle.onCallback(true, NetConstants.SENT_STATUS_SUCCESS, resultMsg,
									resultData);
						}
						return;
					}
					//如果未登录，跳转登录页面
					if (NetConstants.SENT_STATUS_NEED_LOGIN.equals(resultCode)) {
						// 设置未登录
						AppConfig.getInstance().updateLoginStatus(false);
						ActivityJumpUtil.toLoginActivity(FeizaoApp.mContext, true);
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
