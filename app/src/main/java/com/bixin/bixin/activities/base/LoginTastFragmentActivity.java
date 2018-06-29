package com.bixin.bixin.activities.base;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import com.bixin.bixin.user.act.LoginActivity;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.config.AppConfig;
import com.bixin.bixin.library.util.DateUtil;
import com.bixin.bixin.library.util.EvtLog;
import com.lonzh.lib.network.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

import com.framework.net.impl.CallbackDataHandle;
import com.dialog.RewardDialgBuilder;

/**
 * 需要“登录任务”提醒基础类
 * @author Live
 */
@SuppressLint("NewApi")
public abstract class LoginTastFragmentActivity extends BaseFragmentActivity {

	/** 登录任务相关变量 start */
	protected RewardDialgBuilder mLoginRewardDialog;
	private static String LOGIN_TAST_POPPU_TIME = "login_tast_poppu_time";
	private static final int MSG_CK_LOGIN_REWARD_SUCCESSFUL = 0x300;
	private static final int MSG_CK_LOGIN_REWARD_FAIL = 0x400;

	private static final int MSG_REWARD_SUCCESSFUL = 0x500;
	private static final int MSG_REWARD_FAIL = 0x600;
	protected JSONArray defualtRewardArray = new JSONArray();

	/** 登录任务相关变量 end */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			defualtRewardArray = new JSONArray("[120,150,180,210,240,270,300]");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (msg.what == MSG_CK_LOGIN_REWARD_SUCCESSFUL) {
			Map<String, String> lmVerInfo = (Map<String, String>) msg.obj;
			String signed = lmVerInfo.get("signed");
			int cTimes = Integer.parseInt(lmVerInfo.get("ctimes"));
			JSONArray array = null;
			try {
				array = new JSONArray(lmVerInfo.get("sign_coin_list"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// 如果未签到
			if (!Utils.strBool(signed)) {
				showLoginRewardDialog(cTimes, array);
			} else {
				if (mLoginRewardDialog != null && mLoginRewardDialog.isShowDialog()) {
					mLoginRewardDialog.dismissDialog();
					UiHelper.showToast(mActivity, "已领取");
				}
			}

		} else if (msg.what == MSG_REWARD_SUCCESSFUL) {
			UiHelper.showToast(mActivity, "领取成功");
		} else if (msg.what == MSG_REWARD_FAIL) {
			Bundle bundle = msg.getData();
			UiHelper.showToast(mActivity, bundle.getString("errorMsg"));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mLoginRewardDialog != null && mLoginRewardDialog.isShowDialog()) {
			mLoginRewardDialog.dismissDialog();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 如果登录成功，请求登录任务奖励
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			if (resultCode == LoginActivity.RESULT_CODE_OK) {
				BusinessUtils.getSignStatus(mActivity, new CheckLoginRewardReceiverListener());
			}
		}
	}

	protected void getTaskSignStatus() {
		if (!DateUtil.isSameDay(new Date(),
				new Date(Long.parseLong(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, LOGIN_TAST_POPPU_TIME, "0"))))) {
			BusinessUtils.getSignStatus(mActivity, new CheckLoginRewardReceiverListener());
		}
	}

	protected void showLoginRewardDialog(int index, JSONArray array) {
		if (mLoginRewardDialog != null && mLoginRewardDialog.isShowDialog()) {
			mLoginRewardDialog.setRewardIndex(index, array);
			return;
		}
		Utils.setCfg(mActivity, LOGIN_TAST_POPPU_TIME, String.valueOf(System.currentTimeMillis()));
		mLoginRewardDialog = new RewardDialgBuilder(mActivity, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 是否已登录
				if (!AppConfig.getInstance().isLogged) {
					ActivityJumpUtil.gotoActivityForResult(LoginTastFragmentActivity.this, LoginActivity.class,
							Constants.REQUEST_CODE_LOGIN, null, null);
				} else {
					dialog.dismiss();
					BusinessUtils.getSign(mActivity, new RewardCallbackDataHandle());
				}
			}
		}).setRewardIndex(index, array).showDialog();
	}

	/**
	 * 登录奖励信息回调接口 ClassName: CheckForAppUpdateReceiverListener <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private class CheckLoginRewardReceiverListener implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "CheckLoginRewardReceiverListener success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MSG_CK_LOGIN_REWARD_SUCCESSFUL;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					sendMsg(msg);
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
	private class RewardCallbackDataHandle implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "RewardCallbackDataHandle success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				msg.what = MSG_REWARD_SUCCESSFUL;
				sendMsg(msg);
			} else {
				msg.what = MSG_REWARD_FAIL;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				sendMsg(msg);
			}

		}
	}
}