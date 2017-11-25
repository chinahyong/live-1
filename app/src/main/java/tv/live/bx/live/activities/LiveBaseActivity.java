package tv.live.bx.live.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.airbnb.lottie.LottieAnimationView;
import com.gj.effect.EffectComposition;
import com.gj.effect.EffectGiftLoader;
import com.gj.effect.GJEffectView;
import com.lonzh.lib.network.HttpSession;
import com.lonzh.lib.network.JSONParser;
import com.lonzh.lib.network.LZCookieStore;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.umeng.analytics.MobclickAgent;
import com.viewpagerindicator.IconPageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import cn.efeizao.feizao.framework.net.NetConstants;
import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import cn.efeizao.feizao.ui.dialog.PersonInfoCustomDialogBuilder;
import cn.efeizao.feizao.ui.dialog.RedPacketDialogBuilder;
import de.tavendo.autobahn.WebSocket;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.ui.widget.DanmakuView;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.CreateFanActivity;
import tv.live.bx.activities.FanDetailActivity;
import tv.live.bx.activities.LoginActivity;
import tv.live.bx.activities.ShareDialogActivity;
import tv.live.bx.activities.WebViewActivity;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.ChatListAdapter;
import tv.live.bx.adapters.GiftsGridAdapter;
import tv.live.bx.adapters.GiftsGridAdapter.IGiftGridItemOnClick;
import tv.live.bx.adapters.GiftsNumAdapter;
import tv.live.bx.adapters.GiftsNumAdapter.IGiftNumItemListener;
import tv.live.bx.adapters.IconPageAdapter;
import tv.live.bx.adapters.ModeratorGuardAdapter;
import tv.live.bx.callback.MyUserInfoCallbackDataHandle;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.GTValidateRequest;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.common.WebConstants;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.danmu.DanmuBase.BroadcastDanmakuChannel;
import tv.live.bx.danmu.DanmuBase.DanmakuActionManager;
import tv.live.bx.danmu.DanmuBase.DanmakuChannel;
import tv.live.bx.danmu.DanmuBase.DanmakuEntity;
import tv.live.bx.emoji.SelectFaceHelper;
import tv.live.bx.emoji.SelectFaceHelper.OnFaceOprateListener;
import tv.live.bx.fragments.LiveChatFragment;
import tv.live.bx.fragments.LiveChatFragment.OnListViewTouchListener;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.DateUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.HtmlUtil;
import tv.live.bx.live.danmaku.DanmakuViewCommon;
import tv.live.bx.live.ui.CameraPreviewRelativeView;
import tv.live.bx.model.GiftEffectViewData;
import tv.live.bx.model.GiftEffectViewHold;
import tv.live.bx.receiver.ConnectionChangeReceiver;
import tv.live.bx.receiver.ConnectionChangeReceiver.NetwrokChangeCallback;
import tv.live.bx.ui.AutoVerticalLinearLayout;
import tv.live.bx.ui.BonusToast;
import tv.live.bx.ui.FavorLayout;
import tv.live.bx.ui.GiftNumKeybordDialog;
import tv.live.bx.ui.HorizontalListView;
import tv.live.bx.ui.MyWebView;
import tv.live.bx.ui.RippleBackground;
import tv.live.bx.ui.StrokeTextView;
import tv.live.bx.ui.SwipeBackLayout;
import tv.live.bx.ui.TypeTextView;
import tv.live.bx.ui.popwindow.LiveGiftNumPopWindow;
import tv.live.bx.ui.popwindow.LiveHotRankPopWindow;
import tv.live.bx.ui.popwindow.LiveUserManageDialog;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.websocket.WebSocketLiveEngine;
import tv.live.bx.websocket.live.WebSocketFlowerCallBack;
import tv.live.bx.websocket.live.WebSocketFlowerHandler;
import tv.live.bx.websocket.live.WebSocketLiveCallBack;
import tv.live.bx.websocket.live.WebSocketLiveHandler;
import tv.live.bx.websocket.model.AcceptVideoChat;
import tv.live.bx.websocket.model.InviteVideoChat;
import tv.live.bx.websocket.model.VideoChat;


/**
 * ClassName: LiveBaseActivity <br/>
 * Function: 这是直播基础类，页面功能
 *
 * @author Administrator
 * @since JDK 1.6
 */
public class LiveBaseActivity extends BaseFragmentActivity implements CameraPreviewRelativeView
		.Listener, OnClickListener, WebSocketLiveCallBack, IGiftGridItemOnClick,
		IGiftNumItemListener {
	// 请求进入个人中心
	protected static final int REQUEST_CODE_PERSONINFO = 0x210;
	private static final int RECHANGE_REQUEST_CODE = 0x201;
	private static final int REQUEST_CODE_MESSAGE_CARD = 0x202;
	private static final int REQUEST_CODE_NEW_WELFARE = 0x203;
	//进入商场
	private static final int REQUEST_CODE_GOTO_SHOP = 0x204;

	public static final String ANCHOR_RID = "anchor_rid";
	public static final String ANCHOR_PRIVATE = "anchor_private";
	public static final String ROOM_INFO = "room_info";
	// 分享
	public static final String SHARE_TITLE = "来来来~有话躺着说！";
	public static final String SHARE_XXX = "xxx";
	public static final String SHARE_CONTENT = "来来来有话躺着说~我是" + SHARE_XXX + "，我正在比心直播，快来一起看~";

	// 发起连线的 用户身份
	protected static final int TYPE_LIVE_ANCHOR = 2;
	protected static final int TYPE_LIVE_USER = 1;

	/**
	 * 创建饭圈
	 */
	private static final int CREATE_FAN_REQUESTCODE = 0x100;

	/**
	 * 观众数改变
	 */
	private static final int MSG_AUDIENCE_NUM_CHANGE = 0x1104;
	/**
	 * 余额不足
	 */
	public static final int MSG_SEND_GIFT_BLANCE_LACK = 0x1105;
	private static final int MSG_SEND_GIFT_SUCCESS = 0x1106;
	private static final int MSG_SHOW_GIF_GIFT_SUCCESS = 0x1107;
	private static final int MSG_SEND_FLOWER_SUCCESS = 0x1108;

	/**
	 * 系统消息msg
	 */
	private static final int MSG_SYSTEM_MESSAGE = 0x1109;
	// 付费弹幕消息
	private static final int MSG_PAY_DANMU_MESSAGE = 0x1110;
	// 开通守护，更新守护列表
	private static final int MSG_PAY_DANMU_OPEN_GUARD_MESSAGE = 0x1111;
	// 激活私信卡
	private static final int MSG_MESSAGE_CARD_ACTIVE = 0x1112;

	// 连击倒计时
	private static final int MSG_TIME_COUNT_RUNNABLE = 0x1113;

	//更新新人福利倒计时
	private static final int MSG_TIME_UPDATE_NEW_USER_WELFARE = 0x1114;
	//邀请连线
	protected static final int MSG_INVITE_VIDEO_CHAT_SUCC = 0x1127;
	protected static final int MSG_INVITE_VIDEO_CHAT_FAIL = 0x1128;
	//取消邀请连线
	protected static final int MSG_CANCEL_VIDEO_CHAT_SUCC = 0x1122;
	protected static final int MSG_CANCEL_VIDEO_CHAT_FAIL = 0x1123;
	//接受连线
	protected static final int MSG_ACCEPT_VIDEO_CHAT_SUCC = 0x1124;
	protected static final int MSG_ACCEPT_VIDEO_CHAT_FAIL = 0x1125;
	//连线结束（一方关闭或者断线）
	protected static final int MSG_VIDEO_CHAT_END_SUCC = 0x1129;
	protected static final int MSG_VIDEO_CHAT_END_FAIL = 0x1130;
	//用户拒绝主播连线
	protected static final int MSG_USER_REJECT_VIDEO_CHAT_SUCC = 0x1117;
	protected static final int MSG_USER_REJECT_VIDEO_CHAT_FAIL = 0x1118;
	//获取红包CD
	private static final int MSG_GET_RED_PACKET_CD_SUCC = 0x1131;
	private static final int MSG_GET_RED_PACKET_CD_FAIL = 0x1132;
	//领取红包
	private static final int MSG_GET_RED_PACKET_SUCC = 0x1133;
	private static final int MSG_GET_RED_PACKET_FAIL = 0x1134;
	/**
	 * 送礼物成功提示信息
	 */
	private final String GIFT_SEND_SUCCESS = "赠送成功";

	private final static int MODEL_HOT = 1;
	private final static int MODEL_BAG = 2;
	private final static int MODEL_GUARD = 3;

	private static final int COUNT_MAX_PACKAGE_GIFT = 500;//背包礼物送出不能超过500


	protected static final int SHOW_FOCUS_DELAY = 1;//未关注开始计时
	protected static final int SHOW_FOCUS_NEED = 2;//计时结束，退出时显示
	protected static final int SHOW_FOCUS_NOT = 3;//不需要显示
	protected int mIsNeedFocus = SHOW_FOCUS_DELAY;//显示提示关注对话框
	protected static final int MSG_SHOW_FOCUS_DIALOG = 0x5001;

	/**
	 * 礼物面板状态
	 */
	private int mGiftViewModel = MODEL_HOT;

	private boolean mbActivityRunning;
	// private boolean mbFlowerLoaded;

	/**
	 * 消息服务
	 */
	protected WebSocketLiveEngine mWebSocketImpl;
	/**
	 * 送花消息服务
	 */
	protected WebSocketLiveEngine mFlowerWebSocketImpl;
	/**
	 * websocket 发送心跳包定时器
	 */
	private Timer mSendHeartTimer;
	// 当主动stopwebsocket或者connectStutas错误就不再重连，默认为false
	private boolean mWebSocketConnectStatus = false;
	private boolean mWebSocketConnectStatusFlower = false;

	/**
	 * 网络监听广播
	 */
	private ConnectionChangeReceiver networkReceiver;
	/**
	 * 当前是否无网络
	 */
	protected boolean mNoNetworkFlag = false;
	/**
	 * 当前是否wifi网络
	 */
	private boolean isWifiNetwork = false;
	/**
	 * 网络提示仅仅提示一次
	 */
	private boolean onlyNetworkTip = false;
	/**
	 * 是否充值完成获取用户信息
	 */
	private boolean mIsRechangeFlag = false;

	/**
	 * 房间数据
	 */
	protected Map<String, Object> mmRoomInfo;
	protected Map<String, String> mmIntentRoomInfo;
	protected Map<String, String> mmAnchorInfo;

	/**
	 * 饭圈信息
	 */
	private Map<String, String> mFanInfo;

	protected AlertDialog mProgress;

	/**
	 * 退出房间 返回按键之间的时间
	 */
	private long mExitTimeMillis;

	/**
	 * 观看用户Id
	 */
	protected String mUid;

	/**
	 * 键盘是否弹出
	 */
	private boolean isKeyboardUp = false;

	/**
	 * 点击发送监听
	 */
	private OnSendMsg sendMsg = new OnSendMsg();

	// private AnchorInfoPopWindow anchorInfoPopWindow;
	private PersonInfoCustomDialogBuilder mPersonInfoDialogBuidler;
	protected List<String> mManagerUids = new ArrayList<>(); // 房管列表(在房间信息接口获取，只包含用户id)

	//管理dialog
	private LiveUserManageDialog mUserManageDialog;

	/**
	 * 目前屏幕的类型，默认竖屏
	 */
	protected int mCurrentScreenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

	/**
	 * 直播显示的主要界面布局
	 */
	protected CameraPreviewRelativeView mLiveMainLayout;
	/**
	 * 可滑动的控件
	 */
	private SwipeBackLayout mSwipeBackLayout;
	/**
	 * 聊天、排行 fragment
	 */
	private FragmentManager fragmentManager;

	// 弹幕工具类
	protected DanmakuViewCommon mDanmakuViewCommon;
	// 弹幕
	private IDanmakuView mDanmakuView;

	// 自定义弹幕工具类
	private LinearLayout mDanmakuChannelLayout;
	private DanmakuChannel mDanmakuChannelA, mDanmakuChannelB;
	private DanmakuActionManager mDanmakuActionManager;
	private OnClickListener mDanmakuClickListener;

	// 自定义弹幕广播类
	protected BroadcastDanmakuChannel mDanmakuBroastcastA, mDanmakuBroastcastB;
	protected DanmakuActionManager mDanmakuBroastcastActionManager;

	// 活动数据累计调用js(修复活动页未加载完成，未找到js函数bug)，ConcurrentLinkedQueue保证线程安全
	protected ConcurrentLinkedQueue<JSONObject> urlDatas = new ConcurrentLinkedQueue<>();

	// 抽奖活动Url
	protected String mActivityBoxUrl;
	// 标记活动页是否加载完成
	private boolean webFlag;
	// 开宝箱面板
	private MyWebView mWebView;

	/* ###################守护信息start############ */
	protected RelativeLayout mGuardLayout;
	private ImageView mGuardArrow;
	private ImageView mGuardLogo;
	private HorizontalListView mGuardHorizontalListView;
	private ModeratorGuardAdapter mGuardAdapter;
	//私播用户头像
	protected ImageView mPrivateUserPhoto;
	/* ###################守护信息end############ */

	/* ###################顶部活动############ */
	protected LinearLayout mPlayingActivityRigthLayout;
	protected RelativeLayout mPlayingActivityLayout;
	private ProgressBar mPlayingActivityProgress;
	private TextView mPlayingActivityText, mPlayingActivityText2;
	private ImageView mPlayingActivityLogo;
	private String mActivityId, mActivityTargetNum;
	private LayerDrawable myGrad; // 进度条对应drawable
	private ClipDrawable clipDrawable; // 进度条进度对应drawable
	/* #################顶部活动 ############# */
	/* ###################顶部左侧活动############ */
	protected LinearLayout mPlayingActivityLeftLayout;
	protected RelativeLayout mPlayingActivityWeekLayout;
	private ProgressBar mPlayingActivityWeekProgress;
	private TextView mPlayingActivityWeekText, mPlayingActivityWeekText2;
	private ImageView mPlayingActivityWeekLogo;
	private String mActivityWeekId, mActivityWeekTargetNum;
	private LayerDrawable myWeekGrad; // 进度条对应drawable
	private ClipDrawable clipDrawableWeek; // 进度条进度对应drawable
	/* #################顶部左侧活动 ############# */

	/* ############# 新手福利 ############## */
	private LinearLayout mNewUserWelfareLayout;
	private TextView mNewUserWelfareTv;
	private Timer mNewUserWelfareTimer;
	private long mDeadLineTimeDifference;
	/* ############# 红包福利 ############## */
	private LinearLayout mRedPacketLayout;
	private TextView mTvRedPacket;
	private LottieAnimationView mIvRedPacket;
	private Timer mRedPacketTimer;
	private RedPacketDialogBuilder mRedPacketBuilder;
	private long mRedPacketTime;

	/* ####################底部输入框start############## */
	private LinearLayout mLiveInputLayout;
	protected ToggleButton mLiveDanmuBtn;
	private EditText moEtContent;
	private ImageView moIvEmotion;
	private ImageView moIvClearInput;
	private Button moBtnSend;
	private LinearLayout moGvEmotions;
	private SelectFaceHelper mFaceHelper;
	/* ####################底部输入框end############## */

	/* ####################礼物面板start############## */
	// 礼物切换按钮
	private Button mGiftSwitchGeneral, mGiftSwitchGuard, mGiftSwitchGuard2;
	// 守护开通按钮
	private Button mGuardOpenBtn;
	// 守护开通提示布局
	private LinearLayout mGuardOpenTipLayout;
	// 背包礼物未读标示
	private ImageView mGiftGuardUnReadIv;
	// 礼物切换选中
	private View mGiftSwitchGeneralView, mGiftSwitchGuardView, mGiftSwitchGuardView2;
	// 普通礼物面板
	private LinearLayout mLiGeneralGift;
	// 守护礼物面板
	private LinearLayout mLiGuardGift, mLiGuardGift2;
	// 礼物数量面板
	private LinearLayout mLiNumGift;
	// 礼物面板的指示器
	private IconPageIndicator mIndicator, mGuardIndicator, mGuardIndicator2, mNumIndicator;
	// 礼物面板的viewpage
	private ViewPager giftViewPager;
	// 守护礼物面板的viewpage
	private ViewPager mGuardGiftViewPager, mGuardGiftViewPager2;
	// 礼物数量面板
	private ViewPager mGiftNumViewPager;
	// 背包物品GridView
	private List<GridView> mGuardGridView;
	// 获取房间的“普通”礼物列表数据
	private List<Map<String, String>> mGiftsData;
	// 获取房间的“守护”礼物列表数据
	protected List<Map<String, String>> mGuardGiftsData2;
	// “背包”礼物数据
	protected Map<String, Object> mPackageGiftsData;
	// 当前选中礼物信息
	private Map<String, String> giftSelectedData;
	// 当前选中礼物布局
	private View mGiftSelectedView;
	// 赠送礼物按钮
	private Button moBtnSendGifts;
	// 礼物数据，默认是1
	private TextView giftsNum;
	// 用户账户余额
	private String mBalance = "0";
	// 用户点点账户余额
	private String mDianDianBalance = "0";
	private TextView mBalanceTv;
	private LinearLayout mLiBalance;
	// 礼物数量弹出按钮
	private GiftNumKeybordDialog giftNumDialog;
	private LiveGiftNumPopWindow addNumPopWindow;
	// 礼物面板主布局
	protected RelativeLayout mGiftLayout;
	// 礼物布局
	private LinearLayout mLiveGiftlayout;
	// 当前选中礼物Iv
	private ImageView mLiveCurGiftIv;
	// 选择礼物数量
	private Button mLiveBtnNum;
	// 当前礼物数量
	private int mCurGiftNum = 1;
	//背包--商城
	private ImageButton mIbtnShopBag;

	// 热门排名的popupwindow是否显示(默认刚进去是不显示的)
	public boolean mIsHotRankPopUpWindowShowing = false;
	protected int mBandPosition = -1; // 打榜礼物在第几页的第几个
	protected int mBandPagePosition = -1; // 打榜礼物在第几页

	// *************************************************

	/* ####################礼物面板end############## */

	/* ####################私信面板end############## */

	/* ####################聊天、排行start############## */
	// 聊天
	protected LiveChatFragment chatFragment;
	private FrameLayout mLiveChatLayout;
	public Runnable mShowMenu;
	// 礼物，排行动画
	protected Animation mDownOutAnimation, mDownInAnimation, mRightOutAnimation, mRightInAnimation;
	/* ####################聊天、排行end############## */

	/* ########################主播个人资料start######################## */
	private TextView moTvPnickname;
	private ImageView moIvPPhoto;
	private ImageView moIvPPhotoV;
	// 主播个人信息布局
	private RelativeLayout mLiveAnchorLayout;

	// 右上角水印
	protected RelativeLayout mLiveWateMarkLayout;
	private TextView moTvUid, moTvDate;

	// 观众数
	private int mAudienceNum;
	// 主播鲜花数
	private long mFlowwerNum;
	// 泡泡数
	private long mPaopaoNum;
	// 直播间的观众数
	protected TextView mLiveAudience;
	private TextView mRoomIdTv;
	private TextView mTvEarnCoin;
	private RelativeLayout mPRankShowLayout;
	/* ########################主播个人资料end######################## */

	/* ########################顶部，底部start######################## */

	/**
	 * 免费热度
	 */
	private FavorLayout mFavorLayout;

	private ImageView mLiveFlowerIv;

	/**
	 * 底部四个主要的菜单按钮
	 */
	private Button mLiveChatIv;        //发送消息按钮

	protected Button mLiveConnectIv;
	private RelativeLayout mLiveConnectLayout;    //连线按钮整体
	protected LottieAnimationView mLiveConenctAnim;
	protected TextView mLiveConnectNum;

	protected Button moRlGift; // 礼物按钮
	protected Button mLiveScreenSwitchBtn;
	protected Button mLiveShare; // 分享
	private Button mLiveConversationMessage; // 私信按钮
	private ImageView mIvUnReadConversation;

	// 礼物未读消息
	private ImageView mIvGiftUnRead;

	// 底部菜单栏布局
	private LinearLayout mLiveButtomMenulayout;
	private FrameLayout mLiveButtomGiftLayout;
	private View mView;

	// 顶部控件
	private RelativeLayout mLiveToplayout;
	protected Button mLiveReportBtn;
	protected Button mLiveClose;
	// 创建饭圈
	protected Button mLiveCreateFan;

	/* ########################顶部，底部end######################## */

	/* ########################动画效果start######################## */
	// 正在大额礼物动画
	private boolean mGifShowing = false;
	private GJEffectView mGJEffectView;
	protected RelativeLayout mGiftGifInfoLayout;
	private ImageView mGiftGiftUserPhoto;
	private StrokeTextView mGiftGiftUserName, mGiftGiftTip;
	/**
	 * 礼物gif动效数据
	 */
	protected BlockingQueue<Map<String, String>> mGiftGifQueue = new LinkedBlockingQueue<>();

	/**
	 * 连击动效父布局
	 */
	private LinearLayout mGiftEffectParentLayout;
	protected List<FrameLayout> mGiftEffectLayouts = new ArrayList<>();
	private List<GiftEffectViewData> mGiftEffectDatas = new ArrayList<>();
	/**
	 * 礼物特效动画
	 */
	private Animation mGiftLayoutTransAnim, mGiftTransAnim, mGiftInvisibleAnim;
	private AnimationSet mGiftScaleAnim;
	private final int MSG_GIFT_EFFECt_VIEW1 = 0x1101;
	private final int MSG_GIFT_EFFECt_VIEW2 = MSG_GIFT_EFFECt_VIEW1 + 1;
	private final int MSG_GIFT_EFFECt_VIEW3 = MSG_GIFT_EFFECt_VIEW1 + 2;
	/**
	 * 暴击倒计时默认值
	 */
	private static final int TIMES_SECONDS_DEFAULT = 5;
	private static final int TIMES_SECONDS_M_MAX = 99;
	private static final String TIMES_SECONDS_M_MIN = "00";
	// 暴击相关
	private RelativeLayout mRlCountBonusTimes; // 暴击最外层父布局；
	private RippleBackground mRippleCountBonusTime; // 按钮父布局
	private Button mBtnCountBonusTime; // 连击按钮
	private TextView mTvCountBonusTime; // 倒计时 秒
	private RelativeLayout mRlBonusTimes; // 中奖最外层父布局
	private ImageView mIvBonusTimesNum, mIvBonusTimesBg; // 暴击获取奖励，倍率图片；背景图片;暴击按钮动画背景
	private long mBonusTimesSeconds = TIMES_SECONDS_DEFAULT * 1000; // 默认秒数
	private Timer mBonusTimer = null; // 暴击连击计时器
	protected BlockingQueue<String> mBonusTimesQueue = new LinkedBlockingQueue<>();
	private AnimationSet mBonusTimeNumAnim, mBonusTimeBgAnim;
	private boolean isShowBonusFlag = false; // 动画是否正在执行
	/**
	 * 礼物显示时间
	 */
	private final int GIFT_SHOW_TIME = 4000;

	/* ########################动画效果end######################## */

	/* ####################### 用户进入动效 start ################ */
	protected FrameLayout mUserEntryLayout;
	private RelativeLayout mUserEntryBackgroud;
	private ImageView mUserEntryLevel, mUserEntryEffect;
	private TypeTextView mUserEntryText;
	private TextView mUserGuardEntryText;
	private Animation mUserEntryLayoutTransAnimation, mUserEntryLevelAlphaAnimation,
			mUserEntryEffectTranAnimation;
	// 用户进入直播间队列
	private BlockingQueue<Map<String, String>> mUserEntryQueue = new LinkedBlockingQueue<>();
	//最近进入的用户列表
	private LinkedHashMap<String, Long> mRecentUser;
	//座驾进程动画
	private GJEffectView mGJEffectEntry;
	// 高级用户（有隐身功能）
	protected static final String LVIE_USER_HIGH_LEVEL = "17";
	//当前用户进场动画数
	private int mCurrentUserEntryEffectNum = 0;
	/* 显示高级用户进入直播间动效 */
	private static final int MSG_USER_ENTRY_EFFECT_HANDLE = 0x1120;
	protected ClickUsernameListener mClickUsernameListener;
	/* ####################### 用户进入动效 end ################ */


	/* ###################### 主播升级提示 start################## */
	private TypeTextView mAnchorLevelUpTip1, mAnchorLevelUpTip2, mAnchorLevelUpTip3;
	private RelativeLayout mAnchorLevelUpLayout;
	private ImageView mAnchorBackgroupIv, mAnchorLevel, mAnchorLevelAnim;
	private Animation mAnchorBackgroupRotateAnim, mAnchorRocketTranAnim;
	/* 显示主播升级动效 */
	private static final int MSG_ANCHOR_LEVEL_UP_HANDLE = 0x1121;
	// 主播升级队列
	private BlockingQueue<JSONObject> mAnchorLevelUpQueue = new LinkedBlockingQueue<>();
	/* ###################### 主播升级提示 end################## */

	protected String shareContent; // "果酱||鲜肉大叔妖男Young，基腐宅萌有果酱,快来看****的直播，美CRY！！";
	protected String shareTitle; // "果酱直播";
	protected String shareUrImg; // "http://www.guojiang.tv/img/roomlogo/poyin.jpg";
	protected String shareUrl; // "http://www.guojiang.tv";

	protected InputMethodManager mInputMethodManager;

	/***
	 * 热门排名layout
	 **/
	protected RelativeLayout mHotRankLayout;
	protected LiveHotRankPopWindow mHotRankPopupWindow;
	public TextView mNowRank;
	public ImageView mHotRankArrow;

	/***
	 * 底部右侧 连线layout
	 **/
	protected RelativeLayout mConnectLivingLayout;
	protected LinearLayout mConnectAudioLivingLayout, mConnectCloseLayout;
	protected ImageView mConnectAudioLivingHeadPic, mConnectAudioLivingLevel, mConnectLivingClose;
	protected TextView mConnectAudioLivingName, mConnectLivingCloseSure, mConnectLivingCloseCancel;
	protected TXCloudVideoView mLiveConnectVideoView;
	//是否连麦中
	protected boolean mIsLiveConnecting;

	/* ###################### 私播模块 start################## */
	protected static int PRIVATE_USER_UID = 0;
	protected static int PRIVATE_USER_USERNAME = 1;
	protected static int PRIVATE_USER_PHOTO = 2;
	protected ArrayList<SparseArray<String>> mUserPhotoArray = new ArrayList<>();
	//显示进入私播用户头像
	protected static final int MSG_UPDATE_USER_PHOTO = 0x3001;
	//是否私播直播间
	protected boolean mIsPrivatePlay = false;
	/* ###################### 私播模块 end################## */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		isSystemBarTint = false;
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.include_live_play_control_layout;
	}

	@Override
	protected void initMembers() {
		// AndroidBug5497Workaround.assistActivity(this);
		mSwipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipebacklayout);
		mSwipeBackLayout.attachToActivity(mActivity);
		mGiftEffectParentLayout = (LinearLayout) findViewById(R.id.live_batter_effect_layout);
		mGJEffectEntry = (GJEffectView) findViewById(R.id.live_entry_effect);
		mGJEffectView = (GJEffectView) findViewById(R.id.live_gift_effect);
		mGiftGifInfoLayout = (RelativeLayout) findViewById(R.id.live_gift_gifview_info);
		mGiftGiftUserPhoto = (ImageView) findViewById(R.id.item_gif_user_photo);
		mGiftGiftUserName = (StrokeTextView) findViewById(R.id.item_gif_user_name);
		mGiftGiftTip = (StrokeTextView) findViewById(R.id.item_gif_gift_name);
		mGiftEffectLayouts.add((FrameLayout) findViewById(R.id.item_gift_group1));
		initGiftEffectView(mGiftEffectLayouts.get(0));
		mGiftEffectLayouts.add((FrameLayout) findViewById(R.id.item_gift_group2));
		initGiftEffectView(mGiftEffectLayouts.get(1));
		mGiftEffectLayouts.add((FrameLayout) findViewById(R.id.item_gift_group3));
		initGiftEffectView(mGiftEffectLayouts.get(2));

		mDanmakuView = (DanmakuView) findViewById(R.id.sv_danmaku);
		// ((TextureView) mDanmakuView).setZOrderOnTop(true);
		//		((TextureView) mDanmakuView).getHolder().setFormat(PixelFormat.TRANSLUCENT);

		mDanmakuChannelLayout = (LinearLayout) findViewById(R.id.danmu_layout);
		mDanmakuChannelA = (DanmakuChannel) findViewById(R.id.danA);
		mDanmakuChannelB = (DanmakuChannel) findViewById(R.id.danB);

		mDanmakuBroastcastA = (BroadcastDanmakuChannel) findViewById(R.id.broadcastDanmuA);
		mDanmakuBroastcastB = (BroadcastDanmakuChannel) findViewById(R.id.broadcastDanmuB);
		// 用户进入动效
		mUserEntryLayout = (FrameLayout) findViewById(R.id.item_user_entry_layout);
		mUserEntryBackgroud = (RelativeLayout) mUserEntryLayout.findViewById(R.id
				.item_user_entry_bg);
		mUserEntryEffect = (ImageView) mUserEntryLayout.findViewById(R.id.item_effect_background);
		mUserEntryLevel = (ImageView) mUserEntryLayout.findViewById(R.id.item_level);
		mUserGuardEntryText = (TextView) mUserEntryLayout.findViewById(R.id
				.item_user_guard_entry_text);
		mUserEntryText = (TypeTextView) mUserEntryLayout.findViewById(R.id.item_user_entry_text);

		mPlayingActivityLayout = (RelativeLayout) findViewById(R.id.playing_activity_layout);
		mPlayingActivityProgress = (ProgressBar) findViewById(R.id.playing_activity_progress);
		mPlayingActivityText = (TextView) findViewById(R.id.playing_activity_text);
		mPlayingActivityText2 = (TextView) findViewById(R.id.playing_activity_text2);
		mPlayingActivityLogo = (ImageView) findViewById(R.id.playing_activity_logo);

		mPlayingActivityLeftLayout = (LinearLayout) findViewById(R.id.playing_activity_left_layout);
		mPlayingActivityRigthLayout = (LinearLayout) findViewById(R.id
				.playing_activity_linearlayout);
		mPlayingActivityWeekLayout = (RelativeLayout) findViewById(R.id
				.playing_activity_week_layout);
		mPlayingActivityWeekProgress = (ProgressBar) findViewById(R.id
				.playing_activity_week_progress);
		mPlayingActivityWeekText = (TextView) findViewById(R.id.playing_activity_week_text);
		mPlayingActivityWeekText2 = (TextView) findViewById(R.id.playing_activity_week_text2);
		mPlayingActivityWeekLogo = (ImageView) findViewById(R.id.playing_activity_week_logo);

		mNewUserWelfareLayout = (LinearLayout) findViewById(R.id.live_new_user_welfare_layout);
		mNewUserWelfareTv = (TextView) findViewById(R.id.live_new_user_welfare_tv);

		mRedPacketLayout = (LinearLayout) findViewById(R.id.live_red_packet_layout);
		mTvRedPacket = (TextView) findViewById(R.id.live_red_packet_tv);
		mIvRedPacket = (LottieAnimationView) findViewById(R.id.live_red_packet_iv);

		mLiveMainLayout = (CameraPreviewRelativeView) findViewById(R.id.play_main_layout);
		mLiveAnchorLayout = (RelativeLayout) findViewById(R.id.live_anchor_info_layout);

		mLiveClose = (Button) findViewById(R.id.live_btn_exit);
		moIvPPhoto = (ImageView) findViewById(R.id.fragment_playing_other_anchor_iv_photo);
		moIvPPhotoV = (ImageView) findViewById(R.id.fragment_playing_other_anchor_iv_photo_v);
		moTvPnickname = (TextView) findViewById(R.id.fragment_playing_other_anchor_tv_nickname);

		mLiveWateMarkLayout = (RelativeLayout) findViewById(R.id.live_watermark_layout);
		moTvUid = (TextView) findViewById(R.id.playing_activity_tv_uid);
		moTvDate = (TextView) findViewById(R.id.playing_activity_tv_date);

		mLiveAudience = (TextView) findViewById(R.id.live_audience);
		// reChangeBtn = (Button) findViewById(R.id.gift_recharge);
		mRoomIdTv = (TextView) findViewById(R.id.roomId);
		mTvEarnCoin = (TextView) findViewById(R.id.playing_p_total);

		mPRankShowLayout = (RelativeLayout) findViewById(R.id.playing_ranking_layout_show);

		mLiveInputLayout = (LinearLayout) findViewById(R.id.playing_ll_chat);

		mLiveChatLayout = (FrameLayout) findViewById(R.id.live_chat_fragment);

		mLiveCreateFan = (Button) findViewById(R.id.live_create_fan);
		mLiveChatIv = (Button) findViewById(R.id.live_chat);
		mLiveConnectIv = (Button) findViewById(R.id.live_connect);
		mLiveConnectLayout = (RelativeLayout) findViewById(R.id.live_connect_menu_layout);
		mLiveConenctAnim = (LottieAnimationView) findViewById(R.id.live_connect_anim);
		mLiveConnectNum = (TextView) findViewById(R.id.live_connect_num);
		moRlGift = (Button) findViewById(R.id.playing_rl_gift);
		mLiveReportBtn = (Button) findViewById(R.id.live_report);
		mLiveScreenSwitchBtn = (Button) findViewById(R.id.live_switch_srceen);
		mLiveShare = (Button) findViewById(R.id.live_share);
		mLiveConversationMessage = (Button) findViewById(R.id.live_conversation_message);
		mIvUnReadConversation = (ImageView) findViewById(R.id.live_conversation_message_unread);
		mIvGiftUnRead = (ImageView) findViewById(R.id.live_gift_unread);

		mFavorLayout = (FavorLayout) findViewById(R.id.favorLayout);

		mLiveFlowerIv = (ImageView) findViewById(R.id.gifts_bottom_info_img);

		mLiGeneralGift = (LinearLayout) findViewById(R.id.playing_gifts_general);
		mLiGuardGift = (LinearLayout) findViewById(R.id.playing_gifts_guard);
		mLiGuardGift2 = (LinearLayout) findViewById(R.id.playing_gifts_guard_2);
		mLiNumGift = (LinearLayout) findViewById(R.id.playing_gifts_num);

		mGiftSwitchGeneral = (Button) findViewById(R.id.gift_switch_general);
		mGiftSwitchGuard = (Button) findViewById(R.id.gift_switch_guard);
		mGiftSwitchGuardView = findViewById(R.id.gift_switch_guard_view);
		mGiftSwitchGeneralView = findViewById(R.id.gift_switch_general_view);
		mGiftSwitchGuard2 = (Button) findViewById(R.id.gift_switch_guard2);
		mGiftSwitchGuardView2 = findViewById(R.id.gift_switch_guard2_view);
		mGuardOpenBtn = (Button) findViewById(R.id.playing_gifts_guard_tip_btn);
		mGuardOpenTipLayout = (LinearLayout) findViewById(R.id.playing_gifts_guard_tip);

		mRlCountBonusTimes = (RelativeLayout) findViewById(R.id.live_gift_rl_count_bonus_times);
		mRlBonusTimes = (RelativeLayout) findViewById(R.id.live_gift_rl_bonus_times);
		mIvBonusTimesBg = (ImageView) findViewById(R.id.live_gift_bonus_times_num_bg);
		mIvBonusTimesNum = (ImageView) findViewById(R.id.live_gift_bonus_times_num);
		mRippleCountBonusTime = (RippleBackground) findViewById(R.id
				.live_gift_rl_bonus_times_count_time);
		mBtnCountBonusTime = (Button) findViewById(R.id.live_gift_bonus_times_btn_count_time);
		mTvCountBonusTime = (TextView) findViewById(R.id.live_gift_bonus_times_tv_count_time);

		mGiftGuardUnReadIv = (ImageView) findViewById(R.id.gift_switch_guard_unread);

		giftViewPager = (ViewPager) findViewById(R.id.playing_gifts_bottom_pager);
		mGuardGiftViewPager = (ViewPager) findViewById(R.id.playing_gifts_guard_pager);
		mGuardGiftViewPager2 = (ViewPager) findViewById(R.id.playing_gifts_guard_pager_2);
		mGiftNumViewPager = (ViewPager) findViewById(R.id.playing_gifts_num_pager);
		mIndicator = (IconPageIndicator) findViewById(R.id.indicator);
		mGuardIndicator = (IconPageIndicator) findViewById(R.id.indicator_guard);
		mGuardIndicator2 = (IconPageIndicator) findViewById(R.id.indicator_guard_2);
		mNumIndicator = (IconPageIndicator) findViewById(R.id.indicator_num);

		mBalanceTv = (TextView) findViewById(R.id.user_balance);
		mLiBalance = (LinearLayout) findViewById(R.id.ll_user_balance);
		giftsNum = (TextView) findViewById(R.id.playing_gifts_bottom_ll_input_num);
		mLiveBtnNum = (Button) findViewById(R.id.gift_num_cur_select);
		mIbtnShopBag = (ImageButton) findViewById(R.id.live_gift_ibtn_shop_bag);
		// moLvSendGiftsNum = (ListView) findViewById(R.id.send_gifts_num_lv);

		moIvClearInput = (ImageView) findViewById(R.id.playing_iv_clear_msg_content);
		mLiveDanmuBtn = (ToggleButton) findViewById(R.id.input_type_button);
		moEtContent = (EditText) findViewById(R.id.playing_et_msg_content);
		moIvEmotion = (ImageView) findViewById(R.id.playing_iv_emotion);
		moBtnSend = (Button) findViewById(R.id.playing_btn_send_msg);
		moGvEmotions = (LinearLayout) findViewById(R.id.playing_gv_eomotions);

		mGiftLayout = (RelativeLayout) findViewById(R.id.SendGifts);
		// giftView1 = findViewById(R.id.gift_view_1);
		// giftView2 = findViewById(R.id.gift_view_2);
		mLiveGiftlayout = (LinearLayout) findViewById(R.id.sendlayout);

		mLiveCurGiftIv = (ImageView) findViewById(R.id.live_gift_cur_iv);

		moBtnSendGifts = (Button) findViewById(R.id.playing_gifts_bottom_btn_send);
		mLiveButtomMenulayout = (LinearLayout) findViewById(R.id.live_buttom_menu_layout);
		mLiveButtomGiftLayout = (FrameLayout) findViewById(R.id.live_gift_btn_layout);
		mView = findViewById(R.id.menu_line_2);

		mLiveToplayout = (RelativeLayout) findViewById(R.id.live_top_layout);

		mClickUsernameListener = new ClickUsernameListener();
		fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		chatFragment = new LiveChatFragment(mClickUsernameListener);
		chatFragment.setOnListViewTouchListener(new OnViewPage());
		ft.replace(R.id.live_chat_fragment, chatFragment);
		ft.commitAllowingStateLoss();

		mAnchorLevelUpTip1 = (TypeTextView) findViewById(R.id.anchor_level_up_tip1);
		mAnchorLevelUpTip2 = (TypeTextView) findViewById(R.id.anchor_level_up_tip2);
		mAnchorLevelUpTip3 = (TypeTextView) findViewById(R.id.anchor_level_up_tip3);
		mAnchorLevel = (ImageView) findViewById(R.id.anchor_level);
		mAnchorLevelAnim = (ImageView) findViewById(R.id.anchor_level_anim);
		mAnchorBackgroupIv = (ImageView) findViewById(R.id.anchor_level_up_bg);
		mAnchorLevelUpLayout = (RelativeLayout) findViewById(R.id.anchor_level_up_layout);

		// 热门排名
		mHotRankLayout = (RelativeLayout) findViewById(R.id.playing_hot_rank_layout);
		mNowRank = (TextView) findViewById(R.id.hot_rank_now_rank);
		mHotRankArrow = (ImageView) findViewById(R.id.hot_rank_arrow);
		//热门排名
		mHotRankPopupWindow = new LiveHotRankPopWindow(this);

		// 底部右侧连线UI 整体UI / 音频通话中整体布局 / 关闭连线整体布局 / 头像 / 等级 / 昵称 / 结束连线按钮 / 确定结束 / 取消关闭
		mConnectLivingLayout = (RelativeLayout) findViewById(R.id
				.live_connect_living_layout_parent);
		mConnectAudioLivingLayout = (LinearLayout) mConnectLivingLayout.findViewById(R.id
				.live_connect_audio_connecting_layout);
		mConnectCloseLayout = (LinearLayout) mConnectLivingLayout.findViewById(R.id
				.live_connect_connecting_exist_layout);
		mConnectAudioLivingHeadPic = (ImageView) mConnectLivingLayout.findViewById(R.id
				.live_connect_audio_connecting_headpic);
		mConnectAudioLivingLevel = (ImageView) mConnectLivingLayout.findViewById(R.id
				.live_connect_audio_connecting_level);
		mConnectAudioLivingName = (TextView) mConnectLivingLayout.findViewById(R.id
				.live_connect_audio_connecting_name);
		mConnectLivingClose = (ImageView) mConnectLivingLayout.findViewById(R.id
				.live_connect_connecting_close);
		mConnectLivingCloseSure = (TextView) mConnectLivingLayout.findViewById(R.id
				.live_connect_connecting_exist_sure);
		mConnectLivingCloseCancel = (TextView) mConnectLivingLayout.findViewById(R.id
				.live_connect_connecting_exist_cancel);
		mLiveConnectVideoView = (TXCloudVideoView) mConnectLivingLayout.findViewById(R.id
				.live_connect_video_view);

		initModeratorGuard();
	}

	@Override
	public void initWidgets() {
		mInputMethodManager = (InputMethodManager) mActivity.getSystemService(Context
				.INPUT_METHOD_SERVICE);
		mLiveAudience.setText(String.format(mActivity.getResources().getString(R.string
				.live_audience_num), String.valueOf(0)));
		mLiveInputLayout.setVisibility(View.INVISIBLE);
		// Utils.initEtClearView(moEtContent, moIvClearInput);
		// 设置文本输入监听
		mDanmakuViewCommon = new DanmakuViewCommon(mDanmakuView);
		mDanmakuViewCommon.initDanmaku();

		//初始化付费弹幕
		mDanmakuActionManager = new DanmakuActionManager();
		mDanmakuClickListener = new OnDanmuClick();
		mDanmakuActionManager.addChannel(mDanmakuChannelB);
		mDanmakuActionManager.addChannel(mDanmakuChannelA);
		mDanmakuChannelA.setOnClickListener(mDanmakuClickListener);
		mDanmakuChannelB.setOnClickListener(mDanmakuClickListener);

		//初始化广播弹幕
		mDanmakuBroastcastActionManager = new DanmakuActionManager();
		mDanmakuBroastcastActionManager.addChannel(mDanmakuBroastcastA);
		mDanmakuBroastcastActionManager.addChannel(mDanmakuBroastcastB);
		mDanmakuBroastcastA.setOnClickListener(mDanmakuClickListener);
		mDanmakuBroastcastB.setOnClickListener(mDanmakuClickListener);

		WebSocketLiveHandler mWebSocketLiveHandler = new WebSocketLiveHandler(this);
		WebSocketLiveEngine.Builder builder = new WebSocketLiveEngine.Builder
				(mWebSocketLiveHandler);
		mWebSocketImpl = builder.build();
		mFlowerWebSocketImpl = new WebSocketLiveEngine.Builder(new WebSocketFlowerHandler
				(flowerCallBack)).build();
		initConnectChat();
	}

	@Override
	protected void setEventsListeners() {
		// mLiveQuit.setOnClickListener(this);
		keyBoardChangedListener();
		mLiveAnchorLayout.setOnClickListener(this);
		mPRankShowLayout.setOnClickListener(this);
		mLiveClose.setOnClickListener(this);
		mLiveCreateFan.setOnClickListener(this);
		mPlayingActivityLogo.setOnClickListener(this);
		mPlayingActivityWeekLogo.setOnClickListener(this);

		mNewUserWelfareLayout.setOnClickListener(this);
		mRedPacketLayout.setOnClickListener(this);

		moIvEmotion.setOnClickListener(new OnShowHideEmotions());
		moEtContent.setOnClickListener(new OnInputText());
		// moEtContent.setOnEditorActionListener(new OnEditorActiion());
		moIvClearInput.setOnClickListener(new OnClearInputText());
		moBtnSend.setOnClickListener(sendMsg);
		mLiveDanmuBtn.setOnCheckedChangeListener(new OnCheckChangeListener());
		mLiBalance.setOnClickListener(new OnReChange());
		// reChangeBtn.setOnClickListener(new OnReChange());
		mLiveChatLayout.setOnClickListener(this);
		mGiftSwitchGeneral.setOnClickListener(new OnGiftSwitchListenr());
		mGiftSwitchGuard.setOnClickListener(new OnGiftSwitchListenr());
		mGiftSwitchGuard2.setOnClickListener(new OnGiftSwitchListenr());

		mRlCountBonusTimes.setOnClickListener(this);
		mBtnCountBonusTime.setOnClickListener(this);
		// 通过聚焦来设置动画
		mBtnCountBonusTime.setOnTouchListener(onTouchListener);


		mLiveMainLayout.setListener(this);
		// 设置选择礼物数量按钮监听
		mLiveBtnNum.setOnClickListener(new OnEditGiftClick());
		mIbtnShopBag.setOnClickListener(new onGoShoppingOrBag());

		moBtnSendGifts.setOnClickListener(new OnGiftsBottomSendClick());

		mDanmakuView.setOnDanmakuClickListener(new IDanmakuView.OnDanmakuClickListener() {
			@Override
			public boolean onDanmakuClick(IDanmakus danmakus) {
				EvtLog.d(TAG, "onDanmakuClick: danmakus size:" + danmakus.size());
				// BaseDanmaku latest = danmakus.last();
				// if (null != latest) {
				//					EvtLog.d(TAG, "onDanmakuClick: text of latest danmaku:" +
				// latest.text);
				// return true;
				// }
				hideSoftInput();
				return false;
			}

			@Override
			public boolean onViewClick(IDanmakuView view) {
				hideSoftInput();
				return false;
			}
		});

		mLiveChatIv.setOnClickListener(this);
		moRlGift.setOnClickListener(new OnSendGiftsClick());
		// 点击礼物面板以外的地方，与点击返回按钮操作一样
		mGiftLayout.setOnClickListener(new OnPlayChatListenr());
		mLiveFlowerIv.setOnClickListener(this);
		mLiveReportBtn.setOnClickListener(this);
		mLiveScreenSwitchBtn.setOnClickListener(this);
		mLiveShare.setOnClickListener(this);
		mLiveConversationMessage.setOnClickListener(this);
		mGuardOpenTipLayout.setOnClickListener(this);
		mLiveConnectIv.setOnClickListener(this);
		mLiveConenctAnim.setOnClickListener(this);
		// 点击私信面以外的地方,直接隐藏私信面板
		mGiftGiftUserPhoto.setOnClickListener(this);

		mGuardArrow.setOnClickListener(this);
		mGuardLogo.setOnClickListener(this);
		mGuardOpenBtn.setOnClickListener(this);
		mPrivateUserPhoto.setOnClickListener(this);
		// 点击热门排名
		mHotRankLayout.setOnClickListener(this);

		// 底部右侧连线UI点击事件
		mLiveConnectLayout.setOnClickListener(this);
		mConnectLivingLayout.setOnClickListener(this);
		mConnectLivingClose.setOnClickListener(this);
		mConnectLivingCloseSure.setOnClickListener(this);
		mConnectLivingCloseCancel.setOnClickListener(this);
		// 注册网络监听广播
		registerReceiver();
	}

	/**
	 * TODO 简单描述该方法的实现功能（可选）.
	 *
	 * @see BaseFragmentActivity#initData(Bundle)
	 */
	@Override
	protected void initData(Bundle savedInstanceState) {
		// mbFlowerLoaded = true;
		EvtLog.i(TAG, "initData");
		mbActivityRunning = true;
		mDownOutAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.actionsheet_dialog_out);
		mDownInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.actionsheet_dialog_in);
		mRightInAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.a_slide_in_right);
		mRightOutAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.a_slide_out_right);
		mBonusTimeNumAnim = (AnimationSet) AnimationUtils.loadAnimation(mActivity, R.anim
				.anim_live_gift_num_times_scale);
		mBonusTimeBgAnim = (AnimationSet) AnimationUtils.loadAnimation(mActivity, R.anim
				.rotate_0_360_anim);

		mGiftTransAnim = AnimationUtils.loadAnimation(mActivity, R.anim
				.anim_live_gift_effect_trans);
		mUid = UserInfoConfig.getInstance().id;

		initNewUserWelfareData();
		initGiftEffectData();
		initRoomData();
		// 免费礼物“花” 默认可以送，数量1
		// setFlowerProgress(100);
		mRoomIdTv.setText("房间号：" + mmIntentRoomInfo.get("rid"));
		// 如果已登录，获取账户余额信息
		if (AppConfig.getInstance().isLogged) {
			String balance = UserInfoConfig.getInstance().coin;
			if (!Utils.isStrEmpty(balance)) {
				mBalance = balance;
			}
			String diandianBalance = UserInfoConfig.getInstance().lowCoin;
			if (!Utils.isStrEmpty(diandianBalance)) {
				mDianDianBalance = diandianBalance;
			}
		}

		showPrivatePlayLayout(mIsPrivatePlay);
	}

	/**
	 * 初始化新手福利数据
	 */
	private void initNewUserWelfareData() {
		mDeadLineTimeDifference = UserInfoConfig.getInstance().beginnerDeadline - System
				.currentTimeMillis() / 1000;
		//如果还未当截止时间
		if (mDeadLineTimeDifference > 60) {
			mNewUserWelfareLayout.setVisibility(View.VISIBLE);
			if (mDeadLineTimeDifference <= 60 * 60 * 24) {
				mNewUserWelfareTv.setText(getResources().getString(R.string
						.live_new_user_welfare) + "\n" + DateUtil.secToTime
						(mDeadLineTimeDifference * 1000, DateUtil.TYPE_HOUR_MINUTE));
				mNewUserWelfareTimer = new Timer();
				mNewUserWelfareTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						mDeadLineTimeDifference = mDeadLineTimeDifference - 60;
						Message message = Message.obtain();
						message.what = MSG_TIME_UPDATE_NEW_USER_WELFARE;
						message.obj = mDeadLineTimeDifference;
						sendMsg(message);
						//如果已经到期，则取消
						if (mDeadLineTimeDifference <= 0) {
							mNewUserWelfareTimer.cancel();
						}
					}
				}, 60 * 1000, 60 * 1000);
			}
		}
	}

	/**
	 * 初始化红包
	 */
	private void countTimeRedPacketData() {
		// socket断开，停止计时
		releaseRedPacketTimer();
		mRedPacketTimer = new Timer();
		// 停止动画
		if (mIvRedPacket.isAnimating()) {
			mIvRedPacket.pauseAnimation();
		}
		//如果还未当截止时间
		mTvRedPacket.setText(DateUtil.secToTime(mRedPacketTime * 1000, DateUtil
				.TYPE_MINUTE_SECOND));
		mRedPacketTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mRedPacketTime -= 1;
						mTvRedPacket.setText(DateUtil.secToTime(mRedPacketTime * 1000, DateUtil
								.TYPE_MINUTE_SECOND));
						//如果已经到期，则取消
						if (mRedPacketTime <= 0) {
							mRedPacketLayout.setClickable(true);
							mTvRedPacket.setEnabled(true);
							mTvRedPacket.setText(R.string.live_red_packet_click_get);
							if (!mIvRedPacket.isAnimating()) {
								mIvRedPacket.playAnimation();
							}
							mRedPacketTimer.cancel();
						}
					}
				});
			}
		}, 1000, 1000);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		EvtLog.i(TAG, "onSaveInstanceState");
		//		outState.putSerializable(LiveBaseActivity.ROOM_INFO, (Serializable) mmRoomInfo);
		outState.putSerializable(LiveBaseActivity.ANCHOR_RID, (Serializable) mmIntentRoomInfo);
		outState.putInt("screenOrientation", mCurrentScreenType);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		EvtLog.i(TAG, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
		mCurrentScreenType = savedInstanceState.getInt("screenOrientation");
		if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			screenLandscape();
		}
		//		mmIntentRoomInfo = (Map<String, String>) savedInstanceState.getSerializable
		// (LiveBaseActivity.ANCHOR_RID);
		//		mmRoomInfo = (Map<String, Object>) savedInstanceState.getSerializable
		// (LiveBaseActivity.ROOM_INFO);
	}

	// 友盟统计
	@Override
	public void onResume() {
		super.onResume();
		if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
			mDanmakuView.resume();
		}
		// 初始化
		mGifShowing = false;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mDanmakuView != null && mDanmakuView.isPrepared()) {
			mDanmakuView.pause();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if (moGvEmotions.getVisibility() == View.VISIBLE) {
			moGvEmotions.setVisibility(View.GONE);
		} else if (mGiftLayout.getVisibility() == View.VISIBLE) {
			mLiveButtomMenulayout.setVisibility(View.VISIBLE);
			mLiveClose.setVisibility(View.VISIBLE);
			mGiftLayout.setVisibility(View.GONE);
		} else {
			if (SHOW_FOCUS_NEED == mIsNeedFocus) {
				this.sendEmptyMsg(MSG_SHOW_FOCUS_DIALOG);
				return;
			}
			if ((System.currentTimeMillis() - mExitTimeMillis) > Constants.EXIT_INTERVAL) {
				UiHelper.showToast(this, R.string.a_playing_exit_confirm);
				mExitTimeMillis = System.currentTimeMillis();
				return;
			}
			super.onBackPressed();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 切换直播间，隐藏toast
		BonusToast.cancelToast();
		if (intent != null) {
			String preRid = mmIntentRoomInfo.get("rid");
			mmIntentRoomInfo = (Map<String, String>) intent.getSerializableExtra(LiveBaseActivity
					.ANCHOR_RID);
			mIsPrivatePlay = Utils.strBool(mmIntentRoomInfo.get(LiveBaseActivity.ANCHOR_PRIVATE));
			String rid = mmIntentRoomInfo.get("rid");
			if (preRid.equals(rid)) {
				return;
			}
			switchRoomResetData();
		}
	}

	@Override
	public void onDestroy() {
		// 注销网络监听广播
		unregisterReceiver();
		removeAllFragment();
		/** 关闭消息流 */
		stopWebSocket();
		if (mPersonInfoDialogBuidler != null && mPersonInfoDialogBuidler.isShowing())
			mPersonInfoDialogBuidler.dismiss();
		mDanmakuActionManager.release();
		mDanmakuBroastcastActionManager.release();
		if (mNewUserWelfareTimer != null) {
			mNewUserWelfareTimer.cancel();
			mNewUserWelfareTimer = null;
		}

		mHandler.removeCallbacksAndMessages(null);
		mHandler = null;
		mFavorLayout.removeAllViews();
		mDanmakuViewCommon.destoryDanmaku();
		mbActivityRunning = false;
		dismissProgressDialog();
		if (mWebView != null) {
			((ViewGroup) mWebView.getParent()).removeView(mWebView);
			mWebView.removeAllViews();
			mWebView.destroy();
			mWebView = null;
		}
		// 关闭暴击计时器
		releaseBonusTimer();
		// 取消Toast
		BonusToast.cancelToast();
		// 关闭红包计时器
		releaseRedPacketTimer();
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		EvtLog.d(TAG, "onConfigurationChanged:" + newConfig.toString());
		super.onConfigurationChanged(newConfig);
		// 如果是全屏
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			EvtLog.e(TAG, "change screen full...begin");
			screenLandscape();
			EvtLog.e(TAG, "change screen full...finish");
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			EvtLog.e(TAG, "change PORTRAIT_SREEN...begin");
			screenPortrait();
			EvtLog.e(TAG, "change PORTRAIT_SREEN...end");
		}
	}

	/**
	 * 切换房间，充值房间数据
	 */
	protected void switchRoomResetData() {
		// 重新置空房间数据
		mmRoomInfo = null;
		// 重置背包物品数据
		mPackageGiftsData = null;
		//清除上个房间的活动数据
		urlDatas.clear();
		//清楚大额礼物显示数据
		mGiftGifQueue.clear();

		mHandler.removeCallbacksAndMessages(null);

		/** 关闭之前房间消息流 */
		stopWebSocket();
		//清楚聊天信息
		chatFragment.clearChatMessage();
		mDanmakuViewCommon.clear();
		//清除活动显示
		mPlayingActivityLayout.setVisibility(View.GONE);
		mPlayingActivityWeekLayout.setVisibility(View.GONE);
		//清除用户进入直播间
		mUserEntryLayout.setVisibility(View.GONE);
		//清除弹幕
		mDanmakuActionManager.clear();
		mDanmakuBroastcastActionManager.clear();
		mGiftGifInfoLayout.setVisibility(View.GONE);

		// 清除左侧礼物动效
		for (FrameLayout mGiftEffectLayout : mGiftEffectLayouts) {
			mGiftEffectLayout.clearAnimation();
			mGiftEffectLayout.setVisibility(View.GONE);
		}

		//隐藏热门排行
		mHotRankLayout.setVisibility(View.GONE);
		// 如果热门弹框打开，则关闭
		if (mHotRankPopupWindow.isShowing()) {
			mHotRankPopupWindow.dismiss();
		}

		mGiftLayout.setVisibility(View.GONE);

		showPrivatePlayLayout(mIsPrivatePlay);
		releaseRedPacketTimer();
	}

	/**
	 * 显示直播布局,私播或者普通直播间
	 *
	 * @param isPrivatePlay true 私播，false 普通直播间
	 */
	protected void showPrivatePlayLayout(boolean isPrivatePlay) {

	}

	protected void screenPortrait() {
		mCurrentScreenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

		((ViewGroup) mGuardLayout.getParent()).removeView(mGuardLayout);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams
				.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.live_top_layout);
		layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.playing_ranking_layout_show);
		mLiveMainLayout.addView(mGuardLayout, 0, layoutParams);

		layoutParams = (LayoutParams) mLiveWateMarkLayout.getLayoutParams();
		layoutParams.topMargin = Utils.dip2px(mActivity, 90.66f);
		mLiveWateMarkLayout.setLayoutParams(layoutParams);

		// 重新布局“右边活动”
		layoutParams = (LayoutParams) mPlayingActivityRigthLayout.getLayoutParams();
		layoutParams.topMargin = Utils.dip2px(mActivity, 130.66f);
		mPlayingActivityRigthLayout.setLayoutParams(layoutParams);

		// 重新布局“底部按钮”
		layoutParams = (LayoutParams) mLiveButtomMenulayout.getLayoutParams();
		layoutParams.width = LayoutParams.MATCH_PARENT;
		layoutParams.height = LayoutParams.WRAP_CONTENT;
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.live_gift_btn_layout);
		mLiveButtomMenulayout.setOrientation(LinearLayout.HORIZONTAL);
		mView.setVisibility(View.VISIBLE);
		mLiveButtomMenulayout.setLayoutParams(layoutParams);

		// 修改“切换”屏幕按钮图标
		mLiveScreenSwitchBtn.setBackgroundResource(R.drawable.btn_screen_selector);

		// 重新布局“用户进入、特效礼物、弹幕”
		layoutParams = (LayoutParams) mUserEntryLayout.getLayoutParams();
		layoutParams.addRule(RelativeLayout.ABOVE, R.id.live_chat_fragment);
		layoutParams.leftMargin = 0;
		mUserEntryLayout.setLayoutParams(layoutParams);

		layoutParams = (LayoutParams) mDanmakuChannelLayout.getLayoutParams();
		layoutParams.addRule(RelativeLayout.ABOVE, R.id.item_user_entry_layout);
		layoutParams.leftMargin = 0;
		layoutParams.rightMargin = 0;
		mDanmakuChannelLayout.setLayoutParams(layoutParams);

		layoutParams = (LayoutParams) mGiftEffectParentLayout.getLayoutParams();
		layoutParams.leftMargin = 0;
		layoutParams.addRule(RelativeLayout.ABOVE, R.id.danmu_layout);
		mGiftEffectParentLayout.setLayoutParams(layoutParams);

		// 显示弹幕
		mDanmakuViewCommon.hideDanmakuView();
		// 隐藏特效礼物View
		mGJEffectView.setVisibility(View.GONE);
		// 显示聊天消息list
		mLiveChatLayout.setVisibility(View.VISIBLE);

		// 设置“礼物”布局
		initGiftData(giftViewPager, mIndicator, mGiftsData);
		initGiftData(mGuardGiftViewPager2, mGuardIndicator2, mGuardGiftsData2);
		initPackageGiftData(mPackageGiftsData);

		// 设置“礼物面板”布局
		layoutParams = (LayoutParams) mLiveGiftlayout.getLayoutParams();
		layoutParams.height = Utils.dip2px(mActivity, 276);
		mLiveGiftlayout.setLayoutParams(layoutParams);

		// 设置左侧 “活动面板”布局
		((ViewGroup) mPlayingActivityWeekLayout.getParent()).removeView(mPlayingActivityWeekLayout);
		LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout
				.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		linearLayoutParams.topMargin = Utils.dip2px(mActivity, 11.33f);
		linearLayoutParams.leftMargin = Utils.dip2px(mActivity, 10f);
		linearLayoutParams.rightMargin = Utils.dip2px(mActivity, 10f);
		mPlayingActivityLeftLayout.addView(mPlayingActivityWeekLayout, linearLayoutParams);

		// 设置“主播升级”布局
		layoutParams = (LayoutParams) mAnchorLevelUpLayout.getLayoutParams();
		layoutParams.topMargin = Utils.dip2px(mActivity, 100);
		mAnchorLevelUpLayout.setScaleX(1f);
		mAnchorLevelUpLayout.setScaleY(1f);
		mAnchorLevelUpLayout.setLayoutParams(layoutParams);

		// 暴击中奖倍数
		layoutParams = (LayoutParams) mRlBonusTimes.getLayoutParams();
		layoutParams.topMargin = Utils.dip2px(mActivity, 130);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
		} else {
			layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
		}
		mRlBonusTimes.setLayoutParams(layoutParams);
		// 竖屏显示 连线入口
		mLiveConnectLayout.setVisibility(View.VISIBLE);
		// 切回竖屏，重新获取CD看是否显示红包入口
		getRedPacketCDInfo();
		// showSystemUI();
		// mHandler.removeCallbacks(mShowMenu);

		//			WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
		// attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
		// mActivity.getWindow().setAttributes(attrs);
		//			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES
		// .ICE_CREAM_SANDWICH) {
		// View main_layout = mLiveMainLayout.getRootView();
		// main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		// }
		// moSv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
	}

	protected void screenLandscape() {
		mCurrentScreenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		// 重新布局 “守护”
		((ViewGroup) mGuardLayout.getParent()).removeView(mGuardLayout);
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams
				.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.live_report);
		layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.live_anchor_info_layout);
		mLiveToplayout.addView(mGuardLayout, layoutParams);
		// 重新布局 “水印”
		layoutParams = (LayoutParams) mLiveWateMarkLayout.getLayoutParams();
		layoutParams.topMargin = Utils.dip2px(mActivity, 54);
		mLiveWateMarkLayout.setLayoutParams(layoutParams);
		// 重新布局“右边活动”
		layoutParams = (LayoutParams) mPlayingActivityRigthLayout.getLayoutParams();
		layoutParams.topMargin = Utils.dip2px(mActivity, 94);
		mPlayingActivityRigthLayout.setLayoutParams(layoutParams);
		// 重新布局“底部按钮”
		layoutParams = (LayoutParams) mLiveButtomMenulayout.getLayoutParams();
		layoutParams.width = LayoutParams.WRAP_CONTENT;
		layoutParams.height = LayoutParams.WRAP_CONTENT;
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mLiveButtomMenulayout.setOrientation(LinearLayout.VERTICAL);
		mView.setVisibility(View.GONE);
		mLiveButtomMenulayout.setLayoutParams(layoutParams);

		// 修改“切换”屏幕按钮图标
		mLiveScreenSwitchBtn.setBackgroundResource(R.drawable.btn_screen_portait_selector);

		// 大额礼物数据
		mGiftGifQueue.clear();

		// 设置不能滚动
		// mSwipeBackLayout.setScrollEnable(false);

		// 重新布局“用户进入、特效礼物、弹幕”
		layoutParams = (LayoutParams) mUserEntryLayout.getLayoutParams();
		layoutParams.addRule(RelativeLayout.ABOVE, R.id.sv_danmaku);
		layoutParams.leftMargin = Utils.dip2px(mActivity, 55);
		mUserEntryLayout.setLayoutParams(layoutParams);

		layoutParams = (LayoutParams) mGiftEffectParentLayout.getLayoutParams();
		layoutParams.leftMargin = Utils.dip2px(mActivity, 95);
		layoutParams.addRule(RelativeLayout.ABOVE, R.id.item_user_entry_layout);
		mGiftEffectParentLayout.setLayoutParams(layoutParams);

		layoutParams = (LayoutParams) mDanmakuChannelLayout.getLayoutParams();
		layoutParams.addRule(RelativeLayout.ABOVE, R.id.sv_danmaku);
		layoutParams.leftMargin = Utils.dip2px(mActivity, 55);
		layoutParams.rightMargin = Utils.dip2px(mActivity, 55);
		mDanmakuChannelLayout.setLayoutParams(layoutParams);

		// 显示弹幕
		mDanmakuViewCommon.showDanmakuView();
		// 隐藏特效礼物View
		mGJEffectView.setVisibility(View.GONE);
		// 隐藏聊天消息list
		mLiveChatLayout.setVisibility(View.GONE);

		// 设置“礼物面板”布局
		layoutParams = (LayoutParams) mLiveGiftlayout.getLayoutParams();
		layoutParams.height = Utils.dip2px(mActivity, 184);
		mLiveGiftlayout.setLayoutParams(layoutParams);
		initGiftData(giftViewPager, mIndicator, mGiftsData);
		initGiftData(mGuardGiftViewPager2, mGuardIndicator2, mGuardGiftsData2);
		initPackageGiftData(mPackageGiftsData);

		// 设置左侧“活动面板”布局
		((ViewGroup) mPlayingActivityWeekLayout.getParent()).removeView(mPlayingActivityWeekLayout);
		layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = Utils.dip2px(mActivity, 10f);
		layoutParams.rightMargin = Utils.dip2px(mActivity, 10f);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mPlayingActivityRigthLayout.addView(mPlayingActivityWeekLayout, 1, layoutParams);

		// 设置“主播升级”布局
		layoutParams = (LayoutParams) mAnchorLevelUpLayout.getLayoutParams();
		mAnchorLevelUpLayout.setScaleX(0.7f);
		mAnchorLevelUpLayout.setScaleY(0.7f);
		layoutParams.topMargin = Utils.dip2px(mActivity, 50);
		mAnchorLevelUpLayout.setLayoutParams(layoutParams);

		// 暴击中奖倍数
		layoutParams = (LayoutParams) mRlBonusTimes.getLayoutParams();
		layoutParams.topMargin = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
		} else {
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
		}
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		mRlBonusTimes.setLayoutParams(layoutParams);
		// 横屏隐藏 连线入口
		mLiveConnectLayout.setVisibility(View.GONE);
		// 隐藏红包入口
		mRedPacketLayout.setVisibility(View.GONE);

		// showMenu();
		// getWindow().getDecorView().getRootView().setSystemUiVisibility(View
		// .SYSTEM_UI_FLAG_FULLSCREEN);
	}

	// This snippet hides the system bars.
	private void hideSystemUI() {
		// Set the IMMERSIVE flag.
		// Set the content to appear under the system bars so that the content
		// doesn't resize when the system bars hide and show.
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View
				.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
				View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
				| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
				| View.SYSTEM_UI_FLAG_IMMERSIVE);
	}

	// This snippet shows the system bars. It does this by removing all the flags
	// except for the ones that make the content appear under the system bars.
	private void showSystemUI() {
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View
				.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_LOGIN " + resultCode);
			if (resultCode == LoginActivity.RESULT_CODE_OK) {
				// 如果已登录，获取账户余额信息
				String balance = UserInfoConfig.getInstance().coin;
				if (!Utils.isStrEmpty(balance)) {
					mBalance = balance;
				}
				// 每次弹出礼物面板，更新余额
				mBalanceTv.setText(mBalance);
				mUid = UserInfoConfig.getInstance().id;
				/** 重新初始化参数 */
				mWebSocketImpl.reStart(getMessageUrl());
				mFlowerWebSocketImpl.reStart(getFlowerMessageUrl());
			}
		} else if (requestCode == CREATE_FAN_REQUESTCODE) {
			// 主播创建饭圈后，自动加入一些固定数据
			if (resultCode == Activity.RESULT_OK) {
				mFanInfo = (Map<String, String>) data.getSerializableExtra(FanDetailActivity
						.FAN_INFO);
			}
		} else if (requestCode == ShareDialogActivity.SHARE_REQUEST_CODE) {
			// 分享成功后，发送消息
			if (resultCode == Activity.RESULT_OK) {
				mWebSocketImpl.sendCommand(WebSocketLiveEngine.USER_SHARE);
			}
		} else if (requestCode == RECHANGE_REQUEST_CODE) {
			mIsRechangeFlag = true;
			BusinessUtils.getMyUserInfo(mActivity, new MyUserInfoCallbackDataHandle(mHandler));
		} else if (requestCode == REQUEST_CODE_MESSAGE_CARD) {
			// 如果已登录，获取账户余额信息
			mBalance = UserInfoConfig.getInstance().coin;
			// 每次弹出礼物面板，更新余额
			mBalanceTv.setText(mBalance);
		} else if (requestCode == REQUEST_CODE_NEW_WELFARE) {
			BusinessUtils.getUserPackageInfo(mActivity, new GetUserPackageCallbackData
					(LiveBaseActivity.this));
		} else if (requestCode == REQUEST_CODE_GOTO_SHOP) {
			mBalance = UserInfoConfig.getInstance().coin;
			mBalanceTv.setText(mBalance);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.live_switch_srceen:
				switchSrceen();
				break;
			case R.id.live_create_fan:
				operateFan();
				break;
			// 显示出输入框，并且弹出键盘
			case R.id.live_chat:
				mHandler.removeCallbacks(mShowMenu);
				mLiveButtomMenulayout.setVisibility(View.INVISIBLE);
				mLiveClose.setVisibility(View.GONE);
				moEtContent.requestFocus();
				mInputMethodManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
				break;
			case R.id.playing_ranking_layout_show:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "rankingListInLiveroom");
				Bundle bundle = new Bundle();
				bundle.putString("rid", mmIntentRoomInfo.get("rid"));
				ActivityJumpUtil.toActivityAndBundle(this, LiveRankActivity.class, bundle, -1);
				break;
			case R.id.gifts_bottom_info_img:
				sendFlower();
				break;
			case R.id.live_share:
				toShareLiveInfo();
				break;
			// 直播也的“更多”按钮
			case R.id.live_report:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "reportInLiveRoom");
				ActivityJumpUtil.toReportActivity(mActivity, Constants.COMMON_REPORT_TYPE_ROOM,
						mmIntentRoomInfo.get("rid"), 0);
				break;
			// 直播页的主播个人信息布局
			case R.id.live_anchor_info_layout:
				if (mmRoomInfo != null) {
					String lsModerator = (String) mmRoomInfo.get("moderator");
					Map<String, String> moderator;
					try {
						moderator = JSONParser.parseOne(lsModerator);
						showPersonInfoDialog(moderator.get("true_name"), Constants
								.USER_TYPE_ANCHOR, moderator.get("id"), moIvPPhoto);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				break;
			// 点击大额礼物 头像
			case R.id.item_gif_user_photo:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickHeadInGift");
				mClickUsernameListener.onClick((String) v.getTag(R.id.tag_first), (String) v
						.getTag(R.id.tag_second));
				break;
			// 点击 滑动清屏面板 ,送花
			case R.id.swipebacklayout:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickScreenToSendFlower");
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickScreenToSendFlower", null);
				sendFlower();
				break;
			case R.id.sv_danmaku:
				hideSoftInput();
				break;
			case R.id.guard_arrow:
			case R.id.guard_logo:
			case R.id.playing_gifts_guard_tip_btn:
				if (mmAnchorInfo != null) {
					ActivityJumpUtil.toWebViewActivity(mActivity, WebConstants.getFullWebMDomain
									(WebConstants.WEB_MODERATOR_GUARD_URL) + mmAnchorInfo.get("id"),
							true, RECHANGE_REQUEST_CODE);
				}
				break;
			case R.id.live_gift_rl_count_bonus_times:
				// 暴击按钮显示
				if (mRippleCountBonusTime.isShown()) {
					// 隐藏暴击按钮
					mRippleCountBonusTime.setVisibility(View.GONE);
					// 点击空白隐藏 暴击父布局
					mRlCountBonusTimes.setVisibility(View.GONE);
					// 停止计时器
					releaseBonusTimer();
				}
				break;
			case R.id.live_gift_bonus_times_btn_count_time:
				// 暴击连击按钮
				// 开始暴击连击计时
				countTimeBonusData();
				// 发送暴击礼物
				sendGift();
				// 暴击按钮动画
				mRippleCountBonusTime.startRippleAnimation();
				break;
			case R.id.playing_hot_rank_layout:
				mHotRankPopupWindow.showAtLocation(mView, Gravity.CENTER, 0, 0);
				// 点击按热门排名的按钮的时候再次请求网络
				getHotRankInfo();
				break;
			//新手福利领取
			case R.id.live_new_user_welfare_layout:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickFreshmanWelfareButton");
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickFreshmanWelfareButton", null);
				ActivityJumpUtil.toWebViewActivity(mActivity, WebConstants.getFullWebMDomain
						(WebConstants.NEW_USER_WELFARE), REQUEST_CODE_NEW_WELFARE);
				break;
			// 红包立即领取
			case R.id.live_red_packet_layout:
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickGetRedPacketInLivingRoom", null);
				mRedPacketLayout.setClickable(false);
				BusinessUtils.getRedPacket(mActivity, mmIntentRoomInfo.get("rid"), new
						GetRedPacketCallbackDataHandle(this));
				break;
			case R.id.playing_activity_logo:
				try {
					Map<String, String> activityInfo = JSONParser.parseOne((String) mmRoomInfo
							.get("activity"));
					if (!TextUtils.isEmpty(activityInfo.get("logoJumpKey"))) {
						jumpUrl(activityInfo.get("logoJumpUrl"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case R.id.playing_activity_week_logo:
				try {
					Map<String, String> activityInfo = JSONParser.parseOne((String) mmRoomInfo
							.get("starActivity"));
					if (!TextUtils.isEmpty(activityInfo.get("logoJumpKey"))) {
						jumpUrl(activityInfo.get("logoJumpUrl"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case R.id.private_user_logo:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickUserImgOfTheOnlineAudience");
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickUserImgOfTheOnlineAudience",
						null);
				mClickUsernameListener.onClick((String) v.getTag(R.id.tag_first), (String) v
						.getTag(R.id.tag_second));
				break;
			case R.id.live_connect_living_layout_parent:
				mClickUsernameListener.onClick("", (String) v.getTag(R.id.tag_second));
				break;
			case R.id.live_connect_connecting_close:
				mConnectCloseLayout.setVisibility(View.VISIBLE);
				break;
			// 结束连线
			case R.id.live_connect_connecting_exist_sure:
				mConnectCloseLayout.setVisibility(View.GONE);
				clickEndVideoChat((String) mConnectLivingLayout.getTag(R.id.tag_second));
				break;
			case R.id.live_connect_connecting_exist_cancel:
				mConnectCloseLayout.setVisibility(View.GONE);
				break;
			default:
				break;
		}
	}

	private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				mRippleCountBonusTime.stopRippleAnimation();
			}
			return false;
		}
	};

	/**
	 * 显示中奖倍数
	 */
	private void showBonusTimes() {
		if (isShowBonusFlag) {
			return;
		}
		String bonusTime = mBonusTimesQueue.poll();
		if (TextUtils.isEmpty(bonusTime)) {
			mBonusTimesQueue.clear();
			return;
		}
		isShowBonusFlag = true;
		// 设置倍数对应的图片
		int bonusTimesImg = Utils.getFiledDrawable("ic_icon_bonus_times_", bonusTime);
		mIvBonusTimesNum.setBackgroundResource(bonusTimesImg);
		mBonusTimeNumAnim.getAnimations().get(mBonusTimeNumAnim.getAnimations().size() - 1)
				.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
						// 倍数动画执行到最后一个时，显示背景动画
						mBonusTimeBgAnim.getAnimations().get(0).setRepeatCount(Animation.INFINITE);
						mIvBonusTimesBg.setVisibility(View.VISIBLE);
						mIvBonusTimesBg.startAnimation(mBonusTimeBgAnim);
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						mIvBonusTimesNum.clearAnimation();
						mIvBonusTimesBg.clearAnimation();
						mIvBonusTimesNum.setVisibility(View.GONE);
						mIvBonusTimesBg.setVisibility(View.GONE);
						mRlBonusTimes.setVisibility(View.GONE);
						isShowBonusFlag = false;
						showBonusTimes();
					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}
				});
		mRlBonusTimes.setVisibility(View.VISIBLE);
		mIvBonusTimesNum.setVisibility(View.VISIBLE);
		mIvBonusTimesNum.startAnimation(mBonusTimeNumAnim);
	}

	/**
	 * 暴击礼物倒计时
	 */
	private void countTimeBonusData() {
		// socket断开，停止计时
		releaseBonusTimer();
		mBonusTimesSeconds = TIMES_SECONDS_DEFAULT * 1000;
		mBonusTimer = new Timer();
		//如果还未当截止时间
		mTvCountBonusTime.setText(DateUtil.secToTime(mBonusTimesSeconds, DateUtil
				.TYPE_SECOND_MILLION, DateUtil.TIME_SIGN_POINT));
		mBonusTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mBonusTimesSeconds -= 10;
						mTvCountBonusTime.setText(DateUtil.secToTime(mBonusTimesSeconds, DateUtil
								.TYPE_SECOND_MILLION, DateUtil.TIME_SIGN_POINT));
						// 时间到，动画取消
						if (mBonusTimesSeconds <= 0) {
							mRippleCountBonusTime.setVisibility(View.GONE);
							mRlCountBonusTimes.setVisibility(View.GONE);
							releaseBonusTimer();
						}
					}
				});
			}
		}, 10, 10);
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		sendFlower();
		return false;
	}

	@Override
	public boolean onZoomValueChanged(float factor) {
		return false;
	}

	/**
	 * 切换屏幕
	 *
	 * @since JDK 1.6
	 */
	protected void switchSrceen() {
		if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			setPlayerSreenType(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			// 点击【切换到横屏】按钮的次数、人数
			OperationHelper.onEvent(FeizaoApp.mConctext, "clickChangeCrossScreen");

			setPlayerSreenType(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	/**
	 * 是否支持硬编
	 *
	 * @return
	 */
	protected static boolean isSupportHWEncode() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
	}

	protected Bitmap decodeResource(Resources resources, int id) {
		TypedValue value = new TypedValue();
		resources.openRawResource(id, value);
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inTargetDensity = value.density;
		return BitmapFactory.decodeResource(resources, id, opts);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			// 更新在线人数（观众）
			case MSG_AUDIENCE_NUM_CHANGE:
				mLiveAudience.setText(String.format(mActivity.getResources().getString(R.string
						.live_audience_num), String.valueOf(mAudienceNum)));
				break;
			case MSG_SEND_GIFT_BLANCE_LACK:
				UiHelper.showConfirmDialog(mActivity, R.string.live_blance_lack_tip, R.string
						.recharge, R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						OperationHelper.onEvent(FeizaoApp.mConctext,
								"rechargeInNotSufficientFundsBox", null);
						ActivityJumpUtil.toWebViewActivity(mActivity, WebConstants
										.getFullWebMDomain(WebConstants.RECHARGE_WEB_URL), true,
								RECHANGE_REQUEST_CODE);
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
				break;
			case MsgTypes.GET_ROOM_INFO_SUCCESS:
				mmRoomInfo = (Map<String, Object>) msg.obj;
				initRoomData();
				// 如果正在播放，则加载视频
				break;
			case MsgTypes.GET_ROOM_INFO_FAILED:
				showToast((String) msg.obj, TOAST_LONG);
				break;
			// case MsgTypes.FLOWER_LOOP:
			// int liCurSec = (Integer) msg.obj;
			// setFlowerProgress(liCurSec * 100.0f / 600);
			// if (mbFlowerLoaded)
			// mLiveFlowerNum.setText("1");
			// break;
			case MsgTypes.GET_USER_PACKAGE_SUCCESS:
				mPackageGiftsData = (Map<String, Object>) msg.obj;
				initPackageGiftData(mPackageGiftsData);
				break;
			case MsgTypes.GET_MODERATOR_GUARD_SUCCESS:
				updateModeratorGuardInfo((List<Map<String, String>>) msg.obj);
				break;
			case MsgTypes.MSG_ADD_FAN_SUCCESS:
				// 加入饭圈成功后，修改mFanInfo数据
				mFanInfo.put("joined", Constants.COMMON_TRUE);
				UiHelper.showToast(mActivity, mActivity.getString(R.string
						.commutity_fan_add_succuss));
				break;
			case MsgTypes.MSG_ADD_FAN_FAILED:
				String errorMsg = (String) msg.obj;
				UiHelper.showToast(mActivity, errorMsg);
				break;
			case MsgTypes.GET_MY_USER_INFO_SUCCESS:
				dismissProgressDialog();
				if (mIsRechangeFlag) {
					mIsRechangeFlag = false;
					String balance = UserInfoConfig.getInstance().coin;
					if (!Utils.isStrEmpty(balance)) {
						mBalance = balance;
						// 每次弹出礼物面板，更新余额
						mBalanceTv.setText(mBalance);
					}
					return;
				}
				if (UserInfoConfig.getInstance().groupCreateNumLeft == 0) {
					UiHelper.showToast(mActivity, R.string.commutity_fan_created);
				} else {
					ActivityJumpUtil.gotoActivityForResult(mActivity, CreateFanActivity.class,
							CREATE_FAN_REQUESTCODE, CreateFanActivity.ANCHOR_ID, mmAnchorInfo.get
									("id"));
				}
				break;
			case MsgTypes.GET_MY_USER_INFO_FAILED:
				dismissProgressDialog();
				Bundle bundle = msg.getData();
				UiHelper.showToast(mActivity, bundle.getString("errorMsg"));
				break;
			/** 下面是送礼相关的handler处理 **/
			case MSG_SEND_GIFT_SUCCESS:
				Map<String, String> lmGiftInfo = (Map<String, String>) msg.obj;
				String piFrom = lmGiftInfo.get("piFrom");
				String liGiftId = lmGiftInfo.get("id");
				String giftBonusTimes = lmGiftInfo.get("giftBonusTimes");// 礼物倍数，仅限暴击礼物中奖下
				String giftPrice = lmGiftInfo.get("giftPrice"); // 礼物单价，仅限暴击礼物中奖下
				try {
					// 1.获取暴击中奖情况，添加到队列中进行排队显示动画
					List<String> giftBonusTimeList = JSONParser.parseList(giftBonusTimes);
					// 中奖倍数不为空，并且是本人发送的暴击礼物
					if (giftBonusTimes != null && !giftBonusTimes.isEmpty() && piFrom.equals
							(mUid)) {
						for (String giftBonusTime : giftBonusTimeList) {
							mBonusTimesQueue.offer(giftBonusTime);
						}
						// 显示倍数动画
						showBonusTimes();
						// 更新余额(因此处礼物面板关闭，无需直接更新view)
						mBalance = lmGiftInfo.get("balance");
						UserInfoConfig.getInstance().updateCoin(mBalance);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 如果大额礼物
				if (!TextUtils.isEmpty(lmGiftInfo.get("androidEffect"))) {
					if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
						mGiftGifQueue.offer(lmGiftInfo);
						sendEmptyMsg(MSG_SHOW_GIF_GIFT_SUCCESS);
					}
				}
				if (Utils.strBool(lmGiftInfo.get("combo"))) {// 如果是显示连击效果
					batterHandle(lmGiftInfo, piFrom, liGiftId);
				}
				// 如果是本人送的礼物/··
				if (piFrom.equals(mUid)) {
					showTips(GIFT_SEND_SUCCESS);
					// 如果是背包礼物
					if (!TextUtils.isEmpty(lmGiftInfo.get("pkgItemsetId"))) {
						updatePackageGiftData(lmGiftInfo.get("count"), lmGiftInfo.get
								("pkgItemsetId"));
					} else {
						mBalance = lmGiftInfo.get("balance");
						UserInfoConfig.getInstance().updateCoin(mBalance);
						// 每次弹出礼物面板，更新余额
						mBalanceTv.setText(mBalance);
					}
				}
				// 房间主播收入泡泡更新
				mPaopaoNum = mPaopaoNum + Integer.parseInt(lmGiftInfo.get("giftConsume"));
				mTvEarnCoin.setText(String.valueOf(mPaopaoNum));

				if (!TextUtils.isEmpty(lmGiftInfo.get("activityId"))) {
					// 如果有顶部活动,更新顶部活动数据
					if (lmGiftInfo.get("activityId").equals(mActivityId)) {
						updateActivityInfo(lmGiftInfo.get("activityTotalGiftNum"));
					} // 如果有周星或其他活动，更新左侧活动数据
					else if (lmGiftInfo.get("activityId").equals(mActivityWeekId)) {
						updateActivityWeekInfo(lmGiftInfo.get("activityTotalGiftNum"));
					}
				}
				break;
			case MSG_SEND_FLOWER_SUCCESS:
				mFavorLayout.addFavor();// 显示花朵
				if (mPersonInfoDialogBuidler != null && mPersonInfoDialogBuidler.isShowing()) {
					mPersonInfoDialogBuidler.setFlowerNum(String.valueOf(mFlowwerNum));
				}
				break;
			case MSG_GIFT_EFFECt_VIEW1:
				showGiftEffectAnim(mGiftEffectLayouts.get(0));
				break;
			case MSG_GIFT_EFFECt_VIEW2:
				showGiftEffectAnim(mGiftEffectLayouts.get(1));
				break;
			case MSG_GIFT_EFFECt_VIEW3:
				showGiftEffectAnim(mGiftEffectLayouts.get(2));
				break;
			case MSG_SHOW_GIF_GIFT_SUCCESS:// 显示gif动效
				if (!mGifShowing) {
					EvtLog.e(TAG, "showGifEffect...MSG_SHOW_GIF_GIFT_SUCCESS");
					final Map<String, String> info = mGiftGifQueue.poll();
					if (info != null) {
						mGifShowing = true;
						final AnimatorListenerAdapter animatorListenerAdapter = new
								AnimatorListenerAdapter() {
									@Override
									public void onAnimationEnd(Animator animation) {
										super.onAnimationEnd(animation);
										// 动画显示结束
										mGiftGifInfoLayout.setVisibility(View.GONE);
										mGifShowing = false;
										sendEmptyMsg(MSG_SHOW_GIF_GIFT_SUCCESS);
										mGJEffectView.removeAllListeners();
										mGJEffectView.removeAllViews();
										mGJEffectView.setVisibility(View.GONE);
									}
								};
						EffectGiftLoader.getInstance(mActivity).loadDataForComposition(info.get
								("androidEffect"), new EffectComposition
								.OnCompositionLoadedListener() {
							@Override
							public void onCompositionLoaded(EffectComposition composition) {
								EvtLog.e(TAG, "showGifEffect...loading EffectComposition：" +
										composition);
								// 开始显示动画
								if (composition != null && mCurrentScreenType == ActivityInfo
										.SCREEN_ORIENTATION_PORTRAIT) {
									// 如果不是进程动画--显示大额礼物送礼人信息
									mGiftGifInfoLayout.setVisibility(View.VISIBLE);
									updateGifGiftInfo(info);
									// 显示大额礼物动效
									mGJEffectView.setComposition(composition);
									mGJEffectView.setVisibility(View.VISIBLE);
									mGJEffectView.startAnimation(animatorListenerAdapter);
								}
								// 加载动画信息失败
								else {
									EvtLog.e(TAG, "showGifEffect...礼物动效播放失败，播放下一个");
									mGifShowing = false;
									sendEmptyMsg(MSG_SHOW_GIF_GIFT_SUCCESS);
								}
							}
						});
					}
				}
				break;
			case MSG_SYSTEM_MESSAGE:
				CharSequence charSequence = chatFragment.onHtmlTextMsg((String) msg.obj);
				chatFragment.sendChatMsg(charSequence);
				break;
			case MSG_USER_ENTRY_EFFECT_HANDLE:
				if (mCurrentUserEntryEffectNum == 0) {
					//上一个用户的进场动画执行完成
					Map<String, String> userInfo = mUserEntryQueue.poll();
					if (userInfo != null) {
						userEntryTextEffectHandle(userInfo);
						if (Utils.getInteger(userInfo.get("mountId"), 0) > 0 && !TextUtils
								.isEmpty(userInfo.get("androidMount"))) {
							userEntryCarEffectHandle(userInfo.get("androidMount"));
						}
					}
				}
				break;
			case MSG_ANCHOR_LEVEL_UP_HANDLE:
				if (mAnchorLevelUpLayout.getVisibility() != View.VISIBLE) {
					JSONObject object = mAnchorLevelUpQueue.poll();
					if (object != null) {
						anchorLevelUpHandle(object.optString("nickname"), object.optString
								("moderatorLevel"), object.optString("moderatorLevelName"));
					}
				}
				break;
			case MSG_PAY_DANMU_MESSAGE:
				updateBalance(msg.arg1);
				break;
			case MSG_PAY_DANMU_OPEN_GUARD_MESSAGE:
				Map<String, String> tempMap = (Map<String, String>) msg.obj;

				// 房间主播收入泡泡更新
				mPaopaoNum = mPaopaoNum + Integer.parseInt(tempMap.get("cost"));
				mTvEarnCoin.setText(String.valueOf(mPaopaoNum));

				List<Map<String, String>> guardDatas = mGuardAdapter.getData();
				// 显示位置计算
				Iterator<Map<String, String>> it = guardDatas.iterator();
				while (it.hasNext()) {
					Map<String, String> data = it.next();
					if (data.get("uid").equals(tempMap.get("uid")) && data.get("type").equals
							(tempMap.get("type"))) {
						it.remove();
						break;
					}
				}
				int insertPosition = 0;
				for (int i = 0; i < guardDatas.size(); i++) {
					if (guardDatas.get(i).get("timeType").equals(tempMap.get("timeType"))) {
						insertPosition = i;
						break;
					}
				}
				guardDatas.add(insertPosition, tempMap);
				ViewGroup.LayoutParams layoutParams = mGuardHorizontalListView.getLayoutParams();
				layoutParams.width = Utils.dip2px(mActivity, 44) * guardDatas.size();
				mGuardHorizontalListView.setLayoutParams(layoutParams);
				mGuardAdapter.notifyDataSetChanged();
				// 如果是自己开通守护或者续约
				if (tempMap.get("uid").equals(mUid)) {
					mmRoomInfo.put("isGuard", Constants.COMMON_TRUE);
					mGuardOpenTipLayout.setVisibility(View.GONE);
					updateBalance(Integer.valueOf(tempMap.get("cost")));
					// 开通守护用户为当前用户
					BusinessUtils.getGuardGifts(mActivity, mmAnchorInfo.get("id"), new
							CallbackDataHandle() {
								@Override
								public void onCallback(boolean success, String errorCode, String
										errorMsg, final Object result) {
									if (success) {
										mHandler.post(new Runnable() {
											@Override
											public void run() {
												try {
													mGuardGiftsData2 = JSONParser.parseMulti((JSONArray)
															result);
													initGiftData(mGuardGiftViewPager2, mGuardIndicator2,
															mGuardGiftsData2);
												} catch (JSONException e) {
													e.printStackTrace();
												}
											}
										});
									}
								}
							});
				}
				break;
			case MsgTypes.GET_HOT_RANK_SUCCESS:
				Map<String, String> data = (Map<String, String>) msg.obj;
				updateHotRankData(data);
				break;
			case MSG_TIME_COUNT_RUNNABLE:
				break;
			case MSG_MESSAGE_CARD_ACTIVE:// 购买私信卡，webview已经通知更新自己的泡泡
				// 房间主播收入泡泡更新
				mPaopaoNum = mPaopaoNum + (int) msg.obj;
				mTvEarnCoin.setText(String.valueOf(mPaopaoNum));
				break;
			case MSG_TIME_UPDATE_NEW_USER_WELFARE:
				long timeDifference = (long) msg.obj;
				//小于1分钟隐藏，00分钟不好看
				if (timeDifference <= 60) {
					mNewUserWelfareLayout.setVisibility(View.GONE);
				} else {
					mNewUserWelfareTv.setText(getResources().getString(R.string
							.live_new_user_welfare) + "\n" + DateUtil.secToTime(timeDifference *
							1000, DateUtil.TYPE_HOUR_MINUTE));
				}
				break;
			case MSG_UPDATE_USER_PHOTO:
				if (mUserPhotoArray.size() > 0) {
					mPrivateUserPhoto.setVisibility(View.VISIBLE);
					SparseArray<String> sparseArray = mUserPhotoArray.get(mUserPhotoArray.size()
							- 1);
					mPrivateUserPhoto.setTag(R.id.tag_second, sparseArray.get(PRIVATE_USER_UID));
					mPrivateUserPhoto.setTag(R.id.tag_first, sparseArray.get
							(PRIVATE_USER_USERNAME));
					ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity
							.getApplicationContext(), mPrivateUserPhoto, sparseArray.get(2));
				} else {
					mPrivateUserPhoto.setVisibility(View.GONE);
				}
				break;
			// 发起连线邀请成功
			case MSG_INVITE_VIDEO_CHAT_SUCC:
				inviteVideoChatSucc((String) msg.obj);
				break;
			// 发起连线邀请失败
			case MSG_INVITE_VIDEO_CHAT_FAIL:
				inviteVideoChatFail(msg);
				break;
			// 结束连线成功
			case MSG_VIDEO_CHAT_END_SUCC:
				if (msg.obj != null) {
					endVideoChatSucc((JSONObject) msg.obj);
				}
				break;
			case MSG_VIDEO_CHAT_END_FAIL:
				endVideoChatFail(msg);
				break;
			// 取消连线邀请成功
			case MSG_CANCEL_VIDEO_CHAT_SUCC:
				cancelVideoChatSucc();
				break;
			case MSG_CANCEL_VIDEO_CHAT_FAIL:
				cancelVideoChatFail(msg);
				break;
			// 同意连线成功
			case MSG_ACCEPT_VIDEO_CHAT_SUCC:
				if (msg.obj != null) {
					acceptVideoChatSucc((AcceptVideoChat) msg.obj);
				}
				break;
			case MSG_ACCEPT_VIDEO_CHAT_FAIL:
				acceptVideoChatFail(msg);
				break;
			// 获取红包CD时间以及是否显示入口
			case MSG_GET_RED_PACKET_CD_SUCC:
				Map<String, String> redPacketCDData = (Map<String, String>) msg.obj;
				if (redPacketCDData != null) {
					String redCDStr = redPacketCDData.get("cd");
					// 是否显示红包入口
					boolean redShowFlag = Utils.getBooleanFlag(redPacketCDData.get("display"));
					if (redShowFlag) {
						if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
							mRedPacketLayout.setVisibility(View.VISIBLE);
						} else {
							mRedPacketLayout.setVisibility(View.GONE);
						}
						// 红包CD
						mRedPacketTime = 0;
						if (!TextUtils.isEmpty(redCDStr)) {
							mRedPacketTime = Long.parseLong(redCDStr);
						}
						// 开始计时
						if (mRedPacketTime > 0) {
							mRedPacketLayout.setClickable(false);
							mTvRedPacket.setEnabled(false);
							countTimeRedPacketData();
						} else {
							// 停止计时，开始动画
							if (!mIvRedPacket.isAnimating()) {
								mIvRedPacket.playAnimation();
							}
							mRedPacketLayout.setClickable(true);
							mTvRedPacket.setEnabled(true);
							mTvRedPacket.setText(R.string.live_red_packet_click_get);
						}
					} else {
						// 隐藏布局，停止动画
						if (mIvRedPacket.isAnimating()) {
							mIvRedPacket.pauseAnimation();
						}
						// 红包入口隐藏，没必要倒计时
						mRedPacketLayout.setVisibility(View.GONE);
					}
				} else {
					mIvRedPacket.pauseAnimation();
					// 作为异常处理
					mRedPacketLayout.setVisibility(View.GONE);
				}
				break;
			case MSG_GET_RED_PACKET_CD_FAIL:
				mIvRedPacket.pauseAnimation();
				if (msg.obj != null) {
					UiHelper.showToast(mActivity, String.valueOf(msg.obj));
				}
				break;
			// 领取红包成功
			case MSG_GET_RED_PACKET_SUCC:
				Map<String, String> redPacketData = (Map<String, String>) msg.obj;
				if (redPacketData != null) {
					// 是否是最后一次领取了红包
					boolean isLastFlag = Utils.getBooleanFlag(redPacketData.get("last"));
					String money = redPacketData.get("money");
					// 最后一次领取红包关闭入口
					if (isLastFlag) {
						mRedPacketLayout.setVisibility(View.GONE);
					}
					showRedPacketDialog(money, isLastFlag);
					// 本次领取完成，获取CD
					getRedPacketCDInfo();
				}
				mRedPacketLayout.setClickable(true);
				break;
			case MSG_GET_RED_PACKET_FAIL:
				mRedPacketLayout.setClickable(true);
				if (msg.obj != null) {
					UiHelper.showToast(mActivity, String.valueOf(msg.obj));
				}
				break;
			default:
				break;
		}
	}

	/**
	 * 点击结束连线按钮
	 *
	 * @param uid
	 */
	protected void clickEndVideoChat(String uid) {

	}


	/**
	 * 邀请连线成功
	 *
	 * @param uid 被邀请人uid
	 */
	protected void inviteVideoChatSucc(String uid) {
		EvtLog.e(TAG, "inviteVideoChatSucc");
	}

	/**
	 * 邀请连线失败
	 */
	protected void inviteVideoChatFail(Message msg) {
		UiHelper.showToast(mActivity, (String) msg.obj);
	}

	/**
	 * 结束连线
	 */
	protected void endVideoChatSucc(JSONObject data) {
		EvtLog.e(TAG, "endVideoChatSucc");
		mIsLiveConnecting = false;
		initConnectChat();
	}

	/**
	 * 结束失败
	 */
	protected void endVideoChatFail(Message msg) {
		showTips((String) msg.obj);
	}

	/**
	 * 结束连线
	 */
	protected void cancelVideoChatSucc() {
	}

	/**
	 * 结束失败
	 */
	protected void cancelVideoChatFail(Message msg) {
		showTips((String) msg.obj);
	}

	/**
	 * 同意连线
	 */
	protected void acceptVideoChatSucc(AcceptVideoChat acceptVideoChat) {
		EvtLog.e(TAG, "acceptVideoChatSucc");
		mIsLiveConnecting = true;
	}

	protected void acceptVideoChatFail(Message msg) {
		showTips((String) msg.obj);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	/**
	 * 请求横竖屏切换
	 */
	class requestedOrientation implements Runnable {
		int screenType = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

		public requestedOrientation(int type) {
			this.screenType = type;
		}

		@Override
		public void run() {
			EvtLog.d(TAG, "setRequestedOrientation type:" + screenType);
			setRequestedOrientation(screenType);
		}
	}

	/* 设置全屏/竖屏 */
	public void setPlayerSreenType(int type) {
		postDelayed(new requestedOrientation(type), 0);
		mCurrentScreenType = type;
	}

	/**
	 * 收起输入框和表情框
	 */
	private void hideSoftInput() {
		moGvEmotions.setVisibility(View.GONE);
		// 是否已经隐藏虚拟键盘
		if (isKeyboardUp) {
			mInputMethodManager.hideSoftInputFromWindow(mActivity.getWindow().peekDecorView()
					.getApplicationWindowToken(), 0);
		}
	}

	/**
	 * 初始化特效礼物View
	 */
	private void initGiftEffectView(View view) {
		GiftEffectViewHold holder = new GiftEffectViewHold();
		holder.mItemUserName = (TextView) view.findViewById(R.id.item_user_name);
		holder.mItemGiftTip = (TextView) view.findViewById(R.id.item_user_gift);
		holder.mItemGiftNum = (TextView) view.findViewById(R.id.item_user_gift_num);
		holder.mItemLlGiftGroup = (RelativeLayout) view.findViewById(R.id.item_ll_user_gift_group);
		holder.mItemGiftGroup = (StrokeTextView) view.findViewById(R.id.item_user_gift_group);
		holder.mItemLlGiftGroupVer = (AutoVerticalLinearLayout) view.findViewById(R.id
				.item_user_gift_group_ll);
		holder.mItemUserPhoto = (ImageView) view.findViewById(R.id.item_user_photo);
		holder.mItemUserPhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickHeadInGift");
				mClickUsernameListener.onClick((String) v.getTag(R.id.tag_first), (String) v
						.getTag(R.id.tag_second));
			}
		});
		holder.mItemGiftPhoto = (ImageView) view.findViewById(R.id.item_gift_img);
		holder.mItemBackgroup = (RelativeLayout) view.findViewById(R.id.item_user_layout);
		view.setTag(holder);
	}

	/**
	 * 初始化特效礼物数据
	 */
	private void initGiftEffectData() {
		for (int i = 0; i < mGiftEffectLayouts.size(); i++) {
			mGiftEffectDatas.add(new GiftEffectViewData(MSG_GIFT_EFFECt_VIEW1 + i));
		}
	}

	/**
	 * 显示礼物view动效. <br/>
	 *
	 * @since JDK 1.6
	 */
	private void showGiftEffectAnim(final View view) {
		EvtLog.e(TAG, "showGiftEffectAnim onAnimationEnd:" + view.getId());
		view.clearAnimation();
		mGiftInvisibleAnim = AnimationUtils.loadAnimation(mActivity, R.anim
				.anim_live_gift_effect_invisible);
		view.startAnimation(mGiftInvisibleAnim);
		mGiftInvisibleAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				EvtLog.e(TAG, "onAnimationEnd:" + view.getId());
				view.setVisibility(View.INVISIBLE);
				// 动画结束，清楚垂直滚动view以及数据
				((GiftEffectViewHold) view.getTag()).mItemLlGiftGroupVer.clear();
			}
		});
	}

	/**
	 * 关闭对话框
	 */
	protected void dismissProgressDialog() {
		if (mProgress != null && mProgress.isShowing()) mProgress.dismiss();
	}

	/**
	 * 移除所有的Fragment
	 */
	private void removeAllFragment() {
		final FragmentManager fm = getSupportFragmentManager();
		List<Fragment> fragments = fm.getFragments();
		FragmentTransaction ft = fm.beginTransaction();
		for (int i = 0; i < fragments.size(); i++) {
			ft.remove(fragments.get(i));
		}
		ft.commitAllowingStateLoss();
	}

	/**
	 * 开启websocket服务
	 */
	protected void startWebSocket() {
		// 初始化参数
		mWebSocketConnectStatus = false;
		mWebSocketConnectStatusFlower = false;
		mWebSocketImpl.start(getMessageUrl());
		mFlowerWebSocketImpl.start(getFlowerMessageUrl());
		mSendHeartTimer = new Timer();
		mSendHeartTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				mWebSocketImpl.sendHeartBeat();
				mFlowerWebSocketImpl.sendHeartBeat();
			}
		}, Constants.KEEP_WEBSOCKET_CONNENT_TIME, Constants.KEEP_WEBSOCKET_CONNENT_TIME);
	}

	/**
	 * 关闭websocket服务
	 */
	protected void stopWebSocket() {
		mWebSocketConnectStatus = true;
		mWebSocketConnectStatusFlower = true;
		mWebSocketImpl.close();
		mFlowerWebSocketImpl.close();
		if (mSendHeartTimer != null) {
			mSendHeartTimer.cancel();
		}
	}

	public void initRoomData() {
		if (mmRoomInfo == null) return;
		mGiftsData = (List<Map<String, String>>) mmRoomInfo.get("gifts");
		try {
			mGuardGiftsData2 = (List<Map<String, String>>) mmRoomInfo.get("guardGifts");
			mmAnchorInfo = JSONParser.parseOne((String) mmRoomInfo.get("moderator"));
			mFanInfo = JSONParser.parseOne((String) mmRoomInfo.get("group"));
			// 获取房间管理员
			String managerUidStr = (String) mmRoomInfo.get("adminUids");
			// 如果存在房管列表
			if (!TextUtils.isEmpty(managerUidStr)) {
				mManagerUids = JSONParser.parseList(managerUidStr);
			}
		} catch (Exception e) {
		}
		mAudienceNum = mAudienceNum + Integer.parseInt(mmRoomInfo.get("onlineNum").toString());
		// 是否为热门
		EvtLog.e(TAG, "mAudienceNum:" + mAudienceNum + ", onlineNum:" + mmRoomInfo.get
				("onlineNum"));
		initRoomAnchorInfo();

		getModeratorGuardInfo();

		// 开启websocket
		startWebSocket();

		/** 根据网络获取数据，设置礼物面板的页数 ,初始化礼物面板 */
		initGiftData(giftViewPager, mIndicator, mGiftsData);
		initGiftData(mGuardGiftViewPager2, mGuardIndicator2, mGuardGiftsData2);

		// initGiftNumData();
		initShareInfo();
		initActivityInfo();
		initActivityWeekInfo();
		initSystemMessage();
		// 获取CD看是否显示红包入口(是否正在直播，有后台来做判断)
		getRedPacketCDInfo();
	}

	public void getHotRankInfo() {
		BusinessUtils.getHotRankInfo(mActivity, mmAnchorInfo.get("id"), new HotRankCallBack(this));
	}

	/**
	 * 初始化“连麦”窗口
	 */
	protected void initConnectChat() {
		mConnectLivingLayout.setVisibility(View.GONE);
		mConnectLivingLayout.setBackgroundResource(R.drawable.shape_bg_live_connect_line);
		mConnectLivingClose.setVisibility(View.VISIBLE);
		mLiveConnectVideoView.setVisibility(View.GONE);
		mConnectAudioLivingLayout.setVisibility(View.GONE);
		mConnectCloseLayout.setVisibility(View.GONE);
	}

	/**
	 * 初始化分享数据
	 */
	private void initShareInfo() {
		try {
			Map<String, String> moderator = JSONParser.parseOne((String) mmRoomInfo.get
					("moderator"));
			if (!TextUtils.isEmpty(String.valueOf(mmRoomInfo.get("shareTitle")))) {
				shareTitle = String.valueOf(mmRoomInfo.get("shareTitle"));
				shareContent = String.valueOf(mmRoomInfo.get("shareContent"));
				shareUrl = String.valueOf(mmRoomInfo.get("shareUrl"));
				shareUrImg = String.valueOf(mmRoomInfo.get("sharePic"));
			} else {
				shareTitle = LiveBaseActivity.SHARE_TITLE;
				shareContent = LiveBaseActivity.SHARE_CONTENT.replace(LiveBaseActivity.SHARE_XXX,
						moderator.get("true_name"));
				shareUrImg = (String) mmRoomInfo.get("headPic");
				shareUrl = WebConstants.getFullWebMDomain(WebConstants.SHARE_LIVE_PIX) +
						mmAnchorInfo.get("rid");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化活动信息
	 */
	private void initActivityInfo() {
		try {
			//如果是私播，不执行一下代码
			if (Utils.strBool(mmIntentRoomInfo.get(LiveBaseActivity.ANCHOR_PRIVATE))) {
				return;
			}
			Map<String, String> activityInfo = JSONParser.parseOne((String) mmRoomInfo.get
					("activity"));
			mActivityId = activityInfo.get("activityId");
			// 活动抽奖活动的URL
			if (!TextUtils.isEmpty(activityInfo.get("boxOpenUrl"))) {
				mActivityBoxUrl = activityInfo.get("boxOpenUrl");
			}
			mActivityTargetNum = activityInfo.get("targetNum");
			mPlayingActivityLayout.setVisibility(View.VISIBLE);

			if (activityInfo.get("logo").endsWith(".gif")) {
				ImageLoaderUtil.with().loadGif(mActivity, mPlayingActivityLogo, activityInfo.get
						("logo"));
			} else {
				ImageLoaderUtil.with().loadImage(mActivity, mPlayingActivityLogo, activityInfo
						.get("logo"));
			}

			if ("0".equals(mActivityTargetNum)) {
				mPlayingActivityText2.setVisibility(View.VISIBLE);
				mPlayingActivityText2.setText(activityInfo.get("activityPropertyCount"));
				mPlayingActivityText2.setTextColor(Color.parseColor(activityInfo.get
						("progressFontColor")));
				GradientDrawable backgroupDrawble = (GradientDrawable)
						mPlayingActivityText2.getBackground();
				backgroupDrawble.setColor(Color.parseColor(activityInfo.get("progressBgColor")));
				mPlayingActivityProgress.setVisibility(View.GONE);
				mPlayingActivityText.setVisibility(View.GONE);
			} else {
				mPlayingActivityText.setTextColor(Color.parseColor(activityInfo.get
						("progressFontColor")));
				int activityCount = Integer.parseInt(activityInfo.get("activityPropertyCount"));
				int activityTargetNum = Integer.parseInt(mActivityTargetNum);
				int activityProcessCount = activityCount % activityTargetNum;

				mPlayingActivityText.setText(activityProcessCount + "/" + mActivityTargetNum);
				float progress = ((float) activityProcessCount / activityTargetNum);
				myGrad = (LayerDrawable) mPlayingActivityProgress.getProgressDrawable();
				GradientDrawable backgroupDrawble = (GradientDrawable) myGrad
						.findDrawableByLayerId(android.R.id.background);
				backgroupDrawble.setColor(Color.parseColor(activityInfo.get("progressBgColor")));
				GradientDrawable drawable = new GradientDrawable();
				drawable.setCornerRadius(Utils.dip2px(mActivity, 5f));
				drawable.setColor(Color.parseColor(activityInfo.get("progressBarColor")));
				clipDrawable = new ClipDrawable(drawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
				myGrad.setDrawableByLayerId(android.R.id.progress, clipDrawable);
				// 手动设置进度 Level:0 - 10000
				clipDrawable.setLevel((int) (10000 * progress));

				mPlayingActivityProgress.setVisibility(View.VISIBLE);
				mPlayingActivityText.setVisibility(View.VISIBLE);
				mPlayingActivityText2.setVisibility(View.GONE);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化左侧周星或其他活动信息
	 */
	private void initActivityWeekInfo() {
		try {
			Map<String, String> activityInfo = JSONParser.parseOne((String) mmRoomInfo.get
					("starActivity"));
			mActivityWeekId = activityInfo.get("activityId");
			// 活动抽奖活动的URL
			if (!TextUtils.isEmpty(activityInfo.get("boxOpenUrl"))) {
				mActivityBoxUrl = activityInfo.get("boxOpenUrl");
			}
			mActivityWeekTargetNum = activityInfo.get("targetNum");
			mPlayingActivityWeekLayout.setVisibility(View.VISIBLE);

			if (activityInfo.get("logo").endsWith(".gif")) {
				ImageLoaderUtil.with().loadGif(mActivity, mPlayingActivityWeekLogo, activityInfo
						.get("logo"));
			} else {
				ImageLoaderUtil.with().loadImage(mActivity, mPlayingActivityWeekLogo,
						activityInfo.get("logo"));
			}
			if ("0".equals(mActivityWeekTargetNum)) {
				mPlayingActivityWeekText2.setVisibility(View.VISIBLE);
				mPlayingActivityWeekText2.setText(activityInfo.get("activityPropertyCount"));
				mPlayingActivityWeekText2.setTextColor(Color.parseColor(activityInfo.get
						("progressFontColor")));
				GradientDrawable backgroupDrawble = (GradientDrawable)
						mPlayingActivityWeekText2.getBackground();
				backgroupDrawble.setColor(Color.parseColor(activityInfo.get("progressBgColor")));
				mPlayingActivityWeekProgress.setVisibility(View.GONE);
				mPlayingActivityWeekText.setVisibility(View.GONE);
			} else {
				mPlayingActivityWeekText.setTextColor(Color.parseColor(activityInfo.get
						("progressFontColor")));
				int activityCount = Integer.parseInt(activityInfo.get("activityPropertyCount"));
				int activityTargetNum = Integer.parseInt(mActivityWeekTargetNum);
				int activityProcessCount = activityCount % activityTargetNum;

				mPlayingActivityWeekText.setText(activityProcessCount + "/" +
						mActivityWeekTargetNum);
				float progress = ((float) activityProcessCount / activityTargetNum);
				myWeekGrad = (LayerDrawable) mPlayingActivityWeekProgress.getProgressDrawable();
				GradientDrawable backgroupDrawble = (GradientDrawable) myWeekGrad
						.findDrawableByLayerId(android.R.id.background);
				backgroupDrawble.setColor(Color.parseColor(activityInfo.get("progressBgColor")));
				GradientDrawable drawable = new GradientDrawable();
				drawable.setCornerRadius(Utils.dip2px(mActivity, 5f));
				drawable.setColor(Color.parseColor(activityInfo.get("progressBarColor")));
				clipDrawableWeek = new ClipDrawable(drawable, Gravity.LEFT, ClipDrawable
						.HORIZONTAL);
				myWeekGrad.setDrawableByLayerId(android.R.id.progress, clipDrawableWeek);
				// 手动设置进度 Level:0 - 10000
				clipDrawableWeek.setLevel((int) (10000 * progress));

				mPlayingActivityWeekProgress.setVisibility(View.VISIBLE);
				mPlayingActivityWeekText.setVisibility(View.VISIBLE);
				mPlayingActivityWeekText2.setVisibility(View.GONE);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化主播守护列表信息
	 */
	private void initModeratorGuard() {
		mGuardLayout = (RelativeLayout) findViewById(R.id.guard_layout);
		mGuardArrow = (ImageView) findViewById(R.id.guard_arrow);
		mGuardLogo = (ImageView) findViewById(R.id.guard_logo);
		mGuardHorizontalListView = (HorizontalListView) findViewById(R.id.guard_listview);
		//私播头像
		mPrivateUserPhoto = (ImageView) findViewById(R.id.private_user_logo);
		mGuardAdapter = new ModeratorGuardAdapter(mActivity);
		if (AppConfig.getInstance().showGuard) {
			// 右上角守护入口
			mGuardLayout.setVisibility(View.VISIBLE);
			// 守护礼物tab
			mGiftSwitchGuard2.setVisibility(View.VISIBLE);
			mGiftSwitchGuardView2.setVisibility(View.INVISIBLE);
			mGuardHorizontalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Map<String, String> item = (Map<String, String>) mGuardAdapter.getItem
							(position);
					mClickUsernameListener.onClick(item.get("nickname"), item.get("uid"));
				}
			});
			mGuardHorizontalListView.setAdapter(mGuardAdapter);
			// 由于HorizotalListView 更新数据时不更新EmptyView状态，所以更新数据时也重新条用一次
			mGuardHorizontalListView.setEmptyView(mGuardLogo);
		} else {
			mGuardLayout.setVisibility(View.INVISIBLE);
			// 守护礼物tab
			mGiftSwitchGuard2.setVisibility(View.INVISIBLE);
			mGiftSwitchGuardView2.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 显示WebView活动页面
	 */
	protected void showActivityWebView(String activityUrl) {
		if (mWebView == null) {
			try {
				if (!TextUtils.isEmpty(activityUrl)) {
					LZCookieStore.synCookies(LiveBaseActivity.this);
					// 避免webview造成内存溢出，当需要的时候才去加载且传递的是activity对象
					mWebView = new MyWebView(getApplicationContext());
					mLiveMainLayout.addView(mWebView, new LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT));
					mWebView.hideProgressbar(); // 隐藏进度条
					mWebView.addJavascriptInterface(new JsInvokeMainClass(), "recharge");//
					// js调用java方法
					mWebView.setBackgroundColor(0);
					mWebView.loadUrl(activityUrl);// 预加载webview数据
					mWebView.setWebViewClient(new WebViewClient() {
						@Override
						public void onReceivedError(WebView view, int errorCode, String
								description, String failingUrl) {
							EvtLog.e(TAG, "onReceivedError");
							mWebView.setVisibility(View.GONE);
						}

						@Override
						public void onPageFinished(WebView view, String url) {
							super.onPageFinished(view, url);
							EvtLog.e(TAG, "onPageFinished");
							webFlag = true;
							// 判断是否有活动未执行(isEmpty() 相对于 urlDatas.size() 性能提升)
							if (!urlDatas.isEmpty()) mWebView.setVisibility(View.VISIBLE);
							while (!urlDatas.isEmpty())
								mWebView.loadUrl("javascript:showWebviewAnimation('" + urlDatas
										.poll() + "')");
						}
					});
					mWebView.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			if (webFlag) {
				mWebView.setVisibility(View.VISIBLE);
				while (!urlDatas.isEmpty())
					mWebView.loadUrl("javascript:showWebviewAnimation('" + urlDatas.poll() + "')");
			}
		}
	}

	/**
	 * 初始化系统消息
	 */
	private void initSystemMessage() {
		try {
			Map<String, String> messages = JSONParser.parseOne((String) mmRoomInfo.get("messages"));
			for (Map.Entry<String, String> entry : messages.entrySet()) {
				int delayMill = Integer.parseInt(entry.getKey());
				Message msg = new Message();
				msg.what = MSG_SYSTEM_MESSAGE;
				msg.obj = entry.getValue();
				sendMsg(msg, delayMill * 1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取主播守护信息
	 */
	private void getModeratorGuardInfo() {
		try {
			BusinessUtils.getModeratorGuardInfo(mActivity, mmAnchorInfo.get("id"), new
					GetModeratorGuardCallbackData(LiveBaseActivity.this));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取红包福利信息CD
	 */
	private void getRedPacketCDInfo() {
		//		try {
		//			BusinessUtils.getRedPacketCD(mActivity, mmIntentRoomInfo.get("rid"),
		//					new GetRedPacketCDCallbackDataHandle(LiveBaseActivity.this));
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
	}

	/**
	 * 更新热门排名信息
	 *
	 * @param data
	 */
	protected void updateHotRankData(Map<String, String> data) {
		if (mmRoomInfo == null) return;
		if ("-1".equals(data.get("rank"))) {
			mmRoomInfo.put("isHot", false);
		} else {
			mmRoomInfo.put("isHot", true);
			mNowRank.setText(data.get("rank"));
			try {
				mHotRankPopupWindow.setData(data, mmIntentRoomInfo.get("rid"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 更新主播守护列表
	 */
	private void updateModeratorGuardInfo(List<Map<String, String>> data) {
		if (data != null) {
			mGuardAdapter.clearData();
			ViewGroup.LayoutParams layoutParams = mGuardHorizontalListView.getLayoutParams();
			layoutParams.width = Utils.dip2px(mActivity, 44) * data.size();
			mGuardHorizontalListView.setLayoutParams(layoutParams);
			mGuardAdapter.addData(data);
		}
	}

	/**
	 * 更新活动数据
	 */
	private void updateActivityInfo(String activityPropertyCount) {
		if (TextUtils.isEmpty(activityPropertyCount)) return;
		if ("0".equals(mActivityTargetNum)) {
			mPlayingActivityText2.setText(activityPropertyCount);
		} else {
			int activityCount = Integer.parseInt(activityPropertyCount);
			int activityTargetNum = Integer.parseInt(mActivityTargetNum);
			int activityProcessCount = activityCount % activityTargetNum;
			float progress = ((float) activityProcessCount / activityTargetNum);
			mPlayingActivityText.setText(activityProcessCount + "/" + mActivityTargetNum);
			// 设置进度
			clipDrawable.setLevel((int) (10000 * progress));
		}
	}

	/**
	 * 更新活动数据
	 */
	private void updateActivityWeekInfo(String activityPropertyCount) {
		if (TextUtils.isEmpty(activityPropertyCount)) return;
		if ("0".equals(mActivityWeekTargetNum)) {
			mPlayingActivityWeekText2.setText(activityPropertyCount);
		} else {
			int activityCount = Integer.parseInt(activityPropertyCount);
			int activityTargetNum = Integer.parseInt(mActivityWeekTargetNum);
			int activityProcessCount = activityCount % activityTargetNum;
			float progress = ((float) activityProcessCount / activityTargetNum);
			mPlayingActivityWeekText.setText(activityProcessCount + "/" + mActivityWeekTargetNum);
			// 设置进度
			clipDrawableWeek.setLevel((int) (10000 * progress));
		}
	}

	/**
	 * 横屏下，显示菜单布局，且5s后自动隐藏
	 */
	private void showMenu() {
		// if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
		// return;
		// mHandler.removeCallbacks(mShowMenu);
		// showControlLayout();
		// // Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住。
		//		// moVRoot.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
		// mShowMenu = new Runnable() {
		// @Override
		// public void run() {
		// hideControlLayout();
		// }
		//
		// };
		// postDelayed(mShowMenu, 5000);
	}

	private void showControlLayout() {
		mLiveMainLayout.setVisibility(View.VISIBLE);
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setAttributes(attrs);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getWindow().getDecorView().getRootView().setSystemUiVisibility(View
					.SYSTEM_UI_FLAG_VISIBLE);
		}
	}

	private void hideControlLayout() {
		mLiveMainLayout.setVisibility(View.GONE);
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setAttributes(attrs);

		// Hide the software buttons
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			getWindow().getDecorView().getRootView().setSystemUiVisibility(View
					.SYSTEM_UI_FLAG_FULLSCREEN);
		}
	}

	/**
	 * 初始化主播房间信息
	 */
	private void initRoomAnchorInfo() {
		ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity, moIvPPhoto, mmAnchorInfo
				.get("headPic"));
		mPaopaoNum = Long.parseLong(mmAnchorInfo.get("earnCoin"));
		mTvEarnCoin.setText(mmAnchorInfo.get("earnCoin"));
		moIvPPhotoV.setVisibility(Constants.COMMON_TRUE_NUM.equals(mmAnchorInfo.get("verified"))
				? View.VISIBLE : View.GONE);
		if (!Utils.isStrEmpty(mmAnchorInfo.get("true_name"))) {
			moTvPnickname.setText(mmAnchorInfo.get("true_name"));
		}
		moTvUid.setText(String.format(getString(R.string.live_current_uid), mmAnchorInfo.get
				("id")));
		moTvDate.setText(DateUtil.getDateToString(new Date(), DateUtil.DATE_FORMAT_14));
		mFlowwerNum = Long.parseLong((String) mmRoomInfo.get("flowerNumber"));
		mLiveAudience.setText(String.format(mActivity.getResources().getString(R.string
				.live_audience_num), String.valueOf(mAudienceNum)));
	}

	/**
	 * 初始化礼物数据(热门、守护礼物）'
	 */
	private void initGiftData(ViewPager giftViewPager, IconPageIndicator indicator,
							  List<Map<String, String>> giftsData) {
		if (giftsData == null) return;
		// pageSize 每页显示的礼物个数
		int pageSize = mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ?
				GiftsGridAdapter.GIFT_PORTART_PAGE_SIZE : GiftsGridAdapter.GIFT_LANDSCAPE_PAGE_SIZE;
		final int giftPage = (int) Math.ceil(giftsData.size() / Float.valueOf(pageSize));
		// 礼物ViewPage初始化View
		List<GridView> giftsViewData = new ArrayList<GridView>(giftPage);
		for (int i = 0; i < giftPage; i++) {
			GridView appPage = new GridView(mActivity);
			GiftsGridAdapter mAdapter = new GiftsGridAdapter(mActivity, LiveBaseActivity.this);
			// 根据当前页计算装载的应用，每页只装载16个
			int position = i * pageSize;// 当前页的其实位置
			int iEnd = position + pageSize;// 所有数据的结束位置
			int endPosition = iEnd > giftsData.size() ? giftsData.size() : iEnd;
			mAdapter.updateData(giftsData.subList(position, endPosition));

			appPage.setAdapter(mAdapter);
			// 如果是竖屏每行显示 “4”个
			if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				appPage.setNumColumns(4);
			} else {
				appPage.setNumColumns(7);
			}
			appPage.setSelector(new ColorDrawable(Color.TRANSPARENT));
			appPage.setCacheColorHint(Color.TRANSPARENT);
			// appPage.setVerticalSpacing((int) (10 *
			// TelephoneUtil.getDisplayMetrics().density));
			giftsViewData.add(appPage);

		}
		giftViewPager.setAdapter(new IconPageAdapter<GridView>(giftsViewData, R.drawable
				.bg_indicator_selector));
		indicator.setViewPager(giftViewPager);
		indicator.setOnPageChangeListener(new GiftViewPageChangeListener());
		indicator.setCurrentItem(0);
		// 热门列表下，查找打榜礼物
		if (giftViewPager.getId() == R.id.playing_gifts_bottom_pager) {
			Map<String, Integer> rankGiftPosition = getHotRankGiftPosition(giftsData);
			if (rankGiftPosition.get("detailPosition") != null) {
				mBandPosition = rankGiftPosition.get("detailPosition").intValue();
				mBandPagePosition = rankGiftPosition.get("pagePosition").intValue();
			}
		}
	}

	/**
	 * 初始化“背包”礼物数据
	 */
	private void initPackageGiftData(Map<String, Object> data) {
		if (data == null) return;
		List<Map<String, String>> packageGiftList = (List<Map<String, String>>) data.get
				("packageItemsets");
		int guardTotalNum = Integer.valueOf((String) data.get("packageItemsetNum"));
		if (Constants.COMMON_TRUE.equals(data.get("hasNewPkgGift"))) {
			AppConfig.getInstance().updateNewGuardTips(true);
		}
		// 当格子数大于物品数据时，把物品数据补空数据，为了显示空格子
		for (int i = packageGiftList.size(); i < guardTotalNum; i++) {
			packageGiftList.add(new HashMap<String, String>());
		}
		// pageSize 每页显示的礼物个数
		int pageSize = mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ?
				GiftsGridAdapter.GIFT_PORTART_PAGE_SIZE : GiftsGridAdapter.GIFT_LANDSCAPE_PAGE_SIZE;
		final int giftPage = (int) Math.ceil(guardTotalNum / Float.valueOf(pageSize));
		// 礼物ViewPage初始化View
		mGuardGridView = new ArrayList<>(giftPage);
		for (int i = 0; i < giftPage; i++) {
			GridView appPage = new GridView(mActivity);
			GiftsGridAdapter adapter = new GiftsGridAdapter(mActivity, LiveBaseActivity.this);
			// 根据当前页计算装载的应用，每页只装载16个
			int position = i * pageSize;// 当前页的其实位置
			int iEnd = position + pageSize;// 所有数据的结束位置
			int endPosition = iEnd > packageGiftList.size() ? packageGiftList.size() : iEnd;
			adapter.updateData(packageGiftList.subList(position, endPosition));
			appPage.setAdapter(adapter);
			// 如果是竖屏每行显示 “4”个
			if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				appPage.setNumColumns(4);
			} else {
				appPage.setNumColumns(7);
			}
			appPage.setSelector(new ColorDrawable(Color.TRANSPARENT));
			appPage.setCacheColorHint(Color.TRANSPARENT);
			// appPage.setVerticalSpacing((int) (16.66 *
			// TelephoneUtil.getDisplayMetrics().density));
			mGuardGridView.add(appPage);

		}

		mGuardGiftViewPager.setAdapter(new IconPageAdapter<GridView>(mGuardGridView, R.drawable
				.bg_indicator_selector));
		mGuardIndicator.setViewPager(mGuardGiftViewPager);
		mGuardIndicator.setOnPageChangeListener(new GiftViewPageChangeListener());
		mGuardIndicator.setCurrentItem(0);

		updateGuardUnReadTip();
		// 如果背包礼物面板是打开的，设置已读状态
		if (mLiGuardGift.getVisibility() == View.VISIBLE) {
			AppConfig.getInstance().updateNewGuardTips(false);
		}
	}

	/**
	 * 更新背包未读提示
	 */
	private void updateGuardUnReadTip() {
		// 如果有新背包礼物消息
		if (AppConfig.getInstance().sp_guard_new_tip) {
			mIvGiftUnRead.setVisibility(View.VISIBLE);
			mGiftGuardUnReadIv.setVisibility(View.VISIBLE);
		} else {
			mIvGiftUnRead.setVisibility(View.GONE);
			mGiftGuardUnReadIv.setVisibility(View.GONE);
		}
	}

	/**
	 * 更新"背包"礼物数据
	 *
	 * @param count        礼物数量
	 * @param pkgItemsetId
	 */
	private void updatePackageGiftData(String count, String pkgItemsetId) {
		if (mPackageGiftsData == null) return;
		// pageSize 每页显示的礼物个数
		int pageSize = mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ?
				GiftsGridAdapter.GIFT_PORTART_PAGE_SIZE : GiftsGridAdapter.GIFT_LANDSCAPE_PAGE_SIZE;
		List<Map<String, String>> packageGiftList = (List<Map<String, String>>) mPackageGiftsData
				.get("packageItemsets");
		Iterator<Map<String, String>> iterator = packageGiftList.iterator();
		while (iterator.hasNext()) {
			Map<String, String> map = iterator.next();
			if (pkgItemsetId.equals(map.get("pkgItemsetId"))) {
				int num = Integer.valueOf(map.get("num"));
				num = num - Integer.parseInt(count);
				if (num > 0) {
					map.put("num", String.valueOf(num));
				} else {
					iterator.remove();
					packageGiftList.add(new HashMap<String, String>());
					clearGiftSelectedData();
				}
				break;
			}
		}

		for (int i = 0; i < mGuardGridView.size(); i++) {
			GiftsGridAdapter adapter = (GiftsGridAdapter) mGuardGridView.get(i).getAdapter();
			// 根据当前页计算装载的应用，每页只装载16个
			int position = i * pageSize;// 当前页的其实位置
			int iEnd = position + pageSize;// 所有数据的结束位置
			int endPosition = iEnd > packageGiftList.size() ? packageGiftList.size() : iEnd;
			adapter.updateData(packageGiftList.subList(position, endPosition));
		}

	}

	/**
	 * 初始化礼物数量数据
	 */
	private void initGiftNumData() {
		// 礼物ViewPage初始化View
		List<GridView> giftsViewData = new ArrayList<GridView>(1);

		GridView appPage = new GridView(mActivity);
		GiftsNumAdapter adapter = new GiftsNumAdapter(mActivity, LiveBaseActivity.this, 1);
		appPage.setAdapter(adapter);
		appPage.setNumColumns(3);
		appPage.setSelector(new ColorDrawable(Color.TRANSPARENT));
		appPage.setCacheColorHint(Color.TRANSPARENT);
		// appPage.setVerticalSpacing((int) (16.66 *
		// TelephoneUtil.getDisplayMetrics().density));
		giftsViewData.add(appPage);

		mGiftNumViewPager.setAdapter(new IconPageAdapter<>(giftsViewData, R.drawable
				.bg_indicator_selector));
		mNumIndicator.setViewPager(mGiftNumViewPager);
		mNumIndicator.setOnPageChangeListener(new GiftViewPageChangeListener());
	}

	/**
	 * 获取到打榜礼物的位置
	 */
	public Map<String, Integer> getHotRankGiftPosition(List<Map<String, String>> mGiftsData) {
		Map<String, Integer> positionInfo = new HashMap<>();
		int minBandPrice = Integer.MAX_VALUE; // 低价格捧TA礼物
		for (int j = 0; j < mGiftsData.size(); j++) {
			Map<String, String> giftInfo = mGiftsData.get(j);
			String type = giftInfo.get("type");
			if (GiftsGridAdapter.GIFT_TYPE_BANDS.equals(type)) {
				// 上次找到的捧TA礼物金额小于之后的直接进行下一次循环（避免产品添加2个以上捧TA需求，在此使用continue）
				if (minBandPrice > Integer.valueOf(giftInfo.get("price"))) {
					minBandPrice = Integer.valueOf(giftInfo.get("price"));
					int pagePosition = j / 8;
					int detailPosition = j % 8;
					positionInfo.put("detailPosition", detailPosition);
					positionInfo.put("pagePosition", pagePosition);
				}

			}
		}
		return positionInfo;
	}

	/**
	 * batterHandle:连击处理逻辑方法
	 */
	private void batterHandle(Map<String, String> lmGiftInfo, String piFrom, String liGiftId) {
		// 价格最低一组下标
		int minPricePosition = mGiftEffectLayouts.size() - 1;
		int minPrice = Integer.MAX_VALUE;
		// 礼物是本人在发送 要弹出暴击连击按钮
		if (mUid.equals(piFrom) && Utils.getBooleanFlag(lmGiftInfo.get("bonusButtonEnabled"))) {
			if (!mRippleCountBonusTime.isShown()) {
				mRlCountBonusTimes.setVisibility(View.VISIBLE);
				mRippleCountBonusTime.setVisibility(View.VISIBLE);
				onPlayChatListenrOnClick();
				countTimeBonusData();
			}
		}
		// 如果正在显示送的礼物，且是连击，则显示礼物连击动效；
		for (int i = 0; i < mGiftEffectLayouts.size(); i++) {
			if (mGiftEffectLayouts.get(i).getVisibility() == View.VISIBLE) {
				GiftEffectViewData data = mGiftEffectDatas.get(i);
				if (data.mGiftPrice <= minPrice) {
					minPrice = data.mGiftPrice;
					minPricePosition = i;
				}
				// 根据送礼人名称、礼物Id， //{礼物数量，判断是否连击}
				// lmGiftInfo.get("count").equals(data.mGiftCount)
				if (piFrom.equals(data.mUserId) && liGiftId.equals(data.mGiftId)) {
					setGiftEffectData(lmGiftInfo, data);
					// 显示连击动效
					showBatterGiftsEffect(mGiftEffectLayouts.get(i), data);
					return;
				}
			}

		}

		// 如果不是连击，选择一个view显示动效
		for (int i = mGiftEffectLayouts.size() - 1; i >= 0; i--) {
			if (mGiftEffectLayouts.get(i).getVisibility() != View.VISIBLE) {
				GiftEffectViewData data = mGiftEffectDatas.get(i);
				setGiftEffectData(lmGiftInfo, data);
				showBatterGiftsEffect(mGiftEffectLayouts.get(i), data);
				return;
			}
		}

		// 如果都有显示动效，把价格最低一行 动效顶替掉(先invisible，然后)
		int lastPosition = minPricePosition;
		GiftEffectViewData data = mGiftEffectDatas.get(lastPosition);
		if (mHandler != null) mHandler.removeMessages(data.msgWhat);
		mGiftEffectLayouts.get(lastPosition).setVisibility(View.INVISIBLE);
		setGiftEffectData(lmGiftInfo, data);
		showBatterGiftsEffect(mGiftEffectLayouts.get(lastPosition), data);
	}

	/**
	 * 连击礼物特效
	 */
	public void showBatterGiftsEffect(FrameLayout view, final GiftEffectViewData data) {
		final GiftEffectViewHold mHold = (GiftEffectViewHold) view.getTag();
		mGiftScaleAnim = (AnimationSet) AnimationUtils.loadAnimation(mActivity, R.anim
				.anim_live_gift_effect_scale);
		int giftcount = Integer.parseInt(data.mGiftCount);
		// 如果已显示，则是连击效果
		if (view.getVisibility() == View.VISIBLE) {
			// 一次性只送1个
			if (giftcount == 1) {
				mHold.mItemGiftGroup.setText(mActivity.getResources().getString(R.string
						.live_gift_group_num_tip, String.valueOf(data.mGiftNum)));
				EvtLog.e(TAG, "ainm 单个连击：" + data.mGiftNum);
				// mHold.mItemGiftGroup.clearAnimation();
				mHold.mItemLlGiftGroup.startAnimation(mGiftScaleAnim);
				mHold.mItemLlGiftGroup.setVisibility(View.VISIBLE);
				mHold.mItemGiftGroup.setVisibility(View.VISIBLE);
				mHold.mItemLlGiftGroupVer.setVisibility(View.GONE);
			} else {
				mHold.mItemGiftGroup.setVisibility(View.GONE);
				mHold.mItemLlGiftGroupVer.setVisibility(View.VISIBLE);
				mHold.mItemLlGiftGroupVer.addData(data.mGiftNum);
				mHold.mItemLlGiftGroupVer.startAnim();
				EvtLog.e(TAG, "ainm 多个连击：" + data.mGiftNum);
			}
		} else {
			ImageLoaderUtil.with().loadImage(mActivity, mHold.mItemGiftPhoto, data.mGiftPhoto);
			ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity, mHold.mItemUserPhoto,
					data.mUserPhoto);
			// 设置tag属性，用于点击头像需要的用户数据
			mHold.mItemUserPhoto.setTag(R.id.tag_first, data.mUserName);
			mHold.mItemUserPhoto.setTag(R.id.tag_second, data.mUserId);
			mHold.mItemGiftGroup.setText(mActivity.getResources().getString(R.string
					.live_gift_group_num_tip, String.valueOf(data.mGiftNum)));
			mHold.mItemUserName.setText(data.mUserName);
			mHold.mItemGiftNum.setText(mActivity.getResources().getString(R.string
					.live_gift_num_tip, data.mGiftCount));
			mHold.mItemGiftTip.setText(mActivity.getResources().getString(R.string
					.live_gift_name_tip, data.mGiftName));
			if (giftcount > 1314) {
				giftcount = 1314;
			}
			switch (giftcount) {
				case 10:
					mHold.mItemBackgroup.setBackgroundResource(R.drawable.bg_gift_effect_10);
					break;
				case 30:
					mHold.mItemBackgroup.setBackgroundResource(R.drawable.bg_gift_effect_30);
					break;
				case 66:
					mHold.mItemBackgroup.setBackgroundResource(R.drawable.bg_gift_effect_66);
					break;
				case 188:
					mHold.mItemBackgroup.setBackgroundResource(R.drawable.bg_gift_effect_188);
					break;
				case 520:
					mHold.mItemBackgroup.setBackgroundResource(R.drawable.bg_gift_effect_520);
					break;
				case 1314:
					mHold.mItemBackgroup.setBackgroundResource(R.drawable.bg_gift_effect_1314);
					break;
				default:
					mHold.mItemBackgroup.setBackgroundResource(R.drawable.bg_gift_effect);
					break;
			}
			mGiftLayoutTransAnim = AnimationUtils.loadAnimation(mActivity, R.anim
					.anim_live_gift_effect_layout_trans);
			view.clearAnimation();
			view.startAnimation(mGiftLayoutTransAnim);
			mGiftLayoutTransAnim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mHold.mItemGiftPhoto.clearAnimation();
					mHold.mItemGiftPhoto.startAnimation(mGiftTransAnim);
					mHold.mItemGiftPhoto.setVisibility(View.VISIBLE);
					// 如果一次送一个礼物，并且是×1
					if (Integer.parseInt(data.mGiftCount) == 1) {
						mHold.mItemLlGiftGroup.startAnimation(mGiftScaleAnim);
						mHold.mItemGiftGroup.setVisibility(View.VISIBLE);
						mHold.mItemLlGiftGroup.setVisibility(View.VISIBLE);
					} else {
						mHold.mItemLlGiftGroup.setVisibility(View.VISIBLE);
						mHold.mItemGiftGroup.setVisibility(View.GONE);
						mHold.mItemLlGiftGroupVer.setVisibility(View.VISIBLE);
						/* 滚动动画 */
						mHold.mItemLlGiftGroupVer.addData(data.mGiftNum);
						mHold.mItemLlGiftGroupVer.startAnim();
					}
				}
			});
			view.setVisibility(View.VISIBLE);
		}
		if (mHandler != null) mHandler.removeMessages(data.msgWhat);
		sendEmptyMsgDelayed(data.msgWhat, GIFT_SHOW_TIME);

	}

	/**
	 * 用户进入直播间  --- 座驾入场动效处理
	 */
	private void userEntryCarEffectHandle(String androidEffect) {
		mCurrentUserEntryEffectNum++;
		final AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				//动画显示结束
				mGJEffectEntry.removeAllListeners();
				mGJEffectEntry.removeAllViews();
				mGJEffectEntry.setVisibility(View.GONE);
				mCurrentUserEntryEffectNum--;
				sendEmptyMsg(MSG_USER_ENTRY_EFFECT_HANDLE);
			}
		};

		EffectGiftLoader.getInstance(mActivity).loadDataForComposition(androidEffect, new
				EffectComposition.OnCompositionLoadedListener() {
					@Override
					public void onCompositionLoaded(EffectComposition composition) {
						// 开始显示动画
						if (composition != null && mCurrentScreenType == ActivityInfo
								.SCREEN_ORIENTATION_PORTRAIT) {
							// 显示大额礼物动效
							mGJEffectEntry.setComposition(composition);
							mGJEffectEntry.setVisibility(View.VISIBLE);
							mGJEffectEntry.startAnimation(animatorListenerAdapter);
						}
						// 加载动画信息失败
						else {
							mCurrentUserEntryEffectNum--;
							sendEmptyMsg(MSG_USER_ENTRY_EFFECT_HANDLE);
						}
					}
				});
	}

	/**
	 * 用户进入直播间 -- 文字动效
	 *
	 * @param userInfo 用户信息
	 */
	private void userEntryTextEffectHandle(Map<String, String> userInfo) {
		mCurrentUserEntryEffectNum++;
		final String uid = userInfo.get("uid");
		final String nickName = userInfo.get("nickName");
		//用户等级
		String level = userInfo.get("level");
		//		String type = userInfo.get("type");
		// 用户是否开通守护
		final String isGuard = userInfo.get("isGuard");
		//0月守护 1年守护
		final String guardTimeType = userInfo.get("guardTimeType");
		final String mountId = userInfo.get("mountId");
		//显示座驾
		final boolean showMount = Utils.getInteger(mountId, 0) > 0;
		final String mountName = userInfo.get("mountName");
		final String mountAction = userInfo.get("mountAction");
		if (mUserEntryLevelAlphaAnimation == null) {
			mUserEntryLevelAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
			mUserEntryLevelAlphaAnimation.setDuration(200);
		}
		mUserEntryLevelAlphaAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				CharSequence charSequence;
				if (showMount) {
					SpannableString loMountName = new SpannableString(mountName);
					loMountName.setSpan(new ForegroundColorSpan(0xfffff000), 0, loMountName
							.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					charSequence = SpannableStringBuilder.valueOf(nickName).append(" ").append
							(mountAction).append(loMountName).
							append(mActivity.getResources().getString(R.string
									.live_user_entry_text));
				} else {
					SpannableString loFrom = new SpannableString(mActivity.getResources()
							.getString(R.string.live_user_entry_text));
					loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#ffffff")), 0,
							loFrom.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					charSequence = SpannableStringBuilder.valueOf(nickName).append(" ").append
							(loFrom);
				}

				mUserEntryText.start(charSequence);
				mUserEntryText.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		if (mUserEntryEffectTranAnimation == null) {
			mUserEntryEffectTranAnimation = AnimationUtils.loadAnimation(mActivity, R.anim
					.anim_live_user_effect_trans);

		}
		mUserEntryEffectTranAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mUserEntryEffect.setVisibility(View.INVISIBLE);
				postDelayed(new Runnable() {
					@Override
					public void run() {
						mUserEntryLayout.setVisibility(View.INVISIBLE);
						mCurrentUserEntryEffectNum--;
						sendEmptyMsg(MSG_USER_ENTRY_EFFECT_HANDLE);
					}
				}, 1000);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		if (mUserEntryLayoutTransAnimation == null) {
			mUserEntryLayoutTransAnimation = AnimationUtils.loadAnimation(mActivity, R.anim
					.anim_live_user_entry_trans);
		}
		mUserEntryLayoutTransAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// 如果开通守护的用户进入
				if (Boolean.valueOf(isGuard) && !showMount) {
					mUserGuardEntryText.setVisibility(View.VISIBLE);
				}
				mUserEntryLevel.setVisibility(View.VISIBLE);
				mUserEntryLevel.startAnimation(mUserEntryLevelAlphaAnimation);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		// moundId > 0表示为有座驾； isGuard表示有守护； leve表示用户等级，大于10进场动效
		// 进程动画优先级： 隐身 > 座驾 > 守护 > 等级特权
		if (showMount) {
			mUserEntryText.setTextColor(0xffffffff);
			mUserEntryBackgroud.setBackgroundResource(R.drawable.bg_car_user_entry);
			mUserEntryEffect.setImageResource(R.drawable.effect_user_entry_car);
		} else if (Boolean.valueOf(isGuard)) {
			// 如果开通守护的用户进入
			mUserEntryText.setTextColor(0xffffffff);
			if ("1".equals(guardTimeType)) {
				mUserEntryBackgroud.setBackgroundResource(R.drawable.bg_guard_user_entry_year);
			} else {
				mUserEntryBackgroud.setBackgroundResource(R.drawable.bg_guard_user_entry_month);
			}
			mUserEntryEffect.setImageResource(R.drawable.effect_user_entry);
		} else {
			// 如果18级以下
			if (LVIE_USER_HIGH_LEVEL.compareTo(level) > 0) {
				mUserEntryText.setTextColor(0xffFDEAD5);
				mUserEntryBackgroud.setBackgroundResource(R.drawable.bg_user_entry_low);
			} else {
				mUserEntryText.setTextColor(0xffB0F1FF);
				mUserEntryBackgroud.setBackgroundResource(R.drawable.bg_user_entry_high);
			}
			mUserEntryEffect.setImageResource(R.drawable.effect_user_entry);
		}

		mUserEntryEffect.setVisibility(View.INVISIBLE);
		ImageLoaderUtil.with().loadImage(mActivity, mUserEntryLevel, Utils
				.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, level));
		mUserEntryLevel.setVisibility(View.INVISIBLE);
		mUserGuardEntryText.setVisibility(View.GONE);
		mUserEntryText.setVisibility(View.INVISIBLE);
		mUserEntryText.setText("");
		mUserEntryLayout.startAnimation(mUserEntryLayoutTransAnimation);
		mUserEntryLayout.setVisibility(View.VISIBLE);
		mUserEntryLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MobclickAgent.onEvent(FeizaoApp.mConctext,
						"clickHeadInSpecialEffectOfEnterBroadcast");
				mClickUsernameListener.onClick(nickName, uid);
			}
		});

		mUserEntryText.setOnTypeViewListener(new TypeTextView.OnTypeViewListener() {
			@Override
			public void onTypeStart() {

			}

			@Override
			public void onTypeOver() {
				postDelayed(new Runnable() {
					@Override
					public void run() {
						mUserEntryEffect.setVisibility(View.VISIBLE);
						mUserEntryEffect.startAnimation(mUserEntryEffectTranAnimation);
					}
				}, 500);

			}
		});

	}

	/**
	 * 主播升级 动效处理
	 */
	private void anchorLevelUpHandle(final String nickName, String moderatorLevel, final String
			moderatorLevelName) {
		if (mAnchorRocketTranAnim == null) {
			mAnchorRocketTranAnim = AnimationUtils.loadAnimation(mActivity.getApplicationContext
					(), R.anim.anim_anchor_level_up_tran);
		}
		if (mAnchorBackgroupRotateAnim == null) {
			mAnchorBackgroupRotateAnim = AnimationUtils.loadAnimation(mActivity
					.getApplicationContext(), R.anim.anim_anchor_level_up_rotate);
			mAnchorBackgroupRotateAnim.setInterpolator(new LinearInterpolator());
		}
		SpannableString levelNameSs = new SpannableString(moderatorLevelName);
		levelNameSs.setSpan(new ForegroundColorSpan(Color.parseColor("#fff000")), 0, levelNameSs
				.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		final CharSequence charSequence = SpannableStringBuilder.valueOf("荣登").append
				(levelNameSs).append("级啦！");
		AnimationDrawable animationDrawable = (AnimationDrawable) mAnchorLevelAnim.getBackground();

		mAnchorRocketTranAnim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mAnchorBackgroupIv.setVisibility(View.VISIBLE);
				mAnchorBackgroupIv.startAnimation(mAnchorBackgroupRotateAnim);
				mAnchorLevel.setVisibility(View.VISIBLE);
				postDelayed(new Runnable() {
					@Override
					public void run() {
						mAnchorLevelUpTip1.start("恭喜主播");
						mAnchorLevelUpTip1.setVisibility(View.VISIBLE);
						mAnchorLevelUpTip2.start(nickName, TypeTextView.TYPE_TIME_DELAY, 350);
						mAnchorLevelUpTip2.setVisibility(View.VISIBLE);
						mAnchorLevelUpTip3.start(charSequence, TypeTextView.TYPE_TIME_DELAY, 1300);
						mAnchorLevelUpTip3.setVisibility(View.VISIBLE);
					}
				}, 500);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		mAnchorLevelUpLayout.setVisibility(View.VISIBLE);
		mAnchorBackgroupIv.setVisibility(View.INVISIBLE);
		mAnchorLevelUpLayout.startAnimation(mAnchorRocketTranAnim);
		ImageLoaderUtil.with().loadImage(mActivity, mAnchorLevel, Utils.getLevelImageResourceUri
				(Constants.USER_ANCHOR_LEVEL_PIX, moderatorLevel));
		mAnchorLevel.setVisibility(View.INVISIBLE);
		mAnchorLevelUpTip1.setVisibility(View.INVISIBLE);
		mAnchorLevelUpTip1.setText("");
		TextPaint textPaint = mAnchorLevelUpTip1.getPaint();
		float textPaintWidth = textPaint.measureText("恭喜主播");
		mAnchorLevelUpTip1.setWidth((int) textPaintWidth);

		mAnchorLevelUpTip2.setVisibility(View.INVISIBLE);
		mAnchorLevelUpTip2.setText("");
		textPaintWidth = textPaint.measureText(nickName);
		mAnchorLevelUpTip2.setWidth((int) textPaintWidth);

		mAnchorLevelUpTip3.setVisibility(View.INVISIBLE);
		mAnchorLevelUpTip3.setText("");
		textPaintWidth = textPaint.measureText(charSequence, 0, charSequence.length());
		mAnchorLevelUpTip3.setWidth((int) textPaintWidth);
		animationDrawable.start();
		mAnchorLevelUpTip3.setOnTypeViewListener(new TypeTextView.OnTypeViewListener() {
			@Override
			public void onTypeStart() {

			}

			@Override
			public void onTypeOver() {
				postDelayed(new Runnable() {
					@Override
					public void run() {
						mAnchorLevelUpLayout.setVisibility(View.INVISIBLE);
						mAnchorBackgroupIv.clearAnimation();
						sendEmptyMsg(MSG_ANCHOR_LEVEL_UP_HANDLE);
					}
				}, 1000);
			}
		});
	}

	/**
	 * 消息流URL
	 */
	private String getMessageUrl() {
		String lsSessionId = HttpSession.getInstance(mActivity).getCookie("PHPSESSID");
		String lsUid = HttpSession.getInstance(mActivity).getCookie("uid");
		if (lsSessionId == null) lsSessionId = "xxx";
		if (lsUid == null) lsUid = "-1";
		return String.format(Constants.PLAY_MESSAGE_URL, mmRoomInfo.get("msgIp"), mmRoomInfo.get
				("msgPort"), lsSessionId, lsUid, mmIntentRoomInfo.get("rid"));
	}

	/**
	 * 送花消息流url
	 */
	private String getFlowerMessageUrl() {
		String lsSessionId = HttpSession.getInstance(mActivity).getCookie("PHPSESSID");
		String lsUid = HttpSession.getInstance(mActivity).getCookie("uid");
		if (lsSessionId == null) lsSessionId = "xxx";
		if (lsUid == null) lsUid = "-1";
		return String.format(Constants.PLAY_MESSAGE_URL, mmRoomInfo.get("flowerIp"), mmRoomInfo
				.get("flowerPort"), lsSessionId, lsUid, mmIntentRoomInfo.get("rid"));

	}

	/**
	 * 注册广播（消息更新广播）
	 */
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		networkReceiver = new ConnectionChangeReceiver();
		networkReceiver.setOnNetChangeListener(new NetwrokChangeCallback() {

			@Override
			public void wifiConnected() {
				EvtLog.e(TAG, "ConnectionChangeReceiver wifiConnected");
				networkRecovery();
			}

			@Override
			public void noConnected() {
				EvtLog.e(TAG, "ConnectionChangeReceiver noConnected");
				showToast(Constants.NETWORK_FAIL, TOAST_LONG);
				chatFragment.onSysMsg(Constants.NETWORK_FAIL);
				mNoNetworkFlag = true;
				isWifiNetwork = false;
			}

			@Override
			public void gprsConnected() {
				EvtLog.e(TAG, "ConnectionChangeReceiver gprsConnected");
				showToast(R.string.network_2G_msg_2, TOAST_LONG);
				chatFragment.onSysMsg(getResources().getString(R.string.network_2G_msg_2));
				networkRecovery();
			}
		});
		this.registerReceiver(networkReceiver, filter);
	}

	/**
	 * 注销广播
	 */
	private void unregisterReceiver() {
		if (networkReceiver != null) this.unregisterReceiver(networkReceiver);
	}

	/**
	 * networkRecovery:网络恢复处理. <br/>
	 *
	 * @since JDK 1.6
	 */
	protected void networkRecovery() {
		if (mmRoomInfo == null) {
			BusinessUtils.getRoomInfo(this, new GetRoomCallbackData(this), mmIntentRoomInfo.get
					("rid"));
		} else {
			if (!mWebSocketConnectStatus) mWebSocketImpl.start(getMessageUrl());
			if (!mWebSocketConnectStatusFlower) mFlowerWebSocketImpl.start(getFlowerMessageUrl());
		}
		if (mPackageGiftsData == null) {
			BusinessUtils.getUserPackageInfo(this, new GetUserPackageCallbackData(this));
		}
		mNoNetworkFlag = false;
		isWifiNetwork = true;
	}

	/**
	 * 判断当前是否有网络，true有，否则false
	 */
	public boolean isNoNetworkTipMsg() {
		// 如果没有网络，不进行下面操作
		if (mNoNetworkFlag) {
			showToast(Constants.NETWORK_FAIL, Toast.LENGTH_SHORT);
			return true;
		} else return false;
	}

	/**
	 * 判断是否重复进直播间----进入直播间5秒之类不在播放进程动画
	 */
	private boolean checkIsEntryRecently(String uid) {
		if (mRecentUser == null) mRecentUser = new LinkedHashMap<>();

		long currentTime = SystemClock.elapsedRealtime() / 1000;

		//移除所有超过进场超过5秒的用户
		Iterator<Map.Entry<String, Long>> iterator = mRecentUser.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Long> entry = iterator.next();
			if (currentTime - entry.getValue() > AppConfig.getInstance().mountDisplayFreq)
				iterator.remove();
			else break;
		}
		if (mRecentUser.get(uid) != null) return true;
		else {
			mRecentUser.put(uid, currentTime);
			return false;
		}
	}

	/**
	 * websoccket 连接错误
	 */
	public void onConnectStatus() {
		mWebSocketConnectStatus = true;
	}

	/* ####################### 消息回调 start ########################### */

	@Override
	public void onError(String piErrCode, String errorMsg, String cmd) {
		EvtLog.e(TAG, "onError  piErrCode, errorMsg, cmd:" + piErrCode + "," + errorMsg + cmd);
		if (NetConstants.SENT_STATUS_NEED_VALIDATE.equals(piErrCode)) {
			GTValidateRequest.getInstance().validate(new WeakReference<Activity>(LiveBaseActivity
					.this));
		}
		// 如果用户余额不足
		if ((Constants.ON_SEND_GIFT.equals(cmd) || Constants.ON_SEND_BARRAGE.equals(cmd)) &&
				"101".equals(piErrCode)) {
			sendEmptyMsg(MSG_SEND_GIFT_BLANCE_LACK);
			return;
		} else if (Constants.ON_CONNECT_STATUS.equals(cmd)) {
			if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				chatFragment.sendChatMsg(chatFragment.onSysMsg(errorMsg));
			}
			onConnectStatus();
		}
		showTips(errorMsg);

	}

	@Override
	public void initRoom(String piUid, String piType, String psNickname, String level, String
			fromModeratorLevel, String psPhoto, String ban, String cid) {
		EvtLog.e(TAG, "initRoom(String piUid, String piType," + piUid + "," + piType);
	}

	@Override
	public void addUser(String piUid, String piType, String psNickname, String level, String
			fromModeratorLevel, String psPhoto, String lowkeyEnter, String cid, String medals,
						String isGuard, String guardType, String guardTimeType, String mountId,
						String mountName, String mountAction, String androidMount) {
		EvtLog.e(TAG, "addUser(String piUid, String piType," + piUid + "," + piType);
		// 已经进入房间，且不是游客，才是显示进入房间的欢迎广播
		mAudienceNum++;
		sendEmptyMsg(MSG_AUDIENCE_NUM_CHANGE);
		if (!Constants.USER_TYPE_UNLOGIN.equals(piType)) {
			// 如果隐身，直接返回
			if (Utils.getBooleanFlag(lowkeyEnter)) return;
			try {
				// moundId > 0表示为有座驾； isGuard表示有守护； leve表示用户等级，大于10进场动效
				// 进程动画优先级： 隐身 > 座驾 > 守护 > 等级特权
				if (Utils.getInteger(mountId, 0) > 0 || Integer.valueOf(level) >= 10 || Boolean
						.valueOf(isGuard)) {
					if (checkIsEntryRecently(piUid)) return;
					Map<String, String> userInfo = new HashMap<>();
					userInfo.put("uid", piUid);
					userInfo.put("nickName", psNickname);
					userInfo.put("type", piType);
					userInfo.put("level", level);
					userInfo.put("isGuard", isGuard);
					userInfo.put("guardTimeType", guardTimeType);
					userInfo.put("mountId", mountId);
					userInfo.put("mountName", mountName);
					userInfo.put("mountAction", mountAction);
					userInfo.put("androidMount", androidMount);
					mUserEntryQueue.offer(userInfo);
					sendEmptyMsg(MSG_USER_ENTRY_EFFECT_HANDLE);
				}
				CharSequence charSequence = chatFragment.onUserEnter(piUid, psNickname, piType,
						level, ChatListAdapter.USER_COME_IN, medals, fromModeratorLevel, guardType);
				chatFragment.sendChatMsg(charSequence, LiveChatFragment.MSG_TYPE_COMEIN);
				//			mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false,
				// mCurrentScreenType);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void delUser(String piUid, String piType, String psNickname, String psPhoto, String
			ban, String cid) {
		EvtLog.e(TAG, "delUser(String piUid, String piType," + piUid + "," + piType);
		mAudienceNum--;
		sendEmptyMsg(MSG_AUDIENCE_NUM_CHANGE);
	}

	/**
	 * @param piPrivate 私聊或公聊
	 * @return
	 */
	@Override
	public void onChatMsg(String piFrom, String psFromNickname, String fromLevel, String
			fromType, String fromGuardType, String piTo, String psToNickname, String toLevel,
						  String toType, String toGuardType, String psMsg, String piPrivate,
						  String fromMedals, String toMedals, String fromModeratorLevel, String
								  toModeratorLevel) {
		EvtLog.e(TAG, "onChatMsg");
		try {
			int level = Integer.valueOf(fromLevel);
			CharSequence charSequence;
			// 用户18级以上
			if (level >= 17) {
				charSequence = chatFragment.onChatMsg(piFrom, piTo, fromLevel, fromType,
						psFromNickname, fromGuardType, psToNickname, toLevel, toType,
						toGuardType, psMsg, piPrivate, fromMedals, toMedals, true,
						fromModeratorLevel, toModeratorLevel);
			} else {
				charSequence = chatFragment.onChatMsg(piFrom, piTo, fromLevel, fromType,
						psFromNickname, fromGuardType, psToNickname, toLevel, toType,
						toGuardType, psMsg, piPrivate, fromMedals, toMedals, false,
						fromModeratorLevel, toModeratorLevel);
			}
			chatFragment.sendChatMsg(charSequence);
			mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false, mCurrentScreenType);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param data
	 */
	@Override
	public void onNewBulleBarrage(final JSONObject data) {
		DanmakuEntity danmakuEntity = new DanmakuEntity(data);
		// 如果是付费弹幕
		if ("1".equals(data.optString("btype"))) {
			if (data.optString("uid").equals(mUid)) {
				Message message = new Message();
				message.arg1 = Integer.parseInt(data.optString("cost"));
				message.what = MSG_PAY_DANMU_MESSAGE;
				sendMsg(message);
			}
		} else if ("2".equals(data.optString("btype"))) {// 如果是开通守护弹幕,更新主播守护数据
			Map<String, String> tempMap = new HashMap<>();
			tempMap.put("uid", data.optString("openUid"));
			tempMap.put("timeType", data.optString("openTimeType"));
			tempMap.put("type", data.optString("openGuardType"));
			tempMap.put("headPic", data.optString("openHeadPic"));
			tempMap.put("cost", data.optString("cost"));
			Message message = new Message();
			message.obj = tempMap;
			message.what = MSG_PAY_DANMU_OPEN_GUARD_MESSAGE;
			sendMsg(message);
		} else if ("3".equals(data.optString("btype")) || "4".equals(data.optString("btype")) ||
				"5".equals(data.optString("btype"))) {//如果是广播弹幕
			mDanmakuBroastcastActionManager.addDanmu(danmakuEntity);
			return;
		}
		mDanmakuActionManager.addDanmu(danmakuEntity);
	}

	/**
	 * TODO 简单描述该方法的实现功能（可选）.
	 */
	@Override
	public void onNewRewards(JSONObject data) {
		// 累计活动
		urlDatas.offer(data);
		showActivityWebView(data.optString("boxOpenUrl"));
	}

	@Override
	public void onMessageCardActive(JSONObject data) {
		Message msg = Message.obtain();
		msg.what = MSG_MESSAGE_CARD_ACTIVE;
		msg.obj = data.optInt("cost", 0);
		sendMsg(msg);
	}

	// 邀请连线
	@Override
	public void onInviteVideoChat(InviteVideoChat data) {
		//		Message msg = Message.obtain();
		//		msg.what = MSG_INVITE_VIDEO_CHAT_SUCC;
		//		sendMsg(msg);
	}

	// 取消邀请连线
	@Override
	public void onCancelVideoChat(String uid) {
	}

	// 用户拒绝主播连线
	@Override
	public void onUserRejectVideoChat(String uid) {
		showTips(R.string.live_connect_cancel_tip);
	}

	// 接受连线
	@Override
	public void onAcceptVideoChat(AcceptVideoChat data) {
		EvtLog.e(TAG, "onAcceptVideoChat");
		mIsLiveConnecting = true;
		mConnectLivingLayout.setVisibility(View.VISIBLE);
		mConnectLivingLayout.setTag(R.id.tag_second, data.getUid());
	}

	// 结束连线
	@Override
	public void onVideoChatEnd(String uid, String mid, String pullUrl, String pushUrl, String
			msg, String endType) {
		EvtLog.e(TAG, "onVideoChatEnd");
		mIsLiveConnecting = false;
		showTips(msg);
		initConnectChat();
	}

	// 推流地址更新
	@Override
	public void onChangeVideoPullUrl(String pullUrl) {

	}

	//连麦成功广播连麦用户信息
	@Override
	public void onVideoChat(VideoChat data) {
		if (data.getVideoChatType() == InviteVideoChat.INVITE_CHAT_TYPE_MIC) {
			OperationHelper.onEvent(FeizaoApp.mConctext, "clickChooseVoiceLinkModelButton");
		} else {
			OperationHelper.onEvent(FeizaoApp.mConctext, "clickChooseVideoLinkModelButton");
		}
	}

	@Override
	public void onRefreshOnlineNum(JSONObject data) {
		mAudienceNum = data.optInt("num");
		sendEmptyMsg(MSG_AUDIENCE_NUM_CHANGE);
	}

	@Override
	public void onSystemMessage(final JSONObject data) {
		if (data != null) {
			// 如果是弹框提示用户
			if ("1".equals(data.optString("type"))) {
				postDelayed(new Runnable() {
					@Override
					public void run() {
						UiHelper.showSingleConfirmDialog(mActivity, data.optString("msg"), null);
					}
				}, 0);
			} else {
				CharSequence charSequence = chatFragment.onHtmlTextMsg(data.optString("msg"));
				chatFragment.sendChatMsg(charSequence);
				//				mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false,
				// mCurrentScreenType);
			}
		}
	}

	@Override
	public void onHotRank(JSONObject data) {
		try {
			Map<String, String> mapData = JSONParser.parseOne(data);
			// 当前热门排行榜显示，刷新所有数据
			if (mHotRankPopupWindow != null && mHotRankPopupWindow.isShowing()) {
				getHotRankInfo();
			} else {
				// 热门排名列表未显示，只更新当前排名
				if (mmRoomInfo == null) return;
				if ("-1".equals(mapData.get("rank"))) {
					mmRoomInfo.put("isHot", false);
				} else {
					mmRoomInfo.put("isHot", true);
					mNowRank.setText(mapData.get("rank"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onModeratorLevelIncrease(JSONObject data) {
		int moderatorLevel = Integer.parseInt(data.optString("moderatorLevel"));
		// 主播等级大于21级，显示升级提示
		if (moderatorLevel >= 20) {
			// 主播升级
			mAnchorLevelUpQueue.offer(data);
			sendEmptyMsg(MSG_ANCHOR_LEVEL_UP_HANDLE);
		}
	}

	@Override
	public void onUserLevelIncrease(JSONObject data) {
		CharSequence charSequence = chatFragment.onUserLevelUp(data.optString("uid"), data
				.optString("nickname"), null, data.optString("level"));
		chatFragment.sendChatMsg(charSequence);
		mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false, mCurrentScreenType);
	}

	@Override
	public void onSendGift(String piGiftId, String pname, String cost, String ptype, String
			piGiftCount, String piFrom, String psFromNickname, String psFromHeadPic, String
								   fromLevel, String fromModeratorLevel, String fromType, String fromGuardType, String
								   psGiftImg, String combo, String comboNum, String comboGiftNum, String activityId,
						   String activityTotalGiftNum, String models, String pkgItemsetId,
						   String androidEffect, String giftBonus, String bonusButtonEnabled,
						   String hitbangTicketNum, String leftCoin) {
		EvtLog.e(TAG, "onSendGift");
		Map<String, String> lmGiftInfo = new HashMap<>();
		lmGiftInfo.put("piFrom", piFrom);
		lmGiftInfo.put("from_user", psFromNickname);
		lmGiftInfo.put("fromHeadPic", psFromHeadPic);
		lmGiftInfo.put("id", piGiftId);
		lmGiftInfo.put("count", piGiftCount);
		lmGiftInfo.put("combo", combo);
		lmGiftInfo.put("comboNum", comboNum);
		lmGiftInfo.put("comboGiftNum", comboGiftNum);
		lmGiftInfo.put("activityId", activityId);
		lmGiftInfo.put("activityTotalGiftNum", activityTotalGiftNum);

		lmGiftInfo.put("giftImg", psGiftImg);
		lmGiftInfo.put("giftName", pname);
		lmGiftInfo.put("giftConsume", cost);
		lmGiftInfo.put("giftType", ptype);
		lmGiftInfo.put("androidEffect", androidEffect);
		lmGiftInfo.put("hitbangTicketNum", hitbangTicketNum);
		lmGiftInfo.put("balance", leftCoin);
		// 背包礼物
		lmGiftInfo.put("pkgItemsetId", pkgItemsetId);
		// 是否暴击连击
		lmGiftInfo.put("bonusButtonEnabled", bonusButtonEnabled);
		// 暴击中奖
		try {
			if (!TextUtils.isEmpty(giftBonus)) {
				Map<String, String> giftBonusMap = JSONParser.parseOne(giftBonus);
				if (giftBonusMap != null && giftBonusMap.size() > 0) {
					// 中奖倍数
					lmGiftInfo.put("giftBonusTimes", giftBonusMap.get("bonus_times"));
					lmGiftInfo.put("giftPrice", giftBonusMap.get("gift_price"));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		CharSequence charSequence = chatFragment.onGift(piFrom, psFromNickname, fromLevel,
				fromModeratorLevel, fromType, fromGuardType, piGiftCount, psGiftImg, models);
		chatFragment.sendChatMsg(charSequence);
		//		mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false,
		// mCurrentScreenType);

		Message loMsg = Message.obtain();
		loMsg.what = MSG_SEND_GIFT_SUCCESS;
		loMsg.obj = lmGiftInfo;
		sendMsg(loMsg);
	}

	@Override
	public void onSendFlower() {
		mFlowwerNum++;
		Message loMsg = Message.obtain();
		loMsg.what = MSG_SEND_FLOWER_SUCCESS;
		sendMsg(loMsg);
	}

	@Override
	public void onUserAttention(String piFrom, String psFromNickname, String fromLevel, String
			fromType, String medalsStr, String guardType) {
		// 送花 就不显示在弹幕上了
		CharSequence charSequence = chatFragment.onUserOpearMsg(piFrom, psFromNickname, fromType,
				fromLevel, ChatListAdapter.USER_FOCUS_ANCHOR, medalsStr, null, guardType);
		chatFragment.sendChatMsg(charSequence);
		mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false, mCurrentScreenType);
	}

	@Override
	public void onUserShare(String piFrom, String psFromNickname, String fromLevel, String
			fromType, String medalsStr, String moderatorLevel, String guardType) {
		// 分享 就不显示在弹幕上了
		CharSequence charSequence = chatFragment.onUserOpearMsg(piFrom, psFromNickname, fromType,
				fromLevel, ChatListAdapter.USER_SHARE_ROOM, medalsStr, moderatorLevel, guardType);
		chatFragment.sendChatMsg(charSequence);
		mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false, mCurrentScreenType);
	}

	@Override
	public void onBan(String operatorUid, String operatorNickname, String banUid, String
			banNickname, String expires) {
		CharSequence charSequence = chatFragment.onManagerOpearMsg(banUid, banNickname, null,
				null, String.format(mActivity.getResources().getString(R.string.ban_tip), "管理员"));
		//				String.format(mActivity.getResources().getString(R.string.ban_tip),
		// operatorNickname));
		chatFragment.sendChatMsg(charSequence);
		//		mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false,
		// mCurrentScreenType);
		if (UserInfoConfig.getInstance().id.equals(banUid)) showBanHintDialog(true);
	}

	@Override
	public void onUnBan(String operatorUid, String operatorNickname, String banUid, String
			banNickname) {
		CharSequence charSequence = chatFragment.onManagerOpearMsg(banUid, banNickname, null,
				null, String.format(mActivity.getResources().getString(R.string.unban_tip), "管理员"));
		chatFragment.sendChatMsg(charSequence);
		//				String.format(mActivity.getResources().getString(R.string.unban_tip),
		// operatorNickname));

		//		mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false,
		// mCurrentScreenType);
		if (UserInfoConfig.getInstance().id.equals(banUid)) showBanHintDialog(false);
	}


	@Override
	public void onSetAdmin(String operatorUid, String operatorNickname, String setAdminUid,
						   String setAdminNickname) {
		CharSequence charSequence = chatFragment.onManagerOpearMsg(setAdminUid, setAdminNickname,
				null, null, String.format(mActivity.getResources().getString(R.string
						.setadmin_tip), getString(R.string.anchor)));
		chatFragment.sendChatMsg(charSequence);
		//		mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false,
		// mCurrentScreenType);
		mManagerUids.add(setAdminUid);
		// 如果操作的是当前用户，更改用户类别
		if (setAdminUid.equals(mUid)) {
			mmRoomInfo.put("userType", Constants.USER_TYPE_ADMIN);
		}
	}

	@Override
	public void onUnsetAdmin(String operatorUid, String operatorNickname, String setAdminUid,
							 String setAdminNickname) {
		CharSequence charSequence = chatFragment.onManagerOpearMsg(setAdminUid, setAdminNickname,
				null, null, String.format(mActivity.getResources().getString(R.string
						.unsetadmin_tip), getString(R.string.anchor)));
		chatFragment.sendChatMsg(charSequence);
		//		mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false,
		// mCurrentScreenType);
		mManagerUids.remove(setAdminUid);
		// 如果操作的是当前用户，更改用户类别
		if (setAdminUid.equals(mUid)) {
			mmRoomInfo.put("userType", Constants.USER_TYPE_NORMAL);
		}
	}

	@Override
	public void onTi(String operatorUid, String operatorNickname, String tiUid, String
			tiNickname) {
		CharSequence charSequence = chatFragment.onManagerOpearMsg(tiUid, tiNickname, null, null,
				String.format(mActivity.getResources().getString(R.string.ti_room),
						operatorNickname));
		chatFragment.sendChatMsg(charSequence);
		//		mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false,
		// mCurrentScreenType);
	}

	@Override
	public void onTiModerator(String msg) {
		CharSequence charSequence = chatFragment.onSysMsg(msg);
		chatFragment.sendChatMsg(charSequence);
		//		mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false,
		// mCurrentScreenType);
	}

	@Override
	public void onPublish(JSONObject data) {
		getHotRankInfo();
		// 主播开始推流（断流重连），重新请求红包CD
		getRedPacketCDInfo();
	}

	@Override
	public void onUnPublish() {
		mmRoomInfo.put("isHot", false);
		// 主播下线
		mHotRankLayout.setVisibility(View.GONE);
		mHotRankPopupWindow.dismiss();
		mRedPacketLayout.setVisibility(View.GONE);
		releaseRedPacketTimer();
	}

	/**
	 * 释放红包计时器
	 */

	private void releaseRedPacketTimer() {// 主播停止，隐藏红包入口
		if (mRedPacketTimer != null) {
			mRedPacketTimer.cancel();
			mRedPacketTimer = null;
		}
	}

	/**
	 * 释放暴击倒计时
	 */
	private void releaseBonusTimer() {
		if (mBonusTimer != null) {
			mBonusTimer.cancel();
			mBonusTimer = null;
		}
	}

	@Override
	public void onOpen() {
		EvtLog.e(TAG, "onOpen");
		getRedPacketCDInfo();
	}

	@Override
	public void onClose(int code, String errosMsg) {
		EvtLog.e(TAG, "websocket onClose code:" + code + " errosMsg:" + errosMsg);
		// socket断开，停止计时
		releaseRedPacketTimer();
		mRedPacketLayout.setVisibility(View.GONE);
		// 当websocket关闭时，把重置房间人数
		// if (mmRoomInfo != null)
		//			mAudienceNum = Integer.parseInt((String) mmRoomInfo.get("onlineBaseNum"));
		// 非正常关闭websocket，且有网络
		if (code != WebSocket.ConnectionHandler.CLOSE_NORMAL && !isNoNetworkTipMsg() &&
				!mWebSocketConnectStatus) {
			postDelayed(new Runnable() {
				@Override
				public void run() {
					mWebSocketImpl.start(getMessageUrl());
				}
			}, 2000);
		}
	}
	/* ####################### 消息回调 end ########################### */

	/* ####################### 送花消息回调 ############################ */ WebSocketFlowerCallBack
			flowerCallBack = new WebSocketFlowerCallBack() {

		@Override
		public void onOpen() {
			EvtLog.e(TAG, "flower websocket onOpen");
		}

		@Override
		public void onError(String piErrCode, String errorMsg, String cmd) {
			// 请求过于频繁
			if (NetConstants.SENT_STATUS_NEED_VALIDATE.equals(piErrCode)) {
				GTValidateRequest.getInstance().validate(new WeakReference<Activity>
						(LiveBaseActivity.this));
			}
			if (Constants.ON_CONNECT_STATUS.equals(cmd)) {
				mWebSocketConnectStatusFlower = true;
			}
			showTips(errorMsg);
		}

		@Override
		public void onClose(int code, String errosMsg) {
			EvtLog.e(TAG, "flower websocket onClose code:" + code + " errosMsg:" + errosMsg);
			// 如果是网络原因连接错误，且网络是连接上的
			if (code == WebSocket.ConnectionHandler.CLOSE_CANNOT_CONNECT && !isNoNetworkTipMsg()
					&& !mWebSocketConnectStatusFlower) {
				postDelayed(new Runnable() {
					@Override
					public void run() {
						mFlowerWebSocketImpl.start(getFlowerMessageUrl());
					}
				}, 2000);
			}

		}

		@Override
		public void onSendFlower() {
			EvtLog.e(TAG, "flower websocket onSendFlower");
			mFlowwerNum++;
			Message loMsg = Message.obtain();
			loMsg.what = MSG_SEND_FLOWER_SUCCESS;
			sendMsg(loMsg);
		}

		@Override
		public void onFirstSendFlower(String piFrom, String psFromNickname, String fromLevel,
									  String fromType, String medalsStr, String guardType) {
			// 送花 就不显示在弹幕上了
			CharSequence charSequence = chatFragment.onUserOpearMsg(piFrom, psFromNickname,
					fromType, fromLevel, ChatListAdapter.USER_SEND_FLOWER, medalsStr, null,
					guardType);
			chatFragment.sendChatMsg(charSequence);
			//			mDanmakuViewCommon.addDanmaKuShowTextAndImage(charSequence, false,
			// mCurrentScreenType);
		}

	};
	/* ####################### 送花消息回调 ############################ */

	/************************************ 事件处理器 ***************************************/
	/**
	 * 礼物面板充值按钮
	 */
	private class OnReChange implements OnClickListener {
		@Override
		public void onClick(View v) {
			MobclickAgent.onEvent(FeizaoApp.mConctext, "rechargeInGiftPanel");
			OperationHelper.onEvent(FeizaoApp.mConctext, "rechargeInGiftPanel", null);
			if (!AppConfig.getInstance().isLogged)
				Utils.requestLoginOrRegister(mActivity, "充值需要登录，请登录", Constants.REQUEST_CODE_LOGIN);
			else {
				if (isNoNetworkTipMsg()) return;
				ActivityJumpUtil.toWebViewActivity(mActivity, WebConstants.getFullWebMDomain
						(WebConstants.RECHARGE_WEB_URL), true, RECHANGE_REQUEST_CODE);
			}

		}
	}

	/**
	 * 切换礼物
	 */
	private class OnGiftSwitchListenr implements OnClickListener {
		@Override
		public void onClick(View v) {
			EvtLog.d(TAG, "OnGiftSwitchListenr onClick");
			if (v.getId() == R.id.gift_switch_general && mLiGeneralGift.getVisibility() != View
					.VISIBLE) {
				//显示为热门
				mGiftViewModel = MODEL_HOT;
				mLiGuardGift.setVisibility(View.INVISIBLE);
				mLiGuardGift2.setVisibility(View.INVISIBLE);
				mGuardOpenTipLayout.setVisibility(View.GONE);
				mLiNumGift.setVisibility(View.INVISIBLE);
				mLiGeneralGift.setVisibility(View.VISIBLE);
				mLiBalance.setVisibility(View.VISIBLE);
				mLiveBtnNum.setVisibility(View.VISIBLE);
				mGiftSwitchGeneralView.setVisibility(View.VISIBLE);
				mGiftSwitchGuardView.setVisibility(View.INVISIBLE);
				mGiftSwitchGuardView2.setVisibility(View.INVISIBLE);
				mLiveCurGiftIv.setVisibility(View.VISIBLE);

				mIbtnShopBag.setVisibility(View.GONE);
				mIbtnShopBag.setImageResource(R.drawable.icon_more_shangchen);
				/**
				 * @date 2016.6.8
				 * umeng log:java.lang.NullPointerException
				 * 网络连接差，可能mGiftsData为空数据，故添加判断
				if (mGiftsData != null)
				 */
				clearGiftSelectedData();
				updateGuardUnReadTip();
			} else if (v.getId() == R.id.gift_switch_guard && mLiGuardGift.getVisibility() !=
					View.VISIBLE) {
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickBagInGiftPanel");
				//显示为背包
				mGiftViewModel = MODEL_BAG;
				mLiGeneralGift.setVisibility(View.INVISIBLE);
				mLiNumGift.setVisibility(View.INVISIBLE);
				mLiGuardGift.setVisibility(View.VISIBLE);
				mLiGuardGift2.setVisibility(View.INVISIBLE);
				mGuardOpenTipLayout.setVisibility(View.GONE);
				mGiftSwitchGeneralView.setVisibility(View.INVISIBLE);
				mGiftSwitchGuardView.setVisibility(View.VISIBLE);
				mGiftSwitchGuardView2.setVisibility(View.INVISIBLE);
				//				mLiveBtnNum.setVisibility(View.GONE);
				mLiveCurGiftIv.setVisibility(View.INVISIBLE);
				mIbtnShopBag.setVisibility(View.VISIBLE);
				mIbtnShopBag.setImageResource(R.drawable.icon_more_beibao);
				// reChangeBtn.setVisibility(View.GONE);
				clearGiftSelectedData();
				// 如果背包礼物面板是打开的，设置已读状态
				AppConfig.getInstance().updateNewGuardTips(false);
			} else if (v.getId() == R.id.gift_switch_guard2 && mLiGuardGift2.getVisibility() !=
					View.VISIBLE) {
				//显示为守护
				mGiftViewModel = MODEL_GUARD;
				// 如果未开通守护，提示开通
				if (!Utils.getBooleanFlag(mmRoomInfo.get("isGuard"))) {
					mGuardOpenTipLayout.setVisibility(View.VISIBLE);
				} else {
					mGuardOpenTipLayout.setVisibility(View.GONE);
				}
				mLiGuardGift2.setVisibility(View.VISIBLE);
				mLiGeneralGift.setVisibility(View.INVISIBLE);
				mLiNumGift.setVisibility(View.INVISIBLE);
				mLiGuardGift.setVisibility(View.INVISIBLE);
				mLiBalance.setVisibility(View.VISIBLE);
				mLiveBtnNum.setVisibility(View.VISIBLE);
				mGiftSwitchGeneralView.setVisibility(View.INVISIBLE);
				mGiftSwitchGuardView.setVisibility(View.INVISIBLE);
				mGiftSwitchGuardView2.setVisibility(View.VISIBLE);
				mLiveCurGiftIv.setVisibility(View.VISIBLE);
				mIbtnShopBag.setVisibility(View.GONE);
				clearGiftSelectedData();
				updateGuardUnReadTip();
			}
		}
	}

	public void OnBack() {
		// 按返回键弹出对话框
		UiHelper.showConfirmDialog(mActivity, R.string.live_exit, R.string.determine, R.string
				.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		}, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

	}

	/**
	 * 处理viewPage 点击事件（全屏时，点击恢复）
	 */
	private class OnViewPage implements OnListViewTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// EvtLog.d(TAG, "OnViewPage onTouch");
			if (event.getAction() == MotionEvent.ACTION_UP) {
				hideSoftInput();
				showMenu();
			}
			return false;
		}

	}

	/**
	 * 处理赠送礼物弹出时，点击其他区域事件（全屏时，点击恢复）
	 * 礼物面板、私信面板做相同处理，再次添加判断
	 */
	private class OnPlayChatListenr implements OnClickListener {

		@Override
		public void onClick(View v) {
			onPlayChatListenrOnClick();
		}
	}

	private void onPlayChatListenrOnClick() {
		EvtLog.d(TAG, "OnPlayChatListenr onClick");
		// if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
		mGiftLayout.startAnimation(mDownOutAnimation);
		// } else {
		// mGiftLayout.startAnimation(mRightOutAnimation);
		// }
		mGiftLayout.setVisibility(View.GONE);
		mLiveButtomMenulayout.setVisibility(View.VISIBLE);
		mLiveClose.setVisibility(View.VISIBLE);
		showMenu();

		updateGuardUnReadTip();
	}

	/**
	 * 表情按钮事件
	 */
	private class OnShowHideEmotions implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			if (null == mFaceHelper) {
				mFaceHelper = new SelectFaceHelper(mActivity, moGvEmotions);
				mFaceHelper.setFaceOpreateListener(mOnFaceOprateListener);
			}
			if (moGvEmotions.getVisibility() == View.VISIBLE) {
				// moGvEmotions.setVisibility(View.GONE);
			} else {
				mInputMethodManager.hideSoftInputFromWindow(mActivity.getWindow().peekDecorView()
						.getApplicationWindowToken(), 0);
				moGvEmotions.setVisibility(View.VISIBLE);

			}
		}
	}

	OnFaceOprateListener mOnFaceOprateListener = new OnFaceOprateListener() {
		@Override
		public void onFaceSelected(SpannableString spanEmojiStr) {
			if (null != spanEmojiStr) {
				moEtContent.getText().insert(moEtContent.getSelectionStart(), spanEmojiStr);
			}
		}

		@Override
		public void onFaceDeleted() {
			int selection = moEtContent.getSelectionStart();
			String text = moEtContent.getText().toString();
			if (selection > 0) {
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2)) {
					int start = text.lastIndexOf("[");
					int end = selection;
					moEtContent.getText().delete(start, end);
					return;
				}
				moEtContent.getText().delete(selection - 1, selection);
			}

		}

	};

	/**
	 * 清除输入框数据
	 */
	private class OnClearInputText implements OnClickListener {
		@Override
		public void onClick(View v) {
			sendMsg.setToUid("0");
			moIvClearInput.setVisibility(View.GONE);
			if (mLiveDanmuBtn.isChecked()) {
				moEtContent.setHint(R.string.live_input_danmu_tip);
			} else {
				moEtContent.setHint(R.string.click_to_chat_with_player);
			}

		}
	}

	private class OnInputText implements OnClickListener {

		/**
		 * TODO 简单描述该方法的实现功能（可选）.
		 *
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			postDelayed(new Runnable() {

				@Override
				public void run() {
					moGvEmotions.setVisibility(View.GONE);
					moEtContent.requestFocus();
					mInputMethodManager.showSoftInput(moEtContent, 0);
				}
			}, 100);
		}
	}

	/**
	 * 监听键盘发送按钮
	 */
	private class OnEditorActiion implements TextView.OnEditorActionListener {
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			EvtLog.e(TAG, "actionId:" + actionId);
			if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() ==
					KeyEvent.KEYCODE_ENTER)) {
				sendMsg.onClick(v);
				return true;
			}
			return false;
		}
	}

	/**
	 * 弹幕点击事件
	 *
	 * @author Administrator
	 * @version LiveBaseActivity
	 * @since JDK 1.6
	 */
	private class OnDanmuClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			try {
				JSONObject jsonObject = (JSONObject) v.getTag();
				clickDanmuGroup(jsonObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 直播间切换
	 *
	 * @param rid
	 */
	protected void jumpLive(String rid) {
		Map<String, String> lmItem = new HashMap<String, String>();
		lmItem.put("rid", rid);
		ActivityJumpUtil.toLiveMediaPlayerActivity(mActivity, lmItem);
	}

	/**
	 * 打开链接-->活动
	 *
	 * @param url
	 */
	protected void jumpUrl(String url) {
		Map<String, String> lmPageInfo = new HashMap<String, String>();
		lmPageInfo.put(WebViewActivity.URL, url);
		ActivityJumpUtil.gotoActivity(mActivity, WebViewActivity.class, false, WebViewActivity
				.WEB_INFO, (Serializable) lmPageInfo);
	}

	private class OnSendMsg implements OnClickListener {

		private static final String PUBLIC_UID = "0";
		private String toUid = PUBLIC_UID;

		private void setToUid(String uid) {
			this.toUid = uid;
		}

		private String getToUid() {
			return toUid;
		}

		@Override

		public void onClick(View v) {
			if (!AppConfig.getInstance().isLogged)
				Utils.requestLoginOrRegister(mActivity, "在公聊大厅发言需要登录，请登录", Constants
						.REQUEST_CODE_LOGIN);
			else {
				if (isNoNetworkTipMsg()) return;
				OperationHelper.onEvent(FeizaoApp.mConctext,
						"clickSendButtonOfFreeMessageInChatPanelOfLivingRoom", null);
				if (mLiveDanmuBtn.isChecked()) {
					mWebSocketImpl.sendBarrage(toUid, moEtContent.getText().toString());
					OperationHelper.onEvent(FeizaoApp.mConctext,
							"clickSendButtonOfPayMessageInChatPanelOfLivingRoom", null);
				} else {
					mWebSocketImpl.sendMsg(toUid, moEtContent.getText().toString(), false);
				}
				moEtContent.setText("");
			}
		}
	}

	private class OnCheckChangeListener implements CompoundButton.OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) {
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickOpenBarrage");
			} else {
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickCloseBarrage");
			}
			// 如果是公屏讲话，是否弹幕分别提示
			if (OnSendMsg.PUBLIC_UID.equals(sendMsg.getToUid())) {
				if (isChecked) {
					moEtContent.setHint(R.string.live_input_danmu_tip);
				} else {
					moEtContent.setHint(R.string.click_to_chat_with_player);
				}
			}

		}
	}

	/**
	 * 通过此方法监听键盘的弹出/隐藏
	 */
	private void keyBoardChangedListener() {
		final View decordView = this.getWindow().getDecorView();
		decordView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
				.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				Rect rect = new Rect();
				decordView.getWindowVisibleDisplayFrame(rect);
				int disHeight = rect.bottom - rect.top;
				int decordViewHeight = decordView.getRootView().getHeight();
				// 比较Activity根布局与当前布局的大小
				int liRootHeight = decordViewHeight - disHeight;
				EvtLog.d(TAG, "ReLayout RootView.getRootView height:" + liRootHeight);
				// 如果高度差超过100像素，就很有可能是有软键盘...
				if (liRootHeight > 100) {
					// 是否已经弹出虚拟键盘
					if (!isKeyboardUp) {
						isKeyboardUp = true;
						//						FrameLayout.LayoutParams layoutParams =
						// (FrameLayout.LayoutParams) mLiveMainLayout.getLayoutParams();
						//						layoutParams.height = disHeight;
						//						mLiveMainLayout.scrollBy(0, liRootHeight);
						keyboardUporDow(isKeyboardUp);
					}

				} else {
					// 是否已经隐藏虚拟键盘(表情也隐藏才认为是隐藏虚拟键盘）
					if (isKeyboardUp && moGvEmotions.getVisibility() == View.GONE) {
						isKeyboardUp = false;
						//						FrameLayout.LayoutParams layoutParams =
						// (FrameLayout.LayoutParams) mLiveMainLayout.getLayoutParams();
						//						layoutParams.height = decordViewHeight;
						//						mLiveMainLayout.setLayoutParams(layoutParams);
						//						mLiveMainLayout.scrollTo(0, 0);
						keyboardUporDow(isKeyboardUp);
					}
				}
			}
		});
	}

	/**
	 * 清空礼物选择数据
	 */
	private void clearGiftSelectedData() {
		moBtnSendGifts.setEnabled(false);
		if (mGiftSelectedView != null) updateViewStatus(mGiftSelectedView, false);
		giftSelectedData = null;
		mLiveCurGiftIv.setImageDrawable(null);
	}

	/**
	 * 键盘弹出或隐藏的操作
	 */
	@SuppressLint("NewApi")
	private void keyboardUporDow(boolean iskeyboardUp) {
		EvtLog.e(TAG, "ReLayout keyboardUporDow value:" + iskeyboardUp);
		// 弹出
		if (iskeyboardUp) {
			if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				// RelativeLayout.LayoutParams lp1 = (LayoutParams)
				// mLiveChatLayout.getLayoutParams();
				// lp1.height = LayoutParams.MATCH_PARENT;
				// mLiveChatLayout.setLayoutParams(lp1);
				// 设置不能滚动
				mSwipeBackLayout.setScrollEnable(false);
			}
			mLiveInputLayout.setVisibility(View.VISIBLE);
			mLiveButtomMenulayout.setVisibility(View.INVISIBLE);
			mLiveClose.setVisibility(View.GONE);
		}
		// 隐藏
		else {
			if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				// RelativeLayout.LayoutParams lp1 = (LayoutParams)
				// mLiveChatLayout.getLayoutParams();
				// lp1.height = (int) (150 * FeizaoApp.metrics.density);
				// mLiveChatLayout.setLayoutParams(lp1);
				// 恢复能滚动
				mSwipeBackLayout.setScrollEnable(true);
			}

			mLiveInputLayout.setVisibility(View.INVISIBLE);
			mLiveButtomMenulayout.setVisibility(View.VISIBLE);
			mLiveClose.setVisibility(View.VISIBLE);
		}

	}

	// 赠送礼物按钮
	private class OnGiftsBottomSendClick implements OnClickListener {

		@Override
		public void onClick(View v) {

			sendGift();
		}
	}

	/**
	 * 送礼按钮事件
	 */
	public class OnSendGiftsClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			MobclickAgent.onEvent(FeizaoApp.mConctext, "ClickPresentButton");
			// 现在是直接加载视频直播，有可能房间信息没有获取到，如果没有加载到房间信息则点击更多无效
			if (mmRoomInfo == null) {
				return;
			}
			if (mGiftLayout.getVisibility() == View.VISIBLE && moGvEmotions.getVisibility() ==
					View.VISIBLE) {
				mGiftLayout.setVisibility(View.GONE);
			} else {
				mInputMethodManager.hideSoftInputFromWindow(mActivity.getWindow().peekDecorView()
						.getApplicationWindowToken(), 0);
				// 当赠送礼物页面显示时，整个布局让他全屏
				//				if (mCurrentScreenType == ActivityInfo
				// .SCREEN_ORIENTATION_PORTRAIT) {
				// giftView1.setVisibility(View.GONE);
				// giftView2.setVisibility(View.GONE);
				mGiftLayout.startAnimation(mDownInAnimation);
				// } else {
				// giftView1.setVisibility(View.VISIBLE);
				// giftView2.setVisibility(View.VISIBLE);
				// mGiftLayout.startAnimation(mRightInAnimation);
				// }
				mGiftLayout.setVisibility(View.VISIBLE);

				mLiveButtomMenulayout.setVisibility(View.INVISIBLE);
				mLiveClose.setVisibility(View.GONE);
				// 每次弹出礼物面板，更新余额
				mBalance = UserInfoConfig.getInstance().coin;
				mBalanceTv.setText(mBalance);
			}
			mHandler.removeCallbacks(mShowMenu);
		}
	}

	//打开商城或者背包
	private class onGoShoppingOrBag implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (mGiftViewModel == MODEL_HOT) {
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickStoreInGiftBoardOfFeature",
						null);
				ActivityJumpUtil.toWebViewActivity(mActivity, WebConstants.getFullWebMDomain
						(WebConstants.WEB_STORE), true, REQUEST_CODE_GOTO_SHOP);
			} else if (mGiftViewModel == MODEL_BAG) {
				OperationHelper.onEvent(FeizaoApp.mConctext,
						"clickMyBackpackInGiftBoardOfBackpack", null);
				ActivityJumpUtil.toWebViewActivity(mActivity, WebConstants.getFullWebMDomain
						(WebConstants.WEB_MY_BACKPACK), true, REQUEST_CODE_GOTO_SHOP);
			}
		}
	}

	// 赠送礼物输入框事件
	private class OnEditGiftClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			EvtLog.d(TAG, "OnEditGiftClick");
			// 选中的礼物是否为null
			if (giftSelectedData == null) {
				UiHelper.showToast(mActivity, "请选择要赠送的礼物");
				return;
			}
			showHideNumGiftLayout();
		}
	}

	private void updateGifGiftInfo(Map<String, String> lmGiftInfo) {
		ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity.getApplicationContext(),
				mGiftGiftUserPhoto, lmGiftInfo.get("fromHeadPic"));
		mGiftGiftUserPhoto.setTag(R.id.tag_first, lmGiftInfo.get("from_user"));
		mGiftGiftUserPhoto.setTag(R.id.tag_second, lmGiftInfo.get("piFrom"));

		mGiftGiftUserName.setText(lmGiftInfo.get("from_user"));
		mGiftGiftTip.setText(mActivity.getResources().getString(R.string.live_gif_gift_name_tip,
				lmGiftInfo.get("giftName"), lmGiftInfo.get("count")));
	}

	/**
	 * 本人消费的，更新余额
	 * type    0 普通礼物，1 免费礼物
	 *
	 * @param consume 消费的金额 (中奖金额应该为负数）
	 */
	protected void updateBalance(int consume) {
		mBalance = String.valueOf(Long.parseLong(mBalance) - consume);
		UserInfoConfig.getInstance().updateCoin(mBalance);
		// 每次弹出礼物面板，更新余额
		mBalanceTv.setText(mBalance);
	}

	/**
	 * 获取特效字段
	 */
	//	private Map<String, String> getImgEffect(String giftId, String giftType, int count) {
	// Map<String, String> map = new HashMap<String, String>();
	// if (Constants.COMMON_GIFT_TYPE_NORMAL.equals(giftType)) {
	// if (mGiftsData != null) {
	// for (int i = 0; i < mGiftsData.size(); i++) {
	// if (mGiftsData.get(i).get("id").equals(giftId)) {
	// // long consume =
	// // Long.parseLong(mGiftsData.get(i).get("price")) *
	// // count;
	// // map.put("giftImg", mGiftsData.get(i).get("img"));
	// // map.put("giftName", mGiftsData.get(i).get("name"));
	// // map.put("giftConsume", String.valueOf(consume));
	// // map.put("giftType",
	// // Constants.COMMON_GIFT_TYPE_NORMAL);
	// map.put("imgEffect", mGiftsData.get(i).get("imgEffect"));
	// return map;
	// }
	// }
	// }
	// if (mGuardGiftsData2 != null) {
	// for (int i = 0; i < mGuardGiftsData2.size(); i++) {
	// if (mGuardGiftsData2.get(i).get("id").equals(giftId)) {
	// // long consume =
	// // Long.parseLong(mGiftsData.get(i).get("price")) *
	// // count;
	// // map.put("giftImg", mGiftsData.get(i).get("img"));
	// // map.put("giftName", mGiftsData.get(i).get("name"));
	// // map.put("giftConsume", String.valueOf(consume));
	// // map.put("giftType",
	// // Constants.COMMON_GIFT_TYPE_NORMAL);
	// map.put("imgEffect", mGuardGiftsData2.get(i).get("imgEffect"));
	// return map;
	// }
	// }
	// }
	// }
	// return map;
	// }

	/**
	 * 弹出礼物自定义输入对话框
	 */
	private void showGiftNumKeyBoardDialog() {
		giftNumDialog = new GiftNumKeybordDialog(mActivity, new GiftNumKeybordDialog
				.OnItemClickListener() {
			@Override
			public void onClick(String num) {
				mCurGiftNum = Integer.parseInt(num);
				boolean existFlag = false;
				for (Map<String, String> iterator : GiftsNumAdapter.mList) {
					if (num.equals(iterator.get("giftNum"))) {
						mLiveBtnNum.setText(iterator.get("giftNum"));
						//						mLiveBtnNum.setText(String.format(getString(R
						// .string.live_gift_cur_num),
						//								iterator.get("giftNum"), iterator.get
						// ("giftNumName")));
						existFlag = true;
						break;
					}
				}
				if (!existFlag) mLiveBtnNum.setText(num);
			}
		});
		giftNumDialog.builder().setCancelable(false).setCanceledOnTouchOutside(true);
		giftNumDialog.setTitle(giftSelectedData.get("name"));
		giftNumDialog.setGiftNum(String.valueOf(mCurGiftNum));
		// giftNumDialog.setGiftNum(giftsNum.getText().toString());
		giftNumDialog.show();
	}


	/**
	 * 送礼+
	 */
	private void sendGift() throws NumberFormatException {
		if (mmRoomInfo == null) return;
		if (!AppConfig.getInstance().isLogged) {
			Utils.requestLoginOrRegister(mActivity, "赠送礼物需要登录，请登录", Constants.REQUEST_CODE_LOGIN);
			return;
		}
		// 选中的礼物是否为null
		if (giftSelectedData == null) {
			UiHelper.showShortToast(mActivity, "请选择要赠送的礼物");
			return;
		}
		// 如果是背包物品，否则就是礼物
		if (!TextUtils.isEmpty(giftSelectedData.get("pkgItemsetId"))) {
			MobclickAgent.onEvent(FeizaoApp.mConctext, "clickSendInBagPanel");
			int liGiftId = Integer.parseInt(giftSelectedData.get("pid"));
			// 每次固定送1个
			//			mWebSocketImpl.sendGift(liGiftId, 1, giftSelectedData.get("pkgItemsetId"));
			if (mCurGiftNum > Integer.parseInt(giftSelectedData.get("num"))) {
				UiHelper.showShortToast(mActivity, mActivity.getString(R.string
						.live_package_gift_max_c));
				return;
			}
			mWebSocketImpl.sendGift(liGiftId, mCurGiftNum, giftSelectedData.get("pkgItemsetId"));
		} else {
			int liGiftId = Integer.parseInt(giftSelectedData.get("id"));
			mWebSocketImpl.sendGift(liGiftId, mCurGiftNum);
		}

	}

	/**
	 * 点击房间主播头像信息，弹出对话框
	 *
	 * @param userName        点击的用户名称
	 * @param type            点击的用户类别
	 * @param uid             点击的用户id
	 * @param anchorHeadPhoto 主播头像控件,用户更新头像
	 */
	private void showPersonInfoDialog(final String userName, final String type, final String uid,
									  ImageView anchorHeadPhoto) {
		showPersonInfoDialog(userName, type, uid);
		mPersonInfoDialogBuidler.setAnchorHeadPhotoView(anchorHeadPhoto);
	}

	/**
	 * (主播侧)点击个人信息，弹出对话框
	 *
	 * @param userName 点击的用户名称
	 * @param type     点击的用户类别
	 * @param uid      点击的用户id
	 */
	private void showPersonInfoDialog(final String userName, final String type, final String uid) {
		final boolean isOwer = mUid.equals(uid);
		// 横屏模式下,不显示用户信息的“个性签名”
		boolean isShowIntrol = mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		mPersonInfoDialogBuidler = new PersonInfoCustomDialogBuilder(mActivity, userName, type,
				uid, mmAnchorInfo.get("id"), isShowIntrol);
		mPersonInfoDialogBuidler.setControlType(getPersionDialogControlType());
		mPersonInfoDialogBuidler.setConnectFlag(getConnectFlag());
		mPersonInfoDialogBuidler.setFlowerNum(String.valueOf(mFlowwerNum));
		mPersonInfoDialogBuidler.setIsOwen(isOwer);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.item_user_manage:
						boolean isBan = Boolean.parseBoolean((String) v.getTag());
						showUserManageDialog(uid, isBan);
						//点击管理
						break;
					//					case R.id.item_ti:
					//						mWebSocketImpl.sendTi(uid);
					//						break;
					case R.id.item_speak:
						showSoftInput(userName, uid);
						break;
					// 如果是主播测，则是设为管理功能
					case R.id.item_person_info:
						//						if (((Button) v).getText().equals(getString(R
						// .string.live_person_info_tip))) {
						//							Map<String, String> personInfo = new
						// HashMap<String, String>();
						// personInfo.put("id", uid);
						//							ActivityJumpUtil.toPersonInfoActivity
						// (mActivity, personInfo, REQUEST_CODE_PERSONINFO);
						// return;
						// }
						clickPersonInfoView(type, uid);
						break;
					case R.id.item_focus:
						MobclickAgent.onEvent(FeizaoApp.mConctext,
								"followBroadcasterInPersonalCard");
						OperationHelper.onEvent(FeizaoApp.mConctext,
								"followBroadcasterInPersonalCard", null);
						personInfoFocusOperate(Utils.strBool((String) v.getTag()));
						break;
					case R.id.item_line_user://主播连线
						OperationHelper.onEvent(FeizaoApp.mConctext,
								"clickLinkButtonInPersonalCard");
						requestVideoChat(uid);
						break;
					case R.id.item_user_report:
						ActivityJumpUtil.toReportActivity(mActivity, Constants
								.COMMON_REPORT_TYPE_ROOM, mmIntentRoomInfo.get("rid"), 0);
						break;
					default:
						break;
				}
			}

		};
		mPersonInfoDialogBuidler.setOnEventClickListener(onClickListener);
		mPersonInfoDialogBuidler.setOnFocusSuccessListener(onClickListener);
		mPersonInfoDialogBuidler.showDialog();
	}

	/**
	 * 显示管理弹窗
	 */
	private void showUserManageDialog(final String uid, final boolean isBan) {
		if (mUserManageDialog == null) {
			mUserManageDialog = new LiveUserManageDialog(this);
		}
		mUserManageDialog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.dialog_tv_ti:
						// 按返回键弹出对话框
						confirmTi(uid);
						break;
					case R.id.dialog_tv_banned:
						confirmBan(uid, isBan);
						break;
				}
			}
		});
		mUserManageDialog.setHasBanned(isBan);
		mUserManageDialog.setControlType(getPersionDialogControlType());
		mUserManageDialog.showAtLocation(getWindow().getDecorView().findViewById(android.R.id
				.content), Gravity.BOTTOM, 0, 0);
	}

	/**
	 * 确认是否踢出
	 *
	 * @param uid
	 */
	private void confirmTi(final String uid) {
		// 按返回键弹出对话框
		UiHelper.showConfirmDialog(mActivity, "  踢出后，两个小时内TA不能进入直播间  ", R.string.cancel, R.string
				.determine, null, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mWebSocketImpl.sendTi(uid);
			}
		});
	}

	/**
	 * 显示禁言/解禁提示
	 */
	private void showBanHintDialog(boolean isBanned) {
		String content;
		if (isBanned)
			content = "你已被管理员禁言，" + AppConfig.getInstance().banTime / 3600 + "小时内无法发送公聊和弹幕";
		else content = "你已被管理员取消禁言，可以发送公聊和弹幕啦";
		UiHelper.showSingleConfirmDialog(mActivity, content, null);
	}

	/**
	 * 确认是否禁言
	 *
	 * @param uid
	 */
	private void confirmBan(final String uid, final boolean isBanned) {
		// 按返回键弹出对话框
		String content;
		if (isBanned) content = "是否取消禁言该用户";
		else content = "确认禁言TA？禁言" + AppConfig.getInstance().banTime / 3600 + "小时内TA不能发送公聊和弹幕";
		UiHelper.showConfirmDialog(mActivity, content, R.string.cancel, R.string.determine, null,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!isBanned) mWebSocketImpl.sendBan(uid);
						else mWebSocketImpl.sendUnBan(uid);
					}
				});
	}

	/**
	 * 点击红包弹出领取红包对话框
	 */
	private void showRedPacketDialog(String money, boolean lastFlag) {
		mRedPacketBuilder = new RedPacketDialogBuilder(mActivity, money, lastFlag);
		OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					// 关闭对话框
					case R.id.dialog_red_packet_close:
						mRedPacketBuilder.dismiss();
						break;
					// 红包规则
					case R.id.dialog_red_packet_help:
						ActivityJumpUtil.toWebViewActivity(mActivity, WebConstants
								.getFullWebMDomain(WebConstants.REDPACKET_HELP_WEB_URL), true);
						break;
					// 领取红包
					case R.id.dialog_red_packet_get_layout:
						// 邀请好友
					case R.id.dialog_red_packet_empty_invite:
						OperationHelper.onEvent(FeizaoApp.mConctext,
								"clickGetButtonInRedPacketBox", null);
						ActivityJumpUtil.toWebViewActivity(mActivity, WebConstants
								.getFullWebMDomain(WebConstants.REDPACKET_INVITE_WEB_URL) +
								UserInfoConfig.getInstance().id, true);
						break;
					default:
						break;
				}
			}

		};
		mRedPacketBuilder.setOnEventClickListener(onClickListener);
		mRedPacketBuilder.showDialog();
	}

	/**
	 * 弹出个人信息对话框控制类别,（子类具体实现）
	 */
	protected String getPersionDialogControlType() {
		return null;
	}

	/**
	 * 设置是否显示连线
	 *
	 * @return
	 */
	protected boolean getConnectFlag() {
		return false;
	}

	/**
	 * clickPersonInfoView:点击‘他的主页或者设置管理、取消管理按钮（子类具体实现）.
	 *
	 * @param type 点击的用户类别
	 * @param uid  点击的用户uid
	 */
	protected void clickPersonInfoView(String type, String uid) {

	}

	/**
	 * 主播请求连麦
	 *
	 * @param uid
	 */
	protected void requestVideoChat(String uid) {
	}

	/**
	 * 点击个人信息卡 关注按钮。注：只有点击主播信息卡上的关注按钮才会回调到此方法
	 */
	protected void personInfoFocusOperate(boolean flag) {
		// 主播侧就是默认的操作方式
		// 如果是关注
		if (flag) {
			mWebSocketImpl.sendCommand(WebSocketLiveEngine.USER_ATTENTION);
		}
	}

	/**
	 * 点击弹幕背景
	 * 如果是进入个人信息卡，主播测与用户侧都可以进入
	 */
	protected void clickDanmuGroup(JSONObject jsonObject) {
		MobclickAgent.onEvent(FeizaoApp.mConctext, "clickHeadInBarrage");
		if (jsonObject == null) return;
		//统计事件
		if ("3".equals(jsonObject.optString("btype"))) {
			MobclickAgent.onEvent(FeizaoApp.mConctext, "clickBroadcastOfAllIn");
			OperationHelper.onEvent(FeizaoApp.mConctext, "clickBroadcastOfAllIn");
		} else if ("4".equals(jsonObject.optString("btype"))) {
			MobclickAgent.onEvent(FeizaoApp.mConctext, "clickBroadcastOfFeatureOne");
			OperationHelper.onEvent(FeizaoApp.mConctext, "clickBroadcastOfFeatureOne");
		} else if ("5".equals(jsonObject.optString("btype"))) {
			MobclickAgent.onEvent(FeizaoApp.mConctext, "clickBroadcastOfDaBaoJian");
			OperationHelper.onEvent(FeizaoApp.mConctext, "clickBroadcastOfDaBaoJian");
		}
		//响应点击事件
		if (Constants.SYSTEM_MSG_TYPE_USER.equals(jsonObject.optString("jumpKey"))) {// 如果是付费弹幕
			mClickUsernameListener.onClick(jsonObject.optString("nickname"), jsonObject.optString
					("uid"));
		}
	}

	/**
	 * 点击对TA说，显示输入框
	 */
	private void showSoftInput(final String nickname, String toUid) {
		mLiveButtomMenulayout.setVisibility(View.INVISIBLE);
		mLiveClose.setVisibility(View.GONE);
		moIvClearInput.setVisibility(View.VISIBLE);
		sendMsg.setToUid(toUid);
		postDelayed(new Runnable() {

			@Override
			public void run() {
				moEtContent.setHint("对 " + nickname + " 说：");
				moEtContent.setText("");
				moEtContent.setFocusable(true);
				moEtContent.setFocusableInTouchMode(true);
				moEtContent.requestFocus();
				mInputMethodManager.showSoftInput(moEtContent, 0);
			}
		}, 100);
	}

	/**
	 * 设置礼物数据
	 */
	public void setGiftEffectData(Map<String, String> lmGiftInfo, GiftEffectViewData data) {
		data.isVisible = true;
		data.mGiftCount = lmGiftInfo.get("count");
		data.mGiftGroupNum = Integer.parseInt(lmGiftInfo.get("comboNum"));
		data.mGiftNum = Integer.parseInt(lmGiftInfo.get("comboGiftNum"));
		data.mGiftId = lmGiftInfo.get("id");
		data.mGiftName = lmGiftInfo.get("giftName");
		data.mGiftPhoto = lmGiftInfo.get("giftImg");
		data.mUserId = lmGiftInfo.get("piFrom");
		data.mUserName = lmGiftInfo.get("from_user");
		data.mUserPhoto = lmGiftInfo.get("fromHeadPic");
		data.mGiftPrice = Integer.parseInt(lmGiftInfo.get("giftConsume"));
		data.bonusButtonEnabled = lmGiftInfo.get("bonusButtonEnabled");
		data.mVisibleTime = GIFT_SHOW_TIME;
	}

	/**************************** 私有方法 *******************************/

	/**
	 * 显示礼物数量
	 */
	private void showHideNumGiftLayout() {
		addNumPopWindow = new LiveGiftNumPopWindow(mActivity, this);
		addNumPopWindow.showPopUp(mLiveBtnNum);
		// if (mLiNumGift.getVisibility() == View.INVISIBLE) {
		// mLiGuardGift2.setVisibility(View.INVISIBLE);
		// mLiGeneralGift.setVisibility(View.INVISIBLE);
		// mLiNumGift.setVisibility(View.VISIBLE);
		// } else {
		// if (mGiftSwitchGeneralView.getVisibility() == View.VISIBLE)
		// mLiGeneralGift.setVisibility(View.VISIBLE);
		// else
		// mLiGuardGift2.setVisibility(View.VISIBLE);
		// mLiNumGift.setVisibility(View.INVISIBLE);
		// }
	}

	/**
	 * 送免费礼物，"花" ,参数暂时保留，现在不用
	 */
	private void sendFlower() {
		if (!AppConfig.getInstance().isLogged) {
			Utils.requestLoginOrRegister(mActivity, "在公聊大厅发言需要登录，请登录", Constants
					.REQUEST_CODE_LOGIN);
			return;
		}
		mFavorLayout.addFavor();
		mFlowerWebSocketImpl.sendFlower();
	}

	// private void startFlowerTimer(final int piSecs) {
	// new Thread() {
	// private int miTotalSecs = 0;
	// private int miCurSecs = 0;
	//
	// @Override
	// public void run() {
	// miTotalSecs = piSecs;
	// while (mbActivityRunning && miCurSecs <= miTotalSecs) {
	// try {
	// Thread.sleep(100);
	// } catch (InterruptedException e) {}
	// Message loMsg = new Message();
	// loMsg.what = MsgTypes.FLOWER_LOOP;
	// loMsg.obj = miCurSecs;
	// sendMsg(loMsg);
	// miCurSecs++;
	// }
	// mbFlowerLoaded = true;
	// }
	// }.start();
	// }

	private void setFlowerProgress(float pfPercent) {
		Bitmap loBmp = Bitmap.createBitmap(Utils.dip2px(mActivity, 38.66f), Utils.dip2px
				(mActivity, 38.66f), Config.ARGB_8888);
		Canvas loCvs = new Canvas(loBmp);
		Paint loPt = new Paint();
		loCvs.drawColor(Color.TRANSPARENT);
		int liCenterX = loBmp.getWidth() / 2;
		int liCenterY = loBmp.getHeight() / 2;
		int liRadius = loBmp.getWidth() / 2 - 3;

		// 画外层的圆
		loPt.setColor(mActivity.getResources().getColor(R.color.light_blue));
		loPt.setStyle(Paint.Style.STROKE);
		loPt.setStrokeWidth(6);
		loPt.setAntiAlias(true);
		loCvs.drawCircle(liCenterX, liCenterY, liRadius, loPt);

		// 画进度
		loPt.setColor(mActivity.getResources().getColor(R.color.a_bg_color_ffa200));
		RectF loRf = new RectF(0 + 3, 0 + 3, loBmp.getWidth() - 3, loBmp.getHeight() - 3);
		loCvs.drawArc(loRf, 45, 360 * pfPercent / 100, false, loPt);
		mLiveFlowerIv.setImageBitmap(loBmp);
	}

	/**
	 * toShareLiveInfo:用户直播信息.
	 */
	protected void toShareLiveInfo() {
		MobclickAgent.onEvent(FeizaoApp.mConctext, "shareLiveRoom");
		OperationHelper.onEvent(FeizaoApp.mConctext, "shareLiveRoom", null);
		if (!AppConfig.getInstance().isLogged) {
			Utils.requestLoginOrRegister(mActivity, "分享需要先登录", Constants.REQUEST_CODE_LOGIN);
			return;
		}
		if (isNoNetworkTipMsg()) return;
		Map<String, String> shareInfo = new HashMap<>();
		shareInfo.put(ShareDialogActivity.Share_Content, shareContent);
		shareInfo.put(ShareDialogActivity.Share_Img, shareUrImg);
		shareInfo.put(ShareDialogActivity.Share_Title, shareTitle);
		shareInfo.put(ShareDialogActivity.Share_Url, shareUrl);
		shareInfo.put(ShareDialogActivity.Share_Dialog, String.valueOf(true));
		ActivityJumpUtil.toShareActivity(mActivity, shareInfo);
	}

	/**
	 * 操作饭圈
	 */
	private void operateFan() {
		if (!AppConfig.getInstance().isLogged) {
			Utils.requestLoginOrRegister(mActivity, mActivity.getResources().getString(R.string
					.tip_login_title), Constants.REQUEST_CODE_LOGIN);
			return;
		}
		if (isNoNetworkTipMsg()) return;

		if (Constants.COMMON_TRUE.equals(mFanInfo.get("joined"))) {
			if (Constants.FAN_STATUS_FREEZE.equals(mFanInfo.get("status"))) {
				int need = Integer.parseInt(mFanInfo.get("activeNeed"));
				int total = Integer.parseInt(mFanInfo.get("memberTotal"));
				int tt = need - total;
				// 弹出对话框
				UiHelper.showConfirmDialog(mActivity, String.format(mActivity.getResources()
						.getString(R.string.live_fan_freeze_tip), String.valueOf(tt)), R.string
						.live_fan_invide, R.string.logout_cancel, new DialogInterface
						.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						MobclickAgent.onEvent(FeizaoApp.mConctext, "inviteFriendInLiveRoom");
						Map<String, String> shareInfo = new HashMap<String, String>();
						shareInfo.put(ShareDialogActivity.Share_Title, String.format(Constants
								.SHARE_FAN_TITLE, mFanInfo.get("name"), mFanInfo.get
								("memberTotal")));
						shareInfo.put(ShareDialogActivity.Share_Content, String.format(Constants
								.SHARE_FAN_CONTENT, mFanInfo.get("detail")));
						shareInfo.put(ShareDialogActivity.Share_Img, mFanInfo.get("logo"));
						shareInfo.put(ShareDialogActivity.Share_Url, String.format(WebConstants
								.getFullWebMDomain(WebConstants.SHARE_FAN_URL), (String) mFanInfo
								.get("id")));
						shareInfo.put(ShareDialogActivity.Share_Dialog, String.valueOf(true));

						ActivityJumpUtil.toShareActivity(mActivity, shareInfo);

					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
			} else {
				ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class, false,
						FanDetailActivity.FAN_INFO, (Serializable) mFanInfo);
			}
		} else if (!TextUtils.isEmpty(mFanInfo.get("id"))) {
			addFan(mFanInfo.get("id"));
		} else {
			MobclickAgent.onEvent(FeizaoApp.mConctext, "createFanInLiveRoom");
			mProgress = Utils.showProgress(mActivity);
			BusinessUtils.getMyUserInfo(mActivity, new MyUserInfoCallbackDataHandle(mHandler));
		}
	}

	/**
	 * 加入饭圈
	 *
	 * @param groupId
	 * @since JDK 1.6
	 */
	private void addFan(String groupId) {
		MobclickAgent.onEvent(FeizaoApp.mConctext, "joinFanInLiveRoom");
		if (!AppConfig.getInstance().isLogged) {
			Utils.requestLoginOrRegister(mActivity, mActivity.getResources().getString(R.string
					.tip_login_title), Constants.REQUEST_CODE_LOGIN);
			return;
		}
		BusinessUtils.addFan(mActivity, groupId, new AddFanCallbackData());
	}

	/**
	 * 礼物面板点击事件，当点击礼物时，选中
	 */
	@Override
	public void onClick(ViewGroup parent, View view, int position, Map<String, String> giftInfo) {
		EvtLog.d(TAG, "GridView setOnClickListener ");
		// 如果是“背包”空格子
		if (giftInfo == null || TextUtils.isEmpty(giftInfo.get("name"))) return;
		moBtnSendGifts.setEnabled(true);
		BonusToast.cancelToast();
		for (int i = 0; i < parent.getChildCount(); i++) {
			View v = parent.getChildAt(i);
			if (position == i) {
				// 当前选中的Item改变背景颜色
				updateViewStatus(v, true);
				mGiftSelectedView = v;
				giftSelectedData = giftInfo;
				ImageLoaderUtil.with().loadImage(mActivity, mLiveCurGiftIv, giftSelectedData.get
						("imgPreview"));
				//如果有礼物描述信息，则显示描述信息
				if (!TextUtils.isEmpty(giftInfo.get("description"))) {
					BonusToast.showToast(HtmlUtil.htmlTextDeal(FeizaoApp.mConctext, giftInfo.get
							("description"), null, null));
				}
			} else {
				// 去掉选中的Item改变背景颜色
				updateViewStatus(v, false);
			}
		}
	}

	/**
	 * 更新view checked或者Activiate状态
	 *
	 * @param view
	 * @param status
	 */
	private void updateViewStatus(View view, boolean status) {
		final boolean useActivated = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES
				.HONEYCOMB;
		if (view instanceof Checkable) {
			((Checkable) view).setChecked(status);
		} else if (useActivated) {
			view.setActivated(status);
		}
	}

	/**
	 * 礼物数据回调的接口
	 */
	@Override
	public void onGiftNum(Map<String, String> giftNumInfo) {
		if (GiftsNumAdapter.GIFT_NUM_OTHER.equals(giftNumInfo.get("giftNumName"))) {
			showGiftNumKeyBoardDialog();
		} else {
			mCurGiftNum = Integer.valueOf(giftNumInfo.get("giftNum"));
			mLiveBtnNum.setText(giftNumInfo.get("giftNum"));
		}
	}

	/**
	 * 点击聊天用户名称
	 */
	class ClickUsernameListener implements LiveChatFragment.IClickUserName {

		/**
		 * @param username
		 * @param uid
		 */
		@Override
		public void onClick(String username, String uid) {
			// 此處的type是否websocket回調過來了，再次不適用該type，直接通過本地getInfo接口進行類型匹配
			try {
				String tempType = Constants.USER_TYPE_NORMAL;
				Map<String, String> moderator = JSONParser.parseOne((String) mmRoomInfo.get
						("moderator"));
				// 如果是主播
				if (uid.equals(moderator.get("id"))) {
					tempType = Constants.USER_TYPE_ANCHOR;
				}
				// 如果是管理员
				else if (mManagerUids.contains(uid)) {
					// 管理員、官方管理員
					tempType = Constants.USER_TYPE_ADMIN;
				}
				// 否则是普通用户
				else {
					tempType = Constants.USER_TYPE_NORMAL;
				}
				showPersonInfoDialog(Html.fromHtml(username).toString(), tempType, uid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 礼物界面ViewPage的监听 ClassName: MyOnPageChangeListener <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-25 下午4:45:27 <br/>
	 *
	 * @version PlayingMainActivity
	 * @since JDK 1.6
	 */
	public class GiftViewPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int position) {
			clearGiftSelectedData();
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	/**
	 * 加入饭圈回调: AddFanCallbackData <br/>
	 *
	 * @author Administrator
	 * @version LiveBaseActivity
	 * @since JDK 1.6
	 */
	private class AddFanCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "AddFanCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_ADD_FAN_SUCCESS;
					sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_ADD_FAN_FAILED;
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}

	}

	/**
	 * 获取房间信息回调 Reason: TODO ADD REASON(可选). <br/>
	 */
	private static class GetRoomCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mAcivity;

		public GetRoomCallbackData(BaseFragmentActivity fragment) {
			mAcivity = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetRoomCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			try {
				if (success) {
					msg.what = MsgTypes.GET_ROOM_INFO_SUCCESS;
					Map<String, Object> map = JSONParser.parseMultiInSingle((JSONObject) result,
							new String[]{"gifts", "guardGifts", "packageItemsets"});
					msg.obj = map;
				} else {
					msg.what = MsgTypes.GET_ROOM_INFO_FAILED;
					if ("200".equals(errorCode)) {
						msg.obj = "房间不存在";
					} else if (!TextUtils.isEmpty(errorMsg)) {
						msg.obj = errorMsg;
					} else {
						msg.obj = Constants.NETWORK_FAIL;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				msg.what = MsgTypes.GET_ROOM_INFO_FAILED;
				msg.obj = "数据格式错误";
			}

			BaseFragmentActivity baseFragmentActivity = mAcivity.get();
			// 如果fragment未回收，发送消息
			if (baseFragmentActivity != null) baseFragmentActivity.sendMsg(msg);
		}

	}

	/**
	 * 获取用户背包物品信息
	 */
	protected static class GetUserPackageCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mAcivity;

		public GetUserPackageCallbackData(BaseFragmentActivity fragment) {
			mAcivity = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetUserPackageCallbackData success " + success + " errorCode" +
					errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.GET_USER_PACKAGE_SUCCESS;
					Map<String, Object> map = JSONParser.parseMultiInSingle((JSONObject) result,
							new String[]{"packageItemsets"});
					msg.obj = map;
					BaseFragmentActivity baseFragmentActivity = mAcivity.get();
					// 如果fragment未回收，发送消息
					if (baseFragmentActivity != null) {
						baseFragmentActivity.sendMsg(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 获取主播守护列表
	 */
	protected static class GetModeratorGuardCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mAcivity;

		public GetModeratorGuardCallbackData(BaseFragmentActivity fragment) {
			mAcivity = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetModeratorGuardCallbackData success " + success + " errorCode" +
					errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.GET_MODERATOR_GUARD_SUCCESS;
					List<Map<String, String>> map = JSONParser.parseMulti((JSONArray) result);
					msg.obj = map;
					BaseFragmentActivity baseFragmentActivity = mAcivity.get();
					// 如果fragment未回收，发送消息
					if (baseFragmentActivity != null) {
						baseFragmentActivity.sendMsg(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 获取热门排名的列表
	 */
	protected static class HotRankCallBack implements CallbackDataHandle {
		private final WeakReference<BaseFragmentActivity> mAcivity;

		public HotRankCallBack(BaseFragmentActivity fragment) {
			mAcivity = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "HotRankCallBack success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.GET_HOT_RANK_SUCCESS;
					Map<String, String> data = JSONParser.parseOne((JSONObject) result);
					msg.obj = data;
					BaseFragmentActivity baseFragmentActivity = mAcivity.get();
					// 如果fragment未回收，发送消息
					if (baseFragmentActivity != null) {
						baseFragmentActivity.sendMsg(msg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 提供给js调用 ClassName: Contact <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-7-25 上午9:57:04 <br/>
	 *
	 * @version WebViewActivity
	 * @since JDK 1.6
	 */

	public class JsInvokeMainClass {
		// JavaScript调用此方法Login
		@JavascriptInterface
		public void needLogin() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Utils.requestLoginOrRegister(mActivity, mActivity.getResources().getString(R
							.string.tip_login_title), Constants.REQUEST_CODE_LOGIN);
				}
			});

		}

		@JavascriptInterface
		public void goBack() {
			EvtLog.e(TAG, "JsInvokeMainClass goBack");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mWebView != null) mWebView.setVisibility(View.GONE);
				}
			});
		}

		@JavascriptInterface
		public void onShare() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					toShareLiveInfo();
				}
			});

		}

		@JavascriptInterface
		public void goGroupDetail(final String groupId) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Map<String, String> fanInfo = new HashMap<>();
					fanInfo.put("id", groupId);
					ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class, false,
							FanDetailActivity.FAN_INFO, (Serializable) fanInfo);
				}
			});
		}

		@JavascriptInterface
		public void goPostDetail(final String postId) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					@SuppressWarnings("unchecked") Map<String, String> lmItem = new
							HashMap<String, String>();
					lmItem.put("id", postId);
					ActivityJumpUtil.toGroupPostDetailActivity(mActivity, lmItem, null, 0);
				}
			});
		}

		@JavascriptInterface
		public void goPersonInfo(final String uid) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Map<String, String> personInfo = new HashMap<String, String>();
					personInfo.put("id", uid);
					ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
				}
			});
		}

		@JavascriptInterface
		public void roomDetail(final String rid) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					@SuppressWarnings("unchecked") Map<String, Object> lmItem = new
							HashMap<String, Object>();
					lmItem.put("rid", rid);
					ActivityJumpUtil.toLiveMediaPlayerActivity(mActivity, lmItem);
				}
			});
		}

		@JavascriptInterface
		public void refreshCoin(final String num) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					int coin = Integer.parseInt(num);
					updateBalance(coin);
				}
			});
		}

		@JavascriptInterface
		public void refreshPackage() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					BusinessUtils.getUserPackageInfo(mActivity, new GetUserPackageCallbackData
							(LiveBaseActivity.this));
				}
			});
		}
	}

	/**
	 * 发起连线邀请
	 */
	protected static class RequestVideoChatCallbackDataHandle implements CallbackDataHandle {
		private WeakReference<BaseFragmentActivity> mActivity;

		//被邀请人
		private String uid;

		public RequestVideoChatCallbackDataHandle(BaseFragmentActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		public void setUid(String uid) {
			this.uid = uid;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = Message.obtain();
			if (success) {
				msg.what = MSG_INVITE_VIDEO_CHAT_SUCC;
				msg.obj = uid;
			} else {
				msg.what = MSG_INVITE_VIDEO_CHAT_FAIL;
				msg.obj = errorMsg;
			}
			BaseFragmentActivity baseFragmentActivity = mActivity.get();
			// 如果fragment未回收，发送消息
			if (baseFragmentActivity != null) {
				baseFragmentActivity.sendMsg(msg);
			}
		}
	}

	/**
	 * 结束连线
	 */
	protected static class EndVideoChatCallbackDataHandle implements CallbackDataHandle {
		private WeakReference<BaseFragmentActivity> mActivity;

		public EndVideoChatCallbackDataHandle(BaseFragmentActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = Message.obtain();
			if (success) {
				msg.what = MSG_VIDEO_CHAT_END_SUCC;
				msg.obj = result;
			} else {
				msg.what = MSG_VIDEO_CHAT_END_FAIL;
				msg.obj = errorMsg;
			}
			BaseFragmentActivity baseFragmentActivity = mActivity.get();
			// 如果fragment未回收，发送消息
			if (baseFragmentActivity != null) {
				baseFragmentActivity.sendMsg(msg);
			}
		}
	}

	/**
	 * 接受连线
	 */
	protected static class AcceptVideoChatCallbackDataHandle implements CallbackDataHandle {
		private WeakReference<BaseFragmentActivity> mActivity;

		public AcceptVideoChatCallbackDataHandle(BaseFragmentActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = Message.obtain();
			try {
				if (success) {
					msg.what = MSG_ACCEPT_VIDEO_CHAT_SUCC;
					JSONObject jsonObject = (JSONObject) result;
					AcceptVideoChat data = new AcceptVideoChat();
					data.setUid(jsonObject.getString("uid"));
					data.setHeadPic(jsonObject.getString("headPic"));
					data.setLevel(jsonObject.getInt("level"));
					data.setNickname(jsonObject.getString("nickname"));
					data.setVideoChatType(jsonObject.getInt("videoChatType"));

					data.setUserPullUrl(jsonObject.optString("userPullUrl"));
					data.setPushUrl(jsonObject.optString("pushUrl"));

					data.setUserPushUrl(jsonObject.optString("userPushUrl"));

					data.setPullUrl(jsonObject.optString("pullUrl"));

					msg.obj = data;
				} else {
					msg.what = MSG_ACCEPT_VIDEO_CHAT_FAIL;
					msg.obj = errorMsg;
				}
			} catch (Exception e) {
				msg.what = MSG_ACCEPT_VIDEO_CHAT_FAIL;
				msg.obj = "数据格式错误";
			}
			BaseFragmentActivity baseFragmentActivity = mActivity.get();
			// 如果fragment未回收，发送消息
			if (baseFragmentActivity != null) {
				baseFragmentActivity.sendMsg(msg);
			}
		}
	}

	/**
	 * 取消连线申请
	 */
	protected static class CancelVideoChatCallbackDataHandle implements CallbackDataHandle {
		private WeakReference<BaseFragmentActivity> mActivity;

		public CancelVideoChatCallbackDataHandle(BaseFragmentActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = Message.obtain();
			if (success) {
				msg.what = MSG_CANCEL_VIDEO_CHAT_SUCC;
			} else {
				msg.what = MSG_CANCEL_VIDEO_CHAT_FAIL;
				msg.obj = errorMsg;
			}
			BaseFragmentActivity baseFragmentActivity = mActivity.get();
			// 如果fragment未回收，发送消息
			if (baseFragmentActivity != null) {
				baseFragmentActivity.sendMsg(msg);
			}
		}
	}

	/**
	 * 获取红包CD
	 */
	protected static class GetRedPacketCDCallbackDataHandle implements CallbackDataHandle {
		private WeakReference<BaseFragmentActivity> mActivity;

		public GetRedPacketCDCallbackDataHandle(BaseFragmentActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = Message.obtain();
			if (success) {
				msg.what = MSG_GET_RED_PACKET_CD_SUCC;
				try {
					msg.obj = JSONParser.parseOne((JSONObject) result);
				} catch (JSONException e) {
					e.printStackTrace();
					msg.what = MSG_GET_RED_PACKET_CD_FAIL;
				}
			} else {
				msg.what = MSG_GET_RED_PACKET_CD_FAIL;
				msg.obj = errorMsg;
			}
			BaseFragmentActivity baseFragmentActivity = mActivity.get();
			// 如果fragment未回收，发送消息
			if (baseFragmentActivity != null) {
				baseFragmentActivity.sendMsg(msg);
			}
		}
	}

	/**
	 * 领取红包
	 */
	protected static class GetRedPacketCallbackDataHandle implements CallbackDataHandle {
		private WeakReference<BaseFragmentActivity> mActivity;

		public GetRedPacketCallbackDataHandle(BaseFragmentActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = Message.obtain();
			if (success) {
				msg.what = MSG_GET_RED_PACKET_SUCC;
				try {
					msg.obj = JSONParser.parseOne((JSONObject) result);
				} catch (JSONException e) {
					e.printStackTrace();
					msg.what = MSG_GET_RED_PACKET_FAIL;
				}
			} else {
				msg.what = MSG_GET_RED_PACKET_FAIL;
				msg.obj = errorMsg;
			}
			BaseFragmentActivity baseFragmentActivity = mActivity.get();
			// 如果fragment未回收，发送消息
			if (baseFragmentActivity != null) {
				baseFragmentActivity.sendMsg(msg);
			}
		}
	}

}