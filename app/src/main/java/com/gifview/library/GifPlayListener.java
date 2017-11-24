package com.gifview.library;

/**
 * Created by Administrator on 2016/11/11.
 */

/**
 * gif播放监听类
 */
public interface GifPlayListener {

	void onPlayStarted();

	void onPlayFailed();

	void onPlayComplete();
}
