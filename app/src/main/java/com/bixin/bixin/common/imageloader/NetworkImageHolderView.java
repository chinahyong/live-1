package com.bixin.bixin.common.imageloader;

import android.view.View;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.holder.Holder;
import com.bixin.bixin.App;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.home.model.AdInfo;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import tv.live.bx.R;

/**
 * Created by Sai on 15/8/4.
 * 网络图片加载例子
 * 自定义 by 2016/6/17
 *
 * @咕叽
 */
public class NetworkImageHolderView extends Holder<AdInfo> {
	private ImageView imageView;

	public NetworkImageHolderView(View itemView) {
		super(itemView);
	}

	@Override
	protected void initView(View view) {
		//你可以通过layout文件来创建，也可以像我一样用代码创建，不一定是Image，任何控件都可以进行翻页
		imageView = (ImageView) view;
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
	}

	@Override
	public void updateUI(AdInfo adInfo) {
		ImageLoaderUtil.getInstance().loadImageCorner(App.mContext, imageView, adInfo.getAdvImg(),
				R.drawable.icon_loading, R.drawable.icon_loading,
				Utils.dpToPx(5), RoundedCornersTransformation.CornerType.ALL);
	}
}
