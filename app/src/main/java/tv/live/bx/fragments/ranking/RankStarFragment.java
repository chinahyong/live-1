package tv.live.bx.fragments.ranking;


import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.viewpagerindicator.UnderlinePageIndicator;

import java.util.HashMap;
import java.util.Map;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.RankActivity;
import tv.live.bx.adapters.RankListAdapter;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.fragments.BaseFragment;
import tv.live.bx.model.RankBean;
import tv.live.bx.util.ActivityJumpUtil;

/**
 * 明星榜单页面
 */
public class RankStarFragment extends BaseFragment implements RankActivity.IUpdateData, View.OnClickListener {
	//子分类容器
	private TextView btnDay, btnWeek, btnTotal;

	private TextView mRankInstruction;
	private ViewPager viewPager;

	private UnderlinePageIndicator mIndicator;
	//榜单数据
	private RankBean rankData;

	//当前选中项
	private int mCurrentIndex = -1;
	private TextView mCurrentBtn;

	//保存的状态
	private Bundle saveState;

	@Override
	protected int getLayoutRes() {
		return R.layout.a_common_rank_layout;
	}

	@Override
	protected void initData(Bundle bundle) {

	}

	@Override
	protected void initMembers() {
		mRankInstruction = (TextView) mRootView.findViewById(R.id.rank_instruction);
		viewPager = (ViewPager) mRootView.findViewById(R.id.rank_viewPager);
		btnDay = (TextView) mRootView.findViewById(R.id.rank_day_btn);
		btnWeek = (TextView) mRootView.findViewById(R.id.rank_week_btn);
		btnTotal = (TextView) mRootView.findViewById(R.id.rank_total_btn);
		mIndicator = (UnderlinePageIndicator) mRootView.findViewById(R.id.indicator);
	}

	@Override
	protected void initWidgets() {
		viewPager.setAdapter(adapter);
		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				changeChooseIndex(position);
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
		if(saveState != null && saveState.containsKey("index")){
			int index = saveState.getInt("index");
			changeChooseIndex(index);
			viewPager.setCurrentItem(index);
			saveState = null;
		}
		else if (mCurrentIndex == -1) {
			changeChooseIndex(0);
			viewPager.setCurrentItem(0);
		} else {
			changeChooseIndex(mCurrentIndex);
			viewPager.setCurrentItem(mCurrentIndex);
		}

		mIndicator.setViewPager(viewPager);
		mIndicator.setFades(false);
	}

	/**
	 * 改变选中
	 *
	 * @param position
	 */
	private void changeChooseIndex(int position) {
		if (position == mCurrentIndex)
			return;
		mCurrentIndex = position;
		if (mCurrentBtn != null) {
			mCurrentBtn.setSelected(false);
			mCurrentBtn.getPaint().setFakeBoldText(false);
		}
		switch (position) {
			case 0:
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickAnchorIconInDayRankingListOfStar", null);
				mRankInstruction.setText(R.string.rank_star_day_explain);
				mCurrentBtn = btnDay;
				break;
			case 1:
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickAnchorIconInWeekRankingListOfStar", null);
				mRankInstruction.setText(R.string.rank_star_week_explain);
				mCurrentBtn = btnWeek;
				break;
			case 2:
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickAnchorIconInAllRankingListOfStar", null);
				mRankInstruction.setText(R.string.rank_star_total_explain);
				mCurrentBtn = btnTotal;
				break;
		}
		mCurrentBtn.setSelected(true);
		mCurrentBtn.getPaint().setFakeBoldText(true);

	}

	@Override
	protected void setEventsListeners() {
		btnDay.setOnClickListener(this);
		btnWeek.setOnClickListener(this);
		btnTotal.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (Utils.isFastDoubleClick())
			return;
		switch (v.getId()) {
			case R.id.rank_day_btn:
				viewPager.setCurrentItem(0);
				break;
			case R.id.rank_week_btn:
				viewPager.setCurrentItem(1);
				break;
			case R.id.rank_total_btn:
				viewPager.setCurrentItem(2);
				break;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		viewPager = null;
		mRankInstruction = btnDay = btnWeek = btnTotal = null;
		mIndicator = null;
		mCurrentBtn = null;
		saveState = new Bundle();
		saveState.putInt("index", mCurrentIndex);
		mCurrentIndex=-1;
	}

	@Override
	public void update(RankBean bean) {
		rankData = bean;
		adapter.notifyDataSetChanged();
	}

	private PagerAdapter adapter = new PagerAdapter() {
		@Override
		public int getCount() {
			return rankData == null ? 0 : 3;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			ListView listView = (ListView) LayoutInflater.from(getActivity()).inflate(R.layout.a_common_rank_list, container, false);
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int childPosition, long id) {
					RankBean.UserBean bean;
					Map<String, String> lmItem = new HashMap<>();
					switch (position) {
						case 0:
							bean = rankData.last.get(childPosition);
							lmItem.put("id", TextUtils.isEmpty(bean.mid) ? String.valueOf(bean.uid) : String.valueOf(bean.mid));
							break;
						case 1:
							bean = rankData.week.get(childPosition);
							lmItem.put("id", TextUtils.isEmpty(bean.mid) ? String.valueOf(bean.uid) : String.valueOf(bean.mid));
							break;
						case 2:
							bean = rankData.all.get(childPosition);
							lmItem.put("id", TextUtils.isEmpty(bean.mid) ? String.valueOf(bean.uid) : String.valueOf(bean.mid));
							break;
					}
					ActivityJumpUtil.toPersonInfoActivity(mActivity, lmItem, -1);
				}
			});
			RankListAdapter adapter = new RankListAdapter(getActivity(), RankListAdapter.RANK_STAR);
			switch (position) {
				case 0:
					adapter.setData(rankData.last);
					break;
				case 1:
					adapter.setData(rankData.week);
					break;
				case 2:
					adapter.setData(rankData.all);
					break;
			}
			listView.setAdapter(adapter);
			container.addView(listView);
			return listView;
		}
	};
}
