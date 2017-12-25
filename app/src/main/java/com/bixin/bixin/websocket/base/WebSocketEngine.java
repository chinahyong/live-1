/**
 * Project Name:feizao File Name:WebSocketImpl.java Package Name:com.lonzh.lib
 * Date:2015-8-12下午5:44:56
 */

package com.bixin.bixin.websocket.base;

import org.json.JSONObject;

import com.bixin.bixin.library.util.EvtLog;

import de.tavendo.autobahn.WampOptions;
import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

/**
 * ClassName:WebSocketImpl Function: TODO ADD FUNCTION. Reason: TODO ADD REASON.
 * Date: 2015-8-12 下午5:44:56
 * @author Live
 * @version 1.0
 */
public abstract class WebSocketEngine {

	protected final String TAG = WebSocketEngine.class.getSimpleName();
	/** websocket连接基础类 */
	protected WebSocket mConnection;
	/** websocket消息处理类 */
	protected WebSocketConnectionHandler mConnectionHandler;

	protected WebSocketEngine(WebSocketConnectionHandler mConnectionHandler) {
		this.mConnection = new WebSocketConnection();
		this.mConnectionHandler = mConnectionHandler;
	}

	/**
	 * 开始websoket连接
	 * @param wsUri 连接地址
	 */
	public void start(String wsUri) {
		EvtLog.d(TAG, "start wsUri," + wsUri);
		try {
			if (mConnection.isConnected()) {
				EvtLog.i(TAG, "start isConnected");
				return;
			}
			WampOptions options = new WampOptions();
			options.setMaxMessagePayloadSize(1024 * 1024);
			options.setMaxFramePayloadSize(1024 * 1024);
			mConnection.connect(wsUri, mConnectionHandler, options);
		} catch (WebSocketException e) {
			EvtLog.d(TAG, e.toString());
		}
	}

	/**
	 * websoket重新连接
	 * @param wsUri 连接地址
	 */
	public void reStart(String wsUri) {
		EvtLog.d(TAG, "reStart wsUri," + wsUri);
		try {
			if (mConnection.isConnected()) {
				mConnection.disconnect();
			}
			mConnection = new WebSocketConnection();
			mConnection.connect(wsUri, mConnectionHandler);
		} catch (WebSocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * websoket维持连接
	 */
	public void sendHeartBeat() {
		JSONObject object = new JSONObject();
		try {
			object.put("cmd", "ping");
			mConnection.sendTextMessage(object.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * websocket关闭
	 */
	public void close() {
		if (mConnection.isConnected()) {
			mConnection.disconnect();
		}
	}

}
