package tv.live.bx.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.efeizao.bx.R;

public class ListPullnReleaseHintView extends PullnReleaseHintView {

	private int mArrowImageViewId;
	private int mHintTextViewId;
	private int mProgressRingId;
	// 显示箭头的ImageView
	private ImageView mImgArrow;
	// 显示提示文字的TextView
	private TextView mTxtHint;
	// 显示进度圆环的ImageView
	private ImageView mImgProgressRing;

	private volatile boolean mRotated;

	private RotateAnimation mRotateAnimationClockwise;
	private RotateAnimation mRotateAnimationAntiClockwise;
	private RotateAnimation mProgressAnimation;

	private String mPullToInvoke;
	private String mReleaseToInvoke;

	public ListPullnReleaseHintView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.ListPullnReleaseHintView, 0, 0);

		mArrowImageViewId = typedArray.getResourceId(
				R.styleable.ListPullnReleaseHintView_arrow, 0);
		mHintTextViewId = typedArray.getResourceId(
				R.styleable.ListPullnReleaseHintView_txt_hint, 0);
		mProgressRingId = typedArray.getResourceId(
				R.styleable.ListPullnReleaseHintView_progress_ring, 0);

		mPullToInvoke = typedArray
				.getString(R.styleable.ListPullnReleaseHintView_pull_to_invoke);
		mReleaseToInvoke = typedArray
				.getString(R.styleable.ListPullnReleaseHintView_release_to_invoke);

		typedArray.recycle();

		mRotateAnimationClockwise = new RotateAnimation(0, 180,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateAnimationClockwise.setDuration(200);
		mRotateAnimationClockwise.setFillAfter(true);

		mRotateAnimationAntiClockwise = new RotateAnimation(180, 0,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateAnimationAntiClockwise.setDuration(200);
		mRotateAnimationAntiClockwise.setFillAfter(true);

		mProgressAnimation = new RotateAnimation(0f, 360f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mProgressAnimation.setDuration(1200);
		mProgressAnimation.setInterpolator(new LinearInterpolator());
		mProgressAnimation.setRepeatCount(-1);
		mProgressAnimation.setStartOffset(0);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mImgArrow = (ImageView) findViewById(mArrowImageViewId);
		mTxtHint = (TextView) findViewById(mHintTextViewId);
		mImgProgressRing = (ImageView) findViewById(mProgressRingId);
	}

	@Override
	public void updateHintView(int maxOffset, int curOffset) {
		if (curOffset < maxOffset && mRotated) {
			mRotated = false;
			if (mImgArrow != null) {
				mImgArrow.clearAnimation();
				mImgArrow.startAnimation(mRotateAnimationAntiClockwise);
			}
			if (null != mPullToInvoke) {
				mTxtHint.setText(mPullToInvoke);
			}
		} else if (curOffset >= maxOffset && !mRotated) {
			mRotated = true;
			if (mImgArrow != null) {
				mImgArrow.clearAnimation();
				mImgArrow.startAnimation(mRotateAnimationClockwise);
			}
			if (null != mReleaseToInvoke) {
				mTxtHint.setText(mReleaseToInvoke);
			}
		}

	}

	@Override
	public void showWaitHintView() {
		super.showWaitHintView();
		if (mImgProgressRing == null) {
			return;
		}
		// Drawable drawable = mImgProgressRing.getDrawable();
		// if (drawable != null && drawable instanceof AnimationDrawable) {
		// ((AnimationDrawable)drawable).stop();
		// ((AnimationDrawable)drawable).start();
		// }
		if (mImgProgressRing != null) {
			mImgProgressRing.clearAnimation();
			mImgProgressRing.startAnimation(mProgressAnimation);
		}
	}

	@Override
	public void showPullHintView() {
		super.showPullHintView();
		if (mImgProgressRing == null) {
			return;
		}
		// Drawable drawable = mImgProgressRing.getDrawable();
		// if (drawable != null && drawable instanceof AnimationDrawable) {
		// ((AnimationDrawable)drawable).stop();
		// }
		if (mImgProgressRing != null) {
			mImgProgressRing.clearAnimation();
		}
	}

}
