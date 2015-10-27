package com.wdqqy.utils.threadpool;

public class ThreadPoolRunInfo {
	
	
	/* ---- 总计 begin ---- */
	private long taskCount = 0;//计划执行的近似任务总数（总计划执行数）
	
	private long completedTaskCount = 0;//已完成执行的近似任务总数。（总完成数）
	
	private int largestPoolSize = 0;//曾经同时位于池中的最大线程数（曾经峰值）
	
	private long maxThreadSpan = 0;//线程运行的最长时间 近似（有计算时长线程的间隔心跳时间）
	
	private long minThreadSpan = 0;//线程运行的最短时间 近似（有计算时长线程的间隔心跳时间）
	
	/* ---- 总计 end ---- */
	
	
	/* ---- 运行态 begin ---- */
	
	


	private int activeCount = 0;//主动执行任务的近似线程数。（当前正在run的数）
	
	private int poolSize = 0;//池中的当前线程数（当前执行池中的数字）
	
	private int blockingQueueCount = 0;//系统提供的一级缓冲容器中的线程数（系统一级缓冲数）
	
	private int secondLevelCount = 0;//我们自己实现的二级缓冲数 一级缓冲满 放到二级缓冲 重复尝试再次执行（二级等待重新执行数）
		
	/* ---- 运行态 end ---- */
	
	
	/* ---- 设置参数 begin ---- */
	
	private int corePoolSize = 0;//默认池中所保存的线程数，包括空闲线程。（可以动态修改）
	
	private int maximumPoolSize = 0;//池中允许的最大线程数  （可以动态修改）
	
	private long keepAliveTime = 0;//当线程数大于核心时，在一级缓冲中等待的最长时间 （可以动态修改）		
	
	private int maxBlockingQueueSize = 0;//系统提供的一级缓冲容器的最大限制 (这个创建后 不允许再修改了)
	
	/* ---- 设置参数 end ---- */
	
	public long getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(long taskCount) {
		this.taskCount = taskCount;
	}


	public long getCompletedTaskCount() {
		return completedTaskCount;
	}

	public void setCompletedTaskCount(long completedTaskCount) {
		this.completedTaskCount = completedTaskCount;
	}

	public int getLargestPoolSize() {
		return largestPoolSize;
	}

	public void setLargestPoolSize(int largestPoolSize) {
		this.largestPoolSize = largestPoolSize;
	}
	
	
	public synchronized long getMaxThreadSpan() {
		return maxThreadSpan;
	}

	public synchronized void setMaxThreadSpan(long maxThreadSpan) {
		this.maxThreadSpan = maxThreadSpan;
	}

	public synchronized long getMinThreadSpan() {
		return minThreadSpan;
	}

	public synchronized void setMinThreadSpan(long minThreadSpan) {
		this.minThreadSpan = minThreadSpan;
	}
	

	public int getActiveCount() {
		return activeCount;
	}

	public void setActiveCount(int activeCount) {
		this.activeCount = activeCount;
	}

	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public int getBlockingQueueCount() {
		return blockingQueueCount;
	}

	public void setBlockingQueueCount(int blockingQueueCount) {
		this.blockingQueueCount = blockingQueueCount;
	}

	public int getSecondLevelCount() {
		return secondLevelCount;
	}

	public void setSecondLevelCount(int secondLevelCount) {
		this.secondLevelCount = secondLevelCount;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public long getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public int getMaxBlockingQueueSize() {
		return maxBlockingQueueSize;
	}

	public void setMaxBlockingQueueSize(int maxBlockingQueueSize) {
		this.maxBlockingQueueSize = maxBlockingQueueSize;
	}


	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append(StringTool.LINE_SEPARATOR);
		sb.append(" TOTTED [ ");
		
		sb.append(" taskCount(总计划) = "+ taskCount);
		
		sb.append(" , completedTaskCount（总完成）= "+ completedTaskCount);
		
		sb.append(" , largestPoolSize（峰值）= "+ largestPoolSize);
		
		sb.append(" , maxThreadSpan（最长执行）= "+ maxThreadSpan);
		
		sb.append(" , minThreadSpan（最短执行）= "+ minThreadSpan);
		
		
		sb.append(" ]");
		sb.append(StringTool.LINE_SEPARATOR);
		
	
		sb.append(" RUN STATE [ ");
		
		sb.append(" activeCount(正在run) = "+ activeCount);
		
		sb.append(" , poolSize（执行池包含）= "+ poolSize);
		
		sb.append(" , blockingQueueCount（系统一级缓冲）= "+ blockingQueueCount);
		
		sb.append(" , secondLevelCount（二级缓冲）= "+ secondLevelCount);
		
		sb.append(" ]");
		sb.append(StringTool.LINE_SEPARATOR);
		
		
		sb.append(" SET [ ");
		
		sb.append(" corePoolSize(默认数) = "+ corePoolSize);
		
		sb.append(" , maximumPoolSize（最大数）= "+ maximumPoolSize);
		
		sb.append(" , keepAliveTime（一级缓冲等待时间）= "+ keepAliveTime);
		
		sb.append(" , maxBlockingQueueSize（一级缓冲大小）= "+ maxBlockingQueueSize);
		
		sb.append(" ]");
		sb.append(StringTool.LINE_SEPARATOR);
		
		
		return sb.toString();
	}
	
	

//	
//	System.out.println("PoolSize =" + tpe.getPoolSize());//返回池中的当前线程数。
//	System.out.println(" =" + tpe.getTaskCount());//返回曾。
//
//	
//	System.out.println("abq ="+ThreadPoolManager.getInstance().abq.size());

}
