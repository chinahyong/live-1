package tv.live.bx.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.JacksonUtil;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.fragments.BaseFragment;
import tv.live.bx.fragments.ranking.RankHotFragment;
import tv.live.bx.fragments.ranking.RankStarFragment;
import tv.live.bx.fragments.ranking.RankWealthFragment;
import tv.live.bx.model.RankBean;
import tv.live.bx.ui.LoadingProgress;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

/**
 * 排行版（明星榜、财富榜、人气榜）
 *
 * @version 1.1
 * @CreateDate 2014-8-13
 * @updateDate 2017-7-11
 * 页面分开，使用多个Fragment来实现
 */
public class RankActivity extends BaseFragmentActivity implements OnClickListener {
	//loadingView
	private LoadingProgress mLoadProgress;
	//主榜单分类
	private TextView starBtn, popularityBtn, wealthBtn;

	//当前选中title
	private TextView mCurrentCheckedTitle;
	//当前选中页面index
	private int mCurrentIndex = -1;
	//热门榜
	private RankHotFragment mHotFragment;
	//明星榜
	private RankStarFragment mStarFragment;
	//财富榜
	private RankWealthFragment mWealthFragment;
	//index for 热门榜
	private final int INDEX_HOT = 0;
	//index for 明星榜
	private final int INDEX_STAR = 1;
	//index for 财富榜
	private final int INDEX_WEALTH = 2;
	private SparseArray<RankBean> mRankArray = new SparseArray<>();

	@Override
	protected int getLayoutRes() {
		return R.layout.a_main_rank_layout;
	}

	@Override
	protected void initMembers() {
		// 初始化UI
		starBtn = (TextView) findViewById(R.id.start_btn);
		popularityBtn = (TextView) findViewById(R.id.popularity_btn);
		wealthBtn = (TextView) findViewById(R.id.wealth_btn);
		mLoadProgress = (LoadingProgress) findViewById(R.id.progress);
		mLoadProgress.setProgressClickListener(new LoadingProgress.onProgressClickListener() {
			@Override
			public void onReLoad(View v) {
				// 重新加载数据
				reRequestData();

			}

			@Override
			public void onClick(View v) {
				reRequestData();
			}
		});

		//默认选择热门榜
		switchCheckedText(starBtn);
		switchFragment(INDEX_STAR);
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {
		findViewById(R.id.top_left).setOnClickListener(this);

		starBtn.setOnClickListener(this);
		popularityBtn.setOnClickListener(this);
		wealthBtn.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		reRequestData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.top_left:
				onBackPressed();
				break;
			case R.id.start_btn:
				switchCheckedText(starBtn);
				switchFragment(INDEX_STAR);
				break;
			case R.id.popularity_btn:
				switchCheckedText(popularityBtn);
				switchFragment(INDEX_HOT);
				break;
			case R.id.wealth_btn:
				switchCheckedText(wealthBtn);
				switchFragment(INDEX_WEALTH);
				break;
		}
	}

	/**
	 * 改变选中文本
	 */
	private void switchCheckedText(TextView textView) {
		if (mCurrentCheckedTitle != null) {
			mCurrentCheckedTitle.setSelected(false);
			mCurrentCheckedTitle.getPaint().setFakeBoldText(false);
		}
		mCurrentCheckedTitle = textView;
		mCurrentCheckedTitle.setSelected(true);
		mCurrentCheckedTitle.getPaint().setFakeBoldText(true);
	}

	/**
	 * 切换Fragment
	 */
	private void switchFragment(int index) {
		if (index == mCurrentIndex)
			return;
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		final BaseFragment pre = getFragment(mCurrentIndex);
		if (pre != null) {
			if (pre.isAdded())
				transaction.detach(pre);
		}

		mCurrentIndex = index;
		BaseFragment current = getFragment(mCurrentIndex);
		if (current != null) {
			if (current.isDetached())
				transaction.attach(current);
			else
				transaction.add(R.id.rank_frg_container, current);
		}
		transaction.commitAllowingStateLoss();
	}

	/**
	 * 获取当前index对应的Fragment
	 *
	 * @param index
	 * @return
	 */
	private BaseFragment getFragment(int index) {
		switch (index) {
			case INDEX_HOT:
				if (mHotFragment == null) {
					mHotFragment = new RankHotFragment();
				}
				if (mRankArray.get(INDEX_HOT) != null && mHotFragment instanceof IUpdateData)
					mHotFragment.update(mRankArray.get(INDEX_HOT));
				return mHotFragment;
			case INDEX_STAR:
				if (mStarFragment == null) {
					mStarFragment = new RankStarFragment();
				}
				if (mRankArray.get(INDEX_STAR) != null && mStarFragment instanceof IUpdateData)
					mStarFragment.update(mRankArray.get(INDEX_STAR));
				return mStarFragment;
			case INDEX_WEALTH:
				if (mWealthFragment == null) {
					mWealthFragment = new RankWealthFragment();
				}
				if (mRankArray.get(INDEX_WEALTH) != null && mWealthFragment instanceof IUpdateData)
					mWealthFragment.update(mRankArray.get(INDEX_WEALTH));
				return mWealthFragment;
			default:
				return null;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.MSG_RANK_SUCCESS:
				JSONObject jsonResult = (JSONObject) msg.obj;
				parseData(jsonResult);
				// 设置没有数据的EmptyView
				if (mRankArray.size() == 0) {
					String text = mActivity.getString(R.string.rank_no_data);
					mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
				} else {
					mLoadProgress.Hide();
					mLoadProgress.Succeed(-1);
				}
				break;
			case MsgTypes.MSG_RANK_FAILED:
				if (mRankArray.size() == 0) {
					mLoadProgress.Failed("网络出错，点击重新加载!", 0);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
				}
				break;
		}

	}

	/**
	 * 获取到排行榜数据，初始化
	 */
	private void parseData(JSONObject job) {
		if (job == null)
			return;
		try {
//			RankBean hotRank = JacksonUtil.readValue(job.getJSONObject("moderatorAttentionRank"), RankBean.class);
			RankBean hotRank;
			if(job.has("hotRank"))
				hotRank = JacksonUtil.readValue(job.getJSONObject("hotRank"), RankBean.class);
			else
				hotRank = new RankBean();
			RankBean starRank = JacksonUtil.readValue(job.getJSONObject("moderatorIncomeRank"), RankBean.class);
			RankBean wealthRank = JacksonUtil.readValue(job.getJSONObject("userConsumeRank"), RankBean.class);
			mRankArray.put(INDEX_HOT, hotRank);
			mRankArray.put(INDEX_STAR, starRank);
			mRankArray.put(INDEX_WEALTH, wealthRank);

		} catch (Exception e) {
			e.printStackTrace();
		}
		BaseFragment fragment = getFragment(mCurrentIndex);
		((IUpdateData) fragment).update(mRankArray.get(mCurrentIndex));
	}

	/**
	 * 重新请求数据
	 */
	public void reRequestData() {
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		BusinessUtils.getRankInfo(mActivity, new RankCallbackData(this));
	}

	/**
	 * 刷新数据
	 */
	public interface IUpdateData {
		void update(RankBean bean);
	}

	/**
	 * 房间内排行版数据回调函数
	 */
	private static class RankCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public RankCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_RANK_SUCCESS;
					msg.obj = result;
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_RANK_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity fragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (fragment != null)
					fragment.sendMsg(msg);
			}
		}
	}

}
