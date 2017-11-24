package tv.live.bx.common;

import android.annotation.SuppressLint;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.ImageView;

public class Anims {

	private static Anims moAnims;

	public static Anims getInstance() {
		if (moAnims == null)
			moAnims = new Anims();
		return moAnims;
	}

	@SuppressLint("HandlerLeak")
	public void rotateY(final ImageView poIv, final int piDuration,
			final int piAfterRes, final int piAfterResRev, boolean mbToLeft) {
		final int MSG_TYPE_HALF_TIME = 0;
		Animation loAnim = new Anims.RotateYHalf(poIv, mbToLeft);
		// loAnim.setFillBefore(true);
		loAnim.setFillAfter(true);
		loAnim.setDuration(piDuration);
		loAnim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// poIv.setImageResource(piAfterRes);
			}
		});
		poIv.startAnimation(loAnim);
		final Handler loHandler = new Handler() {
			@Override
			public void handleMessage(Message poMsg) {
				switch (poMsg.what) {
				case MSG_TYPE_HALF_TIME:
					poIv.setImageResource(piAfterResRev);
					break;
				}
			}
		};
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(piDuration / 2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				loHandler.sendEmptyMessage(MSG_TYPE_HALF_TIME);
			}
		}.start();
	}

	/************************************** Inner Classes ******************************************/
	class RotateYHalf extends Animation {
		private boolean mbIsToLeft;
		private View moV;

		public RotateYHalf(View poV, boolean pbIsToLeft) {
			mbIsToLeft = pbIsToLeft;
			moV = poV;
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			int liDegree = mbIsToLeft ? 180 : -180;
			Matrix matrix = t.getMatrix();
			Camera camera = new Camera();
			camera.save();
			camera.rotateY(liDegree * interpolatedTime);
			camera.getMatrix(matrix);
			camera.restore();
			matrix.preTranslate(-moV.getMeasuredWidth() / 2,
					-moV.getMeasuredHeight() / 2);
			matrix.postTranslate(moV.getMeasuredWidth() / 2,
					moV.getMeasuredHeight() / 2);
		}
	}
}
