package tv.live.bx.adapters;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.NetworkImageHolderView;
import com.umeng.analytics.MobclickAgent;
import com.viewpagerindicator.CirclePageIndicator;
import com.wxy.adbanner.entity.AdInfo;

import java.util.ArrayList;
import java.util.List;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.StringUtil;
import tv.live.bx.model.AnchorBean;

/**
 * Created by BYC on 2017/7/14.
 */

public class LiveHotAdapter extends RecyclerView.Adapter {
	// 自动轮播的时间间隔
	private final static int TIME_INTERVAL = 5 * 1000;
	private final int TYPE_BANNER = 1;
	private final int TYPE_HOT = 2;
	private final int TYPE_RECOMMEND = 3;
	private final int TYPE_FOOTER = 4;

	private int viewPagerHeight;

	//项点击
	public static final int CLICK_TOTAL = 1;
	//开播标签点击
	public static final int CLICK_TYPE = 2;
	//banner点击
	public static final int CLICK_BANNER = 3;
	//推荐点击
	public static final int CLICK_RECOMMEND = 4;
	//点我上推荐
	public static final int CLICK_BECOME_RECOMMEND = 5;

	//context--Activity
	private Context ctx;

	//官方推荐数据
	private List<AnchorBean> mRecommendList = new ArrayList<>();
	//热门数据
	private List<AnchorBean> mHotList = new ArrayList<>();
	//banner数据
	private List<AdInfo> mAdInfoList = new ArrayList<>();
	/**
	 * Banner
	 */
	private LinearLayout mBannerLayout;
	private ConvenientBanner mBannerView;
	/**
	 * footerView
	 */
	private View footerView;


	/**
	 * 推荐视图
	 */
	private LinearLayout mRecommendView;
	private LiveHotRecommendAdapter pageAdapter;
	private CirclePageIndicator pageIndicator;


	//图片容器高度
	private int mImageViewHeight;
	//
	private OnItemClickListener listener;


	/**
	 * 添加热门列表数据
	 *
	 * @param data
	 */
	public void setHotData(List<AnchorBean> data) {
		this.mHotList.clear();
		mHotList.addAll(data);
		notifyDataSetChanged();
	}

	/**
	 * 添加热门数据
	 *
	 * @param data
	 */
	public void setBannerData(List<AdInfo> data) {
		this.mAdInfoList.clear();
		this.mAdInfoList.addAll(data);
		notifyDataSetChanged();
		if (mBannerView != null)
			initBannerView();
	}

	/**
	 * 添加推荐数据
	 */
	public void setRecommendData(List<AnchorBean> data) {
		this.mRecommendList.clear();
		this.mRecommendList.addAll(data);
		notifyDataSetChanged();
		if (pageAdapter != null) {
			pageAdapter.setData(data);
			if (data.size() <= 1) {
				pageIndicator.setVisibility(View.GONE);
			} else
				pageIndicator.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 设置底部视图
	 */
	public void setFooterView(View footerView) {
		this.footerView = footerView;
	}

	/**
	 * 清理数据
	 */
	public void clear() {
		this.mHotList.clear();
		this.mRecommendList.clear();
		this.mAdInfoList.clear();
		notifyDataSetChanged();
		if (pageAdapter != null)
			pageAdapter.setData(null);
	}

	/**
	 * 释放一些资源
	 */
	public void destroyView() {
		mBannerView.stopTurning();
	}

	public LiveHotAdapter(Context ctx) {
		this.ctx = ctx;
		mImageViewHeight = FeizaoApp.metrics.widthPixels;
		viewPagerHeight = (FeizaoApp.metrics.widthPixels - Utils.dpToPx(36)) / 2;
	}

	public void setOnItemClick(OnItemClickListener listener) {
		this.listener = listener;
	}

	private int getHotPositionFromAdapterPos(int position) {
		int pos;
		if (getBannerViewCount() == 0) {
			if (position <= 2)
			pos = position;
			else {
				if (getRecommendViewCount() == 0)
					pos = position;
				else
					pos = position - 1;
			}
		} else {
			if (position <= 3)
			pos = position - 1;
			else {
				if (getRecommendViewCount() == 0)
					pos = position - 1;
				else
					pos = position - 2;
			}
		}
		return pos;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case TYPE_BANNER:
				return onCreateBannerView(parent);
			case TYPE_RECOMMEND:
				return onCreateRecommendView(parent);
			case TYPE_FOOTER:
				return onCreateFooterView(parent);
			default:
				return onCreateHotView(parent);
		}
	}

	private void initBannerView() {
		//适配banner图片资源以及item点击事件
		mBannerView.setPages(new CBViewHolderCreator() {
			@Override
			public Object createHolder() {
				return new NetworkImageHolderView();
			}
		}, mAdInfoList)
				.setOnItemClickListener(new com.bigkoo.convenientbanner.listener.OnItemClickListener() {
					@Override
					public void onItemClick(int position) {
						MobclickAgent.onEvent(FeizaoApp.mContext, "clickbannerInIndex");
						OperationHelper.onEvent(FeizaoApp.mContext, "clickbannerInIndex", null);

						listener.onInnerClick(CLICK_BANNER, position);
					}
				});

	}

	private RecyclerView.ViewHolder onCreateBannerView(ViewGroup parent) {
		mBannerLayout = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.a_main_banner_layout, parent, false);
		mBannerView = (ConvenientBanner) mBannerLayout.findViewById(R.id.convenientBanner);
		// 设置横幅底部引导图片，并启动自动播放
		mBannerView.setPageIndicator(new int[]{
				R.drawable.icon_circle_focus_off, R.drawable.icon_circle_focus_on
		}).startTurning(TIME_INTERVAL);
		// 设置包含Banner的RelativeLayout大小
		int liLayoutWidth = FeizaoApp.metrics.widthPixels;
		int liLayoutHeight = liLayoutWidth * 442 / 1080;
		ViewGroup.LayoutParams loLayoutParams = mBannerView.getLayoutParams();
		loLayoutParams.width = liLayoutWidth;
		loLayoutParams.height = liLayoutHeight;
		mBannerView.setLayoutParams(loLayoutParams);

		initBannerView();

		return new BannerViewHolder(mBannerLayout);
	}

	private RecyclerView.ViewHolder onCreateHotView(ViewGroup parent) {
		View rootView = LayoutInflater.from(ctx).inflate(R.layout.item_lv_player, parent, false);
		return new HotViewHolder(rootView);
	}

	private RecyclerView.ViewHolder onCreateFooterView(ViewGroup parent) {
		footerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		return new FooterViewHolder(footerView);
	}

	private RecyclerView.ViewHolder onCreateRecommendView(ViewGroup parent) {
		//recommendView init
		mRecommendView = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.fragment_hot_recommend, parent, false);
		RecommendViewHolder holder = new RecommendViewHolder(mRecommendView);
		pageAdapter = new LiveHotRecommendAdapter(ctx);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, viewPagerHeight);
		lp.setMargins(0, Utils.dpToPx(17), 0, 0);
		holder.viewPager.setLayoutParams(lp);
		holder.viewPager.setAdapter(pageAdapter);
		pageAdapter.setData(mRecommendList);
		pageAdapter.setOnInnerClick(new OnItemClickListener() {
			@Override
			public void onInnerClick(int type, int pos) {
				if (listener != null)
					listener.onInnerClick(type, pos);
			}
		});
		pageIndicator = holder.pageIndicator;
		holder.pageIndicator.setViewPager(holder.viewPager);
		holder.pageIndicator.setStrokeWidth(0);
		holder.pageIndicator.setFillColor(0xff989898);
		holder.pageIndicator.setPageColor(0xffededed);
		if (pageAdapter.getCount() <= 1)
			holder.pageIndicator.setVisibility(View.GONE);
		else
			holder.pageIndicator.setVisibility(View.VISIBLE);

		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		int viewType = getItemViewType(position);
		switch (viewType) {
			case TYPE_HOT:
				onBindHotView((HotViewHolder) holder, position);
				break;

		}
//		onBindHotView((HotViewHolder) holder, position);
	}

	private void onBindHotView(HotViewHolder holder, int position) {
		holder.hotPosition = getHotPositionFromAdapterPos(position);
//		holder.hotPosition = position;
		AnchorBean bean = mHotList.get(holder.hotPosition);
		EvtLog.e("LiveHot", "position:" + holder.hotPosition + "bean:" + bean.nickname);
		ImageLoaderUtil.getInstance().loadImage(holder.moIvPhoto, bean.headPic);
		holder.mTvOnline.setText(String.format(ctx.getString(R.string.live_online_num),
				String.valueOf(bean.onlineNum)));
		if (bean.isPlaying) {
			holder.moIvStatus.setVisibility(View.VISIBLE);
			holder.mTvOnline.setVisibility(View.VISIBLE);
		} else {
			holder.moIvStatus.setVisibility(View.GONE);
			holder.mTvOnline.setVisibility(View.GONE);
		}
		holder.moTvNickname.setText(bean.nickname);
		/*
		 * 标题增加话题设置高亮
		 * @version 2.5.0
		 */
		holder.mTvTitle.setText(StringUtil.setHighLigntText(ctx, bean.announcement));
		if (!TextUtils.isEmpty(bean.city)) {
			holder.mTvLocation.setText(bean.city);
		}
	}

	@Override
	public int getItemViewType(int position) {
		if (getBannerViewCount() != 0 && position == 0)
			return TYPE_BANNER;
		else if (getRecommendViewCount() != 0 && (position == 3 || position == 4)) {
			if (getBannerViewCount() != 0 && position == 4)
				return TYPE_RECOMMEND;
			else if (getBannerViewCount() == 0 && position == 3)
				return TYPE_RECOMMEND;
		} else if (getFooterViewCount() != 0 && position == getItemCount() - 1) {
			return TYPE_FOOTER;
		}
		return TYPE_HOT;
	}


	@Override
	public int getItemCount() {
		return getBannerViewCount() + getHotViewCount() + getRecommendViewCount() + getFooterViewCount();
	}

	private int getBannerViewCount() {
		return (getHotViewCount() == 0 || mAdInfoList.size() == 0) ? 0 : 1;
	}

	private int getHotViewCount() {
		return mHotList.size();
	}

	private int getRecommendViewCount() {
		//推荐数据要在热3、4之间显示
		return (getHotViewCount() == 0 || getHotViewCount() < 2 || mRecommendList.size() == 0) ? 0 : 1;
	}

	private int getFooterViewCount() {
		return (getHotViewCount() == 0 || footerView == null) ? 0 : 1;
	}

	private class BannerViewHolder extends RecyclerView.ViewHolder {
		public BannerViewHolder(View itemView) {
			super(itemView);
		}
	}

	private class HotViewHolder extends RecyclerView.ViewHolder {
		//大图
		private ImageView moIvPhoto;
		//用户昵称
		private TextView moTvNickname;
		//在线人数
		private TextView mTvOnline;
		//直播标题
		private TextView mTvTitle;
		//直播状态--是否live
		private ImageView moIvStatus;
		//地点
		private TextView mTvLocation;
		//对应于HotList中的position
		private int hotPosition = 0;
		private View.OnClickListener clickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener == null)
					return;
				if (v == itemView) {
					listener.onInnerClick(CLICK_TOTAL, hotPosition);
					return;
				}
			}
		};


		public HotViewHolder(View itemView) {
			super(itemView);

			moIvPhoto = (ImageView) itemView.findViewById(R.id.item_lv_player_iv_photo);
			ViewGroup.LayoutParams lp = moIvPhoto.getLayoutParams();
			lp.width = mImageViewHeight;
			lp.height = mImageViewHeight;
			moIvPhoto.setLayoutParams(lp);

			moTvNickname = (TextView) itemView.findViewById(R.id.item_lv_player_tv_nickname);
			mTvOnline = (TextView) itemView.findViewById(R.id.item_lv_player_tv_online_num);
			mTvTitle = (TextView) itemView.findViewById(R.id.item_lv_player_title);
			moIvStatus = (ImageView) itemView.findViewById(R.id.item_lv_player_iv_status);
			mTvLocation = (TextView) itemView.findViewById(R.id.item_lv_player_tv_live_location);

			itemView.setOnClickListener(clickListener);
		}
	}

	private class RecommendViewHolder extends RecyclerView.ViewHolder {
		private ViewPager viewPager;
		private CirclePageIndicator pageIndicator;

		public RecommendViewHolder(View itemView) {
			super(itemView);
			viewPager = (ViewPager) mRecommendView.findViewById(R.id.viewpager);
			pageIndicator = (CirclePageIndicator) mRecommendView.findViewById(R.id.page_indicator);
		}
	}

	private class FooterViewHolder extends RecyclerView.ViewHolder {
		public FooterViewHolder(View itemView) {
			super(itemView);
		}
	}

	public interface OnItemClickListener {
		void onInnerClick(int type, int pos);
	}
}
