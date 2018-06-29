package com.framework.net.impl;


/**
 * 数据处理校验回调接口
 *
 * @author Live
 */
public interface CallbackDataHandle extends DataCallBack {

	String TAG = "CallbackDataHandle";

	/**
	 * 数据处理校验回调接口方法
	 *
	 * @param success   请求结果
	 * @param errorCode 错误码（若success为true,忽略该参数）
	 * @param result    请求结果描述信息
	 */
	void onCallback(boolean success, String errorCode, String errorMsg, Object result);

}
