package tv.live.bx.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.util.UiHelper;
import tv.live.bx.fragments.MePublishFragment;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.TelephoneUtil;
import tv.live.bx.listeners.OnUpdateListener;
import tv.live.bx.receiver.ConnectionChangeReceiver;
import tv.live.bx.ui.SingleTabWidget;
import tv.live.bx.ui.SingleTabWidget.OnTabChangedListener;

import java.util.List;

/**
 * Title: PostActivity.java Description: 帖子主界面 Copyright: Copyright (c) 2008
 * @version 1.0
 */
public class PostActivity extends BaseFragmentActivity {

	public static final String EXA_IS_CHECK_UPDATE = "isCheckUpdate";
	/** 请求帖子详情 */
	public static final int REQUEST_CODE_POSTDETAIL_FRAGMENT = 101;
	
	private static final String TAG = "CalMainActivity";

	private final String FRAGMENT_TAG_FORMAT = "PostFragment_%s";

	public static final int TAB_PUBLISH = 0; // 发布
	public static final int TAB_COLLECT = 1; // 收藏
	public static final int TAB_REPLY = 2; // 回复

	/** 底部TAB栏 */
	private SingleTabWidget tabWidget;
	private OnTabChangedListener mOnTabChangedListener;
	/**
	 * 主界面布局
	 */
	private ViewGroup mMainContainer;

	/**
	 * 当前显示的Fragment
	 */
	private int mTabIndex = -1;

	private static final int MSG_CK_UPDATE_SUCCESSFUL = 1;

	private ConnectionChangeReceiver receiver;

	// 数据更新接口
	private DataUpdateInterface dataUpdateInterface;

	// 设置数据更新接口
	private void setDataUpdateInterface(DataUpdateInterface update) {
		this.dataUpdateInterface = update;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		// 初始化UI
		initUI(savedInstanceState);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.post_main_layout;
	}

	@Override
	public void initWidgets() {
		initTitle();
	}

	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.me_post);
		mTopBackLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	@Override
	protected void setEventsListeners() {
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
	}

	/**
	 * 进入列表页面检查网络，提醒用户链接网络
	 */
	private void checkNetwork() {
		if (!TelephoneUtil.isNetworkAvailable()) {
			UiHelper.showConfirmDialog(PostActivity.this, R.string.network_setting_msg, R.string.settings,
					R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							TelephoneUtil.openWifiSetting(getApplicationContext());
						}
					}, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
		}

	}

	/**
	 * 初始化UI控件，加载Fragment
	 */
	private void initUI(final Bundle savedInstanceState) {
		// 初始化底部TabWidget
		tabWidget = (SingleTabWidget) findViewById(R.id.main_tabs);
		tabWidget.setLayout(R.layout.a_main_tab_layout);

		// 添加四个Tab
		tabWidget.addTab(R.layout.post_publish_tab, 0, null, TAB_PUBLISH);
		tabWidget.addTab(R.layout.post_collect_tab, 0, null, TAB_COLLECT);
		tabWidget.addTab(R.layout.post_reply_tab, 0, null, TAB_REPLY);

		final FragmentManager fm = getSupportFragmentManager();
		mOnTabChangedListener = new OnTabChangedListener() {
			@Override
			public void onTabChanged(int tabIndex) {
				if (tabIndex == mTabIndex) {
					Fragment f = getFragmentByIndex(tabIndex, fm, savedInstanceState);
					if (f instanceof OnUpdateListener) {
						// 回调Tab再次点击方法
						((OnUpdateListener) f).onTabClickAgain();
					}
					return;
				}
				switchFragment(savedInstanceState, tabIndex);
				mTabIndex = tabIndex;
			}
		};

		// 设置监听
		tabWidget.setOnTabChangedListener(mOnTabChangedListener);

		if (savedInstanceState != null) {
			// Activity自动保存了Fragment的状态，此处要隐藏所有的Fragment，不然可能导致Fragment重叠
			FragmentTransaction ft = fm.beginTransaction();
			for (Fragment f : fm.getFragments()) {
				ft.hide(f);
			}
			ft.commitAllowingStateLoss();
		}

		// 初始化时，添加消息Tab到Activity中，激活消息Fragment
		// switchFragment(savedInstanceState, TAB_MESSAGE);
		// 默认显示
		tabWidget.setCurrentTab(TAB_PUBLISH);
		switchFragment(savedInstanceState, TAB_PUBLISH);
		mTabIndex = TAB_PUBLISH;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		EvtLog.d(TAG, "onSaveInstanceState.");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onContentChanged() {
		mMainContainer = (ViewGroup) findViewById(R.id.main_frame_layout);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		EvtLog.d(TAG, "onNewIntent.");
		super.onNewIntent(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/* 在这里，我们通过碎片管理器中的Tag，就是每个碎片的名称，来获取对应的fragment */
		Fragment f = getSupportFragmentManager().findFragmentByTag(getFragmentTag(mTabIndex));
		/* 然后在碎片中调用重写的onActivityResult方法 */
		f.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 初始化日历展示的Fragment
	 */
	private void switchFragment(Bundle savedInstanceState, int tabIndex) {
		final FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction t = manager.beginTransaction();
		if (mTabIndex != -1) {
			Fragment last = getFragmentByIndex(mTabIndex, manager, savedInstanceState);
			if (last != null) {
				if (last.isAdded()) {
					// 如果已经关联到Activity，隐藏Fragment
					t.hide(last);
				} else {
					// 此行的代码暂时不会执行？
					t.remove(last);
				}
			}
		}
		Fragment current = getFragmentByIndex(tabIndex, manager, savedInstanceState);
		if (current != null) {
			t.setTransition(FragmentTransaction.TRANSIT_NONE); // 无动画
			if (current.isAdded()) {
				// 如果已经添加，则直接显示Fragment
				t.show(current);
			} else {
				// 添加Fragment到Activity中
				t.add(R.id.cal_frame_layout, current, getFragmentTag(tabIndex));
			}
		}
		t.commitAllowingStateLoss();

		// 回调Tab选中方法
		if (current instanceof OnUpdateListener) {
			((OnUpdateListener) current).onTabClick();
		}
	}

	/**
	 * 根据index生成Fragment的Tag
	 * 
	 * @param tabIndex Fragment对应的下标值（本类中的常量定义）
	 */
	private String getFragmentTag(int tabIndex) {
		return String.format(FRAGMENT_TAG_FORMAT, tabIndex);
	}

	/**
	 * 根据index获取对应的Fragment对象
	 * 
	 * @param index Fragment对应的下标值（本类中的常量定义）
	 */
	private Fragment getFragmentByIndex(int index, FragmentManager fm, Bundle savedInstanceState) {
		Fragment f = fm.findFragmentByTag(getFragmentTag(index));
		if (f == null) {
			switch (index) {
			case TAB_PUBLISH:
				f = new MePublishFragment();
				break;
			case TAB_COLLECT:
				// f = new MeCollectActivity();
				break;
			case TAB_REPLY:
				// f = new MeReplyFragment();
				break;
			}
		}
		return f;
	}

	/**
	 * 移除所有的Fragment
	 */
	private void removeAllFragment() {
		final FragmentManager fm = getSupportFragmentManager();
		List<Fragment> fragments = fm.getFragments();
		FragmentTransaction ft = fm.beginTransaction();
		for (int i = 0; i < fragments.size(); i++) {
			ft.remove(fragments.get(i));
		}
		ft.commitAllowingStateLoss();
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
	}

	@Override
	protected void onResume() {
		super.onResume();
		EvtLog.d(TAG, "onResume");
		// 注册网络监听广播
		// registerReceiver();
		// 起清空通知栏，清除消息条数作用
	}

	@Override
	protected void onPause() {
		super.onPause();
		EvtLog.d(TAG, "onPause");
		// 注销网络监听广播
		// unregisterReceiver();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// 在onDestroy之前调用，移除所有的Fragment
		removeAllFragment();

		super.onDestroy();
		// 注销广播
	}

	/**
	 * 检测是否需要退出App（强制更新场景）
	 * 
	 * @param data Intent对象
	 */
	private void checkExitApp(Intent data) {
		if (data == null) {
			return;
		}
		boolean isExit = data.getBooleanExtra(AppUpdateActivity.EXA_RESULT_IS_FINISH, false);
		if (isExit) {
			finish();
		}

	}

	/**
	 * 需要数据更新接口 ClassName: DataUpdateInterface <br/>
	 * @version CalMainActivity
	 * @since JDK 1.6
	 */
	public interface DataUpdateInterface {
		void onUpdate();
	}

}
