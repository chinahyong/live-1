package com.bixin.bixin.user.act;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bixin.bixin.App;
import com.bixin.bixin.activities.NotifyPasswordActivity;
import com.bixin.bixin.activities.Register1Activity;
import com.bixin.bixin.base.act.BaseFragmentActivity;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.config.UserInfoConfig;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.receiver.LoginStatusChangeReceiver;
import com.bixin.bixin.user.bean.UserBean;
import com.bixin.bixin.user.presenter.UserPresenter;
import com.bixin.bixin.user.view.IUserView;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;

import tv.live.bx.R;

/**
 * @author Live
 * @version 2016/5/31 2.4.2
 * @title LoginPhoneActivity.java Description:手机登录页面
 */
public class LoginPhoneActivity extends BaseFragmentActivity {
	private UserPresenter mUserPresenter;
	private int REQUEST_REGISTER = 103;
	private EditText moEtAccount, moEtPwd;
	private TextView moTvForgetPwd, moBtnRegister;
	private Button moBtnLogin;
	/**
	 * 软键盘相关
	 */
	protected InputMethodManager mInputManager;
	/**
	 * 登陆按钮距底部距离
	 */
	private int mBtnLoginBottomHeight = -1;
	/**
	 * 返回按钮
	 */
	private RelativeLayout backLayout;

	public int getColorPrimary() {
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(android.R.color.transparent, typedValue, true);
		return typedValue.data;
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_login2;
	}

	@Override
	protected void initMembers() {
		mUserPresenter = new UserPresenter(mUserView);
	}

	@Override
	public void initWidgets() {
		mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		backLayout = findViewById(R.id.register1_top_left);
		moEtAccount = findViewById(R.id.login_et_account);
		moEtPwd = findViewById(R.id.login_et_pwd);
		moTvForgetPwd = findViewById(R.id.login_tv_forget_pwd);
		moBtnLogin = findViewById(R.id.login_btn_login);
		moBtnRegister = findViewById(R.id.login_btn_register);
	}

	@Override
	protected void setEventsListeners() {
		moTvForgetPwd.setOnClickListener(new OnGetBackPwd());
		moBtnLogin.setOnClickListener(new OnLogin());
		moBtnRegister.setOnClickListener(new OnRegister());
		backLayout.setOnClickListener(new OnBack());
		keyBoardChangedListener();
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		String username = UserInfoConfig.getInstance().account;
		if (!TextUtils.isEmpty(username))
			moEtAccount.setText(username);
		moBtnRegister.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		moTvForgetPwd.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
	}

	@SuppressLint("NewApi")
	private void keyBoardChangedListener() {
		final View decordView = this.getWindow().getDecorView();
		final int scaleHeight = Utils.dip2px(this, 10);
		decordView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				Rect rect = new Rect();
				decordView.getWindowVisibleDisplayFrame(rect);
				int disHeight = rect.bottom - rect.top;
				// 比较Activity根布局与当前布局的大小
				int heightDiff = decordView.getRootView().getHeight() - disHeight;
				if (mBtnLoginBottomHeight == -1) {
					int position[] = new int[2];
					moBtnLogin.getLocationOnScreen(position);
					mBtnLoginBottomHeight = App.metrics.heightPixels - position[1] - moBtnLogin.getHeight();
				}
				if (heightDiff > 100 && mBtnLoginBottomHeight <= heightDiff) {
					((View) moBtnLogin.getParent()).scrollTo(0, heightDiff - mBtnLoginBottomHeight + scaleHeight);
				} else
					((View) moBtnLogin.getParent()).scrollTo(0, 0);
			}
		});
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_REGISTER) {
			if (resultCode == RESULT_OK) {
				setResult(RESULT_OK);
			}
			onBackPressed();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private class OnGetBackPwd implements View.OnClickListener {
		@Override
		public void onClick(View arg0) {
			MobclickAgent.onEvent(App.mContext, "forgetPassword");
			gotoActivity(NotifyPasswordActivity.class, false, null, null);
		}
	}

	private class OnLogin implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			MobclickAgent.onEvent(App.mContext, "iLogin");
			// 1 获取用户输入
			String lsUsername = moEtAccount.getText().toString();
			String lsPassword = moEtPwd.getText().toString();

			// 2 检查用户输入
			if (Utils.isStrEmpty(lsUsername.trim())) {
				showToast(R.string.please_input_username, TOAST_SHORT);
				return;
			}
			if (Utils.isStrEmpty(lsPassword)) {
				showToast(R.string.please_input_password, TOAST_SHORT);
				return;
			}
			if (lsUsername.replace(" ", "").length() < 6) {
				showToast(R.string.username_min_length, TOAST_SHORT);
				return;
			}
			if (lsPassword.length() < 6) {
				showToast(R.string.password_min_length, TOAST_SHORT);
				return;
			}
			EvtLog.d(TAG, "showProgress");
			mUserPresenter.login(lsUsername, lsPassword);
//			// 3 登录
//			showProgress();
//			try {
//				BusinessUtils.iLogin(LoginPhoneActivity.this, lsUsername, lsPassword, new LoginCallbackDataHandle(LoginPhoneActivity.this));
//			} catch (Exception e) {
//				e.printStackTrace();
//				moProgress.dismiss();
//				// Business.getPubKey(getApplicationContext());
//				showToast("内部错误，请联系APP相关人员,请重试", TOAST_LONG);
//			}
		}
	}

	private class OnRegister implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			MobclickAgent.onEvent(App.mContext, "register");
			ActivityJumpUtil.gotoActivityForResult(LoginPhoneActivity.this, Register1Activity.class, REQUEST_REGISTER, null,
					null);
		}
	}

	private class OnBack implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	}

	private IUserView mUserView = new IUserView() {
		@Override
		public void iLogin(boolean succ) {
			if (succ) {
				mUserPresenter.getUserInfo(0);
				UserInfoConfig.getInstance().updateUsername(moEtAccount.getText().toString());
			}
		}

		@Override
		public void iUserInfo(boolean succ, @NotNull UserBean userBean) {
			if (succ) {
				// 发送登录状态改变广播
				Intent intent = new Intent(LoginStatusChangeReceiver.LOGIN_STATUS_ChANGE_ACTION);
				intent.setPackage(App.mContext.getPackageName());
				// 发送登录状态改变广播
				getApplicationContext().sendBroadcast(intent);
				setResult(RESULT_OK);
				finish();
			}
		}

		@NotNull
		@Override
		public Context getContext() {
			return LoginPhoneActivity.this;
		}
	};

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MsgTypes.LOGIN_SUCCESS:
				break;
			case MsgTypes.LOGIN_FAILED:
				showToast(String.valueOf(msg.obj), TOAST_SHORT);
				break;
			case MsgTypes.GET_MY_USER_INFO_SUCCESS:
				finish();
				break;
			case MsgTypes.GET_MY_USER_INFO_FAILED:
				break;
		}
	}
}
