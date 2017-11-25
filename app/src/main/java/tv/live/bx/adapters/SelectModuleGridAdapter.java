package tv.live.bx.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import tv.live.bx.R;


/**
 * 选择GridView的适配器
 */
public class SelectModuleGridAdapter extends BaseAdapter {
	private List<Map<String, String>> giftsData;
	private Context mContext;

	private ISelectGridItemOnClick lisGridItemOnClick;

	/**
	 * 构造方法
	 * 
	 * @param context 上下文
	 * @param list 所有APP的集合
	 */
	public SelectModuleGridAdapter(Context context, List<Map<String, String>> list,
			ISelectGridItemOnClick lisGridItemOnClick) {
		mContext = context;
		this.lisGridItemOnClick = lisGridItemOnClick;
		this.giftsData = list;
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return giftsData.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return giftsData.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void setData(List<Map<String, String>> giftsData) {
		this.giftsData = giftsData;
		this.notifyDataSetChanged();
	}

	public View getView(final int position, View convertView, final ViewGroup parent) {
		Holder loHolder;
		@SuppressWarnings("unchecked")
		final Map<String, String> giftInfo = giftsData.get(position);
		if (convertView == null) {
			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_select_moudle, null);

			loHolder = new Holder();
			loHolder.itemContent = (TextView) convertView.findViewById(R.id.item_text);
			convertView.setTag(loHolder);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (lisGridItemOnClick != null) {
						lisGridItemOnClick.onClick(parent, v, position);
					}
				}
			});
		} else {
			loHolder = (Holder) convertView.getTag();
		}

		loHolder.itemContent.setText(giftInfo.get("title"));
		return convertView;
	}

	/** 用于点击礼物项的回调事件 */
	public interface ISelectGridItemOnClick {
		void onClick(ViewGroup parent, View v, int position);
	}

	class Holder {
		TextView itemContent;
	}
}
