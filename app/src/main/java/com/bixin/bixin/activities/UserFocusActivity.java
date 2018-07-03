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
import java.util.List;
import java.util.Map;

import com.framework.net.impl.CallbackDataHandle;
import tv.live.bx.R;
import com.bixin.bixin.base.act.BaseFragmentActivity;
import com.bixin.bixin.adapters.UserInfoAdapter;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.ui.ListFooterLoadView;
import com.bixin.bixin.ui.LoadingProgress;
import com.bixin.bixin.ui.LoadingProgress.onProgressClickListener;
import com.bixin.bixin.ui.PullRefreshListView;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;

/**
 * 用户的关注
 */
public class UserFocusActivity extends BaseFragmentActivity implements OnItemClickListener {
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

	private UserInfoAdapter mListAdapter;

	public static String USER_ID = "user_id";
	private String mUserId;
	/**
	 * 是否自己
	 */
	private boolean isOwer;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_my_focus;
	}

	@Override
	protected void initMembers() {
		initListView(mInflater);
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		if (isOwer) {
			mTopTitleTv.setText(R.string.person_me_focus);
		} else {
			mTopTitleTv.setText(R.string.person_ta_focus);
		}
		mTopBackLayout.setOnClickListener(new OnBack());
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MsgTypes.GET_FOCUS_USER_INFO_FAILED:
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

			case MsgTypes.GET_FOCUS_USER_INFO_SUCCESS:
				Object[] objects = (Object[]) msg.obj;
				boolean isRefreh = Boolean.valueOf(objects[0].toString());
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
				String text = mActivity.getString(R.string.love_list_data_empty);
				mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
				break;
			case MsgTypes.FOLLOW_SUCCESS:
				int position = Integer.valueOf(msg.obj.toString());
				mListAdapter.getData().get(position).put("isAttention", Constants.COMMON_TRUE);
				mListAdapter.notifyDataSetChanged();
				showToast(R.string.person_focus_success, TOAST_SHORT);
				UiHelper.showNotificationDialog(mActivity);
				break;
			case MsgTypes.FOLLOW_FAILED:
				showToast(msg.obj.toString(), TOAST_LONG);
				break;
			case MsgTypes.REMOVE_FOLLOW_SUCCESS:
				int removePosition = Integer.valueOf(msg.obj.toString());
				mListAdapter.getData().get(removePosition).put("isAttention", "false");
				mListAdapter.notifyDataSetChanged();
				showToast(R.string.person_remove_focus_success, TOAST_SHORT);
				break;
			case MsgTypes.REMOVE_FOLLOW_FAILED:
				showToast(msg.obj.toString(), TOAST_LONG);
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
		/* umeng后台出现 outOfBounds，先捕获处理，暂未定位到原因 */
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> lmItem = (Map<String, Object>) mListAdapter.getItem(position
					- mListView.getHeaderViewsCount());
			ActivityJumpUtil.toPersonInfoActivity(mActivity, lmItem, REQUEST_CODE_FLUSH_ACTIVITY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//跳转他人主页，并且更新了对他人的关注状态
		if (requestCode == REQUEST_CODE_FLUSH_ACTIVITY && resultCode == RESULT_OK) {
			// 刷新我关注的列表
			reRequestData(false);
		}
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
		mListAdapter = new UserInfoAdapter(this);
		mListAdapter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/**
				 * @date 2016.6.8
				 * umeng log:java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
				 * (String)v.getTag() 强制转换属于泛型的一种方式，不可预计尽量避免使用
				 */
//                final int position = Integer.parseInt((String) v.getTag());
				final int position = Integer.valueOf(v.getTag().toString());
				// 如果已经关注了，则取消关注
				if (Utils.strBool(mListAdapter.getData().get(position).get("isAttention"))) {
					BusinessUtils.removeFollow(mActivity,
							new RemoveFollowCallbackData(UserFocusActivity.this, position),
							mListAdapter.getData().get(position).get("id"));
				} else {
					BusinessUtils.follow(mActivity, new FollowCallbackData(UserFocusActivity.this, position),
							mListAdapter.getData().get(position).get("id"));
				}
			}
		});
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
				// LogUtil.e(TAG,
				// "firstVisibleItem,visibleItemCount,totalItemCount:" +
				// firstVisibleItem + ","
				// + visibleItemCount + "," + totalItemCount);
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
		// 清空界面数据
		page = 0;
		// mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
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
		BusinessUtils.getLoveListData(mActivity, new UserInfoCallbackData(this), page, mUserId);
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		mUserId = getIntent().getStringExtra(USER_ID);
		isOwer = getIntent().getBooleanExtra(PersonInfoActivity.IS_OWER, false);
		initTitle();
		reRequestData(false);
	}

	/*************************************
	 * 事件处理器
	 ************************************/
	private class OnBack implements OnClickListener {
		@Override
		public void onClick(View arg0) {
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
	private static class UserInfoCallbackData implements CallbackDataHandle {
		private final WeakReference<BaseFragmentActivity> mFragment;

		public UserInfoCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "LiveFocusCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.GET_FOCUS_USER_INFO_SUCCESS;
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
				msg.what = MsgTypes.GET_FOCUS_USER_INFO_FAILED;
				msg.obj = errorMsg;
				BaseFragmentActivity fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}

		}
	}

	/**
	 * 关注用户信息回调 Reason: TODO ADD REASON(可选). <br/>
	 */
	private class FollowCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mAcivity;

		private int position;

		public FollowCallbackData(BaseFragmentActivity fragment, int position) {
			mAcivity = new WeakReference<BaseFragmentActivity>(fragment);
			this.position = position;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FollowCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.FOLLOW_SUCCESS;
					msg.obj = position;
					BaseFragmentActivity baseFragmentActivity = mAcivity.get();
					// 如果fragment未回收，发送消息
					if (baseFragmentActivity != null)
						baseFragmentActivity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.FOLLOW_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity baseFragmentActivity = mAcivity.get();
				// 如果fragment未回收，发送消息
				if (baseFragmentActivity != null)
					baseFragmentActivity.sendMsg(msg);
			}
		}

	}

	/**
	 * 取消关注用户信息回调 Reason: TODO ADD REASON(可选). <br/>
	 */
	private class RemoveFollowCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mAcivity;

		private int position;

		public RemoveFollowCallbackData(BaseFragmentActivity fragment, int position) {
			mAcivity = new WeakReference<BaseFragmentActivity>(fragment);
			this.position = position;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FollowCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.REMOVE_FOLLOW_SUCCESS;
					msg.obj = position;
					BaseFragmentActivity baseFragmentActivity = mAcivity.get();
					// 如果fragment未回收，发送消息
					if (baseFragmentActivity != null)
						baseFragmentActivity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.REMOVE_FOLLOW_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity baseFragmentActivity = mAcivity.get();
				// 如果fragment未回收，发送消息
				if (baseFragmentActivity != null)
					baseFragmentActivity.sendMsg(msg);
			}
		}

	}

}