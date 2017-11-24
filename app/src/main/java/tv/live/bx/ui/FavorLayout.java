/**
 * Project Name:feizao
 * File Name:FavorLayout.java
 * Package Name:com.efeizao.feizao.ui
 * Date:2016-4-12下午12:04:07
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 * <p/>
 * <p/>
 * Project Name:feizao
 * File Name:FavorLayout.java
 * Package Name:com.efeizao.feizao.ui
 * Date:2016-4-12下午12:04:07
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 * <p>
 * Project Name:feizao
 * File Name:FavorLayout.java
 * Package Name:com.efeizao.feizao.ui
 * Date:2016-4-12下午12:04:07
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 */
/**
 * Project Name:feizao
 * File Name:FavorLayout.java
 * Package Name:com.efeizao.feizao.ui
 * Date:2016-4-12下午12:04:07
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
 */

package tv.live.bx.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.efeizao.bx.R;
import tv.live.bx.common.Utils;

import java.util.Random;

/**
 * ClassName:FavorLayout <br/>
 * Function: Periscope点赞效果
 * @author Live
 * @since JDK 1.6
 */
@SuppressLint("NewApi")
public class FavorLayout extends RelativeLayout {
	private static final String TAG = FavorLayout.class.getSimpleName();
	private Random random = new Random();// 用于实现随机功能
	private int dHeight;// 爱心的高度
	private int dWidth;// 爱心的宽度
	private int mStartPointX;// 开始起点位置
	private int mHeight;// FavorLayout的高度
	private int mWidth;// FavorLayout的宽度
	// 定义一个LayoutParams 用它来控制子view的位置
	private LayoutParams lp;

	// 首先定义 3个代表不同爱心的drawable,以及他们的和drawables
	private Drawable[] drawables;

	// 我为了实现 变速效果 挑选了几种插补器
	private Interpolator line = new LinearInterpolator();// 线性
	//	private Interpolator acc = new AccelerateInterpolator();// 加速
	private Interpolator dce = new DecelerateInterpolator();// 减速
	private Interpolator accdec = new AccelerateDecelerateInterpolator();// 先加速后减速
	// 在init中初始化
	private Interpolator[] interpolators;

	public FavorLayout(Context context) {
		super(context);
		// init里做一些初始化变量的操作
		init();
	}

	public FavorLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// init里做一些初始化变量的操作
		init();
	}

	// 重写onMeasure 获取控件宽高
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 注意!! 获取本身的宽高 需要在测量之后才有宽高
		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();
		mStartPointX = mWidth - Utils.dip2px(getContext(), 30) - dWidth / 2;
		Log.e(TAG, "onMeasure mStartPointX :" + mStartPointX);
	}

	private void init() {
		// 初始化显示的图片
		drawables = new Drawable[14];

		// 赋值给drawables
		drawables[0] = getResources().getDrawable(R.drawable.ic_rose_1);
		drawables[1] = getResources().getDrawable(R.drawable.ic_rose_2);
		drawables[2] = getResources().getDrawable(R.drawable.ic_rose_3);
		drawables[3] = getResources().getDrawable(R.drawable.ic_rose_4);
		drawables[4] = getResources().getDrawable(R.drawable.ic_rose_5);
		drawables[5] = getResources().getDrawable(R.drawable.ic_rose_6);
		drawables[6] = getResources().getDrawable(R.drawable.ic_rose_7);
		drawables[7] = getResources().getDrawable(R.drawable.ic_rose_8);
		drawables[8] = getResources().getDrawable(R.drawable.ic_rose_9);
		drawables[9] = getResources().getDrawable(R.drawable.ic_rose_10);
		drawables[10] = getResources().getDrawable(R.drawable.ic_rose_11);
		drawables[11] = getResources().getDrawable(R.drawable.ic_rose_12);
		drawables[12] = getResources().getDrawable(R.drawable.ic_rose_13);
		drawables[13] = getResources().getDrawable(R.drawable.ic_rose_14);
		// 获取图的宽高 用于后面的计算
		// 注意 我这里3张图片的大小都是一样的,所以我只取了一个
		dHeight = drawables[0].getIntrinsicHeight();
		dWidth = drawables[0].getIntrinsicWidth();

		// 底部 并且 水平居中
		lp = new LayoutParams(dWidth, dHeight);
		lp.addRule(CENTER_HORIZONTAL, TRUE); // 这里的TRUE 要注意 不是true
		lp.addRule(ALIGN_PARENT_BOTTOM, TRUE);

		// 初始化插补器
		interpolators = new Interpolator[1];
		interpolators[0] = line;
//        interpolators[1] = acc;
		// interpolators[2] = dce;
		// interpolators[3] = accdec;
	}

	/**
	 * 点击送热度效果
	 * @author Live
	 */
	public void addFavor() {
		if (getChildCount() > 30) return;
		ImageView imageView = new ImageView(getContext());
		// 随机选一个
		imageView.setImageDrawable(drawables[random.nextInt(14)]);
		// 设置底部 水平居中
		imageView.setLayoutParams(lp);
		addView(imageView);
		Log.v(TAG, "add后子view数:" + getChildCount());
		Animator set = getAnimator(imageView);
		set.addListener(new AnimEndListener(imageView));
		set.start();
	}

	private Animator getAnimator(View target) {
		int ratation = random.nextInt(130);
		boolean flag = random.nextBoolean();
		float ratationF = flag ? ratation : -ratation;
		ObjectAnimator rotation = ObjectAnimator.ofFloat(target, View.ROTATION, 0f, ratationF);
		rotation.setDuration(3000);

		AnimatorSet set = getEnterAnimtor(target);

		ValueAnimator bezierValueAnimator = getBezierValueAnimator(target);
		bezierValueAnimator.setInterpolator(interpolators[random.nextInt(1)]);
		AnimatorSet finalSet = new AnimatorSet();
//		 finalSet.playSequentially(set,bezierValueAnimator);
		finalSet.playTogether(set, rotation, bezierValueAnimator);
		finalSet.setTarget(target);
		return finalSet;
	}

	private AnimatorSet getEnterAnimtor(final View target) {

		ObjectAnimator scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 0.6f, 1f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 0.6f, 1f);
		AnimatorSet enter = new AnimatorSet();
		enter.setDuration(500);
		enter.setInterpolator(new LinearInterpolator());
		enter.playTogether(scaleX, scaleY);
		enter.setTarget(target);
		return enter;
	}

	private ValueAnimator getBezierValueAnimator(View target) {

		// 初始化一个贝塞尔计算器- - 传入
		BezierEvaluator evaluator = new BezierEvaluator(getPointF(2), getPointF(1));

		// 这里最好画个图 理解一下 传入了起点 和 终点
		ValueAnimator animator = ValueAnimator.ofObject(evaluator, new PointF(mStartPointX, mHeight),
				new PointF(random.nextInt(getWidth()), random.nextInt(200)));
		animator.addUpdateListener(new BezierListener(target));
		animator.setTarget(target);
		animator.setDuration(3000);
		return animator;
	}

	/**
	 * 获取中间的两个 点
	 * @param scale
	 */
	private PointF getPointF(int scale) {

		PointF pointF = new PointF();
		pointF.x = random.nextInt((mWidth));// 减去100 是为了控制 x轴活动范围,看效果 随意~~
		// 再Y轴上 为了确保第二个点 在第一个点之上,我把Y分成了上下两半 这样动画效果好一些 也可以用其他方法
		pointF.y = random.nextInt((mHeight - 100)) / scale;
		return pointF;
	}

	private class BezierListener implements ValueAnimator.AnimatorUpdateListener {

		private View target;

		public BezierListener(View target) {
			this.target = target;
		}

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			// 这里获取到贝塞尔曲线计算出来的的x y值 赋值给view 这样就能让爱心随着曲线走啦
			PointF pointF = (PointF) animation.getAnimatedValue();
			target.setX(pointF.x);
			target.setY(pointF.y);
			// 这里顺便做一个alpha动画
			target.setAlpha(1 - animation.getAnimatedFraction());
		}
	}

	/**
	 * 根据贝塞尔曲线原理 实现 TypeEvaluator 由于我们view的移动需要控制x y 所以就传入PointF 作为参数
	 */
	public class BezierEvaluator implements TypeEvaluator<PointF> {

		private PointF pointF1;// 途径的两个点
		private PointF pointF2;

		public BezierEvaluator(PointF pointF1, PointF pointF2) {
			this.pointF1 = pointF1;
			this.pointF2 = pointF2;
		}

		@Override
		public PointF evaluate(float time, PointF startValue, PointF endValue) {

			float timeLeft = 1.0f - time;
			PointF point = new PointF();// 结果

			PointF point0 = startValue;// 起点

			PointF point3 = endValue;// 终点
			// 代入公式
			point.x = timeLeft * timeLeft * timeLeft * (point0.x) + 3 * timeLeft * timeLeft * time * (pointF1.x) + 3
					* timeLeft * time * time * (pointF2.x) + time * time * time * (point3.x);

			point.y = timeLeft * timeLeft * timeLeft * (point0.y) + 3 * timeLeft * timeLeft * time * (pointF1.y) + 3
					* timeLeft * time * time * (pointF2.y) + time * time * time * (point3.y);
			return point;
		}
	}

	private class AnimEndListener extends AnimatorListenerAdapter {
		private View target;

		public AnimEndListener(View target) {
			this.target = target;
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			super.onAnimationEnd(animation);
			// 因为不停的add 导致子view数量只增不减,所以在view动画结束后remove掉
			removeView((target));
			Log.v(TAG, "removeView后子view数:" + getChildCount());
		}
	}
}
