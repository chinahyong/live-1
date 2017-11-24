package tv.live.bx.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基础的适配器 <br/>
 */
public class MyBaseAdapter<K, V> extends BaseAdapter {

	protected Context mContext;
	protected List<Map<K, V>> mListDatas;
	/**
	 * 是否显示listview head
	 */
	protected boolean mIsShowHead = false;

	public MyBaseAdapter(Context poContext) {
		mContext = poContext;
		mListDatas = new ArrayList<Map<K, V>>();
	}

	public void setIsShowHead(boolean isShowHead) {
		this.mIsShowHead = isShowHead;
	}

	/**
	 * 清除绑定的数据，注意，此方法没有刷新UI
	 */
	public void clearData() {
		mListDatas.clear();
	}

	/**
	 * 添加数据，并自动刷新UI
	 *
	 * @param data
	 */
	public void addData(List<Map<K, V>> data) {
		if (data != null) {
			mListDatas.addAll(data);
			notifyDataSetChanged();
		}
	}

	/**
	 * 添加数据到第一项，并自动刷新
	 *
	 * @param item
	 */
	public void addFirstItem(Map<K, V> item) {
		if (item != null) {
			mListDatas.add(0, item);
			notifyDataSetChanged();
		}
	}

	/**
	 * @return 获取数据
	 */
	public List<Map<K, V>> getData() {
		return mListDatas;
	}

	/**
	 * @return 数据是否为空
	 */
	public boolean isDataEmpty() {
		return mListDatas.isEmpty();
	}

	@Override
	public int getCount() {
		if (mIsShowHead) {
			// 为了显示头部布局
			if (mListDatas.size() == 0) {
				return -1;
			}
		}
		return mListDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mListDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		return null;
	}
}
