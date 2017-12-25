package com.bixin.bixin.model;

import com.bixin.bixin.common.Constants;
import com.bixin.bixin.library.util.PackageUtil;

import java.util.Map;

/**
 * Created by Live on 2017/2/14.
 * 用户操作统计
 */

public class UserActionBean {
	private String v;            //客户端版本
	private String t;            //事件发生时间
	private String e;            //事件关键词
	private String plat;            // 设备
	private String packageId;        //packageId
	private Map<String, String> c;            //事件详细内容，可为空

	public UserActionBean(String t, String e, Map<String, String> c) {
		this.t = t;
		this.e = e;
		this.c = c;
	}

	public String getV() {
		v = PackageUtil.getVersionName();
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public String getT() {
		return t;
	}

	public void setT(String t) {
		this.t = t;
	}

	public String getE() {
		return e;
	}

	public void setE(String e) {
		this.e = e;
	}

	public Map<String, String> getC() {
		return c;
	}

	public void setC(Map<String, String> c) {
		this.c = c;
	}

	public String getPlat() {
		return Constants.DEVICE;
	}

	public void setPlat(String plat) {
		this.plat = plat;
	}

	public String getPackageId() {
		return Constants.PACKAGE_ID;
	}

	public void setPackageId(String packageId) {
		this.packageId = packageId;
	}

}
