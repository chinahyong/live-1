package com.bixin.bixin.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;

import tv.live.bx.R;


/**
 * 加载更多数据View，用于ListView底部加载更多数据用
 *
 * @author fangyuehan 20140731
 */
public class ListFooterLoadView extends FrameLayout {

    public static final int STATUS_HIDDEN = 0;
    public static final int STATUS_LOADING = 1;
    public static final int STATUS_FAILED = 2;
    public static final int STATUS_NOMORE = 3;
    private View mLoadingView;
    private View mLoadingHint;
    private View mNoMoreHint;
    private View mFailedHint;
    // private volatile boolean mLoading = false;
    private volatile int mStatus = STATUS_HIDDEN;

    private RotateAnimation mAnimation;

    public ListFooterLoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLoadingView = findViewById(R.id.list_footer_view);
        mLoadingHint = findViewById(R.id.list_footer_loading);
        mNoMoreHint = findViewById(R.id.list_footer_no_more);
        mFailedHint = findViewById(R.id.list_footer_failed);

        if (mAnimation != null) {
            mAnimation.cancel();
        }
        mAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mAnimation.setDuration(1200);
        mAnimation.setInterpolator(new LinearInterpolator());
        mAnimation.setRepeatCount(-1);
        mAnimation.setStartOffset(0);

        mLoadingView.setVisibility(VISIBLE);
    }

    public void onLoadingStarted() {
    	Log.e("", "onLoadingStarted");
        mStatus = STATUS_LOADING;
        mLoadingView.setVisibility(VISIBLE);
        mLoadingHint.setVisibility(VISIBLE);
        if (mAnimation != null) {
            mAnimation.reset();
            mLoadingHint.findViewById(R.id.list_footer_loading_img).startAnimation(mAnimation);
        }
        mNoMoreHint.setVisibility(INVISIBLE);
        mFailedHint.setVisibility(INVISIBLE);
    }

    public void onNoMoreData() {
        mStatus = STATUS_NOMORE;
        mLoadingView.setVisibility(VISIBLE);
        mLoadingHint.setVisibility(INVISIBLE);
        mLoadingHint.findViewById(R.id.list_footer_loading_img).clearAnimation();
        mNoMoreHint.setVisibility(VISIBLE);
        mFailedHint.setVisibility(INVISIBLE);
    }

    public void onLoadingFailed() {
        mStatus = STATUS_FAILED;
        mLoadingView.setVisibility(VISIBLE);
        mLoadingHint.setVisibility(INVISIBLE);
        mLoadingHint.findViewById(R.id.list_footer_loading_img).clearAnimation();
        mNoMoreHint.setVisibility(INVISIBLE);
        mFailedHint.setVisibility(VISIBLE);
    }

    public boolean canLoadMore(){
        return  mStatus == STATUS_HIDDEN || mStatus == STATUS_FAILED;
    }

    /**
     * @return
     * @deprecated
     */
    public boolean isLoading() {
        return mStatus == STATUS_LOADING;
    }

    public int getStatus() {
        return mStatus;
    }

    public void hide() {
        mStatus = STATUS_HIDDEN;
        mLoadingView.setVisibility(VISIBLE);
        mLoadingHint.setVisibility(INVISIBLE);
        mNoMoreHint.setVisibility(INVISIBLE);
        mFailedHint.setVisibility(INVISIBLE);
    }

    public void gone() {
        mLoadingView.setVisibility(GONE);
    }

    public void visible() {
        mLoadingView.setVisibility(VISIBLE);
    }

}