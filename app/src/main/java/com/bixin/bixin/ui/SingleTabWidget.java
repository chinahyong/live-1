package com.bixin.bixin.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabWidget;
import android.widget.TextView;

import com.bixin.bixin.library.util.EvtLog;

@SuppressLint("NewApi")
public class SingleTabWidget extends TabWidget {

	private OnTabChangedListener mOnTabChangedListener;

	private int mLayoutId = -1;
	private int mSelectedTab = -1;

	public interface OnTabChangedListener {
		void onTabChanged(int tabIndex);
	}

	public SingleTabWidget(Context context, AttributeSet attrs) {
		super(context, attrs);

		setStripEnabled(false);
		setDividerDrawable(null);
	}

	public void setLayout(int layoutResId) {
		mLayoutId = layoutResId;
	}

	public void addTab(int imageResId, int tabIndex) {
		addTab(mLayoutId, imageResId, null, tabIndex);
	}

	public void addTab(String title, int tabIndex) {
		addTab(mLayoutId, 0, title, tabIndex);
	}

	public void addTab(int imageResId, String title, int tabIndex) {
		addTab(mLayoutId, imageResId, title, tabIndex);
	}

	public void addTab(int imageResId, int txtRes, int tabIndex) {
		addTab(mLayoutId, imageResId, getResources().getString(txtRes), tabIndex);
	}

	public void addTab(int mLayoutId, int imageResId, String title, int tabIndex) {
		View view = LayoutInflater.from(getContext()).inflate(mLayoutId, this, false);
		if (view == null) {
			throw new RuntimeException("You must call 'setLayout(int layoutResId)' to initialize the tab.");
		} else {
			LinearLayout.LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
			view.setLayoutParams(lp);
		}

		if (view instanceof TextView) {
			if (imageResId > 0) {
				((TextView) view).setCompoundDrawablesWithIntrinsicBounds(0, imageResId, 0, 0);
			}
			if (!TextUtils.isEmpty(title)) {
				((TextView) view).setText(title);
			}
		} else if (view instanceof ImageView) {
			if (imageResId > 0) {
				((ImageView) view).setImageResource(imageResId);
			}
		} else {
			TextView textView = (TextView) view.findViewById(android.R.id.title);
			if (textView == null) {
				// throw new RuntimeException(
				// "Your layout must have a TextView whose id attribute is 'android.R.id.title'");
			} else {
				textView.setText(title);
			}
			ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
			if (imageView == null) {
				// throw new RuntimeException(
				// "Your layout must have a ImageView whose id attribute is 'android.R.id.icon'");
			} else {
				imageView.setImageResource(imageResId);
			}
		}

		addView(view);
		view.setFocusable(true);
		view.setClickable(true);
		view.setOnClickListener(new TabClickListener(tabIndex));
		view.setOnFocusChangeListener(this);
	}

	public void updateTab(int imageResId, String title, int tabIndex) {
		View view = getChildAt(tabIndex);
		if (view == null) {
			throw new RuntimeException("tabIndex View no to initialize the tab.");
		}
		if (view instanceof TextView) {
			if (imageResId > 0) {
				((TextView) view).setCompoundDrawablesWithIntrinsicBounds(0, imageResId, 0, 0);
			}
			if (!TextUtils.isEmpty(title)) {
				((TextView) view).setText(title);
			}
		} else if (view instanceof ImageView) {
			if (imageResId > 0) {
				((ImageView) view).setImageResource(imageResId);
			}
		} else {
			TextView textView = (TextView) view.findViewById(android.R.id.title);
			if (textView == null) {
				// throw new RuntimeException(
				// "Your layout must have a TextView whose id attribute is 'android.R.id.title'");
			} else {
				textView.setText(title);
			}
			ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
			if (imageView == null) {
				// throw new RuntimeException(
				// "Your layout must have a ImageView whose id attribute is 'android.R.id.icon'");
			} else {
				imageView.setImageResource(imageResId);
			}
		}

	}

	public void setOnTabChangedListener(OnTabChangedListener listener) {
		mOnTabChangedListener = listener;
	}

	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		EvtLog.e("SingleTabWidget", "onFocusChange hasFocus" + hasFocus);
		// if (view instanceof TextView) {
		// if (hasFocus) {
		// ((TextView) view).getPaint().setFakeBoldText(true);
		// } else {
		// ((TextView) view).getPaint().setFakeBoldText(false);
		// }
		// }
	}

	private class TabClickListener implements OnClickListener {
		private final int mIndex;

		public TabClickListener(int index) {
			mIndex = index;
		}

		@Override
		public void onClick(View view) {
			setCurrentTab(mIndex);
		}
	}

	@Override
	public void setCurrentTab(int index) {
		if (mSelectedTab != -1) {
			if (getChildTabViewAt(mSelectedTab) instanceof TextView) {
				((TextView) getChildTabViewAt(mSelectedTab)).getPaint().setFakeBoldText(false);
			}
		}
		super.setCurrentTab(index);
		mSelectedTab = index;
		if (getChildTabViewAt(mSelectedTab) instanceof TextView) {
			((TextView) getChildTabViewAt(mSelectedTab)).getPaint().setFakeBoldText(true);
		}
		if (mOnTabChangedListener != null) {
			mOnTabChangedListener.onTabChanged(mSelectedTab);
		}
	}
}
