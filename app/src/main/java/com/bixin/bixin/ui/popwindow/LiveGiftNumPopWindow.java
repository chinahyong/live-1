package com.bixin.bixin.ui.popwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;

import tv.live.bx.R;
import com.bixin.bixin.adapters.GiftsNumAdapter;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.TelephoneUtil;

import static com.bixin.bixin.adapters.GiftsNumAdapter.mList;

/**
 * 礼物数量popupWindow
 */
public class LiveGiftNumPopWindow extends PopupWindow {
	private View conentView;
	private ListView listView;
	private SimpleAdapter adapter;
	private GiftsNumAdapter.IGiftNumItemListener mGiftNumItemListener;
	private int popHeight;

	public LiveGiftNumPopWindow(Activity context, final GiftsNumAdapter.IGiftNumItemListener mGiftNumItemListener) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		conentView = inflater.inflate(R.layout.pop_gift_num_select_layout, null);
		this.mGiftNumItemListener = mGiftNumItemListener;
		float denity = TelephoneUtil.getDisplayMetrics().density;
		// 设置SelectPicPopupWindow的View
		this.setContentView(conentView);
		// 设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth((int) (132.33 * denity));

		// 设置SelectPicPopupWindow弹出窗体可点击
		popHeight = (int) ((12 + 32 * GiftsNumAdapter.mList.size()) * denity + GiftsNumAdapter.mList.size());
		// 设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(popHeight);
		this.setBackgroundDrawable(new ColorDrawable(0x00000000));

		listView = (ListView) conentView.findViewById(R.id.send_gifts_num_lv);
		adapter = new SimpleAdapter(context, GiftsNumAdapter.mList, R.layout.item_send_gifts_num, new String[]{"giftNum",
				"giftNumName"}, new int[]{R.id.sen_gifts_num, R.id.sen_gifts_num_name});
		listView.setAdapter(adapter);
		listView.setFocusableInTouchMode(true);
		listView.setFocusable(true);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (mGiftNumItemListener != null) {
					mGiftNumItemListener.onGiftNum(mList.get(arg2));
				}
				LiveGiftNumPopWindow.this.dismiss();
			}

		});
		// 实例化一个ColorDrawable颜色为半透明
		// ColorDrawable dw = new ColorDrawable(0000000000);
		// 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
		// this.setBackgroundDrawable(dw);
		// mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
		// 设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.Popup_Animation_Above_UpDown);
	}

	/**
	 * 显示某View上方
	 */
	public void showPopUp(View v) {
		int[] location = new int[2];
		v.getLocationOnScreen(location);
		EvtLog.d("", "showPopUp height:" + popHeight);
		int offset = (v.getWidth() - this.getWidth()) / 2;
		this.showAtLocation(v, Gravity.NO_GRAVITY, location[0] + offset, location[1] - popHeight - 20);
		this.setFocusable(true);
		this.setTouchable(true);
		this.setBackgroundDrawable(new ColorDrawable(0x00000000));
		this.setOutsideTouchable(true);
		// 刷新状态
		this.update();
	}

	/**
	 * 显示popupWindow
	 *
	 * @param parent
	 */
	public void showPopupWindow(View parent) {
		if (!this.isShowing()) {
			// 以下拉方式显示popupwindow
			this.showAsDropDown(parent, parent.getLayoutParams().width / 2, 18);
		} else {
			this.dismiss();
		}
	}

}

