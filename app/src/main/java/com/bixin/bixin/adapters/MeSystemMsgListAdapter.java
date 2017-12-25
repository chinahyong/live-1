package com.bixin.bixin.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import tv.live.bx.R;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.library.util.DateUtil;

/**
 * 我的回复适配器 ClassName: MeSystemMsgListAdapter <br/>
 */
public class MeSystemMsgListAdapter extends MyBaseAdapter {

	/** 是否显示未读消息标志 */
	private boolean isShowUnRead = true;

	private int mMsgTitle;

	public MeSystemMsgListAdapter(Context poContext, int mMsgTitle) {
		super(poContext);
		this.mMsgTitle = mMsgTitle;
	}

	public void setisShowUnRead(boolean isShowUnRead) {
		this.isShowUnRead = isShowUnRead;
	}

	@SuppressLint({ "InflateParams", "NewApi", "ResourceAsColor" })
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Holder loHolder;
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.fragment_system_msg_item, null);

			loHolder = new Holder();

			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_nickname);

			loHolder.moTvTimer = (TextView) convertView.findViewById(R.id.item_time);

			loHolder.moTvContent = (TextView) convertView.findViewById(R.id.item_content);
			loHolder.mUnRead = (TextView) convertView.findViewById(R.id.item_unread);

			// loHolder.mTvSharkage = (TextView)
			// convertView.findViewById(R.id.item_shrink);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		@SuppressWarnings("unchecked")
		final Map<String, String> systemMsgs = (Map<String, String>) getItem(position);
		// 内容显示的行数;
		// loHolder.showRow = 1;
		// 如果内容为空，把标题当成内容显示
		// if (TextUtils.isEmpty(systemMsgs.get("content"))) {
		// loHolder.moTvNickname.setVisibility(View.GONE);
		// loHolder.moTvContent.setText(systemMsgs.get("title"));
		// loHolder.moTvTimer.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong(systemMsgs.get("time"))));
		// float heith = ViewUtils.measureTextViewHeight(moContext,
		// systemMsgs.get("title"), moContext.getResources()
		// .getDimension(R.dimen.a_text_size_42), App.metrics.widthPixels
		// - 32
		// * App.metrics.density);
		// int row_heith = ViewUtils.measureTextViewHeight(moContext,
		// systemMsgs.get("title"), moContext
		// .getResources().getDimension(R.dimen.a_text_size_42),
		// App.metrics.widthPixels - 32
		// * App.metrics.density, 1);
		//
		// loHolder.showRow = (int) Math.ceil(heith / row_heith);
		// } else {
		// loHolder.moTvNickname.setVisibility(View.VISIBLE);
		// loHolder.moTvNickname.setText(systemMsgs.get("title"));
		// loHolder.moTvContent.setText(systemMsgs.get("content"));
		//
		// loHolder.moTvTimer.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong(systemMsgs.get("time"))));
		// float heith = ViewUtils.measureTextViewHeight(moContext,
		// systemMsgs.get("content"), moContext
		// .getResources().getDimension(R.dimen.a_text_size_42),
		// App.metrics.widthPixels - 32
		// * App.metrics.density);
		//
		// int row_heith = ViewUtils.measureTextViewHeight(moContext,
		// systemMsgs.get("content"), moContext
		// .getResources().getDimension(R.dimen.a_text_size_42),
		// App.metrics.widthPixels - 32
		// * App.metrics.density, 1);
		//
		// loHolder.showRow = (int) Math.ceil(heith / row_heith);
		// }
		// loHolder.moTvContent.setLines(1);
		// if (loHolder.showRow > 1) {
		// loHolder.mTvSharkage.setVisibility(View.VISIBLE);
		// loHolder.mTvSharkage.setText(R.string.me_shrinkage_down);
		// loHolder.mTvSharkage.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// if (loHolder.moTvContent.getMaxLines() == 1) {
		// // 如果未读
		// if ("0".equals(systemMsgs.get("status"))) {
		// mReadIdList.put(Integer.parseInt(systemMsgs.get("id")));
		// }
		// loHolder.moTvContent.setLines(loHolder.showRow);
		// loHolder.mTvSharkage.setText(R.string.me_shrinkage_up);
		// } else {
		// loHolder.moTvContent.setLines(1);
		// loHolder.mTvSharkage.setText(R.string.me_shrinkage_down);
		// }
		// }
		// });
		// } else {
		// // 如果未读
		// if ("0".equals(systemMsgs.get("status"))) {
		// mReadIdList.put(Integer.parseInt(systemMsgs.get("id")));
		// }
		// loHolder.mTvSharkage.setVisibility(View.GONE);
		// }

		loHolder.moTvNickname.setText(mMsgTitle);
		loHolder.moTvContent.setText(systemMsgs.get("content"));
		loHolder.moTvTimer.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong(systemMsgs.get("addTime"))));
		if (isShowUnRead) {
			// 如果已读“置灰色”
			if (Constants.COMMON_TRUE.equals(systemMsgs.get("isRead"))) {
				loHolder.mUnRead.setVisibility(View.GONE);
			} else {
				loHolder.mUnRead.setVisibility(View.VISIBLE);
			}
		}
		return convertView;
	}

	class Holder {
		private TextView moTvNickname, moTvTimer, moTvContent, mUnRead;
	}

	public interface IOnclickListener {
		void onClick(View view, int position, TextView num);
	}

}
