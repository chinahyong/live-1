package com.bixin.bixin.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.WrapperListAdapter;

import java.lang.reflect.Field;

import tv.live.bx.R;
import com.bixin.bixin.library.util.EvtLog;

/**
 * 下拉刷新控件
 */
public class PullRefreshListView extends ListView {

	public static final String TAG = PullRefreshListView.class.getSimpleName();

	enum Status {
		NORMAL, PULLING_DOWN, PULLING_UP, RUNNING_TASK, RETURNING
	}

	private static final int MAX_PULL_DOWN_HEIGHT = 300;
	private static final int MAX_PULL_UP_HEIGHT = 300;

	private LinearLayout mTopHeader; // 占位项,使列表可以从大于0的位置开始显示
	private LinearLayout mBottomFooter; // 占位项,使列表可以显示到底部小于高度的位置

	private LinearLayout mStretchHeader; // 拉伸项,用于实现下拉刷新
	private LinearLayout mStretchBody; // 拉伸体,拉伸时真正大小改变的View,是mStretchHeader的子控件

	private View mPullnReleaseHintView; // 提示下拉或释放的控件,是mStretchBody的子控件
	private int mHintViewHeight; // 提示控件的真实高度,下拉超过此高度可以释放刷新,刷新过程中停留在次高度

	private volatile Status mStatus; // 内部状态

	/***/
	private int mTouchSlop;
	private float mLastY = -1; // 上次触摸事件的y位置
	private float mDownX;
	private float mDownY;

	private int mMotionEventProcStatus;
	private static final int PROC_UNKNOWN = -1;
	private static final int PROC_HORIZONTAL = 0;
	private static final int PROC_VERTICAL = 1;

	private float mScrollBarScaleFactor; // 在绘制scrollbar时对画布进行缩放的因数

	private int mTopHeaderHeight;

	private int mBottomFooterHeight;

	private Runnable mTask;

	private boolean mHeaderAdded;
	private boolean mFooterAdded;


	public PullRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();

		// 如果不设置该方法，ListView在Scroll时，子View会生成DrawingCache，这些图片必须手动调用setChildrenDrawingCacheEnabled(boolean)方法释放
		// 默认是true且没有释放，会加大内存的占用，详情见具体是实现
		setScrollingCacheEnabled(false);
	}

	private void init() {
		mTopHeader = new LinearLayout(getContext()) {
			@Override
			protected void onLayout(boolean changed, int l, int t, int r, int b) {
//				EvtLog.d(TAG, "top = "+t);
				super.onLayout(changed, l, t, r, b);
			}
		};
		mTopHeader.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				mTopHeaderHeight));
		mBottomFooter = new LinearLayout(getContext());
		mBottomFooter.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, mBottomFooterHeight));

		mStretchHeader = new LinearLayout(getContext());
		mStretchHeader.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		mStretchHeader.setBackgroundColor(Color.TRANSPARENT);

		mStretchBody = new LinearLayout(getContext());
		mStretchBody.setGravity(Gravity.BOTTOM);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, MAX_PULL_DOWN_HEIGHT);
		lp.topMargin = -MAX_PULL_DOWN_HEIGHT;
		mStretchBody.setLayoutParams(lp);
		mStretchBody.setBackgroundColor(Color.TRANSPARENT);
		mStretchHeader.addView(mStretchBody);

		mStatus = Status.NORMAL;

		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

		tryRemoveBottomGlow();
		tryRemoveTopGlow();
	}

	private void tryRemoveBottomGlow() {
		if (android.os.Build.VERSION.SDK_INT >= 9) {
			removeOverScrollEffect("mEdgeGlowBottom");
		}
	}

	private void tryRemoveTopGlow() {
		if (android.os.Build.VERSION.SDK_INT >= 9) {
			removeOverScrollEffect("mEdgeGlowTop");
		}
	}

	private void removeOverScrollEffect(String field) {
		try {
			Field mEdgeGlowTop = AbsListView.class.getDeclaredField(field);
			mEdgeGlowTop.setAccessible(true);
			Object oEdgeGlowTop = mEdgeGlowTop.get(this);

			Field mEdge = mEdgeGlowTop.getType().getDeclaredField("mEdge");
			mEdge.setAccessible(true);
			Drawable edgeDrawable = getResources().getDrawable(
					R.drawable.a_common_overscroll_edge);
			mEdge.set(oEdgeGlowTop, edgeDrawable);

			Field mGlow = mEdgeGlowTop.getType().getDeclaredField("mGlow");
			mGlow.setAccessible(true);
			Drawable glowDrawable = getResources().getDrawable(
					R.drawable.a_common_overscroll_glow);
			mGlow.set(oEdgeGlowTop, glowDrawable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void setTopHeadHeight(int topHeadHeight) {
		mTopHeaderHeight = topHeadHeight;
		ViewGroup.LayoutParams lp = mTopHeader.getLayoutParams();
		if (lp != null) {
			lp.height = mTopHeaderHeight;
			mTopHeader.requestLayout();
		}
		updateScrollbarScaleFactor();
	}

	public void setBottomFooterHeight(int bottomFooterHeight) {
		mBottomFooterHeight = bottomFooterHeight;
		ViewGroup.LayoutParams lp = mBottomFooter.getLayoutParams();
		if (lp != null) {
			lp.height = mBottomFooterHeight;
			mBottomFooter.requestLayout();
		}
	}

	public void setPullnReleaseHintView(View pullnReleaseHintView) {
		mPullnReleaseHintView = pullnReleaseHintView;
		if (pullnReleaseHintView != null) {
			if (pullnReleaseHintView.getLayoutParams() == null) {
				pullnReleaseHintView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			}
			mStretchBody.addView(pullnReleaseHintView);
			if (pullnReleaseHintView instanceof PullnReleaseHintView) {
				((PullnReleaseHintView) pullnReleaseHintView).showPullHintView();
			}
		}
	}

	public void setHeaderBackgroudColor(int color) {
		mTopHeader.setBackgroundColor(color);
		mStretchBody.setBackgroundColor(color);
	}

	public void setPullnReleaseHintView(int resId) {
		View pullnReleaseHintView = LayoutInflater.from(getContext()).inflate(resId, null);
		setPullnReleaseHintView(pullnReleaseHintView);
	}

	private void updateScrollbarScaleFactor() {
		int height = getHeight();
		mScrollBarScaleFactor = (float) (height - mTopHeaderHeight) / height;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		EvtLog.d(TAG, "onLayout");
		super.onLayout(changed, l, t, r, b);
		mHintViewHeight = mPullnReleaseHintView == null ? 0 : mPullnReleaseHintView.getHeight();
		updateScrollbarScaleFactor();
	}


	private void ensureHeader() {
		if (!mHeaderAdded) {
			super.addHeaderView(mTopHeader, null, true);
			super.addHeaderView(mStretchHeader, null, true);
			mHeaderAdded = true;
		}
	}

	private void addBottomFooter() {
		if (!mFooterAdded) {
			addFooterView(mBottomFooter);
			mFooterAdded = true;
		}
	}

	@Override
	public void addHeaderView(View v, Object data, boolean isSelectable) {
		ensureHeader();
		super.addHeaderView(v, data, isSelectable);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		ensureHeader();
		addBottomFooter();
		super.setAdapter(adapter);
	}

	private int getItemCount() {
		ListAdapter adapter = getAdapter();
		return adapter == null ? 0 : adapter.getCount();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final float y = ev.getY();
		final float x = ev.getX();
//		EvtLog.d(TAG, "dispatch touch event action = "+action + " x = " + x + " y = "+y);
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mDownX = x;
				mDownY = y;
				mLastY = y;
//			mHorizontalViewProc = false;
				mMotionEventProcStatus = PROC_UNKNOWN;
				break;
			case MotionEvent.ACTION_MOVE:
				if (mMotionEventProcStatus == PROC_HORIZONTAL) {
					ev.setLocation(x, mDownY);    // 交由横向View处理
				} else if (mMotionEventProcStatus == PROC_UNKNOWN) {
					if (mStatus == Status.NORMAL) {
						float deltaX = x - mDownX;
						float deltaY = y - mDownY;

						// 若横向先动,记录是否可处理,若横向开始处理,则后续事件交由横向view处理
						if (Math.abs(deltaX) > mTouchSlop || Math.abs(deltaY) > mTouchSlop) {
							boolean handled = super.dispatchTouchEvent(ev);
							if (handled) {
								if (Math.abs(deltaY) < Math.abs(deltaX)) {
									mMotionEventProcStatus = PROC_HORIZONTAL;
								} else if (Math.abs(deltaY) > Math.abs(deltaX)) {
									mMotionEventProcStatus = PROC_VERTICAL;
								}
							}
							return handled;
						}
					}
				}
				break;

			default:
				break;
		}

//		EvtLog.d(TAG, "dispatch touch event action = "+action + " x = " + x + " y = "+y);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
//		EvtLog.d(TAG, "onTouch "+ ev.getAction());
		final int action = ev.getAction();
		final float y = ev.getY();
		final float deltaY = mLastY == -1 ? 0 : y - mLastY;
		float oldLastY = mLastY;
		mLastY = y;
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (mStatus == Status.RETURNING) {
					return true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				switch (mStatus) {
					case NORMAL:
						if (getFirstVisiblePosition() == 0 && mTopHeader.getTop() >= 0) {
//					EvtLog.d(TAG, "delta y == "+deltaY);
							if (deltaY > mTouchSlop) {
								mStatus = Status.PULLING_DOWN;
								if (mCallbacks != null) {
									mCallbacks.onStartPullDown();
									mCallbacks.onPullDistance(0f);
								}
								View view = mPullnReleaseHintView;
								if (view != null && view instanceof PullnReleaseHintView) {
									((PullnReleaseHintView) view).showPullHintView();
								}
								MotionEvent event = MotionEvent.obtain(ev);
//						EvtLog.d(TAG, "set cancle to self");
								event.setAction(MotionEvent.ACTION_CANCEL);
								super.onTouchEvent(event);
								event.recycle();
								pullDownBy(deltaY);
								return true;
							} else if (deltaY > 0) {
								mLastY = oldLastY;
//						EvtLog.d(TAG, "restore y to "+mLastY);
								return true;
							}
						}
						if (getLastVisiblePosition() == getItemCount() - 1
								&& mBottomFooter.getBottom() <= getHeight()) {
							if (deltaY < -mTouchSlop) {
								mStatus = Status.PULLING_UP;
								if (mCallbacks != null) {
									mCallbacks.onStartPullUp();
								}
								MotionEvent event = MotionEvent.obtain(ev);
//						EvtLog.d(TAG, "set cancle to self");
								event.setAction(MotionEvent.ACTION_CANCEL);
								super.onTouchEvent(event);
								event.recycle();
								pullUpBy(deltaY);
								return true;
							} else if (deltaY < 0) {
								mLastY = oldLastY;
								return true;
							}
						}
						return super.onTouchEvent(ev);
					case PULLING_DOWN:
						boolean willScrollUp = deltaY < -mTouchSlop
								&& getHintHeaderHeight() == 0;
						boolean tobeScrollUp = deltaY < 0 && Math.abs(deltaY) <= mTouchSlop
								&& getHintHeaderHeight() == 0;
						if (willScrollUp) {
							mStatus = Status.NORMAL;
							MotionEvent event = MotionEvent.obtain(ev);
							event.setAction(MotionEvent.ACTION_DOWN);
							event.setLocation(ev.getX(), ev.getY() - deltaY);
							super.onTouchEvent(event);
							event.recycle();
							return super.onTouchEvent(ev);
						} else {
							pullDownBy(deltaY);
							if (tobeScrollUp) {
								mLastY = oldLastY;
							}
							return true;
						}
					case PULLING_UP:
						boolean willScrollDown = deltaY > mTouchSlop
								&& getScrollY() == 0;
						boolean tobeScrollDown = deltaY > 0 && Math.abs(deltaY) <= mTouchSlop
								&& getScrollY() == 0;
						if (willScrollDown) {
							mStatus = Status.NORMAL;
							MotionEvent event = MotionEvent.obtain(ev);
							event.setAction(MotionEvent.ACTION_DOWN);
							event.setLocation(ev.getX(), ev.getY() - deltaY);
							super.onTouchEvent(event);
							event.recycle();
							return super.onTouchEvent(ev);
						} else {
							pullUpBy(deltaY);
							if (tobeScrollDown) {
								mLastY = oldLastY;
							}
							return true;
						}
					case RUNNING_TASK:
						break;
					case RETURNING:
						return true;
					default:
						break;
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
//			EvtLog.d(TAG, "按键抬起");
				mLastY = -1;
				if (mStatus == Status.PULLING_DOWN) {

					// 触发释放事件
					LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mStretchBody.getLayoutParams();
					if (mCallbacks != null) {
						mCallbacks.onPullRelease(MAX_PULL_DOWN_HEIGHT + lp.topMargin);
					}

//				EvtLog.d(TAG, "here 1");
					int curHeight = getHintHeaderHeight();
					if (curHeight == 0) {
//					EvtLog.d(TAG, "here 1 1");
						mStatus = Status.NORMAL;
						return true;
					} else if (mPullnReleaseHintView == null || curHeight <= mHintViewHeight
							|| mTask == null) {

						EvtLog.d(TAG, "here 1 2");
						ShirinkAnimation animation = new ShirinkAnimation(mStretchBody, curHeight, 0);
						animation.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationEnd(Animation animation) {
								mStatus = Status.NORMAL;
							}
						});
						animation.setDuration(100);
						animation.setInterpolator(new AccelerateInterpolator());
						mStatus = Status.RETURNING;
						mStretchBody.startAnimation(animation);
//					mStatus = Status.NORMAL;
//					lp = (LinearLayout.LayoutParams) mStretchBody.getLayoutParams();
//					if (getFirstVisiblePosition() <= 1) {
//						int height = (int) 0;
//						lp.topMargin = height - lp.height;
//						mStretchBody.getParent().requestLayout();
//
//						if(mCallbacks != null){
//							mCallbacks.onPullDistance(0f);
//						}
//					}

					} else {
//					EvtLog.d(TAG, "here 1 3");
						ShirinkAnimation animation = new ShirinkAnimation(mStretchBody, curHeight, mHintViewHeight);
						animation.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationEnd(Animation animation) {
								mStatus = Status.RUNNING_TASK;
								View view = mPullnReleaseHintView;
								if (view != null && view instanceof PullnReleaseHintView) {
									((PullnReleaseHintView) view).showWaitHintView();
								}
								if (mTask != null) {
									mTask.run();
								}
							}
						});
						animation.setDuration(100);
						animation.setInterpolator(new AccelerateInterpolator());
						mStatus = Status.RETURNING;
						mStretchBody.startAnimation(animation);
					}
					return true;
				} else if (mStatus == Status.PULLING_UP) {
//				EvtLog.d(TAG, "here 2");
					int curScrollY = getScrollY();

					if (mCallbacks != null) {
						mCallbacks.onPullRelease(curScrollY);
					}

					if (curScrollY == 0) {
						mStatus = Status.NORMAL;
					} else {
						PullUpReturnAnimation animation = new PullUpReturnAnimation();
						animation.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
							}

							@Override
							public void onAnimationEnd(Animation animation) {
								mStatus = Status.NORMAL;
							}
						});
						animation.setDuration(100);
						animation.setInterpolator(new AccelerateInterpolator());
						mStatus = Status.RETURNING;
						startAnimation(animation);
					}
					return true;

				}
				break;

			default:
				break;
		}
		return super.onTouchEvent(ev);
	}

	public void setTask(Runnable task) {
		mTask = task;
	}


	private int getHintHeaderHeight() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mStretchBody
				.getLayoutParams();
		return lp.height + lp.topMargin;
	}

	private void pullDownBy(float deltaHeight) {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mStretchBody
				.getLayoutParams();
		int oldHeight = lp.height + lp.topMargin;
		if (deltaHeight > 0) {
			deltaHeight = (1 - ((float) oldHeight) / MAX_PULL_DOWN_HEIGHT) * deltaHeight;
		} else {
			deltaHeight = Math.max(deltaHeight, -oldHeight);
		}
		lp.topMargin += deltaHeight;
		mStretchBody.requestLayout();

		if (mPullnReleaseHintView != null) {
			if (mPullnReleaseHintView instanceof PullnReleaseHintView) {
				PullnReleaseHintView pullnReleaseHintView = (PullnReleaseHintView) mPullnReleaseHintView;
				int newHeight = getHintHeaderHeight();
				pullnReleaseHintView.updateHintView(mHintViewHeight, newHeight);
			}
		}

		if (mCallbacks != null) {
			mCallbacks.onPullDistance(MAX_PULL_DOWN_HEIGHT + lp.topMargin);
		}
	}

	private void pullUpBy(float deltaHeight) {
		int curScrollY = getScrollY();
		if (deltaHeight < 0) {
			deltaHeight = (1 - ((float) curScrollY) / MAX_PULL_UP_HEIGHT)
					* deltaHeight;
		} else {
			deltaHeight = Math.min(deltaHeight, curScrollY);
		}
		scrollBy(0, (int) -deltaHeight);

		if (mCallbacks != null) {
			mCallbacks.onPullDistance(getScrollY());
		}
	}

	class ShirinkAnimation extends Animation {
		View mShirinkView;
		int mStartHeight;
		int mEndHeigth;

		public ShirinkAnimation(View shirinkView, int startHeight, int endHeight) {
			this.mShirinkView = shirinkView;
			this.mStartHeight = startHeight;
			this.mEndHeigth = endHeight;
		}

		@SuppressLint("NewApi")
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			super.applyTransformation(interpolatedTime, t);
			EvtLog.e("pullDownRefresh", "5555" + mStretchBody.isShown() + "  " + mStretchBody.getHeight());
//			EvtLog.d(TAG, "收缩动画  interpolatedTime = " + interpolatedTime);
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mShirinkView.getLayoutParams();
			if (getFirstVisiblePosition() <= 1) {
				int height = (int) (mStartHeight + interpolatedTime * (mEndHeigth - mStartHeight));
				lp.topMargin = height - lp.height;
				mShirinkView.getParent().requestLayout();
			} else {
				lp.topMargin = -lp.height;
				cancel();
			}
			EvtLog.d(TAG, "收缩动画  lp.topMargin = " + lp.topMargin);
			if (mCallbacks != null) {
				mCallbacks.onPullDistance(MAX_PULL_DOWN_HEIGHT + lp.topMargin);
			}
		}
	}

	private void shirinkHintHeader() {
		int curHeight = getHintHeaderHeight();
		ShirinkAnimation animation = new ShirinkAnimation(mStretchBody, curHeight, 0);
		animation.setDuration(100);
		animation.setInterpolator(new AccelerateInterpolator());
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mStatus = Status.NORMAL;
			}
		});
		mStretchBody.startAnimation(animation);
	}

	class PullUpReturnAnimation extends Animation {
		int startY;

		@Override
		public void initialize(int width, int height, int parentWidth,
							   int parentHeight) {
			startY = getScrollY();
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
										   Transformation t) {
			int desY = (int) (startY - interpolatedTime * startY);
			scrollTo(0, desY);
			if (mCallbacks != null) {
				mCallbacks.onPullDistance(desY);
			}
		}
	}

	@Override
	protected int computeVerticalScrollOffset() {
		return super.computeVerticalScrollOffset();
	}

	@Override
	protected int computeVerticalScrollExtent() {
		return super.computeVerticalScrollExtent();
	}

	@Override
	protected int computeVerticalScrollRange() {
		return super.computeVerticalScrollRange();
	}

	protected void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar,
										   int l, int t, int r, int b) {
		canvas.scale(1, mScrollBarScaleFactor, 0, getHeight());
		scrollBar.setBounds(l, t, r, b);
		scrollBar.draw(canvas);
		canvas.restore();
	}

	public void notifyTaskFinished() {
		if (mTask != null & getFirstVisiblePosition() <= 1) {
			shirinkHintHeader();
		} else { // 通过其他方式加载数据成功时也调用此方法，结束下拉状态
			mStretchBody.clearAnimation();
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mStretchBody
					.getLayoutParams();
			lp.topMargin = -lp.height;
			mStretchHeader.requestLayout();
			mStatus = PullRefreshListView.Status.NORMAL;
		}
	}


	@Override
	protected void onDetachedFromWindow() {
		// 由于本程序中各个TabActivity都有可能临时移除Window，稍后再恢复回来，我们需要保证这种非常规的
		// 操作下，ListView保持其当前位置不变，即，回到这个界面后，当layout行为发生时，ListView不会
		// 滚回到顶部
		// 通过离开或回来时调用Adapter的notifyDataSetChanged方法，可以记录当前的位置，满足以上要求，
		// 考虑本程序使用了很多TabActivity，将这个过程实现的ListView中
		ListAdapter listAdapter = getAdapter();
		while (true) {
			if (listAdapter != null) {
				if (listAdapter instanceof WrapperListAdapter) {
					listAdapter = ((WrapperListAdapter) listAdapter).getWrappedAdapter();
					continue;
				} else {
					if (listAdapter instanceof BaseAdapter) {
						((BaseAdapter) listAdapter).notifyDataSetChanged();
					}
					break;
				}
			} else {
				break;
			}
		}
		super.onDetachedFromWindow();
	}

	/**
	 * 下拉刷新
	 * mTask	下拉后需要执行的任务，不可为空
	 * mPullnReleaseHintView	顶部下拉的view，不可为空
	 */
	public void pullDownRefresh() {
		if (mPullnReleaseHintView != null && mTask != null && mStatus == Status.NORMAL) {
			EvtLog.e("pullDownRefresh", mStretchBody.isShown() + "  " + mStatus);
			// 强行停止 listView的滚动
			this.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
			// 直接定位listview到顶部
			this.setSelection(0);

			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mStretchBody.getLayoutParams();
			lp.topMargin = mHintViewHeight - lp.height;
			mStretchBody.getParent().requestLayout();

			mStatus = Status.RUNNING_TASK;
			View view = mPullnReleaseHintView;
			if (view != null && view instanceof PullnReleaseHintView) {
				((PullnReleaseHintView) view).showWaitHintView();
			}
			if (mTask != null) {
				mTask.run();
			}
		}
	}

	public interface Callbacks {

		/**
		 * 开始下拉之前调用
		 */
		void onStartPullDown();

		/**
		 * 开始上拉之前刷新
		 */
		void onStartPullUp();

		/**
		 * 拉动的距离
		 */
		void onPullDistance(float distance);

		/**
		 * 拉动释放
		 *
		 * @param lastDistance
		 */
		void onPullRelease(float lastDistance);
	}

	private Callbacks mCallbacks;

	public void setCallbacks(Callbacks callbacks) {
		mCallbacks = callbacks;
	}
}
