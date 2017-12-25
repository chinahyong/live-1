package com.bixin.bixin.ui.drawables;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

import com.bixin.bixin.App;
import tv.live.bx.R;
import com.bixin.bixin.common.Utils;

/**
 * Created by BYC on 2017/7/5.
 */

public class GJLoadingDrawable extends Drawable {

	@Override
	public void setAlpha(int alpha) {

	}

	@Override
	public void draw(Canvas canvas) {
		int resID;
		if (getBounds().width() >= (int) (Utils.dpToPx(260) * 2.5f / 3))
			resID = R.drawable.icon_loading;
		else
			resID = R.drawable.icon_loading_mini;
		NinePatchDrawable drawable = (NinePatchDrawable) App.mContext.getResources().getDrawable(resID);
		drawable.setBounds(getBounds());
		drawable.draw(canvas);
	}


	@Override
	public void setColorFilter(int color, PorterDuff.Mode mode) {
		super.setColorFilter(color, mode);
	}

	@Override
	public void setColorFilter(ColorFilter colorFilter) {
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}
}
