package com.bixin.bixin.user.act;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.bixin.bixin.App;
import com.bixin.bixin.base.act.BaseFragmentActivity;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.config.UserInfoConfig;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.util.UiHelper;
import com.framework.net.impl.CallbackDataHandle;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import tv.live.bx.R;

public class EditSignatureActivity extends BaseFragmentActivity {
	public static final int RESULT_CODE_OK = 100;
	public static final int RESULT_CODE_CANCELLED = 101;
	private EditText moEdtIntroduction;
	private AlertDialog moProgress;

	@Override
	protected int getLayoutRes() {
		// TODO Auto-generated method stub
		return R.layout.activity_choice_introduction;
	}

	@Override
	protected void initData(Bundle savedInstanceState) {

		// TODO Auto-generated method stub

	}

	@Override
	protected void initMembers() {
		moEdtIntroduction = (EditText) findViewById(R.id.choice_introduction_edt_introduction);
		initTitle();
	}

	@Override
	protected void initTitleData() {
		mTopBackLayout.setOnClickListener(new OnCancel());
		mTopTitleTv.setText(R.string.edit_update_introduction);
		mTopRightText.setText(R.string.determine);
		mTopRightTextLayout.setOnClickListener(new OnDetermine());
		mTopRightTextLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public void initWidgets() {
		moEdtIntroduction.setText(UserInfoConfig.getInstance().signature);
		// 用于禁止用户手动换行
		moEdtIntroduction.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
			}
		});
	}

	@Override
	protected void setEventsListeners() {
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CODE_CANCELLED);
		super.onBackPressed();
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (moProgress != null && moProgress.isShowing())
			moProgress.dismiss();
		switch (msg.what) {
			case MsgTypes.MODIFY_USER_INFO_SUCCESS:
				String lsInstro = moEdtIntroduction.getText().toString();
				UserInfoConfig.getInstance().updateSignature(lsInstro);
				Intent loIntent = new Intent();
				loIntent.putExtra("introduction", lsInstro);
				setResult(RESULT_CODE_OK, loIntent);
				UiHelper.showShortToast(this, R.string.edit_user_save_success);
				finish();
				break;
			case MsgTypes.MODIFY_USER_INFO_FAILED:
				UiHelper.showToast(this, (String) msg.obj);
				break;
		}
	}

	/***********************************
	 * 事件处理器
	 *************************************/
	private class OnCancel implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			onBackPressed();
		}
	}

	private class OnDetermine implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			MobclickAgent.onEvent(App.mContext, "saveAutographModification");
			String lsIntroduction = moEdtIntroduction.getText().toString();
			if (lsIntroduction.length() > 30) {
				showToast("签名最长30个字", TOAST_SHORT);
				return;
			}
			// 1 取值
			// 2 提交信息
			moProgress = Utils.showProgress(EditSignatureActivity.this);
			BusinessUtils.modifyUserInfo(EditSignatureActivity.this, new UpdateUserCallbackData(), UserInfoConfig.getInstance().nickname, UserInfoConfig.getInstance().sex, lsIntroduction,
					UserInfoConfig.getInstance().birthday, null);
		}
	}

	private class UpdateUserCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "UpdateUserCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				msg.what = MsgTypes.MODIFY_USER_INFO_SUCCESS;
				try {
					msg.obj = JSONParser.parseOne((JSONObject) result);
				} catch (Exception e) {
				}
				sendMsg(msg);
			} else {
				msg.what = MsgTypes.MODIFY_USER_INFO_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}
	}

}
