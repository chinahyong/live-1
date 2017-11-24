package tv.live.bx.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.config.UserInfoConfig;

public class AccountSaleActivity extends BaseFragmentActivity implements OnClickListener {

	private ImageView mAccountSaleLevelIcon, mAccountSaleButtomImage;
	private TextView mAccountSaleLevelText;
	private Button mAccountSaleBind;
	private LinearLayout mAccountSaleLayout;
	private RelativeLayout mAccountSaleTopLayout;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_account_sale;
	}

	@Override
	protected void initData(Bundle savedInstanceState) {

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	protected void initMembers() {
		mAccountSaleTopLayout = (RelativeLayout) findViewById(R.id.common_top_layout);
		mAccountSaleLayout = (LinearLayout) findViewById(R.id.account_sale_layout);
		mAccountSaleLevelIcon = (ImageView) findViewById(R.id.account_sale_level_icon);
		mAccountSaleLevelText = (TextView) findViewById(R.id.account_sale_level_text);
		mAccountSaleBind = (Button) findViewById(R.id.account_sale_bind);
		mAccountSaleButtomImage = (ImageView) findViewById(R.id.account_sale_buttom_icon);
		mAccountSaleTopLayout.setBackgroundColor(getResources().getColor(R.color.trans));
		initTitle();
	}

	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.setting_account_sale_title);
		mTopBackLayout.setOnClickListener(this);
	}

	public void initWidgets() {
		updateAccountSaleData();
	}

	private void updateAccountSaleData() {
		//判断是否账户保护
		if (!TextUtils.isEmpty(UserInfoConfig.getInstance().mobile)) {
			mAccountSaleLayout.setBackgroundResource(R.drawable.bgm_high);
			mAccountSaleLevelIcon.setImageResource(R.drawable.icon_high);
			mAccountSaleLevelText.setText(R.string.setting_account_sale_level_high);
			mAccountSaleBind.setEnabled(false);
			mAccountSaleBind.setText(String.format(getResources().getString(R.string.setting_account_bind_content), UserInfoConfig.getInstance().mobile));
			mAccountSaleBind.setTextColor(getResources().getColor(R.color.a_text_color_058249));
			mAccountSaleButtomImage.setImageResource(R.drawable.bgm_high_bolang);
		} else {
			mAccountSaleLayout.setBackgroundResource(R.drawable.bgm_low);
			mAccountSaleLevelIcon.setImageResource(R.drawable.icon_low);
			mAccountSaleLevelText.setText(R.string.setting_account_sale_level_low);
			mAccountSaleBind.setEnabled(true);
			mAccountSaleBind.setText(R.string.setting_account_bind_tip);
			mAccountSaleBind.setTextColor(getResources().getColor(R.color.a_text_color_da500e));
			mAccountSaleButtomImage.setImageResource(R.drawable.bgm_low_bolang);
		}
	}

	protected void setEventsListeners() {
		mAccountSaleBind.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PhoneBindActivity.REQUEST_PHONE_BIND_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				updateAccountSaleData();
			}
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.top_left:
				onBackPressed();
				break;
			case R.id.account_sale_bind:
				ActivityJumpUtil.toPhoneBindActivity(mActivity, PhoneBindActivity.REQUEST_PHONE_BIND_CODE, false);
				break;
			default:
				break;
		}

	}

}
