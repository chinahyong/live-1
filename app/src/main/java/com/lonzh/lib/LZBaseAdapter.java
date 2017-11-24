package com.lonzh.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class LZBaseAdapter<T extends Map<?, ?>> extends BaseAdapter {

	protected List<T> mlData = new ArrayList<T>();

	/**
	 * 设置数据
	 * 
	 * @param mlData
	 */
	public void setData(List<T> plData) {
		mlData.clear();
		insertData(0, plData);
	}

	/**
	 * 插入数据
	 * 
	 * @param piIndex
	 * @param plData
	 */
	public void insertData(int piIndex, List<T> plData) {
		mlData.addAll(piIndex, plData);
		notifyDataSetChanged();
	}

	/**
	 * 插入数据
	 * 
	 * @param piIndex
	 * @param pmData
	 */
	public void insertData(int piIndex, T pmData) {
		mlData.add(piIndex, pmData);
		notifyDataSetChanged();
	}

	/**
	 * 删除数据
	 * 
	 * @param piIndex
	 */
	public void removeData(int piIndex) {
		mlData.remove(piIndex);
		notifyDataSetChanged();
	}

	/**
	 * 删除数据
	 * 
	 * @param psKey
	 * @param psValue
	 */
	public void removeData(String psKey, Object poValue) {
		List<T> llData = new ArrayList<T>();
		for (T lmItem : mlData)
			if (lmItem.get(psKey) != null && lmItem.get(psKey).equals(poValue)) {
				llData.add(lmItem);
			}
		if (llData.size() > 0) {
			for (T lmItem : llData)
				mlData.remove(lmItem);
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return mlData.size();
	}

	@Override
	public Object getItem(int position) {
		return mlData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);

}
