package com.lib.common.network.http;

import android.text.TextUtils;

import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by author on 26/06/2018.
 */

public class RetrofitServiceManager {
	public static int DEFAULT_TIME_OUT = 5;
	public static int DEFAULT_READ_TIME_OUT = 10;
	public static int DEFAULT_WRITE_TIME_OUT = 10;

	private Retrofit mRetrofit;

	private RetrofitServiceManager() {

		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
				.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS)
				.writeTimeout(DEFAULT_WRITE_TIME_OUT, TimeUnit.SECONDS)
				.cookieJar(HttpConfig.mCookieJar);
		if (HttpConfig.mInterceptors != null) {
			for (Interceptor interceptor :
					HttpConfig.mInterceptors) {
				builder.addInterceptor(interceptor);
			}
		}

		if (!TextUtils.isEmpty(HttpConfig.mBaseUrl)) {
			mRetrofit = new Retrofit.Builder()
					.client(builder.build())
					.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
					.addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
					.baseUrl(HttpConfig.mBaseUrl)
					.build();
		}
	}

	private static class SingletonHolder {
		public static final RetrofitServiceManager INSTANCE = new RetrofitServiceManager();
	}

	public static RetrofitServiceManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public <T> T create(Class<T> clazz) {
		return mRetrofit.create(clazz);
	}
}
