/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package tv.live.bx.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshZoomListView;
import com.handmark.pulltorefresh.library.PullToZoomListView;
import com.handmark.pulltorefresh.library.PullToZoomListView.OnHeadHeightListener;
import com.handmark.pulltorefresh.library.PullToZoomListView.OnRefreshListener;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.FanMenberAdapter;
import tv.live.bx.adapters.NewSubjectListAdapter;
import tv.live.bx.adapters.NewSubjectListAdapter.IOnclickListener;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.common.WebConstants;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.fragments.LiveChatFragment;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.HtmlTagHandler;
import tv.live.bx.listeners.OnDoubleClickListener;
import tv.live.bx.ui.ActionSheetDialog;
import tv.live.bx.ui.ActionSheetDialog.OnSheetItemClickListener;
import tv.live.bx.ui.ActionSheetDialog.SheetItemColor;
import tv.live.bx.ui.HorizontalListView;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.PopFanDetailMoreWindow;
import tv.live.bx.ui.PopFanDetailMoreWindow.IMoreItemListener;
import tv.live.bx.ui.StrokeTextView;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;

@SuppressLint("NewApi")
public final class FanDetailActivity extends BaseFragmentActivity implements OnClickListener, OnItemClickListener {

	static final int MENU_MANUAL_REFRESH = 0;
	static final int MENU_DISABLE_SCROLL = 1;
	static final int MENU_SET_MODE = 2;
	static final int MENU_DEMO = 3;

	static final int REQUEST_CODE_GROUP_MENBER = 0x10;
	public static final int EDIT_FAN_INFO = 100;
	public static final String FAN_INFO = "fanInfo";

	private PullToRefreshZoomListView mPullRefreshListView;
	private ListFooterLoadView mListFooterLoadView;
	private NewSubjectListAdapter mSubjectAdapter;

	/**
	 * 目前暂时都刷新
	 */
	private static boolean isRefresh = true;
	/**
	 * 获取最新页面时，page为0，page以此累加
	 */
	private static int page = 0;

	// private LinearLayout mTopLayout;
	private ImageView mTopBackgroup;
	/**
	 * 头部
	 */
	private LinearLayout mHeadLayout1, mHeadLayout2;
	/**
	 * 头部1控件
	 */
	private ImageView mFanLogo;
	private StrokeTextView mFanName, mFanHostName, mFanDetail, mFanHotIndex, mFanPost, mFanMember;

	/**
	 * 头部2控件
	 */
	private HorizontalListView mHorizontalListView;
	private FanMenberAdapter mFanMenberAdapter;
	private ImageView mFanMenberIv;
	private TextView mReplyTip;

	/**
	 * 发送帖子按钮
	 */
	private ImageView mSendPostIv;

	private LayoutInflater inflater;

	/**
	 * 饭圈信息
	 */
	private Map<String, String> mFanInfo;

	/**
	 * 管理员和圈主对帖子有管理权限，包括删除和置顶和加精功能
	 */
	private ActionSheetDialog actionSheetDialog;
	private PopFanDetailMoreWindow mFanDetailMoreWindow;
	/**
	 * [sort] 支持last_reply_time 和 create_time ,默认使用last_reply_time
	 */
	private String mSort = "last_reply_time";

	/**
	 * 头部背景图需要隐藏的高度
	 */
	private int mTopMargin;
	private int mfirstVisibleItem;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.isSystemBarTint = false;
		super.onCreate(savedInstanceState);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_fan_details;
	}

	@Override
	public void initWidgets() {
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			mFanInfo = (Map<String, String>) bundle.getSerializable(FAN_INFO);
		}
		inflater = LayoutInflater.from(getApplicationContext());
		// mTopLayout = (LinearLayout) findViewById(R.id.top_layout);
		mTopBackgroup = (ImageView) findViewById(R.id.top_backgroud);
		mTopBackgroup.setVisibility(View.GONE);
		mTopMargin = (int) (FeizaoApp.metrics.widthPixels * 0.7f - getResources().getDimension(
				R.dimen.layout_top_title_bg_height));
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mTopBackgroup.getLayoutParams();
		layoutParams.topMargin = -mTopMargin;
		layoutParams.height = (int) (FeizaoApp.metrics.widthPixels * 0.7f);
		mTopBackgroup.setLayoutParams(layoutParams);
		mHeadLayout1 = (LinearLayout) inflater.inflate(R.layout.activity_fan_detail_head_one, null);
		mHeadLayout2 = (LinearLayout) inflater.inflate(R.layout.activity_fan_detail_head_two, null);
		mSendPostIv = (ImageView) findViewById(R.id.publicBtn);
		mSendPostIv.setOnClickListener(this);
		if (isAdd()) {
			mSendPostIv.setVisibility(View.VISIBLE);
		}
		initListView();
		initHeadLayout1();
		initHeadLayout2();
	}

	private void initListView() {
		mPullRefreshListView = (PullToRefreshZoomListView) findViewById(R.id.pull_refresh_list);
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
		mSubjectAdapter.setOnClickListener(new ItemOnclick());
		mSubjectAdapter.setIsShowTop(true);
		mSubjectAdapter.setIsShowGroup(false);
		mPullRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<PullToZoomListView>() {
			String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
					DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

			public void onPullDownToRefresh(PullToRefreshBase<PullToZoomListView> refreshView) {
				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.

			}

			public void onPullUpToRefresh(PullToRefreshBase<PullToZoomListView> refreshView) {
				EvtLog.e(TAG, "onPullUpToRefresh");
				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				isRefresh = false;
				requestPlazaData(page, mSort);
			}
		});
		mPullRefreshListView.setOnItemClickListener(this);
		// 设置PullRefreshListView上提加载时的加载提示
		mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载...");
		mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
		mPullRefreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("松开加载更多...");
		// 设置PullRefreshListView下拉加载时的加载提示
		mPullRefreshListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新...");
		mPullRefreshListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新...");
		mPullRefreshListView.getLoadingLayoutProxy(true, false).setReleaseLabel("松开刷新...");
		mPullRefreshListView.setMode(Mode.DISABLED);
		// Add an end-of-list listener
		mPullRefreshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("ssgdsgsd");
			}
		});

		final PullToZoomListView mPullToZoomListView = mPullRefreshListView.getRefreshableView();
		// Need to use the Actual ListView when registering for Context Menu
		// registerForContextMenu(actualListView);

		/**
		 * Add Sound Event Listener
		 */
		// SoundPullEventListener<ListView> soundListener = new
		// SoundPullEventListener<ListView>(this);
		// soundListener.addSoundEvent(State.PULL_TO_REFRESH, R.raw.pull_event);
		// soundListener.addSoundEvent(State.RESET, R.raw.reset_sound);
		// soundListener.addSoundEvent(State.REFRESHING,
		// R.raw.refreshing_sound);
		// mPullRefreshListView.setOnPullEventListener(soundListener);

		// You can also just use setListAdapter(mAdapter) or
		// mPullRefreshListView.setAdapter(mAdapter)
		mPullToZoomListView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				requestAllData();
			}
		});
		mPullToZoomListView.setOnHeadHeightListener(new OnHeadHeightListener() {
			@SuppressLint("NewApi")
			@Override
			public void onZoom(int precent) {
				EvtLog.e(TAG, "onZoom:" + precent);
				float alpha = 1 - precent / 100f;
				mHeadLayout1.setAlpha(alpha);
				// if (precent >= 75) {
				// mTopBackgroup.setImageAlpha((int) (255 * precent / 100f));
				// } else {
				// mTopBackgroup.setImageAlpha(0);
				// }
			}

			@Override
			public void onScrollY(float by) {
				EvtLog.e(TAG, "onScrollY:" + by);
				if (by >= mTopMargin) {
					mTopBackgroup.setVisibility(View.VISIBLE);
					mTopTitleTv.setVisibility(View.VISIBLE);
				} else if (mfirstVisibleItem == 0) {
					mTopBackgroup.setVisibility(View.GONE);
					mTopTitleTv.setVisibility(View.GONE);
				}
			}
		});
		mPullToZoomListView.addHeaderViewLayerImage(mHeadLayout1);
		mPullToZoomListView.addHeaderView(mHeadLayout2);
		// mPullToZoomListView.getHeaderView().setImageResource(R.drawable.bg_fan_detail_head);
		// ImageLoader.getInstance().displayImage(mFanInfo.get("background"),
		// mPullToZoomListView.getHeaderView(),
		// mIOptions);
		mPullToZoomListView.getHeaderView().setScaleType(ImageView.ScaleType.CENTER_CROP);
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
					requestPlazaData(page, mSort);
				}
			}
		});
		mPullToZoomListView.addFooterView(mListFooterLoadView);
		mPullToZoomListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				EvtLog.e(TAG, "firstVisibleItem:" + firstVisibleItem + ",visibleItemCount:" + visibleItemCount);
				mfirstVisibleItem = firstVisibleItem;
				if (firstVisibleItem >= 2) {
					mTopBackgroup.setVisibility(View.VISIBLE);
					mTopTitleTv.setVisibility(View.VISIBLE);
				}
				if (mListFooterLoadView.getParent() == mPullToZoomListView) {
					// 至少翻过一项，才有可能执行加载更过操作
					if (mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_HIDDEN
							&& mPullToZoomListView.getFirstVisiblePosition() > mPullToZoomListView
							.getHeaderViewsCount()) {
						mListFooterLoadView.onLoadingStarted();
						EvtLog.d(TAG, "滚动加载更多");
						isRefresh = false;
						requestPlazaData(page, mSort);
					}
				} else {
					if (mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_FAILED
							|| mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_NOMORE) {
						mListFooterLoadView.hide();
					}
				}
			}
		});
		mPullToZoomListView.setAdapter(mSubjectAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		EvtLog.d(TAG, "onItemClick:position " + position + " mListView.getHeaderViewsCount():"
				+ mPullRefreshListView.getRefreshableView().getHeaderViewsCount());
		if (position - mPullRefreshListView.getRefreshableView().getHeaderViewsCount() < 0) {
			return;
		}
		if (position - mPullRefreshListView.getRefreshableView().getFooterViewsCount() < 0) {
			return;
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> lmItem = (Map<String, Object>) mSubjectAdapter.getItem(position
				- mPullRefreshListView.getRefreshableView().getHeaderViewsCount());
		EvtLog.d(TAG, "lmItem " + lmItem.toString());
		ActivityJumpUtil.gotoActivity(mActivity, GroupPostDetailActivity.class, false, "subjectInfo",
				(Serializable) lmItem);
	}

	private void initHeadLayout1() {
		mFanLogo = (ImageView) findViewById(R.id.item_fanquan_logo);
		mFanName = (StrokeTextView) findViewById(R.id.item_fanquan_nickname);
		mFanHostName = (StrokeTextView) findViewById(R.id.item_fanquan_host_name);
		mFanDetail = (StrokeTextView) findViewById(R.id.item_fanquan_detail);
		mFanHotIndex = (StrokeTextView) findViewById(R.id.item_fanquan_popularity);
		mFanPost = (StrokeTextView) findViewById(R.id.item_fanquan_post);
		mFanMember = (StrokeTextView) findViewById(R.id.item_fanquan_fans);
	}

	private void initHeadLayout2() {
		mHorizontalListView = (HorizontalListView) findViewById(R.id.listview);
		mFanMenberIv = (ImageView) findViewById(R.id.btn_fan_menber);
		// 没有评论数据提示
		mReplyTip = (TextView) findViewById(R.id.item_no_data);
	}

	private void initHeadLayoutData1() {
		if (mFanInfo != null) {
			ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity, mFanLogo, mFanInfo.get("logo"));

			ImageLoaderUtil.with().loadImage(mActivity, mPullRefreshListView.getRefreshableView().getHeaderView(), mFanInfo.get("background"), R.drawable.bg_fan_detail_head, 0);
			ImageLoaderUtil.with().loadImage(mActivity, mTopBackgroup, mFanInfo.get("background"), R.drawable.bg_fan_detail_head, 0);

			mFanName.setText(mFanInfo.get("name"));
			mFanHostName.setText(mFanInfo.get("nickname"));
			mFanDetail.setText(mFanInfo.get("detail"));
			mFanHotIndex.setText(mFanInfo.get("hot"));
			mFanPost.setText(mFanInfo.get("postCount"));
			mFanMember.setText(mFanInfo.get("memberTotal"));
			if (isAdd()) {
				mSendPostIv.setVisibility(View.VISIBLE);
			} else {
				mSendPostIv.setVisibility(View.GONE);
			}
		}
	}

	private void initHeadLayoutData2() {
		mFanMenberAdapter = new FanMenberAdapter(mActivity);
		mHorizontalListView.setAdapter(mFanMenberAdapter);
		if (mFanInfo != null) {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("headPic", mFanInfo.get("logo"));
			data.put("isOwner", Constants.COMMON_TRUE);
			mFanMenberAdapter.addFirstItem(data);
		}
	}

	@Override
	protected void setEventsListeners() {
		mTopBackgroup.setOnTouchListener(new OnDoubleClickListener(this));
		mFanMenberIv.setOnClickListener(this);
		mHeadLayout1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 如果已登录，且是圈主
				if (AppConfig.getInstance().isLogged && mFanInfo.get("uid").equals(UserInfoConfig.getInstance().id)) {
					ActivityJumpUtil.gotoActivityForResult(mActivity, FanEditActivity.class, EDIT_FAN_INFO, FAN_INFO,
							(Serializable) mFanInfo);
				}
			}
		});
	}

	@Override
	protected void initTitleData() {
		mTopBackLayout.setOnClickListener(this);
		mTopRightImageLayout.setOnClickListener(this);
		mTopRightImage.setImageResource(R.drawable.btn_more_quan);
		mTopTitleTv.setText(mFanInfo.get("name"));
		mTopTitleTv.setVisibility(View.GONE);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {

		initHeadLayoutData1();
		initHeadLayoutData2();
		initTitle();
		requestAllData();
	}

	/**
	 * 重新请求数据
	 *
	 * @param clearAdapter 请求之前是否先清空UI
	 */
	public void reRequestData(boolean clearAdapter, String sort) {
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
		requestPlazaData(page, sort);
	}

	/**
	 * 向服务端请求最新回复列表数据
	 */
	private void requestPlazaData(int page, String sort) {
		BusinessUtils.getFanPostList(mActivity, mFanInfo.get("id"), page, sort, new GetPostListCallbackData(this));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(FAN_INFO, (Serializable) mFanInfo);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		mFanInfo = (Map<String, String>) savedInstanceState.getSerializable(FAN_INFO);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.btn_fan_menber:
				ActivityJumpUtil.gotoActivityForResult(mActivity, FanMenberSearchActivity.class, REQUEST_CODE_GROUP_MENBER,
						FanDetailActivity.FAN_INFO, (Serializable) mFanInfo);
				break;
			case R.id.top_left:
				onBackPressed();
				break;
			case R.id.top_right:
				if (mFanDetailMoreWindow == null || !mFanDetailMoreWindow.isShowing()) {
					mFanDetailMoreWindow = new PopFanDetailMoreWindow(mActivity, isAdd(), new MoreItemListener());
					mFanDetailMoreWindow.showAsDropDown(v, 0, 0);
				}
				break;
			// 发帖按钮
			case R.id.publicBtn:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "postInFanCircle");
				ActivityJumpUtil.gotoActivityForResult(mActivity, GroupPostPublishActivity.class,
						PostPublishActivity.REQUEST_CODE_PUBLIC_FRAGMENT, FanDetailActivity.FAN_INFO,
						(Serializable) mFanInfo);
				break;
			case R.id.top_backgroud:
				mPullRefreshListView.getRefreshableView().setSelection(0);
				mTopBackgroup.setVisibility(View.GONE);
				break;
			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int piRequestCode, int piResultCode, Intent poData) {
		super.onActivityResult(piRequestCode, piResultCode, poData);
		if (piRequestCode == EDIT_FAN_INFO) {
			if (piResultCode == RESULT_OK) {
				BusinessUtils.getFanDetail(mActivity, mFanInfo.get("id"), new GetFanDetailCallbackData(
						FanDetailActivity.this));
			}
		} else if (piRequestCode == PostPublishActivity.REQUEST_CODE_PUBLIC_FRAGMENT) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_PUBLIC_FRAGMENT " + piResultCode);
			if (piResultCode == RESULT_OK) {
				reRequestData(false, mSort);
			}
		} else if (piRequestCode == REQUEST_CODE_GROUP_MENBER) {
			BusinessUtils.getFanAdmin(mActivity, mFanInfo.get("id"),
					new GetFanAdminCallbackData(FanDetailActivity.this));
		}

	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.MSG_GROUP_POST_LIST_FAILED:
				mPullRefreshListView.onRefreshComplete();
				// if (mSubjectAdapter.isEmpty()) {
				// // String text = mActivity.getString(R.string.a_loading_failed);
				// // mLoadProgress.Failed(text, 0);
				// } else {
				UiHelper.showToast(mActivity, (String) msg.obj);
				mListFooterLoadView.onLoadingFailed();
				// }
				break;
			case MsgTypes.MSG_GROUP_POST_LIST_SUCCESS:
				mPullRefreshListView.onRefreshComplete();// 收起正在刷新HeaderView
				Object[] objects = (Object[]) msg.obj;
				boolean isRefreh = (Boolean) objects[0];
				List<Map<String, Object>> listData = (List<Map<String, Object>>) objects[1];
				if (isRefreh) { // 初始化或者下拉刷新模式
					// 如果刷新列表，就隐藏点击刷新按钮
					mSubjectAdapter.clearData();
					mSubjectAdapter.addData(listData);
					if (listData == null || listData.size() == 0) {
						mReplyTip.setVisibility(View.VISIBLE);
					} else {
						mReplyTip.setVisibility(View.GONE);
					}
				} else {
					// 加载更多数据模式
					if (listData.isEmpty()) {
						mListFooterLoadView.onNoMoreData();
					} else {
						// 隐藏ListView的FootView
						mListFooterLoadView.hide();
						mSubjectAdapter.addData(listData);
					}
				}
				// 设置没有数据的EmptyView
				// String text = mThis.getString(R.string.a_list_data_empty);
				// mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
				break;

			case MsgTypes.MSG_GROUP_DETAIL_SUCCESS:
				mFanInfo = (Map<String, String>) msg.obj;
				mSubjectAdapter.notifyDataSetChanged();
				initHeadLayoutData1();
				// UiHelper.showToast(mActivity, (String) msg.obj);
				break;
			case MsgTypes.MSG_GROUP_DETAIL_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;
			case MsgTypes.MSG_GROUP_ADMIN_SUCCESS:
				mFanMenberAdapter.clearData();
				mFanMenberAdapter.addData((List<Map<String, Object>>) msg.obj);
				// UiHelper.showToast(mActivity, (String) msg.obj);
				break;
			// 删除帖子
			case MsgTypes.MSG_FAN_REMOVE_POST_SUCCESS:
				/**
				 * @date 2016.6.8
				 * umeng log:java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
				 */
//                int position = Integer.parseInt((String) msg.obj);
				int position = Integer.parseInt(msg.obj.toString());
				// 如果删除成功，把数据添加到listview
				mSubjectAdapter.getData().remove(position);
				mSubjectAdapter.notifyDataSetChanged();
				if (mSubjectAdapter.isDataEmpty()) {
					// 回复成功，就消失
					mReplyTip.setVisibility(View.VISIBLE);
				}
				UiHelper.showToast(mActivity, "删除成功");
				break;
			case MsgTypes.MSG_FAN_REMOVE_POST_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;
			case MsgTypes.MSG_FAN_TOP_SUCCESS:

				Bundle topBundle = msg.getData();
				boolean flag = topBundle.getBoolean("flag");
				int topPosition = topBundle.getInt("position");
				if (flag) {
					// 如果删除成功，把数据添加到listview
					mSubjectAdapter.getData().get(topPosition).put("isTop", Constants.COMMON_TRUE);
					mSubjectAdapter.notifyDataSetChanged();
					UiHelper.showToast(mActivity, "置顶成功");
				} else {
					// 如果删除成功，把数据添加到listview
					mSubjectAdapter.getData().get(topPosition).put("isTop", "false");
					mSubjectAdapter.notifyDataSetChanged();
					UiHelper.showToast(mActivity, "取消置顶成功");
				}
				break;
			case MsgTypes.MSG_FAN_TOP_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;
			case MsgTypes.MSG_FAN_RECOMMENT_SUCCESS:
				Bundle recommentBundle = msg.getData();
				boolean flag1 = recommentBundle.getBoolean("flag");
				int recommnetBundle = recommentBundle.getInt("position");
				if (flag1) {
					// 如果删除成功，把数据添加到listview
					mSubjectAdapter.getData().get(recommnetBundle).put("isNice", Constants.COMMON_TRUE);
					mSubjectAdapter.notifyDataSetChanged();
					UiHelper.showToast(mActivity, "加精成功");
				} else {
					// 如果删除成功，把数据添加到listview
					mSubjectAdapter.getData().get(recommnetBundle).put("isNice", "false");
					mSubjectAdapter.notifyDataSetChanged();
					UiHelper.showToast(mActivity, "取消加精成功");
				}
				break;

			case MsgTypes.MSG_FAN_RECOMMENT_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;

			case MsgTypes.MSG_QUIT_FAN_SUCCESS:
				UiHelper.showToast(mActivity, "退出饭圈成功");
				mFanInfo.put("joined", "false");
				mSendPostIv.setVisibility(View.INVISIBLE);
				break;
			case MsgTypes.MSG_QUIT_FAN_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;
			case MsgTypes.MSG_ADD_FAN_SUCCESS:
				mFanInfo.put("joined", Constants.COMMON_TRUE);
				mSendPostIv.setVisibility(View.VISIBLE);
				UiHelper.showToast(mActivity, mActivity.getString(R.string.commutity_fan_add_succuss));
				break;
			case MsgTypes.MSG_ADD_FAN_FAILED:
				String errorMsg = (String) msg.obj;
				UiHelper.showToast(mActivity, errorMsg);
				break;
		}

	}

	private void addFan(String groupId) {
		MobclickAgent.onEvent(FeizaoApp.mConctext, "joinFanInSearchCirclePage");
		if (!AppConfig.getInstance().isLogged) {
			Utils.requestLoginOrRegister(mActivity, mActivity.getResources().getString(R.string.tip_login_title),
					Constants.REQUEST_CODE_LOGIN);
			return;
		}
		BusinessUtils.addFan(mActivity, groupId, new AddFanCallbackData());
	}

	private void requestAllData() {
		reRequestData(false, mSort);
		BusinessUtils.getFanAdmin(mActivity, mFanInfo.get("id"), new GetFanAdminCallbackData(FanDetailActivity.this));
		BusinessUtils.getFanDetail(mActivity, mFanInfo.get("id"), new GetFanDetailCallbackData(FanDetailActivity.this));
	}

	private boolean isAdmin() {
		return Utils.getBooleanFlag(mFanInfo.get("isOwner"))
				|| Utils.getBooleanFlag(mFanInfo.get("isAdmin"));
	}

	private boolean isAdd() {
		return Utils.getBooleanFlag(mFanInfo.get("joined"));
	}

	/**
	 * 列表项的点击事件
	 */
	private class ItemOnclick implements IOnclickListener {

		@Override
		public void onClick(View v, int position, View statusView) {
			switch (v.getId()) {
				// 点击头像进入个人中心
				case R.id.item_photo:
					Map<String, String> personInfo = new HashMap<String, String>();
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
					showMoreItemDialog(position);
					break;
				default:
					break;
			}

		}
	}

	/**
	 * 弹出对话框
	 *
	 * @param position
	 * @param
	 */
	private void showMoreItemDialog(final int position) {
		@SuppressWarnings("unchecked")
		final Map<String, Object> subjectInfo = (Map<String, Object>) mSubjectAdapter.getItem(position);
		actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
				.setCanceledOnTouchOutside(true);
		/** 如果是管理员，显示置顶按钮 */
		if (isAdmin()) {
			final boolean isTop = !Utils.getBooleanFlag(subjectInfo.get("isTop"));
			final boolean isRecomment = !Utils.getBooleanFlag(subjectInfo.get("isNice"));
			if (isTop) {
				actionSheetDialog.addSheetItem("置顶", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						MobclickAgent.onEvent(FeizaoApp.mConctext, "topPostInFanCircle");
						BusinessUtils.addTopPost(mActivity, subjectInfo.get("id").toString(), isTop,
								new TopCallbackData(isTop, position));
					}
				});
			} else {
				actionSheetDialog.addSheetItem("取消置顶", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						BusinessUtils.addTopPost(mActivity, subjectInfo.get("id").toString(), isTop,
								new TopCallbackData(isTop, position));
					}
				});
			}
			if (isRecomment) {
				actionSheetDialog.addSheetItem("加精", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						MobclickAgent.onEvent(FeizaoApp.mConctext, "addEssenceInFanCircle");
						BusinessUtils.addRecommentPost(mActivity, subjectInfo.get("id").toString(), isRecomment,
								new RecommentCallbackData(isRecomment, position));
					}
				});
			} else {
				actionSheetDialog.addSheetItem("取消加精", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						BusinessUtils.addRecommentPost(mActivity, subjectInfo.get("id").toString(), isRecomment,
								new RecommentCallbackData(isRecomment, position));
					}
				});
			}

		}
		if (isAdmin() || AppConfig.getInstance().isLogged && UserInfoConfig.getInstance().id.equals(subjectInfo.get("uid"))) {
			actionSheetDialog.addSheetItem("删除", SheetItemColor.BLACK, new OnSheetItemClickListener() {
				@Override
				public void onClick(int which) {
					// 按返回键弹出对话框
					UiHelper.showConfirmDialog(mActivity, R.string.commutity_comfirm_delete_post, R.string.cancel,
							R.string.determine, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {

								}
							}, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									BusinessUtils.deleteGroupPost(mActivity, subjectInfo.get("id").toString(),
											new RemoveCallbackData(FanDetailActivity.this, position));
								}
							});

				}
			});
		} else {
			actionSheetDialog.addSheetItem("举报", SheetItemColor.BLACK, new OnSheetItemClickListener() {
				@Override
				public void onClick(int which) {
					ActivityJumpUtil.toReportActivity(mActivity, Constants.COMMON_REPORT_TYPE_POST,
							subjectInfo.get("id").toString(), 0);
				}
			});
		}
		actionSheetDialog.show();
	}

	/**
	 * 页面“更多”操作的回调处理类
	 */
	private class MoreItemListener implements IMoreItemListener {

		@Override
		public void onItemOnClick(int itemId) {
			switch (itemId) {
				case PopFanDetailMoreWindow.ITEM_ONE:
					// 收藏
					mSort = "create_time";
					reRequestData(false, mSort);
					break;
				case PopFanDetailMoreWindow.ITEM_TWO:
					mSort = "last_reply_time";
					reRequestData(false, mSort);
					break;
				case PopFanDetailMoreWindow.ITEM_THREE:
					MobclickAgent.onEvent(FeizaoApp.mConctext, "inviteFriendInFanCircleDetailPage");
					Map<String, String> shareInfo = new HashMap<String, String>();
					shareInfo.put(ShareDialogActivity.Share_Title,
							String.format(Constants.SHARE_FAN_TITLE, mFanInfo.get("name"), mFanInfo.get("memberTotal")));
					shareInfo.put(ShareDialogActivity.Share_Content,
							String.format(Constants.SHARE_FAN_CONTENT, mFanInfo.get("detail")));
					shareInfo.put(ShareDialogActivity.Share_Img, mFanInfo.get("logo"));
					shareInfo.put(ShareDialogActivity.Share_Url,
							String.format(WebConstants.getFullWebMDomain(WebConstants.SHARE_FAN_URL), (String) mFanInfo.get("id")));
					shareInfo.put(ShareDialogActivity.Share_Dialog, String.valueOf(true));
					ActivityJumpUtil.toShareActivity(mActivity, shareInfo);
					break;
				case PopFanDetailMoreWindow.ITEM_FOUR:
					if (Constants.COMMON_TRUE.equals(mFanInfo.get("joined"))) {
						BusinessUtils.quitFan(mActivity, mFanInfo.get("id"),
								new QuitFanCallbackData(FanDetailActivity.this));
					} else {
						addFan(mFanInfo.get("id"));
					}
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

		private final WeakReference<BaseFragmentActivity> mFragment;

		public GetPostListCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
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
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_GROUP_POST_LIST_FAILED;
				msg.obj = errorMsg;
				BaseFragmentActivity activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

	/**
	 * 饭圈管理员列表 <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class GetFanAdminCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public GetFanAdminCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetFanAdminCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_GROUP_ADMIN_SUCCESS;
					msg.obj = JSONParser.parseSingleInMulti((JSONArray) result, new String[]{""});
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_GROUP_ADMIN_FAILED;
				msg.obj = errorMsg;
				BaseFragmentActivity activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

	/**
	 * 饭圈详情 <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class GetFanDetailCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public GetFanDetailCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetFanDetailCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_GROUP_DETAIL_SUCCESS;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_GROUP_DETAIL_FAILED;
				msg.obj = errorMsg;
				BaseFragmentActivity activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

	/**
	 * 退出饭圈<br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class QuitFanCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public QuitFanCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "QuitFanCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_QUIT_FAN_SUCCESS;
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_QUIT_FAN_FAILED;
				msg.obj = errorMsg;
				BaseFragmentActivity activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

	/**
	 * 帖子删除<br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class RemoveCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		private int position;

		public RemoveCallbackData(BaseFragmentActivity fragment, int position) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
			this.position = position;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "RemoveCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_FAN_REMOVE_POST_SUCCESS;
					msg.obj = position;
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_FAN_REMOVE_POST_FAILED;
				msg.obj = errorMsg;
				BaseFragmentActivity activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

	/**
	 * 帖子置顶<br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private class TopCallbackData implements CallbackDataHandle {
		private boolean flag;
		private int position;

		private TopCallbackData(boolean flag, int position) {
			this.flag = flag;
			this.position = position;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "TopCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_FAN_TOP_SUCCESS;
					Bundle bundle = new Bundle();
					bundle.putBoolean("flag", flag);
					bundle.putInt("position", position);
					msg.setData(bundle);
					sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_FAN_TOP_FAILED;
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}
	}

	/**
	 * 帖子加精<br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private class RecommentCallbackData implements CallbackDataHandle {

		private boolean flag;
		private int position;

		private RecommentCallbackData(boolean flag, int position) {
			this.flag = flag;
			this.position = position;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "RecommentCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_FAN_RECOMMENT_SUCCESS;
					Bundle bundle = new Bundle();
					bundle.putBoolean("flag", flag);
					bundle.putInt("position", position);
					msg.setData(bundle);
					sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_FAN_RECOMMENT_FAILED;
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
