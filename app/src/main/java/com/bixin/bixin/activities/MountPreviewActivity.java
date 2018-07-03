package com.bixin.bixin.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gj.effect.EffectComposition;
import com.gj.effect.EffectGiftLoader;
import com.gj.effect.GJEffectView;

import tv.live.bx.R;
import com.bixin.bixin.base.act.BaseFragmentActivity;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.ui.TypeTextView;
import com.bixin.bixin.util.UiHelper;

public class MountPreviewActivity extends BaseFragmentActivity implements View.OnClickListener {

    //用户id
    private String uid;
    //昵称
    private String nickname;
    private String level;
    //座驾id
    private String mountId;
    //显示动画
    // moundId > 0表示为有座驾；
    private boolean showMount;
    //座驾名称
    private String mountName;
    //座驾行为名称
    private String mountAction;
    //座驾动画zip文件地址
    private String androidMount;

    //动画效果
    private Animation mUserEntryLevelAlphaAnimation, mUserEntryEffectTranAnimation, mUserEntryLayoutTransAnimation;

    protected FrameLayout mUserEntryLayout;
    //进出动画整体背景
    private RelativeLayout mUserEntryBackground;
    //用户等级logo
    private ImageView mUserEntryLevel;
    //横移ImageView
    private ImageView mUserEntryEffect;
    private TypeTextView mUserEntryText;
    private TextView mUserGuardEntryText;

    //特效执行view
    private GJEffectView mGJEffectView;

    private int remainAnimationNums = 0;

    @Override
    public void initWidgets() {
        // 用户进入动效

        mUserEntryLayout = (FrameLayout) findViewById(R.id.item_user_entry_layout);
        mUserEntryBackground = (RelativeLayout) mUserEntryLayout
            .findViewById(R.id.item_user_entry_bg);
        mUserEntryEffect = (ImageView) mUserEntryLayout.findViewById(R.id.item_effect_background);
        mUserEntryLevel = (ImageView) mUserEntryLayout.findViewById(R.id.item_level);
        mUserGuardEntryText = (TextView) mUserEntryLayout
            .findViewById(R.id.item_user_guard_entry_text);
        mUserEntryText = (TypeTextView) mUserEntryLayout.findViewById(R.id.item_user_entry_text);
        mGJEffectView = (GJEffectView) findViewById(R.id.live_gift_effect);

    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_mount_preview;
    }

    @Override
    protected void initMembers() {
        Bundle bundle = getIntent().getExtras();
        uid = bundle.getString("uid");
        nickname = bundle.getString("nickname");
        level = bundle.getString("level");
        mountId = bundle.getString("mountId");
        mountName = bundle.getString("mountName");
        mountAction = bundle.getString("mountAction");
        androidMount = bundle.getString("androidMount");
        showMount = Utils.getInteger(mountId, 0) > 0;

        if (!showMount) {
            UiHelper.showShortToast(mActivity, "座驾Id错误~");
            this.finish();
        } else {
            sendEmptyMsg(1);
        }
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

    }

    @Override
    protected void handleMessage(Message msg) {
        if (remainAnimationNums == 0 && msg.what == 1) {
            userEntryEffectHandle();

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    userEntryCarEffectHandle();
                }
            }, 200);

            remainAnimationNums = 2;
        }
    }

    @Override
    public void onClick(View v) {
        this.finish();
    }

    /**
     *
     */
    private void userEntryCarEffectHandle() {
        final AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mGJEffectView.removeAllListeners();
                mGJEffectView.removeAllViews();
                mGJEffectView.setVisibility(View.GONE);
                // 动画显示结束
                remainAnimationNums--;
                sendEmptyMsg(1);
            }
        };
        EffectGiftLoader.getInstance(mActivity).loadDataForComposition(androidMount,
            new EffectComposition.OnCompositionLoadedListener() {
                @Override
                public void onCompositionLoaded(EffectComposition composition) {
                    EvtLog.e(TAG, "showGifEffect...loading EffectComposition：" + composition);
                    // 开始显示动画
                    if (composition != null) {
                        mGJEffectView.setComposition(composition);
                        mGJEffectView.setVisibility(View.VISIBLE);
                        mGJEffectView.startAnimation(animatorListenerAdapter);
                    }
                }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        remainAnimationNums = -1;
        mHandler.removeMessages(1);
    }

    /**
     * 用户进入直播间动效处理
     */

    private void userEntryEffectHandle() {
        if (mUserEntryLevelAlphaAnimation == null) {
            mUserEntryLevelAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            mUserEntryLevelAlphaAnimation.setDuration(200);
        }
        mUserEntryLevelAlphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                CharSequence charSequence;
                SpannableString loMountName = new SpannableString(mountName);
                loMountName.setSpan(new ForegroundColorSpan(0xfffff000), 0, loMountName.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                charSequence = SpannableStringBuilder.valueOf(nickname).append(" ")
                    .append(mountAction).append(loMountName).
                        append(mActivity.getResources().getString(R.string.live_user_entry_text));

                mUserEntryText.start(charSequence);
                mUserEntryText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        if (mUserEntryEffectTranAnimation == null) {
            mUserEntryEffectTranAnimation = AnimationUtils
                .loadAnimation(mActivity, R.anim.anim_live_user_effect_trans);

        }
        mUserEntryEffectTranAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mUserEntryEffect.setVisibility(View.INVISIBLE);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mUserEntryLayout.setVisibility(View.INVISIBLE);
                        remainAnimationNums--;
                        sendEmptyMsg(1);
                    }
                }, 1000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        if (mUserEntryLayoutTransAnimation == null) {
            mUserEntryLayoutTransAnimation = AnimationUtils
                .loadAnimation(mActivity, R.anim.anim_live_user_entry_trans);
        }
        mUserEntryLayoutTransAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mUserEntryLevel.setVisibility(View.VISIBLE);
                mUserEntryLevel.startAnimation(mUserEntryLevelAlphaAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mUserEntryText.setTextColor(0xffffffff);
        mUserEntryBackground.setBackgroundResource(R.drawable.bg_car_user_entry);
        mUserEntryEffect.setImageResource(R.drawable.effect_user_entry_car);

        mUserEntryEffect.setVisibility(View.INVISIBLE);
        ImageLoaderUtil.getInstance().loadImage(mUserEntryLevel,
            Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, level));
        mUserEntryLevel.setVisibility(View.INVISIBLE);
        mUserGuardEntryText.setVisibility(View.GONE);
        mUserEntryText.setVisibility(View.INVISIBLE);
        mUserEntryText.setText("");
        mUserEntryLayout.startAnimation(mUserEntryLayoutTransAnimation);
        mUserEntryLayout.setVisibility(View.VISIBLE);

        mUserEntryText.setOnTypeViewListener(new TypeTextView.OnTypeViewListener() {
            @Override
            public void onTypeStart() {

            }

            @Override
            public void onTypeOver() {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mUserEntryEffect.setVisibility(View.VISIBLE);
                        mUserEntryEffect.startAnimation(mUserEntryEffectTranAnimation);
                    }
                }, 500);

            }
        });

    }
}
