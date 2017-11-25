package tv.live.bx.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
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
import tv.live.bx.library.util.BitmapHelper;

/**
 * 饭圈列表适配器: FanListAdapter <br/>
 */
public class FanListAdapter extends BaseAdapter {

	private Context moContext;
	private List<Map<String, Object>> mlPlayers;

	private OnItemClickListener mOnItemClickListener;

	public FanListAdapter(Context poContext) {
		moContext = poContext;
		mlPlayers = new ArrayList<Map<String, Object>>();
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mOnItemClickListener = listener;
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

			convertView = loInflater.inflate(R.layout.item_fan_add, null);
			loHolder = new Holder();
			loHolder.mFanLogo = (ImageView) convertView.findViewById(R.id.item_fanquan_logo);
			loHolder.mFanLogLock = (ImageView) convertView.findViewById(R.id.item_fanquan_logo_lock);
			loHolder.mFanName = (TextView) convertView.findViewById(R.id.item_fanquan_nickname);
			loHolder.mFanPeopleNum = (TextView) convertView.findViewById(R.id.item_fanquan_num);
			loHolder.mFanHostName = (TextView) convertView.findViewById(R.id.item_fanquan_host_name);
			loHolder.mFanLock = (ImageView) convertView.findViewById(R.id.item_fanquan_lock);
			loHolder.mFanInvite = (ImageView) convertView.findViewById(R.id.item_fanquan_invite);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> item = (Map<String, Object>) getItem(position);
		ImageLoaderUtil.with().loadImageTransformRoundCircle(moContext, loHolder.mFanLogo, (String) item.get("logo"));
		// [status] -1:删除/0:禁用/1:启用/2:未审核/3:审核不通过/4:冻结
		if (Constants.FAN_STATUS_FREEZE.equals(item.get("status"))) {
			loHolder.mFanLogLock.setVisibility(View.VISIBLE);
			int need = Integer.parseInt((String) item.get("activeNeed"));
			int total = Integer.parseInt((String) item.get("memberTotal"));
			int tt = need - total;
			int pre = tt * 100 / need;
			loHolder.mFanLogLock.setBackgroundDrawable(new BitmapDrawable(BitmapHelper.getArcRadiuProgress(
					Utils.dip2px(moContext, 51f), moContext.getResources().getColor(R.color.a_bg_color_66000000), pre)));
			loHolder.mFanPeopleNum.setText(String.format(
					moContext.getResources().getString(R.string.commutity_fan_unlock_text), item.get("memberTotal"),
					item.get("activeNeed"), tt));

			loHolder.mFanLock.setBackgroundResource(R.drawable.quan_lock_pre);

			// 如果已加入
			if (Constants.COMMON_TRUE.equals(item.get("joined"))) {
				loHolder.mFanInvite.setImageResource(R.drawable.btn_invite_selector);
			} else {
				loHolder.mFanInvite.setImageResource(R.drawable.bg_add_fanquan_selector);
			}
			loHolder.mFanInvite.setVisibility(View.VISIBLE);
		} else {
			loHolder.mFanLogLock.setVisibility(View.INVISIBLE);
			loHolder.mFanPeopleNum.setText(item.get("memberTotal") + "人");
			loHolder.mFanLock.setBackgroundResource(R.drawable.quan_lock_nor);

			// 如果已加入
			if (Constants.COMMON_TRUE.equals(item.get("joined"))) {
				loHolder.mFanInvite.setImageResource(R.drawable.btn_more_right);
			} else {
				loHolder.mFanInvite.setImageResource(R.drawable.bg_add_fanquan_selector);
			}
		}

		loHolder.mFanName.setText((String) item.get("name"));
		loHolder.mFanHostName.setText((String) item.get("nickname"));

		loHolder.mFanInvite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnItemClickListener != null)
					mOnItemClickListener.onClick(v, position);
			}
		});
		return convertView;
	}

	class Holder {
		ImageView mFanLogo, mFanLogLock;
		TextView mFanName, mFanPeopleNum, mFanHostName;
		ImageView mFanLock, mFanInvite;
	}

	public interface OnItemClickListener {
		void onClick(View view, int position);
	}

}
