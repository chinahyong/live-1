package tv.live.bx.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lonzh.lib.network.JSONParser;

import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.PhotoOperate;
import tv.live.bx.common.PhotoSelectImpl;
import tv.live.bx.common.Utils;
import tv.live.bx.common.photopick.ImageInfo;
import tv.live.bx.common.photopick.PhotoPickActivity;
import tv.live.bx.config.AppConfig;
import tv.live.bx.emoji.ParseEmojiMsgUtil;
import tv.live.bx.emoji.SelectFaceHelper;
import tv.live.bx.emoji.SelectFaceHelper.OnFaceOprateListener;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.BitmapUtility;
import tv.live.bx.library.util.BitmapUtils;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.FileUtil;
import tv.live.bx.library.util.Global;
import tv.live.bx.ui.ActionSheetDialog;
import tv.live.bx.ui.SelectMoudleDialog;
import tv.live.bx.ui.event.SimpleTextWatcher;
import tv.live.bx.util.UiHelper;

public class CreateFanActivity extends BaseFragmentActivity implements OnClickListener {
	public static final String CREATE_FAN_STATUS_KEY = "create_fan_status_key";
	public static final String CREATE_FAN_STATUS = "1";// 已创建
	public static final int PHOTO_MAX_COUNT = 1;
	public static final int RESULT_REQUEST_PICK_PHOTO = 1003;
	public static final int RESULT_REQUEST_IMAGE = 1007;

	private static final String File_Dir = "feizao_group_upload";

	/**
	 * 　表情
	 */
	private LinearLayout moGvEmotions;
	// /** 表情适配器 */
	// private EmotionGridAdapter moEmotionAdapter;
	/**
	 * 表情按钮
	 */
	private ImageView moIvEmotion;
	//底部总布局  2.5.0 隐藏
	private LinearLayout moBottomLayout;

	private SelectFaceHelper mFaceHelper;

	/**
	 * 帖子模块选择按钮
	 */
	private RelativeLayout mPostModuleLayout;

	private SelectMoudleDialog selectMoudleDialog;

	private List<Map<String, String>> mPostMoudleInfos;

	/**
	 * 帖子标题输入框
	 */
	private EditText mPostTitle;

	/**
	 * 帖子内容输入框
	 */
	private EditText mPostContent;

	/**
	 * 图片选择
	 */
	private GridView gridView;

	public static final String ANCHOR_ID = "anchor_id";
	/**
	 * 主播Id
	 */
	private String mAnchorId;

	private AlertDialog mProgress;

	private ArrayList<PhotoData> mData = new ArrayList<PhotoData>();

	private PhotoOperate photoOperate = new PhotoOperate(this);
	private File mCameraFile;

	private BaseAdapter adapter = new BaseAdapter() {

		public int getCount() {
			return mData.size() + 1;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return position;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			EvtLog.e(TAG, "position:" + position + "count:" + getCount() + "mData:" + mData.toString());
			if (convertView == null) {
				EvtLog.e(TAG, "null");
				holder = new ViewHolder();
				LayoutInflater mInflater = LayoutInflater.from(CreateFanActivity.this);
				holder.image = (ImageView) mInflater.inflate(R.layout.image_make_maopao, parent, false);
				holder.image.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			EvtLog.e(TAG, "getView position:" + position + "holder.uri:" + holder.uri);
			if (position == getCount() - 1) {
				if (getCount() == (PHOTO_MAX_COUNT + 1)) {
					holder.image.setVisibility(View.INVISIBLE);

				} else {
					holder.image.setVisibility(View.VISIBLE);
					holder.image.setImageResource(R.drawable.ic_add_image_button);
					holder.uri = "";
				}

			} else {
				holder.image.setVisibility(View.VISIBLE);
				PhotoData photoData = mData.get(position);
//				Uri data = photoData.uri;
				holder.uri = photoData.mImageinfo.path;

				ImageLoaderUtil.with().loadImage(mActivity, holder.image, holder.uri, 0, R.drawable.image_not_exist);
			}
			return holder.image;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			gridView.setVisibility(getCount() > 0 ? View.VISIBLE : View.GONE);
		}

		class ViewHolder {
			ImageView image;
			String uri = "";
		}

	};

	@Override
	protected int getLayoutRes() {

		return R.layout.activity_create_fan_layout;
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Intent intent = this.getIntent();
		if (intent != null) {
			mAnchorId = intent.getStringExtra(ANCHOR_ID);
		}
		// 加载本地缓存数据
		// AsyncTaskThreadPool.getThreadExecutorService().submit(new
		// LoadCacheDataTask());
	}

	protected void initMembers() {
		initTitle();
		moBottomLayout = (LinearLayout) findViewById(R.id.playing_ll_chat);
		moBottomLayout.setVisibility(View.GONE);
		moGvEmotions = (LinearLayout) findViewById(R.id.playing_gv_eomotions);
		moIvEmotion = (ImageView) findViewById(R.id.playing_iv_emotion);
		moIvEmotion.setOnClickListener(new OnShowHideEmotions());

		gridView = (GridView) findViewById(R.id.gridView);

		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == mData.size()) {
					ActionSheetDialog actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
							.setCanceledOnTouchOutside(true)
							.addSheetItem(getString(R.string.system_camera), ActionSheetDialog.SheetItemColor.BLACK, new ActionSheetDialog.OnSheetItemClickListener() {
								@Override
								public void onClick(int which) {
									mCameraFile = PhotoSelectImpl.takePhoto(mActivity);
								}
							}).addSheetItem(getString(R.string.system_gallery_select), ActionSheetDialog.SheetItemColor.BLACK, new ActionSheetDialog.OnSheetItemClickListener() {
								@Override
								public void onClick(int which) {
									PhotoSelectImpl.selectPhoto(mActivity);
								}
							});
					actionSheetDialog.show();
				} else {
					Intent intent = new Intent(CreateFanActivity.this, ImageBrowserActivity.class);
					ArrayList<String> arrayUri = new ArrayList<String>();
					for (PhotoData item : mData) {
						arrayUri.add(item.uri.toString());
					}
					intent.putExtra(ImageBrowserActivity.IMAGE_URL, arrayUri);
					intent.putExtra(ImageBrowserActivity.INIT_SHOW_INDEX, position);
					intent.putExtra(ImageBrowserActivity.IS_NEED_EIDT, true);
					startActivityForResult(intent, RESULT_REQUEST_IMAGE);
				}
			}
		});
		/** 监听图片变化 */
		adapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				updataSendBtn();
			}
		});

		gridView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Global.popSoftkeyboard(MaopaoAddActivity.this,
				// mEnterLayout.content, false);
				return false;
			}
		});

		mPostTitle = (EditText) findViewById(R.id.post_title);
		mPostTitle.setOnTouchListener(new OnInputText());
		mPostTitle.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				updataSendBtn();
			}
		});
		mPostTitle.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					moIvEmotion.setEnabled(false);
				} else {
					moIvEmotion.setEnabled(true);

				}
			}
		});

		mPostContent = (EditText) findViewById(R.id.post_content);
		mPostContent.setOnTouchListener(new OnInputText());
		mPostContent.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				updataSendBtn();
			}
		});

		mPostModuleLayout = (RelativeLayout) findViewById(R.id.post_module_layout);
		mPostModuleLayout.setVisibility(View.GONE);
		updataSendBtn();
		initDraftsInfo();
		adapter.notifyDataSetChanged();
	}

	/**
	 * 初始化草稿箱数据
	 */
	private void initDraftsInfo() {
		mPostTitle.setText(ParseEmojiMsgUtil.getExpressionString(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, Constants.SF_FAN_TITLE, "")));
		mPostContent.setText(ParseEmojiMsgUtil.getExpressionString(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, Constants.SF_FAN_CONTENT, "")));
		try {
			for (int i = 0; i < PHOTO_MAX_COUNT; i++) {
				String imagePath = Utils.getCfg(mActivity, Constants.PHOTO_INFO_SF_NAME, Constants.SF_FAN_PHOTO_CONTENT
						+ i, "");
				if (!TextUtils.isEmpty(imagePath)) {
					ImageInfo imageInfo = new ImageInfo(imagePath);
					Uri uri = Uri.parse(imageInfo.path);
					File outputFile = photoOperate.scal(uri);
					mData.add(new CreateFanActivity.PhotoData(outputFile, imageInfo));
				}
			}
		} catch (Exception e) {
			EvtLog.e(TAG, e.toString());
		}

	}

	/**
	 * 保存到草稿箱
	 */
	private void saveDrafts(String title, String content, Map<String, String> photoInfo) {
		Utils.setCfg(mActivity, Constants.SF_FAN_TITLE, title);
		Utils.setCfg(mActivity, Constants.SF_FAN_CONTENT, content);
		if (photoInfo != null) {
			Utils.setCfg(mActivity, Constants.PHOTO_INFO_SF_NAME, photoInfo);
		}
	}

	public void initWidgets() {
	}

	protected void setEventsListeners() {
		// 用于禁止用户手动换行
		mPostTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (!TextUtils.isEmpty(mPostTitle.getText().toString().trim())) {
			outState.putString("title", mPostTitle.getText().toString());
		}
		if (!TextUtils.isEmpty(mPostContent.getText().toString().trim())) {
			outState.putString("content", mPostContent.getText().toString());
		}
		if (!TextUtils.isEmpty(mAnchorId))
			outState.putString(ANCHOR_ID, mAnchorId);
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mAnchorId = savedInstanceState.getString(ANCHOR_ID);
		mPostTitle.setText(savedInstanceState.getString("title"));
		mPostContent.setText(savedInstanceState.getString("content"));
	}

	@Override
	public void onBackPressed() {
		if (moGvEmotions.getVisibility() == View.VISIBLE)
			moGvEmotions.setVisibility(View.GONE);
		else {
			if (TextUtils.isEmpty(mPostTitle.getText().toString().trim())
					&& TextUtils.isEmpty(mPostContent.getText().toString().trim()) && mData.isEmpty()) {
				// 清空之前保存的数据
				saveDrafts("", "", getCfgPhotoMap(null));
				finish();
			} else {
				// 按返回键弹出对话框
				UiHelper.showConfirmDialog(CreateFanActivity.this, R.string.post_save_tip, R.string.post_save_sure,
						R.string.post_save_cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								saveDrafts(mPostTitle.getText().toString(), mPostContent.getText().toString(),
										getCfgPhotoMap(mData));
								finish();
							}
						}, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 清空之前保存的数据
								saveDrafts("", "", getCfgPhotoMap(null));
								finish();
							}
						});
			}
		}
	}

	private void updataSendBtn() {
		if (Global.isEmptyContainSpace(mPostTitle) || Global.isEmptyContainSpace(mPostContent) || adapter.getCount() == 1) {
			mTopRightText.setSelected(true);
			mTopRightTextLayout.setEnabled(false);
		} else {
			mTopRightText.setSelected(false);
			mTopRightTextLayout.setEnabled(true);
		}
	}

	/**
	 * 更新选图数据
	 */
	private void updatePickData(String imagePath) {
		try {
			mData.clear();
			if (!imagePath.startsWith(Constants.FILE_PXI)) {
				imagePath = Constants.FILE_PXI + imagePath;
			}
			@SuppressWarnings("unchecked")
			Uri uri = Uri.parse(imagePath);
			File outputFile = photoOperate.scal(uri);
			mData.add(new CreateFanActivity.PhotoData(outputFile, new ImageInfo(imagePath)));
		} catch (Exception e) {
			showTips("缩放图片失败");
			EvtLog.e(TAG, e.toString());
		}
		adapter.notifyDataSetChanged();
	}

	/**
	 * 生成保存图片的shareprefrece map对象
	 */
	private Map<String, String> getCfgPhotoMap(ArrayList<PhotoData> mData) {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < PHOTO_MAX_COUNT; i++) {
			if (mData != null && i < mData.size()) {
				map.put(Constants.SF_FAN_PHOTO_CONTENT + i, mData.get(i).uri.toString());
			} else {
				map.put(Constants.SF_FAN_PHOTO_CONTENT + i, "");
			}
		}
		return map;
	}

	private class OnInputText implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			moGvEmotions.setVisibility(View.GONE);
			return false;
		}
	}

	/**
	 * 发送按钮 不可点，已过滤一下非法输入情况
	 */
	private class OnPublic implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (!AppConfig.getInstance().isLogged) {
				Utils.requestLoginOrRegister(mActivity, "登录后才能发帖，请登录", Constants.REQUEST_CODE_LOGIN);
				return;
			}

			// 1 获取用户输入
			String title = mPostTitle.getText().toString().trim();
			String content = mPostContent.getText().toString().trim();

			// 2 检查用户输入
			if (title.length() < 1 || title.length() > 10) {
				UiHelper.showToast(mActivity, R.string.edit_fan_title_tip);
				return;
			}

			if (content.length() < 2 || content.length() > 40) {
				UiHelper.showToast(mActivity, R.string.edit_fan_content_toast);
				return;
			}

			// 3 发表

			try {
				// CharSequence mTitleMsg =
				// ParseEmojiMsgUtil.convertToMsg(mPostTitle.getText(),
				// CreateFanActivity.this);//
				// 这里不要直接用mEditMessageEt.getText().toString();
				// CharSequence mContentMsg =
				// ParseEmojiMsgUtil.convertToMsg(mPostContent.getText(),
				// CreateFanActivity.this);
				ArrayList<String> files = compressImageFils(mData);
				String logo = null;
				if (files != null && files.size() > 0) {
					logo = files.get(0);
				} else {
					UiHelper.showToast(mActivity, R.string.commutity_fan_empty_logo_tip);
					return;
				}
				mProgress = Utils.showProgress(CreateFanActivity.this);
				BusinessUtils.createFanDetail(mActivity, title, content, logo, mAnchorId, new PublicPostCallbackData());
			} catch (Exception e) {
				e.printStackTrace();
				mProgress.dismiss();
				UiHelper.showToast(mActivity, "内部错误，请联系APP相关人员,请重试");
			}
		}
	}

	private ArrayList<String> compressImageFils(ArrayList<PhotoData> mList) {
		if (mList == null)
			return null;
		EvtLog.e(TAG, "compressImageFils mlist:" + mList.size());
		ArrayList<String> files = new ArrayList<String>();
		for (int i = 0; i < mList.size(); i++) {
			String resultPath = FileUtil.getDiskCachePath(mActivity, File_Dir) + File.separator + "pic_" + i + ".jpg";
			boolean flag = BitmapUtils.writeImage(
					BitmapUtility.LoadImageFromUrl(mActivity, mList.get(i).uri, FeizaoApp.metrics.heightPixels),
					resultPath, 50);
			if (flag) {
				files.add(resultPath);
			}
		}
		EvtLog.e(TAG, "compressImageFils mlist  end ");
		return files;
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.commutity_create_fan);
		mTopRightText.setText(R.string.commutity_fan_menber_status_create);
		mTopRightTextLayout.setOnClickListener(new OnPublic());
		mTopRightTextLayout.setVisibility(View.VISIBLE);
		mTopBackLayout.setOnClickListener(this);
	}

	/**
	 * 表情按钮事件
	 */
	private class OnShowHideEmotions implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			if (null == mFaceHelper) {
				mFaceHelper = new SelectFaceHelper(CreateFanActivity.this, moGvEmotions);
				mFaceHelper.setFaceOpreateListener(mOnFaceOprateListener);
			}
			if (moGvEmotions.getVisibility() == View.VISIBLE) {
				moGvEmotions.setVisibility(View.GONE);
				// 是否已经隐藏虚拟键盘
				// if (isKeyboardUp) {
				// isKeyboardUp = false;
				// }
			} else {
				InputMethodManager loImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				loImm.hideSoftInputFromWindow(getWindow().peekDecorView().getApplicationWindowToken(), 0);
				moGvEmotions.setVisibility(View.VISIBLE);
				// 是否已经弹出虚拟键盘
				// if (!isKeyboardUp) {
				// isKeyboardUp = true;
				// }

			}
		}
	}

	OnFaceOprateListener mOnFaceOprateListener = new OnFaceOprateListener() {
		@Override
		public void onFaceSelected(SpannableString spanEmojiStr) {
			if (null != spanEmojiStr) {
				if (mPostTitle.isFocused()) {
					mPostTitle.getText().insert(mPostTitle.getSelectionStart(), spanEmojiStr);
				} else if (mPostContent.isFocused()) {
					mPostContent.getText().insert(mPostContent.getSelectionStart(), spanEmojiStr);
				}
			}
		}

		@Override
		public void onFaceDeleted() {
			if (mPostTitle.isFocused()) {
				int selection = mPostTitle.getSelectionStart();
				String text = mPostTitle.getText().toString();
				if (selection > 0) {
					String text2 = text.substring(selection - 1);
					if ("]".equals(text2)) {
						int start = text.lastIndexOf("[");
						int end = selection;
						mPostTitle.getText().delete(start, end);
						return;
					}
					mPostTitle.getText().delete(selection - 1, selection);
				}
			} else if (mPostContent.isFocused()) {
				int selection = mPostContent.getSelectionStart();
				String text = mPostContent.getText().toString();
				if (selection > 0) {
					String text2 = text.substring(selection - 1);
					if ("]".equals(text2)) {
						int start = text.lastIndexOf("[");
						int end = selection;
						mPostContent.getText().delete(start, end);
						return;
					}
					mPostContent.getText().delete(selection - 1, selection);
				}
			}

		}

	};

	public static class PhotoData {
		ImageInfo mImageinfo;
		Uri uri = Uri.parse("");
		String serviceUri = "";

		public PhotoData(File file, ImageInfo info) {
			uri = Uri.fromFile(file);
			mImageinfo = info;
		}

		public PhotoData(PhotoDataSerializable data) {
			uri = Uri.parse(data.uriString);
			serviceUri = data.serviceUri;
			mImageinfo = data.mImageInfo;
		}
	}

	// 因为PhotoData包含Uri，不能直接序列化，所以有了这个类
	public static class PhotoDataSerializable implements Serializable {
		String uriString = "";
		String serviceUri = "";
		ImageInfo mImageInfo;

		public PhotoDataSerializable(PhotoData data) {
			uriString = data.uri.toString();
			serviceUri = data.serviceUri;
			mImageInfo = data.mImageinfo;
		}
	}

	private void startPhotoPickActivity() {
		int count = PHOTO_MAX_COUNT - mData.size();
		if (count <= 0) {
			showTips(String.format("最多能添加%d张图片", PHOTO_MAX_COUNT));
			return;
		}
		Intent intent = new Intent(CreateFanActivity.this, PhotoPickActivity.class);
		intent.putExtra(PhotoPickActivity.EXTRA_MAX, PHOTO_MAX_COUNT);

		ArrayList<ImageInfo> pickImages = new ArrayList<ImageInfo>();
		for (PhotoData item : mData) {
			pickImages.add(item.mImageinfo);
		}
		intent.putExtra(PhotoPickActivity.EXTRA_PICKED, pickImages);
		startActivityForResult(intent, RESULT_REQUEST_PICK_PHOTO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RESULT_REQUEST_PICK_PHOTO) {
			if (resultCode == Activity.RESULT_OK) {
				try {
					mData.clear();
					@SuppressWarnings("unchecked")
					ArrayList<ImageInfo> pickPhots = (ArrayList<ImageInfo>) data.getSerializableExtra("data");
					for (ImageInfo item : pickPhots) {
						Uri uri = Uri.parse(item.path);
						File outputFile = photoOperate.scal(uri);
						mData.add(new CreateFanActivity.PhotoData(outputFile, item));
					}
				} catch (Exception e) {
					showTips("缩放图片失败");
					EvtLog.e(TAG, e.toString());
				}
				adapter.notifyDataSetChanged();
			}
		} else if (requestCode == RESULT_REQUEST_IMAGE) {
			if (resultCode == RESULT_OK) {
				ArrayList<String> delUris = data.getStringArrayListExtra("mDelUrls");
				for (String item : delUris) {
					for (int i = 0; i < mData.size(); ++i) {
						if (mData.get(i).mImageinfo.path.equals(item)) {
							mData.remove(i);
						}
					}
					adapter.notifyDataSetChanged();
				}
			}
		} else if (requestCode == PhotoSelectImpl.REQUEST_CAMERA) {
			if (mCameraFile != null && resultCode == RESULT_OK) {
				updatePickData(mCameraFile.getPath());
				mCameraFile = null;
			}
		} else if (requestCode == PhotoSelectImpl.REQUEST_ALBUM) {
			// 相册选择
			if (data != null) {
				Uri uri = data.getData();
				String path = BitmapUtility.getFilePathFromUri(mActivity, uri);
				updatePickData(path);
			}
		}
	}

	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MsgTypes.MSG_POST_MOUDLE_SUCCESS:
				// 更新信息成功，跳转至更新界面
				mPostMoudleInfos = (List<Map<String, String>>) msg.obj;
				if (selectMoudleDialog != null) {
					selectMoudleDialog.setListData(mPostMoudleInfos);
				}
				break;
			case MsgTypes.MSG_POST_MOUDLE_FAILED:
				break;

			case MsgTypes.MSG_FAN_CREATE_UPDATE_SUCCESS:
				if (mProgress != null && mProgress.isShowing())
					mProgress.dismiss();
				Object[] objects = (Object[]) msg.obj;
				String errorMsg = (String) objects[0];
				Utils.setCfg(mActivity, CREATE_FAN_STATUS_KEY, CREATE_FAN_STATUS);
				// 更新本地数据
				UiHelper.showShortToast(this, R.string.edit_fan_success);
				Intent intent = new Intent();
				intent.putExtra(FanDetailActivity.FAN_INFO, (Serializable) objects[1]);
				setResult(RESULT_OK, intent);
				finish();
				break;
			case MsgTypes.MSG_FAN_CREATE_UPDATE_FAILED:
				if (mProgress != null && mProgress.isShowing())
					mProgress.dismiss();
				UiHelper.showToast(this, (String) msg.obj);
				break;

			default:
				break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.top_left:
				onBackPressed();
				break;
			case R.id.post_module_btn:
				break;
			default:
				break;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mProgress != null && mProgress.isShowing())
			mProgress.dismiss();
		InputMethodManager loImm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
		loImm.hideSoftInputFromWindow(mActivity.getWindow().peekDecorView().getApplicationWindowToken(), 0);
	}

	private class PublicPostCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "PublicPostCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_FAN_CREATE_UPDATE_SUCCESS;
					Object[] objects = new Object[]{errorMsg, JSONParser.parseOne((JSONObject) result)};
					msg.obj = objects;
					sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_FAN_CREATE_UPDATE_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}
	}

}
