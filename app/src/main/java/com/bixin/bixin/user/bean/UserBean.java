package com.bixin.bixin.user.bean;

/**
 * Created by Admin
 */

public class UserBean {
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
	public boolean lowkeyEnter;
	//电话
	public String mobile;
}
