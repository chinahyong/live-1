package tv.live.bx.tasks;

import android.os.Process;

/**
 * Title: BaseRunnable.java Description: 基础线程抽象类 Copyright: Copyright (c) 2008
 * @CreateDate 2013-11-5 下午6:09:05
 * @version 1.0
 */
public abstract class BaseRunnable implements Runnable {

	public final void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

		runImpl();
	}

	public abstract void runImpl();

	protected boolean isInterrupted() {
		return Thread.currentThread().isInterrupted();
	}
}
