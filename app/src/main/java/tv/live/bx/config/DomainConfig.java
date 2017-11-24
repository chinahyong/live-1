package tv.live.bx.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import tv.live.bx.FeizaoApp;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Consts;
import tv.live.bx.library.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by BYC on 2017/6/13.
 */

public class DomainConfig {
	//单例
	private static volatile DomainConfig instance;
	public String base_http_domain;
	public String base_domain;
	public String base_stat_domain;
	public String base_m_domain;
	public String http_domain_lists;

	/**
	 * 获取config单例
	 *
	 * @return
	 */
	public static DomainConfig getInstance() {
		if (instance == null) {
			synchronized (DomainConfig.class) {
				if (instance == null) {
					instance = readFromFile();
				}
			}
		}
		return instance;
	}

	/**
	 * 从sp中读取数据
	 *
	 * @return
	 */
	private static DomainConfig readFromFile() {
		DomainConfig config = new DomainConfig();
		SharedPreferences sp = FeizaoApp.mConctext.getSharedPreferences(Constants.COMMON_SF_HTTP_DOMAIN_NAME, Context.MODE_PRIVATE);
		config.base_domain = sp.getString(Constants.COMMON_SF_BASE_DOMAIN, Consts.BASE_DOMAIN);
		config.base_stat_domain = sp.getString(Constants.COMMON_SF_BASE_STAT_DOMAIN, Consts.BASE_STAT_DOMAIN);
		config.base_m_domain = sp.getString(Constants.COMMON_SF_BASE_M_DOMAIN, Consts.BASE_M_DOMAIN);
		config.base_http_domain = sp.getString(Constants.COMMON_SF_BASE_HTTP_DOMAIN, Consts.BASE_HTTP_DOMAIN);
		config.http_domain_lists = sp.getString(Constants.COMMON_SF_BASE_DOMAIN_LIST, Consts.BASE_HTTP_DOMAIN_BAK);

		Consts.BASE_DOMAIN = config.base_domain;
		Consts.BASE_STAT_DOMAIN = config.base_stat_domain;
		Consts.BASE_M_DOMAIN = config.base_m_domain;
		Consts.BASE_HTTP_DOMAIN = config.base_http_domain;
		Consts.updateBaseDomain();
		return config;
	}

	/**
	 * 更新当前可使用的安全的http地址
	 */
	public void updateSafeHttpDomain(String httpDomain) {
		if (!TextUtils.isEmpty(httpDomain) && !base_http_domain.equals(httpDomain)) {
			Consts.BASE_HTTP_DOMAIN = base_http_domain = httpDomain;
			SharedPreferences.Editor editor = FeizaoApp.mConctext.getSharedPreferences(Constants.COMMON_SF_HTTP_DOMAIN_NAME, Context.MODE_PRIVATE).edit();
			editor.putString(Constants.COMMON_SF_BASE_HTTP_DOMAIN, httpDomain);
			editor.commit();
		}
	}

	/**
	 * 更新stat_domain-- 上报日志域名
	 */
	public void updateSafeStatDomain(String statDomain) {
		if(!TextUtils.isEmpty(statDomain) && !base_stat_domain.equals(statDomain)){
			Consts.BASE_STAT_DOMAIN = base_stat_domain = statDomain;
			Consts.updateBaseDomain();
			SharedPreferences.Editor editor = FeizaoApp.mConctext.getSharedPreferences(Constants.COMMON_SF_HTTP_DOMAIN_NAME, Context.MODE_PRIVATE).edit();
			editor.putString(Constants.COMMON_SF_BASE_STAT_DOMAIN, statDomain);
			editor.commit();
		}
	}

	/**
	 * 更新m_domain-- webview地址
	 */
	public void updateSafeWebDomain(String mDomain) {
		if(!TextUtils.isEmpty(mDomain) && !base_m_domain.equals(mDomain)){
			base_m_domain = mDomain;
			Consts.BASE_M_DOMAIN = DomainConfig.getInstance().base_m_domain;
			Consts.updateBaseDomain();
			SharedPreferences.Editor editor = FeizaoApp.mConctext.getSharedPreferences(Constants.COMMON_SF_HTTP_DOMAIN_NAME, Context.MODE_PRIVATE).edit();
			editor.putString(Constants.COMMON_SF_BASE_M_DOMAIN, mDomain);
			editor.commit();
		}
	}

	/**
	 * 从json解析
	 * 用于获取接口请求的直连ip地址
	 * 以及备用的http lists
	 *
	 * @param job
	 */
	public void parseFromJson(JSONObject job) {
		try {
			String base_domain_tmp = job.getString("addr");
			String http_domain_lists_tmp = StringUtil.base64Decode(job.getString("backList"));

			SharedPreferences.Editor editor = FeizaoApp.mConctext.getSharedPreferences(Constants.COMMON_SF_HTTP_DOMAIN_NAME, Context.MODE_PRIVATE).edit();
			if (!TextUtils.isEmpty(base_domain_tmp)) {
				if(!base_domain.equals(base_domain_tmp)){
					Consts.BASE_DOMAIN = base_domain = base_domain_tmp;
					Consts.updateBaseDomain();
				}
				editor.putString(Constants.COMMON_SF_BASE_DOMAIN, base_domain);
			}
			if (!TextUtils.isEmpty(http_domain_lists_tmp)) {
				http_domain_lists = http_domain_lists_tmp;
				editor.putString(Constants.COMMON_SF_BASE_DOMAIN_LIST, http_domain_lists);
			}


			editor.commit();



		} catch (JSONException ex) {

		}
	}
}
