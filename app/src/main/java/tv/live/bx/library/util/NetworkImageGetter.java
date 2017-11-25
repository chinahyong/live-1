/**
 * Project Name:feizao File Name:NetworkImageGetter.java Package
 * Name:com.efeizao.feizao.library.util Date:2015-8-25下午5:19:16
 */

package tv.live.bx.library.util;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Html.ImageGetter;
import android.widget.TextView;

import com.bumptech.glide.request.target.Target;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.imageloader.ImageSize;
import tv.live.bx.imageloader.SimpleImageLoadingListener;

/**
 * ClassName:NetworkImageGetter Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: 2015-8-25 下午5:19:16
 *
 * @author Live
 * @version 1.0
 */

/**
 * 获取网络图片
 */
public class NetworkImageGetter implements ImageGetter {
	private TextView mView;
	// 设置网络加载图片显示的宽度
	private int mDrawableWidth;

	public NetworkImageGetter(TextView view, int tvWidth) {
		init(view, tvWidth);
	}

	public NetworkImageGetter(TextView view) {
		init(view, 0);
	}

	private void init(TextView view, int tvWidth) {
		this.mView = view;
		this.mDrawableWidth = tvWidth;
	}

	@Override
	public Drawable getDrawable(final String source) {
		EvtLog.e("NetworkImageGetter", "source:" + source);
		final LevelListDrawable mDrawable = new LevelListDrawable();
		// 一般表情，从drawabl目录下解析
		if (source.contains("/emoji/")) {
			int start = source.lastIndexOf("/");
			int end = source.lastIndexOf(".png");
			String name = source.substring(start + 1, end);
			return getEmojiDrawable(name);
		}
		// gif表情，需要放大一般显示
		else if (source.contains(".gif")) {
			mDrawable.addLevel(0, 0, null);
			mDrawable.setBounds(0, 0, 50, 50);
			ImageLoaderUtil.with().loadImage(FeizaoApp.mConctext, source, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(Drawable resource) {
					// EvtLog.e("NetworkImageGetter",
					// "mDrawable.getIntrinsicWidth():" + loadedImage.getWidth()
					// + ",mDrawable.getIntrinsicHight():" +
					// loadedImage.getHeight());
					mDrawable.addLevel(1, 1, resource);
					mDrawable.setBounds(0, 0, Utils.px2px(FeizaoApp.mConctext, resource.getIntrinsicWidth()),
							Utils.px2px(FeizaoApp.mConctext, resource.getIntrinsicHeight()));
					mDrawable.setLevel(1);
					// i don't know yet a better way to refresh TextView
					// mTv.invalidate() doesn't work as expected
					CharSequence t = mView.getText();
					mView.setText(t);
				}

			});

		} else {
			mDrawable.addLevel(0, 0, null);
			mDrawable.setBounds(0, 0, 50, 50);
			ImageLoaderUtil.with().loadImage(FeizaoApp.mConctext, source, ImageSize.SIZE_ORIGINAL, ImageSize.SIZE_ORIGINAL, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(Drawable resource) {
					// EvtLog.e("NetworkImageGetter",
					// "mDrawable.getIntrinsicWidth():" + loadedImage.getWidth()
					// + ",mDrawable.getIntrinsicHight():" +
					// loadedImage.getHeight());
					mDrawable.addLevel(1, 1, resource);

					// int width = mDrawableWidth < loadedImage.getWidth() ? mDrawableWidth
					// : loadedImage.getWidth();
					int width, heigth;
					if (mDrawableWidth > 0) {
						width = mDrawableWidth;
						float radio = (float) resource.getIntrinsicHeight() / resource.getIntrinsicWidth();
						heigth = (int) (mDrawableWidth * radio);
					} else {
						width = Utils.px2px(FeizaoApp.mConctext, resource.getIntrinsicWidth());
						heigth = Utils.px2px(FeizaoApp.mConctext, resource.getIntrinsicHeight());
					}
					// EvtLog.e("NetworkImageGetter", "width:" + mDrawableWidth +
					// ",height:" + height);
					mDrawable.setBounds(0, 0, width, heigth);
					mDrawable.setLevel(1);
					// i don't know yet a better way to refresh TextView
					// mTv.invalidate() doesn't work as expected
					CharSequence t = mView.getText();
					mView.setText(t);
				}
			});

		}
		return mDrawable;
	}

	/**
	 * 获取表情图标
	 *
	 * @param name 表情名称 如：1f319
	 * @return
	 */
	private Drawable getEmojiDrawable(String name) {
		int imageId = Utils.getFiledDrawable(Constants.COMMON_EMOTION_PIX, name);
		Drawable emojiDrawable = FeizaoApp.mConctext.getResources().getDrawable(imageId);
		// EvtLog.e("NetworkImageGetter", "mDrawable.getIntrinsicWidth():" +
		// emojiDrawable.getIntrinsicWidth()
		// + ",mDrawable.getIntrinsicHight():" +
		// emojiDrawable.getIntrinsicHeight());
		emojiDrawable.setBounds(0, 0,
				(int) (FeizaoApp.mConctext.getResources().getDimension(R.dimen.image_emoji_width)),
				(int) (FeizaoApp.mConctext.getResources().getDimension(R.dimen.image_emoji_width)));
		return emojiDrawable;
	}

}