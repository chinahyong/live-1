package com.lib.common.network.http;

import java.util.ArrayList;
import java.util.List;

import okhttp3.CookieJar;
import okhttp3.Interceptor;

/**
 * Created by author on 26/06/2018.
 */

public class HttpConfig {
	static String mBaseUrl;
	static List<Interceptor> mInterceptors;
	static CookieJar mCookieJar;
	private HttpConfig() {

	}

	public static class Builder {
		private HttpConfig mHttpConfig;

		public Builder() {
			mHttpConfig = new HttpConfig();
		}

		public Builder baseUrl(String baseUrl) {
			mBaseUrl = baseUrl;
			return this;
		}

		public Builder interceptors(Interceptor interceptor) {
			if (mInterceptors == null) {
				mInterceptors = new ArrayList<>();
			}
			mInterceptors.add(interceptor);
			return this;
		}

		public Builder cookie(CookieJar cookieJar){
			mCookieJar = cookieJar;
			return this;
		}

		public HttpConfig build() {
			return mHttpConfig;
		}
	}
}
