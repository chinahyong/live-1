package com.bixin.bixin.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.bixin.bixin.App;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import tv.live.bx.R;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.PackageUtil;
import com.bixin.bixin.util.ChannelUtil;

/**
 * Created by Live on 2014/11/29.
 */
public class MyWebView extends WebView {
	private String TAG = "MyWebView";
	private ProgressBar progressbar;
	private int processOffsetInit = 10;
	private int processOffset = processOffsetInit;
	private boolean processFinish = false;
	private int delayTime = 100;
	private Timer timer;
	private TimerTask mTimerTask;
	private int progress;
	private WeakReference<IWebDataInterface> iWebDataInterface;
	private WebSettings settings;

	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MyWebView(Context context) {
		super(context);
		init(context);
	}

	public void setOnIWebDataInterface(IWebDataInterface iWebDataInterface) {
		this.iWebDataInterface = new WeakReference<IWebDataInterface>(iWebDataInterface);
	}

	private void init(Context context) {
		progressbar = (ProgressBar) LayoutInflater.from(context).inflate(R.layout.umc_webview_progressbar, null);
		progressbar.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (3 * App.metrics.density)));
		addView(progressbar);
		// setWebViewClient(new WebViewClient(){});
		setVerticalScrollBarEnabled(true);
		setHorizontalScrollBarEnabled(false);
		setWebChromeClient(new WebChromeClient());
		setWebViewClient(new AuthnWebViewClient());

		settings = this.getSettings();
		settings.setUserAgentString(settings.getUserAgentString() + Constants.WEB_VIEW_AGENT
				+ Constants.WEB_VIEW_AGENT_PACKAGE + Constants.PACKAGE_ID + Constants.WEB_VIEW_AGENT_VERSION
				+ PackageUtil.getVersionName() + Constants.WEB_VIEW_AGENT_CHANNEL + ChannelUtil.getChannel(context));
		settings.setUseWideViewPort(true);
		settings.setSupportZoom(true);
		settings.setJavaScriptEnabled(true);
		settings.setSavePassword(false);
		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
		settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
		settings.setDatabaseEnabled(true);
		settings.setDatabasePath(context.getApplicationContext().getDir("databases", 0).getPath());

		settings.setDomStorageEnabled(true);
	}

	public void setOpenFileChooserCallBack(OpenFileChooserCallBack openFileChooserCallBack) {
		setWebChromeClient(new WebChromeClient(openFileChooserCallBack));
	}

	/**
	 * 导致一直占有cpu 耗电特别快，所以大家记住了，如果遇到这种情况 请在onstop和onresume里分别把setJavaScriptEnabled();
	 */
	public void setWebJavaScriptEnabled(boolean flag) {
		settings.setJavaScriptEnabled(flag);
	}

	@Override
	public void destroy() {
		super.destroy();
		//换成QQ流浪器X5内核
//		releaseAllWebViewCallback();
		EvtLog.e(TAG, "progress:----------------destroy");
		if (null != timer) {
			timer.cancel();
			timer = null;
		}
		if (null != mTimerTask) {
			mTimerTask.cancel();
			mTimerTask = null;
		}
	}

	@Override
	public void loadUrl(String url) {
		EvtLog.e(TAG, "onTextMessage loadurl:" + url);
		super.loadUrl(url);
		processFinish = false;
	}

	/**
	 * 一段时候后还没有打开页面,设置网络失败
	 */
	private void connectTimeOut() {
		if (progressbar.getVisibility() == VISIBLE) {
			progressbar.setVisibility(GONE);
		}
		this.stopLoading();
	}

	/**
	 * 实在不想用开额外进程的方式解决webview 内存泄露的问题，webview的 destroy方法里 调用这个方法就行了
	 */
	public void releaseAllWebViewCallback() {
		if (android.os.Build.VERSION.SDK_INT < 16) {
			try {
				Field field = WebView.class.getDeclaredField("mWebViewCore");
				field = field.getType().getDeclaredField("mBrowserFrame");
				field = field.getType().getDeclaredField("sConfigCallback");
				field.setAccessible(true);
				field.set(null, null);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Field sConfigCallback = Class.forName("android.webkit.BrowserFrame").getDeclaredField("sConfigCallback");
				if (sConfigCallback != null) {
					sConfigCallback.setAccessible(true);
					sConfigCallback.set(null, null);
				}
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public class WebChromeClient extends android.webkit.WebChromeClient {

		private OpenFileChooserCallBack mOpenFileChooserCallBack;

		public WebChromeClient() {
		}

		public WebChromeClient(OpenFileChooserCallBack openFileChooserCallBack) {
			mOpenFileChooserCallBack = openFileChooserCallBack;
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			EvtLog.e(TAG, "onProgressChanged: " + newProgress);
			if (newProgress == 100) {
				processOffset = processOffsetInit;
				processFinish = true;
				if (timer != null)
					timer.cancel();
				progressbar.setVisibility(GONE);
			} else {

				if (processOffset == processOffsetInit) {
					progressbar.setVisibility(View.VISIBLE);
					progressbar.setProgress(newProgress);
					progress = newProgress + processOffset;

					if (null != timer) {
						timer.cancel();
					}
					timer = new Timer();
					if (null != mTimerTask) {
						mTimerTask.cancel();
					}
					mTimerTask = new TimerTask() {

						@Override
						public void run() {
							if (progress < 90 && !processFinish) {
								EvtLog.e(TAG, "progress:" + progress);
								progressbar.setProgress(progress);
								if (processOffset > 1)
									processOffset--;

								progress += processOffset;

							}
						}
					};
					timer.schedule(mTimerTask, delayTime, delayTime);


				}
			}
			super.onProgressChanged(view, newProgress);
		}

		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			EvtLog.e(TAG, "onReceivedTitle title:" + title);
			if (iWebDataInterface != null && iWebDataInterface.get() != null)
				iWebDataInterface.get().onTitle(title);
		}

		// For Android 3.0+
		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
			mOpenFileChooserCallBack.openFileChooserCallBack(uploadMsg, acceptType);
		}

		// For Android < 3.0
		public void openFileChooser(ValueCallback<Uri> uploadMsg) {
			openFileChooser(uploadMsg, "");
		}

		// For Android > 4.1.1
		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
			openFileChooser(uploadMsg, acceptType);
		}

		@Override
		public boolean onShowFileChooser(WebView webView,
										 ValueCallback<Uri[]> filePathCallback,
										 FileChooserParams fileChooserParams) {
			mOpenFileChooserCallBack.openFileChooserCallBack(filePathCallback);
			return true;
		}

	}

	protected void onDetachedFromWindow() {
		EvtLog.e(TAG, "onDetachedFromWindow:");
	}

	/**
	 * webview 监听回调类
	 *
	 * @author Yhyu
	 */
	private class AuthnWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			EvtLog.d(TAG, "OverrideUrlLoading url" + url);
			if (url.startsWith("https") || url.startsWith("http")) {
				return super.shouldOverrideUrlLoading(view, url);
			} else {
				try {
					view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			EvtLog.d(TAG, "onPageStarted url" + url);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			EvtLog.d(TAG, "onPageFinished url:" + url);
		}


		@Override
		public void onLoadResource(WebView view, String url) {
			super.onLoadResource(view, url);
			EvtLog.e(TAG, "onLoadResource:" + url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			EvtLog.e(TAG, "onReceivedError:" + errorCode + " ," + description + "," + failingUrl);
			super.onReceivedError(view, errorCode, description, failingUrl);
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
//		LayoutParams lp = (LayoutParams) progressbar.getLayoutParams();
//		lp.x = l;
//		lp.y = t;
//		progressbar.setLayoutParams(lp);
		super.onScrollChanged(l, t, oldl, oldt);
	}

	public interface IWebDataInterface {
		/**
		 * 　网页标题
		 */
		void onTitle(String title);
	}

	public interface OpenFileChooserCallBack {
		void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType);

		void openFileChooserCallBack(ValueCallback<Uri[]> filePathCallback);
	}

	/**
	 * 进度更新会显示进度条，故在此直接设置进度条width、height为0
	 */
	public void hideProgressbar() {
		progressbar.setVisibility(View.GONE);
	}
}
