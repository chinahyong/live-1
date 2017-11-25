package tv.live.bx.activities;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.lonzh.lib.network.JSONParser;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.fmk.appupdate.ActivityCallBack;
import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import cn.efeizao.feizao.update.AppUpdateManager;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.ProductRecommentAdapter;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.PullRefreshListView;
import tv.live.bx.util.UiHelper;

/**
 * 精品推荐
 */
public class ProductRecommentActivity extends BaseFragmentActivity implements OnItemClickListener {
	private ProductRecommentAdapter mAdapter;

	private PullRefreshListView mListView;
	/**
	 * 加载loading
	 */
	private LoadingProgress mLoadProgress;
	private LayoutInflater inflater;

	@Override
	protected int getLayoutRes() {
		// 与“我的关注"页面可同用一个界面
		return R.layout.activity_my_focus;
	}

	@Override
	protected void initMembers() {
		inflater = LayoutInflater.from(mActivity);
		initListView(inflater);
		initTitle();
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.me_tuijian);
		mTopBackLayout.setOnClickListener(new OnBack());
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MsgTypes.GET_PRODUCT_RECOMMENT_LIST_SUCCESS:
				@SuppressWarnings("unchecked")
				List<Map<String, String>> mData = (List<Map<String, String>>) msg.obj;
				mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
				mAdapter.setData(mData);
				// 设置没有数据的EmptyView
				String text = mActivity.getString(R.string.love_list_data_empty);
				mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
				break;
			case MsgTypes.GET_PRODUCT_RECOMMENT_LIST_FAILED:
				@SuppressWarnings("unchecked")
				String lmResult = (String) msg.obj;
				mListView.notifyTaskFinished();
				if (mAdapter.isEmpty()) {
					String text1 = mActivity.getString(R.string.a_loading_failed);
					mLoadProgress.Failed(text1, 0);
				} else {
					UiHelper.showToast(mActivity, lmResult);
					mLoadProgress.Hide();
				}
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		EvtLog.d(
				TAG,
				"onItemClick:position " + position + " mListView.getHeaderViewsCount():"
						+ mListView.getHeaderViewsCount());
		if (position - mListView.getHeaderViewsCount() < 0) {
			return;
		}
		@SuppressWarnings("unchecked")
		Map<String, String> lmItem = (Map<String, String>) mAdapter.getItem(position - mListView.getHeaderViewsCount());
		AppUpdateManager adapter = new AppUpdateManager();
		adapter.beginToDownload(mActivity, lmItem.get("package_name"), lmItem.get("name"), ImageLoaderUtil.with()
						.loadImageSyn(mActivity, lmItem.get("icon")), mActivity.getString(R.string.me_tuijian_tick_text, lmItem.get("name")),
				lmItem.get("download_url"), new AppUpdateDownloadCallBack());
	}

	/**
	 * 初始化下拉刷新ListView
	 *
	 * @param inflater
	 */
	private void initListView(LayoutInflater inflater) {
		mAdapter = new ProductRecommentAdapter(mActivity);
		mListView = (PullRefreshListView) findViewById(R.id.author_listview);
		mListView.setOnItemClickListener(this);
		mListView.setTopHeadHeight(0);
		mListView.setAdapter(mAdapter);
		// 下拉刷新数据
		mListView.setTask(new Runnable() {
			@Override
			public void run() {
				// 请求主播数据
				reRequestData(false);
			}
		});
		View view = inflater.inflate(R.layout.a_common_list_header_hint, null);
		view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, getResources()
				.getDimensionPixelSize(R.dimen.list_hintview_height)));
		mListView.setPullnReleaseHintView(view);
		// 设置正确的颜色
		mListView.setHeaderBackgroudColor(getResources().getColor(R.color.app_background));

		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});

		// 设置默认图片
		mLoadProgress = (LoadingProgress) findViewById(R.id.progress);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				// 重新加载数据
				reRequestData(true);

			}

			@Override
			public void onClick(View v) {
				reRequestData(true);
			}
		});
		mListView.setEmptyView(mLoadProgress);
	}

	/**
	 * 重新请求数据
	 *
	 * @param clearAdapter 请求之前是否先清空UI
	 */
	public void reRequestData(boolean clearAdapter) {
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		// 清空界面数据
		if (clearAdapter) {
			// moFocusAdapter.clearData();
			mAdapter.notifyDataSetChanged();
		}
		BusinessUtils
				.getProductRecommentList(mActivity, new GetProductRecommentCallbackData(ProductRecommentActivity.this));
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		reRequestData(false);
	}

	private class AppUpdateDownloadCallBack extends ActivityCallBack {
		@Override
		public void update(int state, int progress) {
			EvtLog.i("down1", "progress====" + progress);
		}
	}

	/************************************* 事件处理器 ************************************/
	private class OnBack implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			onBackPressed();
		}
	}

	/**
	 * 获取关注数据回调 ClassName: LogoutCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-23 下午3:50:00 <br/>
	 *
	 * @author Administrator
	 * @version SettingsFragment
	 * @since JDK 1.6
	 */
	private static class GetProductRecommentCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public GetProductRecommentCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetProductRecommentCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.GET_PRODUCT_RECOMMENT_LIST_SUCCESS;
					msg.obj = JSONParser.parseMulti((JSONArray) result);
					BaseFragmentActivity fragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (fragment != null)
						fragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.GET_PRODUCT_RECOMMENT_LIST_FAILED;
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