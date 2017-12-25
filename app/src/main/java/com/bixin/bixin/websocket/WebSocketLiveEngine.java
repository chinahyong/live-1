/**
 * Project Name:feizao File Name:WebSocketImpl.java Package Name:com.lonzh.lib
 * Date:2015-8-12下午5:44:56
 */

package com.bixin.bixin.websocket;

import com.bixin.bixin.config.AppConfig;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.websocket.base.WebSocketEngine;
import org.json.JSONObject;
import de.tavendo.autobahn.WebSocketConnectionHandler;

/**
 * 直播模块的消息服务类
 *
 * @author Live
 * @version 1.0
 */
public class WebSocketLiveEngine extends WebSocketEngine {

	public static String USER_SHARE = "userShare";
	public static String USER_ATTENTION = "userAttention";

	private WebSocketLiveEngine(WebSocketConnectionHandler mConnectionHandler) {
		super(mConnectionHandler);
	}

	/**
	 * 送花
	 */
	public void sendFlower() {
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "sendFlower");
			EvtLog.d(TAG, "sendFlower ," + object.toString());
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 发消息
	 */
	public void sendMsg(String toUid, String msg, boolean isPrivate) {
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "sendMsg");
			JSONObject data = new JSONObject();
			data.put("toUid", toUid);
			data.put("msg", msg);
			data.put("private", isPrivate);
			object.put("data", data);
			EvtLog.d(TAG, "sendMsg ," + object.toString());
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 发弹幕消息
	 */
	public void sendBarrage(String toUid, String msg) {
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "sendBarrage");
			JSONObject data = new JSONObject();
			data.put("toUid", toUid);
			data.put("msg", msg);
			object.put("data", data);
			EvtLog.d(TAG, "sendBarrage ," + object.toString());
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 送背包礼物
	 *
	 * @param pkgItemsetId 背包礼物时传递
	 */
	public void sendGift(int pid, int num, String pkgItemsetId) {
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "sendGift");
			JSONObject data = new JSONObject();
			data.put("pid", pid);
			data.put("num", num);
			data.put("pkgItemsetId", pkgItemsetId);
			object.put("data", data);
			EvtLog.d(TAG, "sendGift pkgItemsetId," + object.toString());
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 送礼物
	 */
	public void sendGift(int pid, int num) {
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "sendGift");
			JSONObject data = new JSONObject();
			data.put("pid", pid);
			data.put("num", num);
			object.put("data", data);
			EvtLog.d(TAG, "sendGift ," + object.toString());
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 踢人
	 */
	public void sendTi(String uid) {
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "ti");
			JSONObject data = new JSONObject();
			data.put("tiUid", uid);
			data.put("expires", 7200);
			object.put("data", data);
			EvtLog.d(TAG, "sendTi ," + object.toString());
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 禁言
	 */
	public void sendBan(String uid){
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "ban");
			JSONObject data = new JSONObject();
			data.put("banUid", uid);
			data.put("expires", AppConfig.getInstance().banTime);
			object.put("data", data);
			EvtLog.d(TAG, "sendBan ," + object.toString());
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 取消禁言
	 */
	public void sendUnBan(String uid){
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "unBan");
			JSONObject data = new JSONObject();
			data.put("unBanUid", uid);
			object.put("data", data);
			EvtLog.d(TAG, "sendBan ," + object.toString());
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 设为管理员
	 */
	public void sendSettingManager(String uid) {
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "setAdmin");
			JSONObject data = new JSONObject();
			data.put("setAdminUid", uid);
			object.put("data", data);
			EvtLog.d(TAG, "sendSettingManager ," + object.toString());
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 取消管理员
	 */
	public void sendRemoveManager(String uid) {
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "unsetAdmin");
			JSONObject data = new JSONObject();
			data.put("unsetAdminUid", uid);
			object.put("data", data);
			EvtLog.d(TAG, "sendRemoveManager ," + object.toString());
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送上线
	 *
	 * @param flag false为会发送推送通知，true不发送
	 */
	public void sendOnLine(boolean flag) {
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "videoPublish");
			JSONObject data = new JSONObject();
			data.put("autoRetry", flag);
			object.put("data", data);
			EvtLog.d(TAG, "sendOnLine ," + object.toString());
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 发送命令
	 */
	public void sendCommand(String command) {
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", command);
			EvtLog.d(TAG, "sendCommand ," + object.toString());
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public static class Builder {
		private WebSocketConnectionHandler mWebSocketConnectionHandler;

		public Builder(WebSocketConnectionHandler mWebSocketConnectionHandler) {
			this.mWebSocketConnectionHandler = mWebSocketConnectionHandler;
		}

		public WebSocketLiveEngine build() {
			return new WebSocketLiveEngine(mWebSocketConnectionHandler);
		}
	}

}
