package com.bixin.bixin.activities;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.bixin.bixin.base.act.BaseFragmentActivity;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Consts;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.ui.ShareDialog;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.social.UMPlatformData;
import com.umeng.analytics.social.UMPlatformData.UMedia;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.util.Map;

import tv.live.bx.R;

/**
 * @version 1.0
 * @CreateDate 2014-8-13
 */
public class ShareDialogActivity extends BaseFragmentActivity {
	/**
	 * 分享对话框
	 */
	private ShareDialog shareDialog;
	protected String shareContent; // "咕叽||鲜肉大叔妖男Young，基腐宅萌有咕叽,快来看****的直播，美CRY！！  ";
	protected String shareTitle; // "咕叽直播";
	protected String shareUrImg; // "http://www.guojiang.tv/img/roomlogo/poyin.jpg";
	protected String shareUrl; // "http://www.guojiang.tv";
	public static final int SHARE_REQUEST_CODE = 0x1010;

	public static String SHARE_INFO = "share_info";
	private Map<String, String> shareInfo = null;
	public static String Share_Content = "share_content";
	public static String Share_Title = "share_title";
	public static String Share_Url = "share_url";
	public static String Share_Img = "share_img";
	public static String Share_Dialog = "share_dialog";

	private int resultCode = RESULT_CANCELED;
	private UMShareAPI mUmShareAPI = null;
	protected UMShareListener mUmShareListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		isSystemBarTint = false;
		super.onCreate(savedInstanceState);
		shareInfo = (Map<String, String>) getIntent().getSerializableExtra(SHARE_INFO);
		// 配置分享平台
		configPlatforms();
	}

	@Override
	public int getStatusBarColor() {
		return R.color.trans;
	}

	@Override
	protected int getLayoutRes() {
		return BaseFragmentActivity.NO_SETTING_CONTENTVIEW;
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
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	// 如果有使用任一平台的SSO授权,则必须在对应的activity中实现onActivityResult方法, 并添加如下代码
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/**
		 * attention to this below ,must add this
		 * 父类调用之后，子类不可再用
		 **/
		UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 防止umeng分享内存泄漏
		UMShareAPI.get(this).release();
	}

	/**
	 * 弹出分享对话框
	 */
	protected void showShareDialog() {
		shareDialog = new ShareDialog(mActivity, new com.bixin.bixin.ui.ShareDialog.OnItemClickListener() {
			@Override
			public void onClick(int which) {
				//umeng sdk5.1.4 取消了onstart对调，故在此提示分享开始
				//umeng sdk6.4.4 又加上了onStart回调
				switch (which) {
					case ShareDialog.WEIXIN:
						onWeiXinClick();
						break;
					case ShareDialog.PENGYOUQUAN:
						onPengyouquanClick();
						break;
					case ShareDialog.WEIBO:
						onWeiBoClick();
						break;
					case ShareDialog.QQZONE:
						onQqZoneClick();
						break;
					case ShareDialog.QQ:
						onQQClick();
						break;

					default:
						break;
				}
			}
		}, new OnDialogDismissListener());
		shareDialog.builder().setCancelable(false).setCanceledOnTouchOutside(true);
		shareDialog.show();

	}

	private class OnDialogDismissListener implements OnDismissListener {

		@Override
		public void onDismiss(DialogInterface dialog) {
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					setResult(resultCode);
					finish();

				}
			}, 300);

		}
	}

	/*********************** 友盟分享 ******************/
	/**
	 * 配置分享平台参数</br>
	 */
	private void configPlatforms() {
		EvtLog.e(TAG, "configPlatforms");
		mUmShareAPI = UMShareAPI.get(this);
		// 初始化数据
		shareContent = "土豪都在围观的美女直播，你还不来！";
		shareUrl = Consts.DOWNLOAD_URL_SERVER;
		shareTitle = Consts.SHARE_TITLE; // "咕叽";
		shareUrImg = Consts.SHARE_URI_IMG;

		if (shareInfo != null) {
			if (!TextUtils.isEmpty(shareInfo.get(ShareDialogActivity.Share_Content))) {
				shareContent = shareInfo.get(ShareDialogActivity.Share_Content);
			}
			if (!TextUtils.isEmpty(shareInfo.get(ShareDialogActivity.Share_Url))) {
				shareUrl = shareInfo.get(ShareDialogActivity.Share_Url);
			}
			if (!TextUtils.isEmpty(shareInfo.get(ShareDialogActivity.Share_Title))) {
				shareTitle = shareInfo.get(ShareDialogActivity.Share_Title);
			}
			if (!TextUtils.isEmpty(shareInfo.get(ShareDialogActivity.Share_Img))) {
				shareUrImg = shareInfo.get(ShareDialogActivity.Share_Img);
			}
			if (Utils.strBool(shareInfo.get(ShareDialogActivity.Share_Dialog))) {
				showShareDialog();
			}
		}

	}


	// 点击微信分享
	protected void onWeiXinClick() {
		/** 是否安装微信客户端，toast提示，并做具体子类的操作 */
		if (!mUmShareAPI.isInstall(this, SHARE_MEDIA.WEIXIN)) {
			showTips(R.string.uninstall_weixin_tip);
			notInstall();
			return;
		}
		performShareWechat(SHARE_MEDIA.WEIXIN);
		UMPlatformData platform = new UMPlatformData(UMedia.WEIXIN_FRIENDS, "lsuserId");
		MobclickAgent.onSocialEvent(mActivity, platform);
	}

	// 点击朋友圈分享
	protected void onPengyouquanClick() {
		/** 是否安装微信客户端，toast提示，并做具体子类的操作 */
		if (!mUmShareAPI.isInstall(this, SHARE_MEDIA.WEIXIN)) {
			showTips(R.string.uninstall_weixin_tip);
			notInstall();
			return;
		}
		performShareWechat(SHARE_MEDIA.WEIXIN_CIRCLE);
		UMPlatformData platform = new UMPlatformData(UMedia.WEIXIN_CIRCLE, "lsuserId");
		MobclickAgent.onSocialEvent(mActivity, platform);
	}

	// 点击qq分享
	protected void onQQClick() {
		/** 是否安装QQ客户端，toast提示，并做具体子类的操作 */
		if (!mUmShareAPI.isInstall(this, SHARE_MEDIA.QQ)) {
			showTips(R.string.uninstall_qq_tip);
			notInstall();
			return;
		}
		performShareQQ(SHARE_MEDIA.QQ);
		UMPlatformData platform = new UMPlatformData(UMedia.TENCENT_QQ, "lsuserId");
		MobclickAgent.onSocialEvent(mActivity, platform);
	}

	// 点击qq空间分享
	protected void onQqZoneClick() {
		/** 是否安装QQ客户端，toast提示，并做具体子类的操作 */
		if (!mUmShareAPI.isInstall(this, SHARE_MEDIA.QQ)) {
			showTips(R.string.uninstall_qq_tip);
			notInstall();
			return;
		}
		performShareQQZone(SHARE_MEDIA.QZONE);
		UMPlatformData platform = new UMPlatformData(UMedia.TENCENT_QZONE, "lsuserId");
		MobclickAgent.onSocialEvent(mActivity, platform);
	}

	// 点击微博分享
	protected void onWeiBoClick() {
		/** 是否安装新浪微博客户端，toast提示，并做具体子类的操作 */
		//网页版sina分享回调会存在问题
		// 新浪未安装，使用网页授权
		UMShareConfig config = new UMShareConfig();
		if (!mUmShareAPI.isInstall(this, SHARE_MEDIA.SINA)) {
			// 网页授权
			config.setSinaAuthType(UMShareConfig.AUTH_TYPE_WEBVIEW);
		} else {
			// 设置新浪sso授权(客户端授权)
			config.setSinaAuthType(UMShareConfig.AUTH_TYPE_SSO);
		}
		UMShareAPI.get(this).setShareConfig(config);
		performShareWeiBo(SHARE_MEDIA.SINA);
		UMPlatformData platform = new UMPlatformData(UMedia.SINA_WEIBO, "lsuserId");
		MobclickAgent.onSocialEvent(mActivity, platform);
	}

	/**
	 * 没有客户端需要执行的操作
	 */
	protected void notInstall() {
	}

	/**
	 * 设置微信和朋友圈的分享内容
	 *
	 * @param platform
	 */
	protected void performShareWechat(SHARE_MEDIA platform) {
		UMWeb web = new UMWeb(shareUrl);
		web.setTitle(shareTitle);//标题
		web.setThumb(extractedUMimage());
		web.setDescription(shareContent);
		new ShareAction(this).setPlatform(platform).setCallback(shareCallback())
				.withMedia(web)
				.share();
	}

	private UMImage extractedUMimage() {
		if (!TextUtils.isEmpty(shareUrImg)) {
			return new UMImage(mActivity, shareUrImg);
		} else {
			return new UMImage(mActivity, R.drawable.icon_logo);
		}

	}

	/**
	 * 设置QQ分享内容
	 *
	 * @param platform
	 */
	protected void performShareQQ(SHARE_MEDIA platform) {
		UMWeb web = new UMWeb(shareUrl);
		web.setTitle(shareTitle);//标题
		web.setThumb(extractedUMimage());
		web.setDescription(shareContent);
		new ShareAction(this).setPlatform(platform).setCallback(shareCallback())
				.withMedia(web)
				.share();
	}

	/**
	 * 设置QQ空间的分享内容
	 *
	 * @param platform
	 */
	protected void performShareQQZone(SHARE_MEDIA platform) {
		UMWeb web = new UMWeb(shareUrl);
		web.setTitle(shareTitle);//标题
		web.setThumb(extractedUMimage());
		web.setDescription(shareContent);
		new ShareAction(this).setPlatform(platform).setCallback(shareCallback())
				.withMedia(web)
				.share();
	}

	/**
	 * 分享到新浪微博
	 */
	protected void performShareWeiBo(SHARE_MEDIA platform) {
		UMWeb web = new UMWeb(shareUrl);
		web.setTitle(shareTitle);//标题
		web.setThumb(new UMImage(this, shareUrImg));
		web.setDescription(shareContent + shareUrl);
		new ShareAction(this).setPlatform(platform).setCallback(shareCallback())
				.withMedia(web)
				.share();
	}

	protected UMShareListener shareCallback() {
		if (mUmShareListener == null) {
			mUmShareListener = new UMShareListener() {

				@Override
				public void onStart(SHARE_MEDIA share_media) {
					showTips(" 开始分享");
				}

				@Override
				public void onResult(SHARE_MEDIA platform) {
					showTips(" 分享成功啦");
					resultCode = RESULT_OK;
					EvtLog.d("performShareWechat", "分享成功，platform：" + platform.toString());
					BusinessUtils.shareReport(mActivity, null);
					if (shareDialog != null) {
						shareDialog.dismiss();
					}
				}

				@Override
				public void onError(SHARE_MEDIA platform, Throwable t) {
					EvtLog.d("performShareWechat", "分享失败，platform：" + platform.toString());
					showTips(" 分享失败啦");
				}

				@Override
				public void onCancel(SHARE_MEDIA platform) {
					EvtLog.d("performShareWechat", "分享取消，platform：" + platform.toString());
					showTips(" 分享取消啦");
				}
			};
		}
		return mUmShareListener;
	}
}
