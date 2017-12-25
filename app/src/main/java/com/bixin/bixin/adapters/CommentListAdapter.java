package com.bixin.bixin.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bixin.bixin.App;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.live.bx.R;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.util.DateUtil;
import com.bixin.bixin.library.util.HtmlUtil;
import com.bixin.bixin.library.util.NetworkImageGetter;

/**
 * 评论适配器 ClassName: MeReplyListAdapter <br/>
 */
public class CommentListAdapter extends BaseAdapter {

	private Context moContext;
	private List<Map<String, Object>> mlPlayers;


	public CommentListAdapter(Context poContext) {
		moContext = poContext;
		mlPlayers = new ArrayList<Map<String, Object>>();
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
		final Holder loHolder;
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(moContext);
			convertView = loInflater.inflate(R.layout.item_comment_list, null);

			loHolder = new Holder();
			loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_photo);

			loHolder.moIvPhotoV = (ImageView) convertView.findViewById(R.id.item_photo_v);

			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_nickname);

			loHolder.moTvTimer = (TextView) convertView.findViewById(R.id.item_time);

			loHolder.moTvContent = (TextView) convertView.findViewById(R.id.item_content);

			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> subjectInfo = (Map<String, Object>) getItem(position);

		ImageLoaderUtil.getInstance().loadHeadPic(moContext, loHolder.moIvPhoto, (String) subjectInfo.get("fromHeadPic"));
		loHolder.moTvNickname.setText((String) subjectInfo.get("fromNickname"));
		loHolder.moTvTimer
				.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong((String) subjectInfo.get("addTime"))));
		loHolder.moTvContent.setText(HtmlUtil.htmlTextDeal(moContext, (String) subjectInfo.get("content"),
				new NetworkImageGetter(loHolder.moTvContent, (int) (
                    App.metrics.widthPixels - (12 * 2 + 15 + 40)
						* App.metrics.density)), null));
		loHolder.moIvPhotoV.setVisibility(Utils.getBooleanFlag(subjectInfo.get("fromUserVerified"))
				? View.VISIBLE
				: View.GONE);
		return convertView;
	}

	class Holder {
		private ImageView moIvPhoto, moIvPhotoV;
		private TextView moTvNickname, moTvTimer, moTvContent;
	}

	public interface IOnclickListener {
		void onClick(View view, int position, TextView num);
	}

}
