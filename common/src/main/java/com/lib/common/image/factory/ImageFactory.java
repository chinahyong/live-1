package com.lib.common.image.factory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.lib.common.image.model.ImageEntity;

/**
 * @author Elvis
 * @date 20/11/2017
 * @description tv.guojiang.baselib.image
 */

public interface ImageFactory {

	/**
	 * 加载图片
	 *
	 * @param context
	 * @param entity  imageview不为空则显示，为空只加载
	 */
	void loadImage(@NonNull Context context, ImageEntity entity);

	/**
	 * 同步加载图片
	 * @param context
	 * @param entity
	 */
	Object loadImageSyn(@NonNull Context context, ImageEntity entity) throws Exception;

	/**
	 * 销毁
	 *
	 * @param context
	 */
	void onDestroy(@NonNull Context context);

	/**
	 * 停止
	 *
	 * @param context
	 */
	void onStop(@NonNull Context context);


	/**
	 * 取消加载
	 *
	 * @param context
	 */
	void clear(@NonNull Context context, ImageView imageView);
}
