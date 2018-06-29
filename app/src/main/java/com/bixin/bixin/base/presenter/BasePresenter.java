package com.bixin.bixin.base.presenter;

import android.support.annotation.Nullable;

import com.lib.common.network.http.interceptor.HttpResponseFunc;

import rx.Observable;


/**
 * Created by Admin
 */

public class BasePresenter<T> {
	public T mService;

	public <T> Observable<T> filterStatus(@Nullable Observable<T> observable) {
		if (observable == null) {
			return null;
		}
		return observable.map(new HttpResponseFunc());
	}
}
