package com.bixin.bixin.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gj.effect.EffectComposition;
import com.gj.effect.EffectGiftLoader;
import com.gj.effect.GJEffectView;

import tv.live.bx.R;
import com.bixin.bixin.base.act.BaseFragmentActivity;
import com.bixin.bixin.common.config.UserInfoConfig;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.ui.StrokeTextView;

/**
 * 直播间礼物动效预览页面
 */
public class GiftEffectPreviewActivity extends BaseFragmentActivity implements View.OnClickListener {
	//动画zip文件地址
	private String androidEffectUrl;
	private String mGiftName;

	//特效执行view
	private GJEffectView mGJEffectView;
	private RelativeLayout mGiftGifInfoLayout;
	private ImageView mGiftGiftUserPhoto;
	private StrokeTextView mGiftGiftUserName, mGiftGiftTip;
	private static int MSG_PLAY_GIFT_EFFECT = 1001;

	private RelativeLayout mLoadLayout;

	@Override
	public void initWidgets() {
		mGJEffectView = (GJEffectView) findViewById(R.id.live_gift_effect);
		mLoadLayout = (RelativeLayout) findViewById(R.id.playing_loadingLayout);
		mGiftGifInfoLayout = (RelativeLayout) findViewById(R.id.live_gift_gifview_info);
		mGiftGiftUserPhoto = (ImageView) findViewById(R.id.item_gif_user_photo);
		mGiftGiftUserName = (StrokeTextView) findViewById(R.id.item_gif_user_name);
		mGiftGiftTip = (StrokeTextView) findViewById(R.id.item_gif_gift_name);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_gift_effect_preview;
	}


	@Override
	protected void setEventsListeners() {
		findViewById(R.id.activity_mount_preview).setOnClickListener(this);
	}

	/**
	 * 获取数据
	 */
	@Override
	protected void initData(Bundle bundle) {
		Intent intent = getIntent();
		androidEffectUrl = intent.getStringExtra("androidEffect");
		mGiftName = intent.getStringExtra("pname");
		updateGifGiftInfo(mGiftName);
		sendEmptyMsg(MSG_PLAY_GIFT_EFFECT);
	}

	@Override
	public void onClick(View v) {
		this.finish();
	}

	@Override
	protected void handleMessage(Message msg) {
		super.handleMessage(msg);
		if (msg.what == MSG_PLAY_GIFT_EFFECT) {
			effectHandle();
		}
	}

	/**
	 * 特效处理
	 */
	private void effectHandle() {
		if (TextUtils.isEmpty(androidEffectUrl)) {
			return;
		}
		final AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				mGJEffectView.removeAllListeners();
				mGJEffectView.removeAllViews();
				mGJEffectView.setVisibility(View.GONE);
				mGiftGifInfoLayout.setVisibility(View.GONE);
				sendEmptyMsgDelayed(MSG_PLAY_GIFT_EFFECT, 1000);
			}
		};

		final EffectComposition.OnCompositionLoadedListener onCompositionLoadedListener = new EffectComposition.OnCompositionLoadedListener() {
			@Override
			public void onCompositionLoaded(EffectComposition composition) {
				EvtLog.e(TAG, "showGifEffect...loading EffectComposition：" + composition);
				mLoadLayout.setVisibility(View.GONE);
				mGiftGifInfoLayout.setVisibility(View.VISIBLE);
				// 开始显示动画
				if (composition != null) {
					mGJEffectView.setComposition(composition);
					mGJEffectView.setVisibility(View.VISIBLE);
					mGJEffectView.startAnimation(animatorListenerAdapter);
				}
			}
		};
//		if (true) {
//			EffectGiftLoader.getInstance(mActivity).loadData(androidEffectUrl, new LoadingListener() {
//				@Override
//				public void onLoadingProcess(long total, long current) {
//
//				}
//
//				@Override
//				public void onLoadingSuccess(String targetFilePath) {
//					//如果是网络下载成功，会返回临时文件后缀
//					CompositionLoader compositionLoader = new CompositionLoader(mActivity, onCompositionLoadedListener);
//					compositionLoader.execute("/storage/emulated/0/Android/data/com.guojiang.meitu.boys/effect/165.zip");
//				}
//
//				@Override
//				public void onLoadingFailure(Throwable e) {
//					onCompositionLoadedListener.onCompositionLoaded(null);
//				}
//			});
//		} else {
		EffectGiftLoader.getInstance(mActivity).loadDataForComposition(androidEffectUrl, onCompositionLoadedListener);
//		}
	}

	private void updateGifGiftInfo(String pname) {
		UserInfoConfig userInfoConfig = UserInfoConfig.getInstance();
		ImageLoaderUtil.getInstance().loadHeadPic(mActivity.getApplicationContext(), mGiftGiftUserPhoto, userInfoConfig.headPic);
		mGiftGiftUserName.setText(userInfoConfig.nickname);
		mGiftGiftTip.setText(mActivity.getResources().getString(R.string.live_gif_gift_name_tip, pname, "1"));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mGJEffectView.removeAllListeners();
		mHandler.removeCallbacksAndMessages(null);
	}

}
