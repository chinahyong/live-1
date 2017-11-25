package tv.live.bx.danmu.DanmuBase;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawableFactory;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.target.Target;

import java.util.Random;

import tv.live.bx.R;
import tv.live.bx.common.Utils;
import tv.live.bx.danmu.AnimationHelper;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.imageloader.SimpleImageLoadingListener;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.library.util.HtmlUtil;
import tv.live.bx.library.util.NetworkImageGetter;

/**
 * Created by Live on 2016/5/12.
 */
public class BroadcastDanmakuChannel extends FrameLayout implements DanmakuChannelInter {

	private static final String TAG = "Danmaku.BroadcastDanmakuChannel";
	private boolean isRunning = false;
	private DanmakuActionInter mDanmakuActionInter;
	private Handler mHandler = new Handler();
	private OnClickListener mOnClickListener;
	private Random mRandom = new Random();

	public void setOnClickListener(OnClickListener l) {
		this.mOnClickListener = l;
	}

	public BroadcastDanmakuChannel(Context context) {
		super(context);
		init();
	}

	public BroadcastDanmakuChannel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BroadcastDanmakuChannel(Context context, AttributeSet attrs, int defStyleAttr) {
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
		final View view = View.inflate(getContext(), R.layout.item_live_broastcast_danmu, null);

		TextView contentView = (TextView) view.findViewById(R.id.item_content);
		ImageView embellishIv = (ImageView) view.findViewById(R.id.item_embellish);
		final ImageView backgroupView = (ImageView) view.findViewById(R.id.item_danmu_bg);
		ImageView photo = (ImageView) view.findViewById(R.id.item_danmu_photo);
		ImageView broastcastUp = (ImageView) view.findViewById(R.id.item_broastcast_up);
		ImageView broastcastDown = (ImageView) view.findViewById(R.id.item_broastcast_down);

		this.addView(view);

		if (entity != null) {
			TextPaint textPaint = contentView.getPaint();
			CharSequence charSequence = HtmlUtil.htmlTextDeal(getContext(), entity.content, new NetworkImageGetter(contentView), null);
			contentView.setText(charSequence);
			//设置文本的内容与宽度(由于图片是一步加载，显示计算没有算上图片，默认加80dp)
			float textPaintWidth = textPaint.measureText(charSequence, 0, charSequence.length()) + Utils.dip2px(getContext(), 80);
			contentView.setWidth((int) textPaintWidth);


			//计算广播背景的宽度
			int bgWidth = (int) textPaintWidth + Utils.dip2px(getContext(), 40);
			if (bgWidth < Utils.dip2px(getContext(), 135)) {
				bgWidth = Utils.dip2px(getContext(), 135);
			}

			createBroastcastBgAnimation(broastcastUp, -200, bgWidth);
			createBroastcastBgAnimation(broastcastDown, 200, -bgWidth);

			if (TextUtils.isEmpty(entity.embellishImg)) {
				embellishIv.setVisibility(View.GONE);
			} else {
				if (entity.embellishImg.endsWith(".gif")) {
					ImageLoaderUtil.with().loadGif(getContext(), embellishIv, entity.embellishImg);
				} else {
					ImageLoaderUtil.with().loadImage(getContext(), embellishIv, entity.embellishImg);
				}
			}

			if (TextUtils.isEmpty(entity.headUrl)) {
				photo.setVisibility(View.GONE);
			} else {
				if (entity.headUrl.endsWith(".gif")) {
					ImageLoaderUtil.with().loadGif(getContext(), photo, entity.headUrl);
				} else {
					ImageLoaderUtil.with().loadImage(getContext(), photo, entity.headUrl);
				}
			}

			ImageLoaderUtil.with().loadImage(getContext(), entity.backgroupImg, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(Drawable resource) {
					EvtLog.e(TAG, "mStartAnimation onLoadingComplete");
					if (resource != null && resource instanceof GlideBitmapDrawable) {
						GlideBitmapDrawable bd = (GlideBitmapDrawable) resource;
						Bitmap bm = bd.getBitmap();
						backgroupView.setBackgroundDrawable(NinePatchDrawableFactory.convertBitmap(getResources(), bm, null));
					}
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
			LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
			params.width = measuredWidth;
			view.setLayoutParams(params);
			EvtLog.e(TAG, "measuredWidth " + measuredWidth + " textPaintWidth：" + textPaintWidth);
			final int leftMargin = Utils.getScreenWH(getContext())[0];
			final ObjectAnimator anim = AnimationHelper.createObjectAnimator(getContext(), view, leftMargin, -measuredWidth);
			final int randomInt = mRandom.nextInt(200);
			EvtLog.e(TAG, "ObjectAnimator duration" + anim.getDuration());
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
								EvtLog.e(TAG, "removeView");
								anim.cancel();
								BroadcastDanmakuChannel.this.removeView(view);
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
			// if (!((Activity) getContext()).isDestroyed()) {//防止内存溢出
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

	/**
	 * 广播两道光的动效
	 *
	 * @param target
	 * @param tranFromX
	 * @param tranToX
	 */
	private void createBroastcastBgAnimation(View target, int tranFromX, int tranToX) {
		ObjectAnimator broastcastUpAnimator = ObjectAnimator.ofFloat(target, View.TRANSLATION_X, tranFromX, tranToX);
		float tranDuration = Math.abs((tranToX - tranFromX) / 0.65f);
		broastcastUpAnimator.setDuration((int) tranDuration);
		broastcastUpAnimator.setRepeatCount(20);
		broastcastUpAnimator.start();
	}

}
