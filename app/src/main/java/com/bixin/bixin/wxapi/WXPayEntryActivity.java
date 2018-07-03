package com.bixin.bixin.wxapi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.bixin.bixin.activities.WebViewActivity;
import com.bixin.bixin.common.model.WebConstants;
import com.bixin.bixin.common.model.LibConstants;
import com.bixin.bixin.library.util.EvtLog;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

	private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";

	private IWXAPI api;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		api = WXAPIFactory.createWXAPI(this, LibConstants.WEIXIN_APPID);
		api.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@SuppressLint("NewApi")
	@Override
	public void onResp(BaseResp resp) {
		EvtLog.e(TAG, "onPayFinish, errCode = " + resp.errCode);
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			if (resp.errCode == 0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("结果");
				builder.setMessage("支付成功！");
				builder.setNeutralButton("确定", new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						Map<String, String> webInfo = new HashMap<String, String>();
						webInfo.put(WebViewActivity.URL, WebConstants.getFullWebMDomain(WebConstants.RECHARGE_WEB_URL));
						webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(true));
						Intent intent = new Intent(WXPayEntryActivity.this, WebViewActivity.class);
						intent.putExtra(WebViewActivity.WX_PAY_SUCCESS, true);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						intent.putExtra(WebViewActivity.WEB_INFO, (Serializable) webInfo);
						WXPayEntryActivity.this.startActivity(intent);
						finish();
					}
				});
				builder.setCancelable(false);
				builder.show();
			} else {
				Toast.makeText(WXPayEntryActivity.this, "微信支付失败", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
}