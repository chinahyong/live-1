package com.bixin.bixin.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import tv.live.bx.R;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.fragments.LiveChatFragment;
import com.bixin.bixin.ui.ChatTextViewClickableSpan;

public class MeAnchorActivity extends BaseFragmentActivity implements OnClickListener {

	private final String CONTACT_TEXT = "申请当主播请点击联系QQ：2627466908或者电话联系：0755-26656095";
	private TextView mContactTv;

	/**
	 * 之前忘记使用这些方法了，这个类暂时不用了
	 */
	@Override
	protected int getLayoutRes() {
		return R.layout.activity_me_anchor;
	}

	@Override
	protected void initData(Bundle savedInstanceState) {

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	protected void initMembers() {
		mContactTv = (TextView) findViewById(R.id.me_anchor_contact);
		mContactTv.setMovementMethod(LinkMovementMethod.getInstance());
		initTitle();
	}

	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.me_anchor);
		mTopBackLayout.setOnClickListener(this);
	}

	public void initWidgets() {
		mContactTv.setText("申请当主播请点击联系");
		SpannableString loFrom = new SpannableString("QQ：2627466908");
		loFrom.setSpan(new ChatTextViewClickableSpan(new LiveChatFragment.IClickUserName() {

			@Override
			public void onClick(String username, String uid) {
				String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + username;
				MeAnchorActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			}
		}, "2627466908", null, null, Color.parseColor("#ffa200")), 0, loFrom.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		mContactTv.append(loFrom);
		mContactTv.append("或者电话联系：");
		loFrom = new SpannableString("0755-26656095");
		loFrom.setSpan(new ChatTextViewClickableSpan(new LiveChatFragment.IClickUserName() {

			@Override
			public void onClick(String username, String uid) {
				// 直接拨号
				// Intent intent = new Intent(Intent.ACTION_CALL,
				// Uri.parse("tel:" + username));
				// MeAnchorActivity.this.startActivity(intent);
				// 拨号界面
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + username));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		}, "075526656095", null, null, Color.parseColor("#333333")), 0, loFrom.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		mContactTv.append(loFrom);
	}

	protected void setEventsListeners() {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.top_left:
			onBackPressed();
			break;
		default:
			break;
		}

	}
}
