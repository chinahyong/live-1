package tv.live.bx.activities.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.efeizao.bx.R;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.PermissionUtil;
import tv.live.bx.library.util.EvtLog;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import cn.jpush.android.api.JPushInterface;

/**
 * 基础Activity
 *
 * @author Live
 */
@SuppressLint("NewApi")
public abstract class BaseFragmentActivity extends FragmentActivity {

	protected static int REQUEST_CODE_FLUSH_ACTIVITY = 0x100;

	// Toast相关
	protected String TAG = "BaseFragmentActivity";
	protected static Toast moToastInstance;
	protected static final int TOAST_SHORT = Toast.LENGTH_SHORT;
	protected static final int TOAST_LONG = Toast.LENGTH_LONG;
	/**
	 * activity不设置setContentView
	 */
	protected static final int NO_SETTING_CONTENTVIEW = -1;
	protected Activity mActivity;
	protected LayoutInflater mInflater;
	protected Handler mHandler = new MyHandler(this);

	// 短信验证码相关
	private boolean mbDuraingSmsLoop = false;

	/** 标题栏相关变量 start */
	/**
	 * activity 返回按钮
	 */
	protected RelativeLayout mTopBackLayout;
	protected ImageView mTopBackIv;
	/**
	 * activity 标题
	 */
	protected TextView mTopTitleTv;
	/**
	 * activity 更多文字按钮
	 */
	protected RelativeLayout mTopRightTextLayout;
	protected TextView mTopRightText;
	/**
	 * activity 更多图片按钮
	 */
	protected RelativeLayout mTopRightImageLayout;
	protected ImageView mTopRightImage;

	/**
	 * 是否沉侵式模式
	 */
	protected boolean isSystemBarTint = true;

	protected Dialog mGuideDialog;

	/**
	 * 标题栏相关变量 end
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (isSystemBarTint) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				setTranslucentStatus(this, true);
			}
			SystemBarTintManager tintManager = new SystemBarTintManager(this);
			tintManager.setStatusBarTintEnabled(true);
			// 使用颜色资源
			tintManager.setStatusBarTintColor(getStatusBarColor());
		}
		if (getLayoutRes() != NO_SETTING_CONTENTVIEW)
			setContentView(getLayoutRes());
		mInflater = LayoutInflater.from(getApplicationContext());
		mActivity = this;
		TAG = getClass().getSimpleName();
		EvtLog.i(TAG, "onCreate");
		initMembers(); // 初始化成员变量
		initWidgets(); // 初始化控件
		setEventsListeners(); // 设置事件处理器
		initData(savedInstanceState);
	}

	@TargetApi(19)
	private static void setTranslucentStatus(Activity activity, boolean on) {
		Window win = activity.getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	public int getStatusBarColor() {
		return getColorPrimary();
	}

	public int getColorPrimary() {
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
		return typedValue.data;
	}

	/**
	 * 初始化Activity 头部的信息，有些activity未使用标准头部布局a_common_top_bar.xml文件，不调用此方法
	 */
	protected void initTitle() {
		mTopBackLayout = (RelativeLayout) findViewById(R.id.top_left);
		mTopBackIv = (ImageView) findViewById(R.id.top_left_image);
		mTopTitleTv = (TextView) findViewById(R.id.top_title);
		mTopRightTextLayout = (RelativeLayout) findViewById(R.id.top_right_text_bg);
		mTopRightText = (TextView) findViewById(R.id.top_right_text);
		mTopRightImageLayout = (RelativeLayout) findViewById(R.id.top_right);
		mTopRightImage = (ImageView) findViewById(R.id.top_right_image);
		initTitleData();
	}

	/**
	 * 初始化标题信息
	 */
	protected void initTitleData() {
	}

	@Override
	protected void onStart() {
		super.onStart();
		EvtLog.i(TAG, "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		EvtLog.i(TAG, "onResume");
		MobclickAgent.onPageStart(TAG);
		MobclickAgent.onResume(this);
		JPushInterface.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		EvtLog.i(TAG, "onPause");
		// 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
		MobclickAgent.onPageEnd(TAG);
		MobclickAgent.onPause(this);
		JPushInterface.onPause(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EvtLog.i(TAG, "onStop");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		EvtLog.i(TAG, "onNewIntent");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EvtLog.i(TAG, "onDestroy");
		if (mGuideDialog != null && mGuideDialog.isShowing()) {
			mGuideDialog.dismiss();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case PermissionUtil.REQUEST_PERMISSION_CAMERA: {
				// 拒绝权限
				if (!PermissionUtil.permissionIsGranted(mActivity, Manifest.permission.CAMERA)) {
					UiHelper.showToast(mActivity, R.string.live_connect_permission_tip);
				}
				break;
			}
			default: {
				break;
			}
		}
	}

	@Override
	public void onBackPressed() {
		EvtLog.i(TAG, "onBackPressed");
		// 发送短信60秒内禁止返回
		if (!mbDuraingSmsLoop)
			super.onBackPressed();
	}

	public void showFullDialog(int layoutId, OnDismissListener dismissListener) {
		mGuideDialog = new Dialog(this, R.style.notitleDialog);
		View rootView = mInflater.inflate(layoutId, null);
		View layout = rootView.findViewById(R.id.main_layout);
		layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mGuideDialog.dismiss();
			}
		});
		mGuideDialog.setOnDismissListener(dismissListener);
		mGuideDialog.setContentView(rootView);
		mGuideDialog.show();
	}

	public void showTips(String tipsText) {
		runOnUiThread(new ShowTipsAction(tipsText));
	}

	public void showTips(int textRes) {
		runOnUiThread(new ShowTipsAction(textRes));
	}

	protected void handleMessage(Message msg) {
	}

	public void sendMsg(Message msg) {
		if (mHandler != null) {
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * 增加延时消息
	 */
	public void sendMsg(Message msg, long delay) {
		if (mHandler != null) {
			mHandler.sendMessageDelayed(msg, delay);
		}
	}

	protected void sendEmptyMsg(int msg) {
		if (mHandler != null) {
			mHandler.sendEmptyMessage(msg);
		}
	}

	protected void sendEmptyMsgDelayed(int msg, long delay) {
		if (mHandler != null) {
			mHandler.sendEmptyMessageDelayed(msg, delay);
		}
	}

	protected void postDelayed(Runnable runnable, long delay) {
		if (mHandler != null) {
			mHandler.postDelayed(runnable, delay);
		}
	}

	/**
	 * 显示“正在加载”的View(R.id.loading_container)，隐藏主View(R.id.content_container)
	 * 需要在布局文件中定义对应View的id
	 */
	// protected void showLoadingView(boolean showLoading) {
	// View loading = findViewById(R.id.loading_container);
	// if (loading != null) {
	// if (showLoading)
	// loading.setVisibility(View.VISIBLE);
	// else
	// loading.setVisibility(View.GONE);
	// }
	// View content = findViewById(R.id.content_container);
	// if (content != null) {
	// if (showLoading)
	// content.setVisibility(View.GONE);
	// else
	// content.setVisibility(View.VISIBLE);
	// }
	// }

	/**
	 * 静态的Handler对象
	 */
	private static class MyHandler extends Handler {

		private final WeakReference<BaseFragmentActivity> mActivity;

		public MyHandler(BaseFragmentActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			BaseFragmentActivity activity = mActivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}

	/**
	 * 显示提示(Toast)
	 */
	private class ShowTipsAction implements Runnable {

		private Object msg;

		public ShowTipsAction(Object msg) {
			this.msg = msg;
		}

		@Override
		public void run() {
			if (msg instanceof String) {
				UiHelper.showToast(getApplicationContext(), (String) msg);
			} else if (msg instanceof Integer) {
				UiHelper.showToast(getApplicationContext(), (Integer) msg);
			}
		}
	}

	/**
	 * 获取内容View
	 *
	 * @return
	 */
	protected View getContentView() {
		return null;
	}

	/**
	 * 获取界面布局资源
	 *
	 * @return -1 表示不设置setContentView,否则设置
	 */
	protected abstract int getLayoutRes();

	/**
	 * 初始化成员变量
	 */
	protected void initMembers() {
	}

	/**
	 * 初始化控件
	 */
	public abstract void initWidgets();

	/**
	 * 设置事件监听器
	 */
	protected abstract void setEventsListeners();

	/**
	 * 初始化数据
	 */
	protected abstract void initData(Bundle savedInstanceState);

	/**
	 * 显示Toast
	 *
	 * @param psText
	 */
	public void showToast(String psText, int piDuration) {
//		if (moToastInstance == null)
		moToastInstance = Toast.makeText(getApplicationContext(), psText, piDuration);
//		else {
//			moToastInstance.setDuration(piDuration);
//			moToastInstance.setText(psText);
//		}
		moToastInstance.show();
	}

	/**
	 * 显示Toast
	 *
	 * @param piStrRes
	 * @param piDuration
	 */
	public void showToast(int piStrRes, int piDuration) {
		showToast(getResources().getString(piStrRes), piDuration);
	}

	/**
	 * 隐藏Toast
	 */
//	public void hideToast(int piDuration) {
//		if (moToastInstance != null)
//			moToastInstance.cancel();
//	}
	protected void setTopBackIv(int resourceId) {
		mTopBackIv.setImageResource(resourceId);
	}

	/**
	 * 转向Activity
	 *
	 * @param poTo
	 * @param pbFinish
	 * @param psExtraName
	 * @param poExtra
	 */
	public void gotoActivity(Class<? extends Activity> poTo, boolean pbFinish, String psExtraName, Serializable poExtra) {
		Intent loIntent = new Intent(this, poTo);
		if (poExtra != null)
			loIntent.putExtra(psExtraName, poExtra);
		startActivity(loIntent);
		if (pbFinish)
			finish();
	}

	/**
	 * 转向Activity并且获取返回结果
	 *
	 * @param poTo
	 * @param piRequestCode
	 * @param psExtraName
	 * @param poExtra
	 */
	protected void gotoActivityForResult(Class<? extends Activity> poTo, int piRequestCode, String psExtraName,
										 Serializable poExtra) {
		Intent loIntent = new Intent(this, poTo);
		if (poExtra != null)
			loIntent.putExtra(psExtraName, poExtra);
		startActivityForResult(loIntent, piRequestCode);
	}

}