package tv.live.bx.ui.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import tv.live.bx.R;
import tv.live.bx.common.Utils;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.PullnReleaseHintView;

/**
 * 自定义上拉刷新以及下拉加载控件
 * Created by BYC on 2017/7/10.
 */

public class CustomRefreshLayout extends LinearLayout {
	//动画时长
	private final int SMOOTH_SCROLL_DURATION_MS = 200;
	//最大高度--比例
	private final float MAX_SCROLL_HEIGHT_RADIO = 1.6f;
	//头部高度
	private final int HEAD_HINT_VIEW_HEIGHT = 60;

	//正在刷新数据
	private boolean mIsRefreshing;
	//正在加载更多
	private boolean mIsLoading;
	private boolean mIsBeingDragged = false;
	//是否正在返回到指定位置
	private boolean mIsReturning = false;

	private float mInitialDownY;

	//滚动动画
	private ValueAnimator mScrollAnimator;

	//头部--视图
	private PullnReleaseHintView mHintView;
	//底部--视图
	private ListFooterLoadView footerLoadView;
	//头部高度
	private int mHeadViewHeight;
	//最大拖动高度
	private int mMaxScrollHeight;
	//targetView
	private View mTargetView;
	//刷新监听器
	private OnRefreshListener onRefreshListener;
	//是否可以加载更多
	private boolean canLoadingMore = false;


	//判断是否在拖动
	private int mTouchSlop;
	private int mActivePointerId = -1;

	public CustomRefreshLayout(Context ctx) {
		this(ctx, null);
	}

	public CustomRefreshLayout(Context ctx, AttributeSet attrs) {
		this(ctx, attrs, 0);
	}

	public CustomRefreshLayout(Context ctx, AttributeSet attrs, int defaultSet) {
		super(ctx, attrs, defaultSet);
		init();
	}

	/**
	 * 初始化，添加头部
	 */
	private void init() {
		setOrientation(VERTICAL);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		mHeadViewHeight = (int) (HEAD_HINT_VIEW_HEIGHT * getResources().getDisplayMetrics().density);
		mMaxScrollHeight = (int) (mHeadViewHeight * MAX_SCROLL_HEIGHT_RADIO);
		addDefaultHintView();

		scrollTo(0, 0);
	}

	/**
	 * 添加默认头部
	 */
	private void addDefaultHintView() {
		mHintView = (PullnReleaseHintView) LayoutInflater.from(getContext()).inflate(R.layout.a_common_list_header_hint, this, false);
		addView(mHintView, 0);
		refreshLoadingViewsSize();
	}

	/**
	 * 设置头部高度
	 *
	 * @param height
	 */
	public void setHeadHintViewHeight(int height) {
		mHeadViewHeight = height;
		mMaxScrollHeight = (int) (mHeadViewHeight * MAX_SCROLL_HEIGHT_RADIO);
		refreshLoadingViewsSize();
	}

	public void setFooterHintView(ListFooterLoadView footerHintView) {
		this.footerLoadView = footerHintView;
		footerLoadView.setOnClickListener(clickListener);
		footerLoadView.setVisibility(View.INVISIBLE);
		canLoadingMore = true;
	}

	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (Utils.isFastDoubleClick())
				return;
			if (footerLoadView.getStatus() == ListFooterLoadView.STATUS_FAILED) {
				mIsLoading = true;
				footerLoadView.onLoadingStarted();
				if (onRefreshListener != null)
					onRefreshListener.onLoadMore();
			}
		}
	};

	//设置刷新监听器
	public void setOnRefreshListener(OnRefreshListener listener) {
		this.onRefreshListener = listener;
	}

	/**
	 * 开启下拉加载
	 */
	public void pullToRefresh() {
		smoothScrollTo(-mHeadViewHeight, new onAnimationEnd() {
			@Override
			public void onFinish() {
				mIsRefreshing = true;
				mHintView.showWaitHintView();
				if (onRefreshListener != null)
					onRefreshListener.onRefresh();
			}
		});
	}

	/**
	 * 刷新完成
	 */
	public void onRefreshComplete() {
		if (mIsRefreshing) {
			this.smoothScrollTo(0, new onAnimationEnd() {
				@Override
				public void onFinish() {
					reset();
				}
			});
		}
	}

	/**
	 * 加载更多完成
	 */
	public void onLoadingComplete(boolean isSuccess, boolean isNoMoreData) {
		if (footerLoadView == null || !mIsLoading)
			return;
		mIsLoading = false;
		if (!isSuccess) {
			footerLoadView.onLoadingFailed();
		} else {
			if (isNoMoreData)
				footerLoadView.onNoMoreData();
			else
				footerLoadView.hide();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldW, int oldH) {
		super.onSizeChanged(w, h, oldW, oldH);

		// We need to update the header/footer when our size changes
		refreshLoadingViewsSize();
		/**
		 * As we're currently in a Layout Pass, we need to schedule another one
		 * to layout any changes we've made here
		 */
		post(new Runnable() {
			@Override
			public void run() {
				requestLayout();
			}
		});
	}

	/**
	 * Re-measure the Loading Views height, and adjust internal padding as
	 * necessary
	 */
	protected final void refreshLoadingViewsSize() {
		int pLeft = getPaddingLeft();
		int pTop = -mHeadViewHeight;
		int pRight = getPaddingRight();
		int pBottom = getPaddingBottom();

		setPadding(pLeft, pTop, pRight, pBottom);
	}


	/**
	 * 确定用于下拉刷新的控件
	 */
	private void ensureTarget() {
		if (mTargetView == null) {
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				if (!(child instanceof PullnReleaseHintView)) {
					mTargetView = child;
					mTargetView.addOnLayoutChangeListener(onLayoutChange);
					addFooterLoadView();
					addScrollListener();
					break;
				}
			}
		}
	}

	/**
	 * 添加底部上拉加载布局
	 */
	private void addFooterLoadView() {
		if (mTargetView instanceof ListView) {
			ListView listView = (ListView) mTargetView;
			setFooterHintView((ListFooterLoadView) LayoutInflater.from(getContext()).inflate(R.layout.a_common_list_footer_loader_view, null));
			listView.addFooterView(footerLoadView);
		} else if (mTargetView instanceof HeaderFooterGridView) {
			HeaderFooterGridView gridView = (HeaderFooterGridView) mTargetView;
			setFooterHintView((ListFooterLoadView) LayoutInflater.from(getContext()).inflate(R.layout.a_common_list_footer_loader_view, null));
			gridView.addFooterView(footerLoadView);
		}
	}

	/**
	 * 添加滚动监听器
	 */
	private void addScrollListener() {
		if (mTargetView instanceof AbsListView) {
			AbsListView absListView = (AbsListView) mTargetView;
			absListView.setOnScrollListener(onAbsListScrollListener);
		} else if (mTargetView instanceof RecyclerView) {
			RecyclerView recyclerView = (RecyclerView) mTargetView;
			recyclerView.addOnScrollListener(onRecycleViewScrollListener);
		}
	}

	/**
	 * 是否能加载
	 */
	private boolean canLoad() {
		return !mIsLoading && !isRefreshing() && footerLoadView.getVisibility() == VISIBLE && footerLoadView.canLoadMore();
	}


	private RecyclerView.OnScrollListener onRecycleViewScrollListener = new RecyclerView.OnScrollListener() {
		@Override

		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			if (dy == 0)
				return;
			if (canLoad() && footerLoadView != null) {
				if (recyclerView.getLayoutManager() instanceof LinearLayoutManager &&
						((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1) {
					mIsLoading = true;
					footerLoadView.onLoadingStarted();
					if (onRefreshListener != null)
						onRefreshListener.onLoadMore();
				} else if (recyclerView.getLayoutManager() instanceof GridLayoutManager &&
						((GridLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1) {
					mIsLoading = true;
					footerLoadView.onLoadingStarted();
					if (onRefreshListener != null)
						onRefreshListener.onLoadMore();
				}
			}
		}
	};


	private AbsListView.OnScrollListener onAbsListScrollListener = new AbsListView.OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (view.getChildCount() == 0 || !canLoad())
				return;
			if (view instanceof HeaderFooterGridView && footerLoadView.getParent().getParent() == view) {
				mIsLoading = true;
				footerLoadView.onLoadingStarted();
				if (onRefreshListener != null)
					onRefreshListener.onLoadMore();
			} else if (footerLoadView.getParent() == view) {
				mIsLoading = true;
				footerLoadView.onLoadingStarted();
				if (onRefreshListener != null)
					onRefreshListener.onLoadMore();
			}
		}
	};

	/**
	 * 布局改变事件监听
	 */
	private OnLayoutChangeListener onLayoutChange = new OnLayoutChangeListener() {
		@Override
		public void onLayoutChange(View v, int left, int top, int right,
								   int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
			if (isListNotFull())
				footerLoadView.setVisibility(View.INVISIBLE);
			else
				footerLoadView.setVisibility(View.VISIBLE);
		}
	};

	/**
	 * 列表是否没有满一页
	 */
	private boolean isListNotFull() {
		if (mTargetView == null)
			return true;
		if (mTargetView instanceof AbsListView) {
			AbsListView absListView = (AbsListView) mTargetView;
			if (absListView.getCount() == 0 || absListView.getChildAt(0) == null)
				return true;
			else {
				return absListView.getFirstVisiblePosition() == 0 && (absListView.getChildAt(0).getTop() - absListView.getPaddingTop() == 0) && !canChildScrollDown();
			}
		} else if (mTargetView instanceof RecyclerView) {
			RecyclerView recyclerView = (RecyclerView) mTargetView;
			if (recyclerView.getAdapter() == null || recyclerView.getChildCount() == 0 || recyclerView.getLayoutManager() == null)
				return true;
			View topView = recyclerView.getChildAt(0);

			return recyclerView.getChildAdapterPosition(topView) == 0 && recyclerView.getLayoutManager().getDecoratedTop(topView) - recyclerView.getPaddingTop() == 0 && !canChildScrollDown();
		} else {
			return mTargetView.getScrollY() == 0 && !canChildScrollDown();
		}
	}

	/**
	 * 列表是否能继续上拉
	 *
	 * @return
	 */
	public boolean canChildScrollDown() {
		if (android.os.Build.VERSION.SDK_INT < 14) {
			if (mTargetView instanceof AbsListView) {
				final AbsListView absListView = (AbsListView) mTargetView;
				if (absListView.getCount() > 0) {
					int lastIndex = absListView.getLastVisiblePosition()
							- absListView.getFirstVisiblePosition();
					return absListView.getChildAt(lastIndex).getBottom() != absListView
							.getHeight() - absListView.getPaddingBottom();
				} else {
					return false;
				}
			} else {
				return ViewCompat.canScrollVertically(mTargetView, 1);
			}
		} else {
			return ViewCompat.canScrollVertically(mTargetView, 1);
		}
	}


	/**
	 * @return Whether it is possible for the child view of this layout to
	 * scroll up. Override this if the child view is a custom view.
	 */
	public boolean canChildScrollUp() {
//		if (mChildScrollUpCallback != null) {
//			return mChildScrollUpCallback.canChildScrollUp(this, mTarget);
//		}
		if (android.os.Build.VERSION.SDK_INT < 14) {
			if (mTargetView instanceof AbsListView) {
				final AbsListView absListView = (AbsListView) mTargetView;
				return absListView.getChildCount() > 0
						&& (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
						.getTop() < absListView.getPaddingTop());
			} else {
				return ViewCompat.canScrollVertically(mTargetView, -1) || mTargetView.getScrollY() > 0;
			}
		} else {
			return ViewCompat.canScrollVertically(mTargetView, -1);
		}
	}

	@Override
	public void addView(View child) {
		super.addView(child);
		ensureTarget();
	}

	@Override
	public void addView(View child, int index) {
		super.addView(child, index);
		ensureTarget();
	}

	@Override
	public void addView(View child, ViewGroup.LayoutParams params) {
		super.addView(child, params);
		ensureTarget();
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
		ensureTarget();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		int pointerIndex;

		if (!isEnabled() || canChildScrollUp() || mIsLoading)
			return false;
		if (mIsRefreshing || mIsReturning)
			return true;

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mActivePointerId = event.getPointerId(0);
				mIsBeingDragged = false;
				pointerIndex = event.findPointerIndex(mActivePointerId);
				if (pointerIndex < 0)
					return false;
				mInitialDownY = event.getY(pointerIndex);
				break;
			case MotionEvent.ACTION_MOVE:
				if (mActivePointerId == -1) {
					return false;
				}
				pointerIndex = event.findPointerIndex(mActivePointerId);
				if (pointerIndex < 0) {
					return false;
				}
				float y = event.getY(pointerIndex);
				startDragging(y);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mIsBeingDragged = false;
				mActivePointerId = -1;
				break;
		}

		return mIsBeingDragged;
	}

	/**
	 * 判断是否开始拖拽
	 *
	 * @param y
	 */
	private void startDragging(float y) {
		float diff = y - mInitialDownY;
		if (diff > mTouchSlop && !mIsBeingDragged) {
			mLastY = mInitialDownY + mTouchSlop;
			mIsBeingDragged = true;
		}
	}

	/**
	 * 重置状态
	 */
	private void reset() {
		mIsLoading = mIsBeingDragged = false;
		mIsRefreshing = false;
		mLastY = mInitialDownY = -1;
		if (mHintView != null)
			mHintView.showPullHintView();
		if (mScrollAnimator != null && mScrollAnimator.isRunning()) {
			mScrollAnimator.removeAllListeners();
			mScrollAnimator.cancel();
		}
		if (footerLoadView != null)
			footerLoadView.hide();
	}

	/**
	 * 下拉控件
	 */
	private void pullEvent(float diff) {
		int oldY = -this.getScrollY();
		if (diff > 0) {
			diff = (1 - oldY * 1.0f / mMaxScrollHeight) * diff;
		}
		int targetY = oldY + (int) diff;
		if (targetY < 0)
			targetY = 0;
		this.scrollTo(0, -targetY);
		if (mHintView != null && mHintView instanceof PullnReleaseHintView) {
			mHintView.updateHintView(mHeadViewHeight, targetY);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		reset();
		super.onDetachedFromWindow();
	}

	private void finishPullEvent() {
		int dis = -this.getScrollY();
		if (dis >= mHeadViewHeight) {
			this.smoothScrollTo(-mHeadViewHeight, null);
			mHintView.showWaitHintView();
			mIsRefreshing = true;
			if (onRefreshListener != null) {
				onRefreshListener.onRefresh();
			}
		} else {
			this.smoothScrollTo(0, null);
		}
	}

	private void smoothScrollTo(int targetY, final onAnimationEnd listener) {
		if (mScrollAnimator != null && mScrollAnimator.isRunning())
			mScrollAnimator.cancel();
		int startY = getScrollY();
		mScrollAnimator = ValueAnimator.ofInt(startY, targetY);
		mScrollAnimator.setInterpolator(new DecelerateInterpolator());
		mScrollAnimator.setDuration(SMOOTH_SCROLL_DURATION_MS);
		mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				CustomRefreshLayout.this.scrollTo(0, (int) animation.getAnimatedValue());
			}
		});
		mScrollAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				mIsReturning = true;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mIsReturning = false;
				if (listener != null)
					listener.onFinish();
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mIsReturning = false;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		mScrollAnimator.start();
	}

	private float mLastY = 0f;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		int pointerIndex;
		if (mIsRefreshing || mIsReturning)
			return false;
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mActivePointerId = event.getPointerId(0);
				mIsBeingDragged = false;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				pointerIndex = event.getActionIndex();
				if (pointerIndex < 0) {
					return false;
				}
				mActivePointerId = event.getPointerId(pointerIndex);
				break;
			case MotionEvent.ACTION_MOVE:
				pointerIndex = event.findPointerIndex(mActivePointerId);
				if (pointerIndex < 0) {
					return false;
				}
				float y = event.getY(pointerIndex);
				startDragging(y);
				if (mIsBeingDragged) {
					float diff = y - mLastY;
					mLastY = y;
					pullEvent(diff);
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
				onSecondaryPointerUp(event);
				break;
			case MotionEvent.ACTION_UP:
				pointerIndex = event.findPointerIndex(mActivePointerId);
				if (pointerIndex < 0) {
					return false;
				}

				if (mIsBeingDragged) {
					mIsBeingDragged = false;
					finishPullEvent();
				}
			case MotionEvent.ACTION_CANCEL:
				if (mIsBeingDragged) {
					mIsBeingDragged = false;
				}
				mActivePointerId = -1;
				break;
		}

		return super.onTouchEvent(event);

	}

	private void onSecondaryPointerUp(MotionEvent ev) {
		final int pointerIndex = ev.getActionIndex();
		final int pointerId = ev.getPointerId(pointerIndex);
		if (pointerId == mActivePointerId) {
			// This was our active pointer going up. Choose a new
			// active pointer and adjust accordingly.
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mActivePointerId = ev.getPointerId(newPointerIndex);
		}
	}

	/**
	 * 是否正在下拉刷新
	 *
	 * @return
	 */
	public boolean isRefreshing() {
		return mIsRefreshing;
	}

	public interface OnRefreshListener {
		void onRefresh();

		void onLoadMore();
	}

	private interface onAnimationEnd {
		void onFinish();
	}


}
