package tv.live.bx.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.HorizontalListViewPhotoAdapter;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Consts;
import tv.live.bx.common.JacksonUtil;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.PhotoSelectImpl;
import tv.live.bx.common.Utils;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.BitmapUtils;
import tv.live.bx.library.util.DateTimePickDialogUtil;
import tv.live.bx.library.util.DateUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.listeners.RecyclerViewOnItemClickListener;
import tv.live.bx.model.AlbumBean;
import tv.live.bx.ui.ActionSheetDialog;
import tv.live.bx.ui.ActionSheetDialog.OnSheetItemClickListener;
import tv.live.bx.ui.ActionSheetDialog.SheetItemColor;
import tv.live.bx.ui.cropimage.CropImageActivity;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;


@SuppressLint("InlinedApi")
public class EditDataActivity extends BaseFragmentActivity implements OnClickListener {
	private String TAG = "EditDataActivity";
	private static final int REQUEST_CODE_EDIT_NAME = 100;
	private static final int REQUEST_CODE_EDIT_INTRODUCTION = 101;
	private static final int REQUEST_CODE_EDIT_ALBUM = 103;
	private static final int GET_ALBUM_SUCCED = 103;
	private static final int GET_ALBUM_FAILED = -103;

	public static final String PERSON_INFO = "person_info";
	public static final String IS_EDITABLE = "is_editable";

	private RelativeLayout moLlEditName, moLlEditIntroduction, moLlEdtBirthday, moLlHeadPic, moLlSex, moLlPhoto;
	private RecyclerView mHorizontallList;
	private HorizontalListViewPhotoAdapter mHorizontallAdapter;
	private TextView moTvName, moTvIntroduction, mTvConstellation;
	private TextView moEdtBirthday;
	private ImageView moIvPhoto;
	private TextView moRbMale;
	private AlertDialog moProgress;

	private String initDateTime = "1990年1月1日"; // 初始化开始时间
	private ActionSheetDialog actionSheetDialog;
	private File mCameraFile;

	public static boolean isEditable = true;
	private boolean isSexChange = false; // 是否是修改性别

	/* 临时保存修改后的数据(该数据尚未成功提交服务器更新) */
	private String mNickName;
	private int mCurSex;
	private String mBirthday;
	private String mSignature;
	private ArrayList<AlbumBean> mAlbumBeans;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_edit_data;
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Intent intent = getIntent();
		if (intent != null) {
			isEditable = intent.getBooleanExtra(EditDataActivity.IS_EDITABLE, true);
		}

		if (AppConfig.getInstance().isLogged) {
			// 1 取值
			String lsPhoto = UserInfoConfig.getInstance().headPic;
			String lsNickname = UserInfoConfig.getInstance().nickname;
			String lsDesc = UserInfoConfig.getInstance().signature;
			String lsSex = UserInfoConfig.getInstance().sex + "";
			String lsBirthday = UserInfoConfig.getInstance().birthday;
			mSignature = lsDesc;
			mCurSex = Integer.parseInt(lsSex);
			mBirthday = lsBirthday;
			mNickName = lsNickname;
			//头像
			if (!TextUtils.isEmpty(lsPhoto)) {
				if (lsPhoto.indexOf("://") == -1) {
					lsPhoto = Constants.FILE_PXI + lsPhoto;
				}
				ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity, moIvPhoto, lsPhoto);
			}
			// 昵称
			moTvName.setText(lsNickname);
			// 签名
			if (!Utils.isStrEmpty(lsDesc))
				moTvIntroduction.setText(lsDesc);
			// 生日
			if (!Utils.isStrEmpty(lsBirthday)) {
				moEdtBirthday.setText(lsBirthday);
				String[] splitStr = lsBirthday.split("-");
				// 数组长度为3说明格式正确为年月日并且以‘-’分割
				if (splitStr != null && splitStr.length == 3) {
					int constellation = DateUtil.getConstellation(Integer.parseInt(splitStr[1]), Integer.parseInt(splitStr[2]));
					mTvConstellation.setText(constellation);
				}
			}
			// 性别
			if (lsSex != null) {
				int liSex = Integer.parseInt(lsSex);
				switch (liSex) {
					case Consts.GENDER_MALE:
						moRbMale.setText(R.string.male);
						moRbMale.setTag(Consts.GENDER_MALE);
						break;
					case Consts.GENDER_FEMALE:
						moRbMale.setText(R.string.female);
						moRbMale.setTag(Consts.GENDER_FEMALE);
						break;
					default:
						break;
				}
				mCurSex = liSex;
			}
			BusinessUtils.getAlbums(mActivity, UserInfoConfig.getInstance().id, new GetAlbumCallbackHandle());
		}
	}

	@Override
	protected void initMembers() {
		// widgets
		moLlEditName = (RelativeLayout) findViewById(R.id.edit_data_ll_name);
		moLlEditIntroduction = (RelativeLayout) findViewById(R.id.edit_data_ll_introduction);
		moLlEdtBirthday = (RelativeLayout) findViewById(R.id.edit_data_ll_birthday);
		moLlHeadPic = (RelativeLayout) findViewById(R.id.edit_data_ll_headpic);
		moLlPhoto = (RelativeLayout) findViewById(R.id.edit_data_ll_photo);
		moLlPhoto.setClickable(false);
		moLlSex = (RelativeLayout) findViewById(R.id.edit_data_ll_sex);
		moTvName = (TextView) findViewById(R.id.edit_data_tv_name);
		moTvIntroduction = (TextView) findViewById(R.id.edit_data_tv_introduction);
		mTvConstellation = (TextView) findViewById(R.id.edit_data_edt_constellation);
		moIvPhoto = (ImageView) findViewById(R.id.edit_data_iv_headpic);
		moEdtBirthday = (TextView) findViewById(R.id.edit_data_edt_birthday);
		moRbMale = (TextView) findViewById(R.id.edit_data_rb_male);
		mHorizontallList = (RecyclerView) findViewById(R.id.edit_data_photo_list);

		// members
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(moEdtBirthday.getWindowToken(), 0);
		moEdtBirthday.setInputType(0);
		LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		// 设置Item间距
		mHorizontallList.addItemDecoration(new RecyclerView.ItemDecoration() {
			@Override
			public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
				super.getItemOffsets(outRect, view, parent, state);
				outRect.set(0, 0, Utils.dip2px(mActivity, 6), 0);
			}
		});
		mHorizontallList.setLayoutManager(layoutManager);
		//设置固定大小
		mHorizontallList.setHasFixedSize(true);
		mHorizontallAdapter = new HorizontalListViewPhotoAdapter(mActivity);
		mHorizontallAdapter.setOnItemClick(new RecyclerViewOnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("gallery", mHorizontallAdapter.getData());
				ActivityJumpUtil.toActivityAndBundle(mActivity, EditAlbumActivity.class, bundle, REQUEST_CODE_EDIT_ALBUM);
			}
		});
		mHorizontallList.setAdapter(mHorizontallAdapter);
		initTitle();
	}

	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.edit_user_title);
		mTopRightTextLayout.setVisibility(View.GONE);
		mTopBackLayout.setOnClickListener(new OnBack());
	}

	@Override
	public void initWidgets() {

	}

	@Override
	protected void setEventsListeners() {
		moLlPhoto.setOnClickListener(this);
		moLlEditName.setOnClickListener(this);
		moLlEditIntroduction.setOnClickListener(this);
		moLlEdtBirthday.setOnClickListener(this);
		moLlHeadPic.setOnClickListener(this);
		moLlSex.setOnClickListener(this);
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (moProgress != null && moProgress.isShowing())
			moProgress.dismiss();
		switch (msg.what) {
			case MsgTypes.MODIFY_USER_INFO_SUCCESS:
				handleUpdateUserInfo(msg);
				UiHelper.showShortToast(this, R.string.edit_user_save_success);
				break;
			case MsgTypes.MODIFY_USER_INFO_FAILED:
				UiHelper.showToast(this, (String) msg.obj);
				break;
			case GET_ALBUM_SUCCED:
				mAlbumBeans = (ArrayList<AlbumBean>) msg.obj;
				if (mAlbumBeans == null || mAlbumBeans.isEmpty()) {
					mHorizontallAdapter.clearData();
					mHorizontallAdapter.notifyDataSetChanged();
				} else {
					mHorizontallAdapter.clearData();
					mHorizontallAdapter.addData(mAlbumBeans);
				}
				moLlPhoto.setClickable(true);
				break;
			case GET_ALBUM_FAILED:
				UiHelper.showToast(mActivity, String.valueOf(msg.obj));
				moLlPhoto.setClickable(false);
				break;
			default:
				break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onActivityResult(int piRequestCode, int piResultCode, Intent poData) {
		switch (piRequestCode) {
			case REQUEST_CODE_EDIT_NAME:
				if (piResultCode == ChoiceNameActivity.RESULT_CODE_OK) {
					mNickName = poData.getStringExtra("name");
					moTvName.setText(mNickName);
				}
				break;
			case REQUEST_CODE_EDIT_INTRODUCTION:
				if (piResultCode == ChoiceIntroductionActivity.RESULT_CODE_OK) {
					mSignature = poData.getStringExtra("introduction");
					moTvIntroduction.setText(mSignature);
				}
				break;

			case PhotoSelectImpl.REQUEST_ALBUM: // 从相册返回
				if (poData != null) {
					Uri uri = poData.getData();
					PhotoSelectImpl.jumpToCrop(this, uri);
				}
				break;
			case PhotoSelectImpl.REQUEST_CAMERA:
				if (mCameraFile != null && piResultCode == RESULT_OK) {
					Uri imgUri = Uri.fromFile(mCameraFile);
					if (imgUri != null) {
						PhotoSelectImpl.jumpToCrop(mActivity, imgUri);
					}
				}
				break;
			case PhotoSelectImpl.REQUEST_CROP: // 从裁剪图片界面返回
				if (piResultCode == RESULT_OK && poData != null) {
					moIvPhoto.setTag(poData.getStringExtra(CropImageActivity.EXA_IMAGE_PATH));
					requestSave();
				}
				break;
			case REQUEST_CODE_EDIT_ALBUM:
				if (piResultCode == RESULT_OK) {
					ArrayList<AlbumBean> albumBeans = poData.getParcelableArrayListExtra("albums");
					if (albumBeans != null) {
						mAlbumBeans = albumBeans;
						if (albumBeans.isEmpty()) {
							mHorizontallAdapter.clearData();
							mHorizontallAdapter.notifyDataSetChanged();
						} else {
							List<AlbumBean> adapterData = new ArrayList<>();
							for (AlbumBean bean : mAlbumBeans) {
								// 上传失败
								if (bean.getStatus() == -1) {
									adapterData.add(bean);
								}
							}
							mAlbumBeans.removeAll(adapterData);
							mHorizontallAdapter.clearData();
							mHorizontallAdapter.addData(mAlbumBeans);
						}
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

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			// 编辑相册
			case R.id.edit_data_ll_photo:
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("gallery", mAlbumBeans);
				ActivityJumpUtil.toActivityAndBundle(mActivity, EditAlbumActivity.class, bundle, REQUEST_CODE_EDIT_ALBUM);
				break;
			// 编辑头像
			case R.id.edit_data_ll_headpic:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickHead");
				// 弹出选择图片的加载方式
				showGetPhotoDialog();
				break;
			// 编辑性别
			case R.id.edit_data_ll_sex:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickSex");
				if (UserInfoConfig.getInstance().canEditSex)
					showChangeSex();
				else
					showTips(R.string.edit_update_toast_sex_no);
				break;
			//编辑昵称
			case R.id.edit_data_ll_name:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickUsername");
				gotoActivityForResult(ChoiceNameActivity.class, REQUEST_CODE_EDIT_NAME, PERSON_INFO, null);
				break;
			//编辑个签
			case R.id.edit_data_ll_introduction:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickAutograph");
				gotoActivityForResult(ChoiceIntroductionActivity.class, REQUEST_CODE_EDIT_INTRODUCTION, PERSON_INFO, null);
				break;
			// 编辑生日
			case R.id.edit_data_ll_birthday:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickBirthday");
				final String birth = moEdtBirthday.getText().toString();
				SimpleDateFormat format = new SimpleDateFormat(DateUtil.DATE_FORMAT_3);
				try {
					Date date = format.parse(birth);
					initDateTime = new SimpleDateFormat(DateUtil.DATE_FORMAT_4).format(date);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				final DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(EditDataActivity.this, initDateTime);
				Dialog dialog = dateTimePicKDialog.dateTimePicKDialog(moEdtBirthday);
				dialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (TextUtils.isEmpty(dateTimePicKDialog.getResultTime())) {
							mBirthday = birth;
							return;
						}
						if (!birth.equals(dateTimePicKDialog.getResultTime())) {
							mBirthday = dateTimePicKDialog.getResultTime();
							requestSave();
						}
					}
				});
				break;
		}
	}

	/**
	 * 修改性别sheetDialog
	 */
	private void showChangeSex() {
		actionSheetDialog = new ActionSheetDialog(EditDataActivity.this).builder()
				.setTitle(getString(R.string.edit_update_sex)).setCancelable(false).setCanceledOnTouchOutside(true)
				.addSheetItem(getString(R.string.male), SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						mCurSex = Consts.GENDER_MALE;
						actionSheetDialog.getDialog().cancel();
						showResultSex();
					}
				}).addSheetItem(getString(R.string.female), SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						mCurSex = Consts.GENDER_FEMALE;
						actionSheetDialog.getDialog().cancel();
						showResultSex();
					}
				});
		actionSheetDialog.show();
	}

	/**
	 * 是否修改
	 */
	private void showResultSex() {
		actionSheetDialog = new ActionSheetDialog(EditDataActivity.this).builder()
				.setTitle(String.format(getString(R.string.edit_update_sex_result), Consts.getGender(mCurSex)))
				.setCancelable(false).setCanceledOnTouchOutside(false)
				.addSheetItem(getString(R.string.edit_user_save), SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						MobclickAgent.onEvent(FeizaoApp.mConctext, "saveSexModification");
						isSexChange = true;
						requestSave();
					}
				});
		actionSheetDialog.show();
	}

	/**
	 * 弹出对话框
	 */
	private void showGetPhotoDialog() {
		actionSheetDialog = new ActionSheetDialog(EditDataActivity.this).builder().setCancelable(true)
				.setCanceledOnTouchOutside(true)
				.addSheetItem(getString(R.string.system_camera), SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						mCameraFile = PhotoSelectImpl.takePhoto(mActivity);
					}
				}).addSheetItem(getString(R.string.system_gallery_select), SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						PhotoSelectImpl.selectPhoto(mActivity);
					}
				});
		actionSheetDialog.show();
	}

	/**
	 * 提交编辑
	 */
	private void requestSave() {
		// 1 取值
		// 2 提交信息
		moProgress = Utils.showProgress(EditDataActivity.this);
		BusinessUtils.modifyUserInfo(EditDataActivity.this, new UpdateUserCallbackData(), mNickName, mCurSex, mSignature,
				mBirthday, (String) moIvPhoto.getTag());
	}

	/**
	 * 用户信息修改成功，回调修改UI显示以及本地缓存数据
	 */
	private void handleUpdateUserInfo(Message msg) {
		Map<String, String> lmInfo = new HashMap<>();
		//昵称
		if (!TextUtils.isEmpty(mNickName)) {
			lmInfo.put("nickname", mNickName);
			moTvName.setText(mNickName);
		}
		//个签
		if (mSignature != null) {
			lmInfo.put("signature", mSignature);
			moTvIntroduction.setText(mSignature);
		}
		//生日
		if (!TextUtils.isEmpty(mBirthday)) {
			lmInfo.put("birthday", mBirthday);
			moEdtBirthday.setText(mBirthday);
			// 星座
			String[] splitStr = mBirthday.split("-");
			// 数组长度为3说明格式正确为年月日并且以‘-’分割
			if (splitStr != null && splitStr.length == 3) {
				int constellation = DateUtil.getConstellation(Integer.parseInt(splitStr[1]), Integer.parseInt(splitStr[2]));
				mTvConstellation.setText(constellation);
			}
		}
		//性别是否可更改
		if (isSexChange) {
			isSexChange = false;
			lmInfo.put("canEditSex", "false");
		}
		//性别
		lmInfo.put("sex", String.valueOf(mCurSex));
		moRbMale.setText(Consts.getGender(mCurSex));
		moRbMale.setTag(mCurSex);
		//头像
		if (msg.obj != null) {
			Map<String, String> temp = (Map<String, String>) msg.obj;
			lmInfo.put("headPic", temp.get("headPic"));
			Bitmap bm = BitmapFactory.decodeFile((String) moIvPhoto.getTag());
			if (bm != null) {
				moIvPhoto.setImageBitmap(BitmapUtils.toRoundBitmap(bm));
				moIvPhoto.setTag("");//更新完了，置空避免没有更改头像也提交图片
			}
		}
		UserInfoConfig.getInstance().updateFromMap(lmInfo);
	}

	/* ********************************** 事件处理器 *********************************** */
	private class OnBack implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			onBackPressed();
		}
	}

	/**
	 * 用户信息更新请求
	 */
	private class UpdateUserCallbackData implements CallbackDataHandle {
		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "UpdateUserCallbackData success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				msg.what = MsgTypes.MODIFY_USER_INFO_SUCCESS;
				try {
					msg.obj = JSONParser.parseOne((JSONObject) result);
				} catch (Exception e) {
				}
				sendMsg(msg);
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

	private class GetAlbumCallbackHandle implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetAlbumCallbackHandle success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				msg.what = GET_ALBUM_SUCCED;
				try {
					List<AlbumBean> mPhotoList = new ArrayList<>();
					// 如果相册列表不为空
					if (result != null && !TextUtils.isEmpty(String.valueOf(result))) {
						try {
							mPhotoList = JacksonUtil.readValue(String.valueOf(result), List.class, AlbumBean.class);
							msg.obj = mPhotoList;
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						msg.obj = mPhotoList;
					}
				} catch (Exception e) {
					msg.what = GET_ALBUM_FAILED;
				}
			} else {
				msg.what = GET_ALBUM_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
			}
			sendMsg(msg);
		}
	}
}
