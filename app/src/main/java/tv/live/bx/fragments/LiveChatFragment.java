package tv.live.bx.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.adapters.ChatListAdapter;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.emoji.ParseEmojiMsgUtil;
import tv.live.bx.library.util.HtmlUtil;
import tv.live.bx.ui.ChatListView;
import tv.live.bx.ui.ChatTextViewClickableSpan;

/**
 * 聊天面板
 */
@SuppressLint("ValidFragment")
public class LiveChatFragment extends BaseFragment {
	static final int COLOR_2CE150 = Color.parseColor("#2ce150");
	static final int COLOR_FBB872 = Color.parseColor("#fbb872");
	static final int COLOR_FFE891 = Color.parseColor("#ffe891");
	static final int COLOR_B092FE = Color.parseColor("#7afafc");
	static final int COLOR_FFF100 = Color.parseColor("#fff100");
	static final int COLOR_FF5454 = Color.parseColor("#ff5454");
	static final int COLOR_FFB4F9 = Color.parseColor("#ffb4f9");
	static final int COLOR_FFFFFF = Color.parseColor("#ffffff");
	private Context mContext;
	/**
	 * 列表延时刷新 时长
	 */
	public static int LIST_DEPLY_NOTIFY = 5000;
	private ChatListView mChatListView;
	private LinearLayout mChatLinearLayout;

	private ChatListAdapter moAdapter;

	private OnListViewTouchListener onTouchListener;
	private WeakReference<IClickUserName> mIClickUserNameWeakRef;

	private Runnable mRunnable;
	//用户来了的消息类别
	public static final int MSG_TYPE_COMEIN = 1;

	// 避免FragmentActivity恢复fragment 找不到默认构造函数
	public LiveChatFragment() {
		init(null);
	}

	public LiveChatFragment(IClickUserName iclickUserName) {
		init(iclickUserName);
	}

	private void init(IClickUserName iclickUserName) {
		mContext = FeizaoApp.mContext.getApplicationContext();
		moAdapter = new ChatListAdapter(mContext);
		this.mIClickUserNameWeakRef = new WeakReference<>(iclickUserName);
	}

	public void setOnListViewTouchListener(OnListViewTouchListener onTouchListener) {
		this.onTouchListener = onTouchListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_palying_chat;
	}

	@Override
	protected void initMembers() {
		mChatLinearLayout = (LinearLayout) mRootView.findViewById(R.id.playing_chat_layout);
		mChatListView = (ChatListView) mRootView.findViewById(R.id.playing_chat_lv_chat);
		mChatListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mChatListView.setStackFromBottom(true);
		mChatListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (onTouchListener != null) {
					return onTouchListener.onTouch(v, event);
				}
				return false;
			}
		});
		mChatListView.setOnDispatchTouchListener(new ChatListView.OnDispatchTouchListener() {

			@Override
			public void onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mHandler.removeCallbacks(mRunnable);
					mChatListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					//松手分两种情况，1、如果当前显示的是最新聊天消息，则马上可以刷新列表；2、如果当前显示的是历史消息，则5s后可以刷新
					if (mChatListView.getLastVisiblePosition() >= mChatListView.getCount() - 1) {
						mChatListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
						scrollToBottom();
					} else {
						delayNotify();
					}
				}
			}
		});

		mChatLinearLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (onTouchListener != null) {
					return onTouchListener.onTouch(v, event);
				}
				return false;
			}
		});
	}

	@Override
	public void initWidgets() {
	}

	protected void initData(Bundle bundle) {
		mChatListView.setAdapter(moAdapter);
	}

	protected void setEventsListeners() {
	}

	@Override
	protected void handleMessage(Message msg) {
		if (msg.what == MsgTypes.ON_CHAT_MSG) {
			@SuppressWarnings("unchecked")
			CharSequence lmMsg = (CharSequence) msg.obj;
			Map<String, CharSequence> data = new HashMap<>();
			data.put("type", String.valueOf(msg.arg1));
			data.put("content", lmMsg);
			//如果是用户进入直播间消息，且消息面板已经有其他消息了
			if (msg.arg1 == MSG_TYPE_COMEIN && moAdapter.getCount() > 0) {
				Map<String, CharSequence> item = (Map<String, CharSequence>) moAdapter.getItem(moAdapter.getCount() - 1);
				if (MSG_TYPE_COMEIN == Integer.valueOf((String) item.get("type"))) {
					moAdapter.updateLastData(data);
					return;
				}
			}
			moAdapter.insertData(moAdapter.getCount(), data);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacks(mRunnable);
	}

	/**
	 * 延时刷新操作
	 */
	private void delayNotify() {
		if (mRunnable == null) {
			mRunnable = new Runnable() {
				@Override
				public void run() {
					mChatListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
					scrollToBottom();
				}
			};
		}
		mHandler.removeCallbacks(mRunnable);
		mHandler.postDelayed(mRunnable, LIST_DEPLY_NOTIFY);
	}

	public void clearChatMessage() {
		if (moAdapter != null) {
			moAdapter.clearData();
		}
	}

	/**
	 * 发送消息到聊天面板
	 *
	 * @param spannableString
	 */
	public void sendChatMsg(CharSequence spannableString, int arg1) {
		Message loMsg = Message.obtain();
		loMsg.what = MsgTypes.ON_CHAT_MSG;
		loMsg.obj = spannableString;
		loMsg.arg1 = arg1;
		sendMsg(loMsg,50);
	}

	/**
	 * 发送消息到聊天面板
	 *
	 * @param spannableString
	 */
	public void sendChatMsg(CharSequence spannableString) {
		this.sendChatMsg(spannableString, 0);
	}

	/**
	 * 系统消息
	 */
	public SpannableString onSysMsg(String content) {
		return showColorText(content, COLOR_2CE150);
	}

	/**
	 * html文本消息
	 */
	public SpannableString onHtmlTextMsg(String content) {
		return HtmlUtil.htmlTextDeal(mContext, content, null, null);
	}

	/**
	 * 生成用户信息 Spannable
	 * 如果是主播，则显示主播等级
	 *
	 * @param spannableStringBuilder
	 * @param userName
	 * @param userType
	 * @param userLevel
	 * @param medalsStr
	 * @param guardType              如果用户开通守护，显示守护等级
	 */
	private void userInfoToSpannable(SpannableStringBuilder spannableStringBuilder, String userId, String userName, String userType, String userLevel, String medalsStr, String moderatorLevel, String guardType) {
		// 如果主播等级不为空，说明是主播显示主播等级
		if (!TextUtils.isEmpty(moderatorLevel)) {
			spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(Utils.getLevelImageResourceUri(Constants.USER_ANCHOR_LEVEL_PIX, moderatorLevel),
					Utils.dip2px(mContext, 16)));
		} else if (!TextUtils.isEmpty(userLevel)) {
			spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, userLevel),
					Utils.dip2px(mContext, 16)));
		}
		// 用户类型
		if (!TextUtils.isEmpty(userType)) {
			spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(Utils.getFiledDrawable(Constants.USER_TYPE_PIX, userType)));
		}
		// 勋章
		if (!TextUtils.isEmpty(medalsStr)) {
			try {
				JSONArray medals = new JSONArray(medalsStr);
				for (int i = 0; i < medals.length(); i++) {
					String url = Utils.getModelUri(String.valueOf(medals.get(i)));
					if (!TextUtils.isEmpty(url)) {
						spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(url, Utils.dip2px(mContext, 16)));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (!TextUtils.isEmpty(guardType)) {
			spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(Utils.getFiledDrawable(Constants.USER_GUARD_LEVEL_PIX, guardType),
					Utils.dip2px(mContext, 14)));
		}

		//如果是官方管理员
		if (Constants.USER_TYPE_OFFICIAL.equals(userType)) {
			spannableStringBuilder.append(showClickableText(userName, userType, userId, COLOR_FFF100));
		} else {
			spannableStringBuilder.append(showClickableText(userName, userType, userId, COLOR_FBB872));
		}
	}

	/**
	 * 用户操作消息.(包括用户分享了视频、关注了主播、送花)
	 */
	public SpannableStringBuilder onUserOpearMsg(String piFrom, String psToNickname, String mType, String mLevel, String content, String medalsStr, String moderatorLevel, String guardType) {
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
		userInfoToSpannable(spannableStringBuilder, piFrom, psToNickname, mType, mLevel, medalsStr, moderatorLevel, guardType);
		spannableStringBuilder.append(showColorText(content, COLOR_FFE891));
		return spannableStringBuilder;
	}

	/**
	 * 管理员或者主播操作消息.(包括设置、取消管理、踢出房间)
	 */
	public SpannableStringBuilder onManagerOpearMsg(String piFrom, String psToNickname, String mType, String mLevel, String content) {
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
		userInfoToSpannable(spannableStringBuilder, piFrom, psToNickname, mType, mLevel, null, null, null);
		spannableStringBuilder.append(showColorText(content, COLOR_FF5454));
		return spannableStringBuilder;
	}


	/**
	 * 用户进入直播间
	 */
	public SpannableStringBuilder onUserEnter(String piFrom, String psToNickname, String mType, String mLevel, String content, String medalsStr, String moderatorLevel, String guardType) {
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
		userInfoToSpannable(spannableStringBuilder, piFrom, psToNickname, mType, mLevel, medalsStr, moderatorLevel, guardType);
		spannableStringBuilder.append(showColorText(content, COLOR_2CE150));
		return spannableStringBuilder;
//		Message loMsg = new Message();
//		loMsg.what = MsgTypes.ON_CHAT_MSG;
//		loMsg.obj = spannableStringBuilder;
//		loMsg.arg1 = MSG_TYPE_COMEIN;
//		sendMsg(loMsg);
	}

	/**
	 * 用户升级消息
	 */
	public SpannableStringBuilder onUserLevelUp(String piFrom, String psToNickname, String mType, String mLevel) {
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("系统消息：恭喜");
		userInfoToSpannable(spannableStringBuilder, piFrom, psToNickname, mType, mLevel, null, null, null);
		int level = Integer.parseInt(mLevel) + 1;
		spannableStringBuilder.append(showColorText("升到" + level + "级", COLOR_2CE150));
		return spannableStringBuilder;
	}


	/**
	 * 用户聊天消息
	 *
	 * @param exclusiveUser true,为20级以上的用户，false低级别用户
	 */
	public SpannableStringBuilder onChatMsg(String piFrom, String piTo, String fromLevel, String fromType, String psFromNickname, String fromGuardType,
											String psToNickname, String toLevel, String toType, String toGuardType, String psMsg, String piPrivate, String fromModels, String toModels, boolean exclusiveUser, String fromModeratorLevel, String toModeratorLevel) {
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
		// 发消息用户信息
		if (!TextUtils.isEmpty(psFromNickname)) {
			userInfoToSpannable(spannableStringBuilder, piFrom, psFromNickname, fromType, fromLevel, fromModels, fromModeratorLevel, fromGuardType);
		}

		// 收消息用户名称
		if (!TextUtils.isEmpty(psToNickname)) {
			spannableStringBuilder.append(showColorText("对", COLOR_FFFFFF));
			userInfoToSpannable(spannableStringBuilder, piTo, psToNickname, toType, toLevel, toModels, toModeratorLevel, toGuardType);
		}
		spannableStringBuilder.append(showColorText("说：", COLOR_FFFFFF));

		SpannableString speakText = ParseEmojiMsgUtil.getExpressionString(psMsg);
		//如果是官方管理员
		if (Constants.USER_TYPE_OFFICIAL.equals(fromType)) {
			speakText.setSpan(new ForegroundColorSpan(COLOR_FFF100), 0, speakText.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		} else {//否则是普通用户（守护的权限最高）
			if (!TextUtils.isEmpty(fromGuardType)) {
				speakText.setSpan(new ForegroundColorSpan(COLOR_FFB4F9), 0, speakText.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			} else {
				if (exclusiveUser) {
					speakText.setSpan(new ForegroundColorSpan(COLOR_B092FE), 0, speakText.length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				} else {
					speakText.setSpan(new ForegroundColorSpan(COLOR_FFFFFF), 0, speakText.length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}

		spannableStringBuilder.append(speakText);
		return spannableStringBuilder;
	}

	/**
	 * 用户送礼
	 */
	public SpannableStringBuilder onGift(String piFrom, String psFromNickname, String fromLevel, String fromModeratorLevel, String fromType, String fromGuardType, String piCount, String psImg, String models) {
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
		// 发消息用户信息
		if (!TextUtils.isEmpty(psFromNickname)) {
			userInfoToSpannable(spannableStringBuilder, piFrom, psFromNickname, fromType, fromLevel, models, fromModeratorLevel, fromGuardType);
		}

		// 如果是送礼
		if (!TextUtils.isEmpty(psImg)) {
			spannableStringBuilder.append(showColorText("送：", COLOR_FBB872));
			spannableStringBuilder.append(Utils.getImageToSpannableString(psImg));
			spannableStringBuilder.append(showColorText(piCount + "个", COLOR_FFE891));
		}
		return spannableStringBuilder;
	}

	public void scrollToBottom() {
		mChatListView.setSelection(mChatListView.getCount() - 1);
	}

	/**
	 * 设置listview触摸事件
	 *
	 * @version PlayingChatFragment
	 * @since JDK 1.6
	 */
	public interface OnListViewTouchListener {
		boolean onTouch(View v, MotionEvent event);
	}


	/**
	 * 可点击的内容
	 *
	 * @param name  点击的内容，一般是用户名称
	 * @param type
	 * @param uid
	 * @param color
	 * @return
	 */
	private SpannableString showClickableText(String name, String type, String uid, int color) {
		SpannableString loFrom = new SpannableString(name);
		ChatTextViewClickableSpan span = new ChatTextViewClickableSpan(mIClickUserNameWeakRef.get(), name, type, uid, color);
		loFrom.setSpan(span, 0, loFrom.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return loFrom;
	}

	/**
	 * 设置文本内容颜色
	 *
	 * @param content
	 * @param color
	 * @return
	 */
	private SpannableString showColorText(String content, int color) {
		SpannableString loFrom = new SpannableString(content);
		loFrom.setSpan(new ForegroundColorSpan(color), 0, loFrom.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return loFrom;
	}

	/**
	 * 聊天列表用户姓名点击接口
	 */
	public interface IClickUserName {
		void onClick(String username, String uid);
	}
}
