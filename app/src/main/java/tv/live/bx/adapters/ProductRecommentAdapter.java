package tv.live.bx.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lonzh.lib.LZBaseAdapter;

import java.util.Map;

import tv.live.bx.R;
import tv.live.bx.imageloader.ImageLoaderUtil;

public class ProductRecommentAdapter extends LZBaseAdapter {

	private Context moContext;

	public ProductRecommentAdapter(Context context) {
		this.moContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(moContext);
			convertView = loInflater.inflate(R.layout.item_product_recomment, null);
			Holder loHolder = new Holder();
			loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_lv_focus_photo);
			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_lv_focus_nickname);
			loHolder.mUserIntrotion = (TextView) convertView.findViewById(R.id.item_lv_focus_intro);
			convertView.setTag(loHolder);
		}
		Holder loHolder = (Holder) convertView.getTag();
		@SuppressWarnings("unchecked")
		Map<String, String> lmItem = (Map<String, String>) getItem(position);
		if (lmItem.get("icon") != null)
			ImageLoaderUtil.with().loadImage(moContext, loHolder.moIvPhoto, lmItem.get("icon"));
		if (lmItem.get("name") != null)
			loHolder.moTvNickname.setText(lmItem.get("name"));
		if (lmItem.get("desc") != null)
			loHolder.mUserIntrotion.setText(lmItem.get("desc"));
		return convertView;
	}

	private class Holder {
		private ImageView moIvPhoto;
		private TextView moTvNickname, mUserIntrotion;
	}
}
