package com.bixin.bixin.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bixin.bixin.listeners.RecyclerViewOnItemClickListener;

import java.util.ArrayList;

/**
 * RecyclerView基础的适配器
 * 原则上Adapter都要继承此类,增加点击事件，空view显示等功能
 */
public abstract class RecyclerViewBaseAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	protected Context mContext;
	protected LayoutInflater mInflater;
	protected ArrayList<T> mArrayList = new ArrayList<>();
	protected RecyclerViewOnItemClickListener recyclerViewOnItemClickListener;
	protected View.OnClickListener onClickListener;

	public RecyclerViewBaseAdapter(Context context) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(mContext);
	}

	public void setRecyclerViewOnItemClickListener(RecyclerViewOnItemClickListener recyclerViewOnItemClickListener) {
		this.recyclerViewOnItemClickListener = recyclerViewOnItemClickListener;
	}

	public void setOnClickListener(View.OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}


	@Override
	public abstract RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);


	@Override
	public abstract void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

	@Override
	public int getItemCount() {
		return mArrayList.size();
	}

	@Override
	public int getItemViewType(int position) {
		return super.getItemViewType(position);
	}

	/**
	 * 清除绑定的数据，注意，此方法没有刷新UI
	 */
	public void clearData() {
		mArrayList.clear();
	}

	/**
	 * 添加数据，并自动刷新UI
	 *
	 * @param data
	 */
	public void addData(ArrayList<T> data) {
		if (data != null) {
			mArrayList.addAll(data);
			notifyDataSetChanged();
		}
	}

	/**
	 * 添加数据到第一项，并自动刷新
	 *
	 * @param item
	 */
	public void addFirstItem(T item) {
		if (item != null) {
			mArrayList.add(0, item);
			notifyDataSetChanged();
		}
	}

	/**
	 * @return 获取数据
	 */
	public ArrayList<T> getData() {
		return mArrayList;
	}

	/**
	 * 获取数据
	 *
	 * @param i
	 * @return
	 */
	public T getData(int i) {
		return mArrayList.get(i);
	}

	/**
	 * @return 数据是否为空
	 */
	public boolean isDataEmpty() {
		return mArrayList.isEmpty();
	}

}
