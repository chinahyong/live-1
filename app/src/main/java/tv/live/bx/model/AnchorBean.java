package tv.live.bx.model;

import android.text.TextUtils;
import android.util.SparseArray;

import tv.live.bx.model.interfaces.IParseToBean;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BYC on 2017/5/23.
 * 主播信息bean
 */

@JsonIgnoreProperties("topics")
public class AnchorBean implements IParseToBean, Serializable {
    public long rid;
    public long playStartTime;
    /***/
    public int sex;
    public int mid;
    public String nickname;
    public String headPic;
    public boolean isPlaying;
    public int onlineNum;
    public int fansNum;
    public String announcement;
    public int moderatorLevel;
    public boolean verified;
    public String verifyInfo;
    public String videoPlayUrl;
    public String recommendTag;

    public SparseArray<String> topics = new SparseArray<>();
    public long weight;
    public long id;
    public String city;

    public int hotCount;

    //主播开播标签
    public Tags tags;

    public class Tags {
        public int id;
        public String name;

        public boolean isValid() {
            return id != 0 && !TextUtils.isEmpty(name);
        }
    }

    @Override
    public void parseFromJSon(JSONObject jsonObject) {
        if (jsonObject == null) return;
        rid = jsonObject.optLong(RID);
        playStartTime = jsonObject.optLong(PLAY_START_TIME);
        sex = jsonObject.optInt(SEX);
        mid = jsonObject.optInt(MID);
        nickname = jsonObject.optString(NICKNAME);
        headPic = jsonObject.optString(HEAD_PIC);
        isPlaying = jsonObject.optBoolean(ISPLAYING);
        onlineNum = jsonObject.optInt(ONLINE_NUM);
        fansNum = jsonObject.optInt(FANSNUM);
        announcement = jsonObject.optString(ANNOUNCEMENT);
        moderatorLevel = jsonObject.optInt(MODERATORLEVEL);
        verified = jsonObject.optBoolean(VERIFIED);
        verifyInfo = jsonObject.optString(VERIFY_INFO);
        videoPlayUrl = jsonObject.optString(VIDEOPLAYURL);
        recommendTag = jsonObject.optString(RECOMMENDTAG);
        weight = jsonObject.optLong(WEIGHT);
        id = jsonObject.optLong(ID);
        city = jsonObject.optString(CITY);
        hotCount = jsonObject.optInt(HOTCOUNT);

        try {
            JSONArray jsonArray = jsonObject.getJSONArray(TOPICS);
            if (jsonArray != null) {
                int length = jsonArray.length();
                for (int i = 0; i < length; i++) {
                    JSONObject job = jsonArray.getJSONObject(i);
                    topics.append(job.getInt(ID), job.getString(TITLE));
                }
            }

            JSONArray jArray = jsonObject.getJSONArray(TAGS);
            if (jArray != null && jArray.length() > 0) {
                JSONObject job = jArray.getJSONObject(0);
                tags = new Tags();
                tags.id = job.getInt(ID);
                tags.name = job.getString(NAME);
            }

        } catch (JSONException e) {

        }
    }

    public static AnchorBean parseAnchor(JSONObject jsonObject) {
        AnchorBean bean = new AnchorBean();
        bean.parseFromJSon(jsonObject);
        return bean;
    }

    public static List<AnchorBean> parseAnchorList(JSONArray jsonArray) {
        int length = jsonArray.length();
        List<AnchorBean> lists = new ArrayList<>();
        try {
            for (int i = 0; i < length; i++) {
                AnchorBean bean = new AnchorBean();
                bean.parseFromJSon(jsonArray.getJSONObject(i));
                lists.add(bean);
            }
        } catch (JSONException e) {

        }
        return lists;
    }

    public final static String RID = "rid";
    public final static String PLAY_START_TIME = "playStartTime";
    public final static String SEX = "sex";
    public final static String MID = "mid";
    public final static String NICKNAME = "nickname";
    public final static String HEAD_PIC = "headPic";
    public final static String ISPLAYING = "isPlaying";
    public final static String ONLINE_NUM = "onlineNum";
    public final static String FANSNUM = "fansNum";
    public final static String ANNOUNCEMENT = "announcement";
    public final static String MODERATORLEVEL = "moderatorLevel";
    public final static String VERIFIED = "verified";
    public final static String VERIFY_INFO = "verifyInfo";
    public final static String VIDEOPLAYURL = "videoPlayUrl";
    public final static String RECOMMENDTAG = "recommendTag";
    public final static String TOPICS = "topics";
    public final static String WEIGHT = "weight";
    public final static String TITLE = "title";
    public final static String ID = "id";
    public final static String CITY = "city";
    public final static String TAGS = "tags";
    public final static String NAME = "name";
    public final static String HOTCOUNT = "hot_count";


}
