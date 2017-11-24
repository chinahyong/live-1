package tv.live.bx.common;

import android.content.Context;
import android.text.TextUtils;

import tv.live.bx.FeizaoApp;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.MD5;
import tv.live.bx.library.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import cn.efeizao.feizao.framework.net.AEntity;
import cn.efeizao.feizao.framework.net.BaseEntityImpl;
import cn.efeizao.feizao.framework.net.NetConstants;
import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import cn.efeizao.feizao.framework.net.impl.IReceiverImpl;
import cn.efeizao.feizao.framework.net.impl.PostCommunication;
import cn.efeizao.feizao.framework.net.impl.PostCommunicationCookie;

public class BusinessUtils {
	private static final String TAG = "BusinessUtils";
	static final int EXECUTOR_POOL_SIZE_PER_CORE = 4;
	// 创建一个容量为1的线程池
	static ExecutorService executorService = getMultiThreadExecutorService();

	public static ExecutorService getMultiThreadExecutorService() {
		if (null == executorService || executorService.isShutdown()) {
			// final int numThreads =
			// Math.round(Runtime.getRuntime().availableProcessors() *
			// EXECUTOR_POOL_SIZE_PER_CORE);
			executorService = Executors.newFixedThreadPool(EXECUTOR_POOL_SIZE_PER_CORE);
			EvtLog.d(TAG, "MultiThreadExecutor created with " + EXECUTOR_POOL_SIZE_PER_CORE + " threads");
		}
		return executorService;
	}

	/************************************ 基本功能 *********************************************/
	/**
	 * 用户登录
	 *
	 * @param psUsername
	 * @param psPassword
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static void login(Context poContext, String psUsername, String psPassword, CallbackDataHandle callDataHandle) throws InvalidKeyException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("username", psUsername);
		lmParams.put("remember", "true");
		lmParams.put("password", Utils.rsaEncrypt(
				(String) FeizaoApp.getCacheData("public_key"), psPassword));
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.LOGIN));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * QQ账号登录
	 *
	 * @param poContext
	 * @param psAccessToken
	 * @param psOpenId
	 * @param psExpiredIn
	 */
	public static void loginByQQ(Context poContext, String psAccessToken, String psOpenId, String psExpiredIn, CallbackDataHandle callDataHandle) {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("accessToken", psAccessToken);
		lmParams.put("openId", psOpenId);
		lmParams.put("expiredIn", psExpiredIn);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.LOGIN_BY_QQ));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 微博登录
	 *
	 * @param poContext
	 * @param psAccessToken
	 * @param psExpiredIn
	 */
	public static void loginByWeibo(Context poContext, String psAccessToken, String uid, String psExpiredIn, CallbackDataHandle callDataHandle) {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("accessToken", psAccessToken);
		lmParams.put("uid", uid);
		lmParams.put("expiredIn", psExpiredIn);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.LOGIN_BY_SINA));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 微信登录
	 *
	 * @param poContext
	 * @param psAccessToken
	 * @param psOpenId
	 * @param psExpiredIn
	 */
	public static void loginByWeixin(Context poContext, String psAccessToken, String psOpenId, String refreshToken,
									 String unionid, String psExpiredIn, CallbackDataHandle callDataHandle) {
		Map<String, String> lmParams = new HashMap<>();
		lmParams.put("accessToken", psAccessToken);
		lmParams.put("refreshToken", refreshToken);
		lmParams.put("openId", psOpenId);
		if (TextUtils.isEmpty(unionid)) {
			unionid = "";
		}
		lmParams.put("unionid", unionid);
		lmParams.put("expiredIn", psExpiredIn);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.LOGIN_BY_WECHAT));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取本人用户信息
	 *
	 * @param poContext
	 */
	public static void getMyUserInfo(Context poContext, CallbackDataHandle callDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.GET_USER_INFO_URL));
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取公钥
	 *
	 * @param poContext
	 */
	public static void getPubKey(Context poContext, CallbackDataHandle callDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.GET_PUBKEY_URL));
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取首页轮播图片
	 */
	public static void getMainBanners(Context poContext, CallbackDataHandle callDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.BANNER_URL));
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取主播列表数据
	 */
	public static void getAuthorListData(Context poContext, int page, String type, int status, int limit,
										 CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.AUTHOR_LIST_URL) + "?page=" + page + "&status=" + status;
		if (!TextUtils.isEmpty(type)) {
			URL = URL + "&type=" + type;
		}
		if (limit > 0) {
			URL = URL + "&limit=" + limit;
		}
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 根据标签获取主播列表数据
	 */
	public static void getAuthorListDataByTag(Context poContext, int page, String tagId, int limit,
											  CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.AUTHOR_LIST_BY_TAG_URL) + "?page=" + page + "&tagId=" + tagId;
		if (limit > 0) {
			URL = URL + "&limit=" + limit;
		}
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取手机主播列表数据
	 */
	public static void getMoblieAuthorListData(Context poContext, int page, String type,
											   CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.AUTHOR_MOBILE_LIST_URL) + "?page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取附近主播列表数据
	 */
	public static void getNearAuthorListData(Context poContext, int page, String location,
											 CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_NEAR_AUTHOR_LIST_URL) + "?page=" + page + "&location=" + location;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取最新版本信息
	 */
	public static void getLastVersion(Context poContext, CallbackDataHandle callDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.GET_NEW_VERSION_URL));
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 退出登录
	 *
	 * @param poContext
	 */
	public static void logout(Context poContext, CallbackDataHandle callDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.LOGOUT_USER_URL));
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 修改用户信息,屏蔽性别修改
	 *
	 * @param poContext
	 * @param psNickname
	 * @param piSex
	 * @param psBirthday
	 * @param callDataHandle
	 * @param headIcon
	 * @param psDesc
	 */
	public static void modifyUserInfo(Context poContext, CallbackDataHandle callDataHandle, String psNickname,
									  int piSex, String psDesc, String psBirthday, String headIcon) {
		Map<String, String> lmParams = new HashMap<>();
		lmParams.put("nickname", psNickname);
		lmParams.put("sex", String.valueOf(piSex));
		if (psBirthday != null)
			lmParams.put("birthday", psBirthday);
		lmParams.put("desc", psDesc);
		if (!TextUtils.isEmpty(headIcon))
			lmParams.put("headPic", headIcon);

		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.UPDATE_USER_INFO_URL));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);

	}

	/**
	 * 获取房间信息
	 *
	 * @param poContext
	 * @param psRid
	 */
	public static void getRoomInfo(Context poContext, CallbackDataHandle callDataHandle, String psRid) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_ROOM_INFO_URL) + "?rid=" + psRid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);

	}

	/**
	 * 关注用户
	 *
	 * @param poContext
	 */
	public static void follow(Context poContext, CallbackDataHandle callDataHandle, String uid) {

		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_FOLLOW_URL) + "?attentionUid=" + uid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 关注一组用户
	 *
	 * @param poContext
	 */
	public static void followUids(Context poContext, CallbackDataHandle callDataHandle, List<String> uids) {
		String uidStr = uids.toArray().toString();
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_FOLLOW_URL) + "?attentionUids=" + uidStr;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 取消关注用户
	 *
	 * @param poContext
	 */
	public static void removeFollow(Context poContext, CallbackDataHandle callDataHandle, String uid) {

		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_REMOVE_FOLLOW_URL) + "?removeAttentionUid=" + uid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 举报
	 *
	 * @param poContext
	 * @param type      1 房间 ; 2 帖子 ; 3 回复
	 * @param id        为房间号、帖子、回复 的id
	 * @param reason    举报类型1 : 色情 2 ：垃圾 3 ：人身 4 ：敏感 5 ： 虚假 6 ：其他
	 */
	public static void reportIllegal(Context poContext, CallbackDataHandle callDataHandle, String type, String id,
									 int reason, String imgPath) {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("type", type);
		lmParams.put("id", id);
		lmParams.put("reason", String.valueOf(reason));
		if (!TextUtils.isEmpty(imgPath)) {
			lmParams.put("pic", imgPath);
		}
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.REPORT_INFO_URL));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取房间排行信息
	 *
	 * @param poContext
	 * @param psRid
	 */
	public static void getRoomRankInfo(Context poContext, CallbackDataHandle callDataHandle, String psRid) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.ROOM_RANK_URL) + "?rid=" + psRid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取排行榜单信息
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void getRankInfo(Context poContext, CallbackDataHandle callDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.GET_RANK_URL));
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 关注
	 *
	 * @param poContext
	 */
	public static void support(Context poContext, CallbackDataHandle callDataHandle, int pid) {

		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_SUPPORT_URL) + "?pid=" + pid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取回复列表信息
	 *
	 * @param poContext
	 */
	public static void getRelayListInfo(Context poContext, int page,
										int limit, CallbackDataHandle callDataHandle,
										int pid) {

		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_REPLY_LIST_URL) + "?pid=" + pid + "&page=" + page + "&limit=" + limit;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 回复楼层
	 *
	 * @param poContext
	 */
	public static void relayLoucengInfo(Context poContext, CallbackDataHandle
			callDataHandle, String toFReplyId,
										String toReplyId, String toUid, String content) {

		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("toFReplyId", toFReplyId);
		lmParams.put("toReplyId", toReplyId);
		lmParams.put("toUid", toUid);
		lmParams.put("content", content);

		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.REPLY_LOUCENG_URL));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 回复帖子
	 *
	 * @param poContext
	 */
	public static void relayPostInfo(Context poContext, CallbackDataHandle
			callDataHandle, String pid, String content) {

		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("pid", pid);
		lmParams.put("content", content);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.REPLY_POST_URL));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 收藏帖子
	 *
	 * @param poContext
	 */
	public static void collectPostInfo(Context poContext, CallbackDataHandle
			callDataHandle, String pid) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.COLLECT_POST_URL) + "?pid=" + pid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 取消帖子收藏
	 *
	 * @param poContext
	 */
	public static void removeCollectPostInfo(Context poContext, CallbackDataHandle
			callDataHandle, String pid) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.REMOVE_COLLECT_POST_URL) + "?pid=" + pid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取全部帖子列表数据
	 */
	public static void getSubjectListData(Context poContext, int page, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_SUBJECT_LIST_URL) + "?page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取栏目帖子列表数据
	 */
	public static void getForumSubjectListData(Context poContext, int page, String forumId,
											   CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_SUBJECT_LIST_URL) + "?forum=" + forumId + "&page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取栏目热门帖子列表数据
	 */
	public static void getForumSubjectHotListData(Context poContext, int page, String forum,
												  CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_HOT_SUBJECT_LIST_URL) + "?forum=" + forum + "&page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取我的收藏列表数据
	 *
	 * @param page 页数
	 */
	public static void getCollectSubjectListData(Context poContext, int page, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.ME_COLLECT_POST_URL) + "?page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取用户帖子列表数据
	 *
	 * @param page 页数
	 */
	public static void getPersonSubjectListData(Context poContext, int page, String uid,
												CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_PERSON_POST_URL) + "?page=" + page + "&uid=" + uid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取我的发布列表数据
	 *
	 * @param page 页数
	 */
	public static void getPublishSubjectListData(Context poContext, int page, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.ME_PUBLISH_POST_URL) + "?page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取我的回复列表数据
	 *
	 * @param page 页数
	 */
	public static void getMeReplyListData(Context poContext, int page, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.ME_REPLY_POST_URL) + "?page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	// /**
	// * 获取忒子详情数据
	// */
	// public static void getPostDetailData(Context poContext, String pid,
	// CallbackDataHandle callDataHandle) {
	// String URL = NetConstants.GET_POST_DETAIL_URL + "?pid=" + pid;
	// AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle),
	// URL);
	// sendCookieHttps(poContext, aEntity);
	// }

	/**
	 * 获取忒子详情数据
	 *
	 * @param pid 帖子id
	 * @param fid 楼层id
	 */
	public static void getPostDetailData(Context poContext, String pid, String
			fid, CallbackDataHandle callDataHandle) {
		String URL;
		if (TextUtils.isEmpty(fid)) {
			URL = NetConstants.getFullRequestUrl(NetConstants.GET_POST_DETAIL_URL) + "?pid=" + pid;
		} else {
			URL = NetConstants.getFullRequestUrl(NetConstants.GET_POST_DETAIL_URL) + "?pid=" + pid + "&fReplyId=" + fid;
		}
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取用户关注列表数据
	 */
	public static void getLoveListData(Context poContext, CallbackDataHandle callDataHandle,
									   int page, String uid) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_LOVE_LIST_URL) + "?page=" + page + "&uid=" + uid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取用户粉丝列表数据
	 */
	public static void getFansListData(Context poContext, CallbackDataHandle callDataHandle,
									   int page, String uid) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_FANS_LIST_URL) + "?page=" + page + "&uid=" + uid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取我的消息列表数据
	 *
	 * @param page 页数
	 */
	public static void getMeMessageListData(Context poContext, int page, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.ME_MESSAGE_POST_URL) + "?page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 删除我回复的楼层信息
	 */
	public static void deletePostReply(Context poContext, String replyId, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.DELETE_REPLY_URL) + "?replyId=" + replyId;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 删除楼中楼里的回复信息
	 */
	public static void deletePostLzlReply(Context poContext, String
			lzlReplyId, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.DELETE_LZL_REPLY_URL) + "?lzlReplyId=" + lzlReplyId;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取帖子模块列表数据
	 */
	public static void getPostMoudleListData(Context poContext, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_POST_MOUDLE_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 发帖
	 */
	public static void publicPostInfo(Context poContext, CallbackDataHandle
			callDataHandle, String forumId,
									  String title, String content, ArrayList<String> imageFiles) {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("forumId", forumId);
		lmParams.put("title", title);
		lmParams.put("content", content);
		lmParams.put("picNum", String.valueOf(imageFiles == null ? 0 : imageFiles.size()));

		for (int i = 0; i < imageFiles.size(); i++) {
			lmParams.put("pic_" + i, imageFiles.get(i));
		}

		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.PUBLIC_POST_URL));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);

	}

	/**
	 * 获取推流信息
	 */
	public static void getLiveStreamInfo(Context poContext, CallbackDataHandle
			callDataHandle, String rid, int videoWidth, int videoHeight) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_LIVE_STREAM_URL) + "?rid=" + rid + "&screenWidth=" + videoWidth + "&screenHeight=" + videoHeight;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取微信预支付参数
	 */
	public static void getPrePayData(Context poContext, CallbackDataHandle
			callDataHandle, String number) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_PRE_PAY_DATA) + "?onumber=" + number;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取预qq支付的数据
	 */
	public static void getPreQQPayData(Context context, CallbackDataHandle callbackDataHandle, String onumber) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_PRE_QQPAY_DATA) + "?onumber=" + onumber;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callbackDataHandle), URL);
		sendCookieHttps(context, aEntity);
	}

	/**
	 * 获取支付宝支付参数
	 */
	public static void getAliPayData(Context poContext, CallbackDataHandle
			callDataHandle, String number) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_ALI_PAY_DATA) + "/payId/" + number + "/platform/android";
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取签到状态信息
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void getSignStatus(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_SIGN_STATUS_DATA);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 签到
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void getSign(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_SIGN_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取签到状态信息
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void getTaskListInfo(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_TAST_LIST_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取签到状态信息
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void shareReport(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.SHARE_REPORT_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取配置信息
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void getConfigInfo(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_CONFIG_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取等级配置信息
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void getLevelConfigInfo(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_LEVEL_CONFIG_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取勋章配置信息
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void getModelConfigInfo(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_MODEL_CONFIG_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取精品推荐列表
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void getProductRecommentList(Context poContext, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_PRODUCT_RECOMMENT_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取搜索主播列表
	 *
	 * @param poContext
	 * @param searchText
	 */
	public static void getSearchAnchorList(Context poContext, CallbackDataHandle
			callDataHandle, int page,
										   String searchText) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.SEARCH_ANCHOR_LIST_URL) + "?keyword=" + searchText + "&page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取系统消息列表
	 *
	 * @param poContext
	 */
	public static void getSystemMsgList(Context poContext, CallbackDataHandle
			callDataHandle, String type, int page) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_SYSTEM_MSG_LSIT_URL) + "?page=" + page + "&type=" + type;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取系统消息列表结合融云
	 *
	 * @param poContext
	 */
	public static void getConversationSystemMsgList(Context poContext, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_CONVERSATION_SYSTEM_MSG_LSIT_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 私聊走app server进行过滤
	 *
	 * @param poContext
	 * @param type      消息类型：TxtMsg，ImgMsg
	 * @param toUid     接收人
	 * @param message   内容
	 * @param pic       图片
	 */
	public static void sendPrivateMsgForFilter(Context poContext, String type, String toUid, String message, String pic, CallbackDataHandle
			callDataHandle) {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("type", type);
		lmParams.put("toUid", toUid);
		if (!TextUtils.isEmpty(message)) {
			lmParams.put("message", message);
		}
		if (!TextUtils.isEmpty(pic)) {
			lmParams.put("pic", pic);
		}
		String URL = NetConstants.getFullRequestUrl(NetConstants.SEND_PRIVATE_MSG);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 关注或粉丝Fragment的list列表
	 */
	public static void getFansCareList(Context poContext, int page, String filter, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_FANS_CARE_INFO) + "?page=" + page + "&status=" + 0 + "&filter=" + filter;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 关注或粉丝Fragment的list列表
	 */
	public static void getSelectRecieverList(Context poContext, int page, String filter, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_FANS_CARE_INFO) + "?page=" + page + "&status=" + 1 + "&filter=" + filter;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}


	/**
	 * 获取系统消息未读
	 *
	 * @param poContext
	 */
	public static void setSystemMsgListtoUnRead(Context poContext, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.SET_SYSTEM_MSG_READ);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取推荐饭圈
	 *
	 * @param poContext
	 */
	public static void getRecommentGroup(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_RECOMMENT_GROUP);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取我的圈子/我加入的圈子列表
	 *
	 * @param poContext
	 */
	public static void getMeGroup(Context poContext, int page, String status, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_ME_GROUP) + "?page=" + page;
		if (!TextUtils.isEmpty(status)) {
			URL = URL + "&status=" + status;
		}
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取饭圈列表
	 */
	public static void getFanListData(Context poContext, int page, String
			status, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_GROUP_LIST_URL) + "?status=" + status + "&page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 加入饭圈
	 */
	public static void addFan(Context poContext, String groupId, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.ADD_GROUP_URL) + "?groupId=" + groupId;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 查询饭圈列表
	 */
	public static void getSearchFanList(Context poContext, int page, String searchText,
										CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.SEARCH_GROUP_LIST_URL) + "?keyword=" + searchText + "&page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取饭圈详情
	 */
	public static void getFanDetail(Context poContext, String groupId, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_GROUP_DETAIL_URL) + "?groupId=" + groupId;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取饭圈管理员列表
	 */
	public static void getFanAdmin(Context poContext, String groupId, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_GROUP_ADMIN_URL) + "?groupId=" + groupId;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取饭圈帖子列表 或者 最新动态
	 *
	 * @param sort 排序字段 支持last_reply_time 和 create_time
	 */
	public static void getFanPostList(Context poContext, String groupId, int page, String sort,
									  CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_GROUP_POST_LIST_URL) + "?page=" + page + "&sort=" + sort + "&limit=" + 20;
		if (!TextUtils.isEmpty(groupId)) {
			URL = URL + "&groupId=" + groupId;
		}
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 搜索成员列表
	 *
	 * @param poContext
	 */
	public static void getSearchMenberList(Context poContext, String groupId, String nickname,
										   int page,
										   CallbackDataHandle callDataHandle) {
		String URL = null;
		if (TextUtils.isEmpty(nickname)) {
			URL = NetConstants.getFullRequestUrl(NetConstants.GET_GROUP_MENBER_LIST_URL) + "?groupId=" + groupId + "&page=" + page;
		} else {
			URL = NetConstants.getFullRequestUrl(NetConstants.SEARCH_MENBER_LIST_URL) + "?groupId=" + groupId + "&nickname=" + nickname + "&page="
					+ page;
		}
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 设置/取消为管理员
	 *
	 * @param flag true为设置为管理员，false为取消
	 */
	public static void addFanAdmin(Context poContext, String groupId, String uid, boolean flag,
								   CallbackDataHandle callDataHandle) {
		String URL = null;
		if (flag) {
			URL = NetConstants.getFullRequestUrl(NetConstants.FAN_ADD_ADMIN_URL) + "?groupId=" + groupId + "&uid=" + uid;
		} else {
			URL = NetConstants.getFullRequestUrl(NetConstants.FAN_REMOVE_ADMIN_URL) + "?groupId=" + groupId + "&uid=" + uid;
		}
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 退出饭圈
	 */
	public static void quitFan(Context poContext, String groupId, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.QUIT_FAN_URL) + "?groupId=" + groupId;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 取消或帖子加精
	 *
	 * @param flag true为帖子加精，false为取消帖子加精
	 */
	public static void addRecommentPost(Context poContext, String postId, boolean flag,
										CallbackDataHandle callDataHandle) {
		String URL = null;
		if (flag) {
			URL = NetConstants.getFullRequestUrl(NetConstants.ADD_RECOMMEND_URL) + "?postId=" + postId;
		} else {
			URL = NetConstants.getFullRequestUrl(NetConstants.CANCEL_RECOMMEND_URL) + "?postId=" + postId;
		}
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 取消或帖子置顶
	 *
	 * @param flag true为帖子置顶，false为取消帖子置顶
	 */
	public static void addTopPost(Context poContext, String postId,
								  boolean flag, CallbackDataHandle callDataHandle) {
		String URL = null;
		if (flag) {
			URL = NetConstants.getFullRequestUrl(NetConstants.SET_POST_TOP_URL) + "?postId=" + postId;
		} else {
			URL = NetConstants.getFullRequestUrl(NetConstants.CANCEL_POST_TOP_URL) + "?postId=" + postId;
		}
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	// /** 删除饭圈帖子 */
	// public static void removeFanPost(Context poContext, String postId,
	// CallbackDataHandle callDataHandle) {
	// String URL = NetConstants.REMOVE_FAN_POST_URL + "?id=" + postId;
	// AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle),
	// URL);
	// sendCookieHttps(poContext, aEntity);
	// }

	/**
	 * 创建或修改饭圈详情
	 *
	 * @param poContext
	 * @param anchorId:可选 name:名称 detai:详情 logo:图片
	 */
	public static void createFanDetail(Context poContext, String name, String detail, String
			logo, String anchorId,
									   CallbackDataHandle callDataHandle) {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("name", name);
		lmParams.put("detail", detail);
		if (!TextUtils.isEmpty(logo))
			lmParams.put("logo", logo);
		if (!TextUtils.isEmpty(anchorId))
			lmParams.put("mid", anchorId);

		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.CREATE_GROUP_INFO_URL));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);

	}

	/**
	 * 创建或修改饭圈详情
	 *
	 * @param poContext
	 * @param groupId   name detail logo:可选 background:可选
	 */
	public static void updateFanDetail(Context poContext, String name, String detail, String
			background, String logo,
									   String groupId, CallbackDataHandle callDataHandle) {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("name", name);
		lmParams.put("detail", detail);
		if (!TextUtils.isEmpty(background))
			lmParams.put("background", background);
		if (!TextUtils.isEmpty(logo))
			lmParams.put("logo", logo);
		if (!TextUtils.isEmpty(groupId))
			lmParams.put("groupId", groupId);

		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.UPDATE_GROUP_INFO_URL));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);

	}

	/**
	 * 获取邀请人信息
	 *
	 * @param poContext
	 */
	public static void getInviteInfo(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_INVATE_INFO_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 提交邀请人
	 *
	 * @param poContext
	 */
	public static void submitInvite(Context poContext, String referrer, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.SUBMIT_INVATE_URL) + "?referrer=" + referrer;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/*************************** 饭圈帖子接口 start **************************************/
	/**
	 * 获取忒子详情数据
	 *
	 * @param pid 帖子id
	 * @param fid 楼层id
	 */
	public static void getGroupPostDetail(Context poContext, String pid, String
			fid, CallbackDataHandle callDataHandle) {
		String URL;
		if (TextUtils.isEmpty(fid)) {
			URL = NetConstants.getFullRequestUrl(NetConstants.GET_GROUP_POST_DETAIL_URL) + "?postId=" + pid;
		} else {
			URL = NetConstants.getFullRequestUrl(NetConstants.GET_GROUP_POST_DETAIL_URL) + "?postId=" + pid + "&fReplyId=" + fid;
		}
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 回复帖子
	 *
	 * @param poContext
	 */
	public static void groupRelayPostInfo(Context poContext, CallbackDataHandle
			callDataHandle, String pid,
										  String content, String pic) {

		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("postId", pid);
		lmParams.put("content", content);
		if (!TextUtils.isEmpty(pic))
			lmParams.put("pic", pic);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.GROUP_REPLY_POST_URL));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 回复楼层
	 *
	 * @param poContext
	 */
	public static void groupRelayLoucengInfo(Context poContext, CallbackDataHandle
			callDataHandle, String toFReplyId,
											 String toReplyId, String toUid, String content) {

		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("lzlReplyId", toFReplyId);
		lmParams.put("fReplyId", toReplyId);
		lmParams.put("content", content);

		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.GROUP_REPLY_LOUCENG_URL));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 收藏帖子
	 *
	 * @param poContext
	 */
	public static void groupCollectPostInfo(Context poContext, CallbackDataHandle
			callDataHandle, String pid) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GROUP_COLLECT_POST_URL) + "?postId=" + pid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 取消收藏
	 */
	public static void removeGroupCollectPostInfo(Context poContext, CallbackDataHandle
			callDataHandle, String pid) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.REMOVE_GROUP_COLLECT_POST_URL) + "?postId=" + pid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取回复列表信息
	 *
	 * @param poContext
	 */
	public static void getGroupRelayListInfo(Context poContext, int page,
											 int limit, CallbackDataHandle callDataHandle,
											 int pid) {

		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_GROUP_REPLY_LIST_URL) + "?postId=" + pid + "&page=" + page + "&limit=" + limit;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 关注（赞）
	 *
	 * @param poContext
	 */
	public static void groupSupport(Context poContext, CallbackDataHandle callDataHandle,
									int pid) {

		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_GROUP_SUPPORT_URL) + "?postId=" + pid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 删除我回复的楼层信息
	 */
	public static void deleteGroupPostReply(Context poContext, String
			replyId, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.DELETE_GROUP_REPLY_URL) + "?fReplyId=" + replyId;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 删除我发表的帖子
	 */
	public static void deleteGroupPost(Context poContext, String postId, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.DELETE_GROUP_POST_URL) + "?postId=" + postId;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 发帖
	 */
	public static void publicGroupPostInfo(Context poContext, CallbackDataHandle
			callDataHandle, String groupId,
										   String content, ArrayList<String> imageFiles) {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("groupId", groupId);
		lmParams.put("content", content);
		// lmParams.put("picNum", String.valueOf(imageFiles == null ? 0 :
		// imageFiles.size()));

		for (int i = 0; i < imageFiles.size() && i < 1; i++) {
			lmParams.put("pic", imageFiles.get(i));
		}

		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.PUBLIC_GROUP_POST_URL));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);

	}

	/**
	 * 获取社区栏目帖子列表数据
	 */
	public static void getGroupSubjectListData(Context poContext, int page, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_GOURP_SUBJECT_LIST_URL) + "?page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取用户创建饭圈数
	 */
	public static void getCreateGroupNum(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_CREATE_NUM_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/********************** 2.0版本的新接口 ****************************************/

	/**
	 * 获取我关注的主播
	 */
	public static void getFocusAnchor(Context poContext, CallbackDataHandle callDataHandle,
									  int page) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_ME_FOCUS_ANCHOR_URL) + "?page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取直播预告信息
	 */
	public static void getPublishRoomInfo(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_PUBLISH_ROOM_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取精选帖子
	 */
	public static void getHotGroupPostList(Context poContext, int page, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_HOT_GROUP_POST_LIST_URL) + "?page=" + page + "&limit=" + 40;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取其他用户信息
	 *
	 * @param uid
	 */
	public static void getPersonInfoData(Context poContext, String uid, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_PERSON_INFO_URL) + "?uid=" + uid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 设置直播时间
	 */
	public static void setPlayTime(Context poContext, String time, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.SET_PLAY_TIME_URL) + "?time=" + time;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 删除直播时间
	 */
	public static void deletePlayTime(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.DELETE_PLAY_TIME_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/** ****************************2.1相关接口 ************************************/
	/**
	 * 圈子禁言
	 *
	 * @param flag    true为帖子禁言，false为解禁 禁言
	 * @param timeLen 时长为秒数 可传 3600, 3600 * 24, 3600 * 72, 0（0是永久禁言）
	 */
	public static void onGroupBanorUnBan(Context poContext, String groupId, String uid, String
			timeLen, boolean flag,
										 CallbackDataHandle callDataHandle) {
		String URL = null;
		Map<String, String> lmParams = new HashMap<String, String>();
		if (flag) {
			URL = NetConstants.getFullRequestUrl(NetConstants.USER_BAN_URL);
			lmParams.put("timeLen", timeLen);
		} else {
			URL = NetConstants.getFullRequestUrl(NetConstants.CANCEL_USER_BAN_URL);
		}
		lmParams.put("groupId", groupId);
		lmParams.put("uid", uid);

		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取饭圈管理日志信息
	 *
	 * @param poContext
	 */
	public static void getGroupManageLogList(Context poContext, String groupId, int page,
											 CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_GROUP_MANAGE_LOG_URL) + "?groupId=" + groupId + "&page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取主播报名信息
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void getAnchorStatus(Context poContext, CallbackDataHandle callDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.ANCHOR_STATUS));
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 提交主播报名信息
	 *
	 * @param poContext
	 * @param mobile
	 * @param qq
	 * @param video
	 * @param callDataHandle
	 */
	public static void submitAnchorInfo(Context poContext, String mobile, String qq, String
			note, String video,
										CallbackDataHandle callDataHandle) {
		Map<String, String> params = new HashMap<String, String>();
		String[] mobiles = mobile.split(" ");
		StringBuffer sb = new StringBuffer();
		for (String str : mobiles) {
			sb.append(str);
		}
		if (!TextUtils.isEmpty(sb))
			params.put("mobile", sb.toString());
		if (!TextUtils.isEmpty(qq))
			params.put("qq", qq);
		if (!TextUtils.isEmpty(video))
			params.put("video", video);
		params.put("note", note);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.SUBMIT_ANCHOR_INFO));
		aEntity.postDatas.putAll(params);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 绑定微信
	 */
	public static void getToBind(Context poContext, String psAccessToken, String
			psOpenId, String refreshToken,
								 String unionid, String psExpiredIn, CallbackDataHandle callDataHandle) {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("accessToken", psAccessToken);
		lmParams.put("refreshToken", refreshToken);
		lmParams.put("openId", psOpenId);
		lmParams.put("unionid", unionid);
		lmParams.put("expiredIn", psExpiredIn);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.GET_BIND_WEIXIN_URL));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 上传房间封面
	 */
	public static void editRoomLogo(Context poContext, CallbackDataHandle
			callDataHandle, String rid, String imageFile) {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("rid", rid);
		lmParams.put("logo", imageFile);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.EDIT_ROOM_LOGO_URL));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);

	}
	/** ****************************2.3新增接口 ************************************/

	/**
	 * 上传直播标题
	 */
	public static void editRoomTitle(Context poContext, CallbackDataHandle
			callDataHandle, String rid, String title, String location, String tagIds) {
		Map<String, String> lmParams = new HashMap<>();
		lmParams.put("rid", rid);
		lmParams.put("announcement", title);
		if (!TextUtils.isEmpty(location)) {
			lmParams.put("location", location);
		}
		if (!TextUtils.isEmpty(tagIds)) {
			lmParams.put("tagIds", tagIds);
		}
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.SUBMIT_LIVE_TITLE));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取直播间泡泡排行榜
	 *
	 * @param poContext
	 * @param callDataHandle
	 * @param rid            直播房间号
	 * @param rankType       排行榜单类型：周榜、总榜 all/week
	 * @param page
	 */
	public static void getLivePRank(Context poContext, CallbackDataHandle
			callDataHandle, String rid, String rankType, int page) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_LIVE_P_RANK) + "?rid=" + rid + "&type=" + rankType + "&page=" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/******************************
	 * 2.4新增接口
	 ************************************/
	public static void getLiveStatus(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_LIVE_STATUS);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}
	/******************************* 2.5.0新增接口 ************************************/
	/**
	 * 修改用户信息,屏蔽性别修改
	 *
	 * @param poContext
	 * @param callDataHandle
	 * @param bgImg
	 */
	public static void uploadUserBg(Context poContext, CallbackDataHandle callDataHandle, String bgImg) {
		Map<String, String> lmParams = new HashMap<String, String>();
		if (!TextUtils.isEmpty(bgImg))
			lmParams.put("bgImg", bgImg);

		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.UPLOAD_USER_BG_IMG));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);

	}

	/**
	 * 获取话题列表
	 *
	 * @param poContext
	 * @param callDataHandle
	 * @param page
	 */
	public static void getTopics(Context poContext, CallbackDataHandle callDataHandle, int page, int limit) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_TOPICS) + "/limit/" + limit + "/page/" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);

	}

	/******************************* 2.6.0新增接口 ************************************/
	/**
	 * 获取话题列表
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void agreeProtocal(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.AGREE_PROTOCAL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);

	}

	/**
	 * 获取用户黑名单列表数据
	 */
	public static void getBlackList(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_BLACKLIST_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取会话列表指定用户的数据
	 *
	 * @param poContext
	 * @param userIds   需要查询用户信息的uid列表
	 */
	public static void getMessageUserInfo(Context poContext, List<String> userIds, CallbackDataHandle
			callDataHandle) {
		String uidStr = StringUtil.strListToString(userIds);
		Map<String, String> lmParams = new HashMap<>();
		lmParams.put("fromUids", uidStr);
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_MESSAGE_USER_INFO_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 上报设备ID
	 *
	 * @param poContext
	 * @param deviceId  设备Id
	 */

	public static void reportDeviceId(Context poContext, String deviceId, String macAddress, String imei) {
		try {
			deviceId = Utils.rsaEncrypt(
					(String) FeizaoApp.getCacheData("public_key"), deviceId);
			macAddress = Utils.rsaEncrypt(
					(String) FeizaoApp.getCacheData("public_key"), macAddress);
			imei = Utils.rsaEncrypt(
					(String) FeizaoApp.getCacheData("public_key"), imei);
			Map<String, String> lmParams = new HashMap<>();
			lmParams.put("deviceId", deviceId);
			lmParams.put("mac", macAddress);
			lmParams.put("imei", imei);
			String URL = NetConstants.getFullRequestUrl(NetConstants.REPORT_DEVICE_ID_URL);
			AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(null), URL);
			aEntity.postDatas.putAll(lmParams);
			sendCookieHttps(poContext, aEntity);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * app 初始化回调
	 *
	 * @param poContext
	 * @param callbackDataHandle
	 */
	public static void appInit(Context poContext, CallbackDataHandle callbackDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callbackDataHandle), NetConstants.getFullRequestUrl(NetConstants.APP_INIT));
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 上报同盾黑盒
	 *
	 * @param poContext
	 * @param reportBlackBox blackbox
	 */
	public static void reportBlackBox(Context poContext, String reportBlackBox) {
		try {
			String md5 = MD5.getMessageDigest(reportBlackBox.getBytes("UTF-8"));
			md5 = Utils.rsaEncrypt(
					(String) FeizaoApp.getCacheData("public_key"), md5);
			Map<String, String> lmParams = new HashMap<>();
			lmParams.put("tdDeviceInfo", reportBlackBox);
			lmParams.put("verify", md5);
			AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(null), NetConstants.getFullRequestUrl(NetConstants.REPORT_BLACK_BOX_URL));
			aEntity.postDatas.putAll(lmParams);
			sendCookieHttps(poContext, aEntity);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/*************************** 饭圈帖子接口 end **************************************/

	/**
	 * 获取用户黑名单列表数据
	 */
	public static void getBlackList(Context context, CallbackDataHandle callDataHandle, int page) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_BLACKLIST_URL) + "/page/" + page;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(context, aEntity);
	}

	public static void reportRegisterId(Context context, CallbackDataHandle callDataHandle, String registerId) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.JPUSH_REGISTER_ID);
		Map<String, String> lmParams = new HashMap<>();
		lmParams.put("registerationId", registerId);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(context, aEntity);
	}

	/**
	 * 获取房管列表
	 */
	public static void getManagerList(Context context, CallbackDataHandle callDataHandle, String uid) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_ROOM_MANAGER_LIST) + "?mid=" + uid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(context, aEntity);
	}

	/**
	 * 移除管理员
	 */
	public static void removeRoomManager(Context context, CallbackDataHandle callDataHandle, String adimId) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.REMOVE_ROOM_MANAGER) + "?unsetAdminUid=" + adimId;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(context, aEntity);
	}

	/**
	 * 設置管理員
	 *
	 * @param context
	 * @param callDataHandle
	 * @param adimId
	 */
	public static void setRoomManager(Context context, CallbackDataHandle callDataHandle, String adimId) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.SET_ROOM_MANAGER) + "?setAdminUid=" + adimId;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(context, aEntity);
	}

	/**
	 * 获取推荐关注列表
	 */
	public static void getRecommendAttentions(Context poContext, CallbackDataHandle callDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.RECOMMEND_ATTENTIONS));
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取推荐关注列表
	 */
	public static void commitRecommendAttentions(Context poContext, List<String> uids, CallbackDataHandle callDataHandle) {
		String uidStr = StringUtil.strListToString(uids);
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("attentionUids", uidStr);
		String URL = NetConstants.getFullRequestUrl(NetConstants.COMMIT_RECOMMEND_ATTENTIONS);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取物品信息
	 *
	 * @param poContext
	 */
	public static void getUserPackageInfo(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_USER_PACKAGE_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);

	}

	/**
	 * 获取推荐关注列表
	 */
	public static void comeInAnimSet(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.COME_IN_ANIM_SWITCH);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 注册获取手机验证码
	 */
	public static void getPhoneBindCode(Context poContext, String mobile, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_PHONE_BIND_CODE_URL);
		try {
			//手机加密
			AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
			aEntity.postDatas.put("mobile", Utils.rsaEncrypt(
					(String) FeizaoApp.getCacheData("public_key"), mobile));
			sendCookieHttps(poContext, aEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 账号绑定手机号
	 */
	public static void phoneBind(Context poContext, String vcode, String password, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.PHONE_BIND_URL);
		try {
			AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
			aEntity.postDatas.put("vcode", vcode);
			aEntity.postDatas.put("password", Utils.rsaEncrypt(
					(String) FeizaoApp.getCacheData("public_key"), password));
			sendCookieHttps(poContext, aEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取物品信息
	 *
	 * @param poContext
	 */
	public static void getModeratorGuardInfo(Context poContext, String mid, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_MODERATOR_GUARD_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		aEntity.postDatas.put("mid", mid);

		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取热门排名信息
	 */

	public static void getHotRankInfo(Context poContext, String mid, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_HOT_RANK_INFO_URL) + "?mid=" + mid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);

	}

	/**
	 * 获取官方推荐信息
	 */

	public static void getOffcialRecommendInfo(Context poContext, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_OFFICIAL_RECOMMEND_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);

	}

	/**
	 * 群发或单发,发送数据给服务器
	 */

	public static void sendPrivateMessageMulti(Context poContext, String uid, String type, String content, String pic, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.SEND_PRIVATE_MESSAGE_INFO);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		Map<String, String> lmParams = new HashMap<>();
		lmParams.put("type", type);
		lmParams.put("toUids", uid);
		if (!TextUtils.isEmpty(content)) {
			lmParams.put("content", content);
		}
		if (!TextUtils.isEmpty(pic)) {
			lmParams.put("pic", pic);
		}
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);

	}

	/**
	 * 提交支付宝实名验证信息
	 *
	 * @param poContext
	 */
	public static void aliUserAuth(Context poContext, String authCode, CallbackDataHandle callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.SET_ALIPLAY_AUTH_CODE_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		aEntity.postDatas.put("code", authCode);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取支付宝登陆授权参数
	 */
	public static void getAliPayLoginData(Context poContext, CallbackDataHandle callDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.GET_ALIPLAY_LOGIN_URL));
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取房间个人卡用户信息
	 *
	 * @param uid
	 * @param mid
	 */
	public static void getRoomUserInfoData(Context poContext, String mid, String uid, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.GET_ROOM_USER_INFO_URL);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		aEntity.postDatas.put("mid", mid);
		aEntity.postDatas.put("uid", uid);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 统计用户操作
	 */
	public static void reportUserAction(Context poContext, String data, String date, CallbackDataHandle
			callDataHandle) {
		String URL = WebConstants.REPORT_USER_ACTION;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		aEntity.headers.put("Token", String.valueOf(date));//时间戳
		aEntity.jsonStr = data;        //json
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 使用私信卡
	 */
	public static void userMessageCard(Context poContext, String uid, CallbackDataHandle
			callDataHandle) {
		String URL = NetConstants.getFullRequestUrl(NetConstants.USER_MESSAGE_CARD) + "/toUid/" + uid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), URL);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 邀请私播
	 */
	public static void requestPrivateLive(Context poContext, String invitedUid, CallbackDataHandle callDataHandle) {
		String url = NetConstants.getFullRequestUrl(NetConstants.PRIVATE_LIVE) + "?invitedUid=" + invitedUid;
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), url);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取守护礼物
	 */
	public static void getGuardGifts(Context poContext, String uid, CallbackDataHandle
			callDataHandle) {
		Map<String, String> lmParams = new HashMap<>();
		lmParams.put("mid", uid);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.GET_GUARD_GIFT));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 提交相册顺序更改
	 *
	 * @param pids 排序后的id数组
	 */
	public static void sortAlbumList(Context poContext, List<Integer> pids, CallbackDataHandle
			callDataHandle) {
		Map<String, String> lmParams = new HashMap<>();
		lmParams.put("pids", JacksonUtil.toJSon(pids));
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.SORT_ALBUM));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 提交相册顺序更改
	 *
	 * @param pids 需要删除的id列表
	 */
	public static void delAlbum(Context poContext, List<Integer> pids, CallbackDataHandle
			callDataHandle) {
		Map<String, String> lmParams = new HashMap<>();
		lmParams.put("pids", JacksonUtil.toJSon(pids));
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.DELETE_ALBUM));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 上传相册图片
	 *
	 * @param url 图片地址
	 */
	public static void uploadAlbum(Context poContext, String url, CallbackDataHandle
			callDataHandle) {
		Map<String, String> lmParams = new HashMap<>();
		lmParams.put("pic", url);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.UPLOAD_ALBUM));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 上传相册图片
	 *
	 * @param uid 用户id
	 */
	public static void getAlbums(Context poContext, String uid, CallbackDataHandle
			callDataHandle) {
		Map<String, String> lmParams = new HashMap<>();
		lmParams.put("uid", uid);
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.GET_ALBUM));
		aEntity.postDatas.putAll(lmParams);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取http请求域名
	 */
	public static void getHttpDomain(Context poContext, CallbackDataHandle
			callDataHandle) {
		String url = "http://" + Consts.BASE_HTTP_DOMAIN + "/app/getAddr";
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), url);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 发起连线邀请
	 *
	 * @param poContext
	 * @param callDataHandle
	 * @param rid            type(1是普通用户发起 2是主播发起)   uid(主播发起时需要传被邀请的用户id)  chatType(用户发起时需要传1视频 2音频)
	 */
	public static void requestVideoChat(Context poContext, CallbackDataHandle
			callDataHandle, String rid, String type, String uid, String chatType) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.REQUEST_VIDEO_CHAT));
		aEntity.postDatas.put("rid", rid);
		aEntity.postDatas.put("type", type);
		if (!TextUtils.isEmpty(uid)) {
			aEntity.postDatas.put("uid", uid);
		}
		aEntity.postDatas.put("chatType", chatType);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 取消连线邀请
	 *
	 * @param poContext
	 * @param callDataHandle
	 * @param rid            rid type(1是普通用户发起 2是主播发起)    uid(主播侧取消需要对应的用户id)
	 * @param type
	 * @param uid
	 */
	public static void cancelVideoChat(Context poContext, CallbackDataHandle
			callDataHandle, String rid, String type, String uid) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.CANCEL_VIDEO_CHAT));
		aEntity.postDatas.put("rid", rid);
		aEntity.postDatas.put("type", type);
		if (!TextUtils.isEmpty(uid)) {
			aEntity.postDatas.put("uid", uid);
		}
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 用户忽略主播连线邀请
	 *
	 * @param poContext
	 * @param callDataHandle
	 * @param rid
	 */
	public static void rejectVideoChat(Context poContext, CallbackDataHandle
			callDataHandle, String rid) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.REJECT_VIDEO_CHAT));
		aEntity.postDatas.put("rid", rid);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 接受连线邀请
	 *
	 * @param poContext
	 * @param callDataHandle
	 * @param rid            rid type(1是普通用户接受主播邀请 2是主播接受用户邀请)  chatType(用户接受主播邀请时需传1视频 2音频)  uid(主播接受用户邀请需要传对应的用户id)
	 * @param type
	 * @param uid
	 * @param chatType
	 */
	public static void acceptVideoChat(Context poContext, CallbackDataHandle
			callDataHandle, String rid, String type, String uid, String chatType) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.ACCEPT_VIDEO_CHAT));
		aEntity.postDatas.put("rid", rid);
		aEntity.postDatas.put("type", type);
		if (!TextUtils.isEmpty(uid)) {
			aEntity.postDatas.put("uid", uid);
		}
		aEntity.postDatas.put("chatType", chatType);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 混流请求
	 *
	 * @param poContext
	 * @param callDataHandle
	 * @param rid
	 */
	public static void mixStream(Context poContext, CallbackDataHandle
			callDataHandle, String rid) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.REQUEST_MIX_STREAM));
		aEntity.postDatas.put("rid", rid);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 结束连线
	 *
	 * @param poContext
	 * @param callDataHandle
	 * @param rid            rid   type(1是用户取消  2是主播取消)   uid(主播结束时需要传)
	 * @param type
	 * @param uid
	 */
	public static void endVideoChat(Context poContext, CallbackDataHandle
			callDataHandle, String rid, String type, String uid) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.REQUEST_END_VIDEO_CHAT));
		aEntity.postDatas.put("rid", rid);
		aEntity.postDatas.put("type", type);
		if (!TextUtils.isEmpty(uid)) {
			aEntity.postDatas.put("uid", uid);
		}
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取红包信息
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void getRedPacketCD(Context poContext, String rid, CallbackDataHandle
			callDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.GET_RED_PACKET_CD));
		if (!TextUtils.isEmpty(rid)) {
			aEntity.postDatas.put("rid", rid);
		}
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 获取红包信息
	 *
	 * @param poContext
	 * @param callDataHandle
	 */
	public static void getRedPacket(Context poContext, String rid, CallbackDataHandle
			callDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.GET_RED_PACKET));
		aEntity.postDatas.put("rid", rid);
		sendCookieHttps(poContext, aEntity);
	}

	/**
	 * 上传用户信息
	 */
	public static void postUserPosition(Context context, String lng, String lat, CallbackDataHandle callDataHandle) {
		AEntity aEntity = new BaseEntityImpl(new IReceiverImpl(callDataHandle), NetConstants.getFullRequestUrl(NetConstants.POST_POSITION));
		aEntity.postDatas.put("lng", lng);
		aEntity.postDatas.put("lat", lat);
		sendCookieHttps(context, aEntity);
	}

	/**
	 * 网络请求,带cookie信息,Post请求方式
	 *
	 * @param context
	 * @param aEntity
	 */
	private static void sendCookieHttps(Context context, AEntity aEntity) {
		final PostCommunicationCookie communication = new PostCommunicationCookie(context.getApplicationContext(), aEntity);
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				communication.sendEntity();
			}
		});
	}

	/**
	 * 网络请求,Post请求方式
	 *
	 * @param aEntity
	 */
	private static void sendHttps(AEntity aEntity) {
		final PostCommunication communication = new PostCommunication(aEntity);
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				communication.sendEntity();
			}
		});
	}
}
