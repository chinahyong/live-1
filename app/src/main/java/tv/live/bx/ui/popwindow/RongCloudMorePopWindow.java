package tv.live.bx.ui.popwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import tv.live.bx.R;


/**
 * 更多popupWindow
 */
public class RongCloudMorePopWindow extends PopupWindow {
	protected View convertView, mLine;
	private TextView mCopy, mDelete;


	/**
	 * Creates a new instance of MorePopWindow.
	 *
	 * @param context
	 */
	public RongCloudMorePopWindow(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = inflater.inflate(R.layout.pop_conversation_more_layout, null);
		// int h = context.getWindowManager().getDefaultDisplay().getHeight();
		// int w = context.getWindowManager().getDefaultDisplay().getWidth();
		// float denity = TelephoneUtil.getDisplayMetrics().density;
		// // 设置SelectPicPopupWindow的View
		this.setContentView(convertView);
		// // 设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(LayoutParams.WRAP_CONTENT);

		// 设置SelectPicPopupWindow弹出窗体可点击
		// popHeight = (int) ((21 + 35 * mList.size()) * denity + mList.size());
		// 设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setBackgroundDrawable(new ColorDrawable(0x00000000));

		// 实例化一个ColorDrawable颜色为半透明
		// ColorDrawable dw = new ColorDrawable(0000000000);
		// 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
		// this.setBackgroundDrawable(dw);
		// mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
		// 设置SelectPicPopupWindow弹出窗体动画效果
		// this.setAnimationStyle(R.style.Popup_Animation_ShowHidden);
		this.setFocusable(true);
		this.setTouchable(true);
		this.setOutsideTouchable(true);
		mCopy = (TextView) convertView.findViewById(R.id.copy);
		mDelete = (TextView) convertView.findViewById(R.id.delete);
		mLine = convertView.findViewById(R.id.line);
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

	public void setOnClickListener(int ViewId, final OnClickListener itemListener) {
		convertView.findViewById(ViewId).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				itemListener.onClick(v);
				RongCloudMorePopWindow.this.dismiss();
			}
		});
	}

	public void setCopyViewVersible(int visible) {
		mCopy.setVisibility(visible);
		mLine.setVisibility(visible);
	}

	public View getContentView() {
		return convertView;
	}

}
