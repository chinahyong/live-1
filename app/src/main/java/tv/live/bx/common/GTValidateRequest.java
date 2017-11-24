package tv.live.bx.common;

import android.app.Activity;

import tv.live.bx.config.UserInfoConfig;
import tv.live.bx.library.util.EvtLog;
import com.example.sdk.GT3GeetestUtils;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Live on 2017/6/21.
 * description : 使用极验验证请求，操作是否频繁，存在自动刷请求
 */

public class GTValidateRequest {
	private static GTValidateRequest gtValidateRequest = null;
	private static boolean isCloseFlag = true;        //如果dialog被关闭则为true，否则为false

	public static GTValidateRequest getInstance() {
		if (gtValidateRequest == null) {
			gtValidateRequest = new GTValidateRequest();
			isCloseFlag = true;
		}
		return gtValidateRequest;
	}

	/**
	 * @param context 上下文activity，用于第三方sdk使用（第三方仅支持activity作为上下文对象）
	 */
	public void validate(WeakReference<Activity> context) {
		EvtLog.e("validate", "activity:" + context.get().getComponentName().getClassName());
		// dialog已经关闭
		if (isCloseFlag) {
			isCloseFlag = false;
			GT3GeetestUtils geetestUtils = new GT3GeetestUtils(context.get());
			geetestUtils.setGtListener(new GTListener());
			geetestUtils.getGeetest();
		}
	}

	/**
	 * 验证回调
	 */
	private static class GTListener implements GT3GeetestUtils.GT3Listener {
		private Map<String, String> data = new HashMap<>();

		// dialog关闭
		@Override
		public void gt3CloseDialog() {
			isCloseFlag = true;
			EvtLog.e("validate", "gt3CloseDialog" + isCloseFlag);
		}

		// 点击其他，关闭dialog
		@Override
		public void gt3CancelDialog() {
			isCloseFlag = true;
			EvtLog.e("validate", "gt3CancelDialog" + isCloseFlag);
		}

		// 验证码加载准备完成
		@Override
		public void gt3DialogReady() {
			EvtLog.e("validate", "gt3DialogReady" + isCloseFlag);
		}

		// 首次请求返回结果,将getCode拿到的两个数据，在发送验证的时候带回去
		// 因为sdk不支持添加cookie，所以用此方法来通知后台是否为同一用户
		@Override
		public void gt3FirstResult(JSONObject jsonObject) {
			EvtLog.e("validate", "gt3FirstResult" + isCloseFlag);
			try {
				data.put("captcha_uid", jsonObject.getString("captcha_uid"));
				data.put("status", jsonObject.getString("status"));
				data.put("uid", UserInfoConfig.getInstance().id);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 二次请求返回结果
		@Override
		public Map<String, String> gt3SecondResult() {
			EvtLog.e("validate", "gt3SecondResult" + isCloseFlag);
			return data;
		}

		@Override
		public void gt3GetDialogResult(String result) {
			EvtLog.e("validate", "gt3GetDialogResult" + isCloseFlag);
		}

		// 验证码 验证成功
		@Override
		public void gt3DialogSuccess() {
			isCloseFlag = true;
			EvtLog.e("validate", "gt3DialogSuccess" + isCloseFlag);
		}

		// 验证码 验证失败
		@Override
		public void gt3DialogOnError() {
			isCloseFlag = true;
			EvtLog.e("validate", "gt3DialogOnError" + isCloseFlag);
		}

		@Override
		public void gt3DialogSuccessResult(String result) {
			EvtLog.e("validate", "gt3DialogSuccessResult" + isCloseFlag);
		}

		@Override
		public Map<String, String> captchaHeaders() {
			EvtLog.e("validate", "captchaHeaders" + isCloseFlag);
			// 此方法只是将cookie相关数据加到header并没有加到cookie，依然会造成后台生成新的seesionid
			// 处理方案，后台返回一个唯一标识，在验证时返回 回去
//			LZCookieStore cookieStore = new LZCookieStore(FeizaoApp.mConctext);
//			List<Cookie> cookies = cookieStore.getCookies();
//			Map<String, String> data = new HashMap<>();
//			if (cookies != null) {
//				for (Cookie cookie :
//						cookies) {
//					data.put(cookie.getName(), cookie.getValue());
//				}
//			}
//			return data;
			return null;
		}
	}
}
