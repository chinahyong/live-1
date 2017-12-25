package com.bixin.bixin.ui.cropimage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * 头像图片选择框的浮层
 */
public class FloatDrawable extends Drawable {

	private Context mContext;
	private Drawable mCropPointDrawable;
	
	private Paint mLinePaint = new Paint();
	{
		mLinePaint.setARGB(200, 50, 50, 50);
		mLinePaint.setStrokeWidth(1F);
		mLinePaint.setStyle(Paint.Style.STROKE);
		mLinePaint.setAntiAlias(true);
		mLinePaint.setColor(Color.WHITE);
	}

	public FloatDrawable(Context context) {
		super();
		this.mContext = context;
		init();
	}

	private void init() {
		// mCropPointDrawable = mContext.getResources().getDrawable(R.drawable.clip_point);
	}

	public int getCirleWidth() {
		return mCropPointDrawable.getIntrinsicWidth();
	}

	public int getCirleHeight() {
		return mCropPointDrawable.getIntrinsicHeight();
	}

	@Override
	public void draw(Canvas canvas) {

		int left = getBounds().left;
		int top = getBounds().top;
		int right = getBounds().right;
		int bottom = getBounds().bottom;
		
		int pointWidth = mCropPointDrawable == null ? 0 : mCropPointDrawable
				.getIntrinsicWidth() / 2;
		int pointHeight = mCropPointDrawable == null ? 0 : mCropPointDrawable
				.getIntrinsicHeight() / 2;

		Rect mRect = new Rect(left + pointWidth, top + pointHeight, right
				- pointWidth, bottom - pointHeight);
		// 方框
		canvas.drawRect(mRect, mLinePaint);
		
		if(mCropPointDrawable != null) {
			// 左上
			mCropPointDrawable.setBounds(left, top,
					left + mCropPointDrawable.getIntrinsicWidth(), top
							+ mCropPointDrawable.getIntrinsicHeight());
			mCropPointDrawable.draw(canvas);

			// 右上
			mCropPointDrawable.setBounds(
					right - mCropPointDrawable.getIntrinsicWidth(), top, right, top
							+ mCropPointDrawable.getIntrinsicHeight());
			mCropPointDrawable.draw(canvas);

			// 左下
			mCropPointDrawable.setBounds(left,
					bottom - mCropPointDrawable.getIntrinsicHeight(), left
							+ mCropPointDrawable.getIntrinsicWidth(), bottom);
			mCropPointDrawable.draw(canvas);

			// 右下
			mCropPointDrawable.setBounds(
					right - mCropPointDrawable.getIntrinsicWidth(), bottom
							- mCropPointDrawable.getIntrinsicHeight(), right,
					bottom);
			mCropPointDrawable.draw(canvas);
		}

	}

	@Override
	public void setBounds(Rect bounds) {
		int pointWidth = mCropPointDrawable == null ? 0 : mCropPointDrawable
				.getIntrinsicWidth() / 2;
		int pointHeight = mCropPointDrawable == null ? 0 : mCropPointDrawable
				.getIntrinsicHeight() / 2;
		super.setBounds(new Rect(bounds.left - pointWidth, bounds.top
				- pointHeight, bounds.right + pointWidth, bounds.bottom
				+ pointHeight));
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}

}
