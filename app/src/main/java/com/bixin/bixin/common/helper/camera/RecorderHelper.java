package com.bixin.bixin.common.helper.camera;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OutputFormat;
import android.media.MediaRecorder.VideoEncoder;
import android.media.MediaRecorder.VideoSource;

/**
 * Title: RecordHelper.java Description:视频录制操作类
 * @author Live
 * @version 2.1.2,2016.4.6
 */
public class RecorderHelper {
	private static MediaRecorder mMediaRecorder;

	public static MediaRecorder getMediaRecorder() {
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.reset();
		return mMediaRecorder;
	}

	/**
	 * 初始化录制 MeidaRecorder
	 * @param dir 摄像头（1:前置 0：后置）
	 * @param filePath 视频保存地址
	 */
	public static void initMediaRecorder(Camera camera, int dir, String filePath) {
		if (mMediaRecorder == null)
			return;
		mMediaRecorder.setCamera(camera);
		mMediaRecorder.setVideoSource(VideoSource.CAMERA);// 视频源
		mMediaRecorder.setAudioSource(AudioSource.MIC);// 音频源
		mMediaRecorder.setVideoEncodingBitRate(1 * 1280 * 720);// 设置帧频率，然后就清晰了
		mMediaRecorder.setOutputFormat(OutputFormat.MPEG_4);// 视频输出格式
		mMediaRecorder.setAudioEncoder(AudioEncoder.AAC);// 音频格式
		mMediaRecorder.setVideoEncoder(VideoEncoder.H264);// 视频录制格式
		if (dir == 0)
			mMediaRecorder.setOrientationHint(90);// 输出旋转90度，保持竖屏录制
		else
			mMediaRecorder.setOrientationHint(270);// 输出旋转270度，保持竖屏录制
		mMediaRecorder.setOutputFile(filePath);
	}

	/** 停止录制 */
	public static void stopMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.setOnErrorListener(null);
			try {
				mMediaRecorder.stop();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
			mMediaRecorder.setPreviewDisplay(null);
		}
	}

	/** 释放录制 */
	public static void releaseMediaRecorder() {
		if (mMediaRecorder != null) {
			mMediaRecorder.setOnErrorListener(null);
			try {
				mMediaRecorder.release();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		mMediaRecorder = null;
	}
}
