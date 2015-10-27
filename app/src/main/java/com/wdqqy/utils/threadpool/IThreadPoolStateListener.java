package com.wdqqy.utils.threadpool;

/**
 * 线程池状态监听器
 * @author Administrator
 *
 */
public interface IThreadPoolStateListener {
	
	/**
	 * 获取当前线程池状态信息
	 * @param info
	 */
	public void currentState(ThreadPoolRunInfo info);
	
	
	/**
	 * 线程池失效了 所有和这个线程池相关的都需要重建
	 * @param pool
	 */
	public void ThreadPoolInvalid(BaseThreadPool pool);

}
