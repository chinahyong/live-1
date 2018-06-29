package com.lib.common.network.http;

/**
 * Created by Admin
 * Http response 返回格式
 */

public class HttpResponse<T> {
	public int code;
	public String msg;
	public T data;
}
