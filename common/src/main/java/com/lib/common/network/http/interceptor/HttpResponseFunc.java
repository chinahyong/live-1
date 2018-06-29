package com.lib.common.network.http.interceptor;

import com.lib.common.network.http.HttpConstants;
import com.lib.common.network.http.HttpResponse;

import rx.functions.Func1;


/**
 * Created by Admin
 * 数据返回拦截器
 */

public class HttpResponseFunc<T> implements Func1<HttpResponse<T>, T> {
	@Override
	public T call(HttpResponse<T> response) {
		if (response.code != HttpConstants.RESPONSE_STATUS_SUCCESS) {
			throw new IllegalStateException(response.msg);
		}
		return response.data;
	}
}
