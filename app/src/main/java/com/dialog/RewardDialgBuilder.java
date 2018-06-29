package com.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;

import tv.live.bx.R;
import com.bixin.bixin.ui.StrokeTextView;

/**
 * Title: RewardDialgBuilder.java</br> Description: 自定义对话框</br> Copyright:
 * Copyright (c) 2008</br>
 * @CreateDate 2014-7-8
 * @version 1.0
 */
public class RewardDialgBuilder {

	private Context mContext;

	/** The custom_body layout */
	private View mDialogView;

	private ImageView mImageClose;
	private ImageView mReward1;
	private TextView mRewardText1;
	private StrokeTextView[] mRewardMoneyArray = new StrokeTextView[7];
	private ImageView mReward2;
	private TextView mRewardText2;
	private ImageView mReward3;
	private TextView mRewardText3;
	private ImageView mReward4;
	private TextView mRewardText4;
	private ImageView mReward5;
	private TextView mRewardText5;
	private ImageView mReward6;
	private TextView mRewardText6;
	private ImageView mReward7;
	private TextView mRewardText7;

	private ImageView mRewardEtc;
	private ImageView mRewardSure;

	private Dialog mDialog;

	public RewardDialgBuilder(Context context, OnClickListener listener) {
		mContext = context;
		mDialogView = View.inflate(context, R.layout.dialog_login_reward_layout, null);
		mImageClose = (ImageView) mDialogView.findViewById(R.id.reward_close);
		mReward1 = (ImageView) mDialogView.findViewById(R.id.reward_1);
		mRewardText1 = (TextView) mDialogView.findViewById(R.id.reward_text_1);
		mRewardMoneyArray[0] = (StrokeTextView) mDialogView.findViewById(R.id.reward_money1);

		mReward2 = (ImageView) mDialogView.findViewById(R.id.reward_2);
		mRewardText2 = (TextView) mDialogView.findViewById(R.id.reward_text_2);
		mRewardMoneyArray[1] = (StrokeTextView) mDialogView.findViewById(R.id.reward_money2);

		mReward3 = (ImageView) mDialogView.findViewById(R.id.reward_3);
		mRewardText3 = (TextView) mDialogView.findViewById(R.id.reward_text_3);
		mRewardMoneyArray[2] = (StrokeTextView) mDialogView.findViewById(R.id.reward_money3);
		mReward4 = (ImageView) mDialogView.findViewById(R.id.reward_4);
		mRewardText4 = (TextView) mDialogView.findViewById(R.id.reward_text_4);
		mRewardMoneyArray[3] = (StrokeTextView) mDialogView.findViewById(R.id.reward_money4);
		mReward5 = (ImageView) mDialogView.findViewById(R.id.reward_5);
		mRewardText5 = (TextView) mDialogView.findViewById(R.id.reward_text_5);
		mRewardMoneyArray[4] = (StrokeTextView) mDialogView.findViewById(R.id.reward_money5);
		mReward6 = (ImageView) mDialogView.findViewById(R.id.reward_6);
		mRewardText6 = (TextView) mDialogView.findViewById(R.id.reward_text_6);
		mRewardMoneyArray[5] = (StrokeTextView) mDialogView.findViewById(R.id.reward_money6);
		mReward7 = (ImageView) mDialogView.findViewById(R.id.reward_7);
		mRewardText7 = (TextView) mDialogView.findViewById(R.id.reward_text_7);
		mRewardMoneyArray[6] = (StrokeTextView) mDialogView.findViewById(R.id.reward_money7);

		mRewardEtc = (ImageView) mDialogView.findViewById(R.id.reward_etc);
		mRewardSure = (ImageView) mDialogView.findViewById(R.id.reward_sure);
		mRewardSure.setOnClickListener(new PositiveListener(listener));
		mImageClose.setOnClickListener(new NegativeListener(null));
		mDialog = new Dialog(context, R.style.full_dialog);
	}

	public RewardDialgBuilder setReward1() {
		mReward1.setEnabled(false);
		mRewardText1.setEnabled(false);
		mRewardMoneyArray[0].setEnabled(false);

		return this;
	}

	public RewardDialgBuilder setReward2() {
		mReward1.setEnabled(false);
		mRewardText1.setEnabled(false);
		mRewardMoneyArray[0].setEnabled(false);
		mReward2.setEnabled(false);
		mRewardText2.setEnabled(false);
		mRewardMoneyArray[1].setEnabled(false);
		return this;
	}

	public RewardDialgBuilder setReward3() {
		mReward1.setEnabled(false);
		mRewardText1.setEnabled(false);
		mRewardMoneyArray[0].setEnabled(false);
		mReward2.setEnabled(false);
		mRewardText2.setEnabled(false);
		mRewardMoneyArray[1].setEnabled(false);
		mReward3.setEnabled(false);
		mRewardText3.setEnabled(false);
		mRewardMoneyArray[2].setEnabled(false);
		return this;
	}

	public RewardDialgBuilder setReward4() {
		mReward1.setEnabled(false);
		mRewardText1.setEnabled(false);
		mRewardMoneyArray[0].setEnabled(false);
		mReward2.setEnabled(false);
		mRewardText2.setEnabled(false);
		mRewardMoneyArray[1].setEnabled(false);
		mReward3.setEnabled(false);
		mRewardText3.setEnabled(false);
		mRewardMoneyArray[2].setEnabled(false);
		mReward4.setEnabled(false);
		mRewardText4.setEnabled(false);
		mRewardMoneyArray[3].setEnabled(false);
		return this;
	}

	public RewardDialgBuilder setReward5() {
		mReward1.setEnabled(false);
		mRewardText1.setEnabled(false);
		mRewardMoneyArray[0].setEnabled(false);
		mReward2.setEnabled(false);
		mRewardText2.setEnabled(false);
		mRewardMoneyArray[1].setEnabled(false);
		mReward3.setEnabled(false);
		mRewardText3.setEnabled(false);
		mRewardMoneyArray[2].setEnabled(false);
		mReward4.setEnabled(false);
		mRewardText4.setEnabled(false);
		mRewardMoneyArray[3].setEnabled(false);
		mReward5.setEnabled(false);
		mRewardText5.setEnabled(false);
		mRewardMoneyArray[4].setEnabled(false);
		return this;
	}

	public RewardDialgBuilder setReward6() {
		mReward1.setEnabled(false);
		mRewardText1.setEnabled(false);
		mRewardMoneyArray[0].setEnabled(false);
		mReward2.setEnabled(false);
		mRewardText2.setEnabled(false);
		mRewardMoneyArray[1].setEnabled(false);
		mReward3.setEnabled(false);
		mRewardText3.setEnabled(false);
		mRewardMoneyArray[2].setEnabled(false);
		mReward4.setEnabled(false);
		mRewardText4.setEnabled(false);
		mRewardMoneyArray[3].setEnabled(false);
		mReward5.setEnabled(false);
		mRewardText5.setEnabled(false);
		mRewardMoneyArray[4].setEnabled(false);
		mReward6.setEnabled(false);
		mRewardText6.setEnabled(false);
		mRewardMoneyArray[5].setEnabled(false);
		return this;
	}

	public RewardDialgBuilder setLastReward(String text) {
		mRewardEtc.setVisibility(View.VISIBLE);
		mRewardText7.setText(text);
		return this;
	}

	public RewardDialgBuilder showDialog() {
		mDialog.setContentView(mDialogView);
		mDialog.show();
		return this;
	}

	public boolean isShowDialog() {
		return mDialog != null && mDialog.isShowing();
	}

	public void dismissDialog() {
		if (mDialog != null && mDialog.isShowing())
			mDialog.dismiss();
	}

	/** 设置连续已领奖的天数数组下标, */
	public RewardDialgBuilder setRewardIndex(int index, JSONArray array) {
		// 设置奖励的点点数
		if (array == null)
			return this;
		try {
			for (int i = 0; i < array.length(); i++) {
				mRewardMoneyArray[i].setText(String.format(mContext.getResources()
						.getString(R.string.login_reward_money), array.getString(i)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (index > 6) {
			mRewardEtc.setVisibility(View.VISIBLE);
			mRewardText7.setText(String.format(mContext.getResources().getString(R.string.login_reward_last),
					(index + 1)));
			return setReward6();
		} else if (index > 5) {
			return setReward6();
		} else if (index > 4) {
			return setReward5();
		} else if (index > 3) {
			return setReward4();
		} else if (index > 2) {
			return setReward3();
		} else if (index > 1) {
			return setReward2();
		} else if (index > 0) {
			return setReward1();
		} else {
			return this;
		}
	}

	private class PositiveListener implements android.view.View.OnClickListener {

		private DialogInterface.OnClickListener dialogListener;

		public PositiveListener(DialogInterface.OnClickListener l) {
			dialogListener = l;
		}

		@Override
		public void onClick(View v) {
			if (dialogListener != null) {
				dialogListener.onClick(mDialog, DialogInterface.BUTTON_POSITIVE);
			}
		}

	}

	private class NegativeListener implements android.view.View.OnClickListener {

		private DialogInterface.OnClickListener dialogListener;

		public NegativeListener(DialogInterface.OnClickListener l) {
			dialogListener = l;
		}

		@Override
		public void onClick(View v) {
			if (mDialog != null)
				mDialog.dismiss();
			if (dialogListener != null) {
				dialogListener.onClick(mDialog, DialogInterface.BUTTON_NEGATIVE);
			}
		}

	}
}
