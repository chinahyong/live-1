package com.bixin.bixin.library.util;

import android.content.Context;
import android.util.TypedValue;
import android.view.View.MeasureSpec;
import android.widget.TextView;

/**
 * Utility methods for Views.
 */
public class ViewUtils {

	public static int measureTextViewHeight(Context mContext, String text, float textSize, float showWidth) {
		TextView textView = new TextView(mContext);
		textView.setText(text);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) showWidth, MeasureSpec.AT_MOST);
		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		textView.measure(widthMeasureSpec, heightMeasureSpec);
		return textView.getMeasuredHeight();
	}

	public static int measureTextViewHeight(Context mContext, String text, float textSize, float showWidth, int row) {
		TextView textView = new TextView(mContext);
		textView.setText(text);
		textView.setMaxLines(row);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) showWidth, MeasureSpec.AT_MOST);
		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		textView.measure(widthMeasureSpec, heightMeasureSpec);
		return textView.getMeasuredHeight();
	}
}
