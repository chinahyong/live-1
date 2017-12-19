package tv.live.bx.imageloader;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import tv.guojiang.baselib.image.ImageDirector;
import tv.guojiang.baselib.image.listener.ImageLoadingListener;
import tv.guojiang.baselib.image.model.ImageConstants;
import tv.guojiang.baselib.image.model.ImageSize;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;

/**
 * @author Live
 */

public class ImageLoaderUtil {

    // implements AlbumImageLoader
    private static ImageLoaderUtil instance;

    public synchronized static ImageLoaderUtil getInstance() {
        if (instance == null) {
            synchronized (ImageLoaderUtil.class) {
                if (instance == null) {
                    instance = new ImageLoaderUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 加载图片
     *
     * @param imageView {@link ImageView}.
     * @param imagePath path from local SDCard.
     * @param width target width.
     * @param height target height.
     */
    //    @Override
    public void loadImage(ImageView imageView, String imagePath, int width, int height) {
        ImageDirector.getInstance(FeizaoApp.mContext).imageBuilder()
            .imageUrl(imagePath)
            .imageSize(width, height)
            .scaleType(ImageView.ScaleType.FIT_XY)
            .into(imageView);
    }

    /**
     * 加载图片
     *
     * @param imageView {@link ImageView}.
     * @param imagePath path from local SDCard.
     */
    //    @Override
    public void loadImage(ImageView imageView, Object imagePath) {
        ImageDirector.getInstance(FeizaoApp.mContext).imageBuilder()
            .imageUrl(imagePath)
            .imageSize(ImageSize.SIZE_ORIGINAL, ImageSize.SIZE_ORIGINAL)
            .scaleType(ImageView.ScaleType.FIT_XY)
            .into(imageView);
    }

    /**
     * 加载图片，通过回调赋值
     */
    public void loadImage(Object imagePath, int width, int height,
        ImageLoadingListener loadingListener) {
        ImageDirector.getInstance(FeizaoApp.mContext).imageBuilder()
            .imageUrl(imagePath)
            .imageSize(width, height)
            .imageLoadingListener(loadingListener)
            .into(null);
    }

    /**
     * 加载图片，默认失败图片
     */
    public void loadImage(ImageView imageView, Object imagePath,
        @DrawableRes int loading, @DrawableRes int error,
        int width, int height, DiskCacheStrategy diskCacheStrategy) {
        ImageDirector.getInstance(FeizaoApp.mContext).imageBuilder()
            .imageUrl(imagePath)
            .loadingImage(loading)
            .errorImage(error)
            .diskCacheStrategy(diskCacheStrategy)
            .into(imageView);
    }

    /**
     * @param imageView
     * @param imagePath
     * @param diskCacheStrategy
     */
    public void loadImage(ImageView imageView, Object imagePath,
        DiskCacheStrategy diskCacheStrategy) {
        loadImage(imageView, imagePath, 0, 0, 0, 0, diskCacheStrategy);
    }

    /**
     * 加载图片，默认失败图片
     */
    public void loadImageAndDefault(ImageView imageView, Object imagePath, @DrawableRes int loading,
        @DrawableRes int error) {
        ImageDirector.getInstance(FeizaoApp.mContext).imageBuilder()
            .imageUrl(imagePath)
            .loadingImage(loading)
            .errorImage(error)
            .into(imageView);
    }

    /**
     * 加载头像
     */
    public void loadHeadPic(Context context, ImageView view, String imagePath) {
        ImageDirector.getInstance(context).imageBuilder()
            .imageUrl(imagePath)
            .loadingImage(R.drawable.bg_user_default)
            .errorImage(R.drawable.bg_user_default)
            .imageTransformation(ImageConstants.IMAGE_TRANSFOR_CROP_CIRCLE)
            .into(view);
    }

    /**
     * 加载动态头像
     */
    public void loadHeadPicGif(Context context, ImageView view, String imagePath) {
        ImageDirector.getInstance(context).imageBuilder()
            .imageUrl(imagePath)
            .loadingImage(R.drawable.bg_user_default)
            .errorImage(R.drawable.bg_user_default)
            .imageType(ImageConstants.IMAGE_TYPE_GIF)
            .imageTransformation(ImageConstants.IMAGE_TRANSFOR_CROP_CIRCLE)
            .into(view);
    }

    /**
     * @param context
     * @param view
     * @param imagePath
     * @param loading
     * @param err
     * @param radius
     */
    public void loadImageCorner(Context context, ImageView view, Object imagePath,
        @DrawableRes int loading, @DrawableRes int err, int radius,
        RoundedCornersTransformation.CornerType cornerType) {
        ImageDirector.getInstance(context).imageBuilder()
            .imageUrl(imagePath)
            .loadingImage(loading)
            .errorImage(err)
            .imageTransformation(ImageConstants.IMAGE_TRANSFOR_CROP_CORNER)
            .radius(radius)
            .cornerType(cornerType)
            .into(view);
    }

    /**
     * 去除加载图跟失败图
     */
    public void loadImageCorner(Context context, ImageView view, Object imagePath, int radius,
        RoundedCornersTransformation.CornerType cornerType) {
        loadImageCorner(context, view, imagePath, 0, 0, radius, cornerType);
    }

    /**
     * 高斯模糊
     */
    public void loadImageBlur(Context context, ImageView view, Object imagePath,
        @DrawableRes int loading, @DrawableRes int err, int radius) {
        ImageDirector.getInstance(context).imageBuilder()
            .imageUrl(imagePath)
            .loadingImage(loading)
            .errorImage(err)
            .imageTransformation(ImageConstants.IMAGE_TRANSFOR_BLUR)
            .radius(radius)
            .into(view);
    }

    /**
     * 清除缓存
     */
    public void clearMemery(Context context) {
        ImageDirector.getInstance(context).clearMemory();
    }

}
