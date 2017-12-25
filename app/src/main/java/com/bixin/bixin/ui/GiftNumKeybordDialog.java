package com.bixin.bixin.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bixin.bixin.App;
import tv.live.bx.R;


/**
 * 礼物数字键盘 Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2015-7-12 下午4:16:19 <br/>
 *
 * @author Administrator
 * @since JDK 1.6
 */
@SuppressLint("ResourceAsColor")
public class GiftNumKeybordDialog {
	private Context context;
	private Dialog dialog;
	private Button giftSend;
	private ImageView giftBack;
	private ImageView giftNumBack;
	private TextView giftName;
	private TextView giftNum;
	private TextView giftNum1, giftNum2, giftNum3, giftNum4, giftNum5, giftNum6, giftNum7, giftNum8, giftNum9,
			giftNum0;
	private TextView giftNumClear;

	private Toast toast = Toast.makeText(App.mContext, R.string.paly_gift_num_empty, Toast.LENGTH_SHORT);
	private Display display;
	private OnItemClickListener itemClickListener;
	private GiftNumOnClick onGiftNumOnClick = new GiftNumOnClick();

	public GiftNumKeybordDialog(Context context, OnItemClickListener itemClickListener) {
		this.context = context;
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		display = windowManager.getDefaultDisplay();
		this.itemClickListener = itemClickListener;
	}

	public GiftNumKeybordDialog builder() {
		// 获取Dialog布局
		View view = LayoutInflater.from(context).inflate(R.layout.a_pop_sendnum_layou, null);

		giftSend = (Button) view.findViewById(R.id.playing_gifts_bottom_btn_complete);
		giftSend.setOnClickListener(onGiftNumOnClick);
		giftBack = (ImageView) view.findViewById(R.id.giftback);
		giftBack.setOnClickListener(onGiftNumOnClick);
		giftNumBack = (ImageView) view.findViewById(R.id.giftnumBack);
		giftNumBack.setOnClickListener(onGiftNumOnClick);

		giftName = (TextView) view.findViewById(R.id.giftname);

		giftNum = (TextView) view.findViewById(R.id.playing_gifts_bottom_ll_input_num);

		giftNum1 = (TextView) view.findViewById(R.id.giftnum1);
		giftNum1.setOnClickListener(onGiftNumOnClick);

		giftNum2 = (TextView) view.findViewById(R.id.giftnum2);
		giftNum2.setOnClickListener(onGiftNumOnClick);
		giftNum3 = (TextView) view.findViewById(R.id.giftnum3);
		giftNum3.setOnClickListener(onGiftNumOnClick);
		giftNum4 = (TextView) view.findViewById(R.id.giftnum4);
		giftNum4.setOnClickListener(onGiftNumOnClick);
		giftNum5 = (TextView) view.findViewById(R.id.giftnum5);
		giftNum5.setOnClickListener(onGiftNumOnClick);
		giftNum6 = (TextView) view.findViewById(R.id.giftnum6);
		giftNum6.setOnClickListener(onGiftNumOnClick);
		giftNum7 = (TextView) view.findViewById(R.id.giftnum7);
		giftNum7.setOnClickListener(onGiftNumOnClick);
		giftNum8 = (TextView) view.findViewById(R.id.giftnum8);
		giftNum8.setOnClickListener(onGiftNumOnClick);
		giftNum9 = (TextView) view.findViewById(R.id.giftnum9);
		giftNum9.setOnClickListener(onGiftNumOnClick);
		giftNum0 = (TextView) view.findViewById(R.id.giftnum0);
		giftNum0.setOnClickListener(onGiftNumOnClick);

		giftNumClear = (TextView) view.findViewById(R.id.giftnumClear);
		giftNumClear.setOnClickListener(onGiftNumOnClick);
		// 设置Dialog最小宽度为屏幕宽度
		view.setMinimumWidth(display.getWidth());

		// 定义Dialog布局和参数
		dialog = new Dialog(context, R.style.ActionSheetDialogStyle);
		dialog.setContentView(view);
		Window dialogWindow = dialog.getWindow();
		dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		lp.x = 0;
		lp.y = 0;
		dialogWindow.setAttributes(lp);

		return this;
	}

	/**
	 * 设置礼物名称
	 */
	public GiftNumKeybordDialog setTitle(String title) {
		giftName.setText(title);
		return this;
	}

	/**
	 * 设置礼物名称
	 */
	public GiftNumKeybordDialog setGiftNum(String num) {
		giftNum.setText(num);
		return this;
	}

	public GiftNumKeybordDialog setCancelable(boolean cancel) {
		dialog.setCancelable(cancel);
		return this;
	}

	public GiftNumKeybordDialog setCanceledOnTouchOutside(boolean cancel) {
		dialog.setCanceledOnTouchOutside(cancel);
		return this;
	}

	public void show() {
		dialog.show();
	}

	private class GiftNumOnClick implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
				case R.id.playing_gifts_bottom_btn_complete:
					if ("0".equals(giftNum.getText())) {
						toast.show();
						return;
					}
					if (itemClickListener != null) {
						itemClickListener.onClick(giftNum.getText().toString());
					}
					dialog.dismiss();
					break;
				case R.id.giftback:
					dialog.dismiss();
					break;
				case R.id.giftnumBack:
					// 当没有数据时，填写0
					if (giftNum.getText().length() == 1) {
						giftNum.setText("0");
						return;
					}
					giftNum.setText(giftNum.getText().toString().substring(0, giftNum.getText().length() - 1));
					break;
				case R.id.giftnum1:
					if (checkIsMinOrMax("1"))
						return;
					giftNum.setText(giftNum.getText() + "1");
					break;
				case R.id.giftnum2:
					if (checkIsMinOrMax("2"))
						return;
					giftNum.setText(giftNum.getText() + "2");
					break;
				case R.id.giftnum3:
					if (checkIsMinOrMax("3"))
						return;
					giftNum.setText(giftNum.getText() + "3");
					break;
				case R.id.giftnum4:
					if (checkIsMinOrMax("4"))
						return;
					giftNum.setText(giftNum.getText() + "4");
					break;
				case R.id.giftnum5:
					if (checkIsMinOrMax("5"))
						return;
					giftNum.setText(giftNum.getText() + "5");
					break;
				case R.id.giftnum6:
					if (checkIsMinOrMax("6"))
						return;
					giftNum.setText(giftNum.getText() + "6");
					break;
				case R.id.giftnum7:
					if (checkIsMinOrMax("7"))
						return;
					giftNum.setText(giftNum.getText() + "7");
					break;
				case R.id.giftnum8:
					if (checkIsMinOrMax("8"))
						return;
					giftNum.setText(giftNum.getText() + "8");
					break;

				case R.id.giftnum9:
					if (checkIsMinOrMax("9"))
						return;
					giftNum.setText(giftNum.getText() + "9");
					break;

				case R.id.giftnum0:
					// 当为0时，
					if (checkIsMinOrMax("0"))
						return;
					giftNum.setText(giftNum.getText() + "0");
					break;

				case R.id.giftnumClear:
					giftNum.setText("0");
					break;

				default:
					break;
			}
		}
	}

	private boolean checkIsMinOrMax(String number) {
		if ("0".equals(giftNum.getText())) {
			giftNum.setText(number);
			return true;
		} else if (giftNum.getText().length() >= 4) {
			giftNum.setText("9999");
			return true;
		}
		return false;
	}

	public interface OnItemClickListener {
		void onClick(String num);
	}
}
