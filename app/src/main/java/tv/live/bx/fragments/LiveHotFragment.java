package tv.live.bx.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.activities.FanDetailActivity;
import tv.live.bx.activities.GroupPostDetailActivity;
import tv.live.bx.activities.MyFocusActivity;
import tv.live.bx.activities.WebViewActivity;
import tv.live.bx.adapters.LiveHotAdapter;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Consts;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.model.AnchorBean;
import tv.live.bx.ui.EmptyRecyclerView;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.drawables.LiveHotDividerDrawable;
import tv.live.bx.ui.widget.CustomRefreshLayout;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;
import com.wxy.adbanner.entity.AdInfo;

import org.json.JSONArray;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import cn.efeizao.feizao.ui.dialog.CustomDialogBuilder;

/**
 * @author Live
 * @version 2.4.0 2016.4.22
 * @title LiveHotFragment.java Description:主播列表页"最热主播列表"
 */
public class LiveHotFragment extends BaseFragment implements OnClickListener {
	private static final int MSG_SHOW_NEW_MESSAGE_TIP = 0x12;
	//热门数据
	private List<AnchorBean> mHotList = new ArrayList<>();
	//Banner 横幅数据列表
	private List<Map<String, String>> mAdInfoList;

	//热门主播列表5分钟提示更新
	private int HOT_LIST_UPDATE_TIME = 5 * 60 * 1000;

	/**
	 * 扩展了EmptyView的RecycleView
	 */
	private CustomRefreshLayout refreshLayout;
	private EmptyRecyclerView recyclerView;
	private LinearLayoutManager layoutManager;
	private DividerItemDecoration dividerItemDecoration;
	//recycleView adapter
	private LiveHotAdapter mHotAdapter;
	/**
	 * 获取最新页面时，page为0，page以此累加
	 */
	private static int page = 0;

	/**
	 * 加载更多FootViews
	 */
	private ListFooterLoadView mListFooterLoadView;

	/**
	 * 加载loading
	 */
	private LoadingProgress mLoadProgress;


	/**
	 * 提示刷新控件
	 */
	private LinearLayout mNewMessageTipLayout;
	private String mQQ;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_live_hot;
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
		mNewMessageTipLayout.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle bundle) {
		requestData(false);

	}

	/**
	 * 切换tab，也更新数据 TODO 简单描述该方法的实现功能（可选）.
	 *
	 * @see tv.live.bx.fragments.BaseFragment#onTabSelected()
	 */
	@Override
	protected void onTabSelected() {
		super.onTabSelected();
		if (mHotAdapter != null && mHotAdapter.getItemCount() == 0) {
			requestData(false);
		}

	}

	@Override
	public void onTabClickAgain() {
		// 双击底部tab 回调到此进行刷新(会掉到ParentFragment，在回调到ChirldrenFragment)
		if (refreshLayout != null)
			refreshLayout.pullToRefresh();
	}

	/**
	 * 初始化UI控件
	 */
	private void initUI(View v, LayoutInflater inflater) {
		//refreshLayout init
		refreshLayout = (CustomRefreshLayout) v.findViewById(R.id.refresh_layout);
		refreshLayout.setOnRefreshListener(onRefreshListener);
		mListFooterLoadView = (ListFooterLoadView) inflater.inflate(R.layout.a_common_list_footer_loader_view, null);
		refreshLayout.setFooterHintView(mListFooterLoadView);

		//recycleView init
		recyclerView = (EmptyRecyclerView) v.findViewById(R.id.recyclerView);
		layoutManager = new LinearLayoutManager(getActivity());
		recyclerView.setLayoutManager(layoutManager);
		dividerItemDecoration = new DividerItemDecoration(getActivity(), layoutManager.getOrientation());
		dividerItemDecoration.setDrawable(new LiveHotDividerDrawable());
		recyclerView.addItemDecoration(dividerItemDecoration);
		mHotAdapter = new LiveHotAdapter(getActivity());
		mHotAdapter.setFooterView(mListFooterLoadView);
		mHotAdapter.setOnItemClick(onItemClickListener);
		recyclerView.setAdapter(mHotAdapter);

		mNewMessageTipLayout = (LinearLayout) v.findViewById(R.id.new_message_tip_layout);

		initProgressView(v);
	}


	//refresh listener
	private CustomRefreshLayout.OnRefreshListener onRefreshListener = new CustomRefreshLayout.OnRefreshListener() {
		@Override
		public void onRefresh() {
			requestData(false);
		}

		@Override
		public void onLoadMore() {
			page++;
			requestPlazaData(page);
		}
	};

	//---itemClick-----
	private LiveHotAdapter.OnItemClickListener onItemClickListener = new LiveHotAdapter.OnItemClickListener() {
		@Override
		public void onInnerClick(int type, int pos) {
			switch (type) {
				case LiveHotAdapter.CLICK_TOTAL:
					MobclickAgent.onEvent(FeizaoApp.mConctext, "clickBbroadcasterSImgInFeatureTab");
					OperationHelper.onEvent(FeizaoApp.mConctext, "clickBbroadcasterSImgInFeatureTab", null);
					AnchorBean bean = mHotList.get(pos);
					Map<String, Object> lmItem = new HashMap<>();
					lmItem.put("rid", String.valueOf(bean.rid));
					lmItem.put("videoPlayUrl", bean.videoPlayUrl);
					lmItem.put("headPic", bean.headPic);
					ActivityJumpUtil.toLiveMediaPlayerActivity(mActivity, lmItem);
					break;
				case LiveHotAdapter.CLICK_TYPE:
					AnchorBean bean2 = mHotList.get(pos);
					ActivityJumpUtil.toLiveTypeActivity(mActivity, String.valueOf(bean2.tags.id),
							bean2.tags.name);
					break;
				case LiveHotAdapter.CLICK_BANNER:
					bannerOnClick(pos);
					break;
			}
		}
	};

	/**
	 * 初始化下拉刷新ListView
	 *
	 * @param v
	 */
	private void initProgressView(View v) {
		// 设置默认图片
		mLoadProgress = (LoadingProgress) v.findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				// 重新加载数据
				requestData(false);
			}

			@Override
			public void onClick(View v) {
				mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				requestData(false);
			}
		});
		recyclerView.setEmptyView(mLoadProgress);
	}

	/**
	 * 重新请求数据
	 *
	 * @param clearAdapter 请求之前是否先清空UI
	 */
	private void requestData(boolean clearAdapter) {
		BusinessUtils.getMainBanners(mActivity, new BannerCallbackData(LiveHotFragment.this));
		// 初始化loading(正在加载...)
		// 重新初始化请求页面
		page = 0;
		if (mHotAdapter == null || mHotAdapter.getItemCount() == 0)
			mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
//		 清空界面数据
		if (clearAdapter && mHotAdapter != null) {
			mHotAdapter.clear();
		}
		requestPlazaData(page);
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page) {
		BusinessUtils.getAuthorListData(mActivity, page, null, LiveFragment.LIVE_STATUS_HOT, 0, new AuthorCallbackData(
				this));
	}

	/**
	 * 初始化广告横幅数据
	 */
	private void initBannerData(List<Map<String, String>> banners) {
		Iterator<Map<String, String>> iterators = banners.iterator();
		ArrayList<AdInfo> mAdInfoList = new ArrayList<>();
		while (iterators.hasNext()) {
			Map<String, String> bannerInfo = iterators.next();
			AdInfo mAdInfo = new AdInfo();
			mAdInfo.setAdvImg(bannerInfo.get("pic"));
			mAdInfo.setAdvType(Integer.parseInt(bannerInfo.get("type")));

			int liBannerType = Integer.parseInt(bannerInfo.get("type"));
			if (liBannerType == Consts.BANNER_URL_TYPE_PAGE) {
				mAdInfo.setAdvLink(bannerInfo.get("banner_info"));
			} else {
				mAdInfo.setAdvLink(bannerInfo.get("room_info"));
			}
			mAdInfoList.add(mAdInfo);
		}
		mHotAdapter.setBannerData(mAdInfoList);

	}

	/**
	 * 横幅列表点击实现方法 banner跳转新增两种类型 type=3 跳转饭圈详情页 type=4跳转饭圈贴子
	 */
	private void bannerOnClick(int position) {
		EvtLog.d(TAG, "bannerOnClick position:" + position);
		Map<String, String> lmPageInfo = mAdInfoList.get(position);
		int liBannerType = Integer.parseInt(lmPageInfo.get("type"));
		switch (liBannerType) {
			case Consts.BANNER_URL_TYPE_PAGE:
				ActivityJumpUtil.gotoActivity(mActivity, WebViewActivity.class, false, WebViewActivity.WEB_INFO,
						(Serializable) lmPageInfo);
				break;
			case Consts.BANNER_URL_TYPE_PLAYER:
				ActivityJumpUtil.toLiveMediaPlayerActivity(mActivity, lmPageInfo);
				break;
			case Consts.BANNER_URL_TYPE_GROUP:
				lmPageInfo.put("id", lmPageInfo.get("groupId"));
				ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class, false, FanDetailActivity.FAN_INFO,
						(Serializable) lmPageInfo);
				break;
			case Consts.BANNER_URL_TYPE_POST_DETAIL:
				lmPageInfo.put("id", lmPageInfo.get("postId"));
				ActivityJumpUtil.gotoActivity(mActivity, GroupPostDetailActivity.class, false, "subjectInfo",
						(Serializable) lmPageInfo);
				break;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mHotAdapter.clear();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			/** 我关注的主播列表Layout */
			case R.id.focus_layout:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "clickmyLovelist");
				ActivityJumpUtil.gotoActivity(mActivity, MyFocusActivity.class, false, null, null);
				break;
			case R.id.new_message_tip_layout:
				layoutManager.scrollToPosition(0);
				mNewMessageTipLayout.setVisibility(View.GONE);
				if (refreshLayout != null)
					refreshLayout.pullToRefresh();
				break;
		}
	}

	/**
	 * 弹出对话框
	 */
	private void showDialog() {
		CustomDialogBuilder dialogBuilder = new CustomDialogBuilder(mActivity, R.layout.dialog_recommend);
		((TextView) dialogBuilder.findViewById(R.id.dialog_recommend_content2)).setText("QQ:" + mQQ);
		dialogBuilder.setOnClickListener(R.id.dialog_recommend_confrim, new OnClickListener() {
			@Override
			public void onClick(View view) {

			}
		});
		Dialog dialog = dialogBuilder.showDialog();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
	}

	/**************************************
	 * 回调事件处理
	 *********************************************/

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.MSG_LOAD_FAILED:
				if (mHotAdapter.getItemCount() == 0) {
					String text = mActivity.getString(R.string.a_loading_failed);
					mLoadProgress.Failed(text, 0);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
					mLoadProgress.Hide();
					mListFooterLoadView.onLoadingFailed();
				}
				if (page == 0)
					refreshLayout.onRefreshComplete();
				if (page > 0) {
					page--;
					refreshLayout.onLoadingComplete(false, true);
				}
				break;

			case MsgTypes.MSG_LOAD_SUCCESS:
				List<AnchorBean> tmp = AnchorBean.parseAnchorList((JSONArray) msg.obj);
				if (page == 0) {
					mHotList.clear();
					mNewMessageTipLayout.setVisibility(View.GONE);
//					mHandler.removeMessages(MSG_SHOW_NEW_MESSAGE_TIP);
//					mHandler.sendEmptyMessageDelayed(MSG_SHOW_NEW_MESSAGE_TIP, HOT_LIST_UPDATE_TIME);
					refreshLayout.onRefreshComplete();
				} else {
					refreshLayout.onLoadingComplete(true, tmp.size() == 0);
				}
				mHotList.addAll(tmp);
				mHotAdapter.setHotData(mHotList);
				// 设置没有数据的EmptyView,如果listView没有数据，EmptyView的Visible会被直接设置为gone
				String text = mActivity.getString(R.string.a_list_data_empty);
				mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
				break;
			case MsgTypes.GET_MAIN_BANNERS_SUCCESS:// 获取Banner
				mAdInfoList = (List<Map<String, String>>) msg.obj;
				initBannerData(mAdInfoList);
				break;
			case MSG_SHOW_NEW_MESSAGE_TIP:
				// 屏蔽
				mNewMessageTipLayout.setVisibility(View.GONE);
				break;

		}
	}

	/**
	 * 主播列表数据处理回调
	 */
	private static class AuthorCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragment> mFragment;

		public AuthorCallbackData(BaseFragment fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = Message.obtain();
			if (success) {
				msg.what = MsgTypes.MSG_LOAD_SUCCESS;
				msg.obj = result;
				BaseFragment authorFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (authorFragment != null)
					authorFragment.sendMsg(msg);
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

	/**
	 * 横幅数据处理回调
	 */
	private static class BannerCallbackData implements CallbackDataHandle {
		private final WeakReference<BaseFragment> mFragment;

		public BannerCallbackData(BaseFragment fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "BannerCallbackData success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MsgTypes.GET_MAIN_BANNERS_SUCCESS;
					msg.obj = JSONParser.parseMulti((JSONArray) result);
					// 如果fragment未回收，发送消息
					if (mFragment.get() != null)
						mFragment.get().sendMsg(msg);
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
}
