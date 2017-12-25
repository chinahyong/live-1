package com.bixin.bixin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lonzh.lib.network.JSONParser;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.R;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.adapters.HotTopicAdapter;
import com.bixin.bixin.common.AsyncTaskThreadPool;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.tasks.BaseRunnable;
import com.bixin.bixin.ui.ListFooterLoadView;
import com.bixin.bixin.ui.LoadingProgress;
import com.bixin.bixin.ui.PullRefreshListView;
import com.bixin.bixin.util.UiHelper;

/**
 * @author Live
 * @version 2016/6/7 2.5.0
 * @title HotTopicActivity.java Description:2.5.0 准备开播添加'热门话题'选择
 */
public class HotTopicActivity extends BaseFragmentActivity implements AdapterView.OnItemClickListener {
	private static final int GET_TOPICS_SUCCESS = 0x001;
	private static final int GET_TOPICS_FAILED = -0x001;
	private static final int LIMIT = 20;
	/**
	 * 目前暂时都刷新
	 */
	private static boolean isRefresh = true;
	/**
	 * 获取最新页面时，page为0，page以此累加
	 */
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

	private HotTopicAdapter mListAdapter;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_topic_hot;
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {

	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		// 加载本地缓存数据
		AsyncTaskThreadPool.getThreadExecutorService().submit(new LoadCacheDataTask());
	}

	@Override
	protected void initTitleData() {
		super.initTitleData();
		mTopTitleTv.setText("热门话题");
		mTopBackLayout.setOnClickListener(new OnBack());
	}

	@Override
	protected void initMembers() {
		super.initMembers();
		initTitle();
		initListView(mInflater);
	}

	/**
	 * 初始化下拉刷新ListView
	 *
	 * @param inflater
	 */
	private void initListView(LayoutInflater inflater) {
		mListView = (PullRefreshListView) findViewById(R.id.topic_pull_refresh_list);
		mListView.setOnItemClickListener(this);
		mListView.setTopHeadHeight(0);
		mListView.setBottomFooterHeight((int) (15 * getResources().getDisplayMetrics().density / 1.5f));
		mListAdapter = new HotTopicAdapter(this);
		mListView.setAdapter(mListAdapter);
		// 下拉刷新数据
		mListView.setTask(new Runnable() {
			@Override
			public void run() {
				// 请求主播数据
				reRequestData(false);
			}
		});
		View view = inflater.inflate(R.layout.a_common_list_header_hint, null);
		view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources()
				.getDimensionPixelSize(R.dimen.list_hintview_height)));
		mListView.setPullnReleaseHintView(view);
		// 设置正确的颜色
		mListView.setHeaderBackgroudColor(getResources().getColor(R.color.app_background));

		// 设置上滑动加载更多
		mListFooterLoadView = (ListFooterLoadView) inflater.inflate(R.layout.a_common_list_footer_loader_view, null);
		mListFooterLoadView.hide();
		mListFooterLoadView.setOnClickListener(new View.OnClickListener() {

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
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
		mLoadProgress.setProgressClickListener(new LoadingProgress.onProgressClickListener() {

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
		// 清空界面数据
		page = 0;
		// 清空界面数据
		if (clearAdapter) {
			mListAdapter.clearData();
			mListAdapter.notifyDataSetChanged();
		}
		isRefresh = true;
		requestPlazaData(page);
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page) {
		BusinessUtils.getTopics(mActivity, new GetTopicsCallbackData(this), page, LIMIT);
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

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case GET_TOPICS_FAILED:
				mListView.notifyTaskFinished();
				if (mListAdapter.isEmpty()) {
					String text = mActivity.getString(R.string.a_loading_failed);
					mLoadProgress.Failed(text, 0);
				} else {
					UiHelper.showToast(mActivity, msg.obj.toString());
					mLoadProgress.Hide();
					mListFooterLoadView.onLoadingFailed();
				}

				break;

			case GET_TOPICS_SUCCESS:
				Object[] objects = (Object[]) msg.obj;
				boolean isRefreh = (Boolean) objects[0];
				List<Map<String, String>> mListData = (List<Map<String, String>>) objects[1];
				if (isRefreh) { // 初始化或者下拉刷新模式
					mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
					mListAdapter.clearData();
					mListAdapter.addData(mListData);
				} else { // 加载更多数据模式
					if (mListData.isEmpty()) {
						mListFooterLoadView.onNoMoreData();
					} else {
						// 隐藏ListView的FootView
						mListFooterLoadView.hide();
						mListAdapter.addData(mListData);
					}
				}
				// 设置没有数据的EmptyView
				String text = mActivity.getString(R.string.live_topic_empty_msg);
				mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent();
		intent.putExtra("topic", ((TextView) view).getText());
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_FLUSH_ACTIVITY) {
			// 请求主播数据
			reRequestData(false);
		}
	}

	/*************************************
	 * 事件处理器
	 ************************************/
	/**
	 * Topbar 返回按钮响应点击
	 */
	private class OnBack implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	}

	/**
	 * 用户关注的主播数据处理回调 ClassName: PublishRoomCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class GetTopicsCallbackData implements CallbackDataHandle {
		private final WeakReference<BaseFragmentActivity> mFragment;

		public GetTopicsCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetTopicsCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = GET_TOPICS_SUCCESS;
					Object[] objects = new Object[]{isRefresh, JSONParser.parseMulti((JSONArray) result)};
					msg.obj = objects;
					// 下次请求的页面数
					page++;
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = GET_TOPICS_FAILED;
				msg.obj = errorMsg;
				BaseFragmentActivity fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}

		}
	}
}
