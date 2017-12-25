/**
 * Project Name:feizao File Name:WebSocketImpl.java Package Name:com.lonzh.lib
 * Date:2015-8-12下午5:44:56
 */

package com.bixin.bixin.websocket.live;

import org.json.JSONObject;


/**
 * WebSocketCallBack
 *
 * @author Live
 * @version 1.0
 */
public interface WebSocketLiveCallBack extends WebSocketCallBack {

	void onSendGift(String piGiftId, String pname, String cost, String ptype, String piGiftCount, String piFrom,
					String psFromNickname, String psFromHeadPic, String fromLevel, String fromModeratorLevel, String fromType, String fromGuardType, String psGiftImg,
					String combo, String comboNum, String comboGiftNum, String activityId, String activityTotalGiftNum, String fromModels, String pkgItemsetId, String androidEffect, String giftBonus, String bonusButtonEnabled, String hitbangTicketNum, String leftCoin);

	void onChatMsg(String piFrom, String psFromNickname, String fromLevel, String fromType, String fromGuardType, String piTo,
				   String psToNickname, String toLevel, String toType, String toGuardType, String psMsg, String piPrivate, String fromMedals, String toMedals, String fromModeratorLevel, String toModeratorLevel);

	void onSendFlower();

	void onUserAttention(String piFrom, String psFromNickname, String fromLevel, String fromType, String fromMedals, String guardType);

	void onUserShare(String piFrom, String psFromNickname, String fromLevel, String fromType, String fromMedals, String moderatorLevel, String guardType);

	void delUser(String piUid, String piType, String psNickname, String psPhoto, String ban, String cid);

	void addUser(String piUid, String piType, String psNickname, String fromLevel, String fromModeratorLevel,
				 String psPhoto, String lowkeyEnter, String cid, String fromMedals, String isGuard, String guardType, String guardTimeType, String mountId, String mountName, String mountAction, String androidMount);

	/**
	 * @paramp String piUid, String piType, String psNickname, String fromLevel,
	 * String fromType, String psPhoto, String ban, String cid
	 */
	void initRoom(String piUid, String piType, String psNickname, String level, String fromModeratorLevel,
				  String psPhoto, String ban, String cid);

	void onPublish(JSONObject data);

	void onUnPublish();

	void onBan(String operatorUid, String operatorNickname, String banUid, String banNickname, String expires);

	void onUnBan(String operatorUid, String operatorNickname, String banUid, String banNickname);

	void onSetAdmin(String operatorUid, String operatorNickname, String setAdminUid, String setAdminNickname);

	void onUnsetAdmin(String operatorUid, String operatorNickname, String setAdminUid, String setAdminNickname);

	void onTi(String operatorUid, String operatorNickname, String tiUid, String tiNickname);

	void onTiModerator(String msg);

	void onNewBulleBarrage(JSONObject data);

	void onNewRewards(JSONObject data);

	void onRefreshOnlineNum(JSONObject data);

	void onModeratorLevelIncrease(JSONObject data);

	void onUserLevelIncrease(JSONObject data);

	void onSystemMessage(JSONObject data);

	void onChangeVideoPullUrl(String pullUrl);

}
