package tv.live.bx.live.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.umeng.analytics.MobclickAgent;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.PersonInfoActivity;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.websocket.WebSocketLiveEngine;

@SuppressLint("NewApi")
public class LiveMediaPlayerActivity extends LiveBaseActivity implements OnClickListener,
    ITXLivePlayListener {

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

    /*** 页面的统计时长 */
    private long mStartTime;
    private static String GUIDE_PLAY = "guide_play";

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
    }

    @Override
    public void initWidgets() {
        super.initWidgets();
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
            mmIntentRoomInfo = (Map<String, String>) intent
                .getSerializableExtra(LiveBaseActivity.ANCHOR_RID);
            mIsPrivatePlay = Utils.strBool(mmIntentRoomInfo.get(LiveBaseActivity.ANCHOR_PRIVATE));
            mMediaPlayUrl = mmIntentRoomInfo.get(LiveMediaPlayerActivity.MEDIA_PLAY_URL);
        }
        super.initData(savedInstanceState);
        if (Utils.getBooleanFlag(
            Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, GUIDE_PLAY, Constants.COMMON_TRUE))) {
            showFullDialog(R.layout.dialog_guide_playing_layout,
                new DialogInterface.OnDismissListener() {

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
    }

    /**
     * TODO 简单描述该方法的实现功能（可选）.
     * @see tv.live.bx.live.activities.LiveBaseActivity#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            // 未开始退出直播
            case R.id.live_btn_exit:
                MobclickAgent.onEvent(FeizaoApp.mContext, "exitLiveRoom");
                OperationHelper.onEvent(FeizaoApp.mContext, "exitLiveRoom", null);
                finish();
                break;
            case R.id.playing_btn_back:
                finish();
                break;
            case R.id.btn_live_focus:
                MobclickAgent.onEvent(FeizaoApp.mContext, "followBroadcaster");
                OperationHelper.onEvent(FeizaoApp.mContext, "followBroadcaster", null);
                BusinessUtils.follow(mActivity, new FollowCallbackData(LiveMediaPlayerActivity
                    .this), mmAnchorInfo.get("id"));
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
        MobclickAgent.onEventValue(FeizaoApp.mContext, "timeSpentOnLiveRoom", null,
            (int) ((currentTime - mStartTime) / 1000));
    }

    @Override
    public void onDestroy() {
        EvtLog.e(TAG, "onDestroy");
        destroyLivePlayer();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        EvtLog.e(TAG, "onActivityResult requestCode " + requestCode + "resultCode " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PERSONINFO) {
            if (resultCode == RESULT_OK) {
                //如果关注的用户是主播，则更新主播信息状态
                if (data.getStringExtra(PersonInfoActivity.PERSON_ID)
                    .equals(mmAnchorInfo.get("id"))) {
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
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MsgTypes.ON_LOAD_VIDEO_SUCCESS:
                EvtLog.i(TAG, "video loading success!");
                //如果主播先断开下线了，然后再上线提示“系统消息：主播回来啦，精彩继续”
                if (!mLiveAnchorOnLineStatus) {
                    CharSequence charSequence = chatFragment.onSysMsg(
                        mActivity.getResources().getString(R.string.live_anchor_back_tip));
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
                OperationHelper.onEvent(FeizaoApp.mContext, "followBroadcasterSuccessful", null);
                mLiveFocusBtn.setVisibility(View.GONE);
                mWebSocketImpl.sendCommand(WebSocketLiveEngine.USER_ATTENTION);
                UiHelper.showShortToast(mActivity, R.string.person_focus_success);
                UiHelper.showNotificationDialog(mActivity);
                break;
            case MsgTypes.FOLLOW_FAILED:
                UiHelper.showShortToast(mActivity, (String) msg.obj);
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

    @Override
    public void initRoomData() {
        super.initRoomData();
        if (mmRoomInfo == null) {
            return;
        }
        mIsPlaying = Boolean.valueOf((String) mmRoomInfo.get("isPlaying"));
        //未关注并且不是自己
        if (!mUid.equals(mmAnchorInfo.get("id")) && !Boolean
            .valueOf((String) mmRoomInfo.get("loved"))) {
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

    @Override
    protected void networkRecovery() {
        super.networkRecovery();
        /** 如果已经开始播放，且视频流断开 */
        if (mIsPlaying && mLiveStreamDisConnect) {
            startLivePlayer(true);
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
    public void addUser(String piUid, String piType, String psNickname, String level,
        String fromModeratorLevel, String psPhoto, String lowkeyEnter, String cid, String medals,
        String isGuard, String guardType, String guardTimeType, String mountId, String mountName,
        String mountAction, String androidMount) {
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
    public void delUser(String piUid, String piType, String psNickname, String psPhoto, String ban,
        String cid) {
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
    }

    @Override
    public void onClose(int code, String errosMsg) {
        super.onClose(code, errosMsg);
    }

    //由于主播推流切换观众需要更换拉流地址
    @Override
    public void onChangeVideoPullUrl(String pullUrl) {
        super.onChangeVideoPullUrl(pullUrl);
        if (!TextUtils.isEmpty(pullUrl)) {
            // 新的拉流地址不为空，并且不等于之前的拉流地址
            if (!pullUrl.equals(mMediaPlayUrl)) {
                mMediaPlayUrl = pullUrl;
                stopLivePlayer(false);
                // 重新拉流，此方法有重置
                startLivePlayer(true);
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
        MobclickAgent.onEvent(FeizaoApp.mContext, "personalPageInPersonalCard");
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
            ImageLoaderUtil.getInstance()
                .loadImageBlur(mActivity, view, mmIntentRoomInfo.get("headPic"),
                    R.drawable.live_load_blur, R.drawable.live_load_blur, 0);
        } else {
            ImageLoaderUtil.getInstance().loadImageBlur(mActivity, view, R.drawable.live_load_blur,
                R.drawable.live_load_blur, R.drawable.live_load_blur, 0);
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
                chatFragment.sendChatMsg(chatFragment.onSysMsg(
                    mActivity.getResources().getString(R.string.live_anchor_go_away_tip)));
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
            int result = mTxLivePlayer.startPlay(pullUrl,
                playType); // result返回值：0 success;  -1 empty url; -2 invalid url; -3 invalid playType;
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
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT
            || event == TXLiveConstants.PLAY_EVT_PLAY_END) {
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
        Log.d(TAG, "ITXLivePlayListener Current status, CPU:" + status
            .getString(TXLiveConstants.NET_STATUS_CPU_USAGE) + ", RES:" + status
            .getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) + "*" + status
            .getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT) + ", SPD:" + status
            .getInt(TXLiveConstants.NET_STATUS_NET_SPEED) + "Kbps" + ", FPS:" + status
            .getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS) + ", ARA:" + status
            .getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps" + ", VRA:" + status
            .getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps");
        if (status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) > status
            .getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT)) {
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
        MobclickAgent.onEvent(FeizaoApp.mContext, "followBroadcasterInBixinBox");
        OperationHelper.onEvent(FeizaoApp.mContext, "followBroadcasterInBixinBox", null);
        BusinessUtils.follow(mActivity, new FollowCallbackData(LiveMediaPlayerActivity.this),
            mmAnchorInfo.get("id"));
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
                    if (meFragment != null) {
                        meFragment.sendMsg(msg);
                    }
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
                if (meFragment != null) {
                    meFragment.sendMsg(msg);
                }
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
