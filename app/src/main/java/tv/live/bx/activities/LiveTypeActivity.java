package tv.live.bx.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lonzh.lib.network.JSONParser;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.LiveTagAdapter;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.database.DatabaseUtils;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.PullRefreshListView;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;

/**
 * Title: AuthorFragment.java</br> Description: 主播Fragment</br> Copyright: *
 * Copyright (c) 2008</br
 *
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class LiveTypeActivity extends BaseFragmentActivity implements OnClickListener, OnItemClickListener {
	private static final int MSG_LOAD_SUCCESS = 0x10;
	private static final int MSG_LOAD_FAILED = 0x11;

	public static final String TYPE_ID = "id";
	public static final String TYPE_NAME = "name";

	/**
	 * 目前暂时都刷新
	 */
	private static boolean isRefresh = true;
	/**
	 * 获取最新页面时，page为0，page以此累加
	 */
	private static int page = 0;
	/**
	 * 用户数据
	 */
	private Map<String, ?> mUserInfo;

	private PullRefreshListView mListView;

	/**
	 * 加载更多FootView
	 */
	private ListFooterLoadView mListFooterLoadView;

	/**
	 * 加载loading
	 */
	private LoadingProgress mLoadProgress;

	private LiveTagAdapter mAuthorAdapter;

	// 直播按钮
	private ImageView mLiveIv;

	private String mTypeId;
	private String mTypeName;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_live_type_layout;
	}

	@Override
	protected void initMembers() {
		// 初始化UI
		initUI();
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Intent intent = getIntent();
		if (intent != null) {
			mTypeId = intent.getStringExtra(TYPE_ID);
			mTypeName = intent.getStringExtra(TYPE_NAME);
		}
		initTitle();
		reRequestData(false);
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(mTypeName);
		mTopBackLayout.setOnClickListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(TYPE_ID, mTypeId);
		outState.putString(TYPE_NAME, mTypeName);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		mTypeId = savedInstanceState.getString(TYPE_ID);
		mTypeName = savedInstanceState.getString(TYPE_NAME);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.top_left:
				onBackPressed();
				break;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position - mListView.getHeaderViewsCount() < 0)
			return;
		@SuppressWarnings("unchecked")
		Map<String, Object> lmItem = (Map<String, Object>) mAuthorAdapter.getItem(position
				- mListView.getHeaderViewsCount());
		lmItem.put("rid", lmItem.get("rid"));
		ActivityJumpUtil.toLiveMediaPlayerActivity(mActivity, lmItem);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_LOAD_FAILED:
				mListView.notifyTaskFinished();
				if (mAuthorAdapter.isEmpty()) {
					String text = mActivity.getString(R.string.a_loading_failed);
					mLoadProgress.Failed(text, 0);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
					mLoadProgress.Hide();
					mListFooterLoadView.onLoadingFailed();
				}

				break;

			case MSG_LOAD_SUCCESS:
				Object[] objects = (Object[]) msg.obj;
				boolean isRefreh = (Boolean) objects[0];
				List<Map<String, Object>> mData = (List<Map<String, Object>>) objects[1];
				if (isRefreh) { // 初始化或者下拉刷新模式
					mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
					mAuthorAdapter.clearData();
					mAuthorAdapter.addData(mData);
				} else { // 加载更多数据模式
					if (mData.isEmpty()) {
						mListFooterLoadView.onNoMoreData();
					} else {
						// 如果点击的foot加载第一页的数据，重新更新adapter数据
						if (page == 1) {
							// 隐藏ListView的FootView
							mListFooterLoadView.hide();
							mAuthorAdapter.clearData();
							mAuthorAdapter.addData(mData);
						} else {
							// 隐藏ListView的FootView
							mListFooterLoadView.hide();
							mAuthorAdapter.addData(mData);
						}
					}
				}
				// 设置没有数据的EmptyView
				String text = mActivity.getString(R.string.a_list_data_empty);
				mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
				break;

		}

	}

	/**
	 * 初始化UI控件
	 */
	private void initUI() {
		mLiveIv = (ImageView) findViewById(R.id.liveBtn);
		mLiveIv.setOnClickListener(this);
		initListView(mInflater);
	}

	/**
	 * 初始化下拉刷新ListView
	 *
	 * @param inflater
	 */
	private void initListView(LayoutInflater inflater) {
		mListView = (PullRefreshListView) findViewById(R.id.author_listview);
		mListView.setTopHeadHeight(0);
		mListView.setBottomFooterHeight((int) (15 * getResources().getDisplayMetrics().density / 1.5f));
		mListView.setOnItemClickListener(this);
		mAuthorAdapter = new LiveTagAdapter(mActivity);
		mListView.setAdapter(mAuthorAdapter);
		// 下拉刷新数据
		mListView.setTask(new Runnable() {
			@Override
			public void run() {
				// 请求主播数据
				reRequestData(false);
			}
		});
		View view = inflater.inflate(R.layout.a_common_list_header_hint, null);
		view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, getResources()
				.getDimensionPixelSize(R.dimen.list_hintview_height)));
		mListView.setPullnReleaseHintView(view);
		// 设置正确的颜色
		mListView.setHeaderBackgroudColor(getResources().getColor(R.color.app_background));

		// 设置上滑动加载更多
		mListFooterLoadView = (ListFooterLoadView) inflater.inflate(R.layout.a_common_list_footer_loader_view, null);
		mListFooterLoadView.hide();
		mListFooterLoadView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ListFooterLoadView listFooterLoadView = (ListFooterLoadView) v;
				int status = listFooterLoadView.getStatus();
				if (status == ListFooterLoadView.STATUS_FAILED || status == ListFooterLoadView.STATUS_NOMORE) {
					listFooterLoadView.onLoadingStarted();
					isRefresh = false;
					requestPlazaData(page);
				}
			}
		});
		mListView.addFooterView(mListFooterLoadView);
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (totalItemCount > mListView.getHeaderViewsCount() + mListView.getFooterViewsCount()) {
					if (mListFooterLoadView.getParent() == mListView) {
						// 至少翻过一项，才有可能执行加载更过操作
						if (mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_HIDDEN
								&& mListView.getFirstVisiblePosition() > mListView.getHeaderViewsCount()) {
							mListFooterLoadView.onLoadingStarted();
							EvtLog.d(TAG, "滚动加载更多");
							isRefresh = false;
							requestPlazaData(page);
						}
					} else {
						if (mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_FAILED
								|| mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_NOMORE) {
							mListFooterLoadView.hide();
						}
					}
				}
			}
		});

		// 设置默认图片
		mLoadProgress = (LoadingProgress) findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				// 重新加载数据
				reRequestData(true);

			}

			@Override
			public void onClick(View v) {
				mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				reRequestData(true);
			}
		});
		mListView.setEmptyView(mLoadProgress);
	}

	/**
	 * 重新请求数据
	 *
	 * @param clearAdapter 请求之前是否先清空UI
	 */
	public void reRequestData(boolean clearAdapter) {
		// 初始化loading(正在加载...)
		// 重新初始化请求页面
		page = 0;
		// mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
		// 清空界面数据
		if (clearAdapter) {
			mAuthorAdapter.clearData();
			mAuthorAdapter.notifyDataSetChanged();
		}
		isRefresh = true;
		requestPlazaData(page);
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page) {
		BusinessUtils.getAuthorListDataByTag(mActivity, page, mTypeId, 0, new AuthorCallbackData(this));
	}

	/**
	 * 主播列表数据处理回调 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class AuthorCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public AuthorCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "AuthorCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_LOAD_SUCCESS;
					Object[] objects = new Object[]{isRefresh,
							JSONParser.parseSingleInMulti((JSONArray) result, new String[]{"moderator"})};
					msg.obj = objects;
					// 下次请求的页面数
					page++;
					LiveTypeActivity authorFragment = (LiveTypeActivity) mFragment.get();
					// 如果fragment未回收，发送消息
					if (authorFragment != null)
						authorFragment.sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
					if (isRefresh) {
						DatabaseUtils.saveListAnchorInfos((List<Map<String, Object>>) objects[1]);
					}
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_LOAD_FAILED;
				msg.obj = errorCode;
				LiveTypeActivity authorFragment = (LiveTypeActivity) mFragment.get();
				// 如果fragment未回收，发送消息
				if (authorFragment != null)
					authorFragment.sendMsg(msg);
			}
		}

	}

}
