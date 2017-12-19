package tv.live.bx.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.StringUtil;

public class AnchorListAdapter extends BaseAdapter {

	private Context moContext;
	private List<Map<String, Object>> mlPlayers;
	private int mImageViewHeight;
	private OnClickListener mOnClickListener;
	private boolean isShowType = false;

	public AnchorListAdapter(Context poContext) {
		moContext = poContext;
		mlPlayers = new ArrayList<>();
		mImageViewHeight = FeizaoApp.metrics.widthPixels;
	}

	public void setOnClickListener(OnClickListener mOnClickListener) {
		this.mOnClickListener = mOnClickListener;
	}


	public void setIsShowType(boolean isShowType) {
		this.isShowType = isShowType;
	}

	/**
	 * 清除绑定的数据，注意，此方法没有刷新UI
	 */
	public void clearData() {
		mlPlayers.clear();
	}

	/**
	 * 添加数据，并自动刷新UI
	 *
	 * @param data
	 */
	public void addData(List<Map<String, Object>> data) {
		if (data != null) {
			mlPlayers.addAll(data);
			notifyDataSetChanged();
		}
	}

	/**
	 * 得到整个列表的总数
	 */
	public List<Map<String, Object>> getAllData() {
		return mlPlayers;
	}

	/**
	 * 添加数据到第一项，并自动刷新
	 *
	 * @param item
	 */
	public void addFirstItem(Map<String, Object> item) {
		if (item != null) {
			mlPlayers.add(0, item);
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
		return mlPlayers;
	}


	/**
	 * @return 数据是否为空
	 */
	public boolean isDataEmpty() {
		return mlPlayers.isEmpty();
	}

	@Override
	public int getCount() {
		return mlPlayers.size();
	}

	@Override
	public Object getItem(int position) {
		return mlPlayers.get(position);
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
			convertView = loInflater.inflate(R.layout.item_lv_player, null);

			loHolder = new Holder();
			// loHolder.mUserLevel = (ImageView)
			// convertView.findViewById(R.id.item_user_level);
			loHolder.moIvPhoto =  convertView.findViewById(R.id.item_lv_player_iv_photo);
			ViewGroup.LayoutParams lp = loHolder.moIvPhoto.getLayoutParams();
			lp.width = mImageViewHeight;
			lp.height = mImageViewHeight;
			loHolder.moIvPhoto.setLayoutParams(lp);
			loHolder.mUserHead =  convertView.findViewById(R.id.item_head);
			loHolder.moTvNickname =  convertView.findViewById(R.id.item_lv_player_tv_nickname);
			loHolder.mTvOnline =  convertView.findViewById(R.id.item_lv_player_tv_online_num);
			loHolder.mTvTitle =  convertView.findViewById(R.id.item_lv_player_title);
			loHolder.moTvStatus =  convertView.findViewById(R.id.item_lv_player_iv_status);
			loHolder.mTvLocation =  convertView.findViewById(R.id.item_lv_player_tv_live_location);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}

		final Map<String, Object> lmRoom = mlPlayers.get(position);
		ImageLoaderUtil.getInstance().loadImage(loHolder.moIvPhoto, lmRoom.get("headPic"));
//		ImageLoaderUtil.getInstance().loadHeadPic(moContext, loHolder.mUserHead, (String) lmRoom.get("headPic"));
		loHolder.mTvOnline.setText(String.format(moContext.getString(R.string.live_online_num),
				(String) lmRoom.get("onlineNum")));
//		loHolder.mTvPlayTime.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong((String) lmRoom
//				.get("playStartTime"))));
		loHolder.moTvStatus.setImageResource(Utils.strBool((String) lmRoom.get("isPlaying"))
				? R.drawable.btn_live
				: R.drawable.btn_rest);
		loHolder.moTvNickname.setText((String) lmRoom.get("nickname"));
		/*
		 * 标题增加话题设置高亮
		 * @version 2.5.0
		 */
		loHolder.mTvTitle.setText(StringUtil.setHighLigntText(moContext, lmRoom.get("announcement").toString()));
		if (!TextUtils.isEmpty(String.valueOf(lmRoom.get("city")))) {
			loHolder.mTvLocation.setText(String.valueOf(lmRoom.get("city")));
		}
//		if (isShowType) {
//			if (lmRoom.get("tags") != null) {
//				try {
//					List<Map<String, String>> tags = JSONParser.parseMulti(String.valueOf(lmRoom.get("tags")));
//					if (tags != null && tags.size() > 0) {
//						loHolder.mTvType.setVisibility(View.VISIBLE);
//						loHolder.mTvType.setText(tags.get(0).get("name"));
//						loHolder.mTvType.setTag(tags.get(0));
//					} else {
//						loHolder.mTvType.setVisibility(View.GONE);
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			} else {
//				loHolder.mTvType.setVisibility(View.GONE);
//			}
//		} else {
//			loHolder.mTvType.setVisibility(View.GONE);
//		}
//		loHolder.mTvType.setOnClickListener(mOnClickListener);

		return convertView;
	}


	class Holder {
		private ImageView moIvPhoto, mUserHead, moTvStatus, mUserHeadV;
		private RelativeLayout mItemPhotoLayout;
		private TextView moTvNickname, mTvType, mTvPlayTime,
				mTvTitle, mTvOnline, mTvLocation;
	}
}
