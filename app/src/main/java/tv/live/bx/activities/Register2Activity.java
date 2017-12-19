package tv.live.bx.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lonzh.lib.LZActivity;
import com.lonzh.lib.network.HttpSession;

import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import tv.live.bx.R;
import tv.live.bx.callback.MyUserInfoCallbackDataHandle;
import tv.live.bx.common.Business;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Consts;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.library.util.TelephoneUtil;
import tv.live.bx.receiver.LoginStatusChangeReceiver;

public class Register2Activity extends LZActivity {

	private EditText moEtNickname, moEtPassword;
	private RadioButton moRbMale, moRbFemale;
	private Button moBtnSubmit;

	private AlertDialog moProgress;
	private LinearLayout moLlMale;
	private LinearLayout moLlFemale;
	private RelativeLayout mBackLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		isSystemBarTint = true;
		super.onCreate(savedInstanceState);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_register2;
	}

	@Override
	protected void initMembers() {
		moEtNickname = (EditText) findViewById(R.id.register2_et_nickname);
		moEtPassword = (EditText) findViewById(R.id.register2_et_password);
		moRbMale = (RadioButton) findViewById(R.id.register2_rb_male);
		moRbFemale = (RadioButton) findViewById(R.id.register2_rb_female);
		moBtnSubmit = (Button) findViewById(R.id.register2_btn_submit);
		moLlMale = (LinearLayout) findViewById(R.id.register2_ll_male);
		moLlFemale = (LinearLayout) findViewById(R.id.register2_ll_female);
		initTitle();
	}

	@Override
	protected void initTitle() {
		mTopTitleTv = (TextView) findViewById(R.id.top_title);
		mBackLayout = (RelativeLayout) findViewById(R.id.register2_top_left);
		initTitleData();
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.register);
	}

	private Handler callbackHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (moProgress != null && moProgress.isShowing())
				moProgress.dismiss();
			switch (msg.what) {
				case MsgTypes.GET_MY_USER_INFO_SUCCESS:
					Intent intent = new Intent(LoginStatusChangeReceiver.LOGIN_STATUS_ChANGE_ACTION);
					intent.setPackage(getApplicationContext().getPackageName());
					// 发送登录状态改变广播
					getApplicationContext().sendBroadcast(intent);
					setResult(RESULT_OK);
						/* 注册成功获取用户信息之后，关闭页面 */
					finish();
					break;
				case MsgTypes.GET_MY_USER_INFO_FAILED:
					break;
			}
		}
	};

	@Override
	protected void registerMsgListeners() {
		OnReceiveMsgListener loOnRegister = new OnReceiveMsgListener() {
			@Override
			public void onReceiveMsg(Message poMsg) {
				if (moProgress != null && moProgress.isShowing())
					moProgress.dismiss();
				switch (poMsg.what) {
					case MsgTypes.REGISTER_SUCCESS:
						AppConfig.getInstance().updateLoginStatus(true);
						String lsUid = HttpSession.getInstance(Register2Activity.this).getCookie("uid");
						UserInfoConfig.getInstance().updateUserId(lsUid);
						JPushInterface.setAliasAndTags(Register2Activity.this, lsUid, null, new TagAliasCallback() {
							@Override
							public void gotResult(int i, String s, Set set) {

							}
						});
						BusinessUtils.getMyUserInfo(Register2Activity.this, new MyUserInfoCallbackDataHandle(callbackHandle));
						showToast(R.string.register_success, TOAST_SHORT);
						break;
					case MsgTypes.REGISTER_FAILED:
						showToast((String) poMsg.obj, TOAST_SHORT);
						break;
				}
			}
		};
		registerMsgListener(MsgTypes.REGISTER_SUCCESS, loOnRegister);
		registerMsgListener(MsgTypes.REGISTER_FAILED, loOnRegister);
	}

//		// 获取用户信息结果
//		OnReceiveMsgListener loOnGetUserInfo = new OnReceiveMsgListener() {
//			@Override
//			public void onReceiveMsg(Message poMsg) {
//				if (moProgress != null && moProgress.isShowing())
//					moProgress.dismiss();
//				switch (poMsg.what) {
//					case MsgTypes.GET_MY_USER_INFO_SUCCESS:
//						Map<String, String> lmUserInfo = (Map<String, String>) poMsg.obj;
//						Utils.setCfg(Register2Activity.this, Constants.USER_SF_NAME, lmUserInfo);
//						Intent intent = new Intent(LoginStatusChangeReceiver.LOGIN_STATUS_ChANGE_ACTION);
//						intent.setPackage(getApplicationContext().getPackageName());
//						// 发送登录状态改变广播
//						getApplicationContext().sendBroadcast(intent);
//						setResult(RESULT_OK);
//						/* 注册成功获取用户信息之后，关闭页面 */
//						finish();
//						break;
//					case MsgTypes.GET_MY_USER_INFO_FAILED:
//						break;
//				}
//			}
//		};
//		registerMsgListener(MsgTypes.GET_MY_USER_INFO_SUCCESS, loOnGetUserInfo);
//		registerMsgListener(MsgTypes.GET_MY_USER_INFO_FAILED, loOnGetUserInfo);
//}

	@Override
	public void onBackPressed() {
		TelephoneUtil.hideSoftInput(this);
		super.onBackPressed();
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {
		OnSelectGender loOnSelectGender = new OnSelectGender();
		moLlFemale.setOnClickListener(loOnSelectGender);
		moLlMale.setOnClickListener(loOnSelectGender);
		moBtnSubmit.setOnClickListener(new OnSubmit());
		mBackLayout.setOnClickListener(new OnBack());
	}

/***************************************
 * 事件处理器
 ***********************************************/
private class OnBack implements OnClickListener {
	@Override
	public void onClick(View v) {
		onBackPressed();
	}
}

private class OnSelectGender implements OnClickListener {
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.register2_ll_male) {
			moRbMale.setChecked(true);
			moRbFemale.setChecked(false);
		} else if (v.getId() == R.id.register2_ll_female) {
			moRbFemale.setChecked(true);
			moRbMale.setChecked(false);
		}
		showToast(R.string.a_userinfo_input_sex_not_modify, TOAST_SHORT);
	}
}

private class OnSubmit implements OnClickListener {
	@Override
	public void onClick(View v) {
		// 1 获取用户输入
		String lsNickname = moEtNickname.getText().toString().replaceAll("\\s+", "");
		String lsPassword = moEtPassword.getText().toString();

		// 检验昵称是否合法
		if (!lsNickname.matches(Constants.REGULAR_NICKNAME)) {
			showToast(R.string.please_input_nickname, TOAST_SHORT);
			moEtNickname.requestFocus();
			return;
		}

		int length = lsPassword.length();
		if (length < Constants.PWD_MIN_LENGHT || length > Constants.PWD_MAX_LENGHT) {
			String text = getString(R.string.a_userinfo_pwd_length_limit, Constants.PWD_MIN_LENGHT,
					Constants.PWD_MAX_LENGHT);
			moEtPassword.requestFocus();
			showToast(text, TOAST_SHORT);
			return;
		}

		// 判断是否已选性别
		if (!(moRbMale.isChecked() || moRbFemale.isChecked())) {
			showToast(R.string.a_userinfo_sex_selected, TOAST_SHORT);
			return;
		}
		// 3 注册
		moProgress = Utils.showProgress(Register2Activity.this);
		try {
			Business.register(Register2Activity.this, lsPassword, lsNickname, null,
					moRbMale.isChecked() ? Consts.GENDER_MALE : Consts.GENDER_FEMALE);
		} catch (Exception e) {
			moProgress.dismiss();
			showToast("内部错误，请联系APP相关人员", TOAST_LONG);
		}
	}
}
}
