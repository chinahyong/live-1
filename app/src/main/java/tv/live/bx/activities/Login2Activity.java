package tv.live.bx.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import com.lonzh.lib.network.HttpSession;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Set;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.callback.MyUserInfoCallbackDataHandle;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.receiver.LoginStatusChangeReceiver;
import tv.live.bx.util.ActivityJumpUtil;

/**
 * @author Live
 * @version 2016/5/31 2.4.2
 * @title Login2Activity.java Description:手机登录页面
 */
public class Login2Activity extends BaseFragmentActivity {
	private int REQUEST_REGISTER = 103;
	private EditText moEtAccount, moEtPwd;
	private TextView moTvForgetPwd, moBtnRegister;
	private Button moBtnLogin;
	/**
	 * 软键盘相关
	 */
	protected InputMethodManager mInputManager;
	private AlertDialog moProgress;
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

	}

	@Override
	public void initWidgets() {
		mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		backLayout = (RelativeLayout) findViewById(R.id.register1_top_left);
		moEtAccount = (EditText) findViewById(R.id.login_et_account);
		moEtPwd = (EditText) findViewById(R.id.login_et_pwd);
		moTvForgetPwd = (TextView) findViewById(R.id.login_tv_forget_pwd);
		moBtnLogin = (Button) findViewById(R.id.login_btn_login);
		moBtnRegister = (TextView) findViewById(R.id.login_btn_register);
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
					mBtnLoginBottomHeight = FeizaoApp.metrics.heightPixels - position[1] - moBtnLogin.getHeight();
				}
				if (heightDiff > 100 && mBtnLoginBottomHeight <= heightDiff) {
					((View) moBtnLogin.getParent()).scrollTo(0, heightDiff - mBtnLoginBottomHeight + scaleHeight);
				} else
					((View) moBtnLogin.getParent()).scrollTo(0, 0);
			}
		});
	}


	private void showProgress() {
		if (moProgress != null && moProgress.isShowing()) {
			return;
		}
		moProgress = Utils.showProgress(this);
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
			MobclickAgent.onEvent(FeizaoApp.mContext, "forgetPassword");
			gotoActivity(GetBackPwdActivity.class, false, null, null);
		}
	}

	private class OnLogin implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			MobclickAgent.onEvent(FeizaoApp.mContext, "login");
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
			// 3 登录
			showProgress();
			try {
				BusinessUtils.login(Login2Activity.this, lsUsername, lsPassword, new LoginCallbackDataHandle(Login2Activity.this));
			} catch (Exception e) {
				e.printStackTrace();
				moProgress.dismiss();
				// Business.getPubKey(getApplicationContext());
				showToast("内部错误，请联系APP相关人员,请重试", TOAST_LONG);
			}
		}
	}

	private class OnRegister implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			MobclickAgent.onEvent(FeizaoApp.mContext, "register");
			ActivityJumpUtil.gotoActivityForResult(Login2Activity.this, Register1Activity.class, REQUEST_REGISTER, null,
					null);
		}
	}

	private class OnBack implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (moProgress != null && moProgress.isShowing()) {
			moProgress.dismiss();
		}
		switch (msg.what) {
			case MsgTypes.LOGIN_SUCCESS:
				AppConfig.getInstance().updateLoginStatus(true);
				String lsUid = HttpSession.getInstance(Login2Activity.this).getCookie("uid");
				EvtLog.e("Login2Activity", "lsUid:" + lsUid);
				UserInfoConfig.getInstance().updateUserId(lsUid);
				JPushInterface.setAliasAndTags(Login2Activity.this, lsUid, null, new TagAliasCallback() {
					@Override
					public void gotResult(int i, String s, Set set) {

					}
				});
				BusinessUtils.getMyUserInfo(Login2Activity.this, new MyUserInfoCallbackDataHandle(mHandler));
				UserInfoConfig.getInstance().updateUsername(moEtAccount.getText().toString());
				showToast("登录成功", TOAST_SHORT);
				break;
			case MsgTypes.LOGIN_FAILED:
				showToast(String.valueOf(msg.obj), TOAST_SHORT);
				break;
			case MsgTypes.GET_MY_USER_INFO_SUCCESS:
				// 发送登录状态改变广播
				Intent intent = new Intent(LoginStatusChangeReceiver.LOGIN_STATUS_ChANGE_ACTION);
				intent.setPackage(getApplicationContext().getPackageName());
				// 发送登录状态改变广播
				getApplicationContext().sendBroadcast(intent);
				setResult(RESULT_OK);
				finish();
				break;
			case MsgTypes.GET_MY_USER_INFO_FAILED:
				break;
		}
	}

	private static class LoginCallbackDataHandle implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public LoginCallbackDataHandle(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "UserInfoCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.LOGIN_SUCCESS;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					BaseFragmentActivity meFragment =  mFragment.get();
					// 如果fragment未回收，发送消息
					if (meFragment != null)
						meFragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.LOGIN_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity meFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.sendMsg(msg);
			}
		}
	}

}
