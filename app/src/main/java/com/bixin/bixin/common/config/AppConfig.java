package com.bixin.bixin.common.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import com.bixin.bixin.App;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.JacksonUtil;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.live.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by BYC on 2017/6/9.
 */

public class AppConfig {
	//key for hashMap
	//for moderatorTags
	public final static String ID = "id";
	public final static String NAME = "name";
	public final static String CLARITY_TYPE = "clarity_type";

	public String levelConfigVersion;
	public String appVersion;
	public int appUpdateType;
	public String androidLaunghPic;
	public boolean showRefer;
	public boolean showFullView;
	public String medalsConfigVersion;
	//不赋值会默认解析为LinkedHashMap
	public List<HashMap<String, Object>> moderatorTags = new ArrayList<>();
	//因为java编译器对泛型的类型擦除，所以在这里要显示的赋值，不然jackson无法解析 -- 对map例外
	public List<AppPatch> appPatch = new ArrayList<>();
	public String lpAppUrl;
	//网页base url
	public String urlDomain;
	//统计操作base domain
	public String statDomain;
	//私播文字
	public String privateText;
	//是否显示推荐标签
	public boolean showRecommend;
	//首页默认显示Tab
	public int liveDefaultTabIndex;
	//手机绑定提示时间
	public int mobileBindAlertInterval;
	public boolean postInsertGroup;
	//商城页面的版本号
	public int shopProductVersion;
	//背包是否有新礼物
	public boolean sp_guard_new_tip;
	//同一用户座驾进场显示的最小时间间隔
	public int mountDisplayFreq;

	/**
	 * 等级配置信息
	 */
	public Map<String, String> mLevelConfigInfo = new HashMap<>();


	//--- 本地configVersion(非网路读取的数据)---------------
	public String currentLevelConfigVersion;
	public String currentMedalsConfigVersion;
	//model勋章的base url
	public String usermodel_base;
	//极光推送是否已注册
	public boolean jpushRegisted;
	//商城页面版本号---上一已读版本
	public int lastShopProductVersion;
	//是否已经登录
	public boolean isLogged = false;
	//判断是不是从SXXXRXXMXX点击返回按钮返回来的
	public boolean isBackFromSelectedReceiver;
	//主播上次开播用的标签
	public String tag;
	//记录上次开播使用的横屏还是竖屏
	public int record_screen_oriention;

	//热播排名
	public String hotrank;
	//默认流程度设置
	public int clarity_type;

	//禁言时间
	public int banTime;
	//退出关注时间
	public int followTime;
	//欢迎页图片
	public String wallPaper;
	// 是否显示守护相关
	public boolean showGuard = false;

	/**
	 * Jackson解析内部类---需要用static内部类
	 */
	public static class AppPatch {
		public String version;
		public int patch;
		//package是关键字，所以加下划线以及set函数来处理
		public int _package;

		public void setPackage(int _package) {
			this._package = _package;
		}
	}


	//全局单例
	private static volatile AppConfig instance;

	/**
	 * 获取config单例
	 *
	 * @return
	 */
	public static AppConfig getInstance() {
		if (instance == null) {
			synchronized (AppConfig.class) {
				if (instance == null) {
					instance = readFromFile();
				}
			}
		}
		return instance;
	}

	private static AppConfig readFromFile() {
		AppConfig config = new AppConfig();

		//移植的代码
//		Utils.getCfg(getApplicationContext(),
//				Constants.COMMON_SF_NAME, Constants.SF_LEVEL_CONFIG_VERSION,
//				Constants.COMMON_LEVEL_CONFIG_VERSION)
//		Utils.getCfg(getApplicationContext(),
//				Constants.COMMON_SF_NAME, Constants.SF_MODEL_CONFIG_VERSION,
//				Constants.COMMON_MODEL_CONFIG_VERSION)
//		Utils.getCfg(getApplicationContext(), Constants.COMMON_SF_NAME,
//				Constants.SF_LEVEL_CONFIG_VERSION, Constants.COMMON_LEVEL_CONFIG_VERSION)
//		String mobileBindAlertInterval =;
//		if (!TextUtils.isEmpty(mobileBindAlertInterval)) {
//			mobileBindInterval = Long.parseLong(mobileBindAlertInterval);
//		}
//		Utils.getCfg(mActivity, "logged");
//		Utils.getCfg(mActivity, "privateText");
//		String tagId = Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, "tag");
//		String screenOriention = Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, "record_screen_oriention");

		SharedPreferences sp = App.mContext
            .getSharedPreferences(Constants.COMMON_SF_NAME, Context.MODE_PRIVATE);
		//兼容以前版本，以前的字段全用String方式存储，新添加的在用对应的类型存储
		config.levelConfigVersion = sp.getString(Constants.SF_LEVEL_CONFIG_VERSION, Constants.COMMON_LEVEL_CONFIG_VERSION);
		config.appVersion = sp.getString("appVersion", "");
		config.appUpdateType = Integer.parseInt(sp.getString("appUpdateType", "0"));
		config.androidLaunghPic = sp.getString(Constants.SF_WELCOME_AD_IMAGE_URL, "");
		config.showRefer = Boolean.parseBoolean(sp.getString("showRefer", "false"));
		config.showFullView = Boolean.parseBoolean(sp.getString("showFullView", "false"));
		config.medalsConfigVersion = sp.getString(Constants.SF_MODEL_CONFIG_VERSION, Constants.COMMON_MODEL_CONFIG_VERSION);
		config.lpAppUrl = sp.getString("lpAppUrl", "");
		config.urlDomain = sp.getString("urlDomain", "");
		config.statDomain = sp.getString("statDomain", "");
		config.privateText = sp.getString("privateText", "视频聊天");
		config.showRecommend = Boolean.parseBoolean(sp.getString("showRecommend", "false"));
		config.liveDefaultTabIndex = Integer.parseInt(sp.getString("liveDefaultTabIndex", "1"));
		config.mobileBindAlertInterval = Integer.parseInt(sp.getString("mobileBindAlertInterval", "0"));
		config.postInsertGroup = Boolean.parseBoolean(sp.getString(Constants.SF_INSERT_GROUP_CONFIG_VERSION, "false"));
		config.shopProductVersion = sp.getInt("shopProductVersion", 0);
		config.sp_guard_new_tip = Boolean.parseBoolean(sp.getString("sp_guard_new_tip", "false"));
		config.mountDisplayFreq = sp.getInt("mountDisplayFreq", 5);
		config.showGuard = sp.getBoolean("showGuard",false);



		//非网络请求信息
		if (!sp.contains("currentLevelConfigVersion")) {
			//兼容上一版本
			config.currentLevelConfigVersion = config.levelConfigVersion;
		} else {
			config.currentLevelConfigVersion = sp.getString("currentLevelConfigVersion", Constants.COMMON_LEVEL_CONFIG_VERSION);
		}

		if (!sp.contains("currentMedalsConfigVersion")) {
			//兼容上一版本
			config.currentMedalsConfigVersion = config.medalsConfigVersion;
		} else {
			config.currentMedalsConfigVersion = sp.getString("currentMedalsConfigVersion", Constants.COMMON_MODEL_CONFIG_VERSION);
		}

		config.usermodel_base = sp.getString(Constants.USER_MODEL_PIX_BASE, "");
		config.jpushRegisted = Boolean.parseBoolean(sp.getString("jpushRegistrationId", "false"));
		config.isLogged = Boolean.parseBoolean(sp.getString("logged", "false"));
		config.lastShopProductVersion = sp.getInt("lastShopProductVersion", 0);
		config.isBackFromSelectedReceiver = Utils.getBooleanFlag(sp.getString("isBackFromSelectedReceiver", "false"));
		config.hotrank = sp.getString("hotrank", "");
		config.clarity_type = Integer.parseInt(sp.getString(CLARITY_TYPE, Config.ENCODING_LEVEL_HEIGHT + ""));
		config.tag = sp.getString("tag", "");
		config.record_screen_oriention = Integer.parseInt(sp.getString("record_screen_oriention", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE + ""));


		//---首页tag信息解析------------------
		SharedPreferences sp2 = App.mContext
            .getSharedPreferences(Constants.SF_TAG_NAME, Context.MODE_PRIVATE);
		try {
			JSONArray jsonArray = new JSONArray(sp2.getString("tag", "[]"));
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				HashMap<String, Object> item = new HashMap<>();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Iterator<String> loIterator = jsonObject.keys();
				while (loIterator.hasNext()) {
					String lsKey = loIterator.next();
					item.put(lsKey, jsonObject.get(lsKey));
				}
				config.moderatorTags.add(item);
			}

		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		config.showRecommend = Boolean.parseBoolean(sp2.getString("showRecommend", "false"));
		config.liveDefaultTabIndex = Integer.parseInt(sp.getString("liveDefaultTabIndex", "1"));


		config.banTime = sp.getInt("banTime",10800);
		config.followTime = sp.getInt("followTime",0);
		config.wallPaper = sp.getString("wallPaper","");


		//------------等级配置信息读取----------------
		SharedPreferences loCfg = App.mContext
            .getApplicationContext().getSharedPreferences(Constants.USER_SF_LEVEL_NAME, Context.MODE_PRIVATE);
		config.mLevelConfigInfo = (Map<String, String>) loCfg.getAll();

		return config;
	}

	/**
	 * 网页商城是否有新货
	 */
	public boolean hasNewShop() {
		return shopProductVersion > lastShopProductVersion;
	}


	/**
	 * 更新礼物面板提示Tips
	 */
	public void updateNewGuardTips(boolean hasNewGuard) {
		sp_guard_new_tip = hasNewGuard;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.COMMON_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("sp_guard_new_tip", String.valueOf(hasNewGuard));
		editor.commit();
	}

	/**
	 * 更新商城页面版本号---上一已读版本
	 */
	public void updateLastShopVersionStatus() {
		lastShopProductVersion = shopProductVersion;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.COMMON_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putInt("lastShopProductVersion", lastShopProductVersion);
		editor.commit();

	}

	/**
	 * 更新激光推送 是否已注册的状态
	 */
	public void updateJpushRegistrationStatus(boolean status) {
		jpushRegisted = status;
		Utils.setCfg(App.mContext, "jpushRegistrationId", String.valueOf(status));
	}

	/**
	 * 更新当前configVersion
	 */
	public void updateCurrentLevelConfigVersion(String currentVersion) {
		//Utils.setCfg(getApplicationContext(), Constants.SF_LEVEL_CONFIG_VERSION, mLevelConfigVersion);
		currentLevelConfigVersion = currentVersion;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.COMMON_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("currentLevelConfigVersion", currentVersion);
		editor.commit();
	}

	/**
	 * 更新登状态
	 */
	public void updateIsBackFromSelectedReceiver(boolean isBackFromSelectedReceiver) {
		this.isBackFromSelectedReceiver = isBackFromSelectedReceiver;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.COMMON_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("isBackFromSelectedReceiver", String.valueOf(isBackFromSelectedReceiver));
		editor.commit();
	}
	/**
	 * 更新主播开播标签
	 */
	public void updatePlayingSetting(int screenOriention, String tag) {
		//移植的代码
//		Map<String, String> val = new HashMap<>();
//		val.put("tag", String.valueOf(mTvTag.getTag()));
//		val.put("record_screen_oriention", String.valueOf(mScreenOriention));
//		Utils.setCfg(mActivity, Constants.COMMON_SF_NAME, val);
		this.record_screen_oriention = screenOriention;
		this.tag = tag;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.COMMON_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("record_screen_oriention", String.valueOf(screenOriention));
		editor.putString("tag", tag);
		editor.commit();
	}

	/**
	 * 更新热播排名
	 */
	public void updateHotRank(String rank) {
		this.hotrank = rank;
		Utils.setCfg(App.mContext, "hotrank", rank);

	}

	/**
	 * 更新默认流畅度配置
	 */
	public void updateClarityType(int clarity_type) {
//		Utils.setCfg(PreviewLivePlayActivity.this, AppConfig.CLARITY_TYPE,
//				String.valueOf(mClarityType));
		this.clarity_type = clarity_type;
		Utils.setCfg(App.mContext, "clarity_type", String.valueOf(clarity_type));
	}


	/**
	 * 更新当前medalsConfigVersion -- 以及勋章baseUrl
	 *
	 * @param
	 */
	public void updateCurrentMedalsConfigVersion(String currentVersion, String imageBase) {
		//移植的代码

//		Utils.setCfg(getApplicationContext(), Constants.SF_MODEL_CONFIG_VERSION, mModelConfigVersion);
//		Utils.setCfg(getApplicationContext(), Constants.USER_MODEL_PIX_BASE, imageBase);
		currentMedalsConfigVersion = currentVersion;
		usermodel_base = imageBase;

		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.COMMON_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("currentMedalsConfigVersion", currentVersion);
		editor.putString(Constants.USER_MODEL_PIX_BASE, imageBase);
		editor.commit();
	}

	/**
	 * 更新登陆状态
	 */
	public void updateLoginStatus(boolean logged) {
		this.isLogged = logged;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.COMMON_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("logged", String.valueOf(logged));
		editor.commit();
	}

	/**
	 * 从网络接口--/app/getConfig中读取的数据
	 * @param config
	 */
	public void updateInfo(AppConfig config) {
		this.levelConfigVersion = config.levelConfigVersion;
		this.appVersion = config.appVersion;
		this.appUpdateType = config.appUpdateType;
		this.androidLaunghPic = config.androidLaunghPic;
		this.showRefer = config.showRefer;
		this.showFullView = config.showFullView;
		this.medalsConfigVersion = config.medalsConfigVersion;
		this.moderatorTags.clear();
		this.moderatorTags.addAll(config.moderatorTags);
		this.appPatch.clear();
		this.appPatch.addAll(config.appPatch);
		this.lpAppUrl = config.lpAppUrl;
		this.urlDomain = config.urlDomain;
		this.statDomain = config.statDomain;
		this.privateText = config.privateText;
		this.showRecommend = config.showRecommend;
		this.liveDefaultTabIndex = config.liveDefaultTabIndex;
		this.postInsertGroup = config.postInsertGroup;
		this.shopProductVersion = config.shopProductVersion;
		this.mountDisplayFreq = config.mountDisplayFreq;
		this.mobileBindAlertInterval = config.mobileBindAlertInterval;

		this.banTime = config.banTime;
		this.followTime = config.followTime;
		this.wallPaper = config.wallPaper;

		this.showGuard = config.showGuard;

		//移植的代码
//		Utils.setCfg(mActivity, Constants.SF_INSERT_GROUP_CONFIG_VERSION, data.get("postInsertGroup"));
//		Utils.setCfg(mActivity, Constants.SF_WELCOME_AD_IMAGE_URL, data.get("androidLaunghPic"));
//		Utils.setCfg(mActivity, "mobileBindAlertInterval", data.get("mobileBindAlertInterval"));
//		Utils.setCfg(mActivity, "privateText", data.get("privateText"));
//		Utils.setCfg(App.mContext, Constants.SF_LEVEL_CONFIG_VERSION, mLevelConfigVersion);

		// 开播标签配置
//		Utils.clearCfg(getApplicationContext(), Constants.SF_TAG_NAME);
//		EvtLog.d(TAG, "TAG success " + data.get("moderatorTags"));
//		Map<String, String> lmCfg = new HashMap<String, String>();
//		lmCfg.put("tag", data.get("moderatorTags"));
//		if (!Utils.isStrEmpty(data.get(Constants.SF_TAG_SHOW_RECOMMEND)))
//			lmCfg.put(Constants.SF_TAG_SHOW_RECOMMEND, data.get(Constants.SF_TAG_SHOW_RECOMMEND));
//		else
//			lmCfg.put(Constants.SF_TAG_SHOW_RECOMMEND, "false");
//		if (!Utils.isStrEmpty(data.get(Constants.SF_TAG_DEFAULT_INDEX)))
//			lmCfg.put(Constants.SF_TAG_DEFAULT_INDEX, data.get(Constants.SF_TAG_DEFAULT_INDEX));
//		Utils.setCfg(getApplicationContext(), Constants.SF_TAG_NAME, lmCfg);


		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.COMMON_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString(Constants.SF_LEVEL_CONFIG_VERSION, levelConfigVersion);
		editor.putString("appVersion", appVersion);
		editor.putString("appUpdateType", String.valueOf(appUpdateType));
		editor.putString(Constants.SF_WELCOME_AD_IMAGE_URL, androidLaunghPic);
		editor.putString(Constants.SF_INSERT_GROUP_CONFIG_VERSION, String.valueOf(postInsertGroup));
		editor.putString("showRefer", String.valueOf(showRefer));
		editor.putString("showFullView", String.valueOf(showFullView));
		editor.putString(Constants.SF_MODEL_CONFIG_VERSION, String.valueOf(medalsConfigVersion));
		editor.putString("lpAppUrl", lpAppUrl);
		editor.putString("urlDomain", urlDomain);
		editor.putString("statDomain", statDomain);
		editor.putString("privateText", privateText);
		editor.putString("mobileBindAlertInterval", String.valueOf(mobileBindAlertInterval));
		editor.putInt("shopProductVersion", shopProductVersion);
		//出于兼容性考虑，先保存当前信息
		editor.putString("currentLevelConfigVersion", currentLevelConfigVersion);
		editor.putString("currentMedalsConfigVersion", currentMedalsConfigVersion);
		editor.putInt("mountDisplayFreq", mountDisplayFreq);

		editor.putInt("banTime", banTime);
		editor.putInt("followTime", followTime);
		editor.putString("wallPaper", wallPaper);

		editor.putBoolean("showGuard",showGuard);

		editor.commit();

		//保存tag信息
		SharedPreferences.Editor editor1 = App.mContext
            .getSharedPreferences(Constants.SF_TAG_NAME, Context.MODE_PRIVATE).edit();
		editor1.clear();
		editor1.putString("tag", JacksonUtil.toJSon(moderatorTags));
		editor1.putString("showRecommend", String.valueOf(showRecommend));
		editor1.putString("liveDefaultTabIndex", String.valueOf(liveDefaultTabIndex));
		editor1.commit();
	}

	/**
	 * 解析从接口读取到的数据
	 * @param result--jsonObject
	 * @param mLevelConfigVersion -- 对应的versionString
	 */
	public void parseLevelConfigInfo(Object result, String mLevelConfigVersion){
		try {
			JSONArray userLevel = ((JSONObject) result).getJSONArray("user");
			mLevelConfigInfo.clear();
			for (int i = 0; i < userLevel.length(); i++) {
				mLevelConfigInfo.put(
						Constants.USER_LEVEL_PIX + userLevel.getJSONObject(i).getString("level"), userLevel
								.getJSONObject(i).getString("pic"));
			}

			JSONArray anchorLevel = ((JSONObject) result).getJSONArray("moderator");
			for (int i = 0; i < anchorLevel.length(); i++) {
				mLevelConfigInfo.put(Constants.USER_ANCHOR_LEVEL_PIX
						+ anchorLevel.getJSONObject(i).getString("level"), anchorLevel.getJSONObject(i)
						.getString("pic"));
			}
			AppConfig.getInstance().updateCurrentLevelConfigVersion(mLevelConfigVersion);
			Utils.setCfg(App.mContext, Constants.USER_SF_LEVEL_NAME, mLevelConfigInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
