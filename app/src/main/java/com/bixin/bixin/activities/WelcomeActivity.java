package com.bixin.bixin.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.bixin.bixin.App;
import com.bixin.bixin.callback.LevelInfoReceiverListener;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Consts;
import com.bixin.bixin.common.JacksonUtil;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.OperationHelper;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.bean.HttpNetConstants;
import com.bixin.bixin.config.AppConfig;
import com.bixin.bixin.config.DomainConfig;
import com.bixin.bixin.config.UserInfoConfig;
import com.bixin.bixin.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.PackageUtil;
import com.bixin.bixin.library.util.TelephoneUtil;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.framework.net.impl.CallbackDataHandle;
import com.lonzh.lib.network.HttpSession;
import com.lonzh.lib.network.JSONParser;
import com.lonzh.lib.network.LZCookieStore;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import cn.tongdun.android.shell.FMAgent;
import cn.tongdun.android.shell.exception.FMException;
import tv.live.bx.R;

public class WelcomeActivity extends FragmentActivity {

	private ImageView mWelcomeImage;

	protected Activity mActivity;
	protected Handler mHandler = new MyHandler(this);
	// Toast相关
	protected static String TAG = "BaseFragmentActivity";
	private long mobileBindInterval = 0;                //绑定手机提示显示间隔时间
	private long mCurLauchTime;                        //本次启动时间
	private boolean mReportFlag = false;                //是否已经上报过

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//解决切到后台，然后点击桌面图标，重新进入应用，而不是启动之前的activity
		//用于判断这个Activity的启动标志，看它所在的应用是不是从后台跑到前台的。如果是，则直接把它finish（）掉，然后系统会去Activity启动历史栈查询上一个activity，然后再新建它，所以还原到了我们按home键出去的那个界面
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
			finish();
			return;
		}
		mActivity = this;
		TAG = getClass().getSimpleName();
		setContentView(getLayoutRes());
		OperationHelper.onEvent(App.mContext, "openApp", null);
		// 获取当前时间
		mCurLauchTime = System.currentTimeMillis() / 1000;
		mobileBindInterval = AppConfig.getInstance().mobileBindAlertInterval;

		initMembers(); // 初始化成员变量
		initWidgets(); // 初始化控件
		setEventsListeners(); // 设置事件处理器
		signVerify();
		initData();
		// 在android
		// //
		// 2.3上运行时报android.os.NetworkOnMainThreadException异常，在2.3中，访问网络不能在主程序中进行
		// StrictMode.setThreadPolicy(new
		// StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().
		// .detectNetwork().penaltyLog().build());
		// StrictMode.setVmPolicy(new
		// StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
		// .detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().build();
			StrictMode.setThreadPolicy(policy);
		}
	}

	protected int getLayoutRes() {
		return R.layout.activity_welcome;
	}

	protected void initMembers() {
		mWelcomeImage = (ImageView) findViewById(R.id.welcome_ad);
		ImageLoaderUtil.getInstance().loadImage(mWelcomeImage, AppConfig.getInstance().androidLaunghPic);
		startTimer();
	}

	/**
	 * 签名验证
	 *
	 * @return
	 */
	private void signVerify() {
		String signMd5 = PackageUtil.getSignMd5(mActivity.getApplicationContext()).toLowerCase();
		EvtLog.i(TAG, "sign md5:" + signMd5);
		//如果验证不通过
		if (!Constants.COMMON_SIGN_MD5.equals(signMd5)) {
			Toast.makeText(mActivity, R.string.welcome_sign_error_tip, Toast.LENGTH_LONG);
			finish();
		}
	}

	public void initWidgets() {
	}

	protected void setEventsListeners() {
	}

	protected void initData() {
		try {
//			String httpDomainLists = DomainConfig.getInstance().http_domain_lists;
//
//			ArrayList<String> arrayList = new ArrayList<>();
//			arrayList.add(HttpNetConstants.BASE_HTTP_DOMAIN);
//
//			if (!TextUtils.isEmpty(httpDomainLists)) {
//				JSONArray temp = new JSONArray(httpDomainLists);
//				for (int i = 0; i < temp.length(); i++) {
//					arrayList.add(temp.getString(i));
//				}
//			}
//			requestHttpDomain(arrayList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String sessionId = HttpSession.getInstance(mActivity).getCookie("PHPSESSID");
		// 本地cookie 没有sessionId
		if (TextUtils.isEmpty(sessionId)) {
			BusinessUtils.appInit(App.mContext, new CallbackDataHandle() {
				@Override
				public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
					if (success) {
						reportDeviceInfo();
						mReportFlag = true;
					} else {
						mReportFlag = false;
					}
				}
			});
		} else {
			mReportFlag = true;
			reportDeviceInfo();
		}
		checkLevelUpdate();
	}

	// 上报 极光注册号
	private void reportRegisterId() {
		// 获取极光注册号，传给后台，该过程不影响app启动
		String registerId = JPushInterface.getRegistrationID(App.mContext);
		if (!TextUtils.isEmpty(registerId)) {
			BusinessUtils.reportRegisterId(App.mContext, new CallbackDataHandle() {
				@Override
				public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
					if (success) {
						// 此参数如果有数据，在JPushReceiver不需要再执行上报
						AppConfig.getInstance().updateJpushRegistrationStatus(true);
					}
				}
			}, registerId);
		}
	}

	/**
	 * 上报 极光ID
	 * 上报 设备ID
	 * 初始化并上报 同盾
	 */
	private void reportDeviceInfo() {
		// 上传设备号
		BusinessUtils.reportDeviceId(mActivity, TelephoneUtil.getDeviceId(mActivity), TelephoneUtil.getMacAddress(mActivity), TelephoneUtil.getDeviceImei(mActivity));
		reportRegisterId();
		// 同盾初始化
		try {
//			if (BuildConfig.DEBUG) {
//				FMAgent.init(App.mContext, FMAgent.ENV_SANDBOX);
//			} else {
			FMAgent.init(App.mContext, FMAgent.ENV_PRODUCTION);
//			}
		} catch (FMException e) {
			e.printStackTrace();
		}
		// 同盾
		String blackBox = FMAgent.onEvent(mActivity);
		BusinessUtils.reportBlackBox(mActivity, blackBox);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart(TAG);
		MobclickAgent.onResume(this);
		JPushInterface.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
		MobclickAgent.onPageEnd(TAG);
		MobclickAgent.onPause(this);
		JPushInterface.onPause(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PhoneBindActivity.REQUEST_PHONE_BIND_CODE) {
			if (UserInfoConfig.getInstance().isFirstLogin && Utils.isRecommendShow()) {
				// 首次登陆，进入推荐关注页面
				ActivityJumpUtil.gotoActivity(mActivity, RecommendActivity.class, true, null, null);
			} else {
				ActivityJumpUtil.welcomeToMainActivity(this);
			}
		}
	}

	protected void handleMessage(Message msg) {
		EvtLog.e(TAG, "CurTime:" + mCurLauchTime + "LastTime:" + UserInfoConfig.getInstance().mLastLauchTimes + "interval:" + mobileBindInterval);
		switch (msg.what) {
			case MsgTypes.WELCOME_ACTIVITY_WAIT_TIMEOUT:
				// 如果未登录进入登录页面
				if (!AppConfig.getInstance().isLogged) {
					ActivityJumpUtil.toLoginActivity(mActivity, mReportFlag);
				}
//				else if (TextUtils.isEmpty(UserInfoConfig.getInstance().mobile) &&
//						mCurLauchTime - UserInfoConfig.getInstance().mLastLauchTimes >= mobileBindInterval) {
//					// 未绑定手机 && 并且此次启动时间 - 上次记录时间(为记录默认为0) >= 显示时间间隔
//					UserInfoConfig.getInstance().updateLaunchTime(mCurLauchTime);
//
//					ActivityJumpUtil.toPhoneBindActivity(WelcomeActivity.this, PhoneBindActivity.REQUEST_PHONE_BIND_CODE, true);
//				}
				else {
					ActivityJumpUtil.welcomeToMainActivity(WelcomeActivity.this);
				}
				break;
			case MsgTypes.GET_CONFIG_INFO_SUCCESS:
				ImageLoaderUtil.getInstance().loadImage(mWelcomeImage, AppConfig.getInstance().androidLaunghPic);
				break;

			default:
				break;
		}
	}

	private void startTimer() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mHandler.sendEmptyMessage(MsgTypes.WELCOME_ACTIVITY_WAIT_TIMEOUT);
			}
		}, Consts.DURATION_WELCOME_ACTIVITY);
	}

	private void requestHttpDomain(final ArrayList<String> array, final int position) {
		try {
			if (array == null || position >= array.size()) {
				return;
			}
			final String current_http_domain = array.get(position);
			BusinessUtils.getHttpDomain(mActivity, new CallbackDataHandle() {
				@Override
				public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
					try {
						if (success) {
							DomainConfig.getInstance().parseFromJson((JSONObject) result);

							LZCookieStore.updateCookieDomain(mActivity);

							//更新当前可用的http domain
							DomainConfig.getInstance().updateSafeHttpDomain(current_http_domain);

						} else {
							if (errorCode == HttpNetConstants.SENT_STATUS_DNS_ERROR) {
//								requestHttpDomain(array, position + 1);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 等级配置更新
	 */
	public void checkLevelUpdate() {
		if (TelephoneUtil.isNetworkAvailable()) {
			BusinessUtils.getConfigInfo(this, new ConfigUpdateReceiverListener());
		}
	}


	public void sendMsg(Message msg) {
		if (mHandler != null) {
			mHandler.sendMessage(msg);
		} else {
			handleMessage(msg);
		}
	}

	/**
	 * 静态的Handler对象
	 */
	private static class MyHandler extends Handler {

		private final WeakReference<WelcomeActivity> mActivity;

		public MyHandler(WelcomeActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			WelcomeActivity activity = mActivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}

	/**
	 * 配置更新回调 Function: TODO ADD FUNCTION. <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * reportDeviceId
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private class ConfigUpdateReceiverListener implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "ConfigUpdateReceiverListener success " + success + " errorCode" + errorCode);
			if (success) {
				try {
					AppConfig config = JacksonUtil.readValue(result.toString(), AppConfig.class);
					AppConfig.getInstance().updateInfo(config);
					int levelVersion = Integer.parseInt(AppConfig.getInstance().levelConfigVersion);
					int currentVersion = Integer.parseInt(AppConfig.getInstance().currentLevelConfigVersion);
					// 存储后台下发的手机绑定弹出间隔时间
					mobileBindInterval = AppConfig.getInstance().mobileBindAlertInterval;

					Message msg = Message.obtain();
					msg.what = MsgTypes.GET_CONFIG_INFO_SUCCESS;
					sendMsg(msg);
					if (levelVersion > currentVersion) {
						BusinessUtils.getLevelConfigInfo(App.mContext, new LevelInfoReceiverListener(AppConfig.getInstance().levelConfigVersion));
					}

					int modelConfigVersion = Integer.parseInt(AppConfig.getInstance().medalsConfigVersion);
					int currentModelVersion = Integer.parseInt(AppConfig.getInstance().currentMedalsConfigVersion);
					if (modelConfigVersion > currentModelVersion) {
						BusinessUtils.getModelConfigInfo(App.mContext, new ModelInfoReceiverListener(AppConfig.getInstance().medalsConfigVersion));
					}
					//得到上报日志域名
					DomainConfig.getInstance().updateSafeStatDomain(AppConfig.getInstance().statDomain);

					//webview 页面域名
					DomainConfig.getInstance().updateSafeWebDomain(AppConfig.getInstance().urlDomain);


				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}
	}

	/**
	 * 勋章配置更新回调 ，下载勋章图标 <br/>
	 *
	 * @author Live
	 * @since JDK 1.6
	 */
	private class ModelInfoReceiverListener implements CallbackDataHandle {
		private String mModelConfigVersion;

		public ModelInfoReceiverListener(String modelConfigVersion) {
			this.mModelConfigVersion = modelConfigVersion;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "ModelInfoReceiverListener success " + success + " errorCode" + errorCode);
			if (success) {
				try {
					Map<String, String> data = JSONParser.parseOne(String.valueOf(result));
					// 勋章的基本url(不包含名称)
					String imageBase = data.get("imageBase");
					AppConfig.getInstance().updateCurrentMedalsConfigVersion(mModelConfigVersion, imageBase);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}
	}
}
