package com.bixin.bixin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.live.bx.R;
import com.bixin.bixin.activities.base.BaseFragmentActivity;

public class EditInfoActivity extends BaseFragmentActivity {
	public static final int RESULT_CODE_OK = 100;
	public static final int RESULT_CODE_CANCELLED = 101;
	public static final String TITLE = "title_info";
	public static final String EDIT_CONTENT = "edit_info";
	public static final String TEXT_TIP = "text_info";
	public static final String MAX_NUM_INFO = "max_num";
	public static final String MIN_NUM_INFO = "min_num";

	private EditText moEdtIntroduction;
	private TextView mTextTipTv;

	private int mEditMin;
	private int mEditMax;
	private String mEditContent;
	private String mTextTip;
	private String mTitleTextInfo;

	@Override
	protected int getLayoutRes() {
		// TODO Auto-generated method stub
		return R.layout.activity_choice_introduction;
	}

	@Override
	protected void initMembers() {
		moEdtIntroduction = (EditText) findViewById(R.id.choice_introduction_edt_introduction);
		mTextTipTv = (TextView) findViewById(R.id.edit_tip);

		mTopBackLayout = (RelativeLayout) findViewById(R.id.ry_bar_left);
		mTopTitleTv = (TextView) findViewById(R.id.tv_bar_title);
		
		mTopRightTextLayout = (RelativeLayout) findViewById(R.id.ry_bar_right_text);
		mTopRightText = (TextView) findViewById(R.id.tv_bar_right);
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Intent intent = this.getIntent();
		if (intent != null) {
			mEditMax = intent.getIntExtra(MAX_NUM_INFO, 30);
			mEditMin = intent.getIntExtra(MIN_NUM_INFO, 0);
			mEditContent = intent.getStringExtra(EDIT_CONTENT);
			mTextTip = intent.getStringExtra(TEXT_TIP);
			mTitleTextInfo = intent.getStringExtra(TITLE);
		}
		mTopTitleTv.setText(mTitleTextInfo);
		moEdtIntroduction.setText(mEditContent);
		moEdtIntroduction.setMaxEms(mEditMax);
		mTextTipTv.setText(mTextTip);

		mTopRightText.setText(R.string.determine);
		mTopRightTextLayout.setVisibility(View.VISIBLE);
	}

	@Override
	protected void setEventsListeners() {
		mTopBackLayout.setOnClickListener(new OnCancel());
		mTopRightTextLayout.setOnClickListener(new OnDetermine());
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CODE_CANCELLED);
		super.onBackPressed();
	}

	/*********************************** 事件处理器 *************************************/
	private class OnCancel implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			onBackPressed();
		}
	}

	private class OnDetermine implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			String lsIntroduction = moEdtIntroduction.getText().toString();
			if (lsIntroduction.length() < mEditMin || lsIntroduction.length() > mEditMax) {
				showToast(mTextTip, TOAST_SHORT);
				return;
			}
			Intent loIntent = new Intent();
			loIntent.putExtra(EDIT_CONTENT, lsIntroduction);
			setResult(RESULT_CODE_OK, loIntent);
			finish();
		}
	}

}
