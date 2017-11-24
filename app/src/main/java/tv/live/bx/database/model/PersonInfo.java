package tv.live.bx.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by Live on 2017/5/16.
 * 用户信息
 */

@DatabaseTable(tableName = "PersonInfo")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonInfo extends ModelObject {

	private static final long serialVersionUID = -585410398397201142L;
	@DatabaseField(columnName = "user_id")
	private String uid;

	@DatabaseField(columnName = "user_name")
	private String nickname;

	@DatabaseField(columnName = "user_headpic")
	private String headPic;

	@DatabaseField(columnName = "user_sex")
	private String sex;

	@DatabaseField(columnName = "user_level")
	private String level;

	@DatabaseField(columnName = "user_moderator_level")
	private String moderatorLevel;

	@DatabaseField(columnName = "user_is_attention")
	private boolean isAttention;
	private boolean attention;

	@DatabaseField(columnName = "user_verified")
	private String verified;

	public PersonInfo() {

	}

	@Override
	public ModelObject setId(long id) {
		this.uid = String.valueOf(id);
		return super.setId(id);
	}

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

	public String getHeadPic() {
		return headPic;
	}

	public void setHeadPic(String headPic) {
		this.headPic = headPic;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getModeratorLevel() {
		return moderatorLevel;
	}

	public void setModeratorLevel(String moderatorLevel) {
		this.moderatorLevel = moderatorLevel;
	}

	public void setVerified(String verified) {
		this.verified = verified;
	}

	public String getVerified() {
		return verified;
	}

	public boolean isAttention() {
		return isAttention;
	}

	public void setAttention(boolean attention) {
		this.attention = attention;
		this.isAttention = attention;
	}

	public boolean getAttention() {
		return isAttention;
	}

}
