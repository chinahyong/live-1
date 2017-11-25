package tv.live.bx.ui.popwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import tv.live.bx.R;
import tv.live.bx.common.Utils;

/**
 * Created by valar on 2017/3/27.
 * detail 筛选按钮点出来的Popupwindow
 */

public class FilterPopupWindow extends PopupWindow implements View.OnClickListener {
	private View view;
	private Activity mActivty ;
	public LinearLayout mFilterSended;
	public LinearLayout mFilterNeverSend;
	public LinearLayout mFilterAll;
	private FitlerClickListener mFilerClickListener;
	public TextView mFilterAllText;
	public TextView mFilterAllTextNum;

	public FilterPopupWindow(Activity context,FitlerClickListener mFilerClickListener) {
		super(context);
		mActivty = context;
		this.mFilerClickListener = mFilerClickListener;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.pop_filter, null);
		//設置SelectPicPopupWindow的view
		this.setContentView(view);
		//设置弹出的宽
		this.setWidth(Utils.dip2px(mActivty, Float.parseFloat(165 + "")));
		//设置弹出的高
		this.setHeight(Utils.dip2px(mActivty, Float.parseFloat(165 + "")));
		// 设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		//刷新状态
		this.update();
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0x00000000);
		// 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
		this.setBackgroundDrawable(dw);
		//--------------------------------------------
		mFilterSended = (LinearLayout) view.findViewById(R.id.filter_sended);
		mFilterNeverSend = (LinearLayout) view.findViewById(R.id.filter_never_Send);
		mFilterAll = (LinearLayout) view.findViewById(R.id.filter_all);
		mFilterAllText = (TextView) view.findViewById(R.id.filter_all_text);
		mFilterAllTextNum = (TextView) view.findViewById(R.id.filter_all_text_num);

		//-------------------------------------------
		mFilterSended.setOnClickListener(this);
		mFilterNeverSend.setOnClickListener(this);
		mFilterAll.setOnClickListener(this);

	}

	public void showPopupWindow(View view) {
		if (!this.isShowing()) {
			this.showAsDropDown(view, Utils.dip2px(mActivty, Float.parseFloat((-13) + "")), 0);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.filter_sended:
				if(mFilerClickListener != null){
					mFilerClickListener.onClick(view);
				}
				break;
			case R.id.filter_never_Send:
				if(mFilerClickListener != null){
					mFilerClickListener.onClick(view);
				}
				break;
			case R.id.filter_all:
				if(mFilerClickListener != null){
					mFilerClickListener.onClick(view);
				}
				break;
		}
	}

	public interface FitlerClickListener {
		void onClick(View view);
	}

}
