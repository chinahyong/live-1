package com.bixin.bixin.danmu.DanmuBase;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.bixin.bixin.App;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.library.util.HtmlUtil;
import com.bixin.bixin.live.danmaku.DanmakuViewCommon;

import org.json.JSONObject;

/**
 * Created by walkingMen on 2016/5/12.
 */
public class DanmakuEntity {
	public CharSequence title;
	public String content;
	public String headUrl;
	public boolean verified;
	//守护神类别
	public String guardType;
	//背景图片是.9图
	public String backgroupImg;
	//广播后缀图片
	public String embellishImg;
	public JSONObject data;

	public DanmakuEntity(JSONObject data) {
		this.data = data;
		init(data);
	}

	private void init(JSONObject data) {
		SpannableString nickname = HtmlUtil.htmlTextDeal(App.mContext, data.optString("nickname"), null, null);
		SpannableStringBuilder ss = new SpannableStringBuilder(nickname);
		// 发消息用户类别
		if (!TextUtils.isEmpty(data.optString("type"))) {
			ss.append(" ");// 留点空隙
			DanmakuViewCommon.showUserType(ss, data.optString("type"));
		}
		// 发消息用户守护类别
		if (!TextUtils.isEmpty(data.optString("guardType"))) {
			ss.append(" ");// 留点空隙
			ss.append(Utils.getImageToSpannableString(
					Utils.getFiledDrawable(Constants.USER_GUARD_LEVEL_PIX, data.optString("guardType")),
					Utils.dip2px(App.mContext, 12)));
		}
		// 发消息用户等级
		if (!TextUtils.isEmpty(data.optString("level"))) {
			ss.append(" ");// 留点空隙
			ss.append(Utils.getImageToSpannableString(Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, data.optString("level")),
					Utils.dip2px(App.mContext, 12)));
		}
		this.embellishImg = data.optString("starImg");
		this.title = ss;
		this.content = data.optString("payload");
		if (TextUtils.isEmpty(data.optString("headPicGif"))) {
			this.headUrl = data.optString("headPic");
		} else {
			this.headUrl = data.optString("headPicGif");
		}

		this.backgroupImg = Utils.to9PngPath(data.optString("backgroundImg"));
		this.verified = Utils.getBooleanFlag(data.optString("verified").toString());
		this.guardType = data.optString("guardType");
	}

}
