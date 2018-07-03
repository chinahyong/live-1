package com.bixin.bixin.base.presenter;

import android.support.annotation.Nullable;

import com.lib.common.network.http.interceptor.HttpResponseFunc;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;


/**
 * Created by Admin
 */

public class BasePresenter<T, V> {
	protected WeakReference<V> weak;
	protected T mService;
	protected V mView;

	public BasePresenter(V view) {
		weak = new WeakReference<>(view);
		mView = weak.get();
	}

	public <T> Observable<T> filterStatus(@Nullable Observable<T> observable) {
		if (observable == null) {
			return null;
		}
		return observable.map(new HttpResponseFunc());
	}

	public void onDestroy() {
		if (weak != null) {
			weak.clear();
		}
		mView = null;
		mService = null;
	}
}
