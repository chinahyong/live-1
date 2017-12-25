package com.bixin.bixin.danmu.DanmuBase;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawableFactory;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.request.target.Target;
import java.util.Random;
import tv.guojiang.baselib.image.listener.ImageLoadingListener;
import tv.live.bx.R;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.danmu.AnimationHelper;
import com.bixin.bixin.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.HtmlUtil;
import com.bixin.bixin.library.util.NetworkImageGetter;

/**
 * Created by Live on 2016/5/12.
 */
public class DanmakuChannel extends FrameLayout implements DanmakuChannelInter {

    private static final String TAG = "Danmaku.DanmakuChannel";
    private boolean isRunning = false;
    private DanmakuActionInter mDanmakuActionInter;
    private Handler mHandler = new Handler();
    private OnClickListener mOnClickListener;
    private Random mRandom = new Random();

    public void setOnClickListener(OnClickListener l) {
        this.mOnClickListener = l;
    }

    public DanmakuChannel(Context context) {
        super(context);
        init();
    }

    public DanmakuChannel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DanmakuChannel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // LayoutInflater inflater = LayoutInflater.from(getContext());
        // inflater.inflate(R.layout.danmaku_channel_layout, null);
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        // this.setClipToOutline(false);
        // }
    }

    @Override
    public void startAnimation(DanmakuEntity entity) {
        EvtLog.e(TAG, "mStartAnimation " + Thread.currentThread().getName());
        isRunning = true;

        final View view = View.inflate(getContext(), R.layout.item_live_danmu, null);
        TextView contentView = view.findViewById(R.id.item_content);
        TextView titleView = view.findViewById(R.id.item_title);
        final LinearLayout backgroupView = view.findViewById(R.id.item_danmu_bg);
        final ImageView imageView = view.findViewById(R.id.item_user_photo);
        ImageView photoV = view.findViewById(R.id.item_user_photo_v);
        this.addView(view);

        if (entity != null) {
            ImageLoaderUtil.getInstance().loadHeadPic(getContext(), imageView, entity.headUrl);
        }
        photoV.setVisibility(entity.verified ? View.VISIBLE : View.GONE);

        TextPaint textPaint = contentView.getPaint();
        CharSequence charSequence = HtmlUtil
            .htmlTextDeal(getContext(), entity.content, new NetworkImageGetter(contentView), null);
        //设置文本的内容与宽度
        float textPaintWidth = textPaint.measureText(charSequence, 0, charSequence.length());
        contentView.setWidth((int) textPaintWidth);
        contentView.setText(charSequence);

        titleView.setText(entity.title);
        ImageLoaderUtil.getInstance()
            .loadImage(entity.backgroupImg, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL,
                new ImageLoadingListener() {
                    @Override
                    public void onLoadStarted(Drawable drawable) {

                    }

                    @Override
                    public void onLoadFailed(Drawable drawable) {

                    }

                    @Override
                    public void onLoadingComplete(Drawable resource) {
                        EvtLog.e(TAG, "mStartAnimation onLoadingComplete");
                        if (resource != null) {
                            BitmapDrawable bd = (BitmapDrawable) resource;
                            Bitmap bm = bd.getBitmap();
                            backgroupView.setBackgroundDrawable(
                                NinePatchDrawableFactory.convertBitmap(getResources(), bm, null));
                        }
                    }

                    @Override
                    public void onLoadCleared(Drawable drawable) {

                    }
                });

        if (mOnClickListener != null) {
            view.setTag(entity.data);
            view.setOnClickListener(mOnClickListener);
        }

        int width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
        final int measuredWidth = view.getMeasuredWidth();
        EvtLog.e(TAG, "measuredWidth " + measuredWidth);
        LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.width = measuredWidth;
        view.setLayoutParams(params);

        final int leftMargin = Utils.getScreenWH(getContext())[0];
        final ObjectAnimator anim = AnimationHelper
            .createObjectAnimator(getContext(), view, leftMargin, -measuredWidth);
        final int randomInt = mRandom.nextInt(200);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float tx = (Float) animation.getAnimatedValue("translationX");
                if (tx < (leftMargin - measuredWidth - randomInt)) {
                    anim.removeAllUpdateListeners();
                    isRunning = false;
                    if (mDanmakuActionInter != null) {
                        mDanmakuActionInter.pollDanmu();
                    }
                }
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            anim.cancel();
                            DanmakuChannel.this.removeView(view);
                        }
                    });
                }
            }
        });
        // anim.setAnimationListener(new Animation.AnimationListener() {
        // @Override
        // public void onAnimationStart(Animation animation) {
        //
        // }
        //
        // @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        // @Override
        // public void onAnimationEnd(Animation animation) {
        // if (!((Activity) getContext()()).isDestroyed()) {//防止内存溢出
        // new Handler().post(new Runnable() {
        // public void run() {
        // view.clearAnimation();
        // DanmakuChannel.this.removeView(view);
        // if (danAction != null) {
        // danAction.pollDanmu();
        // }
        // }
        // });
        // }
        // isRunning = false;
        // }
        //
        // @Override
        // public void onAnimationRepeat(Animation animation) {
        // }
        // });
        // view.startAnimation(anim);
        anim.start();
    }


    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void releaseView() {
        int count = this.getChildCount();
        for (int i = 0; i < count; i++) {
            this.getChildAt(i).animate().cancel();
        }
        this.removeAllViews();
    }

    @Override
    public void setDanmakuActionInter(DanmakuActionInter danAction) {
        this.mDanmakuActionInter = danAction;
    }

}
