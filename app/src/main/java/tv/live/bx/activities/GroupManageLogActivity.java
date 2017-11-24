package tv.live.bx.activities;

import android.app.AlertDialog;
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

import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.GroupManageListAdapter;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.WebConstants;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.PullRefreshListView;
import com.lonzh.lib.network.JSONParser;

import org.json.JSONArray;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

/**
 * 饭圈管理日志: GroupManageLogActivity.java
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class GroupManageLogActivity extends BaseFragmentActivity implements OnClickListener, OnItemClickListener {

	/**
	 * actionType 和 entityType
	 */
	public static String ACTION_TYPE_ADD_ADMIN = "1";
	public static String ACTION_TYPE_REMOVE_ADMIN = "2";
	public static String ACTION_TYPE_ADD_BAN = "3";
	public static String ACTION_TYPE_REMOVE_BAN = "4";
	public static String ACTION_TYPE_ADD_TOP = "5";
	public static String ACTION_TYPE_REMOVE_TOP = "6";
	public static String ACTION_TYPE_ADD_NICE = "7";
	public static String ACTION_TYPE_REMOVE_NICE = "8";
	public static String ACTION_TYPE_REMOVE_POST = "9";

	public static String ENTITY_TYPE_USER = "1";
	public static String ENTITY_TYPE_POST = "2";

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

	private GroupManageListAdapter mGroupManageLogAdapter;
	private AlertDialog mProcessDialog;
	private LayoutInflater inflater;

	/** 饭圈信息 */
	private Map<String, String> mFanInfo;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_group_manage_layout;
	}

	protected void initMembers() {
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			mFanInfo = (Map<String, String>) bundle.getSerializable(FanDetailActivity.FAN_INFO);
		}

		inflater = LayoutInflater.from(getApplicationContext());
		initListView();
		initTitle();
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {

	}

	@Override
	protected void initData(Bundle savedInstanceState) {

		reRequestData(false);
	}

	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.commutity_log_manage);
		mTopBackLayout.setOnClickListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(FanDetailActivity.FAN_INFO, (Serializable) mFanInfo);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		mFanInfo = (Map<String, String>) savedInstanceState.getSerializable(FanDetailActivity.FAN_INFO);
	}

	@Override
	public void onDestroy() {
		dismissProcessDialog();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.top_left:
			onBackPressed();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position - mListView.getHeaderViewsCount() < 0) {
			return;
		}
		if (position - mListView.getHeaderViewsCount() >= mGroupManageLogAdapter.getCount()) {
			return;
		}
		clickItem(position - mListView.getHeaderViewsCount());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case MsgTypes.MSG_GROUP_LOG_SUCCESS:
			dismissProcessDialog();

			Object[] objects = (Object[]) msg.obj;
			boolean isRefreh = (Boolean) objects[0];
			List<Map<String, String>> mListData = (List<Map<String, String>>) objects[1];
			if (isRefreh) { // 初始化或者下拉刷新模式
				mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
				mGroupManageLogAdapter.clearData();
				mGroupManageLogAdapter.addData(mListData);
			} else { // 加载更多数据模式
				if (mListData.isEmpty()) {
					mListFooterLoadView.onNoMoreData();
				} else {
					// 隐藏ListView的FootView
					mListFooterLoadView.hide();
					mGroupManageLogAdapter.addData(mListData);
				}
			}
			// 设置没有数据的EmptyView
			String text = mActivity.getString(R.string.commutity_fan_menber_search_empty);
			mLoadProgress.Succeed(text, 0);

			break;
		case MsgTypes.MSG_GROUP_LOG_FAILED:
			dismissProcessDialog();

			mListView.notifyTaskFinished();
			if (mGroupManageLogAdapter.isEmpty()) {
				String text1 = mActivity.getString(R.string.a_loading_failed);
				mLoadProgress.Failed(text1, 0);
			} else {
				UiHelper.showToast(mActivity, R.string.a_tips_net_error);
				mLoadProgress.Hide();
				mListFooterLoadView.onLoadingFailed();
			}
			break;
		}

	}

	/**
	 * 初始化下拉刷新ListView
	 */
	private void initListView() {

		mListView = (PullRefreshListView) findViewById(R.id.author_listview);
		mListView.setOnItemClickListener(this);
		mListView.setTopHeadHeight(0);
		mListView.setBottomFooterHeight((int) (15 * getResources().getDisplayMetrics().density / 1.5f));
		mGroupManageLogAdapter = new GroupManageListAdapter(this);
		// mfanListAdapter.setOnClickListener(new
		// FanMenberListAdapter.OnClickListener() {
		//
		// @Override
		// public void onClick(View v, boolean flag, String uid, int position) {
		// BusinessUtils.addFanAdmin(mActivity, mFanInfo.get("id"), uid, flag,
		// new FanAdminCallbackData(flag,
		// position));
		// }
		// });
		mListView.setAdapter(mGroupManageLogAdapter);
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
				// EvtLog.e(TAG,
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
		// 初始化loading(正在加载...)
		mLoadProgress.setProgressClickListener(new onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				// 重新加载数据
				reRequestData(true);

			}

			@Override
			public void onClick(View v) {
				reRequestData(true);
			}
		});
		mListView.setEmptyView(mLoadProgress);
	}

	/**
	 * 重新请求数据
	 * @param clearAdapter 请求之前是否先清空UI
	 */
	public void reRequestData(boolean clearAdapter) {
		page = 0;
		isRefresh = true;
		requestPlazaData(page);
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page) {
		BusinessUtils.getGroupManageLogList(this, mFanInfo.get("id"), page, new GroupManageLogCallbackData(this));
	}

	/**
	 * 点击“成员列表”
	 * @param position 成员数据pos
	 */
	private void clickItem(final int position) {
		Map<String, String> mData = (Map<String, String>) mGroupManageLogAdapter.getItem(position);
		// 如果是操作帖子，且不是删除帖子
		if (GroupManageLogActivity.ENTITY_TYPE_POST.equals(mData.get("entityType"))
				&& !GroupManageLogActivity.ACTION_TYPE_REMOVE_POST.equals(mData.get("actionType"))) {
			Map<String, String> subjectInfo = new HashMap<>();
			subjectInfo.put("id", mData.get("postId"));
			ActivityJumpUtil.toGroupPostDetailActivity(mActivity, subjectInfo, null,
					PostActivity.REQUEST_CODE_POSTDETAIL_FRAGMENT);
		}

	}

	private void dismissProcessDialog() {
		if (mProcessDialog != null && mProcessDialog.isShowing()) {
			mProcessDialog.dismiss();
		}
	}

	/** 页面“更多”操作的回调处理类 */
	private class MoreItemListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.item1_layout:
				break;
			case R.id.item2_layout:
				Map<String, String> webInfo = new HashMap<>();
				webInfo.put(WebViewActivity.URL, WebConstants.getFullWebMDomain(WebConstants.COMMON_HELP));
				webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(true));
				ActivityJumpUtil.gotoActivity(mActivity, WebViewActivity.class, false, WebViewActivity.WEB_INFO,
						(Serializable) webInfo);
				break;
			default:
				break;
			}

		}

	}

	/**
	 * 成员列表 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * 
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class GroupManageLogCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public GroupManageLogCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GroupManageLogCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					Object[] objects = new Object[] { isRefresh, JSONParser.parseMulti((JSONArray) result) };
					msg.obj = objects;
					// 下次请求的页面数
					page++;

					msg.what = MsgTypes.MSG_GROUP_LOG_SUCCESS;
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_GROUP_LOG_FAILED;
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

	/**
	 * 设置/取消管理员 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * 
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private class FanAdminCallbackData implements CallbackDataHandle {

		/** 设置/取消为管理员 */
		private boolean flag;
		private int position;

		public FanAdminCallbackData(boolean flag, int position) {
			this.flag = flag;
			this.position = position;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FanAdminCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				msg.what = MsgTypes.MSG_FAN_ADMIN_SUCCESS;
				Bundle bundle = new Bundle();
				bundle.putBoolean("flag", flag);
				bundle.putInt("position", position);
				msg.setData(bundle);
				sendMsg(msg);
			} else {
				msg.what = MsgTypes.MSG_FAN_ADMIN_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}

	}

	/**
	 * 禁言数据回调<br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * 
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private class BanCallbackData implements CallbackDataHandle {

		private boolean flag;
		private int position;

		public BanCallbackData(boolean flag, int position) {
			this.flag = flag;
			this.position = position;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "BanCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				msg.what = MsgTypes.MSG_GROUP_BAN_SUCCESS;
				Bundle bundle = new Bundle();
				bundle.putBoolean("flag", flag);
				bundle.putInt("position", position);
				msg.setData(bundle);
				sendMsg(msg);
			} else {
				msg.what = MsgTypes.MSG_GROUP_BAN_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}

	}

}
