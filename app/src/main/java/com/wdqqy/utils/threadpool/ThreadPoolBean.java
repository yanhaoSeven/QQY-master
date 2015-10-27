package com.wdqqy.utils.threadpool;

public class ThreadPoolBean {
	
private int corePoolSize; //池中所保存的线程数，包括空闲线程

private int maximumPoolSize; // 池中允许的最大线程数

private long keepAliveTime; //- 当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间。
	
private int maxQueneLength;//workQueue - 执行前用于保持任务的队列。此队列仅由保持 execute 方法提交的 Runnable 任务。

private long calculHeartbeatTime ;//计算程序执行时间的计算线程的心跳时间

public ThreadPoolBean() {

}

public ThreadPoolBean(int corePoolSize, int maximumPoolSize, int maxQueneLength,
		long keepAliveTime,long calculHeartbeatTime) {
	this.corePoolSize = corePoolSize;
	this.maximumPoolSize = maximumPoolSize;
	this.maxQueneLength = maxQueneLength;
	this.keepAliveTime = keepAliveTime;
	if(calculHeartbeatTime<500)//不建议小于500 影响性能
	{
		this.calculHeartbeatTime = 500;
	}
	else
	{
		this.calculHeartbeatTime = calculHeartbeatTime;
	}
	
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

public int getMaxQueneLength() {
	return maxQueneLength;
}

public void setMaxQueneLength(int maxQueneLength) {
	this.maxQueneLength = maxQueneLength;
}


public long getCalculHeartbeatTime() {
	return calculHeartbeatTime;
}

public void setCalculHeartbeatTime(long calculHeartbeatTime) {
	this.calculHeartbeatTime = calculHeartbeatTime;
}

public String toString()
{
	StringBuffer sb = new StringBuffer();
	
	sb.append(" [ ");
	
	sb.append(" corePoolSize = "+corePoolSize);//池中所保存的线程数，包括空闲线程

	sb.append(" , maximumPoolSize = "+maximumPoolSize);// 池中允许的最大线程数
	
	sb.append(" , keepAliveTime = "+keepAliveTime);// 当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间。
		
	sb.append(" , maxQueneLength = "+maxQueneLength);//workQueue - 执行前用于保持任务的队列。此队列仅由保持 execute 或 sibmit方法提交的 Runnable 任务。
	
	sb.append(" , calculHeartbeatTime = "+calculHeartbeatTime);//计算程序执行时间的计算线程的心跳时间
	
	sb.append(" ] ");
	return sb.toString();
}


}
