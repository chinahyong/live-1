package tv.live.bx.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.efeizao.bx.R;

/**
 * 礼物GridView的适配器
 */
public class GiftsNumAdapter extends BaseAdapter {
	public static final float APP_PAGE_SIZE = 9;// 每一页装载数据的大小
	private Context mContext;
	private IGiftNumItemListener lisGridItemOnClick;
	/** 礼物数为“其他” */
	public static String GIFT_NUM_OTHER = "自定义";
	private int page;
	public static List<Map<String, String>> mList = new ArrayList<>();
	/*********************************** 公用方法 ***********************************/
	static {
		Map<String, String> map = new HashMap<>();

		map.put("giftNum", "1");
		map.put("giftNumName", "一心一意");
		mList.add(map);

		map = new HashMap<>();
		map.put("giftNum", "10");
		map.put("giftNumName", "十全十美");
		mList.add(map);

		map = new HashMap<>();
		map.put("giftNum", "30");
		map.put("giftNumName", "想你");
		mList.add(map);

		map = new HashMap<>();
		map.put("giftNum", "66");
		map.put("giftNumName", "一切顺利");
		mList.add(map);

		map = new HashMap<>();
		map.put("giftNum", "188");
		map.put("giftNumName", "要抱抱");
		mList.add(map);

		map = new HashMap<>();
		map.put("giftNum", "520");
		map.put("giftNumName", "我爱你");
		mList.add(map);

		map = new HashMap<>();
		map.put("giftNum", "1314");
		map.put("giftNumName", "一生一世");
		mList.add(map);

		map = new HashMap<>();
		map.put("giftNum", "");
		map.put("giftNumName", "自定义");
		mList.add(map);

	}

	/**
	 * 构造方法
	 * 
	 * @param context 上下文
	 * @param list 所有APP的集合
	 * @param page 当前页
	 */
	public GiftsNumAdapter(Context context, IGiftNumItemListener lisGridNumItemOnClick, int page) {
		mContext = context;
		this.lisGridItemOnClick = lisGridNumItemOnClick;
		this.page = page;
		// 根据当前页计算装载的应用，每页只装载9个
		int i = page * (int) APP_PAGE_SIZE;// 当前页的其实位置
		int iEnd = i + (int) APP_PAGE_SIZE;// 所有数据的结束位置
	}

	public int getCount() {
		return mList.size();
	}

	public Object getItem(int position) {
		return mList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, final ViewGroup parent) {
		Holder loHolder;
		@SuppressWarnings("unchecked")
		final Map<String, String> giftNumInfo = mList.get(position);
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_gift_grid_num, null);
			loHolder = new Holder();
			loHolder.mTvGiftNum = (TextView) convertView.findViewById(R.id.item_gift_tv_num);
			loHolder.mTvGiftNumName = (TextView) convertView.findViewById(R.id.item_gift_tv_num_name);
			loHolder.mLineRight = convertView.findViewById(R.id.item_line_right);
			loHolder.mLineRight.setVisibility((position + 1) % 3 == 0 ? View.INVISIBLE : View.VISIBLE);
			convertView.setTag(loHolder);
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (lisGridItemOnClick != null) {
						lisGridItemOnClick.onGiftNum(mList.get(position));
					}
				}
			});
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		loHolder.mTvGiftNum.setText(giftNumInfo.get("giftNum"));
		if (TextUtils.isEmpty(giftNumInfo.get("giftNumName"))) {
			loHolder.mTvGiftNumName.setVisibility(View.GONE);
		} else {
			loHolder.mTvGiftNumName.setVisibility(View.VISIBLE);
			loHolder.mTvGiftNumName.setText(giftNumInfo.get("giftNumName"));
		}
		return convertView;
	}
	class Holder {
		TextView mTvGiftNumName, mTvGiftNum;
		View mLineRight;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	/**
	 * 礼物数量返回接口
	 */
	public interface IGiftNumItemListener {
		void onGiftNum(Map<String, String> giftNumInfo);
	}

	private void onSetIGiftNumItemOnClick(IGiftNumItemListener lisGridItemOnClick) {
		this.lisGridItemOnClick = lisGridItemOnClick;
	}
}
