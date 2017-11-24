package tv.live.bx.activities;

import android.content.Intent;
import android.os.Bundle;

import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;

/**
 * MeMessageFragment
 *
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class MeMessageActivity extends BaseFragmentActivity {


	/**
	 * 消息种类type： system|attention|support|reply|mission
	 */

	@Override
	protected int getLayoutRes() {
		// TODO Auto-generated method stub
		return R.layout.activity_message_layout;
	}

	@Override
	protected void initMembers() {
		// 初始化UI
		initUI();
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
	}

	@Override
	public void initWidgets() {

	}

	@Override
	protected void setEventsListeners() {

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}


	/**
	 * 初始化UI控件
	 */
	private void initUI() {
	}

}
