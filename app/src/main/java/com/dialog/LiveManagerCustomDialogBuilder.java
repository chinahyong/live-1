package com.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.framework.net.impl.CallbackDataHandle;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import com.bixin.bixin.App;
import tv.live.bx.R;
import com.bixin.bixin.adapters.RoomManagerAdapter;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.ui.ListFooterLoadView;
import com.bixin.bixin.ui.LoadingProgress;
import com.bixin.bixin.ui.PullRefreshListView;
import com.bixin.bixin.util.UiHelper;

/**
 * Title: CustomDialogBuilder.java</br> Description: 自定义对话框</br> Copyright:
 * Copyright (c) 2008</br>
 *
 * @version 1.0
 * @CreateDate 2014-7-8
 * 直播间主播侧，管理员列表dialog
 */
public class LiveManagerCustomDialogBuilder extends CustomDialogBuilder {
	private static final int GET_MANAGER_SUCC = 0x001;
	private static final int GET_MANAGER_FAIL = -0x001;

	private static final int REMOVE_MANAGER_SUCC = 0x002;
	private static final int REMOVE_MANAGER_FAIL = -0x002;
	/**
	 * 目前暂时都刷新
	 */
	private static boolean isRefresh = true;
	/**
	 * 获取最新页面时，page为0，page以此累加
	 */
	private static int page = 0;

	private TextView mTvManagerNum;
	private PullRefreshListView mListView;
	/* 加载更多FootView */
	private ListFooterLoadView mListFooterLoadView;
	/* 加载loading */
	private LoadingProgress mLoadProgress;

	private Handler mHandler = new MyHandler();
	private LayoutInflater mInflater;
	private Context mContext;
	private String mUid;

	private RoomManagerAdapter mAdapter;
	private RoomManagerAdapter.RemoveManagerListener removeManagerListener;

	public LiveManagerCustomDialogBuilder(Context context, String uid) {
		super(context, R.layout.dialog_live_manager_layout);
		mContext = context;
		mUid = uid;
		mInflater = LayoutInflater.from(context);
		this.mDialog.setCanceledOnTouchOutside(true);
		this.mDialog.setCancelable(true);
		initWidget();
		initListView();
		requestData();
	}

	private void requestData() {
		BusinessUtils.getManagerList(mContext, new GetManagerListCallbackData(LiveManagerCustomDialogBuilder.this), mUid);
	}

	/* 取消房管执行此方法 */
	public void updateAdapterData(String uid) {
		for (Map<String, Object> data : mAdapter.getData()) {
			// 列表中通过用户id查找到要取消的管理员,移除item跳出
			if (data.get("uid").equals(uid)) {
				mAdapter.getData().remove(data);
				mAdapter.notifyDataSetChanged();
				// 更新管理数量
				mTvManagerNum.setText(String.format(mContext.getString(R.string.live_manager_num), mAdapter.getData().size() + ""));
				break;
			}
		}
	}

	private void initWidget() {
		mListView = (PullRefreshListView) findViewById(R.id.live_manager_listview);
		mTvManagerNum = (TextView) findViewById(R.id.live_manager_tv_num);
		mTvManagerNum.setText(String.format(mContext.getString(R.string.live_manager_num), "0"));
	}

	private void initListView() {
		mListView.setTopHeadHeight(0);
		mListView.setBottomFooterHeight((int) (15 * mContext.getResources().getDisplayMetrics().density / 1.5f));
		mAdapter = new RoomManagerAdapter(mContext);
		removeManagerListener = new RoomManagerAdapter.RemoveManagerListener() {
			@Override
			public void onRemoveManager(int position, String uid) {
				MobclickAgent.onEvent(App.mContext, "cancelManagerByBroadcaster");
				BusinessUtils.removeRoomManager(mContext, new RemoveManagerCallbackData(LiveManagerCustomDialogBuilder.this, uid), uid);
			}
		};
		mAdapter.setListener(removeManagerListener);
		mListView.setAdapter(mAdapter);
		// 下拉刷新数据
		mListView.setTask(new Runnable() {
			@Override
			public void run() {
				// 请求主播数据
				requestData();
			}
		});
		View view = mInflater.inflate(R.layout.a_common_list_header_hint, null);
		view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mContext.getResources()
				.getDimensionPixelSize(R.dimen.list_hintview_height)));
		mListView.setPullnReleaseHintView(view);
		// 设置正确的颜色
		mListView.setHeaderBackgroudColor(mContext.getResources().getColor(R.color.app_background));

		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});

		// 设置默认图片
		mLoadProgress = (LoadingProgress) findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(mContext.getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new LoadingProgress.onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				mLoadProgress.Start(mContext.getResources().getString(R.string.a_progress_loading));
				// 重新加载数据。。。。。。。。。
				requestData();
			}

			@Override
			public void onClick(View v) {
				mLoadProgress.Start(mContext.getResources().getString(R.string.a_progress_loading));
				// 重新加載數據........
				requestData();
			}
		});
		mListView.setEmptyView(mLoadProgress);
	}

	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case GET_MANAGER_SUCC:
					Object[] objects = (Object[]) msg.obj;
					boolean isRefreh = (Boolean) objects[0];
					List<Map<String, Object>> mListData = (List<Map<String, Object>>) objects[1];
					EvtLog.d("Get_manager_succ", mListData.size() + "");
					if (isRefreh) { // 初始化或者下拉刷新模式
						mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
						mAdapter.clearData();
						mAdapter.addData(mListData);
					} else { // 加载更多数据模式
						if (mListData.isEmpty()) {
							mListFooterLoadView.onNoMoreData();
						} else {
							// 隐藏ListView的FootView
							mListFooterLoadView.hide();
							mAdapter.addData(mListData);
						}
					}
					mTvManagerNum.setText(String.format(mContext.getString(R.string.live_manager_num), mListData.size() + ""));
					// 设置没有数据的EmptyView
					mLoadProgress.Succeed(mContext.getString(R.string.live_manager_list_data_empty), R.drawable.a_common_no_data);
					break;
				case GET_MANAGER_FAIL:
					mListView.notifyTaskFinished();
					if (mAdapter.isEmpty()) {
						mLoadProgress.Failed(mContext.getString(R.string.a_loading_failed), 0);
					} else {
						UiHelper.showToast(mContext, R.string.a_tips_net_error);
						mLoadProgress.Hide();
						mListFooterLoadView.onLoadingFailed();
					}
					break;
				case REMOVE_MANAGER_SUCC:
					if (msg.obj != null) {
						String uid = String.valueOf(msg.obj);
						// 此处使用msg传过来的下标，避免同时点击造成数据覆盖
						updateAdapterData(uid);
					}
					break;
				case REMOVE_MANAGER_FAIL:
					// 取消管理员失败
					if (msg.obj != null) {
						UiHelper.showToast(mContext, String.valueOf(msg.obj));
					}
					break;
			}
		}
	}

	/**
	 * 獲取房管列表
	 */
	private static class GetManagerListCallbackData implements CallbackDataHandle {

		private final WeakReference<LiveManagerCustomDialogBuilder> mAcivity;

		public GetManagerListCallbackData(LiveManagerCustomDialogBuilder fragment) {
			mAcivity = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetManagerListCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = GET_MANAGER_SUCC;
					Object[] objects = new Object[]{isRefresh,
							JSONParser.parseSingleInMulti((JSONArray) result, new String[]{""})};
					msg.obj = objects;
					LiveManagerCustomDialogBuilder baseFragmentActivity = mAcivity.get();
					EvtLog.d(TAG, "GetManagerListCallback " + msg.obj);
					// 如果fragment未回收，发送消息
					if (baseFragmentActivity != null)
						baseFragmentActivity.mHandler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				msg.what = GET_MANAGER_FAIL;
				LiveManagerCustomDialogBuilder baseFragmentActivity = mAcivity.get();
				// 如果fragment未回收，发送消息
				if (baseFragmentActivity != null)
					baseFragmentActivity.mHandler.sendMessage(msg);
			}
		}
	}

	private static class RemoveManagerCallbackData implements CallbackDataHandle {
		private final WeakReference<LiveManagerCustomDialogBuilder> mAcivity;
		private String uid;

		public RemoveManagerCallbackData(LiveManagerCustomDialogBuilder fragment, String uid) {
			mAcivity = new WeakReference<>(fragment);
			this.uid = uid;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "RemoveManagerCallbackData success " + success + " errorCode" + errorCode + "remove UID" + uid);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = REMOVE_MANAGER_SUCC;
					msg.obj = uid;
					LiveManagerCustomDialogBuilder baseFragmentActivity = mAcivity.get();
					// 如果fragment未回收，发送消息
					if (baseFragmentActivity != null)
						baseFragmentActivity.mHandler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				msg.what = REMOVE_MANAGER_FAIL;
				msg.obj = errorMsg;
				LiveManagerCustomDialogBuilder baseFragmentActivity = mAcivity.get();
				// 如果fragment未回收，发送消息
				if (baseFragmentActivity != null)
					baseFragmentActivity.mHandler.sendMessage(msg);
			}
		}
	}

}
