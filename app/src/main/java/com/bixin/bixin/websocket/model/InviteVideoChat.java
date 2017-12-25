package com.bixin.bixin.websocket.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by Live on 2017/6/12.
 * 邀请连线
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class InviteVideoChat {
	//(用户发起时需要传1视频 2音频)
	public static int INVITE_CHAT_TYPE_VIDEO = 1;
	public static int INVITE_CHAT_TYPE_MIC = 2;

	//type(1是普通用户接受主播邀请 2是主播接受用户邀请)
	public static String INVITE_TYPE_USER = "1";
	public static String INVITE_TYPE_ANCHOR = "2";
	private String uid;
	private String nickname;
	private int videoChatType;
	private String headPic;
	private String level;
	private String timestamp;
	//是否被选中，主要是主播测请求连麦面板页面使用
	private boolean isSelected = false;
	//是否已读
	private boolean isRead = false;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getVideoChatType() {
		return videoChatType;
	}

	public void setVideoChatType(int videoChatType) {
		this.videoChatType = videoChatType;
	}

	public String getHeadPic() {
		return headPic;
	}

	public void setHeadPic(String headPic) {
		this.headPic = headPic;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean read) {
		isRead = read;
	}
}
