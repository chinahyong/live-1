package tv.live.bx.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.lonzh.lib.network.JSONParser;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.LiveTagListAdapter;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.common.WebConstants;
import tv.live.bx.config.AppConfig;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.BitmapUtility;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.live.Config;
import tv.live.bx.live.activities.LiveBaseActivity;
import tv.live.bx.receiver.ConnectionChangeReceiver;
import tv.live.bx.receiver.ConnectionChangeReceiver.NetwrokChangeCallback;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;

public class PreviewLivePlayActivity extends ShareDialogActivity
		implements View.OnClickListener, ITXLivePushListener {
	private static final int MSG_CAMERA_SWITCHED = 0x001;
	private static final int MSG_CAMERA_PREPARING = 0x002;
	private static final int MSG_OPEN_CAMERA_FAIL = -0x002;
	public static final int REQUEST_CODE_SETTING = 0x003;
	public static final int REQUEST_CODE_TOPIC = 100;
	public static final int REQUEST_CODE_TAG = 101;

	private TXLivePushConfig mLivePushConfig;
	private TXLivePusher mLivePusher;
	private TXCloudVideoView mLiveVideoView;

	private ImageView mLiveQuit, mIvCameraChange;
	private RadioGroup mRgShare, mRgScreenOriention;
	private TextView mTvHotTopic, mTvLocation, mTvSelectTag, mTvTag, mTvLiveProtocal;  //选择热门话题  位置信息
	private LinearLayout mLlTag;
	private EditText mEtTopicAndTitle;
	private ToggleButton mLocationSwitch;
	private double longitude, lantitude;        // 经/纬度
	private RelativeLayout mScreenOrientionLayout;
	private int mScreenOriention = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;        //横屏0，竖屏1,默认横屏

	/* 录屏开播 */
	private ImageView mIvRootBg;            //录屏时，背景图
	private boolean mIsRecordLiveFlag = false;    // 是否为录播开播
	/* 开始直播按钮距底部距离 */
	private int mBtnLoginBottomHeight = -1;
	/* 软键盘相关 */
	protected InputMethodManager mInputManager;
	private View.OnClickListener mLiveClickListener = new LiveOnClicklistener();
	/* 开始直播按钮 */
	private Button mStartLiveBtn;

	/* 网络监听广播 */
	private ConnectionChangeReceiver networkReceiver;

	private AlertDialog mProgress;
	/* 房间数据 */
	private Map<String, Object> mmRoomInfo;
	private Map<String, String> mmAnchorInfo;

	private RadioGroup mLiveScreenLayout;
	private RadioGroup mLiveClarityLayout;
	/* 目前屏幕的类型，默认竖屏 */
	public int mCurrentScreenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	public boolean mCurrentCameraDir = true; // true：前置 false：后置 默认前置
	/* 默认选择的流畅度 */
	public int mClarityType = Config.ENCODING_LEVEL_HEIGHT;

	private ImageView mLiveLogoImageView;
	private Button mLiveLogoEditBtn;

	/* 相机、录音是否异常 */
	private boolean cameraErrorFlag = false;
	private boolean audioErrorFlag = false;
	private AMapLocationClient mLocationClient;        //定位对象

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_preview_play;
	}

	@Override
	protected void initMembers() {
		EvtLog.d(TAG, "oncreate");
		mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mLiveQuit = (ImageView) findViewById(R.id.playing_btn_back);
		mIvCameraChange = (ImageView) findViewById(R.id.playing_btn_camera_change);
		mTvHotTopic = (TextView) findViewById(R.id.live_tv_hot_topic);
		mLlTag = (LinearLayout) findViewById(R.id.live_ll_tag);
		mTvSelectTag = (TextView) findViewById(R.id.live_tv_select_tag);
		mTvTag = (TextView) findViewById(R.id.live_tv_tag);

		mEtTopicAndTitle = (EditText) findViewById(R.id.live_topic_title);
		mLocationSwitch = (ToggleButton) findViewById(R.id.playing_btn_location_change);
		mTvLocation = (TextView) findViewById(R.id.playing_tv_location_city);
		mIvRootBg = (ImageView) findViewById(R.id.preview_back_bg);
		mTvLiveProtocal = (TextView) findViewById(R.id.live_agree_protocal);
		mRgScreenOriention = (RadioGroup) findViewById(R.id.live_screen_rg_oriention);
		mScreenOrientionLayout = (RelativeLayout) findViewById(R.id.live_screen_oriention);

		// afl.setAspectRatio(90);
		mLiveScreenLayout = (RadioGroup) findViewById(R.id.live_screen_layout);
		mLiveClarityLayout = (RadioGroup) findViewById(R.id.live_clarity_layout);
		mRgShare = (RadioGroup) findViewById(R.id.fragment_share_rg);
		mLiveVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
		mLiveLogoImageView = (ImageView) findViewById(R.id.live_logo);
		mLiveLogoEditBtn = (Button) findViewById(R.id.live_logo_edit);
	}

	@Override
	public void initWidgets() {
		// Utils.initEtClearView(moEtContent, moIvClearInput);
		// 设置文本输入监听
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		initLiveTip();
		initCheckChartiy();
		// 开启定位
		initLocation();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void setEventsListeners() {
		mStartLiveBtn.setOnClickListener(mLiveClickListener);
		mLiveQuit.setOnClickListener(this);
		mIvCameraChange.setOnClickListener(this);
		mLiveLogoEditBtn.setOnClickListener(this);
		mTvHotTopic.setOnClickListener(this);
		keyBoardChangedListener();
		mLlTag.setOnClickListener(this);
		mTvLiveProtocal.setOnClickListener(this);
		mRgScreenOriention.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.live_screen_rb_land:
						mScreenOriention = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
						break;
					case R.id.live_screen_rb_portrait:
						mScreenOriention = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
						break;
				}
			}
		});
		// 定位回调
		mLocationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// 开启位置
				if (isChecked) {
					MobclickAgent.onEvent(FeizaoApp.mConctext, "clickOpenLocation");
					// 如果未开启gps或network，跳转到gps设置
					if (!Utils.isOPenLocation(mActivity)) {
						ActivityJumpUtil.toLocationSettingActivity(mActivity, PreviewLivePlayActivity.REQUEST_CODE_SETTING);
					} else {
						// 已开启gps或network
						initLocation();
					}
				} else {
					MobclickAgent.onEvent(FeizaoApp.mConctext, "clickCloseLocation");
					// 关闭位置
					mTvLocation.setText("");
					if (mLocationClient != null) {
						mLocationClient.stopLocation();
					}
				}
			}
		});
		// 视频质量选择
		mLiveClarityLayout.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// 获取变更后的选中项的ID
				int radioButtonId = arg0.getCheckedRadioButtonId();
				if (radioButtonId == R.id.live_clarity_standart) {
					mClarityType = Config.ENCODING_LEVEL_STADART;
				} else if (radioButtonId == R.id.live_clarity_hight) {
					mClarityType = Config.ENCODING_LEVEL_HEIGHT;
				} else {
					mClarityType = Config.ENCODING_LEVEL_SUPER;
				}
				AppConfig.getInstance().updateClarityType(mClarityType);

			}
		});
		// 绑定一个匿名监听器，横竖屏转换
		mLiveScreenLayout.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// 获取变更后的选中项的ID
				int radioButtonId = arg0.getCheckedRadioButtonId();
				if (radioButtonId == R.id.live_screen_portrait) {
					mCurrentScreenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				} else {
					mCurrentScreenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				}
			}
		});
		// 标题焦点
		mEtTopicAndTitle.requestFocus();
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		mmAnchorInfo = (Map<String, String>) getIntent().getSerializableExtra(LiveBaseActivity.ANCHOR_RID);
		mIsRecordLiveFlag = Boolean.parseBoolean(mmAnchorInfo.get("isRecordLive"));
//		ImageLoader.getInstance().displayImage(mmAnchorInfo.get("logo"), mLiveLogoImageView,
//				Constants.COMMON_OPTIONIMAGE_2);
		// 上次开播提交标签进行本地保存，之后在本地拿取
		if (!TextUtils.isEmpty(AppConfig.getInstance().tag)) {
			for (HashMap<String, Object> tag :
					AppConfig.getInstance().moderatorTags) {
				if (tag.get("id").equals(AppConfig.getInstance().tag)) {
					mTvTag.setVisibility(View.VISIBLE);
					mTvSelectTag.setVisibility(View.GONE);
					mTvTag.setText(String.valueOf(tag.get("name")));
					mTvTag.setTag(AppConfig.getInstance().tag);
				}
			}
		}

		// 如果是竖屏，选中竖屏，否则为默认 横屏，不做处理
		if (AppConfig.getInstance().record_screen_oriention == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			((RadioButton) mRgScreenOriention.getChildAt(1)).setChecked(true);
		}
		// 录屏直播
		if (mIsRecordLiveFlag) {
			mIvRootBg.setVisibility(View.VISIBLE);
			ImageLoaderUtil.with().loadImage(mActivity, mIvRootBg, R.drawable.ic_bg_record_part);
			mIvCameraChange.setVisibility(View.GONE);
			mScreenOrientionLayout.setVisibility(View.VISIBLE);
			mLiveVideoView.setVisibility(View.GONE);
		} else {
			// 普通直播
			mIvRootBg.setVisibility(View.GONE);
			mIvCameraChange.setVisibility(View.VISIBLE);
			mScreenOrientionLayout.setVisibility(View.GONE);
			mLiveVideoView.setVisibility(View.VISIBLE);
			// 初始化流
			initLiveStream();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void finish() {
		overridePendingTransition(R.anim.translate_enter, R.anim.translate_exit);
		super.finish();
	}

	@Override
	protected void onDestroy() {
		mHandler.removeCallbacksAndMessages(null);
		dismissProgressDialog();
		destoryPusher();
		destroyLocation();
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * 开启定位
	 */
	private void initLocation() {
		if (mLocationClient != null) {
			mLocationClient.startLocation();
			return;
		}
		mTvLocation.setText(R.string.location_loading);
		mLocationClient = new AMapLocationClient(FeizaoApp.mConctext);
		mLocationClient.setLocationOption(initMapOption());
		mLocationClient.setLocationListener(new AMapLocationListener() {
			@Override
			public void onLocationChanged(AMapLocation aMapLocation) {
				// 获取位置信息成功
				if (aMapLocation.getErrorCode() == 0) {
					mLocationSwitch.setChecked(true);
					mTvLocation.setText(aMapLocation.getCity());
					longitude = aMapLocation.getLongitude();
					lantitude = aMapLocation.getLatitude();
				} else if (aMapLocation.getErrorCode() == 12) {
					// 未开启定位权限
					mLocationSwitch.setChecked(false);
					mTvLocation.setText("");
				} else {
					// 定位失败：外星人
					mLocationSwitch.setChecked(true);
					mTvLocation.setText(R.string.location_default);
				}
			}
		});
		mLocationClient.startLocation();
	}

	/**
	 * 初始化高德地图定位参数
	 */
	private AMapLocationClientOption initMapOption() {
		AMapLocationClientOption locationOption = new AMapLocationClientOption();
		locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);// 高精度定位
		locationOption.setOnceLocation(true);        // 是否单次定位  true：定位1次
		locationOption.setOnceLocationLatest(false);        // 是否获取3秒内最精确定位，false	关闭，定位到城市用不到
		locationOption.setNeedAddress(true);        //是否返回位置信息(此处说的是地址信息)，false关闭，true打开(城市、国家等信息)
		locationOption.setWifiActiveScan(false); //设置是否强制刷新WIFI，默认为强制刷新。每次定位主动刷新WIFI模块会提升WIFI定位精度，但相应的会多付出一些电量消耗。
		//单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
		locationOption.setHttpTimeOut(20000);
		return locationOption;
	}

	/**
	 * 销毁定位
	 */
	private void destroyLocation() {
		if (mLocationClient != null) {
			mLocationClient.onDestroy();        //销毁定位服务，要使用需重新实例化
			mLocationClient = null;
		}
	}

	private void destoryPusher() {
		if (mLivePusher != null) {
			EvtLog.i(TAG, "destoryPusher");
			mLivePusher.stopCameraPreview(true);
			mLivePusher.stopPusher();
			mLivePusher.setPushListener(null);
			mLivePusher = null;
		}
	}

	/**
	 * 更新选图数据
	 */
	private void updatePickData(String imagePath) {
		try {
			if (!imagePath.startsWith(Constants.FILE_PXI)) {
				imagePath = Constants.FILE_PXI + imagePath;
			}
			ImageLoaderUtil.with().loadImageTransformRoundedCorners(mActivity, mLiveLogoImageView, imagePath, Constants.COMMON_DISPLAY_IMAGE_CORNER_2);
			mProgress = Utils.showProgress(mActivity);
			BusinessUtils.editRoomLogo(mActivity, new EditRoomLogoCallbackData(), mmAnchorInfo.get("rid"),
					BitmapUtility.getFilePathFromUri(mActivity, Uri.parse(imagePath)));
		} catch (Exception e) {
			showTips("缩放图片失败");
			EvtLog.e(TAG, e.toString());
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			// 未开始退出直播
			case R.id.playing_btn_back:
				onBackPressed();
				break;
//			case R.id.live_logo_edit:
//				mPhotoSelectImpl.startPhotoPickActivity();
//				break;
			case R.id.playing_btn_camera_change:
				mCurrentCameraDir = !mCurrentCameraDir;
				mLivePusher.switchCamera();
				break;
			case R.id.live_tv_hot_topic:
				gotoActivityForResult(HotTopicActivity.class, REQUEST_CODE_TOPIC, null, null);
				break;
			case R.id.live_ll_tag:
				gotoActivityForResult(LiveTagActivity.class, REQUEST_CODE_TAG, "id", String.valueOf(mTvTag.getTag()));
				break;
			case R.id.live_agree_protocal:
				Map<String, String> webInfo = new HashMap<>();
				webInfo.put(WebViewActivity.URL, WebConstants.getFullWebMDomain(WebConstants.LIVE_PROTOCAL_WEB_URL));
				webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(true));
				ActivityJumpUtil.gotoActivity(PreviewLivePlayActivity.this, WebViewActivity.class, false, WebViewActivity.WEB_INFO,
						(Serializable) webInfo);
				break;
			default:
				break;
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.GET_ROOM_INFO_SUCCESS:
				dismissProgressDialog();
				mmRoomInfo = (Map<String, Object>) msg.obj;
				ImageLoaderUtil.with().loadImageTransformRoundedCorners(mActivity.getApplicationContext(), mLiveLogoImageView, (String) mmRoomInfo.get("logo"), Constants.COMMON_DISPLAY_IMAGE_CORNER_2);
				initShareInfo();
				setBtnLiveEnable(true);
				break;
			case MsgTypes.GET_ROOM_INFO_FAILED:
				dismissProgressDialog();
				showToast((String) msg.obj, TOAST_LONG);
				break;

			case MsgTypes.MSG_EIDT_ROOM_LOGO_SUCCESS:
				dismissProgressDialog();
				UiHelper.showShortToast(this, "上传封面成功");
				break;
			case MsgTypes.MSG_EIDT_ROOM_LOGO_FAILED:
				dismissProgressDialog();
				UiHelper.showToast(this, (String) msg.obj);
				break;
			case MsgTypes.MSG_EIDT_ROOM_TITLE_SUCCESS:
				// 成功开播时，保存开播时选择的 tag/屏幕方向
				AppConfig.getInstance().updatePlayingSetting(mScreenOriention, String.valueOf(mTvTag.getTag()));
				// 提交开播主题成功，直接开播
//				startLivePush();
				break;
			case MsgTypes.MSG_EIDT_ROOM_TITLE_FAILED:
				setBtnLiveEnable(true);
				UiHelper.showToast(this, (String) msg.obj);
				break;
			case MSG_CAMERA_SWITCHED:
				break;
			case MSG_CAMERA_PREPARING:
				mLiveVideoView.setBackgroundResource(R.color.trans);
				break;
			case MSG_OPEN_CAMERA_FAIL:
				mLiveVideoView.setBackgroundResource(R.color.black);
				break;
			default:
				break;
		}
	}

	/**
	 * 初始化分享数据
	 */
	private void initShareInfo() {
		try {
			Map<String, String> moderator = JSONParser.parseOne((String) mmRoomInfo.get("moderator"));
			if (!TextUtils.isEmpty(String.valueOf(mmRoomInfo.get("shareTitle")))) {
				shareTitle = String.valueOf(mmRoomInfo.get("shareTitle"));
				shareContent = String.valueOf(mmRoomInfo.get("shareContent"));
				shareUrl = String.valueOf(mmRoomInfo.get("shareUrl"));
				shareUrImg = String.valueOf(mmRoomInfo.get("sharePic"));
			} else {
				shareTitle = LiveBaseActivity.SHARE_TITLE;
				shareContent = LiveBaseActivity.SHARE_CONTENT.replace(LiveBaseActivity.SHARE_XXX,
						moderator.get("true_name"));
				shareUrImg = (String) mmRoomInfo.get("logo");
				shareUrl = WebConstants.getFullWebMDomain(WebConstants.SHARE_LIVE_PIX) + mmAnchorInfo.get("rid");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭对话框
	 */
	private void dismissProgressDialog() {
		if (mProgress != null && mProgress.isShowing())
			mProgress.dismiss();
	}

	private void initCheckChartiy() {
		mClarityType = AppConfig.getInstance().clarity_type;
		if (mClarityType == Config.ENCODING_LEVEL_STADART) {
			mLiveClarityLayout.check(R.id.live_clarity_standart);
		} else if (mClarityType == Config.ENCODING_LEVEL_SUPER) {
			mLiveClarityLayout.check(R.id.live_clarity_super);
		} else {
			mClarityType = Config.ENCODING_LEVEL_HEIGHT;
			mLiveClarityLayout.check(R.id.live_clarity_hight);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
				mInputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
		return super.onTouchEvent(event);
	}

	// 友盟统计
	public void onResume() {
		super.onResume();
		//设置开始直播可点击(未收到分享结果回调的情况下需要执行)
		setBtnLiveEnable(true);
		// 注册网络监听广播
		registerReceiver();
		try {
			if (mLivePusher != null) {
				mLivePusher.resumePusher();
			}
		} catch (Exception e) {
			e.toString();
		}

	}

	public void onPause() {
		super.onPause();
		// 注销网络监听广播
		unregisterReceiver();
		//surfaceView 放于最顶(修复，出现准备开播页面透明)
		//glSurfaceView.setZOrderOnTop(true);
		if (mLivePusher != null) {
			mLivePusher.pausePusher();
		}
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
				if (mmRoomInfo == null) {
					requestData();
				}
			}

			@Override
			public void noConnected() {
				EvtLog.e(TAG, "ConnectionChangeReceiver noConnected");
				showToast(Constants.NETWORK_FAIL, TOAST_LONG);
			}

			@Override
			public void gprsConnected() {
				if (mmRoomInfo == null) {
					requestData();
				}
			}
		});
		this.registerReceiver(networkReceiver, filter);
	}

	private void requestData() {
		// 房间信息
		mProgress = Utils.showProgress(PreviewLivePlayActivity.this);
		BusinessUtils.getRoomInfo(PreviewLivePlayActivity.this, new GetRoomCallbackData(
				PreviewLivePlayActivity.this), mmAnchorInfo.get("rid"));
	}

	/**
	 * 注销广播
	 */
	private void unregisterReceiver() {
		if (networkReceiver != null)
			this.unregisterReceiver(networkReceiver);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** umeng授权、分享需要重写 */
		if (requestCode == REQUEST_CODE_TOPIC) {
			if (resultCode == RESULT_OK) {
				String text = data.getStringExtra("topic");
				int focusIndex = mEtTopicAndTitle.getSelectionStart();
				Editable edit = mEtTopicAndTitle.getEditableText();
				if (focusIndex < 0 || focusIndex >= edit.length())
					edit.append(text);
				else
					edit.insert(focusIndex, text);      //光标所在位置插入文本
			}
		} else if (requestCode == REQUEST_CODE_SETTING) {
			// GPS开启页面设置完成回调
			// 打开了定位：打开开关，开始定位
			if (Utils.isOPenLocation(mActivity)) {
				mLocationSwitch.setChecked(true);
				mTvLocation.setText(R.string.location_loading);
				initLocation();
			} else {
				// 未打开定位：关闭开关
				mLocationSwitch.setChecked(false);
			}
		} else if (requestCode == REQUEST_CODE_TAG) {
			// 选择TAG
			if (resultCode == RESULT_OK) {
				if (data != null) {
					Map<String, Object> tagMap = (Map<String, Object>) data.getSerializableExtra("tag");
					// 点击保存，且有选择标签
					String tag = String.valueOf(tagMap.get(LiveTagListAdapter.TITLE));
					String id = String.valueOf(tagMap.get(LiveTagListAdapter.ID));
					mTvTag.setText(tag);
					mTvTag.setTag(id);
					mTvTag.setVisibility(View.VISIBLE);
					mTvSelectTag.setVisibility(View.GONE);
				} else {
					// 点击保存但未选择标签
					mTvTag.setTag(null);
					mTvTag.setVisibility(View.GONE);
					mTvSelectTag.setVisibility(View.VISIBLE);
				}
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
			Message msg = Message.obtain();
			try {
				if (success) {
					msg.what = MsgTypes.GET_ROOM_INFO_SUCCESS;
					Map<String, Object> map = JSONParser.parseMultiInSingle((JSONObject) result, new String[]{"gifts", "guardGifts",
							"packageItemsets"});
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
			if (baseFragmentActivity != null)
				baseFragmentActivity.sendMsg(msg);

		}
	}

	/**
	 * 直播提示按钮响应事件
	 */
	class LiveOnClicklistener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btn_live:
					OperationHelper.onEvent(FeizaoApp.mConctext, "clickLiveButtonInPrepareLivingPage", null);
					/**
					 * 判断是否有开播封面上传
					 * @version 2.4.0
					 * @update 取消开播封面上传
					 */
					// if (mLiveLogoImageView.getDrawable() == null) {
					// showTips(R.string.live_toast_logo_is_null);
					// return;
					// }
					if (cameraErrorFlag) {
						showCameraPermission();
						return;
					}
					if (audioErrorFlag) {
						showAudioPermission();
						return;
					}
					setBtnLiveEnable(false);
					/**
					 * 未填写主播标题，需给服务器传递空数据(后台需要)
					 */
					String location = "";
					// 定位开关打开，并且定位成功(显示内容不是外星人)
					if (mLocationSwitch.isChecked() && !mTvLocation.getText().toString().equals(getString(R.string.location_default))) {
						location = lantitude + "," + longitude;
					}
					BusinessUtils.editRoomTitle(mActivity, new EditRoomTitleCallbackData(), mmAnchorInfo.get("rid"),
							mEtTopicAndTitle.getText().toString().trim(), location, String.valueOf(mTvTag.getTag()));
					//分享
					share(mRgShare.getCheckedRadioButtonId());
					break;
				default:
					break;
			}
		}
	}

	private void share(int checkedId) {
		switch (checkedId) {
			case R.id.fragment_share_rb_weixin:
				onWeiXinClick();
				break;
			case R.id.fragment_share_rb_pengyouyuan:
				onPengyouquanClick();
				break;
			case R.id.fragment_share_rb_qq:
				onQQClick();
				break;
			case R.id.fragment_share_rb_qqzone:
				onQqZoneClick();
				break;
			case R.id.fragment_share_rb_weibo:
				onWeiBoClick();
				break;
		}
	}

	/**
	 * 未安装客户端
	 */
	@Override
	protected void notInstall() {
		super.notInstall();
		// 录播
		startLivePush();
	}

	private void startLivePush() {
		destoryPusher();
		ActivityJumpUtil.toLiveActivity(mActivity, mmAnchorInfo, mmRoomInfo, mCurrentCameraDir, mClarityType);
	}

	/**
	 * 分享回调
	 * 成功、失败、取消  最新版取消了 onstart回调
	 * 微信、微博取消登录 ，获取不到回调
	 */
	@Override
	protected UMShareListener shareCallback() {

		if (mUmShareListener == null) {
			mUmShareListener = new UMShareListener() {
				@Override
				public void onStart(SHARE_MEDIA share_media) {
					showTips("开始分享");
				}

				@Override
				public void onResult(SHARE_MEDIA platform) {
					EvtLog.d("performShareWechat", "分享成功.，platform：" + platform.toString());
					showTips("分享成功啦");
					startLivePush();
				}

				@Override
				public void onError(SHARE_MEDIA platform, Throwable t) {
					EvtLog.d("performShareWechat", "分享失败.，platform：" + platform.toString());
					showTips("分享失败啦");
					startLivePush();
				}

				@Override
				public void onCancel(SHARE_MEDIA platform) {
					EvtLog.d("performShareWechat", "分享取消.，platform：" + platform.toString());
					showTips("分享取消啦");
					startLivePush();
				}
			};
		}
		return mUmShareListener;
	}

	/**
	 * 弹出开播提示对话框
	 */
	private void initLiveTip() {
		mStartLiveBtn = (Button) findViewById(R.id.btn_live);
	}

	/**
	 * 初始化流相关参数，信息
	 */
	private void initLiveStream() {
		EvtLog.i(TAG, "initLiveStream");
		mLivePusher = new TXLivePusher(this);
		mLivePushConfig = new TXLivePushConfig();
//		mLivePushConfig.setWatermark(mBitmap, 10, 10);
//		mLivePushConfig.setCustomModeType(customModeType);

//		mLivePushConfig.setPauseImg(300,10);
//		Bitmap bitmap = decodeResource(getResources(),R.drawable.pause_publish);
//		mLivePushConfig.setPauseImg(bitmap);
//		mLivePushConfig.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);

		mLivePushConfig.setFrontCamera(mCurrentCameraDir);
//		mLivePushConfig.setBeautyFilter(5, 3);
		if (!isSupportHWEncode()) {
			mLivePushConfig.setHardwareAcceleration(TXLiveConstants.ENCODE_VIDEO_SOFTWARE);
		} else {
			mLivePushConfig.setHardwareAcceleration(TXLiveConstants.ENCODE_VIDEO_AUTO);
		}
		mLivePushConfig.enableNearestIP(false);

		mLivePusher.setExposureCompensation(-0.1f);
		mLivePusher.setVideoQuality(TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION, true, false);
		mLivePusher.setConfig(mLivePushConfig);
		mLivePusher.setPushListener(this);
		mLivePusher.startCameraPreview(mLiveVideoView);
//        mLivePusher.startScreenCapture();
//		int[] ver = TXLivePusher.getSDKVersion();
//		if (ver != null && ver.length >= 4) {
//			EvtLog.e(TAG, String.format("rtmp sdk version:%d.%d.%d.%d ", ver[0], ver[1], ver[2], ver[3]));
//		}

	}

	@SuppressLint("NewApi")
	private static boolean isSupportHWEncode() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
	}

	/**
	 * 判断是否拥有相机权限 仅限于系统级判断
	 */
	private void showCameraPermission() {
		UiHelper.showPermissionDialog(mActivity, R.string.common_dialog_permission_question_live_title,
				R.string.camera_device, R.string.camera);
	}

	/**
	 * 判断是否拥有录音权限 仅限于系统级判断
	 */
	private void showAudioPermission() {
		UiHelper.showPermissionDialog(mActivity, R.string.common_dialog_permission_question_live_title,
				R.string.camera_audio, R.string.camera_audio);
	}


	/**
	 * 设置开始直播可用
	 */
	private void setBtnLiveEnable(boolean flag) {
		if (flag) {
			mStartLiveBtn.setText(R.string.live_start);
			mStartLiveBtn.setEnabled(true);
		} else {
			mStartLiveBtn.setText(R.string.live_ready);
			mStartLiveBtn.setEnabled(false);
		}
	}

	/**
	 * 用于软键盘弹出监听，进行布局滚动
	 */
	@SuppressLint("NewApi")
	private void keyBoardChangedListener() {
		final View decordView = this.getWindow().getDecorView();
		final int scaleHeight = Utils.dip2px(this, 10);
		final View parentView = (View) mStartLiveBtn.getParent().getParent();
		decordView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				Rect rect = new Rect();
				decordView.getWindowVisibleDisplayFrame(rect);
				int disHeight = rect.bottom - rect.top;
				// 比较Activity根布局与当前布局的大小
				int heightDiff = decordView.getRootView().getHeight() - disHeight;
				if (mBtnLoginBottomHeight == -1) {
					int position[] = new int[2];
					mStartLiveBtn.getLocationOnScreen(position);
					mBtnLoginBottomHeight = FeizaoApp.metrics.heightPixels - position[1] - mStartLiveBtn.getHeight();
				}
				if (heightDiff > 100 && mBtnLoginBottomHeight <= heightDiff) {
					parentView.scrollTo(0, heightDiff - mBtnLoginBottomHeight + scaleHeight);
				} else
					parentView.scrollTo(0, 0);
			}
		});
	}

	/***********************************
	 * 事件处理器
	 *************************************/
	private class EditRoomLogoCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "EditRoomLogoCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_EIDT_ROOM_LOGO_SUCCESS;
					sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_EIDT_ROOM_LOGO_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}
	}

	/**
	 * 提交直播标题
	 */
	private class EditRoomTitleCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "EditRoomLogoCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_EIDT_ROOM_TITLE_SUCCESS;
					sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_EIDT_ROOM_TITLE_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}
	}

	@Override
	public void onPushEvent(int event, Bundle param) {
		String paramString = param.getString(TXLiveConstants.EVT_DESCRIPTION);
		EvtLog.e(TAG, "onPushEvent msg " + paramString + " event:" + event);
		switch (event) {
			case TXLiveConstants.PUSH_EVT_OPEN_CAMERA_SUCC:
				Message msg = Message.obtain();
				audioErrorFlag = false;
				cameraErrorFlag = false;
				msg.what = MSG_CAMERA_PREPARING;
				sendMsg(msg);
				break;
			case TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL:
				msg = Message.obtain();
				cameraErrorFlag = true;
				msg.what = MSG_OPEN_CAMERA_FAIL;
				sendMsg(msg);
				showCameraPermission();
				break;
			case TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL:
				audioErrorFlag = true;
				showAudioPermission();
				break;
			default:
				break;
		}
	}

	@Override
	public void onNetStatus(Bundle status) {
		Log.d(TAG, "Current status, CPU:" + status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE) +
				", RES:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) + "*" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT) +
				", SPD:" + status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED) + "Kbps" +
				", FPS:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS) +
				", ARA:" + status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps" +
				", VRA:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps");
	}

	/**
	 * 监听话题主题内容变化
	 */
	private class TopicTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
//			StringUtil.setHighLigntText(mActivity, s);
		}
	}
}