package com.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import tv.live.bx.R;


/**
 * Title: QustomDialogBuilder.java</br> Description: 自定义对话框</br> Copyright:
 * Copyright (c) 2008</br>
 * @CreateDate 2014-7-8
 * @version 1.0
 */
public class ConfirmDialgBuilder {

	private Context mContext;

	/** The custom_body layout */
	private View mDialogView;

	// private TextView mTitle;

	/** optional message displayed below title if title exists */
	private TextView mMessage;
	/** 肯定性操作按钮 */
	private Button mPositive;
	/** 否定性操作按钮 */
	private Button mNegative;

	private Dialog mDialog;

	public ConfirmDialgBuilder(Context context) {
		mContext = context;

		mDialogView = View.inflate(context, R.layout.a_common_confirm_dialog_layout_2, null);

		mMessage = (TextView) mDialogView.findViewById(R.id.message);
		mPositive = (Button) mDialogView.findViewById(R.id.positive);
		mNegative = (Button) mDialogView.findViewById(R.id.negative);

		mDialog = new Dialog(context, R.style.base_dialog);
	}

	public ConfirmDialgBuilder setMessage(int textResId) {
		mMessage.setText(textResId);
		return this;
	}

	public ConfirmDialgBuilder setMessage(CharSequence text) {
		mMessage.setText(text);
		return this;
	}

	// public ConfirmDialgBuilder setTitle(int textResId) {
	// mTitle.setText(textResId);
	// return this;
	// }
	//
	// public ConfirmDialgBuilder setTitle(CharSequence text) {
	// mTitle.setText(text);
	// return this;
	// }

	public ConfirmDialgBuilder setPositiveButton(int textId, OnClickListener listener) {
		mPositive.setText(textId);
		mPositive.setOnClickListener(new PositiveListener(listener));
		return this;
	}

	public ConfirmDialgBuilder setPositiveButton(CharSequence text, OnClickListener listener) {
		mPositive.setText(text);
		mPositive.setOnClickListener(new PositiveListener(listener));
		return this;
	}

	public ConfirmDialgBuilder setNegativeButton(int textId, OnClickListener listener) {
		mNegative.setText(textId);
		mNegative.setOnClickListener(new NegativeListener(listener));
		return this;
	}

	public ConfirmDialgBuilder setNegativeButton(CharSequence text, OnClickListener listener) {
		mNegative.setText(text);
		mNegative.setOnClickListener(new NegativeListener(listener));
		return this;
	}

	/**
	 * This allows you to specify a custom layout for the area below the title
	 * divider bar in the dialog. As an example you can look at
	 * example_ip_address_layout.xml and how I added it in
	 * TestDialogActivity.java
	 * 
	 * @param resId of the layout you would like to add
	 * @param context
	 */
	public ConfirmDialgBuilder setCustomView(int resId, Context context) {
		View customView = View.inflate(context, resId, null);
		((FrameLayout) mDialogView.findViewById(R.id.customPanel)).addView(customView);
		return this;
	}

	/**
	 * @param content
	 * @return
	 */
	public ConfirmDialgBuilder setCustomView(View content) {
		((FrameLayout) mDialogView.findViewById(R.id.customPanel)).addView(content);
		return this;
	}

	public Dialog showDialog() {
		if (!TextUtils.isEmpty(mPositive.getText())) {
			mPositive.setVisibility(View.VISIBLE);
		}
		if (!TextUtils.isEmpty(mNegative.getText())) {
			mNegative.setVisibility(View.VISIBLE);
		}
		mDialog.setContentView(mDialogView);
		mDialog.show();
		return mDialog;
	}

	private class PositiveListener implements android.view.View.OnClickListener {

		private DialogInterface.OnClickListener dialogListener;

		public PositiveListener(DialogInterface.OnClickListener l) {
			dialogListener = l;
		}

		@Override
		public void onClick(View v) {
			if (mDialog != null)
				mDialog.dismiss();
			if (dialogListener != null) {
				dialogListener.onClick(mDialog, DialogInterface.BUTTON_POSITIVE);
			}
		}

	}

	private class NegativeListener implements android.view.View.OnClickListener {

		private DialogInterface.OnClickListener dialogListener;

		public NegativeListener(DialogInterface.OnClickListener l) {
			dialogListener = l;
		}

		@Override
		public void onClick(View v) {
			if (mDialog != null)
				mDialog.dismiss();
			if (dialogListener != null) {
				dialogListener.onClick(mDialog, DialogInterface.BUTTON_NEGATIVE);
			}
		}

	}

}
