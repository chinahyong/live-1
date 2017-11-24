package tv.live.bx.activities;

import android.content.DialogInterface;
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
import android.widget.TextView;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.NewSubjectListAdapter;
import tv.live.bx.adapters.NewSubjectListAdapter.IOnclickListener;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.AsyncTaskThreadPool;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.fragments.LiveChatFragment;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.HtmlTagHandler;
import tv.live.bx.tasks.BaseRunnable;
import tv.live.bx.ui.ActionSheetDialog;
import tv.live.bx.ui.ActionSheetDialog.OnSheetItemClickListener;
import tv.live.bx.ui.ActionSheetDialog.SheetItemColor;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.PullRefreshListView;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

/**
 * 社区页面 Title: CommutityFragment.java
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class MeCollectActivity extends BaseFragmentActivity implements OnClickListener, OnItemClickListener {

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

	private NewSubjectListAdapter mSubjectAdapter;
	public static final String KEY_TITLE = "title";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_EXTRAS = "extras";


	/** 点击下拉按钮弹出的对话框 */
	private ActionSheetDialog actionSheetDialog;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_collect_layout;
	}

	@Override
	protected void initMembers() {
		// 初始化UI
		initUI(mInflater);
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
	public void onDestroy() {
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
		Map<String, String> lmItem = (Map<String, String>) mSubjectAdapter.getItem(position
				- mListView.getHeaderViewsCount());
		ActivityJumpUtil.toGroupPostDetailActivity(mActivity, lmItem, null,
				PostActivity.REQUEST_CODE_POSTDETAIL_FRAGMENT);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_LOGIN " + resultCode);
			if (resultCode == LoginActivity.RESULT_CODE_OK) {
				/** 重新初始化参数 */
				reRequestData(false);
			} else {

			}
		} else if (requestCode == PostActivity.REQUEST_CODE_POSTDETAIL_FRAGMENT) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_POSTDETAIL_FRAGMENT " + resultCode);
			/** 重新初始化参数 */
			reRequestData(false);
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case MsgTypes.MSG_COLLECT_LIST_FAILED:
			mListView.notifyTaskFinished();
			if (mSubjectAdapter.isEmpty()) {
				String text = mActivity.getString(R.string.a_loading_failed);
				mLoadProgress.Failed(text, 0);
			} else {
				UiHelper.showToast(mActivity, R.string.a_tips_net_error);
				mLoadProgress.Hide();
				mListFooterLoadView.onLoadingFailed();
			}

			break;

		case MsgTypes.MSG_COLLECT_LIST_SUCCESS:
			Object[] objects = (Object[]) msg.obj;
			boolean isRefreh = (Boolean) objects[0];
			List<Map<String, Object>> mListData = (List<Map<String, Object>>) objects[1];
			if (isRefreh) { // 初始化或者下拉刷新模式
				mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
				mSubjectAdapter.clearData();
				mSubjectAdapter.addData(mListData);
			} else { // 加载更多数据模式
				if (mListData.isEmpty()) {
					mListFooterLoadView.onNoMoreData();
				} else {
					// 如果点击的foot加载第一页的数据，重新更新adapter数据
					if (page == 1) {
						// 隐藏ListView的FootView
						mListFooterLoadView.hide();
						mSubjectAdapter.clearData();
						mSubjectAdapter.addData(mListData);
					} else {
						// 隐藏ListView的FootView
						mListFooterLoadView.hide();
						mSubjectAdapter.addData(mListData);
					}
				}
			}
			// 设置没有数据的EmptyView
			String text = mActivity.getString(R.string.me_collect_no_data);
			mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
			break;
		case MsgTypes.MSG_SUPPORT_SUCCESS:
			// UiHelper.showToast(mThis, (String) msg.obj);
			break;
		case MsgTypes.MSG_SUPPORT_FAILED:
			UiHelper.showToast(mActivity, (String) msg.obj);
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
		mTopTitleTv.setText(R.string.me_collect);
		mTopBackLayout.setOnClickListener(this);
	}

	/**
	 * 初始化title信息
	 */
	// private void initTitle(View v) {
	// titleTv = (TextView) v.findViewById(R.id.top_title);
	// titleTv.setText(R.string.me_collect);
	// moreLayout = (RelativeLayout) v.findViewById(R.id.top_right);
	// moreLayout.setOnClickListener(this);
	// moreLayout.setVisibility(View.GONE);
	// backLayout = (RelativeLayout) v.findViewById(R.id.top_left);
	// backLayout.setOnClickListener(this);
	// }

	/**
	 * 添加最新的活动到列表的第一项
	 */
	// private void addLatestItemByGid(String gid) {
	// CEvent event = EventDBManager.getEventByGid(mThis, gid);
	// if (event != null) {
	// PublicEventItem item = PlazaUtils.convert2PlazaItem(event);
	// if (mAdapter != null) {
	// mAdapter.addFirstItem(item);
	// }
	// }
	// }

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
		mSubjectAdapter = new NewSubjectListAdapter(mActivity);
		HtmlTagHandler mHtmlTagHandler = new HtmlTagHandler();
		mHtmlTagHandler.setOnIClickUsernName(new LiveChatFragment.IClickUserName() {
			@Override
			public void onClick(String username, String uid) {
				Map<String, String> fanInfo = new HashMap<String, String>();
				fanInfo.put("id", uid);
				ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class, false, FanDetailActivity.FAN_INFO,
						(Serializable) fanInfo);
			}
		});
		mSubjectAdapter.setTagHandler(mHtmlTagHandler);
		mSubjectAdapter.setOnClickListener(new OnClick());
		mListView.setAdapter(mSubjectAdapter);
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
		// 重新初始化请求页面
		page = 0;
		// mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
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
		BusinessUtils.getCollectSubjectListData(mActivity, page, new CollectCallbackData(this));
	}

	/** 实现帖子列表项中控件的响应 */
	class OnClick implements IOnclickListener {

		@Override
		public void onClick(View v, int position, View statusView) {
			switch (v.getId()) {
			// 点击头像进入个人中心
			case R.id.item_photo:

				break;
			case R.id.item_moudle_text:
				Map<String, String> fanInfo = new HashMap<String, String>();
				fanInfo.put("id", (String) mSubjectAdapter.getData().get(position).get("groupId"));
				ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class, false, FanDetailActivity.FAN_INFO,
						(Serializable) fanInfo);
				break;
			case R.id.item_support:
				if (Utils.getBooleanFlag(v.getTag().toString())) {
					UiHelper.showToast(mActivity, "您已经赞了,不能再赞了");
					return;
				}
				if (!AppConfig.getInstance().isLogged) {
					Utils.requestLoginOrRegister(mActivity, "点赞需要先登录", Constants.REQUEST_CODE_LOGIN);
					return;
				}
				// 先UI更新
				v.setSelected(true);
				v.setTag(Constants.COMMON_TRUE);
				BusinessUtils.groupSupport(mActivity, null,
						Integer.parseInt((String) mSubjectAdapter.getData().get(position).get("id")));
				int supportNum = Integer.parseInt((String) mSubjectAdapter.getData().get(position).get("supportNum"));
				mSubjectAdapter.getData().get(position).put("supported", Constants.COMMON_TRUE);
				mSubjectAdapter.getData().get(position).put("supportNum", String.valueOf(supportNum + 1));
				((TextView) statusView).setText(String.valueOf(supportNum + 1));
				break;
			case R.id.item_share:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "sharePost");
				if (!AppConfig.getInstance().isLogged) {
					Utils.requestLoginOrRegister(mActivity, "分享需要先登录", Constants.REQUEST_CODE_LOGIN);
					return;
				}
				GroupPostDetailActivity.shareGroupPostInfo(mSubjectAdapter.getData().get(position), mActivity);
				break;
			case R.id.item_more:
				showRelayDialog(position);
				break;

			default:
				break;
			}

		}
	}

	/**
	 * 弹出对话框
	 */
	private void showRelayDialog(final int position) {
		EvtLog.e(TAG, "showRelayDialog position" + position);
		final Map<String, Object> subjectInfo = mSubjectAdapter.getData().get(position);
		if (AppConfig.getInstance().isLogged && UserInfoConfig.getInstance().id.equals(subjectInfo.get("uid"))) {
			actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
					.setCanceledOnTouchOutside(true)
					.addSheetItem("删除", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							// 按返回键弹出对话框
							UiHelper.showConfirmDialog(mActivity, R.string.commutity_comfirm_delete_post,
									R.string.cancel, R.string.determine, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {

										}
									}, new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											BusinessUtils.deleteGroupPost(mActivity, (String) subjectInfo.get("id"),
													new DeletePostCallbackData(MeCollectActivity.this, position));
										}
									});

						}
					});
		} else {
			actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
					.setCanceledOnTouchOutside(true)
					.addSheetItem("举报", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							ActivityJumpUtil.toReportActivity(mActivity, Constants.COMMON_REPORT_TYPE_POST, subjectInfo
									.get("id").toString(), 0);
						}
					});
		}
		actionSheetDialog.show();

	}

	/**
	 * 本地缓存数据加载
	 */
	private class LoadCacheDataTask extends BaseRunnable {

		@Override
		public void runImpl() {
			// EvtLog.d(TAG, "LoadCacheDataTask loading local data start");
			// Message msg = new Message();
			// msg.what = MsgTypes.MSG_COLLECT_LIST_SUCCESS;
			// Object[] objects = new Object[] { true,
			// DatabaseUtils.getCollectListSubjectInfos(uId,
			// SubjectInfo.COLLECTED, SubjectInfo.NO_PUBLISHED) };
			// msg.obj = objects;
			// sendMsg(msg);
			// EvtLog.d(TAG, "LoadCacheDataTask loading local data end");

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					reRequestData(false);
				}
			});
		}

	}

	/**
	 * 帖子数据处理回调 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * 
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class CollectCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public CollectCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "CollectCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_COLLECT_LIST_SUCCESS;
					Object[] objects = new Object[] { isRefresh,
							JSONParser.parseSingleInMulti((JSONArray) result, new String[] { "" }) };
					msg.obj = objects;
					// 下次请求的页面数
					page++;
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
					// if (isRefresh) {
					// DatabaseUtils.saveCollectListSubjectInfos((List<Map<String,
					// Object>>) objects[1], uId,
					// SubjectInfo.COLLECTED, SubjectInfo.NO_PUBLISHED);
					// }
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_COLLECT_LIST_FAILED;
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
	 * 删除帖子处理回调 ClassName: DeleteRelayCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * 
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class DeletePostCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;
		private int position;

		public DeletePostCallbackData(BaseFragmentActivity fragment, int position) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
			this.position = position;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "DeletePostCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_DELETE_POST_SUCCESS;
					Bundle bundle = new Bundle();
					bundle.putInt("position", position);
					msg.setData(bundle);
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_DELETE_POST_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

}
