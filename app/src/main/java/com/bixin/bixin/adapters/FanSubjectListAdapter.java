package com.bixin.bixin.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bixin.bixin.App;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.live.bx.R;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.util.DateUtil;
import com.bixin.bixin.library.util.HtmlUtil;
import com.bixin.bixin.library.util.NetworkImageGetter;
import com.bixin.bixin.util.ActivityJumpUtil;

/**
 * 帖子列表适配器 ClassName: SubjectListAdapter <br/>
 */
public class FanSubjectListAdapter extends BaseAdapter {

	private Context moContext;
	private List<Map<String, Object>> mlPlayers;

	private IOnclickListener cliOnclickListener;

	/**
	 * 是否是管理员
	 */
	private boolean isAdmin = true;

	public FanSubjectListAdapter(Context poContext, IOnclickListener cliOnclickListener) {
		moContext = poContext;
		mlPlayers = new ArrayList<Map<String, Object>>();
		this.cliOnclickListener = cliOnclickListener;
	}

	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
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
			convertView = loInflater.inflate(R.layout.item_subject_list, null);

			loHolder = new Holder();
			loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_photo);
			loHolder.mTop = (ImageView) convertView.findViewById(R.id.item_top);
			loHolder.mMoreIv = (ImageView) convertView.findViewById(R.id.item_more);
			loHolder.mRecommentIv = (ImageView) convertView.findViewById(R.id.item_recomment);
			loHolder.mPictureLayout = (LinearLayout) convertView.findViewById(R.id.item_picture_grid);

			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_nickname);
			loHolder.mUserLevel = (ImageView) convertView.findViewById(R.id.item_userlevel);
			// loHolder.mForumTitle = (TextView)
			// convertView.findViewById(R.id.item_moudle_text);

			loHolder.moTvTimer = (TextView) convertView.findViewById(R.id.item_time);
			loHolder.moTvContent = (TextView) convertView.findViewById(R.id.item_content);
			loHolder.mTitle = (TextView) convertView.findViewById(R.id.item_title);
			loHolder.moSupport = (ImageView) convertView.findViewById(R.id.item_support);
			loHolder.moTvSupport = (TextView) convertView.findViewById(R.id.item_support_num);
			loHolder.moTvReply = (TextView) convertView.findViewById(R.id.item_replay_num);
			loHolder.mTvView = (TextView) convertView.findViewById(R.id.item_view_num);
			// loHolder.mStatusLayout = (LinearLayout)
			// convertView.findViewById(R.id.item_status_layout);
			loHolder.mSupportLayout = (RelativeLayout) convertView.findViewById(R.id.item_support_layout);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> subjectInfo = (Map<String, Object>) getItem(position);

		ImageLoaderUtil.getInstance().loadHeadPic(moContext, loHolder.moIvPhoto, (String) subjectInfo.get("headPic"));
		if ("1".equals(subjectInfo.get("is_top"))) {
			loHolder.mTop.setVisibility(View.VISIBLE);
		} else {
			loHolder.mTop.setVisibility(View.GONE);
		}

		if ("1".equals(subjectInfo.get("recommend"))) {
			loHolder.mRecommentIv.setVisibility(View.VISIBLE);
		} else {
			loHolder.mRecommentIv.setVisibility(View.GONE);
		}
		loHolder.mPictureLayout.removeAllViews();
		try {
			JSONArray jsonarray = new JSONArray((String) subjectInfo.get("images"));
			final List<String> imageUrl = new ArrayList<String>();
			for (int i = 0; i < jsonarray.length(); i++) {
				if (i < Constants.SUBJECT_LIST_IMAGE_LIMIT) {
					LinearLayout.LayoutParams params = new LayoutParams(Constants.IMAGE_WIDTH, Constants.IMAGE_WIDTH);
					params.topMargin = (int) (13.33 * App.metrics.density);
					params.rightMargin = (int) (6.66 * App.metrics.density);
					ImageView imageview = new ImageView(moContext);
					imageview.setScaleType(ScaleType.CENTER_CROP);
					loHolder.mPictureLayout.addView(imageview, params);
					imageUrl.add((String) jsonarray.get(i));
					ImageLoaderUtil.getInstance().loadImage(imageview, jsonarray.get(i));
					imageview.setTag(i);
					imageview.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							ActivityJumpUtil.toImageBrowserActivity(moContext, (Integer) v.getTag(), imageUrl);
						}
					});
				} else {
					imageUrl.add((String) jsonarray.get(i));
				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		loHolder.moTvNickname.setText((String) subjectInfo.get("nickname"));
		if (!TextUtils.isEmpty((String) subjectInfo.get("user_level"))) {
			ImageLoaderUtil.getInstance().loadImage(loHolder.mUserLevel, Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX,
					(String) subjectInfo.get("user_level")));
		}
		if (!TextUtils.isEmpty((String) subjectInfo.get("last_reply_time"))) {
			loHolder.moTvTimer.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong((String) subjectInfo
					.get("last_reply_time"))));
		}
		// 显示标题
		loHolder.mTitle.setText(HtmlUtil.htmlTextDeal(moContext, (String) subjectInfo.get("title"), new NetworkImageGetter(
				loHolder.mTitle, (int) (App.metrics.widthPixels - (16 * 2) * App.metrics.density)), null));
		// loHolder.moTvContent.setText((String)
		// subjectInfo.get("content"));Utils.parseEmotionText(moContext,
		// (String) subjectInfo.get("content"));
		String replaceImage = ((String) subjectInfo.get("content")).replaceAll(Constants.REGX_PHOTO,
				Constants.REPLACE_PHOTO);
		loHolder.moTvContent.setText(HtmlUtil.htmlTextDeal(moContext, replaceImage, new NetworkImageGetter(
						loHolder.moTvContent, (int) (App.metrics.widthPixels - (16 * 2) * App.metrics.density)),
				null));
		// EvtLog.e("", "moSupport " + subjectInfo.get("isSupported"));
		if (Boolean.parseBoolean(subjectInfo.get("isSupported").toString())) {
			loHolder.moSupport.setSelected(true);
		} else {
			loHolder.moSupport.setSelected(false);
		}
		loHolder.moSupport.setTag(subjectInfo.get("isSupported").toString());

		loHolder.mSupportLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (cliOnclickListener != null) {
					cliOnclickListener.onClick(v.findViewById(R.id.item_support), position, loHolder.moTvSupport);
				}
			}
		});
		if (isAdmin) {
			loHolder.mMoreIv.setVisibility(View.VISIBLE);
			loHolder.mMoreIv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (cliOnclickListener != null) {
						cliOnclickListener.onClick(v, position, loHolder.moTvSupport);
					}
				}
			});
		} else {
			loHolder.mMoreIv.setVisibility(View.GONE);
		}

		loHolder.moTvSupport.setText((String) subjectInfo.get("support"));
		loHolder.moTvReply.setText((String) subjectInfo.get("reply_count"));
		loHolder.mTvView.setText((String) subjectInfo.get("view_count"));
		return convertView;
	}

	class Holder {
		private ImageView moIvPhoto, mTop, moSupport, mUserLevel, mMoreIv, mRecommentIv;
		private LinearLayout mPictureLayout;
		private RelativeLayout mSupportLayout;
		private TextView moTvNickname, moTvTimer, mTitle, moTvContent, moTvSupport, moTvReply, mTvView;
	}

	public interface IOnclickListener {
		void onClick(View view, int position, View statusView);
	}

}
