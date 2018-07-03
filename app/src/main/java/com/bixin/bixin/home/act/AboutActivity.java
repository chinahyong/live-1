package com.bixin.bixin.home.act;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.bixin.bixin.App;
import tv.live.bx.R;
import com.bixin.bixin.base.act.BaseFragmentActivity;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.util.ChannelUtil;

public class AboutActivity extends BaseFragmentActivity {

	private TextView moTvVersion;
	private ImageView mIvLogo;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_about;
	}

	@Override
	protected void initMembers() {
		moTvVersion = (TextView) findViewById(R.id.about_tv_version);
		mIvLogo = (ImageView) findViewById(R.id.about_logo);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {

	}

	@Override
	protected void initTitleData() {
		mTopBackLayout.setOnClickListener(new OnBack());
		mTopTitleTv.setText(R.string.about);
	}

	@Override
	public void initWidgets() {
		String lsVersionName = Utils.getVersionName(this);
		if (lsVersionName != null)
			moTvVersion.setText("V" + lsVersionName);
		moTvVersion.append("build " + Constants.COMMON_DEBUG_BUILD_NO);
		initTitle();
	}

	@Override
	protected void setEventsListeners() {
		mIvLogo.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				moTvVersion.append(" channel: " + ChannelUtil.getChannel(App.mContext));
				v.setOnLongClickListener(null);
				return false;
			}
		});
	}

	// 友盟统计
	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();
	}

	/***********************************
	 * 事件处理器
	 *************************************/
	private class OnBack implements OnClickListener {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	}
}
