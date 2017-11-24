package cn.efeizao.feizao.fmk.appupdate;

import java.io.File;

/**
 * Title: 数据下载观察者 (类或者接口名称)
 * Description: XXXX (简单对此类或接口的名字进行描述)
 * Copyright: Copyright (c) 2012
 * @version 1.0
 */
public abstract class AUpdateObserver {

	/**
	 * 下载链接
	 * Name:    
	 * Description: 
	 * Author:        
	 * @return   
	 *
	 */
	protected abstract String getDownloadUrl();

	/**
	 *  如果安装文件存在是否重新下载安装文件
	 * Name:    
	 * Description: 
	 * Author:        
	 * @return
	 *
	 */
	protected abstract boolean isRedownloadIfExists();

	/**
	 * 开始下载
	 * Name:    
	 * Description: 
	 * Author:        
	 * @return   
	 *
	 */
	protected abstract void start();

	/**
	 * 下载中
	 * Name:    
	 * Description: 
	 * Author:    
	 * @param perent
	 * 下载进度    
	 * @return   
	 *
	 */
	protected abstract void downloading(int perent);

	/**
	 * 下载成功
	 * Name:    
	 * Description: 
	 * Author:        
	 * @return   
	 *
	 */
	protected abstract void finish(File file);

	/**
	 * 下载失败
	 * Name:    
	 * Description: 
	 * Author:        
	 * @return   
	 *
	 */
	protected abstract void error(int errorCode);

	/**
	 * 无存储卡
	 * Name:    
	 * Description: 
	 * Author:        
	 * @return   
	 *
	 */
	protected void noMiniSD() {

	}

	/**
	 * 上一次下载未完成
	 * Name:    
	 * Description: 
	 * Author:        
	 * @return   
	 *
	 */
	protected void noFinish() {

	}
}
