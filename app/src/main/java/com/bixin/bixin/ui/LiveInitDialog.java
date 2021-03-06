package com.bixin.bixin.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import tv.live.bx.R;


@SuppressLint("ResourceAsColor")
public class LiveInitDialog {
	private Context context;
	private Dialog dialog;
	private Button btnCancel;
	private Display display;
	private OnItemClickListener itemClickListener;
	public static final int WEIXIN = 0;
	public static final int PENGYOUQUAN = 1;
	public static final int WEIBO = 2;
	public static final int QQZONE = 3;
	public static final int QQ = 4;
	public static final int RENREN = 5;
	public static final int CANCEL = 6;
	private OnDismissListener onDismissListener;

	public LiveInitDialog(Context context, OnItemClickListener itemClickListener, OnDismissListener onDismissListener) {
		this.context = context;
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		display = windowManager.getDefaultDisplay();
		this.itemClickListener = itemClickListener;
		this.onDismissListener = onDismissListener;
	}

	public LiveInitDialog builder() {
		// 获取Dialog布局
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_live_share_layout, null);

		// 设置Dialog最小宽度为屏幕宽度
		// view.setMinimumWidth(display.getWidth());

		// 获取自定义Dialog布局中的控件
		view.findViewById(R.id.fragment_share_weixin).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				itemClickListener.onClick(WEIXIN);
			}
		});
		view.findViewById(R.id.fragment_share_pengyouyuan).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				itemClickListener.onClick(PENGYOUQUAN);
			}
		});
		view.findViewById(R.id.fragment_share_weibo).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				itemClickListener.onClick(WEIBO);
			}
		});
		view.findViewById(R.id.fragment_share_qqzone).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				itemClickListener.onClick(QQZONE);
			}
		});
		view.findViewById(R.id.fragment_share_qq).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				itemClickListener.onClick(QQ);
			}
		});
		// view.findViewById(R.id.fragment_share_renren).setOnClickListener(new
		// OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		//
		// itemClickListener.onClick(RENREN);
		//
		// }
		// });
		btnCancel = (Button) view.findViewById(R.id.btn_live);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				itemClickListener.onClick(CANCEL);
			}
		});

		// 定义Dialog布局和参数
		dialog = new Dialog(context, R.style.ActionSheetDialogStyle);
		dialog.setContentView(view);
		dialog.setOnDismissListener(onDismissListener);

		Window dialogWindow = dialog.getWindow();
		dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.x = 0;
		lp.y = 0;
		dialogWindow.setAttributes(lp);

		return this;
	}

	public LiveInitDialog setTitle(String title) {
		return this;
	}

	public LiveInitDialog setCancelable(boolean cancel) {
		dialog.setCancelable(cancel);
		return this;
	}

	public LiveInitDialog setCanceledOnTouchOutside(boolean cancel) {
		dialog.setCanceledOnTouchOutside(cancel);
		return this;
	}

	public void show() {
		dialog.show();
	}

	public interface OnItemClickListener {
		void onClick(int which);
	}
}
