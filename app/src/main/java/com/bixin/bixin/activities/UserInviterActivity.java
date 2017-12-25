package com.bixin.bixin.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import com.bixin.bixin.App;
import tv.live.bx.R;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.adapters.UserInviterListAdater;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Consts;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.WebConstants;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;

public class UserInviterActivity extends BaseFragmentActivity implements OnClickListener, OnRefreshListener {

	public static final String USER_ID = "user_id";
	private TextView mUserId;
	private EditText mInviteId;
	private TextView mInviteTotalNum;
	private TextView mTotalPaoPao;
	private TextView mTotalDianDian;
	private TextView mInviteIdTitle;
	private RelativeLayout mInviteLayout;

	private Button mInviteSubmit;

	private ListView mListView;
	private UserInviterListAdater mUserListAdater;
	private TextView mLoadProgress;

	private static final int MSG_SUBMIT_INVITE_SUCCESSFUL = 3;
	private static final int MSG_SUBMIT_INVITE_FAIL = 4;

	private static final int MSG_INVITE_INFO_SUCCESSFUL = 5;
	private static final int MSG_INVITE_INFO_FAIL = 6;

	// 下拉刷新
	private SwipeRefreshLayout swipeRefreshLayout;
	private AlertDialog mProgress;

	protected String shareContent; // "果酱||鲜肉大叔妖男Young，基腐宅萌有果酱,快来看****的直播，美CRY！！  ";
	protected String shareTitle; // "果酱直播";
	protected String shareUrl; // "http://www.guojiang.tv";

	private String mUserInfoId;

	/**
	 * 之前忘记使用这些方法了，这个类暂时不用了
	 */
	@Override
	protected int getLayoutRes() {
		return R.layout.activity_user_inviter;
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Intent intent = getIntent();
		if (intent != null) {
			mUserInfoId = intent.getStringExtra(USER_ID);
			mUserId.setText(getResources().getString(R.string.invite_me_id, mUserInfoId));
		}
		mTotalPaoPao.setText("克拉总计：");
		SpannableString loFrom = new SpannableString("0");
		loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#ff2a00")), 0, loFrom.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		mTotalPaoPao.append(loFrom);
		mTotalPaoPao.append(" 个");

		mTotalDianDian.setText("点点总计：");
		loFrom = new SpannableString("0");
		loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#ff2a00")), 0, loFrom.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		mTotalDianDian.append(loFrom);
		mTotalDianDian.append(" 个");

		initShareInfo();
		reRequestData();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	protected void initMembers() {

		mUserId = (TextView) findViewById(R.id.invite_me_id);
		mInviteTotalNum = (TextView) findViewById(R.id.invite_total_num);
		mTotalPaoPao = (TextView) findViewById(R.id.invite_paopao_coin);
		mTotalDianDian = (TextView) findViewById(R.id.invite_diandian_coin);
		mInviteSubmit = (Button) findViewById(R.id.invite_submit);
		mInviteId = (EditText) findViewById(R.id.invite_id);
		mInviteIdTitle = (TextView) findViewById(R.id.invite_id_title);

		mInviteLayout = (RelativeLayout) findViewById(R.id.invite_id_layout);

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		swipeRefreshLayout.setOnRefreshListener(this);

		swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		mInviteTotalNum.setText(getResources().getString(R.string.invite_me_invite_title, "0"));
		initTitle();
		initListView();
	}

	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.invite_title);
		mTopBackLayout.setOnClickListener(this);
		mTopRightImage.setImageResource(R.drawable.btn_invite_top_selector);
		mTopRightImageLayout.setVisibility(View.VISIBLE);
	}

	public void initWidgets() {
		mProgress = Utils.showProgress(mActivity);
	}

	protected void setEventsListeners() {
		mTopBackLayout.setOnClickListener(this);
		mTopRightImageLayout.setOnClickListener(this);
		mInviteSubmit.setOnClickListener(this);
	}

	/**
	 * 初始化下拉刷新ListView
	 * 
	 * @param v
	 * @param inflater
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listview);
		mUserListAdater = new UserInviterListAdater(mActivity);
		mListView.setAdapter(mUserListAdater);
		// 设置默认图片
		mLoadProgress = (TextView) findViewById(R.id.list_empty);
		mListView.setEmptyView(mLoadProgress);
	}

	private void updateData(Map<String, Object> data) {
		if (data != null) {
			// 如果不允许，则隐藏
			if ("-1".equals(data.get("referrer"))) {
				mInviteLayout.setVisibility(View.GONE);
				mInviteIdTitle.setVisibility(View.GONE);
			} else if ("0".equals(data.get("referrer"))) {
				mInviteLayout.setVisibility(View.VISIBLE);
				mInviteIdTitle.setVisibility(View.VISIBLE);
			} else {
				mInviteLayout.setVisibility(View.VISIBLE);
				mInviteIdTitle.setVisibility(View.VISIBLE);
				mInviteId.setText((String) data.get("referrer"));
				mInviteId.setEnabled(false);
				mInviteSubmit.setVisibility(View.INVISIBLE);
			}

			mUserListAdater.clearData();
			mUserListAdater.addData((List<Map<String, Object>>) data.get("users"));
			mInviteTotalNum.setText(getResources().getString(R.string.invite_me_invite_title, data.get("user_total")));

			mTotalPaoPao.setText("克拉总计：");
			SpannableString loFrom = new SpannableString((String) data.get("coin_total"));
			loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#ff2a00")), 0, loFrom.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			mTotalPaoPao.append(loFrom);
			mTotalPaoPao.append(" 个");

			mTotalDianDian.setText("点点总计：");
			loFrom = new SpannableString((String) data.get("low_coin_total"));
			loFrom.setSpan(new ForegroundColorSpan(Color.parseColor("#ff2a00")), 0, loFrom.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			mTotalDianDian.append(loFrom);
			mTotalDianDian.append(" 个");
		}
	}

	private void dismissDialog() {
		if (mProgress != null && mProgress.isShowing())
			mProgress.dismiss();
	}

	protected void handleMessage(Message msg) {
		dismissDialog();
		switch (msg.what) {
		case MSG_SUBMIT_INVITE_SUCCESSFUL:
			UiHelper.showToast(mActivity, "提交成功");
			mInviteId.setEnabled(false);
			mInviteSubmit.setVisibility(View.INVISIBLE);
			break;
		case MSG_SUBMIT_INVITE_FAIL:
			Bundle bundle = msg.getData();
			UiHelper.showToast(mActivity, bundle.getString("errorMsg"));
			break;

		case MSG_INVITE_INFO_SUCCESSFUL:
			swipeRefreshLayout.setRefreshing(false);
			Map<String, Object> lmVerInfo = (Map<String, Object>) msg.obj;
			updateData(lmVerInfo);
			break;
		case MSG_INVITE_INFO_FAIL:
			swipeRefreshLayout.setRefreshing(false);
			Bundle bundle2 = msg.getData();
			UiHelper.showToast(mActivity, bundle2.getString("errorMsg"));
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.top_left:
			onBackPressed();
			break;
		case R.id.top_right:
			MobclickAgent.onEvent(App.mContext, "clickInviteButtonInInviteReward");
			Map<String, String> shareInfo = new HashMap<String, String>();
			shareInfo.put(ShareDialogActivity.Share_Content, shareContent);
			shareInfo.put(ShareDialogActivity.Share_Title, shareTitle);
			shareInfo.put(ShareDialogActivity.Share_Url, shareUrl);
			shareInfo.put(ShareDialogActivity.Share_Dialog, String.valueOf(true));
			ActivityJumpUtil.toShareActivity(mActivity, shareInfo);
			break;
		case R.id.invite_submit:
			if (TextUtils.isEmpty(mInviteId.getText().toString())) {
				UiHelper.showToast(mActivity, "请填写邀请人ID");
			} else {
				mProgress = Utils.showProgress(mActivity);
				BusinessUtils.submitInvite(mActivity, mInviteId.getText().toString(),
						new SubmitInviteCallbackDataHandle(UserInviterActivity.this));
			}
			break;
		default:
			break;
		}

	}

	/** 初始化分享信息 */
	private void initShareInfo() {
		shareContent = String.format(WebConstants.SHARE_INVATE_CONTENT, mUserInfoId);
		shareUrl = String.format(WebConstants.getFullWebMDomain(WebConstants.SHARE_INVATE_URL), mUserInfoId);
		shareTitle = Consts.SHARE_TITLE; // "果酱";
	}

	/**
	 * 请求任务数据
	 */
	private void reRequestData() {
		BusinessUtils.getInviteInfo(mActivity, new GetInviterInfoReceiverListener(UserInviterActivity.this));
	}

	@Override
	public void onRefresh() {
		reRequestData();
	}

	/**
	 * Function: TODO ADD FUNCTION. <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private class GetInviterInfoReceiverListener implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public GetInviterInfoReceiverListener(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetInviterInfoReceiverListener success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MSG_INVITE_INFO_SUCCESSFUL;
					msg.obj = JSONParser.parseMultiInSingle((JSONObject) result, new String[]{"users"});
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			} else {
				msg.what = MSG_INVITE_INFO_FAIL;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				BaseFragmentActivity fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}

		}
	}

	/**
	 * Function: TODO ADD FUNCTION. <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class SubmitInviteCallbackDataHandle implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public SubmitInviteCallbackDataHandle(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "SubmitInviteCallbackDataHandle success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				msg.what = MSG_SUBMIT_INVITE_SUCCESSFUL;
				BaseFragmentActivity fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			} else {
				msg.what = MSG_SUBMIT_INVITE_FAIL;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				BaseFragmentActivity fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}

		}
	}

}
