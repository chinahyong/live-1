package com.bigkoo.convenientbanner.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.wxy.adbanner.entity.AdInfo;

import tv.live.bx.R;
import tv.live.bx.imageloader.ImageLoaderUtil;

/**
 * Created by Sai on 15/8/4.
 * 网络图片加载例子
 * 自定义 by 2016/6/17
 *
 * @果酱
 */
public class NetworkImageHolderView implements Holder<AdInfo> {
	private ImageView imageView;

	@Override
	public View createView(Context context) {
		//你可以通过layout文件来创建，也可以像我一样用代码创建，不一定是Image，任何控件都可以进行翻页
		imageView = new ImageView(context);
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		return imageView;
	}

	@Override
	public void UpdateUI(Context context, int position, AdInfo data) {
		ImageLoaderUtil.with().loadImage(context, imageView, data.getAdvImg(), R.drawable.icon_loading, -1);
	}
}
