package tv.live.bx.activities;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.lonzh.lib.LZActivity;

import tv.live.bx.R;

public class ChoiceWeightActivity extends LZActivity {
	public static final int RESULT_CODE_OK = 100;
	public static final int RESULT_CODE_CANCELLED = 101;
	private Button moBtCancel, moBtDetermine;
	private EditText moEdtWeight;

	private String msOriWeight;

	@Override
	protected int getLayoutRes() {
		// TODO Auto-generated method stub
		return R.layout.activity_choice_weight;
	}

	@Override
	protected void initMembers() {
		moBtCancel = (Button) findViewById(R.id.choice_weight_bt_cancel);
		moBtDetermine = (Button) findViewById(R.id.choice_weight_bt_determine);
		moEdtWeight = (EditText) findViewById(R.id.choice_weight_edt_weight);

		msOriWeight = getIntent().getStringExtra("weight");
	}

	@Override
	protected void registerMsgListeners() {

	}

	@Override
	public void initWidgets() {
		moEdtWeight.setText(msOriWeight);
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

			String lsWeight = moEdtWeight.getText().toString();

			if (lsWeight.length() > 2) {
				showToast("体重输入不正确", TOAST_SHORT);
				return;
			}

			Intent loIntent = new Intent();
			loIntent.putExtra("weight", lsWeight);
			setResult(RESULT_CODE_OK, loIntent);
			finish();
		}
	}
}
