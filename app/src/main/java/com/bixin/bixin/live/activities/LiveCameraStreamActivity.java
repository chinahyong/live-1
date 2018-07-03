package com.bixin.bixin.live.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import com.framework.net.impl.CallbackDataHandle;
import com.bixin.bixin.App;
import com.bixin.bixin.base.act.BaseFragmentActivity;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.helper.operation.OperationHelper;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.config.UserInfoConfig;
import com.bixin.bixin.library.util.DateUtil;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.StringUtil;
import com.bixin.bixin.live.Config;
import com.bixin.bixin.ui.popwindow.LiveFilterPopWindow;
import com.bixin.bixin.ui.popwindow.LiveMenuPopWindow;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;
import com.pili.pldroid.streaming.CameraStreamingManager;
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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONObject;
import tv.live.bx.R;

@SuppressLint("NewApi")
public class LiveCameraStreamActivity extends LiveBaseActivity implements ITXLivePushListener {

    private static final String GUIDE_LIVE = "guide_live";
    private static final int MSG_MUTE = 0x1003;
    /**
     * 直播时间显示间隔
     */
    private static final int DELAY_TIME = 1000;
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

    private boolean mIsReady = false;

    /**
     * 默认的直播清晰度
     */
    public int mClarityType = Config.ENCODING_LEVEL_HEIGHT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.setIsLiveRunning(true);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_live_playing;
    }

    @Override
    protected void initMembers() {
        super.initMembers();
        mLiveTimerTv = findViewById(R.id.live_status_time);
        mLiveBottomList = findViewById(R.id.live_bottom_list);

        mLivePushVideoView = findViewById(R.id.video_view);

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
            mmIntentRoomInfo = (Map<String, String>) bundle
                .getSerializable(LiveBaseActivity.ANCHOR_RID);
            mIsPrivatePlay = Utils.strBool(mmIntentRoomInfo.get(LiveBaseActivity.ANCHOR_PRIVATE));
            mmRoomInfo = (Map<String, Object>) bundle.getSerializable(LiveBaseActivity.ROOM_INFO);
        }
        super.initData(savedInstanceState);
        mStartLivingMills = new Date(2000, 1, 1, 0, 0, 0).getTime();
        if (Utils.getBooleanFlag(
            Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, GUIDE_LIVE, Constants.COMMON_TRUE))) {
            showFullDialog(R.layout.dialog_guide_live_layout,
                new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Utils.setCfg(mActivity, Constants.COMMON_SF_NAME, GUIDE_LIVE, "false");
                    }
                });
        }
        mIsFilter = isSupportHWEncode();
        // 初始化TX推流
        initLiveStream();
        //连麦必须使用这个类型
        mLivePusher
            .setVideoQuality(TXLiveConstants.VIDEO_QUALITY_LINKMIC_MAIN_PUBLISHER, false, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mLivePushVideoView.onResume();
        mLivePusher.resumePusher();
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
        App.setIsLiveRunning(false);
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
     * @see com.bixin.bixin.live.activities.LiveBaseActivity#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.live_btn_exit:
                // 按返回键弹出对话框
                UiHelper.showConfirmDialog(this, R.string.live_exist, R.string.cancel,
                    R.string.determine, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MobclickAgent.onEvent(App.mContext, "exitLiveRoom");
                            OperationHelper.onEvent(App.mContext, "exitLiveRoom", null);
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
            mfilterPopWindow = new LiveFilterPopWindow(mActivity,
                UserInfoConfig.getInstance().filterPercent);
            mfilterPopWindow.setOnProgressChange(new LiveFilterPopWindow.OnProgressChange() {
                @Override
                public void onChange(float progress) {
                    UserInfoConfig.getInstance().updateFilterPer(progress);
                }
            });
        }
        mfilterPopWindow
            .showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content),
                Gravity.BOTTOM, 0, 0);
    }


    /**
     * 显示更多下拉对话框
     */
    private void showMorePopWindow(View v) {
        if (mLivePopWindow == null) {
            mLivePopWindow = new LiveMenuPopWindow(mActivity, R.layout.pop_live_bottom_list_layou);
            showItemsInLivePopWindow += extraLivePopWindowManager(mLivePopWindow);
            LiveItemListener mMoreItemListener = new LiveItemListener();
//            mLivePopWindow.setOnClickListener(R.id.live_bottom_item_beauty, mMoreItemListener);
            mLivePopWindow
                .setOnClickListener(R.id.live_bottom_item_switch_caption, mMoreItemListener);
            //			mLivePopWindow.setOnClickListener(R.id.live_bottom_item_screen,
            // mMoreItemListener);
            mLivePopWindow.setOnClickListener(R.id.live_bottom_item_share, mMoreItemListener);
        }
        mLiveBottomList.startAnimation(mAnimation180);
        if (!mLivePopWindow.isShowing()) {
            int[] location = new int[2];
            v.getLocationOnScreen(location);
            if (mCurrentScreenType == Configuration.ORIENTATION_PORTRAIT) {
                mLivePopWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0],
                    location[1] - v.getHeight() * showItemsInLivePopWindow - Utils
                        .dip2px(mActivity, 40));
            } else {
                mLivePopWindow.showAtLocation(v, Gravity.NO_GRAVITY,
                    location[0] - v.getWidth() * showItemsInLivePopWindow - Utils
                        .dip2px(mActivity, 40), location[1]);
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
                if (TXLiveConstants.PUSH_ERR_NET_DISCONNECT == status
                    || CameraStreamingManager.STATE.DISCONNECTED == status) {
                    stopLivePush(PUSH_EVT_STOP_PREVIEW_SHOW_DIALOG);
                } else if ((status == TXLiveConstants.PUSH_EVT_PUSH_BEGIN
                    || status == CameraStreamingManager.STATE.STREAMING) && mLiveTimer == null) {
                    OperationHelper.onEvent(App.mContext, "liveSuccessful", null);
                    mLiveTimerTv.setText(DateUtil.sdf3.format(new Date(mStartLivingMills)));
                    mLiveTimer = new Timer();
                    mLiveTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (mLiveStatusCode != TXLiveConstants.PUSH_ERR_NET_DISCONNECT
                                && mLiveStatusCode != CameraStreamingManager.STATE.SHUTDOWN
                                && mLiveStatusCode != CameraStreamingManager.STATE.IOERROR) {
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
                // TX调用了获取URL
                startLivePush(liveInfo);
                break;
            case MsgTypes.MSG_GET_LIVESTREAM_FAILED:
                showToast((String) msg.obj, TOAST_LONG);
                break;
            case MSG_MUTE:
                // mCameraStreamingManager.mute(mIsNeedMute);
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
     * @see com.bixin.bixin.live.activities.LiveBaseActivity#networkRecovery()
     */
    @Override
    protected void networkRecovery() {
        super.networkRecovery();
        /** 如果已经开始播放，且视频流断开 */
        if (mLiveStatusCode == TXLiveConstants.PUSH_ERR_NET_DISCONNECT
            || mLiveStatusCode == CameraStreamingManager.STATE.SHUTDOWN
            || mLiveStatusCode == CameraStreamingManager.STATE.DISCONNECTED) {
            requestLiveParam();
        }

    }

    @Override
    public void onConnectStatus() {
        super.onConnectStatus();
        anchorDownLine();
    }

    @Override
    public void addUser(String piUid, String piType, String psNickname, String level,
        String fromModeratorLevel, String psPhoto, String lowkeyEnter, String cid, String medals,
        String isGuard, String guardType, String guardTimeType, String mountId, String mountName,
        String mountAction, String androidMount) {
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
    public void delUser(String piUid, String piType, String psNickname, String psPhoto, String ban,
        String cid) {
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
    protected void clickPersonInfoView(String type, String uid) {
        //如果是私播
        if (mIsPrivatePlay) {
            MobclickAgent.onEvent(App.mContext, "personalPageInPersonalCard");
            Map<String, String> personInfo = new HashMap<String, String>();
            personInfo.put("id", uid);
            ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
        } else {
            if (Constants.USER_TYPE_OFFICIAL_ADMIN.equals(type) || Constants.USER_TYPE_ADMIN
                .equals(type)) {
                MobclickAgent.onEvent(App.mContext, "cancelManagerByBroadcaster");
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
                MobclickAgent.onEvent(App.mContext, "managerByBroadcaster");
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
//            mLivePusher.setBeautyFilter(7, 3);
        } else {
//            mLivePusher.setBeautyFilter(0, 0);
        }
    }

    /**
     * 主播下线
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
        EvtLog.i(TAG, "initLiveStream");
        if (mLivePusher == null) {
            mLivePusher = new TXLivePusher(this);
            mLivePushConfig = new TXLivePushConfig();
            //		mLivePushConfig.setWatermark(mBitmap, 10, 10);
            //		mLivePushConfig.setCustomModeType(customModeType);
            mLivePushConfig.setPauseImg(60 * 60 * 10, 10);
            Bitmap bitmap = decodeResource(getResources(), R.drawable.pause_publish);
            mLivePushConfig.setPauseImg(bitmap);
            mLivePushConfig.setPauseFlag(
                TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO | TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);

            mLivePushConfig.setFrontCamera(mCurrentCameraDir);
//            if (mIsFilter) {
//                				mLivePushConfig.setBeautyFilter(5, 3);
//            } else {
//                				mLivePushConfig.setBeautyFilter(0, 0);
//            }
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
            //				LogUtil.e(TAG, String.format("rtmp sdk version:%d.%d.%d.%d ", ver[0], ver[1],
            //						ver[2], ver[3]));
            //			}
        }
    }

    /**
     * 开始推流
     */
    private void startLivePush(String pushUrl) {
        try {
            //如果正在推流且推流地址一致，不重新推流
            if (mLivePusher.isPushing() && pushUrl.equals(this.mPushUrl)) {
                return;
            }
            this.mPushUrl = pushUrl;

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
     * 大主播停止直播推流
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
                chatFragment.sendChatMsg(
                    chatFragment.onSysMsg(mActivity.getResources().getString(R.string.live_error)));
                break;
            case PUSH_EVT_STOP_PREVIEW_SHOW_DIALOG:
                chatFragment.sendChatMsg(
                    chatFragment.onSysMsg(mActivity.getResources().getString(R.string.live_error)));
                if (mLivePushDialog == null || !mLivePushDialog.isShowing()) {
                    mLivePushDialog = UiHelper.showConfirmDialog(LiveCameraStreamActivity.this,
                        R.string.live_dialog_error_tip, R.string.live_restart, R.string.live_quit,
                        new DialogInterface.OnClickListener() {
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
        BusinessUtils.getLiveStreamInfo(mActivity,
            new GetLiveStreamCallbackData(LiveCameraStreamActivity.this),
            mmIntentRoomInfo.get("rid"), pushVideoWidth, pushVideoHeight);
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
                // 美颜开关
                case R.id.live_bottom_item_beauty:
                    // TX美颜切换
                    if (mLivePusher != null) {
                        mIsFilter = !mIsFilter;
                        if (mIsFilter) {
                            MobclickAgent.onEvent(App.mContext, "openBeautyByBroadcaster");
                        } else {
                            MobclickAgent.onEvent(App.mContext, "closeBeautyByBroadcaster");
                        }
                        switchBeautyFilter(mIsFilter);
                    } else {
                        showFilterPopWindow();
                    }
                    break;
                // 前后摄像头切换
                case R.id.live_bottom_item_switch_caption:
                    MobclickAgent.onEvent(App.mContext, "switchCameraInLiveRoom");
                    switchCaption();
                    break;
                case R.id.live_bottom_item_share:
                    MobclickAgent.onEvent(App.mContext, "shareByBroadcaster");
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
                if (!isStartLiveStream && !mNoNetworkFlag) {
                    requestLiveParam();
                }
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
        Log.d(TAG, "Current status, CPU:" + status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE)
            + ", RES:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) + "*" + status
            .getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT) + ", SPD:" + status
            .getInt(TXLiveConstants.NET_STATUS_NET_SPEED) + "Kbps" + ", FPS:" + status
            .getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS) + ", ARA:" + status
            .getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps" + ", VRA:" + status
            .getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps");
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
        return false;
    }

    @Override
    public boolean onZoomValueChanged(float factor) {
        return false;
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
            EvtLog
                .d(TAG, "GetLiveStreamCallbackData success " + success + " errorCode" + errorCode);
            Message msg = new Message();
            try {
                if (success) {
                    msg.what = MsgTypes.MSG_GET_LIVESTREAM_SUCCESS;
                    JSONObject jsonObject = new JSONObject(
                        StringUtil.base64Decode(((JSONObject) result).getString("encrypted")));
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
            if (baseFragmentActivity != null) {
                baseFragmentActivity.sendMsg(msg);
            }
        }
    }
}