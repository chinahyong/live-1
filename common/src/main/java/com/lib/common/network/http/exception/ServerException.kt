package com.lib.common.network.http.exception

/**
 * ServerException发生后，将自动转换为ResponeThrowable返回
 */
open class ServerException(var code: Int?, override var message: String?) : RuntimeException()