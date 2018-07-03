package com.bixin.bixin.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.bixin.bixin.App;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tv.live.bx.R;
import com.bixin.bixin.activities.AnchorCameraActivity;
import com.bixin.bixin.activities.AnchorCameraActivity.OnVideoStatusCallback;
import com.bixin.bixin.common.helper.camera.CameraHelper;
import com.bixin.bixin.common.helper.camera.RecorderHelper;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.library.util.BitmapUtils;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.FileUtil;
import com.bixin.bixin.util.UiHelper;

/**
 * Title: AnchorVideoFragment.java Description:主播报名视频预览页面
 * @author Live
 * @version 2.1.2, 2016.3.24
 */
public class AnchorVideoFragment extends BaseFragment
		implements
			OnErrorListener,
			OnClickListener,
			OnVideoStatusCallback,
			OnVideoSizeChangedListener,
			OnCompletionListener,
			OnPreparedListener {
	private static final int MSG_RECORD_COMPLETE = 0x001; // 录制完成
	private static final int MSG_PLAY_PAUSE_END = 0x002; // 播放完成、暂停
	private static final int MSG_UPDATE_PLAY_PROGRESS = 200; // 进度条更新
	private static final int RECORD_MAX_PROGRESS = 100; // 进度条默认Max
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private ProgressBar mProgressBar;
	private ImageView mIvDirChange, mIvPlay;
	private AlertDialog mProgress;

	private Camera mCamera; // 相机
	private MediaRecorder mMediaRecorder; // 录制
	private MediaPlayer mPlayer; // 播放
	private Camera.Parameters mCameraParams = null;

	private OnRecordFinishListener mOnRecordFinishListener;// 录制完成回调接口

	private boolean mProgressPauseFlag = false;
	private boolean mCancelFlag; // 是否取消录制
	private boolean mExceptionFlag = false;

	private Timer mTimer;// 计时器
	private int mTimeCount;// 时间计数

	private int mPausePos = 0; // 视频播放记录暂停位置
	private int mCameraPos = 1; // 默认前置摄像头(标记摄像头方向)0：后置 1：前置
	private String mFilePath = "";
	private String mUrl = "";
	private int mStatus = -1; // -1:未提交 0：审核中 1：未通过 2：已通过
	/** surfaceView 默认的宽高 */
	private int mWidth;// 相机分辨率宽度
	private int mHeight;// 相机分辨率高度
	private File mRecordFile = null; // 本地文件
	private Camera.Size mPreviewSize; // 预览尺寸

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_anchor_video_display;
	}

	@Override
	protected void initMembers() {
		mSurfaceView = (SurfaceView) mRootView.findViewById(R.id.video_recorder_surfaceview);
		mProgressBar = (ProgressBar) mRootView.findViewById(R.id.video_recorder_progressBar);
		mIvDirChange = (ImageView) mRootView.findViewById(R.id.video_recorder_camera_dir);
		mIvPlay = (ImageView) mRootView.findViewById(R.id.anchor_camera_play);
	}

	@Override
	protected void initWidgets() {
		mProgressBar.setMax(RECORD_MAX_PROGRESS);// 设置进度条最大量
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	protected void setEventsListeners() {
		mSurfaceHolder.addCallback(new SurfaceHolderCallBack());
		mIvDirChange.setOnClickListener(this);
		mIvPlay.setOnClickListener(this);
		mSurfaceView.setOnClickListener(this);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void initData(Bundle bundle) {
		int[] screen = Utils.getScreenWH(mActivity);
		mHeight = screen[1] - Utils.dip2px(mActivity, 50);
		mWidth = screen[0];
		mStatus = bundle.getInt("status");
		Bitmap bitmap = null;
		if (mStatus == -1 || mStatus == 1 && TextUtils.isEmpty(bundle.getString("video"))) {
			mFilePath = bundle.getString("filePath");
			EvtLog.e(TAG, mFilePath);
			if (checkLocal(mFilePath)) {
				bitmap = BitmapUtils.createVideoThumbnail(mFilePath);
			}

		} else {
			mUrl = bundle.getString("video");
			bitmap = BitmapUtils.createVideoThumbnail(mUrl);
		}
		if (bitmap != null) {
			Drawable drawable = new BitmapDrawable(bitmap);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				mSurfaceView.setBackground(drawable);
			} else {
				mSurfaceView.setBackgroundDrawable(drawable);
			}
		}
	}

	/**
	 * 初始化摄像头
	 * @throws IOException
	 */
	private void initCamera() throws IOException {
		int cameraId = CameraHelper.byDirectionGetCameraId(mCameraPos);
		try {
			mCamera = CameraHelper.openCamera(cameraId);
			setCamera();
			mExceptionFlag = false;
		} catch (Exception e) {
			e.printStackTrace();
			mExceptionFlag = true;
			mCamera = null;
			showCameraPermission();
			return;
		}
	}

	private void setCamera() throws IOException {
		if (mCamera == null)
			return;
		mCameraParams = CameraHelper.setCameraParams(mActivity, mWidth, mHeight);
		mPreviewSize = mCameraParams.getPreviewSize();
		resizeHolder(mPreviewSize.height, mPreviewSize.width);
		mCamera.setPreviewDisplay(mSurfaceHolder);
		mCamera.startPreview();
		mCamera.unlock();
	}

	/**
	 * 初始化 录制
	 * @throws IOException
	 */
	@SuppressLint("NewApi")
	private void initRecord() throws IOException {
		createRecordDir();
		try {
			mMediaRecorder = RecorderHelper.getMediaRecorder();
			mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
			RecorderHelper.initMediaRecorder(mCamera, mCameraPos, mRecordFile.getAbsolutePath());
			List<Camera.Size> supportedVideoSize = mCameraParams.getSupportedVideoSizes();
			Size size = CameraHelper.getOptimalPreviewSize(supportedVideoSize, mWidth, mHeight, 0);
			if (size != null) {
				mMediaRecorder.setVideoSize(size.width, size.height);// 设置分辨率：
			}
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
			mExceptionFlag = true;
			showAudioPermission();
			return;
		}
		mMediaRecorder.setOnErrorListener(this);
		mMediaRecorder.prepare();
		try {
			mMediaRecorder.start();
		} catch (Exception e) {
			e.printStackTrace();
			showAudioPermission();
			mExceptionFlag = true;
		}
	}

	/** 初始化播放器 */
	private void initPlayer() {
		releasePlay();
		mPlayer = new MediaPlayer();
		mPlayer.reset();
		// 设置需要播放的视频
		try {
			if (checkLocal(mFilePath)) {
				mPlayer.setDataSource(mFilePath);
			} else {
				if (!TextUtils.isEmpty(mUrl))
					mPlayer.setDataSource(mUrl);
				else {
					showTips(R.string.anchor_toast_player_exception);
					return;
				}
			}
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mPlayer.setDisplay(mSurfaceHolder);
			mPlayer.setVolume(0.5f, 0.5f);
			mPlayer.setOnPreparedListener(this);
			mPlayer.setOnVideoSizeChangedListener(this);
			mPlayer.setOnCompletionListener(this);
			mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					if (mProgress != null && mProgress.isShowing())
						mProgress.dismiss();
					showTips(R.string.anchor_toast_player_exception);
					return true;
				}
			});
		} catch (Exception e) {
			showTips(R.string.anchor_toast_player_exception);
			e.printStackTrace();
		}
	}

	/** 初始化播放进度条 */
	private void initPlayProgress(int beginPos) {
		mProgressBar.setProgress(beginPos);
		mProgressBar.setMax(mPlayer.getDuration());
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (mProgressPauseFlag == true) {
					return;
				}
				mHandler.sendEmptyMessage(MSG_UPDATE_PLAY_PROGRESS);
			}
		}, 0, 10);
	}

	/** 停止录制 */
	public void stopRecord() {
		mProgressBar.setProgress(0);
		try {
			RecorderHelper.stopMediaRecorder();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 释放播放器 */
	private void releasePlay() {
		if (mPlayer != null) {
			mTimeCount = 0;
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}

	/** 释放Timer */
	private void releaseTimer() {
		if (mTimer != null) {
			mTimer.cancel();
		}
	}

	/** 释放所有 */
	public void stopAll() {
		releaseTimer();
		stopRecord();
		try {
			RecorderHelper.releaseMediaRecorder();
		} catch (Exception e) {
			e.printStackTrace();
		}
		CameraHelper.releaseCamera();
		releasePlay();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnRecordFinishListener = (OnRecordFinishListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnRecordFinishListener");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mPlayer != null && mPlayer.isPlaying()) {
			mPlayer.pause();
			mProgressPauseFlag = true;
			mPausePos = mPlayer.getCurrentPosition();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		stopAll();
	}

	/* 文件保存地址 */
	private void createRecordDir() {
		File sampleDir = new File(FileUtil.getDiskCachePath(mActivity, AnchorCameraActivity.RECORD_VIDEO_FILE_PATH));
		if (!sampleDir.exists()) {
			sampleDir.mkdirs();
		}
		mRecordFile = new File(sampleDir.getAbsolutePath() + File.separator
				+ AnchorCameraActivity.RECORD_VIDEO_FILE_NAME);
	}

	/**
	 * 开始录制视频
	 * @param onRecordFinishListener 达到指定时间之后回调接口
	 */
	public void record(final OnRecordFinishListener onRecordFinishListener) {
		try {
			initCamera();
			if (mExceptionFlag)
				return;
			else
				initRecord();
			if (mExceptionFlag)
				return;
			releaseTimer();
			mTimeCount = 0;// 时间计数器重新赋值
			mProgressBar.setProgress(0);
			mProgressBar.setMax(RECORD_MAX_PROGRESS);
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (!mCancelFlag) {
						mTimeCount = mTimeCount + 1;
						mProgressBar.setProgress(mTimeCount);// 设置进度条
					}
					if (mTimeCount > 100) {// 达到指定时间，停止拍摄
						stopAll();
						if (onRecordFinishListener != null) {
							mHandler.sendEmptyMessage(MSG_RECORD_COMPLETE);
							if (!checkLocal(mRecordFile.getAbsolutePath())) {
								showAudioPermission();
								mRecordFile.delete();
								return;
							}
							onRecordFinishListener.onRecordFinish(mRecordFile.getAbsolutePath());
						}
					}
				}
			}, 0, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 检查是否存在本地文件/本地文件大小是否为0
	 * @param path
	 * @return
	 */
	private boolean checkLocal(String path) {
		if (TextUtils.isEmpty(path))
			return false;
		mRecordFile = new File(path);
		if (mRecordFile.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(mRecordFile);
				if (fis.available() <= 0) {
					showTips(R.string.anchor_toast_player_exception);
					mFilePath = "";
					return false;
				}
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} finally {
				if (fis != null)
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		return false;
	}

	/** 判断是否拥有相机权限 仅限于系统级判断 */
	private void showCameraPermission() {
		UiHelper.showPermissionDialog(mActivity, R.string.common_dialog_permission_question_live_title,
				R.string.camera_device, R.string.camera);
	}

	/** 判断是否拥有录音权限 仅限于系统级判断 */
	private void showAudioPermission() {
		UiHelper.showPermissionDialog(mActivity, R.string.common_dialog_permission_question_live_title,
				R.string.camera_audio, R.string.camera_audio);
	}

	/************************************** 接口回调 **************************************************/

	/** 录制完成回调接口 */
	public interface OnRecordFinishListener {
		void onRecordFinish(String filePath);
	}
	public void setmOnRecordFinishListener(OnRecordFinishListener mOnRecordFinishListener) {
		this.mOnRecordFinishListener = mOnRecordFinishListener;
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		// 录制完成
		if (msg.what == MSG_RECORD_COMPLETE) {
			releaseTimer();
			mFilePath = mRecordFile.getAbsolutePath();
			mIvPlay.setVisibility(View.VISIBLE);
		} else if (msg.what == MSG_PLAY_PAUSE_END && mPlayer != null) {
			// 暂停/播放结束
			mIvPlay.setVisibility(View.VISIBLE);
			releasePlay();
			releaseTimer();
		} else if (msg.what == MSG_UPDATE_PLAY_PROGRESS) {
			if (mPlayer != null)
				mProgressBar.setProgress(mPlayer.getCurrentPosition());
		}
	}

	private class SurfaceHolderCallBack implements Callback {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// || !TextUtils.isEmpty(mUrl)
			if (!TextUtils.isEmpty(mFilePath))
				mRecordFile = new File(mFilePath);
			if (mRecordFile != null && mRecordFile.exists() || !TextUtils.isEmpty(mUrl)) {
				mIvDirChange.setVisibility(View.GONE);
				mIvPlay.setVisibility(View.VISIBLE);
			} else {
				try {
					mIvDirChange.setVisibility(View.VISIBLE);
					mIvPlay.setVisibility(View.GONE);
					initCamera();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			stopAll();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.video_recorder_camera_dir:
			try {
				if (mCameraPos == 0) {
					mCameraPos = 1;
				} else {
					mCameraPos = 0;
				}
				int cameraId = CameraHelper.byDirectionGetCameraId(mCameraPos);
				CameraHelper.releaseCamera();
				mCamera = CameraHelper.openCamera(cameraId);
				setCamera();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			break;
		case R.id.anchor_camera_play:
			mIvPlay.setVisibility(View.GONE);
			mIvDirChange.setVisibility(View.GONE);
			mSurfaceView.setBackgroundResource(android.R.color.transparent);
			if (mPausePos > 0) {
				if (mPlayer != null && !mExceptionFlag) {
					mProgressPauseFlag = false;
					initPlayProgress(mPausePos);
					mPlayer.seekTo(mPausePos);
					mPlayer.start();
				}
			} else {
				mProgress = Utils.showProgress(mActivity);
				initPlayer();
				if (!mExceptionFlag) {
					mPlayer.prepareAsync();
				}
			}
			break;
		// 暂停
		case R.id.video_recorder_surfaceview:
			if (mPlayer != null && mPlayer.isPlaying()) {
				mIvPlay.setVisibility(View.VISIBLE);
				mPlayer.pause();
				mProgressPauseFlag = true;
				mPausePos = mPlayer.getCurrentPosition();
			}
			break;
		}
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		try {
			if (mr != null)
				mr.reset();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * MediaPlayer 大小改变
	 */
	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		resizeHolder(width, height);
	}

	private void resizeHolder(int width, int height) {
		if (width == 0 || height == 0) {
			return;
		}
		int holderWidth, holderHeight;
		float scaleWidth = Float.valueOf(width) / App.metrics.widthPixels;
		float scaleHight = Float.valueOf(height) / mHeight;
		if (scaleWidth > scaleHight) {
			holderWidth = App.metrics.widthPixels;
			holderHeight = (int) (height / scaleWidth);
		} else {
			holderWidth = (int) (width / scaleHight);
			holderHeight = mHeight;
		}
		RelativeLayout.LayoutParams layoutParms = new LayoutParams(holderWidth, holderHeight);
		layoutParms.addRule(RelativeLayout.CENTER_IN_PARENT);
		mSurfaceView.setLayoutParams(layoutParms);
	}

	/**
	 * MediaPlayer 准备
	 */
	@Override
	public void onPrepared(MediaPlayer mp) {
		if (mp.getVideoHeight() > 0) {
			if (mProgress != null && mProgress.isShowing())
				mProgress.dismiss();
			mPlayer.start();
			mProgressPauseFlag = false;
			initPlayProgress(mPausePos);
		}
	}

	/**
	 * MediaPlayer播放完成
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mProgress != null && mProgress.isShowing())
			mProgress.dismiss();
		mIvPlay.setVisibility(View.VISIBLE);
		releaseTimer();
		mProgressPauseFlag = true;
		mPausePos = 0;
	}

	/****** RootView接口 ******/
	@Override
	public void onCancel(boolean isRecording) {
		if (!isRecording) {
			stopAll();
			// 点击取消返回，无数据保存
			if (mRecordFile != null && mRecordFile.exists())
				mRecordFile.delete();
		} else {
			// 正在录制 点击取消，返回尚未录制界面
			mIvPlay.setVisibility(View.GONE);
			mIvDirChange.setVisibility(View.VISIBLE);
			// 点击取消返回，无数据保存
			if (mRecordFile != null && mRecordFile.exists())
				mRecordFile.delete();
			releaseTimer();
			stopRecord();
			try {
				RecorderHelper.releaseMediaRecorder();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mCancelFlag = true;
		mPausePos = 0;
		mProgressBar.setProgress(0);
		mTimeCount = 0;
	}

	/*
	 * com.efeizao.feizao.activities.AnchorCameraActivity.OnVideoStatusCallback
	 * #onRecordeAgain() 重新录制
	 */
	@Override
	public void onRecordeAgain() {
		if (mPlayer != null && mPlayer.isPlaying())
			mPlayer.pause();
		mProgressPauseFlag = true;
		releaseTimer();
		releasePlay();
		mSurfaceView.setBackgroundResource(android.R.color.transparent);
		mIvDirChange.setVisibility(View.VISIBLE);
		mIvPlay.setVisibility(View.GONE);
		mPausePos = 0;
		mProgressBar.setProgress(0);
		try {
			initCamera();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * com.efeizao.feizao.activities.AnchorCameraActivity.OnVideoStatusCallback
	 * #onRecord() 开始录制
	 */
	@Override
	public void onRecord() {
		mCancelFlag = false;
		mSurfaceView.setBackgroundResource(android.R.color.transparent);
		mIvPlay.setVisibility(View.GONE);
		mIvDirChange.setVisibility(View.GONE);
		mProgressBar.setProgress(0);
		record(mOnRecordFinishListener);
	}

}
