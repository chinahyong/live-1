package com.bixin.bixin.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bixin.bixin.App;
import com.umeng.analytics.MobclickAgent;

import com.framework.net.impl.CallbackDataHandle;
import tv.live.bx.R;
import com.bixin.bixin.base.act.BaseFragmentActivity;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.config.UserInfoConfig;

public class AnimSettingsActivity extends BaseFragmentActivity implements View.OnClickListener {
	private RelativeLayout mRlSuperior, mRlNone;
	private ImageView mIvSuperior, mIvNone;
	private TextView mTvSuperior, mTvNone;
	private AlertDialog moProgress;


	@Override
	protected int getLayoutRes() {
		return R.layout.activity_anim_settings;
	}

	@Override
	public void initWidgets() {
		mRlSuperior = (RelativeLayout) findViewById(R.id.anim_settings_rl_superior);
		mRlNone = (RelativeLayout) findViewById(R.id.anim_settings_rl_none);
		mIvSuperior = (ImageView) findViewById(R.id.anim_settings_superior_checked);
		mIvNone = (ImageView) findViewById(R.id.anim_settings_none_checked);
		mTvSuperior = (TextView) findViewById(R.id.anim_settings_tv_superior);
		mTvNone = (TextView) findViewById(R.id.anim_settings_tv_none);
		initTitle();
	}

	@Override
	protected void setEventsListeners() {
		mRlSuperior.setOnClickListener(this);
		mRlNone.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		// 用户等级(后台从0开始下发)
		// 当前入场动效：隐身、高级
		// 低于18级，无权限设置动效
		if (UserInfoConfig.getInstance().level < 17) {
			setEnable(false);
			mIvNone.setVisibility(View.GONE);
			mIvSuperior.setVisibility(View.GONE);
		} else {
			setEnable(true);
			// 如果为false：隐身 true：高级
			if (UserInfoConfig.getInstance().lowkeyEnter) {
				mIvSuperior.setVisibility(View.GONE);
				mIvNone.setVisibility(View.VISIBLE);
			} else {
				mIvSuperior.setVisibility(View.VISIBLE);
				mIvNone.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.anim_setting);
		mTopBackLayout.setOnClickListener(new OnBack());
	}

	private void setEnable(boolean enable) {
		mRlSuperior.setClickable(enable);
		mRlNone.setClickable(enable);
		mTvSuperior.setEnabled(enable);
		mTvNone.setEnabled(enable);
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
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.anim_settings_rl_superior:
				// 如果当前选中未变化，则无需执行下面操作
				if (mIvSuperior.isShown()) {
					return;
				}
				MobclickAgent.onEvent(App.mContext, "chooseAdvancedSpecialEffectOfEnterBroadcast");
				break;
			case R.id.anim_settings_rl_none:
				// 如果当前选中未变化，则无需执行下面操作
				if (mIvNone.isShown()) {
					return;
				}
				MobclickAgent.onEvent(App.mContext, "chooseAnonymousApproach");
				break;
		}
		showProgress();
		BusinessUtils.comeInAnimSet(mActivity, new AnimCallbackData());
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		dismissProgress();
		switch (msg.what) {
			case MsgTypes.MSG_BIND_SUCCESS:
				mIvSuperior.setVisibility(mIvSuperior.isShown() ? View.GONE : View.VISIBLE);
				mIvNone.setVisibility(mIvNone.isShown() ? View.GONE : View.VISIBLE);
				break;
			case MsgTypes.MSG_BIND_FAILED:
				showTips(String.valueOf(msg.obj));
				break;
		}
	}

	private class OnBack implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	}

	private class AnimCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_BIND_SUCCESS;
					msg.obj = errorMsg;
					// 如果fragment未回收，发送消息
					sendMsg(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				msg.what = MsgTypes.MSG_BIND_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				// 如果fragment未回收，发送消息
				sendMsg(msg);
			}
		}
	}
}
