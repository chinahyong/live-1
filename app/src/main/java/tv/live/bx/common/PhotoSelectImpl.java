package tv.live.bx.common;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import tv.live.bx.library.util.BitmapUtility;
import tv.live.bx.library.util.FileUtil;
import tv.live.bx.ui.cropimage.CropImageActivity;
import com.yanzhenjie.album.util.AlbumUtils;

import java.io.File;

import static com.umeng.socialize.utils.ContextUtil.getContext;

/**
 * Created by Live on 2017/3/23.
 */

public class PhotoSelectImpl {
	public static final int REQUEST_CAMERA = 0x1021;
	public static final int REQUEST_CROP = 0x1020;
	public static final int REQUEST_ALBUM = 0x1011;

	/**
	 * 选择图片的最大的大小限制 2M，单位B
	 */
	public static final int IMAGE_SIZE_MAX_LIMIT = 2 * 1024 * 1024;

	// 从本地相册选取图片作为头像
	public static void selectPhoto(Activity context) {
		Intent intentFromGallery = new Intent();
		if (Utils.greaterThanNowSDKVersion(android.os.Build.VERSION_CODES.KITKAT)) {
			intentFromGallery.setAction(Intent.ACTION_OPEN_DOCUMENT);
		} else {
			intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
		}
		// 设置文件类型
		intentFromGallery.setType("image/*");
		context.startActivityForResult(intentFromGallery, REQUEST_ALBUM);
	}

	// 从本地相册选取图片作为头像
	public static void selectPhoto(Fragment context) {
		Intent intentFromGallery = new Intent();
		if (Utils.greaterThanNowSDKVersion(android.os.Build.VERSION_CODES.KITKAT)) {
			intentFromGallery.setAction(Intent.ACTION_OPEN_DOCUMENT);
		} else {
			intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
		}
		// 设置文件类型
		intentFromGallery.setType("image/*");
		context.startActivityForResult(intentFromGallery, REQUEST_ALBUM);
	}

	/**
	 * 启动系统相机
	 */
	@TargetApi(Build.VERSION_CODES.M)
	public static File takePhoto(Activity context) {
//		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
//		context.startActivityForResult(takePictureIntent, REQUEST_CAMERA);
//		context.overridePendingTransition(R.anim.a_slide_in_down, 0);
		if (Utils.greaterThanNowSDKVersion(Build.VERSION_CODES.M)) {
			boolean cameraResult = PermissionUtil.permissionIsGranted(context, Manifest.permission.CAMERA);
			boolean storageResult = PermissionUtil.permissionIsGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
			if (cameraResult && storageResult) {
				return cameraWithPermission(context);
			} else if (cameraResult) {
				context.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtil.REQUEST_PERMISSION_CAMERA);
			} else if (storageResult) {
				context.requestPermissions(new String[]{Manifest.permission.CAMERA}, PermissionUtil.REQUEST_PERMISSION_CAMERA);
			} else {
				context.requestPermissions(
						new String[]{
								Manifest.permission.CAMERA,
								Manifest.permission.WRITE_EXTERNAL_STORAGE
						},
						PermissionUtil.REQUEST_PERMISSION_CAMERA);
			}
			return null;
		} else {
			return cameraWithPermission(context);
		}
	}

	public static File cameraWithPermission(Activity context) {
		File cameraFile = FileUtil.getCameraPhotoFile();
		AlbumUtils.startCamera(context, REQUEST_CAMERA, cameraFile);
		return cameraFile;
	}

	public static File cameraWithPermission(Fragment context) {
		File cameraFile = FileUtil.getCameraPhotoFile();
		AlbumUtils.startCamera(context, REQUEST_CAMERA, cameraFile);
		return cameraFile;
	}

	/**
	 * 启动系统相机
	 */

	public static File takePhoto(Fragment context) {
//		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
//		context.startActivityForResult(takePictureIntent, REQUEST_CAMERA);
//		context.overridePendingTransition(R.anim.a_slide_in_down, 0);
		if (Utils.greaterThanNowSDKVersion(Build.VERSION_CODES.M)) {
			boolean cameraResult = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
					== PackageManager.PERMISSION_GRANTED;
			boolean storageResult = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_GRANTED;
			if (cameraResult && storageResult) {
				return cameraWithPermission(context);
			} else if (cameraResult) {
				context.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtil.REQUEST_PERMISSION_CAMERA);
			} else if (storageResult) {
				context.requestPermissions(new String[]{Manifest.permission.CAMERA}, PermissionUtil.REQUEST_PERMISSION_CAMERA);
			} else {
				context.requestPermissions(
						new String[]{
								Manifest.permission.CAMERA,
								Manifest.permission.WRITE_EXTERNAL_STORAGE
						},
						PermissionUtil.REQUEST_PERMISSION_CAMERA);
			}
			return null;
		} else {
			return cameraWithPermission(context);
		}
	}

	/**
	 * 编辑剪裁图片跳转
	 *
	 * @param context
	 * @param imgFile 图片文件的Uri
	 */
	public static void jumpToCrop(Activity context, Uri imgFile) {
		try {
			Intent intent = new Intent(context, CropImageActivity.class);
			String path = BitmapUtility.getFilePathFromUri(context, imgFile);
			// String path = UriUtils.getUrl(context, imgFile);
			intent.putExtra(CropImageActivity.EXA_IMAGE_PATH, path);
			context.startActivityForResult(intent, REQUEST_CROP);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
