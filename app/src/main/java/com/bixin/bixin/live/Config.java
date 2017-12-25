package com.bixin.bixin.live;

import android.content.pm.ActivityInfo;

import com.tencent.rtmp.TXLiveConstants;

/**
 * Created by jerikc on 15/12/8.
 */
public class Config {
	public static final boolean DEBUG_MODE = false;
	public static final int ENCODING_LEVEL_STADART = TXLiveConstants.VIDEO_QUALITY_STANDARD_DEFINITION;//368*656
	public static final int ENCODING_LEVEL_HEIGHT = TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION;//544*960
	public static final int ENCODING_LEVEL_SUPER = TXLiveConstants.VIDEO_QUALITY_SUPER_DEFINITION;//720*1280
	public static final int SCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
}
