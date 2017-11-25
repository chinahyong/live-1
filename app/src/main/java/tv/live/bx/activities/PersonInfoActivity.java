package tv.live.bx.activities;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshZoomListView;
import com.handmark.pulltorefresh.library.PullToZoomListView;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;
import com.viewpagerindicator.IconPageIndicator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.IconPageAdapter;
import tv.live.bx.adapters.PersonAlbumAdapter;
import tv.live.bx.adapters.PersonCenterAdapter;
import tv.live.bx.common.AsyncTaskThreadPool;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Consts;
import tv.live.bx.common.JacksonUtil;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.common.WebConstants;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.database.DatabaseUtils;
import tv.live.bx.database.model.PersonInfo;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.DateUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.model.AlbumBean;
import tv.live.bx.tasks.BaseRunnable;
import tv.live.bx.ui.ActionSheetDialog;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;


/**
 * 查看他人主页 Title: PersonInfoActivity.java
 *
 * @version 1.0
 * @CreateDate 2014-8-13
 * @updateDate 2016.6.14
 * @updateVersion 2.5.0    Live
 */
public class PersonInfoActivity extends BaseFragmentActivity implements OnClickListener,
		OnItemClickListener, PersonAlbumAdapter.AlbumGridClickListener {
	public static final String PERSON_ID = "person_id";
	public static final String PERSON_FOCUS_STATE = "person_focus_state";

	// 是否关注这个用户
	private boolean mIsAttention;
	public static String PERSON_INFO = "person_info";
	public static String IS_OWER = "is_ower";

	// 此页面由两个头布局组成
	private View mHeadLayout, mHeadLayout2;

	//主播直播显示布局
	private LinearLayout mHeadLiveLayout;
	private ImageView mHeadLiveStatusIv, mHeadLiveIcon;
	private ImageView mHeadLivePhoto;
	private View mHeadLivePhotoCover;
	private TextView mHeadUnLiveTv, mHeadAnchorLevelName, mHeadLiveFansNum, mHeadAnchorLevelTip;
	private FrameLayout mHeadLivePhotoLayout;

	// 守护，粉丝，关注
	private LinearLayout mHeadFansLayout, mHeadGuardLayout, mHeadFocusLayout;
	private View mHeadGuardLine;
	// 守护/粉丝/关注数
	private TextView mHeadFansNum, mHeadGuardNum, mHeadFocusNum;
	// 个性签名
	private TextView mTvIntroduction;


	/**
	 * 头像、认证标志、性别图标、背景
	 */
	private ImageView moIvPhoto, moIvPhotoV, mIvUserSex,
			mTopBackgroup, mIvBottomFocus;
	/**
	 * 昵称、ID 、动态、粉丝、关注、动态数、粉丝数、关注数、认证信息、个人签名、topBar标题
	 */
	private TextView moTvNickname, moTvUserId, mTvVerifyInfo,
			mTvBarTitle, mTvAge, mTvConstellation;
	// 头部左侧按钮,右侧按钮
	private RelativeLayout moTopBarLeft, moTopBarRight;
	private ViewPager mAlbumViewPager;
	private IconPageIndicator mIconPageIndicator;
	private TextView mTvModels, mTvModels2;
	private List<AlbumBean> mAlbumBeans;
	private ImageView mBgAlbum;

	// 底部关注、私信、拉黑
	private RelativeLayout moButtomLayout;
	private LinearLayout moBottomFocus;
	private TextView mTvBottomFocus;

	private PullToRefreshZoomListView mListView;
	private PersonCenterAdapter mSubjectAdapter;

	/**
	 * 是否自己
	 */
	private boolean isOwer;
	/**
	 * 是否是存在于黑名单
	 */
	private boolean isBlack = false;
	/**
	 * 个人相关数据
	 */
	private Map<String, String> mPersonInfo;
	/**
	 * 查看的用户Id
	 */
	private String mPersonUid;

	/**
	 * 点击下拉按钮弹出的对话框
	 */
	private ActionSheetDialog actionSheetDialog;

	private AlertDialog mProgress;
	/**
	 * 头部背景图需要隐藏的高度
	 */
	private int mTopMargin;
	private int mfirstVisibleItem;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_person_info_layout;
	}

	@Override
	protected void initMembers() {
		// 初始化UI
		initUI(mInflater);
	}

	@Override
	public void initWidgets() {
		startPhotoAnimation();
	}

	private void startPhotoAnimation() {
		PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0, -Utils.dip2px(mActivity, 232), 0);
		ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(mHeadLivePhoto, propertyValuesHolder);
		objectAnimator.setDuration(10000);
		objectAnimator.setInterpolator(new LinearInterpolator());
		objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
		objectAnimator.start();
	}

	@Override
	protected void setEventsListeners() {
		moIvPhoto.setOnClickListener(this);
		mHeadLivePhotoLayout.setOnClickListener(this);

		mHeadFansLayout.setOnClickListener(this);
		mHeadGuardLayout.setOnClickListener(this);
		mHeadFocusLayout.setOnClickListener(this);

		moBottomFocus.setOnClickListener(this);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		Intent intent = getIntent();
		if (intent != null) {
			mPersonInfo = (Map<String, String>) intent.getSerializableExtra(PersonInfoActivity.PERSON_INFO);
			mPersonUid = mPersonInfo.get("id");
			//获取是否是自己
			isOwer = UserInfoConfig.getInstance().id.equals(mPersonUid);
		}
		initHeadData();
		showProgressDialog();
		// 加载本地缓存数据
		AsyncTaskThreadPool.getThreadExecutorService().submit(new LoadCacheDataTask());
	}

	/**
	 * 显示对话框
	 */
	private void showProgressDialog() {
		if (mProgress != null && mProgress.isShowing())
			return;
		mProgress = Utils.showProgress(mActivity);
	}

	/**
	 * 关闭对话框
	 */
	private void dismissProgressDialog() {
		if (mProgress != null && mProgress.isShowing()) mProgress.dismiss();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void onDestroy() {
		dismissProgressDialog();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.top_left:
				onBackPressed();
				break;
			case R.id.item_dynamic_layout:
				ActivityJumpUtil.toUserDynamicActivity(mActivity, mPersonUid, isOwer, REQUEST_CODE_FLUSH_ACTIVITY);
				break;
			case R.id.item_fans_layout:
				ActivityJumpUtil.toUserFansActivity(mActivity, mPersonUid, isOwer, REQUEST_CODE_FLUSH_ACTIVITY);
				break;
			case R.id.item_focus_layout:
				ActivityJumpUtil.toUserFocusActivity(mActivity, mPersonUid, isOwer, REQUEST_CODE_FLUSH_ACTIVITY);
				break;
			case R.id.item_guard_layout:
				ActivityJumpUtil.toWebViewActivity(mActivity, WebConstants.getFullWebMDomain(WebConstants.WEB_USER_GUARD_LIST_URL) + mPersonUid, true, 0);
				break;
			case R.id.my_info_img_user:
				//点击头像查看大图
				List<String> imgUrl = new ArrayList<>();
				imgUrl.add(mPersonInfo.get("headPic"));
				ActivityJumpUtil.toImageBrowserActivity(this, 0, imgUrl);
				break;
			case R.id.my_info_level_layout:
				//我的等级
				Map<String, String> webInfo = new HashMap<>();
				webInfo.put(WebViewActivity.URL, WebConstants.getFullWebMDomain(WebConstants.COMMON_LEVEL_URL));
				webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(true));
				ActivityJumpUtil.gotoActivity(mActivity, WebViewActivity.class, false,
						WebViewActivity.WEB_INFO, (Serializable) webInfo);
				break;
			case R.id.person_focus_layout:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "followInpersonalPage");
				// 如果已经关注了，则取消关注
				if (Utils.strBool(mPersonInfo.get("isAttention"))) {
					BusinessUtils.removeFollow(mActivity, new RemoveFollowCallbackData(PersonInfoActivity.this), mPersonUid);
				} else {
					OperationHelper.onEvent(FeizaoApp.mConctext, "followInpersonalPage", null);
					BusinessUtils.follow(mActivity, new FollowCallbackData(PersonInfoActivity.this), mPersonUid);
				}
				break;
			case R.id.top_right:
				ActivityJumpUtil.toReportActivity(mActivity, Constants.COMMON_REPORT_TYPE_ROOM,
						mPersonInfo.get("rid"), 0);
				break;
			case R.id.item_live_photo_layout:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "liveInpersonalPage");
				ActivityJumpUtil.toLiveMediaPlayerActivity(mActivity, mPersonInfo);
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
		} else if (requestCode == PostActivity.REQUEST_CODE_POSTDETAIL_FRAGMENT) {
		} else if (requestCode == REQUEST_CODE_FLUSH_ACTIVITY) {
			requestData();
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
			case MsgTypes.GET_USER_INFO_SUCCESS:
				dismissProgressDialog();
				mPersonInfo = (Map<String, String>) msg.obj;
				initHeadData();
				try {
					mAlbumBeans = JacksonUtil.readValue(mPersonInfo.get("gallery"), List.class, AlbumBean.class);
					initAlbumListData(mAlbumViewPager, mIconPageIndicator, mAlbumBeans);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case MsgTypes.GET_USER_INFO_FAILED:
				Bundle bundle = msg.getData();
				UiHelper.showToast(mActivity, bundle.getString("errorMsg"));
				break;
			case MsgTypes.FOLLOW_SUCCESS:
				//关注当前用户
				OperationHelper.onEvent(FeizaoApp.mConctext, "followInpersonalPageSuccessful", null);
				mIsAttention = true;
				/** 返回关注此用户的状态*/
				Intent intent = new Intent();
				intent.putExtra(PERSON_ID, mPersonUid);
				intent.putExtra(PERSON_FOCUS_STATE, mIsAttention);
				setResult(RESULT_OK, intent);

				mPersonInfo.put("isAttention", Constants.COMMON_TRUE);
				mIvBottomFocus.setImageResource(R.drawable.ic_person_focused);
				mTvBottomFocus.setText(R.string.focused);
				showToast(R.string.person_focus_success, TOAST_SHORT);
				UiHelper.showNotificationDialog(mActivity);
				break;
			case MsgTypes.FOLLOW_FAILED:
				showToast(msg.obj.toString(), TOAST_LONG);
				break;
			case MsgTypes.REMOVE_FOLLOW_SUCCESS:
				//取消关注当前用户
				OperationHelper.onEvent(FeizaoApp.mConctext, "clickCancelFollowBroadcasterInpersonalPage", null);
				mIsAttention = false;
				/** 返回关注此用户的状态*/
				Intent intent2 = new Intent();
				intent2.putExtra(PERSON_ID, mPersonUid);
				intent2.putExtra(PERSON_FOCUS_STATE, mIsAttention);
				setResult(RESULT_OK, intent2);

				mPersonInfo.put("isAttention", "false");
				mIvBottomFocus.setImageResource(R.drawable.ic_person_focus);
				mTvBottomFocus.setText(R.string.focus);
				showToast(R.string.person_remove_focus_success, TOAST_SHORT);
				break;
			case MsgTypes.REMOVE_FOLLOW_FAILED:
				showToast(msg.obj.toString(), TOAST_LONG);
				break;
		}
	}

	/**
	 * 初始化UI控件
	 */
	private void initUI(LayoutInflater inflater) {
		mHeadLayout = inflater.inflate(R.layout.a_common_user_page_head, null);
		mHeadLayout2 = inflater.inflate(R.layout.item_person_center_layout, null);

		//moTopBar = (RelativeLayout) findViewById(R.id.my_info_top_bar);
		mTvBarTitle = (TextView) findViewById(R.id.top_title);
		moTopBarLeft = (RelativeLayout) findViewById(R.id.top_left);
		moTopBarRight = (RelativeLayout) findViewById(R.id.top_right);
		/** 设置背景 */
		mTopBackgroup = (ImageView) findViewById(R.id.top_backgroud);
		mTopBackgroup.setVisibility(View.GONE);
		mTopMargin = (int) (FeizaoApp.metrics.widthPixels * 0.7f - getResources().getDimension(R
				.dimen.layout_top_title_bg_height));
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mTopBackgroup
				.getLayoutParams();
		layoutParams.topMargin = -mTopMargin;
		layoutParams.height = (int) (FeizaoApp.metrics.widthPixels * 0.7f);
		mTopBackgroup.setLayoutParams(layoutParams);

		// 底部关注、私信、拉黑
		moButtomLayout = (RelativeLayout) findViewById(R.id.person_bottom_layout);
		moBottomFocus = (LinearLayout) findViewById(R.id.person_focus_layout);
		mTvBottomFocus = (TextView) findViewById(R.id.person_info_tv_focus);
		mIvBottomFocus = (ImageView) findViewById(R.id.person_info_iv_focus);

		initHeadLayout();
		initHeadLayout2();
		initTitleData();
		initListView(inflater);
	}

	@Override
	protected void initTitleData() {
		moTopBarLeft.setOnClickListener(this);
		moTopBarRight.setOnClickListener(this);
	}

	private void initHeadLayout() {
		/** TopBar */
		moIvPhoto = (ImageView) mHeadLayout.findViewById(R.id.my_info_img_user);
		moIvPhotoV = (ImageView) mHeadLayout.findViewById(R.id.my_info_img_user_v);

		moTvUserId = (TextView) mHeadLayout.findViewById(R.id.my_info_tv_user_id);
		mTvVerifyInfo = (TextView) mHeadLayout.findViewById(R.id.my_info_tv_verify_info);

		mAlbumViewPager = (ViewPager) mHeadLayout.findViewById(R.id.person_info_album_pager);
		mIconPageIndicator = (IconPageIndicator) mHeadLayout.findViewById(R.id.person_info_album_indicator);
		mTvModels = (TextView) mHeadLayout.findViewById(R.id.my_info_tv_medals);
		mTvModels2 = (TextView) mHeadLayout.findViewById(R.id.my_info_tv_medals_2);
		mBgAlbum = (ImageView) mHeadLayout.findViewById(R.id.person_info_album_bg);
	}

	private void initHeadLayout2() {
		mTvAge = (TextView) mHeadLayout2.findViewById(R.id.my_info_tv_sex_age);
		mTvConstellation = (TextView) mHeadLayout2.findViewById(R.id.my_info_tv_constellation);
		mTvVerifyInfo = (TextView) mHeadLayout2.findViewById(R.id.my_info_tv_verify_info);
		mIvUserSex = (ImageView) mHeadLayout2.findViewById(R.id.item_user_iv_sex);

		mHeadLiveLayout = (LinearLayout) mHeadLayout2.findViewById(R.id.item_live_layout);
		mHeadLiveStatusIv = (ImageView) mHeadLayout2.findViewById(R.id.item_live_status);
		mHeadUnLiveTv = (TextView) mHeadLayout2.findViewById(R.id.item_live_status_unlive);
		mHeadAnchorLevelName = (TextView) mHeadLayout2.findViewById(R.id.person_anchor_level);
		mHeadLiveFansNum = (TextView) mHeadLayout2.findViewById(R.id.person_anchor_fan);
		mHeadAnchorLevelTip = (TextView) mHeadLayout2.findViewById(R.id.item_anchor_level_tip);
		mHeadLiveIcon = (ImageView) mHeadLayout2.findViewById(R.id.item_live_icon);
		mHeadLivePhoto = (ImageView) mHeadLayout2.findViewById(R.id.item_photo);
		mHeadLivePhotoCover = mHeadLayout2.findViewById(R.id.item_photo_cover);
		mHeadLivePhotoLayout = (FrameLayout) mHeadLayout2.findViewById(R.id.item_live_photo_layout);


		mHeadFansLayout = (LinearLayout) mHeadLayout2.findViewById(R.id.item_fans_layout);
		mHeadGuardLayout = (LinearLayout) mHeadLayout2.findViewById(R.id.item_guard_layout);
		mHeadGuardLine = mHeadLayout2.findViewById(R.id.item_guard_line);
		mHeadFocusLayout = (LinearLayout) mHeadLayout2.findViewById(R.id.item_focus_layout);

		mHeadFansNum = (TextView) mHeadLayout2.findViewById(R.id.item_fans_num);
		mHeadGuardNum = (TextView) mHeadLayout2.findViewById(R.id.item_guard_num);
		mHeadFocusNum = (TextView) mHeadLayout2.findViewById(R.id.item_focus_num);
		mTvIntroduction = (TextView) mHeadLayout2.findViewById(R.id.item_person_signature);
	}

	/**
	 * 初始化礼物数据(热门、守护礼物）'
	 */
	private void initAlbumListData(ViewPager viewPager, IconPageIndicator indicator, List<AlbumBean> giftsData) {
		if (giftsData == null || giftsData.isEmpty()) {
			viewPager.setVisibility(View.GONE);
			mBgAlbum.setVisibility(View.GONE);
			return;
		} else {
			viewPager.setVisibility(View.VISIBLE);
			mBgAlbum.setVisibility(View.VISIBLE);
		}
		// pageSize 每页显示的相片个数
		int pageSize = 8;
		final int giftPage = (int) Math.ceil(giftsData.size() / Float.valueOf(pageSize));
		if (giftPage == 1) {
			indicator.setVisibility(View.GONE);
		} else {
			indicator.setVisibility(View.VISIBLE);
		}
		// 通过图片数量计算相册Gridview以及viewpager的高度，以及是否显示引导
		float scale = 0f;
		if (giftsData.size() > 0 && giftsData.size() <= 4) {
			scale = (FeizaoApp.metrics.widthPixels - Utils.dip2px(mActivity, 5) * 5) / 4;
			indicator.setVisibility(View.GONE);
		} else if (giftsData.size() > 4 && giftsData.size() <= 8) {
			// 图片高度 + 图片上下间距
			scale = (FeizaoApp.metrics.widthPixels - Utils.dip2px(mActivity, 5) * 5) / 4 * 2 + Utils.dip2px(mActivity, 5);
			indicator.setVisibility(View.GONE);
		} else if (giftsData.size() > 8) {
			// 图片高度 + 图片上下间距
			scale = (FeizaoApp.metrics.widthPixels - Utils.dip2px(mActivity, 5) * 5) / 4 * 2 + Utils.dip2px(mActivity, 5);
			indicator.setVisibility(View.VISIBLE);
		}
		// 礼物ViewPage初始化View
		List<GridView> giftsViewData = new ArrayList<>(giftPage);
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) viewPager.getLayoutParams();
		lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
		lp.height = (int) scale;
		for (int i = 0; i < giftPage; i++) {
			GridView appPage = new GridView(mActivity);
			appPage.setLayoutParams(lp);
			appPage.setHorizontalSpacing(Utils.dip2px(mActivity, 5));
			appPage.setVerticalSpacing(Utils.dip2px(mActivity, 5));
			PersonAlbumAdapter adapter = new PersonAlbumAdapter(mActivity, this);
			// 根据当前页计算装载的应用，每页只装载8个
			int position = i * pageSize;// 当前页的起始位置
			int iEnd = position + pageSize;// 所有数据的结束位置
			int endPosition = iEnd > giftsData.size() ? giftsData.size() : iEnd;
			adapter.updateData(giftsData.subList(position, endPosition));

			appPage.setAdapter(adapter);
			appPage.setNumColumns(4);
			appPage.setSelector(new ColorDrawable(Color.TRANSPARENT));
			appPage.setCacheColorHint(Color.TRANSPARENT);
			// appPage.setVerticalSpacing((int) (10 *
			// TelephoneUtil.getDisplayMetrics().density));
			giftsViewData.add(appPage);
		}
		// 此处 相册容器/黑色背景高度要确定，否则会造成，向下拉时，直接拉伸了此处布局，而非图片布局放大
		viewPager.setLayoutParams(lp);
		lp = (RelativeLayout.LayoutParams) mBgAlbum.getLayoutParams();
		lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
		lp.height = (int) scale;
		mBgAlbum.setLayoutParams(lp);
		// 有相册列表
		if (scale > 0) {
			mListView.getRefreshableView().setHeaderViewSize(FeizaoApp.metrics.widthPixels, (int) (FeizaoApp.metrics.widthPixels * 0.66 + scale));
		} else {
			//无相册列表
			mListView.getRefreshableView().setHeaderViewSize(FeizaoApp.metrics.widthPixels, (int) (FeizaoApp.metrics.widthPixels * 0.53));
		}
		viewPager.setAdapter(new IconPageAdapter<>(giftsViewData, R.drawable.bg_person_indicator_selector));
		indicator.setViewPager(viewPager);
		indicator.setCurrentItem(0);
	}

	private void initHeadData() {
		// 1 取值
		String lsPhoto = mPersonInfo.get("headPic");
		String lsNickname = mPersonInfo.get("nickname");
		String introduction = mPersonInfo.get("signature");
		String level = mPersonInfo.get("level");
		String bgImg = mPersonInfo.get("bgImg");
		mIsAttention = Utils.strBool(mPersonInfo.get("isAttention"));
		String verifyInfo = mPersonInfo.get("verifyInfo");
		String moderatorLevel = mPersonInfo.get("moderatorLevel");
		boolean isPlaying = Utils.strBool(mPersonInfo.get("isPlaying"));
		String birthday = mPersonInfo.get("birthday");
		String sex = mPersonInfo.get("sex");
		String medals = mPersonInfo.get("medals");
		/** TopBar标题为用户名 */
		mTvBarTitle.setText(lsNickname);
		// 2 设置
		if (!TextUtils.isEmpty(lsPhoto)) {
			if (lsPhoto.indexOf("://") == -1) {
				lsPhoto = "file://" + lsPhoto;
			}
			ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity.getApplicationContext(), moIvPhoto, lsPhoto);
			ImageLoaderUtil.with().loadImageTransformRoundedCorners(mActivity.getApplicationContext(), mHeadLivePhoto, lsPhoto, Constants.COMMON_DISPLAY_IMAGE_CORNER_2);
		}
		moIvPhotoV.setVisibility(Utils.getBooleanFlag(mPersonInfo.get("verified")) ? View.VISIBLE : View.GONE);
		moTvUserId.setText(mPersonInfo.get("id"));
		/**
		 * 不是主播，不返回主播等级、认证参数
		 * 是主播，但是没认证，会返回空数据
		 */
		if (!TextUtils.isEmpty(verifyInfo)) {
			mTvVerifyInfo.setVisibility(View.VISIBLE);
			mTvVerifyInfo.setText(String.format(getString(R.string.common_verify_info), verifyInfo));
		} else {
			mTvVerifyInfo.setVisibility(View.GONE);
		}

		//性别图标
		if (!TextUtils.isEmpty(sex)) {
			if (Integer.parseInt(sex) == Consts.GENDER_MALE) {
				mIvUserSex.setImageResource(R.drawable.icon_my_info_man_white);
			} else {
				mIvUserSex.setImageResource(R.drawable.icon_my_info_feman_white);
			}
		}
		// 用户生日，根据生日计算年龄，星座
		if (!TextUtils.isEmpty(birthday)) {
			String[] birthdays = birthday.split("-");
			// 获取 年月日
			if (birthdays.length == 3) {
				// 年龄计算，显示
				int age = DateUtil.getCurYear() - Integer.valueOf(birthdays[0]);
				if (age >= 0) {
					mTvAge.setText(String.valueOf(age));
				}
				// 星座获取,显示
				int constellation = DateUtil.getConstellation(Integer.valueOf(birthdays[1]), Integer.valueOf(birthdays[2]));
				mTvConstellation.setText(constellation);
				mTvConstellation.setVisibility(View.VISIBLE);
			} else {
				mTvConstellation.setVisibility(View.GONE);
			}

		}

		int size = Utils.dip2px(mActivity, 13.66f);
		if (!TextUtils.isEmpty(medals)) {
			SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
			SpannableStringBuilder spannableStringBuilder2 = new SpannableStringBuilder();
			int count = 0;        //勋章+主播等级+用户等级总数
			try {
				JSONArray medalArray = new JSONArray(medals);
				count = medalArray.length();
				// 勋章数量小于10，勋章全部添加到第一行
				if (count < 10) {
					for (int i = 0; i < medalArray.length(); i++) {
						String url = Utils.getModelUri(String.valueOf(medalArray.get(i)));
						if (!TextUtils.isEmpty(url)) {
							spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(mTvModels, url, size));
						}
					}
					//主播等级
					if (!TextUtils.isEmpty(moderatorLevel)) {
						//勋章 + 主播等级 < 10 显示第一行
						if (++count <= 10) {
							spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(mTvModels, Utils.getLevelImageResourceUri(Constants.USER_ANCHOR_LEVEL_PIX, moderatorLevel), size));
						} else {
							// 否则  显示到第二行
							spannableStringBuilder2.append(" ").append(Utils.getImageToSpannableString(mTvModels2, Utils.getLevelImageResourceUri(Constants
									.USER_ANCHOR_LEVEL_PIX, moderatorLevel), size));
						}
					}
					// 用户等级
					if (!TextUtils.isEmpty(level)) {
						//勋章 +主播等级 + 用户等级 <= 10 显示第一行
						if (++count <= 10) {
							spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(mTvModels, Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, level), size));
						} else {
							// 否则  显示到第二行
							spannableStringBuilder2.append(" ").append(Utils.getImageToSpannableString(mTvModels2, Utils.getLevelImageResourceUri(Constants
									.USER_LEVEL_PIX, level), size));
						}

					}
					mTvModels.setText(spannableStringBuilder);
					if (spannableStringBuilder2.length() <= 0) {
						mTvModels2.setVisibility(View.GONE);
					} else {
						mTvModels2.setVisibility(View.VISIBLE);
						mTvModels2.setText(spannableStringBuilder2);
					}
				} else {
					// 勋章数量大于10
					for (int i = 0; i < 10; i++) {
						String url = Utils.getModelUri(String.valueOf(medalArray.get(i)));
						if (!TextUtils.isEmpty(url)) {
							spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(mTvModels, url, size));
						}
					}
					mTvModels.setText(spannableStringBuilder);
					// 数量大于10 ，往第二行
					for (int i = 10; i < medalArray.length(); i++) {
						String url = Utils.getModelUri(String.valueOf(medalArray.get(i)));
						if (!TextUtils.isEmpty(url)) {
							spannableStringBuilder2.append(" ").append(Utils.getImageToSpannableString(mTvModels2, url, size));
						}
					}
					//主播等级
					if (!TextUtils.isEmpty(moderatorLevel)) {
						//显示到第二行
						spannableStringBuilder2.append(" ").append(Utils.getImageToSpannableString(mTvModels2, Utils.getLevelImageResourceUri(Constants
								.USER_ANCHOR_LEVEL_PIX, moderatorLevel), size));
					}
					// 用户等级
					if (!TextUtils.isEmpty(level)) {
						//显示到第二行
						spannableStringBuilder2.append(" ").append(Utils.getImageToSpannableString(mTvModels2, Utils.getLevelImageResourceUri(Constants
								.USER_LEVEL_PIX, level), size));

					}
					if (spannableStringBuilder2.length() <= 0) {
						mTvModels2.setVisibility(View.GONE);
					} else {
						mTvModels2.setVisibility(View.VISIBLE);
						mTvModels2.setText(spannableStringBuilder2);
					}
				}
			} catch (Exception e) {

			}
		} else {
			// 勋章为空直接添加主播等级，用户等级
			SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
			//主播等级
			if (!TextUtils.isEmpty(moderatorLevel)) {
				spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(mTvModels, Utils.getLevelImageResourceUri(Constants
						.USER_ANCHOR_LEVEL_PIX, moderatorLevel), size));
			}
			// 用户等级
			if (!TextUtils.isEmpty(level)) {
				spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(mTvModels, Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, level), size));
			}
			mTvModels.setText(spannableStringBuilder);
			mTvModels2.setVisibility(View.GONE);
		}
		//签名
		if (!TextUtils.isEmpty(introduction)) {
			mTvIntroduction.setText(introduction);
		}
		//背景图
		if (!TextUtils.isEmpty(bgImg)) {
			ImageLoaderUtil.with().loadImage(mActivity.getApplicationContext(), mListView.getRefreshableView().getHeaderView(), bgImg, R.drawable.bg_fan_detail_head, R.drawable.bg_fan_detail_head);
			ImageLoaderUtil.with().loadImage(mActivity.getApplicationContext(), mTopBackgroup, bgImg);
		}
//		mHeadDynamicNum.setText(TextUtils.isEmpty(mPersonInfo.get("postNum")) ? "0" : mPersonInfo
//				.get("postNum"));
		String fansNum = TextUtils.isEmpty(mPersonInfo.get("fansNum")) ? "0" : mPersonInfo.get("fansNum");
		mHeadFansNum.setText(fansNum);
		if(AppConfig.getInstance().showGuard){
			mHeadGuardLayout.setVisibility(View.VISIBLE);
			mHeadGuardLine.setVisibility(View.VISIBLE);
			mHeadGuardNum.setText(TextUtils.isEmpty(mPersonInfo.get("guardNum")) ? "0" : mPersonInfo.get("guardNum"));
		}else{
			mHeadGuardLayout.setVisibility(View.GONE);
			mHeadGuardLine.setVisibility(View.GONE);
		}
		mHeadFocusNum.setText(TextUtils.isEmpty(mPersonInfo.get("attentionNum")) ? "0" : mPersonInfo.get("attentionNum"));

		//如果是自己
		if (isOwer) {
			//隐藏进入房间、隐藏底部布局
			moTopBarRight.setVisibility(View.GONE);
			mHeadLiveLayout.setVisibility(View.GONE);
			moButtomLayout.setVisibility(View.GONE);
		} else {
			moTopBarRight.setVisibility(View.VISIBLE);
			//如果是主播/星探，并且正在开播
			if ((Constants.USER_TYPE_ANCHOR.equals(mPersonInfo.get("type")) || Constants.USER_TYPE_ROOMOWNER.equals(mPersonInfo.get("type")))) {
				mHeadLiveLayout.setVisibility(View.VISIBLE);
				if (isPlaying) {
					mHeadLiveStatusIv.setVisibility(View.VISIBLE);
					mHeadUnLiveTv.setVisibility(View.GONE);
					mHeadLiveIcon.setEnabled(true);
					mHeadLivePhotoLayout.setEnabled(true);
					mHeadLiveFansNum.setTextColor(getResources().getColor(R.color.a_text_color_ffffff));
					mHeadAnchorLevelTip.setTextColor(getResources().getColor(R.color.a_text_color_ffffff));
					mHeadLivePhotoCover.setBackgroundResource(R.drawable.bg_live_cover_living);
				} else {
					mHeadLiveStatusIv.setVisibility(View.GONE);
					mHeadUnLiveTv.setVisibility(View.VISIBLE);
					mHeadLiveIcon.setEnabled(false);
					mHeadLivePhotoLayout.setEnabled(false);
					mHeadLiveFansNum.setTextColor(getResources().getColor(R.color.a_text_color_999999));
					mHeadAnchorLevelTip.setTextColor(getResources().getColor(R.color.a_text_color_666666));
					mHeadLivePhotoCover.setBackgroundResource(R.drawable.bg_live_cover_unlive);
				}

				mHeadAnchorLevelName.setText(mPersonInfo.get("moderatorLevelName"));
				mHeadLiveFansNum.setText(String.format(mActivity.getString(R.string.person_anchor_fans), fansNum));
			} else {
				//如果不是自己不是主播、星探
				mHeadLiveLayout.setVisibility(View.GONE);
			}
			//不是自己显示底部布局
			moButtomLayout.setVisibility(View.VISIBLE);
			if (mIsAttention) {
				mIvBottomFocus.setImageResource(R.drawable.ic_person_focused);
				mTvBottomFocus.setText(R.string.focused);
			} else {
				mIvBottomFocus.setImageResource(R.drawable.ic_person_focus);
				mTvBottomFocus.setText(R.string.focus);
			}
		}
	}

	/* 重置头部布局，主要恢复透明度，是否隐藏为初始 */
	private void resetHeadLayout() {
		/* 头部布局恢复透明度，以及隐藏 */
		mHeadLayout.setAlpha(1);
		mTopBackgroup.setVisibility(View.GONE);
	}

	/**
	 * 初始化下拉刷新ListView
	 *
	 * @param inflater
	 */
	private void initListView(LayoutInflater inflater) {
		mListView = (PullToRefreshZoomListView) findViewById(R.id.pull_refresh_list);
		mSubjectAdapter = new PersonCenterAdapter(mActivity);
		mListView.setOnRefreshListener(new PullToRefreshBase
				.OnRefreshListener2<PullToZoomListView>() {
			String label = DateUtils.formatDateTime(getApplicationContext(), System
					.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_ABBREV_ALL);

			public void onPullDownToRefresh(PullToRefreshBase<PullToZoomListView> refreshView) {
				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.

			}

			public void onPullUpToRefresh(PullToRefreshBase<PullToZoomListView> refreshView) {
				EvtLog.e(TAG, "onPullUpToRefresh");
				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
			}
		});
		mListView.setOnItemClickListener(this);
		// 设置PullRefreshListView上提加载时的加载提示
		mListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载...");
		mListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("正在加载...");
		mListView.getLoadingLayoutProxy(false, true).setReleaseLabel("松开加载更多...");
		// 设置PullRefreshListView下拉加载时的加载提示
		mListView.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新...");
		mListView.getLoadingLayoutProxy(true, false).setRefreshingLabel("正在刷新...");
		mListView.getLoadingLayoutProxy(true, false).setReleaseLabel("松开刷新...");
		mListView.setMode(PullToRefreshBase.Mode.DISABLED);
		// Add an end-of-list listener
		mListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				mListView.getLoadingLayoutProxy(false, true).setRefreshingLabel("ssgdsgsd");
			}
		});

		final PullToZoomListView mPullToZoomListView = mListView.getRefreshableView();

		/**
		 * Add Sound Event Listener
		 */
		mPullToZoomListView.setOnRefreshListener(new PullToZoomListView.OnRefreshListener() {

			@Override
			public void onRefresh() {
				//刷新 用户数据、黑名单状态
				requestData();
			}
		});
		mPullToZoomListView.setOnHeadHeightListener(new PullToZoomListView.OnHeadHeightListener() {
			@Override
			public void onZoom(int precent) {
				EvtLog.e(TAG, "onZoom:" + precent);
				float alpha = 1 - precent / 100f;
				mHeadLayout.setAlpha(alpha);
			}

			@Override
			public void onScrollY(float by) {
				EvtLog.e(TAG, "onScrollY:" + by);
				if (by >= mTopMargin) {
					mTopBackgroup.setVisibility(View.VISIBLE);
				} else if (mfirstVisibleItem == 0) {
					mTopBackgroup.setVisibility(View.GONE);
				}
			}
		});
		mPullToZoomListView.addHeaderViewLayerImage(mHeadLayout);
		mPullToZoomListView.addHeaderView(mHeadLayout2);
		mPullToZoomListView.getHeaderView().setScaleType(ImageView.ScaleType.CENTER_CROP);
		mPullToZoomListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int
					totalItemCount) {
				EvtLog.e(TAG, "firstVisibleItem:" + firstVisibleItem + ",visibleItemCount:" +
						visibleItemCount);
				mfirstVisibleItem = firstVisibleItem;
				if (firstVisibleItem >= 2) {
					mTopBackgroup.setVisibility(View.VISIBLE);
				}
			}
		});
		mPullToZoomListView.setAdapter(mSubjectAdapter);
	}

	/**
	 * 首次请求
	 * 初始化，获取对方用户信息，以及黑名单状态
	 */
	private void requestData() {
		/** 获取用户信息 */
		BusinessUtils.getPersonInfoData(mActivity, mPersonUid, new UserInfoCallbackData(this));
	}

	@Override
	public void onClick(ViewGroup parent, View v, int position, AlbumBean data) {
		OperationHelper.onEvent(mActivity, "clickImgOfPhotoAlbumInPersonalPage", null);
		// 进入图片预览
		Intent intent = new Intent(mActivity, ImageBrowserActivity.class);
		ArrayList<String> arrayUri = new ArrayList<>();
		if (mAlbumBeans != null) {
			for (AlbumBean bean : mAlbumBeans) {
				String url = bean.getUrl();
				if (!TextUtils.isEmpty(url)) {
					if (url.indexOf("://") == -1) {
						url = Constants.FILE_PXI + url;
					}
					arrayUri.add(url);
				}
			}
		}
		intent.putExtra(ImageBrowserActivity.IMAGE_URL, arrayUri);
		intent.putExtra(ImageBrowserActivity.INIT_SHOW_INDEX, mAlbumViewPager.getCurrentItem() * 8 + position);
		intent.putExtra(ImageBrowserActivity.IS_NEED_EIDT, false);
		startActivityForResult(intent, EditAlbumActivity.ACTIVITY_REQUEST_IMAGE_BROWSER);
	}

	/**
	 * 本地缓存数据加载
	 */
	private class LoadCacheDataTask extends BaseRunnable {

		@Override
		public void runImpl() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					requestData();
				}
			});
		}

	}

	/**
	 * 更新本地数据库关注状态
	 *
	 * @param followStatus
	 */
	private void updateLocalAttentionStatus(int followStatus) {
		boolean isAttention;
		if (mPersonInfo != null) {
			isAttention = followStatus == MsgTypes.FOLLOW_SUCCESS;
			// 数据库存储用户个人信息
			DatabaseUtils.updatePersonInfoToDatabase(mActivity, mPersonUid, isAttention);
//			DatabaseUtils.getPersonInfoByUid(mActivity, mPersonUid);
		}
	}


	/**
	 * 个人用户信息 ClassName: UserInfoCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2016-1-16 上午11:04:58 <br/>
	 *
	 * @author Administrator
	 * @version PersonInfoActivity
	 * @since JDK 1.6
	 */
	private class UserInfoCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public UserInfoCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "UserInfoCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.GET_USER_INFO_SUCCESS;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					BaseFragmentActivity meFragment = mFragment.get();
					// 数据库存储用户个人信息
					if (result != null) {
						DatabaseUtils.saveOrupdatePersonInfoToDatabase(FeizaoApp.mConctext, JacksonUtil.readValue(String.valueOf(result), PersonInfo.class));
//						DatabaseUtils.getPersonInfoByUid(mActivity, mPersonUid);
					}
					// 如果fragment未回收，发送消息
					if (meFragment != null) meFragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.GET_USER_INFO_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				BaseFragmentActivity meFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null) meFragment.sendMsg(msg);
			}
		}

	}


	/**
	 * 关注用户信息回调 Reason: TODO ADD REASON(可选). <br/>
	 */
	private class FollowCallbackData implements CallbackDataHandle {

		private WeakReference<BaseFragmentActivity> mAcivity;

		public FollowCallbackData(BaseFragmentActivity fragment) {
			mAcivity = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FollowCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				msg.what = MsgTypes.FOLLOW_SUCCESS;
				updateLocalAttentionStatus(MsgTypes.FOLLOW_SUCCESS);
			} else {
				msg.what = MsgTypes.FOLLOW_FAILED;
				msg.obj = errorMsg;
			}
			BaseFragmentActivity baseFragmentActivity = mAcivity.get();
			// 如果fragment未回收，发送消息
			if (baseFragmentActivity != null) baseFragmentActivity.sendMsg(msg);

		}
	}

	/**
	 * 取消关注用户信息回调 Reason: TODO ADD REASON(可选). <br/>
	 */
	private class RemoveFollowCallbackData implements CallbackDataHandle {

		private WeakReference<BaseFragmentActivity> mAcivity;

		public RemoveFollowCallbackData(BaseFragmentActivity fragment) {
			mAcivity = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FollowCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				msg.what = MsgTypes.REMOVE_FOLLOW_SUCCESS;
				updateLocalAttentionStatus(MsgTypes.REMOVE_FOLLOW_SUCCESS);
			} else {
				msg.what = MsgTypes.REMOVE_FOLLOW_FAILED;
				msg.obj = errorMsg;
			}
			BaseFragmentActivity baseFragmentActivity = mAcivity.get();
			// 如果fragment未回收，发送消息
			if (baseFragmentActivity != null) baseFragmentActivity.sendMsg(msg);
		}

	}
}
