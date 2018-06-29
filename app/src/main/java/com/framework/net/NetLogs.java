/**
 * 
 */
package com.framework.net;

import com.bixin.bixin.library.util.EvtLog;

/**
 */
public class NetLogs {

	public static final void d_netstep(AEntity entity, String tag, String msg,
			String step) {
		EvtLog.d(tag, String.format("[F:%s(%s)][url:%s][%s][step:%s]",
				entity.flag, Integer.toHexString(entity.hashCode()),
				entity.url, msg, step));
	}

	public final static void d_time(String tag, String timerFlag, String title,
			String extraMsg) {
		String showMsg = String.format("[Timer:%s][%s][%s]", timerFlag, title,
				extraMsg);
		EvtLog.d(tag, showMsg);
	}
}
