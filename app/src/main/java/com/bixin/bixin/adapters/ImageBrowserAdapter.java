package com.bixin.bixin.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.bixin.bixin.fragments.ImagePagerFragment;
import com.bixin.bixin.library.util.EvtLog;

public class ImageBrowserAdapter extends FragmentStatePagerAdapter {
	private String TAG = "ImageBrowserAdapter";
	private List<String> mPhotos = new ArrayList<String>();
	private Context mContext;

	public ImageBrowserAdapter(Context mContext, List<String> photos, FragmentManager fm) {
		super(fm);
		this.mContext = mContext;
		if (photos != null) {
			mPhotos = photos;
		}
	}

	@Override
	public int getCount() {
		return mPhotos.size();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		EvtLog.d(TAG, "instantiateItem position:" + position);
		ImagePagerFragment fragment = (ImagePagerFragment) super.instantiateItem(container, position);
		fragment.setUri(mPhotos.get(position));
		return fragment;
	}

	@Override
	public Fragment getItem(int arg0) {
		EvtLog.d(TAG, "getItem position:" + arg0);
		ImagePagerFragment fragment = new ImagePagerFragment();
		Bundle bundle = new Bundle();
		bundle.putString(ImagePagerFragment.IMAGE_URL, mPhotos.get(arg0));
		fragment.setArguments(bundle);
		return fragment;
	}
}
