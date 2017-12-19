package tv.live.bx.ui;

import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;


/**
 * Created by Live on 2017/2/16.
 */

public class BonusToast {

	private static Toast mToast;

	public static void showToast(SpannableString toastText) {
		View layout = LayoutInflater.from(FeizaoApp.mContext).inflate(R.layout.toast_bouns_layout, null);
		TextView content = (TextView) layout.findViewById(R.id.bonus_toast_content);
		content.setText(toastText);
		//Toast的初始化
		if (mToast == null) {
			mToast = new Toast(FeizaoApp.mContext);
			mToast.setDuration(Toast.LENGTH_LONG);
		}
		mToast.setView(layout);
		mToast.show();
	}

	public static void cancelToast() {
		if (mToast != null) {
			mToast.cancel();
			mToast = null;
		}
	}
}
