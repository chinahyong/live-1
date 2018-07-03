package com.lib.common.network.http

import android.text.TextUtils
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by author on 26/06/2018.
 */

class RetrofitServiceManager private constructor() {

    private var mRetrofit: Retrofit? = null

    init {

        val builder = OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIME_OUT.toLong(), TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIME_OUT.toLong(), TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_WRITE_TIME_OUT.toLong(), TimeUnit.SECONDS)
                .cookieJar(HttpConfig.mCookieJar)
        if (HttpConfig.mInterceptors != null) {
            for (interceptor in HttpConfig.mInterceptors!!) {
                builder.addInterceptor(interceptor)
            }
        }

        if (!TextUtils.isEmpty(HttpConfig.mBaseUrl)) {
            mRetrofit = Retrofit.Builder()
                    .client(builder.build())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                    .baseUrl(HttpConfig.mBaseUrl)
                    .build()
        }
    }

    private object SingletonHolder {
        val INSTANCE = RetrofitServiceManager()
    }

    fun <T> create(clazz: Class<T>): T {
        return mRetrofit!!.create(clazz)
    }

    companion object {
        var DEFAULT_TIME_OUT = 5
        var DEFAULT_READ_TIME_OUT = 10
        var DEFAULT_WRITE_TIME_OUT = 10

        val instance: RetrofitServiceManager
            get() = SingletonHolder.INSTANCE
    }
}
