package tv.live.bx.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Title: 列表主播房间信息表 AnchorInfo.java
 * @CreateDate 2013-11-5 下午4:22:09
 * @version 1.0
 */
@DatabaseTable(tableName = "AnchorInfo")
public class AnchorInfo extends ModelObject {

	private static final long serialVersionUID = -8792174634114687993L;

	public static final String PARAM_ROMM_ID = "room_id";

	/**
	 * 主播房间号
	 */
	@DatabaseField(columnName = PARAM_ROMM_ID)
	private String roomId = "";

	/**
	 * 房间logo图片url
	 */
	@DatabaseField(columnName = "logo_url")
	private String logoUrl = "";

	/**
	 * 关注数
	 */
	@DatabaseField(columnName = "love_num")
	private String loveNum = "";

	/**
	 * 观众数
	 */
	@DatabaseField(columnName = "audience_num")
	private String audienceNum = "";

	@DatabaseField(columnName = "moderator_desc")
	private String moderatorDesc = "";

	/**
	 * 是否主播中
	 */
	@DatabaseField(columnName = "isPlaying")
	private String isPlaying;

	/**
	 * 主播体重
	 */
	@DatabaseField(columnName = "weight")
	private String mWeight;

	/**
	 * 主播身高
	 */
	@DatabaseField(columnName = "height")
	private String mHeight;

	/**
	 * 主播年纪
	 */
	@DatabaseField(columnName = "age")
	private String mAge;

	/**
	 * 主播真实姓名
	 */
	@DatabaseField(columnName = "true_name")
	private String trueName;

	/** 主播等级 */
	@DatabaseField(columnName = "anchor_level")
	private String mAnchorLevel;
	/**
	 * 扩展字段
	 */
	@DatabaseField(columnName = "extend")
	private String extend;

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getAudienceNum() {
		return audienceNum;
	}

	public void setAudienceNum(String audienceNum) {
		this.audienceNum = audienceNum;
	}

	public String getIsPlaying() {
		return isPlaying;
	}

	public void setIsPlaying(String isPlaying) {
		this.isPlaying = isPlaying;
	}

	public String getLoveNum() {
		return loveNum;
	}

	public void setLoveNum(String loveNum) {
		this.loveNum = loveNum;
	}

	public String getModeratorDesc() {
		return moderatorDesc;
	}

	public void setModeratorDesc(String moderatorDesc) {
		this.moderatorDesc = moderatorDesc;
	}

	public String isPlaying() {
		return isPlaying;
	}

	public void setPlaying(String isPlaying) {
		this.isPlaying = isPlaying;
	}

	public String getmWeight() {
		return mWeight;
	}

	public void setmWeight(String mWeight) {
		this.mWeight = mWeight;
	}

	public String getmHeight() {
		return mHeight;
	}

	public void setmHeight(String mHeight) {
		this.mHeight = mHeight;
	}

	public String getmAge() {
		return mAge;
	}

	public void setmAge(String mAge) {
		this.mAge = mAge;
	}

	public String getTrueName() {
		return trueName;
	}

	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}

	public String getExtend() {
		return extend;
	}

	public String getmAnchorLevel() {
		return mAnchorLevel;
	}

	public void setmAnchorLevel(String mAnchorLevel) {
		this.mAnchorLevel = mAnchorLevel;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	@Override
	public boolean equals(Object o) {
		AnchorInfo cc = (AnchorInfo) o;
		return cc.roomId.equals(roomId);
	}

}
