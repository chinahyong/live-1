package tv.live.bx.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.efeizao.bx.R;
import tv.live.bx.common.Utils;

public class EmotionGridAdapter extends BaseAdapter {

	private Context moContext;
	private int[] maEmotionRes;

	public EmotionGridAdapter(Context poContext) {
		moContext = poContext;
		maEmotionRes = new int[] { R.drawable.emoji_1, R.drawable.emoji_2,
				R.drawable.emoji_3, R.drawable.emoji_4, R.drawable.emoji_5,
				R.drawable.emoji_6, R.drawable.emoji_7, R.drawable.emoji_8,
				R.drawable.emoji_9, R.drawable.emoji_10, R.drawable.emoji_11,
				R.drawable.emoji_12, R.drawable.emoji_13, R.drawable.emoji_14,
				R.drawable.emoji_15, R.drawable.emoji_16, R.drawable.emoji_17,
				R.drawable.emoji_18, R.drawable.emoji_19, R.drawable.emoji_20,
				R.drawable.emoji_21, R.drawable.emoji_22, R.drawable.emoji_23,
				R.drawable.emoji_24, R.drawable.emoji_25, R.drawable.emoji_26,
				R.drawable.emoji_27, R.drawable.emoji_28, R.drawable.emoji_29,
				R.drawable.emoji_30, R.drawable.emoji_31, R.drawable.emoji_32,
				R.drawable.emoji_33, R.drawable.emoji_del };
	}

	@Override
	public int getCount() {
		return maEmotionRes.length;
	}

	@Override
	public Object getItem(int piPos) {
		return maEmotionRes[piPos];
	}

	@Override
	public long getItemId(int piPos) {
		return piPos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			ImageView loIv = new ImageView(moContext);
			loIv.setLayoutParams(new GridView.LayoutParams(Utils.dip2px(
					moContext, 32), Utils.dip2px(moContext, 32)));
			loIv.setPadding(Utils.dip2px(moContext, 2),
					Utils.dip2px(moContext, 2), Utils.dip2px(moContext, 2),
					Utils.dip2px(moContext, 2));
			loIv.setImageResource((Integer) getItem(position));
			convertView = loIv;
		}
		return convertView;
	}
}
