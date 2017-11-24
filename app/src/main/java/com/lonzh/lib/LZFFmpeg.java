package com.lonzh.lib;

import java.lang.ref.WeakReference;

import android.view.Surface;

import tv.live.bx.library.util.EvtLog;

/**
 * -1:未登陆用户 1:普通用户 2:主播 3:管理员 4:僵尸用户 5:房间主
 */
public class LZFFmpeg {

	static {
		System.loadLibrary("avutil-52");
		System.loadLibrary("avcodec-55");
		System.loadLibrary("avformat-55");
		System.loadLibrary("swscale-2");
		System.loadLibrary("swresample-0");
		System.loadLibrary("rtmp");
		System.loadLibrary("feizao");
	}
	private static WeakReference<IFFmpegCallback> iCallback;
	private static String TAG = "LZFFmpeg";

	/**
	 * 初始化的时候设置IFFmpegCallback，退出页面设置为null
	 */
	public static void setIFFmpegCallback(IFFmpegCallback iCallback) {
		LZFFmpeg.iCallback = new WeakReference<LZFFmpeg.IFFmpegCallback>(iCallback);
	}

	/************************** Native Methods ***************************/
	// public static native int naInitRtmp(String psUrl);
	//
	// public static native int naSendMsg(String psTo, String psMsg,
	// boolean pbIsPrivate);
	//
	// public static native int naInitFFmpeg();
	//
	// public static native int[] naGetVideoRes();
	//
	// public static native void naSetSurface(Surface pSurface);
	//
	// public static native int naSetup(int pWidth, int pHeight);
	//
	// public static native void naPlay();
	//
	// public static native void naStop();

	public static native void naWriteBuf(byte[] pBuf, int piSize);

	public static native void startMainThread(String psRtmpUrl, String psAnoRtmpUrl);

	public static native void stopMainThread();

	public static native void setSurface(Surface poSurface, int piWidth, int piHeight);

	public static native void pausePlaying();

	public static native void sendMsg(String psTo, String psMsg, boolean pbIsPrivate);

	public static native void sendGift(int piTo, int piGiftId, int piCount);

	/************************** Callback Methods ***************************/
	// /**
	// * 房间信息观众（这些接口都是底层库回调）
	// */
	// public static void initRoom(int piUid, int piType, int piSex, String
	// psNickname, String psPhoto, String psVipStart,
	// String psVipEnd) {
	// EvtLog.d(TAG, "initRoom " + iCallback);
	// if (iCallback != null)
	// iCallback.initRoom(piUid, piType, psNickname, psPhoto, psVipStart,
	// psVipEnd);
	// }
	//
	// /**
	// * 添加观众（这些接口都是底层库回调）
	// */
	// public static void addUser(int piUid, int piType, int piSex, String
	// psNickname, String psPhoto, String psVipStart,
	// String psVipEnd) {
	// EvtLog.d(TAG, "addUser " + piUid);
	// if (iCallback != null)
	// iCallback.addUser(piUid, piType, psNickname, psPhoto, psVipStart,
	// psVipEnd);
	// }
	//
	// /**
	// * 剔除观众（这些接口都是底层库回调）
	// */
	// public static void onLogout(int piUid, int piType) {
	// EvtLog.d(TAG, "onLogout " + piUid);
	// if (iCallback != null)
	// iCallback.delUser(piUid, piType);
	// }
	//
	// /**
	// * 聊天（这些接口都是底层库回调） {100 : '该用户不在聊天室', 103 : '您已被禁言', 104 : '未登录', 105 :
	// * '发言频率过于频繁，请稍后再试', 106 : '短时间内不能发相同内容', 107 : '悲剧～～您被管理员禁言了T.T', 108 :
	// * '送花过于频繁', 109 : '今日累计送花超过上限'};
	// */
	// public static void onChatMsg(int piErrCode, int piFrom, int piTo, String
	// psFromNickname, String psToNickname,
	// String psFromPhoto, String psToPhoto, String psMsg, int piPrivate) {
	// EvtLog.d(TAG, "onChatMsg " + piErrCode);
	// if (iCallback != null)
	// iCallback.onChatMsg(piErrCode, piFrom, piTo, psFromNickname,
	// psToNickname, psFromPhoto, psMsg, piPrivate);
	// }
	//
	// /**
	// * 送礼物（这些接口都是底层库回调） {200 : '该用户不在聊天室', 201 : '验证登录失败，请重新登录', 202 :
	// * '获取用户数据失败', 203 : '货币不足，请充值'};
	// */
	// public static void onGift(int piErrCode, int piGiftId, int piGiftCount,
	// int piFrom, int piTo,
	// String psFromNickname, String psToNickname, String psFromPhoto, String
	// psToPhoto, String psGiftName,
	// String psGiftImg) {
	// EvtLog.d(TAG, "onGift " + piErrCode);
	// if (iCallback != null) {
	// iCallback.onGift(piErrCode, piGiftId, piGiftCount, piFrom, piTo,
	// psFromNickname, psToNickname, psFromPhoto,
	// psToPhoto, psGiftName, psGiftImg);
	// }
	//
	// }
	//
	// /**
	// * 视频流连接状态 NetConnection.Connect.Success NetConnection.Connect.Failed
	// * NetConnection.Connect.Closed
	// */
	// public static void connectStatus(String statusCode) {
	// EvtLog.d(TAG, "connectStatus " + statusCode);
	// // if (iCallback != null) {
	// // iCallback.connectStatus(statusCode);
	// // }
	//
	// }

	/**
	 * 视频加载成功（这些接口都是底层库回调）
	 * @param isSuccess 是否视频加载成功,1成功,0失败
	 */
	public static void videoLoaded(int isSuccess) {
		EvtLog.d(TAG, "videoLoaded " + isSuccess);
		if (iCallback.get() != null) {
			iCallback.get().videoLoaded(isSuccess);
		}
	}

	/**
	 * 与服务器断开连接(主要是网络断开等原因)（这些接口都是底层库回调）
	 */
	public static void disConnect() {
		EvtLog.d(TAG, "disConnect " + iCallback);
		if (iCallback.get() != null) {
			iCallback.get().disConnect();
		}
	}
	
	/**
	 * 与服务器断开连接(主要是网络断开等原因)（这些接口都是底层库回调）
	 */
	public static void onResult(String result) {
		EvtLog.d(TAG, "onResult " + result);
		if (iCallback.get() != null) {
			iCallback.get().onResult(result);
		}
	}
	
	/**
	 * 播放库回调之后的操作函数 ClassName: IFFmpegCallback <br/>
	 */
	public interface IFFmpegCallback {

		/** 以下三个方法是通过播放库回调 */
		void videoLoaded(int isSuccess);

		void disConnect();

		void onResult(String result);

	}

}
