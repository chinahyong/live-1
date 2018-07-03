package com.bixin.bixin.fragments;

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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bixin.bixin.App;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.framework.net.impl.CallbackDataHandle;
import tv.live.bx.R;
import com.bixin.bixin.activities.LiveTypeActivity;
import com.bixin.bixin.adapters.LiveFragmentStatusAdapter;
import com.bixin.bixin.adapters.LiveTagAdapter;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.helper.operation.OperationHelper;
import com.bixin.bixin.database.DatabaseUtils;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.listeners.GoHotClickListener;
import com.bixin.bixin.ui.ListFooterLoadView;
import com.bixin.bixin.ui.LoadingProgress.onProgressClickListener;
import com.bixin.bixin.ui.LoadingProgressEmptyJump;
import com.bixin.bixin.ui.PullRefreshListView;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;

/**
 * @author Live
 * @version 2.9.0 2017.1.4
 * @title LiveFocusFragment.java Description:主播列表页"后台下发的标签列表对应主播列表"
 */
public class LiveTagFragment extends BaseFragment implements OnClickListener, OnItemClickListener {
	private static final int MSG_LOAD_SUCCESS = 0x10;
	private static final int MSG_LOAD_FAILED = 0x11;
	// 目前暂时都刷新
	private static boolean isRefresh = true;
	// 获取最新页面时，page为0，page以此累加
	private static int page = 0;

	private PullRefreshListView mListView;
	/**
	 * 加载更多FootView
	 */
	private ListFooterLoadView mListFooterLoadView;

	/**
	 * 加载loading
	 */
	private LoadingProgressEmptyJump mLoadProgress;
	private LiveTagAdapter mAuthorAdapter;

	// 空数据
	private Button mBtnBackHot;
	private TextView mTvEmptyMsg;

	private GoHotClickListener mGoHotClickListener;
	private String mTag;

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_live_tag;
	}

	@Override
	protected void initMembers() {
		// 初始化UI
		initUI(mRootView, mInflater);
	}

	@Override
	protected void initWidgets() {

	}

	@Override
	protected void setEventsListeners() {
		mBtnBackHot.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle bundle) {
		if (bundle != null) {
			mTag = bundle.getString(LiveFragmentStatusAdapter.ID);
		}
		reRequestData(false);
	}

	/**
	 * 切换tab，也更新数据 TODO 简单描述该方法的实现功能（可选）.
	 *
	 * @see BaseFragment#onTabSelected()
	 */
	@Override
	protected void onTabSelected() {
		super.onTabSelected();
		if (mAuthorAdapter != null && mAuthorAdapter.isEmpty()) {
			// mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
			reRequestData(false);
		}
	}

	/**
	 * 初始化UI控件
	 */
	private void initUI(View v, LayoutInflater inflater) {
		mBtnBackHot = (Button) v.findViewById(R.id.live_new_btn_back_hot);
		mTvEmptyMsg = (TextView) v.findViewById(R.id.live_empty_msg);
		initListView(v, inflater);
	}

	/**
	 * 初始化下拉刷新ListView
	 *
	 * @param v
	 * @param inflater
	 */
	private void initListView(View v, LayoutInflater inflater) {
		mListView = (PullRefreshListView) v.findViewById(R.id.author_listview);
		mListView.setTopHeadHeight(0);
		mListView.setBottomFooterHeight((int) (15 * getResources().getDisplayMetrics().density / 1.5f));
		mListView.setOnItemClickListener(this);
		mAuthorAdapter = new LiveTagAdapter(mActivity);
		// 主播类别标签的点击事件
		mAuthorAdapter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Map<String, String> type = (Map<String, String>) v.getTag();
				ActivityJumpUtil.toLiveTypeActivity(mActivity, type.get(LiveTypeActivity.TYPE_ID),
						type.get(LiveTypeActivity.TYPE_NAME));

			}
		});
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
		mLoadProgress = (LoadingProgressEmptyJump) v.findViewById(R.id.progress);
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
				// mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				// reRequestData(true);
			}
		});
		mListView.setEmptyView(mLoadProgress);
		mListView.setAdapter(mAuthorAdapter);
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
		BusinessUtils.getAuthorListDataByTag(mActivity, page, mTag, 0,
				new FocusCallbackData(this));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.live_new_btn_back_hot:
				if (mGoHotClickListener != null)
					mGoHotClickListener.onGoHotClick();
				break;
		}
	}

	@Override
	public void onTabClickAgain() {
		// 双击底部tab 回调到此进行刷新(会掉到ParentFragment，在回调到ChirldrenFragment)
		if (mListView != null)
			mListView.pullDownRefresh();
	}

	/**
	 * 主播列表Item点击
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position - mListView.getHeaderViewsCount() < 0) {
			return;
		}
		/* umeng后台出现 outOfBounds，先捕获处理，暂未定位到原因 */
		try {
			Map<String, String> map = new HashMap<>();
			map.put("tabId", mTag);
			MobclickAgent.onEvent(App.mContext, "clickBroadcasterSImgInClassificationTab");
			OperationHelper.onEvent(App.mContext, "clickBroadcasterSImgInClassificationTab", map);
			@SuppressWarnings("unchecked")
			Map<String, Object> lmItem = (Map<String, Object>) mAuthorAdapter.getItem(position
					- mListView.getHeaderViewsCount());
			lmItem.put("rid", lmItem.get("rid"));
			ActivityJumpUtil.toLiveMediaPlayerActivity(mActivity, lmItem);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setGoHotClickListener(GoHotClickListener mGoHotClickListener) {
		this.mGoHotClickListener = mGoHotClickListener;
	}

	/**************************************
	 * 回调事件处理
	 *********************************************/

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
//				if (!TextUtils.isEmpty(String.valueOf(objects[2]))) {
				mTvEmptyMsg.setText(R.string.anchor_live_tag_live_empty_msg);
//				}
				mLoadProgress.Succeed(null, 1);
				break;
		}
	}

	/**
	 * 我关注的主播数据处理回调 ClassName: PublishRoomCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class FocusCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragment> mFragment;

		public FocusCallbackData(BaseFragment fragment) {
			mFragment = new WeakReference<BaseFragment>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "AuthorCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_LOAD_SUCCESS;
					Object[] objects = new Object[3];
					objects[0] = isRefresh;
					objects[1] = JSONParser.parseSingleInMulti((JSONArray) result, new String[]{"moderator"});
					objects[2] = errorMsg;
					msg.obj = objects;
					// 下次请求的页面数
					page++;
					BaseFragment authorFragment = mFragment.get();
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
				BaseFragment fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}
		}
	}
}
