package com.bixin.bixin.live.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.lonzh.lib.network.LZCookieStore;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import tv.live.bx.R;
import com.bixin.bixin.activities.LoginActivity;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.ui.MyWebView;
import com.bixin.bixin.util.ActivityJumpUtil;

public class LiveWebViewActivity extends BaseFragmentActivity {

	public static final String URL = "url";
	public static final String ANIM_DATA = "anim_data";
	public static final String ACTIVITY_COIN = "coin";

	protected MyWebView mWebView;
	private String msUrl;
	//活动数据累计调用js(修复活动页未加载完成，未找到js函数bug)，ConcurrentLinkedQueue保证线程安全
	protected ConcurrentLinkedQueue<JSONObject> urlDatas = new ConcurrentLinkedQueue<JSONObject>();
	//标记活动页是否加载完成
	private boolean webFlag;
	//用户抽中的奖励
	private int mCoin;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		isSystemBarTint = false;
		super.onCreate(savedInstanceState);
	}

	// 友盟统计
	public void onResume() {
		super.onResume();
		mWebView.setWebJavaScriptEnabled(true);
	}

	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mWebView.setWebJavaScriptEnabled(false);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_live_web_view;
	}

	@Override
	protected void initMembers() {
		getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);
		mWebView = (MyWebView) findViewById(R.id.web_view_wv);
		mWebView.hideProgressbar();         //隐藏进度条
		mWebView.setBackgroundColor(0);
	}

	@Override
	public void initWidgets() {
		// js定义的名称
		mWebView.addJavascriptInterface(new JsInvokeMainClass(), "recharge");// web
		LZCookieStore.synCookies(LiveWebViewActivity.this);
	}


	@Override
	protected void initData(Bundle savedInstanceState) {
		try {
			Intent intent = getIntent();
			if (intent != null) {
				msUrl = intent.getStringExtra(URL);
				//累计活动
				urlDatas.offer(new JSONObject(intent.getStringExtra(ANIM_DATA)));
				mWebView.loadUrl(msUrl);
				mWebView.setWebChromeClient(new WebChromeClient() {
					@Override
					public void onProgressChanged(WebView view, int newProgress) {
						super.onProgressChanged(view, newProgress);
						if (newProgress >= 100) {
							webFlag = true;
							while (!urlDatas.isEmpty())
								mWebView.loadUrl("javascript:showWebviewAnimation('" + urlDatas.poll() + "')");
						}
					}
				});
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		try {
			//累计活动
			urlDatas.offer(new JSONObject(intent.getStringExtra(ANIM_DATA)));
			if (webFlag) {
				while (!urlDatas.isEmpty())
					mWebView.loadUrl("javascript:showWebviewAnimation('" + urlDatas.poll() + "')");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		mWebView.removeAllViews();
		mWebView.destroy();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_LOGIN " + resultCode);
			if (resultCode == LoginActivity.RESULT_CODE_OK) {
				LZCookieStore.synCookies(LiveWebViewActivity.this);
				mWebView.reload(); // 刷新
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.dialog_enter, R.anim.dialog_exit);
	}

	@Override
	protected void setEventsListeners() {
	}


	/**
	 * 提供给js调用 ClassName: Contact <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-7-25 上午9:57:04 <br/>
	 *
	 * @version WebViewActivity
	 * @since JDK 1.6
	 */

	public class JsInvokeMainClass {
		// JavaScript调用此方法Login
		@JavascriptInterface
		public void needLogin() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Utils.requestLoginOrRegister(mActivity, mActivity.getResources()
							.getString(R.string.tip_login_title), Constants.REQUEST_CODE_LOGIN);
				}
			});

		}

		@JavascriptInterface
		public void goBack() {
			EvtLog.e(TAG, "JsInvokeMainClass goBack");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onBackPressed();
				}
			});
		}

		@JavascriptInterface
		public void goPersonInfo(final String uid) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Map<String, String> personInfo = new HashMap<String, String>();
					personInfo.put("id", uid);
					ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
				}
			});
		}

		@JavascriptInterface
		public void roomDetail(final String rid) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					@SuppressWarnings("unchecked")
					Map<String, Object> lmItem = new HashMap<String, Object>();
					lmItem.put("rid", rid);
					ActivityJumpUtil.toLiveMediaPlayerActivity(mActivity, lmItem);
				}
			});
		}

		@JavascriptInterface
		public void refreshCoin(final String num) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
//					mCoin = Integer.parseInt(num);
//					Intent intent = new Intent(LiveMediaPlayerActivity.REWARD_COIN_RECEIVER);
//					intent.putExtra(ACTIVITY_COIN, mCoin);
//					intent.setPackage(mActivity.getPackageName());
//					mActivity.getApplicationContext().sendBroadcast(intent);
				}
			});
		}

		@JavascriptInterface
		public void refreshPackage() {
//			Intent intent = new Intent(LiveMediaPlayerActivity.REWARD_GUARD_GIFT_RECEIVER);
//			intent.setPackage(mActivity.getPackageName());
//			mActivity.getApplicationContext().sendBroadcast(intent);
		}
	}

}
