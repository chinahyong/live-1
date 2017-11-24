package com.lonzh.lib.network;

import android.content.Context;

import tv.live.bx.library.util.EvtLog;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpSession {
	public static final int HTTP_CONN_TIMEOUT = 15000;
	public static final int HTTP_READ_TIMEOUT = 15000;

	private static HttpSession moSessionInstance;

	private DefaultHttpClient moClient;
	private HttpPost moPost;
	private HttpGet moGet;
	private HttpPut moPut;
	private HttpDelete moDelete;
	private CookieStore moCookieStore;

	private HttpSession(int piConnTimeout, int piReadTimeout, Context poContext) {
		BasicHttpParams loHttpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(loHttpParams, piConnTimeout);
		HttpConnectionParams.setSoTimeout(loHttpParams, piReadTimeout);
		List<Header> loHeaders = new ArrayList<Header>();
		loHeaders.add(new BasicHeader("User-Agent", "mobile"));
		loHttpParams.setParameter("http.default-headers", loHeaders);
		moClient = new DefaultHttpClient(loHttpParams);
		moCookieStore = new LZCookieStore(poContext);
		moClient.setCookieStore(moCookieStore);
		moGet = new HttpGet();
		moPost = new HttpPost();
		moPut = new HttpPut();
		moDelete = new HttpDelete();
	}

	public static HttpSession getInstance(Context poContext) {
//		if (moSessionInstance == null) {
//			synchronized (HttpSession.class) {
//				if (moSessionInstance == null) {
//					moSessionInstance = new HttpSession(HTTP_CONN_TIMEOUT, HTTP_READ_TIMEOUT, poContext);
//				}
//			}
//		}
		return new HttpSession(HTTP_CONN_TIMEOUT, HTTP_READ_TIMEOUT, poContext);
	}

	public CookieStore getCookieStore() {
		return moCookieStore;
	}

	public String getCookie(String psKey) {
		String lsValue = null;
		if (moClient != null) {
			lsValue = ((LZCookieStore) moClient.getCookieStore()).getCookie(psKey);
		}
		return lsValue;
	}

	public Cookie getCookieObj(String psKey) {
		return ((LZCookieStore) moClient.getCookieStore()).getCookieObj(psKey);
	}

	public void clearCookies() {
		moClient.getCookieStore().clear();
	}

	public HttpResponse get(String psURI) throws URISyntaxException, IOException {
		moGet.setURI(new URI(psURI));
		return moClient.execute(moGet);
	}

	public void putHead(List<BasicHeader> params) throws IOException, URISyntaxException {
		if (params == null) {
			return;
		}
		for (BasicHeader header : params) {
			moPost.setHeader(header);
		}
	}

	public HttpResponse post(String psURI, List<NameValuePair> poParams) throws IOException, URISyntaxException {
		moPost.setURI(new URI(psURI));
//		for (Cookie cookie : moCookieStore.getCookies()) {
//			moPost.addHeader(new BasicHeader("Cookie", cookie.toString()));
//		}

		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		for (int index = 0; index < poParams.size(); index++) {
			EvtLog.d("httpParam", poParams.get(index).getName() + "=" + poParams.get(index).getValue());
			if (poParams.get(index).getValue().endsWith(".jpg") || poParams.get(index).getValue().endsWith(".png")
					|| poParams.get(index).getValue().endsWith(".mp4")) {
				// If the key equals to "image", we use FileBody to transfer the
				// data
				entity.addPart(poParams.get(index).getName(), new FileBody(new File(poParams.get(index).getValue())));
			} else if (poParams.get(index).getName().startsWith("pic_")) {
				entity.addPart(poParams.get(index).getName(), new FileBody(new File(poParams.get(index).getValue())));
			} else {
				// Normal string data
				entity.addPart(poParams.get(index).getName(),
						new StringBody(poParams.get(index).getValue(), Charset.forName("UTF-8")));
			}
		}
		if (entity != null)
			moPost.setEntity(entity);
		HttpResponse httpResponse = moClient.execute(moPost);
//		Header headers[] = httpResponse.getHeaders("Set-Cookie");
//		for (Header header : headers) {
//			String[] str = header.getValue().split(";");
//			moCookieStore.addCookie(new BasicClientCookie("PHPSESSID",str.));
//		}
		return httpResponse;
	}

	public HttpResponse postWithJSON(String url, String json) throws Exception {
		moPost.setURI(new URI(url));
		// 将JSON进行UTF-8编码,以便传输中文
//		String encoderJson = URLEncoder.encode(json, HTTP.UTF_8);
//		moPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
		StringEntity entity = new StringEntity(json);
//		entity.setContentType("text/plain;charset=UTF-8");
		entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/octet-stream"));
		if (entity != null)
			moPost.setEntity(entity);
		return moClient.execute(moPost);
	}

	public HttpResponse put(String psURI, List<NameValuePair> poParams) throws URISyntaxException, IOException {
		moPut.setURI(new URI(psURI));
		if (poParams != null)
			moPut.setEntity(new UrlEncodedFormEntity(poParams, "UTF-8"));
		return moClient.execute(moPut);
	}

	public HttpResponse delete(String psURI) throws URISyntaxException, IOException {
		moDelete.setURI(new URI(psURI));
		return moClient.execute(moDelete);
	}

	public HttpResponse upload(String psUrl, Map<String, Object> poParams) throws IOException, URISyntaxException {
		MultipartEntity moEntity = new MultipartEntity();
		Iterator<Entry<String, Object>> loIterator = poParams.entrySet().iterator();
		while (loIterator.hasNext()) {
			Entry<String, Object> loEntry = loIterator.next();
			String lsKey = loEntry.getKey();
			if (lsKey.equals("file")) {
				File loFile = new File((String) loEntry.getValue());
				moEntity.addPart("file", new FileBody(loFile));
			} else if (lsKey.equals("file_stream"))
				moEntity.addPart("file", new InputStreamBody((InputStream) loEntry.getValue(), "tmp.jpg"));
			else
				moEntity.addPart(lsKey, new StringBody((String) loEntry.getValue(), Charset.forName("UTF-8")));
		}
		moPost.setURI(new URI(psUrl));
		moPost.setEntity(moEntity);
		return moClient.execute(moPost);
	}

	public void download(String psUrl, String psTarget) throws URISyntaxException, IOException {
		moGet.setURI(new URI(psUrl));
		HttpResponse loResponse = moClient.execute(moGet);
		if (HttpStatus.SC_OK == loResponse.getStatusLine().getStatusCode()) {
			HttpEntity loEntity = loResponse.getEntity();
			if (loEntity != null) {
				File loFile = new File(psTarget);
				FileOutputStream loOutput = new FileOutputStream(loFile);
				InputStream loInput = loEntity.getContent();
				byte[] laBytes = new byte[1024];
				int n;
				while ((n = loInput.read(laBytes)) != -1) {
					loOutput.write(laBytes, 0, n);
				}
				loOutput.flush();
				loOutput.close();
				loInput.close();
				loEntity.consumeContent();
			}
		}
	}

	public void releaseGet() {
		// moGet.abort();
	}

	public void releasePost() {
		// moPost.abort();
	}

	public void releasePut() {
		// moPut.abort();
	}

	public void releaseDelete() {
		// moDelete.abort();
	}

	public static String readContent(HttpResponse poResponse) throws IOException {
		InputStream loInput = poResponse.getEntity().getContent();
		BufferedReader loReader = new BufferedReader(new InputStreamReader(loInput));
		String lsContent = "", lsBuf;
		while ((lsBuf = loReader.readLine()) != null) {
			lsContent += lsBuf + "\n";
		}
		loReader.close();
		return lsContent;
	}

}
