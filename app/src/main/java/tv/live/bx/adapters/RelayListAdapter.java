package tv.live.bx.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Html.TagHandler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.DateUtil;
import tv.live.bx.library.util.HtmlUtil;
import tv.live.bx.library.util.NetworkImageGetter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RelayListAdapter extends BaseAdapter {

	private Context moContext;
	private List<Map<String, Object>> replayInfos;
	private IOnclickListener cliOnclickListener;
	private final String REPLY_TEXT = "回复@";
	private int basePosition = 0;
	/**
	 * 楼中楼是否展开
	 */
	private final String IS_EXTENDS = "isExtends";

	private TagHandler mTagHandler;

	public RelayListAdapter(Context poContext, IOnclickListener cliOnclickListener) {
		moContext = poContext;
		replayInfos = new ArrayList<Map<String, Object>>();
		this.cliOnclickListener = cliOnclickListener;
	}

	public void setTagHandler(TagHandler mTagHandler) {
		this.mTagHandler = mTagHandler;
	}

	/**
	 * 清除绑定的数据，注意，此方法没有刷新UI
	 */
	public void clearData() {
		basePosition = 0;
		replayInfos.clear();
	}

	public void setBasePosition(int basePosition) {
		this.basePosition = basePosition;
	}

	/**
	 * 添加数据，并自动刷新UI
	 *
	 * @param data
	 */
	public void addData(List<Map<String, Object>> data) {
		if (data != null) {
			replayInfos.addAll(data);
			notifyDataSetChanged();
		}
	}

	/**
	 * 添加数据，并自动刷新UI
	 *
	 * @param data
	 */
	public void addData(Map<String, Object> data) {
		if (data != null) {
			replayInfos.add(data);
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
			replayInfos.add(0, item);
			notifyDataSetChanged();
		}
	}

	/**
	 * @return 获取数据
	 */
	public List<Map<String, Object>> getData() {
		return replayInfos;
	}

	/**
	 * @return 数据是否为空
	 */
	public boolean isDataEmpty() {
		return replayInfos.isEmpty();
	}

	@Override
	public int getCount() {
		// 如果没有回复数据，让它显示帖子信息
		if (replayInfos.size() == 0) {
			return -1;
		}
		return replayInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return replayInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Holder loHolder;
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(moContext);
			convertView = loInflater.inflate(R.layout.a_post_item, null);

			loHolder = new Holder();
			loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_photo);
			loHolder.moIvPhotoV = (ImageView) convertView.findViewById(R.id.item_photo_v);
			loHolder.mTvLevel = (TextView) convertView.findViewById(R.id.item_level);
			loHolder.mUserLevel = (ImageView) convertView.findViewById(R.id.item_userlevel);
			loHolder.mPictureLayout = (LinearLayout) convertView.findViewById(R.id.item_relay_layout);

			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_nickname);

			loHolder.moTvTimer = (TextView) convertView.findViewById(R.id.item_time);
			loHolder.moTvContent = (TextView) convertView.findViewById(R.id.item_content);

			loHolder.mRepleyIv = (ImageView) convertView.findViewById(R.id.item_replay);
			loHolder.mMoreIv = (ImageView) convertView.findViewById(R.id.item_more);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> relayInfo = (Map<String, Object>) getItem(position);

		ImageLoaderUtil.with().loadImageTransformRoundCircle(moContext, loHolder.moIvPhoto, (String) relayInfo.get("headPic"));
		@SuppressWarnings("unchecked")
		final List<Map<String, String>> lmPlayer = (List<Map<String, String>>) relayInfo.get("lzlReplys");
		if (lmPlayer != null && lmPlayer.size() > 0) {
			loHolder.mPictureLayout.removeAllViews();
			View view = new View(moContext);
			view.setBackgroundColor(moContext.getResources().getColor(R.color.divider_horizontal));
			LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					(int) FeizaoApp.metrics.density);
			params.bottomMargin = (int) (2 * FeizaoApp.metrics.density);
			params.rightMargin = (int) (6 * FeizaoApp.metrics.density);
			loHolder.mPictureLayout.addView(view, params);
			loHolder.mPictureLayout.setVisibility(View.VISIBLE);
			for (int i = 0; i < lmPlayer.size(); i++) {
				// 如果超过2条回复且没有展开，显示更多回复
				if (i >= 2 && !Utils.getBooleanFlag(relayInfo.get(IS_EXTENDS))) {
					RelativeLayout.LayoutParams moreLayoutParam = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					moreLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					moreLayoutParam.rightMargin = (int) (6 * FeizaoApp.metrics.density);
					final RelativeLayout mMoreLayout = new RelativeLayout(moContext);

					TextView mMoreText = new TextView(moContext);
					mMoreText.setTextSize(12);
					mMoreText.setText("更多" + (lmPlayer.size() - 2) + "条回复");
					mMoreText.setTextColor(Color.parseColor("#da500e"));
					mMoreText.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							loHolder.mPictureLayout.removeView(mMoreLayout);
							for (int i = 2; i < lmPlayer.size(); i++) {
								showReplyTextView(position, loHolder, lmPlayer, i);
							}
							((Map<String, Object>) getItem(position)).put(IS_EXTENDS, Constants.COMMON_TRUE);
						}
					});

					mMoreLayout.addView(mMoreText, moreLayoutParam);
					loHolder.mPictureLayout.addView(mMoreLayout, params);
					break;
				}
				params = showReplyTextView(position, loHolder, lmPlayer, i);
			}
		} else {
			loHolder.mPictureLayout.setVisibility(View.GONE);
		}

		loHolder.moTvNickname.setText((String) relayInfo.get("nickname"));
		loHolder.moTvTimer
				.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong((String) relayInfo.get("addTime"))));
		loHolder.mTvLevel.setText("第" + (basePosition + position + 1) + "楼");
		ImageLoaderUtil.with().loadImage(moContext, loHolder.mUserLevel, Utils.getLevelImageResourceUri(relayInfo, true));
		// StringBuilder contentSB = new StringBuilder((String)
		// relayInfo.get("content")).append(" ");
		// try {
		// JSONArray jsonarray = new JSONArray((String) relayInfo.get("pics"));
		// for (int i = 0; i < jsonarray.length(); i++) {
		// contentSB.append("<img src='").append(jsonarray.get(i)).append("' />");
		// }
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }

		loHolder.moTvContent.setText(HtmlUtil.htmlTextDeal(moContext, (String) relayInfo.get("content"),
				new NetworkImageGetter(loHolder.moTvContent, (int) (FeizaoApp.metrics.widthPixels - (32 + 52)
						* FeizaoApp.metrics.density)), mTagHandler));
		loHolder.moTvContent.setText(HtmlUtil.htmlTextUrlClick(moContext, loHolder.moTvContent.getText()));
		loHolder.moTvContent.setMovementMethod(LinkMovementMethod.getInstance());
		loHolder.moIvPhotoV.setVisibility(Utils.getBooleanFlag(relayInfo.get("verified"))
				? View.VISIBLE
				: View.GONE);
		loHolder.moIvPhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (cliOnclickListener != null) {
					cliOnclickListener.onClick(v, position, null);
				}

			}
		});
		loHolder.mMoreIv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (cliOnclickListener != null) {
					cliOnclickListener.onClick(v, position, null);
				}
			}
		});

		loHolder.mRepleyIv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (cliOnclickListener != null) {
					cliOnclickListener.onClick(v, position, null);
				}
			}
		});

		return convertView;
	}

	/**
	 * 显示回复文本
	 */
	private LinearLayout.LayoutParams showReplyTextView(final int position, final Holder loHolder,
														final List<Map<String, String>> lmPlayer, int i) throws NumberFormatException {
		LinearLayout.LayoutParams params;
		final Map<String, String> info = lmPlayer.get(i);
		params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.topMargin = (int) (8 * FeizaoApp.metrics.density);
		TextView textview = new TextView(moContext);
		textview.setLineSpacing(3 * FeizaoApp.metrics.density, 1);
		textview.setGravity(Gravity.CENTER_VERTICAL);
//		textview.setAutoLinkMask(Linkify.WEB_URLS);
//		textview.setLinkTextColor(moContext.getResources().getColor(R.color.a_text_color_da500e));
		String content = info.get("content");
		// 如果是一级回复
		if ("0".equals(info.get("lzlReplyId"))) {

			SpannableString loFrom = new SpannableString(info.get("nickname"));
			loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#da500e")), 0, loFrom.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			textview.setText(loFrom);
			textview.append(Utils.getImageToSpannableString(textview,Utils.getLevelImageResourceUri(info, true), Utils.dip2px(moContext, 14.66f)));
			loFrom = new SpannableString("：");
			loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#da500e")), 0, loFrom.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			textview.append(loFrom);
			loFrom = HtmlUtil.htmlTextDeal(moContext, content, new NetworkImageGetter(textview,
					(int) (FeizaoApp.metrics.widthPixels - (32 + 49.33) * FeizaoApp.metrics.density)), null);
			loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#666666")), 0, loFrom.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			textview.append(loFrom);
			loFrom = new SpannableString(" " + DateUtil.fmtTimemillsToTextFormat(Long.parseLong(info.get("addTime"))));
			loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#ababab")), 0, loFrom.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			loFrom.setSpan(new AbsoluteSizeSpan(10, true), 0, loFrom.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			textview.append(loFrom);
		} else {
			SpannableString loFrom = new SpannableString(info.get("nickname"));
			loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#da500e")), 0, loFrom.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			textview.setText(loFrom);
			textview.append(Utils.getImageToSpannableString(textview,Utils.getLevelImageResourceUri(info, true), Utils.dip2px(moContext, 14.66f)));
			loFrom = new SpannableString("回复");
			loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#666666")), 0, loFrom.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			textview.append(loFrom);

			loFrom = new SpannableString(info.get("toNickname"));
			loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#da500e")), 0, loFrom.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			textview.append(loFrom);
			if (Constants.USER_TYPE_ANCHOR.equals(info.get("toType"))
					|| Constants.USER_TYPE_ROOMOWNER.equals(info.get("toType"))) {
				textview.append(Utils.getImageToSpannableString(textview,
						Utils.getLevelImageResourceUri(Constants.USER_ANCHOR_LEVEL_PIX, info.get("toModeratorLevel")),
						Utils.dip2px(moContext, 14.66f)));
			} else {
				textview.append(Utils.getImageToSpannableString(textview,
						Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, info.get("toLevel")),
						Utils.dip2px(moContext, 14.66f)));
			}
			loFrom = new SpannableString("：");
			loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#da500e")), 0, loFrom.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			textview.append(loFrom);
			loFrom = HtmlUtil.htmlTextDeal(moContext, content, new NetworkImageGetter(textview,
					(int) (FeizaoApp.metrics.widthPixels - (32 + 49.33) * FeizaoApp.metrics.density)), null);
			loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#666666")), 0, loFrom.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			textview.append(loFrom);
			loFrom = new SpannableString(" " + DateUtil.fmtTimemillsToTextFormat(Long.parseLong(info.get("addTime"))));
			loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#ababab")), 0, loFrom.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			loFrom.setSpan(new AbsoluteSizeSpan(10, true), 0, loFrom.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			textview.append(loFrom);
		}
		textview.setTextSize(12);
		textview.setGravity(Gravity.CENTER_VERTICAL);
		// 点击回复 “回复”
		textview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (cliOnclickListener != null) {
					cliOnclickListener.onClick(v, position, info);
				}
			}
		});
		loHolder.mPictureLayout.addView(textview, params);
		return params;
	}

	class Holder {
		private ImageView moIvPhoto, mUserLevel, mRepleyIv, mMoreIv, moIvPhotoV;
		private LinearLayout mPictureLayout;
		private TextView moTvNickname, moTvTimer, moTvContent, mTvLevel;
	}

	public interface IOnclickListener {
		void onClick(View view, int position, Map<String, String> relayinfo);
	}
}
