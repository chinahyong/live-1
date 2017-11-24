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
import android.widget.RelativeLayout;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.activities.GroupPostPublishActivity;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.listeners.OnUpdateListener;
import tv.live.bx.ui.SingleTabWidget;
import tv.live.bx.ui.SingleTabWidget.OnTabChangedListener;
import com.umeng.analytics.MobclickAgent;

/**
 * 首页页面 Title: HomeFragment.java
 * @version 1.0
 * @CreateDate 2014-8-13
 */
@SuppressLint({"NewApi", "ResourceAsColor"})
public class HomeFragment extends BaseFragment implements OnClickListener {
	private final String FRAGMENT_TAG_FORMAT = "HomeFragment%s";
	public static final int TAB_HOT = 0; // 精选
	public static final int TAB_FAN = 1; // 饭圈

	/** 底部TAB栏 */
	private SingleTabWidget tabWidget;
	private OnTabChangedListener mOnTabChangedListener;
	/**
	 * 当前显示的Fragment
	 */
	private int mTabIndex = -1;

	/** 搜索按钮 */
	private RelativeLayout mSearchBtn;
	/** 发帖按钮 */
	private RelativeLayout mPostingBtn;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
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

	/**
	 * TODO 简单描述该方法的实现功能（可选）.
	 * @see tv.live.bx.fragments.BaseFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// 当应用被系统杀死后，重启避免多个fragment都显示出来，导致显示错乱，让fragment先隐藏
		if (savedInstanceState != null) {
			FragmentManager fm = getChildFragmentManager();
			// Activity自动保存了Fragment的状态，此处要隐藏所有的Fragment，不然可能导致Fragment重叠
			FragmentTransaction ft = fm.beginTransaction();
			for (Fragment f : fm.getFragments()) {
				ft.hide(f);
			}
			ft.commitAllowingStateLoss();
		}
		// 默认显示
		tabWidget.setCurrentTab(TAB_HOT);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 发帖
		case R.id.commnity_bar_right_posting:
			MobclickAgent.onEvent(FeizaoApp.mConctext, "PostInCommunity");
			ActivityJumpUtil.gotoActivityForResult(mActivity, GroupPostPublishActivity.class,
					GroupPostPublishActivity.REQUEST_CODE_PUBLIC_FRAGMENT, null, null);
			break;
		// 搜索
		// case R.id.rechargeBtn:
		// MobclickAgent.onEvent(FeizaoApp.mConctext, "searchOnIndex");
		// ActivityJumpUtil.gotoActivity(mActivity, AnchorSearchActivity.class,
		// false, null, null);
		// break;
		default:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/* 在这里，我们通过碎片管理器中的Tag，就是每个碎片的名称，来获取对应的fragment */
		Fragment f = getChildFragmentManager().findFragmentByTag(getFragmentTag(mTabIndex));
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
		// mSearchBtn = (RelativeLayout)
		// mRootView.findViewById(R.id.rechargeBtn);
		// mSearchBtn.setOnClickListener(this);
		mPostingBtn = (RelativeLayout) mRootView.findViewById(R.id.commnity_bar_right_posting);
		mPostingBtn.setOnClickListener(this);
		// 初始化底部TabWidget
		tabWidget = (SingleTabWidget) mRootView.findViewById(R.id.main_tabs);

		// 添加四个Tab
		tabWidget.addTab(R.layout.tab_home_layout, 0, getResources().getString(R.string.home_hot), TAB_HOT);
		tabWidget.addTab(R.layout.tab_home_layout, 0, getResources().getString(R.string.commutity_tab_fan), TAB_FAN);
		final FragmentManager fm = getChildFragmentManager();
		mOnTabChangedListener = new OnTabChangedListener() {
			@Override
			public void onTabChanged(int tabIndex) {
				if (tabIndex == mTabIndex) {
					Fragment f = getFragmentByIndex(tabIndex, fm);
					if (f instanceof OnUpdateListener) {
						// 回调Tab再次点击方法
						((OnUpdateListener) f).onTabClickAgain();
					}
					return;
				}
				// 当前点击与展示index相同，不做统计，故將次放到後面
				switch (tabIndex) {
					case TAB_HOT:
						MobclickAgent.onEvent(FeizaoApp.mConctext, "switchChoiceness");
						break;
					case TAB_FAN:
						MobclickAgent.onEvent(FeizaoApp.mConctext, "switchFanCircle");
						break;
				}
				switchFragment(tabIndex);
				mTabIndex = tabIndex;
			}
		};

		// 设置监听
		tabWidget.setOnTabChangedListener(mOnTabChangedListener);
	}

	/**
	 * 初始化日历展示的Fragment
	 */
	private void switchFragment(int tabIndex) {
		final FragmentManager manager = getChildFragmentManager();
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
				f = new HomeHotFragment();
				break;
			case TAB_FAN:
				f = new HomeFanFragment();
				break;
			}
		}
		return f;
	}

}
