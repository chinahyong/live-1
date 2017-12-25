/**
 * Project Name:feizao File Name:NetworkImageGetter.java Package
 * Name:com.efeizao.feizao.library.util Date:2015-8-25下午5:19:16
 */

package com.bixin.bixin.library.util;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Html.ImageGetter;
import android.widget.TextView;
import com.bixin.bixin.App;
import com.bumptech.glide.request.target.Target;
import tv.guojiang.baselib.image.listener.ImageLoadingListener;
import tv.guojiang.baselib.image.model.ImageSize;
import tv.live.bx.R;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.imageloader.ImageLoaderUtil;

/**
 * ClassName:NetworkImageGetter Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: 2015-8-25 下午5:19:16
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
            ImageLoaderUtil.getInstance()
                .loadImage(source, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL,
                    new ImageLoadingListener() {
                        @Override
                        public void onLoadStarted(Drawable drawable) {

                        }

                        @Override
                        public void onLoadFailed(Drawable drawable) {

                        }

                        @Override
                        public void onLoadingComplete(Drawable resource) {
                            // EvtLog.e("NetworkImageGetter",
                            // "mDrawable.getIntrinsicWidth():" + loadedImage.getWidth()
                            // + ",mDrawable.getIntrinsicHight():" +
                            // loadedImage.getHeight());
                            mDrawable.addLevel(1, 1, resource);
                            mDrawable.setBounds(0, 0,
                                Utils.px2px(App.mContext, resource.getIntrinsicWidth()),
                                Utils.px2px(App.mContext, resource.getIntrinsicHeight()));
                            mDrawable.setLevel(1);
                            // i don't know yet a better way to refresh TextView
                            // mTv.invalidate() doesn't work as expected
                            CharSequence t = mView.getText();
                            mView.setText(t);
                        }

                        @Override
                        public void onLoadCleared(Drawable drawable) {

                        }

                    });

        } else {
            mDrawable.addLevel(0, 0, null);
            mDrawable.setBounds(0, 0, 50, 50);
            ImageLoaderUtil.getInstance()
                .loadImage(source, ImageSize.SIZE_ORIGINAL, ImageSize.SIZE_ORIGINAL,
                    new ImageLoadingListener() {
                        @Override
                        public void onLoadStarted(Drawable drawable) {

                        }

                        @Override
                        public void onLoadFailed(Drawable drawable) {

                        }

                        @Override
                        public void onLoadingComplete(Drawable resource) {
                            mDrawable.addLevel(1, 1, resource);
                            int width, heigth;
                            if (mDrawableWidth > 0) {
                                width = mDrawableWidth;
                                float radio = (float) resource.getIntrinsicHeight() / resource
                                    .getIntrinsicWidth();
                                heigth = (int) (mDrawableWidth * radio);
                            } else {
                                width = Utils
                                    .px2px(App.mContext, resource.getIntrinsicWidth());
                                heigth = Utils
                                    .px2px(App.mContext, resource.getIntrinsicHeight());
                            }
                            mDrawable.setBounds(0, 0, width, heigth);
                            mDrawable.setLevel(1);
                            CharSequence t = mView.getText();
                            mView.setText(t);
                        }

                        @Override
                        public void onLoadCleared(Drawable drawable) {

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
        Drawable emojiDrawable = App.mContext.getResources().getDrawable(imageId);
        emojiDrawable.setBounds(0, 0,
            (int) (App.mContext.getResources().getDimension(R.dimen.image_emoji_width)),
            (int) (App.mContext.getResources().getDimension(R.dimen.image_emoji_width)));
        return emojiDrawable;
    }

}