package com.bixin.bixin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.lonzh.lib.network.JSONParser;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.framework.net.impl.CallbackDataHandle;
import tv.live.bx.R;
import com.bixin.bixin.base.act.BaseFragmentActivity;
import com.bixin.bixin.adapters.FocusListAdapter;
import com.bixin.bixin.common.AsyncTaskThreadPool;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.tasks.BaseRunnable;
import com.bixin.bixin.ui.ListFooterLoadView;
import com.bixin.bixin.ui.LoadingProgress;
import com.bixin.bixin.ui.LoadingProgress.onProgressClickListener;
import com.bixin.bixin.ui.PullRefreshListView;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;

/**
 * MeFocusActivity
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class MeFocusActivity extends BaseFragmentActivity implements OnClickListener, OnItemClickListener {
	/** 目前暂时都刷新 */
	private static boolean isRefresh = true;
	/** 获取最新页面时，page为0，page以此累加 */
	private static int page = 0;

	private PullRefreshListView mListView;

	/**
	 * 加载更多FootView
	 */
	private ListFooterLoadView mListFooterLoadView;

	/**
	 * 加载loading
	 */
	private LoadingProgress mLoadProgress;

	private FocusListAdapter mReplyAdapter;


	@Override
	protected int getLayoutRes() {
		// TODO Auto-generated method stub
		return R.layout.activity_comment_layout;
	}

	@Override
	protected void initMembers() {
		// 初始化UI
		initUI(mInflater);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		// 加载本地缓存数据
		AsyncTaskThreadPool.getThreadExecutorService().submit(new LoadCacheDataTask());
	}

	@Override
	public void initWidgets() {

	}

	@Override
	protected void setEventsListeners() {

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ry_bar_left:
			onBackPressed();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		EvtLog.d(
				TAG,
				"onItemClick:position " + position + " mListView.getHeaderViewsCount():"
						+ mListView.getHeaderViewsCount());
		if (position - mListView.getHeaderViewsCount() < 0) {
			return;
		}
		if (position - mListView.getFooterViewsCount() < 0) {
			return;
		}
		@SuppressWarnings("unchecked")
		Map<String, String> lmItem = (Map<String, String>) mReplyAdapter.getItem(position
				- mListView.getHeaderViewsCount());
		Map<String, String> personInfo = new HashMap<String, String>();
		personInfo.put("id", lmItem.get("uid"));
		ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case MsgTypes.MSG_ME_COMMENT_LIST_FAILED:
			mListView.notifyTaskFinished();
			if (mReplyAdapter.isEmpty()) {
				String text = mActivity.getString(R.string.a_loading_failed);
				mLoadProgress.Failed(text, 0);
			} else {
				UiHelper.showToast(mActivity, R.string.a_tips_net_error);
				mLoadProgress.Hide();
				mListFooterLoadView.onLoadingFailed();
			}

			break;

		case MsgTypes.MSG_ME_COMMENT_LIST_SUCCESS:
			Object[] objects = (Object[]) msg.obj;
			boolean isRefreh = (Boolean) objects[0];
			List<Map<String, Object>> mListData = (List<Map<String, Object>>) objects[1];
			if (isRefreh) { // 初始化或者下拉刷新模式
				mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
				mReplyAdapter.clearData();
				mReplyAdapter.addData(mListData);
			} else { // 加载更多数据模式
				if (mListData.isEmpty()) {
					mListFooterLoadView.onNoMoreData();
				} else {
					// 隐藏ListView的FootView
					mListFooterLoadView.hide();
					mReplyAdapter.addData(mListData);
				}
			}
			// 设置没有数据的EmptyView
			String text = mActivity.getString(R.string.me_message_no_data);
			mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
			break;
		}

	}

	/**
	 * 初始化UI控件
	 */
	private void initUI(LayoutInflater inflater) {
		initTitle();
		initListView(inflater);
	}

	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.message_me_focus);
		mTopBackLayout.setOnClickListener(this);
	}

	/**
	 * 初始化下拉刷新ListView
	 * 
	 * @param inflater
	 */
	private void initListView(LayoutInflater inflater) {
		mListView = (PullRefreshListView) findViewById(R.id.author_listview);
		mListView.setOnItemClickListener(this);
		mListView.setTopHeadHeight(0);

		mListView.setBottomFooterHeight((int) (15 * getResources().getDisplayMetrics().density / 1.5f));
		mReplyAdapter = new FocusListAdapter(mActivity);
		mListView.setAdapter(mReplyAdapter);
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
				mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
				// 重新加载数据
				reRequestData(true);

			}

			@Override
			public void onClick(View v) {
				mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
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
		// 清空界面数据
		if (clearAdapter) {
			mReplyAdapter.clearData();
			mReplyAdapter.notifyDataSetChanged();
		}
		isRefresh = true;
		requestPlazaData(page);
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page) {
		BusinessUtils.getSystemMsgList(mActivity, new MeMessageCallbackData(this), "attention", page);
	}

	/**
	 * 本地缓存数据加载
	 */
	private class LoadCacheDataTask extends BaseRunnable {

		@Override
		public void runImpl() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					reRequestData(false);
				}
			});
		}

	}

	/**
	 * 评论数据处理回调 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * 
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class MeMessageCallbackData implements CallbackDataHandle {

		private WeakReference<BaseFragmentActivity> mFragment;

		public MeMessageCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "MeMessageCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_ME_COMMENT_LIST_SUCCESS;
					Object[] objects = new Object[] { isRefresh,

					JSONParser.parseSingleInMulti((JSONArray) result, new String[] { "" }) };
					msg.obj = objects;
					// 下次请求的页面数
					page++;
					MeFocusActivity fragment = (MeFocusActivity) mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
					if (isRefresh) {
						// DatabaseUtils.saveCollectListSubjectInfos(
						// (List<Map<String, Object>>) objects[1], uId);
					}
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_ME_COMMENT_LIST_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorCode;
				MeFocusActivity fragment = (MeFocusActivity) mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}
		}

	}

}
