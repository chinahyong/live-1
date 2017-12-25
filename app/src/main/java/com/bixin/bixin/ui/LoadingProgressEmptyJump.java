package com.bixin.bixin.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import tv.live.bx.R;


@SuppressLint("NewApi")
public class LoadingProgressEmptyJump extends LoadingProgress {

	private int mEmptyLayoutId; // 显示的布局

	private LinearLayout mShowEmptyLayout; // 显示空布局

	@Override
	public void setShowImage(int imageId) {
		super.setShowImage(imageId);
		if (mShowEmptyLayout != null) {
			mShowEmptyLayout.setVisibility(View.VISIBLE);
		}
	}

	public LoadingProgressEmptyJump(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingProgress, 0, 0);
		mEmptyLayoutId = typedArray.getResourceId(R.styleable.LoadingProgress_LoadingEmptyLayout, 0);
		mShowImageId = typedArray.getResourceId(R.styleable.LoadingProgress_LoadingImage, 0);
		mLoadingProgressId = typedArray.getResourceId(R.styleable.LoadingProgress_LoadingProgress, 0);
		mLoadingInfoId = typedArray.getResourceId(R.styleable.LoadingProgress_LoadingInfo, 0);
		typedArray.recycle();
		typedArray = null;
		mLoadFailed = false;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (mEmptyLayoutId != 0) {
			mShowEmptyLayout = (LinearLayout) findViewById(mEmptyLayoutId);
			mShowEmptyLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void Start(String msg, int imageId) {
		super.Start(msg, imageId);
		if (mShowEmptyLayout != null) {
			if (imageId > 0) {
				mShowEmptyLayout.setVisibility(View.VISIBLE);
			} else {
				mShowEmptyLayout.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void Succeed(String msg, int imageId) {
		super.Succeed(msg, imageId);
		if (mShowEmptyLayout != null) {
			if (imageId > 0) {
				mShowEmptyLayout.setVisibility(View.VISIBLE);
			} else {
				mShowEmptyLayout.setVisibility(View.GONE);
			}
		}
	}

	@Override
	@SuppressLint("NewApi")
	public void Failed(String msg, int imageId) {
		super.Failed(msg, imageId);

		if (mShowEmptyLayout != null) {
			if (imageId > 0) {
				mShowEmptyLayout.setVisibility(View.VISIBLE);
			} else {
				mShowEmptyLayout.setVisibility(View.GONE);
			}
		}
	}
}
