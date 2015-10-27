package com.wdqqy.utils.threadpool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BaseThreadPool implements INominal{
	
	public static final String TAG = BaseThreadPool.class.getSimpleName();
	
	
	public static int DEFAUT_CORE_POOL_SIZE = 20; //默认池中所保存的线程数，包括空闲线程。
	public static int DEFAUT_MAXIMUM_POOL_SIZE =200;// 池中允许的最大线程数	
	
	public static long DEFAUT_KEEPALIVE_TIME = 10000;//当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间		
	public static int DEFAUT_MAX_QUENE_LENGTH = 20;//执行前用于保持任务的队列。此队列仅由保持 execute 方法提交的 Runnable 任务。
	
	public static long DEFAUT_CALCUL_HEARTBEAT_TIME = 200;//默认计算程序执行时间的计算线程的心跳时间

	//系统线程池 使用的缓冲队列 如果不能当前执行  系统线程池会将数据放到这里
	//如果这里也放满了  系统会尝试调用我们的RejectedExecutionHandler
	//如果RejectedExecutionHandler没有设置  则线程池执行函数直接异常
	private ArrayBlockingQueue<Runnable> abq =null;// new ArrayBlockingQueue<Runnable>(maxQueneLength);
	
	//这个是我们自己的执行缓冲（二级） 在系统的线程池满了  abq也满了 会调用RejectedExecutionHandler 
	//在RejectedExecutionHandler中  我们将线程放到这个里面 从而避免异常执行 并且在这里面尝试重新启动执行
	//我们监控进程 就是要保证 这个里面不能有数据  有就说明系统状态不好了 尝试修改bean中的配置  或者其他调整
	//放在这里是方便外面查询线程池的状态 否则按照归属原则 应该放在RetryRejectedExecutionHandler 里面
	private LinkedList<Runnable> list =null;//= new LinkedList<Runnable>();
	
	//我们自己的 线程数量超多后 的执行处理器 负责缓冲这些线程 并尝试重新执行
	private RetryRejectedExecutionHandler rreh= null;
	
	//系统的线程池 这个是有效的线程池 我们对他进行封装使用
	private ThreadPoolExecutor threadPoolExecutor= null;
	
	
	//临时缓存的系统一级缓存的容器最大限制值 这个一旦创建 就不能修改 除非重新创建  这里缓冲 是为了能查看到状态
	private int maxQueneLength = 0;
	
	private String name;
	
	
	//监听心跳线程  没有监听器就没有这个线程
	private ListenerHeartbeatThread listenerHeartbeatThread = null;
	
	//等待的塞子
	private Object listenerHeartLock = new Object();
	
	
	
	long calculHeartbeatTime = DEFAUT_CALCUL_HEARTBEAT_TIME;
	
	//计算线程执行时间线程 线程池启动 这个线程就启动
	private CalculThreadTimeThread calculThreadTimeThread = null;
	
	//等待的塞子
	private Object calculThreadLock = new Object();
	
	
	//用来存放线程执行结果
	private List<Future<FutureObject>> futreList = null;
	
	
	//线程运行的最长时间
	private long maxThreadSpan;


	//线程运行的最长时间( 初始时间是这个 )
	private long minThreadSpan = -1;
	
	
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
 	
	public BaseThreadPool(String name,ThreadPoolBean bean)
	{
		
		
		this.name = name;
		if(bean==null)
		{
			bean = new ThreadPoolBean(DEFAUT_CORE_POOL_SIZE,DEFAUT_MAXIMUM_POOL_SIZE,DEFAUT_MAX_QUENE_LENGTH,DEFAUT_KEEPALIVE_TIME,DEFAUT_CALCUL_HEARTBEAT_TIME);
		}
		
		
		Log.d(TAG, "ThreadPool "+ getName() + "create()   "+bean);
		
		
		abq = new ArrayBlockingQueue<Runnable>(bean.getMaxQueneLength());
		
		//缓存记录这个值  因为创建后不能修改 也无其他地方能获取 这里方便查信息的时候返回
		maxQueneLength = bean.getMaxQueneLength();
		
		list = new LinkedList<Runnable>();
		rreh = new RetryRejectedExecutionHandler(list);//构造后 已经启动轮询线程
		
		
		
		 futreList = new ArrayList<Future<FutureObject>>();
		 
		 
		 calculThreadTimeThread = new CalculThreadTimeThread(this,bean.getCalculHeartbeatTime());
		 calculThreadTimeThread.start();
		 
		
		
//		 newSingleThreadExecutor：创建一个单线程的线程池。这个线程池只有一个线程在工作，也就是相当于单线程串行执行所有任务。如果这个唯一的线程因为异常结束，那么会有一个新的线程来替代它。此线程池保证所有任务的执行顺序按照任务的提交顺序执行。
//		 newFixedThreadPool：创建固定大小的线程池。每次提交一个任务就创建一个线程，直到线程达到线程池的最大大小。线程池的大小一旦达到最大值就会保持不变，如果某个线程因为执行异常而结束，那么线程池会补充一个新线程。
//		 newCachedThreadPool：创建一个可缓存的线程池。如果线程池的大小超过了处理任务所需要的线程，那么就会回收部分空闲（60秒不执行任务）的线程，当任务数增加时，此线程池又可以智能的添加新线程来处理任务。此线程池不会对线程池大小做限制，线程池大小完全依赖于操作系统（或者说JVM）能够创建的最大线程大小。
//		 newScheduledThreadPool：创建一个大小无限的线程池。此线程池支持定时以及周期性执行任务的需求。
//		 newSingleThreadExecutor：创建一个单线程的线程池。此线程池支持定时以及周期性执行任务的需求。
//		 在详细讲解ThreadPoolExecutor的时候会具体讨论上述参数配置后的意义和原理。


		 
		 
		//构造系统线程池
		threadPoolExecutor= new ThreadPoolExecutor(
				bean.getCorePoolSize(),
				bean.getMaximumPoolSize(), 
				bean.getKeepAliveTime(), 
				TimeUnit.MILLISECONDS,
				abq, rreh);
		
		
		
	}
	
	//封装  我们支持持执行这种
	public void execute(Runnable command)
	{
		//threadPoolExecutor.execute(command);
		long beginExecuteTime = System.currentTimeMillis();
		System.out.println(command.toString()+ "  beginExecuteTime A ="+ beginExecuteTime);
		FutureObject fo = new FutureObject(command.toString(),beginExecuteTime);
		Future<FutureObject> futre =  threadPoolExecutor.submit(command, fo);
		//将执行结果放进futreList
		synchronized (futreList) {
			futreList.add(futre);
		}
			
	}
	
	public static class FutureObject
	{		
		String name;
		long beginTime;
		
		public FutureObject(String name,long beginTime)
		{
			this.name = name;
			this.beginTime = beginTime;
		}
		
	}
	
	
	public void calculThreadTime()
	{
		
		List<Future<FutureObject>> delFutreList = new  ArrayList<Future<FutureObject>>();
		
		synchronized (futreList) {
		
			for(Future<FutureObject> futre:futreList)
			{
				if(futre.isDone())//非阻塞函数
				{
					try {
						//这函数会阻塞  用这个虽然可以获取准去的 执行结果数据 但是 需要好多新线程去监听 不是用
						//所以 从现在的时间可能不准缺  会延长加上  计算心跳的间隔时间  但是减少点消耗
						FutureObject fo = futre.get();		
						
						
						System.out.println(fo.name+ "beginExecuteTime ="+ fo.beginTime);
						
						long runTime = System.currentTimeMillis() - fo.beginTime;
						
						System.out.println("runTime ="+ runTime);
						
						//设置最大值
						setMaxThreadSpan(Math.max(getMaxThreadSpan(), runTime));
						
						//计算最小值 还是初始时间  直接填充
						if(getMinThreadSpan() == -1)
						{
							setMinThreadSpan(runTime);
						}
						else
						{
							setMinThreadSpan(Math.min(getMinThreadSpan(), runTime));
						}
						
						
						
						delFutreList.add(futre);
						
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			futreList.removeAll(delFutreList);
			
		}
	}
	
	
	public boolean isShutdown()
	{
		return threadPoolExecutor.isShutdown();
	}
	
	public boolean isTerminating()
	{
		return threadPoolExecutor.isTerminating();
	}

	public boolean isTerminated()
	{
		return threadPoolExecutor.isTerminated();
	}
	
	public void setCorePoolSize(int corePoolSize)
	{
		threadPoolExecutor.setCorePoolSize(corePoolSize);
	}
	
	public void setKeepAliveTime(long time,TimeUnit unit)
	{
		threadPoolExecutor.setKeepAliveTime(time, unit);
	}
	
	public void setMaximumPoolSize(int maximumPoolSize)
	{
		threadPoolExecutor.setMaximumPoolSize(maximumPoolSize);
	}
	
	public ThreadPoolRunInfo getThreadPoolRunInfo()
	{
		
		ThreadPoolRunInfo info = new ThreadPoolRunInfo();
		
		if(threadPoolExecutor==null)
		{
			return info;
		}
		
		
		/* ---- 总计 begin ---- */
		
		//计划执行的近似任务总数（总计划执行数）
		info.setTaskCount(threadPoolExecutor.getTaskCount());
		
		//已完成执行的近似任务总数。（总完成数）
		info.setCompletedTaskCount(threadPoolExecutor.getCompletedTaskCount());
		
		//曾经同时位于池中的最大线程数（曾经峰值）
		info.setLargestPoolSize(threadPoolExecutor.getLargestPoolSize());
		
		//线程运行的最长时间 近似（有计算时长线程的间隔心跳时间）
		info.setMaxThreadSpan(getMaxThreadSpan());
		
		//线程运行的最短时间 近似（有计算时长线程的间隔心跳时间）
		info.setMinThreadSpan(getMinThreadSpan());
		
		
		
		/* ---- 总计 end ---- */
		
		
		/* ---- 运行态 begin ---- */
		
		//主动执行任务的近似线程数。（当前正在run的数）
		info.setActiveCount(threadPoolExecutor.getActiveCount());
		
		//池中的当前线程数（当前执行池中的数字）
		info.setPoolSize(threadPoolExecutor.getPoolSize());
		
		//系统提供的一级缓冲容器中的线程数（系统一级缓冲数）
		if(abq!=null)
		{
			info.setBlockingQueueCount(abq.size());
		}
		else
		{
			info.setBlockingQueueCount(0);
		}
		
		
		//我们自己实现的二级缓冲数 一级缓冲满 放到二级缓冲 重复尝试再次执行（二级等待重新执行数）
		if(list!=null)
		{
			info.setSecondLevelCount(list.size());
		}
		else
		{
			info.setSecondLevelCount(0);
		}
			
		/* ---- 运行态 end ---- */
		
		
		
		/* ---- 设置参数 begin ---- */
		
		//默认池中所保存的线程数，包括空闲线程。（可以动态修改）
		info.setCorePoolSize(threadPoolExecutor.getCorePoolSize());
		
		//池中允许的最大线程数  （可以动态修改）
		info.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize());
		
		//当线程数大于核心时，在一级缓冲中等待的最长时间 （可以动态修改）	
		info.setKeepAliveTime(threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS));
		
		//系统提供的一级缓冲容器的最大限制 (这个创建后 不允许再修改了)
		info.setMaxBlockingQueueSize(maxQueneLength);
		
		/* ---- 设置参数 end ---- */
		
		
		return info;
	}
	
	

	//注意 只要shutdown后 就不能用了 必须重新new
	public void shutdown()
	{

		Log.d(TAG, "ThreadPool "+ getName() +" shutdown()");
		
		threadPoolExecutor.shutdown();
		selfShitDown();
	}
	
	public List<Runnable> shutdownNow()
	{
		List<Runnable>  runnablelist = threadPoolExecutor.shutdownNow();
		selfShitDown();
		
		return runnablelist;
	}
	
	
	private void selfShitDown()
	{
		//停止 重复执行器
		if(rreh!=null)
		{	
			rreh.stop();
		}
		rreh = null;
		
		//清除我们自己的二级缓存
		if(list!=null)
		{	
			synchronized (list)
			{
				if(list.size()>0)
				{
					Log.e(TAG, "关闭线程池  二级缓存中被清除  "+list.size()+"个待执行 线程");
				}
				
				list.clear();
			}
			
			list = null;
		}
		
		//关闭监听相关
		if(listenerHeartbeatThread!=null)
		{
			listenerHeartbeatThread.doStop();
			
			listenerHeartbeatThread =null;
		}
		
		
		//关闭计算线程执行时长相关
		if(calculThreadTimeThread!=null)
		{
			calculThreadTimeThread.doStop();
			
			calculThreadTimeThread =null;
		}
		
		if(futreList!=null)
		{
			synchronized (futreList)
			{
				if(futreList.size()>0)
				{
					Log.e(TAG, "关闭线程池 执行结果队列被清除  "+futreList.size()+"个待执行结果");
				}
				
				futreList.clear();
			}
			
			futreList = null;
		}
		
		
		
		
		
		
	}

	//直接返回这个系统线程池
	public ThreadPoolExecutor getThreadPoolExecutor() {
		return threadPoolExecutor;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}
	
	
	public void setThreadPoolStateListener(IThreadPoolStateListener listener,long heartbeatTime)
	{
		
		if(listenerHeartbeatThread!=null )//有以前的监听 删除
		{
			listenerHeartbeatThread.doStop();
			listenerHeartbeatThread = null;
		}
		
		listenerHeartbeatThread = new ListenerHeartbeatThread(this,listener,heartbeatTime);
		listenerHeartbeatThread.start();
		
	}
	
	
	
	public void delThreadPoolStateListener()
	{
		
		if(listenerHeartbeatThread!=null )//有以前的监听 删除
		{
			listenerHeartbeatThread.doStop();
			listenerHeartbeatThread = null;
		}
	}
	
	
	
	/**
	 * 计算线程执行时长 心跳线程
	 * @author Administrator
	 *
	 */
	private class CalculThreadTimeThread extends Thread {
		
		boolean stopThread = true;
		
		BaseThreadPool pool = null;
		
		
		//默认1秒
		long heartbeatTime = 1000;
		
		public CalculThreadTimeThread(BaseThreadPool pool,long heartbeatTime)
		{
			this.pool = pool;			
			this.heartbeatTime = heartbeatTime;
		}
		
		@Override
		public void run() {
			while (stopThread) {
				
				
				calculThreadTime();
				
				try {
					synchronized (calculThreadLock) {
						calculThreadLock.wait(heartbeatTime);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
		
		public void doStop()
		{
			stopThread = false;
			synchronized (calculThreadLock) {
				calculThreadLock.notifyAll();
			}
		}
	}
	
	
	/**
	 * 监听心跳线程
	 * @author Administrator
	 *
	 */
	private class ListenerHeartbeatThread extends Thread {
		
		boolean stopThread = true;
		
		BaseThreadPool pool = null;
		IThreadPoolStateListener listener = null;
		
		//默认30秒
		long heartbeatTime = 30000;
		
		public ListenerHeartbeatThread(BaseThreadPool pool,IThreadPoolStateListener listener,long heartbeatTime)
		{
			this.pool = pool;
			this.listener = listener;
			this.heartbeatTime = heartbeatTime;
		}
		
		@Override
		public void run() {
			while (stopThread) {
				
				
				//如果有效
				if(listener!=null && pool!=null && !pool.isTerminated())
					{
						//派发监听消息 获取状态
						listener.currentState(pool.getThreadPoolRunInfo());
						
						try {
							synchronized (listenerHeartLock) {
								listenerHeartLock.wait(heartbeatTime);
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
				else
				{
					//无效的 则停止这个心跳线程 结束监听 以后 用户需要重新启动监听
					stopThread= false;
				}
			}
			
			if(listener!=null)
			{
				 //派发监听消息  线程池失效
				listener.ThreadPoolInvalid(pool);
				listener = null;
			}
			
			
		}
		
		
		public void doStop()
		{
			stopThread = false;
			synchronized (listenerHeartLock) {
				listenerHeartLock.notifyAll();
			}
		}
	}

	
	
	public static void main(String[] args)
	{
		
		ThreadPoolBean bean = new ThreadPoolBean(1,1,1,10000,5000);
		BaseThreadPool tp= new BaseThreadPool("测试线程池",bean); 
		
		
		for(int i=1;i<=10;i++)
		{
			tp.execute(new MyRunnable(i));
		}
		
		
//		while(true)
//		{		
//			System.out.println(tp.getName());
//			System.out.println(tp.getThreadPoolRunInfo());
//			
//			try {
//				// Thread.sleep(2000);
//				TimeUnit.MILLISECONDS.sleep(1000);
//
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		
		
		
		
		tp.setThreadPoolStateListener(new IThreadPoolStateListener() {
			
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
		
		//tp.delThreadPoolStateListener();
		
		tp.shutdown();
		
	}
	
	
	private static class MyRunnable implements Runnable
	{

		public int i = 0;
		public MyRunnable(int i)
		{
			this.i = i;
		}
		
		@Override
		public void run(){
			System.out.println("--Thread "+ i +" begin --");
			try {
				// Thread.sleep(2000);
				//TimeUnit.MILLISECONDS.sleep(1000*i);
				TimeUnit.MILLISECONDS.sleep(1000);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("--Thread "+ i +" end --");
		}
		
	}


	

}
