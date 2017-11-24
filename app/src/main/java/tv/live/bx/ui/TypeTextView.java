package tv.live.bx.ui;

/**
 * Created by Administrator on 2016/11/3.
 */

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 模拟打字机效果
 */
public class TypeTextView extends TextView {
	private Context mContext = null;
	private CharSequence mShowTextString = null;
	private OnTypeViewListener mOnTypeViewListener = null;
	public static final int TYPE_TIME_DELAY = 80;
	private int mTypeTimeDelay = TYPE_TIME_DELAY; // 打字间隔
	private int mStartTimeDelay = TYPE_TIME_DELAY; // 延时打字
	private android.os.Handler mHandler = new Handler();
	private Runnable mTaskRunnable;

	public TypeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initTypeTextView(context);
	}

	public TypeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initTypeTextView(context);
	}

	public TypeTextView(Context context) {
		super(context);
		initTypeTextView(context);
	}

	public void setOnTypeViewListener(OnTypeViewListener onTypeViewListener) {
		mOnTypeViewListener = onTypeViewListener;
	}

	public void start(final CharSequence textString) {
		start(textString, TYPE_TIME_DELAY);
	}

	public void start(final CharSequence textString, final int typeTimeDelay) {
		start(textString, TYPE_TIME_DELAY, TYPE_TIME_DELAY);
	}

	public void start(final CharSequence textString, final int typeTimeDelay, final int startTimeDelay) {
		if (TextUtils.isEmpty(textString) || typeTimeDelay < 0 || startTimeDelay < 0) {
			return;
		}
		mShowTextString = textString;
		mTypeTimeDelay = typeTimeDelay;
		mStartTimeDelay = startTimeDelay;
		setText("");
		startTypeTimer();
		if (null != mOnTypeViewListener) {
			mOnTypeViewListener.onTypeStart();
		}
	}

	public void stop() {
		stopTypeTimer();
	}

	private void initTypeTextView(Context context) {
		mContext = context;
	}

	private void startTypeTimer() {
		if (mTaskRunnable == null) {
			mTaskRunnable = new TypeTimerTask();
		}
		mHandler.postDelayed(mTaskRunnable, mStartTimeDelay);
	}

	private void stopTypeTimer() {
		mHandler.removeCallbacksAndMessages(null);
	}


	class TypeTimerTask implements Runnable {
		@Override
		public void run() {
			if (getText().length() < mShowTextString.length()) {
				setText(mShowTextString.subSequence(0, getText().length() + 1));
				mHandler.postDelayed(mTaskRunnable, mTypeTimeDelay);
			} else {
				stopTypeTimer();
				if (null != mOnTypeViewListener) {
					mOnTypeViewListener.onTypeOver();
				}
			}

		}
	}

	public interface OnTypeViewListener {
		void onTypeStart();

		void onTypeOver();
	}
}
