package com.bixin.bixin.common.helper.camera;

import java.util.List;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
/**
 * Title: CameraHelper.java Description:相机操作类
 * @author Live
 * @version 2.1.2,2016.4.6
 */
public class CameraHelper {
	private static Camera mCamera;

	/** 开启相机 */
	public static Camera openCamera(int cameraId) {
		releaseCamera();
		if (cameraId < 0 || cameraId > Camera.getNumberOfCameras() - 1)
			mCamera = Camera.open();
		else
			mCamera = Camera.open(cameraId);
		return mCamera;
	}

	/**
	 * 打开 前置、后置摄像头
	 * @param 1:front 0:back
	 */
	public static int byDirectionGetCameraId(int dir) {
		int cameraId = -1;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			CameraInfo cameraInfo = new CameraInfo();
			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
				Camera.getCameraInfo(i, cameraInfo);
				if (dir == 1) {
					if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
						cameraId = i;
						break;
					}
				} else {
					if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
						cameraId = i;
						break;
					}
				}
			}
		}
		return cameraId;
	}

	/** 摄像相机通用参数 */
	public static Parameters setCameraParams(Activity context, int displayWidth, int displayHeight) {
		if (mCamera == null)
			return null;
		// 获取预览的各种分辨率
		Parameters cameraParams = mCamera.getParameters();
		// 设置90°
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mCamera.setDisplayOrientation(90);
		} else {
			if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				cameraParams.set("orientation", "portrait");
				cameraParams.set("rotation", 90);
			}
		}
		List<Size> supportedPreviewSizes = cameraParams.getSupportedPreviewSizes();
		Size previewSize = getOptimalPreviewSize(supportedPreviewSizes, displayWidth, displayHeight, 0);
		if (previewSize != null) {
			cameraParams.setPreviewSize(previewSize.width, previewSize.height);
		}
		if (cameraParams.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}
		mCamera.setParameters(cameraParams);
		return cameraParams;
	}

	/**
	 * 获取最接近分辨率 设置给相机指定功能的分辨率
	 * @param minWidth 最小的宽
	 */
	public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h, int minWidth) {
		// Use a very small tolerance because we want an exact match.
		final double ASPECT_TOLERANCE = 0.1;
		if (sizes == null)
			return null;
		// 判断显示尺寸传入是否有0
		if (w <= 0 || h <= 0)
			return sizes.get(sizes.size() - 1);
		// 获取显示预览图片的长宽比例
		double targetRatio = (double) h / w;
		Camera.Size optimalSize = null;
		// Try to find a preview size that matches aspect ratio and the target
		// view size.
		// Iterate over all available sizes and pick the largest size that can
		// fit in the view and
		// still maintain the aspect ratio.
		for (int k = sizes.size() - 1; k >= 0; k--) {
			Camera.Size size = sizes.get(k);
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) <= ASPECT_TOLERANCE) {
				optimalSize = size;
				return optimalSize;
			}
		}
		return sizes.get(sizes.size() - 1);
	}

	/** 释放相机 */
	public static void releaseCamera() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview(); // 停掉摄像头的预览
			mCamera.lock();
			mCamera.release(); // 释放资源
			mCamera = null;
		}
	}
}
