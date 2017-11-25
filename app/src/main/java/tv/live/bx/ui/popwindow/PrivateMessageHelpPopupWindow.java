package tv.live.bx.ui.popwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import tv.live.bx.R;
import tv.live.bx.common.Utils;

/**
 * Created by valar on 2017/3/27.
 * details 关注我的，点击？弹出的popupwindow
 */

public class PrivateMessageHelpPopupWindow extends PopupWindow{
	private View view;
	private Activity mActivty ;

	public PrivateMessageHelpPopupWindow(Activity context) {
		super(context);
		mActivty = context;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.pop_private_message_help, null);
		//設置SelectPicPopupWindow的view
		this.setContentView(view);
		//设置弹出的宽
		this.setWidth(Utils.dip2px(mActivty, Float.parseFloat(292 + "")));
		//设置弹出的高
		this.setHeight(Utils.dip2px(mActivty, Float.parseFloat(400 + "")));
		// 设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		//刷新状态
		this.update();
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0x00000000);
		// 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
		this.setBackgroundDrawable(dw);
	}

	public void showPopupWindow(View view) {
		if (!this.isShowing()) {
			showAtLocation(mActivty.getWindow().getDecorView(), Gravity.CENTER,0,0);
		}
	}

}
