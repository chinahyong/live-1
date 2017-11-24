package tv.live.bx.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Title: 帖子模块.java
 * @CreateDate 2013-11-5 下午4:22:09
 * @version 1.0
 */
@DatabaseTable(tableName = "PostMoudleInfo")
public class PostMoudleInfo extends ModelObject {

	private static final long serialVersionUID = -8792174634114687993L;

	/**
	 * 板块Id
	 */
	@DatabaseField(columnName = "moudle_id")
	private String moudleId = "";

	/**
	 * 名称
	 */
	@DatabaseField(columnName = "name")
	private String name = "";

	/** 帖子栏目所有帖子数 */
	@DatabaseField(columnName = "post_count")
	private String postCount;

	/** 帖子栏目所有回复数 */
	@DatabaseField(columnName = "replay_count")
	private String replyCount;

	/** 帖子栏目图标，在列表显示的图标 */
	@DatabaseField(columnName = "list_icon")
	private String listIcon;

	/** 帖子栏目图标 */
	@DatabaseField(columnName = "icon")
	private String icon;

	/**
	 * 扩展字段，是否是饭圈栏目，1饭圈栏目，0社区栏目
	 */
	@DatabaseField(columnName = "extend")
	private String extend = "";

	public String getMoudleId() {
		return moudleId;
	}

	public void setMoudleId(String moudleId) {
		this.moudleId = moudleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public String getPostCount() {
		return postCount;
	}

	public void setPostCount(String postCount) {
		this.postCount = postCount;
	}

	public String getReplyCount() {
		return replyCount;
	}

	public void setReplyCount(String replyCount) {
		this.replyCount = replyCount;
	}

	public String getListIcon() {
		return listIcon;
	}

	public void setListIcon(String listIcon) {
		this.listIcon = listIcon;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	@Override
	public boolean equals(Object o) {
		PostMoudleInfo cc = (PostMoudleInfo) o;
		return cc.moudleId.equals(moudleId);
	}

}
