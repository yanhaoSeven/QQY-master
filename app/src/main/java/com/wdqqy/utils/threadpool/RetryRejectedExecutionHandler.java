package com.wdqqy.utils.threadpool;

import java.util.LinkedList;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 拒绝执行重试handler
 * 线程池满了  等待队列也满了 系统线程池调用这个类，
 * 这个类负责将拒绝的执行线程缓冲起来，并尝试重新执行
 * @author Administrator
 *
 */
public class RetryRejectedExecutionHandler implements RejectedExecutionHandler {
	static private final String TAG = RetryRejectedExecutionHandler.class.getSimpleName();
	
	public LinkedList<Runnable> list;
	ThreadPoolExecutor pool;

	boolean stopThread = true;
	
	//等待的塞子
	private Object lock = new Object();
	

	public RetryRejectedExecutionHandler(LinkedList<Runnable> list) {
		this.list = list;
		
		//启动重试线程
		CycleThread cycleThread = new CycleThread();
		cycleThread.start();
		
	}

	public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
		
		Log.e(TAG, arg0 + " 被拒绝执行");
		
		
		if (pool == null || (!pool.equals(arg1))) {
			pool = arg1;
		}

		synchronized (list) {
			list.add(arg0);
		}
	}

	public void stop() {
		stopThread = false;
		//停止后 马上通知等待的执行过去
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	class CycleThread extends Thread {
		@Override
		public void run() {
			while (stopThread) {
				if (pool != null) {
					if (!pool.isShutdown()) {
						if (list.size() > 0) {
							Runnable task = null;
							synchronized (list) {
								task = list.removeFirst();
							}
							//logger.debug(task + " 重新被执行");
							Log.d(TAG, task + " 重新被执行");
							//这里使用 execute 在BaseThreadPool中使用的是submit 
							//submit时候 绑定了 Future<FutureObject> 此时重新执行 不影响 绑定 所以 重新执行的结果  还是会通知到 submit时候的 Future<FutureObject>
							pool.execute(task);
						}

					}
				}
				
					
				try {
					synchronized (lock) {
						lock.wait(500);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}

}
