package tv.live.bx.fragments;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.PermissionUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.listeners.OnUpdateListener;
import com.squareup.leakcanary.RefWatcher;
import com.umeng.analytics.MobclickAgent;

import java.lang.ref.WeakReference;

/**
 * Title: TBaseFragment.java</br> Description: 界面Fragment基类</br> Copyright:
 * Copyright (c) 2008</br>
 *
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public abstract class BaseFragment extends Fragment implements OnUpdateListener {
	protected String TAG;

	protected Handler mHandler = new MyHandler(this);
	protected Activity mActivity;

	protected View mRootView;
	protected LayoutInflater mInflater;

	protected static int REQUEST_CODE_FLUSH_FRAGMENT = 0x1000;

	@Override
	public void onAttach(Activity activity) {
		TAG = this.getClass().getSimpleName();
		EvtLog.d(TAG, "onAttach");
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mActivity = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		if (mRootView == null) {
			mRootView = inflater.inflate(getLayoutRes(), container, false);
			initMembers(); // 初始化成员变量
			initWidgets(); // 初始化控件
			setEventsListeners(); // 设置事件处理器
			initData(getArguments());
		} else {
			ViewGroup parent = (ViewGroup) mRootView.getParent();
			if (parent != null) {
				parent.removeView(mRootView);
			}
		}
		/**
		 * 防止点击穿透，底层的fragment响应上层点击触摸事件
		 * @trouble 部分机型会出现该问题, 目前发现:google N5,vivo X5SL,魅蓝2
		 */
		mRootView.setClickable(true);
		return mRootView;
	}

	/**
	 * 所有继承BackHandledFragment的子类都将在这个方法中实现物理Back键按下后的逻辑
	 * FragmentActivity捕捉到物理返回键点击事件后会首先询问Fragment是否消费该事件
	 * 如果没有Fragment消息时FragmentActivity自己才会消费该事件, 返回true为已消费改事件
	 */
	public boolean onBackPressed() {
		return false;
	}

	/**
	 * 获取界面布局资源
	 *
	 * @return
	 */
	protected abstract int getLayoutRes();

	/**
	 * 初始化成员变量
	 */
	protected abstract void initMembers();

	/**
	 * 初始化成员变量
	 */
	protected abstract void initWidgets();

	/**
	 * 初始化成员变量
	 */
	protected abstract void setEventsListeners();

	/**
	 * 初始化数据
	 */
	protected abstract void initData(Bundle bundle);

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
	public final void onTabClick() {
		onTabSelected();
	}

	/**
	 * Tab标签选中回调，子类可重写此方法，实现业务逻辑
	 */
	protected void onTabSelected() {
		EvtLog.d(TAG, "onTabSelected");
	}

	/**
	 * Tab标签再次点击时调用，子类可重写此方法，实现业务逻辑
	 */
	@Override
	public void onTabClickAgain() {

	}

	@Override
	public void onStart() {
		EvtLog.d(TAG, "onStart");
		super.onStart();

	}

	@Override
	public void onResume() {
		super.onResume();
		EvtLog.d(TAG, "onResume");
		// 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
		MobclickAgent.onPageStart(TAG);
	}

	@Override
	public void onPause() {
		super.onPause();
		EvtLog.d(TAG, "onPause");
		// 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
		MobclickAgent.onPageEnd(TAG);
	}

	@Override
	public void onStop() {
		EvtLog.d(TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mRootView = null;
		EvtLog.d(TAG, "onDestoryView");
	}

	@Override
	public void onDestroy() {
		EvtLog.d(TAG, "onDestroy");
		super.onDestroy();
		RefWatcher refWatcher = FeizaoApp.getRefWatcher();
		refWatcher.watch(this);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		EvtLog.d(TAG, "onHiddenChanged hidden:" + hidden);
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		EvtLog.d(TAG, "onSaveInstanceState");
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	/**
	 * 消息处理方法
	 *
	 * @param msg 待处理的消息
	 */
	protected void handleMessage(Message msg) {
	}

	public void sendMsg(Message msg) {
		sendMsg(msg,0);
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

	public void showTips(String tipsText) {
		if (mHandler != null) {
			mHandler.post(new ShowTipsAction(tipsText));
		}
	}

	public void showTips(int textRes) {
		if (mHandler != null) {
			mHandler.post(new ShowTipsAction(textRes));
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
				UiHelper.showToast(mActivity.getApplicationContext(), (String) msg);
			} else if (msg instanceof Integer) {
				UiHelper.showToast(mActivity.getApplicationContext(), (Integer) msg);
			}
		}
	}

	/**
	 * 静态的Handler对象
	 */
	private static class MyHandler extends Handler {

		private final WeakReference<BaseFragment> mFragment;

		public MyHandler(BaseFragment fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			BaseFragment fragment = mFragment.get();
			if (fragment != null) {
				fragment.handleMessage(msg);
			}
		}
	}

}
