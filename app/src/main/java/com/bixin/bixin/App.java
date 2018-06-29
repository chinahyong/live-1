/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bixin.bixin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.DisplayMetrics;

import com.bixin.bixin.common.bean.HttpNetConstants;
import com.bixin.bixin.common.bean.LibConstants;
import com.bixin.bixin.config.AppConfig;
import com.bixin.bixin.config.DomainConfig;
import com.bixin.bixin.config.UserInfoConfig;
import com.bixin.bixin.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.common.CrashHandler;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.PackageUtil;
import com.bixin.bixin.util.ChannelUtil;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.lib.common.network.http.HttpConfig;
import com.lib.common.network.http.interceptor.HeaderInterceptor;
import com.lonzh.lib.network.LZCookieStore;
import com.pili.pldroid.streaming.StreamingEnv;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;
import com.umeng.socialize.utils.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;
import tv.guojiang.baselib.BaseLibConfig;
import tv.guojiang.baselib.ConfigBuilder;
import tv.guojiang.baselib.image.factory.GlideFactory;
import tv.live.bx.BuildConfig;
import tv.live.bx.R;


/**
 * because you can not use any other class in your application, we need to
 * move your implement of Application to {@link}
 * As Application, all its direct reference class should be in the main dex.
 * <p>
 * We use tinker-android-anno to make sure all your classes can be patched.
 * <p>
 * application: if it is start with '.', we will add SampleApplicationLifeCycle's package name
 * <p>
 * flags:
 * TINKER_ENABLE_ALL: support dex, lib and resource
 * TINKER_DEX_MASK: just support dex
 * TINKER_NATIVE_LIBRARY_MASK: just support lib
 * TINKER_RESOURCE_MASK: just support resource
 * <p>
 * loaderClass: define the tinker loader class, we can just use the default TinkerLoader
 * <p>
 * loadVerifyFlag: whether check files' md5 on the load time, defualt it is false.
 * <p>
 * Created by zhangshaowen on 16/3/17.
 */
public class App extends Application {
	private final static String TAG = App.class.getSimpleName();
	private static Map<String, Object> mmDataCache = new HashMap<>();
	private LZCookieStore moCookieStore;
	public static Context mContext;
	private static WeakReference<Activity> app_activity;
	/**
	 * 用于获取该主播是否正在进行直播
	 */
	public static boolean isLiveRunning = false;
	public static boolean isDebug = true;
	public static DisplayMetrics metrics;

	private static RefWatcher mRefWatcher;

	@Override
	public void onCreate() {
		super.onCreate();

		mContext = getApplicationContext();
		metrics = mContext.getResources().getDisplayMetrics();

		if (BuildConfig.DEBUG) {
			// 屏蔽错误日志上传
			MobclickAgent.setCatchUncaughtExceptions(false);
			// 重定义异常捕获
			loadCrashHandler();
			// 在android 2.3上运行时报android.os.NetworkOnMainThreadException异常，在2.3中，访问网络不能在主程序中进行
			if (android.os.Build.VERSION.SDK_INT > 9) {
				// 针对线程的相关策略
				StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//						.detectDiskReads()
//						.detectDiskWrites()
//						.detectNetwork()   // or .detectAll() for all detectable problems
//						.penaltyLog()
						.build());
			}
		}
		initHttpConfig();
		initReadKey();
		initConfigs();
		MobclickAgent.openActivityDurationTrack(false);

		moCookieStore = new LZCookieStore(mContext);
		// 初始化七牛推流环境
		StreamingEnv.init(mContext.getApplicationContext());
		initJpush();
		// 初始化umeng
		initUmeng();
		//检测内存泄露
		mRefWatcher = LeakCanary.install(this);
		//打印设备信息
		EvtLog.d(TAG, PackageUtil.getDeviceInfo(mContext));
		// 获取栈顶activity
		initGlobeActivity();
		initBaseLibrary();
	}

	private void initJpush() {
		/** 极光推送初始化 */
		if (PackageUtil.isDebugMode(mContext)) {
			JPushInterface.setDebugMode(BuildConfig.DEBUG);
		}
		JPushInterface.init(mContext);
		JPushInterface.setLatestNotificationNumber(mContext, 3);
	}

	private void initHttpConfig() {
		Map<String, String> headers = new HashMap<>();
		headers.put("User-Agent", "mobile");
		HeaderInterceptor headerInterceptor = new HeaderInterceptor(headers);
		ClearableCookieJar cookieJar =
				new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(mContext));
		new HttpConfig.Builder()
				.baseUrl(HttpNetConstants.BASE_URL_SERVER)
				.interceptors(headerInterceptor)
				.cookie(cookieJar)
				.build();
	}

	private void initBaseLibrary() {
		BaseLibConfig.init(new ConfigBuilder()
				.imageFactory(new GlideFactory()));
	}

	/**
	 * 设置主播是否正在直播中，在Activity的生命周期中做，所以目前都是在主线程做，不用加线程安全控制
	 *
	 * @param status
	 */
	public static void setIsLiveRunning(boolean status) {
		App.isLiveRunning = status;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		ImageLoaderUtil.getInstance().clearMemery(mContext);
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		ImageLoaderUtil.getInstance().clearMemery(mContext);
	}

	public static RefWatcher getRefWatcher() {
		return mRefWatcher;
	}

	private void loadCrashHandler() {
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(mContext);
	}

	public LZCookieStore getCookieStore() {
		return moCookieStore;
	}


	private void initReadKey() {
		try {
			InputStream is = mContext.getResources().openRawResource(R.raw.public_key);
			int size = is.available();

			// Read the entire asset into a local byte buffer.
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();

			// Convert the buffer into a string.
			String text = new String(buffer, "utf-8");
			String lsKey = text.replaceAll("-----BEGIN PUBLIC KEY-----|-----END PUBLIC KEY-----", "").trim();
			EvtLog.e(TAG, lsKey);
			setCacheData("public_key", lsKey);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从文件中读取Config信息
	 */
	private void initConfigs() {
		new Thread() {
			@Override
			public void run() {
				DomainConfig.getInstance();
				AppConfig.getInstance();
				UserInfoConfig.getInstance();
			}
		}.start();
	}

	public static void setCacheData(String psKey, Object poValue) {
		mmDataCache.put(psKey, poValue);
	}

	public static Object getCacheData(String psKey) {
		return mmDataCache.get(psKey);
	}

	/**
	 * 初始化umeng分享
	 */
	private void initUmeng() {
		// 代码设置渠道号
		AnalyticsConfig.setChannel(ChannelUtil.getChannel(mContext));
		Config.isJumptoAppStore = true;  //没用第三方app时进行跳转下载
		Log.LOG = true;    //关闭log
		UMShareConfig config = new UMShareConfig();
		config.isNeedAuthOnGetUserInfo(true);
		UMShareAPI.get(mContext).setShareConfig(config);
		// 微信
		PlatformConfig.setWeixin(LibConstants.WEIXIN_APPID, LibConstants.WEIXIN_APPSECRET);
	}

	public static WeakReference<Activity> getTopActivity() {
		return app_activity;
	}

	/**
	 * 获取栈顶activity
	 * 通过注册activity的生命周期监听获取最新的栈顶activity
	 */
	private void initGlobeActivity() {
		registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
				app_activity = new WeakReference<>(activity);
				Log.e("onActivityCreated===", app_activity + "");
			}

			@Override
			public void onActivityDestroyed(Activity activity) {
			}

			/** Unused implementation **/
			@Override
			public void onActivityStarted(Activity activity) {
				app_activity = new WeakReference<>(activity);
			}

			@Override
			public void onActivityResumed(Activity activity) {
				app_activity = new WeakReference<>(activity);
			}

			@Override
			public void onActivityPaused(Activity activity) {
			}

			@Override
			public void onActivityStopped(Activity activity) {
			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
			}
		});
	}
}
