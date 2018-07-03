package com.lib.common.network.http.exception

import android.text.TextUtils
import android.util.Log
import com.google.gson.JsonParseException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException

/**
 * Created by Admin
 */

class ExceptHandle {

    // 约定异常Code
    internal object ERROR {
        /**
         * 未知错误
         */
        val UNKNOWN = 1000
        /**
         * 解析错误
         */
        val PARSE_ERROR = 1001
        /**
         * 网络错误
         */
        val NETWORD_ERROR = 1002
        /**
         * 协议出错
         */
        val HTTP_ERROR = 1003

        /**
         * 证书出错
         */
        val SSL_ERROR = 1005
    }

    class ResponeThrowable(throwable: Throwable, var code: Int?) : Exception(throwable) {
        override var message: String? = null
    }

    companion object {
        private val UNAUTHORIZED = 401
        private val FORBIDDEN = 403
        private val NOT_FOUND = 404
        private val REQUEST_TIMEOUT = 408
        private val INTERNAL_SERVER_ERROR = 500
        private val BAD_GATEWAY = 502
        private val SERVICE_UNAVAILABLE = 503
        private val GATEWAY_TIMEOUT = 504
        private val RESPONSE = mapOf(
                Pair(UNAUTHORIZED, "请求未授权"),
                Pair(NOT_FOUND, "未知请求"),
                Pair(REQUEST_TIMEOUT, "请求超时"),
                Pair(INTERNAL_SERVER_ERROR, "服务器报错"),
                Pair(BAD_GATEWAY, "网络异常"),
                Pair(SERVICE_UNAVAILABLE, "服务器无效"),
                Pair(GATEWAY_TIMEOUT, "网络超时")
        )

        fun handleException(e: Throwable): ResponeThrowable {
            val ex: ResponeThrowable
            Log.i("tag", "e.toString = " + e.toString())
            if (e is HttpException) {
                ex = ResponeThrowable(e, ERROR.HTTP_ERROR)
                ex.message = RESPONSE[e.code()]
                if (TextUtils.isEmpty(ex.message)) {
                    ex.message = "未知错误"
                }
                return ex
            } else if (e is ServerException) {
                ex = ResponeThrowable(e, e.code)
                ex.message = e.message
                return ex
            } else if (e is JsonParseException || e is JSONException) {
                ex = ResponeThrowable(e, ERROR.PARSE_ERROR)
                ex.message = "解析错误"
                return ex
            } else if (e is ConnectException) {
                ex = ResponeThrowable(e, ERROR.NETWORD_ERROR)
                ex.message = "连接失败"
                return ex
            } else if (e is javax.net.ssl.SSLHandshakeException) {
                ex = ResponeThrowable(e, ERROR.SSL_ERROR)
                ex.message = "证书验证失败"
                return ex
            } else {
                ex = ResponeThrowable(e, ERROR.UNKNOWN)
                ex.message = "未知错误"
                return ex
            }/*|| e instanceof ParseException*/
        }
    }
}
