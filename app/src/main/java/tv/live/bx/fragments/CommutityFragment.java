package tv.live.bx.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.analytics.MobclickAgent;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.CalMainActivity;
import tv.live.bx.listeners.OnUpdateListener;
import tv.live.bx.ui.SingleTabWidget;
import tv.live.bx.ui.SingleTabWidget.OnTabChangedListener;

/**
 * 社区页面 Title: CommutityFragment.java
 * @version 1.0
 * @CreateDate 2014-8-13
 */
@SuppressLint({ "NewApi", "ResourceAsColor" })
public class CommutityFragment extends BaseFragment implements OnClickListener {
	public CalMainActivity mActivity;

	private final String FRAGMENT_TAG_FORMAT = "CommutityFragment_%s";
	public static final int TAB_HOT = 0; // 热门
	public static final int TAB_FAN = 1; // 饭圈

	/** 底部TAB栏 */
	private SingleTabWidget tabWidget;
	private OnTabChangedListener mOnTabChangedListener;
	/**
	 * 当前显示的Fragment
	 */
	private int mTabIndex = -1;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (CalMainActivity) activity;
	}

	@Override
	protected void initMembers() {
		initUI();
	}

	@Override
	protected void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {
	}

	@Override
	protected void initData(Bundle bundle) {
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/* 在这里，我们通过碎片管理器中的Tag，就是每个碎片的名称，来获取对应的fragment */
		Fragment f = mActivity.getSupportFragmentManager().findFragmentByTag(getFragmentTag(mTabIndex));
		/* 然后在碎片中调用重写的onActivityResult方法 */
		f.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onTabClickAgain() {
		// if (mListView != null) {
		// mListView.setSelection(0);
		// }
	}

	/**
	 * 之前忘记使用这些方法了，这个类暂时不用了
	 */
	@Override
	protected int getLayoutRes() {
		return R.layout.a_commutity_list_layout;
	}

	/**
	 * 初始化UI控件，加载Fragment
	 */
	private void initUI() {
		// 初始化底部TabWidget
		tabWidget = (SingleTabWidget) mRootView.findViewById(R.id.main_tabs);

		// 添加四个Tab
		tabWidget.addTab(R.layout.a_common_tab_left, 0, getResources().getString(R.string.commutity_tab_hot), TAB_HOT);
		tabWidget.addTab(R.layout.a_common_tab_right, 0, getResources().getString(R.string.commutity_tab_fan), TAB_FAN);

		final FragmentManager fm = mActivity.getSupportFragmentManager();
		mOnTabChangedListener = new OnTabChangedListener() {
			@Override
			public void onTabChanged(int tabIndex) {
				switch (tabIndex) {
				case TAB_HOT:
					MobclickAgent.onEvent(FeizaoApp.mConctext, "switchDiscover");
					break;
				case TAB_FAN:
					MobclickAgent.onEvent(FeizaoApp.mConctext, "switchFanCircle");
					break;
				}
				if (tabIndex == mTabIndex) {
					Fragment f = getFragmentByIndex(tabIndex, fm);
					if (f instanceof OnUpdateListener) {
						// 回调Tab再次点击方法
						((OnUpdateListener) f).onTabClickAgain();
					}
					return;
				}
				switchFragment(tabIndex);
				mTabIndex = tabIndex;
			}
		};

		// 设置监听
		tabWidget.setOnTabChangedListener(mOnTabChangedListener);

		// if (savedInstanceState != null) {
		// // Activity自动保存了Fragment的状态，此处要隐藏所有的Fragment，不然可能导致Fragment重叠
		// FragmentTransaction ft = fm.beginTransaction();
		// for (Fragment f : fm.getFragments()) {
		// ft.hide(f);
		// }
		// ft.commitAllowingStateLoss();
		// }

		// 默认显示
		tabWidget.setCurrentTab(TAB_HOT);
		switchFragment(TAB_HOT);
		mTabIndex = TAB_HOT;
	}

	/**
	 * 初始化日历展示的Fragment
	 */
	private void switchFragment(int tabIndex) {
		final FragmentManager manager = mActivity.getSupportFragmentManager();
		FragmentTransaction t = manager.beginTransaction();
		if (mTabIndex != -1) {
			Fragment last = getFragmentByIndex(mTabIndex, manager);
			if (last != null) {
				if (last.isAdded()) {
					// 如果已经关联到Activity，隐藏Fragment
					t.hide(last);
				} else {
					// 此行的代码暂时不会执行？
					t.remove(last);
				}
			}
		}
		Fragment current = getFragmentByIndex(tabIndex, manager);
		if (current != null) {
			t.setTransition(FragmentTransaction.TRANSIT_NONE); // 无动画
			if (current.isAdded()) {
				// 如果已经添加，则直接显示Fragment
				t.show(current);
			} else {
				// 添加Fragment到Activity中
				t.add(R.id.frame_layout, current, getFragmentTag(tabIndex));
			}
		}
		t.commitAllowingStateLoss();

		// 回调Tab选中方法
		if (current instanceof OnUpdateListener) {
			((OnUpdateListener) current).onTabClick();
		}
	}

	/**
	 * 根据index生成Fragment的Tag
	 * 
	 * @param tabIndex Fragment对应的下标值（本类中的常量定义）
	 */
	private String getFragmentTag(int tabIndex) {
		return String.format(FRAGMENT_TAG_FORMAT, tabIndex);
	}

	/**
	 * 根据index获取对应的Fragment对象
	 * 
	 * @param index Fragment对应的下标值（本类中的常量定义）
	 */
	private Fragment getFragmentByIndex(int index, FragmentManager fm) {
		Fragment f = fm.findFragmentByTag(getFragmentTag(index));
		if (f == null) {
			switch (index) {
			case TAB_HOT:
				f = new HotFragment();
				break;
			case TAB_FAN:
				f = new HomeFanFragment();
				break;
			}
		}
		return f;
	}

}
