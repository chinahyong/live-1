/**
 * Name:com.efeizao.feizao.ui Date:2015-12-10上午11:24:32
 */

package tv.live.bx.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;


/**
 * 直播间聊天消息Listview
 * Date: 2015-12-10 上午11:24:32
 *
 * @author Live
 * @version 1.0
 */
public class ChatListView extends ListView {

	public ChatListView(Context context) {
		super(context);
	}

	public ChatListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ChatListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private OnDispatchTouchListener mOnDispatchTouchEvent;

	public void setOnDispatchTouchListener(OnDispatchTouchListener listener) {
		this.mOnDispatchTouchEvent = listener;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mOnDispatchTouchEvent != null)
			mOnDispatchTouchEvent.onTouch(this, ev);
		return super.dispatchTouchEvent(ev);
	}

	public interface OnDispatchTouchListener {
		void onTouch(View view, MotionEvent ev);
	}
}
