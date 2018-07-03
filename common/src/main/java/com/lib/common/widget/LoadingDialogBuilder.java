package com.lib.common.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.common.R;

/**
 * Created by Admin
 */

public class LoadingDialogBuilder {
	private static AlertDialog mDialog;
	private static Context mContext;

	public static AlertDialog showDialog(Context context) {
		mContext = context;
		if (mDialog == null) {
			init();
		} else {
			mDialog.dismiss();
			mDialog.show();
		}
		return mDialog;
	}

	public static AlertDialog dismissDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
		return mDialog;
	}

	private static void init() {
		mDialog = new AlertDialog.Builder(mContext).create();
		LayoutInflater loInflater = LayoutInflater.from(mContext);
		View rootView = loInflater.inflate(R.layout.dialog_loading_layout, null);
		mDialog.setView(rootView);
		mDialog.getWindow().setContentView(R.layout.dialog_loading_layout);
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.setCancelable(false);
		final ImageView ivLoading = rootView.findViewById(R.id.dialog_progress_iv);
		Animation loAnimRotate = AnimationUtils.loadAnimation(mContext, R.anim.rotate_loading);
		LinearInterpolator loLin = new LinearInterpolator();
		loAnimRotate.setInterpolator(loLin);
		ivLoading.startAnimation(loAnimRotate);
		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				ivLoading.clearAnimation();
			}
		});
		mDialog.show();
	}
}
