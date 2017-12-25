package com.bixin.bixin.ui.addpopup;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import tv.live.bx.R;


@SuppressLint("NewApi")
public class AddMoreWindow extends PopupWindow implements OnClickListener {

	private String TAG = AddMoreWindow.class.getSimpleName();
	Activity mContext;
	private int mWidth;
	private int mHeight;
	private int statusBarHeight;
	private View mRootView;

	private OnClickListener mOnClickListener;

	public static String GUIDE_MORE = "guide_more";

	private Handler mHandler = new Handler();

	public AddMoreWindow(Activity context) {
		mContext = context;
		//导航栏  底部  防止被虚拟按键遮住
		mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
	}

	public void init() {
		Rect frame = new Rect();
		mContext.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		statusBarHeight = frame.top;
		DisplayMetrics metrics = new DisplayMetrics();
		mContext.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mWidth = metrics.widthPixels;
		mHeight = metrics.heightPixels;

		setWidth(mWidth);
		setHeight(mHeight);
	}

	public void setOnClickListener(OnClickListener mOnClickListener) {
		this.mOnClickListener = mOnClickListener;
	}

	public void showMoreWindow(View anchor, int bottomMargin) {
		mRootView = LayoutInflater.from(mContext).inflate(R.layout.pop_add_more_window, null);
		ImageView moreLive = (ImageView) mRootView.findViewById(R.id.more_live);
		ImageView moreRecordLive = (ImageView) mRootView.findViewById(R.id.more_record_live);
		RelativeLayout moreClose = (RelativeLayout) mRootView.findViewById(R.id.more_layout_close);
		setContentView(mRootView);
		moreLive.setOnClickListener(this);
		moreRecordLive.setOnClickListener(this);
		moreClose.setOnClickListener(this);
//		showAnimation((ViewGroup) mRootView);
		this.setFocusable(true);
		this.setTouchable(true);
		this.setOutsideTouchable(true);
		setAnimationStyle(R.style.popwindow_live_anim_style);
		showAtLocation(anchor, Gravity.BOTTOM, 0, statusBarHeight);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.more_live:
				closeAnimation((ViewGroup) mRootView);
				if (mOnClickListener != null) {
					mOnClickListener.onClick(v);
				}
				break;
			case R.id.more_record_live:
				closeAnimation((ViewGroup) mRootView);
				if (mOnClickListener != null) {
					mOnClickListener.onClick(v);
				}
				break;
			case R.id.more_layout_close:
				closeAnimation((ViewGroup) mRootView);
				break;
		}
	}

	private void showAnimation(final ViewGroup layout) {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				layout.setVisibility(View.VISIBLE);
				ValueAnimator fadeAnim = ObjectAnimator.ofFloat(layout, "translationY", layout.getHeight(), 0);
				fadeAnim.setDuration(300);
				fadeAnim.start();
			}
		}, 50);

	}

	public void closeAnimation(final ViewGroup layout) {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				layout.setVisibility(View.GONE);
				ValueAnimator fadeAnim = ObjectAnimator.ofFloat(layout, "translationY", 0, layout.getHeight());
				fadeAnim.setDuration(300);
				fadeAnim.start();
			}
		}, 50);
		dismiss();
	}
}
