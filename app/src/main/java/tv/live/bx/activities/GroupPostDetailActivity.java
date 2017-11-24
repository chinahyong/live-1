package tv.live.bx.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
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
import tv.live.bx.activities.GroupPostPublishActivity.PhotoData;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.adapters.RelayListAdapter;
import tv.live.bx.adapters.RelayListAdapter.IOnclickListener;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Consts;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.PhotoOperate;
import tv.live.bx.common.PhotoSelectImpl;
import tv.live.bx.common.Utils;
import tv.live.bx.common.WebConstants;
import tv.live.bx.common.photopick.ImageInfo;
import tv.live.bx.config.AppConfig;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.emoji.SelectFaceHelper;
import tv.live.bx.emoji.SelectFaceHelper.OnFaceOprateListener;
import tv.live.bx.fragments.LiveChatFragment;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.BitmapUtility;
import tv.live.bx.library.util.BitmapUtils;
import tv.live.bx.library.util.DateUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.HtmlTagHandler;
import tv.live.bx.library.util.HtmlUtil;
import tv.live.bx.library.util.NetworkImageGetter;
import tv.live.bx.listeners.AuthorityManageable;
import tv.live.bx.receiver.ConnectionChangeReceiver;
import tv.live.bx.receiver.ConnectionChangeReceiver.NetwrokChangeCallback;
import tv.live.bx.ui.ActionSheetDialog;
import tv.live.bx.ui.ActionSheetDialog.OnSheetItemClickListener;
import tv.live.bx.ui.ActionSheetDialog.SheetItemColor;
import tv.live.bx.ui.ListFooterLoadView;
import tv.live.bx.ui.LoadingProgress;
import tv.live.bx.ui.LoadingProgress.onProgressClickListener;
import tv.live.bx.ui.PullRefreshListView;
import com.lonzh.lib.network.JSONParser;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.UMShareAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
public class GroupPostDetailActivity extends BaseFragmentActivity implements OnClickListener, AuthorityManageable {
	public static final int PHOTO_MAX_COUNT = 1;
	public static final int RESULT_REQUEST_IMAGE = 1007;
	private PhotoOperate photoOperate = new PhotoOperate(this);
	/**
	 * 发送图片选择的图片信息
	 */
	private PhotoData mReplyPhotoData;
	private String mReplyPhotoFilePath;
	/**
	 * 显示图片选择的控件
	 */
	private RelativeLayout mReplyPictureLayout;
	private ImageView mReplyPicture, mReplyPictureBg, mReplyPictureClose;

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
	private List<Map<String, Integer>> moreLists = new ArrayList<Map<String, Integer>>();
	/**
	 * 列表头布局
	 */
	private LinearLayout headLayout, rootLayout;
	private LinearLayout mPictureLayout;
	private ImageView moIvPhoto, moIvPhotoV, moSupport, mShare, mMore, mUserLevel;
	private TextView moTvNickname, moTvContent, moTvSupport, moTvReply, mForumTitle, moTvTimer;
	/**
	 * 当跳楼时，需要加载上面楼层数据时
	 */
	private TextView mClickFlushTv, mReplyTip;
	/**
	 * 列表头信息控件 end
	 */

	private RelayListAdapter mRelayAdapter;
	private LayoutInflater inflater;
	public static final String KEY_TITLE = "title";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_EXTRAS = "extras";
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
	/**
	 * 选择图片按钮
	 */
	private ImageView moIvPicture;
	/**
	 * 输入清除
	 */
	private ImageView moIvClearInput;

	private SelectFaceHelper mFaceHelper;

	/**
	 * 点击View的底部坐标
	 */
	private int clickViewButtom = 0;
	/**
	 * 输入框的顶部坐标
	 */
	private int inputViewTop = 0;

	public static String F_REPLAY_ID = "f_replay_id";
	// 跳楼的位置
	private int jump_position = 0;
	// 是否跳楼，（只有从评论中进来才需要跳楼，且也只有第一次加载）
	private boolean isJump = false;

	private AlertDialog mProgress;

	private HtmlTagHandler mHtmlTagHandler = new HtmlTagHandler();
	private boolean mConfigPostInsertGroup = false;
	/**
	 * 区分帖子与回复列表的位置
	 */
	public final static int POST_POSITION = -100;
	private File mCameraFile;

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

		// initMoreList(mmUserInfo);
		// 加载帖子详情
		BusinessUtils.getGroupPostDetail(mActivity, subjectInfo.get("id"), fReplayId, new PostDetailCallbackData(
				GroupPostDetailActivity.this));
		// 如果不需要跳楼,直接加载回复列表
		if (!isJump) {
			// 初始化请求数据
			reRequestData(false);
		}

		mConfigPostInsertGroup = Utils.getBooleanFlag(Utils.getCfg(mActivity, Constants.COMMON_SF_NAME,
				Constants.SF_INSERT_GROUP_CONFIG_VERSION, Constants.COMMON_TRUE));
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
		dismissProcessDialog();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			// 充值
			case R.id.rechargeBtn:
				break;
			// 更多
			case R.id.top_right_text_bg:
				addFan();
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

			// 点击头像进入个人中心
			case R.id.item_photo:
				clickPhotoHead(POST_POSITION);
				break;
			case R.id.item_moudle_text:
				Map<String, String> fanInfo = new HashMap<String, String>();
				fanInfo.put("id", subjectInfo.get("groupId"));
				ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class, false, FanDetailActivity.FAN_INFO,
						(Serializable) fanInfo);
				break;
			case R.id.item_support:
				if (Utils.getBooleanFlag(v.getTag().toString())) {
					UiHelper.showToast(mActivity, "您已经赞了,不能再赞了");
					return;
				}
				if (!AppConfig.getInstance().isLogged) {
					Utils.requestLoginOrRegister(mActivity, "点赞需要先登录", Constants.REQUEST_CODE_LOGIN);
					return;
				}
				// 先UI更新
				v.setSelected(true);
				v.setTag(Constants.COMMON_TRUE);
				BusinessUtils.groupSupport(mActivity, null, Integer.parseInt(subjectInfo.get("id")));
				int supportNum = Integer.parseInt(subjectInfo.get("supportNum"));
				subjectInfo.put("supported", Constants.COMMON_TRUE);
				subjectInfo.put("supportNum", String.valueOf(supportNum + 1));
				moTvSupport.setText(String.valueOf(supportNum + 1));
				break;
			case R.id.item_replay_num:
				if (!AppConfig.getInstance().isLogged) {
					Utils.requestLoginOrRegister(mActivity, "点赞需要先登录", Constants.REQUEST_CODE_LOGIN);
					return;
				}
				initRelayPost();
				inputEditText.setFocusable(true);
				inputEditText.setFocusableInTouchMode(true);
				inputEditText.requestFocus();
				InputMethodManager inputManager = (InputMethodManager) mActivity
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(inputEditText, 0);
				break;
			case R.id.item_share:
				MobclickAgent.onEvent(FeizaoApp.mConctext, "sharePost");
				if (!AppConfig.getInstance().isLogged) {
					Utils.requestLoginOrRegister(mActivity, "分享需要先登录", Constants.REQUEST_CODE_LOGIN);
					return;
				}
				shareGroupPostInfo(subjectInfo, mActivity);
				break;
			case R.id.item_more:
				showPostMoreDialog();
				break;
			case R.id.playing_iv_add:
				ActionSheetDialog actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
						.setCanceledOnTouchOutside(true)
						.addSheetItem(getString(R.string.system_camera), ActionSheetDialog.SheetItemColor.BLACK, new ActionSheetDialog.OnSheetItemClickListener() {
							@Override
							public void onClick(int which) {
								mCameraFile = PhotoSelectImpl.takePhoto(mActivity);
							}
						}).addSheetItem(getString(R.string.system_gallery_select), ActionSheetDialog.SheetItemColor.BLACK, new ActionSheetDialog.OnSheetItemClickListener() {
							@Override
							public void onClick(int which) {
								PhotoSelectImpl.selectPhoto(mActivity);
							}
						});
				actionSheetDialog.show();
				break;
			case R.id.item_picture_close:
				clearReplyPictureInfo();
				break;
			case R.id.item_replay_bg:
				Intent intent = new Intent(mActivity, ImageBrowserActivity.class);
				ArrayList<String> arrayUri = new ArrayList<String>();
				arrayUri.add(mReplyPhotoData.uri.toString());
				intent.putExtra(ImageBrowserActivity.IMAGE_URL, arrayUri);
				// intent.putExtra(ImageBrowserActivity.IS_NEED_EIDT, true);
				startActivityForResult(intent, RESULT_REQUEST_IMAGE);
				break;
		}

	}

	/**
	 * 清空图片信息
	 */
	private void clearReplyPictureInfo() {
		mReplyPictureLayout.setVisibility(View.GONE);
		mReplyPhotoData = null;
		mReplyPhotoFilePath = null;
	}

	private void addFan() {
		if (!AppConfig.getInstance().isLogged) {
			Utils.requestLoginOrRegister(mActivity, mActivity.getResources().getString(R.string.tip_login_title),
					Constants.REQUEST_CODE_LOGIN);
			return;
		}
		BusinessUtils.addFan(mActivity, subjectInfo.get("groupId"), new AddFanCallbackData());
	}

	/**
	 * 弹出对话框
	 */
	private void showPostMoreDialog() {
		if (onIsOwen(subjectInfo.get("uid"))) {
			actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
					.setCanceledOnTouchOutside(true)
					.addSheetItem("删除", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							// 按返回键弹出对话框
							UiHelper.showConfirmDialog(mActivity, R.string.commutity_comfirm_delete_post,
									R.string.cancel, R.string.determine, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {

										}
									}, new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											BusinessUtils.deleteGroupPost(mActivity, subjectInfo.get("id"),
													new DeletePostCallbackData(GroupPostDetailActivity.this));
										}
									});

						}
					});
		} else {
			actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
					.setCanceledOnTouchOutside(true)
					.addSheetItem("举报", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							ActivityJumpUtil.toReportActivity(mActivity, Constants.COMMON_REPORT_TYPE_POST, subjectInfo
									.get("id").toString(), 0);
						}
					});
		}
		/** 如果已经收藏 */
		if (Boolean.parseBoolean(subjectInfo.get("bookmark").toString())) {
			actionSheetDialog.addSheetItem(getResources().getString(R.string.commutity_collect_cancel),
					SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							BusinessUtils.removeGroupCollectPostInfo(mActivity, new CollectCallbackData(
									GroupPostDetailActivity.this, false), subjectInfo.get("id"));
						}
					});
		} else {
			actionSheetDialog.addSheetItem(getResources().getString(R.string.commutity_collect), SheetItemColor.BLACK,
					new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							BusinessUtils.groupCollectPostInfo(mActivity, new CollectCallbackData(
									GroupPostDetailActivity.this, true), subjectInfo.get("id"));
						}
					});
		}

		actionSheetDialog.show();

	}

	/**
	 * 点击“个人头像”
	 */
	private void clickPhotoHead(final int position) {
		final Map<String, ?> mData;
		if (position == POST_POSITION) {
			mData = subjectInfo;
		} else {
			mData = mRelayAdapter.getData().get(position);
		}
		final boolean isOwen = onIsOwen((String) mData.get("uid"));
		if (isOwen) {
			Map<String, String> personInfo = new HashMap<String, String>();
			personInfo.put("id", (String) mData.get("uid"));
			ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
			return;
		}
		// 如果有管理员权限
		if (onIsGroupAdmin(subjectInfo.get("isGroupAdmin"))) {
			actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
					.setCanceledOnTouchOutside(true)
					.addSheetItem("查看ta的主页", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							// 按返回键弹出对话框
							Map<String, String> personInfo = new HashMap<String, String>();
							personInfo.put("id", (String) mData.get("uid"));
							ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
						}
					});
			// 如果未禁言
			if (!Utils.getBooleanFlag(mData.get("isUserBanned"))) {
				actionSheetDialog.addSheetItem("禁言", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						showGroupBanDialog(position, (String) mData.get("uid"));
					}
				});
			} else {
				actionSheetDialog.addSheetItem("解禁", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						BusinessUtils.onGroupBanorUnBan(mActivity, subjectInfo.get("groupId"),
								(String) mData.get("uid"), null, false, new BanCallbackData(false, position));
					}
				});
			}
			actionSheetDialog.show();
		} else {
			Map<String, String> personInfo = new HashMap<String, String>();
			personInfo.put("id", (String) mData.get("uid"));
			ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
		}

	}

	/**
	 * 点击禁言按钮
	 */
	private void showGroupBanDialog(final int position, final String uid) {
		ActionSheetDialog actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
				.setCanceledOnTouchOutside(true)
				.addSheetItem("禁言1小时", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						BusinessUtils.onGroupBanorUnBan(mActivity, subjectInfo.get("groupId"), uid,
								String.valueOf(3600), true, new BanCallbackData(true, position));
					}
				}).addSheetItem("禁言1天", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						BusinessUtils.onGroupBanorUnBan(mActivity, subjectInfo.get("groupId"), uid,
								String.valueOf(3600 * 24), true, new BanCallbackData(true, position));
					}
				}).addSheetItem("禁言3天", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						BusinessUtils.onGroupBanorUnBan(mActivity, subjectInfo.get("groupId"), uid,
								String.valueOf(3600 * 72), true, new BanCallbackData(true, position));
					}
				}).addSheetItem("永久禁言", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						BusinessUtils.onGroupBanorUnBan(mActivity, subjectInfo.get("groupId"), uid, String.valueOf(0),
								true, new BanCallbackData(true, position));
					}
				});
		actionSheetDialog.show();
	}

	/**
	 * @param mmUserInfo 用户数据
	 */
	private void initMoreList(Map<String, ?> mmUserInfo) {
		moreLists.clear();
		Map<String, Integer> item1 = new HashMap<String, Integer>();
		item1 = new HashMap<String, Integer>();
		item1.put("imageId", R.drawable.icon_share_sm);
		item1.put("textId", R.string.commutity_share_item);
		moreLists.add(item1);
		item1 = new HashMap<String, Integer>();
		if (mmUserInfo != null && mmUserInfo.get("id").equals(subjectInfo.get("uid"))) {
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
			Rect rect = new Rect();
			rootLayout.getWindowVisibleDisplayFrame(rect);
			int disHeight = rect.bottom - rect.top;
			// 比较Activity根布局与当前布局的大小
			int heightDiff = mActivity.getWindow().getDecorView().getRootView().getHeight() - disHeight;

			EvtLog.d(TAG, "getDecorView RootView.getRootView height:" + heightDiff);
			// 如果高度差超过100像素，就很有可能是有软键盘...
			if (heightDiff > 100) {
				// 是否已经弹出虚拟键盘
				if (!isKeyboardUp) {
					isKeyboardUp = true;
					Rect inputRect = new Rect();
					inputLayout.getGlobalVisibleRect(inputRect);
					inputViewTop = inputRect.top;
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
				mFaceHelper = new SelectFaceHelper(GroupPostDetailActivity.this, moGvEmotions);
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
				} else {
					inputEditText.getText().delete(selection - 1, selection);
				}
			}

		}

	};

	/**
	 * 更新选图数据
	 */
	private void updatePickData(String imagePath) {
		mReplyPictureLayout.setVisibility(View.VISIBLE);
		if (!imagePath.startsWith(Constants.FILE_PXI)) {
			imagePath = Constants.FILE_PXI + imagePath;
		}
		try {
			Uri uri = Uri.parse(imagePath);
			File outputFile = photoOperate.scal(uri);
			mReplyPhotoData = new PhotoData(outputFile, new ImageInfo(imagePath));
			ImageLoaderUtil.with().loadImageTransformRoundedCorners(mActivity, mReplyPicture, uri.toString(), Constants.COMMON_DISPLAY_IMAGE_CORNER_2);
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	private String compressImageFils(PhotoData mList) {
		if (mList == null)
			return null;
		return BitmapUtility.getFilePathFromUri(mActivity, mList.uri);
	}

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
		if (!AppConfig.getInstance().isLogged)
			showLoginTip();
		else {
			if (isNoNetworkTipMsg())
				return;
			// 这里不要直接用mEditMessageEt.getText().toString();
			CharSequence msgStr = HtmlTagHandler.convertToMsg(inputEditText.getText(), GroupPostDetailActivity.this,
					replyPost);
			// 回复“帖子”
			if (replyPost == true) {
				mReplyPhotoFilePath = compressImageFils(mReplyPhotoData);
				if (TextUtils.isEmpty(content) && TextUtils.isEmpty(mReplyPhotoFilePath)) {
					return;
				}
				if (mReplyPhotoData != null && mReplyPhotoData.mImageLength > PhotoSelectImpl.IMAGE_SIZE_MAX_LIMIT) {
					UiHelper.showToast(mActivity, R.string.commutity_select_image_big);
					return;
				}
				// 3 发表
				mProgress = Utils.showProgress(mActivity);
				BusinessUtils.groupRelayPostInfo(mActivity, new RelayCallbackData(this, Constants.REPLY_POST_TYPE_POST,
						msgStr.toString()), subjectInfo.get("id"), msgStr.toString(), mReplyPhotoFilePath);
			} else {
				if (TextUtils.isEmpty(content)) {
					return;
				}
				// 3 发表
				mProgress = Utils.showProgress(mActivity);
				BusinessUtils.groupRelayLoucengInfo(mActivity, new RelayCallbackData(this,
						Constants.REPLY_POST_TYPE_REPLY, msgStr.toString()), toFReplyId, toReplyId, toUid, msgStr
						.toString());
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
		moIvClearInput.setVisibility(View.GONE);
		inputEditText.setHint(R.string.me_speak);
		moIvPicture.setVisibility(View.VISIBLE);
		if (mReplyPhotoData != null)
			mReplyPictureLayout.setVisibility(View.VISIBLE);
	}

	/**
	 * 显示登录提示对话框
	 */
	private void showLoginTip() {
		Utils.requestLoginOrRegister(GroupPostDetailActivity.this, "需要登录，请登录", Constants.REQUEST_CODE_LOGIN);
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
				BusinessUtils.getGroupPostDetail(mActivity, subjectInfo.get("id"), fReplayId,
						new PostDetailCallbackData(GroupPostDetailActivity.this));
			} else {
			}
			// redirectPassiveEventActivity(getIntent(), false);
		} else if (requestCode == RESULT_REQUEST_IMAGE) {
			// if (resultCode == RESULT_OK) {
			// ArrayList<String> delUris =
			// data.getStringArrayListExtra("mDelUrls");
			// for (String item : delUris) {
			// // if (mReplyPhotoData.path.equals(item)) {
			// // 删除完了，后的处理，续集
			// // }
			// }
			// }
		} else if (requestCode == GroupPostPublishActivity.REQUEST_CODE_SELECT_FAN) {
			if (resultCode == RESULT_OK) {
				Map<String, String> groupData = (Map<String, String>) data
						.getSerializableExtra(FanDetailActivity.FAN_INFO);
				// 获取光标当前位置
				int curIndex = inputEditText.getSelectionStart();
				// 把要#的人插入光标所在位置
				// inputEditText.getText().insert(curIndex,
				// groupData.get("name"));
				// 通过输入#符号进入好友列表并返回#的人，要删除之前输入的#
				if (curIndex >= 1) {
					inputEditText.getText().replace(curIndex - 1, curIndex, "");
				}
				setAtImageSpan(curIndex, groupData.get("id"), groupData.get("name"));
				// inputEditText.append(spanStr);
			}
		} else if (requestCode == PhotoSelectImpl.REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
			if (mCameraFile != null) {
				updatePickData(mCameraFile.getPath());
				mCameraFile = null;
			}
		} else if (requestCode == PhotoSelectImpl.REQUEST_ALBUM) {
			// 相册选择
			if (data != null) {
				Uri uri = data.getData();
				String path = BitmapUtility.getFilePathFromUri(mActivity, uri);
				updatePickData(path);
			}
		}
	}

	private void setAtImageSpan(int selectionIndex, String groupId, String groupName) {
		final Bitmap bmp = BitmapUtils.getNameBitmap(groupName,
				(int) mActivity.getResources().getDimension(R.dimen.a_text_size_40));
		String spString = "[" + groupId + "," + groupName + "]";
		SpannableString ss = new SpannableString(spString);
		BitmapDrawable drawable = new BitmapDrawable(getResources(), bmp);
		drawable.setBounds(0, 0, bmp.getWidth(), bmp.getHeight());
		ImageSpan imageSpan = new ImageSpan(drawable, spString, DynamicDrawableSpan.ALIGN_BASELINE);

		// 把取到的要@的人名，用DynamicDrawableSpan代替
		ss.setSpan(imageSpan, 0, ss.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

		// inputEditText.setTextKeepState(ss);
		inputEditText.getText().insert(inputEditText.getSelectionStart(), ss);
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MsgTypes.MSG_POST_DETAIL_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
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
					} else {
						mReplyTip.setVisibility(View.GONE);
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
					subjectInfo.put("bookmark", Constants.COMMON_TRUE);
					UiHelper.showToast(mActivity, "收藏成功");
				} else {
					subjectInfo.put("bookmark", "false");
					UiHelper.showToast(mActivity, "取消收藏成功");
				}
				break;
			case MsgTypes.MSG_COLLECT_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;

			case MsgTypes.MSG_REPLY_SUCCESS:
				dismissProcessDialog();
				Map<String, String> map = (Map<String, String>) msg.obj;
				Bundle bundle = msg.getData();
				int type = bundle.getInt("type");
				String content = bundle.getString("content");
				String msgContent = bundle.getString("msg");
				// 如果回复成功，把数据添加到listview
				if (Constants.REPLY_POST_TYPE_POST == type) {
					// 添加楼层数据
					Map<String, Object> data = new HashMap<>();
					data.put("uid", UserInfoConfig.getInstance().id);
					data.put("id", map.get("id"));
					data.put("nickname", UserInfoConfig.getInstance().nickname);
					data.put("headPic", UserInfoConfig.getInstance().headPic);
					data.put("level", UserInfoConfig.getInstance().level + "");

					if (mReplyPhotoData == null) {
						// data.put("pics", "[]");
					} else {
						// data.put("pics", "['" + mReplyPhotoData.uri.toString() +
						// "']");
						content = Utils.getImageHtml(mReplyPhotoData.uri.toString()) + " " + content;
					}
					data.put("content", content);
					data.put("addTime", String.valueOf(new Date().getTime() / 1000));
					data.put("lzlReplys", new ArrayList<Map<String, String>>());

					clearReplyPictureInfo();

					// 如果没有一页数据，把数据加入到列表中
					if (mRelayAdapter.getData().size() < Constants.REQUEST_POST_LIMIT
							|| mListFooterLoadView.getStatus() == ListFooterLoadView.STATUS_NOMORE) {
						mRelayAdapter.addData(data);
					}

				} else {
					// 添加回复数据
					Map<String, String> data = new HashMap<>();
					data.put("uid", UserInfoConfig.getInstance().id);
					data.put("id", map.get("id"));
					data.put("nickname", UserInfoConfig.getInstance().nickname);
					data.put("headPic", UserInfoConfig.getInstance().headPic);
					data.put("level", UserInfoConfig.getInstance().level + "");

					data.put("lzlReplyId", toReplyId);
					data.put("fReplyId", toFReplyId);
					data.put("toNickname", toNickname);
					data.put("toLevel", toUserLevel);
					data.put("content", content);

					data.put("addTime", String.valueOf(new Date().getTime() / 1000));

					Map<String, Object> louceng = (Map<String, Object>) mRelayAdapter.getItem(toPosition);
					List<Map<String, String>> lmPlayer = (List<Map<String, String>>) louceng.get("lzlReplys");
					lmPlayer.add(data);
					// 刷新列表
					mRelayAdapter.notifyDataSetChanged();
				}
				// 回复成功，就消失
				mReplyTip.setVisibility(View.GONE);
				UiHelper.showToast(mActivity, msgContent);
				break;
			case MsgTypes.MSG_REPLY_FAILED:
				dismissProcessDialog();
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
			case MsgTypes.MSG_ADD_FAN_SUCCESS:
				subjectInfo.put("groupJoined", Constants.COMMON_TRUE);
				mTopRightText.setText(R.string.commutity_group_added);
				mTopRightTextLayout.setEnabled(false);
				UiHelper.showToast(mActivity, mActivity.getString(R.string.commutity_fan_add_succuss));
				break;
			case MsgTypes.MSG_ADD_FAN_FAILED:
				String errorMsg = (String) msg.obj;
				UiHelper.showToast(mActivity, errorMsg);
				break;
			// 删除帖子
			case MsgTypes.MSG_DELETE_POST_SUCCESS:
				UiHelper.showToast(mActivity, "删除成功");
				setResult(RESULT_OK);
				onBackPressed();
				break;
			case MsgTypes.MSG_DELETE_POST_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;

			case MsgTypes.MSG_GROUP_BAN_SUCCESS:
				Bundle banBundle = msg.getData();
				boolean banFlag = banBundle.getBoolean("flag");
				int banPosition = banBundle.getInt("position");
				if (banFlag) {
					if (banPosition == POST_POSITION) {
						subjectInfo.put("isUserBanned", Constants.COMMON_TRUE);
					} else {
						mRelayAdapter.getData().get(banPosition).put("isUserBanned", Constants.COMMON_TRUE);
					}
					UiHelper.showToast(mActivity, "已成功禁言");
				} else {
					if (banPosition == POST_POSITION) {
						subjectInfo.put("isUserBanned", "false");
					} else {
						mRelayAdapter.getData().get(banPosition).put("isUserBanned", "false");
					}
					UiHelper.showToast(mActivity, "已成功解禁");
				}
				break;
			case MsgTypes.MSG_GROUP_BAN_FAILED:
				UiHelper.showToast(mActivity, (String) msg.obj);
				break;
		}

	}

	/**
	 * 关闭加载对话框
	 */
	private void dismissProcessDialog() {
		if (mProgress != null && mProgress.isShowing())
			mProgress.dismiss();
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
		inputEditText.setFilters(new InputFilter[]{new MyInputFilter()});
		// inputEditText.addTextChangedListener(new SimpleTextWatcher() {
		// @Override
		// public void beforeTextChanged(CharSequence s, int start, int count,
		// int after) {
		//
		// }
		//
		// @Override
		// public void afterTextChanged(Editable s) {
		// if (s.toString().endsWith(Constants.COMMON_INSERT_POST_PIX)) {
		// // 如果服务器下发了插入饭圈标记，且是回复帖子
		// if (mConfigPostInsertGroup && moIvPicture.getVisibility() ==
		// View.VISIBLE) {
		// ActivityJumpUtil.gotoActivityForResult(mActivity,
		// MeFanSelectActivity.class,
		// GroupPostPublishActivity.REQUEST_CODE_SELECT_FAN, null, null);
		// }
		// int length = s.length();
		// s.delete(length - 1, length);
		// return;
		// }
		//
		// int selection = inputEditText.getSelectionStart();
		// String text = inputEditText.getText().toString();
		// if (selection > 0) {
		// String text2 = text.substring(selection - 1);
		// if ("]".equals(text2)) {
		// int start = text.lastIndexOf("[");
		// if (start == -1)
		// return;
		// int end = selection;
		// s.delete(start, end);
		// }
		// }
		//
		// }
		// });
		moGvEmotions = (LinearLayout) findViewById(R.id.playing_gv_eomotions);
		moIvEmotion = (ImageView) findViewById(R.id.playing_iv_emotion);
		moIvPicture = (ImageView) findViewById(R.id.playing_iv_add);
		moIvClearInput = (ImageView) findViewById(R.id.playing_iv_clear_msg_content);
		moIvClearInput.setOnClickListener(new OnClearInputText());

		inputLayout = (LinearLayout) findViewById(R.id.playing_ll_edt_input);
		inputEditText.setHint(R.string.me_speak);
		sendBtn = (Button) findViewById(R.id.playing_btn_send_msg);
		sendBtn.setOnClickListener(this);

		moIvEmotion.setOnClickListener(new OnShowHideEmotions());
		moIvPicture.setOnClickListener(this);

		mReplyPictureLayout = (RelativeLayout) findViewById(R.id.item_picture_layout);
		mReplyPicture = (ImageView) findViewById(R.id.item_replay_pic);
		mReplyPictureBg = (ImageView) findViewById(R.id.item_replay_bg);
		mReplyPictureBg.setOnClickListener(this);
		mReplyPictureClose = (ImageView) findViewById(R.id.item_picture_close);
		mReplyPictureClose.setOnClickListener(this);
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.commutity_post);
		mTopRightText.setText(R.string.commutity_group_add);
		mTopRightTextLayout.setVisibility(View.VISIBLE);
		mTopBackLayout.setOnClickListener(this);
	}

	/**
	 * 初始化帖子信息
	 */
	private void initPostInfo(View v) {
		moIvPhoto = (ImageView) v.findViewById(R.id.item_photo);
		moIvPhotoV = (ImageView) v.findViewById(R.id.item_photo_v);
		mPictureLayout = (LinearLayout) v.findViewById(R.id.item_picture_grid);
		moTvNickname = (TextView) v.findViewById(R.id.item_nickname);
		mUserLevel = (ImageView) v.findViewById(R.id.item_userlevel);
		mForumTitle = (TextView) v.findViewById(R.id.item_moudle_text);

		moTvTimer = (TextView) v.findViewById(R.id.item_time);
		moTvContent = (TextView) v.findViewById(R.id.item_content);
		moSupport = (ImageView) v.findViewById(R.id.item_support);
		moTvSupport = (TextView) v.findViewById(R.id.item_support_num);

		moTvReply = (TextView) v.findViewById(R.id.item_replay_num);
		mShare = (ImageView) v.findViewById(R.id.item_share);
		mMore = (ImageView) v.findViewById(R.id.item_more);
		// 没有评论数据提示
		mReplyTip = (TextView) v.findViewById(R.id.item_no_data);
		// mPictureLayout = (LinearLayout)
		// v.findViewById(R.id.item_picture_grid);
		mClickFlushTv = (TextView) v.findViewById(R.id.item_flush);
		mClickFlushTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 请求主播数据
				reRequestData(false);
				// 加载帖子详情
				BusinessUtils.getGroupPostDetail(mActivity, subjectInfo.get("id"), fReplayId,
						new PostDetailCallbackData(GroupPostDetailActivity.this));

			}
		});
		mHtmlTagHandler.setOnIClickUsernName(new LiveChatFragment.IClickUserName() {
			@Override
			public void onClick(String username, String uid) {
				Map<String, String> fanInfo = new HashMap<String, String>();
				fanInfo.put("id", uid);
				ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class, false, FanDetailActivity.FAN_INFO,
						(Serializable) fanInfo);
			}
		});
		if (subjectInfo.get("nickname") == null)
			return;
		initPostData();
	}

	private void initPostData() {
		ImageLoaderUtil.with().loadImageTransformRoundCircle(mActivity, moIvPhoto, subjectInfo.get("headPic"));
		moIvPhotoV.setVisibility(Utils.getBooleanFlag(subjectInfo.get("verified"))
				? View.VISIBLE
				: View.GONE);
		moTvNickname.setText(subjectInfo.get("nickname"));
		if (!TextUtils.isEmpty(subjectInfo.get("addTime"))) {
			moTvTimer.setText(DateUtil.fmtTimemillsToTextFormat(Long.parseLong(subjectInfo.get("addTime"))));
		}
		ImageLoaderUtil.with().loadImage(mActivity, mUserLevel, Utils.getLevelImageResourceUri(subjectInfo, true));
		mForumTitle.setText(subjectInfo.get("groupName"));

		moTvContent.setText(HtmlUtil.htmlTextDeal(mActivity, subjectInfo.get("content"), new NetworkImageGetter(
						moTvContent, (int) (FeizaoApp.metrics.widthPixels - (12 * 4) * FeizaoApp.metrics.density)),
				mHtmlTagHandler));
		moTvContent.setText(HtmlUtil.htmlTextUrlClick(mActivity, moTvContent.getText()));
		moTvContent.setMovementMethod(LinkMovementMethod.getInstance());

		if (Boolean.parseBoolean(subjectInfo.get("supported").toString())) {
			moSupport.setSelected(true);
		} else {
			moSupport.setSelected(false);
		}
		moSupport.setTag(subjectInfo.get("supported").toString());
		moIvPhoto.setOnClickListener(this);
		mForumTitle.setOnClickListener(this);
		moSupport.setOnClickListener(this);
		mShare.setOnClickListener(this);
		mMore.setOnClickListener(this);
		moTvReply.setOnClickListener(this);
		moTvSupport.setText(subjectInfo.get("supportNum"));
		moTvReply.setText(subjectInfo.get("replyNum"));
		// 如果已加入该帖子饭圈
		if (Boolean.parseBoolean(subjectInfo.get("groupJoined"))) {
			mTopRightText.setText(R.string.commutity_group_added);
			mTopRightTextLayout.setEnabled(false);
		} else {
			// 如果未加入，且已加载了帖子数据，按钮可点
			mTopRightTextLayout.setEnabled(true);
			mTopRightTextLayout.setOnClickListener(this);
		}
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
	 * 识别输入框的是不是#符号
	 */
	private class MyInputFilter implements InputFilter {

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			// TODO Auto-generated method stub
			if (source.toString().equalsIgnoreCase(Constants.COMMON_INSERT_POST_PIX)) {
				if (mConfigPostInsertGroup && moIvPicture.getVisibility() == View.VISIBLE) {
					ActivityJumpUtil.gotoActivityForResult(mActivity, MeFanSelectActivity.class,
							GroupPostPublishActivity.REQUEST_CODE_SELECT_FAN, null, null);
				}
			}
			return source;
		}
	}

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
		HtmlTagHandler mHtmlTagHandler = new HtmlTagHandler();
		mHtmlTagHandler.setOnIClickUsernName(new LiveChatFragment.IClickUserName() {
			@Override
			public void onClick(String username, String uid) {
				Map<String, String> fanInfo = new HashMap<String, String>();
				fanInfo.put("id", uid);
				ActivityJumpUtil.gotoActivity(mActivity, FanDetailActivity.class, false, FanDetailActivity.FAN_INFO,
						(Serializable) fanInfo);
			}
		});
		mRelayAdapter.setTagHandler(mHtmlTagHandler);
		mListView.setAdapter(mRelayAdapter);
		// 下拉刷新数据
		mListView.setTask(new Runnable() {
			@Override
			public void run() {
				// 请求主播数据
				reRequestData(false);
				// 加载帖子详情
				BusinessUtils.getGroupPostDetail(mActivity, subjectInfo.get("id"), fReplayId,
						new PostDetailCallbackData(GroupPostDetailActivity.this));
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
				// if (firstVisibleItem == 0) {
				// scrollTopIv.setVisibility(View.GONE);
				// } else {
				// scrollTopIv.setVisibility(View.VISIBLE);
				// }
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
				BusinessUtils.getGroupPostDetail(mActivity, subjectInfo.get("id"), fReplayId,
						new PostDetailCallbackData(GroupPostDetailActivity.this));
				// 重新加载数据
				reRequestData(true);

			}

			@Override
			public void onClick(View v) {
				// 加载帖子详情
				BusinessUtils.getGroupPostDetail(mActivity, subjectInfo.get("id"), fReplayId,
						new PostDetailCallbackData(GroupPostDetailActivity.this));
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
		BusinessUtils.getGroupRelayListInfo(mActivity, page, Constants.REQUEST_POST_LIMIT,
				new GetRelayListCallbackData(this), Integer.parseInt(subjectInfo.get("id")));
	}

	/**
	 * 点击楼层按钮，及TextView回复
	 */
	private class RelayItemOnclick implements IOnclickListener {

		@SuppressLint("NewApi")
		@Override
		public void onClick(View v, int position, Map<String, String> relayInfo) {

			switch (v.getId()) {
				case R.id.item_photo:
					clickPhotoHead(position);
					break;
				case R.id.item_replay:
					MobclickAgent.onEvent(FeizaoApp.mConctext, "commentPost");
					Map<String, Object> replayInfo = mRelayAdapter.getData().get(position);
					showSoftInput((String) replayInfo.get("nickname"), (String) replayInfo.get("id"), "0",
							(String) replayInfo.get("uid"), null, position);
					mListView.setSelection(position + mListView.getHeaderViewsCount());
					break;
				case R.id.item_more:
					showRelayDialog(position);
					break;
				default:
					// 点击楼层回复文本
					if (relayInfo != null) {
						Rect rect = new Rect();
						v.getGlobalVisibleRect(rect);
						clickViewButtom = rect.bottom;
						EvtLog.e(TAG, "clickViewY: position" + clickViewButtom + "," + position);
						showSoftInput(relayInfo.get("nickname"), (String) mRelayAdapter.getData().get(position).get("id"),
								relayInfo.get("id"), relayInfo.get("uid"), relayInfo.get("level"), position);

						mHandler.postDelayed(new Runnable() {

							@Override
							public void run() {
								// 滚动列表
								EvtLog.e(TAG, "clickViewButtom: inputViewTop" + clickViewButtom + "," + inputViewTop);
								mListView.smoothScrollBy(clickViewButtom - inputViewTop, 100);
							}
						}, 500);
					}
					break;
			}

		}
	}

	/**
	 * 弹出对话框
	 */
	private void showRelayDialog(final int position) {
		EvtLog.e(TAG, "showRelayDialog position" + position);
		final Map<String, Object> replayInfo = mRelayAdapter.getData().get(position);
		if (onIsGroupAdmin(subjectInfo.get("isGroupAdmin")) || onIsOwen((String) replayInfo.get("uid"))) {
			actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
					.setCanceledOnTouchOutside(true)
					.addSheetItem("删除", SheetItemColor.BLACK, new OnSheetItemClickListener() {
						@Override
						public void onClick(int which) {
							// 按返回键弹出对话框
							UiHelper.showConfirmDialog(mActivity, R.string.commutity_comfirm_delete_post,
									R.string.cancel, R.string.determine, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {

										}
									}, new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											BusinessUtils.deleteGroupPostReply(mActivity,
													(String) replayInfo.get("id"), new DeleteRelayCallbackData(
															GroupPostDetailActivity.this, Constants.DELETE_REPLY,
															position));
										}
									});

						}
					});
		} else {
			actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
					.setCanceledOnTouchOutside(true)
					.addSheetItem("举报", SheetItemColor.BLACK, new OnSheetItemClickListener() {
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
	 * 清除输入框数据
	 */
	private class OnClearInputText implements OnClickListener {

		@Override
		public void onClick(View v) {
			/** 清空后，回复帖子 */
			replyPost = true;
			moIvClearInput.setVisibility(View.GONE);
			inputEditText.setHint(R.string.me_speak);
			moIvPicture.setVisibility(View.VISIBLE);
			if (mReplyPhotoData != null)
				mReplyPictureLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 显示输入框
	 */
	private void showSoftInput(final String nickname, String fReplyId, String lzlReplyId, String toUid,
							   String toUserLevel, int position) {
		moIvClearInput.setVisibility(View.VISIBLE);
		moIvPicture.setVisibility(View.GONE);
		mReplyPictureLayout.setVisibility(View.GONE);
		// 回复“帖子回复”
		replyPost = false;
		this.toFReplyId = lzlReplyId;
		this.toReplyId = fReplyId;
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

	public static void shareGroupPostInfo(Map<String, ?> groupPostInfo, Activity mActivity) {
		Map<String, String> shareInfo = new HashMap<String, String>();
		String shareUrImg = Consts.SHARE_URI_IMG;
		try {
			JSONArray jsonarray = new JSONArray((String) groupPostInfo.get("pics"));
			if (jsonarray.length() > 0) {
				shareUrImg = (String) jsonarray.get(0);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 替换图片
		String shareContent = ((String) groupPostInfo.get("content")).replaceAll(Constants.REGX_PHOTO, "");
		// 替换表情图片
		shareContent = shareContent.replaceAll(Constants.REGX_PHOTO_EMOJI, Constants.REPLACE_PHOTO_EMOJI);
		String shareUrl = WebConstants.getFullWebMDomain(WebConstants.SHARE_GROUP_POST_PIX) + groupPostInfo.get("id");
		String shareTitle = mActivity.getResources().getString(R.string.commutity_share_post_title,
				groupPostInfo.get("nickname"));
		if (TextUtils.isEmpty(shareContent)) {
			shareContent = mActivity.getResources().getString(R.string.commutity_post_share_content);
		}
		shareInfo.put(ShareDialogActivity.Share_Content, shareContent);
		shareInfo.put(ShareDialogActivity.Share_Img, shareUrImg);
		shareInfo.put(ShareDialogActivity.Share_Url, shareUrl);
		shareInfo.put(ShareDialogActivity.Share_Title, shareTitle);
		shareInfo.put(ShareDialogActivity.Share_Dialog, String.valueOf(true));
		ActivityJumpUtil.toShareActivity(mActivity, shareInfo);
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
							JSONParser.parseMultiInMulti((JSONArray) result, new String[]{"lzlReplys"})};

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

	/**
	 * 删除帖子处理回调 ClassName: DeleteRelayCallbackData <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private static class DeletePostCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public DeletePostCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "DeletePostCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_DELETE_POST_SUCCESS;
					BaseFragmentActivity activity = mFragment.get();
					// 如果fragment未回收，发送消息
					if (activity != null)
						activity.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_DELETE_POST_FAILED;
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

	private class AddFanCallbackData implements CallbackDataHandle {

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "AddFanCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_ADD_FAN_SUCCESS;
					sendMsg(msg);
					// 发送完消息之后再保存数据，保存数据耗时
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_ADD_FAN_FAILED;
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}

	}

	/**
	 * 禁言数据回调<br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-6-18 上午11:47:50 <br/>
	 *
	 * @author Administrator
	 * @since JDK 1.6
	 */
	private class BanCallbackData implements CallbackDataHandle {

		private boolean flag;
		private int position;

		public BanCallbackData(boolean flag, int position) {
			this.flag = flag;
			this.position = position;
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "BanCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				msg.what = MsgTypes.MSG_GROUP_BAN_SUCCESS;
				Bundle bundle = new Bundle();
				bundle.putBoolean("flag", flag);
				bundle.putInt("position", position);
				msg.setData(bundle);
				sendMsg(msg);
			} else {
				msg.what = MsgTypes.MSG_GROUP_BAN_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				sendMsg(msg);
			}
		}

	}

	@Override
	public boolean onIsOwen(String uid) {
		return AppConfig.getInstance().isLogged && UserInfoConfig.getInstance().id.equals(uid);
	}

	@Override
	public boolean onIsGroupAdmin(String isGroupAdmin) {
		return Utils.getBooleanFlag(isGroupAdmin);
	}

	@Override
	public boolean onIsGroupOwen(String isGroupOwner) {
		return Utils.getBooleanFlag(isGroupOwner);
	}

}
