package com.bixin.bixin.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bixin.bixin.App;
import tv.live.bx.R;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.HtmlUtil;

/**
 * 精选帖子列表适配器 ClassName: SubjectListAdapter <br/>
 */
public class HotSubjectListAdapter extends BaseAdapter {

	private Context moContext;
	private List<Map<String, Object>> mlPlayers;
	private int mImageViewHight;
	// private DisplayImageOptions moIlconfig;

	public HotSubjectListAdapter(Context poContext) {
		moContext = poContext;
		mlPlayers = new ArrayList<Map<String, Object>>();
		mImageViewHight = (int) ((App.metrics.widthPixels - App.metrics.density * 12 * 3) / 2);
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

	@SuppressLint({"InflateParams", "NewApi"})
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		EvtLog.e("", "getView " + position);
		final Holder loHolder;
		int type = position % 2;
		if (convertView == null || type != ((Holder) convertView.getTag()).type) {
			LayoutInflater loInflater = LayoutInflater.from(moContext);
			switch (type) {
				case 0:
					convertView = loInflater.inflate(R.layout.item_hot_subject_list_left, null);
					break;
				case 1:
					convertView = loInflater.inflate(R.layout.item_hot_subject_list_right, null);
					break;
			}
			loHolder = new Holder();
			loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_photo);
			LayoutParams layoutParams = (LayoutParams) loHolder.moIvPhoto.getLayoutParams();
			layoutParams.height = mImageViewHight;
			loHolder.moIvPhoto.setLayoutParams(layoutParams);
			loHolder.mSupport = (ImageView) convertView.findViewById(R.id.item_support);
			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_nickname);

			loHolder.moTvContent = (TextView) convertView.findViewById(R.id.item_content);
			loHolder.moTvSupport = (TextView) convertView.findViewById(R.id.item_support_num);
			loHolder.moTvReply = (TextView) convertView.findViewById(R.id.item_replay_num);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		loHolder.type = type;
		@SuppressWarnings("unchecked")
		Map<String, Object> subjectInfo = (Map<String, Object>) getItem(position);
		loHolder.moTvNickname.setText((String) subjectInfo.get("nickname"));
		String replaceImage = ((String) subjectInfo.get("content")).replaceAll(Constants.REGX_PHOTO, "");
		loHolder.moTvContent.setText(HtmlUtil.htmlTextDeal(moContext, replaceImage, null, null));
		try {
			JSONArray jsonarray = new JSONArray((String) subjectInfo.get("pics"));
			if (jsonarray.length() <= 0) {
				loHolder.moIvPhoto.setImageResource(R.drawable.icon_loading);
			} else {
				ImageLoaderUtil.getInstance().loadImage(loHolder.moIvPhoto, jsonarray.get(0));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (Boolean.parseBoolean(subjectInfo.get("supported").toString())) {
			loHolder.mSupport.setImageResource(R.drawable.btn_parise_pre);
		} else {
			loHolder.mSupport.setImageResource(R.drawable.btn_praise_nor);
		}

		loHolder.moTvSupport.setText((String) subjectInfo.get("supportNum"));
		loHolder.moTvReply.setText((String) subjectInfo.get("replyNum"));
		return convertView;
	}

	class Holder {
		int type;
		private ImageView moIvPhoto, mSupport;
		private TextView moTvNickname, moTvContent, moTvSupport, moTvReply;
	}

	public interface IOnclickListener {
		void onClick(View view, int position, View statusView);
	}

}
