package tv.live.bx.listeners;

/**
 * @author Live
 * @version 2016/10/17 2.6.5
 * @title OnUnreadListener Description:未读消息回调(未关注、已关注)
 */
public interface OnUnreadListener {
	/**
	 * 未读消息回调
	 * 回调到关注状态下
	 *
	 * @param focusUnreadCount 关注未读数
	 * @param unFocusUnreadCount 未关注未读数
	 */
	void onUnread(int focusUnreadCount, int unFocusUnreadCount);
}
