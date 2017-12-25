package com.bixin.bixin.database;

import android.content.Context;

import com.bixin.bixin.App;
import com.bixin.bixin.common.Consts;
import com.bixin.bixin.database.model.AnchorInfo;
import com.bixin.bixin.database.model.BannerInfo;
import com.bixin.bixin.database.model.PersonInfo;
import com.bixin.bixin.database.model.PostMoudleInfo;
import com.bixin.bixin.database.model.SubjectInfo;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.live.activities.LiveBaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Title: DatabaseUtils.java Description: 数据库操作工具类 Copyright: Copyright (c) 2008
 *
 * @version 1.0
 * @CreateDate 2013-12-5 下午3:44:49
 */
public class DatabaseUtils {

	static final String TAG = "DatabaseUtils";

	/**
	 * 获取本地保存的主播列表数据显示到页面
	 */
	public static List<Map<String, Object>> getListAnchorInfos() {

		List<AnchorInfo> mAnchorInfos = DatabaseManager.getAllAnchorInfos(App.mContext);
		EvtLog.d(TAG, "getListAnchorInfos size:" + mAnchorInfos.size());

		List<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
		Iterator<AnchorInfo> iterators = mAnchorInfos.iterator();
		while (iterators.hasNext()) {
			AnchorInfo anchorInfo = iterators.next();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", anchorInfo.getRoomId());
			map.put("logo", anchorInfo.getLogoUrl());
			map.put("love", anchorInfo.getLoveNum());
			map.put("onlineNum", anchorInfo.getAudienceNum());

			map.put("moderator_desc", anchorInfo.getModeratorDesc());
			map.put("isPlaying", anchorInfo.isPlaying());

			Map<String, String> lmPlayer = new HashMap<String, String>();
			lmPlayer.put("true_name", anchorInfo.getTrueName());
			lmPlayer.put("age", anchorInfo.getmAge());
			lmPlayer.put("height", anchorInfo.getmHeight());
			lmPlayer.put("weight", anchorInfo.getmWeight());
			lmPlayer.put("mod_level", anchorInfo.getmAnchorLevel());

			map.put("moderator", lmPlayer);

			mData.add(map);
		}
		return mData;
	}

	/**
	 * 保存主播列表数据
	 */
	public static synchronized void saveListAnchorInfos(List<Map<String, Object>> mData) {
		EvtLog.d(TAG, "saveListAnchorInfos size:" + mData.size());
		// 缓存第一页数据到本地
		Iterator<Map<String, Object>> iterator = mData.iterator();
		List<AnchorInfo> inchorInfos = new ArrayList<AnchorInfo>();
		while (iterator.hasNext()) {
			AnchorInfo info = new AnchorInfo();
			Map<String, Object> map = iterator.next();
			info.setRoomId((String) map.get("id"));
			info.setLogoUrl((String) map.get("logo"));
			info.setLoveNum((String) map.get("love"));
			info.setAudienceNum((String) map.get("onlineNum"));

			info.setModeratorDesc((String) map.get("moderator_desc"));
			info.setPlaying((String) map.get("isPlaying"));

			Map<String, String> lmPlayer = (Map<String, String>) map.get("moderator");
			info.setTrueName(lmPlayer.get("true_name"));
			info.setmAge(lmPlayer.get("age"));
			info.setmHeight(lmPlayer.get("height"));
			info.setmWeight(lmPlayer.get("weight"));
			info.setmAnchorLevel(lmPlayer.get("mod_level"));
			inchorInfos.add(info);
		}
		DatabaseManager.saveOrupdateListAnchorinfoToDatabase(App.mContext, inchorInfos);
	}

	/**
	 * 获取本地保存的Banner显示到页面
	 */
	public static List<Map<String, String>> getListBannerInfos() {

		List<BannerInfo> mBannerInfos = DatabaseManager.getAllBannerInfos(App.mContext);
		EvtLog.d(TAG, "getListBannerInfos size:" + mBannerInfos.size());

		List<Map<String, String>> mData = new ArrayList<Map<String, String>>();
		Iterator<BannerInfo> iterators = mBannerInfos.iterator();
		while (iterators.hasNext()) {
			BannerInfo bannerInfo = iterators.next();
			Map<String, String> map = new HashMap<String, String>();
			map.put("pic", bannerInfo.getPicUrl());
			map.put("type", bannerInfo.getType());

			int liBannerType = Integer.parseInt(bannerInfo.getType());
			if (liBannerType == Consts.BANNER_URL_TYPE_PAGE) {
				map.put("banner_info", bannerInfo.getUrl());
			} else {
				map.put(LiveBaseActivity.ANCHOR_RID, bannerInfo.getRoomId());
			}
			mData.add(map);
		}
		return mData;
	}

	/**
	 * 保存Banner列表数据
	 */
	public static synchronized void saveListBannerInfos(List<Map<String, String>> mData) {
		EvtLog.d(TAG, "saveListBannerInfos size:" + mData.size());

		Iterator<Map<String, String>> iterators = mData.iterator();
		ArrayList<BannerInfo> mBannerInfos = new ArrayList<>();
		while (iterators.hasNext()) {
			Map<String, String> map = iterators.next();
			BannerInfo bannerInfo = new BannerInfo();
			bannerInfo.setPicUrl(map.get("pic"));
			bannerInfo.setType(map.get("type"));

			int liBannerType = Integer.parseInt(map.get("type"));
			if (liBannerType == Consts.BANNER_URL_TYPE_PAGE) {
				bannerInfo.setUrl(map.get("banner_info"));
			} else {
				bannerInfo.setRoomId(map.get(LiveBaseActivity.ANCHOR_RID));
			}
			mBannerInfos.add(bannerInfo);
		}

		DatabaseManager.saveOrupdateListBannerinfoToDatabase(App.mContext, mBannerInfos);
	}

	/**
	 * 获取本地保存的帖子列表数据显示到页面
	 */
	public static List<Map<String, Object>> getListSubjectInfos(String forumId, String isHot) {

		List<SubjectInfo> subjectInfos = DatabaseManager.getForumsSubjectInfos(App.mContext,
				SubjectInfo.NO_USRID, forumId, isHot);
		EvtLog.d(TAG, "getListSubjectInfos size:" + subjectInfos.size());

		List<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
		Iterator<SubjectInfo> iterators = subjectInfos.iterator();
		while (iterators.hasNext()) {

			SubjectInfo subjectInfo = iterators.next();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", subjectInfo.getSubjectId());
			map.put("uid", subjectInfo.getSubjectUserId());
			map.put("is_top", subjectInfo.getIsTop());
			map.put("support", subjectInfo.getLoveNum());

			map.put("nickname", subjectInfo.getPosterName());
			map.put("reply_count", subjectInfo.getRelayNum());
			map.put("user_level", subjectInfo.getUserLevel());
			map.put("content", subjectInfo.getSubjectContent());
			map.put("title", subjectInfo.getSubjectTitle());
			map.put("images", subjectInfo.getSubjectPhotos());
			map.put("last_reply_time", subjectInfo.getSubjectTime());
			map.put("isSupported", subjectInfo.getIsSupport());
			map.put("forum_title", subjectInfo.getForumTitle());
			map.put("forum_id", subjectInfo.getForumId());
			mData.add(map);
		}
		return mData;
	}

	/**
	 * 社区列表，保存帖子列表数据
	 *
	 * @param mData   需要保存的列表数据
	 * @param forumId 0是指“全部”,
	 * @param isHot   0是最新排序，1是热门排序
	 */
	public static synchronized void saveListSubjectInfos(List<Map<String, Object>> mData, String forumId, String isHot) {
		EvtLog.d(TAG, "saveListSubjectInfos size:" + mData.size());
		// 缓存第一页数据到本地
		Iterator<Map<String, Object>> iterator = mData.iterator();
		List<SubjectInfo> subjectInfos = new ArrayList<SubjectInfo>();
		while (iterator.hasNext()) {
			SubjectInfo info = new SubjectInfo();
			Map<String, Object> map = iterator.next();
			info.setSubjectId((String) map.get("id"));
			info.setSubjectUserId((String) map.get("uid"));

			info.setHeadPicUrl((String) map.get("headPic"));
			info.setIsTop((String) map.get("is_top"));
			info.setLoveNum((String) map.get("support"));
			info.setPosterName((String) map.get("nickname"));
			info.setUserLevel((String) map.get("user_level"));
			info.setRelayNum((String) map.get("reply_count"));
			info.setSubjectContent((String) map.get("content"));
			info.setSubjectTitle((String) map.get("title"));
			info.setSubjectPhotos((String) map.get("images"));
			info.setSubjectTime((String) map.get("last_reply_time"));
			info.setIsSupport(map.get("isSupported").toString());
			info.setForumId(forumId);
			info.setForumTitle((String) map.get("forum_title"));
			info.setIsHot(isHot);

			subjectInfos.add(info);
		}
		DatabaseManager.saveOrupdateListSujectInfoToDatabase(App.mContext, SubjectInfo.NO_USRID,
				SubjectInfo.NO_COLLECTED, SubjectInfo.NO_PUBLISHED, forumId, isHot, subjectInfos);

	}

	/**
	 * 获取用户收藏\发表的帖子
	 *
	 * @param uId 用户Id
	 */
	public static List<Map<String, Object>> getCollectListSubjectInfos(String uId, String isCollect, String isPublish) {
		List<SubjectInfo> subjectInfos = DatabaseManager.getAllSubjectInfos(App.mContext, uId, isCollect,
				isPublish);
		EvtLog.d(TAG, "getListSubjectInfos size:" + subjectInfos.size());

		List<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
		Iterator<SubjectInfo> iterators = subjectInfos.iterator();
		while (iterators.hasNext()) {

			SubjectInfo subjectInfo = iterators.next();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", subjectInfo.getSubjectId());
			map.put("uid", subjectInfo.getSubjectUserId());
			map.put("is_top", subjectInfo.getIsTop());
			map.put("support", subjectInfo.getLoveNum());

			map.put("nickname", subjectInfo.getPosterName());
			map.put("reply_count", subjectInfo.getRelayNum());

			map.put("title", subjectInfo.getSubjectTitle());
			map.put("content", subjectInfo.getSubjectContent());
			map.put("images", subjectInfo.getSubjectPhotos());
			map.put("last_reply_time", subjectInfo.getSubjectTime());
			map.put("isSupported", subjectInfo.getIsSupport());

			mData.add(map);
		}
		return mData;
	}

	/**
	 * 保存收藏\发表的帖子列表数据
	 */
	public static synchronized void saveCollectListSubjectInfos(List<Map<String, Object>> mData, String uId,
																String isCollect, String isPublish) {
		EvtLog.d(TAG, "saveListSubjectInfos size:" + mData.size());
		// 缓存第一页数据到本地
		Iterator<Map<String, Object>> iterator = mData.iterator();
		List<SubjectInfo> subjectInfos = new ArrayList<SubjectInfo>();
		while (iterator.hasNext()) {
			SubjectInfo info = new SubjectInfo();
			Map<String, Object> map = iterator.next();
			info.setSubjectId((String) map.get("id"));
			info.setSubjectUserId((String) map.get("uid"));

			info.setHeadPicUrl((String) map.get("headPic"));
			info.setIsTop((String) map.get("is_top"));
			info.setLoveNum((String) map.get("support"));
			info.setPosterName((String) map.get("nickname"));
			info.setRelayNum((String) map.get("reply_count"));
			info.setSubjectContent((String) map.get("content"));
			info.setSubjectTitle((String) map.get("title"));
			info.setSubjectPhotos((String) map.get("images"));
			info.setSubjectTime((String) map.get("last_reply_time"));
			info.setIsSupport(map.get("isSupported").toString());
			info.setuId(uId);
			info.setIsCollect(isCollect);
			info.setIsPublish(isPublish);
			subjectInfos.add(info);
		}
		DatabaseManager.saveOrupdateListSujectInfoToDatabase(App.mContext, uId, isCollect, isPublish,
				subjectInfos);

	}

	/**
	 * 获取帖子模块
	 */
	public static List<Map<String, String>> getListPostMoudleInfos() {

		List<PostMoudleInfo> mPostMoudleInfos = DatabaseManager.getAllPostMoudleInfos(App.mContext);
		EvtLog.d(TAG, "getListPostMoudleInfos size:" + mPostMoudleInfos.size());

		List<Map<String, String>> mData = new ArrayList<Map<String, String>>();
		Iterator<PostMoudleInfo> iterators = mPostMoudleInfos.iterator();
		while (iterators.hasNext()) {
			PostMoudleInfo postMoudleInfo = iterators.next();
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", postMoudleInfo.getMoudleId());
			map.put("title", postMoudleInfo.getName());
			map.put("post_count", postMoudleInfo.getPostCount());
			map.put("reply_count", postMoudleInfo.getReplyCount());
			map.put("list_icon", postMoudleInfo.getListIcon());
			map.put("icon", postMoudleInfo.getIcon());
			map.put("type", postMoudleInfo.getExtend());
			mData.add(map);
		}
		return mData;
	}

	/**
	 * 保存PostMoudleInfo列表数据
	 */
	public static synchronized void saveListPostMoudleInfos(List<Map<String, String>> mData) {
		EvtLog.d(TAG, "saveListPostMoudleInfos size:" + mData.size());

		Iterator<Map<String, String>> iterators = mData.iterator();
		ArrayList<PostMoudleInfo> mPostMoudleInfos = new ArrayList<PostMoudleInfo>();
		while (iterators.hasNext()) {
			Map<String, String> map = iterators.next();
			PostMoudleInfo postMoudleInfo = new PostMoudleInfo();
			postMoudleInfo.setMoudleId(map.get("id"));
			postMoudleInfo.setName(map.get("title"));
			postMoudleInfo.setPostCount(map.get("post_count"));
			postMoudleInfo.setReplyCount(map.get("reply_count"));
			postMoudleInfo.setListIcon(map.get("list_icon"));
			postMoudleInfo.setIcon(map.get("icon"));
			postMoudleInfo.setExtend(map.get("type"));
			mPostMoudleInfos.add(postMoudleInfo);
		}

		DatabaseManager.saveOrupdateListPostMooudleInfoToDatabase(App.mContext, mPostMoudleInfos);
	}


	public static synchronized void saveOrupdatePersonInfoListToDatabase(Context context, List<PersonInfo> personInfos) {
		DatabaseManager.saveOrupdatePersonInfoListToDatabase(context, personInfos);
	}

	/**
	 * 更新关注状态
	 *
	 * @param context
	 * @param uid
	 * @param attention
	 */
	public static synchronized void updatePersonInfoToDatabase(Context context, String uid, boolean attention) {
		DatabaseManager.updatePersonInfoToDatabase(context, uid, attention);
	}

	/**
	 * 更新用户信息
	 *
	 * @param context
	 * @param personInfo
	 */
	public static synchronized void saveOrupdatePersonInfoToDatabase(Context context, PersonInfo personInfo) {
		DatabaseManager.saveOrupdatePersonInfoToDatabase(context, personInfo);
	}

	/**
	 * 更新昵称/头像
	 *
	 * @param context
	 * @param uid
	 * @param nickname
	 * @param headPic
	 */
	public static synchronized void updatePersonInfoToDatabase(Context context, String uid, String nickname, String headPic) {
		DatabaseManager.updatePersonInfoToDatabase(context, uid, nickname, headPic);
	}

	/**
	 * 通过uid获取用户信息
	 *
	 * @param context
	 * @param uid
	 * @return
	 */
	public static synchronized PersonInfo getPersonInfoByUid(Context context, String uid) {
		return DatabaseManager.getPersonInfoByUid(context, uid);
	}

	/**
	 * 发送Events表活动变化广播
	 * @param context
	 */
	// public static void sendEventChanged(Context context) {
	// Intent intent = new Intent(Constants.ACTION_EVENT_CHANGED);
	// context.sendBroadcast(intent);
	// }
	//
	// /**
	// * 发送Calendars表变化广播
	// * @param context
	// */
	// public static void sendCalendarChanged(Context context) {
	// Intent intent = new Intent(Constants.ACTION_CALENDAR_CHANGED);
	// context.sendBroadcast(intent);
	// }
	//
	// /**
	// * 发送Message表订阅关系变化广播
	// * @param context
	// */
	// public static void sendMessageChanged(Context context) {
	// Intent intent = new Intent(Constants.ACTION_MESSAGE_CHANGEN);
	// context.sendBroadcast(intent);
	// }
	//
	// /**
	// * 发送Photo表变化广播
	// * @param context
	// */
	// public static void sendPhotoChanged(Context context) {
	// Intent intent = new Intent(Constants.ACTION_PHOTO_CHANGEN);
	// context.sendBroadcast(intent);
	// }
	//
	// /**
	// * 线程池执行一个数据库操作任务
	// * @param context 上下文
	// * @param runnable 数据库操作任务
	// */
	// public static void executeDbRunnable(Context context, Runnable runnable)
	// {
	// CalendarApplication.getApplication(context)
	// .getDatabaseThreadExecutorService().submit(runnable);
	// }
}
