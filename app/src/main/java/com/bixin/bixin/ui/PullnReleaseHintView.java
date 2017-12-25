package com.bixin.bixin.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import tv.live.bx.R;


/**
 * 用作PullnReleaseContainer的提示控件.
 * 必须包含两个子控件:
 * 1.拉动提示控件.体现用户的拉动过程,提醒用户完成相应动作以触发任务
 * 2.进度控件.任务开始后显示进度条或等待提示
 * @author fangyuehan
 *
 */
public abstract class PullnReleaseHintView extends FrameLayout{

	protected View mPullHintView;
	protected View mWaitHintView;
	
	private int mPullHintViewId;
	private int mWaitHintViewId;
	
	private int mViewShowing;
	private static final int SHOWING_VIEW_UNKNOWN = -1;
	private static final int SHOWING_PULL_HINT = 0;
	private static final int SHOWING_WAIT_HINT = 1;
	
	public PullnReleaseHintView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PullnReleaseHintView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PullnReleaseHintView, defStyle, 0);
		mPullHintViewId = typedArray.getResourceId(R.styleable.PullnReleaseHintView_pull_hint_view, 0);
		
		if (0 == mPullHintViewId) {
			throw new IllegalArgumentException("pull_hint_view must refer to an existing child");
		}
		mWaitHintViewId = typedArray.getResourceId(R.styleable.PullnReleaseHintView_wait_hint_view, 0);
		if (0 == mWaitHintViewId) {
			throw new IllegalArgumentException("wait_hint_view must refer to an existing child");
		}
		typedArray.recycle();
		
		mViewShowing = SHOWING_VIEW_UNKNOWN;
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		mPullHintView = findViewById(mPullHintViewId);
		mWaitHintView = findViewById(mWaitHintViewId);
		
		showPullHintView();
	}
	
	public void showWaitHintView(){
//		if (mViewShowing == SHOWING_WAIT_HINT) {
//			return;
//		}
		if (null != mPullHintView) {
			mPullHintView.setVisibility(INVISIBLE);
		}
		if (null != mWaitHintView) {
			mWaitHintView.setVisibility(VISIBLE);
		}
//		mViewShowing = SHOWING_WAIT_HINT;
	}
	
	public void showPullHintView() {
//		if (mViewShowing == SHOWING_PULL_HINT) {
//			return;
//		}
		if (null != mPullHintView) {
			mPullHintView.setVisibility(VISIBLE);
		}
		if (null != mWaitHintView) {
			mWaitHintView.setVisibility(INVISIBLE);
		}
//		mViewShowing = SHOWING_PULL_HINT;
	}

	public abstract void updateHintView(int maxOffset, int curOffset);
	
}
	