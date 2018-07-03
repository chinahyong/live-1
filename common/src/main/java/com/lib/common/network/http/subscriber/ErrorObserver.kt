package com.lib.common.network.http.subscriber

import com.lib.common.network.http.exception.ExceptHandle
import io.reactivex.Observer

/**
 * Created by Admin
 */
abstract class ErrorObserver<T> : Observer<T> {

    override fun onError(throwable: Throwable) {
        if (throwable is ExceptHandle.ResponeThrowable) {
            onError(ExceptHandle.handleException(throwable))
        } else {
            onError(ExceptHandle.handleException(ExceptHandle.ResponeThrowable(throwable, 1)))
        }
    }

    protected abstract fun onError(e: ExceptHandle.ResponeThrowable)
}
