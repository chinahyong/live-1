package com.bixin.bixin.live.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;

import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.live.bx.R;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.adapters.LiveRankFragmentPagerAdapter;

/**
 * LiveRankActivity.java Description:直播间泡泡排行榜
 *
 * @author Live
 * @version 2.2 2016.4.9
 */
public class LiveRankActivity extends BaseFragmentActivity {
	private ViewPager mViewPager;
	private LiveRankFragmentPagerAdapter mFragmentPagerAdapter;
	private List<Map<String, String>> mapArrayList = new ArrayList<>();
	private TabPageIndicator mTabPageIndicator;
	private String mRid;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_playing_rank;
	}

	@Override
	public void initWidgets() {
		initTitle();
		mViewPager = (ViewPager) findViewById(R.id.viewPage);
		// 实例化TabPageIndicator然后设置ViewPager与之关联
		mTabPageIndicator = (TabPageIndicator) findViewById(R.id.indicator);
		mFragmentPagerAdapter = new LiveRankFragmentPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mFragmentPagerAdapter);
		mTabPageIndicator.setViewPager(mViewPager);
		mTabPageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	@Override
	protected void setEventsListeners() {

	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mRid = bundle.getString("rid");
			Map<String, String> weekMap = new HashMap<>();
			weekMap.put("rid", mRid);
			weekMap.put("rankTypeName", getResources().getString(R.string.rank_weekP));
			weekMap.put("rankType", "week");

			Map<String, String> totalMap = new HashMap<>();
			totalMap.put("rid", mRid);
			totalMap.put("rankTypeName", getResources().getString(R.string.rank_totalP));
			totalMap.put("rankType", "all");
			mapArrayList.add(weekMap);
			mapArrayList.add(totalMap);
			mFragmentPagerAdapter.setDatas(mapArrayList);
			mTabPageIndicator.notifyDataSetChanged();
			mViewPager.setCurrentItem(1);
		}
	}

	@Override
	protected void initTitle() {
		super.initTitle();
		initTitleData();
	}

	@Override
	protected void initTitleData() {
		mTopBackLayout.setOnClickListener(new OnBack());
		mTopTitleTv.setText(getString(R.string.live_pao_rank));
	}


	private class OnBack implements OnClickListener {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	}


}
