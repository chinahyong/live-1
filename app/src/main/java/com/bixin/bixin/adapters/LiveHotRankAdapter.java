package com.bixin.bixin.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tv.live.bx.R;
import com.bixin.bixin.common.config.UserInfoConfig;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;
import com.bixin.bixin.model.AnchorBean;

/**
 * Created by Live on 2017/8/22.
 * 直播间热门排行
 */

public class LiveHotRankAdapter extends MyBaseAdapter {
	private List<AnchorBean> mDatas;
	private boolean isAllFlag = false;  // 是否是总榜

	public LiveHotRankAdapter(Context poContext) {
		super(poContext);
		mDatas = new ArrayList<>();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public void clearData() {
		mDatas.clear();
	}

	@Override
	public void addData(List data) {
		if (data != null) {
			mDatas.addAll(data);
			notifyDataSetChanged();
		}
	}

	public void setData(List data, boolean isAllFlag) {
		this.isAllFlag = isAllFlag;
		clearData();
		addData(data);
	}

	@Override
	public List<AnchorBean> getData() {
		return mDatas;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_pop_hot_rank, null);
			holder = new Holder();
			holder.tvNum = (TextView) convertView.findViewById(R.id.item_hot_rank_num);
			holder.tvNickname = (TextView) convertView.findViewById(R.id.item_hot_rank_nick_name);
			holder.tvStatus = (TextView) convertView.findViewById(R.id.item_hot_rank_status);
			holder.ivHeadPic = (ImageView) convertView.findViewById(R.id.item_hot_rank_headpic);
			holder.ivVerified = (ImageView) convertView.findViewById(R.id.item_hot_rank_headpic_v);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		AnchorBean data = mDatas.get(position);
		ImageLoaderUtil.getInstance().loadHeadPic(mContext, holder.ivHeadPic, data
				.headPic);
		holder.ivVerified.setVisibility(data.verified ? View.VISIBLE : View.GONE);
		holder.tvNickname.setText(data.nickname);
		holder.tvNum.setText(String.valueOf(position + 1));
		// 总榜
		if (isAllFlag) {
			holder.tvStatus.setTextColor(mContext.getResources().getColor(R.color
					.a_text_color_999999));
			String status = mContext.getResources().getString(R.string.live_hot_rank_count) +
					"<font color='#4bbabc'> " + String.valueOf(data.hotCount) + "</font>" + "次";
			holder.tvStatus.setText(Html.fromHtml(status));
		} else {
			holder.tvStatus.setTextColor(mContext.getResources().getColor(R.color
					.a_text_color_4bbabc));
			// 下轮排行榜
			if (UserInfoConfig.getInstance().id.equals(String.valueOf(data.mid))) {
				// 当前显示uid == 当前登录uid，同一个人
				// isOwn
				holder.tvStatus.setText(R.string.live_hot_rank_is_owner);
			} else {
				if (data.isPlaying) {
					// 不是自己，是否正在直播
					holder.tvStatus.setText(R.string.string_state_streaming);
				} else {
					holder.tvStatus.setText("");
				}
			}
		}
		return convertView;
	}

	public interface OnItemClickListener {
		void onClick(View view, int position);
	}

	class Holder {
		TextView tvNum, tvNickname, tvStatus;
		ImageView ivHeadPic, ivVerified;
	}

}
