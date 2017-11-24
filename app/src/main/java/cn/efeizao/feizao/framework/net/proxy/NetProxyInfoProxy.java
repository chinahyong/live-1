/**
 * 
 */
package cn.efeizao.feizao.framework.net.proxy;




/**
 * 代理网关获取代理
 * 
 * @author duminghui
 * 
 */
public class NetProxyInfoProxy {
	private final static NetProxyInfoProxy instance = new NetProxyInfoProxy();
	private ANetProxyInfoFactory factory;

	private NetProxyInfoProxy() {

	}

	public final static NetProxyInfoProxy getInstance() {
		return instance;
	}

	public final void setNetProxyInfoFactory(ANetProxyInfoFactory factory) {
		this.factory = factory;
	}

	public final NetProxyInfo getNetProxyInfo() {

//		NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
//				.getActiveNetworkInfo();

	//	if (networkInfo.getType() == android.net.ConnectivityManager.TYPE_MOBILE) {
			// 获取默认代理主机ip
			String host = android.net.Proxy.getDefaultHost();
			// 获取端口
			int port = android.net.Proxy.getDefaultPort();
			if (host != null && port != -1) {
				// line += "/nhost[" + host + "] port[" + port + "]";
				// 封装代理連接主机IP与端口号。
				// InetSocketAddress inetAddress = new InetSocketAddress(host,
				// port);
				// // 根据URL链接获取代理类型，本链接适用于TYPE.HTTP
				// java.net.Proxy.Type proxyType =
				// java.net.Proxy.Type.valueOf(url
				// .getProtocol().toUpperCase());
				// java.net.Proxy javaProxy = new java.net.Proxy(proxyType,
				// inetAddress);
				//
				// httpconn = (HttpURLConnection) url.openConnection(javaProxy);

				
				
//				return factory.getNetProxyInfo();
			//	Log.i("liaoguang", "cnwap访问");
				NetProxyInfo info = new NetProxyInfo();
				info.port=port;
				info.host=host;
				
				return info;
			} 
				//else {
////
//			}
//		} else {
//
//		}

		 return null;

	}
}
