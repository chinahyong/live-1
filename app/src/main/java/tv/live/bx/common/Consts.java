package tv.live.bx.common;

import android.util.SparseArray;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;

import java.util.HashMap;
import java.util.Map;

public class Consts {
	// 服务器地址
	// public static final String BASE_URL_SERVER =
	// "http://124.115.26.202:14539";
	public static String BASE_HTTP_DOMAIN = "app.gjlive.cn";
	public static String BASE_HTTP_DOMAIN_BAK = "[\"app.gj2018.cn\",\"app.gj2017.cn\"]";
	public static String BASE_DOMAIN = "app.guojiang.tv";
	public static String BASE_STAT_DOMAIN = "stat.guojiang.tv";
	public static String BASE_M_DOMAIN = "m.guojiang.tv";

	public static String BASE_URL_SERVER = "http://" + BASE_DOMAIN;
	// 用户操作统计
	public static String BASE_STAT_URL_SERVER = "http://" + BASE_STAT_DOMAIN + "/";
	// Web页面
	public static String BASE_M_URL_SERVER = "http://" + BASE_M_DOMAIN;


	// public static final String RTMP_URL_SERVER = "rtmp://120.24.218.59";
	public static final String DOWNLOAD_URL_SERVER = "http://www.guojiang.tv/help/app";

	public static final String SHARE_URI_IMG = "";
	public static final String SHARE_TITLE = "星光直播";

	// 欢迎页停留时间(ms)
	public static final int DURATION_WELCOME_ACTIVITY = 2000;

	// LIST相关
	public static final int LIST_MAX_ITEM_NUM = 20;

	// 测试App_id 222222
	// 果酱直播App_id 101144047
	// key fa596527ec39835a2b1be1bb8318e92e

	// Banner跳转类型
	public static final int BANNER_URL_TYPE_PAGE = 1; // 跳转到html页面
	public static final int BANNER_URL_TYPE_PLAYER = 2; // 跳转到主播页
	// public static final int BANNER_URL_TYPE_LOGIN = 3; // 跳转到注册页
	public static final int BANNER_URL_TYPE_GROUP = 3; // 跳转到饭圈
	public static final int BANNER_URL_TYPE_POST_DETAIL = 4; // 跳转到帖子
	// 关注

	public static final int FOLLOW_ON = 1;
	public static final int FOLLOW_OFF = 0;

	// 性别
	public static final int GENDER_UNKNOWN = 0;
	public static final int GENDER_MALE = 1;
	public static final int GENDER_FEMALE = 2;

	public static final String getGender(int piGender) {
		String lsGender = null;
		switch (piGender) {
			case GENDER_UNKNOWN:
				lsGender = "未设置";
				break;
			case GENDER_MALE:
				lsGender = "男";
				break;
			case GENDER_FEMALE:
				lsGender = "女";
				break;
			default:
				lsGender = "未设置";
				break;
		}
		return lsGender;
	}

	// 表情Text对应的表情图片ID
	public static final Map<String, Integer> TEXT_EMOTION_MAP = new HashMap<>();

	static {
		TEXT_EMOTION_MAP.put("{#1#}", R.drawable.emoji_1);
		TEXT_EMOTION_MAP.put("{#2#}", R.drawable.emoji_2);
		TEXT_EMOTION_MAP.put("{#3#}", R.drawable.emoji_3);
		TEXT_EMOTION_MAP.put("{#4#}", R.drawable.emoji_4);
		TEXT_EMOTION_MAP.put("{#5#}", R.drawable.emoji_5);
		TEXT_EMOTION_MAP.put("{#6#}", R.drawable.emoji_6);
		TEXT_EMOTION_MAP.put("{#7#}", R.drawable.emoji_7);
		TEXT_EMOTION_MAP.put("{#8#}", R.drawable.emoji_8);
		TEXT_EMOTION_MAP.put("{#9#}", R.drawable.emoji_9);
		TEXT_EMOTION_MAP.put("{#10#}", R.drawable.emoji_10);
		TEXT_EMOTION_MAP.put("{#11#}", R.drawable.emoji_11);
		TEXT_EMOTION_MAP.put("{#12#}", R.drawable.emoji_12);
		TEXT_EMOTION_MAP.put("{#13#}", R.drawable.emoji_13);
		TEXT_EMOTION_MAP.put("{#14#}", R.drawable.emoji_14);
		TEXT_EMOTION_MAP.put("{#15#}", R.drawable.emoji_15);
		TEXT_EMOTION_MAP.put("{#16#}", R.drawable.emoji_16);
		TEXT_EMOTION_MAP.put("{#17#}", R.drawable.emoji_17);
		TEXT_EMOTION_MAP.put("{#18#}", R.drawable.emoji_18);
		TEXT_EMOTION_MAP.put("{#19#}", R.drawable.emoji_19);
		TEXT_EMOTION_MAP.put("{#20#}", R.drawable.emoji_20);
		TEXT_EMOTION_MAP.put("{#21#}", R.drawable.emoji_21);
		TEXT_EMOTION_MAP.put("{#22#}", R.drawable.emoji_22);
		TEXT_EMOTION_MAP.put("{#23#}", R.drawable.emoji_23);
		TEXT_EMOTION_MAP.put("{#24#}", R.drawable.emoji_24);
		TEXT_EMOTION_MAP.put("{#25#}", R.drawable.emoji_25);
		TEXT_EMOTION_MAP.put("{#26#}", R.drawable.emoji_26);
		TEXT_EMOTION_MAP.put("{#27#}", R.drawable.emoji_27);
		TEXT_EMOTION_MAP.put("{#28#}", R.drawable.emoji_28);
		TEXT_EMOTION_MAP.put("{#29#}", R.drawable.emoji_29);
		TEXT_EMOTION_MAP.put("{#30#}", R.drawable.emoji_30);
		TEXT_EMOTION_MAP.put("{#31#}", R.drawable.emoji_31);
		TEXT_EMOTION_MAP.put("{#32#}", R.drawable.emoji_32);
		TEXT_EMOTION_MAP.put("{#33#}", R.drawable.emoji_33);
		TEXT_EMOTION_MAP.put("{花}", R.drawable.ic_rose_1);
	}

	// 表情图片ID对应的表情文字
	public static final SparseArray<String> EMOTION_TEXT_MAP = new SparseArray<>();

	static {
		EMOTION_TEXT_MAP.put(R.drawable.emoji_1, "{#1#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_2, "{#2#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_3, "{#3#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_4, "{#4#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_5, "{#5#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_6, "{#6#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_7, "{#7#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_8, "{#8#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_9, "{#9#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_10, "{#10#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_11, "{#11#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_12, "{#12#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_13, "{#13#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_14, "{#14#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_15, "{#15#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_16, "{#16#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_17, "{#17#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_18, "{#18#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_19, "{#19#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_20, "{#20#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_21, "{#21#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_22, "{#22#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_23, "{#23#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_24, "{#24#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_25, "{#25#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_26, "{#26#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_27, "{#27#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_28, "{#28#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_29, "{#29#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_30, "{#30#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_31, "{#31#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_32, "{#32#}");
		EMOTION_TEXT_MAP.put(R.drawable.emoji_33, "{#33#}");
		EMOTION_TEXT_MAP.put(R.drawable.ic_rose_1, "{花}");
	}

	public static final int PUSH_TYPE_AUTHOR_BEGIN_PLAYING = 0;// 我的关注
	// 微信的appid:
	public static final String WEIXIN_APPID = FeizaoApp.mConctext.getString(R.string.wx_id);
	public static final String WEIXIN_APPSECRET = FeizaoApp.mConctext.getString(R.string.wx_secret);

	// qq空间的appid
	public static final String QQ_APPID = FeizaoApp.mConctext.getString(R.string.qq_id);
	public static final String QQ_APPKEY = FeizaoApp.mConctext.getString(R.string.qq_key);

	// 新浪微博
	public static final String SINA_APPID = FeizaoApp.mConctext.getString(R.string.sina_id);
	public static final String SINA_APPSECRET = FeizaoApp.mConctext.getString(R.string.sina_secret);

	/**
	 * 报错log日志发送邮箱用户名
	 */
	public static final String LOG_SENDEMAIL_NAME = "yanxiaoyang@lonzh.com";
	/**
	 * 报错log日志发送邮箱密码
	 */
	public static final String LOG_SENDEMAIL_PWD = "yanxiaoyang";
	/**
	 * 报错log日志接收的邮箱用户名
	 */
	public static final String LOG_RECIVEMAIL_NAME = "yanxiaoyang@lonzh.com";

	public static void updateBaseDomain() {
		BASE_URL_SERVER = "http://" + BASE_DOMAIN;
		// 用户操作统计
		BASE_STAT_URL_SERVER = "http://" + BASE_STAT_DOMAIN + "/";
		// Web页面
		BASE_M_URL_SERVER = "http://" + BASE_M_DOMAIN;
	}
}
