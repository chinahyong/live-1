package com.bixin.bixin.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Live
 * @version 2016/6/20 2.5.0
 * @title PersonCenterAdapter.java
 */
public class PersonCenterAdapter extends MyBaseAdapter<String, Object> {

	public PersonCenterAdapter(Context poContext) {
		super(poContext);
	}


	@SuppressLint({"InflateParams", "ResourceAsColor"})
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		return convertView;
	}

}
