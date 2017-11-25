package com.gj.effect.util;

import android.content.Context;

/**
 * author KK
 * date 2017/9/22
 */
public class PhoneUtil {
    public static float phoneDensity = -1;
    public static void setPhoneDensity(Context context){
        if (phoneDensity == -1) {
            phoneDensity = context.getResources().getDisplayMetrics().density;
        }
    }

    public static void reDensity(Context context){
        if (phoneDensity != -1) {
            context.getResources().getDisplayMetrics().density = phoneDensity;
        }
    }

    public static void setDensity(Context context,float den){
        setPhoneDensity(context);

        EvtLog.e("","addEffectLottie  d :  "+den);
        context.getResources().getDisplayMetrics().density = (den/3f) * context.getResources().getDisplayMetrics().density;
    }
}
