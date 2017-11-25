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

import tv.live.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.DateUtil;

/**
 * 饭圈成员适配器
 */
public class FanMenberListAdapter extends BaseAdapter {
	private Context moContext;
	private List<Map<String, Object>> mlPlayers;

	private OnClickListener mOnClickListener;
	// 是否是圈主
	private boolean isOweer;

	public FanMenberListAdapter(Context poContext, boolean isoweer) {
		moContext = poContext;
		this.isOweer = isoweer;
		mlPlayers = new ArrayList<Map<String, Object>>();
	}

	public void setOnClickListener(OnClickListener l) {
		this.mOnClickListener = l;
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
			convertView = loInflater.inflate(R.layout.item_fan_menber_list, null);
			loHolder = new Holder();
			loHolder.mMenberLogo = (ImageView) convertView.findViewById(R.id.item_menber_logo);
			loHolder.mMenberType = (TextView) convertView.findViewById(R.id.item_menber_type);
			loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_lv_focus_nickname);
			loHolder.mUserIntrotion = (TextView) convertView.findViewById(R.id.item_lv_focus_intro);
			loHolder.mUserLevel = (ImageView) convertView.findViewById(R.id.item_user_level);
			loHolder.mUserBanTip = (TextView) convertView.findViewById(R.id.item_user_ban);
			// loHolder.mMenberManager = (ImageView)
			// convertView.findViewById(R.id.item_user_manage);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		@SuppressWarnings("unchecked")
		final Map<String, Object> subjectInfo = (Map<String, Object>) getItem(position);
		ImageLoaderUtil.with().loadImageTransformRoundCircle(moContext, loHolder.mMenberLogo, (String) subjectInfo.get("headPic"));
		if (Utils.getBooleanFlag(subjectInfo.get("isOwner"))) {
			loHolder.mMenberType.setBackgroundResource(R.drawable.bg_pink);
			loHolder.mMenberType.setText(R.string.commutity_fan_host);
			loHolder.mUserIntrotion.setText(DateUtil.fmtTimeMillsToString(
					1000 * Long.parseLong(subjectInfo.get("addTime").toString()), DateUtil.sdf2)
					+ moContext.getResources().getString(R.string.commutity_fan_menber_status_create));
			// loHolder.mMenberManager.setVisibility(View.GONE);
		} else if (Utils.getBooleanFlag(subjectInfo.get("isAdmin"))) {
			loHolder.mMenberType.setBackgroundResource(R.drawable.bg_blue);
			loHolder.mMenberType.setText(R.string.commutity_menber_admin);
			loHolder.mUserIntrotion.setText(DateUtil.fmtTimeMillsToString(
					1000 * Long.parseLong(subjectInfo.get("addTime").toString()), DateUtil.sdf2)
					+ moContext.getResources().getString(R.string.commutity_fan_menber_status_add));
			// if (isOweer) {
			// loHolder.mMenberManager.setVisibility(View.VISIBLE);
			// loHolder.mMenberManager.setImageResource(R.drawable.btn_chexiao);
			// loHolder.mMenberManager.setTag(true);
			// if (mOnClickListener != null) {
			// loHolder.mMenberManager.setOnClickListener(new
			// View.OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// mOnClickListener.onClick(v, false,
			// subjectInfo.get("uid").toString(), position);
			// }
			// });
			// }
		} else {
			loHolder.mMenberType.setBackgroundResource(R.color.trans);
			loHolder.mMenberType.setText("");
			loHolder.mUserIntrotion.setText(DateUtil.fmtTimeMillsToString(
					1000 * Long.parseLong(subjectInfo.get("addTime").toString()), DateUtil.sdf2)
					+ moContext.getResources().getString(R.string.commutity_fan_menber_status_add));
			// if (isOweer) {
			// loHolder.mMenberManager.setVisibility(View.VISIBLE);
			// loHolder.mMenberManager.setImageResource(R.drawable.btn_guanli);
			// loHolder.mMenberManager.setTag(false);
			// if (mOnClickListener != null) {
			// loHolder.mMenberManager.setOnClickListener(new
			// View.OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// mOnClickListener.onClick(v, true,
			// subjectInfo.get("uid").toString(), position);
			// }
			// });
			// }
			// }
		}

		if (Utils.getBooleanFlag(subjectInfo.get("isUserBanned"))) {
			loHolder.mUserBanTip.setVisibility(View.VISIBLE);
		} else {
			loHolder.mUserBanTip.setVisibility(View.GONE);
		}

		loHolder.moTvNickname.setText(subjectInfo.get("nickname").toString());
		if (!TextUtils.isEmpty((String) subjectInfo.get("level"))) {
			ImageLoaderUtil.with().loadImage(moContext,loHolder.mUserLevel,Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX,
					(String) subjectInfo.get("level")));
		}

		return convertView;
	}

	public interface OnClickListener {
		void onClick(View v, boolean flag, String uid, int position);
	}

	class Holder {
		ImageView mMenberLogo, mMenberManager, mUserLevel;
		TextView mMenberType, moTvNickname, mUserIntrotion, mUserBanTip;
	}

}
