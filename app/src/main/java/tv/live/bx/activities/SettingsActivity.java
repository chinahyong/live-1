package tv.live.bx.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.receiver.LoginStatusChangeReceiver;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import static com.efeizao.bx.R.id.settings_rl_anim_settings;

public class SettingsActivity extends BaseFragmentActivity implements OnClickListener {

	private RelativeLayout moRlMsgSettings, moRlAnimSettings, moRlAdvice, moRlGetBackPwd, moRlAbout, moRlCheckUpdate, moRlHelp, mBackListLayout, mAccountSaleLayout;
	private Button moBtnLogout;
	private TextView mAccountSaleStatus;
	private AlertDialog moProgress;

	// private View settingGetBackLine, settingMsgSetLine;

	/**
	 * 之前忘记使用这些方法了，这个类暂时不用了
	 */
	@Override
	protected int getLayoutRes() {

		// TODO Auto-generated method stub
		return R.layout.activity_settings;
	}

	@Override
	protected void initData(Bundle savedInstanceState) {

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	protected void initMembers() {
		moRlAnimSettings = (RelativeLayout) findViewById(settings_rl_anim_settings);
		moRlMsgSettings = (RelativeLayout) findViewById(R.id.settings_rl_msg_settings);
		moRlAdvice = (RelativeLayout) findViewById(R.id.settings_rl_advice);
		moRlGetBackPwd = (RelativeLayout) findViewById(R.id.settings_rl_get_back_pwd);
		mBackListLayout = (RelativeLayout) findViewById(R.id.settings_rl_backlist);
		moRlAbout = (RelativeLayout) findViewById(R.id.settings_rl_about);
		moRlCheckUpdate = (RelativeLayout) findViewById(R.id.settings_rl_check_update);
		moRlHelp = (RelativeLayout) findViewById(R.id.settings_rl_help);

		moBtnLogout = (Button) findViewById(R.id.settings_btn_logout);
		// settingGetBackLine = findViewById(R.id.settings_get_back_line);
		// settingMsgSetLine = findViewById(R.id.settings_msg_setting_line);
		mAccountSaleLayout = (RelativeLayout) findViewById(R.id.settings_rl_account);
		mAccountSaleStatus = (TextView) findViewById(R.id.settings_rl_account_status);
		initTitle();
	}

	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.a_main_tab_setting);
		mTopBackLayout.setOnClickListener(this);
	}

	public void initWidgets() {
		boolean isLogged = AppConfig.getInstance().isLogged;
		moBtnLogout.setVisibility(isLogged ? View.VISIBLE : View.GONE);
		moRlMsgSettings.setVisibility(isLogged ? View.VISIBLE : View.GONE);
		moRlGetBackPwd.setVisibility(isLogged ? View.VISIBLE : View.GONE);
		mBackListLayout.setVisibility(isLogged ? View.VISIBLE : View.GONE);
		// settingGetBackLine.setVisibility(isLogged ? View.VISIBLE :
		// View.GONE);
		// settingMsgSetLine.setVisibility(isLogged ? View.VISIBLE : View.GONE);

		//判断是否账户保护
		setAccountSaleStatus();
	}

	protected void setEventsListeners() {
		moBtnLogout.setOnClickListener(this);
		moRlAbout.setOnClickListener(this);
		moRlAnimSettings.setOnClickListener(this);
		moRlMsgSettings.setOnClickListener(this);
		moRlAdvice.setOnClickListener(this);
		moRlCheckUpdate.setOnClickListener(this);
		moRlGetBackPwd.setOnClickListener(this);
		mBackListLayout.setOnClickListener(this);
		moRlHelp.setOnClickListener(this);
		mAccountSaleLayout.setOnClickListener(this);
	}

	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.LOGOUT_SUCCESS:
				AppConfig.getInstance().updateLoginStatus(false);
				//不清楚cookieds
				// HttpSession.getInstance(mActivity).clearCookies();
				// BusinessUtils.getPubKey(mThis, new PubKeyCallbackData(
				// SettingsActivity.this));
				JPushInterface.setAliasAndTags(mActivity, "", null, new TagAliasCallback() {
					@Override
					public void gotResult(int arg0, String arg1, Set<String> arg2) {

					}
				});

				Intent intent = new Intent(LoginStatusChangeReceiver.LOGIN_STATUS_ChANGE_ACTION);
				intent.putExtra(LoginStatusChangeReceiver.LOGIN_STATUS_TYPE, LoginStatusChangeReceiver.TYPE_LOGOUT);
				intent.setPackage(getApplicationContext().getPackageName());
				sendBroadcast(intent);

				// 注销成功，后续加
				if (moProgress != null && moProgress.isShowing()) {
					moProgress.dismiss();
				}
				ActivityJumpUtil.toLoginActivity(mActivity, true);
				// UiHelper.showShortToast(mThis, "注销成功");
				// 重新刷新列表
				// if (mThis != null) {
				// initWidgets();
				// }
				break;
			case MsgTypes.LOGOUT_FAILED:
				if (moProgress != null && moProgress.isShowing()) {
					moProgress.dismiss();
				}
				UiHelper.showShortToast(mActivity, Constants.NETWORK_FAIL);
				break;

			case MsgTypes.GET_PUB_KEY_SUCCESS:
				@SuppressWarnings("unchecked")
				Map<String, String> lmResult = (Map<String, String>) msg.obj;
				String lsKey = lmResult.get("key")
						.replaceAll("-----BEGIN PUBLIC KEY-----|-----END PUBLIC KEY-----", "").trim();
				FeizaoApp.setCacheData("public_key", lsKey);
				break;
			case MsgTypes.GET_PUB_KEY_FAILED:
				UiHelper.showToast(mActivity, Constants.NETWORK_FAIL);
				break;
			case MsgTypes.GET_LAST_VERSION_SUCCESS:
				if (moProgress != null && moProgress.isShowing()) {
					moProgress.dismiss();
				}
				// 更新信息成功，跳转至更新界面
				Map<String, String> lmVerInfo = (Map<String, String>) msg.obj;
				String lsVerCode = lmVerInfo.get("code");
				if (lsVerCode != null) {
					int liLastCode = Integer.parseInt(lsVerCode);
					int liCurCode = Utils.getVersionCode(mActivity);
					if (liLastCode > liCurCode) {
						String lsUpInfo = lmVerInfo.get("new");
						String lsType = lmVerInfo.get("type");
						String lsVerName = lmVerInfo.get("version");
						String url = lmVerInfo.get("url");

						Bundle bundle = new Bundle();
						/** 有待改进 */
						bundle.putInt(AppUpdateActivity.EXA_RESPONSE_FP, Integer.parseInt(lsType));// Integer.parseInt(lsType));
						bundle.putString(AppUpdateActivity.EXA_RESPONSE_LASTVERSION, lsVerName);
						bundle.putString(AppUpdateActivity.EXA_RESPONSE_URL, url);

						bundle.putString(AppUpdateActivity.EXA_FIXLOG, lsUpInfo);
						bundle.putString(AppUpdateActivity.EXA_FILESIZE, "10.2M");
						AppUpdateActivity.redirect2me(mActivity, bundle);
					} else {
						UiHelper.showToast(mActivity, getResources().getString(R.string.a_update_no_update));
					}

				}
				break;
			case MsgTypes.GET_LAST_VERSION_FAILED:
				if (moProgress != null && moProgress.isShowing()) {
					moProgress.dismiss();
				}
				if (msg.obj != null && !TextUtils.isEmpty(String.valueOf(msg.obj))) {
					UiHelper.showToast(mActivity, String.valueOf(msg.obj));
				}
				break;

			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			// 登录成功进入主页
			if (resultCode == LoginActivity.RESULT_CODE_OK) {
				ActivityJumpUtil.welcomeToMainActivity(mActivity);
			} else {
				closeActivityAndCalMainActivity();
			}
		} else if (requestCode == AccountSaleActivity.REQUEST_CODE_FLUSH_ACTIVITY) {
			setAccountSaleStatus();
		}
	}

	private void setAccountSaleStatus() {
		//判断是否账户保护
		if (!TextUtils.isEmpty(UserInfoConfig.getInstance().mobile)) {
			mAccountSaleStatus.setText(R.string.setting_account_status_sale);
			mAccountSaleStatus.setTextColor(getResources().getColor(R.color.a_text_color_aaaaaa));
		} else {
			mAccountSaleStatus.setText(R.string.setting_account_status_unsale);
			mAccountSaleStatus.setTextColor(getResources().getColor(R.color.a_text_color_da500e));
		}
	}

	/**
	 * 关闭当前Activity，并打开CalMainActivity
	 */
	private void closeActivityAndCalMainActivity() {
		Intent intent = new Intent(this, CalMainActivity.class);
		intent.putExtra(AppUpdateActivity.EXA_RESULT_IS_FINISH, true);
		startActivity(intent);
		this.finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			// 进场特效设置
			case R.id.settings_rl_anim_settings:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "chooseModelOfEnterBroadcast");
				ActivityJumpUtil.gotoActivity(mActivity, AnimSettingsActivity.class, false, null, null);
				break;
			case R.id.top_left:
				onBackPressed();
				break;
			case R.id.settings_rl_msg_settings:
				ActivityJumpUtil.gotoActivity(mActivity, MsgSettingsActivity.class, false, null, null);
				break;
			case R.id.settings_rl_advice:
				ActivityJumpUtil.gotoActivity(mActivity, AdviceActivity.class, false, null, null);
				break;
			case R.id.settings_rl_about:
				ActivityJumpUtil.gotoActivity(mActivity, AboutActivity.class, false, null, null);
				break;
			case R.id.settings_rl_get_back_pwd:
				ActivityJumpUtil.gotoActivity(mActivity, GetBackPwdActivity.class, false, null, null);
				break;
			case R.id.settings_rl_check_update:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "updateVersions");
				moProgress = Utils.showProgress(mActivity);
				BusinessUtils.getLastVersion(mActivity, new CheckForAppUpdateReceiverListener());
				break;
			case R.id.settings_rl_help:
//				Map<String, String> webInfo = new HashMap<String, String>();
//				webInfo.put(WebViewActivity.URL, Constants.COMMON_HELP_URL);
//				webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(true));
				ActivityJumpUtil.gotoActivity(mActivity, PingActivity.class, false, null, null);
				break;
			case R.id.settings_btn_logout:
				UiHelper.showConfirmDialog(mActivity, R.string.logout_msg, R.string.logout_cancel, R.string.determine,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						}, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								moProgress = Utils.showProgress(mActivity);
								BusinessUtils.logout(mActivity, new LogoutCallbackData(SettingsActivity.this));
							}
						});

				break;
			case R.id.settings_rl_backlist:
				break;
			case R.id.settings_rl_account:
				ActivityJumpUtil.gotoActivityForResult(mActivity, AccountSaleActivity.class, AccountSaleActivity.REQUEST_CODE_FLUSH_ACTIVITY, null,
						null);
				break;
			default:
				break;
		}

	}

	/**
	 * 最新版本回调接口 ClassName: CheckForAppUpdateReceiverListener <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private class CheckForAppUpdateReceiverListener implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "CheckForAppUpdateReceiverListener success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.GET_LAST_VERSION_SUCCESS;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					sendMsg(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			} else {
				msg.what = MsgTypes.GET_LAST_VERSION_FAILED;
				msg.obj = errorMsg;
				sendMsg(msg);
			}

		}
	}

	/**
	 * 退出登录数据回调 ClassName: LogoutCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-23 下午3:50:00 <br/>
	 *
	 * @author Administrator
	 * @version SettingsFragment
	 * @since JDK 1.6
	 */
	private static class LogoutCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public LogoutCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "LogoutCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.LOGOUT_SUCCESS;
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.LOGOUT_FAILED;
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

	private static class PubKeyCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public PubKeyCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "PubKeyCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.GET_PUB_KEY_SUCCESS;

					msg.obj = JSONParser.parseOne((JSONObject) result);
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.GET_PUB_KEY_FAILED;
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
