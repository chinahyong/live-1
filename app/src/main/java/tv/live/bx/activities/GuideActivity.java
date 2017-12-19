package tv.live.bx.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;

import com.viewpagerindicator.IconPageIndicator;

import java.util.ArrayList;
import java.util.List;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.IconPageAdapter;
import tv.live.bx.callback.LevelInfoReceiverListener;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.JacksonUtil;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.config.AppConfig;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.TelephoneUtil;
import tv.live.bx.util.ActivityJumpUtil;

public class GuideActivity extends BaseFragmentActivity implements OnClickListener {

	private String mLevelConfigVersion = Constants.COMMON_LEVEL_CONFIG_VERSION;

	public static String GUIDE_IS_SHOW = "guide_is_show";
	// 向导页面的版本号（只有向导页面版本升级了才显示向导页）
	public static String GUIDE_VERSION = "3";

	private IconPageIndicator mIndicator;
	private IconPageAdapter<View> mAdapter;
	private List<View> mViews;
	private ViewPager mViewPage;
	private LayoutInflater mInflater;

	private ImageView mExperienceIv;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_guide;
	}

	@Override
	protected void initMembers() {
		mInflater = LayoutInflater.from(getApplicationContext());
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mViews = new ArrayList<View>();
		mViews.add(mInflater.inflate(R.layout.view_guide_one, null));
		mViews.add(mInflater.inflate(R.layout.view_guide_two, null));
		mViews.add(mInflater.inflate(R.layout.view_guide_three, null));
		mViews.add(mInflater.inflate(R.layout.view_guide_four, null));
		mExperienceIv = (ImageView) findViewById(R.id.guide_experice);
		// 定义一个iewpager的adaper
		mAdapter = new IconPageAdapter<View>(mViews, R.drawable.bg_guide_indicator_selector);
		// 定义个Pager，即布局中定义的那个pagerview
		mViewPage = (ViewPager) findViewById(R.id.pager);
		mViewPage.setAdapter(mAdapter);

		// 定义一个指示变量，即布局中定义的那个
		mIndicator = (IconPageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mViewPage);
		mIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == (mAdapter.getCount() - 1)) {
					mExperienceIv.setVisibility(View.VISIBLE);
				} else {
					mExperienceIv.setVisibility(View.GONE);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		checkLevelUpdate();
	}

	@Override
	public void initWidgets() {
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.guide_experice:
				Utils.setCfg(mActivity, GuideActivity.GUIDE_IS_SHOW, GuideActivity.GUIDE_VERSION);
				// 如果未登录进入登录页面
				if (!AppConfig.getInstance().isLogged) {
					ActivityJumpUtil.gotoActivityForResult(mActivity, LoginActivity.class, Constants.REQUEST_CODE_LOGIN,
							null, null);
				} else {
					ActivityJumpUtil.welcomeToMainActivity(GuideActivity.this);
				}
				break;

			default:
				break;
		}
	}

	@Override
	protected void setEventsListeners() {
		mExperienceIv.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		// startTimer();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			// 登录成功进入主页
			if (resultCode == LoginActivity.RESULT_CODE_OK) {
				ActivityJumpUtil.welcomeToMainActivity(GuideActivity.this);
			} else {
				finish();
			}
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.WELCOME_ACTIVITY_WAIT_TIMEOUT:
				// 如果未登录进入登录页面
				if ("com.guojiang.yyboys".equals(FeizaoApp.mContext.getPackageName())
						&& !AppConfig.getInstance().isLogged) {
					ActivityJumpUtil.gotoActivityForResult(mActivity, LoginActivity.class, Constants.REQUEST_CODE_LOGIN,
							null, null);
				} else {
					ActivityJumpUtil.welcomeToMainActivity(GuideActivity.this);
				}
				break;

			default:
				break;
		}
	}

	/**
	 * 等级配置更新
	 */
	public void checkLevelUpdate() {
		if (TelephoneUtil.isNetworkAvailable()) {
			BusinessUtils.getConfigInfo(this, new LevelConfigUpdateReceiverListener());
		}
	}

	/**
	 * 等级配置更新回调 Function: TODO ADD FUNCTION. <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private class LevelConfigUpdateReceiverListener implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "LevelConfigUpdateReceiverListener success " + success + " errorCode" + errorCode);
			if (success) {
				try {
					AppConfig config = JacksonUtil.readValue(result.toString(), AppConfig.class);
					AppConfig.getInstance().updateInfo(config);
					int levelVersion = Integer.parseInt(AppConfig.getInstance().levelConfigVersion);
					int currentVersion = Integer.parseInt(AppConfig.getInstance().currentLevelConfigVersion);

					if (levelVersion > currentVersion) {
						BusinessUtils.getLevelConfigInfo(FeizaoApp.mContext, new LevelInfoReceiverListener(AppConfig.getInstance().levelConfigVersion));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}
	}


}
