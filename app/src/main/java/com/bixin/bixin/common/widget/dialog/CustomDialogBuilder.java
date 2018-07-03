package com.bixin.bixin.common.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import tv.live.bx.R;


/**
 * Title: CustomDialogBuilder.java</br> Description: 自定义对话框基类</br> Copyright:
 * Copyright (c) 2008</br>
 *
 * @version 1.0
 * @CreateDate 2014-7-8
 */
public class CustomDialogBuilder {

	protected Context mContext;

	/**
	 * The custom_body layout
	 */
	protected View mDialogView;

	protected Dialog mDialog;

	protected PositiveListener mPositiveListener;

	public CustomDialogBuilder(Context context, int layoutId) {
		mContext = context.getApplicationContext();
		mDialogView = View.inflate(context, layoutId, null);
		mDialog = new Dialog(context, R.style.base_dialog);
	}

	public void setOnClickListener(int viewId, OnClickListener listener) {
		mDialogView.findViewById(viewId).setOnClickListener(new PositiveListener(listener));
	}

	public View findViewById(int viewId) {
		return mDialogView.findViewById(viewId);
	}

	public Dialog showDialog() {
		mDialog.setContentView(mDialogView);
		mDialog.show();
		return mDialog;
	}

	public boolean isShowing() {
		return mDialog.isShowing();
	}

	public void dismiss() {
		mDialog.dismiss();
	}

	public class PositiveListener implements android.view.View.OnClickListener {

		private OnClickListener dialogListener;

		public PositiveListener(OnClickListener l) {
			dialogListener = l;
		}

		@Override
		public void onClick(View v) {
			if (mDialog != null)
				mDialog.dismiss();
			if (dialogListener != null) {
				dialogListener.onClick(v);
			}
		}
	}
}
