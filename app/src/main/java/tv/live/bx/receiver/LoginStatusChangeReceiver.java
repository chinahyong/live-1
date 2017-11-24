/**
 * Project Name:feizao File Name:ConnectionChangeReceiver.java Package
 * Name:com.efeizao.feizao.receiver Date:2015-6-23下午6:37:38
 */

package tv.live.bx.receiver;

/**
 * 用户登录状态改变 ADD REASON. Date: 2015-6-23 下午6:37:38
 *
 * @version 1.0
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LoginStatusChangeReceiver extends BroadcastReceiver {

	public static final String LOGIN_STATUS_ChANGE_ACTION = "login_status_change_action";
	public static final String LOGIN_STATUS_TYPE = "login_status_type";
	public static final int TYPE_LOGIN = 1;
	public static final int TYPE_LOGOUT = 2;

	private LoginStatusChangeCallback callback;

	/**
	 * 网络状态改变回调接口
	 * @version ConnectionChangeReceiver
	 * @since JDK 1.6
	 */
	public interface LoginStatusChangeCallback {
		/** 登录,注销 */
		void loginChange(int type);
	}

	public void setOnLoginStatusListener(LoginStatusChangeCallback callback) {
		this.callback = callback;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction() == LOGIN_STATUS_ChANGE_ACTION) {
			//默认是登录
			callback.loginChange(intent.getIntExtra(LOGIN_STATUS_TYPE, TYPE_LOGIN));
		}
	}
}
