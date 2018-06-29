package com.bixin.bixin.common.bean;

/**
 * Title: XXXX (类或者接口名称) Description: XXXX (简单对此类或接口的名字进行描述) Copyright:
 * Copyright (c) 2012
 * @version 1.0
 */
public class HttpNetConstants {

    // 服务器返回格式参数
    public static final String SERVER_RESULT_CODE = "errno";
    public static final String SERVER_RESULT_MSG = "msg";
    public static final String SERVER_RESULT_DATA = "data";

    public static String BASE_HTTP_DOMAIN = "app.gjlive.cn";
    public static String BASE_HTTP_DOMAIN_BAK = "[\"app.gj2018.cn\",\"app.gj2017.cn\"]";
    public static String BASE_DOMAIN = "app.ohf2e.com";
    public static String BASE_STAT_DOMAIN = "stat.guojiang.tv";
    public static String BASE_M_DOMAIN = "m.ohf2e.com";

    public static String BASE_URL_SERVER = "http://" + BASE_DOMAIN;
    // 用户操作统计
    public static String BASE_STAT_URL_SERVER = "http://" + BASE_STAT_DOMAIN + "/";
    // Web页面
    public static String BASE_M_URL_SERVER = "http://" + BASE_M_DOMAIN;

    // 等待发送
    public static final String SENT_STATUS_WAIT = "WAIT";
    // 服务器出错
    public static final String SENT_STATUS_ERROR_SERVER = "ERROR_SERVER";
    // 网络出错
    public static final String SENT_STATUS_ERROR_NET = "ERROR_NET";
    // 超时
    public static final String SENT_STATUS_TIMEOUT = "TIMEOUT";
    // DNS 解析失败
    public static final String SENT_STATUS_DNS_ERROR = "DNS_ERROR_SERVER";

    // 请求失败，需要登录
    public static final String SENT_STATUS_NEED_LOGIN = "-100";
    // 请求过于频繁
    public static final String SENT_STATUS_NEED_VALIDATE = "-300";
    // 请求成功
    public static final String SENT_STATUS_SUCCESS = "0";


    /**
     * qq登录
     */
    public static String LOGIN_BY_QQ = "/user/QQLogin";

    /**
     * 微信登录
     */
    public static String LOGIN_BY_WECHAT = "/user/WxLogin";

    /**
     * 微博登录
     */
    public static String LOGIN_BY_SINA = "/user/weiboLogin";
    /**
     * 获取公玥
     */
    public static String GET_PUBKEY_URL = "/user/getPublicKey";
    /**
     * 获取横幅图片信息接口URL
     */
    public static String BANNER_URL = "/index/focusActivity";
    /**
     * 获取主播列表信息接口URL
     */
    public static String AUTHOR_LIST_URL = "/room/getRooms";

    /**
     * 通过标签获取主播列表信息接口URL
     */
    public static String AUTHOR_LIST_BY_TAG_URL = "/room/getRoomsByTag";

    /**
     * 获取附近主播列表信息接口URL
     */
    public static String GET_NEAR_AUTHOR_LIST_URL = "/room/getRoomsByLocation";

    /**
     * 获取主播报名状态
     */
    public static String ANCHOR_STATUS = "/moderatorApply/getMyApplyInfo";
    /**
     * 提交主播报名信息
     */
    public static String SUBMIT_ANCHOR_INFO = "/moderatorApply/save";

    /**
     * 获取获取手机直播房间URL
     */
    public static String AUTHOR_MOBILE_LIST_URL = "/room/getMobileRooms";
    /**
     * 获取最新版本接口URL
     */
    public static String GET_NEW_VERSION_URL = "/app/getLastestVersion/platform/android";
    /**
     * 获取用户信息
     */
    public static String GET_USER_INFO_URL = "/user/getMyInfo";
    /**
     * 退出登录
     */
    public static String LOGOUT_USER_URL = "/user/logout";
    /**
     * 更新用户信息
     */
    public static String UPDATE_USER_INFO_URL = "/user/updateInfo";
    /**
     * 获取房间信息
     */
    public static String GET_ROOM_INFO_URL = "/room/getInfo";
    /**
     * 提交直播标题
     */
    public static String SUBMIT_LIVE_TITLE = "/room/updateAnnouncement";

    /**
     * 是否可以开播
     */
    public static String GET_LIVE_STATUS = "/room/canILive";

    /**
     * 关注用户
     */
    public static String GET_FOLLOW_URL = "/user/attention";

    /**
     * 取消关注用户
     */
    public static String GET_REMOVE_FOLLOW_URL = "/user/removeAttention";

    /**
     * 举报
     */
    public static String REPORT_INFO_URL = "/App/userReport";

    /**
     * 房间排行
     */
    public static String ROOM_RANK_URL = "/room/getRank";

    /**
     * 排行榜单
     */
    public static String GET_RANK_URL = "/user/rankList";

    /**
     * 获取用户关注列表信息
     */
    public static String GET_LOVE_LIST_URL = "/user/getUserAttentions";

    /**
     * 获取用户粉丝列表信息
     */
    public static String GET_FANS_LIST_URL = "/user/getUserFans";

    // 获取推流的信息
    public static String GET_LIVE_STREAM_URL = "/room/requestLiveAddress";

    // 获取微信预支付订单
    public static String GET_PRE_PAY_DATA = "/recharge/wxpay";

    /**
     * 获取qq预支付信息
     */
    public static String GET_PRE_QQPAY_DATA = "/recharge/qqWalletPay";

    /**
     * 获取支付宝支付订单
     */
    public static String GET_ALI_PAY_DATA = "/recharge/alipayApp";

    /**
     * 获取签到状态
     */
    public static String GET_SIGN_STATUS_DATA = "/user/getsignstatus";

    /**
     * 签到
     */
    public static String GET_SIGN_URL = "/user/signarrive";

    /**
     * 获取任务列表
     */
    public static String SHARE_REPORT_URL = "/App/shareReport";

    /**
     * 分享上报
     */
    public static String GET_TAST_LIST_URL = "/user/getmissions";

    /**
     * 获取配置信息
     */
    public static String GET_CONFIG_URL = "/app/getConfig";

    /**
     * 等级配置信息
     */
    public static String GET_LEVEL_CONFIG_URL = "/app/getLevelConfig";
    /**
     * 勋章配置信息
     */
    public static String GET_MODEL_CONFIG_URL = "/app/getMedalsConfig";
    /**
     * 精品推荐列表
     */
    public static String GET_PRODUCT_RECOMMENT_URL = "/app/applist";
    /**
     * 获取搜索主播列表
     */
    public static String SEARCH_ANCHOR_LIST_URL = "/room/searchRooms";
    /**
     * 获取系统消息列表
     */
    public static String GET_SYSTEM_MSG_LSIT_URL = "/message/getMessages";

    /**
     * 获取自研系统消息列表，结合融云
     */
    public static String GET_CONVERSATION_SYSTEM_MSG_LSIT_URL = "/IM/getMessageList";
    /**
     * 设置系统消息已读
     */
    public static String SET_SYSTEM_MSG_READ = "/message/getUnreadMessageNumInfo";

    /**
     * 获取邀请人信息
     */
    public static String GET_INVATE_INFO_URL = "/User/affliateInfo";
    /**
     * 提交邀请人
     */
    public static String SUBMIT_INVATE_URL = "/User/addReferrer";

    /************************************************ 2.0版本新接口 **************************/
    /**
     * 获取我的关注列表信息
     */
    public static String GET_ME_FOCUS_ANCHOR_URL = "/user/getMyAttentionModerators";
    /**
     * 获取直播预告信息
     */
    public static String GET_PUBLISH_ROOM_URL = "/room/getWillPublishRooms";
    /**
     * 获取个人用户信息
     */
    public static String GET_PERSON_INFO_URL = "/user/getUserInfo";

    /**
     * 设置预播时间信息
     */
    public static String SET_PLAY_TIME_URL = "/room/setMyPlayStartTime";
    /**
     * 删除预播时间信息
     */
    public static String DELETE_PLAY_TIME_URL = "/room/removeMyPlayStartTime";

    /**
     * 用户禁言
     */
    public static String USER_BAN_URL = "/Group/userBan";
    /**
     * 用户取消禁言
     */
    public static String CANCEL_USER_BAN_URL = "/Group/userBanRemove";
    /**
     * 绑定微信
     */
    public static String GET_BIND_WEIXIN_URL = "/User/wxBind";
    /**
     * 房间封面
     */
    public static String EDIT_ROOM_LOGO_URL = "/room/updateLogo";
    /**
     * 直播间泡泡排行榜
     */
    public static String GET_LIVE_P_RANK = "/room/getRoomRankList";

    /**
     * 同意开播协议
     */
    public static String AGREE_PROTOCAL = "/room/signAgree";
    /***************************** 饭圈帖子接口 end ************************************/

    /**
     * 上传用户主页背景图
     */
    public static String UPLOAD_USER_BG_IMG = "/user/uploadBgImg";
    /**
     * 获取话题列表
     */
    public static String GET_TOPICS = "/room/getTopics";
    /**
     * 上报设备ID
     */
    public static String REPORT_DEVICE_ID_URL = "/app/reportDeviceId";
    /**
     * 上报同盾黑盒
     */
    public static String REPORT_BLACK_BOX_URL = "/app/reportTdDeviceInfo";

    /**
     * 融云server端 获取黑名单接口
     */
    public static String GET_BLACKLIST_URL = "/IM/getMyBlackList";

    /**
     * 批量查询消息用户数据
     */
    public static String GET_MESSAGE_USER_INFO_URL = "/IM/getMessageUserInfo";
    /******************************** 2.6.5添加新接口*************************************/
    /**
     * 獲取房管列表
     */
    public static String GET_ROOM_MANAGER_LIST = "/room/getAdmins";

    /**
     * 移除房管
     */
    public static String REMOVE_ROOM_MANAGER = "/room/unsetAdmin";

    /**
     * 設置房管
     */
    public static String SET_ROOM_MANAGER = "/room/setAdmin";

    /**
     * 上报极光注册ID
     */
    public static String JPUSH_REGISTER_ID = "/app/reportJPushRegisterationId";

    /******************************** 2.7.0添加新接口*************************************/
    /**
     * 获取推荐关注列表
     */
    public static String RECOMMEND_ATTENTIONS = "/room/getAttentionRecMods";

    /**
     * 批量提交关注
     */
    public static String COMMIT_RECOMMEND_ATTENTIONS = "/user/attentionMulti";
    /**
     * 进场特效开关
     */
    public static String COME_IN_ANIM_SWITCH = "/user/lowkeyEnterSwitch";

    /**
     * 私信发送消息
     */
    public static String SEND_PRIVATE_MSG = "/IM/SendPrivateMsg";
    /**
     * 获取粉丝或关注列表(私信相关)
     */
    public static String GET_FANS_CARE_INFO = "/IM/UserFans";
    /**
     * 群发或者单发
     */
    public static String SEND_PRIVATE_MESSAGE_INFO = "/IM/MultiMessage";

    /**
     * 获取物品信息
     */
    public static String GET_USER_PACKAGE_URL = "/userPackage/getPackageInfo";

    /**
     * 绑定手机号码
     */
    public static String PHONE_BIND_URL = "/user/mobileBind";

    /**
     * http://app.guojiang.tv/user/getMobileBindVCode
     */
    public static String GET_PHONE_BIND_CODE_URL = "/user/getMobileBindVCode";
    /**
     * 获取守护列表
     */
    public static String GET_MODERATOR_GUARD_URL = "/guard/getModeratorGuards";

    /**
     * 获取支付宝认证授权参数
     */
    public static String GET_ALIPLAY_LOGIN_URL = "/user/alipayUserAuthConf";

    /**
     * 提交支付宝认证
     */
    public static String SET_ALIPLAY_AUTH_CODE_URL = "/user/alipayUserAuth";

    /**
     * 获取房间个人信息卡信息
     */
    public static String GET_ROOM_USER_INFO_URL = "/room/getUserInfo";


    /**
     * 热门排名信息
     */

    public static String GET_HOT_RANK_INFO_URL = "/room/getHotRankInfo";

    /**
     * 官方推荐的信息
     */
    public static String GET_OFFICIAL_RECOMMEND_URL = "/room/getRecommend";


    /**
     * 使用私信卡接口
     */
    public static String USER_MESSAGE_CARD = "/IM/activeMessageCard";

    /**
     * 获取守护礼物
     */
    public static String GET_GUARD_GIFT = "/room/getGuardGifts";

    /**
     * app init
     */
    public static String APP_INIT = "/app/init";

    /**
     * 上传相册排序
     */
    public static String SORT_ALBUM = "/gallery/resortPic";

    /**
     * 删除相片
     */
    public static String DELETE_ALBUM = "/gallery/deletePic";

    /**
     * 上传相册图片
     */
    public static String UPLOAD_ALBUM = "/gallery/upload";

    /**
     * 获取相册列表
     */
    public static String GET_ALBUM = "/gallery/getGalleryByUid";

    /**
     * 发起私播
     */
    public static String PRIVATE_LIVE = "/privateRoom/invite";

    /**
     * 发起连线邀请
     */
    public static String REQUEST_VIDEO_CHAT = "/liveStream/requestVideoChat";

    /**
     * 取消连线邀请
     */
    public static String CANCEL_VIDEO_CHAT = "/liveStream/cancelVideoChat";

    /**
     * 用户忽略主播连线邀请
     */
    public static String REJECT_VIDEO_CHAT = "/liveStream/rejectVideoChat";

    /**
     * 接受连线邀请
     */
    public static String ACCEPT_VIDEO_CHAT = "/liveStream/acceptVideoChat";

    /**
     * 混流请求
     */
    public static String REQUEST_MIX_STREAM = "/liveStream/mixStream";

    /**
     * 结束连线
     */
    public static String REQUEST_END_VIDEO_CHAT = "/liveStream/endVideoChat";

    /**
     * 获取验证码配置信息
     */
    public static String GET_VALIDATE_CODE_CONFIG = "/vcode/getCode";

    /**
     * 验证验证码
     */
    public static String VALIDATE_CODE = "/vcode/verifyCode";

    /**
     * 获取红包cd
     */
    public static String GET_RED_PACKET_CD = "/red/find";

    /**
     * 领取红包
     */
    public static String GET_RED_PACKET = "/red/get";

    /**
     * 上报坐标
     */
    public static String POST_POSITION = "/user/position";

    /**
     * 拼接完整的网页地址
     */
    public static String getFullRequestUrl(String url) {
        return BASE_URL_SERVER + url;
    }

    public static void updateBaseDomain() {
        BASE_URL_SERVER = "http://" + BASE_DOMAIN;
        // 用户操作统计
        BASE_STAT_URL_SERVER = "http://" + BASE_STAT_DOMAIN + "/";
        // Web页面
        BASE_M_URL_SERVER = "http://" + BASE_M_DOMAIN;
    }
}
