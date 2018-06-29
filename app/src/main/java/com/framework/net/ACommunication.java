package com.framework.net;

/**
 * Title: 消息发送模板 (类或者接口名称)
 * Description: XXXX (简单对此类或接口的名字进行描述)
 * Copyright: Copyright (c) 2012
 * @version 1.0
 */
public abstract class ACommunication {
	protected AEntity entity;

	public ACommunication(AEntity entity) {
		this.entity = entity;
	}

	public final void sendEntity() {
		try {
			send();
		} catch (Exception ex) {
			catchException(ex);
		}
		entity.onReceive();
	}

	/**
	 * 发送消息，并将消息返回
	 * Name:    
	 * Description: 
	 * Author:        
	 * @return   
	 * @throws Exception 
	 *
	 */
	protected abstract void send() throws Exception;

	/**
	 * 捕获异常，如有网络错误，设置消息体的SentStatus
	 * Name:    
	 * Description: 
	 * Author:    
	 * @param ex    
	 * @return   
	 *
	 */
	protected abstract void catchException(Exception ex);
}
