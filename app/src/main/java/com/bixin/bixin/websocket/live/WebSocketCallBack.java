/**
 * Project Name:feizao File Name:WebSocketImpl.java Package Name:com.lonzh.lib
 * Date:2015-8-12下午5:44:56
 */

package com.bixin.bixin.websocket.live;

/**
 * WebSocketCallBack
 * @author Live
 * @version 1.0
 */
public interface WebSocketCallBack {

	/**
	 * 错误回调接口
	 * @param piErrCode 错误码
	 * @param errorMsg 错误信息
	 * @param cmd 回调方法名称
	 */
	void onError(String piErrCode, String errorMsg, String cmd);

	void onClose(int code, String errosMsg);

	void onOpen();
}
