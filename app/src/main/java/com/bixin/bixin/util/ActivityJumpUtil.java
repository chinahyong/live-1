package com.bixin.bixin.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.bixin.bixin.activities.CalMainActivity;
import com.bixin.bixin.activities.EditDataActivity;
import com.bixin.bixin.activities.EditInfoActivity;
import com.bixin.bixin.activities.ImageBrowserActivity;
import com.bixin.bixin.activities.LiveTypeActivity;
import com.bixin.bixin.activities.LoginActivity;
import com.bixin.bixin.activities.PersonInfoActivity;
import com.bixin.bixin.activities.PhoneBindActivity;
import com.bixin.bixin.activities.ReportActivity;
import com.bixin.bixin.activities.ShareDialogActivity;
import com.bixin.bixin.activities.UserFansActivity;
import com.bixin.bixin.activities.UserFocusActivity;
import com.bixin.bixin.activities.UserInviterActivity;
import com.bixin.bixin.activities.WebViewActivity;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.live.activities.LiveBaseActivity;
import com.bixin.bixin.live.activities.LiveCameraStreamActivity;
import com.bixin.bixin.live.activities.LiveMediaPlayerActivity;
import com.bixin.bixin.live.activities.LiveWebViewActivity;


/**
 * Title: ActivityJumpUtil.java Description: 界面跳转类
 */
public class ActivityJumpUtil {

	static final String TAG = "ActivityJumpUtil";

	/**
	 * 转向Activity
	 */
	public static void welcomeToMainActivity(Activity activity) {
		Intent intent = new Intent(activity, CalMainActivity.class);
		activity.startActivity(intent);
		activity.finish();
	}

	/**
	 * 转向Activity
	 *
	 * @param mClass
	 * @param isFinish
	 * @param extraName
	 * @param extra
	 */
	public static void gotoActivity(Context activity, Class<? extends Activity> mClass, boolean isFinish,
									String extraName, Serializable extra) {
		Intent intent = new Intent(activity, mClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (extraName != null) {
			intent.putExtra(extraName, extra);
		}
		activity.startActivity(intent);
		if (isFinish)
			((Activity) activity).finish();
	}

	/**
	 * 转向Activity并且获取返回结果
	 *
	 * @param poTo
	 * @param piRequestCode
	 * @param psExtraName
	 * @param poExtra
	 */
	public static void gotoActivityForResult(Activity activity, Class<? extends Activity> poTo, int piRequestCode,
											 String psExtraName, Serializable poExtra) {
		Intent intent = new Intent(activity, poTo);
		if (psExtraName != null)
			intent.putExtra(psExtraName, poExtra);
		activity.startActivityForResult(intent, piRequestCode);
	}

	/**
	 * 转向toImageBrowserActivity
	 *
	 * @param moContext
	 * @param position
	 * @param imageUrl
	 */
	public static void toImageBrowserActivity(Context moContext, int position, List<String> imageUrl) {
		Intent intent = new Intent(moContext, ImageBrowserActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt(ImageBrowserActivity.INIT_SHOW_INDEX, position);
		bundle.putSerializable(ImageBrowserActivity.IMAGE_URL, (Serializable) imageUrl);
		intent.putExtras(bundle);
		moContext.startActivity(intent);
	}

	/**
	 * 转向Activity
	 */
	public static void toShareActivity(Activity activity, Map<String, String> shareInfo) {
		Intent intent = new Intent(activity, ShareDialogActivity.class);
		intent.putExtra(ShareDialogActivity.SHARE_INFO, (Serializable) shareInfo);
		activity.startActivityForResult(intent, ShareDialogActivity.SHARE_REQUEST_CODE);
	}

	public static void toLiveActivity(Activity activity, Map<String, String> anchorInfo, Map<String, Object> roomInfo,
									  boolean cameraDir, int mClarityType) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(LiveBaseActivity.ANCHOR_RID, (Serializable) anchorInfo);
		bundle.putSerializable(LiveBaseActivity.ROOM_INFO, (Serializable) roomInfo);
		bundle.putBoolean("camera_dir", cameraDir);
		bundle.putInt("clarity_type", mClarityType);

		Intent intent = new Intent(activity, LiveCameraStreamActivity.class);
		intent.putExtras(bundle);
		activity.startActivity(intent);
		activity.finish();
	}

	public static void toEditInfoActivity(Activity activity, int piRequestCode, String content, String tipInfo,
										  String title, int min, int max) {
		Intent intent = new Intent(activity, EditInfoActivity.class);
		intent.putExtra(EditInfoActivity.TEXT_TIP, tipInfo);
		intent.putExtra(EditInfoActivity.EDIT_CONTENT, content);
		intent.putExtra(EditInfoActivity.TITLE, title);
		intent.putExtra(EditInfoActivity.MIN_NUM_INFO, min);
		intent.putExtra(EditInfoActivity.MAX_NUM_INFO, max);
		activity.startActivityForResult(intent, piRequestCode);
	}

	public static void toUserInviterActivity(Activity activity, String poExtra) {
		Intent intent = new Intent(activity, UserInviterActivity.class);
		intent.putExtra(UserInviterActivity.USER_ID, poExtra);
		activity.startActivity(intent);
	}

	public static void toLiveTypeActivity(Activity activity, String typeId, String typeName) {
		Intent intent = new Intent(activity, LiveTypeActivity.class);
		intent.putExtra(LiveTypeActivity.TYPE_ID, typeId);
		intent.putExtra(LiveTypeActivity.TYPE_NAME, typeName);
		activity.startActivity(intent);
	}

	/**
	 * 跳转到其他用户的个人主页
	 */
	public static void toPersonInfoActivity(Context activity, Map<String, ?> personInfo,
											int requestCode) {
		Intent intent = new Intent(activity, PersonInfoActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(PersonInfoActivity.PERSON_INFO, (Serializable) personInfo);
		if (activity instanceof Activity)
			((Activity) activity).startActivityForResult(intent, requestCode);
		else {
			activity.startActivity(intent);
		}
	}

	/**
	 * 跳转用户关注页面
	 */
	public static void toUserFocusActivity(Activity activity, String uId, boolean isOwer, int requestCode) {
		Intent intent = new Intent(activity, UserFocusActivity.class);
		intent.putExtra(UserFocusActivity.USER_ID, uId);
		intent.putExtra(PersonInfoActivity.IS_OWER, isOwer);
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 跳转到用户粉丝页面
	 */
	public static void toUserFansActivity(Activity activity, String uId, boolean isOwer, int requestCode) {
		Intent intent = new Intent(activity, UserFansActivity.class);
		intent.putExtra(UserFansActivity.USER_ID, uId);
		intent.putExtra(PersonInfoActivity.IS_OWER, isOwer);
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 跳转用户关注页面
	 */
	public static void toEditDataActivity(Activity activity, boolean isEditable,
										  int requestCode) {
		Intent intent = new Intent(activity, EditDataActivity.class);
		intent.putExtra(EditDataActivity.IS_EDITABLE, isEditable);
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 跳转举报页面
	 */
	public static void toReportActivity(Activity activity, String type, String id, int requestCode) {
		Intent intent = new Intent(activity, ReportActivity.class);
		intent.putExtra(ReportActivity.REPORT_ID, id);
		intent.putExtra(ReportActivity.REPORT_TYPE, type);
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 传递Bundle进行跳转
	 *
	 * @param activity
	 * @param clazz
	 * @param bundle
	 * @param requestCode
	 */
	public static void toActivityAndBundle(Activity activity, Class<?> clazz, Bundle bundle, int requestCode) {
		Intent intent = new Intent(activity, clazz);
		intent.putExtras(bundle);
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 跳转到普通直播间
	 *
	 * @param context
	 * @param lmItem  lmItem needInfo: "videoPlayUrl"	"headPic"   "rid"
	 */
	public static void toLiveMediaPlayerActivity(Context context, Map<String, ?> lmItem) {
		// 在这里可以自己写代码去定义用户点击后的行为
		Intent clickIntent = new Intent();
		clickIntent.setClass(context, LiveMediaPlayerActivity.class);
		clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		clickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		clickIntent.putExtra(LiveBaseActivity.ANCHOR_RID, (Serializable) lmItem);
		context.startActivity(clickIntent);
	}

	/**
	 * 跳转到LoginActivity页面，并且清除之前所有activity
	 * 不登录返回就直接退出APP，登录完成就进主页面
	 *
	 * @param context
	 * @param reportFlag 是否已经上报过
	 */
	public static void toLoginActivity(Context context, boolean reportFlag) {
		Intent intent = new Intent(context, LoginActivity.class);
		intent.putExtra("reportFlag", reportFlag);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 跳转到直播间活动抢泡泡页面
	 *
	 * @param context
	 * @param url      活动webview的url
	 * @param animData 动画数据
	 */
	public static void toLiveWebViewActivity(Activity context, String url, String animData) {
		Intent intent = new Intent(context, LiveWebViewActivity.class);
		intent.putExtra(LiveWebViewActivity.URL, url);
		intent.putExtra(LiveWebViewActivity.ANIM_DATA, animData);
		context.startActivity(intent);
	}

	/**
	 * 跳转到GPS设置页面
	 *
	 * @param context
	 */
	public static void toLocationSettingActivity(Activity context, int requestCode) {
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		context.startActivityForResult(intent, requestCode);
	}

	/**
	 * 跳转到PhoneBindActivity
	 *
	 * @param context
	 * @param isSkip  是否可以跳过
	 */
	public static void toPhoneBindActivity(Activity context, int requestCode, boolean isSkip) {
		Intent intent = new Intent(context, PhoneBindActivity.class);
		intent.putExtra(PhoneBindActivity.IS_SKIP, isSkip);
		context.startActivityForResult(intent, requestCode);
	}


	public static void toCalMainActivity(Activity context) {
		Intent intent = new Intent(context, CalMainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	public static void toWebViewActivity(Activity context, String url, int requestCode) {
		toWebViewActivity(context, url, true, requestCode);
	}

	/**
	 * 跳转到html5页面
	 *
	 * @param context
	 * @param url
	 * @param isNotShare
	 * @param requestCode
	 */
	public static void toWebViewActivity(Activity context, String url, boolean isNotShare, int requestCode) {
		Map<String, String> webInfo = new HashMap<>();
		webInfo.put(WebViewActivity.URL, url);
		webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(isNotShare));
		ActivityJumpUtil.gotoActivityForResult(context, WebViewActivity.class, requestCode, WebViewActivity.WEB_INFO,
				(Serializable) webInfo);
	}

	/**
	 * 跳转到html5页面
	 *
	 * @param context
	 * @param url
	 * @param isNotShare
	 */
	public static void toWebViewActivity(Context context, String url, boolean isNotShare) {
		Map<String, String> webInfo = new HashMap<>();
		webInfo.put(WebViewActivity.URL, url);
		webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(isNotShare));
		ActivityJumpUtil.gotoActivity(context, WebViewActivity.class, false, WebViewActivity.WEB_INFO,
				(Serializable) webInfo);
	}

	/**
	 * 跳转到私密直播间
	 *
	 * @param context
	 * @param lmItem
	 */
	public static void toPrivateLiveMediaPlayerActivity(Context context, Map<String, String> lmItem) {
		// 在这里可以自己写代码去定义用户点击后的行为
		lmItem.put(LiveBaseActivity.ANCHOR_PRIVATE, Constants.COMMON_TRUE);
		Intent clickIntent = new Intent();
		clickIntent.setClass(context, LiveMediaPlayerActivity.class);
		clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		clickIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		clickIntent.putExtra(LiveBaseActivity.ANCHOR_RID, (Serializable) lmItem);
		context.startActivity(clickIntent);
	}

	/**
	 * 主播开启“私播”直播间
	 *
	 * @param activity
	 * @param anchorInfo
	 * @param requestCode 请求code
	 */
	public static void toPrivateLiveCameraStreamActivity(Activity activity, Map<String, String> anchorInfo, int requestCode) {
		anchorInfo.put(LiveBaseActivity.ANCHOR_PRIVATE, Constants.COMMON_TRUE);
		Bundle bundle = new Bundle();
		bundle.putSerializable(LiveBaseActivity.ANCHOR_RID, (Serializable) anchorInfo);
		bundle.putBoolean("camera_dir", true);

		Intent intent = new Intent(activity, LiveCameraStreamActivity.class);
		intent.putExtras(bundle);
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 主播开启“私播”直播间
	 *
	 * @param activity
	 * @param anchorInfo
	 * @param requestCode 请求code
	 */
	public static void toPrivateLiveCameraStreamActivity(Fragment activity, Map<String, String> anchorInfo, int requestCode) {
		anchorInfo.put(LiveBaseActivity.ANCHOR_PRIVATE, Constants.COMMON_TRUE);
		Bundle bundle = new Bundle();
		bundle.putSerializable(LiveBaseActivity.ANCHOR_RID, (Serializable) anchorInfo);
		bundle.putBoolean("camera_dir", true);

		Intent intent = new Intent(activity.getContext(), LiveCameraStreamActivity.class);
		intent.putExtras(bundle);
		activity.startActivityForResult(intent, requestCode);
	}
}


