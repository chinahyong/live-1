package com.dialog;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import tv.live.bx.R;


/**
 * title:LiveStatusCustomDialogBuilder.java Description:我要直播消息提示
 *
 * @author Live
 * @version 2.4.0 2016.4.26
 */
public class LiveStatusCustomDialogBuilder extends CustomDialogBuilder implements OnClickListener {

	private TextView mTvError;
	private ImageView mBtnClose;
	private String mError;

	public LiveStatusCustomDialogBuilder(Context context, String error) {
		super(context, R.layout.dialog_live_begin_error);
		mError = error;
		init();
	}

	public LiveStatusCustomDialogBuilder(Context context, int layoutId, String error) {
		super(context, layoutId);
		mError = error;
		init();
	}

	private void init() {
		initWidget();
		setEventListener();
		setError();
	}

	private void initWidget() {
		mTvError = (TextView) mDialogView.findViewById(R.id.dialog_tv_msg);
		mBtnClose = (ImageView) mDialogView.findViewById(R.id.dialog_btn_close);
	}

	private void setEventListener() {
		mBtnClose.setOnClickListener(this);
	}

	public void setError() {
		mTvError.setText(mError);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.dialog_btn_close:
				mDialog.dismiss();
				break;
		}
	}
}
