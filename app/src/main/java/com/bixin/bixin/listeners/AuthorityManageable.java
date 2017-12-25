/**
 * Project Name:feizao File Name:AuthorityManageable.java Package
 * Name:com.efeizao.feizao.listeners Date:2016-2-25下午6:40:53
 */

package com.bixin.bixin.listeners;

/**
 * ClassName:AuthorityManageable Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: 2016-2-25 下午6:40:53
 * @author Live
 * @version 1.0
 */
public interface AuthorityManageable {

	/**
	 * 该用户是否为自己
	 */
	boolean onIsOwen(String uid);

	/**
	 * 该用户是否为圈管理员
	 */
	boolean onIsGroupAdmin(String isGroupAdmin);

	/**
	 * 该用户是否为圈主
	 */
	boolean onIsGroupOwen(String isGroupOwner);
}
