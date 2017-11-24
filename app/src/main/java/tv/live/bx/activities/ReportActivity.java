package tv.live.bx.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.PermissionUtil;
import tv.live.bx.common.PhotoOperate;
import tv.live.bx.common.PhotoSelectImpl;
import tv.live.bx.common.Utils;
import tv.live.bx.common.photopick.ImageInfo;
import tv.live.bx.config.AppConfig;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.BitmapUtility;
import tv.live.bx.library.util.BitmapUtils;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.FileUtil;
import tv.live.bx.ui.ActionSheetDialog;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;


public class ReportActivity extends BaseFragmentActivity implements OnClickListener {
	public static final int PHOTO_MAX_COUNT = 6;
	public static final int RESULT_REQUEST_IMAGE = 1007;
	private Button[] maReportBtns;

	private int miSelReport;

	public static String REPORT_TYPE = "report_type";
	public static String REPORT_ID = "report_id";

	private String mReportType;
	private String mReportId;
	private Button mBtnCommit;
	private GridView mGridView;
	private ArrayList<PhotoData> mData = new ArrayList<>();
	private PhotoOperate photoOperate = new PhotoOperate(this);
	private File mCameraFile;        //拍照保存地址

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
				LayoutInflater mInflater = LayoutInflater.from(mActivity);
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
				holder.uri = photoData.mImageinfo.path;
//				holder.uri = data.toString();

				ImageLoaderUtil.with().loadImage(mActivity, holder.image, holder.uri, 0, R.drawable.image_not_exist);
			}

			return holder.image;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			mGridView.setVisibility(getCount() > 0 ? View.VISIBLE : View.GONE);
		}

		class ViewHolder {
			ImageView image;
			String uri = "";
		}

	};

	/**
	 * 之前忘记使用这些方法了，这个类暂时不用了
	 */
	@Override
	protected int getLayoutRes() {

		return R.layout.activity_report_layout;
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Intent intent = getIntent();
		if (intent != null) {
			mReportType = intent.getStringExtra(REPORT_TYPE);
			mReportId = intent.getStringExtra(REPORT_ID);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.report);
		mTopRightText.setText(R.string.submit);
		mTopRightTextLayout.setVisibility(View.GONE);
		mTopRightTextLayout.setOnClickListener(this);
		mTopBackLayout.setOnClickListener(this);
	}

	protected void initMembers() {
		initTitle();
		miSelReport = -1;
		maReportBtns = new Button[6];
		maReportBtns[0] = (Button) findViewById(R.id.fragment_playing_other_btn_pink_content);
		maReportBtns[1] = (Button) findViewById(R.id.fragment_playing_other_btn_spam);
		maReportBtns[2] = (Button) findViewById(R.id.fragment_playing_other_btn_personal_attacks);
		maReportBtns[3] = (Button) findViewById(R.id.fragment_playing_other_btn_sensitive_information);
		maReportBtns[4] = (Button) findViewById(R.id.fragment_playing_other_btn_false_winning);
		maReportBtns[5] = (Button) findViewById(R.id.fragment_playing_other_btn_other);
	}

	public void initWidgets() {
		mBtnCommit = (Button) findViewById(R.id.live_report_btn_commit);
		mGridView = (GridView) findViewById(R.id.gridView);
	}

	protected void setEventsListeners() {
		mBtnCommit.setOnClickListener(this);
		mGridView.setAdapter(adapter);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
					Intent intent = new Intent(mActivity, ImageBrowserActivity.class);
					ArrayList<String> arrayUri = new ArrayList<>();
					for (PhotoData item : mData) {
						arrayUri.add(item.mImageinfo.path);
					}
					intent.putExtra(ImageBrowserActivity.IMAGE_URL, arrayUri);
					intent.putExtra(ImageBrowserActivity.INIT_SHOW_INDEX, position);
					intent.putExtra(ImageBrowserActivity.IS_NEED_EIDT, true);
					startActivityForResult(intent, RESULT_REQUEST_IMAGE);
				}
			}
		});
		OnReportSelect loSelReport = new OnReportSelect();
		for (Button loBtn : maReportBtns)
			loBtn.setOnClickListener(loSelReport);
	}

	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.REPORT_ILLEGAL_SUCCESS:// 获取Banner
				UiHelper.showToast(mActivity, R.string.live_report_success);
				onBackPressed();
				break;
			case MsgTypes.REPORT_ILLEGAL_FAILED:
				String errorMsg = (String) msg.obj;
				UiHelper.showToast(mActivity, errorMsg);
				break;

			default:
				break;
		}
	}

	private class OnReportSelect implements OnClickListener {
		@Override
		public void onClick(View poV) {
			int liSel = -1;
			switch (poV.getId()) {
				case R.id.fragment_playing_other_btn_pink_content:
					liSel = 0;
					break;
				case R.id.fragment_playing_other_btn_spam:
					liSel = 1;
					break;
				case R.id.fragment_playing_other_btn_personal_attacks:
					liSel = 2;
					break;
				case R.id.fragment_playing_other_btn_sensitive_information:
					liSel = 3;
					break;
				case R.id.fragment_playing_other_btn_false_winning:
					liSel = 4;
					break;
				case R.id.fragment_playing_other_btn_other:
					liSel = 5;
					break;
			}
			if (liSel != -1 && miSelReport != liSel) {
				maReportBtns[liSel].setBackgroundResource(R.drawable.btn_yello_nor);
				maReportBtns[liSel].setTextColor(mActivity.getResources().getColor(R.color.white));
				if (miSelReport != -1) {
					maReportBtns[miSelReport].setBackgroundResource(R.drawable.btn_yello_pre);
					maReportBtns[miSelReport].setTextColor(mActivity.getResources().getColor(R.color.text_gray));
				}
			}
			miSelReport = liSel;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.top_left:
				onBackPressed();
				break;
//			case R.id.top_right_text_bg:
			case R.id.live_report_btn_commit:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "submitInReportPage");
				if (!AppConfig.getInstance().isLogged) {
					Utils.requestLoginOrRegister(mActivity, getString(R.string.live_report_not_login), 0);
					return;
				}
				if (miSelReport == -1) {
					UiHelper.showToast(mActivity, R.string.live_report_not_content);
					return;
				}
				// 该路径该为必选项
				String imgPath = null;
				if (mData.size() <= 0) {
					UiHelper.showToast(mActivity, R.string.live_report_not_image);
					return;
				}
				imgPath = compressImageFils(mData.get(0));
				BusinessUtils.reportIllegal(mActivity, new ReportCallbackData(this), mReportType, mReportId,
						miSelReport + 1, imgPath);
				break;
			default:
				break;
		}

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
			mData.add(new ReportActivity.PhotoData(outputFile, new ImageInfo(imagePath)));
		} catch (Exception e) {
			showTips("缩放图片失败");
			EvtLog.e(TAG, e.toString());
		}
		adapter.notifyDataSetChanged();
	}

	private String compressImageFils(PhotoData photoData) {
		if (photoData == null)
			return null;
		String resultPath = FileUtil.getDiskCachePath(mActivity, GroupPostPublishActivity.File_Dir) + File.separator + "pic_0" + ".jpg";
		boolean flag = BitmapUtils.writeImage(
				BitmapUtility.LoadImageFromUrl(mActivity, photoData.uri, FeizaoApp.metrics.heightPixels),
				resultPath, 30);
		if (flag) {
			return resultPath;
		}
		return null;
	}

	/**
	 * 举报信息 数据处理回调 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class ReportCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public ReportCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "ReportCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.REPORT_ILLEGAL_SUCCESS;
					BaseFragmentActivity authorFragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (authorFragment != null)
						authorFragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.REPORT_ILLEGAL_FAILED;
				msg.obj = Constants.NETWORK_FAIL;
				if (!TextUtils.isEmpty(errorMsg)) {
					msg.obj = errorMsg;
				}
				BaseFragmentActivity authorFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (authorFragment != null)
					authorFragment.sendMsg(msg);
			}
		}
	}

	public static class PhotoData {
		ImageInfo mImageinfo;
		Uri uri = Uri.parse("");
		String serviceUri = "";

		public PhotoData(File file, ImageInfo info) {
			uri = Uri.fromFile(file);
			mImageinfo = info;
		}

		public PhotoData(PostPublishActivity.PhotoDataSerializable data) {
			uri = Uri.parse(data.uriString);
			serviceUri = data.serviceUri;
			mImageinfo = data.mImageInfo;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case RESULT_REQUEST_IMAGE:
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
				break;
			case PhotoSelectImpl.REQUEST_CAMERA:
				if (mCameraFile != null && resultCode == RESULT_OK) {
					updatePickData(mCameraFile.getPath());
					mCameraFile = null;
				}
				break;
			case PhotoSelectImpl.REQUEST_ALBUM:
				// 相册选择
				if (data != null) {
					Uri uri = data.getData();
					String path = BitmapUtility.getFilePathFromUri(mActivity, uri);
					updatePickData(path);
				}
				break;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case PermissionUtil.REQUEST_PERMISSION_CAMERA: {
				// 拒绝权限
				if (!PermissionUtil.permissionIsGranted(mActivity, Manifest.permission.CAMERA)) {
					UiHelper.showToast(mActivity, R.string.live_connect_permission_tip);
				}
				break;
			}
			default: {
				break;
			}
		}
	}

}
