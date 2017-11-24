package tv.live.bx.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.efeizao.bx.R;
import tv.live.bx.activities.GroupManageLogActivity;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.DateUtil;

import java.util.Map;

/**
 * 我的回复适配器 ClassName: MeSystemMsgListAdapter <br/>
 */
public class GroupManageListAdapter extends MyBaseAdapter<String, String> {

	public GroupManageListAdapter(Context poContext) {
		super(poContext);
	}

	@SuppressLint({"InflateParams", "NewApi", "ResourceAsColor"})
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Holder loHolder;
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_group_manage_item, null);

			loHolder = new Holder();
			loHolder.mUserPhoto = (ImageView) convertView.findViewById(R.id.item_photo);
			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_nickname);

			loHolder.moTvTimer = (TextView) convertView.findViewById(R.id.item_time);

			loHolder.moTvContent = (TextView) convertView.findViewById(R.id.item_content);
			loHolder.mContentLayout = (RelativeLayout) convertView.findViewById(R.id.item_content_layout);

			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		@SuppressWarnings("unchecked")
		final Map<String, String> mData = (Map<String, String>) getItem(position);
		loHolder.moTvNickname.setText(mData.get("operator"));
		loHolder.moTvContent.setText(String.format(
				mContext.getResources().getString(R.string.commutity_manage_log_tip), mData.get("action"),
				mData.get("entity")));
		loHolder.moTvTimer.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong(mData.get("addTime"))));
		ImageLoaderUtil.with().loadImageTransformRoundCircle(mContext, loHolder.mUserPhoto, mData.get("headPic"));
		if (GroupManageLogActivity.ENTITY_TYPE_POST.equals(mData.get("entityType"))
				&& !GroupManageLogActivity.ACTION_TYPE_REMOVE_POST.equals(mData.get("actionType"))) {
			loHolder.mContentLayout.setEnabled(true);
		} else {
			loHolder.mContentLayout.setEnabled(false);
		}

		return convertView;
	}

	class Holder {
		TextView moTvNickname, moTvTimer, moTvContent;
		ImageView mUserPhoto;
		RelativeLayout mContentLayout;
	}

	public interface IOnclickListener {
		void onClick(View view, int position, TextView num);
	}

}
