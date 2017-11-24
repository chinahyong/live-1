package tv.live.bx.imageloader;

import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2017/5/17.
 */

public interface ImageLoadingListener {

	void onLoadStarted(Drawable placeholder);

	void onLoadFailed(Exception e, Drawable errorDrawable);

	void onLoadingComplete(Drawable resource);

	void onLoadCleared(Drawable placeholder);
}
