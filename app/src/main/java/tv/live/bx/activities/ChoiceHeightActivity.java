package tv.live.bx.activities;


import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.lonzh.lib.LZActivity;

import tv.live.bx.R;

public class ChoiceHeightActivity extends LZActivity {
	public static final int RESULT_CODE_OK = 100;
	public static final int RESULT_CODE_CANCELLED = 101;
	private Button moBtCancel, moBtDetermine;
	private EditText moEdtHeight;

	private String msOriHeight;

	@Override
	protected int getLayoutRes() {
		// TODO Auto-generated method stub
		return R.layout.activity_choice_height;
	}

	@Override
	protected void initMembers() {
		moBtCancel = (Button) findViewById(R.id.choice_height_bt_cancel);
		moBtDetermine = (Button) findViewById(R.id.choice_height_bt_determine);
		moEdtHeight = (EditText) findViewById(R.id.choice_height_edt_height);
		msOriHeight = getIntent().getStringExtra("height");
	}

	@Override
	protected void registerMsgListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initWidgets() {
		moEdtHeight.setText(msOriHeight);
	}

	@Override
	protected void setEventsListeners() {
		moBtCancel.setOnClickListener(new OnCancel());
		moBtDetermine.setOnClickListener(new OnDetermine());

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

			String lsHeight = moEdtHeight.getText().toString();

			if (lsHeight.length() > 3) {
				showToast("身高不正确", TOAST_SHORT);
				return;
			}

			Intent loIntent = new Intent();
			loIntent.putExtra("height", lsHeight);
			setResult(RESULT_CODE_OK, loIntent);
			finish();
		}
	}

}