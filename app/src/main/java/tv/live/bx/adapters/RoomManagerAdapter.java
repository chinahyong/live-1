package tv.live.bx.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import tv.live.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;

public class RoomManagerAdapter extends MyBaseAdapter<String, Object> {

	private RemoveManagerListener listener;

	public void setListener(RemoveManagerListener listener) {
		this.listener = listener;
	}

	public RoomManagerAdapter(Context poContext) {
		super(poContext);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_lv_room_manager, null);
			Holder loHolder = new Holder();
			loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_lv_manager_photo);
			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_lv_manager_nickname);
			loHolder.mUserLevel = (ImageView) convertView.findViewById(R.id.item_lv_manager_level);
			loHolder.mIvManage = (ImageView) convertView.findViewById(R.id.item_lv_manager_manage);
			convertView.setTag(loHolder);
		}
		Holder loHolder = (Holder) convertView.getTag();
		@SuppressWarnings("unchecked")
		final Map<String, Object> lmItem = (Map<String, Object>) getItem(position);
		ImageLoaderUtil.with().loadImageTransformRoundCircle(mContext, loHolder.moIvPhoto, (String) lmItem.get("headPic"));
		loHolder.moTvNickname.setText((String) lmItem.get("nickname"));
		if (!TextUtils.isEmpty((String) lmItem.get("level"))) {
			ImageLoaderUtil.with().loadImage(mContext, loHolder.mUserLevel, Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX,
					(String) lmItem.get("level")));
		}
		// 取消管理
		loHolder.mIvManage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onRemoveManager(position, lmItem.get("uid").toString());
			}
		});
		return convertView;
	}

	private class Holder {
		private ImageView moIvPhoto, mUserLevel, mIvManage;
		private TextView moTvNickname;
	}

	public interface RemoveManagerListener {
		void onRemoveManager(int position, String uid);
	}

}
