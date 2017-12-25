package com.bixin.bixin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import tv.live.bx.R;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.fragments.AnchorVideoFragment;
import com.bixin.bixin.fragments.AnchorVideoFragment.OnRecordFinishListener;
import com.bixin.bixin.library.util.FileUtil;

/**
 * Title: AnchorCameraActivity.java Description:主播报名视频录制
 * @author Live
 * @version 2.1.2, 2016.3.24
 */
public class AnchorCameraActivity extends BaseFragmentActivity implements OnClickListener {
	private static final int RECORD_FINISH = 0x001;
	/** 录制视频文件路径名称 */
	public static final String RECORD_VIDEO_FILE_PATH = "video";
	public static final String RECORD_VIDEO_FILE_NAME = "anchor_video.mp4";
	private TextView mTvLeft, mTvRight, mTvCordingMsg, mTvCompleteMsg;
	private AnchorVideoFragment mAvFragment;

	private boolean isCompleteFlag = false; // 是否点击完成正常退出
	private boolean isRecording = false; // 是否正在录制
	private int mStatus = -1;
	private String mVideoUrl = null;
	private String mFilePath;
	private OnVideoStatusCallback mVideoCallback;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_camera_anchor;
	}

	@Override
	protected void initMembers() {
		mTvLeft = (TextView) findViewById(R.id.anchor_camera_bottom_left);
		mTvRight = (TextView) findViewById(R.id.anchor_camera_bottom_right);
		mTvCompleteMsg = (TextView) findViewById(R.id.anchor_camera_coplete_msg);
		mTvCordingMsg = (TextView) findViewById(R.id.anchor_camera_begin_msg);
		mAvFragment = new AnchorVideoFragment();
	}

	@Override
	public void initWidgets() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		mAvFragment.setmOnRecordFinishListener(new OnRecordFinishListener() {
			@Override
			public void onRecordFinish(String filePath) {
				Message msg = new Message();
				msg.what = RECORD_FINISH;
				Map<String, String> data = new HashMap<String, String>();
				data.put("filePath", filePath);
				msg.obj = data;
				mHandler.sendMessage(msg);
			}
		});
		this.setVideoCallback(mAvFragment);
		ft.add(R.id.anchor_camera_content, mAvFragment);
		ft.commit();
	}

	@Override
	protected void setEventsListeners() {
		mTvLeft.setOnClickListener(this);
		mTvRight.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Bundle bundle = getIntent().getExtras();
		Bundle bundle1 = new Bundle();
		File fileDir = new File(FileUtil.getDiskCachePath(this, RECORD_VIDEO_FILE_PATH));
		if (!fileDir.exists())
			fileDir = FileUtil.createSDDir(FileUtil.getDiskCachePath(this, RECORD_VIDEO_FILE_PATH));
		File file = new File(fileDir.getAbsolutePath() + File.separator + RECORD_VIDEO_FILE_NAME);
		if (bundle != null) {
			mStatus = bundle.getInt("status");
			mVideoUrl = bundle.getString("video");
			bundle1.putInt("status", mStatus);
			// 审核中
			if (mStatus == 0) {
				mTvCordingMsg.setVisibility(View.GONE);
				mTvRight.setVisibility(View.GONE);
				mTvLeft.setVisibility(View.VISIBLE);
				bundle1.putString("video", mVideoUrl);
			} else if (mStatus == -1 || mStatus == 1 && TextUtils.isEmpty(mVideoUrl)) {
				// 未提交 未通过且无网络视频文件(针对老主播)
				if (file.exists()) {
					bundle1.putString("filePath", file.getAbsolutePath());
					mFilePath = file.getAbsolutePath();
					mVideoUrl = "";
					recorded();
				}
			} else if (mStatus == 1 && !TextUtils.isEmpty(mVideoUrl)) {
				// 未通过 有网络视频
				if (file.exists()) {
					bundle1.putString("filePath", file.getAbsolutePath());
					mFilePath = file.getAbsolutePath();
				} else {
					bundle1.putString("video", mVideoUrl);
				}
				recorded();
			}
			mAvFragment.setArguments(bundle1);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!isCompleteFlag) {
			if (!TextUtils.isEmpty(mFilePath))
				if (new File(mFilePath).exists()) {
					new File(mFilePath).delete();
					mFilePath = null;
				}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/** 取消录制 */
	private void recordCancel(boolean isRecording) {
		if (!isRecording) {
			mFilePath = null;
			onBackPressed();
		} else {
			isRecording = false;
			mTvRight.setVisibility(View.VISIBLE);
			mTvLeft.setText(getString(R.string.cancel));
			mTvRight.setText(getString(R.string.anchor_camera_begin));
			mVideoCallback.onCancel(true);
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("filePath", mFilePath);
		this.setResult(RESULT_OK, intent);
		this.finish();
	}

	/** 重新录制 */
	private void reRecord() {
		if (!TextUtils.isEmpty(mFilePath)) {
			new File(mFilePath).delete();
			mFilePath = null;
		}
		mTvLeft.setText(getString(R.string.cancel));
		mTvRight.setText(getString(R.string.anchor_camera_begin));
	}

	/** 正在录制 */
	private void recording() {
		isRecording = true;
		mTvCompleteMsg.setVisibility(View.GONE);
		mTvCordingMsg.setVisibility(View.VISIBLE);
		mTvLeft.setVisibility(View.VISIBLE);
		mTvLeft.setText(getString(R.string.cancel));
		mTvRight.setVisibility(View.GONE);
	}

	/** 录制完成 */
	private void recorded() {
		mTvCompleteMsg.setVisibility(View.VISIBLE);
		mTvCordingMsg.setVisibility(View.GONE);
		mTvLeft.setVisibility(View.VISIBLE);
		mTvLeft.setText(getString(R.string.anchor_camera_restart));
		mTvRight.setVisibility(View.VISIBLE);
		mTvRight.setText(getString(R.string.anchor_camera_complete));
	}

	public void setVideoCallback(OnVideoStatusCallback videoCallback) {
		this.mVideoCallback = videoCallback;
	}
	/*********************************** 事件处理器 *************************************/
	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (msg.what == RECORD_FINISH) {
			Map<String, String> data = (Map<String, String>) msg.obj;
			mFilePath = data.get("filePath");
			recorded();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.anchor_camera_bottom_left:
			// 左侧取消 全部取消 左侧重新录制
			if (mStatus == 0)
				this.finish();
			else if (mTvLeft.getText().equals(getString(R.string.cancel)) && mTvRight.getVisibility() == View.GONE) {
				isRecording = true;
				recordCancel(isRecording);
			} else if (mTvLeft.getText().equals(getString(R.string.cancel)) && mTvRight.getVisibility() == View.VISIBLE) {
				isRecording = false;
				recordCancel(isRecording);
			} else if (mTvLeft.getText().equals(getString(R.string.anchor_camera_restart))) {
				isRecording = false;
				reRecord();
				if (mVideoCallback != null)
					mVideoCallback.onRecordeAgain();
			}
			break;
		case R.id.anchor_camera_bottom_right:
			// 右侧开始录制 右侧完成
			if (mTvRight.getText().equals(getString(R.string.anchor_camera_begin))) {
				recording();
				if (mVideoCallback != null)
					mVideoCallback.onRecord();
			} else if (mTvRight.getText().equals(getString(R.string.anchor_camera_complete))) {
				isCompleteFlag = true;
				onBackPressed();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 视频录制操作回调 : OnVideoStatusCallback <br/>
	 * @author Administrator
	 * @version AnchorCameraActivity
	 * @since JDK 1.6
	 */
	public interface OnVideoStatusCallback {
		/* 取消录制 */
		void onCancel(boolean isRecording);
		/* 重新录制 */
		void onRecordeAgain();
		/* 开始录制 */
		void onRecord();
	}
}
