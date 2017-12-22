/**
 * Name:com.efeizao.feizao.library.parse Date:2015-8-13下午5:43:17
 */

package tv.live.bx.websocket.live;

import android.text.TextUtils;
import tv.live.bx.common.pojo.NetConstants;
import com.lonzh.lib.network.JSONParser;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import tv.live.bx.common.Constants;
import tv.live.bx.config.AppConfig;
import tv.live.bx.library.util.EvtLog;

/**
 * 直播消息处理类
 *
 * @author Live
 * @version 1.0
 */
public class WebSocketLiveHandler extends WebSocketConnectionHandler {

	private WeakReference<WebSocketLiveCallBack> webSocketLiveCallBackWeakReference;

	public WebSocketLiveHandler(WebSocketLiveCallBack iCallback) {
		this.webSocketLiveCallBackWeakReference = new WeakReference<WebSocketLiveCallBack>(iCallback);
	}

	@Override
	public void onOpen() {
		if (webSocketLiveCallBackWeakReference.get() != null) {
			webSocketLiveCallBackWeakReference.get().onOpen();
		}
	}

	@Override
	public void onTextMessage(String payload) {
		EvtLog.e("WebSocket", "onTextMessage: " + payload);
		if (TextUtils.isEmpty(payload))
			return;
		try {
			JSONObject json = new JSONObject(payload);
			String methodName = json.optString("cmd");
			Object resultData = json.opt("data");
			String errno = json.optString("errno");
			String msg = json.optString("msg");

			if (NetConstants.SENT_STATUS_NEED_LOGIN.equals(errno)) {
				// 设置未登录
				AppConfig.getInstance().updateLoginStatus(false);
			}

			WebSocketLiveCallBack callback = webSocketLiveCallBackWeakReference.get();
			// 如果onConnectStatus回调，errno肯定不会为0，所以会进入onError方法
			if (!NetConstants.SENT_STATUS_SUCCESS.equals(errno)) {
				if (callback != null) {
					callback.onError(errno, msg, methodName);
				}
				return;
			}
			if (Constants.ON_INIT_ROOM.equals(methodName)) {
				List<Map<String, String>> datas = JSONParser.parseMulti((JSONArray) resultData);
				for (int i = 0; i < datas.size(); i++) {
					if (callback != null) {
						Map<String, String> map = datas.get(i);
						callback.initRoom(map.get("uid"), map.get("type"), map.get("nickname"), map.get("level"),
								map.get("moderatorLevel"), map.get("headPic"), map.get("ban"), map.get("cid"));
					}
				}
			} else if (Constants.ON_SEND_MSG.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					callback.onChatMsg(datas.get("fromUid"), datas.get("fromNickname"), datas.get("fromLevel"),
							datas.get("fromType"), datas.get("fromGuardType"), datas.get("toUid"), datas.get("toNickname"), datas.get("toLevel"),
							datas.get("toType"), datas.get("toGuardType"), datas.get("msg"), datas.get("private"), datas.get("fromMedals"), datas.get("toMedals"), datas.get("fromModeratorLevel"), datas.get("toModeratorLevel"));
				}

			} else if (Constants.ON_SEND_GIFT.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					String androidEffect = datas.get("androidEffect1");
					if (TextUtils.isEmpty(androidEffect)) {
						androidEffect = datas.get("androidEffect");
					}
					callback.onSendGift(datas.get("pid"), datas.get("pname"), datas.get("cost"), datas.get("ptype"),
							datas.get("num"), datas.get("fromUid"), datas.get("fromNickname"),
							datas.get("fromHeadPic"), datas.get("fromLevel"), datas.get("fromModeratorLevel"), datas.get("fromType"), datas.get("fromGuardType"),
							datas.get("giftPic"), datas.get("combo"), datas.get("comboNum"), datas.get("comboGiftNum"), datas.get("activityId"),
							datas.get("activityTotalGiftNum"), datas.get("fromMedals"), datas.get("pkgItemsetId"), androidEffect, datas.get("giftBonus"), datas.get("bonusButtonEnabled"), datas.get("hitbangTicketNum"), datas.get("leftCoin"));
				}

			} else if (Constants.ON_SEND_FLOWER.equals(methodName)) {
				if (callback != null) {
					callback.onSendFlower();
				}

			} else if (Constants.ON_USER_ATTENTION.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					callback.onUserAttention(datas.get("uid"), datas.get("nickname"), datas.get("level"),
							datas.get("type"), datas.get("medals"), datas.get("guardType"));
				}

			} else if (Constants.ON_USER_SHARE.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					callback.onUserShare(datas.get("uid"), datas.get("nickname"), datas.get("level"), datas.get("type"), datas.get("medals"), datas.get("moderatorLevel"), datas.get("guardType"));
				}

			} else if (Constants.ON_LOGIN.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					callback.addUser(datas.get("uid"), datas.get("type"), datas.get("nickname"), datas.get("level"),
							datas.get("moderatorLevel"), datas.get("headPic"), datas.get("lowkeyEnter"), datas.get("cid"), datas.get("medals"), datas.get("isGuard"), datas.get("guardType"), datas.get("guardTimeType"),
							datas.get("mountId"), datas.get("mountName"), datas.get("mountAction"), datas.get("androidMount"));
				}
			} else if (Constants.ON_LOGOUT.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					callback.delUser(datas.get("uid"), datas.get("type"), datas.get("nickname"), datas.get("headPic"),
							datas.get("ban"), datas.get("cid"));
				}

			} else if (Constants.ON_VIDEO_PUBLISH.equals(methodName)) {
				if (callback != null) {
					callback.onPublish((JSONObject) resultData);
				}

			} else if (Constants.ON_VIDEO_UNPUBLISH.equals(methodName)) {
				if (callback != null) {
					callback.onUnPublish();
				}
			} else if (Constants.ON_BAN.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					callback.onBan(datas.get("operatorUid"), datas.get("operatorNickname"), datas.get("banUid"),
							datas.get("banNickname"), datas.get("expires"));
				}

			} else if (Constants.ON_UN_BAN.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					callback.onUnBan(datas.get("operatorUid"), datas.get("operatorNickname"), datas.get("unBanUid"),
							datas.get("unBanNickname"));
				}

			} else if (Constants.ON_SET_ADMIN.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					callback.onSetAdmin(datas.get("operatorUid"), datas.get("operatorNickname"),
							datas.get("setAdminUid"), datas.get("setAdminNickname"));
				}

			} else if (Constants.ON_UNSET_ADMIN.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					callback.onUnsetAdmin(datas.get("operatorUid"), datas.get("operatorNickname"),
							datas.get("unsetAdminUid"), datas.get("unsetAdminNickname"));
				}

			} else if (Constants.ON_TI.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					callback.onTi(datas.get("operatorUid"), datas.get("operatorNickname"), datas.get("tiUid"),
							datas.get("tiNickname"));
				}

			} else if (Constants.ON_TI_MODERATOR.equals(methodName)) {
				Map<String, String> datas = JSONParser.parseOne((JSONObject) resultData);
				if (callback != null) {
					callback.onTiModerator(datas.get("msg"));
				}

			} else if (Constants.ON_BATCH_LOGIN.equals(methodName)) {// 僵死用户进入房间
				List<Map<String, String>> datas = JSONParser.parseMulti((JSONArray) resultData);
				for (int i = 0; i < datas.size(); i++) {
					if (callback != null) {
						Map<String, String> map = datas.get(i);
						callback.initRoom(map.get("uid"), map.get("type"), map.get("nickname"), map.get("level"),
								map.get("moderatorLevel"), map.get("headPic"), map.get("ban"), map.get("cid"));
					}
				}
			} else if (Constants.ON_BATCH_LOGOUT.equals(methodName)) {// 僵死用户退出房间
				List<Map<String, String>> datas = JSONParser.parseMulti((JSONArray) resultData);
				for (int i = 0; i < datas.size(); i++) {
					if (callback != null) {
						Map<String, String> map = datas.get(i);
						callback.delUser(map.get("uid"), map.get("type"), map.get("nickname"), map.get("headPic"),
								map.get("ban"), map.get("cid"));
					}
				}
			} else if (Constants.ON_NEW_BULLE_BARRAGE.equals(methodName)) {// 消息弹幕
				if (callback != null) {
					callback.onNewBulleBarrage((JSONObject) resultData);
				}
			} else if (Constants.ON_NEW_REWARDS.equals(methodName)) {// 开宝箱
				if (callback != null) {
					callback.onNewRewards((JSONObject) resultData);
				}
			} else if (Constants.ON_REFRESH_ONLINE_NUM.equals(methodName)) {//更新在线人数
				if (callback != null) {
					callback.onRefreshOnlineNum((JSONObject) resultData);
				}
			} else if (Constants.ON_MODERATOR_LEVEL_INCREASE.equals(methodName)) {//主播升级
				if (callback != null) {
					callback.onModeratorLevelIncrease((JSONObject) resultData);
				}
			} else if (Constants.ON_USER_LEVEL_INCREASE.equals(methodName)) {//用户升级
				if (callback != null) {
					callback.onUserLevelIncrease((JSONObject) resultData);
				}
			} else if (Constants.ON_SYSTEM_MESSAGE.equals(methodName)) {//用户升级
				if (callback != null) {
					callback.onSystemMessage((JSONObject) resultData);
				}
			} else if (Constants.ON_HOT_RANK.equals(methodName)) { //热门排名
				if (callback != null) {
					callback.onHotRank((JSONObject) resultData);
				}
			} else if (Constants.ON_CHANGE_VIDEO_PULL_URL.equals(methodName)) {    //主播推流切换观众需要更换拉流地址
				if (callback != null) {
					JSONObject jsonObject = (JSONObject) resultData;
					callback.onChangeVideoPullUrl(jsonObject.getString("pullUrl"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClose(int code, String reason) {
		if (webSocketLiveCallBackWeakReference.get() != null) {
			webSocketLiveCallBackWeakReference.get().onClose(code, reason);
		}
	}

}
