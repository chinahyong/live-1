package tv.live.bx.live.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import cn.efeizao.feizao.ui.dialog.LiveManagerCustomDialogBuilder;
import com.pili.pldroid.streaming.CameraStreamingManager;
import com.pili.pldroid.streaming.CameraStreamingSetting;
import com.pili.pldroid.streaming.StreamingProfile;
import com.pili.pldroid.streaming.widget.AspectFrameLayout;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.umeng.analytics.MobclickAgent;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;
import tv.live.bx.BuildConfig;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.library.util.DateUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.StringUtil;
import tv.live.bx.live.Config;
import tv.live.bx.live.gles.FBO;
import tv.live.bx.live.ui.RotateLayout;
import tv.live.bx.ui.popwindow.LiveFilterPopWindow;
import tv.live.bx.ui.popwindow.LiveMenuPopWindow;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.websocket.model.AcceptVideoChat;
import tv.live.bx.websocket.model.InviteVideoChat;

@SuppressLint("NewApi")
public class LiveCameraStreamActivity extends LiveBaseActivity implements ITXLivePushListener {
	private static final String GUIDE_LIVE = "guide_live";
	private static final int MSG_SET_ZOOM = 0x1002;
	private static final int MSG_MUTE = 0x1003;
	private static final int ZOOM_MINIMUM_WAIT_MILLIS = 33; // ms
	private static final int REMOVE_MANAGER_SUCC = 0x001;
	private static final int REMOVE_MANAGER_FAIL = -0x001;
	/**
	 * 直播时间显示间隔
	 */
	private static final int DELAY_TIME = 1000;
	/**
	 * 打榜时间倒计时
	 */
	private static final int MSG_BAND_TIME_COMEDOWN = 0x110101;
	/**
	 * 直播时间
	 */
	private TextView mLiveTimerTv;
	/**
	 * 切换摄像头
	 */
	private boolean mCurrentCameraDir = true; //true 前置 false 后置默认前置
	/**
	 * 底部
	 */
	private Button mLiveBottomList; // 列表

	/***
	 * 弹出框出显示的管理按钮个数
	 **/
	private int showItemsInLivePopWindow = 4;

	private LiveMenuPopWindow mLivePopWindow; // 底部弹框
	private Animation mAnimation180, mAnimation0;
	private LiveManagerCustomDialogBuilder mLiveManagerBuilder;

	/**
	 * filter popupWindow
	 */
	private LiveFilterPopWindow mfilterPopWindow;

	private Timer mLiveTimer;
	private long mStartLivingMills;
	/**
	 * 直播状态码
	 */
	private int mLiveStatusCode;
	private String mStatusMsgContent;

	/**
	 * 发送上线通知
	 */
	private boolean mSendOnLineNotify = true;

	/***
	 * 推流库相关变量
	 */
	private TXLivePushConfig mLivePushConfig;
	private TXLivePusher mLivePusher;
	private TXCloudVideoView mLivePushVideoView;
	private String mPushUrl;
	//是否已开启相机预览
	private boolean isStartCameraPreview;
	private Dialog mLivePushDialog;
	//推流结束类型
	public static final int PUSH_EVT_STOP = 0x1000;
	public static final int PUSH_EVT_STOP_PREVIEW = 0x1001;
	public static final int PUSH_EVT_STOP_PREVIEW_SHOW_DIALOG = 0x1002;

	/**
	 * 是否开启美颜，默认开启：true（如果!isSupportHWEncode() 则为false）
	 */
	public boolean mIsFilter = true;

	/**
	 * 是否开始推流
	 */
	private boolean isStartLiveStream = false;

	// 七牛推流库
	private StreamingProfile mProfile;
	private CameraStreamingSetting setting;
	protected CameraStreamingManager mCameraStreamingManager;
	private AspectFrameLayout afl;
	private GLSurfaceView mGLSurfaceView;
	private RotateLayout mRotateLayout;
	public Switcher mSwitcher = new Switcher();
	/**
	 * 美颜主要类
	 */
	private FBO mFBO;
	private boolean mIsReady = false;
	private int mCurrentZoom = 0;
	private int mMaxZoom = 0;
	private EncodingOrientationSwitcher mEncodingOrientationSwitcher = new EncodingOrientationSwitcher();

	/**
	 * 默认的直播清晰度
	 */
	public int mClarityType = Config.ENCODING_LEVEL_HEIGHT;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FeizaoApp.setIsLiveRunning(true);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_live_playing;
	}

	@Override
	protected void initMembers() {
		super.initMembers();
		mLiveTimerTv =  findViewById(R.id.live_status_time);
		mLiveBottomList =  findViewById(R.id.live_bottom_list);

		mLivePushVideoView =  findViewById(R.id.video_view);

		afl =  findViewById(R.id.cameraPreview_afl);
		afl.setShowMode(AspectFrameLayout.SHOW_MODE.FULL);

		mAnimation180 = AnimationUtils.loadAnimation(mActivity, R.anim.rotate_0_180_anim);
		mAnimation0 = AnimationUtils.loadAnimation(mActivity, R.anim.rotate_180_0_anim);

	}

	@Override
	public void initWidgets() {
		super.initWidgets();
		mLiveTimerTv.setVisibility(View.VISIBLE);
		// mCameraSwitchBtn.setVisibility(View.VISIBLE);
		mLiveBottomList.setVisibility(View.VISIBLE);
		mLiveShare.setVisibility(View.GONE);
		//		mLiveCreateFan.setVisibility(View.VISIBLE);
		// Utils.initEtClearView(moEtContent, moIvClearInput);
		// 设置文本输入监听
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

	}

	@Override
	protected void setEventsListeners() {
		super.setEventsListeners();
		mLiveBottomList.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			mClarityType = bundle.getInt("clarity_type", Config.ENCODING_LEVEL_HEIGHT);
			mCurrentCameraDir = bundle.getBoolean("camera_dir", true);
			mmIntentRoomInfo = (Map<String, String>) bundle.getSerializable(LiveBaseActivity
					.ANCHOR_RID);
			mIsPrivatePlay = Utils.strBool(mmIntentRoomInfo.get(LiveBaseActivity.ANCHOR_PRIVATE));
			mmRoomInfo = (Map<String, Object>) bundle.getSerializable(LiveBaseActivity.ROOM_INFO);
		}
		super.initData(savedInstanceState);
		mStartLivingMills = new Date(2000, 1, 1, 0, 0, 0).getTime();
		if (Utils.getBooleanFlag(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, GUIDE_LIVE,
				Constants.COMMON_TRUE))) {
			showFullDialog(R.layout.dialog_guide_live_layout, new DialogInterface
					.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					Utils.setCfg(mActivity, Constants.COMMON_SF_NAME, GUIDE_LIVE, "false");
				}
			});
		}
		mIsFilter = isSupportHWEncode();
		initQiLiveStream();
	}

	@Override
	public void onResume() {
		super.onResume();
		// 当前是TX推流
		if (mLivePusher != null && mLivePushVideoView.isShown()) {
			mLivePushVideoView.onResume();
			mLivePusher.resumePusher();
		} else if (mCameraStreamingManager != null) {
			mCameraStreamingManager.onResume();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mLivePusher != null) {
			mLivePushVideoView.onPause();
			mLivePusher.pausePusher();
		}

		mIsReady = false;
		if (mCameraStreamingManager != null) {
			new AsyncTask() {
				@Override
				protected Object doInBackground(Object[] params) {
					mCameraStreamingManager.pause();
					return null;
				}
			}.execute();
		}
	}

	@Override
	public void onDestroy() {
		if (mLiveTimer != null) {
			mLiveTimer.cancel();
			mLiveTimer = null;
		}
		mWebSocketImpl.sendCommand(Constants.VIDEO_UNPUBLISH);
		destroyLivePush();
		destroyLivePlayer();
		if (mCameraStreamingManager != null) {
			new AsyncTask() {
				@Override
				protected Object doInBackground(Object[] params) {
					mCameraStreamingManager.destroy();
					return null;
				}
			}.execute();
		}
		FeizaoApp.setIsLiveRunning(false);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mLiveBottomList.setBackgroundResource(R.drawable.btn_live_bottom_down_land_selector);
		} else {
			mLiveBottomList.setBackgroundResource(R.drawable.btn_live_bottom_down_selector);
		}
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
			case R.id.live_btn_exit:
				// 按返回键弹出对话框
				UiHelper.showConfirmDialog(this, R.string.live_exist, R.string.cancel, R.string
						.determine, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						MobclickAgent.onEvent(FeizaoApp.mContext, "exitLiveRoom");
						OperationHelper.onEvent(FeizaoApp.mContext, "exitLiveRoom", null);
						finish();
					}
				});
				break;
			case R.id.live_bottom_list:
				showMorePopWindow(v);
				break;
		}
	}

	/**
	 * 显示调整filter的popwindow
	 */
	private void showFilterPopWindow() {
		if (mfilterPopWindow == null) {
			mfilterPopWindow = new LiveFilterPopWindow(mActivity, UserInfoConfig.getInstance()
					.filterPercent);
			mfilterPopWindow.setOnProgressChange(new LiveFilterPopWindow.OnProgressChange() {
				@Override
				public void onChange(float progress) {
					mFBO.setIFilterPercent(progress);
					UserInfoConfig.getInstance().updateFilterPer(progress);
				}
			});
		}
		mfilterPopWindow.showAtLocation(getWindow().getDecorView().findViewById(android.R.id
				.content), Gravity.BOTTOM, 0, 0);
	}


	/**
	 * 显示更多下拉对话框
	 */
	private void showMorePopWindow(View v) {
		if (mLivePopWindow == null) {
			mLivePopWindow = new LiveMenuPopWindow(mActivity, R.layout.pop_live_bottom_list_layou);
			showItemsInLivePopWindow += extraLivePopWindowManager(mLivePopWindow);
			LiveItemListener mMoreItemListener = new LiveItemListener();
			mLivePopWindow.setOnClickListener(R.id.live_bottom_item_manager, mMoreItemListener);
			mLivePopWindow.setOnClickListener(R.id.live_bottom_item_beauty, mMoreItemListener);
			mLivePopWindow.setOnClickListener(R.id.live_bottom_item_switch_caption,
					mMoreItemListener);
			//			mLivePopWindow.setOnClickListener(R.id.live_bottom_item_screen,
			// mMoreItemListener);
			mLivePopWindow.setOnClickListener(R.id.live_bottom_item_share, mMoreItemListener);
		}
		mLiveBottomList.startAnimation(mAnimation180);
		if (!mLivePopWindow.isShowing()) {
			int[] location = new int[2];
			v.getLocationOnScreen(location);
			if (mCurrentScreenType == Configuration.ORIENTATION_PORTRAIT) {
				mLivePopWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1] - v
						.getHeight() * showItemsInLivePopWindow - Utils.dip2px(mActivity, 40));
			} else {
				mLivePopWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0] - v.getWidth() *
						showItemsInLivePopWindow - Utils.dip2px(mActivity, 40), location[1]);
			}
		}
		mLivePopWindow.setBeauty(mLivePusher == null || mIsFilter);
		mLivePopWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				mLiveBottomList.startAnimation(mAnimation0);
			}
		});
	}

	/**
	 * 返回增加或减少显示items的数目
	 *
	 * @param window
	 * @return
	 */
	protected int extraLivePopWindowManager(LiveMenuPopWindow window) {
		if (mIsPrivatePlay) {
			window.setVisible(R.id.live_bottom_item_manager, View.GONE);
			window.setVisible(R.id.live_bottom_item_share, View.GONE);
			return -2;
		}
		return 0;
	}


	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MsgTypes.MSG_LIVE_STATUS:
				Bundle data = msg.getData();
				int status = data.getInt("msgStatus");
				mLiveStatusCode = status;
				if (!TextUtils.isEmpty(data.getString("msgContent"))) {
					mLiveTimerTv.setText(data.getString("msgContent"));
				}
				// 推流断连
				if (TXLiveConstants.PUSH_ERR_NET_DISCONNECT == status || CameraStreamingManager
						.STATE.DISCONNECTED == status) {
					stopLivePush(PUSH_EVT_STOP_PREVIEW_SHOW_DIALOG);
				} else if ((status == TXLiveConstants.PUSH_EVT_PUSH_BEGIN || status ==
						CameraStreamingManager.STATE.STREAMING) && mLiveTimer == null) {
					OperationHelper.onEvent(FeizaoApp.mContext, "liveSuccessful", null);
					mLiveTimerTv.setText(DateUtil.sdf3.format(new Date(mStartLivingMills)));
					mLiveTimer = new Timer();
					mLiveTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							if (mLiveStatusCode != TXLiveConstants.PUSH_ERR_NET_DISCONNECT &&
									mLiveStatusCode != CameraStreamingManager.STATE.SHUTDOWN &&
									mLiveStatusCode != CameraStreamingManager.STATE.IOERROR) {
								sendEmptyMsg(MsgTypes.MSG_UPDATE_LIVE_TIME);
							}
						}
					}, DELAY_TIME, DELAY_TIME);
				}
				break;
			case MsgTypes.MSG_UPDATE_LIVE_TIME:
				mStartLivingMills = mStartLivingMills + 1000;
				mLiveTimerTv.setText(DateUtil.sdf3.format(new Date(mStartLivingMills)));
				break;
			// 获取到推送信息
			case MsgTypes.MSG_GET_LIVESTREAM_SUCCESS:
				String liveInfo = (String) msg.obj;
				if (mLivePusher != null) {
					// TX调用了获取URL
					startLivePush(liveInfo);
				} else if (mCameraStreamingManager != null) {
					// 七牛调用了获取URL
					startQiLiveStream(liveInfo);
				}
				break;
			case MsgTypes.MSG_GET_LIVESTREAM_FAILED:
				showToast((String) msg.obj, TOAST_LONG);
				break;
			case MSG_SET_ZOOM:
				mCameraStreamingManager.setZoomValue(mCurrentZoom);
				break;
			case MSG_MUTE:
				// mCameraStreamingManager.mute(mIsNeedMute);
				break;
			case REMOVE_MANAGER_SUCC:
				if (msg.obj != null) {
					String uid = String.valueOf(msg.obj);
					// 移除adapter适配的item，移除后重置
					if (mLiveManagerBuilder != null && mLiveManagerBuilder.isShowing()) {
						// 此处使用msg传过来的下标，避免同时点击造成数据覆盖
						mLiveManagerBuilder.updateAdapterData(uid);
					}
				}
				dismissProgressDialog();
				break;
			case REMOVE_MANAGER_FAIL:
				dismissProgressDialog();
				// 取消管理员失败
				if (msg.obj != null) {
					showTips(String.valueOf(msg.obj));
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void initRoomData() {
		super.initRoomData();
	}

	/**
	 * TODO 简单描述该方法的实现功能（可选）.
	 *
	 * @see tv.live.bx.live.activities.LiveBaseActivity#networkRecovery()
	 */
	@Override
	protected void networkRecovery() {
		super.networkRecovery();
		/** 如果已经开始播放，且视频流断开 */
		if (mLiveStatusCode == TXLiveConstants.PUSH_ERR_NET_DISCONNECT || mLiveStatusCode ==
				CameraStreamingManager.STATE.SHUTDOWN || mLiveStatusCode == CameraStreamingManager
				.STATE.DISCONNECTED) {
			requestLiveParam();
		}

	}

	@Override
	public void onConnectStatus() {
		super.onConnectStatus();
		anchorDownLine();
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
			if (!mUid.equals(piUid)) {
				SparseArray<String> sparseArray = new SparseArray<>();
				sparseArray.put(PRIVATE_USER_UID, piUid);
				sparseArray.put(PRIVATE_USER_USERNAME, psNickname);
				sparseArray.put(PRIVATE_USER_PHOTO, psPhoto);
				mUserPhotoArray.add(sparseArray);
				sendEmptyMsg(MSG_UPDATE_USER_PHOTO);
			}
		}
	}

	@Override
	public void delUser(String piUid, String piType, String psNickname, String psPhoto, String
			ban, String cid) {
		super.delUser(piUid, piType, psNickname, psPhoto, ban, cid);
		if (mIsPrivatePlay) {
			if (!mUid.equals(piUid)) {
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
	}

	@Override
	public void onTiModerator(String msg) {
		UiHelper.showSingleConfirmDialog(mActivity, msg, new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		anchorDownLine();
	}

	@Override
	public void onOpen() {
		super.onOpen();
		moderatorOnLine();
	}

	@Override
	public void onClose(int code, String errosMsg) {
		super.onClose(code, errosMsg);
	}

	@Override
	protected String getPersionDialogControlType() {
		if (mIsPrivatePlay) {
			return Constants.USER_TYPE_NORMAL;
		}
		return Constants.USER_TYPE_ANCHOR;
	}

	@Override
	protected boolean getConnectFlag() {
		// 如果是横屏，都不显示连线按钮
		return mCurrentScreenType != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
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
		}
	}

	@Override
	protected void clickPersonInfoView(String type, String uid) {
		//如果是私播
		if (mIsPrivatePlay) {
			MobclickAgent.onEvent(FeizaoApp.mContext, "personalPageInPersonalCard");
			Map<String, String> personInfo = new HashMap<String, String>();
			personInfo.put("id", uid);
			ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
		} else {
			if (Constants.USER_TYPE_OFFICIAL_ADMIN.equals(type) || Constants.USER_TYPE_ADMIN
					.equals(type)) {
				MobclickAgent.onEvent(FeizaoApp.mContext, "cancelManagerByBroadcaster");
				BusinessUtils.removeRoomManager(mActivity, new CallbackDataHandle() {
					@Override
					public void onCallback(boolean success, String errorCode, String errorMsg,
										   Object result) {
						if (!success) {
							showTips(errorMsg);
						}
					}
				}, uid);
			} else {
				MobclickAgent.onEvent(FeizaoApp.mContext, "managerByBroadcaster");
				BusinessUtils.setRoomManager(mActivity, new CallbackDataHandle() {
					@Override
					public void onCallback(boolean success, String errorCode, String errorMsg,
										   Object result) {
						if (!success) {
							showTips(errorMsg);
						}
					}
				}, uid);
			}
		}
	}

	@Override
	protected void requestVideoChat(String uid) {
		RequestVideoChatCallbackDataHandle callbackDataHandle = new
				RequestVideoChatCallbackDataHandle(LiveCameraStreamActivity.this);
		callbackDataHandle.setUid(uid);
		BusinessUtils.requestVideoChat(mActivity, callbackDataHandle, mmIntentRoomInfo.get("rid")
				, InviteVideoChat.INVITE_TYPE_ANCHOR, uid, String.valueOf(InviteVideoChat
						.INVITE_CHAT_TYPE_VIDEO));
	}

	@Override
	protected void updateHotRankData(final Map<String, String> data) {
		super.updateHotRankData(data);
		if (Utils.getBooleanFlag(mmRoomInfo.get("isHot"))) {
			mHotRankLayout.setVisibility(View.VISIBLE);
		} else {
			mHotRankLayout.setVisibility(View.GONE);
			mHotRankPopupWindow.dismiss();
		}
	}

	/**
	 * 主播上线，发布上线消息
	 */

	private void moderatorOnLine() {
		if (mSendOnLineNotify) {
			mSendOnLineNotify = false;
			mWebSocketImpl.sendOnLine(false);
		} else {
			mWebSocketImpl.sendOnLine(true);
		}
	}

	/**
	 * 切换摄像头
	 */
	public void switchCaption() {
		//		if (mLivePusher.isPushing()) {
		mLivePusher.switchCamera();
		// 每切换一次摄像头，则该值取反
		mCurrentCameraDir = !mCurrentCameraDir;
		// 前置开启镜像，后置关闭
		mLivePusher.setMirror(mCurrentCameraDir);
		//		} else {
		//			mLivePushConfig.setFrontCamera(mFrontCamera);
		//		}
	}

	/**
	 * 是否滤镜
	 */
	public void switchBeautyFilter(boolean filter) {
		if (filter) {
//			mLivePusher.setBeautyFilter(7, 3);
		} else {
//			mLivePusher.setBeautyFilter(0, 0);
		}
	}

	/**
	 * 主播下线
	 *
	 * @since JDK 1.6
	 */
	private void anchorDownLine() {
		//设置停止直播
		mWebSocketImpl.sendCommand(Constants.VIDEO_UNPUBLISH);
		stopWebSocket();
		stopLivePush(PUSH_EVT_STOP_PREVIEW);
	}

	/**
	 * 初始化流相关参数，信息
	 */
	private void initLiveStream() {
		changePreview(true);
		EvtLog.i(TAG, "initLiveStream");
		if (mLivePusher == null) {
			mLivePusher = new TXLivePusher(this);
			mLivePushConfig = new TXLivePushConfig();
			//		mLivePushConfig.setWatermark(mBitmap, 10, 10);
			//		mLivePushConfig.setCustomModeType(customModeType);
			mLivePushConfig.setPauseImg(60 * 60 * 10, 10);
			Bitmap bitmap = decodeResource(getResources(), R.drawable.pause_publish);
			mLivePushConfig.setPauseImg(bitmap);
			mLivePushConfig.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants
					.PAUSE_FLAG_PAUSE_AUDIO);

			mLivePushConfig.setFrontCamera(mCurrentCameraDir);
			if (mIsFilter) {
//				mLivePushConfig.setBeautyFilter(5, 3);
			} else {
//				mLivePushConfig.setBeautyFilter(0, 0);
			}
			//		if (!isSupportHWEncode()) {
			mLivePushConfig.setHardwareAcceleration(TXLiveConstants.ENCODE_VIDEO_SOFTWARE);
			//		} else {
			//			mLivePushConfig.setHardwareAcceleration(TXLiveConstants.ENCODE_VIDEO_AUTO);
			//		}
			mLivePushConfig.enableNearestIP(false);
			mLivePusher.setExposureCompensation(-0.1f);
			mLivePusher.setConfig(mLivePushConfig);

			mLivePusher.setVideoQuality(mClarityType, false, false);

			mLivePusher.setPushListener(this);
			mLivePusher.startCameraPreview(mLivePushVideoView);
			// 设置默认滤镜：美白
			Bitmap bmp = decodeResource(getResources(), R.drawable.filter_fennen);
			if (mLivePusher != null) {
				mLivePusher.setFilter(bmp);
			}
			// 镜像录制,前置开启镜像，后置关闭
			mLivePusher.setMirror(mCurrentCameraDir);
			mLivePushVideoView.disableLog(true);
			isStartCameraPreview = true;
//			int[] ver = TXLivePusher.getSDKVersion();
//			if (ver != null && ver.length >= 4) {
//				EvtLog.e(TAG, String.format("rtmp sdk version:%d.%d.%d.%d ", ver[0], ver[1],
//						ver[2], ver[3]));
//			}
		}
	}

	/**
	 * 初始化流相关参数，信息
	 */
	private void initQiLiveStream() {
		changePreview(false);
		// 配置 七牛
		if (mProfile == null) {
			mProfile = new StreamingProfile();
			mProfile.setEncoderRCMode(StreamingProfile.EncoderRCModes.QUALITY_PRIORITY)
					.setDnsManager(null).setSendingBufferProfile(new StreamingProfile
					.SendingBufferProfile(0.2f, 0.8f, 3.0f, 10 * 1000));
			if (mClarityType == Config.ENCODING_LEVEL_STADART) {
				mProfile.setVideoQuality(StreamingProfile.VIDEO_QUALITY_MEDIUM2).setAudioQuality
						(StreamingProfile.AUDIO_QUALITY_HIGH1).setEncodingSizeLevel(Config
						.ENCODING_LEVEL_STADART);
			} else if (mClarityType == Config.ENCODING_LEVEL_HEIGHT) {
				mProfile.setVideoQuality(StreamingProfile.VIDEO_QUALITY_MEDIUM3).setAudioQuality
						(StreamingProfile.AUDIO_QUALITY_HIGH1).setEncodingSizeLevel(Config
						.ENCODING_LEVEL_HEIGHT);
			} else {
				mProfile.setVideoQuality(StreamingProfile.VIDEO_QUALITY_HIGH1).setAudioQuality
						(StreamingProfile.AUDIO_QUALITY_HIGH1).setEncodingSizeLevel(Config
						.ENCODING_LEVEL_SUPER);
			}
		}
		if (setting == null) {
			setting = new CameraStreamingSetting();
			setting.setCameraId(mCurrentCameraDir ? Camera.CameraInfo.CAMERA_FACING_FRONT :
					Camera.CameraInfo.CAMERA_FACING_BACK).setContinuousFocusModeEnabled(true)
					.setRecordingHint(false).setResetTouchFocusDelayInMs(3000).setFocusMode
					(CameraStreamingSetting.FOCUS_MODE_CONTINUOUS_PICTURE).setCameraPrvSizeLevel
					(CameraStreamingSetting.PREVIEW_SIZE_LEVEL.MEDIUM).setCameraPrvSizeRatio
					(CameraStreamingSetting.PREVIEW_SIZE_RATIO.RATIO_16_9);
		}
		if (mCameraStreamingManager == null) {
			// 创建surafceview
			mGLSurfaceView = new GLSurfaceView(mActivity);
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams
					.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
			lp.gravity = Gravity.CENTER;
			afl.addView(mGLSurfaceView, 0, lp);
			// afl.setAspectRatio(90);
			// 如果支持硬编码，优先硬编码
			if (!isSupportHWEncode()) {
				mCameraStreamingManager = new CameraStreamingManager(this, afl, mGLSurfaceView,
						CameraStreamingManager.EncodingType.SW_VIDEO_WITH_SW_AUDIO_CODEC);
			} else {
				mCameraStreamingManager = new CameraStreamingManager(this, afl, mGLSurfaceView,
						CameraStreamingManager.EncodingType.HW_VIDEO_WITH_HW_AUDIO_CODEC);
			}
			// update the StreamingProfile
			mCameraStreamingManager.prepare(setting, mProfile);
			// mProfile.setStream(new Stream2345(mJSONObject1));
			// mCameraStreamingManager.setStreamingProfile(mProfile);
			// 初始化监听器
			mCameraStreamingManager.setStreamingStateListener(new StreamingStateListener());
			mCameraStreamingManager.setStreamingPreviewCallback(new StreamingPreviewCallback());
			mCameraStreamingManager.setSurfaceTextureCallback(new SurfaceTextureCallback());
			mCameraStreamingManager.setStreamingSessionListener(new StreamingSessionListener());
			mCameraStreamingManager.setStreamStatusCallback(new StreamStatusCallback());

			mCameraStreamingManager.setNativeLoggingEnabled(BuildConfig.DEBUG);
		}
	}

	/**
	 * 大主播开始直播推流
	 */
	private void startLivePush(String pushUrl) {
		try {
			//如果正在推流且推流地址一致，不重新推流
			if (mLivePusher.isPushing() && pushUrl.equals(this.mPushUrl)) return;
			this.mPushUrl = pushUrl;
			//rtmp:\/\/guojiang.wslive.cibnlive.com\/live\/U0NKAkSCn6zpbCwq_1074755_590C4122_955
			//"rtmp:\/\/rtmppush.efeizao.com\/live\/room_246960\/chat
			// ?sid=&uid=2677080&rid=246960&timestamp=1489991373&sign
			// =a401184b7cd22537492e239a50f8595a"

			mLivePusher.setConfig(mLivePushConfig);
			if (!isStartCameraPreview) {
				isStartCameraPreview = true;
				//				mLivePushVideoView.setVisibility(View.VISIBLE);
				mLivePusher.startCameraPreview(mLivePushVideoView);
			}
			int result = mLivePusher.startPusher(mPushUrl);
			EvtLog.e(TAG, "startLivePush pushUrl：" + this.mPushUrl + " result:" + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 开始直播
	 */
	private void startQiLiveStream(String pushUrl) {
		try {
			//如果正在推流且推流地址一致，不重新推流
			//			if (this.mPushUrl != pushUrl) {
			this.mPushUrl = pushUrl;
			int index = pushUrl.lastIndexOf("?");
			String tempRtmpUrl;
			StringBuilder sb = new StringBuilder();
			if (index != -1) {
				tempRtmpUrl = pushUrl.substring(7, index);
				sb.append(pushUrl.substring(index + 1));
			} else {
				tempRtmpUrl = pushUrl.substring(7);
				sb.append("timestamp=").append(System.currentTimeMillis());
			}

			String title = tempRtmpUrl.substring(tempRtmpUrl.lastIndexOf("/") + 1);
			String temp2 = tempRtmpUrl.substring(0, tempRtmpUrl.lastIndexOf("/"));
			String hub = temp2.substring(temp2.lastIndexOf("/") + 1);
			String ip = temp2.substring(0, temp2.lastIndexOf("/"));
			String streamJsonStrFromServer = String.format(Constants
					.StreamJsonStrFromServer_URL_NEW, hub, title, sb.toString(), ip);
			JSONObject jsonObject = new JSONObject(streamJsonStrFromServer);
			StreamingProfile.Stream stream = new StreamingProfile.Stream(jsonObject);
			mProfile.setStream(stream);
			mCameraStreamingManager.setStreamingProfile(mProfile);
			//			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					// disable the shutter button before startStreaming
					boolean res = mCameraStreamingManager.startStreaming();
					EvtLog.i(TAG, "res:" + res);
				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 大主播停止直播推流
	 *
	 * @param stopFlag 停止推流标志，{@link #PUSH_EVT_STOP,#PUSH_EVT_STOP_PREVIEW,#PUSH_EVT_STOP_PREVIEW_SHOW_DIALOG}
	 */
	private void stopLivePush(int stopFlag) {
		// #PUSH_EVT_STOP,#PUSH_EVT_STOP_PREVIEW,#PUSH_EVT_STOP_PREVIEW_SHOW_DIALOG
		// 3中状态都执行此方法
		stopLivePush();
		switch (stopFlag) {
			case PUSH_EVT_STOP:
				break;
			case PUSH_EVT_STOP_PREVIEW:
				chatFragment.sendChatMsg(chatFragment.onSysMsg(mActivity.getResources().getString
						(R.string.live_error)));
				break;
			case PUSH_EVT_STOP_PREVIEW_SHOW_DIALOG:
				chatFragment.sendChatMsg(chatFragment.onSysMsg(mActivity.getResources().getString
						(R.string.live_error)));
				if (mLivePushDialog == null || !mLivePushDialog.isShowing()) {
					mLivePushDialog = UiHelper.showConfirmDialog(LiveCameraStreamActivity.this, R
							.string.live_dialog_error_tip, R.string.live_restart, R.string
							.live_quit, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							requestLiveParam();
						}
					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
				}
				break;
		}
		if (mLivePusher != null) {
			mLivePusher.stopPusher();
		}
	}

	/**
	 * 停止推流
	 */
	private void stopLivePush() {
		isStartCameraPreview = false;
		if (mLivePusher != null) {
			mLivePusher.stopCameraPreview(false);
		}
		if (mCameraStreamingManager != null) {
			mCameraStreamingManager.stopStreaming();
		}
		if (mLiveTimer != null) {
			mLiveTimer.cancel();
			mLiveTimer = null;
		}
	}

	/**
	 * 大主播停止推流，并销毁推流相关信息
	 */
	private void destroyLivePush() {
		if (mLivePusher != null) {
			mLivePusher.stopCameraPreview(true);
			mLivePusher.stopPusher();
			mLivePusher.setPushListener(null);
			mLivePusher = null;
		}
	}

	/**
	 * 切换推流预览画面
	 *
	 * @param txFlag
	 */
	private void changePreview(boolean txFlag) {
		if (txFlag) {
			mLivePushVideoView.setVisibility(View.VISIBLE);
			afl.setVisibility(View.GONE);
			if (mGLSurfaceView != null) {
				mGLSurfaceView.setVisibility(View.GONE);
			}
		} else {
			mLivePushVideoView.setVisibility(View.GONE);
			afl.setVisibility(View.VISIBLE);
			if (mGLSurfaceView != null) {
				mGLSurfaceView.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 七牛切换到TX
	 *
	 * @param acceptVideoChat
	 */
	private void changeQiniuToTx(AcceptVideoChat acceptVideoChat) {
		// 停止七牛推流
		if (mCameraStreamingManager != null) {
			mCameraStreamingManager.onPause();
			mCameraStreamingManager.stopStreaming();
			mCameraStreamingManager.destroy();
			mCameraStreamingManager = null;
			afl.removeView(mGLSurfaceView);
			mGLSurfaceView = null;
			//			try {
			//				mCameraStreamingManager.onPause();
			//			} catch (Exception e) {
			//				e.printStackTrace();
			//			}
		}
		// 初始化TX推流
		initLiveStream();
		//连麦必须使用这个类型
		mLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_LINKMIC_MAIN_PUBLISHER, false,
				false);
		// 开始TX推流
		startLivePush(mPushUrl);
	}

	/**
	 * TX 切换 到七牛
	 */
	private void changeTXToQiNiu(final String pushUrl) {
		//结束连麦必须切换回之前的类型
		if (mLivePusher != null) {
			mLivePusher.setVideoQuality(mClarityType, false, false);
		}
		destroyLivePlayer();
		// 停止TX 推流
		stopLivePush(PUSH_EVT_STOP);
		destroyLivePush();
		isStartLiveStream = false;
		postDelayed(new Runnable() {
			@Override
			public void run() {
				// 替换推流库
				String newPushUrl = StringUtil.base64Decode(pushUrl);
				mPushUrl = newPushUrl;
				// 替换预览界面
				initQiLiveStream();
				mCameraStreamingManager.onResume();
				//				startQiLiveStream(newPushUrl);
			}
		}, 1000);

		//		changePreview(false);
		//		//开始推流
		//		try {
		//			mCameraStreamingManager.onResume();
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
	}

	/**
	 * 获取推流的相关信息,30s就失效，每次推流之前都需要重新请求
	 */
	private void requestLiveParam() {
		if (mLivePushDialog != null && mLivePushDialog.isShowing()) {
			mLivePushDialog.dismiss();
		}
		int pushVideoWidth = 0;
		int pushVideoHeight = 0;
		if (mClarityType == Config.ENCODING_LEVEL_STADART) {
			pushVideoWidth = 368;
			pushVideoHeight = 656;
		} else if (mClarityType == Config.ENCODING_LEVEL_HEIGHT) {
			pushVideoWidth = 544;
			pushVideoHeight = 960;
		} else {
			pushVideoWidth = 720;
			pushVideoHeight = 1280;
		}
		BusinessUtils.getLiveStreamInfo(mActivity, new GetLiveStreamCallbackData
						(LiveCameraStreamActivity.this), mmIntentRoomInfo.get("rid"), pushVideoWidth,
				pushVideoHeight);
	}


	@Override
	public void onSetAdmin(String operatorUid, String operatorNickname, String setAdminUid,
						   String setAdminNickname) {
		super.onSetAdmin(operatorUid, operatorNickname, setAdminUid, setAdminNickname);
	}

	@Override
	public void onUnsetAdmin(String operatorUid, String operatorNickname, String setAdminUid,
							 String setAdminNickname) {
		super.onUnsetAdmin(operatorUid, operatorNickname, setAdminUid, setAdminNickname);
	}

	/**
	 * 主播操作按钮监听
	 */
	private class LiveItemListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.live_bottom_item_manager:
					mLiveManagerBuilder = new LiveManagerCustomDialogBuilder(mActivity, mUid);
					mLiveManagerBuilder.showDialog();
					break;
				// 美颜开关
				case R.id.live_bottom_item_beauty:
					// TX美颜切换
					if (mLivePusher != null) {
						mIsFilter = !mIsFilter;
						if (mIsFilter) {
							MobclickAgent.onEvent(FeizaoApp.mContext, "openBeautyByBroadcaster");
						} else {
							MobclickAgent.onEvent(FeizaoApp.mContext, "closeBeautyByBroadcaster");
						}
						switchBeautyFilter(mIsFilter);
					} else {
						showFilterPopWindow();
					}
					break;
				// 前后摄像头切换
				case R.id.live_bottom_item_switch_caption:
					MobclickAgent.onEvent(FeizaoApp.mContext, "switchCameraInLiveRoom");
					if (mLivePusher != null) {
						switchCaption();
					}
					if (mCameraStreamingManager != null) {
						switchQiCaption();
					}
					break;
				case R.id.live_bottom_item_share:
					MobclickAgent.onEvent(FeizaoApp.mContext, "shareByBroadcaster");
					toShareLiveInfo();
					break;
			}
		}
	}

	@Override
	public void onPushEvent(int event, Bundle param) {
		String msgParam = param.getString(TXLiveConstants.EVT_DESCRIPTION);
		EvtLog.e(TAG, "onPushEvent msg " + msgParam + " event:" + event);
		String mStatusMsgContent = null;
		switch (event) {
			case TXLiveConstants.PUSH_ERR_NET_DISCONNECT:
				mStatusMsgContent = getString(R.string.string_state_ready);
				break;
			case TXLiveConstants.PUSH_WARNING_NET_BUSY:
				//				showTips(R.string.live_push_warning_net_busy);
				break;
			case TXLiveConstants.PUSH_EVT_OPEN_CAMERA_SUCC:
				if (!isStartLiveStream && !mNoNetworkFlag) requestLiveParam();
				break;
			case TXLiveConstants.PUSH_EVT_CONNECT_SUCC:
				break;
			case TXLiveConstants.PLAY_EVT_RTMP_STREAM_BEGIN:
				mStatusMsgContent = getString(R.string.string_state_connecting);
				break;
			case TXLiveConstants.PUSH_EVT_PUSH_BEGIN:
				isStartLiveStream = true;
				mStatusMsgContent = getString(R.string.string_state_streaming);
				break;
		}
		//错误还是要明确的报一下
		//		if (event < 0) {
		//			showTips(param.getString(TXLiveConstants.EVT_DESCRIPTION));
		//			if (event == TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL) {
		//				stopLivePush();
		//			}
		//		}
		Message msg = Message.obtain();
		msg.what = MsgTypes.MSG_LIVE_STATUS;
		Bundle bundle = new Bundle();
		bundle.putString("msgContent", mStatusMsgContent);
		bundle.putInt("msgStatus", event);
		msg.setData(bundle);
		sendMsg(msg);
	}

	@Override
	public void onNetStatus(Bundle status) {
		Log.d(TAG, "Current status, CPU:" + status.getString(TXLiveConstants
				.NET_STATUS_CPU_USAGE) + ", RES:" + status.getInt(TXLiveConstants
				.NET_STATUS_VIDEO_WIDTH) + "*" + status.getInt(TXLiveConstants
				.NET_STATUS_VIDEO_HEIGHT) + ", SPD:" + status.getInt(TXLiveConstants
				.NET_STATUS_NET_SPEED) + "Kbps" + ", FPS:" + status.getInt(TXLiveConstants
				.NET_STATUS_VIDEO_FPS) + ", ARA:" + status.getInt(TXLiveConstants
				.NET_STATUS_AUDIO_BITRATE) + "Kbps" + ", VRA:" + status.getInt(TXLiveConstants
				.NET_STATUS_VIDEO_BITRATE) + "Kbps");
	}

	/**
	 * 销毁连麦视频播放器
	 */
	private void destroyLivePlayer() {
		if (mLivePushDialog != null && mLivePushDialog.isShowing()) {
			mLivePushDialog.dismiss();
		}
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		EvtLog.e(TAG, "onSingleTapUp X:" + e.getX() + ",Y:" + e.getY());
		if (mIsReady && mCameraStreamingManager != null) {
			setFocusAreaIndicator();
			mCameraStreamingManager.doSingleTapUp((int) e.getX(), (int) e.getY());
			return true;
		}
		return false;
	}

	@Override
	public boolean onZoomValueChanged(float factor) {
		if (mIsReady && mCameraStreamingManager != null && mCameraStreamingManager.isZoomSupported()) {
			mCurrentZoom = (int) (mMaxZoom * factor);
			mCurrentZoom = Math.min(mCurrentZoom, mMaxZoom);
			mCurrentZoom = Math.max(0, mCurrentZoom);

			EvtLog.e(TAG, "zoom ongoing, scale: " + mCurrentZoom + ",factor:" + factor + ",maxZoom:" + mMaxZoom);
			if (!mHandler.hasMessages(MSG_SET_ZOOM)) {
				mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ZOOM), ZOOM_MINIMUM_WAIT_MILLIS);
				return true;
			}
		}
		return false;
	}

	private void setFocusAreaIndicator() {
		if (mRotateLayout == null) {
			mRotateLayout = (RotateLayout) findViewById(R.id.focus_indicator_rotate_layout);
			mCameraStreamingManager.setFocusAreaIndicator(mRotateLayout, mRotateLayout.findViewById(R.id.focus_indicator));
		}
	}

	/**
	 * 七牛 切换摄像头
	 */
	public void switchQiCaption() {
		if (mSwitcher == null)
			// 如果为空，初始化
			mSwitcher = new Switcher();
		else
			// 如果不为空，直接移除
			mHandler.removeCallbacks(mSwitcher);
		mHandler.postDelayed(mSwitcher, 100);
	}

	/**
	 * 七牛 切换屏幕,重写此方法
	 */
	@Override
	protected void switchSrceen() {
		if (mEncodingOrientationSwitcher == null)
			// 如果为空，初始化
			mEncodingOrientationSwitcher = new EncodingOrientationSwitcher();
		else
			// 如果不为空，直接移除
			mHandler.removeCallbacks(mEncodingOrientationSwitcher);
		mHandler.post(mEncodingOrientationSwitcher);
	}

	/**
	 * 七牛 推流状态
	 */
	private class StreamingStateListener implements CameraStreamingManager.StreamingStateListener {

		@Override
		public void onStateChanged(int state, Object extra) {
			EvtLog.i(TAG, "onStateChanged state:" + state);
			mLiveStatusCode = state;
			switch (state) {
				case CameraStreamingManager.STATE.PREPARING:
					mStatusMsgContent = getString(R.string.string_state_preparing);
					break;
				case CameraStreamingManager.STATE.READY:
					mIsReady = true;
					mMaxZoom = mCameraStreamingManager.getMaxZoom();
					mStatusMsgContent = getString(R.string.string_state_ready);
					if (!isStartLiveStream && !mNoNetworkFlag) {
						if (TextUtils.isEmpty(mPushUrl)) {
							requestLiveParam();
						} else {
							Message msg = Message.obtain();
							msg.what = MsgTypes.MSG_GET_LIVESTREAM_SUCCESS;
							msg.obj = mPushUrl;
							sendMsg(msg);
						}
					}
					break;
				case CameraStreamingManager.STATE.CONNECTING:
					mStatusMsgContent = getString(R.string.string_state_connecting);
					break;
				case CameraStreamingManager.STATE.STREAMING:
					isStartLiveStream = true;
					mStatusMsgContent = getString(R.string.string_state_streaming);
					// setShutterButtonEnabled(true);
					break;
				case CameraStreamingManager.STATE.SHUTDOWN:
					isStartLiveStream = false;
					mStatusMsgContent = getString(R.string.string_state_ready);
					// setShutterButtonEnabled(true);
					// setShutterButtonPressed(false);
					break;
				case CameraStreamingManager.STATE.IOERROR:
					mStatusMsgContent = getString(R.string.string_state_ready);
					// setShutterButtonEnabled(true);
					break;
				case CameraStreamingManager.STATE.NETBLOCKING:
					mStatusMsgContent = getString(R.string.string_state_netblocking);
					break;
				case CameraStreamingManager.STATE.CONNECTION_TIMEOUT:
					mStatusMsgContent = getString(R.string.string_state_con_timeout);
					break;
				case CameraStreamingManager.STATE.UNKNOWN:
					mStatusMsgContent = getString(R.string.string_state_ready);
					break;
				case CameraStreamingManager.STATE.SENDING_BUFFER_EMPTY:
					break;
				case CameraStreamingManager.STATE.SENDING_BUFFER_FULL:
					break;
				case CameraStreamingManager.STATE.AUDIO_RECORDING_FAIL:
					break;
				case CameraStreamingManager.STATE.CAMERA_SWITCHED:
					if (extra != null) {
						EvtLog.i(TAG, "onStateChanged current camera id:" + (int) extra);
					}
					EvtLog.i(TAG, "onStateChanged camera switched");
					break;
				case CameraStreamingManager.STATE.DISCONNECTED:
					mStatusMsgContent = getString(R.string.string_state_ready);
					break;
				case CameraStreamingManager.STATE.TORCH_INFO:
					if (extra != null) {
						boolean isSupportedTorch = (Boolean) extra;
						Log.i(TAG, "onStateChanged isSupportedTorch=" + isSupportedTorch);
					}
					break;
			}
			Message msg = Message.obtain();
			msg.what = MsgTypes.MSG_LIVE_STATUS;
			Bundle bundle = new Bundle();
			bundle.putString("msgContent", mStatusMsgContent);
			bundle.putInt("msgStatus", state);
			msg.setData(bundle);
			sendMsg(msg);
		}

		@Override
		public boolean onStateHandled(int state, Object extra) {
			EvtLog.e(TAG, "onStateHandled state:" + state);
			switch (state) {
				case CameraStreamingManager.STATE.SENDING_BUFFER_HAS_FEW_ITEMS:
					mProfile.improveVideoQuality(1);
					mCameraStreamingManager.notifyProfileChanged(mProfile);
					return true;
				case CameraStreamingManager.STATE.SENDING_BUFFER_HAS_MANY_ITEMS:
					mProfile.reduceVideoQuality(1);
					mCameraStreamingManager.notifyProfileChanged(mProfile);
					return true;
			}
			return false;
		}
	}

	/**
	 * 七牛 推流状态
	 */
	private class StreamStatusCallback implements com.pili.pldroid.streaming.StreamStatusCallback {

		@Override
		public void notifyStreamStatusChanged(StreamingProfile.StreamStatus streamStatus) {
			EvtLog.d(TAG, "bitrate:" + streamStatus.totalAVBitrate / 1024 + " kbps" + "audio:" + streamStatus.audioFps + " fps" + ",video:" + streamStatus.videoFps + " fps-" + streamStatus.videoBitrate / 1024 + " kbps");
		}
	}

	/**
	 * 七牛 推流session
	 */
	private class StreamingSessionListener implements CameraStreamingManager.StreamingSessionListener {

		@Override
		public boolean onRecordAudioFailedHandled(int i) {
			mCameraStreamingManager.updateEncodingType(CameraStreamingManager.EncodingType.SW_VIDEO_CODEC);
			mCameraStreamingManager.startStreaming();
			return true;
		}

		@Override
		public boolean onRestartStreamingHandled(int i) {
			return mCameraStreamingManager.startStreaming();
		}

		@Override
		public Camera.Size onPreviewSizeSelected(List<Camera.Size> list) {
			return null;
		}
	}

	/**
	 * 七牛 推流预览
	 */
	private class StreamingPreviewCallback implements com.pili.pldroid.streaming.StreamingPreviewCallback {

		@Override
		public void onPreviewFrame(byte[] bytes, Camera camera) {

		}

		@Override
		public boolean onPreviewFrame(byte[] bytes, int i, int i1) {
			return true;
		}
	}

	/**
	 * 七牛 推流surface
	 */
	private class SurfaceTextureCallback implements com.pili.pldroid.streaming.SurfaceTextureCallback {

		@Override
		public void onSurfaceCreated() {
			if (mFBO == null) {
				mFBO = new FBO();
			}
			mFBO.initialize(LiveCameraStreamActivity.this, mGLSurfaceView);
		}

		@Override
		public void onSurfaceChanged(int width, int height) {
			EvtLog.i(TAG, "onSurfaceChanged width:" + width + ",height:" + height);
			if (mFBO != null) {
				mFBO.updateSurfaceSize(width, height);
			}
		}

		@Override
		public void onSurfaceDestroyed() {
			if (mFBO != null) {
				mFBO.release();
			}
		}

		@Override
		public int onDrawFrame(int texId, int texWidth, int texHeight) {
			//			if (!mIsFilter) {
			//				return texId;
			//			} else {
			try {
				int newTexId = mFBO.drawFrame(texId, texWidth, texHeight);
				return newTexId;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return texId;
			//			}
		}
	}

	/**
	 * 七牛 切换摄像头
	 */
	private class Switcher implements Runnable {
		@Override
		public void run() {
			if (mCameraStreamingManager != null) mCameraStreamingManager.switchCamera();
		}
	}

	/**
	 * 七牛 横竖屏切换
	 */
	private class EncodingOrientationSwitcher implements Runnable {
		@Override
		public void run() {
			if (mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				mProfile.setEncodingOrientation(StreamingProfile.ENCODING_ORIENTATION.PORT);
				mCameraStreamingManager.setStreamingProfile(mProfile);
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else {
				mProfile.setEncodingOrientation(StreamingProfile.ENCODING_ORIENTATION.LAND);
				mCameraStreamingManager.setStreamingProfile(mProfile);
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			mCameraStreamingManager.notifyActivityOrientationChanged();
		}
	}

	/**
	 * 获取直播流信息
	 */
	private static class GetLiveStreamCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mAcivity;

		public GetLiveStreamCallbackData(BaseFragmentActivity fragment) {
			mAcivity = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetLiveStreamCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			try {
				if (success) {
					msg.what = MsgTypes.MSG_GET_LIVESTREAM_SUCCESS;
					JSONObject jsonObject = new JSONObject(StringUtil.base64Decode(((JSONObject) result).getString("encrypted")));
					msg.obj = jsonObject.getString("pushAddr");
				} else {
					msg.what = MsgTypes.MSG_GET_LIVESTREAM_FAILED;
					msg.obj = errorMsg;
				}
			} catch (Exception e) {
				e.printStackTrace();
				msg.what = MsgTypes.MSG_GET_LIVESTREAM_FAILED;
				msg.obj = "推流地址解析错误";
			}
			BaseFragmentActivity baseFragmentActivity = mAcivity.get();
			// 如果fragment未回收，发送消息
			if (baseFragmentActivity != null) baseFragmentActivity.sendMsg(msg);
		}
	}


}