package com.dialog;


import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import tv.live.bx.R;

/**
 * Title: PermissionDialogBuilder.java Description: 自定义对话框(权限)
 * @author Live
 * @version 2.2 2016.4.8
 */
public class PermissionDialogBuilder {
	private Context mContext;
	private Dialog mDialog;

	private View mRootView;
	private TextView mTvQuestionTitle, mTvExceptionTitle, mTvMethod1, mTvMethod2;
	public PermissionDialogBuilder(Context context) {
		mContext = context;

		mRootView = View.inflate(context, R.layout.a_common_permission_dialog_layout, null);

		mTvQuestionTitle = (TextView) mRootView.findViewById(R.id.dialog_permission_question);
		mTvExceptionTitle = (TextView) mRootView.findViewById(R.id.dialog_permission_exception);
		mTvMethod1 = (TextView) mRootView.findViewById(R.id.dialog_permission_method_1);
		mTvMethod2 = (TextView) mRootView.findViewById(R.id.dialog_permission_method_2);

		mDialog = new Dialog(context, R.style.base_dialog);
		mDialog.setCanceledOnTouchOutside(true);
	}

	/** 问题标题 */
	public PermissionDialogBuilder setQuestionTitle(CharSequence questionTitle) {
		mTvQuestionTitle.setText(questionTitle);
		return this;
	}

	public PermissionDialogBuilder setQuestionTitle(int questionTitle) {
		mTvQuestionTitle.setText(mContext.getString(questionTitle));
		return this;
	}

	/** 异常标题 */
	public PermissionDialogBuilder setExceptionTitle(CharSequence deviceTitle) {
		String title = mContext.getString(R.string.common_dialog_permission_exception);
		title = String.format(title, deviceTitle);
		mTvExceptionTitle.setText(title);
		setMethod2(title);
		return this;
	}

	public PermissionDialogBuilder setExceptionTitle(int deviceTitle) {
		String title = mContext.getString(R.string.common_dialog_permission_exception);
		title = String.format(title, mContext.getString(deviceTitle));
		mTvExceptionTitle.setText(title);
		setMethod2(title);
		return this;
	}

	/** 解决方式1 */
	public PermissionDialogBuilder setMethod1(CharSequence method) {
		String msg = mContext.getString(R.string.common_dialog_permission_method_1);
		msg = String.format(msg, msg);
		mTvMethod1.setText(msg);
		return this;
	}
	public PermissionDialogBuilder setMethod1(int method) {
		String msg = mContext.getString(R.string.common_dialog_permission_method_1);
		msg = String.format(msg, mContext.getString(method));
		mTvMethod1.setText(msg);
		return this;
	}

	/** 解决方式2 */
	private void setMethod2(CharSequence method) {
		String msg = mContext.getString(R.string.common_dialog_permission_method_2);
		method = String.format(msg, method);
		mTvMethod2.setText(method);
	}

	private void setMethod2(int method) {
		String msg = mContext.getString(R.string.common_dialog_permission_method_2);
		msg = String.format(msg, mContext.getString(method));
		mTvMethod2.setText(msg);
	}

	/** 显示Dialog */
	public Dialog showDialog() {
		mDialog.setContentView(mRootView);
		mDialog.show();
		return mDialog;
	}
}
