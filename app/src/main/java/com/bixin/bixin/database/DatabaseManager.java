package com.bixin.bixin.database;

import android.content.Context;
import android.text.TextUtils;

import com.bixin.bixin.database.model.AnchorInfo;
import com.bixin.bixin.database.model.BannerInfo;
import com.bixin.bixin.database.model.PersonInfo;
import com.bixin.bixin.database.model.PostMoudleInfo;
import com.bixin.bixin.database.model.SubjectInfo;
import com.bixin.bixin.library.util.EvtLog;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Title: DatabaseManager.java Description: 应用数据库操作工具类
 *
 * @version 1.0
 * @CreateDate 2013-11-5 下午5:12:50
 */
public class DatabaseManager {
	static final String TAG = "DatabaseManager";

	private static String SQL_LIKE_CONSTRAINT = "%$%";

	/**
	 * 获取数据库操作helper
	 *
	 * @param context 上下文
	 * @return 数据库操作helper
	 */
	public static DatabaseHelper getDatabaseHelper(Context context) {
		return DatabaseHelper.getHelper(context);
	}

	/**
	 * 根据房间号获取主播用户信息
	 *
	 * @param context 上下文
	 * @param roomId  房间号
	 */
	public static AnchorInfo getAnchorInfoByRoomId(Context context, String roomId) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		AnchorInfo anchorInfo = new AnchorInfo();
		try {
			final Dao<AnchorInfo, String> dao = helper.getAnchorInfo();
			anchorInfo = dao.queryForFirst(dao.queryBuilder().where().eq(AnchorInfo.PARAM_ROMM_ID, roomId).prepare());
		} catch (SQLException e) {
			EvtLog.e(TAG, e);
		}
		return anchorInfo;
	}

	/**
	 * 保存主播列表数据到数据库
	 *
	 * @param context
	 */
	public static void saveOrupdateListAnchorinfoToDatabase(Context context, final List<AnchorInfo> anchorInfos) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			deleteAnchorTables(context);
			final Dao<AnchorInfo, String> dao = helper.getAnchorInfo();
			dao.callBatchTasks(new Callable<Void>() {
				public Void call() throws Exception {
					for (AnchorInfo info : anchorInfos) {
						dao.createOrUpdate(info);
					}
					return null;
				}
			});
			EvtLog.d(TAG, "saveListAnchorinfoToDatabase success");
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		} finally {
		}
	}

	/**
	 * 清空Anchor表数据
	 *
	 * @param context
	 */
	public static void deleteAnchorTables(Context context) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			final Dao<AnchorInfo, String> dao = helper.getAnchorInfo();
			dao.executeRaw("DELETE FROM AnchorInfo");
		} catch (SQLException e) {
			EvtLog.e(TAG, e);
		}
	}

	/**
	 * 清空BannnerInfo表数据
	 *
	 * @param context
	 */
	public static void deleteBannerInfoTables(Context context) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			final Dao<BannerInfo, String> dao = helper.getBannerInfo();
			dao.executeRaw("DELETE FROM BannnerInfo");
		} catch (SQLException e) {
			EvtLog.e(TAG, e);
		}
	}

	/**
	 * 保存Banner列表数据到数据库
	 *
	 * @param context
	 * @param mBannerInfos
	 */
	public static void saveOrupdateListBannerinfoToDatabase(Context context, final List<BannerInfo> mBannerInfos) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			deleteBannerInfoTables(context);
			final Dao<BannerInfo, String> dao = helper.getBannerInfo();
			dao.callBatchTasks(new Callable<Void>() {
				public Void call() throws Exception {
					for (BannerInfo info : mBannerInfos) {
						dao.createOrUpdate(info);
					}
					return null;
				}
			});
			EvtLog.d(TAG, "saveListBannerinfoToDatabase success");
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		} finally {
		}
	}

	/**
	 * 获取本地保存的所有主播数据
	 *
	 * @param context
	 */
	public static List<AnchorInfo> getAllAnchorInfos(Context context) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		List<AnchorInfo> mAnchorInfos = null;
		try {
			final Dao<AnchorInfo, String> dao = helper.getAnchorInfo();
			mAnchorInfos = dao.queryForAll();// fatherCode = fatherCode
			EvtLog.d(TAG, "getAllAnchorInfos success");
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		}
		return mAnchorInfos;
	}

	/**
	 * 获取本地保存的所有Banner数据
	 *
	 * @param context
	 */
	public static List<BannerInfo> getAllBannerInfos(Context context) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		List<BannerInfo> mBannerInfos = null;
		try {
			final Dao<BannerInfo, String> dao = helper.getBannerInfo();
			mBannerInfos = dao.queryForAll();// fatherCode = fatherCode
			EvtLog.d(TAG, "getAllBannerInfos success");
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		}
		return mBannerInfos;
	}

	// /**
	// * 保存帖子列表数据到数据库
	// * @param context
	// * @param list
	// */
	// public static void saveOrupdateListSujectInfoToDatabase(Context context,
	// String uId,
	// final List<SubjectInfo> subjectInfos) {
	// final DatabaseHelper helper = getDatabaseHelper(context);
	// try {
	// final Dao<SubjectInfo, String> dao = helper.getSubjectInfo();
	// dao.executeRaw("DELETE FROM SubjectInfo WHERE uid = " + uId);
	// dao.callBatchTasks(new Callable<Void>() {
	// public Void call() throws Exception {
	// for (SubjectInfo info : subjectInfos) {
	// dao.createOrUpdate(info);
	// }
	// return null;
	// }
	// });
	// LogUtil.d(TAG, "saveOrupdateListSujectInfoToDatabase success");
	// } catch (Exception e) {
	// LogUtil.e(TAG, e);
	// } finally {
	// }
	// }

	/**
	 * 社区列表，保存帖子列表数据到数据库
	 *
	 * @param context
	 */
	public static void saveOrupdateListSujectInfoToDatabase(Context context, String uId, String isCollect,
															String isPublish, String forumId, String isHot, final List<SubjectInfo> subjectInfos) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			final Dao<SubjectInfo, String> dao = helper.getSubjectInfo();
			dao.executeRaw("DELETE FROM SubjectInfo WHERE uid = " + uId + " AND is_collect = " + isCollect
					+ " AND is_publish = " + isPublish + " AND forum_id = " + forumId + " AND is_hot = " + isHot);
			dao.callBatchTasks(new Callable<Void>() {
				public Void call() throws Exception {
					for (SubjectInfo info : subjectInfos) {
						dao.createOrUpdate(info);
					}
					return null;
				}
			});
			EvtLog.d(TAG, "saveOrupdateListSujectInfoToDatabase success");
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		} finally {
		}
	}

	/**
	 * 我的页面（收藏、发表）保存帖子列表数据到数据库
	 *
	 * @param context
	 */
	public static void saveOrupdateListSujectInfoToDatabase(Context context, String uId, String isCollect,
															String isPublish, final List<SubjectInfo> subjectInfos) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			final Dao<SubjectInfo, String> dao = helper.getSubjectInfo();
			dao.executeRaw("DELETE FROM SubjectInfo WHERE uid = " + uId + " AND is_collect = " + isCollect
					+ " AND is_publish = " + isPublish);
			dao.callBatchTasks(new Callable<Void>() {
				public Void call() throws Exception {
					for (SubjectInfo info : subjectInfos) {
						dao.createOrUpdate(info);
					}
					return null;
				}
			});
			EvtLog.d(TAG, "saveOrupdateListSujectInfoToDatabase success");
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		} finally {
		}
	}

	/**
	 * 清空SubjectInfo表数据
	 *
	 * @param context
	 */
	public static void deleteSubjectTables(Context context) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			final Dao<SubjectInfo, String> dao = helper.getSubjectInfo();
			dao.executeRaw("DELETE FROM SubjectInfo");
		} catch (SQLException e) {
			EvtLog.e(TAG, e);
		}
	}

	/**
	 * 社区列表，根据栏目获取数据数据
	 *
	 * @param context
	 * @param uId     根据用户Id获取帖子
	 */
	public static List<SubjectInfo> getForumsSubjectInfos(Context context, String uId, String forumId, String isHot) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		List<SubjectInfo> mSubjectInfos = null;
		try {
			final Dao<SubjectInfo, String> dao = helper.getSubjectInfo();
			mSubjectInfos = dao.queryForEq("uid", uId);// fatherCode =

			QueryBuilder<SubjectInfo, String> queryBuilder = dao.queryBuilder();
			queryBuilder.where().eq("uid", uId).and().eq("forum_id", forumId).and().eq("is_hot", isHot);
			mSubjectInfos = queryBuilder.query();

			EvtLog.d(TAG, "getAllSubjectInfos success");
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		}
		return mSubjectInfos;
	}

	/**
	 * 获取本地保存的收藏的帖子数据
	 *
	 * @param context
	 * @param uId       根据用户Id获取帖子
	 * @param isCollect
	 */
	public static List<SubjectInfo> getAllSubjectInfos(Context context, String uId, String isCollect, String isPublish) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		List<SubjectInfo> mSubjectInfos = null;
		try {
			final Dao<SubjectInfo, String> dao = helper.getSubjectInfo();
			QueryBuilder<SubjectInfo, String> queryBuilder = dao.queryBuilder();
			queryBuilder.where().eq("uid", uId).and().eq("is_collect", isCollect).and().eq("is_publish", isPublish);
			mSubjectInfos = queryBuilder.query();
			EvtLog.d(TAG, "getAllSubjectInfos success");
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		}
		return mSubjectInfos;
	}

	/**
	 * 清空PostMoudleInfo表数据
	 *
	 * @param context
	 */
	public static void deletePostMoudleInfoTables(Context context) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			final Dao<PostMoudleInfo, String> dao = helper.getPostMoudleInfo();
			dao.executeRaw("DELETE FROM PostMoudleInfo");
		} catch (SQLException e) {
			EvtLog.e(TAG, e);
		}
	}

	/**
	 * 保存PostMooudleInfo列表数据到数据库
	 *
	 * @param context
	 * @param mPostMoudleInfos
	 */
	public static void saveOrupdateListPostMooudleInfoToDatabase(Context context,
																 final List<PostMoudleInfo> mPostMoudleInfos) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			deletePostMoudleInfoTables(context);
			final Dao<PostMoudleInfo, String> dao = helper.getPostMoudleInfo();
			dao.callBatchTasks(new Callable<Void>() {
				public Void call() throws Exception {
					for (PostMoudleInfo info : mPostMoudleInfos) {
						dao.createOrUpdate(info);
					}
					return null;
				}
			});
			EvtLog.d(TAG, "saveOrupdateListPostMooudleInfoToDatabase success");
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		} finally {
		}
	}

	/**
	 * 获取本地保存的所有PostMoudleInfo数据
	 *
	 * @param context
	 */
	public static List<PostMoudleInfo> getAllPostMoudleInfos(Context context) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		List<PostMoudleInfo> mPostMoudleInfos = null;
		try {
			final Dao<PostMoudleInfo, String> dao = helper.getPostMoudleInfo();
			mPostMoudleInfos = dao.queryForAll();
			EvtLog.d(TAG, "getAllPostMoudleInfos success");
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		}
		return mPostMoudleInfos;
	}

	/**
	 * 保存用户信息列表
	 *
	 * @param context
	 * @param personInfos
	 */
	public static void saveOrupdatePersonInfoListToDatabase(Context context, final List<PersonInfo> personInfos) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			final Dao<PersonInfo, String> dao = helper.getPersonInfo();
			dao.callBatchTasks(new Callable<Void>() {
				public Void call() throws Exception {
					for (PersonInfo info : personInfos) {
						dao.createOrUpdate(info);
					}
					return null;
				}
			});
			EvtLog.d(TAG, "saveOrupdatePersonInfoListToDatabase success");
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		} finally {
		}
	}

	/**
	 * 保存用户信息列表
	 *
	 * @param context
	 * @param personInfo
	 */
	public static void saveOrupdatePersonInfoToDatabase(Context context, final PersonInfo personInfo) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			final Dao<PersonInfo, String> dao = helper.getPersonInfo();
			dao.callBatchTasks(new Callable<Void>() {
				public Void call() throws Exception {
					dao.createOrUpdate(personInfo);
					return null;
				}
			});
			EvtLog.d(TAG, "saveOrupdatePersonInfoToDatabase success");
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		} finally {
		}
	}

	/**
	 * 更新单个用户关注状态
	 *
	 * @param context
	 * @param uid
	 * @param attention
	 */
	public static void updatePersonInfoToDatabase(Context context, String uid, boolean attention) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			final Dao<PersonInfo, String> dao = helper.getPersonInfo();
			PersonInfo personInfo = getPersonInfoByUid(context, uid);
			if (personInfo != null) {
				personInfo.setAttention(attention);
				dao.update(personInfo);
				EvtLog.d(TAG, "updatePersonInfoToDatabase success");
			}
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		} finally {
		}
	}

	/**
	 * 更新单个用户头像/昵称/关注状态
	 *
	 * @param context
	 * @param uid
	 * @param nickname
	 * @param headPic
	 */
	public static void updatePersonInfoToDatabase(Context context, String uid, String nickname, String headPic) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			final Dao<PersonInfo, String> dao = helper.getPersonInfo();
			PersonInfo personInfo = getPersonInfoByUid(context, uid);
			// 存在该用户直接更新
			if (personInfo != null) {
				if (!TextUtils.isEmpty(nickname)) {
					personInfo.setNickname(nickname);
				}
				if (!TextUtils.isEmpty(headPic)) {
					personInfo.setHeadPic(headPic);
				}
				dao.update(personInfo);
				EvtLog.d(TAG, "updatePersonInfoToDatabase update success");
			} else {
				// 本地未找到该用户，直接将用户基本信息插入
				final PersonInfo insertPersonInfo = new PersonInfo();
				insertPersonInfo.setUid(uid);
				insertPersonInfo.setNickname(nickname);
				insertPersonInfo.setHeadPic(headPic);
				// 不存在该用户，添加该用户
				dao.callBatchTasks(new Callable<Void>() {
					public Void call() throws Exception {
						dao.createOrUpdate(insertPersonInfo);
						return null;
					}
				});
				EvtLog.d(TAG, "updatePersonInfoToDatabase insert success");
			}
		} catch (Exception e) {
			EvtLog.e(TAG, e);
		} finally {
		}
	}

	/**
	 * 通过UID查询用户相关信息
	 */
	public static PersonInfo getPersonInfoByUid(Context context, String uid) {
		final DatabaseHelper helper = getDatabaseHelper(context);
		try {
			Dao<PersonInfo, String> dao = helper.getPersonInfo();
			QueryBuilder<PersonInfo, String> queryBuilder = dao.queryBuilder();
			queryBuilder.where().eq("user_id", uid);
			List<PersonInfo> personInfos = queryBuilder.query();
			if (personInfos != null && personInfos.size() >= 1) {
				return personInfos.get(0);
			}
			EvtLog.d(TAG, "getPersonInfoByUid success");
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
