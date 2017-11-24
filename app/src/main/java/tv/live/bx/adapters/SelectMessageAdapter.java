package tv.live.bx.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.efeizao.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Consts;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by valar on 2017/3/29.
 * detail 描述的是点击进入群发页面的list的适配器
 */

public class SelectMessageAdapter extends MyBaseAdapter {

	private OnItemClickListener mOnItemClickListener;
	private Map<Integer, Boolean> selectedAllMap = new HashMap<>();// 点击全选的记录的map
	private List<Map<String, String>> selectedInfo = new ArrayList<>(); //点击全选记录的条目
	private List<Map<String, String>> mSelectReceiverListInfo;
	private int mleftMessageNums;

	public SelectMessageAdapter(Context poContext) {
		super(poContext);
		mSelectReceiverListInfo = new ArrayList<>();
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mOnItemClickListener = listener;
	}

	@Override
	public int getCount() {
		return mSelectReceiverListInfo.size();
	}


	/**
	 * 添加数据并且刷新
	 */
	@Override
	public void addData(List data) {
		if (data != null) {
			mSelectReceiverListInfo.addAll(data);
			notifyDataSetChanged();
		}
	}

	public Map<Integer, Boolean> getselectedAllMap() {
		return selectedAllMap;
	}

	public List<Map<String, String>> getSelectedAllList() {
		selectedInfo.clear();
		if (mSelectReceiverListInfo.size() != 0) {
			for (int i = 0; i < mSelectReceiverListInfo.size(); i++) {
				if (Utils.getBooleanFlag(selectedAllMap.get(i))) {
					selectedInfo.add(mSelectReceiverListInfo.get(i));
				}
			}
		}
		return selectedInfo;
	}

	public void getleftMessageNums(int leftMessageNums) {
		mleftMessageNums = leftMessageNums;
	}

	@Override
	public void clearData() {
		mSelectReceiverListInfo.clear();
	}

	/**
	 * checkbox状态的保存
	 *
	 * @param isChecked CheckBox状态
	 */
	public int initCheckBox(boolean isChecked) {
		if (getCount() <= mleftMessageNums) {
			selectedAllMap.clear();
			for (int i = 0; i < getCount(); i++) {
				if (isChecked) {
					selectedAllMap.put(i, isChecked);
				} else {
					selectedAllMap.remove(i);
				}
			}
			return getCount();
		} else {
			selectedAllMap.clear();
			for (int i = 0; i < mleftMessageNums; i++) {
				if (isChecked) {
					selectedAllMap.put(i, isChecked);
				} else {
					selectedAllMap.remove(i);
				}
			}
			return mleftMessageNums;
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Holder holder;
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_select_message, null);
			holder = new Holder();
			holder.mRlSelectMessage = (RelativeLayout) convertView.findViewById(R.id.select_item_layout);
			holder.mCBSelectMessage = (CheckBox) convertView.findViewById(R.id.select_message_cb);
			holder.mTvSelectMessage = (TextView) convertView.findViewById(R.id.select_message_name);
			holder.mSelectState = (TextView) convertView.findViewById(R.id.select_message_state);
			holder.mSelectHead = (ImageView) convertView.findViewById(R.id.select_message_head_pic);
			holder.mSelectSex = (ImageView) convertView.findViewById(R.id.select_message_sex);
			holder.mSelectLevel = (ImageView) convertView.findViewById(R.id.select_message_level);
			holder.mSelectCard = (ImageView) convertView.findViewById(R.id.select_message_card);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.mRlSelectMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mleftMessageNums <= selectedAllMap.size()) {
					if ( !holder.mCBSelectMessage.isChecked()) {
						holder.mCBSelectMessage.setChecked(false);
					}else {
						holder.mCBSelectMessage.setChecked(false);
						selectedAllMap.remove(position);
					}
				} else {
					if (holder.mCBSelectMessage.isChecked()) {
						holder.mCBSelectMessage.setChecked(false);
						selectedAllMap.remove(position);
					} else {
						holder.mCBSelectMessage.setChecked(true);
						selectedAllMap.put(position, true);
					}
				}

				if (mOnItemClickListener != null) {
					mOnItemClickListener.onClick(view, position, selectedAllMap);
				}
			}
		});

		if (selectedAllMap != null && selectedAllMap.containsKey(position) && selectedAllMap.get(position)) {
			holder.mCBSelectMessage.setTag(position);
			holder.mCBSelectMessage.setChecked(true);
		} else {
			holder.mCBSelectMessage.setTag(position);
			holder.mCBSelectMessage.setChecked(false);
		}
		Map<String, String> selectListInfo = mSelectReceiverListInfo.get(position);
		String selectName = selectListInfo.get("nickname");
		String selectLevel = selectListInfo.get("level");
		String selectSex = selectListInfo.get("sex");
		String selectPic = selectListInfo.get("headPic");
		String selectState = selectListInfo.get("lastState");
		String selectIsUseCard = selectListInfo.get("messageCardAvailable");
		if (!TextUtils.isEmpty(selectName)) {
			holder.mTvSelectMessage.setText(selectName);
		}
		if (!TextUtils.isEmpty(selectLevel)) {
			ImageLoaderUtil.with().loadImage(mContext, holder.mSelectLevel, Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, selectLevel));
		}
		if (!TextUtils.isEmpty(selectSex)) {
			if (Integer.parseInt(selectSex) == Consts.GENDER_MALE) {
				holder.mSelectSex.setImageResource(R.drawable.icon_my_info_man);
			} else {
				holder.mSelectSex.setImageResource(R.drawable.icon_my_info_feman);
			}
		}
		ImageLoaderUtil.with().loadImageTransformRoundCircle(mContext, holder.mSelectHead, selectPic);
		if ("1".equals(selectState)) {
			holder.mSelectState.setText("今日已发");
			holder.mSelectState.setVisibility(View.VISIBLE);
		} else {
			holder.mSelectState.setVisibility(View.GONE);
		}
		if ("1".equals(selectIsUseCard)) {
			holder.mSelectCard.setVisibility(View.VISIBLE);
		} else {
			holder.mSelectCard.setVisibility(View.GONE);
		}

		return convertView;
	}

	class Holder {
		RelativeLayout mRlSelectMessage;
		CheckBox mCBSelectMessage;
		ImageView mSelectHead, mSelectSex, mSelectLevel, mSelectCard;
		TextView mTvSelectMessage, mSelectState;
	}

	public interface OnItemClickListener {
		void onClick(View view, int position, Map<Integer, Boolean> selectedAllMap);
	}
}
