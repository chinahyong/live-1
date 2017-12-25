/**
 * Project Name:feizao File Name:WebSocketImpl.java Package Name:com.lonzh.lib
 * Date:2015-8-12下午5:44:56
 */

package com.bixin.bixin.websocket.live;

/**
 * WebSocketCallBack
 *
 * @author Live
 * @version 1.0
 */
public interface WebSocketFlowerCallBack extends WebSocketCallBack {
	void onSendFlower();

	void onFirstSendFlower(String piFrom, String psFromNickname, String fromLevel, String fromType, String medals, String guardType);
}
