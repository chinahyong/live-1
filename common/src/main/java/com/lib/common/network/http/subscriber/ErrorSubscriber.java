package com.lib.common.network.http.subscriber;

import com.lib.common.network.http.ExceptHandle;

import rx.Subscriber;

/**
 * Created by Admin
 */

public abstract class ErrorSubscriber<T> extends Subscriber<T> {

	@Override
	public void onError(Throwable throwable) {
		if (throwable instanceof ExceptHandle.ResponeThrowable) {
			onError((ExceptHandle.ResponeThrowable) throwable);
		} else {
			onError(new ExceptHandle.ResponeThrowable(throwable, 1));
		}
	}

	protected abstract void onError(ExceptHandle.ResponeThrowable e);
}
