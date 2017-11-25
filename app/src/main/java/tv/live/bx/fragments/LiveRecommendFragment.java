package tv.live.bx.fragments;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.NetworkImageHolderView;
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
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.FanDetailActivity;
import tv.live.bx.activities.GroupPostDetailActivity;
import tv.live.bx.activities.WebViewActivity;
import tv.live.bx.adapters.LiveRecommendListAdapter;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Consts;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.database.DatabaseUtils;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.model.AnchorBean;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.widget.CustomRefreshLayout;
import tv.live.bx.ui.widget.HeaderFooterGridView;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;


/**
 * Created by BYC on 2017/5/22.
 */

public class LiveRecommendFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
	// 自动轮播的时间间隔
	private final static int TIME_INTERVAL = 5 * 1000;

	/**
	 * 刷新信息的时候，page为0；下拉加载更多时，page依次累加
	 */
	private int page = 0;

	/***
	 * 主播列表
	 */
	private CustomRefreshLayout refreshLayout;
	private HeaderFooterGridView mGridView;
	private LiveRecommendListAdapter mRecommendAdapter;


	/**
	 * 加载loading
	 */
	private LoadingProgress mLoadProgress;
	/**
	 * Banner
	 */
	private LinearLayout mAdBanner;
	private ConvenientBanner mBanner;       //横幅控件
	private TextView tv_title;
	/**
	 * Banner 横幅数据列表
	 */
	private List<Map<String, String>> mAdInfoList;

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_live_recommed;
	}

	@Override
	protected void initMembers() {
		initBannerView();
		initProgressView();
		initGridView();
	}

	/**
	 * 初始化
	 */
	private void initBannerView() {
		mAdBanner = (LinearLayout) mInflater.inflate(R.layout.a_main_banner_layout, null);
		mBanner = (ConvenientBanner) mAdBanner.findViewById(R.id.convenientBanner);
		tv_title = (TextView) mAdBanner.findViewById(R.id.tv_title);
		tv_title.setText("推荐主播");
		// 设置横幅底部引导图片，并启动自动播放
		mBanner.setPageIndicator(new int[]{
				R.drawable.icon_circle_focus_off, R.drawable.icon_circle_focus_on
		}).startTurning(TIME_INTERVAL);
		// 设置包含Banner的RelativeLayout大小
		int liLayoutWidth = Utils.getScreenWH(mActivity)[0];
		int liLayoutHeight = liLayoutWidth * 442 / 1080;
		ViewGroup.LayoutParams loLayoutParams = mBanner.getLayoutParams();
		loLayoutParams.width = liLayoutWidth;
		loLayoutParams.height = liLayoutHeight;
		mBanner.setLayoutParams(loLayoutParams);

	}

	private void initProgressView() {
		// 设置默认图片
		mLoadProgress = (LoadingProgress) mRootView.findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new LoadingProgress.onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				// 重新加载数据
				reRequestData(false);
			}

			@Override
			public void onClick(View v) {
				mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				reRequestData(true);
			}
		});
	}

	private void initGridView() {
		refreshLayout = (CustomRefreshLayout) mRootView.findViewById(R.id.refresh_layout);
		mGridView = (HeaderFooterGridView) mRootView.findViewById(R.id.gridView_anchor);

		mRecommendAdapter = new LiveRecommendListAdapter(mActivity);
		mGridView.addHeaderView(mAdBanner);
		mGridView.setAdapter(mRecommendAdapter);

		refreshLayout.setOnRefreshListener(new CustomRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				OperationHelper.onEvent(FeizaoApp.mConctext, "refreshInRecommendPageOfIndex", null);
				// 加载Banner,anchor列表
				reRequestData(false);
			}

			@Override
			public void onLoadMore() {
				requestPlazaData(page);
			}
		});
		mGridView.setOnItemClickListener(this);
	}

	@Override
	protected void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {

	}

	@Override
	protected void initData(Bundle bundle) {
		reRequestData(true);
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mBanner.stopTurning();
	}

	/**
	 * 重新请求数据
	 *
	 * @param clearAdapter 请求之前是否先清空UI
	 */
	private void reRequestData(boolean clearAdapter) {
		BusinessUtils.getMainBanners(mActivity, new BannerCallbackData(LiveRecommendFragment.this));
		// 初始化loading(正在加载...)
		// 重新初始化请求页面
		page = 0;
		// mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
		// 清空界面数据
		if (clearAdapter) {
			mRecommendAdapter.clear();
			mRecommendAdapter.notifyDataSetChanged();
			changeBannerShowStatus();
		}
		requestPlazaData(page);
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page) {
		BusinessUtils.getAuthorListData(mActivity, page, null, LiveFragment.LIVE_STATUS_RECOMMEND, 0, new AuthorCallbackData(
				this));
//		BusinessUtils.getAuthorListData(mActivity, page, null, LiveFragment.LIVE_STATUS_RECOMMEND, 0, new AuthorCallbackData(
//				this));
	}

	/**
	 * 初始化广告横幅
	 */
	private void initAdBannerView(List<Map<String, String>> banners) {
		if (banners == null)
			return;
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
		//适配banner图片资源以及item点击事件
		mBanner.setPages(new CBViewHolderCreator() {
			@Override
			public Object createHolder() {
				return new NetworkImageHolderView();
			}
		}, mAdInfoList)
				.setOnItemClickListener(new com.bigkoo.convenientbanner.listener.OnItemClickListener() {
					@Override
					public void onItemClick(int position) {
						MobclickAgent.onEvent(FeizaoApp.mConctext, "clickbannerInIndex");
						OperationHelper.onEvent(FeizaoApp.mConctext, "clickbannerInIndex", null);
						bannerOnClick(position);
					}
				});

		mRecommendAdapter.notifyDataSetChanged();
	}

	/**
	 * 横幅列表点击实现方法 banner跳转新增两种类型 type=3 跳转饭圈详情页 type=4跳转饭圈贴子
	 */
	private void bannerOnClick(int position) {
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
	protected void onTabSelected() {
		super.onTabSelected();
	}

	@Override
	public void onTabClickAgain() {
		super.onTabClickAgain();
		if (refreshLayout != null)
			refreshLayout.pullToRefresh();
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		OperationHelper.onEvent(FeizaoApp.mConctext, "clickBbroadcasterSImgInRecommendTab", null);
		int pos = position - mGridView.getHeaderViewCount() * mGridView.getNumColumns();
		if(pos >= mRecommendAdapter.getCount())
			return;
		AnchorBean bean = (AnchorBean) mRecommendAdapter.getItem(pos);
		Map<String, Object> lmItem = new HashMap<>();
		lmItem.put(AnchorBean.RID, String.valueOf(bean.rid));
		lmItem.put(AnchorBean.VIDEOPLAYURL, bean.videoPlayUrl);
		lmItem.put(AnchorBean.HEAD_PIC, bean.headPic);
		ActivityJumpUtil.toLiveMediaPlayerActivity(mActivity, lmItem);
	}

	/**
	 * 改变banner的显示状态
	 */
	private void changeBannerShowStatus() {
		if (mRecommendAdapter.getCount() == 0 || mAdInfoList == null || mAdInfoList.isEmpty()) {
			mAdBanner.setVisibility(View.GONE);
		} else {
			mAdBanner.setVisibility(View.VISIBLE);
		}
	}

	/**************************************
	 * 回调事件处理
	 *********************************************/

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.MSG_LOAD_FAILED:
				if (page == 0) {
					// 收起正在刷新HeaderView
					refreshLayout.onRefreshComplete();
				}else {
				   refreshLayout.onLoadingComplete(false, false);
				}

				if (mRecommendAdapter.isEmpty()) {
					String text = mActivity.getString(R.string.a_loading_failed);
					mLoadProgress.Failed(text, 0);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
					mLoadProgress.Hide();
				}
				changeBannerShowStatus();
				break;

			case MsgTypes.MSG_LOAD_SUCCESS:
				JSONArray jsonArray = (JSONArray) msg.obj;
				List<AnchorBean> anchors = AnchorBean.parseAnchorList(jsonArray);
				refreshLayout.onRefreshComplete(); // 收起正在刷新HeaderView
				if (page == 0) {
					mRecommendAdapter.clear();
					mRecommendAdapter.addItems(anchors);
				} else {
					//加载更多的模式
					mRecommendAdapter.addItems(anchors);
					refreshLayout.onLoadingComplete(true, anchors.size() == 0);
				}
				changeBannerShowStatus();
				// 设置没有数据的EmptyView
				if (mRecommendAdapter.getCount() == 0) {
					String text = mActivity.getString(R.string.a_list_data_empty);
					mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
				} else
					mLoadProgress.Succeed(0);

				page++;
				break;
			case MsgTypes.GET_MAIN_BANNERS_SUCCESS:// 获取Banner
				mAdInfoList = (List<Map<String, String>>) msg.obj;
				changeBannerShowStatus();
				initAdBannerView(mAdInfoList);
				break;
			case MsgTypes.GET_MAIN_BANNERS_FAILED:
				changeBannerShowStatus();
				break;
		}
	}

	/**
	 * 主播列表数据处理回调 ClassName: BannerCallbackData <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class AuthorCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragment> mFragment;

		public AuthorCallbackData(BaseFragment fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "AuthorCallbackData success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_LOAD_SUCCESS;
					msg.obj = result;
					// 下次请求的页面数
					BaseFragment authorFragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (authorFragment != null)
						authorFragment.sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
				} catch (Exception e) {
				}
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
	 * 横幅数据处理回调 ClassName: BannerCallbackData <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
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
}
