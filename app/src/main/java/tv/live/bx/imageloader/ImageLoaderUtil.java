package tv.live.bx.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import tv.live.bx.imageloader.glide.GlideImageLoader;

/**
 * Created by Administrator on 2017/5/17.
 * <p>
 * 特别提醒： loadImage(Context context...执行的时候，context对象不能onDestroy；
 */

public class ImageLoaderUtil {

	private static volatile ImageLoaderUtil mInstance;

	private static ImageLoader imageLoader;

	public static ImageLoaderUtil with() {
		if (mInstance == null) {
			synchronized (ImageLoaderUtil.class) {
				if (mInstance == null) {
					mInstance = new ImageLoaderUtil();
				}
			}
		}
		return mInstance;
	}

	private ImageLoaderUtil() {
		if (imageLoader == null) {
			//默认使用Glide加载方式
			imageLoader = new GlideImageLoader();
		}
	}

	/**
	 * Initialize ImageLoaderUtil.
	 * 建议在application中调用此方法
	 *
	 * @param imageLoader {@link ImageLoader}.
	 */
	public static void initialize(ImageLoader imageLoader) {
		imageLoader = imageLoader;
	}

	/**
	 * 加载并显示（高斯模糊图图片）
	 *
	 * @param context
	 * @param imageView
	 * @param imageUrl
	 * @param showLodingId
	 * @param showFailId
	 */
	public void loadImageTransformBlurTrans(Context context, ImageView imageView, String imageUrl, Integer showLodingId, Integer showFailId) {
		imageLoader.loadImageTransformBlurTrans(context, imageView, imageUrl, showLodingId, showFailId);
	}

	public void loadImageTransformBlurTrans(Context context, ImageView imageView, int imageId, Integer showLodingId, Integer showFailId) {
		imageLoader.loadImageTransformBlurTrans(context, imageView, imageId, showLodingId, showFailId);
	}

	/**
	 * 加载并显示（圆形图）
	 *
	 * @param context
	 * @param imageView
	 * @param imageUrl
	 */
	public void loadImageTransformRoundCircle(Context context, ImageView imageView, String imageUrl) {
		imageLoader.loadImageTransformRoundCircle(context, imageView, imageUrl);
	}

	/**
	 * 加载屏显示（圆角图）
	 *
	 * @param context
	 * @param imageView
	 * @param imageUrl
	 * @param radius    角度
	 */
	public void loadImageTransformRoundedCorners(Context context, ImageView imageView, String imageUrl, int radius) {
		imageLoader.loadImageTransformRoundedCorners(context, imageView, imageUrl, radius);
	}

	public void loadImage(Context context, ImageView imageView, String imageUrl) {
		imageLoader.loadImage(context, imageView, imageUrl);
	}

	public void loadImage(Context context, ImageView imageView, Integer resourceId) {
		imageLoader.loadImage(context, imageView, resourceId);
	}

	/**
	 * 加载指定大小的图片资源
	 *
	 * @param context
	 * @param imageUrl
	 * @param outWidth             Width and height must both be > 0 or Target#SIZE_ORIGINAL
	 * @param outHeight
	 * @param imageLoadingListener
	 */
	public void loadImage(@NonNull Context context, @NonNull String imageUrl, int outWidth, int outHeight, ImageLoadingListener imageLoadingListener) {
		imageLoader.loadImage(context, imageUrl, outWidth, outHeight, imageLoadingListener);
	}

	public void loadImage(Context context, ImageView imageView, String imageUrl, Integer showLodingId, Integer showFailId) {
		imageLoader.loadImage(context, imageView, imageUrl, showLodingId, showFailId);
	}

	public void loadImage(Context context, ImageView imageView, Integer resourceId, Integer showLodingId, Integer showFailId) {
		imageLoader.loadImage(context, imageView, resourceId, showLodingId, showFailId);
	}

	/**
	 * 加载图片到缓存，没有返回图片资源
	 *
	 * @param context
	 * @param imageUrl
	 */
	public void loadImageOnlyDownload(Context context, String imageUrl) {
		imageLoader.loadImageOnlyDownload(context, imageUrl);
	}

	/**
	 * 同步加载图片资源. 注：不能在main线程使用
	 *
	 * @param context
	 * @param imageUrl
	 * @return
	 */
	public Bitmap loadImageSyn(Context context, String imageUrl) {
		return imageLoader.loadImageSyn(context, imageUrl);
	}

	public Bitmap loadImageSyn(Context context, Integer resourceId) {
		return imageLoader.loadImageSyn(context, resourceId);
	}

	/**
	 * 加载并显示gif动画
	 *
	 * @param context
	 * @param imageView
	 * @param resourceId
	 */
	public void loadGif(Context context, ImageView imageView, Integer resourceId) {
		imageLoader.loadGif(context, imageView, resourceId);
	}

	public void loadGif(Context context, ImageView imageView, String resourceUri) {
		imageLoader.loadGif(context, imageView, resourceUri);
	}

	public void onLowMemory(Context context) {
		imageLoader.onLowMemory(context);
	}

	public void onTrimMemory(Context context, int level) {
		imageLoader.onTrimMemory(context, level);
	}
}
