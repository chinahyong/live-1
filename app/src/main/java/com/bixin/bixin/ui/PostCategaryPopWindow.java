package com.bixin.bixin.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.live.bx.R;

/**
 * 社区帖子分类
 */
public class PostCategaryPopWindow extends PopupWindow {
	private View conentView;
	private GridView mGridView;
	private BaseAdapter adapter;
	private Context mContext;
	private static List<Map<String, String>> mList = new ArrayList<Map<String, String>>();
	private OnItemClickListener itemClickListener;

	public PostCategaryPopWindow(Activity context, OnItemClickListener itemClickListener,
			List<Map<String, String>> mList) {
		super(context);
		mContext = context;
		PostCategaryPopWindow.mList = mList;
		this.itemClickListener = itemClickListener;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		conentView = inflater.inflate(R.layout.pop_post_category_layout, null);
		// 设置SelectPicPopupWindow的View
		this.setContentView(conentView);
		// 设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(LayoutParams.MATCH_PARENT);
		this.setHeight(LayoutParams.MATCH_PARENT);
		this.setBackgroundDrawable(new ColorDrawable(0x00000000));

		mGridView = (GridView) conentView.findViewById(R.id.gridView);
		adapter = new MGridViewAdapter();
		mGridView.setAdapter(adapter);
		mGridView.setFocusableInTouchMode(true);
		mGridView.setFocusable(true);
		if (itemClickListener != null)
			mGridView.setOnItemClickListener(itemClickListener);
		this.setAnimationStyle(R.style.Popup_Animation_Bellow_UpDown);
		this.setFocusable(true);
		this.setTouchable(true);
		this.setBackgroundDrawable(new ColorDrawable(0x00000000));
		this.setOutsideTouchable(true);
	}

	/**
	 * 显示某View上方
	 */
	public void showPopUp(View v) {
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		this.showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1]);
		this.setFocusable(true);
		this.setTouchable(true);
		this.setBackgroundDrawable(new ColorDrawable(0x00000000));
		this.setOutsideTouchable(true);
		// 刷新状态
		this.update();
	}

	class MGridViewAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {

			// TODO Auto-generated method stub
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {

			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder loHolder;
			if (convertView == null) {
				LayoutInflater loInflater = LayoutInflater.from(mContext);
				convertView = loInflater.inflate(R.layout.pop_post_category_item, null);

				loHolder = new Holder();
				loHolder.categoryIcon = (ImageView) convertView.findViewById(R.id.category_image);
				loHolder.categoryName = (TextView) convertView.findViewById(R.id.category_name);
				convertView.setTag(loHolder);
			} else {
				loHolder = (Holder) convertView.getTag();
			}
			loHolder.categoryName.setText(mList.get(position).get("title"));
			// ImageLoader.getInstance().displayImage(mList.get(position).get("icon"),
			// loHolder.categoryIcon);
			return convertView;
		}

		class Holder {
			ImageView categoryIcon;
			TextView categoryName;
		}

	}
}
