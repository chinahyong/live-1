package tv.live.bx.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lonzh.lib.network.HttpSession;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.tongdun.android.shell.FMAgent;
import cn.tongdun.android.shell.exception.FMException;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.callback.MyUserInfoCallbackDataHandle;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.common.WebConstants;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.TelephoneUtil;
import tv.live.bx.receiver.LoginStatusChangeReceiver;
import tv.live.bx.util.ActivityJumpUtil;

import static tv.live.bx.FeizaoApp.mConctext;

public class LoginActivity extends BaseFragmentActivity {
	public static String TAG = "LoginActivity";
	public static final int RESULT_CODE_OK = 100;
	public static final int REQUEST_LOGIN_BY_PHONE = 100;
	public static final int RESULT_CODE_CANCELLED = 101;
	private UMShareAPI umShareAPI = null;

	private ImageView mLoginBg;
	private ImageView moLlLoginByQq, mLoginByWeixin, mLoginByWeibo, mLoginByPhone;
	private TextView mTvProtocal;

	private AlertDialog moProgress;
	//	private long mobileBindInterval = 0;                //绑定手机提示显示间隔时间
//	private long mLastLauchTimes = 0;             // 上一次启动app时间
//	private long mCurLauchTime;                        //本次启动时间
	private boolean mReportFlag = false;            //是否上报过设备信息

	public int getColorPrimary() {
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(android.R.color.transparent, typedValue, true);
		return typedValue.data;
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_login;
	}

	@Override
	protected void initMembers() {
		umShareAPI = UMShareAPI.get(this.getApplicationContext());        //初始化umeng share api
	}

	@Override
	public void initWidgets() {
		mLoginBg = (ImageView)findViewById(R.id.iv_login_bg);
		moLlLoginByQq = (ImageView) findViewById(R.id.login_ll_by_qq);
		mLoginByWeixin = (ImageView) findViewById(R.id.login_ll_by_weixin);
		mLoginByWeibo = (ImageView) findViewById(R.id.login_ll_by_weibo);
		mLoginByPhone = (ImageView) findViewById(R.id.login_ll_by_phone);
		mTvProtocal = (TextView) findViewById(R.id.login_agree_protocal);
		Paint paint = mTvProtocal.getPaint();
		paint.setFlags(Paint.UNDERLINE_TEXT_FLAG);
		paint.setAntiAlias(true);
	}

	@Override
	protected void setEventsListeners() {
		OnClickListener listener = new NewClickListener();
		mLoginByWeixin.setOnClickListener(listener);
		moLlLoginByQq.setOnClickListener(listener);
		mLoginByWeibo.setOnClickListener(listener);
		mLoginByPhone.setOnClickListener(listener);
		mTvProtocal.setOnClickListener(listener);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		if (getIntent() != null) {
			mReportFlag = getIntent().getBooleanExtra("reportFlag", false);
		}
		//如果配置文件不为空，则显示网络配置的登录背景
		if(!TextUtils.isEmpty(AppConfig.getInstance().wallPaper)){
			ImageLoaderUtil.with().loadImage(mActivity, mLoginBg, AppConfig.getInstance().wallPaper, 0, R.drawable.login_bg2);
		}

	}

	private void showProgress() {
		if (moProgress != null && moProgress.isShowing()) {
			return;
		}
		moProgress = Utils.showProgress(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 有些情况第三方分享收不到回调，再次关闭dialog
		dismissDialog();
	}

	/**
	 * 授權回調
	 */
	private UMAuthListener umAuthListener = new UMAuthListener() {
		@Override
		public void onStart(SHARE_MEDIA share_media) {
			showTips("授权开始");
			// 开始授权
			EvtLog.d(TAG, "showProgress");
			showProgress();
		}

		@Override
		public void onComplete(SHARE_MEDIA platform, int i, Map<String, String> value) {
			EvtLog.d(TAG, "onComplete " + value.toString());
			if (LoginActivity.this.isFinishing()) {
				return;
			}
			String psOpenId = value.get("openid");
			String psAccessToken = value.get("access_token");
			String expiresIn = value.get("expires_in");
			if (platform == SHARE_MEDIA.WEIXIN) {
				// 获取unionid
				String unionid = value.get("unionid");
				String refreshToken = value.get("refresh_token");
				BusinessUtils.loginByWeixin(LoginActivity.this, psAccessToken, psOpenId, refreshToken, unionid,
						expiresIn, new WxCallbackDataHandle(LoginActivity.this));
			} else if (platform == SHARE_MEDIA.QQ) {
				// 3 QQ登录
				BusinessUtils.loginByQQ(LoginActivity.this, psAccessToken, psOpenId, expiresIn, new WxCallbackDataHandle(LoginActivity.this));
			} else if (platform == SHARE_MEDIA.SINA) {
				// 3 新浪微博登录
				String uid = value.get("uid");
				String accessToken = value.get("accessToken");
				if (TextUtils.isEmpty(accessToken)) {
					accessToken = value.get("access_token");
				}
				expiresIn = value.get("expiration");
				BusinessUtils.loginByWeibo(LoginActivity.this, accessToken, uid, expiresIn, new WxCallbackDataHandle(LoginActivity.this));
			}
		}

		@Override
		public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
			Toast.makeText(LoginActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
			dismissDialog();
		}

		@Override
		public void onCancel(SHARE_MEDIA share_media, int i) {
			Toast.makeText(LoginActivity.this, "授权取消", Toast.LENGTH_SHORT).show();
			dismissDialog();
		}
	};

	private void dismissDialog() {
		if (moProgress != null && moProgress.isShowing()) {
			moProgress.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CODE_CANCELLED);
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 防止umeng分享内存泄漏
		UMShareAPI.get(this).release();
	}

	// 如果有使用任一平台的SSO授权, 则必须在对应的activity中实现onActivityResult方法, 并添加如下代码
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 根据requestCode获取对应的SsoHandler
		umShareAPI.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_LOGIN_BY_PHONE) {
			if (resultCode == RESULT_OK) {
				loginSuccessJump();
			}
		} else if (requestCode == PhoneBindActivity.REQUEST_PHONE_BIND_CODE) {
			if (UserInfoConfig.getInstance().isFirstLogin && Utils.isRecommendShow()) {
				// 首次登陆，进入推荐关注页面
				ActivityJumpUtil.gotoActivity(mActivity, RecommendActivity.class, true, null, null);
			} else {
				ActivityJumpUtil.welcomeToMainActivity(this);
			}
		}
	}


	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		dismissDialog();
		switch (msg.what) {
			case MsgTypes.LOGIN_BY_QQ_SUCCESS:
				AppConfig.getInstance().updateLoginStatus(true);
				String lsUid = HttpSession.getInstance(LoginActivity.this).getCookie("uid");
				// 用户登录，先清空之前用户缓存
				UserInfoConfig.logout();
				UserInfoConfig.getInstance().updateUserId(lsUid);
				JPushInterface.setAliasAndTags(LoginActivity.this, lsUid, null, new TagAliasCallback() {
					@Override
					public void gotResult(int arg0, String arg1, Set<String> arg2) {

					}
				});
				BusinessUtils.getMyUserInfo(LoginActivity.this, new MyUserInfoCallbackDataHandle(mHandler));
				showToast("登录成功", TOAST_SHORT);
				System.out.println("lsUid..........." + lsUid);
				break;
			case MsgTypes.LOGIN_BY_QQ_FAILED:
				showToast(String.valueOf(msg.obj), TOAST_SHORT);
				break;
			case MsgTypes.GET_MY_USER_INFO_SUCCESS:
				// 发送登录状态改变广播
				Intent intent = new Intent(LoginStatusChangeReceiver.LOGIN_STATUS_ChANGE_ACTION);
				intent.setPackage(getApplicationContext().getPackageName());
				// 发送登录状态改变广播
				getApplicationContext().sendBroadcast(intent);

				loginSuccessJump();
				break;
			case MsgTypes.GET_MY_USER_INFO_FAILED:
				break;
		}
	}

	// 上报极光注册号
	private void reportRegisterId() {
		// 获取极光注册号，传给后台，该过程不影响app启动
		String registerId = JPushInterface.getRegistrationID(mConctext);
		BusinessUtils.reportRegisterId(mConctext, new CallbackDataHandle() {
			@Override
			public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
				if (success) {
					// 此参数如果有数据，在JPushReceiver不需要再执行上报
					AppConfig.getInstance().updateJpushRegistrationStatus(true);
				}
			}
		}, registerId);
	}

	/**
	 * 上报 极光ID
	 * 上报 设备ID
	 * 初始化并上报 同盾
	 */
	private void reportDeviceInfo() {
		reportRegisterId();
		// 上传设备号
		BusinessUtils.reportDeviceId(mActivity, TelephoneUtil.getDeviceId(mActivity), TelephoneUtil.getMacAddress(mActivity), TelephoneUtil.getDeviceImei(mActivity));
		// 同盾初始化
		try {
			FMAgent.init(mConctext, FMAgent.ENV_PRODUCTION);
		} catch (FMException e) {
			e.printStackTrace();
		}
		// 同盾
		String blackBox = FMAgent.onEvent(mActivity);
		BusinessUtils.reportBlackBox(mActivity, blackBox);
	}

	/**
	 * 登录成功后，下一步跳转
	 */
	private void loginSuccessJump() {
		// 获取当前时间
		long mCurLauchTime = System.currentTimeMillis() / 1000;
		long mLastLauchTimes = UserInfoConfig.getInstance().mLastLauchTimes;
		int mobileBindInterval = AppConfig.getInstance().mobileBindAlertInterval;
		EvtLog.e(TAG, "CurTime:" + mCurLauchTime + "LastTime:" + mLastLauchTimes + "interval:" + mobileBindInterval);
		setResult(RESULT_CODE_OK);
		// 之前没有上报，登录成功上报一次
		if (!mReportFlag) {
			mReportFlag = true;
			reportDeviceInfo();
		}
//		if (TextUtils.isEmpty(UserInfoConfig.getInstance().mobile) &&
//				mCurLauchTime - mLastLauchTimes >= mobileBindInterval) {
//			// 未绑定手机 && 并且此次启动时间 - 上次记录时间(为记录默认为0) >= 显示时间间隔
//			UserInfoConfig.getInstance().updateLaunchTime(mCurLauchTime);
//			ActivityJumpUtil.toPhoneBindActivity(mActivity, PhoneBindActivity.REQUEST_PHONE_BIND_CODE, true);
//		} else
//			if (UserInfoConfig.getInstance().isFirstLogin && Utils.isRecommendShow()) {
//			// 首次登陆，进入推荐关注页面
//			ActivityJumpUtil.gotoActivity(mActivity, RecommendActivity.class, true, null, null);
//		} else {
		// 如果不是首次登陆，直接进入主页
		ActivityJumpUtil.welcomeToMainActivity(this);
//		}
	}

	public class NewClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.login_ll_by_qq:
					MobclickAgent.onEvent(mConctext, "qqLogin");
					if (!umShareAPI.isInstall(LoginActivity.this, SHARE_MEDIA.QQ)) {
						Toast.makeText(getApplicationContext(), R.string.uninstall_qq_tip, Toast.LENGTH_SHORT).show();
						return;
					}
					umShareAPI.doOauthVerify(LoginActivity.this, SHARE_MEDIA.QQ, umAuthListener);
					break;
				case R.id.login_ll_by_weixin:
					MobclickAgent.onEvent(mConctext, "wechatLogin");
					if (!umShareAPI.isInstall(LoginActivity.this, SHARE_MEDIA.WEIXIN)) {
						Toast.makeText(getApplicationContext(), R.string.uninstall_weixin_tip, Toast.LENGTH_SHORT).show();
						return;
					}
					umShareAPI.deleteOauth(LoginActivity.this, SHARE_MEDIA.WEIXIN, new UMAuthListener() {
						@Override
						public void onStart(SHARE_MEDIA share_media) {

						}

						@Override
						public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
							umShareAPI.doOauthVerify(LoginActivity.this, SHARE_MEDIA.WEIXIN, umAuthListener);
						}

						@Override
						public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {

						}

						@Override
						public void onCancel(SHARE_MEDIA share_media, int i) {

						}
					});
					break;
				case R.id.login_ll_by_weibo:
					MobclickAgent.onEvent(mConctext, "weiboLogin");
					umShareAPI.doOauthVerify(LoginActivity.this, SHARE_MEDIA.SINA, umAuthListener);
					break;
				case R.id.login_ll_by_phone:
					MobclickAgent.onEvent(mConctext, "phoneLogin");
					gotoActivityForResult(Login2Activity.class, REQUEST_LOGIN_BY_PHONE, null, null);
					break;
				case R.id.login_agree_protocal:
					Map<String, String> webInfo = new HashMap<>();
					webInfo.put(WebViewActivity.URL, WebConstants.getFullWebMDomain(WebConstants.REGISTER_PROTOCOL));
					webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(true));
					ActivityJumpUtil.gotoActivity(LoginActivity.this, WebViewActivity.class, false, WebViewActivity.WEB_INFO,
							(Serializable) webInfo);
					break;
			}
		}
	}

	private static class WxCallbackDataHandle implements CallbackDataHandle {

		private WeakReference<BaseFragmentActivity> mFragment;

		public WxCallbackDataHandle(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "UpdateUserCallbackData success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				msg.what = MsgTypes.LOGIN_BY_QQ_SUCCESS;
				try {
					msg.obj = JSONParser.parseOne((JSONObject) result);
				} catch (Exception e) {
				}
				BaseFragmentActivity meFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.sendMsg(msg);
			} else {
				msg.what = MsgTypes.LOGIN_BY_QQ_FAILED;
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
