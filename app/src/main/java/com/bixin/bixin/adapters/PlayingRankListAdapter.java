package com.bixin.bixin.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Map;

import tv.live.bx.R;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.imageloader.ImageLoaderUtil;

/**
 * @author Live
 * @version 2.2 2016.4.11
 * @title PlayingRankListAdapter.java Description:直播间泡泡排行榜
 */
public class PlayingRankListAdapter extends MyBaseAdapter<String, Object> {
	protected int layoutId;
	protected int mRankType;
	private final int[] layouts = {R.layout.item_playing_rank_before, R.layout.item_playing_rank};
	private final int[] orderNo = {R.drawable.ic_icon_ranking_1rt, R.drawable.ic_icon_ranking_2rd,
			R.drawable.ic_icon_ranking_3rd};
	private final int[] photoHeader = {R.drawable.ic_icon_ranking_header_1rt, R.drawable.ic_icon_ranking_header_2rd,
			R.drawable.ic_icon_ranking_header_3rd};

	/**
	 * Creates a new instance of PlayingRankListAdapter.
	 *
	 * @param poContext
	 * @param layoutId  列表布局文件Id，如：R.layout.item_fm_rank_field
	 */
	public PlayingRankListAdapter(Context poContext) {
		super(poContext);
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (position < 3)
			return 0;
		else
			return 1;
	}

	@SuppressLint({"InflateParams", "ResourceAsColor"})
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder loHolder = null;
		int type = getItemViewType(position);
		if (convertView == null || type != ((Holder) convertView.getTag()).type) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(layouts[type], null);
			loHolder = new Holder();
			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_playing_ranking_nick_name);
			loHolder.moTvMoney = (TextView) convertView.findViewById(R.id.item_playing_ranking_num);
			loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_playing_ranking_iv_photo);
			loHolder.moIvPhotoV = (ImageView) convertView.findViewById(R.id.item_playing_ranking_iv_photo_v);
			loHolder.mIvUserLevel = (ImageView) convertView.findViewById(R.id.item_playing_ranking_user_level);
			if (position < 3) {
				loHolder.moPhotoLayout = (RelativeLayout) convertView
						.findViewById(R.id.item_playing_ranking_photo_layout);
				loHolder.moIvOrderNo = (ImageView) convertView.findViewById(R.id.item_playing_ranking_iv_order);
				loHolder.moIvPhotoHeader = (ImageView) convertView.findViewById(R.id.item_playing_ranking_iv_hearder);
			} else {
				loHolder.moTvOrderNo = (TextView) convertView.findViewById(R.id.item_playing_ranking_tv_order_num);
			}
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> lmItem = (Map<String, Object>) getItem(position);
		LayoutParams lp = loHolder.moIvPhoto.getLayoutParams();
		switch (position) {
			case 0:
				lp.height = Utils.dip2px(mContext, 55.33f);
				lp.width = Utils.dip2px(mContext, 55.33f);
				loHolder.moPhotoLayout.setBackgroundResource(R.drawable.ic_bg_lv1rt);
				break;
			case 1:
				loHolder.moIvPhotoHeader.setPadding(0, Utils.dip2px(mContext, 29.66f), 0, 0);
				lp.height = Utils.dip2px(mContext, 47.33f);
				lp.width = Utils.dip2px(mContext, 47.33f);
				loHolder.moPhotoLayout.setBackgroundResource(R.drawable.ic_bg_lv2rd);
				break;
			case 2:
				loHolder.moIvPhotoHeader.setPadding(0, Utils.dip2px(mContext, 29.66f), 0, 0);
				lp.height = Utils.dip2px(mContext, 47.33f);
				lp.width = Utils.dip2px(mContext, 47.33f);
				loHolder.moPhotoLayout.setBackgroundResource(R.drawable.ic_bg_lv3rd);
				break;
		}
		loHolder.moIvPhoto.setLayoutParams(lp);
		if (position < 3) {
			loHolder.moIvOrderNo.setImageResource(orderNo[position]);
			loHolder.moIvPhotoHeader.setImageResource(photoHeader[position]);
		} else {
			loHolder.moTvOrderNo.setText(String.valueOf(position + 1));
		}
		if (lmItem.get("headPic") != null)
			ImageLoaderUtil.getInstance().loadHeadPic(mContext, loHolder.moIvPhoto, (String) lmItem.get("headPic"));
		loHolder.moIvPhotoV.setVisibility(Utils.getBooleanFlag(lmItem.get("verified"))
				? View.VISIBLE
				: View.GONE);
		loHolder.moTvMoney.setText(String.format(mContext.getString(R.string.rank_totalP_num),
				(String) lmItem.get("cost")));
		ImageLoaderUtil.getInstance().loadImage(loHolder.mIvUserLevel, Utils.getLevelImageResourceUri(lmItem, false));
		if (lmItem.get("nickname") != null)
			loHolder.moTvNickname.setText((String) lmItem.get("nickname"));

		return convertView;
	}

	protected class Holder {
		int type;
		ImageView moIvPhoto, moIvPhotoV, moIvOrderNo, moIvPhotoHeader, mIvUserLevel;
		TextView moTvNickname, moTvMoney, moTvOrderNo;
		RelativeLayout moPhotoLayout;
	}
}
