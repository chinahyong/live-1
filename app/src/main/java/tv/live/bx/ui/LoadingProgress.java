package tv.live.bx.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import tv.live.bx.R;


@SuppressLint("NewApi")
public class LoadingProgress extends LinearLayout {

	protected int mShowImageId; // 显示的图片
	protected int mLoadingProgressId; // 进度图片
	protected int mLoadingInfoId; // 进度信息

	protected ImageView mShowImage; // 显示的图片
	protected ImageView mLoadingProgress; // 进度图片
	protected TextView mLoadingInfo; // 进度信息

	protected RotateAnimation mAnimation;

	// 重载监听
	protected onProgressClickListener reloadListener;

	protected boolean mLoadFailed = false; // 判断是否加载失败

	/**
	 * 重新加载监听器
	 * @author fangyuehan
	 */
	public interface onProgressClickListener {

		/**
		 * 数据加载失败下重新载入
		 */
		void onReLoad(View v);

		/**
		 * 普通点击
		 */
		void onClick(View v);
	}

	public void setShowImage(int imageId) {
		if (mShowImage != null) {
			mShowImage.setImageResource(imageId);
			mShowImage.setVisibility(View.VISIBLE);
		}
	}

	public void setProgressClickListener(onProgressClickListener listener) {
		this.reloadListener = listener;
	}

	public LoadingProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoadingProgress, 0, 0);
		mShowImageId = typedArray.getResourceId(R.styleable.LoadingProgress_LoadingImage, 0);
		mLoadingProgressId = typedArray.getResourceId(R.styleable.LoadingProgress_LoadingProgress, 0);
		mLoadingInfoId = typedArray.getResourceId(R.styleable.LoadingProgress_LoadingInfo, 0);
		typedArray.recycle();
		typedArray = null;
		mLoadFailed = false;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (mShowImageId != 0) {
			mShowImage = (ImageView) findViewById(mShowImageId);
			mShowImage.setVisibility(View.VISIBLE);
		}
		if (mLoadingProgressId != 0) {
			mLoadingProgress = (ImageView) findViewById(mLoadingProgressId);
			mLoadingProgress.setVisibility(View.GONE);
		}
		if (mLoadingInfoId != 0) {
			mLoadingInfo = (TextView) findViewById(mLoadingInfoId);
			mLoadingInfo.setVisibility(View.GONE);
		}
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (reloadListener != null) {
					if (mLoadFailed) {
						reloadListener.onReLoad(v);
					} else {
						reloadListener.onClick(v);
					}
				}
			}
		});
	}

	public void Start(String msg) {
		Start(msg, 0);
	}

	public void Start(String msg, int imageId) {
		this.setVisibility(View.VISIBLE);
		mLoadFailed = false;

		if (mShowImage != null) {
			if (imageId > 0) {
				mShowImage.setVisibility(View.VISIBLE);
				mShowImage.setImageResource(imageId);
			} else {
				mShowImage.setVisibility(View.GONE);
			}
		}
		if (mLoadingInfo != null) {
			if (msg != null) {
				mLoadingInfo.setVisibility(View.VISIBLE);
				mLoadingInfo.setText(msg);
			} else {
				mLoadingInfo.setVisibility(View.GONE);
			}
		}
		if (mLoadingProgress != null) {
			mLoadingProgress.setVisibility(View.VISIBLE);
			if (mAnimation != null) {
				mAnimation.cancel();
			}
			mAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
					0.5f);
			mAnimation.setDuration(1200);
			mAnimation.setInterpolator(new LinearInterpolator());
			mAnimation.setRepeatCount(-1);
			mAnimation.setStartOffset(0);
			mLoadingProgress.startAnimation(mAnimation);
		}
	}

	public void Succeed(int imageId) {
		Succeed(null, imageId);
	}

	/**
	 * 设置进度条样式
	 */
	public void setProcessImageView(int imageId) {
		if (imageId > 0) {
			if (mLoadingProgress != null) {
				mLoadingProgress.setImageResource(imageId);
			}
		}
	}

	public void Succeed(String msg, int imageId) {
		if (mShowImage != null) {
			if (imageId > 0) {
				mShowImage.setVisibility(View.VISIBLE);
				mShowImage.setImageResource(imageId);
			} else {
				mShowImage.setVisibility(View.GONE);
			}
		}
		if (mLoadingInfo != null) {
			if (msg != null) {
				mLoadingInfo.setVisibility(View.VISIBLE);
				mLoadingInfo.setText(msg);
			} else {
				mLoadingInfo.setVisibility(View.GONE);
			}
		}
		if (mLoadingProgress != null) {
			mLoadingProgress.clearAnimation();
			mLoadingProgress.setVisibility(View.GONE);
		}

		if (mAnimation != null) {
			mAnimation.cancel();
		}
	}

	public void Failed(String msg) {
		Failed(msg, 0);
	}

	public void Failed(int imageId) {
		Failed(null, imageId);
	}

	@SuppressLint("NewApi")
	public void Failed(String msg, int imageId) {

		this.setVisibility(View.VISIBLE);
		mLoadFailed = true;

		if (mShowImage != null) {
			if (imageId > 0) {
				mShowImage.setVisibility(View.VISIBLE);
				mShowImage.setImageResource(imageId);
			} else {
				mShowImage.setVisibility(View.GONE);
			}
		}
		if (mLoadingInfo != null) {
			if (msg != null) {
				mLoadingInfo.setVisibility(View.VISIBLE);
				mLoadingInfo.setText(msg);
			} else {
				mLoadingInfo.setVisibility(View.GONE);
			}
		}
		if (mLoadingProgress != null) {
			mLoadingProgress.clearAnimation();
			mLoadingProgress.setVisibility(View.GONE);
		}
		if (mAnimation != null) {
			mAnimation.cancel();
		}
	}

	public void setLoadingInfo(String msg) {
		mLoadingInfo.setText(msg);
	}

	public void Hide() {
		this.setVisibility(View.GONE);
	}
}
