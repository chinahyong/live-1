package tv.live.bx.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.lonzh.lib.network.JSONParser;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.library.util.EvtLog;


public class PhoneBindActivity extends BaseFragmentActivity implements OnClickListener {

	private EditText moEtMobile, moEtVerifyCode, moEtNewPwd;
	private Button moBtnSendVCode, moBtnSubmit;

	private AlertDialog moProgress;

	private final int MSG_LAST_TIMER = 0x0110;

	public static final String IS_SKIP = "is_skip";

	public static final int REQUEST_PHONE_BIND_CODE = 0x1002;

	private EditChangedListener mEditChangedListener;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_phone_bind_layout;
	}

	@Override
	protected void initMembers() {
		moEtMobile = (EditText) findViewById(R.id.et_mobile_phone);
		moEtVerifyCode = (EditText) findViewById(R.id.et_verify_code);
		moEtNewPwd = (EditText) findViewById(R.id.et_new_pwd);
		moBtnSendVCode = (Button) findViewById(R.id.btn_get_code);
		moBtnSubmit = (Button) findViewById(R.id.btn_submit);
		initTitle();
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.bind_title);
		mTopRightText.setText(R.string.bind_skip_text);
		mTopBackLayout.setOnClickListener(new OnBack());
		mTopRightTextLayout.setOnClickListener(this);
	}


	@Override
	public void initWidgets() {
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		if (this.getIntent() != null) {
			if (this.getIntent().getBooleanExtra(IS_SKIP, false)) {
				mTopRightTextLayout.setVisibility(View.VISIBLE);
				mTopBackLayout.setVisibility(View.INVISIBLE);
			}
		}

	}

	@Override
	protected void setEventsListeners() {
		mEditChangedListener = new EditChangedListener();
		moBtnSendVCode.setOnClickListener(new OnSendVCode());
		moBtnSubmit.setOnClickListener(new OnSubmit());
		moEtMobile.addTextChangedListener(mEditChangedListener);
		moEtVerifyCode.addTextChangedListener(mEditChangedListener);
		moEtNewPwd.addTextChangedListener(mEditChangedListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dismissProcessDialog();
	}

	private void showProcessDialog() {
		if (moProgress == null) {
			moProgress = Utils.showProgress(PhoneBindActivity.this);
		}
	}

	private void dismissProcessDialog() {
		if (moProgress != null && moProgress.isShowing()) {
			moProgress.dismiss();
			moProgress = null;
		}
	}

	/**
	 * 倒数计时
	 */
	private void startSendSmsBtnLoop(int piDuration) {
		if (piDuration > 0) {
			moBtnSendVCode.setText(String.format("请稍等%1$s秒", piDuration));
			moBtnSendVCode.setEnabled(false);
			piDuration--;
			Message msg = Message.obtain();
			msg.what = MSG_LAST_TIMER;
			msg.arg1 = piDuration;
			sendMsg(msg, 1000);
		} else {
			moBtnSendVCode.setSelected(false);
			moBtnSendVCode.setEnabled(true);
//			moBtnSendVCode.setText(get_verify_code);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.top_right_text_bg:
				finish();
				break;
		}
	}

	/***********************************
	 * 事件处理器
	 *******************************/
	private class OnBack implements OnClickListener {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	}

	private class OnSendVCode implements OnClickListener {
		@Override
		public void onClick(View v) {
			String lsMobile = moEtMobile.getText().toString().replaceAll("\\s+", "");
			if (Utils.isStrEmpty(lsMobile)) {
				showToast(R.string.input_mobile, TOAST_SHORT);
				moEtMobile.requestFocus();
				return;
			}
			// 校验手机号是否合法
			if (!lsMobile.matches(Constants.REGULAR_NUMBER)) {
				showToast(R.string.a_userinfo_input_right_phone_number, TOAST_SHORT);
				moEtMobile.requestFocus();
				return;
			}
			if (lsMobile.length() < 11) {
				showToast(R.string.invalid_mobile, TOAST_SHORT);
				moEtMobile.requestFocus();
				return;
			}
			showProcessDialog();
			BusinessUtils.getPhoneBindCode(PhoneBindActivity.this, lsMobile, new GetCodeCallbackDataHandle(PhoneBindActivity.this));
		}
	}

	private class OnSubmit implements OnClickListener {
		@Override
		public void onClick(View v) {
			// 1 获取用户输入
			String lsMobile = moEtMobile.getText().toString().replaceAll("\\s+", "");
			String lsVCode = moEtVerifyCode.getText().toString();
			String lsNewPwd = moEtNewPwd.getText().toString();

			// 2 验证输入
			if (Utils.isStrEmpty(lsMobile)) {
				showToast(R.string.input_mobile, TOAST_SHORT);
				return;
			}
			if (Utils.isStrEmpty(lsVCode)) {
				showToast(R.string.input_verify_code, TOAST_SHORT);
				return;
			}
			if (Utils.isStrEmpty(lsNewPwd)) {
				showToast(R.string.please_input_password, TOAST_SHORT);
				return;
			}
			if (lsMobile.length() < 11) {
				showToast(R.string.invalid_mobile, TOAST_SHORT);
				return;
			}
			if (lsVCode.length() < 4) {
				showToast(R.string.invalid_verify_code, TOAST_SHORT);
				return;
			}
			if (lsNewPwd.length() < 6) {
				showToast(R.string.password_min_length, TOAST_SHORT);
				return;
			}

			// 3 验证验证码
			showProcessDialog();
			BusinessUtils.phoneBind(PhoneBindActivity.this, lsVCode, lsNewPwd, new PhoneBindCallbackDataHandle(PhoneBindActivity.this));
		}
	}

	class EditChangedListener implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			//if (moBtnSendVCode.getText().contains("获取验证码") && moEtMobile.getText().toString().trim().length() >= 11) {
			//在color osV2.1/android4.4.4中，输入11位数字之后，无法满足上述条件，怀疑没有加moBtnSendVCode.getText().toString().trim()
			if (moBtnSendVCode.getText().toString().trim().contains("获取") && moEtMobile.getText().toString().trim().length() >= 11) {
				moBtnSendVCode.setEnabled(true);
			} else {
				moBtnSendVCode.setEnabled(false);
			}
			submitButtonChangeEnble();
		}
	}

	/**
	 * 提交按钮是否可点击
	 */
	private void submitButtonChangeEnble() {
		if (!TextUtils.isEmpty(moEtMobile.getText()) && !TextUtils.isEmpty(moEtVerifyCode.getText()) && !TextUtils.isEmpty(moEtNewPwd.getText())) {
			moBtnSubmit.setEnabled(true);
		} else {
			moBtnSubmit.setEnabled(false);
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		dismissProcessDialog();
		switch (msg.what) {
			case MsgTypes.SEND_REGISTER_VERIFY_CODE_SUCCESS:
				Message message = Message.obtain();
				message.what = MSG_LAST_TIMER;
				message.arg1 = 60;
				sendMsg(message);
				break;
			case MsgTypes.SEND_REGISTER_VERIFY_CODE_FAILED:
				showToast((String) msg.obj, TOAST_LONG);
				break;
			case MsgTypes.PHONE_BIND_SUCCESS:
				showToast(R.string.bind_phone_success, TOAST_LONG);
				setResult(Activity.RESULT_OK);
				finish();
				break;
			case MsgTypes.PHONE_BIND_FAILED:
				showToast((String) msg.obj, TOAST_LONG);
				break;
			case MSG_LAST_TIMER:
				startSendSmsBtnLoop(msg.arg1);
				break;
		}
	}

	private static class GetCodeCallbackDataHandle implements CallbackDataHandle {

		private WeakReference<BaseFragmentActivity> mFragment;

		public GetCodeCallbackDataHandle(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetCodeCallbackDataHandle success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				msg.what = MsgTypes.SEND_REGISTER_VERIFY_CODE_SUCCESS;
				BaseFragmentActivity meFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.sendMsg(msg);
			} else {
				msg.what = MsgTypes.SEND_REGISTER_VERIFY_CODE_FAILED;
				msg.obj = errorMsg;
				BaseFragmentActivity meFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.sendMsg(msg);
			}
		}
	}

	private static class PhoneBindCallbackDataHandle implements CallbackDataHandle {

		private WeakReference<BaseFragmentActivity> mFragment;

		public PhoneBindCallbackDataHandle(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "PhoneBindCallbackDataHandle success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				msg.what = MsgTypes.PHONE_BIND_SUCCESS;
				try {
					Map<String, String> data = JSONParser.parseOne((JSONObject) result);
					UserInfoConfig.getInstance().updateMobile( data.get("mobile"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				BaseFragmentActivity meFragment =  mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.sendMsg(msg);
			} else {
				msg.what = MsgTypes.PHONE_BIND_FAILED;
				msg.obj = errorMsg;
				BaseFragmentActivity meFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.sendMsg(msg);
			}
		}
	}

}
