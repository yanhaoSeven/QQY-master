/**
 * Log.java
 * com.lb.s.part.log
 *
 * Function： TODO 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2013-1-11 		liangbo
 *
 * Copyright (c) 2013, BMV All Rights Reserved.
*/

package com.wdqqy.utils.threadpool;
/**
 * ClassName:Log
 * Function: TODO ADD FUNCTION
 * Reason:	 TODO ADD REASON
 *
 * @author   liangbo
 * @version  
 * @since    Ver 1.0
 * @Date	 2013-1-11		下午03:24:52
 *
 * @see 	 
 */
public class Log {
	
	public static  int d(String tag, String msg)
	{
		System.out.println(tag+" "+ msg);
		return 0;
	}
	
	public static  int d(String tag, String msg,Throwable tr)
	{
		System.out.println(tag+" "+ msg);
		return 0;
	}
	
	public static  int e(String tag, String msg)
	{
		System.err.println(tag+" "+ msg);
		return 0;
	}
	
	public static  int e(String tag, String msg,Throwable tr)
	{
		return 0;
	}
	
	public static  int i(String tag, String msg)
	{
		return 0;
	}
	
	public static  int i(String tag, String msg,Throwable tr)
	{
		return 0;
	}

	public static  int v(String tag, String msg)
	{
		return 0;
	}
	
	public static  int v(String tag, String msg,Throwable tr)
	{
		return 0;
	}

	public static  int w(String tag, String msg)
	{
		return 0;
	}
	
	public static  int w(String tag, String msg,Throwable tr)
	{
		return 0;
	}
	
	public static  int w(String tag, Throwable tr)
	{
		return 0;
	}
	
	public static  int wtf(String tag, String msg)
	{
		return 0;
	}
	
	public static  int wtf(String tag, String msg,Throwable tr)
	{
		return 0;
	}
	
	public static  int wtf(String tag, Throwable tr)
	{
		return 0;
	}

}

