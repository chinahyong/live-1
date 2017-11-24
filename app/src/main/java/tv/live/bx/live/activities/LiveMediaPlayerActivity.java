package tv.live.bx.live.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.activities.PersonInfoActivity;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.PermissionUtil;
import tv.live.bx.common.Utils;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.StringUtil;
import tv.live.bx.websocket.WebSocketLiveEngine;
import tv.live.bx.websocket.model.AcceptVideoChat;
import tv.live.bx.websocket.model.InviteVideoChat;
import tv.live.bx.websocket.model.VideoChat;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

@SuppressLint("NewApi")
public class LiveMediaPlayerActivity extends LiveBaseActivity implements OnClickListener, ITXLivePlayListener {
	private static final int CONNECT_LIVE_STATUS_NORMAL = 0;        // 未建立 连线
	private static final int CONNECT_LIVE_STATUS_INVITING = 1;        // 正在邀请，等待对方接收连线
	private static final int CONNECT_LIVE_STATUS_LIVING = 2;        // 正在连线
	private static final int CONNECT_LIVE_STATUS_RECEIVING = 3;        // 接收对方连线
	private static final int CONNECT_LIVE_STATUS_INVITING_RECEIVING = 4;        // 邀请与被邀请
	//推流地址更新
	private static final int MSG_CHANGE_VIDEO_PULL_URL = 0x1120;
	/**
	 * 视频播放地址
	 */
	public static final String MEDIA_PLAY_URL = "videoPlayUrl";
	/**
	 * 主播下线
	 */
	public static final int ANCHOR_UNLINE = 1000;
	/**
	 * 1 为成功，0 为加载视频视频，需要重新加载
	 */
	private static final int LoadVideoFail = 0;

	private static final int MSG_SHOW_FOCUS = 0x110;
	private static int DELAY_SHOW_FOCUS = AppConfig.getInstance().followTime == 0 ? 3 * 60 * 1000
			: AppConfig.getInstance().followTime * 1000;

	/*** 直播间播放器**/
	private TXLivePlayConfig mTxLivePlayConfig;
	private TXLivePlayer mTxLivePlayer;
	private TXCloudVideoView mTxPlayVideoView;
	//默认认为是竖屏画面
	private boolean isPortraitVideo = true;
	// 主播是否正在播
	private boolean mIsPlaying = false;
	// 视频流是否断开
	private boolean mLiveStreamDisConnect = true;
	// 视频播放地址
	private String mMediaPlayUrl;
	// 主播在线状态，默认在线
	private boolean mLiveAnchorOnLineStatus = true;


	//关注主播按钮
	private ImageButton mLiveFocusBtn;
	private TextView mAnchorUnlineTv;

	/*** 视频播放正在加载进度条 **/
	private RelativeLayout mPlayLoadingLayout;
	private ImageView mPlayLoadingBlur;
	private ImageView mPlayLoadingExitIv;

	/*** 底部连线面板 */
	private LinearLayout mConnectLayoutReceive;    //接收选择父布局
	//视频/音频 接收  /忽略   取消连线按钮/发起连线按钮
	private Button mConnectBtnReceiveVideo, mConnectBtnReceiveAudio, mConnectBtnReceiveIgnore,
			mConnectBtnConnectCancel, mConnectBtnConnectStart;
	private RadioGroup mConnectRgSendLiveType;        //发送视频类型
	// 发起连线对方 头像/等级
	private ImageView mConnectIvTargetHeadPic, mConnectIvTargetLevel;
	// 发起连线对方 昵称   连线面板标题（准备发起/正在发起/连接中/接受连接）
	private TextView mConnectTvTargetName, mConnectTvTitle;
	private RelativeLayout mConnectLayout;
	//是否小主播邀请连麦中
	private boolean mIsInvateConnecting;

	/*** 页面的统计时长 */
	private long mStartTime;
	private static String GUIDE_PLAY = "guide_play";


	/*** 小主播连麦推流 */
	private TXLivePushConfig mLivePushConfig;
	private TXLivePusher mLivePusher;
	//小主播推流是否断开
	private boolean mLivePushDisConnect;
	// 用户推流地址
	private String mPublishUrl;
	//小主播推流类型
	private int mLivePushType;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_mian_playing;
	}

	@Override
	protected void initMembers() {
		// AndroidBug5497Workaround.assistActivity(this,
		// findViewById(R.id.review_root_layout));
		super.initMembers();
		mLiveFocusBtn = (ImageButton) findViewById(R.id.btn_live_focus);
		mAnchorUnlineTv = (TextView) findViewById(R.id.noPlayingTv);
		mPlayLoadingLayout = (RelativeLayout) findViewById(R.id.playing_loadingLayout);
		mPlayLoadingBlur = (ImageView) findViewById(R.id.playing_loading_blur);
		mPlayLoadingExitIv = (ImageView) findViewById(R.id.playing_btn_back);
		mTxPlayVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
		// 底部连线面板
		mConnectLayoutReceive = (LinearLayout) findViewById(R.id.live_connect_receive_layout);
		//视频/音频 接收  /忽略   取消连线按钮/发起连线按钮
		mConnectLayout = (RelativeLayout) findViewById(R.id.live_connect_layout_parent);
		mConnectTvTitle = (TextView) findViewById(R.id.live_connect_title);

		mConnectIvTargetHeadPic = (ImageView) findViewById(R.id.live_connect_user_headpic);
		mConnectIvTargetLevel = (ImageView) findViewById(R.id.live_connect_user_level);
		mConnectTvTargetName = (TextView) findViewById(R.id.live_connect_user_name);

		mConnectRgSendLiveType = (RadioGroup) findViewById(R.id.live_connect_rg_send_type);

		mConnectLayoutReceive = (LinearLayout) findViewById(R.id.live_connect_receive_layout);
		mConnectBtnReceiveVideo = (Button) findViewById(R.id.live_connect_receive_video);
		mConnectBtnReceiveAudio = (Button) findViewById(R.id.live_connect_receive_audio);
		mConnectBtnReceiveIgnore = (Button) findViewById(R.id.live_connect_receive_ignore);
		mConnectBtnConnectCancel = (Button) findViewById(R.id.live_connect_btn_cancel);
		mConnectBtnConnectStart = (Button) findViewById(R.id.live_connect_btn_connect);
	}

	@Override
	public void initWidgets() {
		super.initWidgets();
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// 房间信息
		//		mLiveReportBtn.setVisibility(View.VISIBLE);
		//		mLiveScreenSwitchBtn.setVisibility(View.VISIBLE);
		//		mLiveShare.setVisibility(View.VISIBLE);
		mPlayLoadingLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public void finish() {
		if (SHOW_FOCUS_NEED == mIsNeedFocus) {
			this.sendEmptyMsg(MSG_SHOW_FOCUS_DIALOG);
			return;
		}
		super.finish();
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Intent intent = getIntent();
		if (intent != null) {
			mmIntentRoomInfo = (Map<String, String>) intent.getSerializableExtra(LiveBaseActivity
					.ANCHOR_RID);
			mIsPrivatePlay = Utils.strBool(mmIntentRoomInfo.get(LiveBaseActivity.ANCHOR_PRIVATE));
			mMediaPlayUrl = mmIntentRoomInfo.get(LiveMediaPlayerActivity.MEDIA_PLAY_URL);
		}
		super.initData(savedInstanceState);
		if (Utils.getBooleanFlag(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, GUIDE_PLAY,
				Constants.COMMON_TRUE))) {
			showFullDialog(R.layout.dialog_guide_playing_layout, new DialogInterface
					.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					Utils.setCfg(mActivity, Constants.COMMON_SF_NAME, GUIDE_PLAY, "false");
				}
			});
		}
		EvtLog.e(TAG, "doBlur start xxx:" + SystemClock.currentThreadTimeMillis());
		applyBlur(mPlayLoadingBlur);
		EvtLog.e(TAG, "doBlur end xxx:" + SystemClock.currentThreadTimeMillis());
		/** 如果有播放地址，直接先请求视频流 */
		if (!TextUtils.isEmpty(mMediaPlayUrl)) {
			startLivePlayer(true);
		}
	}

	/**
	 * @see tv.live.bx.live.activities.LiveBaseActivity#setEventsListeners()
	 */
	@Override
	protected void setEventsListeners() {
		super.setEventsListeners();
		mPlayLoadingExitIv.setOnClickListener(this);
		mLiveFocusBtn.setOnClickListener(this);
		mConnectLayout.setOnClickListener(this);
		mConnectBtnConnectStart.setOnClickListener(this);
		mConnectBtnConnectCancel.setOnClickListener(this);
		mConnectBtnReceiveAudio.setOnClickListener(this);
		mConnectBtnReceiveVideo.setOnClickListener(this);
		mConnectBtnReceiveIgnore.setOnClickListener(this);
		mConnectRgSendLiveType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
				switch (checkedId) {
					case R.id.live_connect_send_audio:
						OperationHelper.onEvent(FeizaoApp.mConctext,
								"clickChooseVoiceLinkModelButton");
						break;
					case R.id.live_connect_send_video:
						OperationHelper.onEvent(FeizaoApp.mConctext,
								"clickChooseVideoLinkModelButton");
						break;
				}
			}
		});
	}

	/**
	 * 底部连线按钮初始化
	 *
	 * @param isSelected 当前连线按钮是否高亮显示（只要是连线中均高亮）
	 */
	private void initBottomBtnConnect(boolean isSelected) {
		mLiveConnectNum.setVisibility(View.GONE);
		mLiveConenctAnim.setVisibility(View.GONE);
		mLiveConnectIv.setVisibility(View.VISIBLE);
		mLiveConnectIv.setSelected(isSelected);
	}

	/**
	 * TODO 简单描述该方法的实现功能（可选）.
	 *
	 * @see tv.live.bx.live.activities.LiveBaseActivity#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
			// 未开始退出直播
			case R.id.live_btn_exit:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "exitLiveRoom");
				OperationHelper.onEvent(FeizaoApp.mConctext, "exitLiveRoom", null);
				finish();
				break;
			case R.id.playing_btn_back:
				finish();
				break;
			case R.id.btn_live_focus:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "followBroadcaster");
				OperationHelper.onEvent(FeizaoApp.mConctext, "followBroadcaster", null);
				BusinessUtils.follow(mActivity, new FollowCallbackData(LiveMediaPlayerActivity
						.this), mmAnchorInfo.get("id"));
				break;
			// 无动效的时候连线点击事件
			case R.id.live_connect:
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickLinkButtonInLivingRoom");
				mConnectLayout.setVisibility(View.VISIBLE);
				break;
			// 有动效的时候连线点击事件
			case R.id.live_connect_anim:
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickLinkButtonInLivingRoom");
				mConnectLayout.setVisibility(View.VISIBLE);
				break;
			case R.id.live_connect_layout_parent:
				mConnectLayout.startAnimation(mDownOutAnimation);
				mConnectLayout.setVisibility(View.GONE);
				break;
			// 立即连线按钮
			case R.id.live_connect_btn_connect:
				// 音频连接/视频连接都是需要判断 音频权限
				// 获取录音权限，如果不存在直接申请
				if (Utils.greaterThanNowSDKVersion(Build.VERSION_CODES.M)) {
					boolean connectPermission = PermissionUtil.permissionGrantedAndRequest
							(mActivity, PermissionUtil.REQUEST_PERMISSION_CONNECT, PermissionUtil
									.PERMISSION_LIVES);
					if (!connectPermission) {
						return;
					}
				}
				clickConnectRequest();
				break;
			// 取消连线按钮
			case R.id.live_connect_btn_cancel:
				// 取消连线按钮变为等待中，不可点击
				mConnectBtnConnectCancel.setText(R.string.live_connect_canceling);
				mConnectBtnConnectCancel.setEnabled(false);
				// 正在连线中
				if (getResources().getText(R.string.live_connect_connecting_title).equals
						(mConnectTvTitle.getText())) {
					BusinessUtils.endVideoChat(mActivity, new EndVideoChatCallbackDataHandle
									(this), mmIntentRoomInfo.get("rid"), String.valueOf(TYPE_LIVE_USER),
							null);
				} else {
					BusinessUtils.cancelVideoChat(mActivity, new
									CancelVideoChatCallbackDataHandle(this), mmIntentRoomInfo.get("rid"),
							String.valueOf(TYPE_LIVE_USER), null);
				}
				break;
			// 视频接收
			case R.id.live_connect_receive_video:
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickVoiceLinkButton");
				if (Utils.greaterThanNowSDKVersion(Build.VERSION_CODES.M)) {
					// 获取摄像头权限，如果不存在直接申请
					boolean videoReceiveAudioPermission = PermissionUtil
							.permissionGrantedAndRequest(mActivity, PermissionUtil
									.REQUEST_PERMISSION_RECEIVE, PermissionUtil.PERMISSION_LIVES);
					if (!videoReceiveAudioPermission) {
						return;
					}
				}
				clickReceiveAccept(InviteVideoChat.INVITE_CHAT_TYPE_VIDEO);
				break;
			// 音频接收
			case R.id.live_connect_receive_audio:
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickVideoLinkButton");
				if (Utils.greaterThanNowSDKVersion(Build.VERSION_CODES.M)) {
					// 获取录音权限，如果不存在直接申请
					boolean audioReceiveAudioPermission = PermissionUtil
							.permissionGrantedAndRequest(mActivity, PermissionUtil
									.REQUEST_PERMISSION_RECORD_AUDIO, Manifest.permission
									.RECORD_AUDIO);
					// 不存在录音权限
					if (!audioReceiveAudioPermission) {
						return;
					}
				}
				clickReceiveAccept(InviteVideoChat.INVITE_CHAT_TYPE_MIC);
				break;
			// 拒绝接收
			case R.id.live_connect_receive_ignore:
				mConnectBtnReceiveIgnore.setEnabled(false);
				BusinessUtils.rejectVideoChat(mActivity, new RejectVideoChatCallbackDataHandle
						(this), mmIntentRoomInfo.get("rid"));
				break;
			default:
				break;
		}
	}

	/**
	 * 立即连线按钮，当权限正常时，执行此功能（仅做了音频）
	 */
	private void clickConnectRequest() {
		// 权限正常
		// 1. 立即连线 按钮变为等待中，且不可点击
		mConnectBtnConnectStart.setText(R.string.live_connect_connecting);
		mConnectBtnConnectStart.setEnabled(false);
		// 2. 点不连线按钮转为选中
		mLiveConnectIv.setSelected(true);
		BusinessUtils.requestVideoChat(mActivity, new RequestVideoChatCallbackDataHandle(this),
				mmIntentRoomInfo.get("rid"), String.valueOf(TYPE_LIVE_USER), null, String.valueOf
						(InviteVideoChat.INVITE_CHAT_TYPE_VIDEO));
	}

	/**
	 * 点击 视频接受/音频接受 当权限正常时，执行此功能
	 */
	private void clickReceiveAccept(int type) {
		if (InviteVideoChat.INVITE_CHAT_TYPE_VIDEO == type) {
			mConnectBtnReceiveVideo.setEnabled(false);
		} else {
			mConnectBtnReceiveAudio.setEnabled(false);
		}
		BusinessUtils.acceptVideoChat(mActivity, new AcceptVideoChatCallbackDataHandle(this),
				mmIntentRoomInfo.get("rid"), String.valueOf(TYPE_LIVE_USER), null, String.valueOf
						(type));
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mLivePusher != null) {
			mLiveConnectVideoView.onResume();
			mLivePusher.resumePusher();
		}
		if (mTxLivePlayer != null) {
			mTxLivePlayer.resume();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		mStartTime = System.currentTimeMillis();
	}

	@Override
	public void onStop() {
		super.onStop();
		long currentTime = System.currentTimeMillis();
		MobclickAgent.onEventValue(FeizaoApp.mConctext, "timeSpentOnLiveRoom", null, (int) (
				(currentTime - mStartTime) / 1000));
		if (mLivePusher != null) {
			mLiveConnectVideoView.onPause();
			mLivePusher.pausePusher();
		}
	}

	@Override
	public void onDestroy() {
		EvtLog.e(TAG, "onDestroy");
		destroyLivePlayer();
		stopLivePushStream();
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		switch (requestCode) {
			// 立即连线权限判断回调
			case PermissionUtil.REQUEST_PERMISSION_CONNECT:
				// 拒绝权限
				if (!PermissionUtil.permissionIsGranted(mActivity, PermissionUtil
						.PERMISSION_LIVES)) {
					UiHelper.showToast(mActivity, R.string.live_connect_permission_tip);
				} else {
					clickConnectRequest();
				}
				break;
			// 视频接受权限判断回调
			case PermissionUtil.REQUEST_PERMISSION_RECEIVE:
				// 拒绝权限
				if (!PermissionUtil.permissionIsGranted(mActivity, PermissionUtil
						.PERMISSION_LIVES)) {
					UiHelper.showToast(mActivity, R.string.live_connect_permission_tip);
				} else {
					clickReceiveAccept(InviteVideoChat.INVITE_CHAT_TYPE_VIDEO);
				}
				break;
			// 音频接受 权限判断回调
			case PermissionUtil.REQUEST_PERMISSION_RECORD_AUDIO:
				// 拒绝权限
				if (!PermissionUtil.permissionIsGranted(mActivity, Manifest.permission.CAMERA)) {
					UiHelper.showToast(mActivity, R.string.live_connect_permission_tip);
				} else {
					clickReceiveAccept(InviteVideoChat.INVITE_CHAT_TYPE_MIC);
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		EvtLog.e(TAG, "onActivityResult requestCode " + requestCode + "resultCode " + resultCode);
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_PERSONINFO) {
			if (resultCode == RESULT_OK) {
				//如果关注的用户是主播，则更新主播信息状态
				if (data.getStringExtra(PersonInfoActivity.PERSON_ID).equals(mmAnchorInfo.get
						("id"))) {
					if (!data.getBooleanExtra(PersonInfoActivity.PERSON_FOCUS_STATE, false)) {
						mLiveFocusBtn.setVisibility(View.VISIBLE);
					} else {
						mLiveFocusBtn.setVisibility(View.GONE);
					}
				}
			}
		}

	}

	@Override
	public void onBackPressed() {
		if (mConnectLayout.isShown()) {
			mConnectLayout.startAnimation(mDownOutAnimation);
			mConnectLayout.setVisibility(View.GONE);
			return;
		}
		super.onBackPressed();
	}

	@Override
	protected void screenLandscape() {
		super.screenLandscape();
		updateLivePlayRenderMode();
	}

	@Override
	protected void screenPortrait() {
		super.screenPortrait();
		updateLivePlayRenderMode();
	}

	@Override
	protected void switchSrceen() {
		//主动邀请中，或者连麦中
		if (mIsInvateConnecting || mIsLiveConnecting) {
			showTips(R.string.live_connect_switch_screen_tip);
		} else {
			super.switchSrceen();
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MsgTypes.ON_LOAD_VIDEO_SUCCESS:
				EvtLog.i(TAG, "video loading success!");
				//如果主播先断开下线了，然后再上线提示“系统消息：主播回来啦，精彩继续”
				if (!mLiveAnchorOnLineStatus) {
					CharSequence charSequence = chatFragment.onSysMsg(mActivity.getResources()
							.getString(R.string.live_anchor_back_tip));
					chatFragment.sendChatMsg(charSequence);
				}
				mLiveAnchorOnLineStatus = true;
				mPlayLoadingLayout.setVisibility(View.GONE);
				int isSuccess = (Integer) msg.obj;
				// 如果视频记载失败，一般为主播不在线
				if (isSuccess == LoadVideoFail) {
					mAnchorUnlineTv.setVisibility(View.VISIBLE);
				}
				break;
			case ANCHOR_UNLINE:
				mAnchorUnlineTv.setVisibility(View.VISIBLE);
				mPlayLoadingLayout.setVisibility(View.GONE);
				break;
			case MsgTypes.FOLLOW_SUCCESS:
				OperationHelper.onEvent(FeizaoApp.mConctext, "followBroadcasterSuccessful", null);
				mLiveFocusBtn.setVisibility(View.GONE);
				mWebSocketImpl.sendCommand(WebSocketLiveEngine.USER_ATTENTION);
				UiHelper.showShortToast(mActivity, R.string.person_focus_success);
				UiHelper.showNotificationDialog(mActivity);
				break;
			case MsgTypes.FOLLOW_FAILED:
				UiHelper.showShortToast(mActivity, (String) msg.obj);
				break;
			// 拒绝连线成功
			case MSG_USER_REJECT_VIDEO_CHAT_SUCC:
				UiHelper.showToast(mActivity, R.string.live_connect_cancel_yourself_tip);
				if (mConnectLayout.isShown()) {
					mConnectLayout.startAnimation(mDownOutAnimation);
					mConnectLayout.setVisibility(View.GONE);
				}
				// 拒绝连线成功，更新连线面板tag状态
				if (mConnectLayout.getTag(R.id.tag_connect_status) != null) {
					int status = Integer.parseInt(String.valueOf(mConnectLayout.getTag(R.id
							.tag_connect_status)));
					// 邀请状态
					if (status == CONNECT_LIVE_STATUS_RECEIVING) {
						// 3. 被对方同意邀请后，置空邀请状态
						mConnectLayout.setTag(R.id.tag_connect_status, null);
					} else if (status == CONNECT_LIVE_STATUS_INVITING_RECEIVING) {
						// 邀请 + 接收状态
						// 3. 被对方同意邀请后，置空邀请状态
						mConnectLayout.setTag(R.id.tag_connect_status,
								CONNECT_LIVE_STATUS_INVITING);
					}
				}
				// 返回上一级状态
				connectLayoutGoBack();
				// 拒绝按钮设置为可点击
				mConnectBtnReceiveIgnore.setEnabled(true);
				break;
			case MSG_USER_REJECT_VIDEO_CHAT_FAIL:
				if (msg.obj != null) {
					UiHelper.showToast(mActivity, (String) msg.obj);
				}
				// 拒绝按钮设置为可点击
				mConnectBtnReceiveIgnore.setEnabled(true);
				break;
			case MSG_SHOW_FOCUS:
				mIsNeedFocus = SHOW_FOCUS_NEED;
				break;
			case MSG_SHOW_FOCUS_DIALOG:
				showNeedFocusDialog();
				break;
			default:
				break;
		}
	}

	/**
	 * 发起连线成功
	 */
	protected void inviteVideoChatSucc(String uid) {
		super.inviteVideoChatSucc(uid);
		// 等待对方接收，邀请中 底部标记为  邀请中
		updateConnectUIStatus(CONNECT_LIVE_STATUS_INVITING);
		mIsInvateConnecting = true;
	}

	@Override
	protected void inviteVideoChatFail(Message msg) {
		super.inviteVideoChatFail(msg);
		mLiveConnectIv.setSelected(false);
		// 用户发起连线成功，更新UI
		mConnectBtnConnectStart.setText(R.string.live_connect_connect);
		mConnectBtnConnectStart.setEnabled(true);
	}

	@Override
	protected void clickEndVideoChat(String uid) {
		super.clickEndVideoChat(uid);
		BusinessUtils.endVideoChat(mActivity, new EndVideoChatCallbackDataHandle(this),
				mmIntentRoomInfo.get("rid"), String.valueOf(TYPE_LIVE_USER), null);
	}

	/**
	 * 结束连线成功
	 */
	protected void endVideoChatSucc(JSONObject data) {
		super.endVideoChatSucc(data);
		// 1. 结束连麦，UI更新
		stopPublishUpdateUi();
		// 2. 停止推流
		stopPublishRtmp();
		String pullUrl = data.optString("pullUrl");
		// 新的拉流地址不为空，并且不等于之前的拉流地址
		if (!TextUtils.isEmpty(pullUrl) && !pullUrl.equals(mMediaPlayUrl)) {
			mMediaPlayUrl = pullUrl;
			stopLivePlayer(false);
			// 重新拉流，此方法有重置
			startLivePlayer(false);
		}
	}

	/**
	 * 结束连线失败
	 */
	@Override
	protected void endVideoChatFail(Message msg) {
		super.endVideoChatFail(msg);
		// 用户发起连线成功，更新UI
		mConnectBtnConnectCancel.setText(R.string.live_connect_cancel);
		mConnectBtnConnectCancel.setEnabled(true);
	}

	/**
	 * 取消连线邀请成功
	 */
	@Override
	protected void cancelVideoChatSucc() {
		super.cancelVideoChatSucc();
		// 隐藏底部连线面板
		if (mConnectLayout.isShown()) {
			mConnectLayout.startAnimation(mDownOutAnimation);
			mConnectLayout.setVisibility(View.GONE);
		}
		updateConnectUIStatus(CONNECT_LIVE_STATUS_NORMAL);
		mIsInvateConnecting = false;
	}

	/**
	 * 取消连线邀请失败
	 *
	 * @param msg
	 */
	@Override
	protected void cancelVideoChatFail(Message msg) {
		super.cancelVideoChatFail(msg);
		// 用户发起连线成功，更新UI
		mConnectBtnConnectCancel.setText(R.string.live_connect_cancel);
		mConnectBtnConnectCancel.setEnabled(true);
	}

	/**
	 * 同意连线
	 */
	@Override
	protected void acceptVideoChatSucc(AcceptVideoChat acceptVideoChat) {
		super.acceptVideoChatSucc(acceptVideoChat);
		// 接受连线 设置为可点击
		mConnectBtnReceiveAudio.setEnabled(true);
		mConnectBtnReceiveVideo.setEnabled(true);
		// 1. 开始推流更新UI
		startPublishUpdateUI(acceptVideoChat);
		if (mConnectLayout.getTag(R.id.tag_connect_status) != null) {
			int status = Integer.parseInt(String.valueOf(mConnectLayout.getTag(R.id
					.tag_connect_status)));
			// 邀请状态
			if (status == CONNECT_LIVE_STATUS_RECEIVING) {
				// 3. 被对方同意邀请后，置空邀请状态
				mConnectLayout.setTag(R.id.tag_connect_status, null);
			} else if (status == CONNECT_LIVE_STATUS_INVITING_RECEIVING) {
				// 邀请 + 接收状态,同意了对方邀请，此处转为邀请状态
				mConnectLayout.setTag(R.id.tag_connect_status, CONNECT_LIVE_STATUS_INVITING);
			}
		}
		// 2. 重新获取大主播拉流地址，并且小主播开始推流
		startPublishRtmp(acceptVideoChat);
		// 3. 同意连线，底部按钮还原（连线按钮高亮显示）
		initBottomBtnConnect(true);

		//同意连线后，邀请状态也算结束
		mIsInvateConnecting = false;
	}

	@Override
	protected void acceptVideoChatFail(Message msg) {
		super.acceptVideoChatFail(msg);
		// 接受连线 设置为可点击
		mConnectBtnReceiveAudio.setEnabled(true);
		mConnectBtnReceiveVideo.setEnabled(true);
	}

	/**
	 * 更新连麦面板UI
	 *
	 * @param status {@link #CONNECT_LIVE_STATUS_NORMAL} or
	 *               {@link #CONNECT_LIVE_STATUS_INVITING} or
	 *               {@link #CONNECT_LIVE_STATUS_LIVING} or
	 *               {@link #CONNECT_LIVE_STATUS_RECEIVING}
	 */
	private void updateConnectUIStatus(int status) {
		// 状态：正常未连线
		if (status == CONNECT_LIVE_STATUS_NORMAL) {
			// 修改连线面板title
			mConnectTvTitle.setText(R.string.live_connect_title);
			// 隐藏 取消连线 按钮
			mConnectBtnConnectCancel.setText(R.string.live_connect_cancel);
			mConnectBtnConnectCancel.setVisibility(View.GONE);
			mConnectBtnConnectCancel.setEnabled(true);
			// 显示 立即连线 按钮
			mConnectBtnConnectStart.setVisibility(View.VISIBLE);
			mConnectBtnConnectStart.setEnabled(true);
			mConnectBtnConnectStart.setText(R.string.live_connect_connect);
			// 显示 连线类型
			//			mConnectRgSendLiveType.setVisibility(View.VISIBLE);
			// 隐藏 接受连线
			mConnectLayoutReceive.setVisibility(View.GONE);

			// 底部连线按钮状态：未选中
			initBottomBtnConnect(false);
			// 被邀请/邀请均转为null
			mConnectLayout.setTag(R.id.tag_connect_status, null);
		} else if (status == CONNECT_LIVE_STATUS_INVITING) {
			// 状态：主动邀请
			// 用户发起连线成功，更新UI
			mConnectTvTitle.setText(R.string.live_connect_request_title);
			// 显示 取消连线，并设置可点击以及修改文本
			mConnectBtnConnectCancel.setText(R.string.live_connect_cancel);
			mConnectBtnConnectCancel.setEnabled(true);
			mConnectBtnConnectCancel.setVisibility(View.VISIBLE);
			// 隐藏 立即连线
			mConnectBtnConnectStart.setText(R.string.live_connect_connect);
			mConnectBtnConnectStart.setEnabled(true);
			mConnectBtnConnectStart.setVisibility(View.GONE);
			mConnectRgSendLiveType.setVisibility(View.GONE);
			// 隐藏 接收连线UI
			mConnectLayoutReceive.setVisibility(View.GONE);

			// 邀请连线成功，连线申请中为高亮显示连线按钮
			mConnectLayout.setTag(R.id.tag_connect_status, CONNECT_LIVE_STATUS_INVITING);
			initBottomBtnConnect(true);
		} else if (status == CONNECT_LIVE_STATUS_LIVING) {
			// 状态：连线中
			// 修改连线面板Title
			mConnectTvTitle.setText(R.string.live_connect_connecting_title);
			// 显示 取消连线，并设置可点击以及修改文本
			mConnectBtnConnectCancel.setText(R.string.live_connect_cancel);
			mConnectBtnConnectCancel.setEnabled(true);
			mConnectBtnConnectCancel.setVisibility(View.VISIBLE);
			// 隐藏 立即连线
			mConnectBtnConnectStart.setText(R.string.live_connect_connect);
			mConnectBtnConnectStart.setEnabled(true);
			mConnectBtnConnectStart.setVisibility(View.GONE);
			mConnectRgSendLiveType.setVisibility(View.GONE);
			// 隐藏 接收连线UI
			mConnectLayoutReceive.setVisibility(View.GONE);

			// 连线中状态：连线按钮至灰
			initBottomBtnConnect(false);
		} else if (status == CONNECT_LIVE_STATUS_RECEIVING) {
			// 状态：被邀请中
			// 修改title
			mConnectTvTitle.setText(R.string.live_connect_by_anchor_title);
			// 显示 取消连线，并设置可点击以及修改文本
			mConnectBtnConnectCancel.setText(R.string.live_connect_cancel);
			mConnectBtnConnectCancel.setEnabled(true);
			mConnectBtnConnectCancel.setVisibility(View.GONE);
			// 隐藏 立即连线
			mConnectBtnConnectStart.setText(R.string.live_connect_connect);
			mConnectBtnConnectStart.setEnabled(true);
			mConnectBtnConnectStart.setVisibility(View.GONE);
			mConnectRgSendLiveType.setVisibility(View.GONE);
			// 接收连线打开
			mConnectLayoutReceive.setVisibility(View.VISIBLE);
			// 被邀请 ， 底部标记为被邀请中
			if (mConnectLayout.getTag(R.id.tag_connect_status) != null) {
				int tagStatus = Integer.parseInt(String.valueOf(mConnectLayout.getTag(R.id
						.tag_connect_status)));
				if (tagStatus == CONNECT_LIVE_STATUS_RECEIVING) {
					mConnectLayout.setTag(R.id.tag_connect_status,
							CONNECT_LIVE_STATUS_INVITING_RECEIVING);
				}
			} else {
				mConnectLayout.setTag(R.id.tag_connect_status, CONNECT_LIVE_STATUS_RECEIVING);
			}
		}
	}

	@Override
	public void initRoomData() {
		super.initRoomData();
		if (mmRoomInfo == null) return;
		mIsPlaying = Boolean.valueOf((String) mmRoomInfo.get("isPlaying"));
		//未关注并且不是自己
		if (!mUid.equals(mmAnchorInfo.get("id")) && !Boolean.valueOf((String) mmRoomInfo.get
				("loved"))) {
			mLiveFocusBtn.setVisibility(View.VISIBLE);
			setDelayShowFocus();
		} else {
			mLiveFocusBtn.setVisibility(View.GONE);
			mIsNeedFocus = SHOW_FOCUS_NOT;
		}
		if (!mIsPlaying) {
			mAnchorUnlineTv.setVisibility(View.VISIBLE);
			mPlayLoadingLayout.setVisibility(View.GONE);
		} else if (mIsPlaying) {// 如果正在播放，则加载视频
			mAnchorUnlineTv.setVisibility(View.GONE);
			if (!mmRoomInfo.get("videoPlayUrl").equals(mMediaPlayUrl)) {
				mMediaPlayUrl = (String) mmRoomInfo.get("videoPlayUrl");
				stopLivePlayer(false);
				startLivePlayer(true);
			} else if (mLiveStreamDisConnect) {
				startLivePlayer(true);
			}
		}
		if (Utils.getBooleanFlag(mmRoomInfo.get("isHot")) && mIsPlaying) {
			//获取热门排名信息
			getHotRankInfo();
		}
		// 初始化底部连线面板中的主播数据
		initConnectLayoutData();
	}

	/**
	 * 初始化底部连线面板的主播数据
	 */
	private void initConnectLayoutData() {
		// 连线面板
		ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity, mConnectIvTargetHeadPic,
				mmAnchorInfo.get("headPic"));
		mConnectTvTargetName.setText(mmAnchorInfo.get("true_name"));
		ImageLoaderUtil.with().loadImage(mActivity, mConnectIvTargetLevel, Utils
				.getLevelImageResourceUri(Constants.USER_ANCHOR_LEVEL_PIX, mmAnchorInfo.get
						("moderatorLevel")));
	}

	@Override
	protected void switchRoomResetData() {
		super.switchRoomResetData();
		String newMediaPlayUrl = mmIntentRoomInfo.get(LiveMediaPlayerActivity.MEDIA_PLAY_URL);
		mMediaPlayUrl = newMediaPlayUrl;

		// 重置数据
		mIsPlaying = false;
		mLiveStreamDisConnect = true;

		/** 暂停之前房间视频 */
		stopLivePlayer(true);

		EvtLog.e(TAG, "doBlur start xxx:" + SystemClock.currentThreadTimeMillis());
		// 切换房间，只是用默认的高斯模糊图
		mPlayLoadingBlur.setImageResource(R.drawable.live_load_blur);
		mPlayLoadingLayout.setVisibility(View.VISIBLE);
		EvtLog.e(TAG, "doBlur end xxx:" + SystemClock.currentThreadTimeMillis());
		/** 如果有播放地址，直接先请求视频流 */
		if (!TextUtils.isEmpty(mMediaPlayUrl)) {
			startLivePlayer(true);
		}
		// 初始化连线状态
		initConnectChat();        // 连麦区域初始化
		updateConnectUIStatus(CONNECT_LIVE_STATUS_NORMAL);        //// 连麦面板初始化
		networkRecovery();
	}

	@Override
	protected void showPrivatePlayLayout(boolean isPrivatePlay) {
		super.showPrivatePlayLayout(isPrivatePlay);
		if (isPrivatePlay) {
			mPlayingActivityLeftLayout.setVisibility(View.GONE);
			//去掉左上角主播信息那里的观众数
			mLiveAudience.setVisibility(View.GONE);
			//分享按钮、水印都去掉。
			mLiveWateMarkLayout.setVisibility(View.GONE);
			mLiveShare.setVisibility(View.GONE);
			mLiveScreenSwitchBtn.setVisibility(View.GONE);
			//去掉守护列表，守护图标
			mGuardLayout.setVisibility(View.GONE);
		} else {
			mPlayingActivityLeftLayout.setVisibility(View.VISIBLE);
			//去掉左上角主播信息那里的观众数
			mLiveAudience.setVisibility(View.VISIBLE);
			//分享按钮、水印都去掉。
			mLiveWateMarkLayout.setVisibility(View.VISIBLE);
			mLiveShare.setVisibility(View.VISIBLE);
			//			mLiveScreenSwitchBtn.setVisibility(View.VISIBLE);
			//去掉守护列表，守护图标
			//			mGuardLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 小主播开始推流
	 */
	private void startLivePushStream() {
		// 1. 初始化推流配置
		if (mLivePushConfig == null) {
			mLivePushConfig = new TXLivePushConfig();
			mLivePushConfig.setAudioSampleRate(48000); //音频采样率默认就是48K，不要设为其它值
			mLivePushConfig.setPauseImg(60 * 60 * 10, 10);
			Bitmap bitmap = decodeResource(getResources(), R.drawable.small_anchor_pause);
			mLivePushConfig.setPauseImg(bitmap);
			mLivePushConfig.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants
					.PAUSE_FLAG_PAUSE_AUDIO);

			// 用户推流只允许前置
			mLivePushConfig.setFrontCamera(true);
			mLivePushConfig.enableNearestIP(false);
			//开启回声消除：连麦时必须开启，非连麦时不要开启
			mLivePushConfig.enableAEC(true);
			// 用户可默认开启美颜
			mLivePushConfig.setBeautyFilter(5, 3);
			// 关闭聚焦
			mLivePushConfig.setTouchFocus(false);
			if (!isSupportHWEncode()) {
				mLivePushConfig.setHardwareAcceleration(TXLiveConstants.ENCODE_VIDEO_SOFTWARE);
			} else {
				mLivePushConfig.setHardwareAcceleration(TXLiveConstants.ENCODE_VIDEO_AUTO);
			}
		}
		// 2. 根据聊天类型，配置推流类型
		// 如果当前为音频通话
		if (mLivePushType == InviteVideoChat.INVITE_CHAT_TYPE_MIC) {
			// 纯音频推流
			mLivePushConfig.enablePureAudioPush(true);
		} else {
			// 关闭纯音频推流
			mLivePushConfig.enablePureAudioPush(false);
		}
		// 3. 初始化pusher
		if (mLivePusher == null) {
			mLivePusher = new TXLivePusher(this);
			mLivePusher.setPushListener(new ITXLivePushListener() {
				@Override
				public void onPushEvent(int event, Bundle param) {
					String msgParam = param.getString(TXLiveConstants.EVT_DESCRIPTION);
					EvtLog.e(TAG, "onPushEvent msg " + msgParam + " event:" + event);
					switch (event) {
						case TXLiveConstants.PUSH_ERR_NET_DISCONNECT:
							mLivePushDisConnect = true;
							if (mLivePusher != null) {
								mLivePusher.stopCameraPreview(false);
								mLivePusher.stopPusher();
								showTips(R.string.live_error);
							}
							break;
						case TXLiveConstants.PUSH_WARNING_NET_BUSY:
							showTips(R.string.live_push_warning_net_busy);
							break;
						case TXLiveConstants.PUSH_EVT_OPEN_CAMERA_SUCC:
							break;
						case TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL:
							showTips(msgParam);
							break;
						case TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL:
							showTips(msgParam);
							break;
						// 小主播开始推流
						case TXLiveConstants.PUSH_EVT_PUSH_BEGIN:
							mLivePushDisConnect = false;
							break;
					}
				}

				@Override
				public void onNetStatus(Bundle status) {
					Log.d(TAG, "ITXLivePushListener Current status, CPU:" + status.getString
							(TXLiveConstants.NET_STATUS_CPU_USAGE) + ", RES:" + status.getInt
							(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) + "*" + status.getInt
							(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT) + ", SPD:" + status.getInt
							(TXLiveConstants.NET_STATUS_NET_SPEED) + "Kbps" + ", FPS:" + status
							.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS) + ", ARA:" + status
							.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps" + ", VRA:"
							+ status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps");
				}
			});
			int[] ver = TXLivePusher.getSDKVersion();
			if (ver != null && ver.length >= 4) {
				EvtLog.e(TAG, String.format("rtmp sdk version:%d.%d.%d.%d ", ver[0], ver[1],
						ver[2], ver[3]));
			}
		}
		// 4. 新的配置配置给pusher
		mLivePusher.setConfig(mLivePushConfig);
		// 5. 根据聊天类型，打开或关闭预览画面
		if (mLivePushType == InviteVideoChat.INVITE_CHAT_TYPE_VIDEO) {
			mLivePusher.startCameraPreview(mLiveConnectVideoView);
		} else {
			mLivePusher.stopCameraPreview(true);
		}
		//连麦模式：小主播
		mLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_LINKMIC_SUB_PUBLISHER, false,
				false);
		// 设置默认滤镜：美白(该资源图片从iOS拿的，尺寸没问题)
		Bitmap bmp = decodeResource(getResources(), R.drawable.filter_fennen);
		if (mLivePusher != null) {
			mLivePusher.setFilter(bmp);
		}
		// 镜像录制
		mLivePusher.setMirror(true);
		// 6. 开始推流
		int result = mLivePusher.startPusher(mPublishUrl);
		EvtLog.e(TAG, "startLivePushStream pushUrl:" + mPublishUrl + " result:" + result);
	}


	/**
	 * 小主播退出连麦
	 */
	private void stopLivePushStream() {
		if (mLivePusher != null) {
			mLivePusher.stopPusher();
			mLivePusher.setPushListener(null);
			mLivePusher = null;
		}
	}

	@Override
	protected void networkRecovery() {
		super.networkRecovery();
		/** 如果已经开始播放，且视频流断开 */
		if (mIsPlaying && mLiveStreamDisConnect) {
			startLivePlayer(true);
		}
		//如果正在连麦，且小主播推流已经断开，重新推流
		if (mIsLiveConnecting && mLivePushDisConnect) {
			startLivePushStream();
		}
	}

	@Override
	public void onConnectStatus() {
		super.onConnectStatus();
		stopWebSocket();
		if (mIsPlaying) {
			EvtLog.d(TAG, "onConnectStatus stopMainThread");
			stopLivePlayer(false);
		}
	}

	@Override
	public void addUser(String piUid, String piType, String psNickname, String level, String
			fromModeratorLevel, String psPhoto, String lowkeyEnter, String cid, String medals,
						String isGuard, String guardType, String guardTimeType, String mountId,
						String mountName, String mountAction, String androidMount) {
		super.addUser(piUid, piType, psNickname, level, fromModeratorLevel, psPhoto, lowkeyEnter,
				cid, medals, isGuard, guardType, guardTimeType, mountId, mountName, mountAction,
				androidMount);
		//私播
		if (mIsPrivatePlay) {
			SparseArray<String> sparseArray = new SparseArray<>();
			sparseArray.put(PRIVATE_USER_UID, piUid);
			sparseArray.put(PRIVATE_USER_USERNAME, psNickname);
			sparseArray.put(PRIVATE_USER_PHOTO, psPhoto);
			mUserPhotoArray.add(sparseArray);
			sendEmptyMsg(MSG_UPDATE_USER_PHOTO);
		}
	}

	@Override
	public void delUser(String piUid, String piType, String psNickname, String psPhoto, String
			ban, String cid) {
		super.delUser(piUid, piType, psNickname, psPhoto, ban, cid);
		if (mIsPrivatePlay) {
			Iterator<SparseArray<String>> iterator = mUserPhotoArray.iterator();
			while (iterator.hasNext()) {
				SparseArray sparseArray = iterator.next();
				if (piUid.equals(sparseArray.get(PRIVATE_USER_UID))) {
					iterator.remove();
					break;
				}
			}
			sendEmptyMsg(MSG_UPDATE_USER_PHOTO);
		}
	}

	@Override
	public void onTi(String operatorUid, String operatorNickname, String tiUid, String tiNickname) {
		super.onTi(operatorUid, operatorNickname, tiUid, tiNickname);
		// 如果是自己被T了，则弹出Toast显示
		if (tiUid != null && tiUid.equals(UserInfoConfig.getInstance().id)) {
			showTips(String.format(getResources().getString(R.string.ti_room_2), operatorNickname));
			stopLivePlayer(false);
			/** 关闭消息流 */
			stopWebSocket();
		}
	}


	@Override
	public void onPublish(JSONObject data) {
		super.onPublish(data);
		mIsPlaying = true;
		mAnchorUnlineTv.setVisibility(View.INVISIBLE);

		String newPlayUrl = data.optString("videoPlayUrl");
		if (!TextUtils.isEmpty(newPlayUrl)) {
			mMediaPlayUrl = data.optString("videoPlayUrl");
			if (mLiveStreamDisConnect) {
				startLivePlayer(true);
			}
		}
	}

	@Override
	public void onUnPublish() {
		super.onUnPublish();
		mIsPlaying = false;
		EvtLog.e(TAG, "onUnPublish " + mIsPlaying);
		// 主播断流 状态更新到 未连接状态（连线中，断流会发送 onVideoChatEnd命令，再次不做小主播视频区操作）
		// 连线面板更新到未连接状态
		updateConnectUIStatus(CONNECT_LIVE_STATUS_NORMAL);
	}

	@Override
	public void onClose(int code, String errosMsg) {
		super.onClose(code, errosMsg);
		//websocket断开，连麦状态也全部恢复默认状态
		resumeVideoConnectStatus();
	}

	// 用户被邀请
	@Override
	public void onInviteVideoChat(InviteVideoChat inviteVideoChat) {
		super.onInviteVideoChat(inviteVideoChat);
		//1. 收到邀请，并且此时底部连线面板未打开，底部连线按钮显示动画，数字为1
		if (!mConnectLayout.isShown()) {
			mLiveConnectNum.setVisibility(View.VISIBLE);
			mLiveConenctAnim.setVisibility(View.VISIBLE);
			mLiveConnectIv.setVisibility(View.INVISIBLE);
			mLiveConnectIv.setSelected(false);
			mLiveConnectNum.setText("1");
		}
		// 2. 被对方邀请，连线面板更新为接收状态
		updateConnectUIStatus(CONNECT_LIVE_STATUS_RECEIVING);
	}

	// 被取消邀请连线
	@Override
	public void onCancelVideoChat(String uid) {
		super.onCancelVideoChat(uid);
		showTips(R.string.live_connect_cancel_tip);
		// 被对方取消，更新连线面板tag状态
		if (mConnectLayout.getTag(R.id.tag_connect_status) != null) {
			int status = Integer.parseInt(String.valueOf(mConnectLayout.getTag(R.id
					.tag_connect_status)));
			// 被邀请状态
			if (status == CONNECT_LIVE_STATUS_RECEIVING) {
				// 3. 被对方同意邀请后，置空邀请状态
				mConnectLayout.setTag(R.id.tag_connect_status, null);
			} else if (status == CONNECT_LIVE_STATUS_INVITING_RECEIVING) {
				// 邀请 + 接收状态
				// 3. 被对方同意邀请后，置空邀请状态
				mConnectLayout.setTag(R.id.tag_connect_status, CONNECT_LIVE_STATUS_INVITING);
			}
		}
		// 返回上一级状态
		connectLayoutGoBack();
	}

	/**
	 * 被主播接收  邀请
	 *
	 * @param data
	 */
	@Override
	public void onAcceptVideoChat(AcceptVideoChat data) {
		super.onAcceptVideoChat(data);
		// 1. 开始推流更新UI
		startPublishUpdateUI(data);
		// 2. 重新获取大主播拉流地址，并且小主播开始推流
		startPublishRtmp(data);
		if (mConnectLayout.getTag(R.id.tag_connect_status) != null) {
			int status = Integer.parseInt(String.valueOf(mConnectLayout.getTag(R.id
					.tag_connect_status)));
			// 邀请状态
			if (status == CONNECT_LIVE_STATUS_INVITING) {
				// 3. 被对方同意邀请后，置空邀请状态
				mConnectLayout.setTag(R.id.tag_connect_status, null);
			} else if (status == CONNECT_LIVE_STATUS_INVITING_RECEIVING) {
				// 邀请 + 接收状态
				// 3. 被对方同意邀请后，置空邀请状态
				mConnectLayout.setTag(R.id.tag_connect_status, CONNECT_LIVE_STATUS_RECEIVING);
			}
		}
	}


	//由于主播推流切换观众需要更换拉流地址
	@Override
	public void onChangeVideoPullUrl(String pullUrl) {
		super.onChangeVideoPullUrl(pullUrl);
		if (!TextUtils.isEmpty(pullUrl)) {
			// 如果不是连麦用户
			if (!mIsLiveConnecting) {
				// 新的拉流地址不为空，并且不等于之前的拉流地址
				if (!pullUrl.equals(mMediaPlayUrl)) {
					mMediaPlayUrl = pullUrl;
					stopLivePlayer(false);
					// 重新拉流，此方法有重置
					startLivePlayer(true);
				}
			}
		}
	}

	/**
	 * 连麦成功，只有普通用户需要处理
	 *
	 * @param data
	 */
	@Override
	public void onVideoChat(VideoChat data) {
		super.onVideoChat(data);
		// 如果不是连麦用户
		if (!mIsLiveConnecting) {
			mConnectLivingLayout.setVisibility(View.VISIBLE);
			mConnectLivingLayout.setTag(R.id.tag_second, data.getUid());
			mConnectLivingLayout.setBackgroundResource(R.color.trans);
			mConnectLivingClose.setVisibility(View.GONE);
			//如果是音频连麦
			if (data.getVideoChatType() == InviteVideoChat.INVITE_CHAT_TYPE_MIC) {
				mConnectAudioLivingLayout.setVisibility(View.VISIBLE);
				// 设置小主播 信息
				ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity,
						mConnectAudioLivingHeadPic, data.getHeadPic());
				mConnectAudioLivingName.setText(data.getNickname());
				ImageLoaderUtil.with().loadImage(mActivity, mConnectAudioLivingLevel, Utils
						.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, String.valueOf(data
								.getLevel())));
			}
		}
	}

	/**
	 * 被对方结束连麦
	 *
	 * @param uid
	 * @param mid
	 * @param endType 1小主播结束 2大主播结束 0混流失败
	 */
	@Override
	public void onVideoChatEnd(String uid, String mid, String pullUrl, String pushUrl, String
			msg, String endType) {
		//如果不是连麦用户
		if (!mUid.equals(uid)) {
			initConnectChat();
		} else {
			// 如果不是小主播结束直播
			if (!"1".equals(endType)) {
				super.onVideoChatEnd(uid, mid, pullUrl, pushUrl, msg, endType);
				// 1. 被对方结束连麦，UI更新
				stopPublishUpdateUi();
				// 2. 停止推流
				stopPublishRtmp();

				// 新的拉流地址不为空，并且不等于之前的拉流地址
				if (!TextUtils.isEmpty(pullUrl) && !pullUrl.equals(mMediaPlayUrl)) {
					mMediaPlayUrl = pullUrl;
					stopLivePlayer(false);
					// 重新拉流，此方法有重置
					startLivePlayer(false);
				}
			}
		}
	}

	@Override
	protected String getPersionDialogControlType() {
		if (mIsPrivatePlay) {
			return Constants.USER_TYPE_NORMAL;
		}
		return (String) mmRoomInfo.get("userType");
	}

	@Override
	protected void clickPersonInfoView(String type, String uid) {
		MobclickAgent.onEvent(FeizaoApp.mConctext, "personalPageInPersonalCard");
		Map<String, String> personInfo = new HashMap<String, String>();
		personInfo.put("id", uid);
		ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
	}

	/**
	 * 点击个人信息卡 关注按钮
	 */
	@Override
	protected void personInfoFocusOperate(boolean flag) {
		//如果已是关注
		if (flag) {
			mWebSocketImpl.sendCommand(WebSocketLiveEngine.USER_ATTENTION);
			mLiveFocusBtn.setVisibility(View.GONE);
		} else {
			mLiveFocusBtn.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void updateHotRankData(Map<String, String> data) {
		super.updateHotRankData(data);
		if (Utils.getBooleanFlag(mmRoomInfo.get("isHot")) && Utils.getBooleanFlag(mIsPlaying)) {
			mHotRankLayout.setVisibility(View.VISIBLE);
		} else {
			mHotRankLayout.setVisibility(View.GONE);
			mHotRankPopupWindow.dismiss();
		}
	}

	/**
	 * 点击弹幕背景
	 */
	@Override
	protected void clickDanmuGroup(JSONObject jsonObject) {
		super.clickDanmuGroup(jsonObject);
		//跳转直播间
		if (Constants.SYSTEM_MSG_TYPE_ROOM.equals(jsonObject.optString("jumpKey"))) {
			jumpLive(jsonObject.optString("rid"));
		} else if (Constants.SYSTEM_MSG_TYPE_PAGE.equals(jsonObject.optString("jumpKey"))) {
			jumpUrl(jsonObject.optString("url"));
		}
	}


	private void applyBlur(final ImageView view) {
		// 显示高斯模糊效果
		if (!TextUtils.isEmpty(mmIntentRoomInfo.get("headPic"))) {
			ImageLoaderUtil.with().loadImageTransformBlurTrans(mActivity, view, mmIntentRoomInfo
					.get("headPic"), R.drawable.live_load_blur, 0);
		} else {
			ImageLoaderUtil.with().loadImageTransformBlurTrans(mActivity, view, R.drawable
					.live_load_blur, R.drawable.live_load_blur, 0);
		}
	}

	/**
	 * 开始推流，更新UI
	 *
	 * @param data
	 */
	private void startPublishUpdateUI(AcceptVideoChat data) {
		// 1. 隐藏并 调整连麦面板UI，更新连线面板状态为 正在连线
		if (mConnectLayout.isShown()) {
			mConnectLayout.startAnimation(mDownOutAnimation);
			mConnectLayout.setVisibility(View.GONE);
		}
		updateConnectUIStatus(CONNECT_LIVE_STATUS_LIVING);
		//2、显示连麦窗口
		mConnectLivingLayout.setVisibility(View.VISIBLE);
		mConnectLivingLayout.setTag(R.id.tag_second, data.getUid());
		if (data.getVideoChatType() == InviteVideoChat.INVITE_CHAT_TYPE_VIDEO) {
			// 视频流
			mLiveConnectVideoView.setVisibility(View.VISIBLE);
		} else {
			//音频流
			mConnectAudioLivingLayout.setVisibility(View.VISIBLE);
			// 设置小主播 信息
			ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity,
					mConnectAudioLivingHeadPic, data.getHeadPic());
			mConnectAudioLivingName.setText(data.getNickname());
			ImageLoaderUtil.with().loadImage(mActivity, mConnectAudioLivingLevel, Utils
					.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, String.valueOf(data
							.getLevel())));
		}
	}

	/**
	 * 开始推流
	 */
	private void startPublishRtmp(AcceptVideoChat data) {
		// 3. 如果大主播的拉流地址发生变化，重新拉流
		if (!TextUtils.isEmpty(data.getPullUrl())) {
			String pullUrl = StringUtil.base64Decode(data.getPullUrl());
			if (!TextUtils.isEmpty(pullUrl) && !pullUrl.equals(mMediaPlayUrl)) {
				mMediaPlayUrl = pullUrl;
				EvtLog.e(TAG, "acceptVideoChat playUrl:" + mMediaPlayUrl);
				stopLivePlayer(false);
				// 重新拉流，此方法有重置
				startLivePlayer(true);
			}
		}
		// 4. 推流
		if (!TextUtils.isEmpty(data.getUserPushUrl())) {
			String publishUrl = StringUtil.base64Decode(data.getUserPushUrl());
			if (!publishUrl.equals(mPublishUrl)) {
				mLivePushType = data.getVideoChatType();
				mPublishUrl = publishUrl;
				EvtLog.e(TAG, "acceptVideoChat pushUrl:" + mMediaPlayUrl);
				// 初始化 推理相关
				startLivePushStream();
			}
		}
	}

	/**
	 * 结束连麦 或者 断流，UI更新
	 */
	private void stopPublishUpdateUi() {
		// 2. 隐藏底部连线面板，并且状态转为未连接
		if (mConnectLayout.isShown()) {
			mConnectLayout.startAnimation(mDownOutAnimation);
			mConnectLayout.setVisibility(View.GONE);
		}
		// 返回上一级状态
		connectLayoutGoBack();

	}

	/**
	 * 连线面板返回上一步状态
	 */
	private void connectLayoutGoBack() {
		// 邀请状态存在/被邀请状态存在，返回时，返回到 邀请状态
		if (mConnectLayout.getTag(R.id.tag_connect_status) != null) {
			int status = Integer.parseInt(String.valueOf(mConnectLayout.getTag(R.id.tag_connect_status)));
			if (status == CONNECT_LIVE_STATUS_RECEIVING) {
				updateConnectUIStatus(CONNECT_LIVE_STATUS_RECEIVING);
			} else if (status == CONNECT_LIVE_STATUS_INVITING) {
				updateConnectUIStatus(CONNECT_LIVE_STATUS_INVITING);
			}
		} else {
			updateConnectUIStatus(CONNECT_LIVE_STATUS_NORMAL);
		}
	}

	/**
	 * 恢复连麦状态，默认的状态
	 */
	private void resumeVideoConnectStatus() {
		// 1、小主播视频区恢复
		initConnectChat();
		//2、如果推流，则停止推流
		stopPublishRtmp();
		//3、连线面板也恢复到最初状态
		updateConnectUIStatus(CONNECT_LIVE_STATUS_NORMAL);
		//4、连麦状态变量恢复
		mIsLiveConnecting = false;
		mIsInvateConnecting = false;
	}

	/**
	 * 停止推流
	 */
	private void stopPublishRtmp() {
		// 3. 断开推流
		if (mLivePusher != null) {
			mLivePusher.stopCameraPreview(true);        //关闭摄像头预览
			mLivePusher.stopPusher();
		}
	}

	/**
	 * 播放完成了或者失败
	 * 显示主播正在休息也是播放完成后执行 by 2016/7/11
	 */
	private void playComplementOrFail() {
		mLiveStreamDisConnect = true;
		// 播放完成,如果主播下线了，提示“主播正在休息”
		if (mIsPlaying == false) {
			EvtLog.e(TAG, "MediaPlayerEndReached xxxx " + mIsPlaying);
			Message msg3 = new Message();
			msg3.what = ANCHOR_UNLINE;
			sendMsg(msg3);
		} else if (!mNoNetworkFlag)// 如果不是因为主播下线且有网络，2s后自动重连
		{
			if (mLiveAnchorOnLineStatus) {
				chatFragment.sendChatMsg(chatFragment.onSysMsg(mActivity.getResources().getString(R.string.live_anchor_go_away_tip)));
				mLiveAnchorOnLineStatus = false;
			}
			EvtLog.e(TAG, "disConnect 2s reconnect");
			delayStartLivePlayer();
		}
	}

	/**
	 * 初始化请求网络
	 */
	private void connectMediaPlay(String pullUrl) {
		if (mTxLivePlayer == null) {
			mTxLivePlayer = new TXLivePlayer(this);
			mTxLivePlayer.setPlayerView(mTxPlayVideoView);
			mTxLivePlayer.setPlayListener(this);
			mTxLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
			// 硬件加速在1080p解码场景下效果显著，但细节之处并不如想象的那么美好：
			// (1) 只有 4.3 以上android系统才支持
			// (2) 兼容性我们目前还仅过了小米华为等常见机型，故这里的返回值您先不要太当真
			//			mTxLivePlayer.enableHardwareDecode(isSupportHWEncode());
			//			mTxLivePlayer.setRenderRotation(mCurrentRenderRotation);
			//设置播放器缓存策略
			//这里将播放器的策略设置为自动调整，调整的范围设定为1到4s，您也可以通过setCacheTime将播放器策略设置为采用
			//固定缓存时间。如果您什么都不调用，播放器将采用默认的策略（默认策略为自动调整，调整范围为1到4s）
			//mLivePlayer.setCacheTime(5);
			mTxLivePlayConfig = new TXLivePlayConfig();
			//			mTxLivePlayConfig.setAutoAdjustCacheTime(true);
			//			mTxLivePlayConfig.setConnectRetryCount(1);
			mTxLivePlayer.setConfig(mTxLivePlayConfig);
		}
		if (!TextUtils.isEmpty(pullUrl)) {
			int playType;
			if (pullUrl.contains("txSecret=")) {
				playType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP_ACC;
				//开启回声消除：连麦时必须开启，非连麦时不要开启
				mTxLivePlayConfig.enableAEC(true);
				mTxLivePlayer.setConfig(mTxLivePlayConfig);
			} else {
				playType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
				mTxLivePlayConfig.enableAEC(false);
				mTxLivePlayer.setConfig(mTxLivePlayConfig);
			}
			int result = mTxLivePlayer.startPlay(pullUrl, playType); // result返回值：0 success;  -1 empty url; -2 invalid url; -3 invalid playType;
			EvtLog.e(TAG, "startPlay pullUrl " + pullUrl + " result:" + result);
		}
	}

	/**
	 * 更新视频渲染模式
	 */
	private void updateLivePlayRenderMode() {
		if (isPortraitVideo) {
			if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				mTxLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
			} else {
				mTxLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
			}
		} else {
			if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				mTxLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
			} else {
				mTxLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
			}
		}
	}

	/**
	 * 开始播放大主播视频流
	 *
	 * @param flag 是否显示加载框
	 */
	private void startLivePlayer(boolean flag) {
		EvtLog.d(TAG, "startLivePlayer");
		if (flag) {
			mPlayLoadingLayout.setVisibility(View.VISIBLE);
		}
		connectMediaPlay(mMediaPlayUrl);
		mLiveStreamDisConnect = false;
	}

	/**
	 * 结束大主播视频流
	 */
	private void stopLivePlayer(boolean mIsNeedClearLastImg) {
		mPlayLoadingLayout.setVisibility(View.GONE);
		if (mTxLivePlayer != null) {
			mTxLivePlayer.stopPlay(mIsNeedClearLastImg);
		}
	}

	private void destroyLivePlayer() {
		if (mTxLivePlayer != null) {
			mTxLivePlayer.setPlayListener(null);
			mTxLivePlayer.pause();
			mTxLivePlayer.stopPlay(true);
			mTxLivePlayer = null;
		}
	}

	@Override
	public void onPlayEvent(int event, Bundle param) {
		String msg = param.getString(TXLiveConstants.EVT_DESCRIPTION);
		EvtLog.e(TAG, "onPlayEvent event:" + event + " message:" + msg);
		if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
			Message msg2 = new Message();
			msg2.what = MsgTypes.ON_LOAD_VIDEO_SUCCESS;
			msg2.obj = 1;
			sendMsg(msg2);
		} else if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
			int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS);
			int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION);
		} else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT || event == TXLiveConstants.PLAY_EVT_PLAY_END) {
			playComplementOrFail();
		} else if (event == TXLiveConstants.PLAY_WARNING_RECONNECT) {

		} else if (event == TXLiveConstants.PLAY_ERR_GET_RTMP_ACC_URL_FAIL) {
			playComplementOrFail();
		}
	}

	/**
	 * 延时开始请求视频流
	 */
	private void delayStartLivePlayer() {
		postDelayed(new Runnable() {
			@Override
			public void run() {
				startLivePlayer(false);
			}
		}, 2000);
	}

	@Override
	public void onNetStatus(Bundle status) {
		Log.d(TAG, "ITXLivePlayListener Current status, CPU:" + status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE) + ", RES:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) + "*" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT) + ", SPD:" + status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED) + "Kbps" + ", FPS:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS) + ", ARA:" + status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps" + ", VRA:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps");
		if (status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) > status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT)) {
			if (isPortraitVideo) {
				isPortraitVideo = false;
				updateLivePlayRenderMode();
			}
		} else {
			if (!isPortraitVideo) {
				isPortraitVideo = true;
				updateLivePlayRenderMode();
			}
		}
	}

	private void setDelayShowFocus() {
		removeNeedFousHandler();
		this.sendEmptyMsgDelayed(MSG_SHOW_FOCUS, DELAY_SHOW_FOCUS);
	}


	private void removeNeedFousHandler() {
		this.mHandler.removeMessages(MSG_SHOW_FOCUS);
	}

	private void focusLiver() {
		MobclickAgent.onEvent(FeizaoApp.mConctext, "followBroadcasterInBixinBox");
		OperationHelper.onEvent(FeizaoApp.mConctext, "followBroadcasterInBixinBox", null);
		BusinessUtils.follow(mActivity, new FollowCallbackData(LiveMediaPlayerActivity.this), mmAnchorInfo.get("id"));
	}

	/**
	 * DELAY_SHOW_FOCUS时间后，提示关注主播
	 */
	private void showNeedFocusDialog() {
		mIsNeedFocus = SHOW_FOCUS_NOT;
		UiHelper.showLiveNeedFocusDialog(this, new OnClickListener() {
			@Override
			public void onClick(View v) {

				switch (v.getId()) {
					case R.id.tv_left:
						LiveMediaPlayerActivity.this.finish();
						break;
					case R.id.tv_right:
						focusLiver();
						break;
				}
			}
		});
	}

	/**
	 * 关注用户信息回调 Reason: TODO ADD REASON(可选). <br/>
	 */
	private static class FollowCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mAcivity;

		public FollowCallbackData(BaseFragmentActivity fragment) {
			mAcivity = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FollowCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.FOLLOW_SUCCESS;
					BaseFragmentActivity meFragment = mAcivity.get();
					// 如果fragment未回收，发送消息
					if (meFragment != null) meFragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.FOLLOW_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity meFragment = mAcivity.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null) meFragment.sendMsg(msg);
			}
		}
	}

	/**
	 * 拒绝连线
	 */
	private static class RejectVideoChatCallbackDataHandle implements CallbackDataHandle {
		private WeakReference<BaseFragmentActivity> mActivity;

		public RejectVideoChatCallbackDataHandle(BaseFragmentActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = Message.obtain();
			if (success) {
				msg.what = MSG_USER_REJECT_VIDEO_CHAT_SUCC;
			} else {
				msg.what = MSG_USER_REJECT_VIDEO_CHAT_FAIL;
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
