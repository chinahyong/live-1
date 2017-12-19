package cn.efeizao.feizao.framework.net;

import tv.live.bx.FeizaoApp;
import tv.live.bx.util.ChannelUtil;
import tv.live.bx.common.Constants;
import tv.live.bx.library.util.PackageUtil;
import tv.live.bx.library.util.TelephoneUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Live 2014年9月3日
 * @version 1.0
 * @Description:横幅广告
 */
public class BaseEntityImpl extends BaseEntity {

	private String baseUrl;

	final String ANDROID = "android";

	/**
	 * 应用版本名称
	 */
	protected String appVersion = PackageUtil.getVersionName();

	/**
	 * 平台，android或iso
	 */
	protected String platform = ANDROID;

	public BaseEntityImpl(IReceiverListener receiverListener, String url) {
		super(receiverListener);
		this.baseUrl = url;
		buildUrl();
	}

	@Override
	protected void init() { // 该方法在父类的构造方法中调用，不能用于初始化url参数
	}

	@Override
	protected void initHttpHeader() {
		// add something into the HTTP request head.
	}

	protected void buildUrl() {
		StringBuilder sb = new StringBuilder(baseUrl);
		try {
			if (sb.indexOf("?") == -1) {
				sb.append("?");
			} else {
				sb.append("&");
			}
			sb.append("version=").append(appVersion)
					.append("&platform=").append(platform)
					.append("&packageId=").append(Constants.PACKAGE_ID)
					.append("&channel=").append(ChannelUtil.getChannel(FeizaoApp.mContext))
					.append("&deviceName=").append(URLEncoder.encode(TelephoneUtil.getDeviceName(), "UTF-8"))
					.append("&androidVersion=").append(TelephoneUtil.getAndridVersion());

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		url = sb.toString();
	}

	@Override
	public void decodeReceiveData() {
	}

	/**
	 * http post请求数据
	 */
	public void setSendData(String data) {
		this.sendData = data;
	}
}
