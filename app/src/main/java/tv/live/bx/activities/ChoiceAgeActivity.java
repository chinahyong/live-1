package tv.live.bx.activities;


import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.lonzh.lib.LZActivity;

import tv.live.bx.R;

public class ChoiceAgeActivity extends LZActivity {
	public static final int RESULT_CODE_OK = 100;
	public static final int RESULT_CODE_CANCELLED = 101;
	private Button moBtCancel, moBtDetermine;
	private EditText moEdtAge;
	private String msOriAge;

	@Override
	protected int getLayoutRes() {
		// TODO Auto-generated method stub
		return R.layout.activity_choice_age;
	}

	@Override
	protected void initMembers() {
		moBtCancel = (Button) findViewById(R.id.choice_age_bt_cancel);
		moBtDetermine = (Button) findViewById(R.id.choice_age_bt_determine);
		moEdtAge = (EditText) findViewById(R.id.choice_age_edt_age);
		msOriAge = getIntent().getStringExtra("age");
	}

	@Override
	protected void registerMsgListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initWidgets() {
		moEdtAge.setText(msOriAge);

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

			String lsAge = moEdtAge.getText().toString();

			if (lsAge.length() > 2) {
				showToast("年龄不正确", TOAST_SHORT);
				return;
			}

			Intent loIntent = new Intent();
			loIntent.putExtra("age", lsAge);
			setResult(RESULT_CODE_OK, loIntent);
			finish();
		}
	}
}
