package tv.live.bx.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.lonzh.lib.network.JSONParser;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.adapters.LiveNearListAdapter;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.listeners.GoHotClickListener;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.LoadingProgressEmptyJump;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;


/**
 * Title: LiveNearFragment.java</br> Description: 主播列表附近主播</br> Copyright: *
 *
 * @author Live
 * @version 2.4.0 2016.4.22
 */
@SuppressLint("NewApi")
public class LiveNearFragment extends BaseFragment implements OnClickListener, OnItemClickListener {

	/**
	 * 目前暂时都刷新
	 */
	private static boolean isRefresh = true;
	/**
	 * 获取最新页面时，page为0，page以此累加
	 */
	private static int page = 0;

	private PullToRefreshGridView mPullRefreshGridView;
	private LiveNearListAdapter mLiveNearAdapter;
	/**
	 * 头部
	 */
	private TextView mHeadLayout;
	/**
	 * 加载更多FootView
	 */
	private ListFooterLoadView mListFooterLoadView;
	/**
	 * 空数据
	 */
	private Button mBtnBackHot;

	/**
	 * 加载loading
	 */
	private LoadingProgressEmptyJump mLoadProgress;

	private GoHotClickListener mGoHotClickListener;

	private static final int REQUEST_CODE_SETTING = 0x1041;
	private AMapLocationClient mLocationClient;        //定位对象
	private AMapLocationClientOption mLocationOption;    //定位参数
	private String mLocation;//位置信息

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_live_near_layout;
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
		mBtnBackHot.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle bundle) {
		reRequestAllData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		destroyLocation();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.live_new_btn_back_hot:
				if (mGoHotClickListener != null)
					mGoHotClickListener.onGoHotClick();
				break;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		OperationHelper.onEvent(FeizaoApp.mConctext, "clickBbroadcasterSImgInNearbyTab", null);
		Map<String, Object> lmItem = (Map<String, Object>) mLiveNearAdapter.getItem(position);
		lmItem.put("rid", lmItem.get("rid"));
		ActivityJumpUtil.toLiveMediaPlayerActivity(mActivity, lmItem);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_SETTING) {
			// GPS开启页面设置完成回调
			// 打开了定位：打开开关，开始定位
			if (Utils.isOPenLocation(mActivity)) {
				initLocation();
			} else {
				// 未打开定位：关闭开关
				showStartLocationDialog();
			}
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.MSG_LOAD_FAILED:
				mPullRefreshGridView.onRefreshComplete();
				if (mLiveNearAdapter.isEmpty()) {
					String text = mActivity.getString(R.string.a_loading_failed);
					mLoadProgress.Failed(text, 0);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
					mLoadProgress.Hide();
					mListFooterLoadView.onLoadingFailed();
				}
				break;

			case MsgTypes.MSG_LOAD_SUCCESS:
				Object[] objects = (Object[]) msg.obj;
				boolean isRefresh = (Boolean) objects[0];
				List<Map<String, Object>> mData = (List<Map<String, Object>>) objects[1];
				mPullRefreshGridView.onRefreshComplete(); // 收起正在刷新HeaderView
				if (isRefresh) { // 初始化或者下拉刷新模式
					mLiveNearAdapter.clearData();
					mLiveNearAdapter.addData(mData);
				} else { // 加载更多数据模式
					if (mData.isEmpty()) {
						mListFooterLoadView.onNoMoreData();
					} else {
						// 如果点击的foot加载第一页的数据，重新更新adapter数据
						if (page == 1) {
							// 隐藏ListView的FootView
							mListFooterLoadView.hide();
							mLiveNearAdapter.clearData();
							mLiveNearAdapter.addData(mData);
						} else {
							// 隐藏ListView的FootView
							mListFooterLoadView.hide();
							mLiveNearAdapter.addData(mData);
						}
					}
				}
				mLoadProgress.Succeed("", R.drawable.a_common_no_data);
				break;
		}
	}

	@Override
	public void onTabClickAgain() {
		if (mPullRefreshGridView != null)
			mPullRefreshGridView.setRefreshing();
	}

	/**
	 * 切换tab，也更新数据 TODO 简单描述该方法的实现功能（可选）.
	 *
	 * @see BaseFragment#onTabSelected()
	 */
	@Override
	protected void onTabSelected() {
		super.onTabSelected();
		if (mLiveNearAdapter != null && mLiveNearAdapter.isEmpty()) {
			// 初始化loading(正在加载...)
			// mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
			reRequestData(false);
		}
	}

	/**
	 * 初始化UI控件
	 */
	private void initUI(View v, LayoutInflater inflater) {
		mHeadLayout = (TextView) inflater.inflate(R.layout.fragment_live_new_head, null);
		mBtnBackHot = (Button) v.findViewById(R.id.live_new_btn_back_hot);
		initListView(v, inflater);
	}

	/**
	 * 初始化下拉刷新ListView
	 *
	 * @param v
	 * @param inflater
	 */
	private void initListView(View v, LayoutInflater inflater) {
		mPullRefreshGridView = (PullToRefreshGridView) v.findViewById(R.id.live_new_listview);
		mPullRefreshGridView.setMode(Mode.BOTH);
		mPullRefreshGridView.getLoadingLayoutProxy(false, true).setLoadingDrawable(
				mActivity.getResources().getDrawable(R.drawable.a_common_progress_circle));
		mPullRefreshGridView.getLoadingLayoutProxy(false, true).setPullLabel(
				mActivity.getText(R.string.a_list_hint_pullup_to_load_more));
		mPullRefreshGridView.getLoadingLayoutProxy(false, true).setRefreshingLabel(
				mActivity.getText(R.string.a_list_hint_loading));
		mPullRefreshGridView.getLoadingLayoutProxy(false, true).setReleaseLabel(
				mActivity.getText(R.string.a_list_hint_release_to_load_more));
		mPullRefreshGridView.getRefreshableView().setColumnWidth((FeizaoApp.metrics.widthPixels - Utils.dpToPx(LiveNearListAdapter.Uniform_Space) * 3) / 2);
		mLiveNearAdapter = new LiveNearListAdapter(mActivity);

		// mPullRefreshGridView.getRefreshableView().addHeaderView(mHeadLayout);

		mPullRefreshGridView.setOnRefreshListener(new OnRefreshListener2<GridView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
				OperationHelper.onEvent(FeizaoApp.mConctext, "refreshInNearbyPageOfIndex", null);
				// 加载Banner,anchor列表
				reRequestData(false);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				EvtLog.d(TAG, "滚动加载更多");
				isRefresh = false;
				requestPlazaData(page);
			}

		});
		mPullRefreshGridView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				EvtLog.d(TAG, "setOnLastItemVisibleListener");
				mPullRefreshGridView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
			}
		});

		mPullRefreshGridView.setOnItemClickListener(this);

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

		// 设置默认图片
		mLoadProgress = (LoadingProgressEmptyJump) v.findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				// 加载Banner,anchor列表
				reRequestData(false);
			}

			@Override
			public void onClick(View v) {
				// mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				// // 加载Banner,anchor列表
				// reRequestAllData();
			}
		});
		mPullRefreshGridView.setEmptyView(mLoadProgress);
		mPullRefreshGridView.getRefreshableView().setAdapter(mLiveNearAdapter);
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
		// 清空界面数据
		if (clearAdapter) {
			mLiveNearAdapter.clearData();
			mLiveNearAdapter.notifyDataSetChanged();
		}
		isRefresh = true;
		requestPlazaData(page);
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page) {
		BusinessUtils.getNearAuthorListData(mActivity, page, mLocation, new AuthorCallbackData(this));
	}

	private void reRequestAllData() {
		// 是否开启定位
		if (!Utils.isOPenLocation(mActivity)) {
			showStartLocationDialog();
		} else {
			initLocation();
		}
	}

	private void showStartLocationDialog() {
		// 按返回键弹出对话框
		UiHelper.showConfirmDialog(mActivity, R.string.live_location_fail_tip, R.string.live_start_location,
				R.string.determine, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityJumpUtil.toLocationSettingActivity(mActivity, REQUEST_CODE_SETTING);
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendEmptyMsg(MsgTypes.MSG_LOAD_FAILED);
					}
				});
	}

	/**
	 * 开启定位
	 */
	private void initLocation() {
		if (mLocationClient == null) {
			mLocationClient = new AMapLocationClient(FeizaoApp.mConctext);
			mLocationClient.setLocationOption(initMapOption());
			mLocationClient.setLocationListener(new AMapLocationListener() {
				@Override
				public void onLocationChanged(AMapLocation aMapLocation) {
					EvtLog.e(TAG, "onLocationChanged:" + aMapLocation.toString());
					// 获取位置信息成功
					if (aMapLocation.getErrorCode() == 0) {
						mLocation = aMapLocation.getLatitude() + "," + aMapLocation.getLongitude();
						reRequestData(false);
					} else {
						sendEmptyMsg(MsgTypes.MSG_LOAD_FAILED);
					}
				}
			});
		}
		mLocationClient.startLocation();
	}

	/**
	 * 初始化高德地图定位参数
	 */
	private AMapLocationClientOption initMapOption() {
		mLocationOption = new AMapLocationClientOption();
		mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);// 高精度定位
		mLocationOption.setOnceLocation(false);        // 是否单次定位  true：定位1次
		mLocationOption.setInterval(1000 * 60 * 30);
		mLocationOption.setOnceLocationLatest(false);        // 是否获取3秒内最精确定位，false	关闭，定位到城市用不到
		mLocationOption.setNeedAddress(false);        //是否返回位置信息(此处说的是地址信息)，false关闭
		return mLocationOption;
	}

	/**
	 * 销毁定位
	 */
	private void destroyLocation() {
		if (mLocationClient != null) {
			mLocationClient.onDestroy();        //销毁定位服务，要使用需重新实例化
			mLocationClient = null;
		}
	}

	public void setGoHotClickListener(GoHotClickListener mGoHotClickListener) {
		this.mGoHotClickListener = mGoHotClickListener;
	}

	/**
	 * 最新主播列表数据处理回调 ClassName: AuthorCallbackData <br/>
	 *
	 * @author Live
	 */
	private static class AuthorCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragment> mFragment;

		public AuthorCallbackData(BaseFragment fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_LOAD_SUCCESS;
					Object[] objects = new Object[]{isRefresh,
							JSONParser.parseSingleInMulti((JSONArray) result, new String[]{""})};
					msg.obj = objects;
					// 下次请求的页面数
					page++;
					BaseFragment fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_LOAD_FAILED;
				msg.obj = errorMsg;
				BaseFragment activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}
}
