/**
 * Project Name:feizao File Name:ConnectionChangeReceiver.java Package
 * Name:com.efeizao.feizao.receiver Date:2015-6-23下午6:37:38
 */

package tv.live.bx.receiver;

/**
 * 用户登录状态改变 ADD REASON. Date: 2015-6-23 下午6:37:38
 * @version 1.0
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MessageReceiver extends BroadcastReceiver {

	public static final String ACTION_MESSAGE_INFO = "action_message_info";
	public static final String MESSAGE_TYPE = "message_type";
	private MessageCallback callback;

	/**
	 * 网络状态改变回调接口
	 * @version ConnectionChangeReceiver
	 * @since JDK 1.6
	 */
	public interface MessageCallback {
		/** 登录,注销 */
		void callBack(String type);
	}

	public void setOnMessageListener(MessageCallback callback) {
		this.callback = callback;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction() == ACTION_MESSAGE_INFO) {
			callback.callBack(intent.getStringExtra(MESSAGE_TYPE));
		}
	}
}
