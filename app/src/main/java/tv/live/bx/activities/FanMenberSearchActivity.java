package tv.live.bx.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
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
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.FanMenberListAdapter;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.common.WebConstants;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.TelephoneUtil;
import tv.live.bx.listeners.AuthorityManageable;
import tv.live.bx.ui.ActionSheetDialog;
import tv.live.bx.ui.ActionSheetDialog.OnSheetItemClickListener;
import tv.live.bx.ui.ActionSheetDialog.SheetItemColor;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.PullRefreshListView;
import tv.live.bx.ui.popwindow.MorePopWindow;
import com.lonzh.lib.network.JSONParser;

import org.json.JSONArray;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

/**
 * 成员搜索页面: MenberSearchActivity.java
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class FanMenberSearchActivity extends BaseFragmentActivity implements OnClickListener, OnItemClickListener, AuthorityManageable {

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

	private FanMenberListAdapter mfanListAdapter;
	private String mSearchText;
	private EditText mSearchContent;
	private ImageView mSearchClear, mSearchBtn;
	private AlertDialog mProcessDialog;
	private LayoutInflater inflater;

	/** 更多popwindow */
	private MorePopWindow mMorePopWindow;

	private TextView mMenberNum;
	/** 饭圈信息 */
	private Map<String, String> mFanInfo;
	/** 是否属于圈主 */
	private boolean isOwenr = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.isSystemBarTint = false;
		super.onCreate(savedInstanceState);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_menber_layout;
	}

	protected void initMembers() {
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			mFanInfo = (Map<String, String>) bundle.getSerializable(FanDetailActivity.FAN_INFO);
			// 如果已登录，且是圈主
			if (AppConfig.getInstance().isLogged && mFanInfo.get("uid").equals(UserInfoConfig.getInstance().id)) {
				isOwenr = true;
			}
		}

		inflater = LayoutInflater.from(getApplicationContext());
		mSearchClear = (ImageView) findViewById(R.id.search_clear_iv);
		mSearchContent = (EditText) findViewById(R.id.search_content);
		mSearchBtn = (ImageView) findViewById(R.id.serach_iv);
		mMenberNum = (TextView) findViewById(R.id.menber_num);
		initListView();
		initTitle();
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {
		mSearchClear.setOnClickListener(this);
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
				} else {
					mSearchClear.setVisibility(View.GONE);
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
		// 如果已登录
		mMenberNum.setText(String.format(getResources().getString(R.string.commutity_fan_menber_num),
				mFanInfo.get("memberTotal")));
		reRequestData(false);
	}

	@Override
	protected void initTitleData() {

		mTopTitleTv.setText(R.string.commutity_fan_menber_titil);
		mTopRightText.setText(R.string.commutity_fan_menber_help);
		mTopRightImageLayout.setOnClickListener(this);
		mTopRightImageLayout.setVisibility(View.VISIBLE);
		mTopRightImage.setImageResource(R.drawable.btn_more_fan_selector);
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
		case R.id.top_right_text_bg:
			break;
		case R.id.top_right:
			showMorePopWindow(v);
			break;
		case R.id.search_clear_iv:
			mSearchContent.setText("");
			break;
		case R.id.serach_iv:
			String searchText = mSearchContent.getText().toString().trim();
			if (TextUtils.isEmpty(searchText)) {
				showToast(R.string.anchor_search_text_empty, TOAST_SHORT);
			} else {
				mProcessDialog = Utils.showProgress(this);
				reRequestData(false);
			}
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position - mListView.getHeaderViewsCount() < 0) {
			return;
		}
		if (position - mListView.getHeaderViewsCount() >= mfanListAdapter.getCount()) {
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

		case MsgTypes.SEARCH_MENBER_LIST_SUCCESS:
			dismissProcessDialog();

			Object[] objects = (Object[]) msg.obj;
			boolean isRefreh = (Boolean) objects[0];
			List<Map<String, Object>> mListData = (List<Map<String, Object>>) objects[1];
			if (isRefreh) { // 初始化或者下拉刷新模式
				mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
				mfanListAdapter.clearData();
				mfanListAdapter.addData(mListData);
			} else { // 加载更多数据模式
				if (mListData.isEmpty()) {
					mListFooterLoadView.onNoMoreData();
				} else {
					// 隐藏ListView的FootView
					mListFooterLoadView.hide();
					mfanListAdapter.addData(mListData);
				}
			}
			// 设置没有数据的EmptyView
			String text = mActivity.getString(R.string.commutity_fan_menber_search_empty);
			mLoadProgress.Succeed(text, 0);

			break;
		case MsgTypes.SEARCH_MENBER_LIST_FAILED:
			dismissProcessDialog();

			mListView.notifyTaskFinished();
			if (mfanListAdapter.isEmpty()) {
				String text1 = mActivity.getString(R.string.a_loading_failed);
				mLoadProgress.Failed(text1, 0);
			} else {
				UiHelper.showToast(mActivity, R.string.a_tips_net_error);
				mLoadProgress.Hide();
				mListFooterLoadView.onLoadingFailed();
			}
			break;
		case MsgTypes.MSG_FAN_ADMIN_SUCCESS:
			Bundle bundle = msg.getData();
			boolean flag = bundle.getBoolean("flag");
			int position = bundle.getInt("position");
			if (flag) {
				mfanListAdapter.getData().get(position).put("isAdmin", Constants.COMMON_TRUE);
				mfanListAdapter.notifyDataSetChanged();
				UiHelper.showToast(mActivity, "设置成功");
			} else {
				mfanListAdapter.getData().get(position).put("isAdmin", "false");
				mfanListAdapter.notifyDataSetChanged();
				UiHelper.showToast(mActivity, "取消成功");
			}

			break;
		case MsgTypes.MSG_FAN_ADMIN_FAILED:
			UiHelper.showToast(mActivity, (String) msg.obj);
			break;
		case MsgTypes.MSG_GROUP_BAN_SUCCESS:
			Bundle bundle2 = msg.getData();
			boolean flag2 = bundle2.getBoolean("flag");
			int position2 = bundle2.getInt("position");
			if (flag2) {
				mfanListAdapter.getData().get(position2).put("isUserBanned", Constants.COMMON_TRUE);
				mfanListAdapter.notifyDataSetChanged();
				UiHelper.showToast(mActivity, "已成功禁言");
			} else {
				mfanListAdapter.getData().get(position2).put("isUserBanned", "false");
				mfanListAdapter.notifyDataSetChanged();
				UiHelper.showToast(mActivity, "已成功解禁");
			}

			break;
		case MsgTypes.MSG_GROUP_BAN_FAILED:
			UiHelper.showToast(mActivity, (String) msg.obj);
			break;
		}

	}

	/**
	 * 初始化下拉刷新ListView
	 * 
	 */
	private void initListView() {

		mListView = (PullRefreshListView) findViewById(R.id.author_listview);
		mListView.setOnItemClickListener(this);
		mListView.setTopHeadHeight(0);
		mListView.setBottomFooterHeight((int) (15 * getResources().getDisplayMetrics().density / 1.5f));
		mfanListAdapter = new FanMenberListAdapter(this, isOwenr);
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
		mListView.setAdapter(mfanListAdapter);
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
		// 初始化loading(正在加载...)
		// 重新初始化请求页面
		mSearchText = mSearchContent.getText().toString().trim();
		page = 0;
		isRefresh = true;
		requestPlazaData(page, mSearchText);
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page, String searchText) {
		TelephoneUtil.hideSoftInput(mActivity);
		BusinessUtils.getSearchMenberList(this, mFanInfo.get("id"), searchText, page, new FanMenberSearchCallbackData(
				this));
		return;

	}

	/**
	 * 显示更多下拉对话框
	 */
	private void showMorePopWindow(View v) {
		if (mMorePopWindow == null) {
			mMorePopWindow = new MorePopWindow(mActivity, R.layout.pop_fan_menber_search_layout);
			MoreItemListener mMoreItemListener = new MoreItemListener();
			View view = mMorePopWindow.getContentView().findViewById(R.id.item1_layout);
			if (onIsGroupAdmin(mFanInfo.get("isAdmin"))) {
				view.setVisibility(View.VISIBLE);
			} else {
				view.setVisibility(View.GONE);
			}
			mMorePopWindow.setOnClickListener(R.id.item1_layout, mMoreItemListener);
			mMorePopWindow.setOnClickListener(R.id.item2_layout, mMoreItemListener);
		}
		if (!mMorePopWindow.isShowing()) {
			mMorePopWindow.showAsDropDown(v, 0, 0);
		}
	}

	/**
	 * 点击“成员列表”
	 * @param position 成员数据position
	 */
	private void clickItem(final int position) {
		final Map<String, Object> mData = (Map<String, Object>) mfanListAdapter.getItem(position);
		final boolean isOwen = onIsOwen((String) mData.get("uid"));
		// 如果点击的头像是自己
		if (isOwen) {
			Map<String, String> personInfo = new HashMap<String, String>();
			personInfo.put("id", (String) mData.get("uid"));
			ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
			return;
		}

		// 如果有管理员权限
		if (onIsGroupAdmin(mFanInfo.get("isAdmin"))) {
			ActionSheetDialog actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
					.setCanceledOnTouchOutside(true)
					.addSheetItem("查看ta的主页", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							// 按返回键弹出对话框
							Map<String, String> personInfo = new HashMap<String, String>();
							personInfo.put("id", (String) mData.get("uid"));
							ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
						}
					});
			if (onIsGroupOwen(mFanInfo.get("isOwner"))) {
				// 如果不是管理员
				if (!Utils.getBooleanFlag(mData.get("isAdmin"))) {
					actionSheetDialog.addSheetItem("设为管理", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							BusinessUtils.addFanAdmin(mActivity, mFanInfo.get("id"), (String) mData.get("uid"), true,
									new FanAdminCallbackData(true, position));
						}
					});
				} else {
					actionSheetDialog.addSheetItem("取消管理", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							BusinessUtils.addFanAdmin(mActivity, mFanInfo.get("id"), (String) mData.get("uid"), false,
									new FanAdminCallbackData(false, position));
						}
					});
				}
			}
			// 如果未禁言
			if (!Utils.getBooleanFlag(mData.get("isUserBanned"))) {
				actionSheetDialog.addSheetItem("禁言", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						showGroupBanDialog(position, (String) mData.get("uid"));
					}
				});
			} else {
				actionSheetDialog.addSheetItem("解禁", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						BusinessUtils.onGroupBanorUnBan(mActivity, mFanInfo.get("id"), (String) mData.get("uid"), null,
								false, new BanCallbackData(false, position));
					}
				});
			}
			actionSheetDialog.show();
		} else {
			Map<String, String> personInfo = new HashMap<String, String>();
			personInfo.put("id", (String) mData.get("uid"));
			ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
		}

	}

	/**
	 * 点击禁言按钮
	 */
	private void showGroupBanDialog(final int position, final String uid) {
		ActionSheetDialog actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
				.setCanceledOnTouchOutside(true)
				.addSheetItem("禁言1小时", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						BusinessUtils.onGroupBanorUnBan(mActivity, mFanInfo.get("id"), uid, String.valueOf(3600), true,
								new BanCallbackData(true, position));
					}
				}).addSheetItem("禁言1天", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						BusinessUtils.onGroupBanorUnBan(mActivity, mFanInfo.get("id"), uid, String.valueOf(3600 * 24),
								true, new BanCallbackData(true, position));
					}
				}).addSheetItem("禁言3天", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						BusinessUtils.onGroupBanorUnBan(mActivity, mFanInfo.get("id"), uid, String.valueOf(3600 * 72),
								true, new BanCallbackData(true, position));
					}
				}).addSheetItem("永久禁言", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						BusinessUtils.onGroupBanorUnBan(mActivity, mFanInfo.get("id"), uid, String.valueOf(0), true,
								new BanCallbackData(true, position));
					}
				});
		actionSheetDialog.show();
	}

	@Override
	public boolean onIsOwen(String uid) {
		return AppConfig.getInstance().isLogged && UserInfoConfig.getInstance().id.equals(uid);
	}

	@Override
	public boolean onIsGroupAdmin(String isGroupAdmin) {
		return Utils.getBooleanFlag(isGroupAdmin);
	}

	@Override
	public boolean onIsGroupOwen(String isGroupOwner) {
		return Utils.getBooleanFlag(isGroupOwner);
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
				ActivityJumpUtil.gotoActivity(mActivity, GroupManageLogActivity.class, false,
						FanDetailActivity.FAN_INFO, (Serializable) mFanInfo);
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
	private static class FanMenberSearchCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public FanMenberSearchCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FanMenberSearchCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					Object[] objects = new Object[] { isRefresh, JSONParser.parseMulti((JSONArray) result) };
					msg.obj = objects;
					// 下次请求的页面数
					page++;

					msg.what = MsgTypes.SEARCH_MENBER_LIST_SUCCESS;
					BaseFragmentActivity fragment =  mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.SEARCH_MENBER_LIST_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity fragment =  mFragment.get();
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
