package tv.live.bx.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.efeizao.bx.R;
import tv.live.bx.activities.LoginActivity;
import tv.live.bx.activities.PostActivity;
import tv.live.bx.adapters.SubjectListAdapter;
import tv.live.bx.adapters.SubjectListAdapter.IOnclickListener;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.AsyncTaskThreadPool;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.database.DatabaseUtils;
import tv.live.bx.database.model.SubjectInfo;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.tasks.BaseRunnable;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.PullRefreshListView;
import com.lonzh.lib.network.JSONParser;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

/**
 * 我发布过的帖子列表 Title: MePublishFragment.java
 *
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class MePublishFragment extends BaseFragment implements OnClickListener, OnItemClickListener {
	private final String TAG = "MePublishFragment";

	private static final long REPORT_GEO_INTERVAL = DateUtils.HOUR_IN_MILLIS;

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

	private SubjectListAdapter mSubjectAdapter;
	public static final String KEY_TITLE = "title";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_EXTRAS = "extras";

	/**
	 * 更多按钮
	 */
	private RelativeLayout moreLayout;
	private ImageView moreImageView;
	/**
	 * 返回按钮
	 */
	private RelativeLayout backLayout;
	/**
	 * 标题
	 */
	private TextView titleTv;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_collect_layout;
	}

	@Override
	protected void initMembers() {
		// 初始化UI
		initUI(mRootView, mInflater);
	}

	@Override
	protected void initWidgets() {

		// TODO Auto-generated method stub

	}

	@Override
	protected void setEventsListeners() {

		// TODO Auto-generated method stub

	}

	@Override
	protected void initData(Bundle bundle) {
		// 加载本地缓存数据
		AsyncTaskThreadPool.getThreadExecutorService().submit(new LoadCacheDataTask());
	}

	@Override
	protected void onTabSelected() {
		super.onTabSelected();
		if (mListView != null) {
			reRequestData(false);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.top_left:
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
		if ("0".equals(lmItem.get("group_id"))) {
			ActivityJumpUtil.toPostDetailActivity(mActivity, lmItem, null,
					PostActivity.REQUEST_CODE_POSTDETAIL_FRAGMENT);
		} else {
			ActivityJumpUtil.toGroupPostDetailActivity(mActivity, lmItem, null,
					PostActivity.REQUEST_CODE_POSTDETAIL_FRAGMENT);
		}

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
			case MsgTypes.MSG_PUBLISH_LIST_FAILED:
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

			case MsgTypes.MSG_PUBLISH_LIST_SUCCESS:
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
				// UiHelper.showToast(mActivity, (String) msg.obj);
				break;
			case MsgTypes.MSG_SUPPORT_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;
		}

	}

	/**
	 * 初始化UI控件
	 */
	private void initUI(View v, LayoutInflater inflater) {
		initTitle(v);
		initListView(v, inflater);
	}

	/**
	 * 初始化title信息
	 */
	private void initTitle(View v) {
		titleTv = (TextView) v.findViewById(R.id.top_title);
		titleTv.setText(R.string.me_collect);
		moreLayout = (RelativeLayout) v.findViewById(R.id.top_right);
		moreLayout.setOnClickListener(this);
		moreLayout.setVisibility(View.GONE);
		backLayout = (RelativeLayout) v.findViewById(R.id.top_left);
		backLayout.setOnClickListener(this);
	}

	/**
	 * 添加最新的活动到列表的第一项
	 */
	// private void addLatestItemByGid(String gid) {
	// CEvent event = EventDBManager.getEventByGid(mActivity, gid);
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
	 * @param v
	 * @param inflater
	 */
	private void initListView(View v, LayoutInflater inflater) {
		mListView = (PullRefreshListView) v.findViewById(R.id.author_listview);
		mListView.setOnItemClickListener(this);
		mListView.setTopHeadHeight(0);

		mListView.setBottomFooterHeight((int) (15 * getResources().getDisplayMetrics().density / 1.5f));
		mSubjectAdapter = new SubjectListAdapter(mActivity, new SupportOnclick());
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
		mLoadProgress = (LoadingProgress) v.findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
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
	 *
	 * @param clearAdapter 请求之前是否先清空UI
	 */
	public void reRequestData(boolean clearAdapter) {
		// 初始化loading(正在加载...)
		// 重新初始化请求页面
		page = 0;
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
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
		BusinessUtils.getPublishSubjectListData(mActivity, page, new PublishCallbackData(this));
	}

	/**
	 * 点赞
	 */
	private class SupportOnclick implements IOnclickListener {

		@Override
		public void onClick(View v, int position, TextView numTextV) {
			if ("true".equals(v.getTag().toString())) {
				UiHelper.showToast(mActivity, "您已经赞了,不能再赞了");
				return;
			}
			if (!AppConfig.getInstance().isLogged) {
				Utils.requestLoginOrRegister(mActivity, "点赞需要先登录", Constants.REQUEST_CODE_LOGIN);
				return;
			}
			// 先UI更新
			v.setSelected(true);
			v.setTag("true");
			BusinessUtils.support(mActivity, new SupportCallbackData(MePublishFragment.this),
					Integer.parseInt((String) mSubjectAdapter.getData().get(position).get("id")));
			int supportNum = Integer.parseInt((String) mSubjectAdapter.getData().get(position).get("support"));
			mSubjectAdapter.getData().get(position).put("support", String.valueOf(supportNum + 1));
			numTextV.setText(String.valueOf(supportNum + 1));
		}
	}

	/**
	 * 本地缓存数据加载
	 */
	private class LoadCacheDataTask extends BaseRunnable {

		@Override
		public void runImpl() {
			EvtLog.d(TAG, "LoadCacheDataTask loading local data start");
			Message msg = new Message();
			msg.what = MsgTypes.MSG_PUBLISH_LIST_SUCCESS;
			Object[] objects = new Object[]{true,
					DatabaseUtils.getCollectListSubjectInfos(UserInfoConfig.getInstance().id, SubjectInfo.NO_COLLECTED, SubjectInfo.PUBLISHED)};
			msg.obj = objects;
			sendMsg(msg);
			EvtLog.d(TAG, "LoadCacheDataTask loading local data end");

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
	private static class PublishCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragment> mFragment;

		public PublishCallbackData(BaseFragment fragment) {
			mFragment = new WeakReference<BaseFragment>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "PublishCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_PUBLISH_LIST_SUCCESS;
					Object[] objects = new Object[]{isRefresh,
							JSONParser.parseSingleInMulti((JSONArray) result, new String[]{""})};
					msg.obj = objects;
					// 下次请求的页面数
					page++;
					MePublishFragment fragment = (MePublishFragment) mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
					if (isRefresh) {
						DatabaseUtils.saveCollectListSubjectInfos((List<Map<String, Object>>) objects[1], UserInfoConfig.getInstance().id,
								SubjectInfo.NO_COLLECTED, SubjectInfo.PUBLISHED);
					}
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_PUBLISH_LIST_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				MePublishFragment fragment = (MePublishFragment) mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}
		}

	}

	/**
	 * 点赞处理回调 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class SupportCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragment> mFragment;

		public SupportCallbackData(BaseFragment fragment) {
			mFragment = new WeakReference<BaseFragment>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "SupportCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_SUPPORT_SUCCESS;
					// // 下次请求的页面数
					// CommutityFragment fragment = (CommutityFragment)
					// mFragment
					// .get();
					// // 如果fragment未回收，发送消息
					// if (fragment != null)
					// fragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				// msg.what = MsgTypes.GET_USER_INFO_FAILED;
				// if (TextUtils.isEmpty(errorMsg)) {
				// errorMsg = Constants.NETWORK_FAIL;
				// }
				// msg.obj = errorMsg;
				// CommutityFragment fragment = (CommutityFragment) mFragment
				// .get();
				// // 如果fragment未回收，发送消息
				// if (fragment != null)
				// fragment.sendMsg(msg);
			}
		}

	}

}
