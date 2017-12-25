package com.bixin.bixin.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lonzh.lib.network.JSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.R;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.library.util.DateUtil;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;
/**
 * Title: AnchorActivity.java Description:主播报名界面
 * @author Live
 * @version 2.1.2,2016.4.1
 */
public class AnchorActivity extends BaseFragmentActivity implements OnClickListener, OnRefreshListener {

	private static final int REQUEST_CODE = 200; // startActivity request code
	// mTvStep2Time,mTvStep1
	private TextView mTvVideo, mTvSubmit, mTvStep2, mTvStep3, mTvStep1Time, mTvStep3Time, mTvMsgSuccess, mTvMsgContent,
			mTvPhoneAddr;
	private EditText mEtPhone, mEtQq, mEtNote;
	private ImageView mIvVideo, mIvVideoClick, mIvPhone, mIvQq, mIvPhoneClear, mIvQqClear, mIvNote, mIvNoteClear;
	private RelativeLayout mLayoutVideo;
	private LinearLayout mLayoutContent, mLayoutResult;
	private SwipeRefreshLayout mSwipeRefreshLayout;

	private AlertDialog mProgress; // loadingDialog

	private Map<String, String> mAnchorInfo; // 报名信息
	private String mFilePath; // 本地video url
	private String mFileUrl; // 网络video url
	private int mStatus = -1; // 报名状态 -1:未报名 0：审核中 1：未通过 2：已通过
	/** -1未报名 0 待审核 1未通过 2已通过 */
	/** 主播状态：未报名 */
	public static final int ANCHOR_STATUS_UNSIGN = -1;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_anchor;
	}

	@Override
	protected void initMembers() {

		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		mSwipeRefreshLayout.setOnRefreshListener(this);
		/* 方法过时，setColorScheme也是在调用setColorSchemeResources */
		mSwipeRefreshLayout.setColorSchemeResources(R.color.a_bg_color_da500e, android.R.color.holo_green_light,
				android.R.color.holo_orange_light, android.R.color.holo_red_light);
		// mTvStep1 = (TextView) findViewById(R.id.anchor_step_1_status);
		mTvStep2 = (TextView) findViewById(R.id.anchor_step_2_status);
		mTvStep3 = (TextView) findViewById(R.id.anchor_step_3_status);
		mTvStep1Time = (TextView) findViewById(R.id.anchor_step_1_date);
		// mTvStep2Time = (TextView) findViewById(R.id.anchor_step_2_date);
		mTvStep3Time = (TextView) findViewById(R.id.anchor_step_3_date);

		mTvPhoneAddr = (TextView) findViewById(R.id.anchor_tv_mobile_addr);
		mEtNote = (EditText) findViewById(R.id.anchor_et_note);
		mEtPhone = (EditText) findViewById(R.id.anchor_et_phone);
		mEtQq = (EditText) findViewById(R.id.anchor_et_qq);
		mIvPhone = (ImageView) findViewById(R.id.anchor_iv_phone);
		mIvQq = (ImageView) findViewById(R.id.anchor_iv_qq);
		mIvNote = (ImageView) findViewById(R.id.anchor_iv_note);
		mIvNoteClear = (ImageView) findViewById(R.id.anchor_iv_note_clear);
		mIvPhoneClear = (ImageView) findViewById(R.id.anchor_iv_phone_clear);
		mIvQqClear = (ImageView) findViewById(R.id.anchor_iv_qq_clear);
		mIvVideo = (ImageView) findViewById(R.id.anchor_iv_video);
		mTvVideo = (TextView) findViewById(R.id.anchor_tv_video);
		mIvVideoClick = (ImageView) findViewById(R.id.anchor_iv_video_click);

		mTvMsgSuccess = (TextView) findViewById(R.id.anchor_message_success);
		mTvSubmit = (TextView) findViewById(R.id.anchor_tv_submit);
		mLayoutVideo = (RelativeLayout) findViewById(R.id.anchor_rl_video);
		mTvMsgContent = (TextView) findViewById(R.id.anchor_ll_message_content);
		mLayoutResult = (LinearLayout) findViewById(R.id.anchor_ll_content_result);
		mLayoutContent = (LinearLayout) findViewById(R.id.anchor_ll_content);

	}

	@Override
	public void initWidgets() {
		initTitle();
		mSwipeRefreshLayout.post(new Runnable() {
			@Override
			public void run() {
				mSwipeRefreshLayout.setRefreshing(true);
			}
		});
	}

	@Override
	protected void initTitleData() {
		mTopBackLayout.setOnClickListener(new OnBack());
		mTopTitleTv.setText(getString(R.string.anchor_title));
	}

	@Override
	protected void setEventsListeners() {
		mEtPhone.addTextChangedListener(phoneWatcher);
		mEtQq.setOnFocusChangeListener(qqFocusChange);
		mEtPhone.setOnFocusChangeListener(mobileFocusChange);
		mEtNote.setOnFocusChangeListener(noteFocusChange);
		mIvPhoneClear.setOnClickListener(this);
		mIvQqClear.setOnClickListener(this);
		mIvNoteClear.setOnClickListener(this);
		mTvSubmit.setOnClickListener(this);
		mLayoutVideo.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null && !TextUtils.isEmpty(bundle.get("status").toString())) {
			mStatus = Integer.parseInt(bundle.get("status").toString());
			mTvStep1Time.setText(DateUtil.getFormatTime(Long.parseLong(bundle.get("add_time").toString()),
					DateUtil.sdf5));
			// 审核中
			if (mStatus == 0) {
				initWidgetData(bundle);
				checking();
			} else {
				// 未通过
				mTvStep2.setText(getString(R.string.anchor_step_status_2_end));
				mTvStep3.setTypeface(Typeface.DEFAULT_BOLD);
				mTvStep3Time.setText(DateUtil.getFormatTime(Long.parseLong(bundle.get("update_time").toString()),
						DateUtil.sdf5));
				if (mStatus == 1) {
					initWidgetData(bundle);
				} else if (mStatus == 2)
					checkIn();
			}
		}

		BusinessUtils.getAnchorStatus(this, new AnchorStatusCallbackData());
	}

	@Override
	public void onRefresh() {
		BusinessUtils.getAnchorStatus(this, new AnchorStatusCallbackData());
	}

	/** 审核中、未通过 控件数据设置 */
	private void initWidgetData(Bundle bundle) {
		mEtPhone.setText(bundle.getString("mobile"));
		mEtQq.setText(bundle.getString("qq"));
		mEtNote.setText(bundle.getString("note"));
		mTvStep3.setText(getString(R.string.me_anchor_status_failure));
		if (!TextUtils.isEmpty(bundle.getString("video"))) {
			mFileUrl = bundle.getString("video");
			mIvVideoClick.setVisibility(View.VISIBLE);
			mTvVideo.setVisibility(View.GONE);
		} else {
			mIvVideoClick.setVisibility(View.GONE);
			mTvVideo.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 
	 */
	private void initWidgetDefault() {
		mTvPhoneAddr.setEnabled(true);
		mTvSubmit.setEnabled(true);
		mEtPhone.setEnabled(true);
		mEtQq.setEnabled(true);
		mEtNote.setEnabled(true);
		mIvPhone.setImageResource(R.drawable.ic_icon_phone_nor);
		mIvNote.setImageResource(R.drawable.ic_icon_anchor_note_nor);
		mIvQq.setImageResource(R.drawable.ic_icon_qq_nor);
		mIvVideo.setImageResource(R.drawable.ic_icon_camera_nor);
		mLayoutVideo.setBackgroundResource(R.drawable.ic_bg_border_normal);
	}
	/**
	 * 审核中、未通过 控件数据设置
	 * @param Map<String,String>
	 */
	private void initWidgetData(Map<String, String> data) {
		mEtPhone.setText(data.get("mobile"));
		mEtQq.setText(data.get("qq"));
		mEtNote.setText(data.get("note"));
		if (!TextUtils.isEmpty(data.get("video"))) {
			mFileUrl = data.get("video");
			mIvVideoClick.setVisibility(View.VISIBLE);
			mTvVideo.setVisibility(View.GONE);
		} else {
			mIvVideoClick.setVisibility(View.GONE);
			mTvVideo.setVisibility(View.VISIBLE);
		}
	}

	/** 审核中 控件状态设置 */
	private void checking() {
		mTvStep3Time.setText("");
		mTvStep3.setTypeface(Typeface.DEFAULT);
		mTvStep3.setText(getString(R.string.anchor_step_status_3));
		mTvStep2.setText(getString(R.string.anchor_step_status_2_ing));
		mTvPhoneAddr.setEnabled(false);
		mTvSubmit.setEnabled(false);
		mEtPhone.setEnabled(false);
		mEtQq.setEnabled(false);
		mEtNote.setEnabled(false);
		mIvPhone.setImageResource(R.drawable.ic_icon_phone_disable);
		mIvNote.setImageResource(R.drawable.ic_icon_anchor_note_pre);
		mIvQq.setImageResource(R.drawable.ic_icon_qq_disable);
		mIvVideo.setImageResource(R.drawable.ic_icon_camera_disable);
		mLayoutVideo.setBackgroundResource(R.drawable.ic_bg_border_enable);
		mIvPhoneClear.setVisibility(View.GONE);
		mIvQqClear.setVisibility(View.GONE);
		mIvNoteClear.setVisibility(View.GONE);
	}

	/** 已通过控件状态设置 */
	private void checkIn() {
		mTvStep3.setText(getString(R.string.me_anchor_status_sucess));
		mLayoutContent.setVisibility(View.INVISIBLE);
		mTvMsgContent.setVisibility(View.GONE);
		mLayoutResult.setVisibility(View.VISIBLE);
		mTvMsgSuccess.setVisibility(View.VISIBLE);
	}

	/** 接收Activity回调 AnchorCameraActivity回调视频地址 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				mFilePath = intent.getStringExtra("filePath");
				if (TextUtils.isEmpty(mFilePath) && TextUtils.isEmpty(mFileUrl)) {
					mTvVideo.setVisibility(View.VISIBLE);
					mIvVideoClick.setVisibility(View.GONE);
				} else {
					mTvVideo.setVisibility(View.GONE);
					mIvVideoClick.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mProgress != null && mProgress.isShowing())
			mProgress.dismiss();
	}
	/*********************************** 事件处理器 *************************************/
	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case MsgTypes.SUBMIT_ANCHOR_STATUS_SUCCESS:
			// 提交资料成功
			if (mProgress != null && mProgress.isShowing())
				mProgress.dismiss();
			Map<String, String> data = (Map<String, String>) msg.obj;
			mStatus = 0;
			checking();
			mTvStep1Time
					.setText(DateUtil.getFormatTime(Long.parseLong(data.get("add_time").toString()), DateUtil.sdf5));
			mFileUrl = data.get("video");
			mTvStep3Time.setText("");
			showTips(R.string.anchor_toast_commit_success);
			break;
		case MsgTypes.SUBMIT_ANCHOR_STATUS_FAILED:
			// 资料提交失败
			Bundle bundle = msg.getData();
			if (mProgress != null && mProgress.isShowing())
				mProgress.dismiss();
			showTips(bundle.getString("errorMsg"));
			break;
		case MsgTypes.GET_ANCHOR_STATUS_SUCCESS:
			mSwipeRefreshLayout.setRefreshing(false);
			mAnchorInfo = (Map<String, String>) msg.obj;
			if (Integer.parseInt(mAnchorInfo.get("status")) == -1) {
				return;
			} else
				mStatus = Integer.parseInt(mAnchorInfo.get("status"));
			mTvStep1Time.setText(DateUtil.getFormatTime(Long.parseLong(mAnchorInfo.get("add_time").toString()),
					DateUtil.sdf5));
			if (mStatus == 0) {
				initWidgetData(mAnchorInfo);
				checking();
			} else if (mStatus == 1) {
				mTvStep2.setText(getString(R.string.anchor_step_status_2_end));
				mTvStep3.setTypeface(Typeface.DEFAULT_BOLD);
				mTvStep3Time.setText(DateUtil.getFormatTime(Long.parseLong(mAnchorInfo.get("update_time").toString()),
						DateUtil.sdf5));
				initWidgetDefault();
				initWidgetData(mAnchorInfo);
			} else {
				mTvStep2.setText(getString(R.string.anchor_step_status_2_end));
				mTvStep3.setTypeface(Typeface.DEFAULT_BOLD);
				mTvStep3Time.setText(DateUtil.getFormatTime(Long.parseLong(mAnchorInfo.get("update_time").toString()),
						DateUtil.sdf5));
				checkIn();
			}
			break;
		case MsgTypes.GET_ANCHOR_STATUS_FAILED:
			mSwipeRefreshLayout.setRefreshing(false);
			Bundle bundle2 = msg.getData();
			if (bundle2 != null && !TextUtils.isEmpty(bundle2.getString("errorMsg"))) {
				UiHelper.showToast(mActivity, bundle2.getString("errorMsg"));
			}
			break;
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.anchor_iv_phone_clear:
			mEtPhone.setText("");
			break;
		case R.id.anchor_iv_qq_clear:
			mEtQq.setText("");
			break;
		case R.id.anchor_iv_note_clear:
			mEtNote.setText("");
			break;
		case R.id.anchor_rl_video:
			Bundle bundle = new Bundle();
			bundle.putInt("status", mStatus);
			bundle.putString("video", mFileUrl);
			if (mStatus == -1 || mStatus == 1)
				ActivityJumpUtil.toActivityAndBundle(this, AnchorCameraActivity.class, bundle, REQUEST_CODE);
			else if (mStatus == 0) {
				ActivityJumpUtil.toActivityAndBundle(this, AnchorCameraActivity.class, bundle, -1);
			}
			break;
		case R.id.anchor_tv_submit:
			if (TextUtils.isEmpty(mEtPhone.getText().toString().trim())) {
				mIvPhone.setImageResource(R.drawable.ic_icon_phone_err);
				mEtPhone.setHintTextColor(getResources().getColor(R.color.a_text_color_ff0000));
				return;
			} else if (mEtPhone.getText().toString().length() != 13) {
				showTips("o(╯□╰)o手机号是11位啦");
				return;
			}
			mIvPhone.setImageResource(R.drawable.ic_icon_phone_nor);
			mEtPhone.setHintTextColor(getResources().getColor(R.color.a_text_color_333333));
			if (TextUtils.isEmpty(mEtQq.getText().toString().trim())) {
				mIvQq.setImageResource(R.drawable.ic_icon_qq_err);
				mEtQq.setHintTextColor(getResources().getColor(R.color.a_text_color_ff0000));
				return;
			} else if (mEtQq.getText().toString().length() < 5 || mEtQq.getText().toString().length() > 12) {
				showTips("QQ号格式不对哟");
				return;
			}
			mIvQq.setImageResource(R.drawable.ic_icon_qq_nor);
			mEtQq.setHintTextColor(getResources().getColor(R.color.a_text_color_333333));
			if (TextUtils.isEmpty(mFilePath) && TextUtils.isEmpty(mFileUrl)) {
				mIvVideo.setImageResource(R.drawable.ic_icon_camera_err);
				showTips("请录制视频");
				return;
			}
			mIvVideo.setImageResource(R.drawable.ic_icon_camera_nor);
			mProgress = Utils.showProgress(this);
			BusinessUtils.submitAnchorInfo(this, mEtPhone.getText().toString(), mEtQq.getText().toString(), mEtNote
					.getText().toString(), TextUtils.isEmpty(mFilePath) ? "" : mFilePath, new SubmitAnchor());
			break;
		}
	}

	/** 手机文本监听 添加空格设置手机格式 */
	private TextWatcher phoneWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s == null || s.length() == 0)
				return;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < s.length(); i++) {
				if (i != 3 && i != 8 && s.charAt(i) == ' ') {
					continue;
				} else {
					sb.append(s.charAt(i));
					if ((sb.length() == 4 || sb.length() == 9) && sb.charAt(sb.length() - 1) != ' ') {
						sb.insert(sb.length() - 1, ' ');
					}
				}
			}
			if (!sb.toString().equals(s.toString())) {
				int index = start + 1;
				if (sb.charAt(start) == ' ') {
					if (before == 0) {
						index++;
					} else {
						index--;
					}
				} else {
					if (before == 1) {
						index--;
					}
				}
				mEtPhone.setText(sb.toString());
				mEtPhone.setSelection(index);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	private OnFocusChangeListener mobileFocusChange = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus)
				mIvPhoneClear.setVisibility(View.VISIBLE);
			else
				mIvPhoneClear.setVisibility(View.GONE);
		}
	};

	private OnFocusChangeListener noteFocusChange = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus)
				mIvNoteClear.setVisibility(View.VISIBLE);
			else
				mIvNoteClear.setVisibility(View.GONE);
		}
	};

	private OnFocusChangeListener qqFocusChange = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus)
				mIvQqClear.setVisibility(View.VISIBLE);
			else
				mIvQqClear.setVisibility(View.GONE);
		}
	};

	private class OnBack implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (!TextUtils.isEmpty(mFilePath))
				new File(mFilePath).delete();
			AnchorActivity.this.finish();
			onBackPressed();
		}
	}

	/** 提交资料回调 */
	private class SubmitAnchor implements CallbackDataHandle {
		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.SUBMIT_ANCHOR_STATUS_SUCCESS;
					Map<String, String> data = JSONParser.parseOne((JSONObject) result);
					msg.obj = data;
					// 如果fragment未回收，发送消息
					sendMsg(msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				msg.what = MsgTypes.SUBMIT_ANCHOR_STATUS_FAILED;
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				sendMsg(msg);
			}
		}
	}
	/** 获取报名状态 */
	private class AnchorStatusCallbackData implements CallbackDataHandle {
		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "AnchorStatusCallbackData " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.GET_ANCHOR_STATUS_SUCCESS;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					// 如果fragment未回收，发送消息
					sendMsg(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				msg.what = MsgTypes.GET_ANCHOR_STATUS_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				// 如果fragment未回收，发送消息
				sendMsg(msg);
			}
		}
	}
}
