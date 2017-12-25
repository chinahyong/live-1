package com.bixin.bixin.danmu;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import m.framework.utils.Utils;

/**
 * 动画工具类 Created by hanj on 15-6-4.
 */
@SuppressLint("NewApi")
public class AnimationHelper {
	/**
	 * 创建平移动画
	 */
	public static Animation createTranslateAnim(Context context, int fromX, int toX) {
		TranslateAnimation tlAnim = new TranslateAnimation(fromX, toX, 0, 0);
		// 自动计算时间
		// long duration = (long) (Math.abs(toX - fromX) * 1.0f /
		// ScreenUtils.getScreenW(context) * 4000);
		long duration = 10000;
		tlAnim.setDuration(duration);
		// tlAnim.setInterpolator(new DecelerateAccelerateInterpolator());
		tlAnim.setFillAfter(true);

		return tlAnim;
	}

	public static ObjectAnimator createObjectAnimator(Context context, View target, float fromX, float toX) {
		ObjectAnimator rotation = ObjectAnimator.ofFloat(target, View.TRANSLATION_X, fromX, toX);
		float duration = Math.abs(toX - fromX) / Utils.getScreenWidth(context) * 5000;
		rotation.setInterpolator(new LinearInterpolator());
		rotation.setDuration((int) duration);
		return rotation;
	}
}
