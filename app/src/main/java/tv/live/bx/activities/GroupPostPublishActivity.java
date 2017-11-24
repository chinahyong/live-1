package tv.live.bx.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.efeizao.bx.R;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.PhotoOperate;
import tv.live.bx.common.PhotoSelectImpl;
import tv.live.bx.common.Utils;
import tv.live.bx.common.WebConstants;
import tv.live.bx.common.photopick.ImageInfo;
import tv.live.bx.common.photopick.PhotoPickActivity;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.emoji.ParseEmojiMsgUtil;
import tv.live.bx.emoji.SelectFaceHelper;
import tv.live.bx.emoji.SelectFaceHelper.OnFaceOprateListener;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.BitmapUtility;
import tv.live.bx.library.util.BitmapUtils;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.Global;
import tv.live.bx.library.util.HtmlTagHandler;
import tv.live.bx.ui.ActionSheetDialog;
import tv.live.bx.ui.SelectMoudleDialog;
import com.lonzh.lib.network.JSONParser;

import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

public class GroupPostPublishActivity extends ShareDialogActivity implements OnClickListener {
	public static final int PHOTO_MAX_COUNT = 1;
	public static final int RESULT_REQUEST_PICK_PHOTO = 1003;
	public static final int RESULT_REQUEST_IMAGE = 1007;
	/**
	 * 发帖
	 */
	public static int REQUEST_CODE_PUBLIC_FRAGMENT = 1002;
	/**
	 * 插入选择饭圈
	 */
	public static int REQUEST_CODE_SELECT_FAN = 1004;

	/**
	 * 发帖选择饭圈
	 */
	public static int REQUEST_CODE_PUBLIC_SELECT_FAN = 1005;

	/**
	 * 同步分享按钮
	 */
	private CheckBox mFriendCheckBox, mSinaCheckBox, mSpaceCheckBox;

	private static final String MOUDLE_ID = "-1";
	private static final String MOUDLE_NAME = "请选择圈子";

	public static final String File_Dir = "feizao_upload";

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

	private SelectFaceHelper mFaceHelper;

	/**
	 * 帖子模块选择按钮
	 */
	private Button mPostModuleBtn;
	/**
	 * 帖子模块选择按钮
	 */
	private RelativeLayout mPostModuleLayout;

	private SelectMoudleDialog selectMoudleDialog;

	private Map<String, String> mPostMoudleInfo;

	/** 帖子标题输入框 */
	// private EditText mPostTitle;

	/**
	 * 帖子内容输入框
	 */
	private EditText mPostContent;

	private boolean mConfigPostInsertGroup = false;

	/**
	 * 图片选择
	 */
	private GridView gridView;

	private AlertDialog mProgress;

	private ArrayList<PhotoData> mData = new ArrayList<PhotoData>();

	private PhotoOperate photoOperate = new PhotoOperate(this);
	private File mCameraFile;        // 系统相机拍照存储地址

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
			if (convertView == null) {
				holder = new ViewHolder();
				LayoutInflater mInflater = LayoutInflater.from(GroupPostPublishActivity.this);
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

		return R.layout.activity_publish_post_layout;
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		int px = (int) getResources().getDimension(R.dimen.image_add_maopao_width);
		mConfigPostInsertGroup = Constants.COMMON_TRUE.equals(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME,
				Constants.SF_INSERT_GROUP_CONFIG_VERSION, Constants.COMMON_TRUE));
	}

	protected void initMembers() {
		initTitle();
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
					Intent intent = new Intent(GroupPostPublishActivity.this, ImageBrowserActivity.class);
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

		gridView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Global.popSoftkeyboard(MaopaoAddActivity.this,
				// mEnterLayout.content, false);
				return false;
			}
		});

		mFriendCheckBox = (CheckBox) findViewById(R.id.friendCircle);
		mSinaCheckBox = (CheckBox) findViewById(R.id.sinaCircle);
		mSpaceCheckBox = (CheckBox) findViewById(R.id.spaceCircle);

		mPostContent = (EditText) findViewById(R.id.post_content);
		mPostContent.requestFocus();
		mPostContent.setOnTouchListener(new OnInputText());
		mPostContent.setFilters(new InputFilter[]{new MyInputFilter()});
		// mPostContent.addTextChangedListener(new SimpleTextWatcher() {
		// @Override
		// public void afterTextChanged(Editable s) {
		// updataSendBtn();
		// }
		// });

		mPostModuleBtn = (Button) findViewById(R.id.post_module_btn);

		mPostModuleLayout = (RelativeLayout) findViewById(R.id.post_module_layout);
		mPostModuleLayout.setOnClickListener(this);

		mPostMoudleInfo = (Map<String, String>) getIntent().getSerializableExtra(FanDetailActivity.FAN_INFO);
		if (mPostMoudleInfo != null) {
			mPostModuleBtn.setText(mPostMoudleInfo.get("name"));
			mPostModuleBtn.setTag(mPostMoudleInfo.get("id"));
			Utils.setCfg(mActivity, Constants.SF_GROUP_ID, mPostMoudleInfo.get("id"));
			Utils.setCfg(mActivity, Constants.SF_GROUP_NAME, mPostMoudleInfo.get("name"));
		}
		// mPostModuleBtn.setTag(Utils.getCfg(mActivity,
		// Constants.COMMON_SF_NAME, Constants.SF_GROUP_ID, MOUDLE_ID));
		// mPostModuleBtn.setText(Utils.getCfg(mActivity,
		// Constants.COMMON_SF_NAME, Constants.SF_GROUP_NAME, MOUDLE_NAME));
		mPostModuleBtn.setTag(MOUDLE_ID);
		mPostModuleBtn.setText(MOUDLE_NAME);
		// updataSendBtn();
		initDraftsInfo();
		adapter.notifyDataSetChanged();
	}

	/**
	 * 初始化草稿箱数据
	 */
	private void initDraftsInfo() {
		mPostContent.setText(ParseEmojiMsgUtil.getExpressionString(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, Constants.SF_POST_CONTENT, "")));

		try {
			for (int i = 0; i < PHOTO_MAX_COUNT; i++) {
				String imagePath = Utils.getCfg(mActivity, Constants.PHOTO_INFO_SF_NAME,
						Constants.SF_POST_PHOTO_CONTENT + i, "");
				if (!TextUtils.isEmpty(imagePath)) {
					ImageInfo imageInfo = new ImageInfo(imagePath);
					Uri uri = Uri.parse(imageInfo.path);
					File outputFile = photoOperate.scal(uri);
					mData.add(new GroupPostPublishActivity.PhotoData(outputFile, imageInfo));
				}
			}
		} catch (Exception e) {
			EvtLog.e(TAG, e.toString());
		}

	}

	/**
	 * 保存到草稿箱
	 */
	private void saveDrafts(String content, Map<String, String> photoInfo) {
		Utils.setCfg(mActivity, Constants.SF_POST_CONTENT, content);
		if (photoInfo != null) {
			Utils.setCfg(mActivity, Constants.PHOTO_INFO_SF_NAME, photoInfo);
		}
	}

	public void initWidgets() {
	}

	protected void setEventsListeners() {
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (!TextUtils.isEmpty(mPostContent.getText().toString().trim())) {
			outState.putString("content", mPostContent.getText().toString());
		}
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mPostContent.setText(savedInstanceState.getString("content"));
	}

	@Override
	public void onBackPressed() {
		if (moGvEmotions.getVisibility() == View.VISIBLE)
			moGvEmotions.setVisibility(View.GONE);
		else {
			if (TextUtils.isEmpty(mPostContent.getText().toString().trim()) && mData.isEmpty()) {
				// 清空之前保存的数据
				saveDrafts("", getCfgPhotoMap(null));
				finish();
			} else {
				// 按返回键弹出对话框
				UiHelper.showConfirmDialog(GroupPostPublishActivity.this, R.string.post_save_tip,
						R.string.post_save_sure, R.string.post_save_cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								saveDrafts(mPostContent.getText().toString(), getCfgPhotoMap(mData));
								finish();
							}
						}, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 清空之前保存的数据
								saveDrafts("", getCfgPhotoMap(null));
								finish();
							}
						});
			}
		}
	}

	/**
	 * 识别输入框的是不是#符号
	 */
	private class MyInputFilter implements InputFilter {

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			// TODO Auto-generated method stub
			if (source.toString().equalsIgnoreCase(Constants.COMMON_INSERT_POST_PIX)) {
				if (mConfigPostInsertGroup) {
					ActivityJumpUtil.gotoActivityForResult(mActivity, MeFanSelectActivity.class,
							GroupPostPublishActivity.REQUEST_CODE_SELECT_FAN, null, null);
				}
			}
			return source;
		}
	}

	private void updataSendBtn() {
		if (Global.isEmptyContainSpace(mPostContent)) {
			mTopRightText.setSelected(true);
			mTopRightTextLayout.setEnabled(false);
		} else {
			mTopRightText.setSelected(false);
			mTopRightTextLayout.setEnabled(true);
		}
	}

	/**
	 * 生成保存图片的shareprefrece map对象
	 */
	private Map<String, String> getCfgPhotoMap(ArrayList<PhotoData> mData) {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < PHOTO_MAX_COUNT; i++) {
			if (mData != null && i < mData.size()) {
				map.put(Constants.SF_POST_PHOTO_CONTENT + i, mData.get(i).uri.toString());
			} else {
				map.put(Constants.SF_POST_PHOTO_CONTENT + i, "");
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
			String content = mPostContent.getText().toString().trim();
			String moudleId = mPostModuleBtn.getTag().toString();

			if (content.length() <= 0 && mData.size() <= 0) {
				UiHelper.showToast(mActivity, R.string.post_content_less_tip);
				return;
			}

			if (mData.size() > 0 && mData.get(0).mImageLength > PhotoSelectImpl.IMAGE_SIZE_MAX_LIMIT) {
				UiHelper.showToast(mActivity, R.string.commutity_select_image_big);
				return;
			}

			if (MOUDLE_ID.equals(moudleId)) {
				UiHelper.showToast(mActivity, R.string.post_moudle_empty_tip);
				return;
			}

			// 3 发表
			mProgress = Utils.showProgress(GroupPostPublishActivity.this);
			try {
				// 这里不要直接用mEditMessageEt.getText().toString();
				CharSequence msgStr = HtmlTagHandler.convertToMsg(mPostContent.getText(), mActivity, true);
				// 这里不要直接用mEditMessageEt.getText().toString();
				// CharSequence mContentMsg =
				// ParseEmojiMsgUtil.convertToMsg(mPostContent.getText(),
				// GroupPostPublishActivity.this);
				ArrayList<String> files = compressImageFils(mData);
				BusinessUtils.publicGroupPostInfo(mActivity, new PublicPostCallbackData(), moudleId, msgStr.toString(),
						files);
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
			files.add(BitmapUtility.getFilePathFromUri(mActivity, mList.get(i).uri));
			// String resultPath = FileUtil.getDiskCachePath(mActivity,
			// File_Dir) + File.separator + "pic_" + i + ".jpg";
			// boolean flag = BitmapUtils.writeImage(
			// BitmapUtility.LoadImageFromUrl(mActivity, mList.get(i).uri,
			// FeizaoApp.metrics.heightPixels),
			// resultPath, 50);
			// if (flag) {
			// files.add(resultPath);
			// }
		}
		EvtLog.e(TAG, "compressImageFils mlist  end ");
		return files;
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.post_publish_title);
		mTopRightText.setText(R.string.send_msg);
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
				mFaceHelper = new SelectFaceHelper(GroupPostPublishActivity.this, moGvEmotions);
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
				if (mPostContent.isFocused()) {
					mPostContent.getText().insert(mPostContent.getSelectionStart(), spanEmojiStr);
				}
			}
		}

		@Override
		public void onFaceDeleted() {
			if (mPostContent.isFocused()) {
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
		long mImageLength;
		Uri uri = Uri.parse("");
		String serviceUri = "";

		public PhotoData(File file, ImageInfo info) {
			mImageLength = file.length();
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

	/**
	 * 选图功能
	 *
	 * @deprecated
	 */
	private void startPhotoPickActivity2() {
		int count = PHOTO_MAX_COUNT - mData.size();
		if (count <= 0) {
			showTips(String.format("最多能添加%d张图片", PHOTO_MAX_COUNT));
			return;
		}
		Intent intent = new Intent(GroupPostPublishActivity.this, PhotoPickActivity.class);
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
				updatePickData2(data);
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
		} else if (requestCode == REQUEST_CODE_PUBLIC_SELECT_FAN) {
			if (resultCode == RESULT_OK) {
				mPostMoudleInfo = (Map<String, String>) data.getSerializableExtra(FanDetailActivity.FAN_INFO);
				mPostModuleBtn.setText(mPostMoudleInfo.get("name"));
				mPostModuleBtn.setTag(mPostMoudleInfo.get("id"));
				Utils.setCfg(mActivity, Constants.SF_GROUP_ID, mPostMoudleInfo.get("id"));
				Utils.setCfg(mActivity, Constants.SF_GROUP_NAME, mPostMoudleInfo.get("name"));
			}
		} else if (requestCode == GroupPostPublishActivity.REQUEST_CODE_SELECT_FAN) {
			if (resultCode == RESULT_OK) {
				Map<String, String> groupData = (Map<String, String>) data
						.getSerializableExtra(FanDetailActivity.FAN_INFO);
				// 获取光标当前位置
				int curIndex = mPostContent.getSelectionStart();
				// 把要#的人插入光标所在位置
				// inputEditText.getText().insert(curIndex,
				// groupData.get("name"));
				// 通过输入#符号进入好友列表并返回#的人，要删除之前输入的#
				if (curIndex >= 1) {
					mPostContent.getText().replace(curIndex - 1, curIndex, "");
				}
				setAtImageSpan(curIndex, groupData.get("id"), groupData.get("name"));
				// inputEditText.append(spanStr);
			}
		} else if (requestCode == PhotoSelectImpl.REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
			if (mCameraFile != null) {
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

	private void setAtImageSpan(int selectionIndex, String groupId, String groupName) {
		final Bitmap bmp = BitmapUtils.getNameBitmap(groupName,
				(int) mActivity.getResources().getDimension(R.dimen.a_text_size_40));
		String spString = "[" + groupId + "," + groupName + "]";
		SpannableString ss = new SpannableString(spString);
		BitmapDrawable drawable = new BitmapDrawable(getResources(), bmp);
		drawable.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
		ImageSpan imageSpan = new ImageSpan(drawable, spString, DynamicDrawableSpan.ALIGN_BASELINE);

		// 把取到的要@的人名，用DynamicDrawableSpan代替
		ss.setSpan(imageSpan, 0, ss.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

		// inputEditText.setTextKeepState(ss);
		mPostContent.getText().insert(mPostContent.getSelectionStart(), ss);
	}

	/**
	 * 更新选图数据
	 *
	 * @deprecated
	 */
	private void updatePickData2(Intent data) {
		try {
			mData.clear();
			@SuppressWarnings("unchecked")
			ArrayList<ImageInfo> pickPhots = (ArrayList<ImageInfo>) data.getSerializableExtra("data");
			for (ImageInfo item : pickPhots) {
				Uri uri = Uri.parse(item.path);
				File outputFile = photoOperate.scal(uri);
				mData.add(new GroupPostPublishActivity.PhotoData(outputFile, item));
			}
		} catch (Exception e) {
			showTips("缩放图片失败");
			EvtLog.e(TAG, e.toString());
		}
		adapter.notifyDataSetChanged();
	}

	/**
	 * 更新选图数据
	 *
	 * @param imagePath 通过Tusdk选图的路径
	 */
	private void updatePickData(String imagePath) {
		try {
			mData.clear();
			if (!imagePath.startsWith(Constants.FILE_PXI)) {
				imagePath = Constants.FILE_PXI + imagePath;
			}
			@SuppressWarnings("unchecked")
			Uri uri = Uri.parse(imagePath);
			//生成上传的图片
			File outputFile = photoOperate.scal(uri);
			mData.add(new GroupPostPublishActivity.PhotoData(outputFile, new ImageInfo(imagePath)));
		} catch (Exception e) {
			showTips("缩放图片失败");
			EvtLog.e(TAG, e.toString());
		}
		adapter.notifyDataSetChanged();
	}

	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MsgTypes.MSG_PUBLIC_POST_SUCCESS:
				if (mProgress != null && mProgress.isShowing())
					mProgress.dismiss();
				Map<String, String> data = (Map<String, String>) msg.obj;
				// 清空之前保存的数据
				saveDrafts("", getCfgPhotoMap(null));
				UiHelper.showShortToast(this, data.get("errorMsg"));

				// 如果不需要审核
				if (!Constants.COMMON_TRUE.equals(data.get("needVerify"))) {
					// 设置分享内容
					if (!TextUtils.isEmpty(UserInfoConfig.getInstance().headPic))
						shareUrImg = UserInfoConfig.getInstance().headPic;
					// 替换图片
					shareContent = mPostContent.getText().toString().trim();
					if (TextUtils.isEmpty(shareContent)) {
						shareContent = mActivity.getResources().getString(R.string.commutity_post_share_content);
					}
					shareUrl = WebConstants.getFullWebMDomain(WebConstants.SHARE_GROUP_POST_PIX) + data.get("id");
					shareTitle = mActivity.getResources().getString(R.string.commutity_share_post_title,
							UserInfoConfig.getInstance().nickname);
					if (mFriendCheckBox.isChecked()) {
						onPengyouquanClick();
					}
					if (mSinaCheckBox.isChecked()) {
						onWeiBoClick();
					}
					if (mSpaceCheckBox.isChecked()) {
						onQqZoneClick();
					}
				}
				setResult(RESULT_OK);
				finish();
				break;
			case MsgTypes.MSG_PUBLIC_POST_FAILED:
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
				super.onBackPressed();
				break;
			case R.id.post_module_layout:
				ActivityJumpUtil.gotoActivityForResult(mActivity, MeFanSelectActivity.class,
						REQUEST_CODE_PUBLIC_SELECT_FAN, null, null);
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
	}

	private class PublicPostCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "PublicPostCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_PUBLIC_POST_SUCCESS;
					Map<String, String> data = JSONParser.parseOne((JSONObject) result);
					data.put("errorMsg", errorMsg);
					msg.obj = data;
					sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_PUBLIC_POST_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}
	}

}
