/**
 * Name:com.efeizao.feizao.library.parse Date:2015-8-13下午5:43:17
 */

package com.bixin.bixin.websocket.live;

import android.text.TextUtils;

import com.bixin.bixin.common.Constants;
import com.bixin.bixin.config.AppConfig;
import com.bixin.bixin.library.util.EvtLog;
import com.lonzh.lib.network.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import com.bixin.bixin.common.bean.HttpNetConstants;
import de.tavendo.autobahn.WebSocketConnectionHandler;

/**
 * 直播消息处理类
 *
 * @author Live
 * @version 1.0
 */
public class WebSocketFlowerHandler extends WebSocketConnectionHandler {

	private WebSocketFlowerCallBack callback;

	public WebSocketFlowerHandler(WebSocketFlowerCallBack iCallback) {
		this.callback = iCallback;
	}

	@Override
	public void onOpen() {
		if (callback != null) {
			callback.onOpen();
		}
	}

	@Override
	public void onTextMessage(String payload) {
		EvtLog.e("WebSocket", "flower onTextMessage:" + payload);
		if (TextUtils.isEmpty(payload))
			return;
		try {
			JSONObject json = new JSONObject(payload);
			String methodName = json.optString("cmd");
			Object resultData = json.opt("data");
			String errno = json.optString("errno");
			String msg = json.optString("msg");
			if (HttpNetConstants.SENT_STATUS_NEED_LOGIN.equals(errno)) {
				// 设置未登录
				AppConfig.getInstance().updateLoginStatus(false);
			}

			if (!HttpNetConstants.SENT_STATUS_SUCCESS.equals(errno)) {
				if (callback != null) {
					callback.onError(errno, msg, methodName);
				}
				return;
			}

			if (Constants.ON_SEND_FLOWER.equals(methodName)) {
				if (callback != null) {
					callback.onSendFlower();
				}
			} else if (Constants.ON_FIRST_SEND_FLOWER.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					callback.onFirstSendFlower(datas.get("uid"), datas.get("nickname"), datas.get("level"),
							datas.get("type"), datas.get("medals"), datas.get("guardType"));
				}

			}
		} catch (JSONException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	@Override
	public void onClose(int code, String reason) {
		if (callback != null) {
			callback.onClose(code, reason);
		}
	}

}
