package com.bixin.bixin.ui.popwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import tv.live.bx.R;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.config.AppConfig;

/**
 * Created by BYC on 2017/8/15.
 */

public class LiveUserManageDialog extends PopupWindow implements View.OnClickListener {
	private TextView tv_ti;
	private View view_line_1;
	private TextView tv_banned;

	//监听器
	private View.OnClickListener listener;

	public LiveUserManageDialog(Context ctx) {
		super(ctx);
		View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_live_manage, null);
		tv_ti = (TextView) v.findViewById(R.id.dialog_tv_ti);
		view_line_1 = v.findViewById(R.id.dialog_line_1);
		tv_banned = (TextView) v.findViewById(R.id.dialog_tv_banned);


		tv_ti.setOnClickListener(this);
		tv_banned.setOnClickListener(this);
		v.findViewById(R.id.dialog_tv_cancel).setOnClickListener(this);

		setContentView(v);
		this.setBackgroundDrawable(new ColorDrawable(0x00000000));

		//设置宽高
		setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
		setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

		setFocusable(true);
		setOutsideTouchable(true);
		setTouchable(true);

		setAnimationStyle(R.style.popwindow_live_filter);
	}

	/**
	 * 设置是否已被禁言
	 */
	public void setHasBanned(boolean isBanned){
		if(!isBanned){
			tv_banned.setText("禁言" + AppConfig.getInstance().banTime / 3600 +"小时");
		}else
			tv_banned.setText("取消禁言");
	}

	/**
	 * 设置控制权限
	 */
	public void setControlType(String controlType) {
		if (Constants.USER_TYPE_ANCHOR.equals(controlType) || Constants.USER_TYPE_OFFICIAL.equals(controlType) || Constants.USER_TYPE_OFFICIAL_ADMIN.equals(controlType)) {
			// 如果用户类别为管理员，则显示“取消管理员”
			tv_ti.setVisibility(View.VISIBLE);
			view_line_1.setVisibility(View.VISIBLE);
		} else if (Constants.USER_TYPE_ADMIN.equals(controlType)) {
			tv_ti.setVisibility(View.GONE);
			view_line_1.setVisibility(View.GONE);
		}
	}

	public void setOnClickListener(View.OnClickListener listener) {
		this.listener = listener;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.dialog_tv_ti:
			case R.id.dialog_tv_banned:
				this.dismiss();
				if (listener != null)
					listener.onClick(v);
				break;
			case R.id.dialog_tv_cancel:
				this.dismiss();
				break;
		}
	}
}
