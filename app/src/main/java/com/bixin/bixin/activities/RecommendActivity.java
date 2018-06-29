package com.bixin.bixin.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;

import com.bixin.bixin.App;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.framework.net.impl.CallbackDataHandle;
import tv.live.bx.R;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.adapters.RecommendListAdapter;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.OperationHelper;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.ui.LoadingProgress;
import com.bixin.bixin.ui.PullRefreshListView;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;

/**
 * @author Live
 * @version 2016/10/27 2.7.0
 * @title RecommendActivity Description:首次登陆，推荐关注
 */
public class RecommendActivity extends BaseFragmentActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
	private Button mBtnCommit;
	private ImageView mIvClose;
	private AlertDialog moProgress;
	private PullRefreshListView mListView;
	private RecommendListAdapter mAdapter;
	private LoadingProgress mLoadProgress;        //加载loading

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_recommend;
	}

	@Override
	public void initWidgets() {
		mBtnCommit = (Button) findViewById(R.id.recommend_focus_commit);
		mIvClose = (ImageView) findViewById(R.id.recommend_focus_close);
		mListView = (PullRefreshListView) findViewById(R.id.recommend_focus_list);
		initList();
	}

	@Override
	protected void setEventsListeners() {
		mListView.setOnItemClickListener(this);
		mIvClose.setOnClickListener(this);
		mBtnCommit.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		requestData();
	}

	@Override
	protected void initMembers() {
		super.initMembers();
		mAdapter = new RecommendListAdapter(mActivity);
	}

	private void requestData() {
		BusinessUtils.getRecommendAttentions(mActivity, new RecommendCallbackData());
	}

	/**
	 * 初始化listview
	 */
	private void initList() {
		mListView.setAdapter(mAdapter);
		// 设置默认图片
		mLoadProgress = (LoadingProgress) findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new LoadingProgress.onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				requestData();
			}

			@Override
			public void onClick(View v) {
				mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				requestData();
			}
		});
		mListView.setEmptyView(mLoadProgress);
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		dismissProgress();
		switch (msg.what) {
			case MsgTypes.MSG_LOAD_SUCCESS:
				List<Map<String, Object>> mData = (List<Map<String, Object>>) msg.obj;
				mListView.notifyTaskFinished();
				mAdapter.clearData();
				mAdapter.addData(mData);
				// 设置没有数据的EmptyView
				String text = mActivity.getString(R.string.a_list_data_empty);
				mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
				break;
			case MsgTypes.MSG_LOAD_FAILED:
				mListView.notifyTaskFinished();
				if (mAdapter.isEmpty()) {
					mLoadProgress.Failed(mActivity.getString(R.string.a_loading_failed), 0);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
					mLoadProgress.Hide();
				}
				break;
			case MsgTypes.MSG_BIND_SUCCESS:
				mBtnCommit.setEnabled(true);
				// 关注成功跳转主界面
				if (!TextUtils.isEmpty(String.valueOf(msg.obj))) {
					showTips(String.valueOf(msg.obj));
				}
				ActivityJumpUtil.welcomeToMainActivity(this);
				break;
			case MsgTypes.MSG_BIND_FAILED:
				mBtnCommit.setEnabled(true);
				// 关注失败留在此界面
				showTips(String.valueOf(msg.obj));
				break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.recommend_focus_close:
				MobclickAgent.onEvent(App.mContext, "clickCloseInRecommendBroadcasterPage");
				ActivityJumpUtil.welcomeToMainActivity(this);
				break;
			case R.id.recommend_focus_commit:
				MobclickAgent.onEvent(App.mContext, "clickFinishInRecommendBroadcasterPage");
				// 如果没有选中任何直接跳转主页，不做无效请求
				if (mAdapter.getCheckedMap().size() <= 0) {
					ActivityJumpUtil.welcomeToMainActivity(this);
					return;
				}
				showProgress();
				List<String> uids = new ArrayList<>();
				for (String value : mAdapter.getCheckedMap().values()) {
					uids.add(value);
				}
				mBtnCommit.setEnabled(false);
				// 提交推荐关注
				BusinessUtils.commitRecommendAttentions(mActivity, uids, new CommitRecommendCallbackData());
				break;
		}
	}

	private void showProgress() {
		if (moProgress != null && moProgress.isShowing()) {
			return;
		}
		moProgress = Utils.showProgress(mActivity);
	}

	private void dismissProgress() {
		if (moProgress != null && moProgress.isShowing()) {
			moProgress.dismiss();
		}
	}


	@Override
	public void onBackPressed() {
		ActivityJumpUtil.welcomeToMainActivity(this);
	}

	/**
	 * item点击事件
	 *
	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 */
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
		/* umeng后台出现 outOfBounds，先捕获处理，暂未定位到原因 */
		try {
			OperationHelper.onEvent(App.mContext, "clickAnchorIconInRecommendBroadcasterPage", null);
			@SuppressWarnings("unchecked")
			Map<String, String> lmItem = (Map<String, String>) mAdapter.getItem(position
					- mListView.getHeaderViewsCount());
			Map<String, String> personInfo = new HashMap<>();
			personInfo.put("id", lmItem.get("mid"));
			ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 推荐关注列表
	 */
	private class RecommendCallbackData implements CallbackDataHandle {
		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_LOAD_SUCCESS;
					msg.obj = JSONParser.parseMulti((JSONArray) result);
					// 如果fragment未回收，发送消息
					sendMsg(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				msg.what = MsgTypes.MSG_LOAD_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				// 如果fragment未回收，发送消息
				sendMsg(msg);
			}
		}
	}

	/**
	 * 推荐关注列表
	 */
	private class CommitRecommendCallbackData implements CallbackDataHandle {
		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = new Message();
			if (success) {
				msg.what = MsgTypes.MSG_BIND_SUCCESS;
				msg.obj = errorMsg;
			} else {
				msg.what = MsgTypes.MSG_BIND_FAILED;
				msg.obj = errorMsg;
			}
			sendMsg(msg);
		}
	}
}
