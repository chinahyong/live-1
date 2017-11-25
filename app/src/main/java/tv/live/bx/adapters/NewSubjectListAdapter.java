package tv.live.bx.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.text.Html.TagHandler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.DateUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.HtmlUtil;
import tv.live.bx.util.ActivityJumpUtil;

/**
 * 帖子列表适配器 ClassName: SubjectListAdapter <br/>
 */
public class NewSubjectListAdapter extends MyBaseAdapter<String, Object> {

	private int mImageViewHight;

	private IOnclickListener cliOnclickListener;

	private boolean isShowTop = false;

	private boolean isShowGroup = true;

	private TagHandler mTagHandler;

	public NewSubjectListAdapter(Context poContext) {
		super(poContext);
		mImageViewHight = (int) (FeizaoApp.metrics.widthPixels - FeizaoApp.metrics.density * 12 * 2);
	}

	public void setTagHandler(TagHandler mTagHandler) {
		this.mTagHandler = mTagHandler;
	}

	public void setOnClickListener(IOnclickListener cliOnclickListener) {
		this.cliOnclickListener = cliOnclickListener;
	}

	public void setIsShowTop(boolean isShowTop) {
		this.isShowTop = isShowTop;
	}

	public void setIsShowGroup(boolean isShowGroup) {
		this.isShowGroup = isShowGroup;
	}

	@SuppressLint({"InflateParams", "NewApi"})
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		EvtLog.e("", "getView " + position);
		final Holder loHolder;
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_new_subject_list, null);
			loHolder = new Holder();
			loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_photo);
			loHolder.moIvPhotoV = (ImageView) convertView.findViewById(R.id.item_photo_v);

			loHolder.mPicture = (ImageView) convertView.findViewById(R.id.item_picture_grid);
			loHolder.mIvPhotoLoogTip = (ImageView) convertView.findViewById(R.id.item_picture_long_tip);

			loHolder.mTop = (ImageView) convertView.findViewById(R.id.item_top);
			loHolder.mRecommentIv = (ImageView) convertView.findViewById(R.id.item_recomment);
			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_nickname);
			loHolder.mUserLevel = (ImageView) convertView.findViewById(R.id.item_userlevel);
			loHolder.mForumTitle = (TextView) convertView.findViewById(R.id.item_moudle_text);

			loHolder.moTvTimer = (TextView) convertView.findViewById(R.id.item_time);
			loHolder.moTvContent = (TextView) convertView.findViewById(R.id.item_content);
			loHolder.moSupport = (ImageView) convertView.findViewById(R.id.item_support);
			loHolder.moTvSupport = (TextView) convertView.findViewById(R.id.item_support_num);

			loHolder.moTvReply = (TextView) convertView.findViewById(R.id.item_replay_num);
			loHolder.mShare = (ImageView) convertView.findViewById(R.id.item_share);
			loHolder.mMore = (ImageView) convertView.findViewById(R.id.item_more);
			LayoutParams layoutParams = (LayoutParams) loHolder.mPicture.getLayoutParams();
			layoutParams.height = mImageViewHight;
			loHolder.mPicture.setLayoutParams(layoutParams);

			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> subjectInfo = (Map<String, Object>) getItem(position);
		ImageLoaderUtil.with().loadImageTransformRoundCircle(mContext, loHolder.moIvPhoto, (String) subjectInfo.get("headPic"));
		loHolder.moTvNickname.setText((String) subjectInfo.get("nickname"));
		// 去掉内容里面的 image标签+后面的\n
		String replaceImage = ((String) subjectInfo.get("content")).replaceAll(Constants.REGX_PHOTO, "");
		if (TextUtils.isEmpty(replaceImage)) {
			loHolder.moTvContent.setVisibility(View.GONE);
		} else {
			loHolder.moTvContent.setVisibility(View.VISIBLE);
			loHolder.moTvContent.setText(HtmlUtil.htmlTextDeal(mContext, replaceImage, null, mTagHandler));
		}
		if (!TextUtils.isEmpty((String) subjectInfo.get("lastReplyTime"))) {
			loHolder.moTvTimer.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong((String) subjectInfo
					.get("lastReplyTime"))));
		}
		if (!TextUtils.isEmpty((String) subjectInfo.get("level"))) {
			ImageLoaderUtil.with().loadImage(mContext,loHolder.mUserLevel,Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX,
					(String) subjectInfo.get("level")));
		}

		if (isShowTop) {
			if (Utils.getBooleanFlag(subjectInfo.get("isTop"))) {
				loHolder.mTop.setVisibility(View.VISIBLE);
			} else {
				loHolder.mTop.setVisibility(View.GONE);
			}
			if (Utils.getBooleanFlag(subjectInfo.get("isNice"))) {
				loHolder.mRecommentIv.setVisibility(View.VISIBLE);
			} else {
				loHolder.mRecommentIv.setVisibility(View.GONE);
			}
		}
		if (isShowGroup) {
			loHolder.mForumTitle.setVisibility(View.VISIBLE);
			loHolder.mForumTitle.setText((String) subjectInfo.get("groupName"));
		}
		try {
			JSONArray jsonarray = new JSONArray((String) subjectInfo.get("pics"));
			if (jsonarray.length() <= 0) {
				loHolder.mPicture.setVisibility(View.GONE);
				loHolder.mIvPhotoLoogTip.setVisibility(View.GONE);
			} else {
				final List<String> imageUrl = new ArrayList<String>();
				for (int i = 0; i < jsonarray.length(); i++) {
					if (i < Constants.SUBJECT_LIST_IMAGE_LIMIT) {
						loHolder.mPicture.setVisibility(View.VISIBLE);
						String url = (String) jsonarray.get(i);
						if (Utils.getBooleanFlag(Uri.parse(url).getQueryParameter("long"))) {
							loHolder.mIvPhotoLoogTip.setVisibility(View.VISIBLE);
						} else {
							loHolder.mIvPhotoLoogTip.setVisibility(View.GONE);
						}
						imageUrl.add(url);
						ImageLoaderUtil.with().loadImage(mContext, loHolder.mPicture, url);
						loHolder.mPicture.setTag(i);
						loHolder.mPicture.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								ActivityJumpUtil.toImageBrowserActivity(mContext, (Integer) v.getTag(), imageUrl);
							}
						});
					} else {
						imageUrl.add((String) jsonarray.get(i));
					}

				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (Boolean.parseBoolean(subjectInfo.get("supported").toString())) {
			loHolder.moSupport.setSelected(true);
		} else {
			loHolder.moSupport.setSelected(false);
		}
		loHolder.moSupport.setTag(subjectInfo.get("supported").toString());
		loHolder.moIvPhotoV.setVisibility(Utils.getBooleanFlag(subjectInfo.get("verified"))
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
		loHolder.mForumTitle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (cliOnclickListener != null) {
					cliOnclickListener.onClick(v, position, null);
				}
			}
		});
		loHolder.moSupport.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (cliOnclickListener != null) {
					cliOnclickListener.onClick(v, position, loHolder.moTvSupport);
				}
			}
		});

		loHolder.mShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (cliOnclickListener != null) {
					cliOnclickListener.onClick(v, position, null);
				}
			}
		});

		loHolder.mMore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (cliOnclickListener != null) {
					cliOnclickListener.onClick(v, position, null);
				}
			}
		});
		loHolder.moTvSupport.setText((String) subjectInfo.get("supportNum"));
		loHolder.moTvReply.setText((String) subjectInfo.get("replyNum"));
		return convertView;
	}

	class Holder {
		private ImageView moIvPhoto, mPicture, moSupport, mShare, mMore, mUserLevel, mRecommentIv, mTop, moIvPhotoV,
				mIvPhotoLoogTip;
		private TextView moTvNickname, moTvContent, moTvSupport, moTvReply, mForumTitle, moTvTimer;
	}

	public interface IOnclickListener {
		void onClick(View view, int position, View statusView);
	}

}
