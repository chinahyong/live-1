package tv.live.bx.ui.window;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.efeizao.bx.R;
import tv.live.bx.adapters.ChatListAdapter;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.Utils;
import tv.live.bx.emoji.ParseEmojiMsgUtil;
import tv.live.bx.fragments.LiveChatFragment;
import tv.live.bx.ui.ChatListView;

import org.json.JSONArray;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Live on 2017/2/23.
 */

public class WindowRecordBig extends LinearLayout {
	static final int COLOR_2CE150 = Color.parseColor("#2ce150");
	static final int COLOR_FBB872 = Color.parseColor("#fbb872");
	static final int COLOR_FFE891 = Color.parseColor("#ffe891");
	static final int COLOR_B092FE = Color.parseColor("#b092fe");
	static final int COLOR_FFF100 = Color.parseColor("#fff100");
	static final int COLOR_FF5454 = Color.parseColor("#ff5454");
	static final int COLOR_FFB4F9 = Color.parseColor("#ffb4f9");
	static final int COLOR_FFFFFF = Color.parseColor("#ffffff");
	//用户来了的消息类别
	private final int MSG_TYPE_COMEIN = 1;
	private Context mContext;
	protected Handler mHandler = new MyHandler(this);

	private ChatListView mRecordChatListView;
	private ChatListAdapter adapter;
	private Runnable mRunnable;

	public WindowRecordBig(Context context) {
		super(context);
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.activity_record_window_big, this);
		mRecordChatListView = (ChatListView) findViewById(R.id.record_window_chat_chatListView);
		mRecordChatListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mRecordChatListView.setStackFromBottom(true);
		mRecordChatListView.setOnDispatchTouchListener(new ChatListView.OnDispatchTouchListener() {

			@Override
			public void onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					mHandler.removeCallbacks(mRunnable);
					mRecordChatListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					//松手分两种情况，1、如果当前显示的是最新聊天消息，则马上可以刷新列表；2、如果当前显示的是历史消息，则5s后可以刷新
					if (mRecordChatListView.getLastVisiblePosition() >= mRecordChatListView.getCount() - 1) {
						mRecordChatListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
						scrollToBottom();
					} else {
						delayNotify();
					}
				}
			}
		});
		adapter = new ChatListAdapter(context);
		mRecordChatListView.setAdapter(adapter);
	}

	protected void handleMessage(Message msg) {
		if (msg.what == MsgTypes.ON_CHAT_MSG) {
			@SuppressWarnings("unchecked")
			CharSequence lmMsg = (CharSequence) msg.obj;
			Map<String, CharSequence> data = new HashMap<>();
			data.put("type", String.valueOf(msg.arg1));
			data.put("content", lmMsg);
			//如果是用户进入直播间消息，且消息面板已经有其他消息了
			if (msg.arg1 == MSG_TYPE_COMEIN && adapter.getCount() > 0) {
				Map<String, CharSequence> item = (Map<String, CharSequence>) adapter.getItem(adapter.getCount() - 1);
				if (MSG_TYPE_COMEIN == Integer.valueOf((String) item.get("type"))) {
					adapter.updateLastData(data);
					return;
				}
			}
			adapter.insertData(adapter.getCount(), data);
		}
	}

	/**
	 * 延时刷新操作
	 */
	private void delayNotify() {
		if (mRunnable == null) {
			mRunnable = new Runnable() {
				@Override
				public void run() {
					mRecordChatListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
					scrollToBottom();
				}
			};
		}
		mHandler.removeCallbacks(mRunnable);
		mHandler.postDelayed(mRunnable, LiveChatFragment.LIST_DEPLY_NOTIFY);
	}

	public void clearChatMessage() {
		if (adapter != null) {
			adapter.clearData();
		}
	}

	public void sendMsg(Message msg) {
		if (mHandler != null) {
			mHandler.sendMessage(msg);
		} else {
			handleMessage(msg);
		}
	}

	/**
	 * 发送消息到聊天面板
	 *
	 * @param spannableString
	 */
	public void sendChatMsg(CharSequence spannableString, int arg1) {
		Message loMsg = new Message();
		loMsg.what = MsgTypes.ON_CHAT_MSG;
		loMsg.obj = spannableString;
		loMsg.arg1 = arg1;
		sendMsg(loMsg);
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
	public void onSysMsg(String content) {
		SpannableString spannableString = showColorText(content, COLOR_2CE150);
		Message loMsg = new Message();
		loMsg.what = MsgTypes.ON_CHAT_MSG;
		loMsg.obj = spannableString;
		sendMsg(loMsg);
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
		//如果是官方管理员
		if (Constants.USER_TYPE_OFFICIAL.equals(userType)) {
			spannableStringBuilder.append(showClickableText(userName, userType, userId, COLOR_FFF100));
		} else {
			spannableStringBuilder.append(showClickableText(userName, userType, userId, COLOR_FBB872));
		}
		if (!TextUtils.isEmpty(userType)) {
			spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(Utils.getFiledDrawable(Constants.USER_TYPE_PIX, userType)));
		}
		if (!TextUtils.isEmpty(medalsStr)) {
			try {
				JSONArray medals = new JSONArray(medalsStr);
				for (int i = 0; i < medals.length(); i++) {
					String url = Utils.getModelUri(String.valueOf(medals.get(i)));
					if (!TextUtils.isEmpty(url)) {
						spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(url, Utils.dip2px(mContext, 14)));
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

		// 如果主播等级不为空，说明是主播显示主播等级
		if (!TextUtils.isEmpty(moderatorLevel)) {
			spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(Utils.getLevelImageResourceUri(Constants.USER_ANCHOR_LEVEL_PIX, moderatorLevel),
					Utils.dip2px(mContext, 14)));
		} else if (!TextUtils.isEmpty(userLevel)) {
			spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, userLevel),
					Utils.dip2px(mContext, 14)));
		}
	}

	/**
	 * 用户聊天消息
	 *
	 * @param exclusiveUser true,为20级以上的用户，false低级别用户
	 */
	public void onChatMsg(String piFrom, String piTo, String fromLevel, String fromType, String psFromNickname, String fromGuardType,
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
		Message loMsg = new Message();
		loMsg.what = MsgTypes.ON_CHAT_MSG;
		loMsg.obj = spannableStringBuilder;
		sendMsg(loMsg);
	}

	public void scrollToBottom() {
		mRecordChatListView.setSelection(mRecordChatListView.getCount() - 1);
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
		Spannable span = new SpannableString("");
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
	 * 静态的Handler对象
	 */
	private static class MyHandler extends Handler {

		private final WeakReference<WindowRecordBig> mFragment;

		public MyHandler(WindowRecordBig view) {
			mFragment = new WeakReference<>(view);
		}

		@Override
		public void handleMessage(Message msg) {
			WindowRecordBig view = mFragment.get();
			if (view != null) {
				view.handleMessage(msg);
			}
		}
	}
}
