package com.bixin.bixin.adapters;

import java.util.List;

import android.view.View;

import com.viewpagerindicator.IconPagerAdapter;

/**
 * ViewPage页面适配器 ClassName: GiftsBottomAdapter <br/>
 * @author Administrator
 * @version
 * @since JDK 1.6
 */
public class IconPageAdapter<T extends View> extends BasePageAdapter<T> implements IconPagerAdapter {

	private int mIconResId;

	public IconPageAdapter(List<T> mListViews, int resId) {
		super(mListViews);
		this.mIconResId = resId;
	}

	@Override
	public int getIconResId(int index) {
		return mIconResId;
	}


}
