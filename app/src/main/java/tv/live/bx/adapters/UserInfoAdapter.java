package tv.live.bx.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.efeizao.bx.R;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;

import java.util.Map;

public class UserInfoAdapter extends MyBaseAdapter<String, String> {

	private OnClickListener onClickListener;

	public UserInfoAdapter(Context poContext) {
		super(poContext);
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_user_info_list, null);
			Holder loHolder = new Holder();
			loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_lv_focus_photo);
			loHolder.moIvPhotoV = (ImageView) convertView.findViewById(R.id.item_lv_focus_photo_v);
			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_lv_focus_nickname);
			loHolder.mUserIntrotion = (TextView) convertView.findViewById(R.id.item_lv_focus_intro);
			// loHolder.mIsPlaing = (ImageView)
			// convertView.findViewById(R.id.isPlaying);
			loHolder.mUserFocus = (ImageView) convertView.findViewById(R.id.item_user_focus);
			convertView.setTag(loHolder);
		}
		Holder loHolder = (Holder) convertView.getTag();
		@SuppressWarnings("unchecked")
		Map<String, String> lmItem = (Map<String, String>) getItem(position);
		ImageLoaderUtil.with().loadImageTransformRoundCircle(mContext, loHolder.moIvPhoto, lmItem.get("headPic"));

		loHolder.moIvPhotoV.setVisibility(Utils.getBooleanFlag(lmItem.get("verified"))
				? View.VISIBLE
				: View.GONE);
		loHolder.moTvNickname.setText(lmItem.get("nickname"));
		loHolder.mUserIntrotion.setText(lmItem.get("signature"));

		if (Utils.strBool(lmItem.get("isAttention"))) {
			loHolder.mUserFocus.setTag(position);
			loHolder.mUserFocus.setImageResource(R.drawable.btn_focused_selector);
		} else {
			loHolder.mUserFocus.setTag(position);
			loHolder.mUserFocus.setImageResource(R.drawable.btn_focus_selector);
		}
		if (onClickListener != null)
			loHolder.mUserFocus.setOnClickListener(onClickListener);
		return convertView;
	}

	private class Holder {
		private ImageView moIvPhoto, mUserFocus, mIsPlaing, moIvPhotoV;
		private TextView moTvNickname, mUserIntrotion;
	}

}
