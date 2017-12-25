/**
 * Project Name:feizao File Name:OnUpdateListener.java Package
 * Name:com.efeizao.feizao.listeners Date:2015-8-5上午11:51:30
 */

package com.bixin.bixin.listeners;

/**
 * Activity更新Fragment的接口，Fragment应该实现此接口，以便Activity可以动态更新Fragment状态
 */
public interface OnUpdateListener {

	/**
	 * Tab标签选中监听
	 */
	void onTabClick();

	/**
	 * Tab标签选中后，再次点击，将回调此方法
	 */
	void onTabClickAgain();
}
