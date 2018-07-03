package com.bixin.bixin.ui.popwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Message;
import android.support.annotation.IdRes;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tv.live.bx.R;
import com.bixin.bixin.adapters.LiveHotRankAdapter;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.config.UserInfoConfig;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.util.DateUtil;
import com.bixin.bixin.model.AnchorBean;
import com.bixin.bixin.ui.LoadingProgress;
import com.bixin.bixin.ui.PullRefreshListView;
import com.bixin.bixin.util.ActivityJumpUtil;

/**
 * Created by Live on 8/22/17.
 */

public class LiveHotRankPopWindow extends BasePopWindow {
	private static final int TITLE_ID_NEXT = 0;
	private static final int TITLE_ID_ALL = 1;
	private static final int MSG_TIME_COUNT = 0x001;

	private View mView;

	private RadioGroup mHotRankTitle;
	private View mHotRankNextLine, mHotRankAllLine;
	private PullRefreshListView mListView;
	private LiveHotRankAdapter mAdapter;
	private LoadingProgress mLoading;
	// 当前主播
	private RelativeLayout mHotRankAnchorLayout;
	private TextView mTvNum, mTvNickName, mTvNeedNum, mTvTime;
	private ImageView mIvHeadPic, mIvVerify;
	private Timer mTimer;
	private long remainTimes;

	private String mRid = "";        //用户当前所在房间

	private Map<String, String> mDatas = new HashMap<>();
	private List<AnchorBean> mNextDatas = new ArrayList<>();
	private List<AnchorBean> mAllDatas = new ArrayList<>();
	private AnchorBean mAnchorBean;

	public LiveHotRankPopWindow(Context context) {
		super(context);
		initLayoutContent();
		initWidget();
		addListener();
	}

	private void initLayoutContent() {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context
				.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.pop_hot_rank_layout, null);
		//設置SelectPicPopupWindow的view
		this.setContentView(mView);
		//设置弹出的宽
		this.setWidth(Utils.dip2px(mContext, 326.66f));
		//设置弹出的高
		this.setHeight(Utils.dip2px(mContext, 450));

		// 设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		this.setOutsideTouchable(true);
		//刷新状态
		this.update();
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0000000000);
		// 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
		this.setBackgroundDrawable(dw);
	}

	private void initWidget() {
		mHotRankTitle = (RadioGroup) mView.findViewById(R.id.pop_hot_rank_title);
		mListView = (PullRefreshListView) mView.findViewById(R.id.pop_hot_rank_list);
		mTvNum = (TextView) mView.findViewById(R.id.hot_rank_num);
		mIvHeadPic = (ImageView) mView.findViewById(R.id.hot_rank_headpic);
		mIvVerify = (ImageView) mView.findViewById(R.id.hot_rank_headpic_v);
		mTvNickName = (TextView) mView.findViewById(R.id.hot_rank_nick_name);
		mTvNeedNum = (TextView) mView.findViewById(R.id.hot_rank_status);
		mTvTime = (TextView) mView.findViewById(R.id.hot_rank_time);
		mHotRankNextLine = mView.findViewById(R.id.pop_hot_rank_next_line);
		mHotRankAllLine = mView.findViewById(R.id.pop_hot_rank_all_line);
		mHotRankAnchorLayout = (RelativeLayout) mView.findViewById(R.id.pop_hot_rank_anchor_layout);
		initListView();
	}

	private void initListView() {
		mAdapter = new LiveHotRankAdapter(mContext);
		mLoading = new LoadingProgress(mContext, null);
		mListView.setAdapter(mAdapter);
		mListView.setEmptyView(mLoading);
	}

	private void addListener() {
		setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				if (mTimer != null) {
					mTimer.cancel();
					mTimer = null;
				}
			}
		});
		// 下一轮排行 ／ 总榜切换
		mHotRankTitle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
				switch (checkedId) {
					case R.id.pop_hot_rank_next_title:
						mHotRankNextLine.setVisibility(View.VISIBLE);
						mHotRankAllLine.setVisibility(View.INVISIBLE);
						mHotRankAnchorLayout.setVisibility(View.VISIBLE);
						adapterList(TITLE_ID_NEXT);
						break;
					case R.id.pop_hot_rank_all_title:
						mHotRankNextLine.setVisibility(View.INVISIBLE);
						mHotRankAllLine.setVisibility(View.VISIBLE);
						mHotRankAnchorLayout.setVisibility(View.GONE);
						adapterList(TITLE_ID_ALL);
						break;
				}
			}
		});

		// listview item点击事件
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				position = position - mListView.getHeaderViewsCount();
				if (position < 0) {
					return;
				}
				AnchorBean bean = mAdapter.getData().get(position);
				// 当前用户的uid
				Long uid = Long.parseLong(UserInfoConfig.getInstance().id);
				// 当前是nextList/点击用户 != 自己(他人直播间)／当前主播 != 自己(未开播) / 当前房间 != 跳转房间(同一个房间)
				if (mHotRankTitle.getCheckedRadioButtonId() == R.id.pop_hot_rank_next_title &&
						uid != bean.mid && uid != mAnchorBean.mid && !mRid.equals(bean.rid)) {
					Map<String, Object> lmItem = new HashMap<>();
					lmItem.put("rid", String.valueOf(bean.rid));
					ActivityJumpUtil.toLiveMediaPlayerActivity(mContext, lmItem);
				}
			}
		});
	}

	// 所有数据适配
	private void adapterData() {
		// 主播下轮排名
		String nextRank = mDatas.get("nextRank");
		mTvNum.setText(nextRank);
		// 距离第一名差距
		if (!TextUtils.isEmpty(nextRank)) {
			// 下一轮排名：第一   取反
			if (!"1".equals(nextRank)) {
				String needText = mContext.getResources().getString(R.string
						.live_hot_rank_next_need) + "<font color='#4bbabc'> " + mDatas.get
						("topOffset") + "</font>" + "比心";
				mTvNeedNum.setText(Html.fromHtml(needText));
			} else {
				mTvNeedNum.setText("");
			}
		}
		// 当前主播基本信息
		if (mAnchorBean != null) {
			ImageLoaderUtil.getInstance().loadHeadPic(mContext, mIvHeadPic,
					mAnchorBean.headPic);
			mIvVerify.setVisibility(View.VISIBLE);
			mTvNickName.setText(mAnchorBean.nickname);
		}
		// 默认选中 下一轮排行
		((RadioButton) mHotRankTitle.getChildAt(0)).setChecked(true);
		// list适配数据
		adapterList(TITLE_ID_NEXT);
	}

	// 列表数据适配／切换
	private void adapterList(int checkedId) {
		if (checkedId == TITLE_ID_NEXT) {
			mAdapter.setData(mNextDatas, false);
		} else {
			mAdapter.setData(mAllDatas, true);
		}
		mListView.setSelection(0);
	}

	/**
	 * 设置数据
	 * 当前用户内容
	 * 下一轮排行数据
	 * 总榜数据
	 *
	 * @param data
	 */
	public void setData(final Map<String, String> data, String rid) throws Exception {
		mRid = rid;
		if (data != null) {
			mDatas = data;
			// 获取当前主播数据
			String curStr = mDatas.get("moderatorInfo");
			if (!TextUtils.isEmpty(curStr)) {
				mAnchorBean = AnchorBean.parseAnchor(new JSONObject(curStr));
			}
			// 获取下一轮排行数据
			String nextRankStr = mDatas.get("nextRankList");
			if (!TextUtils.isEmpty(nextRankStr)) {
				mNextDatas = AnchorBean.parseAnchorList(new JSONArray(nextRankStr));
			}
			// 获取总榜数据
			String allRankStr = mDatas.get("hotRank");
			if (!TextUtils.isEmpty(allRankStr)) {
				mAllDatas = AnchorBean.parseAnchorList(new JSONArray(allRankStr));
			}
			timeCount(data);
			// 适配数据
			adapterData();
		}
	}

	// 倒计时
	private void timeCount(final Map<String, String> data) {
		if (!TextUtils.isEmpty(data.get("timeLeft"))) {
			remainTimes = Long.parseLong(data.get("timeLeft"));
			if (mTimer != null) {
				mTimer.cancel();
			}
			mTvTime.setText(DateUtil.getTimeShort(remainTimes * 1000));
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					remainTimes--;
					if (remainTimes <= 0) {
						remainTimes = Long.parseLong(data.get("totalTime"));
					}
					Message msg = Message.obtain();
					msg.obj = remainTimes;
					msg.what = MSG_TIME_COUNT;
					sendMsg(msg);
				}
			}, 0, 1000);
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MSG_TIME_COUNT:
				if (msg.obj != null) {
					long times = Long.parseLong(String.valueOf(msg.obj)) * 1000;
					mTvTime.setText(DateUtil.getTimeShort(times));
				}
				break;
		}
	}


}
