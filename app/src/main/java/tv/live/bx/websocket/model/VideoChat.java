package tv.live.bx.websocket.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by Live on 2017/6/12.
 * 连麦成功广播连麦用户信息
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoChat {
	private String uid;
	private String nickname;
	private int level;
	private String headPic;
	private int videoChatType;

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getHeadPic() {
		return headPic;
	}

	public void setHeadPic(String headPic) {
		this.headPic = headPic;
	}

	public int getVideoChatType() {
		return videoChatType;
	}

	public void setVideoChatType(int videoChatType) {
		this.videoChatType = videoChatType;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
