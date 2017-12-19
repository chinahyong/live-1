package tv.live.bx.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;


/**
 * 描边+ 指定字体 +字体上下渐变
 *
 * @author Live
 */
@SuppressLint("AppCompatCustomView")
public class StrokeGradientTextView extends StrokeTextView {
	private Typeface mTypeface;
	private LinearGradient mGradient;
	// 字体纵向渐变色
	private final static int GRADIENT_VERTICAL[] = {ContextCompat.getColor(FeizaoApp.mContext, R.color.a_text_color_fff000), ContextCompat.getColor(FeizaoApp.mContext, R.color.a_text_color_f99200)};

	public StrokeGradientTextView(Context context) {
		super(context);
	}

	public StrokeGradientTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public StrokeGradientTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init() {
		super.init();
		mTypeface = Typeface.createFromAsset(FeizaoApp.mContext.getAssets(), "font/Sansus Webissimo-Regular.ttf");
		this.setTypeface(mTypeface);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			mGradient = new LinearGradient(0, 0, 0, getHeight(), GRADIENT_VERTICAL, new float[]{0, 1}, Shader.TileMode.CLAMP);
			getPaint().setShader(mGradient);
		}
	}
}