package com.bixin.bixin.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bixin.bixin.config.AppConfig;
import com.bixin.bixin.database.model.AnchorInfo;
import com.bixin.bixin.database.model.BannerInfo;
import com.bixin.bixin.database.model.PersonInfo;
import com.bixin.bixin.database.model.PostMoudleInfo;
import com.bixin.bixin.database.model.SubjectInfo;
import com.bixin.bixin.library.util.EvtLog;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Title: DatabaseHelper.java Description: 管理或升级日历数据库、提供DAO操作 Copyright:
 * Copyright (c) 2008
 *
 * @version 1.0
 * @CreateDate 2013-11-5 下午3:44:25
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	public static final String DATABASE_NAME = "feizao.db";
	private static final int DATABASE_VERSION = 7;

	static final String TAG = "DatabaseHelper";

	private static final Class<?>[] DATA_CLASSES = {AnchorInfo.class, BannerInfo.class, SubjectInfo.class,
			PostMoudleInfo.class, PersonInfo.class};

	private Dao<AnchorInfo, String> mAnchorInfo = null;
	private Dao<BannerInfo, String> mBannerInfo = null;
	private Dao<SubjectInfo, String> mSubjectInfo = null;
	private Dao<PostMoudleInfo, String> mPostMoudleInfo = null;
	private Dao<PersonInfo, String> mPersonInfo = null;

	// we do this so there is only one helper
	private static DatabaseHelper helper = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			EvtLog.i(TAG, "create database");
			for (Class<?> dataClass : DATA_CLASSES) {
				TableUtils.createTableIfNotExists(connectionSource, dataClass);
			}
			// createEventsTableIfNotExists(db, connectionSource);
		} catch (SQLException e) {
			EvtLog.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		EvtLog.i(TAG, "upgrade database");
		if (oldVersion == 1) {
			upgradeToVersion2(db);
			oldVersion = 2;
		}
		if (oldVersion == 2) {
			upgrade2ToVersion3(db);
			oldVersion = 3;
		}
		if (oldVersion == 3) {
			upgrade3ToVersion4(db);
			oldVersion = 4;
		}
		if (oldVersion == 4) {
			upgrade4ToVersion5(db);
			oldVersion = 5;
		}
		if (oldVersion == 5) {
			upgrade5ToVersion6(db);
			oldVersion = 6;
		}
		if (oldVersion == 6) {
			upgrade6ToVersion7(db);
			oldVersion = 7;
		}
	}

	/**
	 * Get the helper, possibly constructing it if necessary. For each call to
	 * this method, there should be 1 and only 1 call to {@link #close()}.
	 */
	public static DatabaseHelper getHelper(Context context) {
		if (helper == null) {
			synchronized (DatabaseHelper.class) {
				if (helper == null)
					helper = new DatabaseHelper(context);
			}

		}
		return helper;
	}

	public Dao<AnchorInfo, String> getAnchorInfo() throws SQLException {
		if (mAnchorInfo == null) {
			mAnchorInfo = getDao(AnchorInfo.class);
		}
		return mAnchorInfo;
	}

	public Dao<BannerInfo, String> getBannerInfo() throws SQLException {
		if (mBannerInfo == null) {
			mBannerInfo = getDao(BannerInfo.class);
		}
		return mBannerInfo;
	}

	public Dao<PostMoudleInfo, String> getPostMoudleInfo() throws SQLException {
		if (mPostMoudleInfo == null) {
			mPostMoudleInfo = getDao(PostMoudleInfo.class);
		}
		return mPostMoudleInfo;
	}

	public Dao<SubjectInfo, String> getSubjectInfo() throws SQLException {
		if (mSubjectInfo == null) {
			mSubjectInfo = getDao(SubjectInfo.class);
		}
		return mSubjectInfo;
	}

	public Dao<PersonInfo, String> getPersonInfo() throws SQLException {
		if (mPersonInfo == null) {
			mPersonInfo = getDao(PersonInfo.class);
		}
		return mPersonInfo;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		mAnchorInfo = null;
		mBannerInfo = null;
		mSubjectInfo = null;
		mPostMoudleInfo = null;
		mPersonInfo = null;
		helper = null;
	}

	/**
	 * 创建Events表
	 *
	 * @param db
	 * @param connectionSource
	 */
	private void createEventsTableIfNotExists(SQLiteDatabase db, ConnectionSource connectionSource) {
		db.execSQL("CREATE TABLE IF NOT EXISTS Events (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ " calendar_sid BIGINT, title VARCHAR, description VARCHAR, location VARCHAR,"
				+ " dtstart BIGINT, dtend BIGINT, eventTimezone VARCHAR, duration VARCHAR,"
				+ " allDay INTEGER, availability INTEGER, hasAlarm INTEGER, rrule VARCHAR,"
				+ " rdate VARCHAR, dirty INTEGER, deleted INTEGER, type INTEGER, rrule_desc VARCHAR,"
				+ " minutes INTEGER, sid BIGINT, gid VARCHAR, orderDtstart BIGINT, synced INTEGER,"
				+ " calendarType INTEGER, source INTEGER, geoPoint VARCHAR,"
				+ " ownerName VARCHAR, eventSign VARCHAR, fee DOUBLE, feeDesc VARCHAR,"
				+ " maxPerson INTEGER DEFAULT 0, gatherTime BIGINT, gatherAddress VARCHAR,"
				+ " gatherGeo VARCHAR, checkFlag INTEGER, checkInfo VARCHAR, eventStatus INTEGER,"
				+ " prepay DOUBLE, longitude DOUBLE, latitude DOUBLE, regionCode VARCHAR, senderUserNumber VARCHAR,"
				+ " UNIQUE(gid) ON CONFLICT REPLACE )");
	}

	/**
	 * 数据库版本从1升级至2,增加subjectInfo表
	 *
	 * @param db
	 */
	private void upgradeToVersion2(SQLiteDatabase db) {
		EvtLog.i(TAG, "Upgrading to version 2.");
		// 增加 SubjectInfo 表
		Class<?> subjectInfo = SubjectInfo.class;
		try {
			TableUtils.createTableIfNotExists(connectionSource, subjectInfo);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// 由于改动了域名，cookie状态消失了，需要重新登录
		AppConfig.getInstance().updateLoginStatus(false);

		// 将Events表的 calendar_sid 与 Calendars表的 _id关联
		// db.execSQL("UPDATE Events SET calendar_sid = 1");
		// db.execSQL("UPDATE Events SET eventTimezone = 'Asia/Shanghai'");
		// db.execSQL("ALTER TABLE Events ADD COLUMN synced INTEGER DEFAULT 0");
		// db.execSQL("ALTER TABLE Events ADD COLUMN calendarType INTEGER DEFAULT 1");
	}

	/**
	 * 数据库版本从2升级至3；1、修改subjectInfo表字段，增加title 2、创建PostMoudleInfo表
	 *
	 * @param db
	 */
	private void upgrade2ToVersion3(SQLiteDatabase db) {
		EvtLog.i(TAG, "Upgrading to version 3.");
		Class<?> mClass = PostMoudleInfo.class;
		// 增加 SubjectInfo 表
		try {
			db.execSQL("ALTER TABLE SubjectInfo ADD COLUMN subjetc_title TEXT DEFAULT ''");
			db.execSQL("ALTER TABLE SubjectInfo ADD COLUMN is_collect TEXT DEFAULT '0'");
			db.execSQL("ALTER TABLE SubjectInfo ADD COLUMN is_publish TEXT DEFAULT '0'");

			TableUtils.createTableIfNotExists(connectionSource, mClass);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 将Events表的 calendar_sid 与 Calendars表的 _id关联
		// db.execSQL("UPDATE Events SET calendar_sid = 1");
		// db.execSQL("UPDATE Events SET eventTimezone = 'Asia/Shanghai'");
		// db.execSQL("ALTER TABLE Events ADD COLUMN synced INTEGER DEFAULT 0");
		// db.execSQL("ALTER TABLE Events ADD COLUMN calendarType INTEGER DEFAULT 1");
	}

	/**
	 * 数据库版本从3升级至4,修改subjectInfo表字段，增加userType,userLevel字段
	 *
	 * @param db
	 */
	private void upgrade3ToVersion4(SQLiteDatabase db) {
		EvtLog.i(TAG, "Upgrading to version 4.");
		// 增加 SubjectInfo 表
		try {
			db.execSQL("ALTER TABLE SubjectInfo ADD COLUMN user_level TEXT DEFAULT '0'");
			db.execSQL("ALTER TABLE SubjectInfo ADD COLUMN user_type TEXT DEFAULT '0'");

		} catch (Exception e) {
			e.printStackTrace();
		}
		// 将Events表的 calendar_sid 与 Calendars表的 _id关联
		// db.execSQL("UPDATE Events SET calendar_sid = 1");
		// db.execSQL("UPDATE Events SET eventTimezone = 'Asia/Shanghai'");
		// db.execSQL("ALTER TABLE Events ADD COLUMN synced INTEGER DEFAULT 0");
		// db.execSQL("ALTER TABLE Events ADD COLUMN calendarType INTEGER DEFAULT 1");
	}

	/**
	 * 数据库版本从4升级至5,修改subjectInfo表字段，增加anchor_level字段
	 *
	 * @param db
	 */
	private void upgrade4ToVersion5(SQLiteDatabase db) {
		EvtLog.i(TAG, "Upgrading to version 5.");
		// 增加 SubjectInfo 表
		try {
			db.execSQL("ALTER TABLE AnchorInfo ADD COLUMN anchor_level TEXT DEFAULT '0'");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 数据库版本从5升级至6,修改PostMoudleInfo表字段，增加post_count,reply_count,list_icon,icon字段;
	 * 修改SubjectInfo表字段，增加forum_id，forum_title
	 *
	 * @param db
	 */
	private void upgrade5ToVersion6(SQLiteDatabase db) {
		EvtLog.i(TAG, "Upgrading to version 6.");
		// 增加 SubjectInfo 表
		try {
			db.execSQL("ALTER TABLE PostMoudleInfo ADD COLUMN post_count TEXT DEFAULT '0'");
			db.execSQL("ALTER TABLE PostMoudleInfo ADD COLUMN replay_count TEXT DEFAULT '0'");
			db.execSQL("ALTER TABLE PostMoudleInfo ADD COLUMN list_icon TEXT DEFAULT '0'");
			db.execSQL("ALTER TABLE PostMoudleInfo ADD COLUMN icon TEXT DEFAULT '0'");

			db.execSQL("ALTER TABLE SubjectInfo ADD COLUMN forum_id TEXT DEFAULT '0'");
			db.execSQL("ALTER TABLE SubjectInfo ADD COLUMN forum_title TEXT DEFAULT ''");
			db.execSQL("ALTER TABLE SubjectInfo ADD COLUMN is_hot TEXT DEFAULT '0'");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 数据库版本从6升级至7,增加PersonInfo用户信息表
	 *
	 * @param db
	 */
	private void upgrade6ToVersion7(SQLiteDatabase db) {
		EvtLog.i(TAG, "Upgrading to version 6.");
		// 增加 PersonInfo 表
		Class<?> personInfo = PersonInfo.class;

		try {
			TableUtils.createTableIfNotExists(connectionSource, personInfo);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// /**
	// * 数据库版本从9升级到10 新增讨论、聊天记录表
	// *
	// * @param db
	// * @param connectionSource
	// */
	// private void upgradeToVersion10(SQLiteDatabase db,
	// ConnectionSource connectionSource) {
	// LogUtil.i(TAG, "Upgrading to version 10.");
	//
	// // 增加ImChat表
	// ChatDBHelper.toVersion10(db, connectionSource);
	//
	// // 增加ImUser表
	// Class<?> userClass = ImUser.class;
	// try {
	// TableUtils.createTableIfNotExists(connectionSource, userClass);
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	//
	// // 增加RegionData表
	// Class<?> regionClass = CRegionData.class;
	// try {
	// TableUtils.createTableIfNotExists(connectionSource, regionClass);
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	//
	// // Event表增加一个字段
	// EventDBHelper.toVersion10(db);
	//
	// MsgDBHelper.toVersion10(db);
	// }
	//
	// /**
	// * 数据库版本从10升级到11 新增讨论、聊天记录表
	// *
	// * @param db
	// * @param connectionSource
	// */
	// private void upgradeToVersion11(SQLiteDatabase db,
	// ConnectionSource connectionSource) {
	// // Event表增加4个字段
	// EventDBHelper.toVersion11(db);
	//
	// // 参与人表增加报名时间字段、报名信息字段
	// ParticipateDBHelper.toVersion11(db);
	// }
	//
	// /**
	// * 数据库版本重11升到12
	// * @param db
	// * @param connectionSource
	// */
	// private void upgradeToVersion12(SQLiteDatabase db,
	// ConnectionSource connectionSource) {
	//
	// // 参与人表增加报名信息展示、用户ID、聊天账号字段
	// ParticipateDBHelper.toVersion12(db);
	//
	// MsgDBHelper.toVersion12(db);
	// }
	//
	// /**
	// * 数据库版本重12升到13
	// * @param db
	// * @param connectionSource
	// */
	// private void upgradeToVersion13(SQLiteDatabase db,
	// ConnectionSource connectionSource) {
	//
	// // 提醒表增加type字段
	// AlterDBHelper.toVersion13(db);
	// }
	//
	// /**
	// * 数据库版本从13升级到14 新增讨论、聊天记录表
	// *
	// * @param db
	// * @param connectionSource
	// */
	// private void upgradeToVersion14(SQLiteDatabase db,
	// ConnectionSource connectionSource) {
	// LogUtil.i(TAG, "Upgrading to version 14.");
	//
	// // 增加CStatistics表，消息个数统计表
	// StatisticsDBHelper.toVersion14(db, connectionSource);
	// }

}
