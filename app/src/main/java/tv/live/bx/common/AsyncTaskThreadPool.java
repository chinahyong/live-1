/**
 * Project Name:feizao File Name:AsyncTaskThreadPool.java Package
 * Name:com.efeizao.feizao.common Date:2015-6-17下午7:26:40
 */

package tv.live.bx.common;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassName:AsyncTaskThreadPool Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: 2015-6-17 下午7:26:40
 * @author Live
 * @version 1.0
 */
public class AsyncTaskThreadPool {
	/** Creates default implementation of task distributor */

	private static ExecutorService mThreadExecutor;

	public static Executor createTaskDistributor() {
		return Executors.newCachedThreadPool(createThreadFactory(
				Thread.NORM_PRIORITY, "uil-pool-d-"));
	}

	public static ExecutorService getThreadExecutorService() {
		if (null == mThreadExecutor || mThreadExecutor.isShutdown()) {
			mThreadExecutor = Executors
					.newSingleThreadExecutor(createThreadFactory(
							Thread.NORM_PRIORITY, "uil-pool-d-"));
		}
		return mThreadExecutor;
	}

	/**
	 * Creates default implementation of {@linkplain ThreadFactory thread
	 * factory} for task executor
	 */
	private static ThreadFactory createThreadFactory(int threadPriority,
			String threadNamePrefix) {
		return new DefaultThreadFactory(threadPriority, threadNamePrefix);
	}

	private static class DefaultThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);

		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final int threadPriority;

		DefaultThreadFactory(int threadPriority, String threadNamePrefix) {
			this.threadPriority = threadPriority;
			group = Thread.currentThread().getThreadGroup();
			namePrefix = threadNamePrefix + poolNumber.getAndIncrement()
					+ "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix
					+ threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			t.setPriority(threadPriority);
			return t;
		}
	}
}
