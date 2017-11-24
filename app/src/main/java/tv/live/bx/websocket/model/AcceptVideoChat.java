package tv.live.bx.websocket.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by Live on 2017/6/12.
 * 接受连线
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AcceptVideoChat {
	private String uid;
	private int level;
	private String headPic;
	private String nickname;
	private String mid;
	private int videoChatType;
	private String userPushUrl;
	private String userPullUrl;
	private String pullUrl;
	//加密之前的
	private String pushUrl;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public int getVideoChatType() {
		return videoChatType;
	}

	public void setVideoChatType(int videoChatType) {
		this.videoChatType = videoChatType;
	}


	public String getUserPushUrl() {
		return userPushUrl;
	}

	public void setUserPushUrl(String userPushUrl) {
		this.userPushUrl = userPushUrl;
	}

	public String getPushUrl() {
		return pushUrl;
	}

	public void setPushUrl(String pushUrl) {
		this.pushUrl = pushUrl;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setHeadPic(String headPic) {
		this.headPic = headPic;
	}

	public String getHeadPic() {
		return headPic;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getUserPullUrl() {
		return userPullUrl;
	}

	public void setUserPullUrl(String userPullUrl) {
		this.userPullUrl = userPullUrl;
	}

	public String getPullUrl() {
		return pullUrl;
	}

	public void setPullUrl(String pullUrl) {
		this.pullUrl = pullUrl;
	}
}
