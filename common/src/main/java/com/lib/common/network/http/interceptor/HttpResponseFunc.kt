package com.lib.common.network.http.interceptor

import com.lib.common.network.http.bean.HttpConstants
import com.lib.common.network.http.bean.HttpResponse
import com.lib.common.network.http.exception.ServerException
import io.reactivex.functions.Function


/**
 * Created by Admin
 * 数据返回拦截器
 */

class HttpResponseFunc<T> : Function<HttpResponse<T>, T> {
    override fun apply(response: HttpResponse<T>): T? {
        if (response.code != HttpConstants.RESPONSE_STATUS_SUCCESS) {
            throw ServerException(response.code, response.msg)
        }
        return response.data
    }
}
