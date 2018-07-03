package com.bixin.bixin.common;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;

import com.bixin.bixin.App;
import com.bixin.bixin.common.model.HttpConstants;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.PackageUtil;
import com.bixin.bixin.util.ChannelUtil;
import com.lonzh.lib.LZActivity;
import com.lonzh.lib.network.HttpSession;
import com.lonzh.lib.network.JSONParser;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * 此类以后不再使用
 */
public class Business {

	final static String ANDROID = "android";

	/**
	 * 应用版本名称
	 */
	protected static String appVersion = PackageUtil.getVersionName();

	/**
	 * 平台，android或iso
	 */
	protected static String platform = ANDROID;

	public static final int HTTP_METHOD_GET = 0, HTTP_METHOD_POST = 1;
	public static final int RESPONSE_TYPE_NULL = 0, RESPONSE_TYPE_SINGLE = 1, RESPONSE_TYPE_MULTI = 2,
			RESPONSE_TYPE_MULTI_IN_SINGLE = 3, RESPONSE_TYPE_MULTI_IN_MULTI = 4, RESPONSE_TYPE_SINGLE_IN_MULTI = 5;

	/**
	 * 用户注册发送验证码
	 *
	 * @param poContext
	 * @param psMobile
	 */
	public static void sendRegisterVerifyCode(Context poContext, String psMobile) throws InvalidKeyException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {
		Map<String, String> lmParams = new HashMap<String, String>();
//		lmParams.put("mobile", psMobile);
		//加密手机号
		lmParams.put("mobile", Utils.rsaEncrypt(
				(String) App.getCacheData("public_key"), psMobile));
		String lsUrl = "user/getMobileRegisterVCode2";
		ErrCodeParser loErrCodeParser = new ErrCodeParser() {
			@Override
			public String getErrMsg(int piErrCode) {
				String lsMsg;
				switch (piErrCode) {
					case 400:
						lsMsg = "手机号码格式不正确";
						break;
					case 401:
						lsMsg = "短信发送失败";
						break;
					case -200:
						lsMsg = "操作过于频繁";
						break;
					default:
						lsMsg = "错误码：" + piErrCode;
						break;
				}
				return lsMsg;
			}
		};
		initHttpPamas(lmParams);
		//使用POST请求
		newBusiness(poContext, lsUrl, HTTP_METHOD_POST, lmParams, RESPONSE_TYPE_NULL, null,
				MsgTypes.SEND_REGISTER_VERIFY_CODE_SUCCESS, MsgTypes.SEND_REGISTER_VERIFY_CODE_FAILED, loErrCodeParser);
	}

	/**
	 * 修改密码发送验证码
	 *
	 * @param poContext
	 * @param psMobile
	 */
	public static void sendModifyVerifyCode(Context poContext, String psMobile) throws InvalidKeyException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {
		Map<String, String> lmParams = new HashMap<String, String>();
//		lmParams.put("mobile", psMobile);
		//手机加密
		lmParams.put("mobile", Utils.rsaEncrypt(
				(String) App.getCacheData("public_key"), psMobile));
		String lsUrl = "user/getMobileModifyVCode2";
		ErrCodeParser loErrCodeParser = new ErrCodeParser() {
			@Override
			public String getErrMsg(int piErrCode) {
				String lsMsg;
				switch (piErrCode) {
					case 400:
						lsMsg = "手机号码格式不正确";
						break;
					case 401:
						lsMsg = "短信发送失败";
						break;
					case -200:
						lsMsg = "操作过于频繁";
						break;
					default:
						lsMsg = "错误码：" + piErrCode;
						break;
				}
				return lsMsg;
			}
		};
		initHttpPamas(lmParams);
		//使用POST请求
		newBusiness(poContext, lsUrl, HTTP_METHOD_POST, lmParams, RESPONSE_TYPE_NULL, null,
				MsgTypes.SEND_REGISTER_VERIFY_CODE_SUCCESS, MsgTypes.SEND_REGISTER_VERIFY_CODE_FAILED, loErrCodeParser);
	}

	/**
	 * 校验注册验证码
	 *
	 * @param psVerifyCode
	 */
	public static void checkRegisterVerifyCode(Context poContext, String psVerifyCode) {
		String lsUrl = "user/checkMobileVCode";
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("vcode", psVerifyCode);
		ErrCodeParser loErrCodeParser = new ErrCodeParser() {
			@Override
			public String getErrMsg(int piErrCode) {
				String lsMsg;
				switch (piErrCode) {
					case 500:
						lsMsg = "验证码不正确";
						break;
					case 501:
						lsMsg = "手机号码信息丢失";
						break;
					default:
						lsMsg = "错误码：" + piErrCode;
						break;
				}
				return lsMsg;
			}
		};
		initHttpPamas(lmParams);
		newBusiness(poContext, lsUrl, HTTP_METHOD_GET, lmParams, RESPONSE_TYPE_NULL, null,
				MsgTypes.CHECK_REGISTER_VERIFY_CODE_SUCCESS, MsgTypes.CHECK_REGISTER_VERIFY_CODE_FAILED,
				loErrCodeParser);
	}

	/**
	 * 用户注册
	 *
	 * @param psPassword
	 * @param psNickname
	 * @param piGender
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static void register(Context poContext, String psPassword, String psNickname, String inviteId, int piGender)
			throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("password", Utils.rsaEncrypt(
				(String) App.getCacheData("public_key"), psPassword));
		lmParams.put("nickname", psNickname);
		if (!TextUtils.isEmpty(inviteId)) {
			lmParams.put("referrer", inviteId);
		}
		lmParams.put("sex", String.valueOf(piGender));
		String lsUrl = "user/mobileRegister";
		ErrCodeParser loErrCodeParser = new ErrCodeParser() {
			@Override
			public String getErrMsg(int piErrCode) {
				String lsMsg;
				switch (piErrCode) {
					case 600:
						lsMsg = "会话失效";
						break;
					case 601:
						lsMsg = "解密失败";
						break;
					case 602:
						lsMsg = "注册失败";
						break;
					default:
						lsMsg = "错误码：" + piErrCode;
						break;
				}
				return lsMsg;
			}
		};
		initHttpPamas(lmParams);
		newBusiness(poContext, lsUrl, HTTP_METHOD_POST, lmParams, RESPONSE_TYPE_NULL, null, MsgTypes.REGISTER_SUCCESS,
				MsgTypes.REGISTER_FAILED, loErrCodeParser);
	}

	/**
	 * 反馈意见
	 *
	 * @param poContext
	 * @param psContact
	 * @param psContent
	 */
	public static void feedback(Context poContext, String psContact, String psContent, String deviceName,
								String androidVersionName) {
		EvtLog.d("", "feedback deviceName:" + deviceName + ",androidVersionName:" + androidVersionName);
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("content", psContent);
		lmParams.put("contactWay", psContact);
		lmParams.put("device", deviceName);
		lmParams.put("system", androidVersionName);
		String lsUrl = "app/feedback";
		ErrCodeParser loErrCodeParser = new ErrCodeParser() {
			@Override
			public String getErrMsg(int piErrCode) {
				String lsMsg;
				switch (piErrCode) {
					case 100:
						lsMsg = "操作过于频繁";
						break;
					case 101:
						lsMsg = "保存失败";
						break;
					default:
						lsMsg = "错误码：" + piErrCode;
						break;
				}
				return lsMsg;
			}
		};
		initHttpPamas(lmParams);
		newBusiness(poContext, lsUrl, HTTP_METHOD_POST, lmParams, RESPONSE_TYPE_NULL, null, MsgTypes.FEEDBACK_SUCCESS,
				MsgTypes.FEEDBACK_FAILED, loErrCodeParser);
	}

	/**
	 * 找回登录密码
	 *
	 * @param poContext
	 * @param psNewPwd
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public static void getBackLogPwd(Context poContext, String psNewPwd) throws InvalidKeyException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException {
		Map<String, String> lmParams = new HashMap<String, String>();
		lmParams.put("newPassword", Utils.rsaEncrypt(
				(String) App.getCacheData("public_key"), psNewPwd));
		String lsUrl = "user/modifyPassword";
		ErrCodeParser loErrCodeParser = new ErrCodeParser() {
			@Override
			public String getErrMsg(int piErrCode) {
				String lsMsg;
				switch (piErrCode) {
					case 900:
						lsMsg = "手机号码丢失";
						break;
					case 901:
						lsMsg = "手机未验证";
						break;
					case 902:
						lsMsg = "揭秘失败";
						break;
					case 903:
						lsMsg = "手机号未注册";
						break;
					case 904:
						lsMsg = "更新失败";
						break;
					default:
						lsMsg = "错误码：" + piErrCode;
						break;
				}
				return lsMsg;
			}
		};
		initHttpPamas(lmParams);
		newBusiness(poContext, lsUrl, HTTP_METHOD_POST, lmParams, RESPONSE_TYPE_NULL, null,
				MsgTypes.GETBACK_LOGIN_PWD_SUCCESS, MsgTypes.GETBACK_LOGIN_PWD_FAILED, loErrCodeParser);
	}

	private static void initHttpPamas(Map<String, String> httpPamas) {
		httpPamas.put("version", appVersion);
		httpPamas.put("platform", platform);
		httpPamas.put("packageId", Constants.PACKAGE_ID);
		httpPamas.put("channel", ChannelUtil.getChannel(App.mContext));
//		httpPamas.put("deviceId", TelephoneUtil.getDeviceImei(App.mContext));
	}

	/**
	 * 业务连接
	 *
	 * @param poContext
	 * @param psUrl
	 * @param piHttpMethod
	 * @param pmParams
	 * @param piResultType
	 * @param paMultiKeys
	 * @param piSuccessMsg
	 * @param piFailedMsg
	 * @param poErrParser
	 */
	private static void newBusiness(final Context poContext, final String psUrl, final int piHttpMethod,
									final Map<String, String> pmParams, final int piResultType, final String[] paMultiKeys,
									final int piSuccessMsg, final int piFailedMsg, final ErrCodeParser poErrParser) {
		AsyncTaskThreadPool.createTaskDistributor().execute(new Runnable() {

			@Override
			public void run() {
				// 1 创建HttpSession
				HttpSession loHttp = HttpSession.getInstance(poContext);
				Message loMsg = new Message();
				loMsg.what = piFailedMsg;
				if (!Utils.isNetAvailable(poContext)) {
					loMsg.obj = "网络不给力";
					LZActivity.getHandler().sendMessage(loMsg);
					return;
				}

				try {
					// 2 会话链接
					HttpResponse loResponse;
					if (piHttpMethod == HTTP_METHOD_GET) {
						String lsAbsUrl = HttpConstants.BASE_URL_SERVER + "/" + psUrl;
						if (pmParams != null)
							lsAbsUrl += "?" + buildGetParams(pmParams);
						loResponse = loHttp.get(lsAbsUrl);
						EvtLog.d("", lsAbsUrl);
					} else if (piHttpMethod == HTTP_METHOD_POST) {
						loResponse = loHttp.post(HttpConstants.BASE_URL_SERVER + "/" + psUrl, pmParams == null ? null
								: buildPostParams(pmParams));
					} else
						return;

					// 3 解析结果
					String lsResponse = HttpSession.readContent(loResponse);
					EvtLog.d("Business", "psUrl:" + psUrl + "," + lsResponse);
					// 3.1 预解析
					Map<String, Object> lmPreResult = Utils.parseResponseHead(piResultType, lsResponse);
					// 3.2 解析内容
					if ((Boolean) lmPreResult.get("success")) {
						loMsg.what = piSuccessMsg;
						switch (piResultType) {
							case RESPONSE_TYPE_SINGLE:
								loMsg.obj = JSONParser.parseOne((JSONObject) lmPreResult.get("result"));
								break;
							case RESPONSE_TYPE_MULTI:
								loMsg.obj = JSONParser.parseMulti((JSONArray) lmPreResult.get("result"));
								break;
							case RESPONSE_TYPE_MULTI_IN_SINGLE:
								loMsg.obj = JSONParser.parseMultiInSingle((JSONObject) lmPreResult.get("result"),
										paMultiKeys);
								break;
							case RESPONSE_TYPE_MULTI_IN_MULTI:
								loMsg.obj = JSONParser.parseMultiInMulti((JSONArray) lmPreResult.get("result"), paMultiKeys);
								break;
							case RESPONSE_TYPE_SINGLE_IN_MULTI:
								loMsg.obj = JSONParser.parseSingleInMulti((JSONArray) lmPreResult.get("result"),
										paMultiKeys);
								break;
						}
					} else {
						loMsg.what = piFailedMsg;
						// loMsg.obj = poErrParser == null ? lmPreResult
						// .get("msg") : poErrParser
						// .getErrMsg((Integer) lmPreResult.get("errno"));
						loMsg.obj = lmPreResult.get("msg");
					}
				} catch (IOException e) {
					e.printStackTrace();
					loMsg.obj = "网络不给力";
				} catch (URISyntaxException e) {
					e.printStackTrace();
					loMsg.obj = "网络不给力";
				} catch (JSONException e) {
					e.printStackTrace();
					loMsg.obj = "服务器返回格式错误";
				} catch (Exception e) {
					e.printStackTrace();
					loMsg.obj = "服务器返回格式错误";
				}
				LZActivity.getHandler().sendMessage(loMsg);
			}
		});
	}

	private static String buildGetParams(Map<String, String> pmParams) {
		Iterator<Entry<String, String>> loIterator = pmParams.entrySet().iterator();
		String lsParams = "";
		while (loIterator.hasNext()) {
			Entry<String, String> loEntry = loIterator.next();
			lsParams += loEntry.getKey() + "=" + loEntry.getValue() + "&";
		}
		return lsParams.substring(0, lsParams.length() - 1);
	}

	private static List<NameValuePair> buildPostParams(Map<String, String> pmParams) {
		List<NameValuePair> llParams = new ArrayList<NameValuePair>();
		Iterator<Entry<String, String>> loIterator = pmParams.entrySet().iterator();
		while (loIterator.hasNext()) {
			Entry<String, String> loEntry = loIterator.next();
			NameValuePair loPair = new BasicNameValuePair(loEntry.getKey(), loEntry.getValue());
			llParams.add(loPair);
		}
		return llParams;
	}

	private interface ErrCodeParser {
		String getErrMsg(int piErrCode);
	}
}
