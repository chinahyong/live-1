package tv.live.bx.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.FanExpandableAdapter;
import tv.live.bx.adapters.SelectFanExpandableAdapter;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.AsyncTaskThreadPool;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.tasks.BaseRunnable;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

/**
 * FanFragment
 *
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class MeFanSelectActivity extends BaseFragmentActivity implements OnClickListener {

	/**
	 * 需要刷新列表
	 */
	public static final int REQUEST_CODE_FLUSH_FRAGMENT = 102;
	/**
	 * 目前暂时都刷新
	 */
	private static boolean isRefresh = true;
	/**
	 * 获取最新页面时，page为0，page以此累加
	 */
	private static int page = 0;

	private PullToRefreshExpandableListView mPullRefreshListView;

	private LayoutInflater inflater;
	/**
	 * 加载loading
	 */
	private LoadingProgress mLoadProgress;
	private ListFooterLoadView mListFooterLoadView;
	private SelectFanExpandableAdapter mFanExpandableAdapter;

	/**
	 * 推荐数据
	 */
	private Map<String, Object> mRecommentData = null;

	@Override
	protected int getLayoutRes() {
		// TODO Auto-generated method stub
		return R.layout.fragment_fan_layout;
	}

	@Override
	protected void initMembers() {
		inflater = LayoutInflater.from(mActivity.getApplicationContext());
		// 初始化UI
		initUI(inflater);
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
	public void onPause() {
		super.onPause();
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
			case R.id.add_fanquan:
				ActivityJumpUtil.gotoActivityForResult(mActivity, AddFanActivity.class, REQUEST_CODE_FLUSH_FRAGMENT, null,
						null);
				break;
			case R.id.create_fanquan:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "createFanInCommunity");
				UiHelper.showToast(mActivity, R.string.commutity_fan_create_tip);
				break;

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_LOGIN " + resultCode);
			if (resultCode == LoginActivity.RESULT_CODE_OK) {

			}
		} else if (requestCode == REQUEST_CODE_FLUSH_FRAGMENT) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_FLUSH_FRAGMENT " + resultCode);
			// 重新加载数据
			reRequestData(false);
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.MSG_ME_FAN_LIST_FAILED:
				mPullRefreshListView.onRefreshComplete();
				if (mFanExpandableAdapter.isEmpty()) {
					String text2 = mActivity.getString(R.string.a_loading_failed);
					mLoadProgress.Failed(text2, 0);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
					mListFooterLoadView.onLoadingFailed();
				}

				break;

			case MsgTypes.MSG_ME_FAN_LIST_SUCCESS:
				Object[] objects = (Object[]) msg.obj;
				boolean isRefreh = (Boolean) objects[0];
				List<Map<String, Object>> mData = (List<Map<String, Object>>) objects[1];
				mPullRefreshListView.onRefreshComplete(); // 收起正在刷新HeaderView
				if (isRefreh) { // 初始化或者下拉刷新模式
					mFanExpandableAdapter.clearData();
					if (mRecommentData != null) {
						mData.add(0, mRecommentData);
					}
					mFanExpandableAdapter.addData(mData);
				} else { // 加载更多数据模式
					if (mData.isEmpty()) {
						mListFooterLoadView.onNoMoreData();
					} else {
						// 隐藏ListView的FootView
						mListFooterLoadView.hide();
						mFanExpandableAdapter.mergeAddData(mData);
					}
				}
				// 设置没有数据的EmptyView
				String text2 = mActivity.getString(R.string.a_list_data_empty);
				mLoadProgress.Succeed(text2, R.drawable.a_common_no_data);
				break;

			case MsgTypes.MSG_ADD_FAN_SUCCESS:
				UiHelper.showToast(mActivity, mActivity.getString(R.string.commutity_fan_add_succuss));
				break;
			case MsgTypes.MSG_ADD_FAN_FAILED:
				String errorMsg = (String) msg.obj;
				UiHelper.showToast(mActivity, errorMsg);
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
		mTopTitleTv.setText(R.string.home_me_group);
		mTopBackLayout.setOnClickListener(this);
	}

	/**
	 * 初始化下拉刷新ListView
	 *
	 * @param v
	 * @param inflater
	 */
	private void initListView(LayoutInflater inflater) {
		mPullRefreshListView = (PullToRefreshExpandableListView) findViewById(R.id.pull_refresh_expandable_list);
		mFanExpandableAdapter = new SelectFanExpandableAdapter(mActivity);
		// mFanExpandableAdapter.setOnItemClickListener(new
		// MeOnItemClickListener());
		mFanExpandableAdapter.setExpandableListView(mPullRefreshListView.getRefreshableView());
		mPullRefreshListView.getRefreshableView().setGroupIndicator(null);
		mPullRefreshListView.getRefreshableView().setDivider(null);
		mPullRefreshListView.getRefreshableView().setSelector(android.R.color.transparent);
		mPullRefreshListView.setMode(Mode.PULL_FROM_START);
		// mPullRefreshListView.getLoadingLayoutProxy(false,
		// true).setPullLabel("haha");
		// mPullRefreshListView.getLoadingLayoutProxy(false,
		// true).setRefreshingLabel("111");
		// mPullRefreshListView.getLoadingLayoutProxy(false,
		// true).setReleaseLabel("222");
		mPullRefreshListView.getRefreshableView().setAdapter(mFanExpandableAdapter);
		mPullRefreshListView.getRefreshableView().setChildDivider(
				mActivity.getResources().getDrawable(R.color.a_bg_color_f0f0f0));
		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ExpandableListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
				// 请求主播数据
				reRequestData(false);
			}
		});
		mFanExpandableAdapter.setOnChildClick(new SelectFanExpandableAdapter.OnChildClickListener() {
			@Override
			public void onChildClick(int groupPosition, int childPosition) {
				EvtLog.e(TAG, "onChildClick groupPosition,childPosition:" + groupPosition + "," + childPosition);
				Map<String, String> item = (Map<String, String>) ((ArrayList) mFanExpandableAdapter.getData()
						.get(groupPosition).get(FanExpandableAdapter.KEY_GROUP)).get(childPosition);
				Intent intent = new Intent();
				intent.putExtra(FanDetailActivity.FAN_INFO, (Serializable) item);
				setResult(Activity.RESULT_OK, intent);
				onBackPressed();
			}
		});
		// 为ExpandableListView的子列表单击事件设置监听器
		// ExpandableListView onCHildClickListener存在bug，将lastest item作为footer未添加到item列表中，导致响应不了点击
//		mPullRefreshListView.getRefreshableView().setOnChildClickListener(
//				new ExpandableListView.OnChildClickListener() {
//
//					public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
//												int childPosition, long id) {
//						EvtLog.e(TAG, "onChildClick groupPosition,childPosition:" + groupPosition + "," + childPosition);
//						Map<String, String> item = (Map<String, String>) ((ArrayList) mFanExpandableAdapter.getData()
//								.get(groupPosition).get(FanExpandableAdapter.KEY_GROUP)).get(childPosition);
//						Intent intent = new Intent();
//						intent.putExtra(FanDetailActivity.FAN_INFO, (Serializable) item);
//						setResult(Activity.RESULT_OK, intent);
//						onBackPressed();
//						return false;
//					}
//				});

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
		mPullRefreshListView.setEmptyView(mLoadProgress);
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
		mPullRefreshListView.getRefreshableView().addFooterView(mListFooterLoadView);
		mPullRefreshListView.getRefreshableView().setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (mListFooterLoadView.getParent() == mPullRefreshListView.getRefreshableView()) {
					// 至少翻过一项，才有可能执行加载更过操作
					if (mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_HIDDEN
							&& mPullRefreshListView.getRefreshableView().getFirstVisiblePosition() > mPullRefreshListView
							.getRefreshableView().getHeaderViewsCount()) {
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
		});
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
			mFanExpandableAdapter.clearData();
			mFanExpandableAdapter.notifyDataSetChanged();
		}
		isRefresh = true;
		requestPlazaData(page);
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page) {
		BusinessUtils.getMeGroup(mActivity, page, Constants.FAN_STATUS_NORMAL, new MeFanCallback(this));
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
	 * 列表中某些按钮的操作
	 */
	// class MeOnItemClickListener implements
	// FanExpandableAdapter.OnItemClickListener {
	//
	// @Override
	// public void onClick(View view, int groupType, int groupPosition, int
	// childPosition) {
	// EvtLog.e("MeOnItemClickListener", "onClick groupPosition:" +
	// groupPosition + ",groupType:" + groupType
	// + ",position:" + childPosition);
	// Map<String, String> item = (Map<String, String>) ((ArrayList)
	// mFanExpandableAdapter.getData()
	// .get(groupPosition).get(FanExpandableAdapter.KEY_GROUP)).get(childPosition);
	// if (groupType == FanExpandableAdapter.CATEGORY_REMEMENT) {
	// if (view.getId() == R.id.item_fanquan_add) {
	// MobclickAgent.onEvent(FeizaoApp.mConctext, "joinFanFromRecommend");
	// if (Constants.FAN_NO_JOIN.equals(item.get("joined"))) {
	// addFan(item.get("id"));
	// }
	// } else {
	// if (Constants.FAN_STATUS_NORMAL.equals(item.get("status"))) {
	// // 如果已加入
	// if (Constants.FAN_JOINED.equals(item.get("joined"))) {
	// ActivityJumpUtil.gotoActivityForResult(mThis, FanDetailActivity.class,
	// REQUEST_CODE_FLUSH_FRAGMENT, FanDetailActivity.FAN_INFO, (Serializable)
	// item);
	// } else {
	// UiHelper.showToast(mThis,
	// mThis.getResources().getString(R.string.commutity_fan_add_tip));
	// }
	// } else if (Constants.FAN_STATUS_FREEZE.equals(item.get("status"))) {
	// // 如果已加入
	// if (Constants.FAN_JOINED.equals(item.get("joined"))) {
	// UiHelper.showToast(mThis,
	// mThis.getResources().getString(R.string.commutity_fan_activate_tip));
	// } else {
	// UiHelper.showToast(mThis,
	// mThis.getResources().getString(R.string.commutity_fan_add_tip));
	// }
	// }
	// }
	//
	// } else {
	// switch (view.getId()) {
	// case R.id.item_fan_layout:
	// if (Constants.FAN_STATUS_FREEZE.equals(item.get("status"))) {
	// MobclickAgent.onEvent(FeizaoApp.mConctext, "inviteFriendInFanCircle");
	// Map<String, String> shareInfo = new HashMap<String, String>();
	// shareInfo.put(ShareActivity.Share_Title,
	// String.format(Constants.SHARE_FAN_TITLE, item.get("title"),
	// item.get("member_total")));
	// shareInfo.put(ShareActivity.Share_Content,
	// String.format(Constants.SHARE_FAN_CONTENT, item.get("detail")));
	// shareInfo.put(ShareActivity.Share_Img, item.get("logo"));
	// shareInfo.put(ShareActivity.Share_Url,
	// String.format(Constants.SHARE_FAN_URL, (String) item.get("id")));
	// shareInfo.put(ShareActivity.Share_Dialog, String.valueOf(true));
	// ActivityJumpUtil.toShareActivity(mThis, shareInfo);
	// } else {
	// ActivityJumpUtil.gotoActivityForResult(mThis, FanDetailActivity.class,
	// REQUEST_CODE_FLUSH_FRAGMENT, FanDetailActivity.FAN_INFO, (Serializable)
	// item);
	// }
	// break;
	//
	// default:
	// break;
	// }
	// }
	//
	// }
	// };

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

	/**
	 * 我的饭圈/我加入的饭圈回调 <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class MeFanCallback implements CallbackDataHandle {

		private WeakReference<BaseFragmentActivity> mFragment;

		public MeFanCallback(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "MeFanCallback success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_ME_FAN_LIST_SUCCESS;
					ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
					JSONArray myGroup = ((JSONObject) result).getJSONArray("myGroups");
					JSONArray myJoinedGroup = ((JSONObject) result).getJSONArray("myJoinedGroups");
					if (myGroup != null && myGroup.length() > 0) {
						Map<String, Object> item = new HashMap<String, Object>();
						item.put(FanExpandableAdapter.KEY_TITLE, "我创建的");
						item.put(FanExpandableAdapter.KEY_CATEGORY, FanExpandableAdapter.CATEGORY_LIST);
						item.put(FanExpandableAdapter.KEY_GROUP,
								JSONParser.parseSingleInMulti(myGroup, new String[]{""}));
						data.add(item);
					}
					if (myJoinedGroup != null && myJoinedGroup.length() > 0) {
						Map<String, Object> item = new HashMap<String, Object>();
						item.put(FanExpandableAdapter.KEY_TITLE, "我加入的");
						item.put(FanExpandableAdapter.KEY_CATEGORY, FanExpandableAdapter.CATEGORY_LIST);
						item.put(FanExpandableAdapter.KEY_GROUP,
								JSONParser.parseSingleInMulti(myJoinedGroup, new String[]{""}));
						data.add(item);
					}
					Object[] objects = new Object[]{isRefresh, data};
					msg.obj = objects;
					// 下次请求的页面数
					page++;
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
					// DatabaseUtils.saveCollectListSubjectInfos(
					// (List<Map<String, Object>>) objects[1], uId);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_ME_FAN_LIST_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}
		}
	}

}
