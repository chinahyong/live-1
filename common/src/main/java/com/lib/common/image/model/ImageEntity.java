package com.lib.common.image.model;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.TransitionFactory;
import com.lib.common.image.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Elvis
 * @date 20/11/2017
 * @description tv.guojiang.baselib.image
 */

public class ImageEntity {

	// 图片宽高
	public ImageSize imageSize = new ImageSize(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
	// 加载失败图
	public int errorImage;
	public Drawable errorDrawable;
	// 正在加载loading图
	public int loadingImage;
	public Drawable loadingDrawable;
	// 显示的图片地址
	public Object imageUrl;
	// 显示的图片类型
	public int imageType = ImageConstants.IMAGE_TYPE_DRAWABLE;
	public ImageView.ScaleType scaleType = ImageView.ScaleType.CENTER_CROP;
	// 加载监听事件
	public ImageLoadingListener imageLoadingListener;

	// 图片外形类型
	public List<Integer> imageTransfor = new ArrayList<>();
	public RoundedCornersTransformation.CornerType cornerType;
	// blur/corner
	public int radius;
	public int cropWidth;
	public int cropHeight;
	public CropTransformation.CropType cropType = CropTransformation.CropType.CENTER;
	// 过度动画设置
	public TransitionFactory transitionFactory;
	// mask
	public int maskId;
	// color
	public int colorFilter;
	// 缓存策略
	public DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.ALL;
	// 绑定的控件
	public ImageView imageView;
}
