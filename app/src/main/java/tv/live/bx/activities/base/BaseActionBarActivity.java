package tv.live.bx.activities.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import tv.live.bx.util.UiHelper;
import com.umeng.analytics.MobclickAgent;

import java.lang.ref.WeakReference;

import cn.jpush.android.api.JPushInterface;

/**
 * Title: BaseActivity.java Description: activity基类 Copyright: Copyright (c)
 * 2008
 * @version 1.0
 * @CreateDate 2013-11-25 下午6:41:13
 */
@SuppressLint("NewApi")
public class BaseActionBarActivity extends AppCompatActivity {

	protected String _TAG = "BaseActionBarActivity";

	private Dialog mProgressDialog;

	protected Activity mThis;
	protected Handler mHandler = new MyHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mThis = this;
		_TAG = getClass().getSimpleName();
		initViews();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(_TAG);
		MobclickAgent.onResume(this);
		JPushInterface.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(_TAG);
		MobclickAgent.onPause(this);
		JPushInterface.onPause(this);
	}

	/**
	 * 子类可以重写此方法，在此方法中初始化所有的控件
	 */
	protected void initViews() {

	}

	// public void showProgressDialog(int msgRes) {
	// runOnUiThread(new ShowProgressAction(msgRes));
	// }
	//
	// public void showProgressDialogNonCancel(int msgRes) {
	// runOnUiThread(new ShowProgressAction(msgRes, false));
	// }
	//
	// public void cancelProgressDialog() {
	// runOnUiThread(new CancelProgressAction());
	// }

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
		} else {
			handleMessage(msg);
		}
	}

	protected void sendEmptyMsg(int msg) {
		if (mHandler != null) {
			mHandler.sendEmptyMessage(msg);
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

		private final WeakReference<BaseActionBarActivity> mActivity;

		public MyHandler(BaseActionBarActivity activity) {
			mActivity = new WeakReference<BaseActionBarActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			BaseActionBarActivity activity = mActivity.get();
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
				UiHelper.showToast(BaseActionBarActivity.this, (String) msg);
			} else if (msg instanceof Integer) {
				UiHelper.showToast(BaseActionBarActivity.this, (Integer) msg);
			}
		}
	}

	// /**
	// * 从左向右关闭Activity
	// */
	// public void ActivityBack() {
	// ImeUtil.hideSoftInput(this);
	// finish();
	// overridePendingTransition(0, R.anim.a_slide_out_right);
	// }
	//
	// /**
	// * 从上往下关闭页面
	// */
	// public void ActivityClose() {
	// ImeUtil.hideSoftInput(this);
	// finish();
	// overridePendingTransition(R.anim.a_slide_in_up, R.anim.a_slide_out_down);
	// }

}
