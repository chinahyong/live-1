/**
 * Project Name:feizao File Name:OnDoubleClickListener.java Package
 * Name:com.efeizao.feizao.listeners Date:2016-3-15下午3:29:43
 */

package tv.live.bx.listeners;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * ClassName:OnDoubleClickListener Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: 2016-3-15 下午3:29:43
 * @author Live
 * @version 1.0
 */
public class OnDoubleClickListener implements View.OnTouchListener {
	int count = 0;
	long firClick = 0, secClick = 0;

	public OnDoubleClickListener(OnClickListener l) {
		this.mOnClickListener = l;
	}

	public OnClickListener mOnClickListener;

	public void setOnDoubleClick(OnClickListener l) {
		this.mOnClickListener = l;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (MotionEvent.ACTION_DOWN == event.getAction()) {
			count++;
			if (count == 1) {
				firClick = SystemClock.uptimeMillis();

			} else if (count == 2) {
				secClick = SystemClock.uptimeMillis();
				if (secClick - firClick < 1000) {
					// 双击事件
					if (mOnClickListener != null) {
						mOnClickListener.onClick(v);
					}
				}
				count = 0;
				firClick = 0;
				secClick = 0;
			}
		}
		return true;
	}

}
