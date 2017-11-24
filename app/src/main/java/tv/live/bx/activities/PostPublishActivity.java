package tv.live.bx.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.AsyncTaskThreadPool;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.PhotoOperate;
import tv.live.bx.common.Utils;
import tv.live.bx.common.photopick.ImageInfo;
import tv.live.bx.common.photopick.PhotoPickActivity;
import tv.live.bx.config.AppConfig;
import tv.live.bx.database.DatabaseUtils;
import tv.live.bx.emoji.ParseEmojiMsgUtil;
import tv.live.bx.emoji.SelectFaceHelper;
import tv.live.bx.emoji.SelectFaceHelper.OnFaceOprateListener;
import tv.live.bx.fragments.GroupSubjectFragment;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.BitmapUtility;
import tv.live.bx.library.util.BitmapUtils;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.FileUtil;
import tv.live.bx.library.util.Global;
import tv.live.bx.tasks.BaseRunnable;
import tv.live.bx.ui.SelectMoudleDialog;
import tv.live.bx.ui.event.SimpleTextWatcher;
import com.lonzh.lib.network.JSONParser;

import org.json.JSONArray;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

public class PostPublishActivity extends BaseFragmentActivity implements OnClickListener {
	public static final int PHOTO_MAX_COUNT = 6;
	public static final int RESULT_REQUEST_PICK_PHOTO = 1003;
	public static final int RESULT_REQUEST_IMAGE = 1007;
	/**
	 * 发帖
	 */
	public static int REQUEST_CODE_PUBLIC_FRAGMENT = 1002;

	private static final String MOUDLE_ID = "-1";
	private static final String MOUDLE_NAME = "请选择板块";

	private static final String File_Dir = "feizao_upload";

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

	private AlertDialog mProgress;

	private ArrayList<PhotoData> mData = new ArrayList<PhotoData>();

	private PhotoOperate photoOperate = new PhotoOperate(this);

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
				LayoutInflater mInflater = LayoutInflater.from(PostPublishActivity.this);
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
				ImageLoaderUtil.with().loadImage(mActivity, holder.image, holder.uri, R.drawable.icon_loading, R.drawable.image_not_exist);
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
		// 加载本地缓存数据
		AsyncTaskThreadPool.getThreadExecutorService().submit(new LoadCacheDataTask());
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
					startPhotoPickActivity();

				} else {
					Intent intent = new Intent(PostPublishActivity.this, ImageBrowserActivity.class);
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

		mPostModuleBtn = (Button) findViewById(R.id.post_module_btn);
		mPostModuleBtn.setOnClickListener(this);

		mPostModuleBtn.setTag(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, Constants.SF_GROUP_ID, MOUDLE_ID));
		mPostModuleBtn.setText(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, Constants.SF_GROUP_NAME, MOUDLE_NAME));
		updataSendBtn();
		initDraftsInfo();
		adapter.notifyDataSetChanged();
	}

	/**
	 * 初始化草稿箱数据
	 */
	private void initDraftsInfo() {
		mPostTitle.setText(ParseEmojiMsgUtil.getExpressionString(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, Constants.SF_POST_TITLE, "")));
		mPostContent.setText(ParseEmojiMsgUtil.getExpressionString(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, Constants.SF_POST_CONTENT, "")));

		try {
			for (int i = 0; i < PHOTO_MAX_COUNT; i++) {
				String imagePath = Utils.getCfg(mActivity, Constants.PHOTO_INFO_SF_NAME,
						Constants.SF_POST_PHOTO_CONTENT + i, "");
				if (!TextUtils.isEmpty(imagePath)) {
					ImageInfo imageInfo = new ImageInfo(imagePath);
					Uri uri = Uri.parse(imageInfo.path);
					File outputFile = photoOperate.scal(uri);
					mData.add(new PostPublishActivity.PhotoData(outputFile, imageInfo));
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
		Utils.setCfg(mActivity, Constants.SF_POST_TITLE, title);
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
		if (!TextUtils.isEmpty(mPostTitle.getText().toString().trim())) {
			outState.putString("title", mPostTitle.getText().toString());
		}
		if (!TextUtils.isEmpty(mPostContent.getText().toString().trim())) {
			outState.putString("content", mPostContent.getText().toString());
		}
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
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
				UiHelper.showConfirmDialog(PostPublishActivity.this, R.string.post_save_tip, R.string.post_save_sure,
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
		if (Global.isEmptyContainSpace(mPostTitle) || Global.isEmptyContainSpace(mPostContent)) {
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
			String title = mPostTitle.getText().toString().trim();
			String content = mPostContent.getText().toString().trim();
			String moudleId = mPostModuleBtn.getTag().toString();

			if (MOUDLE_ID.equals(moudleId)) {
				showToast(R.string.post_moudle_empty_tip, TOAST_SHORT);
				return;
			}
			// 2 检查用户输入
			if (title.length() < 5 || title.length() > 15) {
				showToast(R.string.post_name_hit, TOAST_SHORT);
				return;
			}

			if (content.length() < 20) {
				showToast(R.string.post_content_less_tip, TOAST_SHORT);
				return;
			}

			// 3 发表
			mProgress = Utils.showProgress(PostPublishActivity.this);
			try {
				// CharSequence mTitleMsg =
				// ParseEmojiMsgUtil.convertToMsg(mPostTitle.getText(),
				// PostPublishActivity.this);//
				// 这里不要直接用mEditMessageEt.getText().toString();
				// CharSequence mContentMsg =
				// ParseEmojiMsgUtil.convertToMsg(mPostContent.getText(),
				// PostPublishActivity.this);
				ArrayList<String> files = compressImageFils(mData);
				BusinessUtils.publicPostInfo(mActivity, new PublicPostCallbackData(), mPostModuleBtn.getTag()
						.toString(), title, content, files);
			} catch (Exception e) {
				e.printStackTrace();
				mProgress.dismiss();
				showToast("内部错误，请联系APP相关人员,请重试", TOAST_LONG);
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
					resultPath, 30);
			if (flag) {
				files.add(resultPath);
			}
		}
		EvtLog.e(TAG, "compressImageFils mlist  end ");
		return files;
	}

	/**
	 * 本地缓存数据加载
	 */
	private class LoadCacheDataTask extends BaseRunnable {

		@Override
		public void runImpl() {
			EvtLog.d(TAG, "LoadCacheDataTask loading local data start");
			Message msg = new Message();
			msg.what = MsgTypes.MSG_POST_MOUDLE_SUCCESS;
			List<Map<String, String>> mPostMoudleInfos = DatabaseUtils.getListPostMoudleInfos();
			List<Map<String, String>> data = new ArrayList<Map<String, String>>();
			for (int i = 0; i < mPostMoudleInfos.size(); i++) {
				if (GroupSubjectFragment.GROUP_FORUM.equals(mPostMoudleInfos.get(i).get("type"))) {
					break;
				}
				data.add(mPostMoudleInfos.get(i));
			}
			msg.obj = data;
			sendMsg(msg);
			EvtLog.d(TAG, "LoadCacheDataTask loading local data end");

			mHandler.post(new Runnable() {

				@Override
				public void run() {
					BusinessUtils.getPostMoudleListData(mActivity, new GetMoudleCallbackData(PostPublishActivity.this));
				}
			});
		}

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
				mFaceHelper = new SelectFaceHelper(PostPublishActivity.this, moGvEmotions);
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
		Intent intent = new Intent(PostPublishActivity.this, PhotoPickActivity.class);
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
						mData.add(new PostPublishActivity.PhotoData(outputFile, item));
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

			case MsgTypes.MSG_PUBLIC_POST_SUCCESS:
				if (mProgress != null && mProgress.isShowing())
					mProgress.dismiss();
				// 清空之前保存的数据
				saveDrafts("", "", getCfgPhotoMap(null));
				// 更新本地数据
				UiHelper.showShortToast(this, (String) msg.obj);
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
				onBackPressed();
				break;
			case R.id.post_module_btn:
				showSelectMoudleDialog();
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

	/**
	 * 板块对话框
	 */
	private void showSelectMoudleDialog() {
		BusinessUtils.getPostMoudleListData(mActivity, new GetMoudleCallbackData(PostPublishActivity.this));
		selectMoudleDialog = new SelectMoudleDialog(this, mPostMoudleInfos,
				new SelectMoudleDialog.OnItemClickListener() {
					@Override
					public void onClick(int position) {
						mPostModuleBtn.setText(mPostMoudleInfos.get(position).get("title"));
						mPostModuleBtn.setTag(mPostMoudleInfos.get(position).get("id"));
						Utils.setCfg(mActivity, Constants.SF_GROUP_ID, mPostMoudleInfos.get(position).get("id"));
						Utils.setCfg(mActivity, Constants.SF_GROUP_NAME, mPostMoudleInfos.get(position).get("title"));
					}
				});
		selectMoudleDialog.builder().setCancelable(false).setCanceledOnTouchOutside(true);
		selectMoudleDialog.show();
	}

	private static class GetMoudleCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public GetMoudleCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetMoudleCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_POST_MOUDLE_SUCCESS;

					List<Map<String, String>> lists = JSONParser.parseMulti((JSONArray) result);
					List<Map<String, String>> data = new ArrayList<Map<String, String>>();
					for (int i = 0; i < lists.size(); i++) {
						if (GroupSubjectFragment.GROUP_FORUM.equals(lists.get(i).get("type"))) {
							break;
						}
						data.add(lists.get(i));
					}
					msg.obj = data;
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
					DatabaseUtils.saveListPostMoudleInfos(lists);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				msg.what = MsgTypes.MSG_POST_MOUDLE_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}
		}
	}

	private class PublicPostCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "PublicPostCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_PUBLIC_POST_SUCCESS;
					msg.obj = errorMsg;
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
