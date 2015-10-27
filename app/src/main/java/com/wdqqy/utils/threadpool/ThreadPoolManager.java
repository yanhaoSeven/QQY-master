package com.wdqqy.utils.threadpool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {

	public static final String TAG = ThreadPoolManager.class.getSimpleName();

	// 自身实例
	private static ThreadPoolManager instance = null;

	// 全部线程池的容器
	Map<String, BaseThreadPool> threadPoolMap = new HashMap<String, BaseThreadPool>();

	public static ThreadPoolManager getInstance() {
		if (instance == null) {
			instance = new ThreadPoolManager();
		}
		return instance;
	}

	public BaseThreadPool getThreadPool(String threadPoolName) {
		return threadPoolMap.get(threadPoolName);
	}

	public BaseThreadPool getAndCreateThreadPool(String threadPoolName, ThreadPoolBean bean) {
		if (bean == null) {
			bean = new ThreadPoolBean(BaseThreadPool.DEFAUT_CORE_POOL_SIZE, BaseThreadPool.DEFAUT_MAXIMUM_POOL_SIZE,
					BaseThreadPool.DEFAUT_MAX_QUENE_LENGTH, BaseThreadPool.DEFAUT_KEEPALIVE_TIME, BaseThreadPool.DEFAUT_CALCUL_HEARTBEAT_TIME);
		}

		BaseThreadPool threadPool = threadPoolMap.get(threadPoolName);
		if (threadPool != null) {
			if (threadPool.isTerminated() || threadPool.isTerminating() || threadPool.isShutdown()) {
				// 如果已经执行完毕了 则删除这个 重新构建一个新的线程池执行器
				threadPoolMap.remove(threadPoolName);

				// 创建新的线程池
				threadPool = new BaseThreadPool(threadPoolName, bean);

				// 压回去
				threadPoolMap.put(threadPoolName, threadPool);
			} else {
				// 就返回这个
			}
		} else {
			threadPool = new BaseThreadPool(threadPoolName, bean);

			// 压回去
			threadPoolMap.put(threadPoolName, threadPool);
		}

		return threadPool;
	}

	public BaseThreadPool getAndCreateThreadPool(String threadPoolName) {
		return getAndCreateThreadPool(threadPoolName, null);
	}

	public void shutdownThreadPool(String threadPoolName) {
		BaseThreadPool threadPool = getThreadPool(threadPoolName);
		if (threadPool != null) {
			threadPool.shutdown();
		}

		threadPoolMap.remove(threadPoolName);
	}

	public void shutdownAll() {

		for (BaseThreadPool threadPool : threadPoolMap.values()) {
			if (threadPool != null) {
				threadPool.shutdown();
			}
		}

		threadPoolMap.clear();

	}

	public static void main(String[] args) {

		BaseThreadPool btp = ThreadPoolManager.getInstance().getAndCreateThreadPool("liangbo梁波测试");

		for (int i = 1; i <= 10; i++) {
			btp.execute(new MyRunnable(i));
		}

		btp.setThreadPoolStateListener(new IThreadPoolStateListener() {

			@Override
			public void currentState(ThreadPoolRunInfo info) {
				System.out.println(info);

			}

			@Override
			public void ThreadPoolInvalid(BaseThreadPool pool) {

				System.out.println(pool);

			}
		}, 1000);

		try {
			// Thread.sleep(2000);
			TimeUnit.MILLISECONDS.sleep(20000);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// tp.delThreadPoolStateListener();

		ThreadPoolManager.getInstance().shutdownThreadPool("liangbo梁波测试");

	}

	private static class MyRunnable implements Runnable {

		public int i = 0;

		public MyRunnable(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			System.out.println("--Thread " + i + " begin --");
			try {
				// Thread.sleep(2000);
				// TimeUnit.MILLISECONDS.sleep(1000*i);
				TimeUnit.MILLISECONDS.sleep(1000);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("--Thread " + i + " end --");
		}

	}

}
