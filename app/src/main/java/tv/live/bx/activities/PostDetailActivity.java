package tv.live.bx.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.RelayListAdapter;
import tv.live.bx.adapters.RelayListAdapter.IOnclickListener;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Consts;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.common.WebConstants;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.emoji.SelectFaceHelper;
import tv.live.bx.emoji.SelectFaceHelper.OnFaceOprateListener;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.DateUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.HtmlUtil;
import tv.live.bx.library.util.NetworkImageGetter;
import tv.live.bx.receiver.ConnectionChangeReceiver;
import tv.live.bx.receiver.ConnectionChangeReceiver.NetwrokChangeCallback;
import tv.live.bx.ui.ActionSheetDialog;
import tv.live.bx.ui.ActionSheetDialog.OnSheetItemClickListener;
import tv.live.bx.ui.ActionSheetDialog.SheetItemColor;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.PullRefreshListView;
import tv.live.bx.ui.popwindow.MorePopWindow;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.UMShareAPI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

/**
 * 帖子主页 Title: PostActivity.java
 *
 * @version 1.0
 * @CreateDate 2014-8-13
 */
@SuppressLint("NewApi")
public class PostDetailActivity extends BaseFragmentActivity implements OnClickListener {
	private static final long REPORT_GEO_INTERVAL = DateUtils.HOUR_IN_MILLIS;

	/**
	 * 回到顶部按钮
	 */
	private ImageView scrollTopIv;

	private PullRefreshListView mListView;
	/**
	 * 加载更多FootView
	 */
	private ListFooterLoadView mListFooterLoadView;
	/**
	 * 目前暂时都刷新
	 */
	private static boolean isRefresh = true;
	/**
	 * 获取最新页面时，page为0，page以此累加
	 */
	private static int page = 0;

	/**
	 * 加载loading
	 */
	private LoadingProgress mLoadProgress;
	/**
	 * 更多显示数据
	 */
	private List<Map<String, Integer>> moreLists = new ArrayList<>();
	/**
	 * 列表头布局
	 */
	private LinearLayout headLayout, rootLayout;
	private RelativeLayout mSupportLayout;
	/**
	 * 列表头信息控件 start
	 */
	private ImageView mPhotoIv, mUserLevel, mCollectImageView, mSupportIV;
	private LinearLayout mPictureLayout;
	/**
	 * 当跳楼时，需要加载上面楼层数据时
	 */
	private TextView mClickFlushTv;
	private TextView mNicknameTv, mTimerTv, mContentTv, mSupportTv, mReplyTv, mViewTv, mReplyTip, mTitleTv;
	/**
	 * 列表头信息控件 end
	 */

	private RelayListAdapter mRelayAdapter;
	private LayoutInflater inflater;
	public static final String KEY_TITLE = "title";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_EXTRAS = "extras";
	private MorePopWindow morePopWindow;
	/** 帖子回复相关数据 */
	// private List<Map<String, Object>> mListData;
	/**
	 * 主贴信息
	 */
	private Map<String, String> subjectInfo;
	/**
	 * 楼层Id
	 */
	private String fReplayId;

	/**
	 * 点击楼层 下拉按钮弹出的对话框
	 */
	private ActionSheetDialog actionSheetDialog;

	/**
	 * 输入框
	 */
	private EditText inputEditText;
	/**
	 * 发送
	 */
	private Button sendBtn;
	/**
	 * 网络监听广播
	 */
	private ConnectionChangeReceiver networkReceiver;
	/**
	 * 当前是否无网络
	 */
	private boolean isNoNetwork = false;

	/**
	 * 回复帖子
	 */
	private boolean replyPost = true;

	/**
	 * 贴字ID
	 */
	private String toFReplyId;
	/**
	 * 回复跟帖ID
	 */
	private String toReplyId;
	/**
	 * 回复的用户ID
	 */
	private String toUid;
	/**
	 * 回复的位置
	 */
	private int toPosition;
	/**
	 * 回复的名字
	 */
	private String toNickname;
	/**
	 * 回复人的等级
	 */
	private String toUserLevel;

	/**
	 * 输入框布局
	 */
	private LinearLayout inputLayout;
	/**
	 * 键盘是否弹出
	 */
	private boolean isKeyboardUp = false;
	/**
	 * 　表情
	 */
	private LinearLayout moGvEmotions;
	// /** 表情适配器 */
	// private EmotionGridAdapter moEmotionAdapter;
	/**
	 * 表情按钮
	 */
	private ImageView moIvEmotion;

	private SelectFaceHelper mFaceHelper;

	/**
	 * 点击View的底部坐标
	 */
	private int clickViewButtom = 0;
	/**
	 * 输入框的顶部坐标
	 */
	private int inputViewTop = 0;

	/**
	 * 分享对话框
	 */
	private String shareContent; // "果酱||鲜肉大叔妖男Young，基腐宅萌有果酱,快来看****的直播，美CRY！！  ";
	private String shareTitle; // "果酱直播";
	private String shareUrImg; // "http://www.guojiang.tv/img/roomlogo/poyin.jpg";
	private String shareUrl; // "http://www.guojiang.tv";
	private Map<String, String> shareInfo = new HashMap<String, String>();

	public static String F_REPLAY_ID = "f_replay_id";
	// 跳楼的位置
	private int jump_position = 0;
	// 是否跳楼，（只有从评论中进来才需要跳楼，且也只有第一次加载）
	private boolean isJump = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inflater = LayoutInflater.from(getApplicationContext());
		subjectInfo = (Map<String, String>) getIntent().getSerializableExtra("subjectInfo");
		fReplayId = getIntent().getStringExtra(F_REPLAY_ID);
		// 如果没有楼层Id，不需要跳楼，直接请求数据
		if (!TextUtils.isEmpty(fReplayId)) {
			isJump = true;
		}
		// 初始化请求页数
		page = 0;
		// 初始化刷新
		isRefresh = false;

		// 初始化UI
		initUI(inflater);

		initMoreList();
		// 加载帖子详情
		BusinessUtils.getPostDetailData(mActivity, subjectInfo.get("id"), fReplayId, new PostDetailCallbackData(
				PostDetailActivity.this));
		// 如果不需要跳楼,直接加载回复列表
		if (!isJump) {
			// 初始化请求数据
			reRequestData(false);
		}

		initShareData();
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_post_layout;
	}

	@Override
	public void initWidgets() {
	}

	@Override
	protected void setEventsListeners() {
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("subjectInfo", (Serializable) subjectInfo);
		// if (!TextUtils.isEmpty(fReplayId)) {
		// outState.putString(F_REPLAY_ID, fReplayId);
		// }
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		subjectInfo = (Map<String, String>) savedInstanceState.getSerializable("subjectInfo");
		// fReplayId = savedInstanceState.getString(F_REPLAY_ID);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			// 充值
			case R.id.rechargeBtn:
				break;
			// 更多
			case R.id.top_right:
				if (morePopWindow == null || !morePopWindow.isShowing()) {
					initMoreList();
					morePopWindow = new MorePopWindow(mActivity, 0);
					morePopWindow.showAsDropDown(v, 0, 0);
				}

				break;
			// 返回
			case R.id.top_left:
				onBackPressed();
				break;
			// 发送
			case R.id.playing_btn_send_msg:
				sendRelayText();
				break;
			// 回到顶部
			case R.id.scroll_top:
				setListViewPos(0);
				break;
		}

	}

	private void initMoreList() {
		moreLists.clear();
		Map<String, Integer> item1 = new HashMap<>();
		if (mCollectImageView.isSelected()) {
			item1.put("imageId", R.drawable.btn_collect_pre);
			item1.put("textId", R.string.commutity_collect_cancel);
		} else {
			item1.put("imageId", R.drawable.btn_clooect_nor);
			item1.put("textId", R.string.commutity_collect);
		}
		moreLists.add(item1);
		item1 = new HashMap<>();
		item1.put("imageId", R.drawable.icon_share_sm);
		item1.put("textId", R.string.commutity_share_item);
		moreLists.add(item1);
		item1 = new HashMap<>();
		if (AppConfig.getInstance().isLogged && UserInfoConfig.getInstance().id.equals(subjectInfo.get("uid"))) {
			item1.put("imageId", R.drawable.icon_delete);
			item1.put("textId", R.string.commutity_delete);
		} else {
			item1.put("imageId", R.drawable.icon_warning_sm);
			item1.put("textId", R.string.commutity_report_item);
		}
		moreLists.add(item1);
	}

	/**
	 * 滚动ListView到指定位置
	 *
	 * @param position
	 */
	private void setListViewPos(int position) {
		mListView.setSelection(position);
		// mListView.setSelectionAfterHeaderView();
		// if (android.os.Build.VERSION.SDK_INT >= 8) {
		// mListView.smoothScrollToPosition(position);
		// } else {
		// mListView.setSelection(position);
		// }
	}

	/**
	 * 注册广播（消息更新广播）
	 */
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		networkReceiver = new ConnectionChangeReceiver();
		networkReceiver.setOnNetChangeListener(new NetwrokChangeCallback() {

			@Override
			public void wifiConnected() {
				EvtLog.e(TAG, "ConnectionChangeReceiver wifiConnected");
				isNoNetwork = false;
			}

			@Override
			public void noConnected() {
				EvtLog.e(TAG, "ConnectionChangeReceiver noConnected");
				isNoNetwork = true;
			}

			@Override
			public void gprsConnected() {
				EvtLog.e(TAG, "ConnectionChangeReceiver gprsConnected");
				isNoNetwork = false;
			}
		});
		this.registerReceiver(networkReceiver, filter);
	}

	/**
	 * 注销广播
	 */
	private void unregisterReceiver() {
		if (networkReceiver != null)
			this.unregisterReceiver(networkReceiver);
	}

	/**
	 * 通过此方法监听键盘的弹出/隐藏
	 */
	private class OnReLayout implements OnGlobalLayoutListener {
		@SuppressLint("NewApi")
		@Override
		public void onGlobalLayout() {
			int liRootHeight = rootLayout.getRootView().getHeight() - rootLayout.getHeight();
			EvtLog.d(TAG, "ReLayout RootView.getRootView height:" + liRootHeight);
			// 如果高度差超过100像素，就很有可能是有软键盘...
			if (liRootHeight > 100) {
				// 是否已经弹出虚拟键盘
				if (!isKeyboardUp) {
					isKeyboardUp = true;
					Rect rect = new Rect();
					inputLayout.getGlobalVisibleRect(rect);
					inputViewTop = rect.top;
				}
			} else {
				// 是否已经隐藏虚拟键盘
				if (isKeyboardUp && moGvEmotions.getVisibility() == View.GONE) {
					isKeyboardUp = false;
					// 如果没有输入任何内容，则键盘隐藏就重新初始化回复
					if (TextUtils.isEmpty(inputEditText.getText().toString())) {
						initRelayPost();
					}
				}
			}
		}
	}

	/**
	 * 表情按钮事件
	 */
	private class OnShowHideEmotions implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			if (null == mFaceHelper) {
				mFaceHelper = new SelectFaceHelper(PostDetailActivity.this, moGvEmotions);
				mFaceHelper.setFaceOpreateListener(mOnFaceOprateListener);
			}

			if (moGvEmotions.getVisibility() == View.VISIBLE) {
				moGvEmotions.setVisibility(View.GONE);
				// 是否已经隐藏虚拟键盘
				if (isKeyboardUp) {
					isKeyboardUp = false;
				}
			} else {
				InputMethodManager loImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				loImm.hideSoftInputFromWindow(getWindow().peekDecorView().getApplicationWindowToken(), 0);
				moGvEmotions.setVisibility(View.VISIBLE);
				// 是否已经弹出虚拟键盘
				if (!isKeyboardUp) {
					isKeyboardUp = true;
				}

			}
		}
	}

	OnFaceOprateListener mOnFaceOprateListener = new OnFaceOprateListener() {
		@Override
		public void onFaceSelected(SpannableString spanEmojiStr) {
			if (null != spanEmojiStr) {
				inputEditText.getText().insert(inputEditText.getSelectionStart(), spanEmojiStr);
			}
		}

		@Override
		public void onFaceDeleted() {
			int selection = inputEditText.getSelectionStart();
			String text = inputEditText.getText().toString();
			if (selection > 0) {
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2)) {
					int start = text.lastIndexOf("[");
					int end = selection;
					inputEditText.getText().delete(start, end);
					return;
				}
				inputEditText.getText().delete(selection - 1, selection);
			}
		}

	};

	private class OnInputText implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			moGvEmotions.setVisibility(View.GONE);
			return false;
		}
	}

	/**
	 * 发送回复信息
	 */
	private void sendRelayText() {
		String content = inputEditText.getText().toString().trim();
		if (TextUtils.isEmpty(content)) {
			return;
		}
		if (!AppConfig.getInstance().isLogged)
			showLoginTip();
		else {
			if (isNoNetworkTipMsg())
				return;
			// CharSequence msgStr =
			// ParseEmojiMsgUtil.convertToMsg(inputEditText.getText(),
			// PostDetailActivity.this);//
			// 这里不要直接用mEditMessageEt.getText().toString();
			String replyContent = inputEditText.getText().toString();
			// 回复“帖子”
			if (replyPost == true) {
				BusinessUtils.relayPostInfo(mActivity, new RelayCallbackData(this, Constants.REPLY_POST_TYPE_POST,
						replyContent), subjectInfo.get("id"), replyContent);
				// 生成一条数据
				Map<String, Object> info = new HashMap<String, Object>();
			} else {
				BusinessUtils.relayLoucengInfo(mActivity, new RelayCallbackData(this, Constants.REPLY_POST_TYPE_REPLY,
						replyContent), toFReplyId, toReplyId, toUid, replyContent);
			}
			inputEditText.setText("");
			InputMethodManager loImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			loImm.hideSoftInputFromWindow(getWindow().peekDecorView().getApplicationWindowToken(), 0);

		}
	}

	/**
	 * 初始化，回复帖子
	 */
	private void initRelayPost() {
		replyPost = true;
		inputEditText.setHint(R.string.me_speak);
		inputEditText.setText("");
	}

	/**
	 * 显示登录提示对话框
	 */
	private void showLoginTip() {
		Utils.requestLoginOrRegister(PostDetailActivity.this, "需要登录，请登录", Constants.REQUEST_CODE_LOGIN);
	}

	/**
	 * 是否有网络，且提示消息
	 */
	private boolean isNoNetworkTipMsg() {
		// 如果没有网络，不进行下面操作
		if (isNoNetwork) {
			UiHelper.showToast(this, Constants.NETWORK_FAIL);
			return true;
		} else
			return false;
	}

	// 如果有使用任一平台的SSO授权,则必须在对应的activity中实现onActivityResult方法, 并添加如下代码
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** umeng授权、分享需要重写 */
		UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_LOGIN " + resultCode);
			if (resultCode == LoginActivity.RESULT_CODE_OK) {
				// 加载帖子详情
				BusinessUtils.getPostDetailData(mActivity, subjectInfo.get("id"), fReplayId,
						new PostDetailCallbackData(PostDetailActivity.this));
			} else {
			}
			// redirectPassiveEventActivity(getIntent(), false);
		}
	}

	/**
	 * 页面“更多”操作的回调处理类
	 */
	private class MoreItemListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.item1_layout:
					MobclickAgent.onEvent(FeizaoApp.mConctext, "collectPost");
					// 收藏
					if (!AppConfig.getInstance().isLogged)
						showLoginTip();
					else {
						if (mCollectImageView.isSelected()) {
							BusinessUtils.removeCollectPostInfo(mActivity, new CollectCallbackData(PostDetailActivity.this,
									!mCollectImageView.isSelected()), subjectInfo.get("id"));
						} else {
							BusinessUtils.collectPostInfo(mActivity, new CollectCallbackData(PostDetailActivity.this,
									!mCollectImageView.isSelected()), subjectInfo.get("id"));
						}
					}
					break;
				case R.id.item2_layout:
					MobclickAgent.onEvent(FeizaoApp.mConctext, "sharePost");
					if (!AppConfig.getInstance().isLogged) {
						Utils.requestLoginOrRegister(mActivity, "分享需要先登录", Constants.REQUEST_CODE_LOGIN);
						return;
					}
					if (isNoNetworkTipMsg())
						return;
					// 设置分享内容
					if (!TextUtils.isEmpty(subjectInfo.get("headPic")))
						shareUrImg = subjectInfo.get("headPic");
					if (subjectInfo != null) {
						// 替换图片
						shareContent = subjectInfo.get("content").replaceAll(Constants.REGX_PHOTO,
								Constants.REPLACE_PHOTO);
						// 替换表情图片
						shareContent = shareContent.replaceAll(Constants.REGX_PHOTO_EMOJI, Constants.REPLACE_PHOTO_EMOJI);
					}
					if (subjectInfo != null) {
						shareUrl = WebConstants.getFullWebMDomain(WebConstants.SHARE_POST_PIX) + subjectInfo.get("id");
					}

					shareInfo.put(ShareDialogActivity.Share_Content, shareContent);
					shareInfo.put(ShareDialogActivity.Share_Img, shareUrImg);
					shareInfo.put(ShareDialogActivity.Share_Title, shareTitle);
					shareInfo.put(ShareDialogActivity.Share_Url, shareUrl);
					shareInfo.put(ShareDialogActivity.Share_Dialog, String.valueOf(true));
					ActivityJumpUtil.toShareActivity(mActivity, shareInfo);
					break;
				case R.id.item3_layout:
					// 如果是自己的发表的帖子，就是删除，否是就是举报
					if (AppConfig.getInstance().isLogged && UserInfoConfig.getInstance().id.equals(subjectInfo.get("uid"))) {
					} else {
						ActivityJumpUtil.toReportActivity(mActivity, Constants.COMMON_REPORT_TYPE_POST,
								subjectInfo.get("id"), 0);

					}
					break;
				default:
					break;
			}

		}
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MsgTypes.MSG_POST_DETAIL_FAILED:
				break;

			case MsgTypes.MSG_POST_DETAIL_SUCCESS:
				subjectInfo = (Map<String, String>) msg.obj;
				initPostData();
				// 如果有楼层Id数据且没有跳过，则跳楼
				if (!TextUtils.isEmpty(subjectInfo.get("fReplyPosition")) && isJump) {
					jump_position = Integer.parseInt(subjectInfo.get("fReplyPosition"));
					if (jump_position > 0) {
						page = jump_position / Constants.REQUEST_POST_LIMIT;
					}
					requestPlazaData(page);
				}
				break;
			case MsgTypes.MSG_REPLY_LIST_FAILED:
				mListView.notifyTaskFinished();
				if (mRelayAdapter.isEmpty()) {
					// String text = mThis.getString(R.string.a_loading_failed);
					// mLoadProgress.Failed(text, 0);
				} else {
					UiHelper.showToast(mActivity, R.string.a_tips_net_error);
					mLoadProgress.Hide();
					mListFooterLoadView.onLoadingFailed();
				}

				break;

			case MsgTypes.MSG_REPLY_LIST_SUCCESS:
				Object[] objects = (Object[]) msg.obj;
				boolean isRefreh = (Boolean) objects[0];
				List<Map<String, Object>> listData = (List<Map<String, Object>>) objects[1];
				if (isRefreh) { // 初始化或者下拉刷新模式
					// 如果刷新列表，就隐藏点击刷新按钮
					mClickFlushTv.setVisibility(View.GONE);
					mListView.notifyTaskFinished(); // 收起正在刷新HeaderView
					mRelayAdapter.clearData();
					mRelayAdapter.addData(listData);
					if (listData == null || listData.size() == 0) {
						mReplyTip.setVisibility(View.VISIBLE);
					}
				} else {
					// 加载更多数据模式
					if (listData.isEmpty()) {
						// 是否之前有数据了，如果没有显示没有评论，否则显示没有更多
						if (mRelayAdapter.getCount() == -1) {
							mReplyTip.setVisibility(View.VISIBLE);
							mListFooterLoadView.hide();
						} else {
							mListFooterLoadView.onNoMoreData();
						}
					} else {
						// 隐藏ListView的FootView
						mListFooterLoadView.hide();
						mRelayAdapter.addData(listData);

						// 跳楼操作
						if (!TextUtils.isEmpty(fReplayId) && isJump) {
							int po = jump_position % Constants.REQUEST_POST_LIMIT + mListView.getHeaderViewsCount();
							EvtLog.e(TAG, "jump position:" + po);
							mListView.setSelection(po);
							isJump = false;
							if (page > 1) {
								mClickFlushTv.setVisibility(View.VISIBLE);
								mRelayAdapter.setBasePosition((page - 1) * Constants.REQUEST_POST_LIMIT);
							}

						}
					}
				}
				// 设置没有数据的EmptyView
				String text = mActivity.getString(R.string.a_list_data_empty);
				mLoadProgress.Succeed(text, R.drawable.a_common_no_data);
				break;
			case MsgTypes.MSG_COLLECT_SUCCESS:
				Boolean isCollct = (Boolean) msg.obj;
				if (isCollct) {
					mCollectImageView.setSelected(true);
					UiHelper.showToast(mActivity, "收藏成功");
				} else {
					mCollectImageView.setSelected(false);
					UiHelper.showToast(mActivity, "取消收藏成功");
				}
				break;
			case MsgTypes.MSG_COLLECT_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;

			case MsgTypes.MSG_REPLY_SUCCESS:
				Map<String, String> map = (Map<String, String>) msg.obj;
				Bundle bundle = msg.getData();
				int type = bundle.getInt("type");
				String content = bundle.getString("content");
				String msgContent = bundle.getString("msg");
				// 如果回复成功，把数据添加到listview
				if (Constants.REPLY_POST_TYPE_POST == type) {
					// 添加楼层数据
					Map<String, Object> data = new HashMap<String, Object>();
					data.put("uid", UserInfoConfig.getInstance().id);
					data.put("id", map.get("replyId"));
					data.put("nickname", UserInfoConfig.getInstance().nickname);
					data.put("headPic", UserInfoConfig.getInstance().headPic);
					data.put("user_level", UserInfoConfig.getInstance().level+ "");
					data.put("content", content);
					data.put("update_time", String.valueOf(new Date().getTime() / 1000));
					data.put("lzlReply", new ArrayList<Map<String, String>>());

					// 如果没有一页数据，把数据加入到列表中
					if (mRelayAdapter.getData().size() < Constants.REQUEST_POST_LIMIT
							|| mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_NOMORE) {
						mRelayAdapter.addData(data);
					}

				} else {
					// 添加回复数据
					Map<String, String> data = new HashMap<>();
					data.put("uid", UserInfoConfig.getInstance().id);
					data.put("id", map.get("lzlReplyId"));
					data.put("nickname", UserInfoConfig.getInstance().nickname);
					data.put("headPic", UserInfoConfig.getInstance().headPic);
					data.put("user_level", UserInfoConfig.getInstance().level + "");

					data.put("to_reply_id", toReplyId);
					data.put("to_uid", toUid);
					data.put("to_nickname", toNickname);
					data.put("to_user_level", toUserLevel);
					data.put("content", content);

					data.put("ctime", String.valueOf(new Date().getTime() / 1000));

					Map<String, Object> louceng = (Map<String, Object>) mRelayAdapter.getItem(toPosition);
					List<Map<String, String>> lmPlayer = (List<Map<String, String>>) louceng.get("lzlReply");
					lmPlayer.add(data);
					// 刷新列表
					mRelayAdapter.notifyDataSetChanged();
				}
				// 回复成功，就消失
				mReplyTip.setVisibility(View.GONE);
				UiHelper.showToast(mActivity, msgContent);
				break;
			case MsgTypes.MSG_REPLY_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;
			case MsgTypes.MSG_DELETE_REPLAY_SUCCESS:
				Bundle bundle2 = msg.getData();
				int type2 = bundle2.getInt("type");
				int position = bundle2.getInt("position");
				// 如果删除成功，把数据添加到listview
				if (Constants.DELETE_REPLY == type2) {
					mRelayAdapter.getData().remove(position);
					mRelayAdapter.notifyDataSetChanged();
				} else {

					// Map<String, Object> louceng = (Map<String, Object>)
					// mRelayAdapter.getItem(toPosition);
					// List<Map<String, String>> lmPlayer = (List<Map<String,
					// String>>) louceng.get("lzlReply");
					// lmPlayer.add(data);
					// // 刷新列表
					// mRelayAdapter.notifyDataSetChanged();
				}
				if (mRelayAdapter.getCount() == -1) {
					// 回复成功，就消失
					mReplyTip.setVisibility(View.VISIBLE);
				}
				UiHelper.showToast(mActivity, "删除成功");
				break;
			case MsgTypes.MSG_DELETE_REPLAY_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;
		}

	}

	/**
	 * 初始化UI控件
	 */
	private void initUI(LayoutInflater inflater) {
		rootLayout = (LinearLayout) findViewById(R.id.root_layout);
		rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnReLayout());
		initTitle();
		headLayout = (LinearLayout) inflater.inflate(R.layout.activity_post_head_layout, null);
		initPostInfo(headLayout);

		scrollTopIv = (ImageView) findViewById(R.id.scroll_top);
		scrollTopIv.setOnClickListener(this);

		initListView(inflater);

		inputEditText = (EditText) findViewById(R.id.playing_et_msg_content);
		inputEditText.setOnTouchListener(new OnInputText());
		moGvEmotions = (LinearLayout) findViewById(R.id.playing_gv_eomotions);
		moIvEmotion = (ImageView) findViewById(R.id.playing_iv_emotion);

		inputLayout = (LinearLayout) findViewById(R.id.playing_ll_edt_input);
		inputEditText.setHint(R.string.me_speak);
		sendBtn = (Button) findViewById(R.id.playing_btn_send_msg);
		sendBtn.setOnClickListener(this);

		moIvEmotion.setOnClickListener(new OnShowHideEmotions());
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.commutity_post);
		mTopRightImageLayout.setOnClickListener(this);
		mTopRightImageLayout.setVisibility(View.VISIBLE);
		mTopBackLayout.setOnClickListener(this);
	}

	/**
	 * 初始化帖子信息
	 */
	private void initPostInfo(View v) {
		mPhotoIv = (ImageView) v.findViewById(R.id.item_photo);
		mSupportIV = (ImageView) v.findViewById(R.id.item_support);
		mSupportTv = (TextView) v.findViewById(R.id.item_support_num);
		mSupportLayout = (RelativeLayout) v.findViewById(R.id.item_support_layout);

		// mCollectImageView = (ImageView) v.findViewById(R.id.item_collect);

		mNicknameTv = (TextView) v.findViewById(R.id.item_nickname);
		mUserLevel = (ImageView) v.findViewById(R.id.item_userlevel);

		mTimerTv = (TextView) v.findViewById(R.id.item_time);
		mContentTv = (TextView) v.findViewById(R.id.item_content);
		mTitleTv = (TextView) v.findViewById(R.id.item_title);

		mReplyTv = (TextView) v.findViewById(R.id.item_replay_num);
		mViewTv = (TextView) v.findViewById(R.id.item_view_num);
		// 没有评论数据提示
		mReplyTip = (TextView) v.findViewById(R.id.item_no_data);
		mPictureLayout = (LinearLayout) v.findViewById(R.id.item_picture_grid);
		mClickFlushTv = (TextView) v.findViewById(R.id.item_flush);
		mClickFlushTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 请求主播数据
				reRequestData(false);
				// 加载帖子详情
				BusinessUtils.getPostDetailData(mActivity, subjectInfo.get("id"), fReplayId,
						new PostDetailCallbackData(PostDetailActivity.this));

			}
		});
		if (subjectInfo.get("nickname") == null)
			return;
		initPostData();
	}

	private void initPostData() {
		ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity.getApplicationContext(), mPhotoIv, subjectInfo.get("headPic"));
		// try {
		// JSONArray jsonarray = new JSONArray((String)
		// subjectInfo.get("images"));
		// if (jsonarray.length() > 0)
		// mPictureLayout.removeAllViews();
		// final List<String> imageUrl = new ArrayList<String>();
		// for (int i = 0; i < jsonarray.length(); i++) {
		//
		// if (i < Constants.SUBJECT_LIST_IMAGE_LIMIT) {
		// LinearLayout.LayoutParams params = new
		// LinearLayout.LayoutParams(Constants.IMAGE_WIDTH,
		// Constants.IMAGE_WIDTH);
		// params.topMargin = (int) (13.33 * FeizaoApp.metrics.density);
		// params.rightMargin = (int) (6.66 * FeizaoApp.metrics.density);
		// ImageView imageview = new ImageView(mThis);
		// imageview.setScaleType(ScaleType.CENTER_CROP);
		// mPictureLayout.addView(imageview, params);
		// ImageLoader.getInstance()
		// .displayImage((String) jsonarray.get(i), imageview,
		// Constants.optionsImage);
		// imageview.setTag(i);
		// imageUrl.add((String) jsonarray.get(i));
		// imageview.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// ActivityJumpUtil.toImageBrowserActivity(mThis, (Integer) v.getTag(),
		// imageUrl);
		// }
		// });
		// } else {
		// imageUrl.add((String) jsonarray.get(i));
		// }
		//
		// }
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }
		mNicknameTv.setText(subjectInfo.get("nickname"));
		if (!TextUtils.isEmpty(subjectInfo.get("user_level"))) {
			ImageLoaderUtil.with().loadImage(mActivity.getApplicationContext(), mUserLevel, Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, subjectInfo.get("user_level")));
		}
		mTimerTv.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong(subjectInfo.get("last_reply_time"))));

		mContentTv.setText(HtmlUtil.htmlTextDeal(mActivity, subjectInfo.get("content"), new NetworkImageGetter(
				mContentTv, (int) (FeizaoApp.metrics.widthPixels - 16 * 2 * FeizaoApp.metrics.density)), null));
		mContentTv.setText(HtmlUtil.htmlTextUrlClick(mActivity, mContentTv.getText()));
		mTitleTv.setText(subjectInfo.get("title").trim());
		EvtLog.e("", "moSupport " + subjectInfo.get("isSupported"));

		// mCollectImageView.setSelected(Boolean.parseBoolean(subjectInfo.get("bookmark")));
		// mCollectImageView.setOnClickListener(this);

		if (Boolean.parseBoolean(subjectInfo.get("isSupported").toString())) {
			mSupportIV.setSelected(true);
		} else {
			mSupportIV.setSelected(false);
		}
		mSupportIV.setTag(subjectInfo.get("isSupported").toString());

		mSupportLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onSupportClick();
			}
		});
		mSupportTv.setText(subjectInfo.get("support"));
		mReplyTv.setText(subjectInfo.get("reply_count"));
		mViewTv.setText(subjectInfo.get("view_count"));
		// mReplyTv.setText((String) subjectInfo.get("reply_count"));
	}

	/**
	 * 添加最新的活动到列表的第一项
	 */
	// private void addLatestItemByGid(String gid) {
	// CEvent event = EventDBManager.getEventByGid(mThis, gid);
	// if (event != null) {
	// PublicEventItem item = PlazaUtils.convert2PlazaItem(event);
	// if (mAdapter != null) {
	// mAdapter.addFirstItem(item);
	// }
	// }
	// }

	/**
	 * 初始化下拉刷新ListView
	 *
	 * @param inflater
	 */
	private void initListView(LayoutInflater inflater) {
		mListView = (PullRefreshListView) findViewById(R.id.author_listview);
		// mListView.setOnItemClickListener(this);
		mListView.setTopHeadHeight(0);
		mListView.addHeaderView(headLayout);

		mRelayAdapter = new RelayListAdapter(mActivity, new RelayItemOnclick());
		mListView.setAdapter(mRelayAdapter);
		// 下拉刷新数据
		mListView.setTask(new Runnable() {
			@Override
			public void run() {
				// 请求主播数据
				reRequestData(false);
				// 加载帖子详情
				BusinessUtils.getPostDetailData(mActivity, subjectInfo.get("id"), fReplayId,
						new PostDetailCallbackData(PostDetailActivity.this));
			}
		});
		View view = inflater.inflate(R.layout.a_common_list_header_hint, null);
		view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, getResources()
				.getDimensionPixelSize(R.dimen.list_hintview_height)));
		mListView.setPullnReleaseHintView(view);
		// 设置正确的颜色
		mListView.setHeaderBackgroudColor(getResources().getColor(R.color.app_background));
		// 设置上滑动加载更多
		mListFooterLoadView = (ListFooterLoadView) inflater.inflate(R.layout.a_common_list_footer_loader_view, null);
		mListFooterLoadView.hide();
		mListFooterLoadView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ListFooterLoadView listFooterLoadView = (ListFooterLoadView) v;
				int status = listFooterLoadView.getStatus();
				if (status == ListFooterLoadView.STATUS_FAILED || status == ListFooterLoadView.STATUS_NOMORE) {
					listFooterLoadView.onLoadingStarted();
					isRefresh = false;
					requestPlazaData(page);
				}
			}
		});
		mListView.addFooterView(mListFooterLoadView);
		mListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					moGvEmotions.setVisibility(View.GONE);
					// 是否已经隐藏虚拟键盘
					if (isKeyboardUp) {
						InputMethodManager loImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						loImm.hideSoftInputFromWindow(getWindow().peekDecorView().getApplicationWindowToken(), 0);
					}
				}
				return false;
			}
		});
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// if (mListView.getChildCount() >
				// mListView.getHeaderViewsCount()
				// + mListView.getFooterViewsCount()) {
				// EvtLog.e(TAG,
				// "firstVisibleItem,visibleItemCount,totalItemCount:" +
				// firstVisibleItem + ","
				// + visibleItemCount + "," + totalItemCount);
				// 如果有滑动动作，不是在顶部，显示“回到顶部”按钮
				if (firstVisibleItem == 0) {
					scrollTopIv.setVisibility(View.GONE);
				} else {
					scrollTopIv.setVisibility(View.VISIBLE);
				}
				if (totalItemCount > mListView.getHeaderViewsCount() + mListView.getFooterViewsCount()) {
					if (mListFooterLoadView.getParent() == mListView) {
						// 至少翻过一项，才有可能执行加载更过操作
						// if (mListFooterLoadView.getStatus() ==
						// ListFooterLoadView.STATUS_HIDDEN
						// && mListView.getFirstVisiblePosition() >
						// mListView.getHeaderViewsCount()) {
						if (mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_HIDDEN
								&& mListView.getFirstVisiblePosition() > mListView.getHeaderViewsCount()) {
							mListFooterLoadView.onLoadingStarted();
							EvtLog.d(TAG, "滚动加载更多");
							isRefresh = false;
							requestPlazaData(page);
						}
					} else {
						if (mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_FAILED
								|| mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_NOMORE) {
							mListFooterLoadView.hide();
						}
					}
				}
			}
		});
		// 设置默认图片
		mLoadProgress = (LoadingProgress) findViewById(R.id.progress);
		mLoadProgress.setProcessImageView(R.drawable.a_common_progress_circle);
		// 初始化loading(正在加载...)
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		mLoadProgress.setProgressClickListener(new onProgressClickListener() {

			@Override
			public void onReLoad(View v) {
				// 加载帖子详情
				BusinessUtils.getPostDetailData(mActivity, subjectInfo.get("id"), fReplayId,
						new PostDetailCallbackData(PostDetailActivity.this));
				// 重新加载数据
				reRequestData(true);

			}

			@Override
			public void onClick(View v) {
				// 加载帖子详情
				BusinessUtils.getPostDetailData(mActivity, subjectInfo.get("id"), fReplayId,
						new PostDetailCallbackData(PostDetailActivity.this));
				// 加载Banner,anchor列表
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
		// 重新初始化请求页面
		page = 0;
		if (subjectInfo == null)
			return;
		mLoadProgress.Start(getResources().getString(R.string.a_progress_loading));
		// 清空界面数据
		if (clearAdapter) {
			mRelayAdapter.clearData();
			mRelayAdapter.notifyDataSetChanged();
		}
		isRefresh = true;
		requestPlazaData(page);
	}

	@Override
	public void onBackPressed() {
		if (moGvEmotions.getVisibility() == View.VISIBLE)
			moGvEmotions.setVisibility(View.GONE);
		else {
			super.onBackPressed();
		}
	}

	/**
	 * 向服务端请求主播列表数据
	 */
	private void requestPlazaData(int page) {
		BusinessUtils.getRelayListInfo(mActivity, page, Constants.REQUEST_POST_LIMIT,
				new GetRelayListCallbackData(this), Integer.parseInt(subjectInfo.get("id")));
	}

	/**
	 * 点赞
	 */
	private void onSupportClick() {
		if ("true".equals(mSupportIV.getTag().toString())) {
			UiHelper.showToast(mActivity, "您已经赞了,不能再赞了");
			return;
		}
		if (!AppConfig.getInstance().isLogged) {
			Utils.requestLoginOrRegister(mActivity, "点赞需要先登录", Constants.REQUEST_CODE_LOGIN);
			return;
		}
		// 先UI更新
		mSupportIV.setSelected(true);
		mSupportIV.setTag("true");
		BusinessUtils.support(mActivity, null, Integer.parseInt(subjectInfo.get("id")));
		int supportNum = Integer.parseInt(subjectInfo.get("support"));
		subjectInfo.put("support", String.valueOf(supportNum + 1));
		mSupportTv.setText(String.valueOf(supportNum + 1));
	}

	/**
	 * 点击楼层按钮，及TextView回复
	 */
	private class RelayItemOnclick implements IOnclickListener {

		@SuppressLint("NewApi")
		@Override
		public void onClick(View v, int position, Map<String, String> relayInfo) {
			// 如果是点击楼层的下拉框
			if (relayInfo == null) {
				showRelayDialog(position);
			}
			// 点击楼层回复文本
			else {
				Rect rect = new Rect();
				v.getGlobalVisibleRect(rect);
				clickViewButtom = rect.bottom;
				EvtLog.e(TAG, "clickViewY: position" + clickViewButtom + "," + position);
				showSoftInput(relayInfo.get("nickname"), (String) mRelayAdapter.getData().get(position).get("id"),
						relayInfo.get("id"), relayInfo.get("uid"), relayInfo.get("user_level"), position);

				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// 滚动列表
						EvtLog.e(TAG, "clickViewButtom: inputViewTop" + clickViewButtom + "," + inputViewTop);
						mListView.smoothScrollBy(clickViewButtom - inputViewTop, 100);
					}
				}, 500);

			}
		}
	}

	/**
	 * 弹出对话框
	 */
	private void showRelayDialog(final int position) {
		EvtLog.e(TAG, "showRelayDialog position" + position);
		final Map<String, Object> replayInfo = mRelayAdapter.getData().get(position);
		if (AppConfig.getInstance().isLogged && UserInfoConfig.getInstance().id.equals(replayInfo.get("uid"))) {
			actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
					.setCanceledOnTouchOutside(true)
					.addSheetItem("回复", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							MobclickAgent.onEvent(FeizaoApp.mConctext, "commentPost");
							showSoftInput((String) replayInfo.get("nickname"), (String) replayInfo.get("id"), "0",
									(String) replayInfo.get("uid"), null, position);
							mListView.setSelection(position + mListView.getHeaderViewsCount());
						}
					}).addSheetItem("删除", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							BusinessUtils.deletePostReply(mActivity, (String) replayInfo.get("id"),
									new DeleteRelayCallbackData(PostDetailActivity.this, Constants.DELETE_REPLY,
											position));
						}
					});
		} else {
			actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
					.setCanceledOnTouchOutside(true)
					.addSheetItem("回复", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							MobclickAgent.onEvent(FeizaoApp.mConctext, "commentPost");
							showSoftInput((String) replayInfo.get("nickname"), (String) replayInfo.get("id"), "0",
									(String) replayInfo.get("uid"), null, position);
							mListView.setSelection(position + mListView.getHeaderViewsCount());
						}
					}).addSheetItem("举报", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							ActivityJumpUtil.toReportActivity(mActivity, Constants.COMMON_REPORT_TYPE_REPLY, replayInfo
									.get("id").toString(), 0);
						}
					});
		}
		actionSheetDialog.show();

	}

	/**
	 * 显示输入框
	 */
	private void showSoftInput(final String nickname, String toFReplyId, String toReplyId, String toUid,
							   String toUserLevel, int position) {
		// 回复“帖子回复”
		replyPost = false;
		this.toFReplyId = toFReplyId;
		this.toReplyId = toReplyId;
		this.toUid = toUid;
		this.toPosition = position;
		this.toNickname = nickname;
		this.toUserLevel = toUserLevel;

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {

				inputEditText.setHint("回复" + nickname);
				inputEditText.setFocusable(true);
				inputEditText.setFocusableInTouchMode(true);
				inputEditText.requestFocus();
				InputMethodManager inputManager = (InputMethodManager) mActivity
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(inputEditText, 0);

			}
		}, 100);

	}

	private void initShareData() {
		// 初始化数据
		shareContent = "果酱||鲜肉大叔妖男Young，基腐宅萌有果酱,快来看的直播，美CRY！！  ";
		shareUrl = Consts.DOWNLOAD_URL_SERVER;
	}

	/**
	 * 帖子回复数据处理回调 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class GetRelayListCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public GetRelayListCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "GetRelayListCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {

					msg.what = MsgTypes.MSG_REPLY_LIST_SUCCESS;
					Object[] objects = new Object[]{isRefresh,
							JSONParser.parseMultiInMulti((JSONArray) result, new String[]{"lzlReply"})};

					msg.obj = objects;
					// 下次请求的页面数
					page++;
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
					EvtLog.e(TAG, e.toString());
					msg.what = MsgTypes.MSG_REPLY_LIST_FAILED;
					msg.obj = errorCode;
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				}
			} else {
				msg.what = MsgTypes.MSG_REPLY_LIST_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

	/**
	 * 回复帖子处理回调 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class RelayCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;
		private int type;
		private String content;

		public RelayCallbackData(BaseFragmentActivity fragment, int type, String content) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
			this.type = type;
			this.content = content;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "RelayCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_REPLY_SUCCESS;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					Bundle bundle = new Bundle();
					bundle.putInt("type", type);
					bundle.putString("content", content);
					bundle.putString("msg", errorMsg);
					msg.setData(bundle);
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_REPLY_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				msg.arg1 = type;
				BaseFragmentActivity activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

	/**
	 * 删除楼层回复处理回调 ClassName: DeleteRelayCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class DeleteRelayCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;
		private int type;
		private int position;

		public DeleteRelayCallbackData(BaseFragmentActivity fragment, int type, int position) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
			this.type = type;
			this.position = position;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "DeleteRelayCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_DELETE_REPLAY_SUCCESS;
					Bundle bundle = new Bundle();
					bundle.putInt("type", type);
					bundle.putInt("position", position);
					msg.setData(bundle);
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_DELETE_REPLAY_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				msg.arg1 = type;
				BaseFragmentActivity activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

	/**
	 * 收藏帖子处理回调 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class CollectCallbackData implements CallbackDataHandle {

		private WeakReference<BaseFragmentActivity> mFragment;
		private boolean isCollect;

		public CollectCallbackData(BaseFragmentActivity fragment, boolean isCollect) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
			this.isCollect = isCollect;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "CollectCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_COLLECT_SUCCESS;
					msg.obj = isCollect;
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_COLLECT_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

	/**
	 * 帖子详情回调 ClassName: BannerCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @version AuthorFragment
	 * @since JDK 1.6
	 */
	private static class PostDetailCallbackData implements CallbackDataHandle {

		private WeakReference<BaseFragmentActivity> mFragment;

		public PostDetailCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "PostDetailCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_POST_DETAIL_SUCCESS;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_POST_DETAIL_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				BaseFragmentActivity activity = mFragment.get();
				// 如果fragment未回收，发送消息
				if (activity != null)
					activity.sendMsg(msg);
			}
		}
	}

}
