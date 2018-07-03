package com.bixin.bixin.callback;

import android.os.Handler;
import android.os.Message;

import com.bixin.bixin.common.JacksonUtil;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.config.UserInfoConfig;
import com.framework.net.impl.CallbackDataHandle;

import java.lang.ref.WeakReference;

/**
 * Created by Administrator on 2017/4/1.
 */

public class MyUserInfoCallbackDataHandle implements CallbackDataHandle {

	private WeakReference<Handler> reference;

	public MyUserInfoCallbackDataHandle(Handler handler) {
		this.reference = new WeakReference<>(handler);
	}

	@Override
	public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
		Message message = Message.obtain();
		if (success) {
			try {
				UserInfoConfig config = JacksonUtil.readValue(result.toString(), UserInfoConfig.class);
				UserInfoConfig.getInstance().updateFromInfo(config);
				message.what = MsgTypes.GET_MY_USER_INFO_SUCCESS;
			} catch (Exception e) {
				e.printStackTrace();
				message.what = MsgTypes.GET_MY_USER_INFO_FAILED;
				message.obj = "数据格式错误";
			}
		} else {
			message.what = MsgTypes.GET_MY_USER_INFO_FAILED;
			message.obj = errorMsg;
		}
		if (reference.get() != null) {
			reference.get().sendMessage(message);
		}
	}
}
