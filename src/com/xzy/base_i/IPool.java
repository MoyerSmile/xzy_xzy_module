package com.xzy.base_i;

public interface IPool {
	public static enum POOL_TYPE{
		THREAD_POOL_TYPE,
		TIMER_POOL_TYPE,
		TOTAL_NUM
	};
	//得到池的名字
	public String getPoolName();
	
	//得到创建时的堆栈
	public Exception getCreateStack();
	
	//得到待执行任务数
	public int getWaitTaskNum();
	//得到完成的任务数
	public long getFinishTaskNum();
	//得到取消掉的任务数
	public long getCancelTaskNum();
	
	//启动池
	public boolean start();
	
	//是否启动中
	public boolean isRunning();
	
	//关闭池
	public void stop();
}
