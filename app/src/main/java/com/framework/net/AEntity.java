package com.framework.net;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bixin.bixin.common.model.HttpConstants;

/**
 * Title: 消息实体类 (类或者接口名称)
 * Description: XXXX (简单对此类或接口的名字进行描述)
 * Copyright: Copyright (c) 2012
 *
 * @version 1.0
 */
public abstract class AEntity {

	public String url;

	public String flag;

	public String sendData;
	/**
	 * 发送状态
	 */
	public String sentStatus = HttpConstants.SENT_STATUS_WAIT;

	/**
	 * HTTP返回的头信码
	 */
	public int http_ResponseCode;
	/**
	 * 消息临听者
	 */
	private IReceiverListener receiverListener;

	/**
	 * 消息体（post消息）
	 */
	public Map<String, String> postDatas;
	/**
	 * 消息头
	 */
	public Map<String, String> headers;
	/**
	 * 消息体(json)
	 */
	public String jsonStr;
	/**
	 * 服务器端返回的数据
	 */
	public String receiveData;

	/**
	 * 服务端返回的Cookies
	 */
	public Map<String, List<String>> receiveHeaders;

	public AEntity(IReceiverListener receiverListener) {
		this.receiverListener = receiverListener;
		init();
		headers = new HashMap<>();
		postDatas = new HashMap<String, String>();
		receiveHeaders = new HashMap<String, List<String>>();
	}

	/**
	 * 初始化一些数据，如Flag、URL
	 * Name:
	 * Description:
	 * Author:
	 *
	 * @return
	 */
	protected abstract void init();

	/**
	 * 初始化Http头信息
	 * Name:
	 * Description:
	 * Author:
	 *
	 * @return
	 */
	protected abstract void initHttpHeader();

	/**
	 * 获取发送数据
	 * Name:
	 * Description:
	 * Author:
	 *
	 * @return
	 */
	public abstract String getSendData();

	/**
	 * 解码网络返回的数据
	 * Name:
	 * Description:
	 * Author:
	 *
	 * @return
	 */
	public abstract void decodeReceiveData();

	/**
	 * 回传消息
	 * Name:
	 * Description:
	 * Author:
	 *
	 * @return
	 */
	public final void onReceive() {
		receiverListener.onReceive(this);
	}

	/**
	 * 解析网络返回的数据
	 *
	 * @param receiveData 响应内容
	 */
	public abstract void decodeReceiveData(String receiveData);

	/**
	 * http post请求数据
	 */
	public abstract void setSendData(String data);
}
