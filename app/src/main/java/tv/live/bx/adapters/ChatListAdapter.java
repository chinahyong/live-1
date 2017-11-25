package tv.live.bx.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Map;

import tv.live.bx.R;
import tv.live.bx.library.util.EvtLog;

public class ChatListAdapter extends BaseAdapter {
	public static final int MAX_SIZE = 200;
	public static final String USER_COME_IN = " 来了";
	public static final String USER_SEND_FLOWER = " 送了心";
	public static final String USER_SHARE_ROOM = " 分享了这个直播";
	public static final String USER_FOCUS_ANCHOR = " 关注了主播";

	private Context mContext;
	private LinkedList<Map<String, CharSequence>> mlData = new LinkedList<Map<String, CharSequence>>();

	public ChatListAdapter(Context poContext) {
		mContext = poContext;
	}

	/**
	 * 插入数据
	 *
	 * @param piIndex
	 * @param pmData
	 */
	public void insertData(int piIndex, Map<String, CharSequence> pmData) {
		mlData.add(piIndex, pmData);
		if (mlData.size() > MAX_SIZE) {
			mlData.remove(0);
		}
		notifyDataSetChanged();
	}

	/**
	 * 更新数据
	 *
	 * @param pmData
	 */
	public void updateLastData(Map<String, CharSequence> pmData) {
		mlData.removeLast();
		mlData.addLast(pmData);
		if (mlData.size() > MAX_SIZE) {
			mlData.removeFirst();
		}
		notifyDataSetChanged();
	}

	public void clearData() {
		mlData.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mlData.size();
	}

	@Override
	public Object getItem(int position) {
		return mlData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		EvtLog.e("ChatListAdapter", "positin:" + position);
		Holder loHolder = null;
		if (convertView == null) {
			loHolder = new Holder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_general, null);
			loHolder.moTvContent = (TextView) convertView.findViewById(R.id.item_chat_left_tv);
			convertView.setTag(loHolder);
		}
		loHolder = (Holder) convertView.getTag();
		Map<String, CharSequence> data = (Map<String, CharSequence>) getItem(position);
		loHolder.moTvContent.setText(data.get("content"));
		/* 该方法内部源码 设置了 setLongClickable(true)，所以讲setLongClickable(false)放到后面 */
		loHolder.moTvContent.setMovementMethod(LinkMovementMethod.getInstance());
		/* 解决umeng bug:禁止长按 兼容小米5.0以上、魅族5.0以上SpannableString长按崩溃*/
		loHolder.moTvContent.setLongClickable(false);
		return convertView;
	}

	class Holder {
		TextView moTvContent;
	}

}
