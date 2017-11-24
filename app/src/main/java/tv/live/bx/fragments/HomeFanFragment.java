package tv.live.bx.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.activities.AddFanActivity;
import tv.live.bx.activities.CreateFanActivity;
import tv.live.bx.activities.FanDetailActivity;
import tv.live.bx.activities.GroupPostDetailActivity;
import tv.live.bx.activities.GroupPostPublishActivity;
import tv.live.bx.activities.LoginActivity;
import tv.live.bx.activities.MeFanActivity;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.MeFanAdapter;
import tv.live.bx.adapters.NewSubjectListAdapter;
import tv.live.bx.adapters.NewSubjectListAdapter.IOnclickListener;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.callback.MyUserInfoCallbackDataHandle;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.HtmlTagHandler;
import tv.live.bx.ui.ActionSheetDialog;
import tv.live.bx.ui.ActionSheetDialog.OnSheetItemClickListener;
import tv.live.bx.ui.ActionSheetDialog.SheetItemColor;
import tv.live.bx.ui.HorizontalListView;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.PullRefreshListView;
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
 * Title: HomeFanFragment.java</br>
 *
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class HomeFanFragment extends BaseFragment implements OnClickListener, OnItemClickListener {

	private static final int REQUEST_CODE_POST_DETAIL = 0x13;
	protected static final int REQUEST_CODE_CREATE_FAN = 0x14;
	private RelativeLayout mAddFanBtn;
	private RelativeLayout mCreateFanBtn;

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

	private PullRefreshListView mListView;
	private NewSubjectListAdapter mSubjectAdapter;
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
	private TextView mHeadAll;
	private HorizontalListView mHorizontalListView;
	private MeFanAdapter mMeFanAdapter;

	/**
	 * 用户相关数据
	 */
//	private Map<String, String> mmUserInfo = null;

	/**
	 * 点击下拉按钮弹出的对话框
	 */
	private ActionSheetDialog actionSheetDialog;

	private AlertDialog mProgress;

	private static String GUIDE_FAN = "guide_fan";

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_home_fan_layout;
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
		mAddFanBtn.setOnClickListener(this);
		mCreateFanBtn.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle bundle) {
		// 如果已登录
		// 加载Banner,anchor列表
		reRequestAllData();
		if (Utils.getBooleanFlag(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, GUIDE_FAN,
				Constants.COMMON_TRUE))) {
			((BaseFragmentActivity) mActivity).showFullDialog(R.layout.dialog_guide_fan_layout,
					new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
							Utils.setCfg(mActivity, Constants.COMMON_SF_NAME, GUIDE_FAN, "false");
						}
					});
		}
	}

	@Override
	public void onDestroy() {
		dismissProgressDialog();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.head_all:
				ActivityJumpUtil.gotoActivity(mActivity, MeFanActivity.class, false, null, null);
				break;
			case R.id.add_fanquan:
				ActivityJumpUtil.gotoActivityForResult(mActivity, AddFanActivity.class, REQUEST_CODE_FLUSH_FRAGMENT, null,
						null);
				break;
			case R.id.create_fanquan:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "createFanInCommunity");
				mProgress = Utils.showProgress(mActivity);
				BusinessUtils.getMyUserInfo(mActivity, new MyUserInfoCallbackDataHandle(mHandler));
				// UiHelper.showToast(mActivity, R.string.commutity_fan_create_tip);
				break;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position - mListView.getHeaderViewsCount() < 0) {
			return;
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> lmItem = (Map<String, Object>) mSubjectAdapter.getItem(position
				- mListView.getHeaderViewsCount());
		EvtLog.d(TAG, "lmItem " + lmItem.toString());
		ActivityJumpUtil.gotoActivityForResult(mActivity, GroupPostDetailActivity.class, REQUEST_CODE_POST_DETAIL,
				"subjectInfo", (Serializable) lmItem);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_LOGIN " + resultCode);
			if (resultCode == LoginActivity.RESULT_CODE_OK) {

			}
		} else if (requestCode == REQUEST_CODE_FLUSH_FRAGMENT) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_FLUSH_FRAGMENT " + resultCode);
			// 重新加载数据
			BusinessUtils.getMeGroup(mActivity, 0, null, new FanRecommentCallback(this));
		} else if (requestCode == REQUEST_CODE_POST_DETAIL) {
			if (resultCode == Activity.RESULT_OK) {
				// 重新加载数据
				reRequestData(false);
			}
		} else if (requestCode == REQUEST_CODE_CREATE_FAN) {
			if (resultCode == Activity.RESULT_OK) {
				BusinessUtils.getMeGroup(mActivity, 0, null, new FanRecommentCallback(this));
			}
		} else if (requestCode == GroupPostPublishActivity.REQUEST_CODE_PUBLIC_FRAGMENT) {
			// 重新加载数据
			reRequestData(false);
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.MSG_GROUP_POST_LIST_FAILED:
				mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
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
				Object[] objects = (Object[]) msg.obj;
				boolean isRefreh = (Boolean) objects[0];
				List<Map<String, Object>> mData = (List<Map<String, Object>>) objects[1];
				if (isRefreh) { // 初始化或者下拉刷新模式
					mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
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

			case MsgTypes.MSG_FAN_ME_LIST_SUCCESS:
				mMeFanAdapter.clearData();
				mMeFanAdapter.addData((List<Map<String, Object>>) msg.obj);
				break;
			case MsgTypes.MSG_DELETE_POST_SUCCESS:
				Bundle bundle2 = msg.getData();
				int position = bundle2.getInt("position");
				// 如果删除成功，把数据更新listview
				mSubjectAdapter.getData().remove(position);
				mSubjectAdapter.notifyDataSetChanged();

				UiHelper.showToast(mActivity, "删除成功");
				break;
			case MsgTypes.MSG_DELETE_POST_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;
			case MsgTypes.GET_MY_USER_INFO_SUCCESS:
				dismissProgressDialog();
//				Utils.setCfg(mActivity, Constants.USER_SF_NAME, mmUserInfo);
				if (UserInfoConfig.getInstance().groupCreateNumLeft == 0) {
					UiHelper.showToast(mActivity, R.string.commutity_fan_created);
				} else {
					ActivityJumpUtil.gotoActivityForResult(mActivity, CreateFanActivity.class, REQUEST_CODE_CREATE_FAN,
							null, null);
				}
				break;
			case MsgTypes.GET_MY_USER_INFO_FAILED:
				dismissProgressDialog();
				Bundle bundle = msg.getData();
				UiHelper.showToast(mActivity, bundle.getString("errorMsg"));
				break;
		}

	}

	@Override
	public void onTabClickAgain() {
		if (mListView != null) {
			mListView.setSelection(0);
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
		// if (mListView != null) {
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
		mHeadLayout = (LinearLayout) inflater.inflate(R.layout.fragment_home_fan_head, null);
		initHeadLayout();

		initListView(v, inflater);
	}

	private void initHeadLayout() {
		mHeadAll = (TextView) mHeadLayout.findViewById(R.id.head_all);
		mHeadAll.setOnClickListener(this);
		mAddFanBtn = (RelativeLayout) mHeadLayout.findViewById(R.id.add_fanquan);
		mCreateFanBtn = (RelativeLayout) mHeadLayout.findViewById(R.id.create_fanquan);
		mHorizontalListView = (HorizontalListView) mHeadLayout.findViewById(R.id.tariler_listview);
		mHorizontalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Map<String, Object> item = (Map<String, Object>) mMeFanAdapter.getItem(position);
				if (Constants.FAN_STATUS_NORMAL.equals(item.get("status"))) {
					ActivityJumpUtil.gotoActivityForResult(mActivity, FanDetailActivity.class,
							REQUEST_CODE_FLUSH_FRAGMENT, FanDetailActivity.FAN_INFO, (Serializable) item);
				} else if (Constants.FAN_STATUS_FREEZE.equals(item.get("status"))) {
					UiHelper.showToast(mActivity,
							mActivity.getResources().getString(R.string.commutity_fan_activate_tip));
				}
			}
		});

		mMeFanAdapter = new MeFanAdapter(mActivity);
		mHorizontalListView.setAdapter(mMeFanAdapter);
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
		mListView.addHeaderView(mHeadLayout);
		mListView.setBottomFooterHeight((int) (15 * getResources().getDisplayMetrics().density / 1.5f));
		mListView.setOnItemClickListener(this);

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
				// 加载Banner,anchor列表
				reRequestAllData();
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
		BusinessUtils.getFanPostList(mActivity, null, page, "last_reply_time", new GetPostListCallbackData(this));
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
		BusinessUtils.getMeGroup(mActivity, 0, null, new FanRecommentCallback(this));
		reRequestData(false);
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
													new DeletePostCallbackData(HomeFanFragment.this, position));
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
	 * 实现帖子列表项中控件的响应
	 */
	class OnClick implements IOnclickListener {

		@Override
		public void onClick(View v, int position, View statusView) {
			switch (v.getId()) {
				// 点击头像进入个人中心
				case R.id.item_photo:
					Map<String, String> personInfo = new HashMap<>();
					personInfo.put("id", (String) mSubjectAdapter.getData().get(position).get("uid"));
					ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
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
					msg.what = MsgTypes.MSG_FAN_ME_LIST_SUCCESS;
					List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
					JSONArray myGroup = ((JSONObject) result).getJSONArray("myGroups");
					JSONArray myJoinedGroup = ((JSONObject) result).getJSONArray("myJoinedGroups");
					if (myGroup != null && myGroup.length() > 0) {
						data.addAll(JSONParser.parseSingleInMulti(myGroup, new String[]{""}));
					}
					if (myJoinedGroup != null && myJoinedGroup.length() > 0) {
						data.addAll(JSONParser.parseSingleInMulti(myJoinedGroup, new String[]{""}));
					}
					msg.obj = data;
					BaseFragment fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);

				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_FAN_ME_LIST_FAILED;
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

		private final WeakReference<BaseFragment> mFragment;
		private int position;

		public DeletePostCallbackData(BaseFragment fragment, int position) {
			mFragment = new WeakReference<BaseFragment>(fragment);
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
					BaseFragment activity = mFragment.get();
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
				BaseFragment activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

}
