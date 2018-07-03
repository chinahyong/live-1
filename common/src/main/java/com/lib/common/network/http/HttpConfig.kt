package com.lib.common.network.http

import java.util.ArrayList

import okhttp3.CookieJar
import okhttp3.Interceptor

/**
 * Created by author on 26/06/2018.
 */

object HttpConfig {
    internal var mBaseUrl: String
    internal var mInterceptors: MutableList<Interceptor>? = null
    internal var mCookieJar: CookieJar

    class Builder {
        private val mHttpConfig: HttpConfig

        init {
            mHttpConfig = HttpConfig()
        }

        fun baseUrl(baseUrl: String): Builder {
            mBaseUrl = baseUrl
            return this
        }

        fun interceptors(interceptor: Interceptor): Builder {
            if (mInterceptors == null) {
                mInterceptors = ArrayList()
            }
            mInterceptors!!.add(interceptor)
            return this
        }

        fun cookie(cookieJar: CookieJar): Builder {
            mCookieJar = cookieJar
            return this
        }

        fun build(): HttpConfig {
            return mHttpConfig
        }
    }
}
