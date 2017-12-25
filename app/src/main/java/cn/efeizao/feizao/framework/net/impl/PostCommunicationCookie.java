/**
 *
 */

package cn.efeizao.feizao.framework.net.impl;

import android.content.Context;
import android.text.TextUtils;

import com.bixin.bixin.common.Utils;
import com.lonzh.lib.network.HttpSession;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.efeizao.feizao.framework.net.ACommunication;
import cn.efeizao.feizao.framework.net.AEntity;
import com.bixin.bixin.common.pojo.NetConstants;
import cn.efeizao.feizao.framework.net.NetLogs;

/**
 * Title: XXXX (类或者接口名称) Description: XXXX (简单对此类或接口的名字进行描述) Copyright:
 * Copyright (c) 2012
 *
 * @version 1.0
 */

public class PostCommunicationCookie extends ACommunication {

	private Context mContext;

	public PostCommunicationCookie(Context context, AEntity entity) {
		super(entity);
		this.mContext = context.getApplicationContext();
	}

	@Override
	protected void send() throws Exception {

		try {
			NetLogs.d_netstep(entity, "PostCommunication", "",
					"Start->openConn");
			if (!Utils.isNetAvailable(mContext)) {
				throw new IOException("not network available");
			}
			// 1 创建HttpSession
			HttpSession loHttp = HttpSession.getInstance(mContext);
			// 添加头信息
			loHttp.putHead(entity.headers == null ? null : buildPostHeads(entity.headers));
			// 2 会话链接
			HttpResponse response = null;
			if (!TextUtils.isEmpty(entity.jsonStr)) {
				response = loHttp.postWithJSON(entity.url, entity.jsonStr);
			} else {
				response = loHttp.post(entity.url,
						entity.postDatas == null ? null
								: buildPostParams(entity.postDatas));
			}

			if (response.getStatusLine().getStatusCode() == 200) {
				// 3 解析结果
				String strResponse = HttpSession.readContent(response);
				entity.receiveData = strResponse;
				entity.decodeReceiveData();
				entity.sentStatus = NetConstants.SENT_STATUS_SUCCESS;
			} else {
				entity.sentStatus = NetConstants.SENT_STATUS_ERROR_NET;
			}
			NetLogs.d_netstep(entity, "PostCommunication", "respcode:"
					+ entity.sentStatus, "Get->Finish");
		} finally {
		}
	}

	@Override
	protected void catchException(Exception ex) {
		ex.printStackTrace();
		String msg = ex.getMessage();
		if (msg != null && msg.indexOf("Connection timed out") > -1) {
			entity.sentStatus = NetConstants.SENT_STATUS_TIMEOUT;
		} else if (msg != null && msg.indexOf("Connection refused") > -1) {
			entity.sentStatus = NetConstants.SENT_STATUS_ERROR_SERVER;
		} else if (ex instanceof UnknownHostException) {
			entity.sentStatus = NetConstants.SENT_STATUS_DNS_ERROR;
		} else {
			entity.sentStatus = NetConstants.SENT_STATUS_ERROR_NET;
		}
	}

	private List<NameValuePair> buildPostParams(Map<String, String> pmParams) {
		List<NameValuePair> llParams = new ArrayList<NameValuePair>();
		Iterator<Entry<String, String>> loIterator = pmParams.entrySet()
				.iterator();
		while (loIterator.hasNext()) {
			Entry<String, String> loEntry = loIterator.next();
			NameValuePair loPair = new BasicNameValuePair(loEntry.getKey(),
					loEntry.getValue());
			llParams.add(loPair);
		}
		return llParams;
	}

	private List<BasicHeader> buildPostHeads(Map<String, String> pmParams) {
		List<BasicHeader> llParams = new ArrayList<>();
		Iterator<Entry<String, String>> loIterator = pmParams.entrySet()
				.iterator();
		while (loIterator.hasNext()) {
			Entry<String, String> loEntry = loIterator.next();
			BasicHeader loPair = new BasicHeader(loEntry.getKey(),
					loEntry.getValue());
			llParams.add(loPair);
		}
		return llParams;
	}

}
