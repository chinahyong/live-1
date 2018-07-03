package com.lib.common.network.http.interceptor

import java.io.IOException

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * 请求头部信息拦截器
 */

class HeaderInterceptor(private val mHeaders: Map<String, String>) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
                .headers(Headers.of(mHeaders))
                .build()
        return chain.proceed(request)
    }
}
