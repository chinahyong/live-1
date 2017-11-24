package tv.live.bx.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import tv.live.bx.fragments.LiveFragment;
import tv.live.bx.fragments.LiveHotFragment;
import tv.live.bx.fragments.LiveNearFragment;
import tv.live.bx.fragments.LiveNewFragment;
import tv.live.bx.fragments.LiveRecommendFragment;
import tv.live.bx.fragments.LiveTagFragment;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.listeners.GoHotClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveFragmentStatusAdapter extends FragmentStatePagerAdapter {
	private static String TAG = LiveFragmentStatusAdapter.class.getSimpleName();
	public static final String ID = "id";
	public static final String TITLE = "name";
	private List<Map<String, Object>> mDatas = new ArrayList<>();
	private Map<Integer, Fragment> mPageReferenceMap = new HashMap<>();
	private GoHotClickListener mGoHotClickListener;

	public LiveFragmentStatusAdapter(FragmentManager fm, GoHotClickListener listener) {
		super(fm);
		this.mGoHotClickListener = listener;
	}

	/**
	 * 添加数据，并自动刷新UI
	 *
	 * @param data
	 */
	public void setDatas(List<Map<String, Object>> data) {
		if (data != null) {
			mDatas.clear();
			mDatas.addAll(data);
			notifyDataSetChanged();
		}
	}

	public Map<String, Object> getData(int position) {
		return mDatas.get(position);
	}

	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public Fragment getItem(int position) {
		EvtLog.d(TAG, "getItem position:" + position);
		Fragment fragment;
		if ((mDatas.get(position).get(ID).equals(LiveFragment.TAB_NEW))) {
			fragment = new LiveNewFragment();
			((LiveNewFragment) fragment).setGoHotClickListener(mGoHotClickListener);
		} else if ((mDatas.get(position).get(ID).equals(LiveFragment.TAB_HOT))) {
			fragment = new LiveHotFragment();
		} else if (mDatas.get(position).get(ID).equals(LiveFragment.TAB_RECOMMEND)) {
			fragment = new LiveRecommendFragment();
		} else if ((mDatas.get(position).get(ID).equals(LiveFragment.TAB_NEAR))) {
			fragment = new LiveNearFragment();
		} else {
			fragment = new LiveTagFragment();
			((LiveTagFragment) fragment).setGoHotClickListener(mGoHotClickListener);
			Bundle args = new Bundle();
			args.putString(ID, String.valueOf(mDatas.get(position).get(ID)));
			fragment.setArguments(args);
		}
		mPageReferenceMap.put(position, fragment);
		return fragment;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
		mPageReferenceMap.remove(position);
	}

	public Fragment getFragment(int key) {
		return mPageReferenceMap.get(key);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return (CharSequence) mDatas.get(position).get(TITLE);
	}

}
