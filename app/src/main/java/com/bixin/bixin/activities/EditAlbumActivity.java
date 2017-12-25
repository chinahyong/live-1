package com.bixin.bixin.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.View;

import com.bixin.bixin.App;
import com.yanzhenjie.album.Album;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.R;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.adapters.EditAlbumAdapter;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.JacksonUtil;
import com.bixin.bixin.common.OperationHelper;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.VibratorUtil;
import com.bixin.bixin.library.util.BitmapUtility;
import com.bixin.bixin.library.util.BitmapUtils;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.FileUtil;
import com.bixin.bixin.listeners.ItemTouchCallbackListener;
import com.bixin.bixin.listeners.OnItemTouchListener;
import com.bixin.bixin.model.AlbumBean;
import com.bixin.bixin.util.UiHelper;

/**
 * Created by Live on 2017/4/24.
 * Description:相册页面，点击添加图片，图片预览删除，图片排序
 */

public class EditAlbumActivity extends BaseFragmentActivity implements ItemTouchCallbackListener.ItemDragListener, View.OnClickListener {
	private static final int ALBUM_COUNT = 33;
	public static final int ACTIVITY_REQUEST_SELECT_PHOTO = 101;
	public static final int ACTIVITY_REQUEST_IMAGE_BROWSER = 102;
	public static final int UPLOAD_SUCCED = 200;
	public static final int UPLOAD_FAILED = -200;
	public static final String File_Dir = "bx_upload";
	private RecyclerView mPhotoListView;
	private ItemTouchHelper mItemTouchHelper;
	private ItemTouchCallbackListener mItemTouchListener;
	private EditAlbumAdapter mAdapter;
	private List<AlbumBean> mAlbumBeanList;
	private AlertDialog mProgress;
	private int mUploadCount = 0;            //uploadCount
	private int mUploadTotal = 0;
	private List<AlbumBean> mUploadBeans = new ArrayList<>();
	private List<AlbumBean> mDelBeans = new ArrayList<>();

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_edit_photo;
	}

	@Override
	protected void initMembers() {
		super.initMembers();
		mAdapter = new EditAlbumAdapter(mActivity);
		mItemTouchListener = new ItemTouchCallbackListener(mAdapter);
		mItemTouchListener.setItemDragListener(this);
		mItemTouchHelper = new ItemTouchHelper(mItemTouchListener);
	}

	@Override
	public void initWidgets() {
		mPhotoListView = (RecyclerView) findViewById(R.id.edit_photo_list);
		mPhotoListView.setHasFixedSize(true);
		GridLayoutManager layoutManager = new GridLayoutManager(mActivity, 4);
		// 设置Item间距
		mPhotoListView.addItemDecoration(new RecyclerView.ItemDecoration() {
			@Override
			public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
				super.getItemOffsets(outRect, view, parent, state);
				outRect.set(0, 0, 0, Utils.dip2px(mActivity, 5));
			}
		});
		mPhotoListView.setLayoutManager(layoutManager);
		mItemTouchHelper.attachToRecyclerView(mPhotoListView);
		mPhotoListView.setAdapter(mAdapter);
		initTitle();
	}

	@Override
	protected void setEventsListeners() {
		mPhotoListView.addOnItemTouchListener(new OnItemTouchListener(mPhotoListView) {
			@Override
			public void onLongClick(RecyclerView.ViewHolder holder) {
				EvtLog.e(TAG, "onLongClick:" + holder.getLayoutPosition());
				if (holder.getLayoutPosition() != 0) {
					mItemTouchHelper.startDrag(holder);
					VibratorUtil.Vibrate(mActivity, 70);   //震动70ms
				}
			}

			@Override
			public void onItemClick(RecyclerView.ViewHolder holder) {
				EvtLog.e(TAG, "onItemClick:" + holder.getAdapterPosition());
				super.onItemClick(holder);
				// 添加图片
				if (holder.getAdapterPosition() == 0) {
					OperationHelper.onEvent(mActivity, "clickUploadingButtonInPhotoAlbumPage", null);
					if (mAdapter.getItemCount() == ALBUM_COUNT) {
						UiHelper.showToast(mActivity, R.string.edit_album_count_max);
						return;
					}
					if (mUploadBeans != null) {
						fromAlbum(ALBUM_COUNT - mAdapter.getItemCount() + mUploadBeans.size());
					} else {
						fromAlbum(ALBUM_COUNT - mAdapter.getItemCount());
					}
				} else {
					OperationHelper.onEvent(mActivity, "clickPhotoAlbumbarInPersonalMessagePage", null);
					// 进入图片预览
					Intent intent = new Intent(mActivity, ImageBrowserActivity.class);
					ArrayList<String> arrayUri = new ArrayList<>();
					for (AlbumBean item : mAdapter.getData()) {
						String url = null;
						if (!TextUtils.isEmpty(item.getUrl())) {
							url = item.getUrl();
						} else if (!TextUtils.isEmpty(item.getPath())) {
							url = item.getPath();
						}
						if (!TextUtils.isEmpty(url)) {
							if (url.indexOf("://") == -1) {
								url = "file://" + url;
							}
							arrayUri.add(url);
						}
					}
					intent.putExtra(ImageBrowserActivity.IMAGE_URL, arrayUri);
					intent.putExtra(ImageBrowserActivity.INIT_SHOW_INDEX, holder.getAdapterPosition() - 1);
					intent.putExtra(ImageBrowserActivity.IS_NEED_EIDT, true);
					startActivityForResult(intent, ACTIVITY_REQUEST_IMAGE_BROWSER);
				}
			}
		});
		mTopRightTextLayout.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			mAlbumBeanList = bundle.getParcelableArrayList("gallery");
			mAdapter.addDatas(mAlbumBeanList);
		}
	}

	@Override
	protected void initTitleData() {
		super.initTitleData();
		mTopTitleTv.setText(R.string.system_gallery);
		mTopRightText.setText(R.string.edit_user_save);
		mTopRightTextLayout.setVisibility(View.VISIBLE);
		// 默认右侧保存不可点击，当用户操作了 item时变为可点击
		mTopRightTextLayout.setEnabled(false);
		mTopRightText.setEnabled(false);
		mTopBackLayout.setOnClickListener(this);
	}

	/**
	 * Select image from fromAlbum.
	 */
	private void fromAlbum(int count) {
		ArrayList<String> selectedList = new ArrayList<>();
		if (!mUploadBeans.isEmpty()) {
			for (AlbumBean bean :
					mUploadBeans) {
				selectedList.add(bean.getPath());
			}
		}
		Album.album(this)
				.checkedList(selectedList)
				.requestCode(ACTIVITY_REQUEST_SELECT_PHOTO)
				.toolBarColor(ContextCompat.getColor(this, R.color.white)) // Toolbar color.
				.statusBarColor(ContextCompat.getColor(this, R.color.a_bg_color_da500e)) // StatusBar color.
				.navigationBarColor(ActivityCompat.getColor(this, R.color.a_bg_backgroup_color)) // NavigationBar color.
				.selectCount(count) // select count.
				.columnCount(4) // span count.
				.camera(true) // has fromCamera function.
				.title(getString(R.string.system_gallery))
				.start();
	}

	// 拖动结束回调执行
	@Override
	public void onDragFinish() {
		// 默认右侧保存不可点击，用户进行了拖动操作，右上角按钮高亮
		mTopRightTextLayout.setEnabled(true);
		mTopRightText.setEnabled(true);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.top_left:
				onBackPressed();
				break;
			// 点击右上角保存
			case R.id.top_right_text_bg:
				mTopRightTextLayout.setEnabled(false);
				OperationHelper.onEvent(mActivity, "clickSaveButtonInPhotoAlbumPage", null);
				/**
				 * 1. 提交删除
				 * 2. 删除成功，进行添加（添加失败状态为：违规）
				 * 3. 添加完成进行排序
				 */
				showDialog();
				// 获取删除列表
				List<Integer> delIds = new ArrayList<>();
				for (AlbumBean delBean :
						mDelBeans) {
					if (delBean.getId() >= 0) {
						delIds.add(delBean.getId());
					}
				}
				// 删除ID列表为空，直接进行 添加操作
				if (delIds.isEmpty()) {
					// 如果添加相片不为空，执行添加
					if (!mUploadBeans.isEmpty()) {
						uploadAlbums();
					} else {
						// 否则执行排序
						sortAlbums();
					}
					return;
				} else {
					// 删除请求
					delAlbums(delIds);
				}
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case ACTIVITY_REQUEST_SELECT_PHOTO:
				if (resultCode == RESULT_OK) {
					ArrayList<String> albums = Album.parseResult(data);
					if (albums != null && !albums.isEmpty()) {
						if (!mUploadBeans.isEmpty()) {
							mAdapter.getData().removeAll(mUploadBeans);
						}
						mUploadBeans.clear();
						for (int i = 0; i < albums.size(); i++) {
							String path = albums.get(i);
							EvtLog.e(TAG, "Album zip before path:" + path);
							AlbumBean albumBean = new AlbumBean(-1, null, path, 0);
							mUploadBeans.add(albumBean);
						}
						// 适配到本地显示
						mAdapter.addDatas(mUploadBeans);
						// 默认右侧保存不可点击，用户进行了添加操作，右上角按钮高亮
						mTopRightTextLayout.setEnabled(true);
						mTopRightText.setEnabled(true);
					}
				}
				break;
			case ACTIVITY_REQUEST_IMAGE_BROWSER:
				// 浏览图片时进行了删除操作
				if (resultCode == RESULT_OK) {
					ArrayList<String> delUrls = data.getStringArrayListExtra("mDelUrls");
					if (delUrls != null && !delUrls.isEmpty()) {
						for (String path :
								delUrls) {
							for (AlbumBean bean : mAdapter.getData()) {
								// 删除本地图片
								if (path.replace(Constants.FILE_PXI, "").equals(bean.getPath())) {
									EvtLog.e(TAG, "Album Delete:" + path);
									mDelBeans.add(bean);
									// 删除的该图片为之前添加但未提交的（服务器不存在该图片）
									mUploadBeans.remove(bean);
								}
								// 删除网络图片
								if (path.replace(Constants.FILE_PXI, "").equals(bean.getUrl())) {
									mDelBeans.add(bean);
								}
							}
						}
						mAdapter.getData().removeAll(mDelBeans);
						mAdapter.notifyDataSetChanged();
						// 默认右侧保存不可点击，用户进行了删除操作，右上角按钮高亮
						mTopRightTextLayout.setEnabled(true);
						mTopRightText.setEnabled(true);
					}
				}
				break;
		}
	}

	/**
	 * 添加相片
	 * 上传时进行压缩，如果选完图进行压缩太耗时会出现短暂黑屏（优化不方便）
	 */
	private void uploadAlbums() {
		// 需要添加的图片总数
		mUploadTotal = mUploadBeans.size();
		for (int i = 0; i < mUploadBeans.size(); i++) {
			final AlbumBean album = mUploadBeans.get(i);
			// 压缩后上传
			album.setUrl(compressImageFils(album.getPath(), i));
			if (!TextUtils.isEmpty(album.getUrl())) {
				if (!TextUtils.isEmpty(album.getUrl())) {
					BusinessUtils.uploadAlbum(mActivity, album.getUrl(), new UploadAlbumCallbackHandle(EditAlbumActivity.this, album));
				}
			}
		}
	}

	/**
	 * 相册图片排序
	 *
	 * @return
	 */
	private void sortAlbums() {
		List<Integer> sortIds = mAdapter.getIds();
		if (sortIds != null && !sortIds.isEmpty()) {
			BusinessUtils.sortAlbumList(mActivity, sortIds, new CallbackDataHandle() {
				@Override
				public void onCallback(boolean success, String errorCode, final String errorMsg, Object result) {
					if (success) {
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								UiHelper.showToast(mActivity, R.string.edit_user_save_success);
								mTopRightTextLayout.setEnabled(true);
								dissDialog();
								Intent intent = new Intent();
								intent.putParcelableArrayListExtra("albums", mAdapter.getData());
								setResult(RESULT_OK, intent);
								finish();
							}
						});
					} else {
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(errorMsg)) {
									UiHelper.showToast(mActivity, errorMsg);
								}
								dissDialog();
								mTopRightTextLayout.setEnabled(true);
							}
						});
					}
				}
			});
		} else {
			dissDialog();
			Intent intent = new Intent();
			intent.putParcelableArrayListExtra("albums", mAdapter.getData());
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	/**
	 * 删除图片
	 */
	private void delAlbums(List<Integer> delIds) {
		BusinessUtils.delAlbum(mActivity, delIds, new CallbackDataHandle() {
			@Override
			public void onCallback(boolean success, String errorCode, final String errorMsg, Object result) {
				if (success) {
					// 删除成功，清空删除列表
					mDelBeans.clear();
					// 进行相片添加
					if (!mUploadBeans.isEmpty()) {
						uploadAlbums();
					} else {
						// 没有需要添加的相片，直接排序
						sortAlbums();
					}
				} else {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							if (!TextUtils.isEmpty(errorMsg)) {
								UiHelper.showToast(mActivity, errorMsg);
							}
							dissDialog();
							mTopRightTextLayout.setEnabled(true);
						}
					});
				}
			}
		});
	}

	private String compressImageFils(String album, int num) {
		if (TextUtils.isEmpty(album))
			return null;
		if (album.indexOf("://") == -1) {
			album = "file://" + album;
		}
		File file = new File(album);
		String resultPath = FileUtil.getDiskCachePath(mActivity, File_Dir) + File.separator + num + file.getName();
		int maxSize = App.metrics.heightPixels > App.metrics.widthPixels ? App.metrics.heightPixels : App.metrics.widthPixels;
		boolean flag = BitmapUtils.writeImage(
				BitmapUtility.LoadImageFromUrl(mActivity, Uri.parse(album), maxSize),
				resultPath, 30);
		if (flag) {
			return resultPath;
		}
		return null;
	}

	@Override
	public void onBackPressed() {
		EvtLog.i(TAG, "onBackPressed");
		super.onBackPressed();
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case UPLOAD_SUCCED:
				mUploadCount++;
				List<AlbumBean> beans = (List<AlbumBean>) msg.obj;
				// 旧的图片对象
				AlbumBean localBean = beans.get(0);
				//添加成功，服务器下发的新对象
				AlbumBean bean = beans.get(1);
				// 查找旧的位置
				int position = mAdapter.getData().indexOf(localBean);
				// 设置新数据到旧的位置（排序不变）
				mAdapter.getData().set(position, bean);
				// 所有数据添加完成
				if (mUploadCount == mUploadTotal) {
					// adapter更新，计数归零
					mAdapter.notifyDataSetChanged();
					mUploadCount = 0;
					mUploadTotal = 0;
					// 查找上传成功的图片
					List<AlbumBean> succBeans = new ArrayList<>();
					for (AlbumBean succBean :
							mUploadBeans) {
						if (succBean.getStatus() != -1) {
							succBeans.add(succBean);
						}
					}
					// 移除上传成功的保留上传失败的，以便用户重新上传
					mUploadBeans.removeAll(succBeans);
					// 排序
					sortAlbums();
				}
				break;
			case UPLOAD_FAILED:
				mUploadCount++;
				AlbumBean localAlbumBean = (AlbumBean) msg.obj;            // 添加之前本地的书
				// 旧的位置
				int oldPosition = mAdapter.getData().indexOf(localAlbumBean);
				// 找到旧的图片对象修改状态
				if (oldPosition >= 0) {
					mAdapter.getData().get(oldPosition).setStatus(-1);
				}
				// 所有数据添加完成
				if (mUploadCount == mUploadTotal) {
					mAdapter.notifyDataSetChanged();
					mUploadCount = 0;
					mUploadTotal = 0;
					// 查找上传成功的图片
					List<AlbumBean> succBeans = new ArrayList<>();
					for (AlbumBean succBean :
							mUploadBeans) {
						if (succBean.getStatus() != -1) {
							succBeans.add(succBean);
						}
					}
					// 移除上传成功的保留上传失败的，以便用户重新上传
					mUploadBeans.removeAll(succBeans);
					// 排序
					sortAlbums();
				}
				break;
		}
	}

	private void showDialog() {
		if (mProgress != null && mProgress.isShowing()) {
			return;
		}
		mProgress = Utils.showProgress(this);
	}

	private void dissDialog() {
		if (mProgress != null && mProgress.isShowing()) {
			mProgress.dismiss();
		}
	}

	/**
	 * 相册添加上传
	 */
	private static class UploadAlbumCallbackHandle implements CallbackDataHandle {
		private WeakReference<EditAlbumActivity> mActivity;
		private AlbumBean mAlbum;

		public UploadAlbumCallbackHandle(EditAlbumActivity activity, AlbumBean album) {
			mActivity = new WeakReference<>(activity);
			mAlbum = album;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = new Message();
			List<AlbumBean> albumBeanList = new ArrayList<>();
			albumBeanList.add(mAlbum);
			if (success) {
				try {
					AlbumBean albumBean = JacksonUtil.readValue(result.toString(), AlbumBean.class);
					msg.what = UPLOAD_SUCCED;
					albumBeanList.add(albumBean);
					msg.obj = albumBeanList;
				} catch (Exception e) {
					msg.what = UPLOAD_FAILED;
					msg.obj = mAlbum;
				}
			} else {
				msg.what = UPLOAD_FAILED;
				msg.obj = mAlbum;
			}
			BaseFragmentActivity fragment = mActivity.get();
			if (fragment != null)
				fragment.sendMsg(msg);
		}
	}
}
