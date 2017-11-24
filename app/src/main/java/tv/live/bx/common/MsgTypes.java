package tv.live.bx.common;

public class MsgTypes {
	// 欢迎页等待时间
	public static final int WELCOME_ACTIVITY_WAIT_TIMEOUT = 0;

	// 主页Banner轮播时间
	public static final int MAIN_ACTIVITY_BANNER_LOOPER = 1;

	// 玫瑰花刷新事件
	public static final int FLOWER_LOOP = 2;

	// 用户登录
	public static final int LOGIN_SUCCESS = 10;
	public static final int LOGIN_FAILED = 11;

	// 获取首页Banner数据
	public static final int GET_MAIN_BANNERS_SUCCESS = 20;
	public static final int GET_MAIN_BANNERS_FAILED = 21;

	// 获取首页推荐列表
	public static final int GET_RECOMMEND_ROOMS_SUCCESS = 30;
	public static final int GET_RECOMMEND_ROOMS_FAILED = 31;

	public static final int MSG_LOAD_SUCCESS = 0x10;
	public static final int MSG_LOAD_FAILED = 0x11;

	// 用户注册发送验证码
	public static final int SEND_REGISTER_VERIFY_CODE_SUCCESS = 40;
	public static final int SEND_REGISTER_VERIFY_CODE_FAILED = 41;

	// 用户注册校验验证码
	public static final int CHECK_REGISTER_VERIFY_CODE_SUCCESS = 50;
	public static final int CHECK_REGISTER_VERIFY_CODE_FAILED = 51;

	// 用户注册
	public static final int REGISTER_SUCCESS = 60;
	public static final int REGISTER_FAILED = 61;

	// 获取服务器公钥
	public static final int GET_PUB_KEY_SUCCESS = 70;
	public static final int GET_PUB_KEY_FAILED = 71;

	// 关注
	public static final int FOLLOW_SUCCESS = 80;
	public static final int FOLLOW_FAILED = 81;

	// 获取房间信息
	public static final int GET_ROOM_INFO_SUCCESS = 90;
	public static final int GET_ROOM_INFO_FAILED = 91;

	// 退出登录
	public static final int LOGOUT_SUCCESS = 100;
	public static final int LOGOUT_FAILED = 101;

	// 修改用户信息
	public static final int MODIFY_USER_INFO_SUCCESS = 110;
	public static final int MODIFY_USER_INFO_FAILED = 111;

	// QQ登录
	public static final int LOGIN_BY_QQ_SUCCESS = 120;
	public static final int LOGIN_BY_QQ_FAILED = 121;

	// 获取本人用户信息
	public static final int GET_MY_USER_INFO_SUCCESS = 130;
	public static final int GET_MY_USER_INFO_FAILED = 131;

	// 获取用户信息
	public static final int GET_USER_INFO_SUCCESS = 132;
	public static final int GET_USER_INFO_FAILED = 133;

	// 获取报名信息
	public static final int GET_ANCHOR_STATUS_SUCCESS = 150;
	public static final int GET_ANCHOR_STATUS_FAILED = 151;

	// 提交报名信息
	public static final int SUBMIT_ANCHOR_STATUS_SUCCESS = 152;
	public static final int SUBMIT_ANCHOR_STATUS_FAILED = 153;

	public static final int ADD_AUDIENCE = 140; // 新增观众
	public static final int DEL_AUDIENCE = 141; // 删除观众
	public static final int ON_CHAT_MSG = 142; // 聊天信息
	public static final int ON_LOAD_VIDEO_SUCCESS = 143; // 加载视频流成功

	public static final int FEEDBACK_SUCCESS = 160; // 反馈成功
	public static final int FEEDBACK_FAILED = 161; // 反馈失败

	// 获取最新版本信息
	public static final int GET_LAST_VERSION_SUCCESS = 170;
	public static final int GET_LAST_VERSION_FAILED = 171;

	// 举报
	public static final int REPORT_ILLEGAL_SUCCESS = 180;
	public static final int REPORT_ILLEGAL_FAILED = 181;

	// 找回密码
	public static final int GETBACK_LOGIN_PWD_SUCCESS = 190;
	public static final int GETBACK_LOGIN_PWD_FAILED = 191;

	// 获取关注列表
	public static final int GET_LOVE_LIST_SUCCESS = 200;
	public static final int GET_LOVE_LIST_FAILED = 201;

	// 获取粉丝列表
	public static final int GET_FOLLOWED_LIST_SUCCESS = 210;
	public static final int GET_FOLLOWED_LIST_FAILED = 211;

	// 下拉刷新
	public static final int REQUEST_TYPE_REFRESH = 220;// 刷新
	public static final int REQUEST_TYPE_LOADMORE = 221;// 加载更多

	// 获取房间排行版
	public static final int MSG_ROOM_RANK_SUCCESS = 230;
	public static final int MSG_ROOM_RANK_FAILED = 231;

	// 获取排行榜单
	public static final int MSG_RANK_SUCCESS = 240;
	public static final int MSG_RANK_FAILED = 241;

	// 获取社区帖子列表
	public static final int MSG_SUBJECT_SUCCESS = 250;
	public static final int MSG_SUBJECT_FAILED = 251;

	// 点赞
	public static final int MSG_SUPPORT_SUCCESS = 260;
	public static final int MSG_SUPPORT_FAILED = 261;

	// 获取帖子回复列表
	public static final int MSG_REPLY_LIST_SUCCESS = 270;
	public static final int MSG_REPLY_LIST_FAILED = 271;

	// 帖子回复
	public static final int MSG_REPLY_SUCCESS = 280;
	public static final int MSG_REPLY_FAILED = 281;

	// 收藏帖子
	public static final int MSG_COLLECT_SUCCESS = 290;
	public static final int MSG_COLLECT_FAILED = 291;

	// 收藏帖子列表
	public static final int MSG_COLLECT_LIST_SUCCESS = 300;
	public static final int MSG_COLLECT_LIST_FAILED = 301;

	// 我的回复列表
	public static final int MSG_ME_REPLY_LIST_FAILED = 311;
	public static final int MSG_ME_REPLY_LIST_SUCCESS = 310;

	// 我的帖子详情
	public static final int MSG_POST_DETAIL_SUCCESS = 320;
	public static final int MSG_POST_DETAIL_FAILED = 321;

	// 我的消息列表
	public static final int MSG_ME_MESSAGE_LIST_SUCCESS = 330;
	public static final int MSG_ME_MESSAGE_LIST_FAILED = 331;

	// 发表帖子列表
	public static final int MSG_PUBLISH_LIST_SUCCESS = 340;
	public static final int MSG_PUBLISH_LIST_FAILED = 341;

	// 删除回复
	public static final int MSG_DELETE_REPLAY_SUCCESS = 350;
	public static final int MSG_DELETE_REPLAY_FAILED = 351;

	// 获取帖子板块
	public static final int MSG_POST_MOUDLE_SUCCESS = 360;
	public static final int MSG_POST_MOUDLE_FAILED = 361;

	// 发布帖子
	public static final int MSG_PUBLIC_POST_SUCCESS = 370;
	public static final int MSG_PUBLIC_POST_FAILED = 371;

	// 获取帖子
	public static final int MSG_GET_LIVESTREAM_SUCCESS = 380;
	public static final int MSG_GET_LIVESTREAM_FAILED = 381;

	// 直播状态消息
	public static final int MSG_LIVE_STATUS = 390;
	public static final int MSG_UPDATE_LIVE_TIME = 391;
	// 微信支付
	public static final int MSG_PRE_PAY_SUCCESS = 392;
	public static final int MSG_PRE_PAY_FAILED = 393;

	// 支付宝支付
	public static final int MSG_ALI_PAY_SUCCESS = 394;
	public static final int MSG_ALI_PAY_FAILED = 395;

	// 获取任务列表
	public static final int MSG_GET_TASK_LIST_SUCCESS = 401;
	public static final int MSG_GET_TASK_LIST_FAILED = 402;

	// 获取精品推荐列表
	public static final int GET_PRODUCT_RECOMMENT_LIST_SUCCESS = 410;
	public static final int GET_PRODUCT_RECOMMENT_LIST_FAILED = 411;

	// 获取搜索主播列表
	public static final int SEARCH_ANCHOR_LIST_SUCCESS = 420;
	public static final int SEARCH_ANCHOR_LIST_FAILED = 421;

	// 我的评论列表
	public static final int MSG_ME_COMMENT_LIST_SUCCESS = 430;
	public static final int MSG_ME_COMMENT_LIST_FAILED = 431;

	// 饭圈推荐列表
	public static final int MSG_FAN_RECOMMENT_LIST_SUCCESS = 440;
	public static final int MSG_FAN_RECOMMENT_LIST_FAILED = 441;

	// 我的饭圈列表（包括我创建与我加入的）
	public static final int MSG_ME_FAN_LIST_SUCCESS = 450;
	public static final int MSG_ME_FAN_LIST_FAILED = 451;

	// 饭圈列表
	public static final int MSG_FAN_LIST_SUCCESS = 460;
	public static final int MSG_FAN_LIST_FAILED = 461;

	// 加入饭圈
	public static final int MSG_ADD_FAN_SUCCESS = 470;
	public static final int MSG_ADD_FAN_FAILED = 471;

	// 搜索饭圈
	public static final int MSG_FAN_SERACH_SUCCESS = 480;
	public static final int MSG_FAN_SERACH_FAILED = 481;

	// 饭圈详情
	public static final int MSG_GROUP_DETAIL_SUCCESS = 490;
	public static final int MSG_GROUP_DETAIL_FAILED = 491;

	// 获取饭圈管理员
	public static final int MSG_GROUP_ADMIN_SUCCESS = 500;
	public static final int MSG_GROUP_ADMIN_FAILED = 501;
	// 获取饭圈管理员
	public static final int MSG_GROUP_POST_LIST_SUCCESS = 510;
	public static final int MSG_GROUP_POST_LIST_FAILED = 511;

	// 成员列表
	public static final int SEARCH_MENBER_LIST_SUCCESS = 520;
	public static final int SEARCH_MENBER_LIST_FAILED = 521;

	// 设置管理员/取消管理员
	public static final int MSG_FAN_ADMIN_SUCCESS = 530;
	public static final int MSG_FAN_ADMIN_FAILED = 531;

	// 退出饭圈
	public static final int MSG_QUIT_FAN_SUCCESS = 540;
	public static final int MSG_QUIT_FAN_FAILED = 541;

	// 删除帖子
	public static final int MSG_FAN_REMOVE_POST_SUCCESS = 550;
	public static final int MSG_FAN_REMOVE_POST_FAILED = 551;

	// 置顶帖子
	public static final int MSG_FAN_TOP_SUCCESS = 560;
	public static final int MSG_FAN_TOP_FAILED = 561;

	// 加精帖子
	public static final int MSG_FAN_RECOMMENT_SUCCESS = 570;
	public static final int MSG_FAN_RECOMMENT_FAILED = 571;

	// 创建更新详情
	public static final int MSG_FAN_CREATE_UPDATE_SUCCESS = 580;
	public static final int MSG_FAN_CREATE_UPDATE_FAILED = 581;

	// 获取饭圈或饭圈帖子列表
	public static final int MSG_FAN_SUBJECT_SUCCESS = 590;
	public static final int MSG_FAN_SUBJECT_FAILED = 591;

	// 获取用户创建饭圈数
	public static final int MSG_CREATE_FAN_NUM_SUCCESS = 600;
	public static final int MSG_CREATE_FAN_NUM_FAILED = 601;

	/************************
	 * 2.0新接口
	 ************************************/
	public static final int GET_PUBLISH_ROOM_SUCCESS = 610;
	public static final int GET_PUBLISH_ROOM_FAILED = 611;

	public static final int GET_LIVE_FOCUS_SUCCESS = 620;
	public static final int GET_LIVE_FOCUS_FAILED = 621;

	// 删除帖子
	public static final int MSG_DELETE_POST_SUCCESS = 630;
	public static final int MSG_DELETE_POST_FAILED = 631;

	// 饭圈推荐列表
	public static final int MSG_FAN_ME_LIST_SUCCESS = 640;
	public static final int MSG_FAN_ME_LIST_FAILED = 641;

	// 未读消息数
	public static final int MSG_UNREAD_MESSAGE_SUCCESS = 650;
	public static final int MSG_UNREAD_MESSAGE_FAILED = 651;

	// 取消关注用户
	public static final int REMOVE_FOLLOW_SUCCESS = 660;
	public static final int REMOVE_FOLLOW_FAILED = 661;

	public static final int GET_FOCUS_USER_INFO_SUCCESS = 670;
	public static final int GET_FOCUS_USER_INFO_FAILED = 671;

	public static final int SET_LVIE_TIME_SUCCESS = 680;
	public static final int SET_LVIE_TIME_FAILED = 681;

	public static final int REMOVE_LVIE_TIME_SUCCESS = 690;
	public static final int REMOVE_LVIE_TIME_FAILED = 691;

	public static final int GET_CONFIG_INFO_SUCCESS = 700;
	public static final int GET_CONFIG_INFO_FAILED = 701;

	// 禁言
	public static final int MSG_GROUP_BAN_SUCCESS = 710;
	public static final int MSG_GROUP_BAN_FAILED = 711;

	// 饭圈管理日志
	public static final int MSG_GROUP_LOG_SUCCESS = 720;
	public static final int MSG_GROUP_LOG_FAILED = 721;

	// 微信绑定
	public static final int MSG_BIND_SUCCESS = 733;
	public static final int MSG_BIND_FAILED = 734;

	// 编辑房间图片
	public static final int MSG_EIDT_ROOM_LOGO_SUCCESS = 740;
	public static final int MSG_EIDT_ROOM_LOGO_FAILED = 741;

	// 提交房间直播标题
	public static final int MSG_EIDT_ROOM_TITLE_SUCCESS = 750;
	public static final int MSG_EIDT_ROOM_TITLE_FAILED = 751;

	// 获取泡泡排行榜
	public static final int MSG_GET_LIVE_P_RANK_SUCCESS = 760;
	public static final int MSG_GET_LIVE_P_RANK_FAILED = 761;

	// 获取用户直播状态
	public static final int GET_LIVE_STATUS_SUCCESS = 770;
	public static final int GET_LIVE_STATUS_FAILED = 771;

	// 上传用户个人主页背景
	public static final int UPLOAD_BG_SUCCESS = 772;
	public static final int UPLOAD_BG_FAILED = 773;

	// 获取黑名单列表
	public static final int GET_BLACK_LIST_INFO_SUCCESS = 670;
	public static final int GET_BLACK_LIST_INFO_FAILED = 671;

	//获取融云黑名单列表
	public static final int GET_CONVERSATION_BLACK_LIST_FAILED = 672;
	public static final int GET_CONVERSATION_BLACK_LIST_SUCCESS = 673;

	//添加融云黑名单列表
	public static final int ADD_CONVERSATION_BLACK_FAILED = 674;
	public static final int ADD_CONVERSATION_BLACK_SUCCESS = 675;

	//移除融云黑名单列表
	public static final int REMOVE_CONVERSATION_BLACK_LIST_FAILED = 676;
	public static final int REMOVE_CONVERSATION_BLACK_LIST_SUCCESS = 677;

	// 获取会话列表用户信息
	public static final int GET_MSG_USER_INFO_SUCCESS = 680;
	public static final int GET_MSG_USER_INFO_FAILED = 681;

	// 获取用户背包物品信息
	public static final int GET_USER_PACKAGE_SUCCESS = 690;
	public static final int GET_USER_PACKAGE_FAILED = 691;

	// 绑定手机号码
	public static final int PHONE_BIND_SUCCESS = 700;
	public static final int PHONE_BIND_FAILED = 701;

	// 获取主播守护列表
	public static final int GET_MODERATOR_GUARD_SUCCESS = 710;
	public static final int GET_MODERATOR_GUARD_FAILED = 711;

	// 支付宝登录参数
	public static final int MSG_ALI_LOGIN_SUCCESS = 720;
	public static final int MSG_ALI_LOGIN_FAILED = 721;

	// 提交支付宝授权信息
	public static final int MSG_ALI_AUTH_CODE_SUCCESS = 730;
	public static final int MSG_ALI_AUTH_CODE_FAILED = 731;


	//获取热门排名的信息
	public static final int GET_HOT_RANK_SUCCESS = 740;
	public static final int GET_HOT_RANK_FAILED = 741;

	//获取关注、粉丝列表的fragment的信息
	public static final int MSG_FAN_CARE_LIST_FAILED = 750;
	public static final int MSG_FAN_CARE_LIST_SUCCESS = 751;

	//点击全选之后的信息
	public static final int MSG_SELECT_RECEIVER_SUCCESS = 760;
	public static final int MSG_SELECT_RECEIVER_FAILED = 761;

	//群发消息成功的回调
	public static final int MSG_SEND_MESSAGE_SUCCESS = 770;
	public static final int MSG_SEND_MESSAGE_FAILED = 771;

	//官方推荐的信息
	public static final int GET_OFFICIAL_RECOMMEND_SUCCESS = 780;
	public static final int GET_OFFICIAL_RECOMMEND_FAILED = 781;

	//开始私播成功
	public static final int MSG_START_PRIVATE_LIVE_SUCCESS = 790;
	public static final int MSG_START_PRIVATE_LIVE_FAILED = 791;

	// QQ支付
	public static final int MSG_PRE_QQPAY_SUCCESS = 800;
	public static final int MSG_PRE_QQPAY_FAILED = 801;
}
