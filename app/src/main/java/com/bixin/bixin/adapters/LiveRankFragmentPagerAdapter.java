package com.bixin.bixin.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.bixin.bixin.fragments.LiveRankFragment;
import com.bixin.bixin.library.util.EvtLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LiveRankFragmentPagerAdapter extends FragmentPagerAdapter {
	private String TAG = "LiveRankFragmentPagerAdapter";
	private List<Map<String, String>> mapArrayList = new ArrayList<Map<String, String>>();

	public LiveRankFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	/**
	 * 添加数据，并自动刷新UI
	 *
	 * @param data
	 */
	public void setDatas(List<Map<String, String>> data) {
		if (data != null) {
			mapArrayList.clear();
			mapArrayList.addAll(data);
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return mapArrayList.size();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		EvtLog.d(TAG, "instantiateItem position:" + position);
		return super.instantiateItem(container, position);
	}

	@Override
	public Fragment getItem(int position) {
		EvtLog.d(TAG, "getItem position:" + position);
		Fragment fragment = new LiveRankFragment();
		Bundle args = new Bundle();
		args.putString(LiveRankFragment.ROOM_ID, mapArrayList.get(position).get("rid"));
		args.putString(LiveRankFragment.RANK_TYPE, mapArrayList.get(position).get("rankType"));
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mapArrayList.get(position).get("rankTypeName");
	}
}
