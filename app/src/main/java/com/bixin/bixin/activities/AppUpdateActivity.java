package com.bixin.bixin.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.efeizao.feizao.fmk.appupdate.ActivityCallBack;
import cn.efeizao.feizao.fmk.appupdate.DefaultUpdateObserver;
import cn.efeizao.feizao.ui.dialog.QustomDialogBuilder;
import cn.efeizao.feizao.update.AppUpdateManager;
import tv.live.bx.R;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.FileUtil;
import com.bixin.bixin.library.util.StringUtil;
import com.bixin.bixin.library.util.UserPreference;
import com.bixin.bixin.util.UiHelper;

/**
 * Title: AppUpdateActivity.java Description: 应用升级对话框界面 Copyright: Copyright (c)
 * 2008
 * @version 1.0
 * @CreateDate 2013-12-17 下午2:37:44
 */
@SuppressLint("NewApi")
public class AppUpdateActivity extends FragmentActivity {

	static final String TAG = "AppUpdateActivity";

	public static final String EXA_RESPONSE_FP = "responseFP";
	public static final String EXA_FIXLOG = "fixLog";
	public static final String EXA_FILESIZE = "fileSize";
	public static final String EXA_RESPONSE_URL = "responseUrl";
	public static final String EXA_RESPONSE_LASTVERSION = "responseLastVersion";
	public static final String EXA_RESULT_IS_FINISH = "isFinish";
	public static final String EXA_IS_UPDTAE_TOAST = "isUpdateToast";

	private Dialog mCheckUpdateDialog = null;
	private Dialog update_dialog;
	private ProgressBar progress_bar;
	private TextView textversion;
	private Button dialog_ok;
	private Button dialog_cancel;
	private String update_url;
	private String last_version;
	private boolean mIsAppDownloading;

	private long mExitTimeMillis;

	private boolean mIsManualUpdate;

	/**
	 * 标识此Activity是否可用关闭
	 */
	private boolean mShouldFinish = true;

	private Context mContext;

	/**
	 * @param context 跳转App更新界面
	 */
	public static void redirect2me(Context context, Bundle bundle) {
		Intent intent = new Intent(context, AppUpdateActivity.class);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mContext = this;
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			finish();
			return;
		}

		init(bundle);
	}

	private void init(Bundle bundle) {
		int responseFp = bundle.getInt(EXA_RESPONSE_FP, -1);// 是否强制升级
		String fixLog = bundle.getString(EXA_FIXLOG);// 升级日志
		// String fileSize = bundle.getString(EXA_FILESIZE);// 升级包大小
		update_url = bundle.getString(EXA_RESPONSE_URL);// 升级包下载地址
		last_version = bundle.getString(EXA_RESPONSE_LASTVERSION);// 升级包版本号
		boolean isManualUpdate = responseFp != 1;
		boolean isUpdateToast = bundle.getBoolean(EXA_IS_UPDTAE_TOAST, false);

		// URL为空的话表明无需更新
		if (StringUtil.isNullOrEmpty(update_url)) {
			EvtLog.d(TAG, "init --> " + isUpdateToast);
			UserPreference.delUpdateApk(AppUpdateActivity.this);
			if (isUpdateToast) {
				UiHelper.showToast(this, R.string.a_update_no_update);
			}
			finish();
			return;
		} else {
			UserPreference.setUpdateApkPath(FileUtil.getDiskCachePath(AppUpdateActivity.this, "update_apkfile"), this);
		}

		if (!StringUtil.isNullOrEmpty(fixLog)) {
			fixLog = "\n" + fixLog;
		}

		// if (!StringUtil.isNullOrEmpty(fileSize)) {
		// fileSize = "\n" + getString(R.string.a_update_file_size, fileSize);
		// }

		String sumaryText = "";
		String titleText = "";
		if (isManualUpdate) {
			// 用户手动更新
			titleText = getString(R.string.a_setting_check_update);
			sumaryText = getString(R.string.a_update_new_version) + last_version + fixLog;// +
																						  // fileSize;
		} else {
			// 强制更新
			titleText = getString(R.string.a_update_auto_update);
			sumaryText = getString(R.string.a_update_new_version) + last_version + fixLog + "\n" // +
																								 // fileSize
																								 // +
																								 // "\n"
					+ getString(R.string.a_update_auto_update_suffix);
		}

		mCheckUpdateDialog = createCheckUpdateDialog(titleText, sumaryText, isManualUpdate);
		mCheckUpdateDialog.show();

		mIsManualUpdate = isManualUpdate;
	}

	/**
	 * 关闭当前Activity，并退出App
	 */
	private void closeActivityAndExit() {
		Intent intent = new Intent(this, CalMainActivity.class);
		intent.putExtra(EXA_RESULT_IS_FINISH, true);
		startActivity(intent);
	}

	private Dialog createCheckUpdateDialog(String title, String sumary, boolean isManualUpdate) {
		LayoutInflater inflater = LayoutInflater.from(this);
		View dialogView = inflater.inflate(R.layout.a_update_dialog_layout, null);

		TextView dialog_title = (TextView) dialogView.findViewById(R.id.title);
		dialog_title.setText(title);
		TextView dialog_sumary = (TextView) dialogView.findViewById(R.id.summary);
		dialog_sumary.setText(sumary);

		/* Button btn_ok = (Button) dialogView.findViewById(R.id.btn_ok);
		 * btn_ok.setText(R.string.update_now); btn_ok.setOnClickListener(new
		 * OnOKBtnClickListener(isManualUpdate)); Button btn_cancle = (Button)
		 * dialogView.findViewById(R.id.btn_cancle);
		 * btn_cancle.setText(R.string.later); btn_cancle.setOnClickListener(new
		 * OnCancleBtnClickListener(isManualUpdate)); */

		int positiveRes = R.string.a_update_now;
		int negativeRes = isManualUpdate ? R.string.a_update_later : R.string.exit_app;
		// Dialog dialog = UiHelper.showCustomDialog(this, 0, dialogView,
		// positiveRes, negativeRes, new OnOKBtnClickListener(isManualUpdate),
		// new OnCancleBtnClickListener(isManualUpdate));
		Dialog dialog = new QustomDialogBuilder(this)
				.setPositiveButton(positiveRes, new OnOKBtnClickListener(isManualUpdate))
				.setNegativeButton(negativeRes, new OnCancleBtnClickListener(isManualUpdate)).setCustomView(dialogView)
				.setIcon(R.drawable.a_update_ic_checkupdate_dialog).showDialog();

		if (!isManualUpdate) { // 强制升级
			dialog.setCancelable(false);
		}
		dialog.setOnDismissListener(new OnDialogDismissListener());
		dialog.setOnKeyListener(new DialogKeyListener());
		return dialog;
	}

	private void closeCheckUpdateDialog() {
		if (mCheckUpdateDialog != null && mCheckUpdateDialog.isShowing()) {
			mCheckUpdateDialog.dismiss();
		}
	}

	private Dialog createDownloadProgressDialog() {
		Dialog update_dialog = new Dialog(this, R.style.base_dialog);
		LayoutInflater inflater = LayoutInflater.from(this);
		View dialogView = inflater.inflate(R.layout.a_update_appupdate_dialog, null);
		update_dialog.setContentView(dialogView);
		progress_bar = (ProgressBar) update_dialog.findViewById(R.id.progress_bar);
		textversion = (TextView) update_dialog.findViewById(R.id.textversion);
		dialog_ok = (Button) update_dialog.findViewById(R.id.dialog_button_ok);

		dialog_cancel = (Button) update_dialog.findViewById(R.id.dialog_button_cancel);

		dialog_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AppUpdateManager adapter = new AppUpdateManager();
				adapter.beginToDownload(mContext, mContext.getPackageName(),
						mContext.getString(R.string.a_update_app_name, last_version),
						BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_logo),
						mContext.getString(R.string.a_update_tick_text, last_version), update_url,
						new AppUpdateDownloadCallBack());
			}
		});

		dialog_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 用户取消强制更新，结束应用
				closeActivityAndExit();
			}
		});
		update_dialog.setCancelable(false);
		update_dialog.setOnDismissListener(new OnDialogDismissListener());
		update_dialog.setOnKeyListener(new DialogKeyListener());
		return update_dialog;
	}

	public void updateProgress(int state, int progress) {
		switch (state) {
		case DefaultUpdateObserver.STATE_DOWNING:
			progress_bar.setProgress(progress);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (!mIsAppDownloading) {
						dialog_ok.setEnabled(false);
						dialog_cancel.setEnabled(true);
						textversion.setText(R.string.a_update_downloading);
						mIsAppDownloading = true;
					}
				}
			});
			break;
		case DefaultUpdateObserver.STATE_FINISH:
			progress_bar.setProgress(progress);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					dialog_ok.setEnabled(true);
					dialog_ok.setText(R.string.a_update_install_now);
					dialog_cancel.setEnabled(true);
					dialog_cancel.setText(R.string.a_update_later);
					textversion.setText(R.string.a_update_download_complete);
					mIsAppDownloading = false;
				}
			});

			break;

		case DefaultUpdateObserver.STATE_ERROR:

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					dialog_ok.setEnabled(true);
					dialog_ok.setText(R.string.a_update_retry);
					dialog_cancel.setEnabled(true);
					dialog_cancel.setText(R.string.a_update_later);
					textversion.setText(R.string.a_update_download_failed);
					mIsAppDownloading = false;
				}
			});

			break;

		default:
			break;
		}
	}

	private class AppUpdateDownloadCallBack extends ActivityCallBack {
		@Override
		public void update(int state, int progress) {
			EvtLog.i("down1", "progress====" + progress);
			if (update_dialog != null && update_dialog.isShowing()) {
				updateProgress(state, progress);
			}
		}
	}

	private class OnOKBtnClickListener implements DialogInterface.OnClickListener {
		private boolean isManualUpdate = true;

		private OnOKBtnClickListener(boolean isManualUpdate) {
			this.isManualUpdate = isManualUpdate;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {

			if (!isManualUpdate) {
				// 强制更新
				mShouldFinish = false; // 此时不能关闭Activity
				closeCheckUpdateDialog();
				update_dialog = createDownloadProgressDialog();
				update_dialog.show();
			} else {
				mShouldFinish = true; // 此时关闭Activity
				closeCheckUpdateDialog();
			}
			AppUpdateManager adapter = new AppUpdateManager();
			adapter.beginToDownload(mContext, mContext.getPackageName(),
					mContext.getString(R.string.a_update_app_name, last_version),
					BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_logo),
					mContext.getString(R.string.a_update_tick_text, last_version), update_url,
					new AppUpdateDownloadCallBack());
		}

	}

	private class OnCancleBtnClickListener implements DialogInterface.OnClickListener {
		private boolean isManualUpdate = true;

		private OnCancleBtnClickListener(boolean isManualUpdate) {
			this.isManualUpdate = isManualUpdate;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			closeCheckUpdateDialog();
			if (!isManualUpdate) {
				// 用户取消强制更新，结束应用
				closeActivityAndExit();
			} else {
				finish();
			}
		}
	}

	private class OnDialogDismissListener implements OnDismissListener {

		@Override
		public void onDismiss(DialogInterface dialog) {
			if (mShouldFinish)
				finish();
		}
	}

	private class DialogKeyListener implements OnKeyListener {

		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if (mIsManualUpdate) {
				return false;
			}

			if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
				// 按两次返回键退出程序
				if ((System.currentTimeMillis() - mExitTimeMillis) > Constants.EXIT_INTERVAL) {
					UiHelper.showToast(AppUpdateActivity.this, R.string.a_main_exit_confirm);
					mExitTimeMillis = System.currentTimeMillis();
				} else {
					closeActivityAndExit();
				}
				return true;
			}
			return false;
		}

	}

}
