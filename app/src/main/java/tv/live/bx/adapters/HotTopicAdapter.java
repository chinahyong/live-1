package tv.live.bx.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import tv.live.bx.R;
import tv.live.bx.common.Constants;

/**
 * @author Live
 * @version 2016/6/8 ${VERSION}
 * @title ${CLASS_NAME} Description:
 */
public class HotTopicAdapter extends MyBaseAdapter<String, String> {

	public HotTopicAdapter(Context poContext) {
		super(poContext);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_topic_hot_list, null);
			Holder loHolder = new Holder();
			loHolder.moTvTopicName = (TextView) convertView;
			convertView.setTag(loHolder);
		}
		Holder loHolder = (Holder) convertView.getTag();
		@SuppressWarnings("unchecked")
		Map<String, String> lmItem = (Map<String, String>) getItem(position);
		loHolder.moTvTopicName.setText(Constants.COMMON_INSERT_POST_PIX + lmItem.get("title") + Constants.COMMON_INSERT_POST_PIX);

		return convertView;
	}

	private class Holder {
		private TextView moTvTopicName;
	}
}
