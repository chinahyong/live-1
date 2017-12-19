package cn.efeizao.feizao.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lonzh.lib.network.JSONParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Map;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.JacksonUtil;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.database.DatabaseUtils;
import tv.live.bx.database.model.PersonInfo;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.util.UiHelper;

/**
 * Title: CustomDialogBuilder.java</br> Description: 自定义对话框</br> Copyright:
 * Copyright (c) 2008</br>
 *
 * @version 1.0
 * @CreateDate 2014-7-8
 */
public class PersonInfoCustomDialogBuilder extends CustomDialogBuilder {

	private ImageView mUserHeadPhoto, mUserHeadPhotoV;
	private TextView mUserHeadTextView, mVerifiedInfo, mUserIdTextView;
	private LinearLayout mUserIconLayout;
	private TextView mUserIntrolTextView;
	private ImageView mUserManage,mUserReport;
	private Button mUserFocusBtn, mUserLineBtn;
	private ImageButton mUserSpeakBtn, mUserChatBtn, mUserPersonInfoBtn;
	private TextView mHeadFlowerNum, mHeadFansNum, mHeadFocusNum;
	private View mLineView;
	private LinearLayout mUserSpeakLayout, UserFlowerLayout;
	private TextView mUpgradeCoin;
	private ProgressBar mLevelProgress;
	private Handler mHandler = new MyHandler();
	private String mUid;
	private String mUserType;
	//是否显示主播等级
	private boolean isShowAnchorLevel = false;
	private boolean isOwen;
	private OnClickListener mOnFocusSuccessListener;
	// 主播头像
	private ImageView mAnchorHeadPhoto = null;
	private Map<String, String> mPersonInfo;


	public PersonInfoCustomDialogBuilder(Context context, String userName, String userType, String uid, String mid, boolean isShowIntrol) {
		super(context, R.layout.dialog_live_userinfo_layout);
		mContext = context;
		mUserIdTextView = (TextView) mDialogView.findViewById(R.id.item_user_id);
		mUserHeadPhoto = (ImageView) mDialogView.findViewById(R.id.item_head);
		mUserHeadPhotoV = (ImageView) mDialogView.findViewById(R.id.item_head_v);
		mUserHeadTextView = (TextView) mDialogView.findViewById(R.id.item_user_name);
		mVerifiedInfo = (TextView) mDialogView.findViewById(R.id.item_user_verifiedinfo);
		mUserIconLayout = (LinearLayout) mDialogView.findViewById(R.id.item_user_icon);
		mUserIntrolTextView = (TextView) mDialogView.findViewById(R.id.item_user_intro);
		mUserManage = (ImageView) mDialogView.findViewById(R.id.item_user_manage);
		mUserReport = (ImageView) mDialogView.findViewById(R.id.item_user_report);
		mUserFocusBtn = (Button) mDialogView.findViewById(R.id.item_focus);
		mUserSpeakBtn = (ImageButton) mDialogView.findViewById(R.id.item_speak);
		mUserChatBtn = (ImageButton) mDialogView.findViewById(R.id.item_chat);
		mUserLineBtn = (Button) mDialogView.findViewById(R.id.item_line_user);
		mUserChatBtn.setEnabled(false);
		mUserPersonInfoBtn = (ImageButton) mDialogView.findViewById(R.id.item_person_info);

		mHeadFlowerNum = (TextView) mDialogView.findViewById(R.id.item_flower_num);
		mHeadFansNum = (TextView) mDialogView.findViewById(R.id.item_fans_num);
		mHeadFocusNum = (TextView) mDialogView.findViewById(R.id.item_focus_num);

		mLevelProgress = (ProgressBar) mDialogView.findViewById(R.id.item_level_progress);
		mUpgradeCoin = (TextView) mDialogView.findViewById(R.id.item_level_jine);

		mLineView = mDialogView.findViewById(R.id.item_line);
		mUserSpeakLayout = (LinearLayout) mDialogView.findViewById(R.id.item_speak_layout);
		UserFlowerLayout = (LinearLayout) mDialogView.findViewById(R.id.item_flower_layout);
		this.mDialog.setCanceledOnTouchOutside(true);
		this.mDialog.setCancelable(true);

		//如果被操作的主播，显示主播经验信息
		if (Constants.USER_TYPE_ANCHOR.equals(userType)) {
			mUpgradeCoin.setVisibility(View.VISIBLE);
			mLevelProgress.setVisibility(View.VISIBLE);
			UserFlowerLayout.setVisibility(View.VISIBLE);
			isShowAnchorLevel = true;
		}

		//如果不显示个人信息“签名信息”
		if (!isShowIntrol) {
			mUserIntrolTextView.setVisibility(View.GONE);
		}
		initData(userName, userType, uid);
		mUserManage.setEnabled(false);
		BusinessUtils.getRoomUserInfoData(mContext, mid, uid, new UserInfoCallbackData(this));
	}

	private void initData(String userName, String userType, String uid) {
		this.mUid = uid;
		this.mUserType = userType;
		mUserHeadTextView.setText(userName);
		mUserFocusBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 如果已经关注了，则取消关注
				if (Utils.strBool((String) v.getTag())) {
					BusinessUtils.removeFollow(mContext, new RemoveFollowCallbackData(PersonInfoCustomDialogBuilder.this), mUid);
				} else {
					OperationHelper.onEvent(FeizaoApp.mContext, "followBroadcasterInPersonalCard", null);
					BusinessUtils.follow(mContext, new FollowCallbackData(PersonInfoCustomDialogBuilder.this), mUid);
				}
			}
		});

	}

	/**
	 * 设置操作用户类型，如主播，管理员，普通用户
	 */
	public void setControlType(String type) {
		if (Constants.USER_TYPE_ANCHOR.equals(mUserType)) {
			mUserManage.setVisibility(View.INVISIBLE);
			return;
		}
		if (Constants.USER_TYPE_ANCHOR.equals(type)) {
			// 如果用户类别为管理员，则显示“取消管理员”
//			if (Constants.USER_TYPE_OFFICIAL_ADMIN.equals(mUserType) || Constants.USER_TYPE_ADMIN.equals(mUserType)) {
//				mUserPersonInfoBtn.setText(R.string.live_remove_mannger_tip);
//			} else {
//				mUserPersonInfoBtn.setText(R.string.live_setting_mannger_tip);
//			}
			mUserManage.setVisibility(View.VISIBLE);
		} else if (Constants.USER_TYPE_OFFICIAL.equals(type) || Constants.USER_TYPE_ADMIN.equals(type)) {
			mUserManage.setVisibility(View.VISIBLE);
		} else {
			mUserManage.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 设置是否显示连线按钮
	 * 普通直播间主播侧会显示
	 */
	public void setConnectFlag(boolean flag) {
		if (flag) {
//			mUserLineBtn.setVisibility(View.VISIBLE);
			mUserLineBtn.setVisibility(View.GONE);
		} else {
			mUserLineBtn.setVisibility(View.GONE);
		}
	}

	public void setAnchorHeadPhotoView(ImageView anchorHeadPhoto) {
		this.mAnchorHeadPhoto = anchorHeadPhoto;
	}

	public void setFlowerNum(String flowerNum) {
		mHeadFlowerNum.setText(flowerNum);
	}

	/**
	 * 设置是否为自己
	 */
	public void setIsOwen(boolean isOwen) {
		if (isOwen) {
			mUserReport.setVisibility(View.GONE);
			mUserManage.setVisibility(View.INVISIBLE);
			mUserFocusBtn.setVisibility(View.GONE);
			mLineView.setVisibility(View.GONE);
			mUserSpeakLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 按钮点击事件
	 */
	public void setOnEventClickListener(OnClickListener listener) {
		PositiveListener positiveListener = new PositiveListener(listener);
		mUserManage.setOnClickListener(positiveListener);
		mUserReport.setOnClickListener(positiveListener);
		mUserSpeakBtn.setOnClickListener(positiveListener);
		mUserLineBtn.setOnClickListener(positiveListener);
		mUserChatBtn.setOnClickListener(positiveListener);    //添加私信
		mUserPersonInfoBtn.setOnClickListener(positiveListener);
	}

	/**
	 * 设置关注功能监听 setOnFocusSuccessListener
	 */
	public void setOnFocusSuccessListener(OnClickListener listener) {
		this.mOnFocusSuccessListener = listener;
	}

	private void updateData(Map<String, String> personInfo) {
		ImageLoaderUtil.getInstance().loadHeadPic(FeizaoApp.mContext, mUserHeadPhoto, personInfo.get("headPic"));
		// 更新主播头像
		if (mAnchorHeadPhoto != null) {
			ImageLoaderUtil.getInstance().loadHeadPic(FeizaoApp.mContext, mAnchorHeadPhoto, personInfo.get("headPic"));
		}
		// 如果不是自己，显示的是关注按钮
		if (Utils.getBooleanFlag(personInfo.get("isAttention"))) {
			mUserFocusBtn.setBackgroundResource(R.drawable.btn_focused_selector);
			// 标记已关注
			mUserFocusBtn.setTag(Constants.COMMON_TRUE);
		} else {
			mUserFocusBtn.setBackgroundResource(R.drawable.btn_focus_selector);
			// 标记已取消关注
			mUserFocusBtn.setTag("false");
		}
		mUserIdTextView.setText("ID:" + personInfo.get("uid"));

		int size = Utils.dip2px(mContext, 16f);
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
		spannableStringBuilder.append(personInfo.get("nickname"));
		try {
			if (!TextUtils.isEmpty(personInfo.get("medals"))) {
				JSONArray medals = new JSONArray(personInfo.get("medals"));
				for (int i = 0; i < medals.length(); i++) {
					String url = Utils.getModelUri(String.valueOf(medals.get(i)));
					if (!TextUtils.isEmpty(url)) {
						spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(mUserHeadTextView, url, size));
					}
				}
			}

			if (!TextUtils.isEmpty(personInfo.get("guardTypes"))) {
				JSONArray guardTypes = new JSONArray(personInfo.get("guardTypes"));
				for (int i = 0; i < guardTypes.length(); i++) {
					spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(Utils.getFiledDrawable(Constants.USER_GUARD_LEVEL_PIX, String.valueOf(guardTypes.get(i))), size));
				}
			}

			spannableStringBuilder.append(" ").append(Utils.getImageToSpannableString(mUserHeadTextView, Utils.getLevelImageResourceUri(personInfo, isShowAnchorLevel), size));
		} catch (Exception e) {
			e.printStackTrace();
		}
		mUserHeadTextView.setText(spannableStringBuilder);

		if (TextUtils.isEmpty(personInfo.get("signature"))) {
			mUserIntrolTextView.setText(R.string.live_signature_empty_tip);
		} else {
			mUserIntrolTextView.setText(personInfo.get("signature"));
		}

		mHeadFansNum.setText(personInfo.get("fansNum"));
		mHeadFocusNum.setText(personInfo.get("attentionNum"));
		mUserHeadPhotoV.setVisibility(Utils.getBooleanFlag(personInfo.get("verified")) ? View.VISIBLE : View.GONE);
		if (mUserHeadPhotoV.getVisibility() == View.VISIBLE) {
			mVerifiedInfo.setVisibility(View.VISIBLE);
			mVerifiedInfo.setText(String.format(mContext.getString(R.string.common_verify_info), (String) personInfo.get("verifyInfo")));
		} else {
			mVerifiedInfo.setVisibility(View.GONE);
		}

		//如果被操作的用户是主播
		if (Constants.USER_TYPE_ANCHOR.equals(mUserType)) {
			mUpgradeCoin.setText(String.format(mContext.getResources().getString(R.string.anchor_update_coin), personInfo.get("moderatorNextLevelNeedCoin")));
			float comsumeCoin = 0, moreCoin = 1;
			if (!TextUtils.isEmpty(personInfo.get("moderatorLevelCoin"))) {
				comsumeCoin = Float.parseFloat(personInfo.get("moderatorLevelCoin"));
			}
			if (!TextUtils.isEmpty(personInfo.get("moderatorNextLevelNeedCoin"))) {
				moreCoin = Integer.parseInt(personInfo.get("moderatorNextLevelNeedCoin"));
			}
			mLevelProgress.setProgress((int) ((comsumeCoin / (comsumeCoin + moreCoin)) * 100));
		}

		mUserChatBtn.setEnabled(true);
		// 双方是否已经使用了私信卡
		mUserChatBtn.setTag(personInfo.get("messageCardAvailable"));

		//设置tag信息
		mUserManage.setTag(personInfo.get("isBan"));
	}

	public String getHeadPic() {
		return mPersonInfo != null ? mPersonInfo.get("headPic") : null;
	}

	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MsgTypes.GET_USER_INFO_SUCCESS:
					mPersonInfo = (Map<String, String>) msg.obj;
					updateData(mPersonInfo);
					mUserManage.setEnabled(true);
					break;
				case MsgTypes.GET_USER_INFO_FAILED:
					Bundle bundle = msg.getData();
					UiHelper.showToast(mContext, bundle.getString("errorMsg"));
					mUserManage.setEnabled(false);
					break;
				case MsgTypes.FOLLOW_SUCCESS:
					OperationHelper.onEvent(FeizaoApp.mContext, "followBroadcasterInPersonalCardSuccessful", null);
					mUserFocusBtn.setTag(Constants.COMMON_TRUE);
					mUserFocusBtn.setBackgroundResource(R.drawable.btn_focused_selector);

					int fansNum = Integer.parseInt(mHeadFansNum.getText().toString()) + 1;
					mHeadFansNum.setText(String.valueOf(fansNum));
					//如果是主播信息卡，则有回调信息
					if (mOnFocusSuccessListener != null && Constants.USER_TYPE_ANCHOR.equals(mUserType)) {
						mOnFocusSuccessListener.onClick(mUserFocusBtn);
					}
					UiHelper.showShortToast(mContext, R.string.person_focus_success);
					UiHelper.showNotificationDialog(mContext);
					break;
				case MsgTypes.FOLLOW_FAILED:
					UiHelper.showShortToast(mContext, (String) msg.obj);
					break;
				case MsgTypes.REMOVE_FOLLOW_SUCCESS:
					OperationHelper.onEvent(FeizaoApp.mContext, "clickCancelFollowBroadcasterInPersonalCard", null);
					mUserFocusBtn.setTag("false");
					mUserFocusBtn.setBackgroundResource(R.drawable.btn_focus_selector);
					int fansNum2 = Integer.parseInt(mHeadFansNum.getText().toString()) - 1;
					mHeadFansNum.setText(String.valueOf(fansNum2));
					//如果是主播信息卡，则有回调信息
					if (mOnFocusSuccessListener != null && Constants.USER_TYPE_ANCHOR.equals(mUserType)) {
						mOnFocusSuccessListener.onClick(mUserFocusBtn);
					}
					UiHelper.showShortToast(mContext, R.string.person_remove_focus_success);
					break;
				case MsgTypes.REMOVE_FOLLOW_FAILED:
					UiHelper.showShortToast(mContext, (String) msg.obj);
					break;
				default:
					break;
			}
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
	private static class UserInfoCallbackData implements CallbackDataHandle {

		private final WeakReference<PersonInfoCustomDialogBuilder> mFragment;

		public UserInfoCallbackData(PersonInfoCustomDialogBuilder fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "UserInfoCallbackData success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MsgTypes.GET_USER_INFO_SUCCESS;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					PersonInfoCustomDialogBuilder meFragment = mFragment.get();
					// 数据库存储用户个人信息
					if (result != null) {
						DatabaseUtils.saveOrupdatePersonInfoToDatabase(FeizaoApp.mContext, JacksonUtil.readValue(String.valueOf(result), PersonInfo.class));
					}
					// 如果fragment未回收，发送消息
					if (meFragment != null)
						meFragment.mHandler.sendMessage(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.GET_USER_INFO_FAILED;
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				PersonInfoCustomDialogBuilder meFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.mHandler.sendMessage(msg);
			}
		}

	}

	/**
	 * 取消关注用户信息回调 Reason: TODO ADD REASON(可选). <br/>
	 */
	private static class RemoveFollowCallbackData implements CallbackDataHandle {

		private final WeakReference<PersonInfoCustomDialogBuilder> mAcivity;

		private int position;

		public RemoveFollowCallbackData(PersonInfoCustomDialogBuilder fragment) {
			mAcivity = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FollowCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.REMOVE_FOLLOW_SUCCESS;
					msg.obj = position;
					PersonInfoCustomDialogBuilder meFragment = mAcivity.get();
					// 如果fragment未回收，发送消息
					if (meFragment != null)
						meFragment.mHandler.sendMessage(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.REMOVE_FOLLOW_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				PersonInfoCustomDialogBuilder meFragment = mAcivity.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.mHandler.sendMessage(msg);
			}
		}

	}

	/**
	 * 关注用户信息回调 Reason: TODO ADD REASON(可选). <br/>
	 */
	private static class FollowCallbackData implements CallbackDataHandle {

		private final WeakReference<PersonInfoCustomDialogBuilder> mAcivity;

		public FollowCallbackData(PersonInfoCustomDialogBuilder fragment) {
			mAcivity = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "FollowCallbackData success " + success + " errorCode" + errorCode);
			Message msg = new Message();
			if (success) {
				try {
					msg.what = MsgTypes.FOLLOW_SUCCESS;
					PersonInfoCustomDialogBuilder meFragment = mAcivity.get();
					// 如果fragment未回收，发送消息
					if (meFragment != null)
						meFragment.mHandler.sendMessage(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.FOLLOW_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				msg.obj = errorMsg;
				PersonInfoCustomDialogBuilder meFragment = mAcivity.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.mHandler.sendMessage(msg);
			}
		}

	}

}
