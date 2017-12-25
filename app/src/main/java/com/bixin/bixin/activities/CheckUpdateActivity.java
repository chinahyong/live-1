package com.bixin.bixin.activities;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.lonzh.lib.LZActivity;

import tv.live.bx.R;

public class CheckUpdateActivity extends LZActivity {
	private Button moBtnCancel, moBtnUpdate;

	@Override
	protected int getLayoutRes() {
		// TODO Auto-generated method stub
		return R.layout.activity_update_news;
	}

	@Override
	protected void initMembers() {
		moBtnCancel = (Button) findViewById(R.id.update_news_btn_cancel);
		moBtnUpdate = (Button) findViewById(R.id.update_news_btn_update);

	}

	@Override
	protected void registerMsgListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initWidgets() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setEventsListeners() {
		moBtnCancel.setOnClickListener(new OnCancel());
		moBtnUpdate.setOnClickListener(new OnUpdate());

	}


	/*********************************** 事件处理器 *************************************/
	private class OnCancel implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			onBackPressed();
		}
	}

	private static class OnUpdate implements OnClickListener {
		@Override
		public void onClick(View arg0) {

		}
	}
}
