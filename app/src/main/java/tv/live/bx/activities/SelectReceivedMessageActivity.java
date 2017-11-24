package tv.live.bx.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.efeizao.bx.R;
import com.lonzh.lib.network.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.SelectMessageAdapter;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.config.AppConfig;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.PullRefreshListView;
import tv.live.bx.ui.popwindow.FilterPopupWindow;
import tv.live.bx.util.UiHelper;

/**
 * Created by valar on 2017/3/28.
 * detail 描述选择收信人的activity
 */

public class SelectReceivedMessageActivity extends BaseFragmentActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

	public static final String SELECTRECEIVERINFO = "selectReceiveInfo";
	private RelativeLayout mRlSelectAll;
	private PullRefreshListView mPullRefreshListView;
	private FilterPopupWindow mFilterPopupWindow;
	private FilterPopupWindow.FitlerClickListener mFilterClickListener;
	private SelectMessageAdapter selectMessageAdapter;
	private ImageView mSeletAllImg1;
	private ImageView mSeletAllImg2;
	private LinearLayout mLlNextStep;
	private TextView mLlNextStepText;
	private LinearLayout mLlSelectNone;
	private TextView mTvFansCareText;
	private String leftMessageNums;


	/**
	 * 目前暂时都刷新
	 */
	private static boolean isRefresh = true;
	/**
	 * 获取最新页面时，page为0，page以此累加
	 */
	private static int page = 0;

	/**
	 * 加载更多FootView
	 */
	private ListFooterLoadView mListFooterLoadView;
	/**
	 * 加载loading
	 */
	private LoadingProgress mLoadProgress;

	/**
	 * 所有的集合
	 */
	private Map<Integer, Boolean> selectedFlagPosition = new HashMap<>();  //全选标记的map key:position value:true
	private List<Map<String, String>> mSelectRecieverInfo;   //每一页返回的集合
	private String maxMessageNum;     //每天发的最多人数
	private String isClickWhichInFilter = "0";
	private TextView mTvNosenedMessage;
	private TextView mTvNoNeversenedMessage;


	@Override
	protected int getLayoutRes() {
		return R.layout.activity_select_receive_message;
	}

	@Override
	public void initWidgets() {
		initTitle();
		selectMessageAdapter = new SelectMessageAdapter(mActivity);
		mPullRefreshListView = (PullRefreshListView) findViewById(R.id.select_receive_list);
		mRlSelectAll = (RelativeLayout) findViewById(R.id.select_receive_all);
		mSeletAllImg1 = (ImageView) findViewById(R.id.select_receive_all_img1);
		mSeletAllImg2 = (ImageView) findViewById(R.id.select_receive_all_img2);
		mLlNextStep = (LinearLayout) findViewById(R.id.select_receive_next_step);
		mLlNextStepText = (TextView) findViewById(R.id.select_receive_next_step_text);
		mLlSelectNone = (LinearLayout) findViewById(R.id.select_message_no_reciever);
		mTvFansCareText = (TextView) findViewById(R.id.select_message_text1);
		mTvNosenedMessage = (TextView) findViewById(R.id.select_receive_sended);
		mTvNoNeversenedMessage = (TextView) findViewById(R.id.select_receive_neverSend);
		// 设置默认图片
		mLoadProgress = (LoadingProgress) findViewById(R.id.progress);
		mPullRefreshListView.setAdapter(selectMessageAdapter);
		//----------------点击了筛选按钮里面的内容---------------------
		mFilterClickListener = new FilterPopupWindow.FitlerClickListener() {
			@Override
			public void onClick(View view) {
				switch (view.getId()) {
					case R.id.filter_sended:
						isClickWhichInFilter = "1";
						selectNone();
						sendedReceiver();
						NoReceiverGone();
						reRequestData(true, "1");
						mFilterPopupWindow.dismiss();
						break;
					case R.id.filter_never_Send:
						isClickWhichInFilter = "2";
						selectNone();
						neverSend();
						NoReceiverGone();
						reRequestData(true, "2");
						mFilterPopupWindow.dismiss();
						break;
					case R.id.filter_all:
						isClickWhichInFilter = "0";
						selectNone();
						allReceiver();
						NoReceiverGone();
						reRequestData(true, "0");
						mFilterPopupWindow.dismiss();
						break;
				}
			}
		};
		//----------------点击了筛选按钮里面的内容---------------------
		initFilterPopupWindow();

	}

	private void NoReceiverGone() {
		mLlSelectNone.setVisibility(View.GONE);
		mTvNosenedMessage.setVisibility(View.GONE);
		mTvNoNeversenedMessage.setVisibility(View.GONE);
	}

	private void allReceiver() {
		mFilterPopupWindow.mFilterSended.setBackgroundResource(R.drawable.shape_sened_bg);
		mFilterPopupWindow.mFilterNeverSend.setBackgroundColor(Color.parseColor("#ffffff"));
		mFilterPopupWindow.mFilterAll.setBackgroundResource(R.drawable.shape_all_checked_bg);
	}

	private void neverSend() {
		mFilterPopupWindow.mFilterSended.setBackgroundResource(R.drawable.shape_sened_bg);
		mFilterPopupWindow.mFilterNeverSend.setBackgroundColor(Color.parseColor("#f2f2f2"));
		mFilterPopupWindow.mFilterAll.setBackgroundResource(R.drawable.shape_all_bg);
	}

	private void sendedReceiver() {
		mFilterPopupWindow.mFilterSended.setBackgroundResource(R.drawable.shape_sened_checked_bg);
		mFilterPopupWindow.mFilterNeverSend.setBackgroundColor(Color.parseColor("#ffffff"));
		mFilterPopupWindow.mFilterAll.setBackgroundResource(R.drawable.shape_all_bg);
	}


	private void initFilterPopupWindow() {
		mFilterPopupWindow = new FilterPopupWindow(mActivity, mFilterClickListener);
	}

	@Override
	protected void setEventsListeners() {
		mRlSelectAll.setOnClickListener(this);
		mTopRightTextLayout.setOnClickListener(this);
		mPullRefreshListView.setOnItemClickListener(this);
		mLlNextStep.setOnClickListener(this);
		mTvNosenedMessage.setOnClickListener(this);
		mTvNoNeversenedMessage.setOnClickListener(this);
	}

	@Override
	protected void initTitleData() {
		mTopBackLayout.setOnClickListener(new OnBack());
		mTopTitleTv.setText(R.string.select_receiver_title);
		mTopTitleTv.setTextColor(Color.parseColor("#666666"));
		mTopRightText.setText(R.string.select_receiver_right_title);
		mTopRightTextLayout.setVisibility(View.VISIBLE);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		allReceiver();
		reRequestData(false, "0");
		//点击了listview中的checkbox执行的方法
		selectMessageAdapter.setOnItemClickListener(new SelectMessageAdapter.OnItemClickListener() {
			@Override
			public void onClick(View view, int position, Map<Integer, Boolean> selectedAllMap) {
				selectedFlagPosition = selectedAllMap;
				if (selectedAllMap.size() >= 1) {
					mLlNextStep.setBackgroundColor(Color.parseColor("#da500e"));
					mLlNextStepText.setText("下一步(" + selectedAllMap.size() + ")");
				} else if (selectedAllMap.size() == 0) {
					mLlNextStep.setBackgroundColor(Color.parseColor("#cbcbcb"));
					mSeletAllImg2.setVisibility(View.INVISIBLE);
					mSeletAllImg1.setVisibility(View.VISIBLE);
					mLlNextStepText.setText("下一步");
				}
				if (leftMessageNums != null && selectedFlagPosition.size()  == Integer.parseInt(leftMessageNums)) {
					showToast("每天最多可给" + maxMessageNum + "个未对您使用私信卡的粉丝发消息哦", 0);
				}
			}
		});
		// 下拉刷新数据
		mPullRefreshListView.setTask(new Runnable() {
			@Override
			public void run() {
				// 请求主播数据
				reRequestData(false, isClickWhichInFilter);
			}
		});
		View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.a_common_list_header_hint, null);
		view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources()
				.getDimensionPixelSize(R.dimen.list_hintview_height)));
		mPullRefreshListView.setPullnReleaseHintView(view);
		// 设置正确的颜色
		mPullRefreshListView.setHeaderBackgroudColor(getResources().getColor(R.color.app_background));

		// 设置上滑动加载更多
		mListFooterLoadView = (ListFooterLoadView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.a_common_list_footer_loader_view, null);
		mListFooterLoadView.hide();
		mListFooterLoadView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ListFooterLoadView listFooterLoadView = (ListFooterLoadView) v;
				int status = listFooterLoadView.getStatus();
				if (status == ListFooterLoadView.STATUS_FAILED || status == ListFooterLoadView.STATUS_NOMORE) {
					listFooterLoadView.onLoadingStarted();
					isRefresh = false;
					requestPlazaData(page, isClickWhichInFilter);
				}
			}
		});
		mPullRefreshListView.addFooterView(mListFooterLoadView);
		mPullRefreshListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// if (mListView.getChildCount() >
				// mListView.getHeaderViewsCount()
				// + mListView.getFooterViewsCount()) {
				if (mListFooterLoadView.getParent() == mPullRefreshListView) {
					// 至少翻过一项，才有可能执行加载更过操作
					if (mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_HIDDEN
							&& mPullRefreshListView.getFirstVisiblePosition() > mPullRefreshListView.getHeaderViewsCount()) {
						mListFooterLoadView.onLoadingStarted();
						EvtLog.d(TAG, "滚动加载更多");
						isRefresh = false;
						requestPlazaData(page, isClickWhichInFilter);
					}
				} else {
					if (mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_FAILED
							|| mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_NOMORE) {
						mListFooterLoadView.hide();
					}
				}
				// }
			}
		});


		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new LoadingProgress.onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
				// 重新加载数据
				reRequestData(true, isClickWhichInFilter);
			}

			@Override
			public void onClick(View v) {
				mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
				reRequestData(true, isClickWhichInFilter);
			}
		});
		mPullRefreshListView.setEmptyView(mLoadProgress);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.MSG_SELECT_RECEIVER_FAILED:
				mPullRefreshListView.notifyTaskFinished();
				if (selectMessageAdapter.isEmpty()) {
					String text = mActivity.getString(R.string.a_loading_failed);
					mLoadProgress.Failed(text, 0);
					mLlSelectNone.setVisibility(View.GONE);
					mTvNosenedMessage.setVisibility(View.GONE);
					mTvNoNeversenedMessage.setVisibility(View.GONE);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
					mListFooterLoadView.onLoadingFailed();
					mLlSelectNone.setVisibility(View.GONE);
					mTvNosenedMessage.setVisibility(View.GONE);
					mTvNoNeversenedMessage.setVisibility(View.GONE);
				}
				break;

			case MsgTypes.MSG_SELECT_RECEIVER_SUCCESS:
				Object[] objects = (Object[]) msg.obj;
				boolean isRefreh = (Boolean) objects[0];
				Map<String, Object> mListData = (Map<String, Object>) objects[1];
				String dataInfo = (String) mListData.get("list");
				maxMessageNum = (String) mListData.get("maxMessageNum");
				leftMessageNums = (String) mListData.get("leftMessageNum");
				String mCanSendNumCounts = (String) mListData.get("userNum");
				mFilterPopupWindow.mFilterAllTextNum.setText("(" + mCanSendNumCounts + ")");
				selectMessageAdapter.getleftMessageNums(Integer.parseInt(leftMessageNums));
				mTvFansCareText.setText("今日你还可以给" + leftMessageNums + "个粉丝发私信哦");
				try {
					mSelectRecieverInfo = JSONParser.parseMulti(dataInfo);

					/**
					 * 这里只要执行了下拉刷新，就默认选中全选按钮，将所以的数据清空，只显示第一页最新的数据
					 */
					if (isRefreh) { // 初始化或者下拉刷新模式
						if (mSelectRecieverInfo.size() == 0) {
							if ("0".equals(isClickWhichInFilter)) {
								mLlSelectNone.setVisibility(View.VISIBLE);
								mTvNosenedMessage.setVisibility(View.GONE);
								mTvNoNeversenedMessage.setVisibility(View.GONE);
							} else if ("1".equals(isClickWhichInFilter)) {
								mLlSelectNone.setVisibility(View.GONE);
								mTvNosenedMessage.setVisibility(View.VISIBLE);
								mTvNoNeversenedMessage.setVisibility(View.GONE);
							} else if ("2".equals(isClickWhichInFilter)) {
								mLlSelectNone.setVisibility(View.GONE);
								mTvNosenedMessage.setVisibility(View.GONE);
								mTvNoNeversenedMessage.setVisibility(View.VISIBLE);
							}
						} else {
							mLlSelectNone.setVisibility(View.GONE);
							mTvNosenedMessage.setVisibility(View.GONE);
							mTvNoNeversenedMessage.setVisibility(View.GONE);
						}
						mSeletAllImg1.setVisibility(View.VISIBLE);
						mSeletAllImg2.setVisibility(View.INVISIBLE);
						mLlNextStepText.setText("下一步");
						mLlNextStep.setBackgroundColor(Color.parseColor("#cbcbcb"));
						mPullRefreshListView.notifyTaskFinished(); // 收起正在刷新HeaderView
						selectedFlagPosition.clear();
						selectMessageAdapter.clearData();
						selectMessageAdapter.addData(mSelectRecieverInfo);
						selectMessageAdapter.notifyDataSetChanged();
					} else { // 加载更多数据模式
						if (mSelectRecieverInfo.isEmpty()) {
							mListFooterLoadView.onNoMoreData();
						} else {
							// 如果点击的foot加载第一页的数据，重新更新adapter数据
							if (page == 1) {
								// 隐藏ListView的FootView
								mListFooterLoadView.hide();
								selectMessageAdapter.clearData();
								selectMessageAdapter.addData(mSelectRecieverInfo);
							} else {
								// 隐藏ListView的FootView
								mListFooterLoadView.hide();
								selectMessageAdapter.addData(mSelectRecieverInfo);
								if (leftMessageNums != null && mSeletAllImg2.getVisibility() == View.VISIBLE && selectedFlagPosition.size() < Integer.parseInt(leftMessageNums)) {
									mSeletAllImg2.setVisibility(View.INVISIBLE);
									mSeletAllImg1.setVisibility(View.VISIBLE);
								}
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				// 不管有没有数据，只要请求成功了都要将其隐藏
				mLoadProgress.Hide();
				break;
		}

	}

	/**
	 * 重新请求数据
	 *
	 * @param clearAdapter 请求之前是否先清空UI
	 */
	public void reRequestData(boolean clearAdapter, String filter) {
		// 初始化loading(正在加载...)
		// 重新初始化请求页面
		page = 0;
		// 清空界面数据
		if (clearAdapter) {
			mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
			selectMessageAdapter.clearData();
			selectMessageAdapter.notifyDataSetChanged();
		}
		isRefresh = true;
		requestPlazaData(page, filter);
	}

	/**
	 * 向服务端请求最新回复列表数据
	 */
	private void requestPlazaData(int page, String filter) {
		BusinessUtils.getSelectRecieverList(mActivity, page, filter, new SelectReceivedCallBack());
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.select_receive_all:
				if (mSeletAllImg1.getVisibility() == View.VISIBLE) {
					if (leftMessageNums != null && mPullRefreshListView.getLastVisiblePosition() > Integer.parseInt(leftMessageNums)) {
						UiHelper.showConfirmDialog(this, "粉丝数已经超出可发送私信名额  是否默认选中前" + leftMessageNums + "名", R.string.cancel,
								R.string.select_all, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {

									}
								}, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										selectAll();
									}
								});
					} else {
						selectAll();
					}

				} else {
					selectNone();
				}

				break;
			case R.id.top_right_text_bg:
				//点击了筛选的按钮
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickSiftingInFansPage", null);
				mFilterPopupWindow.showPopupWindow(view);
				break;

			case R.id.select_receive_next_step:
				if (selectedFlagPosition.size() == 0) {
					return;
				} else {
					List<Map<String, String>> selectedAllList = selectMessageAdapter.getSelectedAllList();
				}
				break;
			case R.id.select_receive_sended:
				if (mTvNosenedMessage.getVisibility() == View.VISIBLE) {
					mFilterPopupWindow.mFilterAll.callOnClick();
				}
				break;
			case R.id.select_receive_neverSend:
				if (mTvNoNeversenedMessage.getVisibility() == View.VISIBLE) {
					mFilterPopupWindow.mFilterAll.callOnClick();
				}
				break;
		}
	}

	private void selectAll() {
		mSeletAllImg1.setVisibility(View.INVISIBLE);
		mSeletAllImg2.setVisibility(View.VISIBLE);
		int selectItemNum = selectMessageAdapter.initCheckBox(true);
		selectedFlagPosition = selectMessageAdapter.getselectedAllMap();
		selectMessageAdapter.notifyDataSetChanged();
		if (selectedFlagPosition.size() == 0) {
			mLlNextStep.setBackgroundColor(Color.parseColor("#cbcbcb"));
			mLoadProgress.Hide();
			mSeletAllImg2.setVisibility(View.INVISIBLE);
			mSeletAllImg1.setVisibility(View.VISIBLE);
			mLlNextStepText.setText("下一步");
		} else {
			mLlNextStep.setBackgroundColor(Color.parseColor("#da500e"));
			mLlNextStepText.setText("下一步(" + selectItemNum + ")");
		}
	}

	private void selectNone() {
		mSeletAllImg1.setVisibility(View.VISIBLE);
		mSeletAllImg2.setVisibility(View.INVISIBLE);
		selectMessageAdapter.initCheckBox(false);

		selectMessageAdapter.notifyDataSetChanged();
		mLlNextStepText.setText("下一步");
		mLlNextStep.setBackgroundColor(Color.parseColor("#cbcbcb"));
	}


	/**
	 * pullRefreshListView的点击事件
	 */
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {


	}

	private class OnBack implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			AppConfig.getInstance().updateIsBackFromSelectedReceiver(false);
			onBackPressed();
		}
	}

	private class SelectReceivedCallBack implements CallbackDataHandle {
		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FanListCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_SELECT_RECEIVER_SUCCESS;
					Object[] objects = new Object[]{isRefresh,
							JSONParser.parseOne((JSONObject) result)};
					msg.obj = objects;
					// 下次请求的页面数
					page++;
					sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_SELECT_RECEIVER_FAILED;
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}
	}

}
