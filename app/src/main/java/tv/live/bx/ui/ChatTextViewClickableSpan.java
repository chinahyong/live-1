/**
 * Project Name:feizao File Name:TextViewClickableSpan.java Package
 * Name:com.efeizao.feizao.ui Date:2015-8-15下午12:06:12
 */

package tv.live.bx.ui;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import tv.live.bx.fragments.LiveChatFragment.IClickUserName;


public class ChatTextViewClickableSpan extends ClickableSpan {

	String username;
	String uid;
	String type;
	int color;
	IClickUserName iclickUserName;

	public ChatTextViewClickableSpan(IClickUserName iclickUserName, String username, String type, String uid, int color) {
		super();
		this.username = username;
		this.uid = uid;
		this.color = color;
		this.type = type;
		this.iclickUserName = iclickUserName;
	}

	public String getUserName() {
		return username;
	}

	public String getUid() {
		return uid;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(color);
	}

	@Override
	public void onClick(View widget) {
		if (iclickUserName != null)
			iclickUserName.onClick(username, uid);
	}
}
