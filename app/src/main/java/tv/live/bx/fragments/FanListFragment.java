package tv.live.bx.fragments;

import android.app.Activity;
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
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.FanDetailActivity;
import tv.live.bx.activities.LoginActivity;
import tv.live.bx.activities.ShareDialogActivity;
import tv.live.bx.adapters.FanListAdapter;
import tv.live.bx.common.AsyncTaskThreadPool;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.common.WebConstants;
import tv.live.bx.config.AppConfig;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.tasks.BaseRunnable;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.PullRefreshListView;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;

/**
 * 饭圈列表 FanListFragment.java
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class FanListFragment extends BaseFragment implements OnClickListener, OnItemClickListener {

	/** 饭圈状态信息 */
	private String mData;

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

	private FanListAdapter mFanListAdapter;
	public static final String FAN_STATUS = "fan_status";

	private Map<String, String> shareInfo = new HashMap<String, String>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	/**
	 * 之前忘记使用这些方法了，这个类暂时不用了
	 */
	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_subject_layout;
	}

	@Override
	protected void initMembers() {
		initListView(mRootView, mActivity.getLayoutInflater());
	}

	@Override
	protected void initWidgets() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void setEventsListeners() {
	}

	@Override
	protected void initData(Bundle bundle) {
		if (bundle != null) {
			mData = bundle.getString(FAN_STATUS);
		}
		// 加载本地缓存数据
		AsyncTaskThreadPool.getThreadExecutorService().submit(new LoadCacheDataTask());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {

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
		Map<String, Object> item = (Map<String, Object>) mFanListAdapter.getItem(position
				- mListView.getHeaderViewsCount());
		EvtLog.d(TAG, "lmItem " + item.toString());
		if (Constants.FAN_STATUS_NORMAL.equals(item.get("status"))) {
			// 如果已加入
			// if (Constants.FAN_JOINED.equals(item.get("joined"))) {
			// ActivityJumpUtil.gotoActivity(mThis, FanDetailActivity.class,
			// false, FanDetailActivity.FAN_INFO,
			// (Serializable) item);
			// } else {
			// UiHelper.showToast(mThis,
			// mThis.getResources().getString(R.string.commutity_fan_add_tip));
			// }
			ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class, false, FanDetailActivity.FAN_INFO,
					(Serializable) item);
		} else if (Constants.FAN_STATUS_FREEZE.equals(item.get("status"))) {
			// 如果已加入
			if (Utils.getBooleanFlag(item.get("joined"))) {
				UiHelper.showToast(mActivity, mActivity.getResources().getString(R.string.commutity_fan_activate_tip));
			} else {
				UiHelper.showToast(mActivity, mActivity.getResources().getString(R.string.commutity_fan_add_tip));
			}
		}

		// if (Constants.FAN_STATUS_NORMAL.equals(item.get("status"))) {
		// ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class,
		// false, FanDetailActivity.FAN_INFO,
		// (Serializable) item);
		// } else if (Constants.FAN_STATUS_FREEZE.equals(item.get("status"))) {
		// UiHelper.showToast(mActivity,
		// mActivity.getResources().getString(R.string.commutity_fan_activate_tip));
		// }

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_LOGIN " + resultCode);
			if (resultCode == LoginActivity.RESULT_CODE_OK) {
				/** 重新初始化参数 */
				reRequestData(false, mData);
			}
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case MsgTypes.MSG_FAN_LIST_FAILED:
			mListView.notifyTaskFinished();
			if (mFanListAdapter.isEmpty()) {
				String text = mActivity.getString(R.string.a_loading_failed);
				mLoadProgress.Failed(text, 0);
			} else {
				UiHelper.showToast(mActivity, R.string.a_tips_net_error);
				mListFooterLoadView.onLoadingFailed();
			}

			break;

		case MsgTypes.MSG_FAN_LIST_SUCCESS:
			Object[] objects = (Object[]) msg.obj;
			boolean isRefreh = (Boolean) objects[0];
			List<Map<String, Object>> mListData = (List<Map<String, Object>>) objects[1];
			if (isRefreh) { // 初始化或者下拉刷新模式
				mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
				mFanListAdapter.clearData();
				mFanListAdapter.addData(mListData);
			} else { // 加载更多数据模式
				if (mListData.isEmpty()) {
					mListFooterLoadView.onNoMoreData();
				} else {
					// 如果点击的foot加载第一页的数据，重新更新adapter数据
					if (page == 1) {
						// 隐藏ListView的FootView
						mListFooterLoadView.hide();
						mFanListAdapter.clearData();
						mFanListAdapter.addData(mListData);
					} else {
						// 隐藏ListView的FootView
						mListFooterLoadView.hide();
						mFanListAdapter.addData(mListData);
					}
				}
			}
			// 设置没有数据的EmptyView
			String text = mActivity.getString(R.string.a_list_data_empty);
			mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
			break;
		case MsgTypes.MSG_ADD_FAN_SUCCESS:
			reRequestData(false, mData);
			UiHelper.showToast(mActivity, mActivity.getString(R.string.commutity_fan_add_succuss));
			break;
		case MsgTypes.MSG_ADD_FAN_FAILED:
			String errorMsg = (String) msg.obj;
			UiHelper.showToast(mActivity, errorMsg);
			break;
		}

	}

	@Override
	public void onTabClickAgain() {
		if (mListView != null) {
			mListView.setSelection(0);
		}
	}

	private void addFan(String groupId) {
		if (!AppConfig.getInstance().isLogged) {
			Utils.requestLoginOrRegister(mActivity, mActivity.getResources().getString(R.string.tip_login_title),
					Constants.REQUEST_CODE_LOGIN);
			return;
		}
		BusinessUtils.addFan(mActivity, groupId, new AddFanCallbackData());
	}

	/**
	 * 初始化下拉刷新ListView
	 * @param v
	 * @param inflater
	 */
	private void initListView(View v, LayoutInflater inflater) {
		mListView = (PullRefreshListView) v.findViewById(R.id.author_listview);
		mListView.setOnItemClickListener(this);
		mListView.setTopHeadHeight(0);

		mListView.setBottomFooterHeight((int) (15 * getResources().getDisplayMetrics().density / 1.5f));
		mFanListAdapter = new FanListAdapter(mActivity);
		mFanListAdapter.setOnItemClickListener(new FanListAdapter.OnItemClickListener() {

			@Override
			public void onClick(View view, int position) {
				Map<String, Object> item = (Map<String, Object>) mFanListAdapter.getItem(position);
				if (Constants.FAN_STATUS_FREEZE.equals(item.get("status"))) {
					// 如果已加入
					if (Utils.getBooleanFlag(item.get("joined"))) {
						MobclickAgent.onEvent(FeizaoApp.mConctext, "inviteFriendInFanCircle");
						shareInfo.put(ShareDialogActivity.Share_Title,
								String.format(Constants.SHARE_FAN_TITLE, item.get("name"), item.get("memberTotal")));
						shareInfo.put(ShareDialogActivity.Share_Content,
								String.format(Constants.SHARE_FAN_CONTENT, item.get("detail")));
						shareInfo.put(ShareDialogActivity.Share_Img, (String) item.get("logo"));
						shareInfo.put(ShareDialogActivity.Share_Url,
								String.format(WebConstants.getFullWebMDomain(WebConstants.SHARE_FAN_URL), (String) item.get("id")));
						shareInfo.put(ShareDialogActivity.Share_Dialog, String.valueOf(true));

						ActivityJumpUtil.toShareActivity(mActivity, shareInfo);
					} else {
						addFan(item.get("id").toString());
					}
				} else {
					if (Utils.getBooleanFlag(item.get("joined"))) {
						ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class, false,
								FanDetailActivity.FAN_INFO, (Serializable) item);
					} else {
						addFan(item.get("id").toString());
					}
				}
			}
		});
		mListView.setAdapter(mFanListAdapter);
		// 下拉刷新数据
		mListView.setTask(new Runnable() {
			@Override
			public void run() {
				// 请求主播数据
				reRequestData(false, mData);
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
					requestPlazaData(mData, page);
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
				// if (mListView.getChildCount() >
				// mListView.getHeaderViewsCount()
				// + mListView.getFooterViewsCount()) {
				if (mListFooterLoadView.getParent() == mListView) {
					// 至少翻过一项，才有可能执行加载更过操作
					if (mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_HIDDEN
							&& mListView.getFirstVisiblePosition() > mListView.getHeaderViewsCount()) {
						mListFooterLoadView.onLoadingStarted();
						EvtLog.d(TAG, "滚动加载更多");
						isRefresh = false;
						requestPlazaData(mData, page);
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
		mLoadProgress = (LoadingProgress) v.findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
				// 重新加载数据
				reRequestData(true, mData);

			}

			@Override
			public void onClick(View v) {
				mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
				reRequestData(true, mData);
			}
		});
		mListView.setEmptyView(mLoadProgress);
	}

	/** 更新数据 */
	public void updateRequestData() {
		if (!TextUtils.isEmpty(mData)) {
			reRequestData(false, mData);
		}
	}

	/**
	 * 重新请求数据
	 * 
	 * @param clearAdapter 请求之前是否先清空UI
	 */
	public void reRequestData(boolean clearAdapter, String status) {
		// 初始化loading(正在加载...)
		// 重新初始化请求页面
		page = 0;
		// 清空界面数据
		if (clearAdapter) {
			mFanListAdapter.clearData();
			mFanListAdapter.notifyDataSetChanged();
		}
		isRefresh = true;
		requestPlazaData(status, page);
	}

	/**
	 * 向服务端请求最新回复列表数据
	 */
	private void requestPlazaData(String status, int page) {
		BusinessUtils.getFanListData(mActivity, page, status, new FanListCallbackData());
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
					// 大于一分钟请求
					reRequestData(false, mData);
				}
			});

		}
	}

	/**
	 * 饭圈列表回调 ClassName: FanListCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * 
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private class FanListCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FanListCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_FAN_LIST_SUCCESS;
					Object[] objects = new Object[] { isRefresh,
							JSONParser.parseSingleInMulti((JSONArray) result, new String[] { "" }) };
					msg.obj = objects;
					// 下次请求的页面数
					page++;
					sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_FAN_LIST_FAILED;
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}

	}

	private class AddFanCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "AddFanCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_ADD_FAN_SUCCESS;
					sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_ADD_FAN_FAILED;
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}

	}

}
