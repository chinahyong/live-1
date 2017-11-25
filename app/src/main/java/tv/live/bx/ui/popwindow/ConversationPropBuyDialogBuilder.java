package tv.live.bx.ui.popwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import cn.efeizao.feizao.ui.dialog.CustomDialogBuilder;
import tv.live.bx.R;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.listeners.ViewOnClickListener;


/**
 * Created by Valar on 2017/3/22.
 * details：这个是描述私信的popupwindow
 */

public class ConversationPropBuyDialogBuilder extends CustomDialogBuilder implements View.OnClickListener {

	public Button mBtnBuyProp;
	private TextView mTvPropNum, mBtnLookUserInfo;
	private ImageView mIvHead;
	private ViewOnClickListener mViewOnClickListener;
	private Handler mHandler = new MyHandler();

	public ConversationPropBuyDialogBuilder(final Context context, String uri) {
		super(context, R.layout.pop_conversation_prop_buy);
		mBtnBuyProp = (Button) mDialogView.findViewById(R.id.conversation_buy_ticket);
		mTvPropNum = (TextView) mDialogView.findViewById(R.id.conversation_ticket_num);
		mBtnLookUserInfo = (TextView) mDialogView.findViewById(R.id.btn_person_info);
		mBtnLookUserInfo.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
		mBtnLookUserInfo.getPaint().setAntiAlias(true);//抗锯齿

		mIvHead = (ImageView) mDialogView.findViewById(R.id.private_message_user_head);
		mBtnBuyProp.setOnClickListener(this);
		mBtnLookUserInfo.setOnClickListener(this);
		if (uri != null) {
			if (uri.indexOf("://") == -1) {
				uri = "file://" + uri;
			}
			ImageLoaderUtil.with().loadImageTransformRoundCircle(mContext, mIvHead, uri);
		}
	}

	/**
	 * 设置 私信卡  数量
	 */
	public void setPropNum(String propNum) {
		// 私信卡大于0
		if (Integer.valueOf(propNum) > 0) {
			mBtnBuyProp.setText(R.string.live_conversation_use_prop);
		} else {
			mBtnBuyProp.setText(R.string.live_conversation_buy_prop);
		}
		mBtnBuyProp.setEnabled(true);
		String txt = mContext.getString(R.string.popwindow_conversation_prop_result_num);
		txt = String.format(txt, propNum);
		SpannableString ss = new SpannableString(txt);
		ss.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.a_text_color_da500e)), 2, txt.indexOf("张") + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		ss.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.a_text_color_999999)), 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
		ss.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.a_text_color_999999)), txt.indexOf("张") + 1, ss.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
		mTvPropNum.setText(ss);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btn_person_info:
			case R.id.conversation_buy_ticket:
				if (mViewOnClickListener != null) {
					mViewOnClickListener.onClick(view);
				}
				break;
		}
	}

	public void setViewOnClickListener(ViewOnClickListener mViewOnClickListener) {
		this.mViewOnClickListener = mViewOnClickListener;
	}

	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
		}
	}
}
