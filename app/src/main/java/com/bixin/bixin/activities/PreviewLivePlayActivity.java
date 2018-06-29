package com.bixin.bixin.activities;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.framework.net.impl.CallbackDataHandle;
import com.amap.api.location.AMapLocationClient;
import com.bixin.bixin.App;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.OperationHelper;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.WebConstants;
import com.bixin.bixin.config.AppConfig;
import com.bixin.bixin.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.live.Config;
import com.bixin.bixin.live.activities.LiveBaseActivity;
import com.bixin.bixin.receiver.ConnectionChangeReceiver;
import com.bixin.bixin.receiver.ConnectionChangeReceiver.NetwrokChangeCallback;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;
import com.lonzh.lib.network.JSONParser;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation.CornerType;
import org.json.JSONObject;
import tv.live.bx.R;

public class PreviewLivePlayActivity extends ShareDialogActivity implements View.OnClickListener,
    ITXLivePushListener {

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
        mScreenOrientionLayout = (RelativeLayout) findViewById(R.id.live_screen_oriention);

        // afl.setAspectRatio(90);
        mLiveScreenLayout = (RadioGroup) findViewById(R.id.live_screen_layout);
        mLiveClarityLayout = (RadioGroup) findViewById(R.id.live_clarity_layout);
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
        // 标题焦点
        mEtTopicAndTitle.requestFocus();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mmAnchorInfo = (Map<String, String>) getIntent()
            .getSerializableExtra(LiveBaseActivity.ANCHOR_RID);
        mIsRecordLiveFlag = Boolean.parseBoolean(mmAnchorInfo.get("isRecordLive"));
        //		ImageLoader.getInstance().displayImage(mmAnchorInfo.get("logo"), mLiveLogoImageView,
        //				Constants.COMMON_OPTIONIMAGE_2);
        // 上次开播提交标签进行本地保存，之后在本地拿取
        if (!TextUtils.isEmpty(AppConfig.getInstance().tag)) {
            for (HashMap<String, Object> tag : AppConfig.getInstance().moderatorTags) {
                if (tag.get("id").equals(AppConfig.getInstance().tag)) {
                    mTvTag.setVisibility(View.VISIBLE);
                    mTvSelectTag.setVisibility(View.GONE);
                    mTvTag.setText(String.valueOf(tag.get("name")));
                    mTvTag.setTag(AppConfig.getInstance().tag);
                }
            }
        }

        // 普通直播
        mIvRootBg.setVisibility(View.GONE);
        mIvCameraChange.setVisibility(View.VISIBLE);
        mScreenOrientionLayout.setVisibility(View.GONE);
        mLiveVideoView.setVisibility(View.VISIBLE);
        // 初始化流
        initLiveStream();
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
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
                gotoActivityForResult(LiveTagActivity.class, REQUEST_CODE_TAG, "id",
                    String.valueOf(mTvTag.getTag()));
                break;
            case R.id.live_agree_protocal:
                Map<String, String> webInfo = new HashMap<>();
                webInfo.put(WebViewActivity.URL,
                    WebConstants.getFullWebMDomain(WebConstants.LIVE_PROTOCAL_WEB_URL));
                webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(true));
                ActivityJumpUtil
                    .gotoActivity(PreviewLivePlayActivity.this, WebViewActivity.class, false,
                        WebViewActivity.WEB_INFO, (Serializable) webInfo);
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
                ImageLoaderUtil.getInstance()
                    .loadImageCorner(mActivity.getApplicationContext(), mLiveLogoImageView,
                        mmRoomInfo.get("logo"), Constants.COMMON_DISPLAY_IMAGE_CORNER_2,
                        CornerType.ALL);
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
                AppConfig.getInstance()
                    .updatePlayingSetting(mScreenOriention, String.valueOf(mTvTag.getTag()));
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
     * 关闭对话框
     */
    private void dismissProgressDialog() {
        if (mProgress != null && mProgress.isShowing()) {
            mProgress.dismiss();
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
        BusinessUtils.getRoomInfo(PreviewLivePlayActivity.this,
            new GetRoomCallbackData(PreviewLivePlayActivity.this), mmAnchorInfo.get("rid"));
    }

    /**
     * 注销广播
     */
    private void unregisterReceiver() {
        if (networkReceiver != null) {
            this.unregisterReceiver(networkReceiver);
        }
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
                if (focusIndex < 0 || focusIndex >= edit.length()) {
                    edit.append(text);
                } else {
                    edit.insert(focusIndex, text);      //光标所在位置插入文本
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
            if (baseFragmentActivity != null) {
                baseFragmentActivity.sendMsg(msg);
            }

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
                    OperationHelper
                        .onEvent(App.mContext, "clickLiveButtonInPrepareLivingPage", null);
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
                    if (mLocationSwitch.isChecked() && !mTvLocation.getText().toString()
                        .equals(getString(R.string.location_default))) {
                        location = lantitude + "," + longitude;
                    }
                    BusinessUtils.editRoomTitle(mActivity, new EditRoomTitleCallbackData(),
                        mmAnchorInfo.get("rid"), mEtTopicAndTitle.getText().toString().trim(),
                        location, String.valueOf(mTvTag.getTag()));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 未安装客户端
     */
    @Override
    protected void notInstall() {
        super.notInstall();
        startLivePush();
    }

    private void startLivePush() {
        destoryPusher();
        ActivityJumpUtil
            .toLiveActivity(mActivity, mmAnchorInfo, mmRoomInfo, mCurrentCameraDir, mClarityType);
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

    }

    @SuppressLint("NewApi")
    private static boolean isSupportHWEncode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    /**
     * 判断是否拥有相机权限 仅限于系统级判断
     */
    private void showCameraPermission() {
        UiHelper
            .showPermissionDialog(mActivity, R.string.common_dialog_permission_question_live_title,
                R.string.camera_device, R.string.camera);
    }

    /**
     * 判断是否拥有录音权限 仅限于系统级判断
     */
    private void showAudioPermission() {
        UiHelper
            .showPermissionDialog(mActivity, R.string.common_dialog_permission_question_live_title,
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
        decordView.getViewTreeObserver()
            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

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
                        mBtnLoginBottomHeight =
                            App.metrics.heightPixels - position[1] - mStartLiveBtn.getHeight();
                    }
                    if (heightDiff > 100 && mBtnLoginBottomHeight <= heightDiff) {
                        parentView.scrollTo(0, heightDiff - mBtnLoginBottomHeight + scaleHeight);
                    } else {
                        parentView.scrollTo(0, 0);
                    }
                }
            });
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
        Log.d(TAG, "Current status, CPU:" + status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE)
            + ", RES:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) + "*" + status
            .getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT) + ", SPD:" + status
            .getInt(TXLiveConstants.NET_STATUS_NET_SPEED) + "Kbps" + ", FPS:" + status
            .getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS) + ", ARA:" + status
            .getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps" + ", VRA:" + status
            .getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps");
    }

}