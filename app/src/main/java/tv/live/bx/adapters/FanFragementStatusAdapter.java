package tv.live.bx.adapters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import tv.live.bx.fragments.FanListFragment;
import tv.live.bx.library.util.EvtLog;

public class FanFragementStatusAdapter extends FragmentStatePagerAdapter {
	private String TAG = "CommutityFragementStatusAdapter";
	private List<Map<String, String>> mStatus = new ArrayList<Map<String, String>>();
	private Context mContext;

	public FanFragementStatusAdapter(Context mContext, FragmentManager fm) {
		super(fm);
		this.mContext = mContext;
	}

	/**
	 * 添加数据，并自动刷新UI
	 * @param data
	 */
	public void setDatas(List<Map<String, String>> data) {
		if (data != null) {
			mStatus.clear();
			mStatus.addAll(data);
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return mStatus.size();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		EvtLog.d(TAG, "instantiateItem position:" + position);
		FanListFragment fragment = (FanListFragment) super.instantiateItem(container, position);
		return fragment;
	}

	@Override
	public Fragment getItem(int position) {
		EvtLog.d(TAG, "getItem position:" + position);
		Fragment fragment = new FanListFragment();
		Bundle args = new Bundle();
		args.putString(FanListFragment.FAN_STATUS, mStatus.get(position).get("status"));
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mStatus.get(position).get("title");
	}
}
