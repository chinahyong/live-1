package tv.live.bx.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.model.AnchorBean;

/**
 * Created by BYC on 2017/7/14.
 */

public class LiveHotRecommendAdapter extends PagerAdapter {
	private List<AnchorBean> recommends = new ArrayList<>();
	private Context context;
	private int imageHeight;
	private LiveHotAdapter.OnItemClickListener itemClickListener;

	public LiveHotRecommendAdapter(Context ctx) {
		this.context = ctx;
		imageHeight = (FeizaoApp.metrics.widthPixels - Utils.dpToPx(36)) / 2;
	}

	public void setOnInnerClick(LiveHotAdapter.OnItemClickListener onInnerClick) {
		this.itemClickListener = onInnerClick;
	}

	public void setData(List<AnchorBean> data) {
		this.recommends.clear();
		if (data != null)
			this.recommends.addAll(data);
		notifyDataSetChanged();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Integer i = (Integer) v.getTag();
			if (i < recommends.size())
				itemClickListener.onInnerClick(LiveHotAdapter.CLICK_RECOMMEND, i);
			else
				itemClickListener.onInnerClick(LiveHotAdapter.CLICK_BECOME_RECOMMEND,i);
		}
	};


	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setPadding(Utils.dpToPx(12), 0, Utils.dpToPx(12), 0);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		View left = LayoutInflater.from(context).inflate(R.layout.item_hot_official_recommended, linearLayout, false);
		LinearLayout.LayoutParams layoutParamLeft = new LinearLayout.LayoutParams(imageHeight, imageHeight);
		linearLayout.addView(left, layoutParamLeft);
		ImageView left_iv_photo = (ImageView) left.findViewById(R.id.item_iv_photo);
		TextView left_tv_tag = (TextView) left.findViewById(R.id.item_tv_tag);
		TextView left_tv_online = (TextView) left.findViewById(R.id.item_tv_online);
		TextView left_tv_title = (TextView) left.findViewById(R.id.item_tv_title);
		AnchorBean anchorBeanLeft = recommends.get(2 * position);
		ImageLoaderUtil.with().loadImageTransformRoundedCorners(context, left_iv_photo, anchorBeanLeft.headPic, Constants.COMMON_DISPLAY_IMAGE_CORNER_2);
		left_tv_tag.setText(anchorBeanLeft.recommendTag);
		left_tv_online.setText(anchorBeanLeft.onlineNum + "人");
		left_tv_title.setText(anchorBeanLeft.announcement);
		left.setTag(2 * position);
		left.setOnClickListener(clickListener);

		if (recommends.size() >= 2 * (position + 1)) {
			View right = LayoutInflater.from(context).inflate(R.layout.item_hot_official_recommended, linearLayout, false);
			LinearLayout.LayoutParams layoutParamRight = new LinearLayout.LayoutParams(imageHeight, imageHeight);
			layoutParamRight.setMargins(Utils.dpToPx(12), 0, 0, 0);
			linearLayout.addView(right, layoutParamRight);

			ImageView right_iv_photo = (ImageView) right.findViewById(R.id.item_iv_photo);
			TextView right_tv_tag = (TextView) right.findViewById(R.id.item_tv_tag);
			TextView right_tv_online = (TextView) right.findViewById(R.id.item_tv_online);
			TextView right_tv_title = (TextView) right.findViewById(R.id.item_tv_title);

			AnchorBean anchorBeanRight = recommends.get(2 * position + 1);
			ImageLoaderUtil.with().loadImageTransformRoundedCorners(context, right_iv_photo, anchorBeanRight.headPic, Constants.COMMON_DISPLAY_IMAGE_CORNER_2);
			right_tv_tag.setText(anchorBeanRight.recommendTag);
			right_tv_online.setText(anchorBeanRight.onlineNum + "人");
			right_tv_title.setText(anchorBeanRight.announcement);
			right.setTag(2 * position + 1);
			right.setOnClickListener(clickListener);
		} else {
			View right = LayoutInflater.from(context).inflate(R.layout.item_hot_official_recommended, linearLayout, false);
			LinearLayout.LayoutParams layoutParamRight = new LinearLayout.LayoutParams(imageHeight, imageHeight);
			layoutParamRight.setMargins(Utils.dpToPx(12), 0, 0, 0);
			linearLayout.addView(right, layoutParamRight);

			ImageView right_iv_photo = (ImageView) right.findViewById(R.id.item_iv_photo);
			TextView right_tv_tag = (TextView) right.findViewById(R.id.item_tv_tag);
			TextView right_tv_online = (TextView) right.findViewById(R.id.item_tv_online);
			TextView right_tv_title = (TextView) right.findViewById(R.id.item_tv_title);

			right_iv_photo.setImageResource(R.drawable.icon_become_recommend);
			right_tv_tag.setVisibility(View.GONE);
			right_tv_online.setVisibility(View.GONE);
			right_tv_title.setVisibility(View.GONE);
			right.setTag(2 * position + 1);
			right.setOnClickListener(clickListener);
		}
		container.addView(linearLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, imageHeight));
		return linearLayout;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public int getCount() {
		return (recommends.size() >> 1) + (recommends.size() & 0x01);
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
}
