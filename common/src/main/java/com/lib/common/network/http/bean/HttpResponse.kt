package com.lib.common.network.http.bean

/**
 * Created by Admin
 * Http response 返回格式
 */

class HttpResponse<T> {
    var code: Int = 0
    var msg: String? = null
    var data: T? = null
}
