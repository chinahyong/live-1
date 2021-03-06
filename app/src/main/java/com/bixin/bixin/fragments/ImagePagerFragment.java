package com.bixin.bixin.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.bixin.bixin.App;
import com.bumptech.glide.request.target.Target;
import com.yanzhenjie.album.Album;

import java.io.File;

import tv.guojiang.baselib.image.listener.ImageLoadingListener;
import tv.live.bx.R;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.util.BitmapUtils;
import com.bixin.bixin.library.util.DateUtil;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.FileUtil;
import com.bixin.bixin.ui.ActionSheetDialog;
import com.bixin.bixin.ui.ActionSheetDialog.OnSheetItemClickListener;
import com.bixin.bixin.ui.ActionSheetDialog.SheetItemColor;
import com.bixin.bixin.ui.photoview.PhotoView;
import com.bixin.bixin.ui.photoview.PhotoViewAttacher;

/**
 * Created by chaochen on 2014-9-7.
 */
public class ImagePagerFragment extends BaseFragment {

	private ImageView circleLoading;
	private PhotoView imageLoad;
	private String uri;
	private ActionSheetDialog actionSheetDialog;
	public static String IMAGE_URL = "IMAGE_URL";
	public static String IMAGE_CACHE_PATH = "imageSave";
	public static String IMAGE_FIX = ".jpg";

	public static final int HTTP_CODE_FILE_NOT_EXIST = 1304;
	private final View.OnClickListener onClickImageClose = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mActivity.onBackPressed();
		}
	};
	private final PhotoViewAttacher.OnPhotoTapListener onPhotoTapClose = new PhotoViewAttacher.OnPhotoTapListener() {
		@Override
		public void onPhotoTap(View view, float v, float v2) {
//			mActivity.onBackPressed();
		}
	};
	private final PhotoViewAttacher.OnViewTapListener onViewTapListener = new PhotoViewAttacher.OnViewTapListener() {
		@Override
		public void onViewTap(View view, float v, float v1) {
//			mActivity.onBackPressed();
		}
	};

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_image_pager_item;
	}

	@Override
	protected void initMembers() {

		circleLoading = (ImageView) mRootView.findViewById(R.id.dialog_progress_iv);
		circleLoading.setVisibility(View.INVISIBLE);

		imageLoad = (PhotoView) mRootView.findViewById(R.id.imageLoad);
		imageLoad.setOnPhotoTapListener(onPhotoTapClose);
		imageLoad.setOnViewTapListener(onViewTapListener);

		Animation loAnimRotate = AnimationUtils.loadAnimation(mActivity, R.anim.rotate_clockwise);
		LinearInterpolator loLin = new LinearInterpolator();
		loAnimRotate.setInterpolator(loLin);
		circleLoading.startAnimation(loAnimRotate);

	}

	@Override
	protected void initWidgets() {

		// TODO Auto-generated method stub

	}

	@Override
	protected void setEventsListeners() {

		// TODO Auto-generated method stub

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Override
	protected void initData(Bundle bundle) {
		if (bundle != null) {
			uri = bundle.getString(IMAGE_URL);
			EvtLog.d(TAG, "initData bundle IMAGE_URL:" + uri);
		}
		if (!TextUtils.isEmpty(uri)) {
			showPhoto();
		} else {
			imageLoad.setImageResource(R.drawable.image_not_exist);
		}
	}

	@Override
	public void onDestroyView() {
//		if (imageLoad != null) {
//			if (imageLoad instanceof PhotoView) {
//				try {
//					((GlideBitmapDrawable) (imageLoad).getDrawable()).getBitmap().recycle();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
		super.onDestroyView();
	}

	/**
	 * 弹出对话框
	 */
	private void showGetPhotoDialog(final Bitmap bitmap) {
		actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
				.setCanceledOnTouchOutside(true)
				.addSheetItem("保存到手机", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						String destPath = FileUtil.getDiskCachePath(mActivity, IMAGE_CACHE_PATH) + File.separator
								+ DateUtil.fmtTimeMillsToString(System.currentTimeMillis(), DateUtil.sdf1) + IMAGE_FIX;
						boolean flag = BitmapUtils.writeImage(bitmap, destPath, 100);
						showTips(flag ? R.string.commutity_image_save_success : R.string.commutity_image_save_fail);
					}
				});
		actionSheetDialog.show();

	}

	private void showPhoto() {
		if (!isAdded()) {
			return;
		}
		uri = uri.replace(Constants.FILE_PXI, "");
		// 不是Uri地址,并且存在://，说明是网络地址
		if (uri.indexOf("://") != -1) {
			ImageLoaderUtil.getInstance().loadImage(uri, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL, new ImageLoadingListener() {
				@Override
				public void onLoadStarted(Drawable placeholder) {
					if (!isAdded()) {
						return;
					}
					EvtLog.d(TAG, "onLoadingStarted IMAGE_URL:" + uri);
					circleLoading.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadFailed(Drawable drawable) {
					if (!isAdded()) {
						return;
					}
					EvtLog.d(TAG, "onLoadingFailed IMAGE_URL:" + uri);
					circleLoading.clearAnimation();
					circleLoading.setVisibility(View.GONE);
					imageLoad.setImageResource(R.drawable.image_not_exist);
				}

				@Override
				public void onLoadingComplete(Drawable resource) {
					if (!isAdded()) {
						return;
					}
					EvtLog.d(TAG, "onLoadingComplete IMAGE_URL:" + uri);
					circleLoading.clearAnimation();
					circleLoading.setVisibility(View.GONE);

					try {
						imageLoad.setImageDrawable(resource);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onLoadCleared(Drawable placeholder) {
					if (!isAdded()) {
						return;
					}
					EvtLog.d(TAG, "onLoadingCancelled IMAGE_URL:" + uri);
					circleLoading.clearAnimation();
					circleLoading.setVisibility(View.GONE);
				}
			});
		} else {
			// 本地地址
			Album.getAlbumConfig().getImageLoader().loadImage(imageLoad, uri, App.metrics.widthPixels, App.metrics.heightPixels);
			circleLoading.clearAnimation();
			circleLoading.setVisibility(View.GONE);
		}
	}
}
