package com.bixin.bixin.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Title: 首页轮播图信息 BannerInfo.java
 * @CreateDate 2013-11-5 下午4:22:09
 * @version 1.0
 */
@DatabaseTable(tableName = "BannnerInfo")
public class BannerInfo extends ModelObject {

	private static final long serialVersionUID = -8792174634114687993L;

	/**
	 * 房间号 type==2 才有值
	 */
	@DatabaseField(columnName = "room_id")
	private String roomId = "";

	/**
	 * 类型 1 html页 ；2主播房间
	 */
	@DatabaseField(columnName = "type")
	private String type = "";

	/**
	 * 如果type==1 才有值
	 */
	@DatabaseField(columnName = "url")
	private String url = "";

	@DatabaseField(columnName = "pic_url")
	private String picUrl = "";

	/**
	 * 扩展字段
	 */
	@DatabaseField(columnName = "extend")
	private String extend = "";

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	@Override
	public boolean equals(Object o) {
		BannerInfo cc = (BannerInfo) o;
		return cc.roomId.equals(roomId) && cc.url.equals(url);
	}

}
