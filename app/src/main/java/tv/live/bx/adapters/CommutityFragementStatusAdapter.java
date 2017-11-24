package tv.live.bx.adapters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import tv.live.bx.fragments.GroupSubjectFragment;
import tv.live.bx.fragments.SubjectFragment;
import tv.live.bx.library.util.EvtLog;

public class CommutityFragementStatusAdapter extends FragmentStatePagerAdapter {
	private String TAG = "CommutityFragementStatusAdapter";
	private List<Map<String, String>> mPostMoudles = new ArrayList<Map<String, String>>();
	private Context mContext;

	public CommutityFragementStatusAdapter(Context mContext, FragmentManager fm) {
		super(fm);
		this.mContext = mContext;
	}

	/**
	 * 添加数据，并自动刷新UI
	 * @param data
	 */
	public void setDatas(List<Map<String, String>> data) {
		if (data != null) {
			mPostMoudles.clear();
			mPostMoudles.addAll(data);
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return mPostMoudles.size();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		EvtLog.d(TAG, "instantiateItem position:" + position);
		if (GroupSubjectFragment.GROUP_FORUM.equals(mPostMoudles.get(position).get("type"))) {
			GroupSubjectFragment fragment = (GroupSubjectFragment) super.instantiateItem(container, position);
			return fragment;
		} else {
			SubjectFragment fragment = (SubjectFragment) super.instantiateItem(container, position);
			return fragment;
		}

	}

	@Override
	public Fragment getItem(int position) {
		EvtLog.d(TAG, "getItem position:" + position);
		if (GroupSubjectFragment.GROUP_FORUM.equals(mPostMoudles.get(position).get("type"))) {
			Fragment fragment = new GroupSubjectFragment();
			Bundle args = new Bundle();
			args.putSerializable(GroupSubjectFragment.POST_MOUDLE, (Serializable) mPostMoudles.get(position));
			fragment.setArguments(args);
			return fragment;
		} else {
			Fragment fragment = new SubjectFragment();
			Bundle args = new Bundle();
			args.putSerializable(SubjectFragment.POST_MOUDLE, (Serializable) mPostMoudles.get(position));
			fragment.setArguments(args);
			return fragment;
		}
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return mPostMoudles.get(position).get("title");
	}
}
