package tv.live.bx.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.common.Utils;
import tv.live.bx.library.util.EvtLog;

import java.util.LinkedList;
import java.util.List;

/**
 * 左右滑屏，让子view可以左右滑动；
 *
 * @author update Live
 * @blog http://blog.csdn.net/xiaanming
 */
public class SwipeBackLayout extends FrameLayout implements GestureDetector.OnGestureListener {
	private static final String TAG = SwipeBackLayout.class.getSimpleName();
	private View mContentView;
	private int mTouchSlop;
	private int downX;
	private int downY;
	private int tempX;
	private Scroller mScroller;
	private int viewWidth;
	private boolean isSilding;
	private boolean mScrollRight;
	// private Drawable mShadowDrawable;
	private Activity mActivity;
	private List<ViewPager> mViewPagers = new LinkedList<>();
	private List<HorizontalListView> mHorizontalListViews = new LinkedList<>();
	/**
	 * 是否能滑动
	 */
	private boolean isScrollEnable = true;
	private int verticalMinDistance = 20;
	private int minVelocity = 10;
	private GestureDetector mGestureDetector;

	public SwipeBackLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mScroller = new Scroller(context);
		mGestureDetector = new GestureDetector(context, this);
		// mShadowDrawable = getResources().getDrawable(R.drawable.shadow_left);
	}

	public void setScrollEnable(boolean isScrollEnable) {
		this.isScrollEnable = isScrollEnable;
	}

	// public void attachToActivity(Activity activity) {
	// mActivity = activity;
	// TypedArray a = activity.getTheme().obtainStyledAttributes(new int[] {
	// android.R.attr.windowBackground });
	// int background = a.getResourceId(0, 0);
	// a.recycle();
	//
	// ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
	// ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
	// decorChild.setBackgroundResource(background);
	// decor.removeView(decorChild);
	// addView(decorChild);
	// setContentView(decorChild);
	// decor.addView(this);
	// }

	/**
	 * @category update
	 */
	public void attachToActivity(Activity activity) {
		mActivity = activity;
		TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowBackground});
		int background = a.getResourceId(0, 0);
		a.recycle();

		ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
		ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
		decorChild.setBackgroundResource(background);
		// decor.removeView(decorChild);
		// addView(decorChild);
		setContentView(this);
		// decor.addView(this);
	}

	private void setContentView(View decorChild) {
		mContentView = decorChild;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 事件拦截操作
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// 处理ViewPager冲突问题
		EvtLog.i(TAG, "mViewPager = " + ev.getX() + "," + ev.getY());

		if (getTouchViewPager(ev)) {
			return super.onInterceptTouchEvent(ev);
		}
		// 如果不在触摸区域就不拦截
//		if (!getTouchRect(ev)) {
//			return super.onInterceptTouchEvent(ev);
//		}

		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				downX = tempX = (int) ev.getRawX();
				downY = (int) ev.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				int moveX = (int) ev.getRawX();
				// 满足此条件屏蔽SildingFinishLayout里面子类的touch事件
				if (Math.abs(moveX - downX) > mTouchSlop && Math.abs(moveX - downX) > Math.abs((int) ev.getRawY() - downY)) {
					return true;
				}
				break;
		}

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				int moveX = (int) event.getRawX();
				int deltaX = tempX - moveX;
				tempX = moveX;
				if (isScrollEnable && moveX - downX > mTouchSlop && !mScrollRight
						&& Math.abs((int) event.getRawY() - downY) < mTouchSlop) {
					isSilding = true;
				}
				if (isScrollEnable && downX - moveX > mTouchSlop && mScrollRight
						&& Math.abs((int) event.getRawY() - downY) < mTouchSlop) {
					isSilding = true;
				}
				// if (Math.abs((int) moveX - downX) > mTouchSlop && Math.abs((int)
				// event.getRawY() - downY) < mTouchSlop) {
				// isSilding = true;
				// }

				if (isSilding) {
					mContentView.scrollBy(deltaX, 0);
				}
				break;
			case MotionEvent.ACTION_UP:
				isSilding = false;
				if (mContentView.getScrollX() <= -viewWidth / 2) {
					scrollRight();
				} else {
					scrollOrigin();
				}
				break;
		}
		mGestureDetector.onTouchEvent(event);
		return true;
	}

	/**
	 * 获取SwipeBackLayout里面的ViewPager的集合
	 *
	 * @param mViewPagers
	 * @param parent
	 */
	private void getAlLViewPager(List<ViewPager> mViewPagers, ViewGroup parent) {
		int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = parent.getChildAt(i);
			if (child instanceof ViewPager) {
				mViewPagers.add((ViewPager) child);
			} else if (child instanceof ViewGroup) {
				getAlLViewPager(mViewPagers, (ViewGroup) child);
			}
		}
	}

	private void getAllHorizontalListView(List<HorizontalListView> views, ViewGroup parent) {
		int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = parent.getChildAt(i);
			if (child instanceof HorizontalListView) {
				views.add((HorizontalListView) child);
			} else if (child instanceof ViewGroup) {
				getAllHorizontalListView(views, (ViewGroup) child);
			}
		}
	}

	/**
	 * 返回我们touch的ViewPager
	 *
	 * @param ev
	 * @return
	 */
	private boolean getTouchViewPager(MotionEvent ev) {
		Rect mRect = new Rect();
		for (ViewPager v : mViewPagers) {
			if (v.getVisibility() != View.VISIBLE) {
				break;
			}
			final int[] locations = new int[2];
			v.getLocationOnScreen(locations);
			mRect.left = locations[0];
			mRect.top = locations[1];
			mRect.right = mRect.left + v.getRight() - v.getLeft();
			mRect.bottom = mRect.top + v.getBottom() - v.getTop();
			// v.getHitRect(mRect);
			// Log.i(TAG, "mViewPager = " + v);
			// Log.i(TAG, "getHitRect = " + mRect.toShortString() +
			// "  ev.getX()[" + ev.getX() + "," + ev.getY());
			if (mRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
				return true;
			}
		}
		for (HorizontalListView v : mHorizontalListViews) {
			if (v.getVisibility() != View.VISIBLE) {
				break;
			}
			final int[] locations = new int[2];
			v.getLocationOnScreen(locations);
			mRect.left = locations[0];
			mRect.top = locations[1];
			mRect.right = mRect.left + v.getRight() - v.getLeft();
			mRect.bottom = mRect.top + v.getBottom() - v.getTop();
			// v.getHitRect(mRect);
			// Log.i(TAG, "mViewPager = " + v);
			// Log.i(TAG, "getHitRect = " + mRect.toShortString() +
			// "  ev.getX()[" + ev.getX() + "," + ev.getY());
			if (mRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 触摸区域
	 */
	private boolean getTouchRect(MotionEvent ev) {
		Rect mRect = new Rect();
		mRect.left = 0;
		mRect.top = 0;
		mRect.right = FeizaoApp.metrics.widthPixels;
		mRect.bottom = (int) (FeizaoApp.metrics.heightPixels - Utils.dip2px(getContext(), 233f) - getContext()
				.getResources().getDimension(R.dimen.a_button_gift_height_bg_135));
		// Log.i(TAG, "getHitRect = " + mRect.toShortString() + "  ev.getX()[" +
		// ev.getX() + "," + ev.getY());
		return mRect.contains((int) ev.getX(), (int) ev.getY());
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			viewWidth = this.getWidth();

			getAlLViewPager(mViewPagers, this);
			getAllHorizontalListView(mHorizontalListViews, this);
			Log.i(TAG, "ViewPager size = " + mViewPagers.size());
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		// if (mShadowDrawable != null && mContentView != null) {
		//
		// int left = mContentView.getLeft() -
		// mShadowDrawable.getIntrinsicWidth();
		// int right = left + mShadowDrawable.getIntrinsicWidth();
		// int top = mContentView.getTop();
		// int bottom = mContentView.getBottom();
		//
		// mShadowDrawable.setBounds(left, top, right, bottom);
		// mShadowDrawable.draw(canvas);
		// }

	}

	/**
	 * 滚动出界面
	 */
	private void scrollRight() {
		final int delta = (viewWidth + mContentView.getScrollX());
		// 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
		mScroller.startScroll(mContentView.getScrollX(), 0, -delta + 1, 0, Math.abs(delta));
		postInvalidate();
		mScrollRight = true;
	}

	/**
	 * 滚动到起始位置
	 */
	private void scrollOrigin() {
		int delta = mContentView.getScrollX();
		mScroller.startScroll(mContentView.getScrollX(), 0, -delta, 0, Math.abs(delta));
		postInvalidate();
		mScrollRight = false;
	}

	@Override
	public void computeScroll() {
		// 调用startScroll的时候scroller.computeScrollOffset()返回true，
		if (mScroller.computeScrollOffset()) {
			mContentView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();

			// if (mScroller.isFinished() && mScrollRight) {
			// mActivity.finish();
			// }
		}
	}

	/*
	 * 在onTouch()方法中，我们调用GestureDetector的onTouchEvent()方法，将捕捉到的MotionEvent交给GestureDetector
     * 来分析是否有合适的callback函数来处理用户的手势
     */
	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		EvtLog.e(TAG, "onShowPress ");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//		if (distanceX < 0 && !mScrollRight)
//			mContentView.scrollBy((int) distanceX, 0);
//		else if (distanceX > 0 && mScrollRight)
//			mContentView.scrollBy((int) distanceX, 0);
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		EvtLog.e(TAG, "onLongPress " + e);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		EvtLog.e(TAG, "onFling " + velocityX + "mScrollRight:" + mScrollRight);
		if (!isScrollEnable) return true;
		if (velocityX > 0 && Math.abs(velocityX) > minVelocity && !mScrollRight) {
			scrollRight();
		} else if (velocityX < 0 && Math.abs(velocityX) > minVelocity && mScrollRight) {
			scrollOrigin();
		}
		return true;
	}
}
