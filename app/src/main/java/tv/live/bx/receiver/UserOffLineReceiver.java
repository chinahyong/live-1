/**
 * Project Name:feizao File Name:ConnectionChangeReceiver.java Package
 * Name:com.efeizao.feizao.receiver Date:2015-6-23下午6:37:38
 */

package tv.live.bx.receiver;

/**
 * 用户下线广播处理
 *
 * @version 1.0
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tv.live.bx.library.util.EvtLog;

public class UserOffLineReceiver extends BroadcastReceiver {

	public static final String ACTION_USER_OFFLINE = "action_user_offline";
	private ReceiverCallback callback;

	/**
	 * 广播回调接口
	 *
	 * @version ConnectionChangeReceiver
	 * @since JDK 1.6
	 */
	public interface ReceiverCallback {
		void callBack();
	}

	public void setOnReceiverCallbackListener(ReceiverCallback callback) {
		this.callback = callback;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		EvtLog.e("UserOffLineReceiver", "onReceive " + intent.getAction());
		if (intent.getAction() == ACTION_USER_OFFLINE) {
			callback.callBack();
		}
	}
}
