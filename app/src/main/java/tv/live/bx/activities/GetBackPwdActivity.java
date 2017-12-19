package tv.live.bx.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.lonzh.lib.LZActivity;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.common.Business;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;

public class GetBackPwdActivity extends LZActivity {

	private EditText moEtMobile, moEtVerifyCode, moEtNewPwd;
	private Button moBtnSendVCode, moBtnSubmit;
	private RelativeLayout mBackLayout;

	private AlertDialog moProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		isSystemBarTint = true;
		super.onCreate(savedInstanceState);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_get_back_pwd;
	}

	@Override
	protected void initMembers() {
		moEtMobile = (EditText) findViewById(R.id.get_back_pwd_et_mobile_phone);
		moEtMobile.addTextChangedListener(new EditChangedListener());
		moEtVerifyCode = (EditText) findViewById(R.id.get_back_pwd_et_verify_code);
		moEtNewPwd = (EditText) findViewById(R.id.get_back_pwd_et_new_pwd);
		moBtnSendVCode = (Button) findViewById(R.id.get_back_pwd_btn_get_vcode);
		moBtnSubmit = (Button) findViewById(R.id.get_back_pwd_btn_submit);
		initTitle();
	}

	@Override
	protected void initTitle() {
		mBackLayout = (RelativeLayout) findViewById(R.id.getpwd_top_left);
		initTitleData();
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mBackLayout.setOnClickListener(new OnBack());
	}

	@Override
	protected void registerMsgListeners() {
		OnReceiveMsgListener loOnSentVCode = new OnReceiveMsgListener() {
			@Override
			public void onReceiveMsg(Message poMsg) {
				moProgress.dismiss();
				moProgress = null;
				if (poMsg.what == MsgTypes.SEND_REGISTER_VERIFY_CODE_SUCCESS) {
					startSendSmsBtnLoop(moBtnSendVCode, 60);
				} else
					showToast((String) poMsg.obj, TOAST_LONG);
			}
		};
		registerMsgListener(MsgTypes.SEND_REGISTER_VERIFY_CODE_SUCCESS, loOnSentVCode);
		registerMsgListener(MsgTypes.SEND_REGISTER_VERIFY_CODE_FAILED, loOnSentVCode);

		OnReceiveMsgListener loOnCheckVCode = new OnReceiveMsgListener() {
			@Override
			public void onReceiveMsg(Message poMsg) {
				if (poMsg.what == MsgTypes.CHECK_REGISTER_VERIFY_CODE_SUCCESS)
					try {
						Business.getBackLogPwd(GetBackPwdActivity.this, moEtNewPwd.getText().toString());
					} catch (Exception e) {
						moProgress.dismiss();
						moProgress = null;
						e.printStackTrace();
						showToast("内部错误：" + e.toString(), TOAST_LONG);
					}
				else {
					moProgress.dismiss();
					moProgress = null;
					showToast((String) poMsg.obj, TOAST_LONG);
				}
			}
		};
		registerMsgListener(MsgTypes.CHECK_REGISTER_VERIFY_CODE_SUCCESS, loOnCheckVCode);
		registerMsgListener(MsgTypes.CHECK_REGISTER_VERIFY_CODE_FAILED, loOnCheckVCode);

		OnReceiveMsgListener loOnGetBackPwd = new OnReceiveMsgListener() {
			@Override
			public void onReceiveMsg(Message poMsg) {
				moProgress.dismiss();
				moProgress = null;
				if (poMsg.what == MsgTypes.GETBACK_LOGIN_PWD_SUCCESS) {
					showToast("密码修改成功", TOAST_LONG);
					finish();
				} else
					showToast((String) poMsg.obj, TOAST_LONG);
			}
		};
		registerMsgListener(MsgTypes.GETBACK_LOGIN_PWD_SUCCESS, loOnGetBackPwd);
		registerMsgListener(MsgTypes.GETBACK_LOGIN_PWD_FAILED, loOnGetBackPwd);
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {
		moBtnSendVCode.setOnClickListener(new OnSendVCode());
		moBtnSubmit.setOnClickListener(new OnSubmit());
	}

	/**********************************
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

			moProgress = Utils.showProgress(GetBackPwdActivity.this);
			try {
				Business.sendModifyVerifyCode(GetBackPwdActivity.this, lsMobile);
			} catch (Exception e) {
				e.printStackTrace();
				moProgress.dismiss();
				// Business.getPubKey(getApplicationContext());
				showToast("内部错误，请联系APP相关人员,请重试", TOAST_LONG);
			}
		}
	}

	private class OnSubmit implements OnClickListener {
		@Override
		public void onClick(View v) {
			OperationHelper.onEvent(FeizaoApp.mContext, "clickFinishInForgetPasswordPage", null);
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
			moProgress = Utils.showProgress(GetBackPwdActivity.this);
			Business.checkRegisterVerifyCode(GetBackPwdActivity.this, lsVCode);
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
			editStart = moEtMobile.getSelectionStart();
			editEnd = moEtMobile.getSelectionEnd();
			if (temp.length() > charMaxNum) {
				showToast(R.string.input_mobile_length, TOAST_SHORT);
				s.delete(editStart - 1, editEnd);
				int tempSelection = editStart;
				moEtMobile.setText(s);
				moEtMobile.setSelection(tempSelection);
			}

		}
	}

}
