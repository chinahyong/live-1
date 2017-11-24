package tv.live.bx.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextSwitcher;
import android.widget.ViewSwitcher;

import com.efeizao.bx.R;
import tv.live.bx.library.util.EvtLog;

import java.util.ArrayList;

/**
 * Created by Live on 2017/2/13.
 */

public class VerticalTextview extends TextSwitcher implements ViewSwitcher.ViewFactory {
	private String TAG = VerticalTextview.class.getSimpleName();
	private static final int FLAG_START_AUTO_SCROLL = 0;
	private static final int FLAG_STOP_AUTO_SCROLL = 1;
	private ArrayList<Integer> showList = new ArrayList<>();        // 实际滚动数据组

	private Context mContext;
	// 描边默认白色
	private int strokeTextColor;
	// 字体默认黑色
	private int verticalTextColor;
	// 字体大小(px)
	private float mTextSize;
	// 字体style
	private int mTextStyle;

	// 所有文本从滑入到滑出，以及停留的时间
	private long mAnimDurationTotal = 3000;
	// 最终滚动结束，需要显示的数据
	private int num = 0;        //要显示的num
	private int currentNum = 0;    //当前的num
	private int currentId = -1;        //滚动下标
	private int scrollNum = 1;        //滚动数量
	private Handler handler;

	public VerticalTextview(Context context) {
		this(context, null);
		mContext = context;
	}

	public VerticalTextview(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VerticalTextview);
		verticalTextColor = a.getColor(R.styleable.VerticalTextview_verticalTextColor, 0XFFFFFFFF);
		strokeTextColor = a.getColor(R.styleable.VerticalTextview_verticalStrokeTextColor, 0XFFFFFFFF);
		mTextSize = a.getDimension(R.styleable.VerticalTextview_verticalTextSize, mTextSize);
		mTextStyle = a.getInteger(R.styleable.VerticalTextview_verticalTextStyle, mTextStyle);
		a.recycle();
	}

	public void setAnimTime(long animDuration) {
		if (getChildCount() < 2) {
			setFactory(this);
		}
		if (showList.size() > 0) {
			scrollNum = showList.size();
		}
		Animation in = new TranslateAnimation(0, 0, 100, 0);
		in.setDuration(mAnimDurationTotal / scrollNum);
		in.setInterpolator(new LinearInterpolator());
		Animation out = new TranslateAnimation(0, 0, 0, -100);
		out.setDuration(mAnimDurationTotal / scrollNum);
		out.setInterpolator(new LinearInterpolator());
		setInAnimation(in);
		setOutAnimation(out);
	}

	/**
	 * 间隔时间
	 */
	public void setTextStillTime() {
		if (handler != null) {
			return;
		}
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case FLAG_START_AUTO_SCROLL:
						if (showList.size() > 0) {
							currentId++;
							// 滚动一轮
							if (currentId >= showList.size()) {
								currentId = -1;        // 还原
								handler.removeMessages(FLAG_START_AUTO_SCROLL);
								return;
							}
							// 正常设置
							setText(String.valueOf(showList.get(currentId)));
							handler.sendEmptyMessageDelayed(FLAG_START_AUTO_SCROLL, mAnimDurationTotal / scrollNum);
							EvtLog.e(TAG, "currentId:" + currentId + "text:" + showList.get(currentId));
						}
						break;
					case FLAG_STOP_AUTO_SCROLL:
						showList.clear();
						currentId = -1;        // 还原
						handler.removeMessages(FLAG_START_AUTO_SCROLL);
						break;
				}
			}
		};
	}

	/**
	 * 开始滚动
	 */
	public void startAutoScroll() {
		if (handler != null) {
			handler.sendEmptyMessage(FLAG_START_AUTO_SCROLL);
		}
	}

	/**
	 * 停止滚动
	 */
	public void stopAutoScroll() {
		if (handler != null) {
			handler.sendEmptyMessage(FLAG_STOP_AUTO_SCROLL);
		}
	}

	@Override
	public View makeView() {
		StrokeGradientTextView t = new StrokeGradientTextView(mContext);
		t.setMaxLines(1);
//		t.setTextColor(verticalTextColor);
		t.setStrokeTextColor(strokeTextColor);
		t.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
		t.setTypeface(Typeface.defaultFromStyle(mTextStyle));
		return t;
	}

	// 设置最终的赋值
	public void setNum(int num) {
		showList.clear();
		currentId = -1;        // 还原
		EvtLog.e(TAG, "anim before num:" + num + "curnum:" + currentNum);
		//根据currentNum 与 num 算 得出showList
		if (num < currentNum) {
			int count = 11 - currentNum + num;
			// 第二次数据比之前数据小，叠加到最大，从0加到第二次数据
			for (int i = 0; i < count; i++) {
				showList.add(currentNum);
				currentNum = (++currentNum) % 10;
			}
		} else {
			for (int i = currentNum; i <= num; i++) {
				showList.add(i);
			}
		}
		EvtLog.e(TAG, "anim showlist:" + showList.size());
		//修改初始化
		currentNum = num;
	}

	// 设置所有文本滚动到停留的动画总时长
	public void setDurationTotal(long mAnimDurationTotal) {
		this.mAnimDurationTotal = mAnimDurationTotal;
		setAnimTime(mAnimDurationTotal);
	}

	public void setStrokeTextColor(int strokeTextColor) {
		this.strokeTextColor = strokeTextColor;
	}

	public void setVerticalTextColor(int verticalTextColor) {
		this.verticalTextColor = verticalTextColor;
	}

	public void setTextSize(int textSize) {
		this.mTextSize = textSize;
	}

	public void setTextStyle(int textStyle) {
		this.mTextStyle = textStyle;
	}
}
