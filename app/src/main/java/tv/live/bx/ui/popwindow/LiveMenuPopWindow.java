package tv.live.bx.ui.popwindow;

import android.app.Activity;
import android.widget.Button;

import com.efeizao.bx.R;

/**
 * @author Live
 * @version 2.2 2016.4.13
 * @title LiveMenuPopWindow.java Description:Live 底部 弹出菜单(主播侧)
 */
public class LiveMenuPopWindow extends MorePopWindow {
	private Button mBtnBeauty, mBtnManage;

	public LiveMenuPopWindow(Activity context) {
		this(context, R.layout.pop_live_bottom_list_layou);
	}

	public LiveMenuPopWindow(Activity context, int layoutId) {
		super(context, layoutId);
		mBtnBeauty = (Button) convertView.findViewById(R.id.live_bottom_item_beauty);
		mBtnManage = (Button) convertView.findViewById(R.id.live_bottom_item_share);
	}

	/**
	 * 横竖屏切换(Live)
	 */
//	public void initOrientationUI(int orientation) {
//		line1 = convertView.findViewById(R.id.menu_live_line_1);
//		line2 = convertView.findViewById(R.id.menu_live_line_2);
//		line3 = convertView.findViewById(R.id.menu_live_line_3);
//		line4 = convertView.findViewById(R.id.menu_live_line_4);
//		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//			((LinearLayout) convertView).setOrientation(LinearLayout.VERTICAL);
//			line1.setVisibility(View.GONE);
//			line2.setVisibility(View.GONE);
//			line3.setVisibility(View.GONE);
//			line4.setVisibility(View.GONE);
//		} else {
//			((LinearLayout) convertView).setOrientation(LinearLayout.HORIZONTAL);
//			line1.setVisibility(View.VISIBLE);
//			line2.setVisibility(View.VISIBLE);
//			line3.setVisibility(View.VISIBLE);
//			line4.setVisibility(View.VISIBLE);
//		}
//	}

	/**
	 * 设置美颜是否开启
	 */
	public void setBeauty(boolean openFlag) {
		if (openFlag)
			mBtnBeauty.setBackgroundResource(R.drawable.btn_live_beauty_selector);
		else
			mBtnBeauty.setBackgroundResource(R.drawable.btn_live_beauty_dis_selector);
	}
}
