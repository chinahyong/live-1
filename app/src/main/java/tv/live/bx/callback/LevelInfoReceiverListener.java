package tv.live.bx.callback;

/**
 * Created by Administrator on 2017/5/27.
 */

import tv.live.bx.config.AppConfig;
import tv.live.bx.library.util.EvtLog;

import cn.efeizao.feizao.framework.net.impl.CallbackDataHandle;

/**
 * 等级配置更新回调 ，下载等级图标 <br/>
 * date: 2015-6-18 上午11:47:50 <br/>
 *
 * @author Administrator
 * @since JDK 1.6
 */
public class LevelInfoReceiverListener implements CallbackDataHandle {
	//等级配置版本号
	private String mLevelConfigVersion;

	public LevelInfoReceiverListener(String mLevelConfigVersion) {
		this.mLevelConfigVersion = mLevelConfigVersion;
	}

	@Override
	public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
		EvtLog.d(TAG, "LevelInfoReceiverListener success " + success + " errorCode" + errorCode);
		if (success) {
			AppConfig.getInstance().parseLevelConfigInfo(result, mLevelConfigVersion);
		}

	}
}