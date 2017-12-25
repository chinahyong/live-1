/**
 *
 */

package cn.efeizao.feizao.framework.net.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

import android.text.TextUtils;
import cn.efeizao.feizao.framework.lang.IOUtils;
import cn.efeizao.feizao.framework.net.ACommunication;
import cn.efeizao.feizao.framework.net.AEntity;
import com.bixin.bixin.common.pojo.NetConstants;
import cn.efeizao.feizao.framework.net.NetLogs;
import cn.efeizao.feizao.framework.net.proxy.NetProxyInfo;
import cn.efeizao.feizao.framework.net.proxy.NetProxyInfoProxy;

/**
 * Title: XXXX (类或者接口名称) Description: XXXX (简单对此类或接口的名字进行描述) Copyright:
 * Copyright (c) 2012
 * @version 1.0
 */

public class PostCommunication extends ACommunication {

	/**
	 * 默认连接超时时间
	 */
	public static int DEFAULT_CONNECT_TIMEOUT = 15000;
	/**
	 * 默认读取数据超时时间
	 */
	public static int DEFAULT_READ_TIMEOUT = 30000;

	public PostCommunication(AEntity entity) {
		super(entity);
	}

	@Override
	protected void send() throws IOException {
		HttpURLConnection httpconn = null;
		InputStream inPs = null;
		OutputStream outPs = null;
		try {
			URL url = new URL(entity.url);
			NetProxyInfo proxyInfo = NetProxyInfoProxy.getInstance()
					.getNetProxyInfo();
			NetLogs.d_netstep(entity, "PostCommunication", "",
					"Start->openConn");
			if (proxyInfo == null || !TextUtils.isEmpty(proxyInfo.host)) {
				httpconn = (HttpURLConnection) url.openConnection();
			} else {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
						proxyInfo.host, proxyInfo.port));
				httpconn = (HttpURLConnection) url.openConnection(proxy);
			}

			// NetLogs.d_netstep(entity, "PostCommunication", "",
			// "openConn->Send");
			Map<String, String> httpHeaders = entity.postDatas;
			if (httpHeaders != null) {
				// NetLogs.d_netstep(entity, "PostCommunication", "",
				// "openConn->Send:set header");
				for (String key : httpHeaders.keySet()) {
					httpconn.addRequestProperty(key, httpHeaders.get(key));
				}
			}
			// 支持处理网络压缩流
			httpconn.addRequestProperty("Accept-Encoding", "gzip,deflate");
			httpconn.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
			httpconn.setReadTimeout(DEFAULT_READ_TIMEOUT);
			httpconn.setRequestMethod("POST");
			httpconn.setDoOutput(true);
			outPs = httpconn.getOutputStream();
			if (entity.getSendData() != null
					&& !"".equals(entity.getSendData().trim())) {
				byte[] datas = entity.getSendData().getBytes("UTF-8");
				outPs.write(datas, 0, datas.length);
				outPs.flush();
			}
			// NetLogs.d_netstep(entity, "PostCommunication", "", "Send->Get");
			int responseCode = httpconn.getResponseCode();
			entity.http_ResponseCode = responseCode;
			if (responseCode != 200) {
				entity.sentStatus = NetConstants.SENT_STATUS_ERROR_SERVER;
			} else {
				entity.receiveHeaders = httpconn.getHeaderFields();
				inPs = httpconn.getInputStream();
				byte[] bytes = null;
				boolean gzip = false;
				String headerStr = entity.receiveHeaders.toString();
				if (headerStr != null) {
					headerStr = headerStr.toUpperCase();
					if (headerStr.indexOf("CONTENT-ENCODING=[GZIP]") != -1) {
						gzip = true;
					}
				}
				// List<String> contentEncoding =
				// entity.receiveHeaders.get("Content-Encoding") ;
				// if(contentEncoding == null){
				// contentEncoding =
				// entity.receiveHeaders.get("content-encoding");
				// }
				if (/* contentEncoding != null &&
					 * contentEncoding.contains("gzip") */gzip) {
					bytes = IOUtils.getGZipBytes(inPs);
				} else {
					bytes = IOUtils.getByteByStream(inPs);
				}
				entity.receiveData = new String(bytes, "UTF-8");
				entity.decodeReceiveData();
				entity.sentStatus = NetConstants.SENT_STATUS_SUCCESS;
			}
			NetLogs.d_netstep(entity, "PostCommunication", "respcode:"
					+ responseCode, "Get->Finish");
		} finally {
			try {
				if (inPs != null) {
					inPs.close();
					inPs = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (outPs != null) {
					outPs.close();
					outPs = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (httpconn != null) {
				httpconn.disconnect();
				httpconn = null;
			}
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
		} else {
			entity.sentStatus = NetConstants.SENT_STATUS_ERROR_NET;
		}
	}

}
