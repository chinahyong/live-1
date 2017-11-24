package tv.live.bx.activities;

import android.app.AlertDialog;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tv.live.bx.FeizaoApp;
import com.efeizao.bx.R;
import tv.live.bx.common.Business;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.common.OperationHelper;
import tv.live.bx.common.Utils;
import tv.live.bx.library.util.TelephoneUtil;
import com.lonzh.lib.LZActivity;

public class AdviceActivity extends LZActivity {
	private EditText moEtContent, moEtContact;
	private Button moBtnSubmit;

	private AlertDialog moProgress;
	/**
	 * 更多按钮
	 */
	private RelativeLayout moreLayout;
	private ImageView moreImageView;
	/**
	 * 返回按钮
	 */
	private RelativeLayout backLayout;
	/**
	 * 标题
	 */
	private TextView titleTv;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_advice;
	}

	@Override
	protected void initMembers() {
		moEtContent = (EditText) findViewById(R.id.advice_et_content);
		moEtContact = (EditText) findViewById(R.id.advice_et_contact);
		moBtnSubmit = (Button) findViewById(R.id.advice_btn_submit);
	}

	@Override
	protected void registerMsgListeners() {
		OnReceiveMsgListener loOnSubmitted = new OnReceiveMsgListener() {
			@Override
			public void onReceiveMsg(Message poMsg) {
				moProgress.dismiss();
				moProgress = null;
				switch (poMsg.what) {
					case MsgTypes.FEEDBACK_SUCCESS:
						OperationHelper.onEvent(FeizaoApp.mConctext, "clickFeedbackButtonSuccessful", null);
						showToast("感谢您的宝贵意见", TOAST_SHORT);
						onBackPressed();
						break;
					case MsgTypes.FEEDBACK_FAILED:
						showToast((String) poMsg.obj, TOAST_LONG);
						break;
				}
			}
		};
		registerMsgListener(MsgTypes.FEEDBACK_SUCCESS, loOnSubmitted);
		registerMsgListener(MsgTypes.FEEDBACK_FAILED, loOnSubmitted);
	}

	@Override
	public void initWidgets() {
		initTitle();
	}

	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.advice);
		mTopBackLayout.setOnClickListener(new OnBack());
	}

	@Override
	protected void setEventsListeners() {
		moBtnSubmit.setOnClickListener(new OnSubmit());
	}

	/***********************************
	 * 事件处理器
	 *************************************/
	private class OnBack implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			onBackPressed();
		}
	}

	private class OnSubmit implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			OperationHelper.onEvent(FeizaoApp.mConctext, "clickFeedbackButton", null);
			// 1 获取用户输入
			String lsContent = moEtContent.getText().toString();
			String lsContact = moEtContact.getText().toString();

			// 2 判断是否为空
			if (Utils.isStrEmpty(lsContent)) {
				showToast("请输入您的意见", TOAST_LONG);
				return;
			}
			if (Utils.isStrEmpty(lsContact)) {
				showToast("请输入您的联系方式", TOAST_LONG);
				return;
			}

			// 3 提交
			moProgress = Utils.showProgress(AdviceActivity.this);
			Business.feedback(AdviceActivity.this, lsContact, lsContent, TelephoneUtil.getDeviceName(),
					TelephoneUtil.getAndridVersion());
		}
	}
}
