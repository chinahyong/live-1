package tv.live.bx.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lonzh.lib.network.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.SelectReceivedMessageActivity;
import tv.live.bx.activities.WebViewActivity;
import tv.live.bx.adapters.FansCareAdapter;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.WebConstants;
import tv.live.bx.config.AppConfig;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.PullRefreshListView;
import tv.live.bx.ui.popwindow.FilterPopupWindow;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;

/**
 * Created by valar on 2017/3/23.
 * details：这是描述粉丝或关注的Fragment
 */

public class FansCareFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {

	public static final String SELECTEDSINGLEINFO = "selectSingInfo";
	public static final int REQUEST_CODE_BATCH_SEND = 0x1001;
	private PullRefreshListView mPullRefreshListView;
	private RelativeLayout mFansCareFitler;
	private FilterPopupWindow mFilterPopupWindow;
	private FilterPopupWindow.FitlerClickListener mFitlerClickListener;
	private FansCareAdapter mFansCareAdapter;

	/**
	 * 目前暂时都刷新
	 */
	private static boolean isRefresh = true;
	/**
	 * 获取最新页面时，page为0，page以此累加
	 */
	private static int page = 0;

	/**
	 * 加载更多FootView
	 */
	private ListFooterLoadView mListFooterLoadView;

	/**
	 * 加载更多 loading图
	 */
	private LoadingProgress mLoadProgress;

	/**
	 * 群发按钮
	 */
	private RelativeLayout mRlSendAllPerson;
	/**
	 * 小问号的layout
	 */
	private RelativeLayout mRlfansCareNotice;

	private List<Map<String, String>> mdataListInfo;     //每次网路请求返回来的集合
	/**
	 * 没有粉丝显示的Layout
	 */
	private LinearLayout mLlNoFans;

	private String isClickWhichInFilterFansCare = "0";
	private TextView mTvNoFansSended;
	private TextView mTvNoFansNeverSend;

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_fans_care;
	}

	@Override
	protected void initMembers() {
		initPullToRefreshListView();
		mFansCareFitler = (RelativeLayout) mRootView.findViewById(R.id.fans_care_filter);
		mRlSendAllPerson = (RelativeLayout) mRootView.findViewById(R.id.send_all_person);
		mRlfansCareNotice = (RelativeLayout) mRootView.findViewById(R.id.fans_care_notice);
		mLlNoFans = (LinearLayout) mRootView.findViewById(R.id.fans_care_nofans);
		mTvNoFansSended = (TextView) mRootView.findViewById(R.id.fans_care_nofans_sended);
		mTvNoFansNeverSend = (TextView) mRootView.findViewById(R.id.fans_care_nofans_neverSend);

	}

	private void initPullToRefreshListView() {
		mPullRefreshListView = (PullRefreshListView) mRootView.findViewById(R.id.fragment_fans_care_list);
		mPullRefreshListView.setOnItemClickListener(this);
		mPullRefreshListView.setTopHeadHeight(0);
		mPullRefreshListView.setBottomFooterHeight((int) (15 * getResources().getDisplayMetrics().density / 1.5f));
	}

	@Override
	protected void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {
		mFansCareFitler.setOnClickListener(this);
		mRlSendAllPerson.setOnClickListener(this);
		mFansCareAdapter = new FansCareAdapter(mActivity);
		mPullRefreshListView.setAdapter(mFansCareAdapter);
		mRlfansCareNotice.setOnClickListener(this);
		mTvNoFansNeverSend.setOnClickListener(this);
		mTvNoFansSended.setOnClickListener(this);
		mFitlerClickListener = new FilterPopupWindow.FitlerClickListener() {
			@Override
			public void onClick(View view) {
				switch (view.getId()) {
					case R.id.filter_sended:
						selectSendedFans();
						NoFansGone();
						reRequestData(true,"1");
						mFilterPopupWindow.dismiss();
						break;
					case R.id.filter_never_Send:
						selectNeverSendFans();
						NoFansGone();
						reRequestData(true,"2");
						mFilterPopupWindow.dismiss();
						break;
					case R.id.filter_all:
						selectAllFans();
						NoFansGone();
						reRequestData(true,"0");
						mFilterPopupWindow.dismiss();
						break;
				}
			}
		};
		if (mFilterPopupWindow == null) {
			mFilterPopupWindow = new FilterPopupWindow(mActivity, mFitlerClickListener);
		}

	}

	private void NoFansGone() {
		mLlNoFans.setVisibility(View.GONE);
		mTvNoFansSended.setVisibility(View.GONE);
		mTvNoFansNeverSend.setVisibility(View.GONE);
	}


	@Override
	protected void initData(Bundle bundle) {
		//刚点击进入粉丝界面请求的是全选的数据
		selectAllFans();
		reRequestData(false,"0");

		// 下拉刷新数据
		mPullRefreshListView.setTask(new Runnable() {
			@Override
			public void run() {
				// 请求主播数据
				reRequestData(false, isClickWhichInFilterFansCare);
			}
		});
		View view = mActivity.getLayoutInflater().inflate(R.layout.a_common_list_header_hint, null);
		view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources()
				.getDimensionPixelSize(R.dimen.list_hintview_height)));
		mPullRefreshListView.setPullnReleaseHintView(view);
		// 设置正确的颜色
		mPullRefreshListView.setHeaderBackgroudColor(getResources().getColor(R.color.app_background));

		// 设置上滑动加载更多
		mListFooterLoadView = (ListFooterLoadView) mActivity.getLayoutInflater().inflate(R.layout.a_common_list_footer_loader_view, null);
		mListFooterLoadView.hide();
		mListFooterLoadView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ListFooterLoadView listFooterLoadView = (ListFooterLoadView) v;
				int status = listFooterLoadView.getStatus();
				if (status == ListFooterLoadView.STATUS_FAILED || status == ListFooterLoadView.STATUS_NOMORE) {
					listFooterLoadView.onLoadingStarted();
					isRefresh = false;
					requestPlazaData(page, isClickWhichInFilterFansCare);
				}
			}
		});
		mPullRefreshListView.addFooterView(mListFooterLoadView);
		mPullRefreshListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// if (mListView.getChildCount() >
				// mListView.getHeaderViewsCount()
				// + mListView.getFooterViewsCount()) {
				if (mListFooterLoadView.getParent() == mPullRefreshListView) {
					// 至少翻过一项，才有可能执行加载更过操作
					if (mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_HIDDEN
							&& mPullRefreshListView.getFirstVisiblePosition() > mPullRefreshListView.getHeaderViewsCount()) {
						mListFooterLoadView.onLoadingStarted();
						EvtLog.d(TAG, "滚动加载更多");
						isRefresh = false;
						requestPlazaData(page, isClickWhichInFilterFansCare);
					}
				} else {
					if (mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_FAILED
							|| mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_NOMORE) {
						mListFooterLoadView.hide();
					}
				}
				// }
			}
		});

		// 设置默认图片
		mLoadProgress = (LoadingProgress) mRootView.findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new LoadingProgress.onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
				// 重新加载数据
				reRequestData(true, isClickWhichInFilterFansCare);

			}

			@Override
			public void onClick(View v) {
				mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
				reRequestData(true, isClickWhichInFilterFansCare);
			}
		});
		mPullRefreshListView.setEmptyView(mLoadProgress);

	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.MSG_FAN_CARE_LIST_FAILED:
				mPullRefreshListView.notifyTaskFinished();
				if (mFansCareAdapter.isEmpty()) {
					String text = mActivity.getString(R.string.a_loading_failed);
					mLoadProgress.Failed(text, 0);
					mLlNoFans.setVisibility(View.GONE);
					mTvNoFansSended.setVisibility(View.GONE);
					mTvNoFansNeverSend.setVisibility(View.GONE);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
					mListFooterLoadView.onLoadingFailed();
					mLlNoFans.setVisibility(View.GONE);
					mTvNoFansSended.setVisibility(View.GONE);
					mTvNoFansNeverSend.setVisibility(View.GONE);
				}
				break;
			case MsgTypes.MSG_FAN_CARE_LIST_SUCCESS:
				Object[] objects = (Object[]) msg.obj;
				boolean isRefreh = (Boolean) objects[0];
				Map<String, Object> mListData = (Map<String, Object>) objects[1];
				String dataInfo = (String) mListData.get("list");
				String mFansCounts = (String) mListData.get("userNum");
				mFilterPopupWindow.mFilterAllTextNum.setText("(" + mFansCounts + ")");
				try {
					//解析回来最新的数据 3种情况 第一种:所有 第二种：已发 第三种：未发
					mdataListInfo = JSONParser.parseMulti(dataInfo);
					/**
					 * 这里只要执行了下拉刷新，就默认选中全选按钮，将之前所有的数据清空，只显示第一页最新的数据
					 */
					if (isRefreh) { // 初始化或者下拉刷新模式
						if (mdataListInfo.size() == 0 ) {
							if ("0".equals(isClickWhichInFilterFansCare)) {
								mLlNoFans.setVisibility(View.VISIBLE);
								mTvNoFansSended.setVisibility(View.GONE);
								mTvNoFansNeverSend.setVisibility(View.GONE);
							}else if ("1".equals(isClickWhichInFilterFansCare)){
								mLlNoFans.setVisibility(View.GONE);
								mTvNoFansSended.setVisibility(View.VISIBLE);
								mTvNoFansNeverSend.setVisibility(View.GONE);
							}else if ("2".equals(isClickWhichInFilterFansCare)){
								mLlNoFans.setVisibility(View.GONE);
								mTvNoFansSended.setVisibility(View.GONE);
								mTvNoFansNeverSend.setVisibility(View.VISIBLE);
							}
						} else {
							mLlNoFans.setVisibility(View.GONE);
							mTvNoFansSended.setVisibility(View.GONE);
							mTvNoFansNeverSend.setVisibility(View.GONE);
						}
						mPullRefreshListView.notifyTaskFinished(); // 收起正在刷新HeaderView
						mFansCareAdapter.clearData();
						mFansCareAdapter.addData(mdataListInfo);
					} else { // 加载更多数据模式
						if (mdataListInfo.isEmpty()) {
							mListFooterLoadView.onNoMoreData();
						} else {
							// 如果点击的foot加载第一页的数据，重新更新adapter数据
							if (page == 1) {
								// 隐藏ListView的FootView
								mListFooterLoadView.hide();
								mFansCareAdapter.clearData();
								mFansCareAdapter.addData(mdataListInfo);
							} else {
								// 隐藏ListView的FootView
								mListFooterLoadView.hide();
								mFansCareAdapter.addData(mdataListInfo);
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mLoadProgress.Hide();
				break;
		}
	}

	private void selectSendedFans() {
		isClickWhichInFilterFansCare = "1";
		mFilterPopupWindow.mFilterSended.setBackgroundResource(R.drawable.shape_sened_checked_bg);
		mFilterPopupWindow.mFilterNeverSend.setBackgroundColor(Color.parseColor("#ffffff"));
		mFilterPopupWindow.mFilterAll.setBackgroundResource(R.drawable.shape_all_bg);
	}

	private void selectNeverSendFans() {
		isClickWhichInFilterFansCare = "2" ;
		mFilterPopupWindow.mFilterSended.setBackgroundResource(R.drawable.shape_sened_bg);
		mFilterPopupWindow.mFilterNeverSend.setBackgroundColor(Color.parseColor("#f2f2f2"));
		mFilterPopupWindow.mFilterAll.setBackgroundResource(R.drawable.shape_all_bg);
	}

	private void selectAllFans() {
		isClickWhichInFilterFansCare = "0";
		mFilterPopupWindow.mFilterSended.setBackgroundResource(R.drawable.shape_sened_bg);
		mFilterPopupWindow.mFilterNeverSend.setBackgroundColor(Color.parseColor("#ffffff"));
		mFilterPopupWindow.mFilterAll.setBackgroundResource(R.drawable.shape_all_checked_bg);
	}


	@Override
	public void onStart() {
		super.onStart();
		//判断是不是从SXXXRXXMXX点击返回按钮返回来的
		if (AppConfig.getInstance().isBackFromSelectedReceiver) {
			reRequestData(true,"0");
			selectAllFans();
			AppConfig.getInstance().updateIsBackFromSelectedReceiver(false);
		}

	}

	@Override
	protected void onTabSelected() {
		if (mActivity != null) {
			NoFansGone();
			selectAllFans();
			reRequestData(true,"0");
		}
	}

	@Override
	public void onTabClickAgain() {
		if (mActivity != null) {
			NoFansGone();
			selectAllFans();
			reRequestData(true,"0");
		}
	}

	/**
	 * 重新请求数据
	 *
	 * @param clearAdapter 请求之前是否先清空UI
	 */
	public void reRequestData(boolean clearAdapter,String filter) {
		// 初始化loading(正在加载...)
		// 重新初始化请求页面
		page = 0;
		// 清空界面数据
		if (clearAdapter) {
			mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
			mFansCareAdapter.clearData();
			mFansCareAdapter.notifyDataSetChanged();
		}
		isRefresh = true;
		requestPlazaData(page,filter);
	}

	/**
	 * 向服务端请求最新回复列表数据
	 */
	public void requestPlazaData(int page ,String filter) {
		BusinessUtils.getFansCareList(mActivity, page, filter,new FansCareListCallbackData());
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.fans_care_filter:
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickSiftingInFansPage", null);
				mFilterPopupWindow.showPopupWindow(view);
				break;
			case R.id.send_all_person:
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickSendAllInFansPage", null);
				ActivityJumpUtil.gotoActivity(mActivity, SelectReceivedMessageActivity.class, false, null, null);
				break;
			case R.id.fans_care_notice:
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickHelpingBarInFansPage", null);
				Map<String, String> webInfo = new HashMap<String, String>();
				webInfo.put(WebViewActivity.URL, WebConstants.getFullWebMDomain(WebConstants.FANS_CARE_NOTICE));
				webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(true));
				ActivityJumpUtil.gotoActivity(mActivity, WebViewActivity.class, false, WebViewActivity.WEB_INFO,
						(Serializable) webInfo);
				break;
			case R.id.fans_care_nofans_sended:
				mFilterPopupWindow.mFilterAll.callOnClick();
				break;
			case R.id.fans_care_nofans_neverSend:
				mFilterPopupWindow.mFilterAll.callOnClick();
				break;
		}
	}

	//列表的监听
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
		Map<String,String> singItem = (Map<String, String>) adapterView.getAdapter().getItem(position );
	}

	/**
	 * 关注Fragment的数据回调再此
	 */
	private class FansCareListCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FanListCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_FAN_CARE_LIST_SUCCESS;
					Object[] objects = new Object[]{isRefresh,
							JSONParser.parseOne((JSONObject) result)};

					msg.obj = objects;
					// 下次请求的页面数
					page++;
					sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_FAN_CARE_LIST_FAILED;
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}

	}


}
