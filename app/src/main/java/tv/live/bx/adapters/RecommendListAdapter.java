package tv.live.bx.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.StringUtil;

public class RecommendListAdapter extends BaseAdapter {

	private Context moContext;
	private List<Map<String, Object>> mData;
	private Map<Integer, String> checkFlagMap = new HashMap<>();
	private boolean isShowType = true;

	public RecommendListAdapter(Context poContext) {
		moContext = poContext;
		mData = new ArrayList<>();
	}

	public Map<Integer, String> getCheckedMap() {
		return checkFlagMap;
	}

	public void setIsShowType(boolean isShowType) {
		this.isShowType = isShowType;
	}

	/**
	 * 清除绑定的数据，注意，此方法没有刷新UI
	 */
	public void clearData() {
		mData.clear();
	}

	/**
	 * 添加数据，并自动刷新UI
	 *
	 * @param data
	 */
	public void addData(List<Map<String, Object>> data) {
		if (data != null) {
			mData.addAll(data);
			for (int i = 0; i < data.size(); i++) {
				checkFlagMap.put(i, String.valueOf(data.get(i).get("mid")));
			}
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
			mData.add(0, item);
			notifyDataSetChanged();
		}
	}

	@Override
	public boolean hasStableIds() {
		return super.hasStableIds();
	}

	/**
	 * @return 获取数据
	 */
	public List<Map<String, Object>> getData() {
		return mData;
	}


	/**
	 * @return 数据是否为空
	 */
	public boolean isDataEmpty() {
		return mData.isEmpty();
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder loHolder;
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(moContext);
			convertView = loInflater.inflate(R.layout.item_recommend_focus, null);

			loHolder = new Holder();
			loHolder.mUserHead = (ImageView) convertView.findViewById(R.id.item_recommend_head);
			loHolder.mUserHeadV = (ImageView) convertView.findViewById(R.id.item_recommend_head_v);
			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_recommend_nickname);
			loHolder.mTvTitle = (TextView) convertView.findViewById(R.id.item_recommend_title);
			loHolder.moCkChoose = (ToggleButton) convertView.findViewById(R.id.item_recommend_focus_check);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}

		@SuppressWarnings("unchecked")
		final Map<String, Object> lmRoom = mData.get(position);
		ImageLoaderUtil.getInstance().loadHeadPic(moContext, loHolder.mUserHead, (String) lmRoom.get("headPic"));
		loHolder.moTvNickname.setText((String) lmRoom.get("nickname"));
		loHolder.mUserHeadV.setVisibility(Utils.getBooleanFlag(lmRoom.get("verified").toString())
				? View.VISIBLE
				: View.GONE);
		// 使用map保存ToggleButton选中状态，用于避免view复用造成的错乱
		loHolder.moCkChoose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					MobclickAgent.onEvent(FeizaoApp.mContext,"clickChooseFollowInRecommendBroadcasterPage");
					checkFlagMap.put(position, String.valueOf(lmRoom.get("mid")));
				} else {
					MobclickAgent.onEvent(FeizaoApp.mContext,"clickCancelFollowInRecommendBroadcasterPage");
					checkFlagMap.remove(position);
				}
			}
		});
		if (checkFlagMap.containsKey(position)) {
			loHolder.moCkChoose.setChecked(true);
		} else {
			loHolder.moCkChoose.setChecked(false);
		}
		/*
		 * 标题增加话题设置高亮
		 * @version 2.5.0
		 */
		loHolder.mTvTitle.setText(StringUtil.setHighLigntText(moContext, lmRoom.get("announcement").toString()));
		return convertView;
	}

	class Holder {
		private ImageView mUserHead, mUserHeadV;
		private TextView moTvNickname, mTvTitle;
		private ToggleButton moCkChoose;
	}

	/**
	 * 是否关注的图标点击事件
	 */
	public interface OnCheckClickListener {
		void onCheckClick(boolean v, int position);
	}
}
