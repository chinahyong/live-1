package com.bixin.bixin.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.bixin.bixin.App;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by BYC on 2017/6/9.
 */

public class UserInfoConfig {
	public String id = "0";
	//昵称
	public String nickname;
	//头像
	public String headPic;
	//生日
	public String birthday;
	//Consts.GENDER_MALE
	//1:male 2:female
	public int sex;
	//签名
	public String signature;
	//用户等级
	public int level;
	//下一级别
	public int nextLevel;
	//当前等级进度
	public long levelCoin;
	//升级需要泡泡
	public int nextLevelNeedCoin;
	//关注数
	public int attentionNum;
	//粉丝数
	public int fansNum;
	//用户：1； 主播：2或者5；
	public int type;
	//余额
	public String coin;
	public String lowCoin;
	// 用户收入,当此参数为null时（后台未下发，指定主播不显示收益入口）
	public String incomeAvailable;
	//可编辑性别
	public boolean canEditSex;
	//大图
	public String bgImg;
	//是否首次登陆
	public boolean isFirstLogin;
	//进场动效是否开启
	public boolean lowkeyEnter;
	//电话
	public String mobile;
	//剩余可用私信卡数量
	public int messageCardNum;
	//是否实名认证的主播
	public boolean isIdVerifiedModerator;
	public int iosStatus;
	//新手福利结束时间
	public long beginnerDeadline;
	//相册相关
	public List<HashMap<String, String>> gallery = new ArrayList<>();
	public int newMessageNum;
	public int postNum;
	public int groupCreateNumLeft;
	public String ryToken;
	//是否被禁言
	public boolean isBan;
	/**
	 * 禁言剩余时间 -1是永久禁言
	 */
	public long banLeftTime;
	//守护数量
	public int guardNum;
	public boolean applePay = false;
	//是否果酱认证
	private String verified;
	//认证文字信息
	public String verifyInfo;
	//主播等级
	public int moderatorLevel;
	//主播等级全称
	public String moderatorLevelName;
	// 用户等级全称
	public String userLevelName;



	//--------本地信息---非网络请求信息----
	//上次启动时间
	public long mLastLauchTimes;
	//用户名---和昵称有什么区别？
	public String account;
	// 是否显示红包提示
	public String isShowRedMsgFlag;
	//美颜---比率（七牛）
	public float filterPercent;


	private static volatile UserInfoConfig instance;

	/**
	 * 获取用户信息单例
	 */
	public static UserInfoConfig getInstance() {
		if (instance == null) {
			synchronized (UserInfoConfig.class) {
				if (instance == null) {
					instance = readFromFile();
				}
			}
		}
		return instance;
	}

	private static UserInfoConfig readFromFile() {
		//移植的代码
		//Utils.getCfg(mActivity, Constants.USER_SF_NAME, "mobile");
		//Utils.getCfg(mActivity, Constants.USER_SF_NAME, "level");
		//Utils.getCfg(mActivity, Constants.USER_SF_NAME, "lowkeyEnter");
		//Utils.strBool(Utils.getCfg(App.mContext, Constants.USER_SF_NAME, "isBan"));
		//long bantime = Long.parseLong(Utils.getCfg(App.mContext, Constants.USER_SF_NAME, "banLeftTime"));
		//final String curUid = Utils.getCfg(App.mContext, Constants.USER_SF_NAME, "id");
		//"true".equals(Utils.getCfg(mActivity, Constants.USER_SF_NAME, "canEditSex"))
		//String beginnerDeadline = Utils.getCfg(App.mContext, Constants.USER_SF_NAME, "beginnerDeadline");
//		String account = Utils.getCfg(this, Constants.USER_SF_NAME, USER_NAME);
//		mBalance = Utils.getCfg(mActivity, Constants.USER_SF_NAME, "coin", "0");

		UserInfoConfig userInfo = new UserInfoConfig();
		SharedPreferences sp = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE);
		//兼容以前版本，全部用getString处理
		userInfo.id = sp.getString("id", "0");
		userInfo.nickname = sp.getString("nickname", "");
		userInfo.headPic = sp.getString("headPic", "");
		userInfo.birthday = sp.getString("birthday", "");
		userInfo.sex = Integer.parseInt(sp.getString("sex", "1")); //待修改
		userInfo.signature = sp.getString("signature", "");
		userInfo.level = Integer.parseInt(sp.getString("level", "0"));
		userInfo.nextLevel = Integer.parseInt(sp.getString("nextLevel", "0"));
		userInfo.levelCoin = Long.parseLong(sp.getString("levelCoin", "0"));
		userInfo.nextLevelNeedCoin = Integer.parseInt(sp.getString("nextLevelNeedCoin", "0"));
		userInfo.attentionNum = Integer.parseInt(sp.getString("attentionNum", "0"));
		userInfo.fansNum = Integer.parseInt(sp.getString("fansNum", "0"));
		userInfo.type = Integer.parseInt(sp.getString("type", "1"));
		userInfo.coin = sp.getString("coin", "0");
		userInfo.lowCoin = sp.getString("lowCoin", "");
		userInfo.incomeAvailable = sp.getString("incomeAvailable", null);
		userInfo.canEditSex = Boolean.parseBoolean(sp.getString("canEditSex", "false"));
		userInfo.bgImg = sp.getString("bgImg", "");
		userInfo.isFirstLogin = Boolean.parseBoolean(sp.getString("isFirstLogin", "false"));
		userInfo.lowkeyEnter = Boolean.parseBoolean(sp.getString("lowkeyEnter", "false"));
		userInfo.mobile = sp.getString("mobile", "");
		userInfo.messageCardNum = Integer.parseInt(sp.getString("messageCardNum", "0"));
		userInfo.isIdVerifiedModerator = Boolean.parseBoolean(sp.getString("isIdVerifiedModerator", "false"));
		userInfo.iosStatus = Integer.parseInt(sp.getString("iosStatus", "0"));
		userInfo.beginnerDeadline = Long.parseLong(sp.getString("beginnerDeadline", "0")); //新手福利结束时间
		userInfo.newMessageNum = Integer.parseInt(sp.getString("newMessageNum", "0"));
		userInfo.postNum = Integer.parseInt(sp.getString("postNum", "0"));
		userInfo.groupCreateNumLeft = Integer.parseInt(sp.getString("groupCreateNumLeft", "0"));
		userInfo.ryToken = sp.getString("ryToken", "");
		userInfo.isBan = Boolean.parseBoolean(sp.getString("isBan", "false"));
		userInfo.banLeftTime = Long.parseLong(sp.getString("banLeftTime", "0"));
		userInfo.guardNum = Integer.parseInt(sp.getString("guardNum", "0"));
		userInfo.applePay = Boolean.parseBoolean(sp.getString("applePay", "false"));
		userInfo.verifyInfo = sp.getString("verifyInfo", "");
		userInfo.moderatorLevel = Integer.parseInt(sp.getString("moderatorLevel", "0"));
		userInfo.moderatorLevelName = sp.getString("moderatorLevelName", "");
		userInfo.userLevelName = sp.getString("userLevelName", "");
		userInfo.verified = sp.getString("verified", "0");

		//本地数据
		userInfo.mLastLauchTimes = Long.parseLong(sp.getString("mLastLauchTimes", "0"));
		userInfo.account = sp.getString("username", "");
		userInfo.isShowRedMsgFlag = sp.getString("isShowRedMsgFlag", "");
		userInfo.filterPercent = sp.getFloat("filterPercent", 0.5f);


		return userInfo;
	}

	/**
	 * 是否果酱认证了的主播--兼容0/1 true/false两种模式
	 */
	public boolean isVerifyed() {
		return Utils.getBooleanFlag(verified);
	}

	/**
	 * 登出
	 */
	public static void logout() {
		Utils.clearCfg(App.mContext, Constants.USER_SF_NAME);
		instance = null;
	}

	/**
	 * @param name 用于更新的用户名
	 */
	public void updateUsername(String name) {
		//更新用户名
		account = name;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("username", name);
		editor.commit();
	}

	/**
	 * @param coin 更新帐户余额
	 */
	public void updateCoin(String coin) {
		//移植的代码
		//Utils.setCfg(mActivity, Constants.USER_SF_NAME, "coin", mBalance);
		//更新用户名
		this.coin = coin;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("coin", coin);
		editor.commit();
	}

	/**
	 * 更新美颜滤镜--比率
	 */
	public void updateFilterPer(float per){
		this.filterPercent = per;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putFloat("filterPercent", filterPercent);
		editor.commit();
	}

	/**
	 * @param signature 更新签名
	 */
	public void updateSignature(String signature) {
//		移植的代码
//		Utils.setCfg(this, Constants.USER_SF_NAME, "signature", signature);
		//更新用户名
		this.signature = signature;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("signature", signature);
		editor.commit();
	}

	/**
	 * 更新用户最近启动时间
	 *
	 * @param mCurLauchTime
	 */
	public void updateLaunchTime(long mCurLauchTime) {
		//移植的代码
		mLastLauchTimes = mCurLauchTime;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("mLastLauchTimes", String.valueOf(mCurLauchTime));
		editor.commit();
	}

	/**
	 * 更新用户id
	 *
	 * @param id
	 */
	public void updateUserId(String id) {
		//移植的代码
		//Utils.setCfg(Login2Activity.this, Constants.USER_SF_NAME, "id", lsUid);
		this.id = id;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("id", id);
		editor.commit();
	}

	/**
	 * 更新用户id
	 *
	 * @param nickname 昵称
	 */
	public void updateNickname(String nickname) {
		//移植的代码
		//Utils.setCfg(this, Constants.USER_SF_NAME, "nickname", lsName);
		this.nickname = nickname;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("nickname", nickname);
		editor.commit();
	}

	/**
	 * 更新点点币
	 *
	 * @param lowCoin
	 */
	public void updateLowCoin(String lowCoin) {
		//移植的代码
		//	Utils.setCfg(mActivity, Constants.USER_SF_NAME, "lowCoin", mDianDianBalance);
		this.lowCoin = lowCoin;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("lowCoin", lowCoin);
		editor.commit();
	}

	/**
	 * 更新用户电话
	 *
	 * @param mobile
	 */
	public void updateMobile(String mobile) {
		//移植的代码
		this.mobile = mobile;
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("mobile", mobile);
		editor.commit();
	}

	/**
	 * 更新是否主播
	 */
	public void updateIsIdVerifiedModerator(boolean isIdVerifiedModerator) {
		this.isIdVerifiedModerator = isIdVerifiedModerator;
		//移植的代码--之前保存在了错误的地方
		//Utils.setCfg(mActivity, "isIdVerifiedModerator", Constants.COMMON_TRUE);
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("isIdVerifiedModerator", String.valueOf(isIdVerifiedModerator));
		editor.commit();
	}

	/**
	 * 领取红包，dialog中当天最后一次领取完成，是否显示提示
	 *
	 * @param showRedMsgFlag
	 */
	public void updateRedPacketMsg(String showRedMsgFlag) {
		this.isShowRedMsgFlag = showRedMsgFlag;
		//移植的代码--之前保存在了错误的地方
		//Utils.setCfg(mActivity, "isIdVerifiedModerator", Constants.COMMON_TRUE);
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		editor.putString("isShowRedMsgFlag", showRedMsgFlag);
		editor.commit();
	}

	/**
	 * 更新多个信息
	 */
	public void updateFromMap(Map<String, String> map) {
		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		try {
			Class<?> clazz = UserInfoConfig.class;
			for (String key : map.keySet()) {
				// 可以直接对 private 的属性赋值
				Field field = clazz.getDeclaredField(key);
				field.setAccessible(true);
				Class<?> type = field.getType();
				String typeName = type.getName();
				if (typeName.equals("int")) {
					//或者用Integer.class.equals(type)
					field.set(this, Integer.parseInt(map.get(key)));
				} else if (typeName.equals("long"))
					field.set(this, Long.parseLong(map.get(key)));
				else if (typeName.equals("boolean"))
					field.set(this, Boolean.parseBoolean(map.get(key)));
				else if (typeName.equals("java.lang.String"))
					//或者用 String.class.equals(type)
					field.set(this, map.get(key));

				editor.putString(key, map.get(key));
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} finally {
			editor.commit();
		}

	}

	public void updateFromInfo(UserInfoConfig config) {
		this.id = config.id;
		this.nickname = config.nickname;
		this.headPic = config.headPic;
		this.birthday = config.birthday;
		this.sex = config.sex;//1:male 2:female
		this.signature = config.signature; //签名
		this.level = config.level;
		this.nextLevel = config.nextLevel; //下一级别
		this.levelCoin = config.levelCoin; //当前等级进度
		this.nextLevelNeedCoin = config.nextLevelNeedCoin; //升级需要泡泡
		this.attentionNum = config.attentionNum; //关注数
		this.fansNum = config.fansNum;  //粉丝数
		this.type = config.type;   //用户：1； 主播：2或者5；
		this.coin = config.coin; //收益
		this.lowCoin = config.lowCoin;
		this.incomeAvailable = config.incomeAvailable;//收入是否可兑现
		this.canEditSex = config.canEditSex;//可编辑性别
		this.bgImg = config.bgImg;//大图
		this.isFirstLogin = config.isFirstLogin; //是否首次登陆
		this.lowkeyEnter = config.lowkeyEnter;  //进场动效是否开启
		this.mobile = config.mobile;  //电话
		this.messageCardNum = config.messageCardNum; //剩余可用私信卡数量
		this.isIdVerifiedModerator = config.isIdVerifiedModerator;//是否实名认证的主播
		this.iosStatus = config.iosStatus;
		this.beginnerDeadline = config.beginnerDeadline; //新手福利结束时间
		this.gallery.clear();//相册相关
		this.gallery.addAll(config.gallery);
		this.newMessageNum = config.newMessageNum;
		this.postNum = config.postNum;
		this.groupCreateNumLeft = config.groupCreateNumLeft;
		this.ryToken = config.ryToken;
		this.isBan = config.isBan;
		this.banLeftTime = config.banLeftTime;
		this.guardNum = config.guardNum;
		this.applePay = config.applePay;
		this.verifyInfo = config.verifyInfo;
		this.moderatorLevel = config.moderatorLevel;
		this.moderatorLevelName = config.moderatorLevelName;
		this.userLevelName = config.userLevelName;
		this.verified = config.verified;

		SharedPreferences.Editor editor = App.mContext
            .getSharedPreferences(Constants.USER_SF_NAME, Context.MODE_PRIVATE).edit();
		//兼容以前版本，全部用getString处理
		//为了兼容updateFromMap--以后的字段也最好用putString存储
		editor.putString("id", id);
		editor.putString("nickname", nickname);
		editor.putString("headPic", headPic);
		editor.putString("birthday", birthday);
		editor.putString("sex", String.valueOf(sex)); //带修改
		editor.putString("signature", signature);
		editor.putString("level", String.valueOf(level));
		editor.putString("nextLevel", String.valueOf(nextLevel));
		editor.putString("levelCoin", String.valueOf(levelCoin));
		editor.putString("nextLevelNeedCoin", String.valueOf(nextLevelNeedCoin));
		editor.putString("attentionNum", String.valueOf(attentionNum));
		editor.putString("fansNum", String.valueOf(fansNum));
		editor.putString("type", String.valueOf(type));
		editor.putString("coin", coin);
		editor.putString("lowCoin", lowCoin);
		editor.putString("incomeAvailable", incomeAvailable);
		editor.putString("canEditSex", String.valueOf(canEditSex));
		editor.putString("bgImg", bgImg);
		editor.putString("isFirstLogin", String.valueOf(isFirstLogin));
		editor.putString("lowkeyEnter", String.valueOf(lowkeyEnter));
		editor.putString("mobile", mobile);
		editor.putString("messageCardNum", String.valueOf(messageCardNum));
		editor.putString("isIdVerifiedModerator", String.valueOf(isIdVerifiedModerator));
		editor.putString("iosStatus", String.valueOf(iosStatus));
		editor.putString("beginnerDeadline", String.valueOf(beginnerDeadline)); //新手福利结束时间
		editor.putString("newMessageNum", String.valueOf(newMessageNum));
		editor.putString("postNum", String.valueOf(postNum));
		editor.putString("groupCreateNumLeft", String.valueOf(groupCreateNumLeft));
		editor.putString("ryToken", ryToken);
		editor.putString("isBan", String.valueOf(isBan));
		editor.putString("banLeftTime", String.valueOf(banLeftTime));
		editor.putString("guardNum", String.valueOf(guardNum));
		editor.putString("applePay", String.valueOf(applePay));
		editor.putString("verifyInfo", verifyInfo);
		editor.putString("moderatorLevel", String.valueOf(moderatorLevel));
		editor.putString("moderatorLevelName", moderatorLevelName);
		editor.putString("userLevelName",userLevelName);
		editor.putString("verified", verified);
		editor.commit();
	}
}
