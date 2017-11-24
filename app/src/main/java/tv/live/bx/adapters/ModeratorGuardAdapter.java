package tv.live.bx.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.efeizao.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;

import java.util.Map;

/**
 * 主播守护列表
 */
public class ModeratorGuardAdapter extends MyBaseAdapter {

	public ModeratorGuardAdapter(Context poContext) {
		super(poContext);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Holder loHolder;
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_moderator_guard_item, null);
			loHolder = new Holder();
			loHolder.userHead = (ImageView) convertView.findViewById(R.id.item_user_head);
			loHolder.userHeadLayout = (RelativeLayout) convertView.findViewById(R.id.item_layout);
			loHolder.guardLevel = (ImageView) convertView.findViewById(R.id.item_user_guard_level);
			loHolder.mIvYearGuardGb = (ImageView) convertView.findViewById(R.id.iv_year_guard_bg);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		@SuppressWarnings("unchecked")
		Map<String, String> itemData = (Map<String, String>) getItem(position);

		ImageLoaderUtil.with().loadImageTransformRoundCircle(mContext, loHolder.userHead, itemData.get("headPic"));
		ImageLoaderUtil.with().loadImage(mContext, loHolder.guardLevel, Utils.getFiledDrawable(Constants.USER_GUARD_LEVEL_PIX, itemData.get("type")));
		//0月费 1年费
		if ("1".equals(itemData.get("timeType"))) {
			loHolder.userHeadLayout.setBackgroundResource(R.drawable.shape_circle_cd8a30);
			loHolder.mIvYearGuardGb.setVisibility(View.VISIBLE);
		} else {
			loHolder.userHeadLayout.setBackgroundResource(R.drawable.shape_circle_b2b2b2);
			loHolder.mIvYearGuardGb.setVisibility(View.GONE);
		}
		return convertView;
	}

	class Holder {
		ImageView userHead;
		ImageView guardLevel;
		ImageView mIvYearGuardGb;
		RelativeLayout userHeadLayout;
	}

}
