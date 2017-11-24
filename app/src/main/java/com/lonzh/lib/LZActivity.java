package com.lonzh.lib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.efeizao.bx.R;
import com.lonzh.lib.exceptions.MsgTypeExists;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import cn.jpush.android.api.JPushInterface;

/**
 * 基础Activity
 *
 * @author Keven Mong
 */
public abstract class LZActivity extends Activity {
	// Toast相关
	protected String TAG = "BaseFragmentActivity";
	// 消息接受器列表
	private static SparseArray<OnReceiveMsgListener> moMsgListeners;

	// Handler
	private static Handler moHandler;

	// Toast相关
	protected static Toast moToastInstance;
	protected static final int TOAST_SHORT = Toast.LENGTH_SHORT;
	protected static final int TOAST_LONG = Toast.LENGTH_LONG;

	// 加载
	private Animation moLoadAnim;

	// 短信验证码相关
	private boolean mbDuraingSmsLoop = false;
	private int miSmsTipSec = 0;

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
	protected boolean isSystemBarTint = false;

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
		TAG = getClass().getSimpleName();
		setContentView(getLayoutRes());
		firstInit(); // 前期初始化
		initMembers(); // 初始化成员变量
		registerMsgListeners(); // 设置消息处理器
		setHandler(); // 设置Handler
		initWidgets(); // 初始化控件
		onLoadStart(); // 开始页面加载效果
		setEventsListeners(); // 设置事件处理器
		lastInit(); // 后期初始化
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
		getTheme().resolveAttribute(android.R.color.transparent, typedValue, true);
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

	// 友盟统计
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(TAG);
		MobclickAgent.onResume(this);
		JPushInterface.onResume(this);
	}

	public void onPause() {
		super.onPause();
		// 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
		MobclickAgent.onPageEnd(TAG);
		MobclickAgent.onPause(this);
		JPushInterface.onPause(this);
	}

	/**
	 * 初始化标题信息
	 */
	protected void initTitleData() {

	}

	@Override
	protected void onDestroy() {
		mbDuraingSmsLoop = false;
		clearRegisterMsgListener();
		super.onDestroy();
	}

	/**
	 * 获取加载效果View
	 *
	 * @return
	 */
	protected View getLoadView() {
		return null;
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
	 * 获取加载效果ImageView
	 *
	 * @return
	 */
	protected ImageView getLoadAnimIv() {
		return null;
	}

	/**
	 * 获取加载动画Res
	 *
	 * @return
	 */
	protected int getLoadAnimRes() {
		return 0;
	}

	// 页面加载开始
	protected void onLoadStart() {
		if (getLoadView() != null && getContentView() != null) {
			getLoadView().setVisibility(View.VISIBLE);
			getContentView().setVisibility(View.GONE);
			if (getLoadAnimIv() != null) {
				if (moLoadAnim == null)
					moLoadAnim = AnimationUtils.loadAnimation(this, getLoadAnimRes());
				getLoadAnimIv().startAnimation(moLoadAnim);
			}
		}
	}

	// 页面加载结束
	protected void onLoadFinished() {
		if (getLoadView() != null && getContentView() != null) {
			getLoadView().setVisibility(View.GONE);
			getContentView().setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 获取界面布局资源
	 *
	 * @return
	 */
	protected abstract int getLayoutRes();

	/**
	 * 重写此方法来进行所有工作开始前的初始化工作
	 */
	protected void firstInit() {
	}

	/**
	 * 重写此方法来进行onCreated中的最后一步初始化
	 */
	protected void lastInit() {
	}

	/**
	 * 初始化成员变量
	 */
	protected abstract void initMembers();

	/**
	 * 注册Handler Message监听器
	 */
	protected abstract void registerMsgListeners();

	/**
	 * 防止对象泄露
	 *
	 * @author Keven Mong
	 */
	private static class LZHandler extends Handler {
		protected WeakReference<SparseArray<OnReceiveMsgListener>> moListeners;

		public LZHandler(SparseArray<OnReceiveMsgListener> poListeners) {
			moListeners = new WeakReference<SparseArray<OnReceiveMsgListener>>(poListeners);
		}
	}

	/**
	 * 设置Handler
	 */
	private void setHandler() {
		moHandler = new LZHandler(moMsgListeners) {
			@Override
			public void handleMessage(Message poMsg) {
				SparseArray<OnReceiveMsgListener> loListeners = moListeners.get();
				OnReceiveMsgListener loListener = loListeners.get(poMsg.what);
				if (loListener != null)
					loListener.onReceiveMsg(poMsg);
			}
		};
	}

	/**
	 * 获取Handler
	 *
	 * @return
	 */
	public static Handler getHandler() {
		if (moHandler == null) {
			moHandler = new LZHandler(moMsgListeners) {
				@Override
				public void handleMessage(Message poMsg) {
					SparseArray<OnReceiveMsgListener> loListeners = moListeners.get();
					OnReceiveMsgListener loListener = loListeners.get(poMsg.what);
					if (loListener != null)
						loListener.onReceiveMsg(poMsg);
				}
			};

		}
		return moHandler;
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
	 * Handler接收到消息后的处理监听器
	 *
	 * @author Keven Mong
	 */
	public interface OnReceiveMsgListener {
		/**
		 * 接收到消息后调用此方法
		 *
		 * @param poMsg 消息Message
		 */
		void onReceiveMsg(Message poMsg);
	}

	/**
	 * 注册消息接收器
	 *
	 * @param piMsgType
	 * @param poListener
	 */
	public static void registerMsgListener(int piMsgType, OnReceiveMsgListener poListener) throws MsgTypeExists {
		if (moMsgListeners == null)
			moMsgListeners = new SparseArray<>();
		moMsgListeners.put(piMsgType, poListener);
	}

	/**
	 * 取消消息接收器
	 *
	 * @param piMsgType
	 */
	public static void unRegisterMsgListener(int piMsgType) throws MsgTypeExists {
		if (moMsgListeners != null)
			moMsgListeners.remove(piMsgType);
	}

	public static void clearRegisterMsgListener() throws MsgTypeExists {
		if (moMsgListeners != null) {
			moMsgListeners.clear();
			moMsgListeners = null;
		}
	}

	/**
	 * 显示Toast
	 *
	 * @param psText
	 * @param piLength
	 */
	public void showToast(String psText, int piDuration) {
		if (moToastInstance == null)
			moToastInstance = Toast.makeText(getApplicationContext(), psText, piDuration);
		else {
			moToastInstance.setDuration(piDuration);
			moToastInstance.setText(psText);
		}
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
	 *
	 * @param psText
	 * @param piDuration
	 */
	public void hideToast(int piDuration) {
		if (moToastInstance != null)
			moToastInstance.cancel();
	}

	protected void setTopBackIv(int resourceId) {
		mTopBackIv.setBackgroundResource(resourceId);
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
	 * @param pbFinish
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

	/**
	 * 发送短信按钮发送后处理流程
	 *
	 * @param poContext
	 * @param poBtn
	 * @param piDuration
	 */
	public void startSendSmsBtnLoop(final Button poBtn, final int piDuration) {
		final int liMsgTip = -1;
		final String lsOriText = poBtn.getText().toString();
		final Drawable loOriBg = poBtn.getBackground();
		poBtn.setSelected(true);
		poBtn.setEnabled(false);
		miSmsTipSec = piDuration;
		OnReceiveMsgListener loMsgListener = new OnReceiveMsgListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onReceiveMsg(Message poMsg) {
				switch (poMsg.what) {
					case liMsgTip:
						if (miSmsTipSec > 0)
							poBtn.setText(String.format("请稍等%1$s秒", miSmsTipSec));
						else {
							mbDuraingSmsLoop = false;
							poBtn.setSelected(false);
							poBtn.setEnabled(true);
							poBtn.setText(lsOriText);
						}
						--miSmsTipSec;
						break;
				}
			}
		};
		registerMsgListener(liMsgTip, loMsgListener);

		new Thread() {
			@Override
			public void run() {
				mbDuraingSmsLoop = true;
				while (mbDuraingSmsLoop) {
					getHandler().sendEmptyMessage(liMsgTip);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	public void killSendSmsBtnLoop() {
		mbDuraingSmsLoop = false;
	}
}