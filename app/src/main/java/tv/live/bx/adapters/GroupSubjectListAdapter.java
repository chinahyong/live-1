package tv.live.bx.adapters;

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

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.DateUtil;
import tv.live.bx.library.util.HtmlUtil;
import tv.live.bx.library.util.NetworkImageGetter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 饭圈帖子列表适配器 ClassName: GroupSubjectListAdapter <br/>
 */
public class GroupSubjectListAdapter extends BaseAdapter {

	private Context moContext;
	private List<Map<String, Object>> mlPlayers;

	private IOnclickListener cliOnclickListener;

	public static final String TYPE_POST = "post";
	private final String TYPE_GROUP = "timeline";

	/**
	 * 列表是否显示栏目名称
	 */
	private boolean isShowFroum = false;

	public GroupSubjectListAdapter(Context poContext, IOnclickListener cliOnclickListener) {
		moContext = poContext;
		mlPlayers = new ArrayList<Map<String, Object>>();
		this.cliOnclickListener = cliOnclickListener;
	}

	public void setIsShowFroum(boolean isShowFroum) {
		this.isShowFroum = isShowFroum;
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

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Holder loHolder;
		@SuppressWarnings("unchecked")
		Map<String, Object> subjectInfo = (Map<String, Object>) getItem(position);
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
			loHolder.mForumTitle = (TextView) convertView.findViewById(R.id.item_moudle_text);

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
		ImageLoaderUtil.with().loadImageTransformRoundCircle(moContext, loHolder.moIvPhoto, (String) subjectInfo.get("headPic"));
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
					params.topMargin = (int) (13.33 * FeizaoApp.metrics.density);
					params.rightMargin = (int) (6.66 * FeizaoApp.metrics.density);
					ImageView imageview = new ImageView(moContext);
					imageview.setScaleType(ScaleType.CENTER_CROP);
					loHolder.mPictureLayout.addView(imageview, params);
					imageUrl.add((String) jsonarray.get(i));
					ImageLoaderUtil.with().loadImage(moContext, imageview, (String) jsonarray.get(i));
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
		if (isShowFroum) {
			loHolder.mForumTitle.setVisibility(View.VISIBLE);
			loHolder.mForumTitle.setText(String.format(
					moContext.getResources().getString(R.string.commutity_catelory_text),
					subjectInfo.get("group_title")));
		}

		loHolder.moTvNickname.setText((String) subjectInfo.get("nickname"));
		if (!TextUtils.isEmpty((String) subjectInfo.get("user_level"))) {
			ImageLoaderUtil.with().loadImage(moContext, loHolder.mUserLevel, Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX,
					(String) subjectInfo.get("user_level")));
		}
		if (!TextUtils.isEmpty((String) subjectInfo.get("last_reply_time"))) {
			loHolder.moTvTimer.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong((String) subjectInfo
					.get("last_reply_time"))));
		}
		// 显示标题
		loHolder.mTitle.setText(HtmlUtil.htmlTextDeal(moContext, (String) subjectInfo.get("title"), new NetworkImageGetter(
				loHolder.mTitle, (int) (FeizaoApp.metrics.widthPixels - (16 * 2) * FeizaoApp.metrics.density)), null));
		// loHolder.moTvContent.setText((String)
		// subjectInfo.get("content"));Utils.parseEmotionText(moContext,
		// (String) subjectInfo.get("content"));
		String replaceImage = ((String) subjectInfo.get("content")).replaceAll(Constants.REGX_PHOTO,
				Constants.REPLACE_PHOTO);
		loHolder.moTvContent.setText(HtmlUtil.htmlTextDeal(moContext, replaceImage, new NetworkImageGetter(
						loHolder.moTvContent, (int) (FeizaoApp.metrics.widthPixels - (16 * 2) * FeizaoApp.metrics.density)),
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
		loHolder.moTvSupport.setText((String) subjectInfo.get("support"));
		loHolder.moTvReply.setText((String) subjectInfo.get("reply_count"));
		loHolder.mTvView.setText((String) subjectInfo.get("view_count"));
		return convertView;
	}

	class Holder {
		ImageView moIvPhoto, mTop, moSupport, mUserLevel, mMoreIv, mRecommentIv;
		LinearLayout mPictureLayout;
		RelativeLayout mSupportLayout;
		TextView moTvNickname, moTvTimer, mTitle, moTvContent, moTvSupport, moTvReply, mTvView, mForumTitle;
	}

	public interface IOnclickListener {
		void onClick(View view, int position, TextView num);
	}

}
