package tv.live.bx.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.activities.AnchorSearchActivity;
import tv.live.bx.activities.RankActivity;
import tv.live.bx.adapters.LiveFragmentStatusAdapter;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.config.AppConfig;
import tv.live.bx.listeners.GoHotClickListener;
import tv.live.bx.listeners.OnUpdateListener;
import com.umeng.analytics.MobclickAgent;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Title: AuthorFragment.java</br> Description: 主播Fragment</br> Copyright: *
 * Copyright (c) 2008</br
 *
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class LiveFragment extends BaseFragment implements OnClickListener {
	// 直播列表类型
	public static final int LIVE_STATUS_HOT = 1;
	public static final int LIVE_STATUS_NEW = 0;
	public static final int LIVE_STATUS_FOCUS = 10;
	public static final int LIVE_STATUS_RECOMMEND = 2;
	//	private final String FRAGMENT_TAG_FORMAT = "HomeFragment%s";
	public final static int TAB_HOT = 0; // 热门
	public final static int TAB_NEW = 1; // 最新
	public final static int TAB_NEAR = 2;// 附近
	public final static int TAB_RECOMMEND = 3; //推荐
	/**
	 * 顶部Bar TAB栏
	 */
	private TabPageIndicator mTabPagerIndicator;
	private ViewPager mViewPager;
	private LiveFragmentStatusAdapter mAdapter;
	private int mCurTabIndex = -1;

	/**
	 * 默认显示的tab标签
	 */
	private int mDefaultIndex = 0;

	/**
	 * 之前忘记使用这些方法了，这个类暂时不用了
	 */
	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_live_layout;
	}

	@Override
	protected void initMembers() {
		mViewPager = (ViewPager) mRootView.findViewById(R.id.live_frame_content_viewpager);
		mAdapter = new LiveFragmentStatusAdapter(getChildFragmentManager(), new ShowHotFragmentListener());
		List<Map<String, Object>> datas = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		map = new HashMap<>();
		map.put(LiveFragmentStatusAdapter.ID, LiveFragment.TAB_HOT);
		map.put(LiveFragmentStatusAdapter.TITLE, getString(R.string.anchor_tab_hot));
		datas.add(map);

		//是否显示推荐
		if (AppConfig.getInstance().showRecommend) {
			map = new HashMap<>();
			map.put(LiveFragmentStatusAdapter.ID, LiveFragment.TAB_RECOMMEND);
			map.put(LiveFragmentStatusAdapter.TITLE, "发现");
			datas.add(map);
		}

		map = new HashMap<>();
		map.put(LiveFragmentStatusAdapter.ID, LiveFragment.TAB_NEW);
		map.put(LiveFragmentStatusAdapter.TITLE, getString(R.string.anchor_tab_new));
		datas.add(map);

		map = new HashMap<>();
		map.put(LiveFragmentStatusAdapter.ID, LiveFragment.TAB_NEAR);
		map.put(LiveFragmentStatusAdapter.TITLE, getString(R.string.anchor_tab_near));
		datas.add(map);

		// 系统下发tag
		datas.addAll(AppConfig.getInstance().moderatorTags);
		mAdapter.setDatas(datas);
	}

	@Override
	protected void initWidgets() {
		mDefaultIndex = AppConfig.getInstance().liveDefaultTabIndex;

		mTabPagerIndicator = (TabPageIndicator) mRootView.findViewById(R.id.main_live_tabs);
		mViewPager.setAdapter(mAdapter);
		mTabPagerIndicator.setViewPager(mViewPager, mDefaultIndex);
		mTabPagerIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int i, float v, int i1) {

			}

			@Override
			public void onPageSelected(int i) {
				mCurTabIndex = i;
			}

			@Override
			public void onPageScrollStateChanged(int i) {

			}
		});
		mTabPagerIndicator.setOnTabReselectedListener(new TabPageIndicator.OnTabReselectedListener() {
			@Override
			public void onTabReselected(int position) {
				Map<String, Object> data = mAdapter.getData(position);
				String id = String.valueOf(data.get(LiveFragmentStatusAdapter.ID));
				int index = Integer.parseInt(id);
				if (TAB_NEW == index) {
					MobclickAgent.onEvent(FeizaoApp.mConctext, "clickNewButtonInIndex");
					OperationHelper.onEvent(FeizaoApp.mConctext, "clickNewButtonInIndex", null);
				} else if (TAB_HOT == index) {
					MobclickAgent.onEvent(FeizaoApp.mConctext, "clickFeatureButtonInIndex");
					OperationHelper.onEvent(FeizaoApp.mConctext, "clickFeatureButtonInIndex", null);
				} else if (TAB_RECOMMEND == index) {
					MobclickAgent.onEvent(FeizaoApp.mConctext, "clickRecommendButtonInIndex");
					OperationHelper.onEvent(FeizaoApp.mConctext, "clickRecommendButtonInIndex", null);
				} else if (TAB_NEAR == index) {
					MobclickAgent.onEvent(FeizaoApp.mConctext, "clickNearbyButtonInIndex");
					OperationHelper.onEvent(FeizaoApp.mConctext, "clickNearbyButtonInIndex", null);
				} else {
					Map<String, String> map = new HashMap<>();
					map.put("tabId", String.valueOf(data.get(LiveFragmentStatusAdapter.ID)));
					MobclickAgent.onEvent(FeizaoApp.mConctext, "clickClassificationButtonInIndex");
					OperationHelper.onEvent(FeizaoApp.mConctext, "clickClassificationButtonInIndex", map);
				}
				if (position == mCurTabIndex) {
					Fragment f = mAdapter.getFragment(mCurTabIndex);
					if (f instanceof OnUpdateListener) {
						// 回调Tab再次点击方法
						((OnUpdateListener) f).onTabClickAgain();
						return;
					}
				}
			}
		});
	}

	@Override
	protected void setEventsListeners() {
		//排行榜按钮
		mRootView.findViewById(R.id.live_bar_search_btn).setOnClickListener(this);
		//搜索按钮
		mRootView.findViewById(R.id.rechargeBtn).setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle bundle) {

	}

	/**
	 * TODO 简单描述该方法的实现功能（可选）.
	 *
	 * @see tv.live.bx.fragments.BaseFragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// 当应用被系统杀死后，重启避免多个fragment都显示出来，导致显示错乱，让fragment先隐藏
//		if (savedInstanceState != null) {
//			FragmentManager fm = getChildFragmentManager();
//			// Activity自动保存了Fragment的状态，此处要隐藏所有的Fragment，不然可能导致Fragment重叠
//			FragmentTransaction ft = fm.beginTransaction();
//			for (Fragment f : fm.getFragments()) {
//				ft.hide(f);
//			}
//			ft.commitAllowingStateLoss();
//		}
		// 默认显示
//		mTabPagerIndicator.setCurrentTab(TAB_HOT);
//		mCurTabIndex = TAB_HOT;
//		mTabPagerIndicator.setViewPager(mViewPager, TAB_HOT);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.live_bar_search_btn:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "searchOnIndex");
				ActivityJumpUtil.gotoActivity(mActivity, AnchorSearchActivity.class, false, null, null);
				break;
			// 排行榜
			case R.id.rechargeBtn:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "searchOnIndex");
				ActivityJumpUtil.gotoActivity(mActivity, RankActivity.class, false, null, null);
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/* 在这里，我们通过碎片管理器中的Tag，就是每个碎片的名称，来获取对应的fragment */
		Fragment f = mAdapter.getFragment(mCurTabIndex);
		/* 然后在碎片中调用重写的onActivityResult方法 */
		if (f != null) {
			f.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onTabClickAgain() {
		super.onTabClickAgain();
		Fragment f = mAdapter.getFragment(mCurTabIndex);
		if (f instanceof OnUpdateListener) {
			// 回调Tab再次点击方法
			((OnUpdateListener) f).onTabClickAgain();
		}
	}

	class ShowHotFragmentListener implements GoHotClickListener {

		@Override
		public void onGoHotClick() {
			// 默认热门
			mTabPagerIndicator.setCurrentItem(1);
		}
	}
}
