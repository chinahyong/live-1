package tv.live.bx.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.efeizao.bx.R;
import tv.live.bx.activities.FanDetailActivity;
import tv.live.bx.activities.GroupPostDetailActivity;
import tv.live.bx.adapters.HotSubjectListAdapter;
import tv.live.bx.adapters.RecommentFanAdapter;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.database.DatabaseUtils;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.ui.HeaderGridView;
import tv.live.bx.ui.HorizontalListView;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshHeaderGridView;
import com.lonzh.lib.network.JSONParser;

import org.json.JSONArray;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

/**
 * Title: HomeHotFragment.java</br> Description: 主播Fragment</br> Copyright: *
 * Copyright (c) 2008</br
 *
 * @version 1.0
 * @CreateDate 2014-8-13
 */
@SuppressLint("NewApi")
public class HomeHotFragment extends BaseFragment implements OnClickListener, OnItemClickListener {

	private static final int MSG_LOAD_SUCCESS = 0x10;
	private static final int MSG_LOAD_FAILED = 0x11;
	private static final int MSG_LOAD_CITIES = 0x12;

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

	private PullToRefreshHeaderGridView mPullRefreshGridView;
	private HotSubjectListAdapter mSubjectAdapter;
	/**
	 * 加载更多FootView
	 */
	private ListFooterLoadView mListFooterLoadView;

	/**
	 * 加载loading
	 */
	private LoadingProgress mLoadProgress;

	/**
	 * 头部
	 */
	private LinearLayout mHeadLayout;
	private HorizontalListView mHorizontalListView;
	private RecommentFanAdapter mRecommentFanAdapter;

	private AlertDialog mProgress;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.a_author_list_layout;
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

	}

	@Override
	protected void initData(Bundle bundle) {
//		mProgress = Utils.showProgress(mActivity);
		// 加载Banner,anchor列表
		reRequestAllData();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		dismissProgressDialog();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			// case R.id.liveBtn:
			// MobclickAgent.onEvent(FeizaoApp.mConctext, "clickLiveButton");
			// Map<String, String> lmItem = new HashMap<String, String>();
			// lmItem.put("rid", (String) mUserInfo.get("rid"));
			// ActivityJumpUtil.gotoActivity(mActivity,
			// PreviewLivePlayActivity.class, false, LiveBaseActivity.ANCHOR_INFO,
			// (Serializable) lmItem);
			// break;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		int mPosition = position - mPullRefreshGridView.getRefreshableView().getHeaderViewCount()
				* mPullRefreshGridView.getRefreshableView().getNumColumns();
		if (mPosition < 0) {
			return;
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> lmItem = (Map<String, Object>) mSubjectAdapter.getItem(mPosition);
		ActivityJumpUtil.gotoActivity(mActivity, GroupPostDetailActivity.class, false, "subjectInfo",
				(Serializable) lmItem);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.MSG_GROUP_POST_LIST_FAILED:
				dismissProgressDialog();
				mPullRefreshGridView.onRefreshComplete();
				if (mSubjectAdapter.isEmpty()) {
					String text = mActivity.getString(R.string.a_loading_failed);
					mLoadProgress.Failed(text, 0);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
					mLoadProgress.Hide();
					mListFooterLoadView.onLoadingFailed();
				}

				break;

			case MsgTypes.MSG_GROUP_POST_LIST_SUCCESS:
				dismissProgressDialog();
				Object[] objects = (Object[]) msg.obj;
				boolean isRefreh = (Boolean) objects[0];
				List<Map<String, Object>> mData = (List<Map<String, Object>>) objects[1];
				mPullRefreshGridView.onRefreshComplete(); // 收起正在刷新HeaderView
				if (isRefreh) { // 初始化或者下拉刷新模式
					mSubjectAdapter.clearData();
					mSubjectAdapter.addData(mData);
				} else { // 加载更多数据模式
					if (mData.isEmpty()) {
						mListFooterLoadView.onNoMoreData();
					} else {
						// 如果点击的foot加载第一页的数据，重新更新adapter数据
						if (page == 1) {
							// 隐藏ListView的FootView
							mListFooterLoadView.hide();
							mSubjectAdapter.clearData();
							mSubjectAdapter.addData(mData);
						} else {
							// 隐藏ListView的FootView
							mListFooterLoadView.hide();
							mSubjectAdapter.addData(mData);
						}
					}
				}
				// 设置没有数据的EmptyView
				String text = mActivity.getString(R.string.a_list_data_empty);
				mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
				break;

			case MsgTypes.MSG_FAN_RECOMMENT_LIST_SUCCESS:
				mRecommentFanAdapter.clearData();
				mRecommentFanAdapter.addData((List<Map<String, Object>>) msg.obj);
				break;
			case MsgTypes.MSG_FAN_RECOMMENT_LIST_FAILED:
				break;
		}

	}

	@Override
	public void onTabClickAgain() {
		if (mPullRefreshGridView != null) {
			mPullRefreshGridView.getRefreshableView().setSelection(0);
		}
	}

	/**
	 * 切换tab，也更新数据 TODO 简单描述该方法的实现功能（可选）.
	 *
	 * @see tv.live.bx.fragments.BaseFragment#onTabSelected()
	 */
	@Override
	protected void onTabSelected() {
		super.onTabSelected();
		// 没有初始化，不加载数据
		// if (mPullRefreshGridView != null) {
		// // 加载Banner,anchor列表
		// reRequestAllData();
		// }

	}

	/**
	 * 关闭对话框
	 */
	private void dismissProgressDialog() {
		if (mProgress != null && mProgress.isShowing())
			mProgress.dismiss();
	}

	/**
	 * 初始化UI控件
	 */
	private void initUI(View v, LayoutInflater inflater) {

		mHeadLayout = (LinearLayout) inflater.inflate(R.layout.fragment_home_hot_head, null);
		initHeadLayout();

		initListView(v, inflater);
	}

	private void initHeadLayout() {
		mHorizontalListView = (HorizontalListView) mHeadLayout.findViewById(R.id.tariler_listview);
		mHorizontalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Map<String, Object> item = (Map<String, Object>) mRecommentFanAdapter.getItem(position);
				if (Constants.FAN_STATUS_NORMAL.equals(item.get("status"))) {
					ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class, false,
							FanDetailActivity.FAN_INFO, (Serializable) item);
				} else if (Constants.FAN_STATUS_FREEZE.equals(item.get("status"))) {
					UiHelper.showToast(mActivity,
							mActivity.getResources().getString(R.string.commutity_fan_activate_tip));
				}
			}
		});

		mRecommentFanAdapter = new RecommentFanAdapter(mActivity);
		mHorizontalListView.setAdapter(mRecommentFanAdapter);
	}

	/**
	 * 初始化下拉刷新ListView
	 *
	 * @param v
	 * @param inflater
	 */
	private void initListView(View v, LayoutInflater inflater) {
		mPullRefreshGridView = (PullToRefreshHeaderGridView) v.findViewById(R.id.author_listview);
		mPullRefreshGridView.setMode(Mode.BOTH);
		mPullRefreshGridView.getLoadingLayoutProxy(false, true).setLoadingDrawable(
				mActivity.getResources().getDrawable(R.drawable.a_common_progress_circle));
		mPullRefreshGridView.getLoadingLayoutProxy(false, true).setPullLabel(
				mActivity.getText(R.string.a_list_hint_pullup_to_load_more));
		mPullRefreshGridView.getLoadingLayoutProxy(false, true).setRefreshingLabel(
				mActivity.getText(R.string.a_list_hint_loading));
		mPullRefreshGridView.getLoadingLayoutProxy(false, true).setReleaseLabel(
				mActivity.getText(R.string.a_list_hint_release_to_load_more));
		mSubjectAdapter = new HotSubjectListAdapter(mActivity);

		// mPullRefreshGridView.getRefreshableView().addHeaderView(mAdBanner);
		mPullRefreshGridView.getRefreshableView().addHeaderView(mHeadLayout);
		// mPullRefreshGridView.getRefreshableView().setBottomFooterHeight((int)
		// (15 * getResources().getDisplayMetrics().density / 1.5f));

		// mPullRefreshGridView.setOnRefreshListener(new
		// OnRefreshListener<HeaderGridView>() {
		// @Override
		// public void onRefresh(PullToRefreshBase<HeaderGridView> refreshView)
		// {
		// // 加载Banner,anchor列表
		// reRequestAllData();
		// }
		// });

		mPullRefreshGridView.setOnRefreshListener(new OnRefreshListener2<HeaderGridView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<HeaderGridView> refreshView) {
				// 加载Banner,anchor列表
				reRequestAllData();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<HeaderGridView> refreshView) {
				EvtLog.d(TAG, "滚动加载更多");
				isRefresh = false;
				requestPlazaData(page);
			}

		});
		mPullRefreshGridView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				EvtLog.d(TAG, "setOnLastItemVisibleListener");
				mPullRefreshGridView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
			}
		});

		mPullRefreshGridView.setOnItemClickListener(this);
		// View view = inflater.inflate(R.layout.a_common_list_header_hint,
		// null);
		// view.setLayoutParams(new
		// LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, getResources()
		// .getDimensionPixelSize(R.dimen.list_hintview_height)));
		// mPullRefreshGridView.setPullnReleaseHintView(view);
		// // 设置正确的颜色
		// mPullRefreshGridView.setHeaderBackgroudColor(getResources().getColor(R.color.app_background));

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
		// mPullRefreshGridView.getRefreshableView().addFooterView(mListFooterLoadView);
		// mPullRefreshGridView.setOnScrollListener(new OnScrollListener() {
		// @Override
		// public void onScrollStateChanged(AbsListView view, int scrollState) {
		//
		// }
		//
		// @Override
		// public void onScroll(AbsListView view, int firstVisibleItem, int
		// visibleItemCount, int totalItemCount) {
		// if (totalItemCount > mPullRefreshGridView.getHeaderViewsCount()
		// + mPullRefreshGridView.getFooterViewsCount()) {
		// if (mListFooterLoadView.getParent() == mPullRefreshGridView) {
		// // 至少翻过一项，才有可能执行加载更过操作
		// if (mListFooterLoadView.getStatus() ==
		// ListFooterLoadView.STATUS_HIDDEN
		// && mPullRefreshGridView.getFirstVisiblePosition() >
		// mPullRefreshGridView
		// .getHeaderViewsCount()) {
		// mListFooterLoadView.onLoadingStarted();
		// EvtLog.d(TAG, "滚动加载更多");
		// isRefresh = false;
		// requestPlazaData(page);
		// }
		// } else {
		// if (mListFooterLoadView.getStatus() ==
		// ListFooterLoadView.STATUS_FAILED
		// || mListFooterLoadView.getStatus() ==
		// ListFooterLoadView.STATUS_NOMORE) {
		// mListFooterLoadView.hide();
		// }
		// }
		// }
		// }
		// });

		// 设置默认图片
		mLoadProgress = (LoadingProgress) v.findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				// 加载Banner,anchor列表
				reRequestAllData();

			}

			@Override
			public void onClick(View v) {
				mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				// 加载Banner,anchor列表
				reRequestAllData();
			}
		});
		mPullRefreshGridView.getRefreshableView().setEmptyView(mLoadProgress);
		mPullRefreshGridView.getRefreshableView().setAdapter(mSubjectAdapter);
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
			mSubjectAdapter.clearData();
			mSubjectAdapter.notifyDataSetChanged();
		}
		isRefresh = true;
		requestPlazaData(page);
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page) {
		BusinessUtils.getHotGroupPostList(mActivity, page, new GetPostListCallbackData(this));
	}

	// /**
	// * 本地缓存数据加载
	// */
	// private class LoadCacheDataTask extends BaseRunnable {
	//
	// @Override
	// public void runImpl() {
	// // EvtLog.d(TAG, "LoadCacheDataTask loading local data start");
	// // Message msg = new Message();
	// // msg.what = MsgTypes.GET_MAIN_BANNERS_SUCCESS;
	// // List<Map<String, String>> bannerDatas =
	// // DatabaseUtils.getListBannerInfos();
	// // msg.obj = bannerDatas;
	// // sendMsg(msg);
	// //
	// // Message msg2 = new Message();
	// // msg2.what = MsgTypes.MSG_LOAD_SUCCESS;
	// // Object[] objects = new Object[] { true,
	// // DatabaseUtils.getListAnchorInfos() };
	// // msg2.obj = objects;
	// // sendMsg(msg2);
	// // EvtLog.d(TAG, "LoadCacheDataTask loading local data end");
	//
	// mHandler.post(new Runnable() {
	//
	// @Override
	// public void run() {
	// // 加载Banner,anchor列表
	// BusinessUtils.getMainBanners(mActivity, new
	// BannerCallbackData(HomeHotFragment.this));
	// BusinessUtils.getRecommentGroup(mActivity, new
	// FanRecommentCallback(this));
	// reRequestData(false);
	// }
	// });
	// }
	//
	// }

	private void reRequestAllData() {
//		BusinessUtils.getMainBanners(mActivity, new BannerCallbackData(HomeHotFragment.this));
		BusinessUtils.getRecommentGroup(mActivity, new FanRecommentCallback(HomeHotFragment.this));
		reRequestData(false);
	}

	/**
	 * 横幅数据处理回调 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class BannerCallbackData implements CallbackDataHandle {
		private final WeakReference<BaseFragment> mFragment;

		public BannerCallbackData(BaseFragment fragment) {
			mFragment = new WeakReference<BaseFragment>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "BannerCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.GET_MAIN_BANNERS_SUCCESS;
					List<Map<String, String>> bannerDatas = JSONParser.parseMulti((JSONArray) result);
					msg.obj = JSONParser.parseMulti((JSONArray) result);
					// 如果fragment未回收，发送消息
					if (mFragment.get() != null)
						mFragment.get().sendMsg(msg);
					DatabaseUtils.saveListBannerInfos((List<Map<String, String>>) msg.obj);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.GET_MAIN_BANNERS_FAILED;
				msg.obj = errorCode;
				// 如果fragment未回收，发送消息
				if (mFragment.get() != null)
					mFragment.get().sendMsg(msg);
			}

		}
	}

	/**
	 * 帖子列表数据处理回调 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class GetPostListCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragment> mFragment;

		public GetPostListCallbackData(BaseFragment fragment) {
			mFragment = new WeakReference<BaseFragment>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetPostListCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_GROUP_POST_LIST_SUCCESS;
					Object[] objects = new Object[]{isRefresh,
							JSONParser.parseSingleInMulti((JSONArray) result, new String[]{""})};
					msg.obj = objects;
					// 下次请求的页面数
					page++;
					BaseFragment activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_GROUP_POST_LIST_FAILED;
				msg.obj = errorMsg;
				BaseFragment activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

	/**
	 * 饭圈推荐列表 <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class FanRecommentCallback implements CallbackDataHandle {

		private WeakReference<BaseFragment> mFragment;

		public FanRecommentCallback(BaseFragment fragment) {
			mFragment = new WeakReference<BaseFragment>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FanRecommentCallback success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_FAN_RECOMMENT_LIST_SUCCESS;
					msg.obj = JSONParser.parseMultiInMulti((JSONArray) result, new String[]{""});
					BaseFragment fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
					// DatabaseUtils.saveCollectListSubjectInfos(
					// (List<Map<String, Object>>) objects[1], uId);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_FAN_RECOMMENT_LIST_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragment fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}
		}
	}

	// /**
	// * 首页Activity更新回调接口
	// */
	// @Override
	// public void onUpdate() {
	// // 加载Banner,anchor列表
	// BusinessUtils.getMainBanners(mActivity, new
	// BannerCallbackData(HomeHotFragment.this));
	// reRequestData(false);
	//
	// }

}
