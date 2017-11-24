package cn.efeizao.feizao.framework.net;

/**
 * Title: 消息回传 (类或者接口名称)
 * Description: XXXX (简单对此类或接口的名字进行描述)
 * Copyright: Copyright (c) 2012
 * @version 1.0
 */
public interface IReceiverListener {
	/**
	 * 回传消息结果
	 * Name:    
	 * Description: 
	 * Author:    
	 * @param entity    
	 * @return   
	 *
	 */
	void onReceive(AEntity entity);
}
