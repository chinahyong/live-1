package tv.live.bx.activities;

import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.lonzh.lib.LZActivity;

import cn.jpush.android.api.JPushInterface;
import tv.live.bx.R;

public class MsgSettingsActivity extends LZActivity {
	private ToggleButton moToButton;

	@Override
	protected void initMembers() {
		moToButton = (ToggleButton) findViewById(R.id.msg_setting_sw_playing_notice);
		initTitle();
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.msg_settings);
		mTopBackLayout.setOnClickListener(new OnBack());
		if (JPushInterface.isPushStopped(getApplicationContext())) {
			moToButton.setChecked(false);
		}
	}

	@Override
	public void initWidgets() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setEventsListeners() {
		moToButton.setOnClickListener(new OnToButton());
	}

	@Override
	protected int getLayoutRes() {

		return R.layout.activity_msg_settings;
	}

	@Override
	protected void registerMsgListeners() {

	}

	/***********************************
	 * 事件处理器
	 ************************************/
	private class OnBack implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			onBackPressed();
		}
	}

	private class OnToButton implements OnClickListener {
		private Toast toast;

		@Override
		public void onClick(View arg0) {
			if (moToButton.isChecked()) {
				JPushInterface.resumePush(getApplicationContext());
				toast = Toast.makeText(getApplicationContext(), R.string.settting_open_notification, Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			} else {
				JPushInterface.stopPush(getApplicationContext());
				toast = Toast.makeText(getApplicationContext(), R.string.settting_close_notification, Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}

		}
	}
}
