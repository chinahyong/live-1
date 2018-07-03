package com.bixin.bixin.common.widget.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;

import tv.live.bx.R;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.config.UserInfoConfig;

/**
 * Title: CustomDialogBuilder.java</br> Description: 自定义对话框</br> Copyright:
 * Copyright (c) 2008</br>
 *
 * @version 1.0
 * @CreateDate 2014-7-8
 */
public class RedPacketDialogBuilder extends CustomDialogBuilder {
	private ImageView mIvClose, mIvHelp;    // 关闭按钮/帮助按钮
	private TextView mTvMount, mTvInvite, mTvEmptyMsg;    // 红包额度/邀请好友/今日领取完成文本
	private CheckBox mCbHide;    //隐藏提示
	private Button mBtnGet;        //立即领取按钮
	private RelativeLayout mLayoutGet, mLayoutEmpty;

	public RedPacketDialogBuilder(Context context, String money, boolean lastFlag) {
		super(context, R.layout.dialog_live_red_packet_layout);
		initWidget();
		initData(money, lastFlag);
	}

	private void initWidget() {
		mIvClose = (ImageView) mDialogView.findViewById(R.id.dialog_red_packet_close);
		mIvHelp = (ImageView) mDialogView.findViewById(R.id.dialog_red_packet_help);
		mTvMount = (TextView) mDialogView.findViewById(R.id.dialog_red_packet_mount);

		mLayoutGet = (RelativeLayout) mDialogView.findViewById(R.id.dialog_red_packet_get_layout);

		mLayoutEmpty = (RelativeLayout) mDialogView.findViewById(R.id.dialog_red_packet_empty_layout);
		mCbHide = (CheckBox) mDialogView.findViewById(R.id.dialog_red_packet_empty_cb);
		mTvInvite = (TextView) mDialogView.findViewById(R.id.dialog_red_packet_empty_invite);
		mTvEmptyMsg = (TextView) mDialogView.findViewById(R.id.dialog_red_packet_empty_tv);


		this.mDialog.setCanceledOnTouchOutside(true);
		this.mDialog.setCancelable(true);
	}

	/**
	 * 初始化数据
	 *
	 * @param money
	 * @param lastFlag
	 */
	private void initData(String money, boolean lastFlag) {
		// 判断是否开启了：不再提示
		// 本地存储显示状态
		// 领取红包余额
		double mount = (double) Long.parseLong(money) / 100;
		BigDecimal bg = new BigDecimal(String.valueOf(mount));
		mTvMount.setText(bg.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
		// 是否领取了当天最后一个红包
		if (lastFlag) {
			mLayoutGet.setVisibility(View.GONE);
			// 显示红包提示为false，但是却选择为显示，说明是初始化
			String isShowFlag = UserInfoConfig.getInstance().isShowRedMsgFlag;
			// 本地未做存储，则为默认选中 / 不再显示为true
			if (TextUtils.isEmpty(isShowFlag)) {
				mCbHide.setChecked(true);
				mCbHide.setVisibility(View.VISIBLE);
				UserInfoConfig.getInstance().updateRedPacketMsg("true");
				mTvEmptyMsg.setVisibility(View.VISIBLE);
			} else if (Utils.getBooleanFlag(isShowFlag)) {
				// 本地为true ， 选择了  不再显示
				mCbHide.setChecked(true);
				mCbHide.setVisibility(View.GONE);
				UserInfoConfig.getInstance().updateRedPacketMsg("true");
				mTvEmptyMsg.setVisibility(View.GONE);
			} else {
				mCbHide.setChecked(false);
				mCbHide.setVisibility(View.VISIBLE);
				mTvEmptyMsg.setVisibility(View.VISIBLE);
				UserInfoConfig.getInstance().updateRedPacketMsg("false");
			}
			mLayoutEmpty.setVisibility(View.VISIBLE);
		} else {
			mLayoutGet.setVisibility(View.VISIBLE);
			mLayoutEmpty.setVisibility(View.GONE);
		}
	}

	/**
	 * 按钮点击事件
	 */
	public void setOnEventClickListener(OnClickListener listener) {
		PositiveListener positiveListener = new PositiveListener(listener);
		mIvClose.setOnClickListener(positiveListener);
		mIvHelp.setOnClickListener(positiveListener);

		mLayoutGet.setOnClickListener(positiveListener);

		mTvInvite.setOnClickListener(positiveListener);
		mCbHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// 更新本地是否显示的值
				UserInfoConfig.getInstance().updateRedPacketMsg(String.valueOf(isChecked));
			}
		});
	}

	public class PositiveListener implements android.view.View.OnClickListener {

		private OnClickListener dialogListener;

		public PositiveListener(OnClickListener l) {
			dialogListener = l;
		}

		@Override
		public void onClick(View v) {
			if (dialogListener != null) {
				dialogListener.onClick(v);
			}
		}

	}

}
