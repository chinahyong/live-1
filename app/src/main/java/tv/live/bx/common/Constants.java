package tv.live.bx.common;

import tv.live.bx.FeizaoApp;
import tv.live.bx.library.util.PackageUtil;

public class Constants {

	/**
	 * 当前等级配置版本
	 */
	public static final String COMMON_LEVEL_CONFIG_VERSION = "3";
	/**
	 * 当前勋章配置版本
	 */
	public static final String COMMON_MODEL_CONFIG_VERSION = "0";

	/**
	 * appid ,区分不同的应用，默认果酱直播。目前果酱直播“0”，土豪直播 “2”
	 */
	public static final String PACKAGE_ID = "7";

	/**
	 * 设备类型
	 */
	public static final String DEVICE = "android";

	/**
	 * 测试每个版本提交的build号
	 */
	public static final int COMMON_DEBUG_BUILD_NO = 87;

	/**
	 * 签名值
	 */
	public static final String COMMON_SIGN_MD5 = "f17c3b8d76440f8889dbea0c33fe1ef3";
	/**
	 * 直播缓冲时间
	 */
	public static final int LIVE_BUFFER_TIME = 4000;

	public static final String DESCRIPTOR = "com.umeng.share";
	/**
	 * 设置webview表示
	 */
	public static final String WEB_VIEW_AGENT = " guojiang_android";
	/**
	 * 设置webview表示
	 */
	public static final String WEB_VIEW_AGENT_VERSION = " guojiang_version/";
	/**
	 * 设置webview表示
	 */
	public static final String WEB_VIEW_AGENT_PACKAGE = " guojiang_package/";
	/**
	 * 设置webview标识
	 */
	public static final String WEB_VIEW_AGENT_CHANNEL = " guojiang_channel/";

	/**
	 * SharePrefrace域名信息
	 */
	public static final String COMMON_SF_HTTP_DOMAIN_NAME = "cfg_http_domain";
	public static final String COMMON_SF_BASE_HTTP_DOMAIN = "base_http_domain";
	public static final String COMMON_SF_BASE_DOMAIN = "base_domain";
	public static final String COMMON_SF_BASE_STAT_DOMAIN = "base_stat_domain";
	public static final String COMMON_SF_BASE_M_DOMAIN = "base_m_domain";
	public static final String COMMON_SF_BASE_DOMAIN_LIST = "base_domain_list";

	/**
	 * SharePrefrace普通信息
	 */
	public static final String COMMON_SF_NAME = "cfg";
	/**
	 * SharePrefrace用户信息
	 */
	public static final String USER_SF_NAME = "cf_user";
	/**
	 * SharePrefrace用户信息
	 */
	public static final String USERTASK_SF_NAME = "cf_usertask";
	/**
	 * SharePrefrace发帖图片信息
	 */
	public static final String PHOTO_INFO_SF_NAME = "cf_photo_info";
	/**
	 * SharePrefrace等级信息
	 */
	public static final String USER_SF_LEVEL_NAME = "cf_user_level";
	/**
	 * SharePrefrace勋章信息
	 */
	public static final String USER_SF_MODEL_NAME = "cf_user_model";
	/**
	 * SharePrefrace首页Tag配置信息
	 */
	public static final String SF_TAG_NAME = "cf_tag";

	/**
	 * 首页tag---是否显示推荐页
	 */
	public static final String SF_TAG_SHOW_RECOMMEND = "showRecommend";
	/**
	 * 首页tag---默认显示推荐还是热门 1：热门， 2：推荐
	 */
	public static final String SF_TAG_DEFAULT_INDEX = "liveDefaultTabIndex";

	/**
	 * SharePrefrace帖子板块Id
	 */
	public static final String SF_GROUP_ID = "post_group_id";
	/**
	 * SharePrefrace帖子板块名称
	 */
	public static final String SF_GROUP_NAME = "post_group_name";
	/**
	 * SharePrefrace帖子标题
	 */
	public static final String SF_POST_TITLE = "post_title";
	/**
	 * SharePrefrace帖子内容
	 */
	public static final String SF_POST_CONTENT = "post_content";
	/**
	 * SharePrefrace帖子图片内容
	 */
	public static final String SF_POST_PHOTO_CONTENT = "post_photo_content_";

	/**
	 * SharePrefrace饭圈标题
	 */
	public static final String SF_FAN_TITLE = "fan_title";
	/**
	 * SharePrefrace饭圈标题内容
	 */
	public static final String SF_FAN_CONTENT = "fan_content";
	/**
	 * SharePrefrace饭圈标题图片内容
	 */
	public static final String SF_FAN_PHOTO_CONTENT = "fan_photo_content_";

	/**
	 * SharePrefrace等级配置版本
	 */
	public static final String SF_LEVEL_CONFIG_VERSION = "level_config_version";
	/**
	 * SharePrefrace勋章配置版本
	 */
	public static final String SF_MODEL_CONFIG_VERSION = "model_config_version";
	/**
	 * SharePrefrace帖子是否插入配置版本
	 */
	public static final String SF_INSERT_GROUP_CONFIG_VERSION = "insert_group_config_version";

	/**
	 * SharePrefrace欢迎广告
	 */
	public static final String SF_WELCOME_AD_IMAGE_URL = "sf_welcome_ad_image_url";

	/**
	 * 消息回调的方法名称 start
	 */
	public static final String ON_INIT_ROOM = "onInitRoom";
	public static final String ON_SEND_FLOWER = "onSendFlower";
	public static final String ON_FIRST_SEND_FLOWER = "onFirstSendFlower";
	public static final String ON_USER_ATTENTION = "onUserAttention";
	public static final String ON_USER_SHARE = "onUserShare";
	public static final String ON_LOGIN = "onLogin";
	public static final String ON_LOGOUT = "onLogout";
	public static final String ON_SEND_MSG = "onSendMsg";
	public static final String ON_SEND_GIFT = "onSendGift";
	public static final String ON_VIDEO_PUBLISH = "onVideoPublish";
	public static final String ON_VIDEO_UNPUBLISH = "onVideoUnpublish";
	public static final String ON_CONNECT_STATUS = "onConnectStatus";
	public static final String ON_BAN = "onBan";
	public static final String ON_UN_BAN = "onUnBan";
	public static final String ON_SET_ADMIN = "onSetAdmin";
	public static final String ON_UNSET_ADMIN = "onUnsetAdmin";
	public static final String ON_TI = "onTi";
	public static final String ON_TI_MODERATOR = "onTiModerator";
	public static final String ON_BATCH_LOGIN = "onBatchLogin";
	public static final String ON_BATCH_LOGOUT = "onBatchLogout";
	public static final String ON_NEW_BULLE_BARRAGE = "onNewBulletBarrage";
	public static final String ON_NEW_REWARDS = "onNewRewards";
	public static final String ON_REFRESH_ONLINE_NUM = "onRefreshOnlineNum";
	public static final String ON_MODERATOR_LEVEL_INCREASE = "onModeratorLevelIncrease";
	public static final String ON_USER_LEVEL_INCREASE = "onUserLevelIncrease";
	public static final String ON_SEND_BARRAGE = "onSendBarrage";
	public static final String ON_SYSTEM_MESSAGE = "onSystemMsg";
	public static final String ON_HOT_RANK = "onNewHotRank";            //排名变化的通知
	public static final String ON_MESSAGE_CARD_ACTIVE = "onMessageCardActive";
	public static final String ON_INVITE_VIDEO_CHAT = "onInviteVideoChat";        // 邀请连线
	public static final String ON_CANCEL_VIDEO_CHAT = "onCancelVideoChat";        // 取消连线
	public static final String ON_USER_REJECT_VIDEO_CHAT = "onUserRejectVideoChat";    //用户拒绝主播连线
	public static final String ON_ACCEPT_VIDEO_CHAT = "onAcceptVideoChat";    //接受连线
	public static final String ON_VIDEO_CHAT_END = "onVideoChatEnd";    //连线结束（混流失败，用户关闭，用户断线，主播断线都会发出此命令）
	public static final String ON_CHANGE_VIDEO_PULL_URL = "onUpdateModeratorPullUrl";    //观众更换拉流地址
	public static final String ON_VIDEO_CHAT = "onVideoChat";    //连麦成功广播连麦用户信息


	/**
	 * 发布消息
	 */
	public static final String VIDEO_PUBLISH = "videoPublish";
	public static final String VIDEO_UNPUBLISH = "videoUnpublish";

	/**
	 * 余额不足错误码
	 */
	public static final String PLAY_MESSAGE_ERROR_BALANCE_LEAK = "101";
	/** 消息回调的方法名称 end */

	/**
	 * 视频流 url
	 */
	public static final String PLAY_VIDEO_URL_NEW = "rtmp://%s%s%s";
	/**
	 * 视频流 url
	 */
	public static final String PLAY_VIDEO_URL = "rtmp://rtmppull.efeizao.com/live/room_%s/chat";
	/**
	 * 视频直播流 url
	 */
	public static final String StreamJsonStrFromServer_URL_NEW = "{'id': 'z1.room_4.chat'"
			+ ",'hub': '%s','title': '%s','publishKey': '%s','publishSecurity': 'static','hosts' : {'publish' : {'rtmp':'%s'},'play': {'hls': 'xxx.hls1.z1.pili.qiniucdn.com','rtmp': 'xxx.live1.z1.pili.qiniucdn.com'}}}";
	/**
	 * 视频直播流 url
	 */
	public static final String StreamJsonStrFromServer_URL = "{'id': 'z1.room_4.chat'"
			+ ",'hub': 'room_%s','title': 'chat','publishKey': '%s','publishSecurity': 'static','hosts' : {'publish' : {'rtmp':'rtmppull.efeizao.com/live'},'play': {'hls': 'xxx.hls1.z1.pili.qiniucdn.com','rtmp': 'xxx.live1.z1.pili.qiniucdn.com'}}}";

	/**
	 * 消息流 url
	 */
	public static final String PLAY_MESSAGE_URL = "ws://%s:%s/?sid=%s&uid=%s&rid=%s&platform=android&packageId="
			+ PACKAGE_ID + "&version=" + PackageUtil.getVersionName();


//	public static DisplayImageOptions COMMON_OPTIONIMAGE = new DisplayImageOptions.Builder()
//			.showImageOnLoading(R.drawable.icon_loading).showImageOnFail(R.drawable.icon_loading)
//			.bitmapConfig(Config.RGB_565).cacheOnDisc(true).cacheInMemory(true).considerExifParams(true)
//			.imageScaleType(ImageScaleType.EXACTLY).build();
//
//	public static DisplayImageOptions COMMON_OPTIONIMAGE_80 = new DisplayImageOptions.Builder()
//			.showImageOnLoading(R.drawable.bg_user_default).showImageOnFail(R.drawable.bg_user_default)
//			.bitmapConfig(Config.RGB_565).cacheInMemory(true).cacheOnDisc(true).cacheInMemory(true)
//			.displayer(new RoundedBitmapDisplayer(Utils.dip2px(FeizaoApp.mConctext, 80f))).build();
//
//	public static DisplayImageOptions COMMON_OPTIONIMAGE_2 = new DisplayImageOptions.Builder()
//			.bitmapConfig(Config.RGB_565).cacheInMemory(true).cacheOnDisc(true).cacheInMemory(true)
//			.displayer(new RoundedBitmapDisplayer(Utils.dip2px(FeizaoApp.mConctext, 2.33f))).build();
//
//	public static DisplayImageOptions COMMON_OPTIONIMAGE_1 = new DisplayImageOptions.Builder()
//			.bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.icon_loading).cacheInMemory(true).cacheOnDisc(true)
//			.imageScaleType(ImageScaleType.EXACTLY).displayer(new RoundedBitmapDisplayer(Utils.dip2px(FeizaoApp.mConctext, 1.33f))).build();

	//图片圆角度
	public static int COMMON_DISPLAY_IMAGE_CORNER_1 = (int) (1 * FeizaoApp.metrics.density);
	public static int COMMON_DISPLAY_IMAGE_CORNER_2 = (int) (2 * FeizaoApp.metrics.density);
	public static int COMMON_DISPLAY_IMAGE_CORNER_3 = (int) (3 * FeizaoApp.metrics.density);

	private static final String TIPS = "请移步官方网站 ";
	private static final String END_TIPS = ", 查看相关说明.";
	public static final String TENCENT_OPEN_URL = TIPS + "http://wiki.connect.qq.com/android_sdk使用说明" + END_TIPS;
	public static final String PERMISSION_URL = TIPS + "http://wiki.connect.qq.com/openapi权限申请" + END_TIPS;

	public static final String SOCIAL_LINK = "http://www.umeng.com/social";
	public static final String SOCIAL_TITLE = "友盟社会化组件帮助应用快速整合分享功能";
	public static final String SOCIAL_IMAGE = "http://www.umeng.com/images/pic/banner_module_social.png";

	public static final String SOCIAL_CONTENT = "友盟社会化组件（SDK）让移动应用快速整合社交分享功能，我们简化了社交平台的接入，为开发者提供坚实的基础服务：（一）支持各大主流社交平台，"
			+ "（二）支持图片、文字、gif动图、音频、视频；@好友，关注官方微博等功能" + "（三）提供详尽的后台用户社交行为分析。http://www.umeng.com/social";

	/**
	 * 退出应用时间间隔
	 */
	public static final long EXIT_INTERVAL = 2000L;

	// ImageLoader 图片保存的路径相关:"/Android/data/" + context.getPackageName();
	public static final String IMAGE_CACHE_DIR = "img_cache";
	// crash 日志的路径相关:"/Android/data/" + context.getPackageName();
	public static final String CRASH_DIR = "crash";
	// 下载更新apk目录
	public static final String APK_UPDATE_DIR = "apk_update";

	/**
	 * 提示信息
	 */
	public static final String NETWORK_FAIL = "网络不给力";
	/**
	 * 注册页面密码长度
	 */
	public static final int PWD_MIN_LENGHT = 6;
	public static final int PWD_MAX_LENGHT = 16;
	/**
	 * 网络图片正则表达式
	 */
	public static final String REGX_PHOTO = "<img src=\".*?\" type=\"pic\"/>\n?";
	/**
	 * 表情图片正则表达式
	 */
	public static final String REGX_PHOTO_EMOJI = "<img src=\".*?\" type=\"face\"/>";

	public static final String REPLACE_PHOTO = "[图片]";
	public static final String REPLACE_PHOTO_EMOJI = "[表情]";
	/**
	 * 数字的正则表达式
	 */
	public static final String REGEX_DIGIT = "^[0-9]+$";

	/**
	 * 手机号码的正则表达式
	 */
	public static final String REGULAR_NUMBER = "^((13[0-9])|(14[0-9])|(17[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$";
	public static final String REGULAR_SAME_CHARS = "^([0-9a-zA-Z])\1{5}$";
	public static final String REGULAR_PHONENUM = "1\\d{10}";

	/**
	 * 身份证号码匹配
	 */
	public static final String REGULAR_IDENEIEYCARD = "(\\d{15}$)|(\\d{17}(?:\\d|x|X)$)";

	/**
	 * # 饭圈信息正则表达式 [1,果酱大陆]
	 */
	public static final String REGULAR_GROUP_INFO = "\\[\\d+,.*?\\]";

	/**
	 * 注册界面昵称对应的正则表达式
	 */
	public static final String REGULAR_NICKNAME = ".{2,10}";

	/**
	 * 第一次使用标志，保存在SharedPreferences字段名, "1" 已使用过，“0” 未使用过
	 */
	public static final String isFirstUsered = "is_first_usered";

	/**
	 * 是否更新首页列表数据
	 */
	public static final String UPDATE_LISTVIEW_DATA = "update_listview_data";
	/**
	 * 是否显示首页
	 */
	public static final String SHOW_HOME_PAGE = "show_home_page";

	/**
	 * 极光推送 通知类型
	 */
	public static final String NOTIFICATION_TYPE = "notification_type";
	/**
	 * 通知类型，关注的房间信息，点击进入房间
	 */
	public static final String NOTIFICATION_TYPE_ROOM = "1";
	/**
	 * 通知类型，活动,进入活动页面
	 */
	public static final String NOTIFICATION_TYPE_ACTIVITY = "2";
	/**
	 * 通知类型，帖子 进入帖子详情
	 */
	public static final String NOTIFICATION_TYPE_POST = "3";
	/**
	 * 通知类型，我的消息 进入我的消息页面
	 */
	public static final String NOTIFICATION_TYPE_ME_MESSAGE = "4";

	/**
	 * -1:未登陆用户
	 */
	public static final String USER_TYPE_UNLOGIN = "-1";
	/**
	 * 1:普通用户
	 */
	public static final String USER_TYPE_NORMAL = "1";
	/**
	 * 2:主播
	 */
	public static final String USER_TYPE_ANCHOR = "2";
	/**
	 * 3:管理员
	 */
	public static final String USER_TYPE_ADMIN = "3";
	/**
	 * 4:僵尸用户
	 */
	public static final String USER_TYPE_DEAD = "4";
	/**
	 * 5:既是主播又是星探
	 */
	public static final String USER_TYPE_ROOMOWNER = "5";
	/**
	 * 6:官方普通账号
	 */
	public static final String USER_TYPE_OFFICIAL = "6";
	/**
	 * 7:官方管理员账号,(2.7.0以后没有了）
	 */
	public static final String USER_TYPE_OFFICIAL_ADMIN = "7";
	/**
	 * 用户性别
	 */
	public static final String USER_SEX_MAN = "2";

	/**
	 * 帖子列表最多显示1张图片
	 */
	public static final int SUBJECT_LIST_IMAGE_LIMIT = 1;

	// 列表图片显示的宽高，为90 dp
	public static final int IMAGE_WIDTH = (int) (80 * FeizaoApp.metrics.density);
	// 列表图片显示的宽高，为90 dp
	public static final int IMAGE_HEIGHT = (int) (80 * FeizaoApp.metrics.density);
	/**
	 * 回复类型1，回复“帖子”
	 */
	public static final int REPLY_POST_TYPE_POST = 1;
	/**
	 * 回复类型2，回复“回复”
	 */
	public static final int REPLY_POST_TYPE_REPLY = 2;

	/**
	 * 删除类型1，删除楼层
	 */
	public static final int DELETE_REPLY = 1;
	/**
	 * 删除类型2，删除“回复”
	 */
	public static final int DELETE_LZL_REPLY = 2;

	/**
	 * 请求帖子的每页条数
	 */
	public static final int REQUEST_POST_LIMIT = 20;

	/**
	 * 送花的内容
	 */
	public static final String FLOWER_MSG = "{花}";

	/**
	 * 保持 与websocket连接 5分钟之内
	 */
	public static final int KEEP_WEBSOCKET_CONNENT_TIME = 3 * 60 * 1000;
	/**
	 * 排行前缀 1,2,3等名
	 */
	public static final String USER_RANK_PIX = "icon_rank_";

	/**
	 * 社区表情图片名称前缀
	 */
	public static final String COMMON_EMOTION_PIX = "emoji_";
	/**
	 * 用户类别的图片名称前缀
	 */
	public static final String USER_TYPE_PIX = "usertype_";
	/**
	 * 用户等级的图片名称前缀
	 */
	public static final String USER_LEVEL_PIX = "userlevel_";
	/**
	 * 用户守护等级的图片名称前缀
	 */
	public static final String USER_GUARD_LEVEL_PIX = "user_guard_level_";

	/**
	 * 用户勋章的url base
	 */
	public static final String USER_MODEL_PIX_BASE = "usermodel_base";
	/**
	 * 用户等级的图片名称前缀
	 */
	public static final String USER_ANCHOR_LEVEL_PIX = "user_anchor_";

	/**
	 * onActivityResult requestCode未登录，请求登录
	 */
	public static int REQUEST_CODE_LOGIN = 100;

	/**
	 * 系统消息 房间信息
	 */
	public static String SYSTEM_MSG_TYPE_ROOM = "room";
	/**
	 * 系统消息 网页信息
	 */
	public static String SYSTEM_MSG_TYPE_PAGE = "url";
	/**
	 * 弹幕消息 用户信息
	 */
	public static String SYSTEM_MSG_TYPE_USER = "user";
	/**
	 * 系统消息 任务信息
	 */
	public static String SYSTEM_MSG_TYPE_USER_TASK = "userTask";
	/**
	 * 系统消息 个人中心
	 */
	public static String SYSTEM_MSG_TYPE_PERSION_INFO = "userDetail";
	/**
	 * 系统消息 饭圈详情
	 */
	public static String SYSTEM_MSG_TYPE_GROUP_DETAIL = "groupDetail";
	/**
	 * 系统消息 饭圈帖子详情
	 */
	public static String SYSTEM_MSG_TYPE_GROUP_POST = "groupPost";
	/**
	 * 系统消息 饭圈帖子详情楼中楼
	 */
	public static String SYSTEM_MSG_TYPE_GROUP_POST_REPLY = "groupPostFloorReply";
	/**
	 * 系统消息 饭圈帖子楼中楼回复
	 */
	public static String SYSTEM_MSG_TYPE_GROUP_POST_LZL_REPLY = "groupPostLzlReply";
	/**
	 * 系统消息 饭圈首页
	 */
	public static String SYSTEM_MSG_TYPE_GROUP = "group";

	/** [status] -1:删除/0:禁用/1:启用/2:未审核/3:审核不通过/4:冻结 */
	/**
	 * 启用
	 */
	public static String FAN_STATUS_NORMAL = "1";
	/**
	 * 冻结
	 */
	public static String FAN_STATUS_FREEZE = "4";

	public static String SHARE_FAN_TITLE = "%1$s的饭圈,有%2$s人在这里玩耍，就差你了";

	public static String SHARE_FAN_CONTENT = "%1$s\n#找到组织，你不是一个人#";

	/**
	 * 星探邀请标题
	 */
	public static String COMMON_TALENT_INVITE_TITLE = "快来报名成为比心主播！";
	/**
	 * 星探邀请内容
	 */
	public static String COMMON_TALENT_INVITE_CONTENT = "点我报名成为比心主播，美好的你有美好的未来！";

	/**
	 * 字段为真 的字符串形式
	 */
	public static String COMMON_TRUE = "true";

	/**
	 * 字段为真 1 为假 0
	 */
	public static String COMMON_TRUE_NUM = "1";

	/**
	 * 插入帖子的饭圈的字符“#”
	 */
	public static String COMMON_INSERT_POST_PIX = "#";

	/**
	 * 1 房间 ; 2 帖子 ; 3 回复
	 */
	public static String COMMON_REPORT_TYPE_ROOM = "1";
	public static String COMMON_REPORT_TYPE_POST = "2";
	public static String COMMON_REPORT_TYPE_REPLY = "3";

	public static final String FILE_PXI = "file://";

	/**
	 * 礼物特效金额 （单位：泡泡）
	 */
	public static final int COMMON_GIFT_EFFECT_CONSUME = 100;
	/**
	 * 普通礼物
	 */
	public static final String COMMON_GIFT_TYPE_NORMAL = "0";
	/**
	 * 免费礼物 ：点点
	 */
	public static final String COMMON_GIFT_TYPE_FREE = "1";
	/**
	 * 房间封面长宽比例
	 */
	public static final float COMMON_LIVE_ROOM_LOGO_RATE = 0.56f;

	/* 融云免打擾 */
	public static final String SLEEP_START_TIME = "00:00:00";    // 起始时间格式：   HH：mm：ss
	public static final int SLEEP_DELAY = 1439;        //时间间隔： >0   <1440

}
