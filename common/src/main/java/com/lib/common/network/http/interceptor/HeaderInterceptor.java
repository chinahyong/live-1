package com.lib.common.network.http.interceptor;

import java.io.IOException;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 请求头部信息拦截器
 */

public class HeaderInterceptor implements Interceptor {
	private Map<String, String> mHeaders;

	public HeaderInterceptor(Map<String, String> header) {
		mHeaders = header;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request().newBuilder()
				.headers(Headers.of(mHeaders))
				.build();
		return chain.proceed(request);
	}
}
