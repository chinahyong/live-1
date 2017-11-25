package tv.live.bx.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import tv.live.bx.R;


/**
 * 更多popupWindow
 */
public class PopFanDetailMoreWindow extends PopupWindow {
	private View conentView;
	private IMoreItemListener mItemListener;
	public static final int ITEM_ONE = 1;
	public static final int ITEM_TWO = 2;
	public static final int ITEM_THREE = 3;
	public static final int ITEM_FOUR = 4;

	/**
	 * Creates a new instance of MorePopWindow.
	 * @param context
	 * @param isDeleteable 是否能够删除，不能就是举报
	 * @param itemListener
	 */
	public PopFanDetailMoreWindow(Activity context, boolean isAdd, IMoreItemListener itemListener) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mItemListener = itemListener;
		conentView = inflater.inflate(R.layout.pop_fan_detail_layout, null);
		// int h = context.getWindowManager().getDefaultDisplay().getHeight();
		// int w = context.getWindowManager().getDefaultDisplay().getWidth();
		// float denity = TelephoneUtil.getDisplayMetrics().density;
		// // 设置SelectPicPopupWindow的View
		this.setContentView(conentView);
		// // 设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(LayoutParams.WRAP_CONTENT);

		// 设置SelectPicPopupWindow弹出窗体可点击
		// popHeight = (int) ((21 + 35 * mList.size()) * denity + mList.size());
		// 设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setBackgroundDrawable(new ColorDrawable(0x00000000));

		conentView.findViewById(R.id.item1_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItemListener != null)
					mItemListener.onItemOnClick(ITEM_ONE);
				PopFanDetailMoreWindow.this.dismiss();
			}
		});
		conentView.findViewById(R.id.item2_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItemListener != null)
					mItemListener.onItemOnClick(ITEM_TWO);
				PopFanDetailMoreWindow.this.dismiss();
			}
		});
		conentView.findViewById(R.id.item3_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItemListener != null)
					mItemListener.onItemOnClick(ITEM_THREE);
				PopFanDetailMoreWindow.this.dismiss();
			}
		});
		conentView.findViewById(R.id.item4_layout).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItemListener != null)
					mItemListener.onItemOnClick(ITEM_FOUR);
				PopFanDetailMoreWindow.this.dismiss();
			}
		});

		if (!isAdd) {
			((TextView) conentView.findViewById(R.id.item_text4)).setText(R.string.commutity_add_fan);
			((ImageView) conentView.findViewById(R.id.icon4)).setImageResource(R.drawable.icon_add);
		}

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
		// conentView.getViewTreeObserver().addOnGlobalLayoutListener(
		// new OnGlobalLayoutListener() {
		//
		// @Override
		// public void onGlobalLayout() {
		// conentView.getViewTreeObserver()
		// .removeGlobalOnLayoutListener(this);
		// popHeight = conentView.getHeight();
		// }
		// });
		// LinearLayout addTaskLayout = (LinearLayout) conentView
		// .findViewById(R.id.add_task_layout);
		// LinearLayout teamMemberLayout = (LinearLayout) conentView
		// .findViewById(R.id.team_member_layout);
		// addTaskLayout.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// AddPopWindow.this.dismiss();
		// }
		// });
		//
		// teamMemberLayout.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// AddPopWindow.this.dismiss();
		// }
		// });
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

	/**
	 * 更多接口返回接口
	 * @version AddPopWindow
	 * @since JDK 1.6
	 */
	public interface IMoreItemListener {

		void onItemOnClick(int itemId);
	}
}
