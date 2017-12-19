package tv.live.bx.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.widget.Toast;
import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;
import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.PayTask;
import com.lonzh.lib.network.JSONParser;
import com.lonzh.lib.network.LZCookieStore;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;
import tv.live.bx.common.BusinessUtils;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Consts;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.common.WebConstants;
import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.FileUtil;
import tv.live.bx.library.util.PackageUtil;
import tv.live.bx.model.PayResult;
import tv.live.bx.ui.ActionSheetDialog;
import tv.live.bx.ui.ActionSheetDialog.OnSheetItemClickListener;
import tv.live.bx.ui.ActionSheetDialog.SheetItemColor;
import tv.live.bx.ui.MyWebView;
import tv.live.bx.ui.MyWebView.IWebDataInterface;
import tv.live.bx.ui.MyWebView.OpenFileChooserCallBack;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.util.UiHelper;

public class WebViewActivity extends BaseFragmentActivity implements IWebDataInterface, OpenFileChooserCallBack {

	public static final int REQUEST_WEBVIEW_CODE = 0x2001;
	public static final String WEB_INFO = "webinfo";
	public static final String IS_NOT_SHARE = "isnotshare";
	public static final String URL = "url";
	public static final String WX_PAY_SUCCESS = "wx_pay_success";

	private UMShareAPI mUmShareAPI;
	protected MyWebView mWebView;
	private String msUrl;
	private Map<String, String> mBundleData;
	protected Map<String, String> mShareInfo = new HashMap<>();
	// 是否需要分享
	private boolean isNotShare = false;

	// 上传图片文件
	private ValueCallback<Uri> mUploadMsg;
	private ValueCallback<Uri[]> mUploadMsgs;
	private ActionSheetDialog actionSheetDialog;
	private File mCameraFile;

	public static final int REQUEST_CAMERA = 0x200;
	public static final int REQUEST_ALBUM = 0x202;
	private final static int MSG_SDK_ALI_AUTH_FLAG = 0x203;
	private final static int MSG_SDK_ALI_PAY_FLAG = 0x204;

	private AlertDialog moProgress;

	//注册微信支付
	final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);
	private PayReq mWxPayReq;

	// 友盟统计
	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_web_view;
	}

	@Override
	protected void initMembers() {
		mWebView = (MyWebView) findViewById(R.id.web_view_wv);
		mWebView.setOnIWebDataInterface(this);
		mWebView.setOpenFileChooserCallBack(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void initWidgets() {
		mWebView.addJavascriptInterface(new JsInvokeMainClass(), "recharge");// web
		// js定义的名称
		mWebView.addJavascriptInterface(new JsInvokeClass(), "gBridge");// web
		// js定义的名称
		LZCookieStore.synCookies(WebViewActivity.this);

		// 将该app注册到微信
		mWxPayReq = new PayReq();
		msgApi.registerApp(Consts.WEIXIN_APPID);
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		mBundleData = (Map<String, String>) getIntent().getSerializableExtra(WEB_INFO);
		msUrl = mBundleData.get(URL);
		isNotShare = Utils.strBool(mBundleData.get(IS_NOT_SHARE));
		mWebView.loadUrl(msUrl);

		mShareInfo.put(ShareDialogActivity.Share_Content, mBundleData.get(ShareDialogActivity.Share_Content));
		mShareInfo.put(ShareDialogActivity.Share_Img, mBundleData.get(ShareDialogActivity.Share_Img));
		mShareInfo.put(ShareDialogActivity.Share_Title, mBundleData.get(ShareDialogActivity.Share_Title));
		if (TextUtils.isEmpty(mBundleData.get(ShareDialogActivity.Share_Url))) {
			mShareInfo.put(ShareDialogActivity.Share_Url, msUrl);
		} else {
			mShareInfo.put(ShareDialogActivity.Share_Url, mBundleData.get(ShareDialogActivity.Share_Url));
		}
		mShareInfo.put(ShareDialogActivity.Share_Dialog, String.valueOf(true));
		initTitle();
	}

	@Override
	protected void setEventsListeners() {
	}

	@Override
	protected void initTitleData() {
		mTopBackLayout.setOnClickListener(new OnBaCkClick());
		setTopBackIv(R.drawable.a_common_btn_close_selector);
		if (!isNotShare) {
			mTopRightTextLayout.setOnClickListener(new OnShare());
			mTopRightTextLayout.setVisibility(View.VISIBLE);
			mTopRightText.setText(R.string.share);
		} else if (msUrl.endsWith(WebConstants.WEB_STORE)) {
			mTopRightTextLayout.setOnClickListener(new gotoMyBackpack());
			mTopRightTextLayout.setVisibility(View.VISIBLE);
			mTopRightText.setText(R.string.me_knapsack);
		} else if (msUrl.endsWith(WebConstants.WEB_MY_BACKPACK)) {
//			mTopRightTextLayout.setOnClickListener(new gotoStore());
//			mTopRightTextLayout.setVisibility(View.VISIBLE);
//			mTopRightText.setText(R.string.shop);
		} else if (msUrl.endsWith(WebConstants.REDPACKET_WEB_URL)) {
			mTopRightImageLayout.setOnClickListener(new gotoRedPacketHelp());
			mTopRightImageLayout.setVisibility(View.VISIBLE);
			mTopRightImage.setImageResource(R.drawable.icon_red_packet_help);
		} else {
			mTopRightTextLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onTitle(String title) {
		mTopTitleTv.setText(title);
	}


	@Override
	protected void onNewIntent(Intent intent) {
		if (intent != null) {
			boolean flag = intent.getBooleanExtra(WX_PAY_SUCCESS, false);
			//如果微信支付成功
			if (flag) {
				OperationHelper.onEvent(FeizaoApp.mContext, "theAmountOfRechargeSuccessful", null);
				mWebView.loadUrl("javascript:payResultCallback(true,'')");
			}
			setIntent(intent);
		}
		super.onNewIntent(intent);
	}

	@Override
	protected void onDestroy() {
		if (mWebView != null) {
			((ViewGroup) mWebView.getParent()).removeView(mWebView);
			mWebView.removeAllViews();
			mWebView.destroy();
			mWebView = null;
		}
		dismissProgressDialog();
		msgApi.unregisterApp();
		// 防止umeng分享内存泄漏
		UMShareAPI.get(this).release();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** umeng授权、分享需要重写 */
		UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.REQUEST_CODE_LOGIN) {
			EvtLog.e(TAG, "onActivityResult REQUEST_CODE_LOGIN " + resultCode);
			if (resultCode == LoginActivity.RESULT_CODE_OK) {
				LZCookieStore.synCookies(WebViewActivity.this);
				mWebView.reload(); // 刷新
			}
		} else if (requestCode == REQUEST_ALBUM) {
			try {
				if (data != null && resultCode == RESULT_OK) {
					Uri uri = data.getData();
					if (mUploadMsg != null) {
						mUploadMsg.onReceiveValue(uri);
					}
					if (mUploadMsgs != null) {
						mUploadMsgs.onReceiveValue(new Uri[]{uri});
					}
				} else {
					if (mUploadMsg != null) {
						mUploadMsg.onReceiveValue(null);
						mUploadMsg = null;
					}
					if (mUploadMsgs != null) {
						mUploadMsgs.onReceiveValue(null);
						mUploadMsgs = null;
					}
				}
			} catch (Exception e) {
				e.toString();
			}

		} else if (requestCode == REQUEST_CAMERA) {
			EvtLog.d(TAG, "拍摄照片：" + mCameraFile);
			try {
				if (mCameraFile != null && resultCode == RESULT_OK) {
					if (mUploadMsg != null) {
						mUploadMsg.onReceiveValue(Uri.fromFile(mCameraFile));
					}
					if (mUploadMsgs != null) {
						mUploadMsgs.onReceiveValue(new Uri[]{Uri.fromFile(mCameraFile)});
					}
				} else {
					if (mUploadMsg != null) {
						mUploadMsg.onReceiveValue(null);
						mUploadMsg = null;
					}
					if (mUploadMsgs != null) {
						mUploadMsgs.onReceiveValue(null);
						mUploadMsgs = null;
					}
				}
			} catch (Exception e) {
			}
		}
	}

	@Override
	// 设置回退
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			//避免 java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
			//onBackPressed();
			finish();
		}
		return false;
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
			case MsgTypes.MSG_PRE_PAY_SUCCESS:
				dismissProgressDialog();
				Map<String, String> preData = (Map<String, String>) msg.obj;
				genPayReq(preData);
				break;
			case MsgTypes.MSG_PRE_PAY_FAILED:
				dismissProgressDialog();
				Bundle bundle2 = msg.getData();
				UiHelper.showToast(this, bundle2.getString("errorMsg"));
				break;
			case MsgTypes.MSG_ALI_PAY_SUCCESS:
				dismissProgressDialog();
				Map<String, String> aliData = (Map<String, String>) msg.obj;
				aliPay(aliData.get("string"));
				break;
			case MsgTypes.MSG_ALI_PAY_FAILED:
				dismissProgressDialog();
				UiHelper.showToast(this, msg.getData().getString("errorMsg"));
				break;
			case MSG_SDK_ALI_PAY_FLAG:
				PayResult payResult = new PayResult((String) msg.obj);
				String result = payResult.getMemo();
				String resultStatus = payResult.getResultStatus();
				// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
				if (TextUtils.equals(resultStatus, "9000")) {
					OperationHelper.onEvent(FeizaoApp.mContext, "theAmountOfRechargeSuccessful", null);
					mWebView.loadUrl("javascript:payResultCallback(true,'')");
				} else {
					// 判断resultStatus 为非"9000"则代表可能支付失败
					// "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
					if (TextUtils.equals(resultStatus, "8000")) {
						showTips(R.string.me_ali_pay_ing);
					} else {
						// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
						showTips(result);
					}
				}
				break;
			case MsgTypes.MSG_ALI_LOGIN_SUCCESS:
				dismissProgressDialog();
				Map<String, String> aliLoginData = (Map<String, String>) msg.obj;
				aliAuth(aliLoginData.get("string"));
				break;
			case MsgTypes.MSG_ALI_LOGIN_FAILED:
				dismissProgressDialog();
				UiHelper.showToast(this, msg.getData().getString("errorMsg"));
				break;
			case MSG_SDK_ALI_AUTH_FLAG:
				PayResult authResult = new PayResult((String) msg.obj);
				// 判断resultStatus 为“9000”则代表认证成功，具体状态码代表含义可参考接口文档
				if (TextUtils.equals(authResult.getResultStatus(), "9000")) {
					String resultContent = authResult.getResult();
					String prefix = "auth_code=";
					String authCode = resultContent.substring(resultContent.indexOf(prefix) + prefix.length(),
							resultContent.indexOf("&", resultContent.indexOf(prefix)));
					showProgressDialog();
					BusinessUtils.aliUserAuth(mActivity, authCode, new AliAuthCodeCallbackData(WebViewActivity.this));
				} else {
					showTips(authResult.getMemo());
				}
				break;
			case MsgTypes.MSG_ALI_AUTH_CODE_SUCCESS:
				dismissProgressDialog();
				Map<String, String> tempMap = (Map<String, String>) msg.obj;
				if (Utils.getBooleanFlag(tempMap.get("isCertified"))) {
					mWebView.loadUrl("javascript:bindAlipayResult(true,'')");
				} else {
					mWebView.loadUrl("javascript:bindAlipayResult(false,'')");
				}
				break;
			case MsgTypes.MSG_ALI_AUTH_CODE_FAILED:
				dismissProgressDialog();
				showTips(msg.getData().getString("errorMsg"));
//				mWebView.loadUrl("javascript:bindAlipayResult('false','" + msg.getData().getString("errorMsg") + "')");
				break;
			case MsgTypes.MSG_BIND_SUCCESS:
				dismissProgressDialog();
				mWebView.loadUrl("javascript:bindWechatResult(true,'绑定成功')");
				break;
			case MsgTypes.MSG_BIND_FAILED:
				dismissProgressDialog();
				mWebView.loadUrl("javascript:bindWechatResult(false,'" + msg.getData().getString("errorMsg") + "')");
				break;
			default:
				break;
		}
	}

	private void showProgressDialog() {
		if (moProgress != null && moProgress.isShowing()) {
			return;
		}
		moProgress = Utils.showProgress(WebViewActivity.this);
	}

	/**
	 * dismissProgressDialog:关闭对话框.
	 */
	private void dismissProgressDialog() {
		if (moProgress != null && moProgress.isShowing())
			moProgress.dismiss();
	}

	/**
	 * 通过支付参数调起微信支付
	 *
	 * @param reqPay
	 */
	private void genPayReq(Map<String, String> reqPay) {
		//前端签名、参数屏蔽，全部使用后台发送过来的签名数据
//		req.appId = Consts.WEIXIN_APPID;
//		req.packageValue = "Sign=WXPay";
//		req.nonceStr = genNonceStr();
//		req.timeStamp = String.valueOf(genTimeStamp());
		mWxPayReq.partnerId = reqPay.get("partnerid");
		mWxPayReq.prepayId = reqPay.get("prepay_id");
		mWxPayReq.appId = reqPay.get("appid");
		mWxPayReq.packageValue = reqPay.get("package");
		mWxPayReq.nonceStr = reqPay.get("noncestr");
		mWxPayReq.timeStamp = reqPay.get("timestamp");


		List<NameValuePair> signParams = new LinkedList<>();
		signParams.add(new BasicNameValuePair("appid", mWxPayReq.appId));
		signParams.add(new BasicNameValuePair("noncestr", mWxPayReq.nonceStr));
		signParams.add(new BasicNameValuePair("package", mWxPayReq.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", mWxPayReq.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", mWxPayReq.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", mWxPayReq.timeStamp));

		//直接使用后台发送的签名，前端不再进行签名
		mWxPayReq.sign = reqPay.get("sign");        //genAppSign(signParams)
		EvtLog.e("orion", signParams.toString());
		sendPayReq();

	}

	private void sendPayReq() {
		msgApi.registerApp(Consts.WEIXIN_APPID);
		msgApi.sendReq(mWxPayReq);
	}

	private int paySerial = 1;
	private String callbackScheme = "mqqwallet1104759767";

	/**
	 * 支付宝支付
	 *
	 * @param payInfo
	 */
	private void aliPay(final String payInfo) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				PayTask aliPay = new PayTask(WebViewActivity.this);

				String result = aliPay.pay(payInfo, true);
				Message msg = new Message();
				msg.what = MSG_SDK_ALI_PAY_FLAG;
				msg.obj = result;
				sendMsg(msg);
			}
		}).start();
	}


	/**
	 * 支付宝登录授权
	 *
	 * @param payInfo
	 */
	private void aliAuth(final String payInfo) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				AuthTask aliAuth = new AuthTask(mActivity);
				String result = aliAuth.auth(payInfo, true);
				Message msg = new Message();
				msg.what = MSG_SDK_ALI_AUTH_FLAG;
				msg.obj = result;
				sendMsg(msg);
			}
		}).start();
	}

	/****************************
	 * 点击事件
	 ******************************/
	public class OnBaCkClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			finish();
		}
	}

	public class OnShare implements OnClickListener {

		@Override
		public void onClick(View v) {
			EvtLog.e(TAG, "toShareActivity ---------- ");
			OperationHelper.onEvent(FeizaoApp.mContext, "clickShareButtonInHtmlPage", null);
			ActivityJumpUtil.toShareActivity(mActivity, mShareInfo);
		}
	}

	public class gotoStore implements OnClickListener {
		@Override
		public void onClick(View v) {
			OperationHelper.onEvent(FeizaoApp.mContext, "clickStoreInMyBackpackPage", null);
			ActivityJumpUtil.toWebViewActivity(mActivity, WebConstants.getFullWebMDomain(WebConstants.WEB_STORE), true, -1);
		}
	}

	public class gotoMyBackpack implements OnClickListener {
		@Override
		public void onClick(View v) {
			OperationHelper.onEvent(FeizaoApp.mContext, "clickMyBackpackInStorePage", null);
			ActivityJumpUtil.toWebViewActivity(mActivity, WebConstants.getFullWebMDomain(WebConstants.WEB_MY_BACKPACK), true, -1);
		}
	}

	public class gotoRedPacketHelp implements OnClickListener {
		@Override
		public void onClick(View v) {
			ActivityJumpUtil.toWebViewActivity(mActivity, WebConstants.getFullWebMDomain(WebConstants.REDPACKET_HELP_WEB_URL) + "#my", true);
		}
	}


	/**
	 * 弹出对话框
	 */
	private void showGetPhotoDialog() {
		actionSheetDialog = new ActionSheetDialog(this).builder().setCancelable(true).setCanceledOnTouchOutside(true)
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						if (mUploadMsg != null) {
							mUploadMsg.onReceiveValue(null);
							mUploadMsg = null;
						}
						if (mUploadMsgs != null) {
							mUploadMsgs.onReceiveValue(null);
							mUploadMsgs = null;
						}
					}
				}).addSheetItem("拍照", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						takePhoto();
					}
				}).addSheetItem("从手机相册选择", SheetItemColor.BLACK, new OnSheetItemClickListener() {
					@Override
					public void onClick(int which) {
						selectPhoto();
					}
				});
		actionSheetDialog.show();

	}

	// 从本地相册选取图片作为头像
	private void selectPhoto() {
		Intent intentFromGallery = new Intent();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			intentFromGallery.setAction(Intent.ACTION_OPEN_DOCUMENT);
		} else {
			intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
		}
		// 设置文件类型
		intentFromGallery.setType("image/*");
		intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
		this.startActivityForResult(intentFromGallery, REQUEST_ALBUM);
	}

	/**
	 * 启动系统相机
	 */
	private void takePhoto() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		mCameraFile = FileUtil.getCameraPhotoFile();
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraFile));
		startActivityForResult(takePictureIntent, REQUEST_CAMERA);
		overridePendingTransition(R.anim.a_slide_in_down, 0);
	}

	@Override
	public void openFileChooserCallBack(ValueCallback<Uri> uploadMsg, String acceptType) {
		mUploadMsg = uploadMsg;
		showGetPhotoDialog();
	}

	@Override
	public void openFileChooserCallBack(ValueCallback<Uri[]> uploadMsg) {
		mUploadMsgs = uploadMsg;
		showGetPhotoDialog();
	}

	/**
	 * 微信授权+绑定
	 */
	private void weiXinAuthorize() {
		if (mUmShareAPI == null) {
			mUmShareAPI = UMShareAPI.get(this);
		}
		mUmShareAPI.deleteOauth(mActivity, SHARE_MEDIA.WEIXIN, new UMAuthListener() {
			@Override
			public void onStart(SHARE_MEDIA share_media) {

			}

			@Override
			public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
				mUmShareAPI.doOauthVerify(mActivity, SHARE_MEDIA.WEIXIN, new UMAuthListener() {

					@Override
					public void onStart(SHARE_MEDIA share_media) {
						if (moProgress != null && moProgress.isShowing()) {
							return;
						}
						moProgress = Utils.showProgress(mActivity);
					}

					@Override
					public void onComplete(SHARE_MEDIA platform, int i, Map<String, String> value) {
						EvtLog.d(TAG, "onComplete " + value.toString());
						if (mActivity.isFinishing()) {
							return;
						}
						String psOpenId = value.get("openid");
						String psAccessToken = value.get("access_token");
						String expiresIn = value.get("expires_in");
						if (platform == SHARE_MEDIA.WEIXIN) {
							// 获取unionid
							String unionid = value.get("unionid");
							String refreshToken = value.get("refresh_token");
							// 微信绑定
							BusinessUtils.getToBind(mActivity, psAccessToken, psOpenId, refreshToken, unionid,
									expiresIn, new BindCallbackData(WebViewActivity.this));
						}
					}

					@Override
					public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
						showToast("授权失败", Toast.LENGTH_SHORT);
						if (moProgress != null && moProgress.isShowing()) {
							moProgress.dismiss();
						}
					}

					@Override
					public void onCancel(SHARE_MEDIA share_media, int i) {
						showToast("授权取消", Toast.LENGTH_SHORT);
						if (moProgress != null && moProgress.isShowing()) {
							moProgress.dismiss();
						}
					}
				});
			}

			@Override
			public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {

			}

			@Override
			public void onCancel(SHARE_MEDIA share_media, int i) {

			}
		});
	}


	/**
	 * 提供给js调用 ClassName: Contact <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-7-25 上午9:57:04 <br/>
	 *
	 * @version WebViewActivity
	 * @since JDK 1.6
	 */

	public class JsInvokeMainClass {
		// JavaScript调用此方法Login
		@JavascriptInterface
		public void needLogin() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Utils.requestLoginOrRegister(mActivity,
							WebViewActivity.this.getResources().getString(R.string.tip_login_title),
							Constants.REQUEST_CODE_LOGIN);
				}
			});

		}

		@JavascriptInterface
		public void goBack() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					finish();
				}
			});
		}

		//微信支付
		@JavascriptInterface
		public void toPay(final String orderNo) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					OperationHelper.onEvent(FeizaoApp.mContext, "clickWechatModeOfPayment", null);
					if (!msgApi.isWXAppInstalled()) {
						Toast.makeText(getApplicationContext(), R.string.uninstall_weixin_tip, Toast.LENGTH_SHORT)
								.show();
						return;
					}
					showProgressDialog();
					BusinessUtils.getPrePayData(mActivity, new PrePayCallbackData(WebViewActivity.this), orderNo);
				}
			});
		}

		@JavascriptInterface
		public void onShare() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ActivityJumpUtil.toShareActivity(mActivity, mShareInfo);
				}
			});

		}

		@JavascriptInterface
		public void roomDetail(final String rid) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					@SuppressWarnings("unchecked")
					Map<String, Object> lmItem = new HashMap<>();
					lmItem.put("rid", rid);
					ActivityJumpUtil.toLiveMediaPlayerActivity(mActivity, lmItem);
				}
			});
		}

		@JavascriptInterface
		public void toBrowserDownload(final String url) {
			EvtLog.e(TAG, "toBrowserDownload url:" + url);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
				}
			});
		}

		@JavascriptInterface
		public void toWeiXinAuthorize() {
			EvtLog.e(TAG, "toWeiXinAuthorize");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					weiXinAuthorize();
				}
			});
		}

		@JavascriptInterface
		public void toWeiXinBindPublic() {
			EvtLog.e(TAG, "toWeiXinBindPublic");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					PackageUtil.startWeiXin(mActivity);
					// weixinBindPublic();
				}
			});
		}
	}

	/**
	 * 提供给js调用 设置分享信息
	 */
	public class JsInvokeClass {
		// JavaScript调用此方法Login
		@JavascriptInterface
		public void setShareData(final String shareInfo) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						JSONObject shareJson = new JSONObject(shareInfo);
						mShareInfo.put(ShareDialogActivity.Share_Content, shareJson.getString("content"));
						mShareInfo.put(ShareDialogActivity.Share_Img, shareJson.getString("imgLink"));
						mShareInfo.put(ShareDialogActivity.Share_Title, shareJson.getString("title"));
						mShareInfo.put(ShareDialogActivity.Share_Url, shareJson.getString("link"));
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			});
		}

		@JavascriptInterface
		public void userDetail(final String uid) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Map<String, String> personInfo = new HashMap<String, String>();
					personInfo.put("id", uid);
					ActivityJumpUtil.toPersonInfoActivity(mActivity, personInfo, 0);
				}
			});
		}

		/**
		 * 支付宝支付
		 */
		@JavascriptInterface
		public void alipay(final String payId) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showProgressDialog();
					OperationHelper.onEvent(FeizaoApp.mContext, "clickZFBModeOfPayment", null);
					BusinessUtils.getAliPayData(mActivity, new AliPayCallbackData(WebViewActivity.this), payId);
				}
			});
		}

		@JavascriptInterface
		public void bindAUGF5() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showProgressDialog();
					BusinessUtils.getAliPayLoginData(mActivity, new AliAuthCallbackData(WebViewActivity.this));
				}
			});
		}

		@JavascriptInterface
		public void successClose() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setResult(RESULT_OK);
					finish();
				}
			});
		}

		@JavascriptInterface
		public void updateMyInfo(final String userInfo) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						//用来更新用户余额--coin
						//或者更新是否隐身进场
						Map<String, String> data = JSONParser.parseOne(userInfo);
						UserInfoConfig.getInstance().updateFromMap(data);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});
		}

		@JavascriptInterface
		public void mountPreview(String userInfo) {
			try {
				Map<String, String> data = JSONParser.parseOne(userInfo);
				Intent intent = new Intent(WebViewActivity.this, MountPreviewActivity.class);
				for (String key : data.keySet()) {
					intent.putExtra(key, data.get(key));
				}
				WebViewActivity.this.startActivity(intent);
			} catch (JSONException e) {

			}

		}

		@JavascriptInterface
		public void onEffectPreview(String effectInfo) {
			try {
				JSONObject jsonObject = new JSONObject(effectInfo);
				Intent intent = new Intent(WebViewActivity.this, GiftEffectPreviewActivity.class);
				intent.putExtra("androidEffect", jsonObject.getString("androidEffect"));
				intent.putExtra("pname", jsonObject.optString("pname"));
				WebViewActivity.this.startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * 微信预支付回调
	 */
	private static class PrePayCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public PrePayCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "PrePayCallbackData success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_PRE_PAY_SUCCESS;

					msg.obj = JSONParser.parseOne((JSONObject) result);
					BaseFragmentActivity meFragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (meFragment != null)
						meFragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_PRE_PAY_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				BaseFragmentActivity meFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.sendMsg(msg);
			}
		}
	}

	/**
	 * qq预支付回调
	 */
	private static class PreQQPayCallbackData implements CallbackDataHandle {

		private WeakReference<BaseFragmentActivity> mFrgActivity;

		public PreQQPayCallbackData(BaseFragmentActivity activity) {
			mFrgActivity = new WeakReference<>(activity);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_PRE_QQPAY_SUCCESS;

					msg.obj = JSONParser.parseOne((JSONObject) result);
					BaseFragmentActivity meFragment = mFrgActivity.get();
					// 如果fragment未回收，发送消息
					if (meFragment != null)
						meFragment.sendMsg(msg);
				} catch (JSONException e) {
				}
			} else {
				msg.what = MsgTypes.MSG_PRE_QQPAY_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				BaseFragmentActivity meFragment = mFrgActivity.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.sendMsg(msg);
			}
		}
	}

	/**
	 * 支付宝支付回调
	 */
	private static class AliPayCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public AliPayCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "AliPayCallbackData success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_ALI_PAY_SUCCESS;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					BaseFragmentActivity meFragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (meFragment != null)
						meFragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_ALI_PAY_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				BaseFragmentActivity meFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.sendMsg(msg);
			}
		}

	}

	/**
	 * 支付宝授权登录回调
	 */
	private static class AliAuthCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public AliAuthCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "AliAuthCallbackData success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_ALI_LOGIN_SUCCESS;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					BaseFragmentActivity meFragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (meFragment != null)
						meFragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_ALI_LOGIN_FAILED;
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				BaseFragmentActivity meFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.sendMsg(msg);
			}
		}

	}

	/**
	 * 提交支付宝授权信息回调
	 */
	private static class AliAuthCodeCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public AliAuthCodeCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "AliAuthCodeCallbackData success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_ALI_AUTH_CODE_SUCCESS;
					msg.obj = JSONParser.parseOne((JSONObject) result);
					BaseFragmentActivity meFragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (meFragment != null)
						meFragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_ALI_AUTH_CODE_FAILED;
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				BaseFragmentActivity meFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.sendMsg(msg);
			}
		}
	}

	private static class BindCallbackData implements CallbackDataHandle {

		private final WeakReference<BaseFragmentActivity> mFragment;

		public BindCallbackData(BaseFragmentActivity fragment) {
			mFragment = new WeakReference<BaseFragmentActivity>(fragment);
		}

		@Override
		public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
			EvtLog.d(TAG, "BindCallbackData success " + success + " errorCode" + errorCode);
			Message msg = Message.obtain();
			if (success) {
				try {
					msg.what = MsgTypes.MSG_BIND_SUCCESS;
					BaseFragmentActivity meFragment = mFragment.get();
					// 如果fragment未回收，发送消息
					if (meFragment != null)
						meFragment.sendMsg(msg);
				} catch (Exception e) {
				}
			} else {
				msg.what = MsgTypes.MSG_BIND_FAILED;
				if (TextUtils.isEmpty(errorMsg)) {
					errorMsg = Constants.NETWORK_FAIL;
				}
				Bundle bundle = new Bundle();
				bundle.putString("errorCode", errorCode);
				bundle.putString("errorMsg", errorMsg);
				msg.setData(bundle);
				BaseFragmentActivity meFragment = mFragment.get();
				// 如果fragment未回收，发送消息
				if (meFragment != null)
					meFragment.sendMsg(msg);
			}
		}

	}
}
