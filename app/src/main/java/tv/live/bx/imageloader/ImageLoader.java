package tv.live.bx.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * Created by Administrator on 2017/5/17.
 * <p>
 * 图片加载接口类
 */

public interface ImageLoader {

	<T> void loadImageTransformBlurTrans(Context context, ImageView imageView, T imageUrl, Integer showLodingId, Integer showFailId);

	<T> void loadImageTransformRoundCircle(@NonNull Context context, @NonNull ImageView imageView, @NonNull T resource);

	<T> void loadImageTransformRoundedCorners(@NonNull Context context, @NonNull ImageView imageView, @NonNull T resource, @NonNull int radius);

	<T> void loadImage(@NonNull Context context, @NonNull ImageView imageView, @NonNull T resource);

	<T> void loadImage(Context context, ImageView imageView, T resource, Integer showLodingId, Integer showFailId);

	<T> void loadImageOnlyDownload(Context context, T resource);

	<T> void loadImage(@NonNull Context context, @NonNull T resource, int outWidth, int outHeigth, ImageLoadingListener imageLoadingListener);

	<T> Bitmap loadImageSyn(Context context, T resource);

	<T> void loadGif(@NonNull Context context, @NonNull ImageView imageView, @NonNull T resource);

	void onLowMemory(Context context);

	void onTrimMemory(Context context, int level);
}
