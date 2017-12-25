package com.bixin.bixin.danmu.DanmuBase;


/**
 * Created by walkingMen on 2016/5/12.
 * 弹幕管道接口
 */
public interface DanmakuChannelInter {

	void startAnimation(DanmakuEntity entity);

	void releaseView();

	boolean isRunning();

	void setDanmakuActionInter(DanmakuActionInter danAction);
}
