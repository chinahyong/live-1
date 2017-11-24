package tv.live.bx.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.efeizao.bx.R;
import tv.live.bx.library.util.EvtLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * title:最新主播列表适配器 ClassName: LiveNewListAdapter <br/>
 *
 * @author Live
 * @version 2.4.0 2016.4.26
 */
public class LiveTagListAdapter extends BaseAdapter {
	public static final String ID = "id";
	public static final String TITLE = "name";
	private Context moContext;
	private List<Map<String, Object>> mDatas;
	private int selectIndex = -1;        //默认未选中
	private String intentSelectId = "";

	private OnCheckChangeListener mOnCheckChangeListener;

	public LiveTagListAdapter(Context poContext) {
		moContext = poContext;
		mDatas = new ArrayList<>();
	}

	public void setSelectIndex(int selectIndex) {
		// 当前选中 == 点击的，取消选中
		this.selectIndex = selectIndex;
	}

	public int getSelectIndex() {
		return selectIndex;
	}

	public void setIntentSelectId(String intentSelectId) {
		this.intentSelectId = intentSelectId;
	}

	/**
	 * 清除绑定的数据，注意，此方法没有刷新UI
	 */
	public void clearData() {
		mDatas.clear();
	}

	/**
	 * 添加数据，并自动刷新UI
	 *
	 * @param data
	 */
	public void addData(List<? extends Map<String, Object>> data) {
		if (data != null) {
			mDatas.addAll(data);
			notifyDataSetChanged();
		}
	}

	/**
	 * 添加数据到第一项，并自动刷新
	 *
	 * @param item
	 */
	public void addFirstItem(Map<String, Object> item) {
		if (item != null) {
			mDatas.add(0, item);
			notifyDataSetChanged();
		}
	}

	/**
	 * @return 获取数据
	 */
	public List<Map<String, Object>> getData() {
		return mDatas;
	}

	/**
	 * @return 数据是否为空
	 */
	public boolean isDataEmpty() {
		return mDatas.isEmpty();
	}

	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint({"InflateParams", "NewApi"})
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		EvtLog.e("", "getView " + position);
		final Holder loHolder;
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(moContext);
			convertView = loInflater.inflate(R.layout.item_live_tag, null);
			loHolder = new Holder();
			loHolder.mCheckTag = (CheckBox) convertView.findViewById(R.id.live_tag_item_name);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		Map<String, Object> data = mDatas.get(position);
		// 如果当前tag的id等于intent传入的id，则选中当前
		if (data.get(ID).equals(intentSelectId)) {
			selectIndex = position;
			intentSelectId = "";
		}
		loHolder.mCheckTag.setText((CharSequence) data.get("name"));
		// 如果当前下标等于选中下标，则设置为选中，否则为不选中
		if (selectIndex == position) {
			loHolder.mCheckTag.setChecked(true);
		} else {
			loHolder.mCheckTag.setChecked(false);
		}
		loHolder.mCheckTag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (mOnCheckChangeListener != null) {
					mOnCheckChangeListener.onCheckChange(buttonView);
				}
			}
		});
		EvtLog.e("LiveTagActivity", "getView" + selectIndex + "      " + position);
		return convertView;
	}

	public void setOnCheckChangeListener(OnCheckChangeListener mOnCheckChangeListener) {
		this.mOnCheckChangeListener = mOnCheckChangeListener;
	}

	class Holder {
		private CheckBox mCheckTag;
	}

	public interface OnCheckChangeListener {
		void onCheckChange(View v);
	}
}
