package tv.live.bx.ui.widget;

/**
 * 继承自swipeRefreshLayout，用于扩展
 */
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import tv.live.bx.common.Utils;

/**
 * @author yong 继承自SwipeRefreshLayout，扩展实现上拉加载的功能。
 * @see ListView HeaderFooterGridView. 因为底部loading目前是用addFooter来实现的。
 *
 */
public class RefreshLayout extends SwipeRefreshLayout {
	public enum LoadingStatus {
		WaitToLoading, ClickToLoading, NoMore
	}

	private AbsListView mAdpView;
	/** 上拉加载监听器 */
	private OnLoadListener monLoadListener;
	/** loading View */
	private View mAdpViewFooter;
	/** loading progress */
	private ProgressBar progressBar;
	/** loading TextView */
	private TextView loadingText;

	private int mTouchSlop;
	/** 加载状态 */
	private LoadingStatus loadingStatus = LoadingStatus.WaitToLoading;

	public RefreshLayout(Context ctx) {
		super(ctx);
	}

	public RefreshLayout(Context ctx, AttributeSet attri) {
		super(ctx, attri);

		mTouchSlop = ViewConfiguration.get(ctx).getScaledTouchSlop();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

	}

	@Override
	public void addView(View child, LayoutParams params) {
		super.addView(child, params);
		if (mAdpView == null && child instanceof AbsListView) {
			getListView(child);
		}
	}

	private float mPrevX;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mPrevX = event.getX();
			break;

		case MotionEvent.ACTION_MOVE:
			final float eventX = event.getX();
			float xDiff = Math.abs(eventX - mPrevX);

			if (xDiff > mTouchSlop) {
				return false;
			}
		}

		return super.onInterceptTouchEvent(event);
	}

	/**
	 * 获取ListView对象
	 */
	private void getListView(View child) {
		if (child instanceof AbsListView) {
			mAdpView = (AbsListView) child;
			mAdpView.setOnScrollListener(onScrollListener);
			RefreshLayout.this.setOnTouchListener(onTouchListener);
			mAdpView.addOnLayoutChangeListener(onLayoutChange);

//			mAdpViewFooter = LayoutInflater.from(getContext()).inflate(
//					R.layout.lo_loading_more, mAdpView, false);
//			progressBar = (ProgressBar) mAdpViewFooter
//					.findViewById(R.id.pull_to_refresh_load_progress);
//			loadingText = (TextView) mAdpViewFooter
//					.findViewById(R.id.pull_to_refresh_loading_text);

			mAdpViewFooter.setVisibility(View.GONE);
			mAdpViewFooter.setOnClickListener(onFooterClick);

			if (child instanceof ListView) {
				((ListView) mAdpView).addFooterView(mAdpViewFooter);
			}
//			else if (child instanceof HeaderFooterGridView) {
//				((HeaderFooterGridView) mAdpView).addFooterView(mAdpViewFooter);
//			}
		}
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}

	/**
	 * 
	 * @param v
	 * @param event
	 * @return
	 */
	private OnTouchListener onTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				v.performClick();
				break;
			default:
				break;
			}
			// 如果正在执行上拉加载的请求，拦截手势事件，不去触发下拉刷新
			return mIsLoading;
		}
	};

	@Override
	public void setRefreshing(boolean arg0) {
		if (!arg0 && mIsLoading) {
			setLoadingStatus(false, LoadingStatus.WaitToLoading);
		}
		super.setRefreshing(arg0);

	}

	/**
	 * 列表是否没有满一页
	 */
	private boolean isListNotFull() {
		if (mAdpView == null)
			return true;
		if (mAdpView.getCount() == 0)
			return true;
		else {
			int lastIndex = mAdpView.getLastVisiblePosition()
					- mAdpView.getFirstVisiblePosition();
			if (lastIndex < 0)
				return true;
			return mAdpView.getChildAt(lastIndex).getBottom() < mAdpView
					.getBottom() - mAdpView.getPaddingBottom();
		}
	}

	@Override
	public boolean canChildScrollUp() {
//		if (mAdpView instanceof HeaderFooterGridView) {
//			final AbsListView absListView = (AbsListView) mAdpView;
//			if (absListView.getCount() > 0) {
//				int index = mAdpView.getFirstVisiblePosition();
//				View top = mAdpView.getChildAt(0);
//				if (top == null
//						|| (mAdpView.getChildAt(0).getTop() == mAdpView
//								.getPaddingTop() && index == 0))
//					return false;
//				else
//					return true;
//
//			}
//			return false;
//		}
		return super.canChildScrollUp();
	}

	/**
	 * 列表是否能继续上拉
	 * 
	 * @return
	 */
	public boolean canChildScrollDown() {
		if (android.os.Build.VERSION.SDK_INT < 14) {
			if (mAdpView instanceof AbsListView) {
				final AbsListView absListView = mAdpView;
				try {
					if (absListView.getCount() > 0) {
						int lastIndex = absListView.getLastVisiblePosition()
								- absListView.getFirstVisiblePosition();
						return absListView.getChildAt(lastIndex).getBottom() != absListView
								.getHeight() - absListView.getPaddingBottom();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			} else {
				return true;
			}
		} else {
			return ViewCompat.canScrollVertically(mAdpView, 1);
		}
	}

	private void loadData() {
		if (mIsLoading)
			return;
		if (monLoadListener != null) {
			// 设置状态
			setLoadingStatus(true, LoadingStatus.WaitToLoading);
			monLoadListener.onLoad();
		}
	}

	public boolean isOnLoading() {
		return mIsLoading;
	}

	private boolean mIsLoading;

	/**
	 * @param loading
	 *            是否在加载
	 */
	public void setLoadingStatus(boolean loading, LoadingStatus status) {
		mIsLoading = loading;
		this.loadingStatus = status;

		switch (status) {
		case WaitToLoading:
			mAdpViewFooter.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.VISIBLE);
//			loadingText.setText(R.string.loading);
			break;
		case ClickToLoading:
			mAdpViewFooter.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
//			loadingText.setText(R.string.click_to_load_more);
			break;
		case NoMore:
			mAdpViewFooter.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
//			loadingText.setText(R.string.no_more_items);
			break;
		default:
			break;
		}
	}

	/**
	 * 是否能加载
	 */
	private boolean canLoad() {
		return !mIsLoading && loadingStatus == LoadingStatus.WaitToLoading
				&& !isListNotFull() && !canChildScrollDown() && !isRefreshing();
	}

	/**
	 * 
	 * @param loadListener
	 */
	public void setOnLoadListener(OnLoadListener loadListener) {
		this.monLoadListener = loadListener;
	}

	/**
	 * footer click
	 */
	private OnClickListener onFooterClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (Utils.isFastDoubleClick(400))
				return;

			if (loadingStatus != LoadingStatus.ClickToLoading)
				return;

			loadData();
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
				mAdpViewFooter.setVisibility(View.GONE);
			else
				mAdpViewFooter.setVisibility(View.VISIBLE);
		}
	};

	/**
	 * 
	 */
	private OnScrollListener onScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (canLoad()) {
				loadData();
			}
		}
	};

	/**
	 * 加载更多的监听器
	 */
	public interface OnLoadListener {
		void onLoad();
	}
}
