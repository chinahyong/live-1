package tv.live.bx.imageloader.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.GifRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.yanzhenjie.album.impl.AlbumImageLoader;

import java.io.File;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import tv.live.bx.R;
import tv.live.bx.imageloader.ImageLoader;
import tv.live.bx.imageloader.ImageLoadingListener;
import tv.live.bx.ui.drawables.GJLoadingDrawable;

/**
 * Created by Live on 2017/5/11.
 * glide图片加载器
 *
 * @Context with(Context context). 使用Application上下文，Glide请求将不受Activity/Fragment生命周期控制。
 * with(Activity activity).使用Activity作为上下文，Glide的请求会受到Activity生命周期控制。
 * with(FragmentActivity activity).Glide的请求会受到FragmentActivity生命周期控制。
 * with(android.app.Fragment fragment).Glide的请求会受到Fragment 生命周期控制。
 * with(android.support.v4.app.Fragment fragment).Glide的请求会受到Fragment生命周期控制
 * <p>
 * 特别提醒： Glide.with(context)执行的时候，context不能onDestroy；
 */

public class GlideImageLoader implements AlbumImageLoader, ImageLoader {

	@Override
	public void loadImage(ImageView imageView, String imagePath, int width, int height) {
		Glide.with(imageView.getContext()).load(new File(imagePath)).into(imageView);
	}

	@Override
	public <T> void loadImageTransformBlurTrans(Context context, ImageView imageView, T resource, Integer showLodingId, Integer showFailId) {
		loadImage(context, imageView, resource, showLodingId, null, showFailId, null, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL, new CenterCrop(context), new BlurTransformation(context));
	}

	@Override
	public <T> void loadImageTransformRoundCircle(Context context, ImageView imageView, T resource) {
		loadImage(context, imageView, resource, R.drawable.bg_user_default, null, R.drawable.bg_user_default, null, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL, new CropCircleTransformation(context));
	}

	@Override
	public <T> void loadImageTransformRoundedCorners(Context context, ImageView imageView, T resource, int radius) {
		loadImage(context, imageView, resource, -1, new GJLoadingDrawable(), 0, null, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL, new CenterCrop(context), new RoundedCornersTransformation(context, radius, 0));
	}

	@Override
	public <T> void loadImage(Context context, ImageView imageView, T resource) {
		loadImage(context, imageView, resource, -1, new GJLoadingDrawable(), 0, null, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
	}

	@Override
	public <T> void loadImage(Context context, ImageView imageView, T resource, Integer showLodingId, Integer showFailId) {
		loadImage(context, imageView, resource, showLodingId, null, showFailId, null, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
	}

	@Override
	public <T> Bitmap loadImageSyn(Context context, T resource) {
		return loadImageSyn(context, resource, 0, 0);
	}

	@Override
	public <T> void loadImage(@NonNull Context context, @NonNull T resource, int outWidth, int outHeigth, ImageLoadingListener imageLoadingListener) {
		loadImage(context, null, resource, -1, new GJLoadingDrawable(), 0, imageLoadingListener, outWidth, outHeigth);
	}

	@Override
	public <T> void loadGif(Context context, ImageView imageView, T resource) {
		loadGif(context, imageView, resource, R.drawable.icon_loading, 0);
	}

	public <T> void loadImageOnlyDownload(Context context, T resource) {
		Glide.with(context.getApplicationContext()).load(resource).downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
	}

	public void onLowMemory(Context context) {
		Glide.with(context.getApplicationContext()).onLowMemory();
	}

	public void onTrimMemory(Context context, int level) {
		Glide.with(context.getApplicationContext()).onTrimMemory(level);
	}

	/**
	 * @param context
	 * @param imageView
	 * @param resourceUri
	 * @param showLoadingId
	 * @param showFailId
	 * @param imageLoadingListener
	 * @param width                 Width and height must both be > 0 or Target#SIZE_ORIGINAL
	 * @param height
	 * @param bitmapTransformations
	 * @param <T>
	 */
	private static <T> void loadImage(Context context, @NonNull ImageView imageView, T resourceUri, Integer showLoadingId, Drawable showLoadingDrawable, Integer showFailId, final ImageLoadingListener imageLoadingListener, int width, int height, Transformation<Bitmap>... bitmapTransformations) {
		DrawableRequestBuilder<T> requestBuilder = getDrawableRequestBuilder(context, resourceUri, showLoadingId, showLoadingDrawable, showFailId);
		requestBuilder.diskCacheStrategy(DiskCacheStrategy.ALL);
		if (bitmapTransformations.length > 0) {
			requestBuilder.bitmapTransform(bitmapTransformations);
		}
		if (imageLoadingListener != null) {
			requestBuilder.into(new SimpleTarget<GlideDrawable>(width, height) {
				@Override
				public void onResourceReady(GlideDrawable resource, GlideAnimation<? super
						GlideDrawable> glideAnimation) {
					imageLoadingListener.onLoadingComplete(resource);
				}

				@Override
				public void onLoadStarted(Drawable placeholder) {
					imageLoadingListener.onLoadStarted(placeholder);
				}

				@Override
				public void onLoadCleared(Drawable placeholder) {
					imageLoadingListener.onLoadCleared(placeholder);
				}

				@Override
				public void onLoadFailed(Exception e, Drawable errorDrawable) {
					imageLoadingListener.onLoadFailed(e, errorDrawable);
				}
			});
		} else {
			requestBuilder.into(imageView);
		}

	}


	private static <T> void loadGif(Context context, ImageView imageView, T resourceId, Integer showLodingId, Integer showFailId) {
		GifRequestBuilder<T> requestBuilder = getGifRequestBuilder(context, resourceId, showLodingId, showFailId);
		requestBuilder.into(imageView);
	}

	private static <T> Bitmap loadImageSyn(Context context, T resourceUri, Integer showLodingId, Integer showFailId) {
		try {
			return Glide.with(context.getApplicationContext()).load(resourceUri).asBitmap().placeholder(showLodingId).error(showFailId).diskCacheStrategy(DiskCacheStrategy.ALL)
					.into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static <T> DrawableRequestBuilder<T> getDrawableRequestBuilder(Context context, T resource, Integer showLodingId, Drawable showLoadingDrawable, Integer showFailId) {
		if (showLoadingDrawable != null)
			return Glide.with(context.getApplicationContext()).load(resource).placeholder(showLoadingDrawable).error(showFailId).diskCacheStrategy(DiskCacheStrategy.ALL);
		else
			return Glide.with(context.getApplicationContext()).load(resource).placeholder(showLodingId).error(showFailId).diskCacheStrategy(DiskCacheStrategy.ALL);
	}

	private static <T> GifRequestBuilder<T> getGifRequestBuilder(Context context, T resource, Integer showLodingId, Integer showFailId) {
		return Glide.with(context.getApplicationContext()).load(resource).asGif().placeholder(showLodingId).error(showFailId).dontAnimate().diskCacheStrategy(DiskCacheStrategy.SOURCE);
	}
}
