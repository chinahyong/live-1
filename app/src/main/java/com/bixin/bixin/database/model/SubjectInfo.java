package com.bixin.bixin.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Title: 帖子信息表 SubjectInfo.java
 * @CreateDate 2013-11-5 下午4:22:09
 * @version 1.0
 */
@DatabaseTable(tableName = "SubjectInfo")
public class SubjectInfo extends ModelObject {

	private static final long serialVersionUID = -8792174634114687993L;

	public static final String PARAM_SUBJECT_ID = "subject_id";

	/** 没有用户ID */
	public static final String NO_USRID = "-1";
	public static final String NO_COLLECTED = "0";
	/** 收藏的帖子 */
	public static final String COLLECTED = "1";
	public static final String NO_PUBLISHED = "0";
	/** 发表的帖子 */
	public static final String PUBLISHED = "1";
	/** 最新回复排序的帖子 */
	public static final String HOT = "1";
	/** 最热排序的帖子 */
	public static final String NEW_REPLY = "0";

	/**
	 * 主贴id
	 */
	@DatabaseField(columnName = PARAM_SUBJECT_ID)
	private String subjectId = "";

	/**
	 * 发帖人Id
	 */
	@DatabaseField(columnName = "user_id")
	private String subjectUserId = "";

	/**
	 * 发帖人头像url
	 */
	@DatabaseField(columnName = "head_url")
	private String headPicUrl = "";

	/**
	 * 发帖人名字
	 */
	@DatabaseField(columnName = "poster_name")
	private String posterName = "";

	/**
	 * 发帖的时间
	 */
	@DatabaseField(columnName = "subject_time")
	private String subjectTime = "";

	/**
	 * 帖子标题
	 */
	@DatabaseField(columnName = "subjetc_title")
	private String subjectTitle = "";

	/**
	 * 帖子内容
	 */
	@DatabaseField(columnName = "subjetc_content")
	private String subjectContent = "";

	/**
	 * 帖子内容图片
	 */
	@DatabaseField(columnName = "subject_photos")
	private String subjectPhotos;

	/** 是否已点赞 */
	@DatabaseField(columnName = "is_support")
	private String isSupport;

	/** 是否已收藏 0未，1收藏 */
	@DatabaseField(columnName = "is_collect")
	private String isCollect = SubjectInfo.NO_COLLECTED;

	/** 是否是自己发布的 0未，1是 */
	@DatabaseField(columnName = "is_publish")
	private String isPublish = SubjectInfo.NO_PUBLISHED;

	/**
	 * 关注数
	 */
	@DatabaseField(columnName = "love_num")
	private String loveNum = "";

	/**
	 * 回复数
	 */
	@DatabaseField(columnName = "relay_num")
	private String relayNum = "";

	/**
	 * 是否置顶
	 */
	@DatabaseField(columnName = "is_top")
	private String isTop;

	/** 用户Id，为""，表示帖子列表信息,其他的为用户收藏的帖子 */
	@DatabaseField(columnName = "uid")
	private String uId = NO_USRID;

	/** 发帖人用户等级 */
	@DatabaseField(columnName = "user_level")
	private String userLevel;

	/** 发帖人用户类别 */
	@DatabaseField(columnName = "user_type")
	private String userType;

	/** 所属栏目id */
	@DatabaseField(columnName = "forum_id")
	private String forumId = "0";

	/** 所属栏目标题 */
	@DatabaseField(columnName = "forum_title")
	private String forumTitle = "";

	/** 1是热门，0最新回复 */
	@DatabaseField(columnName = "is_hot")
	private String isHot = NEW_REPLY;

	public String getForumId() {
		return forumId;
	}

	public void setForumId(String forumId) {
		this.forumId = forumId;
	}

	public String getForumTitle() {
		return forumTitle;
	}

	public void setForumTitle(String forumTitle) {
		this.forumTitle = forumTitle;
	}

	public String getIsHot() {
		return isHot;
	}

	public void setIsHot(String isHot) {
		this.isHot = isHot;
	}

	/**
	 * 扩展字段
	 */
	@DatabaseField(columnName = "extend")
	private String extend;

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public String getHeadPicUrl() {
		return headPicUrl;
	}

	public void setHeadPicUrl(String headPicUrl) {
		this.headPicUrl = headPicUrl;
	}

	public String getPosterName() {
		return posterName;
	}

	public void setPosterName(String posterName) {
		this.posterName = posterName;
	}

	public String getSubjectTime() {
		return subjectTime;
	}

	public void setSubjectTime(String subjectTime) {
		this.subjectTime = subjectTime;
	}

	public String getSubjectContent() {
		return subjectContent;
	}

	public void setSubjectContent(String subjectContent) {
		this.subjectContent = subjectContent;
	}

	public String getSubjectTitle() {
		return subjectTitle;
	}

	public void setSubjectTitle(String subjectTitle) {
		this.subjectTitle = subjectTitle;
	}

	public String getLoveNum() {
		return loveNum;
	}

	public void setLoveNum(String loveNum) {
		this.loveNum = loveNum;
	}

	public String getRelayNum() {
		return relayNum;
	}

	public void setRelayNum(String relayNum) {
		this.relayNum = relayNum;
	}

	public String getIsTop() {
		return isTop;
	}

	public void setIsTop(String isTop) {
		this.isTop = isTop;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static String getParamSubjectId() {
		return PARAM_SUBJECT_ID;
	}

	public String getSubjectUserId() {
		return subjectUserId;
	}

	public void setSubjectUserId(String subjectUserId) {
		this.subjectUserId = subjectUserId;
	}

	public String getSubjectPhotos() {
		return subjectPhotos;
	}

	public void setSubjectPhotos(String subjectPhotos) {
		this.subjectPhotos = subjectPhotos;
	}

	public String getIsSupport() {
		return isSupport;
	}

	public void setIsSupport(String isSupport) {
		this.isSupport = isSupport;
	}

	public String getIsCollect() {
		return isCollect;
	}

	public void setIsCollect(String isCollect) {
		this.isCollect = isCollect;
	}

	public String getIsPublish() {
		return isPublish;
	}

	public void setIsPublish(String isPublish) {
		this.isPublish = isPublish;
	}

	public String getuId() {
		return uId;
	}

	public void setuId(String uId) {
		this.uId = uId;
	}

	public String getUserLevel() {
		return userLevel;
	}

	public void setUserLevel(String userLevel) {
		this.userLevel = userLevel;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	@Override
	public boolean equals(Object o) {
		SubjectInfo cc = (SubjectInfo) o;
		return cc.subjectId.equals(this.subjectId);
	}

}
