package com.bixin.bixin.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YONG on 2017/7/11.
 */

public class RankBean {
	public List<UserBean> all = new ArrayList<>();
	public List<UserBean> last = new ArrayList<>();
	public List<UserBean> week = new ArrayList<>();

	public static class UserBean {
		//头像
		public String headPic;
		//主播等级
		public int moderatorLevel;
		//用户级别
		public int level;
		//上热门前十次数
		public int hot_count=0;
		//昵称
		public String nickname;
		//主播id
		public String mid;
		//用户id
		public String uid;
		//是否咕叽认证
		public boolean verified;
		//认证信息
		public String verifyInfo;
	}
}
