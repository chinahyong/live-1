/**
 * Project Name:feizao File Name:WrapListView.java Package
 * Name:com.efeizao.feizao.ui Date:2015-12-10上午11:24:32
 */

package com.bixin.bixin.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * ClassName:WrapListView Function: TODO ADD FUNCTION. Reason: TODO ADD REASON.
 * Date: 2015-12-10 上午11:24:32
 * @author Live
 * @version 1.0
 */
public class WrapListView extends ListView {

	public WrapListView(Context context) {
		super(context);
	}

	public WrapListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WrapListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

}
