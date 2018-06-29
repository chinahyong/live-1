
package com.bixin.bixin.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bixin.bixin.App;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.callback.LevelInfoReceiverListener;
import com.bixin.bixin.callback.MyUserInfoCallbackDataHandle;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.JacksonUtil;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.OperationHelper;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.config.AppConfig;
import com.bixin.bixin.config.UserInfoConfig;
import com.bixin.bixin.database.DatabaseUtils;
import com.bixin.bixin.fragments.BaseFragment;
import com.bixin.bixin.fragments.LiveFragment;
import com.bixin.bixin.fragments.MeFragment;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.TelephoneUtil;
import com.bixin.bixin.listeners.OnUpdateListener;
import com.bixin.bixin.live.activities.LiveBaseActivity;
import com.bixin.bixin.receiver.LoginStatusChangeReceiver;
import com.bixin.bixin.receiver.UserOffLineReceiver;
import com.bixin.bixin.ui.SingleTabWidget;
import com.bixin.bixin.ui.SingleTabWidget.OnTabChangedListener;
import com.bixin.bixin.ui.addpopup.AddMoreWindow;
import com.bixin.bixin.user.act.LoginActivity;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;
import com.framework.net.impl.CallbackDataHandle;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.live.bx.R;

/**
 * Title: CalMainActivity.java Description: 果酱主界面 Copyright: Copyright (c) 2008
 *
 * @version 1.0
 */
public class CalMainActivity extends BaseFragmentActivity implements OnClickListener {
	public static final String EXA_IS_CHECK_UPDATE = "isCheckUpdate";

	private final String FRAGMENT_TAG_FORMAT = "MainFragment_%s";

	/**
	 * 何第一次打开X5webview 就能加载X5内核的方法
	 * 将适用于特殊的场景
	 * 但是注意 额外的冷启动时间
	 */
	public static final int MSG_WEBVIEW_POLLING = 0x10001;

	public static final int TAB_LIVE = 0; // 直播
	// 主页Home替换为社区Community并放到第二页
	// public static final int TAB_HOME = 0; // 主页
	public static final int TAB_ADD = 1; // 增加
	public static final int TAB_ME = 2; // 我

	private BaseFragment mBackHandedFragment;

	/**
	 * 是否已经销毁
	 */
	private boolean mDestoryed = false;
	/**
	 * 是否已上传用户Position
	 */
	private boolean hasUpdatePos = false;

	/**
	 * 底部TAB栏
	 */
	private SingleTabWidget tabWidget;
	private OnTabChangedListener mOnTabChangedListener;
	private AlertDialog mProgress;
	/**
	 * 当前显示的Fragment
	 */
	private int mTabIndex = -1;

	private static final int MSG_CK_UPDATE_SUCCESSFUL = 1;
	private FragmentManager fm = getSupportFragmentManager();

	/**
	 * 加号图标
	 */
	private ImageView mMoreAddImage;
	private AddMoreWindow mMoreWindow;
	private boolean isRecordLiveFlag = false;        //是否为录播

	private LoginStatusChangeReceiver loginStatusChangeReceiver;
	private String mRid;        //房间号

	/**
	 * 用户下线广播Receiver
	 */
	private UserOffLineReceiver mUserOffLineReceiver;

	/**
	 * 跳转主界面
	 *
	 * @param context
	 */
	public static void redirectToMe(Context context) {
		Intent intent = new Intent(context, CalMainActivity.class);
		context.startActivity(intent);
		if (context instanceof Activity) {
			((Activity) context).overridePendingTransition(0, R.anim.a_slide_out_right);
		}
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.a_main_cal_main_layout;
	}

	@Override
	public void initWidgets() {
		// 初始化UI
		initUI();
	}

	@Override
	protected void setEventsListeners() {
		registerLoginReceiver();
		//注册用户下线广播Receiver
		registerReceiver();
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		final Intent intent = getIntent();

		// 是否播放页面
		// redirectPlayActivity(intent);

		// 检查版本更新
		checkUpdate(intent);

		// 用户账号相关操作
		// loadCalAccount();

		// 判断App第一次运行的相关操作

		// 环信相关监听设置
		// 清除本地已经过期的聊天消息
		// clearOverdueDiscussMsg();
		// 检查网络
		checkNetwork();

		initTabWidgetData(savedInstanceState);

		//每次进入主Activity都去获取一次用户信息
		BusinessUtils.getMyUserInfo(mActivity, new MyUserInfoCallbackDataHandle(mHandler));

		//没有等级配置信息时，重新请求一遍，暂时的（3.1.5版本更新）
		if (AppConfig.getInstance().mLevelConfigInfo.size() == 0) {
			BusinessUtils.getLevelConfigInfo(App.mContext, new LevelInfoReceiverListener(Constants.COMMON_LEVEL_CONFIG_VERSION));
		}

		//上报用户坐标
		updateUserLocation();
		registerNetWorkChangeReceiver();
	}

	private AMapLocationClient mLocationClient;

	/**
	 * 上报用户地址
	 */
	private void updateUserLocation() {
		if (mLocationClient != null) {
			mLocationClient.startLocation();
			return;
		}
		mLocationClient = new AMapLocationClient(App.mContext);
		mLocationClient.setLocationOption(initMapOption());
		mLocationClient.setLocationListener(locationListener);
		mLocationClient.startLocation();
	}

	/**
	 * location回调监听器
	 */
	private AMapLocationListener locationListener = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation aMapLocation) {
			EvtLog.e(TAG, "onLocationChanged:" + aMapLocation.toString());
			if (CalMainActivity.this.mDestoryed || hasUpdatePos)
				return;
			// 获取位置信息成功
			if (aMapLocation.getErrorCode() == 0 && Utils.isNetAvailable(CalMainActivity.this)) {
				hasUpdatePos = true;
				BusinessUtils.postUserPosition(CalMainActivity.this, String.valueOf(aMapLocation.getLongitude()),
						String.valueOf(aMapLocation.getLatitude()), null);
			} else {
			}
		}
	};

	/**
	 * 初始化高德地图定位参数
	 */
	private AMapLocationClientOption initMapOption() {
		AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);// 高精度定位
		mLocationOption.setOnceLocation(false);        // 是否单次定位  true：定位1次
		mLocationOption.setInterval(1000 * 60 * 30);
		mLocationOption.setOnceLocationLatest(false);        // 是否获取3秒内最精确定位，false	关闭，定位到城市用不到
		mLocationOption.setNeedAddress(false);        //是否返回位置信息(此处说的是地址信息)，false关闭
		return mLocationOption;
	}

	/**
	 * 销毁LocationClient以及监听器
	 */
	private void destroyLocationClient() {
		if (mLocationClient != null) {
			mLocationClient.unRegisterLocationListener(locationListener);
			mLocationClient.onDestroy();
			mLocationClient = null;
		}
	}

	/**
	 * 注册网络状态监听器
	 */
	private void registerNetWorkChangeReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(netWorkChangeReceiver, filter);
	}

	private void unRegisterNetWorkChangeReceiver() {
		unregisterReceiver(netWorkChangeReceiver);
	}

	/**
	 * 监听网络状态，改变的时候上报用户location
	 */
	private BroadcastReceiver netWorkChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (hasUpdatePos) {
				return;
			}
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()) && Utils.isNetAvailable(CalMainActivity.this)) {
				updateUserLocation();
			}
		}
	};

	/**
	 * 直播按钮状态
	 */
	private void liveStatus() {
		// 判断房间号是否为空
		if (AppConfig.getInstance().isLogged && !TextUtils.isEmpty(mRid)) {
			MobclickAgent.onEvent(App.mContext, "clickLiveButton");
			Map<String, String> lmItem = new HashMap<>();
			lmItem.put("rid", mRid);
			lmItem.put("isRecordLive", String.valueOf(isRecordLiveFlag));
			ActivityJumpUtil.gotoActivity(mActivity, PreviewLivePlayActivity.class, false, LiveBaseActivity.ANCHOR_RID,
					(Serializable) lmItem);
			//如果之前不是主播，更新个人信息
			UserInfoConfig.getInstance().updateIsIdVerifiedModerator(true);
			return;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
//		updateMeInfoNum();
		// 起清空通知栏，清除消息条数作用
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (mBackHandedFragment != null && mBackHandedFragment.onBackPressed()) {
				return true;
			}
			// 按返回键弹出对话框
			UiHelper.showConfirmDialog(CalMainActivity.this, R.string.a_main_exit_message, R.string.logout_cancel,
					R.string.determine, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		MobclickAgent.onEvent(App.mContext, "logout");
		// 在onDestroy之前调用，移除所有的Fragment
		removeAllFragment();
		OperationHelper.onEventEnd(App.mContext);
		super.onDestroy();
		// 注销广播
		unregisterLoginReceiver();
		unregisterReceiver();
		unRegisterNetWorkChangeReceiver();
		dismissProgressDialog();
		destroyLocationClient();
		mDestoryed = true;
	}

	/**
	 * r
	 * 初始化UI控件，加载Fragment
	 */
	private void initUI() {
		mMoreAddImage = (ImageView) findViewById(R.id.more_add);
		mMoreAddImage.setOnClickListener(this);
		// 初始化底部TabWidget
		tabWidget = (SingleTabWidget) findViewById(R.id.main_tabs);
		tabWidget.setLayout(R.layout.a_main_tab_layout);

		// 添加四个Tab
		tabWidget.addTab(R.drawable.btn_home_selector, TAB_LIVE);
		tabWidget.addTab(R.drawable.trans_bg,  TAB_ADD);
		tabWidget.addTab(R.drawable.btn_user_selector, TAB_ME);
		//禁止 开播按钮 底部空白层 点击
		tabWidget.getChildTabViewAt(TAB_ADD).setEnabled(false);

		mOnTabChangedListener = new OnTabChangedListener() {
			@Override
			public void onTabChanged(int tabIndex) {
				// 如果点击"我的"页面，需要判断是否登录
				if (tabIndex == TAB_ME) {
					// 未登录，跳到登录页面，否则进去我的页面
					if (!AppConfig.getInstance().isLogged) {
						ActivityJumpUtil.gotoActivityForResult(CalMainActivity.this, LoginActivity.class,
								Constants.REQUEST_CODE_LOGIN, null, null);
						return;
					}
				}
				if (tabIndex == mTabIndex) {
					Fragment f = fm.findFragmentByTag(getFragmentTag(tabIndex));
					if (f != null) {
						if (f instanceof OnUpdateListener) {
							// 回调Tab再次点击方法
							((OnUpdateListener) f).onTabClickAgain();
						}
						return;
					}
				}
				switchFragment(tabIndex);
				mTabIndex = tabIndex;
			}
		};

		// 设置监听
		tabWidget.setOnTabChangedListener(mOnTabChangedListener);
	}

	private void initTabWidgetData(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			// Activity自动保存了Fragment的状态，此处要隐藏所有的Fragment，不然可能导致Fragment重叠
			FragmentTransaction ft = fm.beginTransaction();
			for (Fragment f : fm.getFragments()) {
				ft.hide(f);
			}
			ft.commitAllowingStateLoss();
		}
		// 默认显示
		tabWidget.setCurrentTab(TAB_LIVE);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onContentChanged() {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.more_add:
				OperationHelper.onEvent(App.mContext, "switchLive", null);
				showMoreWindow(v);
				break;
			default:
				break;
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		EvtLog.d(TAG, "onNewIntent.");
		// 是否进入播放界面
		// redirectPlayActivity(intent);
		// 是否更新列表数据界面
		if (intent.getBooleanExtra(Constants.UPDATE_LISTVIEW_DATA, false)) {
		}
		// 是否切换到首页，切换也会更新数据
		if (intent.getBooleanExtra(Constants.SHOW_HOME_PAGE, false)) {
//			tabWidget.setCurrentTab(TAB_FOLLOW);
//			mOnTabChangedListener.onTabChanged(TAB_FOLLOW);
		}
		checkExitApp(intent);
		super.onNewIntent(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_LOGIN " + resultCode);
			if (resultCode == LoginActivity.RESULT_CODE_OK) {
				tabWidget.setCurrentTab(TAB_ME);
			} else {
				// 如果登陆失败，或者返回，切换之前TAB页面
				tabWidget.setCurrentTab(mTabIndex);
			}
			// redirectPassiveEventActivity(getIntent(), false);
		} else if (requestCode == REQUEST_CODE_FLUSH_ACTIVITY) {
			//同意协议，跳转开播页面
			if (resultCode == RESULT_OK) {
				liveStatus();
			}
		} else if (requestCode == WebViewActivity.REQUEST_WEBVIEW_CODE) {
			//实名认证成功，跳转开播
			if (resultCode == RESULT_OK) {
				liveStatus();
			}
		} else {
			/* 在这里，我们通过碎片管理器中的Tag，就是每个碎片的名称，来获取对应的fragment */
			Fragment f = getSupportFragmentManager().findFragmentByTag(getFragmentTag(mTabIndex));
			/* 然后在碎片中调用重写的onActivityResult方法 */
			f.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MSG_CK_UPDATE_SUCCESSFUL:
				// 更新信息成功，跳转至更新界面
				Map<String, String> lmVerInfo = (Map<String, String>) msg.obj;
				String lsVerCode = lmVerInfo.get("code");
				if (lsVerCode != null) {
					int liLastCode = Integer.parseInt(lsVerCode);
					int liCurCode = Utils.getVersionCode(this);
					if (liLastCode > liCurCode) {
						String lsUpInfo = lmVerInfo.get("new");
						String lsType = lmVerInfo.get("type");
						String lsVerName = lmVerInfo.get("version");
						String url = lmVerInfo.get("url");

						Bundle bundle = new Bundle();
						/** 有待改进 */
						bundle.putInt(AppUpdateActivity.EXA_RESPONSE_FP, Integer.parseInt(lsType));// ;
						bundle.putString(AppUpdateActivity.EXA_RESPONSE_LASTVERSION, lsVerName);
						bundle.putString(AppUpdateActivity.EXA_RESPONSE_URL, url);

						bundle.putString(AppUpdateActivity.EXA_FIXLOG, lsUpInfo);
						bundle.putString(AppUpdateActivity.EXA_FILESIZE, "10.2M");
						AppUpdateActivity.redirect2me(this, bundle);
					}
				}
				break;
			case MsgTypes.GET_LIVE_STATUS_SUCCESS:
				dismissProgressDialog();
				Map<String, String> result = (Map<String, String>) msg.obj;
				String canFlag = result.get("canLive");
				mRid = result.get("rid");
				if (canFlag.equals("1")) {
					// 可以开播
					liveStatus();
				} else if (canFlag.equals("3")) {
					// 不能开播
					Dialog dialog = UiHelper.showLiveStatusCustomDialogBuilder(mActivity, result.get("reason"));
					dialog.setCanceledOnTouchOutside(true);
				} else if (canFlag.equals("5")) {
					//未实名认证
					Map<String, String> webInfo = new HashMap<>();
					webInfo.put(WebViewActivity.URL, result.get("url"));
					webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(true));
					ActivityJumpUtil.gotoActivityForResult(mActivity, WebViewActivity.class, WebViewActivity.REQUEST_WEBVIEW_CODE, WebViewActivity.WEB_INFO,
							(Serializable) webInfo);
				}
				break;
			case MsgTypes.GET_LIVE_STATUS_FAILED:
				dismissProgressDialog();
				Bundle bundle = msg.getData();
				if (bundle != null) {
					showTips(bundle.getString("errorMsg"));
				}
				break;
			case MsgTypes.GET_MY_USER_INFO_SUCCESS:
//				Map<String, String> mUserInfo = (Map<String, String>) msg.obj;
//				Utils.setCfg(mActivity, Constants.USER_SF_NAME, mUserInfo);
				break;
			case MsgTypes.MSG_ME_MESSAGE_LIST_SUCCESS:
				break;
			case MsgTypes.MSG_ME_MESSAGE_LIST_FAILED:
				break;
		}
	}

	public void setBackHandedFragment(BaseFragment mBackHandedFragment) {
		this.mBackHandedFragment = mBackHandedFragment;
	}

	/**
	 * 初始化日历展示的Fragment
	 */
	private void switchFragment(int tabIndex) {
		FragmentTransaction t = fm.beginTransaction();
		if (mTabIndex != -1) {
			Fragment last = getFragmentByIndex(mTabIndex, fm);
			if (last != null) {
				if (last.isAdded()) {
					// 如果已经关联到Activity，隐藏Fragment
					t.hide(last);
				} else {
					// 此行的代码暂时不会执行？
					t.remove(last);
				}
			}
		}
		Fragment current = getFragmentByIndex(tabIndex, fm);
		if (current != null) {
			t.setTransition(FragmentTransaction.TRANSIT_NONE); // 无动画
			if (current.isAdded()) {
				// 如果已经添加，则直接显示Fragment
				t.show(current);
			} else {
				// 添加Fragment到Activity中
				t.add(R.id.cal_frame_layout, current, getFragmentTag(tabIndex));
			}
		}
		t.commitAllowingStateLoss();

		// 回调Tab选中方法
		if (current instanceof OnUpdateListener) {
			((OnUpdateListener) current).onTabClick();
		}
	}

	/**
	 * 根据index生成Fragment的Tag
	 *
	 * @param tabIndex Fragment对应的下标值（本类中的常量定义）。
	 *                 如果是@TAB_FLLOW_FANS 应该有两种tag
	 */
	private String getFragmentTag(int tabIndex) {
		return String.format(FRAGMENT_TAG_FORMAT, tabIndex);
	}

	/**
	 * 根据index获取对应的Fragment对象
	 *
	 * @param index Fragment对应的下标值（本类中的常量定义）
	 */
	private Fragment getFragmentByIndex(int index, FragmentManager fm) {
		Fragment f = fm.findFragmentByTag(getFragmentTag(index));
		if (f == null) {
			switch (index) {
				case TAB_LIVE:
					f = new LiveFragment();
					break;
				case TAB_ME:
					f = new MeFragment();
					break;
			}
		}
		return f;
	}

	/**
	 * 移除所有的Fragment
	 */
	private void removeAllFragment() {
		try {
			final FragmentManager fm = getSupportFragmentManager();
			List<Fragment> fragments = fm.getFragments();
			FragmentTransaction ft = fm.beginTransaction();
			for (int i = 0; i < fragments.size(); i++) {
				ft.remove(fragments.get(i));
			}
			ft.commitAllowingStateLoss();
			// fm.executePendingTransactions();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 进入列表页面检查网络，提醒用户链接网络
	 */

	private void checkNetwork() {
		if (!TelephoneUtil.isNetworkAvailable()) {
			UiHelper.showConfirmDialog(CalMainActivity.this, R.string.network_setting_msg, R.string.settings,
					R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							TelephoneUtil.openWifiSetting(getApplicationContext());
						}
					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
		}

	}

	/**
	 * 注册广播（消息更新广播）
	 */
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(UserOffLineReceiver.ACTION_USER_OFFLINE);

		mUserOffLineReceiver = new UserOffLineReceiver();

		mUserOffLineReceiver.setOnReceiverCallbackListener(new UserOffLineReceiver.ReceiverCallback() {
			@Override
			public void callBack() {
				EvtLog.e(TAG, "onChanged UserOffLineReceiver.ReceiverCallback");
				//设置登录状态为false
				AppConfig.getInstance().updateLoginStatus(false);
				// 按返回键弹出对话框
				Dialog dialog = UiHelper.showConfirmDialog(mActivity, R.string.rongcloud_user_online_tip,
						R.string.rongcloud_user_relogin, R.string.rongcloud_user_logout, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//重新连接
								reLogin();
							}
						}, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								ActivityJumpUtil.toLoginActivity(App.mContext, true);
							}
						});
				dialog.setCanceledOnTouchOutside(false);
				dialog.setCancelable(false);
			}
		});
		this.registerReceiver(mUserOffLineReceiver, filter);
	}


	/**
	 * 注销用户下线广播
	 */
	private void unregisterReceiver() {
		if (mUserOffLineReceiver != null)
			this.unregisterReceiver(mUserOffLineReceiver);
	}

	/**
	 * 重新登录（两步：1、获取用户信息；2、连接融云）
	 * 请求一次用户信息，更新cookie，如果cookie状态丢失，需要再次登录
	 */
	public void reLogin() {
		//每次进入主Activity都去获取一次用户信息
		BusinessUtils.getMyUserInfo(mActivity, new CallbackDataHandle() {
			@Override
			public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
				if (success) {
					try {
						//设置登录状态为true
						AppConfig.getInstance().updateLoginStatus(true);
						UserInfoConfig config = JacksonUtil.readValue(result.toString(), UserInfoConfig.class);
						UserInfoConfig.getInstance().updateFromInfo(config);
					} catch (Exception e) {
					}
				} else {
					UiHelper.showToast(mActivity, "errorMsg");
				}
			}
		});

	}


	/**
	 * 注册登陆状态广播
	 */
	private void registerLoginReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(LoginStatusChangeReceiver.LOGIN_STATUS_ChANGE_ACTION);
		loginStatusChangeReceiver = new LoginStatusChangeReceiver();
		loginStatusChangeReceiver.setOnLoginStatusListener(new LoginStatusChangeReceiver.LoginStatusChangeCallback() {

			@Override
			public void loginChange(int type) {
				EvtLog.e(TAG, "LoginStatusChangeReceiver loginChange type " + type);
			}

		});
		mActivity.registerReceiver(loginStatusChangeReceiver, filter);
	}

	/**
	 * 注销登陆状态广播
	 */
	private void unregisterLoginReceiver() {
		if (loginStatusChangeReceiver != null)
			mActivity.unregisterReceiver(loginStatusChangeReceiver);
	}

	/**
	 * 检测更新提交
	 */
	public void checkUpdate(Intent intent) {
		boolean isCheckUpdate = intent == null || intent.getBooleanExtra(EXA_IS_CHECK_UPDATE, true);
		if (isCheckUpdate && TelephoneUtil.isNetworkAvailable()) {
			BusinessUtils.getLastVersion(this, new CheckForAppUpdateReceiverListener(this));
		}
	}

	/**
	 * 检测是否需要退出App（强制更新场景）
	 *
	 * @param data Intent对象
	 */
	private void checkExitApp(Intent data) {
		if (data == null) {
			return;
		}
		boolean isExit = data.getBooleanExtra(AppUpdateActivity.EXA_RESULT_IS_FINISH, false);
		if (isExit) {
			finish();
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
		super.onSaveInstanceState(outState, outPersistentState);
	}

	/**
	 * 关闭对话框
	 */
	private void dismissProgressDialog() {
		if (mProgress != null && mProgress.isShowing())
			mProgress.dismiss();
	}

	/**
	 * 融云用户注销
	 */
	private void showMoreWindow(View view) {
		mProgress = Utils.showProgress(mActivity);
		BusinessUtils.getLiveStatus(mActivity, new LiveStatusCallbackDataHandle(CalMainActivity.this));
	}

	/**
	 * 最新版本回调接口 ClassName: CheckForAppUpdateReceiverListener <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class CheckForAppUpdateReceiverListener implements CallbackDataHandle {
		private WeakReference<BaseFragmentActivity> reference;

		public CheckForAppUpdateReceiverListener(BaseFragmentActivity activity) {
			reference = new WeakReference<>(activity);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "CheckForAppUpdateReceiverListener success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MSG_CK_UPDATE_SUCCESSFUL;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					if (reference != null && reference.get() != null)
						reference.get().sendMsg(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

		}
	}

	/**
	 * 直播状态回调接口
	 */
	private static class LiveStatusCallbackDataHandle implements CallbackDataHandle {
		private WeakReference<BaseFragmentActivity> reference;

		public LiveStatusCallbackDataHandle(BaseFragmentActivity activity) {
			reference = new WeakReference<>(activity);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MsgTypes.GET_LIVE_STATUS_SUCCESS;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					// 如果fragment未回收，发送消息
					if (reference != null && reference.get() != null) {
						BaseFragmentActivity ac = reference.get();
						ac.sendMsg(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				msg.what = MsgTypes.GET_LIVE_STATUS_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				// 如果fragment未回收，发送消息
				if (reference != null && reference.get() != null) {
					BaseFragmentActivity ac = reference.get();
					ac.sendMsg(msg);
				}
			}
		}
	}

	/**
	 * 获取未读系统消息列表	MeMessageCallbackData
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class MeMessageCallbackData implements CallbackDataHandle {

		private WeakReference<BaseFragmentActivity> mFragment;

		public MeMessageCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "MeMessageCallbackData success " + success + " errorCode" + errorCode);
			android.os.Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_ME_MESSAGE_LIST_SUCCESS;
					Map<String, Object> mListData = JSONParser.parseMultiInSingle((JSONObject) result, new String[]{""});
					try {
//						Map<String, Object> mListData = (Map<String, Object>) msg.obj;
						if (mListData != null) {
							// 解析content，转换为会话对象
							List<Map<String, Object>> messageList = JSONParser.parseMultiInMulti(String.valueOf(mListData.get("list")), new String[]{""});
							// 获取系统消息用户信息
							Map<String, Object> userMap = JSONParser.parseMultiInSingle(String.valueOf(mListData.get("userInfo")), new String[]{""});
							String uid = null;
							String nickname = "";
							String headPic = null;
							if (userMap != null) {
								uid = String.valueOf(userMap.get("uid"));
								nickname = String.valueOf(userMap.get("nickname"));
								headPic = String.valueOf(userMap.get("headPic"));
							}
							// 更新本地数据库系统消息
							DatabaseUtils.updatePersonInfoToDatabase(App.mContext, uid, nickname, headPic);
							msg.obj = messageList.size();
						} else {
							msg.obj = null;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else

			{
				msg.what = MsgTypes.MSG_ME_MESSAGE_LIST_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}
		}

	}
}
