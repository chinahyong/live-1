package tv.live.bx.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.BitmapUtility;
import tv.live.bx.library.util.BitmapUtils;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.FileUtil;
import tv.live.bx.ui.ActionSheetDialog;
import tv.live.bx.ui.ActionSheetDialog.OnSheetItemClickListener;
import tv.live.bx.ui.ActionSheetDialog.SheetItemColor;
import tv.live.bx.ui.cropimage.CropImageActivity;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;

@SuppressLint("InlinedApi")
public class FanEditActivity extends BaseFragmentActivity {
	private static final int REQUEST_CODE_EDIT_NAME = 100;
	private static final int REQUEST_CODE_EDIT_INTRODUCTION = 101;
	private static int mCropImageHeigth;
	private static final String CropLogoImageName = "fan_logo_crop_result.jpg";
	private static final String CropBackgroupImageName = "fan_backgroup_crop_result.jpg";

	private RelativeLayout moLlPhoto, moLlTitle, moLlEditName, moLlEdtMenber, moLlEditIntroduction, moL1Background;
	private TextView mTvTitle, moTvName, mTvMenber, moTvIntroduction;
	private ImageView moIvPhoto, mIvBackground;
	private AlertDialog moProgress;

	/**
	 * 饭圈信息
	 */
	private Map<String, String> mFanInfo;

	private ActionSheetDialog actionSheetDialog;
	private File mCameraFile;
	static final int REQUEST_CAMERA = 0x100;
	static final int REQUEST_CROP = 0x101;
	static final int REQUEST_ALBUM = 0x102;

	/**
	 * 是否修改Logo
	 */
	private boolean isUpdateLogo;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_fan_edit_layout;
	}

	@Override
	protected void initMembers() {
		moLlPhoto = (RelativeLayout) findViewById(R.id.edit_data_ll_headpic);
		moLlTitle = (RelativeLayout) findViewById(R.id.edit_data_ll_title);
		moLlEditName = (RelativeLayout) findViewById(R.id.edit_data_ll_name);
		moLlEdtMenber = (RelativeLayout) findViewById(R.id.edit_data_ll_menber);
		moLlEditIntroduction = (RelativeLayout) findViewById(R.id.edit_data_ll_introduction);
		moL1Background = (RelativeLayout) findViewById(R.id.edit_data_ll_background);

		moIvPhoto = (ImageView) findViewById(R.id.edit_data_iv_headpic);
		mTvTitle = (TextView) findViewById(R.id.edit_data_tv_title);
		moTvName = (TextView) findViewById(R.id.edit_data_rb_name);
		mTvMenber = (TextView) findViewById(R.id.edit_data_edt_menber);
		moTvIntroduction = (TextView) findViewById(R.id.edit_data_tv_introduction);
		mIvBackground = (ImageView) findViewById(R.id.edit_data_iv_backgound);

		initTitle();
	}

	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.edit_user_title);
		mTopRightText.setText(R.string.edit_user_save);
		mTopRightTextLayout.setOnClickListener(new OnSubmit());
		mTopRightTextLayout.setVisibility(View.VISIBLE);
		mTopBackLayout.setOnClickListener(new OnBack());
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			mFanInfo = (Map<String, String>) bundle.getSerializable(FanDetailActivity.FAN_INFO);
		}
		if (mFanInfo != null) {
			ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity, moIvPhoto, mFanInfo.get("logo"));
			mTvTitle.setText(mFanInfo.get("name"));
			moTvName.setText(mFanInfo.get("nickname"));
			mTvMenber.setText(mFanInfo.get("memberTotal"));
			moTvIntroduction.setText(mFanInfo.get("detail"));
			ImageLoaderUtil.with().loadImage(mActivity, mIvBackground, mFanInfo.get("background"), R.drawable.bg_fan_detail_head, 0);
		}
		mCropImageHeigth = (int) (FeizaoApp.metrics.widthPixels * 0.7f);
	}

	@Override
	protected void setEventsListeners() {

		moLlTitle.setOnClickListener(new OnEditTitle());
		moLlEditIntroduction.setOnClickListener(new OnEditIntroduction());
		// 点击头像
		moLlPhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 弹出选择图片的加载方式
				showGetPhotoDialog(true);
			}
		});
		// 点击背景
		moL1Background.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 弹出选择图片的加载方式
				showGetPhotoDialog(false);
			}
		});
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (moProgress != null && moProgress.isShowing())
			moProgress.dismiss();
		switch (msg.what) {
			case MsgTypes.MODIFY_USER_INFO_SUCCESS:
				UiHelper.showShortToast(this, R.string.edit_user_save_success);
				setResult(RESULT_OK, null);
				finish();
				break;
			case MsgTypes.MODIFY_USER_INFO_FAILED:
				UiHelper.showToast(this, (String) msg.obj);
				break;
			default:
				break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	/**
	 * 弹出对话框
	 */
	private void showGetPhotoDialog(boolean flag) {
		isUpdateLogo = flag;
		actionSheetDialog = new ActionSheetDialog(FanEditActivity.this).builder().setCancelable(true)
				.setCanceledOnTouchOutside(true)
				.addSheetItem("拍照", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						takePhoto();
					}
				}).addSheetItem("从手机相册选择", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						selectPhoto();
					}
				});
		actionSheetDialog.show();

	}

	// 从本地相册选取图片作为头像
	private void selectPhoto() {
		Intent intentFromGallery = new Intent();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			intentFromGallery.setAction(Intent.ACTION_OPEN_DOCUMENT);
		} else {
			intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
		}
		// 设置文件类型
		intentFromGallery.setType("image/*");
		intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intentFromGallery, REQUEST_ALBUM);
	}

	/**
	 * 启动系统相机
	 */
	private void takePhoto() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		mCameraFile = FileUtil.getCameraPhotoFile();
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraFile));
		startActivityForResult(takePictureIntent, REQUEST_CAMERA);
		overridePendingTransition(R.anim.a_slide_in_down, 0);
	}

	@Override
	public void onActivityResult(int piRequestCode, int piResultCode, Intent poData) {
		switch (piRequestCode) {
			case REQUEST_CODE_EDIT_NAME:
				if (piResultCode == ChoiceNameActivity.RESULT_CODE_OK) {
					String lsNewName = poData.getStringExtra(EditInfoActivity.EDIT_CONTENT);
					mTvTitle.setText(lsNewName);
				}
				break;
			case REQUEST_CODE_EDIT_INTRODUCTION:
				if (piResultCode == ChoiceIntroductionActivity.RESULT_CODE_OK) {
					String lsNewIntroduction = poData.getStringExtra(EditInfoActivity.EDIT_CONTENT);
					moTvIntroduction.setText(lsNewIntroduction);
				}
				break;

			case REQUEST_ALBUM: // 从相册返回
				if (poData != null) {
					Uri uri = poData.getData();
					if (isUpdateLogo) {
						jumpToCrop(this, uri, 0, CropLogoImageName);
					} else {
						jumpToCrop(this, uri, mCropImageHeigth, CropBackgroupImageName);
					}
				}
				break;

			case REQUEST_CROP: // 从裁剪图片界面返回
				if (piResultCode == RESULT_OK && poData != null) {
					String path = poData.getStringExtra(CropImageActivity.EXA_IMAGE_PATH);
					EvtLog.d(TAG, "接受裁剪后的图片数据: " + path);

					// 设置到ImageView
					Bitmap bm = BitmapFactory.decodeFile(path);
					if (bm != null) {
						if (isUpdateLogo) {
							moIvPhoto.setImageBitmap(BitmapUtils.toRoundBitmap(bm));
							moIvPhoto.setTag(path);
						} else {
							mIvBackground.setImageBitmap(bm);
							mIvBackground.setTag(path);
						}
					}
				}
				break;

			case REQUEST_CAMERA: // 从相机返回
				EvtLog.d(TAG, "拍摄照片：" + mCameraFile);
				if (mCameraFile != null) {
					if (isUpdateLogo) {
						jumpToCrop(this, Uri.fromFile(mCameraFile), 0, CropLogoImageName);
					} else {
						jumpToCrop(this, Uri.fromFile(mCameraFile), mCropImageHeigth, CropBackgroupImageName);
					}
				}
				break;

			default:
				break;
		}

	}

	// 友盟统计
	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();
	}

	/**
	 * 编辑剪裁图片跳转
	 *
	 * @param context
	 * @param imgFile 图片文件的Uri
	 */
	private static void jumpToCrop(Context context, Uri imgFile, int cropImageHieght, String saveImageName) {
		try {
			Intent intent = new Intent(context, CropImageActivity.class);
			String path = BitmapUtility.getFilePathFromUri(context, imgFile);
			// String path = UriUtils.getPath(context, imgFile);
			intent.putExtra(CropImageActivity.EXA_IMAGE_PATH, path);
			if (cropImageHieght > 0)
				intent.putExtra(CropImageActivity.EXA_IMAGE_HEIGT, cropImageHieght);
			((Activity) context).startActivityForResult(intent, REQUEST_CROP);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*********************************** 事件处理器 ************************************/
	private class OnBack implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			onBackPressed();

		}
	}

	private class OnEditTitle implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			ActivityJumpUtil.toEditInfoActivity(mActivity, REQUEST_CODE_EDIT_NAME, mTvTitle.getText().toString(),
					mActivity.getResources().getString(R.string.edit_fan_title_tip), mActivity.getResources()
							.getString(R.string.edit_fan_title), 1, 10);
		}
	}

	private class OnEditIntroduction implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			ActivityJumpUtil.toEditInfoActivity(mActivity, REQUEST_CODE_EDIT_INTRODUCTION, moTvIntroduction.getText()
					.toString(), mActivity.getResources().getString(R.string.edit_fan_content_tip), mActivity
					.getResources().getString(R.string.edit_fan_content), 2, 40);
		}
	}

	private class OnSubmit implements OnClickListener {
		@Override
		public void onClick(View v) {
			// 1 取值
			String lvTitle = mTvTitle.getText().toString().trim();
			String lsDesc = moTvIntroduction.getText().toString();
			String logo = (String) moIvPhoto.getTag();
			String background = (String) mIvBackground.getTag();
			// 2 提交信息
			moProgress = Utils.showProgress(FanEditActivity.this);
			BusinessUtils.updateFanDetail(FanEditActivity.this, lvTitle, lsDesc, background, logo, mFanInfo.get("id"),
					new UpdateUserCallbackData());

		}
	}

	private class UpdateUserCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "UpdateUserCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MODIFY_USER_INFO_SUCCESS;
					sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MODIFY_USER_INFO_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}
	}

}
