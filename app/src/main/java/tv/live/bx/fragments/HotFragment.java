package tv.live.bx.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.CalMainActivity;
import tv.live.bx.activities.LoginActivity;
import tv.live.bx.activities.PostPublishActivity;
import tv.live.bx.adapters.CommutityFragementStatusAdapter;
import tv.live.bx.common.AsyncTaskThreadPool;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.database.DatabaseUtils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.tasks.BaseRunnable;
import tv.live.bx.util.ActivityJumpUtil;

/**
 * 社区页面 Title: CommutityFragment.java
 *
 * @version 1.0
 * @CreateDate 2014-8-13
 */
@SuppressLint({"NewApi", "ResourceAsColor"})
public class HotFragment extends BaseFragment implements OnClickListener {
	private final String TAG = "CommutityFragment";
	private CalMainActivity mActivity;
	/**
	 * 发送帖子按钮
	 */
	private ImageView mSendPostIv;
	private TextView mSwitchCategoryTV;
	private ImageView mIndicatorIV;
	private ViewPager mViewPager;
	private CommutityFragementStatusAdapter mFragmentStatePagerAdapter;
	private List<Map<String, String>> mPostMoudleInfos = new ArrayList<Map<String, String>>();
	private TabPageIndicator mTabPageIndicator;

	private LinearLayout mCategoryLayout;
	private GridView mGridView;
	private MGridViewAdapter adapter;
	private Animation aplhaShow, rotateAnim, rotate0Anim;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (CalMainActivity) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			// 发帖按钮
			case R.id.publicBtn:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "PostInCommunity");
				ActivityJumpUtil.gotoActivityForResult(mActivity, PostPublishActivity.class,
						PostPublishActivity.REQUEST_CODE_PUBLIC_FRAGMENT, null, null);
				break;
			case R.id.btn_indicator:
				if (mTabPageIndicator.getVisibility() == View.VISIBLE) {
					showCategoryLayout();
				} else {
					hideCategoryLayout();
				}

				break;
		}
	}

	private void showCategoryLayout() throws NotFoundException {
		mIndicatorIV.startAnimation(rotateAnim);
		mTabPageIndicator.setVisibility(View.GONE);
		mSwitchCategoryTV.clearAnimation();
		mSwitchCategoryTV.startAnimation(aplhaShow);
		mSwitchCategoryTV.setVisibility(View.VISIBLE);
		mCategoryLayout.clearAnimation();
		Animation showAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.popup_up_to_down_show);
		adapter.setSeletedPosition(mViewPager.getCurrentItem());
		showAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}
		});
		mCategoryLayout.startAnimation(showAnimation);
		mCategoryLayout.setVisibility(View.VISIBLE);

	}

	private void hideCategoryLayout() throws NotFoundException {
		mIndicatorIV.startAnimation(rotate0Anim);
		mTabPageIndicator.clearAnimation();
		mTabPageIndicator.startAnimation(aplhaShow);
		mTabPageIndicator.setVisibility(View.VISIBLE);
		mSwitchCategoryTV.setVisibility(View.GONE);
		mCategoryLayout.clearAnimation();
		Animation hideAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.popup_up_to_down_hidden);
		hideAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mCategoryLayout.setVisibility(View.GONE);
			}
		});
		mCategoryLayout.startAnimation(hideAnimation);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_LOGIN " + resultCode);
			if (resultCode == LoginActivity.RESULT_CODE_OK) {
				/** 重新初始化参数 */
				SubjectFragment subjectFragment = (SubjectFragment) mFragmentStatePagerAdapter.instantiateItem(
						mViewPager, mViewPager.getCurrentItem());
				subjectFragment.onFlushFragment();
			} else {

			}
		} else if (requestCode == PostPublishActivity.REQUEST_CODE_PUBLIC_FRAGMENT) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_PUBLIC_FRAGMENT " + resultCode);
			if (resultCode == LoginActivity.RESULT_CODE_OK) {
				/** 重新初始化参数 */
				SubjectFragment subjectFragment = (SubjectFragment) mFragmentStatePagerAdapter.instantiateItem(
						mViewPager, mViewPager.getCurrentItem());
				subjectFragment.onFlushFragment();
			}
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.MSG_POST_MOUDLE_SUCCESS:
				mPostMoudleInfos.clear();
				// 添加“热门”栏目数据，此数据客户端写死
				Map<String, String> allPostMoudle = new HashMap<String, String>();
				allPostMoudle.put("id", "0");
				allPostMoudle.put("title", mActivity.getResources().getString(R.string.commutity_catelory_all));
				allPostMoudle.put("list_icon", "drawable://" + R.drawable.iocn_hot_nor);
				allPostMoudle.put("type", "forum");
				mPostMoudleInfos.add(allPostMoudle);
				mPostMoudleInfos.addAll((List<Map<String, String>>) msg.obj);
				mFragmentStatePagerAdapter.setDatas(mPostMoudleInfos);
				mTabPageIndicator.notifyDataSetChanged();
				break;
			case MsgTypes.MSG_POST_MOUDLE_FAILED:
				break;
		}

	}

	@Override
	public void onTabClickAgain() {
		// if (mListView != null) {
		// mListView.setSelection(0);
		// }
	}

	/**
	 * 之前忘记使用这些方法了，这个类暂时不用了
	 */
	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_hot_layout;
	}

	@Override
	protected void initMembers() {
		mSwitchCategoryTV = (TextView) mRootView.findViewById(R.id.category_switch_text);
		mIndicatorIV = (ImageView) mRootView.findViewById(R.id.btn_indicator);
		mSendPostIv = (ImageView) mRootView.findViewById(R.id.publicBtn);
		mSendPostIv.setOnClickListener(this);
		mViewPager = (ViewPager) mRootView.findViewById(R.id.viewPage);
		// 实例化TabPageIndicator然后设置ViewPager与之关联
		mTabPageIndicator = (TabPageIndicator) mRootView.findViewById(R.id.indicator);

		mFragmentStatePagerAdapter = new CommutityFragementStatusAdapter(mActivity,
				mActivity.getSupportFragmentManager());
		mViewPager.setAdapter(mFragmentStatePagerAdapter);
		mTabPageIndicator.setViewPager(mViewPager);
		mTabPageIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				if (GroupSubjectFragment.GROUP_FORUM.equals(mPostMoudleInfos.get(arg0).get("type"))) {
					mSendPostIv.setVisibility(View.GONE);
				} else {
					mSendPostIv.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		mCategoryLayout = (LinearLayout) mRootView.findViewById(R.id.category_layout);
		mGridView = (GridView) mRootView.findViewById(R.id.gridView);
		adapter = new MGridViewAdapter();
		mGridView.setAdapter(adapter);
		mGridView.setFocusableInTouchMode(true);
		mGridView.setFocusable(true);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				hideCategoryLayout();
				mViewPager.setCurrentItem(position);
			}
		});

	}

	@Override
	protected void setEventsListeners() {
		mIndicatorIV.setOnClickListener(this);
		mActivity.setBackHandedFragment(this);
	}

	@Override
	protected void initData(Bundle bundle) {
		// 加载本地缓存数据
		AsyncTaskThreadPool.getThreadExecutorService().submit(new LoadCacheDataTask());
	}

	public boolean onBackPressed() {
		if (mTabPageIndicator.getVisibility() == View.GONE) {
			hideCategoryLayout();
			return true;
		}
		return false;
	}

	/**
	 * 本地缓存数据加载
	 */
	private class LoadCacheDataTask extends BaseRunnable {

		@Override
		public void runImpl() {
			EvtLog.d(TAG, "LoadCacheDataTask loading local getListPostMoudleInfos start");
			Message msg = new Message();
			msg.what = MsgTypes.MSG_POST_MOUDLE_SUCCESS;
			List<Map<String, String>> mPostMoudleInfos = DatabaseUtils.getListPostMoudleInfos();
			msg.obj = mPostMoudleInfos;
			sendMsg(msg);
			EvtLog.d(TAG, "LoadCacheDataTask loading local getListPostMoudleInfos end");
		}

	}

	@Override
	protected void initWidgets() {
		aplhaShow = AnimationUtils.loadAnimation(mActivity, R.anim.alpha_show_anim);
		rotateAnim = AnimationUtils.loadAnimation(mActivity, R.anim.rotate_180_anim);
		rotate0Anim = AnimationUtils.loadAnimation(mActivity, R.anim.rotate_0_anim);
	}

	/**
	 * 帖子模块类别
	 */
	class MGridViewAdapter extends BaseAdapter {

		private int mSeletedPosition;

		@Override
		public int getCount() {
			return mPostMoudleInfos.size();
		}

		@Override
		public Object getItem(int position) {

			// TODO Auto-generated method stub
			return mPostMoudleInfos.get(position);
		}

		@Override
		public long getItemId(int position) {

			// TODO Auto-generated method stub
			return position;
		}

		public void setSeletedPosition(int position) {
			mSeletedPosition = position;
			notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder loHolder;
			if (convertView == null) {
				LayoutInflater loInflater = LayoutInflater.from(mActivity);
				convertView = loInflater.inflate(R.layout.pop_post_category_item, null);

				loHolder = new Holder();
				loHolder.categoryLayout = (RelativeLayout) convertView.findViewById(R.id.category_layout);
				loHolder.categoryIcon = (ImageView) convertView.findViewById(R.id.category_image);
				loHolder.categoryName = (TextView) convertView.findViewById(R.id.category_name);
				convertView.setTag(loHolder);
			} else {
				loHolder = (Holder) convertView.getTag();
			}
			if (mSeletedPosition == position) {
				loHolder.categoryLayout.setSelected(true);
			} else {
				loHolder.categoryLayout.setSelected(false);
			}
			loHolder.categoryName.setText(mPostMoudleInfos.get(position).get("title"));
			ImageLoaderUtil.with().loadImage(mActivity, loHolder.categoryIcon, mPostMoudleInfos.get(position).get("list_icon"));
			return convertView;
		}

		class Holder {
			RelativeLayout categoryLayout;
			ImageView categoryIcon;
			TextView categoryName;
		}

	}
}
