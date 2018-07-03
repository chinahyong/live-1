package com.lib.common.network.http.subscriber

import android.content.Context
import com.lib.common.network.http.exception.ExceptHandle
import com.lib.common.utils.ToastUtil
import com.lib.common.widget.LoadingDialogBuilder
import io.reactivex.disposables.Disposable

/**
 * Created by Admin
 */

abstract class RxObserver<T>(private val mContext: Context) : ErrorObserver<T>() {

    override fun onComplete() {
        LoadingDialogBuilder.dismissDialog()
    }

    public override fun onError(throwable: ExceptHandle.ResponeThrowable) {
        LoadingDialogBuilder.dismissDialog()
        ToastUtil.showToast(mContext, throwable.message)
    }

    override fun onNext(t: T) {

    }

    override fun onSubscribe(p0: Disposable) {
        LoadingDialogBuilder.showDialog(mContext)
    }
}
