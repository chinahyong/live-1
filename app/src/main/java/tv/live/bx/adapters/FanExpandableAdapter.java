/**
 * Project Name:feizao File Name:FanExpandableAdapter.java Package
 * Name:com.efeizao.feizao.adapters Date:2015-11-26上午10:48:12
 */

package tv.live.bx.adapters;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
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
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.ui.HorizontalListView;

/**
 * ClassName:FanExpandableAdapter Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: 2015-11-26 上午10:48:12
 *
 * @author Live
 * @version 1.0
 */
public class FanExpandableAdapter extends BaseExpandableListAdapter {
	public final static int CATEGORY_LIST = 1;
	private ExpandableListView mListView;
	private OnItemClickListener mOnItemClickListener;
	public static final String KEY_GROUP = "groups";
	public static final String KEY_TITLE = "title";
	public static final String KEY_CATEGORY = "category";

	private Context mContext;
	private List<Map<String, Object>> subcategory;

	public FanExpandableAdapter(Context context) {
		this.mContext = context;
		subcategory = new ArrayList<Map<String, Object>>();
	}

	public void setExpandableListView(ExpandableListView mListView) {
		this.mListView = mListView;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mOnItemClickListener = listener;
	}

	/**
	 * 清除绑定的数据，注意，此方法没有刷新UI
	 */
	public void clearData() {
		subcategory.clear();
	}

	/**
	 * 添加数据到第一项，并自动刷新
	 *
	 * @param item
	 */
	public void addFirstItem(Map<String, Object> item) {
		if (item != null) {
			subcategory.add(0, item);
			notifyDataSetChanged();
		}
	}

	// /**
	// * 更新数据到第一项，并自动刷新
	// * @param item
	// */
	// public void updateFirstItem(Map<String, Object> item) {
	// if (item != null) {
	// if (subcategory.size() > 0 &&
	// item.get(KEY_CATEGORY).equals(subcategory.get(0).get(KEY_CATEGORY))) {
	// subcategory.remove(0);
	// subcategory.add(0, item);
	// } else {
	// subcategory.add(0, item);
	// }
	// notifyDataSetChanged();
	// }
	// }

	/**
	 * 添加数据，并自动刷新UI
	 *
	 * @param data
	 */
	public void addData(List<Map<String, Object>> data) {
		if (data != null) {
			subcategory.addAll(data);
			notifyDataSetChanged();
		}
	}

	/**
	 * 合并添加数据，并自动刷新UI (由于数组数量很小，使用冒泡)
	 *
	 * @param data
	 */
	public void mergeAddData(List<Map<String, Object>> data) {
		if (data != null) {
			for (int i = 0; i < data.size(); i++) {
				for (int j = 0; j < subcategory.size(); j++) {
					if (data.get(i).get(KEY_TITLE).equals(subcategory.get(j).get(KEY_TITLE))) {
						((ArrayList) subcategory.get(j).get(KEY_GROUP)).addAll((ArrayList) data.get(i).get(KEY_GROUP));
						break;
					}
				}

			}
			notifyDataSetChanged();
		}
	}

	public List<Map<String, Object>> getData() {
		return subcategory;
	}

	/**
	 * @return 数据是否为空
	 */
	public boolean isDataEmpty() {
		return subcategory.isEmpty();
	}

	@Override
	public boolean isEmpty() {
		return subcategory.isEmpty();
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	// 取得用于显示给定分组的视图. 这个方法仅返回分组的视图对象
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		EvtLog.e("FanExpandableAdapter", "getGroupView groupPosition:" + groupPosition + ",isExpanded:" + isExpanded);
		Holder loHolder;
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_fan_category_layout, null);
			loHolder = new Holder();
			loHolder.mCategoryName = (TextView) convertView.findViewById(R.id.item_fan_catatory_name);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		mListView.expandGroup(groupPosition);
		loHolder.mCategoryName.setText((String) getGroup(groupPosition));
		return convertView;
	}

	// 取得指定分组的ID.该组ID必须在组中是唯一的.必须不同于其他所有ID（分组及子项目的ID）.
	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	// 取得分组数
	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return subcategory.size();
	}

	// 取得与给定分组关联的数据
	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return subcategory.get(groupPosition).get(KEY_TITLE);
	}

	// 取得指定分组的子元素数.
	@Override
	public int getChildrenCount(int groupPosition) {
		// if (groupPosition == CATEGORY_REMEMENT)
		// return 1;
		// TODO Auto-generated method stub
		return ((ArrayList) subcategory.get(groupPosition).get(KEY_GROUP)).size();
	}

	// 取得给定分组中给定子视图的ID. 该组ID必须在组中是唯一的.必须不同于其他所有ID（分组及子项目的ID）.
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return ((ArrayList) subcategory.get(groupPosition).get(KEY_GROUP)).get(childPosition);
	}

	// 取得显示给定分组给定子位置的数据用的视图
	@Override
	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView,
							 ViewGroup parent) {
		EvtLog.e("", "getChildView groupPosition:" + groupPosition + "," + ",childPosition:" + childPosition
				+ ",isLastChild:" + isLastChild);
		Holder loHolder = null;
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_fan_add, null);
			loHolder = new AddHolder();
			// ((AddHolder) loHolder).mItemFanLayout = (LinearLayout)
			// convertView.findViewById(R.id.item_fan_layout);
			((AddHolder) loHolder).mFanLogo = (ImageView) convertView.findViewById(R.id.item_fanquan_logo);
			((AddHolder) loHolder).mFanLogLock = (ImageView) convertView.findViewById(R.id.item_fanquan_logo_lock);
			((AddHolder) loHolder).mFanName = (TextView) convertView.findViewById(R.id.item_fanquan_nickname);
			((AddHolder) loHolder).mFanPeopleNum = (TextView) convertView.findViewById(R.id.item_fanquan_num);
			((AddHolder) loHolder).mFanHostName = (TextView) convertView.findViewById(R.id.item_fanquan_host_name);
			((AddHolder) loHolder).mFanLock = (ImageView) convertView.findViewById(R.id.item_fanquan_lock);
			((AddHolder) loHolder).mFanInvite = (ImageView) convertView.findViewById(R.id.item_fanquan_invite);

			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		setFanCreate(groupPosition, childPosition, loHolder);

		return convertView;
	}

	/**
	 * 我创建的饭圈
	 */
	private void setFanCreate(final int groupPosition, final int childPosition, Holder loHolder) {
		Map<String, Object> item = (Map<String, Object>) ((ArrayList) subcategory.get(groupPosition).get(KEY_GROUP)).get(childPosition);
		ImageLoaderUtil.getInstance().loadHeadPic(mContext, ((AddHolder) loHolder).mFanLogo, (String) item.get("logo"));
		// [status] -1:删除/0:禁用/1:启用/2:未审核/3:审核不通过/4:冻结
		if (Constants.FAN_STATUS_FREEZE.equals(item.get("status"))) {
			((AddHolder) loHolder).mFanLogLock.setVisibility(View.VISIBLE);
			int need = Integer.parseInt((String) item.get("activeNeed"));
			int total = Integer.parseInt((String) item.get("memberTotal"));
			int tt = need - total;
			int pre = tt * 100 / need;
			((AddHolder) loHolder).mFanLogLock.setBackgroundDrawable(new BitmapDrawable(BitmapHelper
					.getArcRadiuProgress(Utils.dip2px(mContext, 54f),
							mContext.getResources().getColor(R.color.a_bg_color_66000000), pre)));
			((AddHolder) loHolder).mFanPeopleNum.setText(String.format(
					mContext.getResources().getString(R.string.commutity_fan_unlock_text), item.get("memberTotal"),
					item.get("activeNeed"), tt));

			((AddHolder) loHolder).mFanLock.setBackgroundResource(R.drawable.quan_lock_pre);
			((AddHolder) loHolder).mFanInvite.setImageResource(R.drawable.btn_invite_selector);
			// ((AddHolder) loHolder).mFanInvite.setOnClickListener(new
			// View.OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// if (mOnItemClickListener != null)
			// mOnItemClickListener.onClick(v, groupPosition, childPosition);
			// }
			// });
		} else {
			((AddHolder) loHolder).mFanLogLock.setVisibility(View.INVISIBLE);
			((AddHolder) loHolder).mFanPeopleNum.setText(item.get("memberTotal") + "人");
			((AddHolder) loHolder).mFanLock.setBackgroundResource(R.drawable.quan_lock_nor);
			((AddHolder) loHolder).mFanInvite.setImageResource(R.drawable.btn_more_right);
		}
		((AddHolder) loHolder).mFanName.setText((String) item.get("name"));
		((AddHolder) loHolder).mFanHostName.setText((String) item.get("nickname"));

	}

	class Holder {
		TextView mCategoryName;
		int groupType;
	}

	class RecommentHolder extends Holder {
		HorizontalListView listView;
	}

	class AddHolder extends Holder {
		ImageView mFanLogo, mFanLogLock;
		TextView mFanName, mFanPeopleNum, mFanHostName;
		ImageView mFanLock, mFanInvite;
	}

	public interface OnItemClickListener {
		void onClick(View view, int groupType, int groupPosition, int position);
	}
}
