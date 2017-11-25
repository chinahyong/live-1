package tv.live.bx.common;

/**
 * Created by Administrator on 2017/1/5.
 */

public class WebConstants {
	/**
	 * 开通守护的WEB URl
	 * http://m.guojiang.tv/guard/index/mid/6225   守护页面
	 */
	public static String WEB_MODERATOR_GUARD_URL = "/guard/index/mid/";

	/**
	 * 我的守护的WEB URl
	 * http://m.guojiang.tv/guard/list/uid/1387686   我的守护页面
	 */
	public static String WEB_USER_GUARD_LIST_URL = "/guard/list/uid/";
	/**
	 * 分享直播间地址前缀
	 */
	public static String SHARE_LIVE_PIX = "/room/";
	/**
	 * 分享社区帖子地址前缀
	 */
	public static String SHARE_POST_PIX = "/forum/detail/";
	/**
	 * 分享饭圈社区帖子地址前缀
	 */
	public static String SHARE_GROUP_POST_PIX = "/group/postDetail/id/";

	public static String SHARE_FAN_URL = "/group/invite/gid/%1$s";

	/**
	 * 帮助连接
	 */
	public static String COMMON_HELP = "/help/group";

	/**
	 * 邀请注册
	 */
	public static String SHARE_INVATE_CONTENT = "来陪我一起看直播，更有比心大礼包等着你！我的比心ID是：%1$s~";
	public static String SHARE_INVATE_URL = "/user/invite/uid/%1$s";

	/**
	 * 我的收益url
	 */
	public static String COMMON_ME_INCOME_URL = "/income";

	/**
	 * 申请主播星探url
	 */
//	public static String COMMON_ANCHOR_URL = Consts.BASE_M_URL_SERVER + "/apply/index";
//	/**
//	 * 帮助url
//	 */
//	public static String COMMON_HELP_URL = Consts.BASE_M_URL_SERVER + "/help";

	/**
	 * 等级说明url
	 */
	public static String COMMON_LEVEL_URL = "/help/level";

//	/**
//	 * 星探邀请url
//	 */
//	public static String COMMON_TALENT_INVITE_URL = Consts.BASE_M_URL_SERVER + "/Apply/moderator?talentscout=%1$s";

	/**
	 * 我的等级
	 */
	public static String GET_MY_LEVEL_URL = "/myLevel";
	/**
	 * 主播等级
	 */
	public static String GET_MY_ANCHOR_LEVEL_URL = "/myLevel/moderator";

//	/**
//	 * 我要直播(集赞)
//	 */
//	public static String I_WANT_LIVE = Consts.BASE_M_URL_SERVER + "/unlockLive";
	/**
	 * 注册 用户协议
	 */
	public static String REGISTER_PROTOCOL = "/user/protocol";
	/**
	 * 开播协议URL
	 */
	public static String LIVE_PROTOCAL_WEB_URL = "/liveProtocol";

	/**
	 * 购买私信卡
	 */
	public static String BUY_CONVERSATION_CARD = "/myProps/buy.html";

	/**
	 * 获取我的道具页面
	 */
//	public static String GET_MY_PROPS = Consts.BASE_M_URL_SERVER + "/myProps/index.html";

	/**
	 * 点击关注我的fragment  小字文案 进入h5页面
	 */
	public static String FANS_CARE_NOTICE = "/privateLetter/notice";

	/**
	 * 充值URL
	 */
	public static String RECHARGE_WEB_URL = "/rechargeApp";

	/**
	 * 新手活动
	 */
	public static String NEW_USER_WELFARE = "/dist/activity/noviceWelfare/noviceWelfare.html";

	/**
	 * 商城页面
	 */
	public static String WEB_STORE = "/dist/store/list.html";

	/**
	 * 我的背包
	 */
	public static String WEB_MY_BACKPACK = "/dist/backpack/list.html";

	/**
	 * 操作统计，自研
	 * 正式环境：http://stat.guojiang.tv/
	 */
	public static String REPORT_USER_ACTION = Consts.BASE_STAT_URL_SERVER;

	/**
	 * 我的红包地址
	 */
	public static String REDPACKET_WEB_URL = "/dist/redPacket/index.html";

	/**
	 * 红包邀请地址
	 *
	 * @param uid
	 */
	public static String REDPACKET_INVITE_WEB_URL = "/dist/redPacket/invite.html?inviter=";

	/**
	 * 红包规则地址
	 *
	 * @param uid
	 */
	public static String REDPACKET_HELP_WEB_URL = "/dist/redPacket/rule.html";

	/**
	 * 拼接完整的网页地址
	 *
	 * @return
	 */
	public static String getFullWebMDomain(String url) {
		return Consts.BASE_M_URL_SERVER + url;
	}

}
