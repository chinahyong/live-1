package tv.live.bx.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.FanFragementStatusAdapter;
import tv.live.bx.adapters.FanListAdapter;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.common.WebConstants;
import tv.live.bx.config.AppConfig;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.TelephoneUtil;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.PullRefreshListView;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;
import com.viewpagerindicator.TabPageIndicator;

import org.json.JSONArray;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

/**
 * 加入饭圈页面: AddFanActivity.java
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class AddFanActivity extends BaseFragmentActivity implements OnClickListener, OnItemClickListener {

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

	private FanListAdapter mFanListAdapter;
	private String mSearchText;
	private RelativeLayout mCancel;
	private EditText mSearchContent;
	private ImageView mSearchClear;
	private TextView mSearchBtn;
	private AlertDialog mProcessDialog;
	private LayoutInflater inflater;

	private LinearLayout mDefualtLayout;

	private ViewPager mViewPager;
	private FanFragementStatusAdapter mFragmentStatePagerAdapter;
	private TabPageIndicator mTabPageIndicator;
	private List<Map<String, String>> mFanMoudleInfos = new ArrayList<Map<String, String>>();

	private Map<String, String> shareInfo = new HashMap<String, String>();

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_add_fan_layout;
	}

	protected void initMembers() {
		inflater = LayoutInflater.from(getApplicationContext());
		mSearchClear = (ImageView) findViewById(R.id.search_clear_iv);
		mSearchContent = (EditText) findViewById(R.id.search_content);
		mCancel = (RelativeLayout) findViewById(R.id.cancel);
		mSearchBtn = (TextView) findViewById(R.id.search_tv);

		mDefualtLayout = (LinearLayout) findViewById(R.id.default_layotu);

		mViewPager = (ViewPager) findViewById(R.id.viewPage);
		// 实例化TabPageIndicator然后设置ViewPager与之关联
		mTabPageIndicator = (TabPageIndicator) findViewById(R.id.indicator);

		mFragmentStatePagerAdapter = new FanFragementStatusAdapter(this, this.getSupportFragmentManager());
		mViewPager.setAdapter(mFragmentStatePagerAdapter);
		mTabPageIndicator.setViewPager(mViewPager);
		initListView();
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {
		mSearchClear.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		mSearchBtn.setOnClickListener(this);
		mSearchContent.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// 当输入框有文字，显示发送按钮；没有时显示礼物按钮
				if (s.length() > 0) {
					mSearchClear.setVisibility(View.VISIBLE);
					mDefualtLayout.setVisibility(View.GONE);
					mLoadProgress.Failed(getResources().getString(R.string.commutity_fan_search_tip, s.toString()));
				} else {
					mSearchClear.setVisibility(View.GONE);
					mDefualtLayout.setVisibility(View.VISIBLE);
					mFanListAdapter.clearData();
					mFanListAdapter.notifyDataSetChanged();
				}
			}
		});
		mSearchContent.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				String searchText = mSearchContent.getText().toString().trim();
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					if (!TextUtils.isEmpty(searchText)) {
						reRequestData(false);
						return true;
					}
				}
				return false;
			}
		});

	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Map<String, String> mStatusData = new HashMap<String, String>();
		mStatusData.put("title", mActivity.getResources().getString(R.string.commutity_fan_freeze));
		mStatusData.put("status", Constants.FAN_STATUS_FREEZE);
		mFanMoudleInfos.add(mStatusData);

		mStatusData = new HashMap<String, String>();
		mStatusData.put("title", mActivity.getResources().getString(R.string.commutity_fan_unlock));
		mStatusData.put("status", Constants.FAN_STATUS_NORMAL);
		mFanMoudleInfos.add(mStatusData);

		mFragmentStatePagerAdapter.setDatas(mFanMoudleInfos);
		mTabPageIndicator.notifyDataSetChanged();
	}

	@Override
	public void onDestroy() {
		dismissProcessDialog();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancel:
			onBackPressed();
			break;
		case R.id.search_clear_iv:
			mSearchContent.setText("");
			break;
		case R.id.search_tv:
			String searchText = mSearchContent.getText().toString().trim();
			if (TextUtils.isEmpty(searchText)) {
				showToast(R.string.anchor_search_text_empty, TOAST_SHORT);
			} else {
				reRequestData(false);
			}
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
		Map<String, Object> item = (Map<String, Object>) mFanListAdapter.getItem(position
				- mListView.getHeaderViewsCount());
		EvtLog.d(TAG, "lmItem " + item.toString());
		if (Constants.FAN_STATUS_NORMAL.equals(item.get("status"))) {
			// 如果已加入
			// if (Constants.FAN_JOINED.equals(item.get("joined"))) {
			// ActivityJumpUtil.gotoActivity(mThis, FanDetailActivity.class,
			// false, FanDetailActivity.FAN_INFO,
			// (Serializable) item);
			// } else {
			// UiHelper.showToast(mThis,
			// mThis.getResources().getString(R.string.commutity_fan_add_tip));
			// }
			ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class, false, FanDetailActivity.FAN_INFO,
					(Serializable) item);
		} else if (Constants.FAN_STATUS_FREEZE.equals(item.get("status"))) {
			// 如果已加入
			if (Utils.getBooleanFlag(item.get("joined"))) {
				UiHelper.showToast(mActivity, mActivity.getResources().getString(R.string.commutity_fan_activate_tip));
			} else {
				UiHelper.showToast(mActivity, mActivity.getResources().getString(R.string.commutity_fan_add_tip));
			}
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
			}
		} else if (requestCode == REQUEST_CODE_FLUSH_ACTIVITY) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_LOGIN " + resultCode);
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {

		case MsgTypes.MSG_FAN_SERACH_SUCCESS:
			dismissProcessDialog();
			Object[] objects = (Object[]) msg.obj;
			boolean isRefreh = (Boolean) objects[0];
			List<Map<String, Object>> mListData = (List<Map<String, Object>>) objects[1];
			if (isRefreh) { // 初始化或者下拉刷新模式
				mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
				mFanListAdapter.clearData();
				mFanListAdapter.addData(mListData);
			} else { // 加载更多数据模式
				if (mListData.isEmpty()) {
					mListFooterLoadView.onNoMoreData();
				} else {
					// 如果点击的foot加载第一页的数据，重新更新adapter数据
					if (page == 1) {
						// 隐藏ListView的FootView
						mListFooterLoadView.hide();
						mFanListAdapter.clearData();
						mFanListAdapter.addData(mListData);
					} else {
						// 隐藏ListView的FootView
						mListFooterLoadView.hide();
						mFanListAdapter.addData(mListData);
					}
				}
			}
			// 设置没有数据的EmptyView
			String text = mActivity.getString(R.string.commutity_fan_search_empty_tip);
			mLoadProgress.Succeed(text, 0);

			break;
		case MsgTypes.MSG_FAN_SERACH_FAILED:
			dismissProcessDialog();
			mListView.notifyTaskFinished();
			if (mFanListAdapter.isEmpty()) {
				String text1 = mActivity.getString(R.string.a_loading_failed);
				mLoadProgress.Failed(text1, 0);
			} else {
				UiHelper.showToast(mActivity, R.string.a_tips_net_error);
				mLoadProgress.Hide();
				mListFooterLoadView.onLoadingFailed();
			}
			break;
		case MsgTypes.MSG_ADD_FAN_SUCCESS:
			reRequestData(false);
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

	/**
	 * 初始化下拉刷新ListView
	 */
	private void initListView() {

		mListView = (PullRefreshListView) findViewById(R.id.author_listview);
		mListView.setOnItemClickListener(this);
		mListView.setTopHeadHeight(0);
		mListView.setBottomFooterHeight((int) (15 * getResources().getDisplayMetrics().density / 1.5f));
		mFanListAdapter = new FanListAdapter(this);
		mListView.setAdapter(mFanListAdapter);
		mFanListAdapter.setOnItemClickListener(new FanListAdapter.OnItemClickListener() {

			@Override
			public void onClick(View view, int position) {
				Map<String, Object> item = (Map<String, Object>) mFanListAdapter.getItem(position);
				if (Constants.FAN_STATUS_FREEZE.equals(item.get("status"))) {
					// 如果已加入
					if (Utils.getBooleanFlag(item.get("joined"))) {
						MobclickAgent.onEvent(FeizaoApp.mConctext, "inviteFriendInFanCircle");
						shareInfo.put(ShareDialogActivity.Share_Title,
								String.format(Constants.SHARE_FAN_TITLE, item.get("name"), item.get("memberTotal")));
						shareInfo.put(ShareDialogActivity.Share_Content,
								String.format(Constants.SHARE_FAN_CONTENT, item.get("detail")));
						shareInfo.put(ShareDialogActivity.Share_Img, (String) item.get("logo"));
						shareInfo.put(ShareDialogActivity.Share_Url,
								String.format(WebConstants.getFullWebMDomain(WebConstants.SHARE_FAN_URL), (String) item.get("id")));
						shareInfo.put(ShareDialogActivity.Share_Dialog, String.valueOf(true));

						ActivityJumpUtil.toShareActivity(mActivity, shareInfo);
					} else {
						addFan(item.get("id").toString());
					}
				} else {
					// 如果已加入
					if (Utils.getBooleanFlag(item.get("joined"))) {
						ActivityJumpUtil.gotoActivityForResult(mActivity, FanDetailActivity.class,
								REQUEST_CODE_FLUSH_ACTIVITY, FanDetailActivity.FAN_INFO, (Serializable) item);
					} else {
						addFan(item.get("id").toString());
					}
				}
			}
		});
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
					requestPlazaData(page, mSearchText);
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
							requestPlazaData(page, mSearchText);
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
		mLoadProgress.Failed(getResources().getString(R.string.commutity_fan_search_empty_tip));
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
		// 初始化loading(正在加载...)
		// 重新初始化请求页面
		mSearchText = mSearchContent.getText().toString().trim();
		if (TextUtils.isEmpty(mSearchText))
			return;
		page = 0;
		isRefresh = true;
		requestPlazaData(page, mSearchText);
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page, String searchText) {
		mProcessDialog = Utils.showProgress(this);
		TelephoneUtil.hideSoftInput(mActivity);
		BusinessUtils.getSearchFanList(this, page, searchText, new FanSearchCallbackData(this));
	}

	private void dismissProcessDialog() {
		if (mProcessDialog != null && mProcessDialog.isShowing()) {
			mProcessDialog.dismiss();
		}
	}

	/**
	 * 饭圈搜索数据处理回调 ClassName: FanSearchCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * 
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class FanSearchCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public FanSearchCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FanSearchCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					Object[] objects = new Object[] { isRefresh, JSONParser.parseMulti((JSONArray) result) };
					msg.obj = objects;
					// 下次请求的页面数
					page++;

					msg.what = MsgTypes.MSG_FAN_SERACH_SUCCESS;
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_FAN_SERACH_FAILED;
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
