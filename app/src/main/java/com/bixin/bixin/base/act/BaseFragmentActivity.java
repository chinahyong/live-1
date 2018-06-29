package com.bixin.bixin.base.act;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lib.common.utils.LogUtil;
import com.lib.common.utils.SystemBarTintUtil;
import com.lib.common.widget.LoadingDialogBuilder;
import com.umeng.analytics.MobclickAgent;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import cn.jpush.android.api.JPushInterface;
import tv.live.bx.R;

/**
 * @author Amin
 */

public abstract class BaseFragmentActivity extends AppCompatActivity {

	// Toast相关
	protected String TAG = "BaseFragmentActivity";

	/**
	 * activity不设置setContentView
	 */
	protected static final int NO_SETTING_CONTENTVIEW = -1;
	protected Activity mActivity;
	protected LayoutInflater mInflater;
	protected Handler mHandler = new MyHandler(this);

	// 短信验证码相关
	private boolean mbDuraingSmsLoop = false;

	/**
	 * 标题栏TopBar相关变量 start
	 */
	// activity 返回按钮
	protected RelativeLayout mTopbarLayout;
	protected RelativeLayout mTopBackLayout;
	protected ImageView mTopBackIv;
	// activity 标题
	protected TextView mTopTitleTv;
	// activity 更多文字按钮
	protected RelativeLayout mTopRightTextLayout;
	protected TextView mTopRightText;
	// activity 更多图片按钮
	protected RelativeLayout mTopRightImageLayout;
	protected ImageView mTopRightImage;

	// 手指向右滑动时的最小距离
	private static final int XDISTANCE_MIN = 440;
	// 手指向右滑动时的最小速度
	private static final int XSPEED_MIN = 2000;

	// 记录手指按下时的横坐标。
	private float xDown;

	// 记录手指移动时的横坐标。
	private float xMove;

	// 用于计算手指滑动的速度。
	private VelocityTracker mVelocityTracker;

	// 是否沉侵式模式
	protected boolean isSystemBarTint = true;
	// 默认不支持滑动关闭
	protected boolean isTouchFlag = false;

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
			SystemBarTintUtil tintManager = new SystemBarTintUtil(this);
			tintManager.setStatusBarTintEnabled(true);
			// 使用颜色资源
			tintManager.setStatusBarTintColor(getStatusBarColor());
		}
		if (getLayoutRes() > 0) {
			setContentView(getLayoutRes());
		}
		ButterKnife.bind(this);
		mInflater = LayoutInflater.from(getApplicationContext());
		initTitle();
		mActivity = this;
		TAG = getClass().getSimpleName();
		LogUtil.d(TAG, "onCreate");
		initMembers(); // 初始化成员变量
		initData(savedInstanceState);
		initTitleData();
		addListener();
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

	protected abstract int getLayoutRes();

	/**
	 * 初始化Activity 头部的信息，有些activity未使用标准头部布局a_common_top_bar.xml文件，不调用此方法
	 */
	protected void initTitle() {
		mTopbarLayout = findViewById(R.id.ry_bar_layout);
		mTopBackLayout = findViewById(R.id.ry_bar_left);
		mTopBackIv = findViewById(R.id.iv_bar_left);
		mTopTitleTv = findViewById(R.id.tv_bar_title);
		mTopRightTextLayout = findViewById(R.id.ry_bar_right_text);
		mTopRightText = findViewById(R.id.tv_bar_right);
		mTopRightImageLayout = findViewById(R.id.ry_bar_right);
		mTopRightImage = findViewById(R.id.iv_bar_right);
	}

	// 设置顶部状态栏底色
	protected void setTopbarBackground(@ColorRes int color) {
		if (mTopbarLayout == null) {
			new Throwable("setTopbarBackground need to run after initTitle");
		}
		mTopbarLayout.setBackgroundColor(getResources().getColor(color));
	}

	/**
	 * 初始化标题信息
	 */
	protected void initTitleData() {
	}

	/**
	 * 初始化成员变量
	 */
	protected void initMembers() {
	}

	/**
	 * 初始化数据
	 */
	protected abstract void initData(Bundle savedInstanceState);

	/**
	 * 设置监听器
	 */
	protected void addListener() {

	}

	protected void setTopBackIv(int resId) {
		mTopBackIv.setImageResource(resId);
	}

	@Override
	protected void onStart() {
		super.onStart();
		LogUtil.d(TAG, "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		LogUtil.d(TAG, "onResume");
		MobclickAgent.onPageStart(TAG);
		MobclickAgent.onResume(this);
		JPushInterface.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LogUtil.d(TAG, "onPause");
		// 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
		MobclickAgent.onPageEnd(TAG);
		MobclickAgent.onPause(this);
		JPushInterface.onPause(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		LogUtil.d(TAG, "onStop");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		LogUtil.d(TAG, "onNewIntent");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtil.d(TAG, "onDestroy");
		// 清除handler消息
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		dismissLoadingProgress();
	}

	@Override
	public void onBackPressed() {
		LogUtil.d(TAG, "onBackPressed");
		// 发送短信60秒内禁止返回
		if (!mbDuraingSmsLoop) {
			super.onBackPressed();
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		createVelocityTracker(event);
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				xDown = event.getRawX();
				break;
			case MotionEvent.ACTION_MOVE:
				xMove = event.getRawX();
				// 活动的距离
				int distanceX = (int) (xMove - xDown);
				// 获取顺时速度
				int xSpeed = getScrollVelocity();
				// 当滑动的距离大于我们设定的最小距离且滑动的瞬间速度大于我们设定的速度时，返回到上一个activity
				if (distanceX > XDISTANCE_MIN && xSpeed > XSPEED_MIN && isTouchFlag) {
					finish();
					// 设置切换动画，从右边进入，左边退出
					overridePendingTransition(R.anim.a_slide_in_left,
							R.anim.a_slide_out_right);
				}
				break;
			case MotionEvent.ACTION_UP:
				recycleVelocityTracker();
				break;
			default:
				break;
		}
		return super.dispatchTouchEvent(event);
	}

	/**
	 * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
	 *
	 * @param event
	 */
	private void createVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	/**
	 * 回收VelocityTracker对象。
	 */
	private void recycleVelocityTracker() {
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}

	/**
	 * 获取手指在content界面滑动的速度。
	 *
	 * @return 滑动速度，以每秒钟移动了多少像素值为单位。
	 */
	private int getScrollVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) mVelocityTracker.getXVelocity();
		return Math.abs(velocity);
	}

	protected void showLoadingProgress() {
		LoadingDialogBuilder.showDialog(this);
	}

	protected void dismissLoadingProgress() {
		LoadingDialogBuilder.dismissDialog();
	}

	/**
	 * 静态的Handler对象
	 */
	private static class MyHandler extends Handler {

		private WeakReference<BaseFragmentActivity> mActivity;

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

	public void sendEmptyMsg(int what) {
		if (mHandler != null) {
			mHandler.sendEmptyMessage(what);
		}
	}

}
