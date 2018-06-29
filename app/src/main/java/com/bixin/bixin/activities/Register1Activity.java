package com.bixin.bixin.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bixin.bixin.user.act.LoginActivity;
import com.lonzh.lib.LZActivity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import tv.live.bx.R;
import com.bixin.bixin.common.Business;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.WebConstants;
import com.bixin.bixin.library.util.TelephoneUtil;
import com.bixin.bixin.util.ActivityJumpUtil;

public class Register1Activity extends LZActivity {

	private EditText moEtMobilePhone, moEtVerifyCode;
	private Button moBtnGetVCode, moBtnNextStep;
	private TextView mTvAgree;

	private AlertDialog moProgress;
	private RelativeLayout mBackLayout;

	private static int REUQEST_REIGSTER = 102;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		isSystemBarTint = true;
		super.onCreate(savedInstanceState);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_register1;
	}

	@Override
	protected void initMembers() {
		moEtMobilePhone = (EditText) findViewById(R.id.register1_et_mobile_phone);
		moEtMobilePhone.addTextChangedListener(new EditChangedListener());
		moEtVerifyCode = (EditText) findViewById(R.id.register1_et_verify_code);
		moBtnGetVCode = (Button) findViewById(R.id.register1_btn_get_vcode);
		moBtnNextStep = (Button) findViewById(R.id.register1_btn_next_step);
		mBackLayout = (RelativeLayout) findViewById(R.id.register1_top_left);
		mTvAgree = (TextView) findViewById(R.id.register_tv_agree);
	}

	@Override
	protected void registerMsgListeners() {
		// 发送验证码
		OnReceiveMsgListener loOnSendVCode = new OnReceiveMsgListener() {
			@Override
			public void onReceiveMsg(Message poMsg) {
				if (moProgress != null && moProgress.isShowing())
					moProgress.dismiss();
				if (poMsg.what == MsgTypes.SEND_REGISTER_VERIFY_CODE_SUCCESS) {
					startSendSmsBtnLoop(moBtnGetVCode, 60);
				} else
					showToast((String) poMsg.obj, TOAST_LONG);
			}
		};
		registerMsgListener(MsgTypes.SEND_REGISTER_VERIFY_CODE_SUCCESS, loOnSendVCode);
		registerMsgListener(MsgTypes.SEND_REGISTER_VERIFY_CODE_FAILED, loOnSendVCode);

		// 验证验证码
		OnReceiveMsgListener loOnCheckVCode = new OnReceiveMsgListener() {
			@Override
			public void onReceiveMsg(Message poMsg) {
				if (moProgress != null && moProgress.isShowing())
					moProgress.dismiss();
				switch (poMsg.what) {
					case MsgTypes.CHECK_REGISTER_VERIFY_CODE_SUCCESS:
						killSendSmsBtnLoop();
						String lsMobile = moEtMobilePhone.getText().toString();
						ActivityJumpUtil.gotoActivityForResult(Register1Activity.this, Register2Activity.class,
								REUQEST_REIGSTER, "mobile", lsMobile);
						break;
					case MsgTypes.CHECK_REGISTER_VERIFY_CODE_FAILED:
						showToast((String) poMsg.obj, TOAST_LONG);
						break;
				}
			}
		};
		registerMsgListener(MsgTypes.CHECK_REGISTER_VERIFY_CODE_SUCCESS, loOnCheckVCode);
		registerMsgListener(MsgTypes.CHECK_REGISTER_VERIFY_CODE_FAILED, loOnCheckVCode);
	}

	@Override
	public void initWidgets() {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REUQEST_REIGSTER) {
			if (resultCode == RESULT_OK) {
				setResult(RESULT_OK);
			} else {
				setResult(LoginActivity.RESULT_CODE_CANCELLED);
			}
			onBackPressed();
		}
	}

	@Override
	public void onBackPressed() {
		TelephoneUtil.hideSoftInput(this);
		super.onBackPressed();
	}

	@Override
	protected void setEventsListeners() {
		moBtnGetVCode.setOnClickListener(new OnSendVerifyCode());
		moBtnNextStep.setOnClickListener(new OnNextStep());
		mBackLayout.setOnClickListener(new OnBack());
		mTvAgree.setOnClickListener(onClick);
	}

	/***********************************
	 * 事件处理器
	 *************************************/
	private class OnBack implements OnClickListener {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	}

	private OnClickListener onClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.register_tv_agree:
					Map<String, String> webInfo = new HashMap<String, String>();
					webInfo.put(WebViewActivity.URL, WebConstants.getFullWebMDomain(WebConstants.REGISTER_PROTOCOL));
					webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(true));
					ActivityJumpUtil.gotoActivity(Register1Activity.this, WebViewActivity.class, false, WebViewActivity.WEB_INFO,
							(Serializable) webInfo);
					break;
			}
		}
	};

	private class OnSendVerifyCode implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			// 1 获取用户输入
			String lsMobile = moEtMobilePhone.getText().toString();

			// 2 验证输入
			if (Utils.isStrEmpty(lsMobile)) {
				showToast(R.string.input_mobile, TOAST_SHORT);
				moEtMobilePhone.requestFocus();
				return;
			}
			// 校验手机号是否合法
			if (!lsMobile.matches(Constants.REGULAR_NUMBER)) {
				showToast(R.string.a_userinfo_input_right_phone_number, TOAST_SHORT);
				moEtMobilePhone.requestFocus();
				return;
			}
			if (lsMobile.length() < 11) {
				showToast(R.string.invalid_mobile, TOAST_SHORT);
				moEtMobilePhone.requestFocus();
				return;
			}

			// 3 获取验证码
			moProgress = Utils.showProgress(Register1Activity.this);
			try {
				Business.sendRegisterVerifyCode(Register1Activity.this, lsMobile);
			} catch (Exception e) {
				e.printStackTrace();
				moProgress.dismiss();
				// Business.getPubKey(getApplicationContext());
				showToast("内部错误，请联系APP相关人员,请重试", TOAST_LONG);
			}
		}
	}

	private class OnNextStep implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			// 1 获取用户输入
			String lsMobile = moEtMobilePhone.getText().toString();
			String lsVCode = moEtVerifyCode.getText().toString();

			// 2 验证输入
			if (Utils.isStrEmpty(lsMobile)) {
				showToast(R.string.input_mobile, TOAST_SHORT);
				return;
			}
			// 校验手机号是否合法
			if (!lsMobile.matches(Constants.REGULAR_NUMBER)) {
				showToast(R.string.a_userinfo_input_right_phone_number, TOAST_SHORT);
				moEtMobilePhone.requestFocus();
				return;
			}
			if (Utils.isStrEmpty(lsVCode)) {
				showToast(R.string.input_verify_code, TOAST_SHORT);
				moEtVerifyCode.requestFocus();
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

			// 3 验证验证码
			moProgress = Utils.showProgress(Register1Activity.this);
			Business.checkRegisterVerifyCode(Register1Activity.this, lsVCode);
		}

	}

	class EditChangedListener implements TextWatcher {
		private CharSequence temp;// 监听前的文本
		private int editStart;// 光标开始位置
		private int editEnd;// 光标结束位置
		private final int charMaxNum = 11;

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			temp = s;
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			/** 得到光标开始和结束位置 ,超过最大数后记录刚超出的数字索引进行控制 */
			editStart = moEtMobilePhone.getSelectionStart();
			editEnd = moEtMobilePhone.getSelectionEnd();
			if (temp.length() > charMaxNum) {
				showToast(R.string.input_mobile_length, TOAST_SHORT);
				s.delete(editStart - 1, editEnd);
				int tempSelection = editStart;
				moEtMobilePhone.setText(s);
				moEtMobilePhone.setSelection(tempSelection);
			}

		}
	}

}
