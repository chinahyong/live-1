package tv.live.bx.activities;

import com.efeizao.bx.R;

public class LiveReadyWebActivity extends WebViewActivity {

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.translate_exit, R.anim.translate_exit);
	}

}
