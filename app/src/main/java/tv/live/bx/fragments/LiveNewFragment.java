package tv.live.bx.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.adapters.LiveNewListAdapter;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.listeners.GoHotClickListener;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.LoadingProgressEmptyJump;
import tv.live.bx.ui.widget.CustomRefreshLayout;
import tv.live.bx.ui.widget.HeaderFooterGridView;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;

/**
 * Title: LiveNewFragment.java</br> Description: 主播列表最新主播</br> Copyright: *
 *
 * @author Live
 * @version 2.4.0 2016.4.22
 */
@SuppressLint("NewApi")
public class LiveNewFragment extends BaseFragment implements OnClickListener, OnItemClickListener {

	/**
	 * 获取最新页面时，page为0，page以此累加
	 */
	private int page = 0;

	private CustomRefreshLayout refreshLayout;
	private HeaderFooterGridView mGridView;
	private LiveNewListAdapter mLiveNewAdapter;
	/**
	 * 头部
	 */
	private TextView mHeadLayout;
	/**
	 * 空数据
	 */
	private Button mBtnBackHot;

	/**
	 * 加载loading
	 */
	private LoadingProgressEmptyJump mLoadProgress;

	private GoHotClickListener mGoHotClickListener;

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_live_new;
	}

	@Override
	protected void initMembers() {
		// 初始化UI
		initUI(mRootView, mInflater);
	}

	@Override
	protected void initWidgets() {

	}

	@Override
	protected void setEventsListeners() {
		mBtnBackHot.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle bundle) {
		// 加载Banner,anchor列表
		reRequestData(false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.live_new_btn_back_hot:
				if (mGoHotClickListener != null)
					mGoHotClickListener.onGoHotClick();
				break;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		int mPosition = position -
				mGridView.getHeaderViewCount()
						* mGridView.getNumColumns();
		if (mPosition < 0 || mPosition >= mLiveNewAdapter.getCount()) {
			return;
		}
		MobclickAgent.onEvent(FeizaoApp.mConctext, "clickBbroadcasterSImgInNewTab");
		OperationHelper.onEvent(FeizaoApp.mConctext, "clickBbroadcasterSImgInNewTab", null);
		Map<String, Object> lmItem = (Map<String, Object>) mLiveNewAdapter.getItem(mPosition);
		lmItem.put("rid", lmItem.get("rid"));
		ActivityJumpUtil.toLiveMediaPlayerActivity(mActivity, lmItem);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.MSG_LOAD_FAILED:
				if (page == 0)
					refreshLayout.onRefreshComplete();
				else {
					refreshLayout.onLoadingComplete(false, false);
				}
				if (mLiveNewAdapter.isEmpty()) {
					String text = mActivity.getString(R.string.a_loading_failed);
					mLoadProgress.Failed(text, 0);
					mHeadLayout.setVisibility(View.GONE);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
					mLoadProgress.Hide();
					mHeadLayout.setVisibility(View.VISIBLE);
				}
				break;

			case MsgTypes.MSG_LOAD_SUCCESS:
				List<Map<String, Object>> mData = (List<Map<String, Object>>) msg.obj;
				if (page == 0) { // 初始化或者下拉刷新模式
					mLiveNewAdapter.clearData();
					mLiveNewAdapter.addData(mData);
					refreshLayout.onRefreshComplete(); // 收起正在刷新HeaderView
				} else { // 加载更多数据模式
					mLiveNewAdapter.addData(mData);
					refreshLayout.onLoadingComplete(true, mData.size() == 0);
				}
				if (mLiveNewAdapter.getCount() == 0) {
					mLoadProgress.Succeed(null, 1);
					mHeadLayout.setVisibility(View.GONE);
				} else {
					mLoadProgress.Hide();
					mHeadLayout.setVisibility(View.VISIBLE);
				}
				page++;
				break;
		}
	}

	@Override
	public void onTabClickAgain() {
		if (refreshLayout != null)
			refreshLayout.pullToRefresh();
	}

	/**
	 * 初始化UI控件
	 */
	private void initUI(View v, LayoutInflater inflater) {
		mHeadLayout = (TextView) inflater.inflate(R.layout.fragment_live_new_head, null);
		mHeadLayout.setVisibility(View.GONE);
		mBtnBackHot = (Button) v.findViewById(R.id.live_new_btn_back_hot);
		initGridView(v);
	}

	/**
	 * 初始化下拉刷新GridView
	 *
	 * @param v
	 */
	private void initGridView(View v) {
		refreshLayout = (CustomRefreshLayout) v.findViewById(R.id.refresh_layout);
		mGridView = (HeaderFooterGridView) v.findViewById(R.id.gridView_new);
		mGridView.addHeaderView(mHeadLayout);
		mLiveNewAdapter = new LiveNewListAdapter(mActivity);
		mGridView.setAdapter(mLiveNewAdapter);
		mGridView.setColumnWidth((FeizaoApp.metrics.widthPixels - Utils.dpToPx(LiveNewListAdapter.Uniform_Space) * 4) / 3);
		refreshLayout.setOnRefreshListener(new CustomRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				OperationHelper.onEvent(FeizaoApp.mConctext, "refreshInNewPageOfIndex", null);
				// 加载Banner,anchor列表
				reRequestData(false);
			}

			@Override
			public void onLoadMore() {
				requestPlazaData(page);
			}
		});

		mGridView.setOnItemClickListener(this);

		// 设置默认图片
		mLoadProgress = (LoadingProgressEmptyJump) v.findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				// 加载Banner,anchor列表
				reRequestData(false);
			}

			@Override
			public void onClick(View v) {
				// mLoadProgress.Start(mActivity.getResources().getString(R.string.a_progress_loading));
				// // 加载Banner,anchor列表
				// reRequestAllData();
			}
		});
	}

	/**
	 * 重新请求数据
	 *
	 * @param clearAdapter 请求之前是否先清空UI
	 */
	public void reRequestData(boolean clearAdapter) {
		// 初始化loading(正在加载...)
		// 重新初始化请求页面
		page = 0;
		// 清空界面数据
		if (clearAdapter) {
			mLiveNewAdapter.clearData();
			mLiveNewAdapter.notifyDataSetChanged();
		}
		requestPlazaData(page);
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page) {
		BusinessUtils.getAuthorListData(mActivity, page, null, LiveFragment.LIVE_STATUS_NEW, 0,
				new AuthorCallbackData(this));
	}


	public void setGoHotClickListener(GoHotClickListener mGoHotClickListener) {
		this.mGoHotClickListener = mGoHotClickListener;
	}

	/**
	 * 最新主播列表数据处理回调 ClassName: AuthorCallbackData <br/>
	 *
	 * @author Live
	 */
	private static class AuthorCallbackData implements CallbackDataHandle {

		private WeakReference<BaseFragment> mFragment;

		public AuthorCallbackData(BaseFragment fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_LOAD_SUCCESS;
					msg.obj = JSONParser.parseSingleInMulti((JSONArray) result, new String[]{""});
					// 下次请求的页面数
					BaseFragment activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_LOAD_FAILED;
				msg.obj = errorMsg;
				BaseFragment activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}
}
