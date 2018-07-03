package com.bixin.bixin.ui.cropimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.bixin.bixin.App;
import java.io.File;
import java.io.IOException;

import tv.live.bx.R;
import com.bixin.bixin.base.act.BaseFragmentActivity;
import com.bixin.bixin.library.util.BitmapUtility;
import com.bixin.bixin.library.util.BitmapUtils;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.FileUtil;
import com.bixin.bixin.library.util.PackageUtil;

/**
 * Title: CropImageActivity.java</br> Description: 裁剪图片界面</br> Copyright:
 * Copyright (c) 2008</br>
 *
 * @version 1.0
 * @CreateDate 2014-6-12
 */
public class CropImageActivity extends BaseFragmentActivity {

	public static final String EXA_IMAGE_PATH = "image_path";

	public static final String EXA_IMAGE_SAVE_PATH = "image_save_path";

	public static final String EXA_IMAGE_HEIGT = "image_heigth";

	static final String TAG = "CropImageActivity";

	static final String IMAGE_NAME = "crop_result.jpg";

	static final int CROP_WIDTH = PackageUtil.getScreenWidth(App.mContext);
	static int CROP_HEIGHT = CROP_WIDTH;

	private CropImageView mCropImageView;

	private String mImagePath;

	private String mImageSavePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initTopBar();
		Intent intent = getIntent();
		if (intent != null) {
			mImagePath = intent.getStringExtra(EXA_IMAGE_PATH);
			mImageSavePath = intent.getStringExtra(EXA_IMAGE_SAVE_PATH);
			CROP_HEIGHT = intent.getIntExtra(EXA_IMAGE_HEIGT, CROP_WIDTH);
		}

		EvtLog.d(TAG, "图片路径：" + mImagePath);
		if (mImagePath == null) {
			EvtLog.e(TAG, "参数为空，退出Activity");
			finish();
			return;
		}
		Drawable drawable = null;
		try {
			// 没有做机型适配
//			drawable = Drawable.createFromPath(mImagePath);
			/**
			 * 用于三星、锤子手机系统相机的兼容(图片旋转)
			 */
//			BitmapFactory bitmapFac = new BitmapFactory();
			int maxSize = App.metrics.heightPixels > App.metrics.widthPixels ? App.metrics.heightPixels : App.metrics.widthPixels;
			Bitmap bitmap = BitmapUtility.LoadImageFromUrl(mImagePath, maxSize);
			// 获取图片角度，然后旋转回去
			bitmap = rotaingImageView(readPictureDegree(mImagePath), bitmap);
//			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
//					bitmap.getHeight(), matrix, true);
			drawable = new BitmapDrawable(bitmap);
		} catch (Exception e) {
		}
		if (drawable != null) {
			mCropImageView.setDrawable(drawable, CROP_WIDTH, CROP_HEIGHT);
		} else {
			EvtLog.e(TAG, "获取Drawable对象失败，退出Activity");
			finish();
		}
	}

	@Override
	protected int getLayoutRes() {

		// TODO Auto-generated method stub
		return R.layout.a_common_activity_crop_image;
	}

	@Override
	public void initWidgets() {
		mCropImageView = (CropImageView) findViewById(R.id.cropImageView);
	}

	@Override
	protected void setEventsListeners() {

		// TODO Auto-generated method stub

	}

	@Override
	protected void initData(Bundle savedInstanceState) {

		// TODO Auto-generated method stub

	}

	/**
	 * 读取图片属性：旋转的角度
	 *
	 * @param path 图片绝对路径
	 * @return degree旋转的角度
	 */
	private int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 旋转图片
	 *
	 * @param angle  被旋转角度
	 * @param bitmap 图片对象
	 * @return 旋转后的图片
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		Bitmap returnBm = null;
		// 根据旋转角度，生成旋转矩阵
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		try {
			// 将原始图片按照旋转矩阵进行旋转，并得到新的图片
			returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		} catch (OutOfMemoryError e) {
		}
		if (returnBm == null) {
			returnBm = bitmap;
		}
		if (bitmap != returnBm) {
			bitmap.recycle();
		}
		return returnBm;
	}

	@Override
	public void finish() {
		super.finish();
	}

	private void initTopBar() {
		TextView btnLeft = (TextView) findViewById(R.id.left);
		TextView btnRight = (TextView) findViewById(R.id.right);
		// btnLeft.setImageResource(R.drawable.a_common_btn_cancel_selector);

		btnLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		btnRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				submit();
			}
		});
	}

	/**
	 * 提交裁剪完成的图片到SD中
	 */
	private void submit() {
		if (TextUtils.isEmpty(mImageSavePath))
			mImageSavePath = IMAGE_NAME;
		String cropResultPath = FileUtil.getCameraPhotoFile().getParentFile().getPath() + File.separator
				+ mImageSavePath;
		BitmapUtils.writeImage(mCropImageView.getCropImage(), cropResultPath, 30);

		Intent intent = new Intent();
		EvtLog.e(TAG, "ImagePath After Crop：" + cropResultPath);
		intent.putExtra(EXA_IMAGE_PATH, cropResultPath);
		setResult(RESULT_OK, intent);
		finish();
	}

}
