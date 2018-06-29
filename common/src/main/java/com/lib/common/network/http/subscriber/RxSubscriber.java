package com.lib.common.network.http.subscriber;

import android.content.Context;

import com.lib.common.network.http.ExceptHandle;
import com.lib.common.utils.ToastUtil;
import com.lib.common.widget.LoadingDialogBuilder;

/**
 * Created by Admin
 */

public abstract class RxSubscriber<T> extends ErrorSubscriber<T> {
	private Context mContext;

	public RxSubscriber(Context context) {
		mContext = context;
	}

	@Override
	public void onCompleted() {
		LoadingDialogBuilder.showDialog(mContext);
	}

	@Override
	public void onError(ExceptHandle.ResponeThrowable throwable) {
		LoadingDialogBuilder.showDialog(mContext);
		ToastUtil.showToast(mContext, throwable.message);
	}

	@Override
	public void onNext(T t) {

	}

	@Override
	public void onStart() {
		super.onStart();
		LoadingDialogBuilder.showDialog(mContext);
	}
}
