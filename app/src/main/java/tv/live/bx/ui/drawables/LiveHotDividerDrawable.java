package tv.live.bx.ui.drawables;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import tv.live.bx.common.Utils;

/**
 * Created by BYC on 2017/7/15.
 */

public class LiveHotDividerDrawable extends Drawable {
	private Paint paint;
	public LiveHotDividerDrawable(){
		paint = new Paint();
		paint.setColor(0x00000000);
	}

	@Override
	public int getIntrinsicHeight() {
		return Utils.dpToPx(10);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawRect(getBounds(), paint);
	}

	@Override
	public void setAlpha(int alpha) {
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSPARENT;
	}

	@Override
	public void setColorFilter(ColorFilter colorFilter) {

	}
}
