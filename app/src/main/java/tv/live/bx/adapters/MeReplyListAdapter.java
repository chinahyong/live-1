package tv.live.bx.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.DateUtil;
import tv.live.bx.library.util.HtmlUtil;
import tv.live.bx.library.util.NetworkImageGetter;

/**
 * 我的回复适配器 ClassName: MeReplyListAdapter <br/>
 */
public class MeReplyListAdapter extends BaseAdapter {

	private Context moContext;
	private List<Map<String, Object>> mlPlayers;

	private IOnclickListener cliOnclickListener;

	public MeReplyListAdapter(Context poContext, IOnclickListener cliOnclickListener) {
		moContext = poContext;
		mlPlayers = new ArrayList<Map<String, Object>>();
		this.cliOnclickListener = cliOnclickListener;
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
			convertView = loInflater.inflate(R.layout.actvity_me_reply_item, null);

			loHolder = new Holder();
			loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_photo);

			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_nickname);

			loHolder.moTvTimer = (TextView) convertView.findViewById(R.id.item_time);

			loHolder.moTvReplyContent = (TextView) convertView.findViewById(R.id.item_reply_content);

			loHolder.moTvContent = (TextView) convertView.findViewById(R.id.item_content);

			loHolder.moTvContentPrefix = (TextView) convertView.findViewById(R.id.item_content_prefix);

			loHolder.moTvContentSurfix = (TextView) convertView.findViewById(R.id.item_content_suffix);

			loHolder.mUserLevel = (ImageView) convertView.findViewById(R.id.item_userlevel);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> subjectInfo = (Map<String, Object>) getItem(position);

		ImageLoaderUtil.with().loadImageTransformRoundCircle(moContext, loHolder.moIvPhoto, (String) subjectInfo.get("headPic"));
		loHolder.moTvNickname.setText((String) subjectInfo.get("nickname"));

		loHolder.moTvTimer
				.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong((String) subjectInfo.get("addTime"))));
		// 如果这个字段为空，则是回复帖子
		if (TextUtils.isEmpty((String) subjectInfo.get("fReplyId"))) {
			loHolder.moTvContentPrefix.setText("在“");
			loHolder.moTvContentSurfix.setText("”回复");
		} else {
			loHolder.moTvContentPrefix.setText("回复“");
			loHolder.moTvContentSurfix.setText("”");
		}
		if (!TextUtils.isEmpty((String) subjectInfo.get("level"))) {
			ImageLoaderUtil.with().loadImage(moContext, loHolder.mUserLevel, Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX,
					(String) subjectInfo.get("level")));

		}
		loHolder.moTvContent.setText(HtmlUtil.htmlTextDeal(moContext, (String) subjectInfo.get("content"),
				new NetworkImageGetter(loHolder.moTvContent, (int) (FeizaoApp.metrics.widthPixels - (12 * 2 + 10 + 40)
						* FeizaoApp.metrics.density)), null));
		loHolder.moTvReplyContent.setText(HtmlUtil.htmlTextDeal(moContext, subjectInfo.get("toContent").toString(), new NetworkImageGetter(loHolder.moTvReplyContent,
				(int) (FeizaoApp.metrics.widthPixels - (12 * 2 + 10 + 40) * FeizaoApp.metrics.density)), null));
		return convertView;
	}

	class Holder {
		private ImageView moIvPhoto, mUserLevel;
		private TextView moTvNickname, moTvTimer, moTvContent, moTvReplyContent, moTvContentPrefix, moTvContentSurfix;
	}

	public interface IOnclickListener {
		void onClick(View view, int position, TextView num);
	}

}
