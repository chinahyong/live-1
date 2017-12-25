package com.bixin.bixin.ui.popwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import tv.live.bx.R;
import com.bixin.bixin.common.Utils;

/**
 * Created by BYC on 2017/8/2.
 */

public class LiveFilterPopWindow extends PopupWindow {

	private SeekBar filterSeekBar;
	private OnProgressChange changeListener;

	public LiveFilterPopWindow(Context ctx, float filterLevel) {
		super(ctx);
		View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_live_filter, null);

		filterSeekBar = (SeekBar) v.findViewById(R.id.seek_bar_filter);


		filterSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (changeListener != null)
					changeListener.onChange(progress * 1.0f / seekBar.getMax());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		setContentView(v);
		this.setBackgroundDrawable(new ColorDrawable(0x00000000));
		filterSeekBar.setProgress((int)(filterLevel * filterSeekBar.getMax()));

		//设置宽高
		setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
		setHeight(Utils.dpToPx(100));

		setFocusable(true);
		setOutsideTouchable(true);
		setTouchable(true);

		setAnimationStyle(R.style.popwindow_live_filter);
	}

	public void setOnProgressChange(OnProgressChange changeListener) {
		this.changeListener = changeListener;
	}


	public interface OnProgressChange {
		void onChange(float progress);
	}
}
