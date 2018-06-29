package com.bixin.bixin.activities;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bixin.bixin.App;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;

import com.framework.net.impl.CallbackDataHandle;
import tv.live.bx.R;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.util.UiHelper;

public class PersonTaskActivity extends BaseFragmentActivity implements OnClickListener, OnRefreshListener {

	private TextView mLoginRewardTv;
	private Button mLoginRewardBtn;

	/** 每日任务 */
	private TextView mOnlineRewardNumTv;
	private ImageView mOnlineStatusIv;

	private TextView mSendGiftTimersTv, mSendGiftTotalTimersTv;
	private TextView mSendGiftRewardNumTv;
	private ImageView mSendGiftStatusIv;

	private TextView mShareTotalTimersTv;
	private TextView mShareRewardNumTv;
	private ImageView mShareStatusIv;

	private TextView mSendPostRewardNumTv;
	private ImageView mSendPostStatusIv;

	private TextView mReplyPostRewardNumTv;
	private ImageView mReplyPostStatusIv;

	/** 新人任务 */
	private TextView mFocusTimerTv;
	private TextView mFocusRewardNumTv;
	private ImageView mFocusStatusIv;

	private TextView mUpdateHeadRewardNumTv;
	private ImageView mUpdateHeadStatusIv;

	private TextView mUpdateSignRewardNumTv;
	private ImageView mUpdateSignStatusIv;

	private TextView mRechargeRewardNumTv;
	private ImageView mRechargeStatusIv;

	private TextView mSendRoseRewardNumTv;
	private ImageView mSendRoseStatusIv;

	private TextView mFirstSendPostRewardNumTv;
	private ImageView mFirstSendPostStatusIv;

	private TextView mFirstReplyPostRewardNumTv;
	private ImageView mFirstReplyPostStatusIv;

	private TextView mPhoneRewardNumTv;
	private ImageView mPhoneStatusIv;

	private static final String FOCUS_STATUS = "focus_status";
	private static final String UPDATE_HEAD_STATUS = "update_head_status";
	private static final String UPDATE_SIGN_STATUS = "update_sign_status";
	private static final String RECHARGE_STATUS = "recharge_status";
	private static final String SEND_ROSE_STATUS = "send_rose_status";
	private static final String SEND_POST_STATUS = "send_post_status";
	private static final String REPLY_POST_STATUS = "replay_status";
	private static final String PHONE_STATUS = "phone_status";

	private static final int MSG_CK_LOGIN_REWARD_SUCCESSFUL = 3;
	private static final int MSG_CK_LOGIN_REWARD_FAIL = 4;

	private static final int MSG_REWARD_SUCCESSFUL = 5;
	private static final int MSG_REWARD_FAIL = 6;
	// 每日任务数据
	private JSONObject mDialyTaskData;
	// 新手任务数据
	private JSONObject mNewerTaskData;

	// 下拉刷新
	private SwipeRefreshLayout swipeRefreshLayout;

	/**
	 * 之前忘记使用这些方法了，这个类暂时不用了
	 */
	@Override
	protected int getLayoutRes() {
		return R.layout.activity_person_tast;
	}

	@Override
	protected void initData(Bundle savedInstanceState) {

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	protected void initMembers() {
		mLoginRewardTv = (TextView) findViewById(R.id.task_login_reward_money);
		mLoginRewardBtn = (Button) findViewById(R.id.task_reward_btn);

		mOnlineRewardNumTv = (TextView) findViewById(R.id.task_online_status_tv);
		mOnlineStatusIv = (ImageView) findViewById(R.id.task_online_status_im);

		mSendGiftTimersTv = (TextView) findViewById(R.id.task_send_gift_text);
		mSendGiftTotalTimersTv = (TextView) findViewById(R.id.task_send_gift_text_desc);
		mSendGiftRewardNumTv = (TextView) findViewById(R.id.task_sendgift_status_tv);
		mSendGiftStatusIv = (ImageView) findViewById(R.id.task_sendgift_status_im);

		mShareTotalTimersTv = (TextView) findViewById(R.id.task_share_text_desc);
		mShareRewardNumTv = (TextView) findViewById(R.id.task_share_status_tv);
		mShareStatusIv = (ImageView) findViewById(R.id.task_share_status_im);

		mSendPostRewardNumTv = (TextView) findViewById(R.id.task_sendpost_status_tv);
		mSendPostStatusIv = (ImageView) findViewById(R.id.task_sendpost_status_im);

		mReplyPostRewardNumTv = (TextView) findViewById(R.id.task_replypost_status_tv);
		mReplyPostStatusIv = (ImageView) findViewById(R.id.task_replypost_status_im);

		// 新人任务
		mFocusTimerTv = (TextView) findViewById(R.id.task_focus_anchor_text_desc);
		mFocusRewardNumTv = (TextView) findViewById(R.id.task_focus_status_tv);
		mFocusStatusIv = (ImageView) findViewById(R.id.task_focus_status_im);

		mUpdateHeadRewardNumTv = (TextView) findViewById(R.id.task_updatehead_status_tv);
		mUpdateHeadStatusIv = (ImageView) findViewById(R.id.task_updatehead_status_im);

		mUpdateSignRewardNumTv = (TextView) findViewById(R.id.task_updatesign_status_tv);
		mUpdateSignStatusIv = (ImageView) findViewById(R.id.task_updatesign_status_im);

		mRechargeRewardNumTv = (TextView) findViewById(R.id.task_recharge_status_tv);
		mRechargeStatusIv = (ImageView) findViewById(R.id.task_recharge_status_im);

		mSendRoseRewardNumTv = (TextView) findViewById(R.id.task_sendrose_status_tv);
		mSendRoseStatusIv = (ImageView) findViewById(R.id.task_sendrose_status_im);

		mFirstSendPostRewardNumTv = (TextView) findViewById(R.id.task_firstsendpost_status_tv);
		mFirstSendPostStatusIv = (ImageView) findViewById(R.id.task_firstsendpost_status_im);

		mFirstReplyPostRewardNumTv = (TextView) findViewById(R.id.task_firstreplaypost_status_tv);
		mFirstReplyPostStatusIv = (ImageView) findViewById(R.id.task_firstreplaypost_status_im);

		mPhoneRewardNumTv = (TextView) findViewById(R.id.task_phone_status_tv);
		mPhoneStatusIv = (ImageView) findViewById(R.id.task_phone_status_im);

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		swipeRefreshLayout.setOnRefreshListener(this);
		/* 方法过时，setColorScheme也是在调用setColorSchemeResources */
		swipeRefreshLayout.setColorSchemeResources(R.color.a_bg_color_da500e, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		initTitle();
	}

	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.task_title);
		mTopBackLayout.setOnClickListener(this);
		initTaskData();
		reRequestData();
	}

	public void initWidgets() {
	}

	protected void setEventsListeners() {
		mTopBackLayout.setOnClickListener(this);
		mLoginRewardBtn.setOnClickListener(this);
	}

	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case MsgTypes.MSG_GET_TASK_LIST_SUCCESS:
			swipeRefreshLayout.setRefreshing(false);
			updateDailyTaskData(mDialyTaskData);
			updateNewerTaskData(mNewerTaskData);
			break;
		case MsgTypes.MSG_GET_TASK_LIST_FAILED:
			swipeRefreshLayout.setRefreshing(false);
			UiHelper.showToast(mActivity, Constants.NETWORK_FAIL);
			break;

		case MSG_CK_LOGIN_REWARD_SUCCESSFUL:
			Map<String, String> lmVerInfo = (Map<String, String>) msg.obj;
			updateLoginRewardTaskData(lmVerInfo);
			break;
		case MSG_REWARD_SUCCESSFUL:
			mLoginRewardBtn.setEnabled(false);
			mLoginRewardBtn.setText(R.string.task_login_rewarded);
			UiHelper.showToast(mActivity, "领取成功");
			break;
		case MSG_REWARD_FAIL:
			Bundle bundle = msg.getData();
			UiHelper.showToast(mActivity, bundle.getString("errorMsg"));
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ry_bar_left:
			onBackPressed();
			break;
		case R.id.task_reward_btn:
			MobclickAgent.onEvent(App.mContext, "clickGetDiandianButton");
			BusinessUtils.getSign(mActivity, new RewardCallbackDataHandle(PersonTaskActivity.this));
			break;
		default:
			break;
		}

	}

	/**
	 * 初始化静态数据
	 */
	private void initTaskData() {
		mSendGiftTotalTimersTv.setText(String.format(getResources().getString(R.string.task_send_gift_text_desc), "6"));
		mSendGiftTimersTv.setText(String.format(getResources().getString(R.string.task_send_gift_text), "0/6"));
		mShareTotalTimersTv.setText(String.format(getResources().getString(R.string.task_share_text_desc), "3"));
		mFocusTimerTv.setText(String.format(getResources().getString(R.string.task_focus_anchor_text_desc), "10"));

		mLoginRewardTv.setText(String.format(getResources().getString(R.string.login_reward_money), "120"));

		// 新手任务初始化
		if (Integer.parseInt(Utils.getCfg(mActivity, Constants.USERTASK_SF_NAME, FOCUS_STATUS, "0")) == 1) {
			mFocusStatusIv.setVisibility(View.VISIBLE);
			mFocusRewardNumTv.setEnabled(false);
		}
		if (Integer.parseInt(Utils.getCfg(mActivity, Constants.USERTASK_SF_NAME, UPDATE_HEAD_STATUS, "0")) == 1) {
			mUpdateHeadStatusIv.setVisibility(View.VISIBLE);
			mUpdateHeadRewardNumTv.setEnabled(false);
		}
		if (Integer.parseInt(Utils.getCfg(mActivity, Constants.USERTASK_SF_NAME, UPDATE_SIGN_STATUS, "0")) == 1) {
			mUpdateSignStatusIv.setVisibility(View.VISIBLE);
			mUpdateSignRewardNumTv.setEnabled(false);
		}
		if (Integer.parseInt(Utils.getCfg(mActivity, Constants.USERTASK_SF_NAME, RECHARGE_STATUS, "0")) == 1) {
			mRechargeStatusIv.setVisibility(View.VISIBLE);
			mRechargeRewardNumTv.setEnabled(false);
		}
		if (Integer.parseInt(Utils.getCfg(mActivity, Constants.USERTASK_SF_NAME, SEND_ROSE_STATUS, "0")) == 1) {
			mSendRoseStatusIv.setVisibility(View.VISIBLE);
			mSendRoseRewardNumTv.setEnabled(false);
		}
		if (Integer.parseInt(Utils.getCfg(mActivity, Constants.USERTASK_SF_NAME, SEND_POST_STATUS, "0")) == 1) {
			mFirstSendPostStatusIv.setVisibility(View.VISIBLE);
			mFirstSendPostRewardNumTv.setEnabled(false);
		}
		if (Integer.parseInt(Utils.getCfg(mActivity, Constants.USERTASK_SF_NAME, REPLY_POST_STATUS, "0")) == 1) {
			mFirstReplyPostStatusIv.setVisibility(View.VISIBLE);
			mFirstReplyPostRewardNumTv.setEnabled(false);
		}
		if (Integer.parseInt(Utils.getCfg(mActivity, Constants.USERTASK_SF_NAME, PHONE_STATUS, "0")) == 1) {
			mPhoneStatusIv.setVisibility(View.VISIBLE);
			mPhoneRewardNumTv.setEnabled(false);
		}
	}

	/**
	 * 请求任务数据
	 */
	private void reRequestData() {
		BusinessUtils.getTaskListInfo(mActivity, new GetTaskListReceiverListener(PersonTaskActivity.this));
		BusinessUtils.getSignStatus(mActivity, new CheckLoginRewardReceiverListener(PersonTaskActivity.this));
	}

	/**
	 * 更新每日任务数据
	 */
	private void updateDailyTaskData(JSONObject dailyTaskData) {
		try {
			@SuppressWarnings("unchecked")
			Iterator<String> loIterator = dailyTaskData.keys();
			while (loIterator.hasNext()) {
				String lsKey = loIterator.next();
				JSONObject taskData = dailyTaskData.getJSONObject(lsKey);
				if ("daily_in_room".equals(taskData.getString("key"))) {
					mOnlineRewardNumTv.setText(taskData.getString("lowCoin"));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						mOnlineStatusIv.setVisibility(View.VISIBLE);
						mOnlineRewardNumTv.setEnabled(false);
					}
				} else if ("daily_gift".equals(taskData.getString("key"))) {

					mSendGiftRewardNumTv.setText(taskData.getString("lowCoin"));
					mSendGiftTotalTimersTv.setText(String.format(
							getResources().getString(R.string.task_send_gift_text_desc), taskData.getString("num")));
					mSendGiftTimersTv.setText(String.format(getResources().getString(R.string.task_send_gift_text),
							taskData.getString("numDone") + "/" + taskData.getString("num")));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						mSendGiftStatusIv.setVisibility(View.VISIBLE);
						mSendGiftRewardNumTv.setEnabled(false);
					}
				} else if ("daily_share".equals(taskData.getString("key"))) {

					mShareRewardNumTv.setText(taskData.getString("lowCoin"));
					mShareTotalTimersTv.setText(String.format(getResources().getString(R.string.task_share_text_desc),
							taskData.getString("num")));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						mShareStatusIv.setVisibility(View.VISIBLE);
						mShareRewardNumTv.setEnabled(false);
					}
				} else if ("daily_post".equals(taskData.getString("key"))) {

					mSendPostRewardNumTv.setText(taskData.getString("lowCoin"));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						mSendPostStatusIv.setVisibility(View.VISIBLE);
						mSendPostRewardNumTv.setEnabled(false);
					}
				} else if ("daily_reply_or_support".equals(taskData.getString("key"))) {

					mReplyPostRewardNumTv.setText(taskData.getString("lowCoin"));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						mReplyPostStatusIv.setVisibility(View.VISIBLE);
						mReplyPostRewardNumTv.setEnabled(false);
					}
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新新人任务数据
	 */
	private void updateNewerTaskData(JSONObject newerTaskData) {
		try {
			@SuppressWarnings("unchecked")
			Iterator<String> loIterator = newerTaskData.keys();
			while (loIterator.hasNext()) {
				String lsKey = loIterator.next();
				JSONObject taskData = newerTaskData.getJSONObject(lsKey);
				if ("follow_moderator".equals(taskData.getString("field"))) {
					mFocusRewardNumTv.setText(taskData.getString("lowCoin"));
					mFocusTimerTv.setText(String.format(getResources().getString(R.string.task_focus_anchor_text_desc),
							taskData.getString("num")));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, FOCUS_STATUS, "1");
						mFocusStatusIv.setVisibility(View.VISIBLE);
						mFocusRewardNumTv.setEnabled(false);
					} else {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, FOCUS_STATUS, "0");
						mFocusStatusIv.setVisibility(View.GONE);
						mFocusRewardNumTv.setEnabled(true);
					}
				} else if ("upload_head_pic".equals(taskData.getString("field"))) {
					mUpdateHeadRewardNumTv.setText(taskData.getString("lowCoin"));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, UPDATE_HEAD_STATUS, "1");
						mUpdateHeadStatusIv.setVisibility(View.VISIBLE);
						mUpdateHeadRewardNumTv.setEnabled(false);
					} else {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, UPDATE_HEAD_STATUS, "0");
						mUpdateHeadStatusIv.setVisibility(View.GONE);
						mUpdateHeadRewardNumTv.setEnabled(true);
					}
				} else if ("signature".equals(taskData.getString("field"))) {
					mUpdateSignRewardNumTv.setText(taskData.getString("lowCoin"));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, UPDATE_SIGN_STATUS, "1");
						mUpdateSignStatusIv.setVisibility(View.VISIBLE);
						mUpdateSignRewardNumTv.setEnabled(false);
					} else {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, UPDATE_SIGN_STATUS, "0");
						mUpdateSignStatusIv.setVisibility(View.GONE);
						mUpdateSignRewardNumTv.setEnabled(true);
					}
				} else if ("recharge".equals(taskData.getString("field"))) {
					mRechargeRewardNumTv.setText(taskData.getString("lowCoin"));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, RECHARGE_STATUS, "1");
						mRechargeStatusIv.setVisibility(View.VISIBLE);
						mRechargeRewardNumTv.setEnabled(false);
					} else {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, RECHARGE_STATUS, "0");
						mRechargeStatusIv.setVisibility(View.GONE);
						mRechargeRewardNumTv.setEnabled(true);
					}
				} else if ("send_flower".equals(taskData.getString("field"))) {
					mSendRoseRewardNumTv.setText(taskData.getString("lowCoin"));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, SEND_ROSE_STATUS, "1");
						mSendRoseStatusIv.setVisibility(View.VISIBLE);
						mSendRoseRewardNumTv.setEnabled(false);
					} else {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, SEND_ROSE_STATUS, "0");
						mSendRoseStatusIv.setVisibility(View.GONE);
						mSendRoseRewardNumTv.setEnabled(true);
					}
				} else if ("post".equals(taskData.getString("field"))) {
					mFirstSendPostRewardNumTv.setText(taskData.getString("lowCoin"));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, SEND_POST_STATUS, "1");
						mFirstSendPostStatusIv.setVisibility(View.VISIBLE);
						mFirstSendPostRewardNumTv.setEnabled(false);
					} else {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, SEND_POST_STATUS, "0");
						mFirstSendPostStatusIv.setVisibility(View.GONE);
						mFirstSendPostRewardNumTv.setEnabled(true);
					}
				} else if ("reply_or_support".equals(taskData.getString("field"))) {
					mFirstReplyPostRewardNumTv.setText(taskData.getString("lowCoin"));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, REPLY_POST_STATUS, "1");
						mFirstReplyPostStatusIv.setVisibility(View.VISIBLE);
						mFirstReplyPostRewardNumTv.setEnabled(false);
					} else {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, REPLY_POST_STATUS, "0");
						mFirstReplyPostStatusIv.setVisibility(View.GONE);
						mFirstReplyPostRewardNumTv.setEnabled(true);
					}
				} else if ("app_login".equals(taskData.getString("field"))) {
					mPhoneRewardNumTv.setText(taskData.getString("lowCoin"));
					int status = Integer.parseInt(taskData.getString("status"));
					// 已领取
					if (status == 1) {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, PHONE_STATUS, "1");
						mPhoneStatusIv.setVisibility(View.VISIBLE);
						mPhoneRewardNumTv.setEnabled(false);
					} else {
						Utils.setCfg(mActivity, Constants.USERTASK_SF_NAME, PHONE_STATUS, "0");
						mPhoneStatusIv.setVisibility(View.GONE);
						mPhoneRewardNumTv.setEnabled(true);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新今日登陆任务状态
	 * */
	private void updateLoginRewardTaskData(Map<String, String> lmVerInfo) {
		if (lmVerInfo == null)
			return;
		String signed = lmVerInfo.get("signed");
		String signCoin = lmVerInfo.get("signCoin");
		mLoginRewardTv.setText(String.format(getResources().getString(R.string.login_reward_money), signCoin));
		// 如果已签到
		if (Utils.strBool(signed)) {
			mLoginRewardBtn.setEnabled(false);
			mLoginRewardBtn.setText(R.string.task_login_rewarded);
		}
	}

	@Override
	public void onRefresh() {
		reRequestData();
	}

	/**
	 * 获取任务列表回调接口 ClassName: CheckForAppUpdateReceiverListener <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class GetTaskListReceiverListener implements CallbackDataHandle {
		private final WeakReference<BaseFragmentActivity> mFragment;

		public GetTaskListReceiverListener(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetTaskListReceiverListener success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				msg.what = MsgTypes.MSG_GET_TASK_LIST_SUCCESS;
				try {
					JSONObject data = (JSONObject) result;
					PersonTaskActivity fragment = (PersonTaskActivity) mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null) {
						fragment.mDialyTaskData = data.getJSONObject("daily_missions");
						fragment.mNewerTaskData = data.getJSONObject("missions");
						fragment.sendMsg(msg);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				msg.what = MsgTypes.MSG_GET_TASK_LIST_FAILED;
				msg.obj = errorCode;
				BaseFragmentActivity fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}

		}
	}

	/**
	 * 登录奖励信息回调接口 ClassName: CheckForAppUpdateReceiverListener <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class CheckLoginRewardReceiverListener implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public CheckLoginRewardReceiverListener(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "CheckLoginRewardReceiverListener success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MSG_CK_LOGIN_REWARD_SUCCESSFUL;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

		}
	}

	/**
	 * 登录奖励领取回调接口 ClassName: CheckForAppUpdateReceiverListener <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class RewardCallbackDataHandle implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public RewardCallbackDataHandle(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "RewardCallbackDataHandle success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				msg.what = MSG_REWARD_SUCCESSFUL;
				BaseFragmentActivity fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			} else {
				msg.what = MSG_REWARD_FAIL;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				BaseFragmentActivity fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}

		}
	}

}
