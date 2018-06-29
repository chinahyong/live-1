package com.bixin.bixin.fragments;

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
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.framework.net.impl.CallbackDataHandle;
import tv.live.bx.R;
import com.bixin.bixin.adapters.PlayingRankListAdapter;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.ui.ListFooterLoadView;
import com.bixin.bixin.ui.LoadingProgress;
import com.bixin.bixin.ui.LoadingProgress.onProgressClickListener;
import com.bixin.bixin.ui.PullRefreshListView;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;

/**
 * Title:PlayingRankActivity.java Description:直播间泡泡排行榜
 *
 * @author Live
 * @version 2.2 2016.4.9
 */
public class LiveRankFragment extends BaseFragment implements OnItemClickListener {

	public static String ROOM_ID = "rid";
	public static String RANK_TYPE = "rankType";
	private PullRefreshListView mListView;
	/**
	 * 加载更多FootView
	 */
	private ListFooterLoadView mListFooterLoadView;
	/**
	 * 加载loading
	 */
	private LoadingProgress mLoadProgress;
	private PlayingRankListAdapter mRankListAdapter;

	/**
	 * 目前暂时都刷新
	 */
	private static boolean isRefresh = true;
	/**
	 * 获取最新页面时，page为0，page以此累加
	 */
	private int page = 0;
	private String mRid;
	//票票榜单类型:周榜，总榜等
	private String mRankType;


	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_live_rank_layout;
	}

	@Override
	protected void initMembers() {

	}

	@Override
	public void initWidgets() {
		mListView = (PullRefreshListView) mRootView.findViewById(R.id.playing_rank_listview);
		initListView(mInflater);
	}

	@Override
	protected void setEventsListeners() {

	}

	@Override
	protected void initData(Bundle arguments) {
		if (arguments != null) {
			mRid = arguments.getString(ROOM_ID);
			mRankType = arguments.getString(RANK_TYPE);
			reRequestData(false);
		}
	}

	/**
	 * 初始化下拉刷新ListView
	 *
	 * @param inflater
	 */
	private void initListView(LayoutInflater inflater) {
		mListView = (PullRefreshListView) mRootView.findViewById(R.id.playing_rank_listview);
		mListView.setOnItemClickListener(this);
		mListView.setTopHeadHeight(0);

		mListView.setBottomFooterHeight((int) (15 * getResources().getDisplayMetrics().density / 1.5f));
		mRankListAdapter = new PlayingRankListAdapter(mActivity);
		mListView.setAdapter(mRankListAdapter);
		// 下拉刷新数据
		mListView.setTask(new Runnable() {
			@Override
			public void run() {
				// 请求排行榜数据
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
		mLoadProgress = (LoadingProgress) mRootView.findViewById(R.id.progress);
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
		// mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		// 清空界面数据
		if (clearAdapter) {
			mRankListAdapter.clearData();
			mRankListAdapter.notifyDataSetChanged();
		}
		isRefresh = true;
		requestPlazaData(page);
	}

	/**
	 * 向服务端请求泡泡排行榜数据
	 */
	private void requestPlazaData(int page) {
		BusinessUtils.getLivePRank(mActivity, new RankCallbackDataHandle(), mRid, mRankType, page);
	}

	/***********************************
	 * 事件处理器
	 ************************************/
	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MsgTypes.MSG_GET_LIVE_P_RANK_SUCCESS:
				Object[] objects = (Object[]) msg.obj;
				boolean isRefresh = (Boolean) objects[0];
				List<Map<String, Object>> rankDatas = (List<Map<String, Object>>) objects[1];
				if (isRefresh) { // 初始化或者下拉刷新模式
					mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
					mRankListAdapter.clearData();
					mRankListAdapter.addData(rankDatas);
				} else { // 加载更多数据模式
					if (rankDatas.isEmpty()) {
						mListFooterLoadView.onNoMoreData();
					} else {
						// 隐藏ListView的FootView
						mListFooterLoadView.hide();
						mRankListAdapter.addData(rankDatas);
					}
				}
				// 设置没有数据的EmptyView
				String text = mActivity.getString(R.string.rank_no_data);
				mLoadProgress.Succeed(text, R.drawable.a_common_no_data);

				break;
			case MsgTypes.MSG_GET_LIVE_P_RANK_FAILED:
				mListView.notifyTaskFinished();
				if (mRankListAdapter.isEmpty()) {
					mLoadProgress.Failed(mActivity.getString(R.string.rank_net_err), 0);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
					mLoadProgress.Hide();
					mListFooterLoadView.onLoadingFailed();
				}
				break;
			default:
				break;
		}
	}

	private class OnBack implements OnClickListener {
		@Override
		public void onClick(View v) {
			onBackPressed();
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
		/* umeng后台出现 outOfBounds，先捕获处理，暂未定位到原因 */
		try {
			@SuppressWarnings("unchecked")
			Map<String, String> lmItem = (Map<String, String>) mRankListAdapter.getItem(position
					- mListView.getHeaderViewsCount());
			Map<String, String> personInfo = new HashMap<String, String>();
			personInfo.put("id", lmItem.get("uid"));
			ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class RankCallbackDataHandle implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_GET_LIVE_P_RANK_SUCCESS;
					Object[] objects = new Object[]{isRefresh,
							JSONParser.parseSingleInMulti((JSONArray) result, new String[]{""})};
					msg.obj = objects;
					// 下次请求的页面数
					page++;
					sendMsg(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}
	}
}
