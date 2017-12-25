package com.bixin.bixin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshHeaderGridView;

import java.io.Serializable;

import tv.live.bx.R;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.adapters.LiveTagListAdapter;
import com.bixin.bixin.config.AppConfig;
import com.bixin.bixin.ui.ListFooterLoadView;
import com.bixin.bixin.ui.LoadingProgress;

/**
 * Description:LiveTagActivity.java 通过标签获取直播列表
 * Created by Live on 2017/1/5.
 * verison:2.9.0
 */

public class LiveTagActivity extends BaseFragmentActivity implements View.OnClickListener {

	private PullToRefreshHeaderGridView mPullRefreshGridView;
	private LiveTagListAdapter mAdapter;
	/**
	 * 头部
	 */
	private LinearLayout mHeadLayout;
	/**
	 * 加载更多FootView
	 */
	private ListFooterLoadView mListFooterLoadView;

	/**
	 * 加载loading
	 */
	private LoadingProgress mLoadProgress;
	private String intentTagId = "-1";        //准备开播传入的 选中的标签key

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_live_tag;
	}

	@Override
	public void initWidgets() {
		mHeadLayout = (LinearLayout) mInflater.inflate(R.layout.activity_tag_head, null);
		mPullRefreshGridView = (PullToRefreshHeaderGridView) findViewById(R.id.live_tag_listview);
		mPullRefreshGridView.setMode(PullToRefreshBase.Mode.DISABLED);
		mAdapter = new LiveTagListAdapter(mActivity);
		mAdapter.setOnCheckChangeListener(new LiveTagListAdapter.OnCheckChangeListener() {
			@Override
			public void onCheckChange(View v) {
				// 只要调用了该方法，将保存按钮转为可点击
				if (!mTopRightTextLayout.isEnabled()) {
					mTopRightTextLayout.setEnabled(true);
					mTopRightText.setEnabled(true);
				}
			}
		});
		mPullRefreshGridView.getRefreshableView().addHeaderView(mHeadLayout);
		mPullRefreshGridView.getRefreshableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				position = position - mPullRefreshGridView.getRefreshableView().getHeaderViewCount()
						* mPullRefreshGridView.getRefreshableView().getNumColumns();
				if (position < 0) {
					return;
				}
				if (mAdapter.getSelectIndex() == position) {
					mAdapter.setSelectIndex(-1);
				} else {
					mAdapter.setSelectIndex(position);
				}
				mAdapter.notifyDataSetChanged();
			}
		});

		// 设置上滑动加载更多
		mListFooterLoadView = (ListFooterLoadView) mInflater.inflate(R.layout.a_common_list_footer_loader_view, null);
		mListFooterLoadView.hide();
		// 设置默认图片
		mLoadProgress = (LoadingProgress) findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		initTitle();
	}

	@Override
	protected void setEventsListeners() {
		mPullRefreshGridView.getRefreshableView().setAdapter(mAdapter);
		mPullRefreshGridView.setEmptyView(mLoadProgress);
		mTopRightTextLayout.setOnClickListener(this);
		mTopBackLayout.setOnClickListener(onBack);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Intent intent = getIntent();
		if (intent.getStringExtra(LiveTagListAdapter.ID) != null) {
			intentTagId = intent.getStringExtra(LiveTagListAdapter.ID);
		}

		mAdapter.setIntentSelectId(intentTagId);
		mAdapter.addData(AppConfig.getInstance().moderatorTags);
	}

	@Override
	protected void initTitleData() {
		super.initTitleData();
		mTopTitleTv.setText(R.string.live_ready_add_tag);
		mTopRightText.setText(R.string.edit_user_save);
		// 默认右侧保存不可点击，当用户操作了 item时变为可点击
		mTopRightTextLayout.setEnabled(false);
		mTopRightText.setEnabled(false);
		mTopRightTextLayout.setVisibility(View.VISIBLE);
	}

	private View.OnClickListener onBack = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.top_right_text_bg:
				if (mAdapter.getData() != null && mAdapter.getData().size() > 0) {
					// 获取GridView当前选中的item下标
					int position = mAdapter.getSelectIndex();
					if (position >= 0) {
						Intent intent = new Intent();
						intent.putExtra("tag", (Serializable) mAdapter.getData().get(position));
						setResult(RESULT_OK, intent);
						finish();
					} else {
						setResult(RESULT_OK);
						finish();
					}
				} else {
					finish();
				}
				break;
		}
	}
}
