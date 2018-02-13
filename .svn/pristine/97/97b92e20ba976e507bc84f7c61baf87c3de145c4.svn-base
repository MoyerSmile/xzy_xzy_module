package com.xzy.base.server.pool;

import com.xzy.base_i.IEventListener;

public class ThreadPoolInfo {
	public static enum QUEUE_TYPE{
		SINGLE_QUEUE_POOL,
		MULTIPLE_QUEUE_POOL
	}
	public static enum THREAD_TYPE{
		STATIC_TYPE,
		DYNAMIC_TYPE
	}
	
	//队列类型，单队列或者多队列
	private QUEUE_TYPE _queueType = QUEUE_TYPE.SINGLE_QUEUE_POOL;
	//线程数量类型，静态数量类型和动态数量类型。依据任务动态增长和减少
	private THREAD_TYPE _threadType = THREAD_TYPE.STATIC_TYPE;
	//最小和最大线程数量，如果是静态数量线程，只使用最小线程数量
	private int minThreadNum,maxThreadNum;
	//最大堆积任务数量
	private int maxTaskNum = 0;
	//线程优先级
	private int priority;
	//是否守护线程
	private boolean isDaemo = true;
	//线程池的事件监听
	private IEventListener listener = null;
	
	public ThreadPoolInfo(int threadNum){
		this(threadNum,1000,QUEUE_TYPE.SINGLE_QUEUE_POOL,Thread.NORM_PRIORITY,true);
	}
	public ThreadPoolInfo(int threadNum,int maxTaskNum){
		this(threadNum,maxTaskNum,QUEUE_TYPE.SINGLE_QUEUE_POOL,Thread.NORM_PRIORITY,true);
	}
	public ThreadPoolInfo(int threadNum,int maxTaskNum,QUEUE_TYPE queueType){
		this(threadNum,maxTaskNum,queueType,Thread.NORM_PRIORITY,true);
	}

	public ThreadPoolInfo(int threadNum,int maxTaskNum,int priority){
		this(threadNum,maxTaskNum,QUEUE_TYPE.SINGLE_QUEUE_POOL,priority,true);
	}

	public ThreadPoolInfo(int threadNum,int maxTaskNum,QUEUE_TYPE queueType,int priority,boolean isDaemo){
		this.minThreadNum = threadNum;
		this.maxThreadNum = threadNum;
		this.maxTaskNum = maxTaskNum;
		this._queueType = queueType;
		this.priority = priority;
		this.isDaemo = isDaemo;
	}
	
	/**
	 * 更新为动态线程数，指明目标的最大线程数，如果小于最小线程数，则更新失败
	 * @param maxThreadNum 最大线程数量
	 * @return 成功或失败
	 */
	public boolean update2DynamicThreadNum(int maxThreadNum){
		if(maxThreadNum <= this.minThreadNum){
			return false;
		}
		this.maxThreadNum = maxThreadNum;
		this._threadType = THREAD_TYPE.DYNAMIC_TYPE;
		return true;
	}
	
	public void setListener(IEventListener listener) {
		this.listener = listener;
	}
	public IEventListener getListener(){
		return this.listener;
	}
	
	public QUEUE_TYPE get_queueType() {
		return _queueType;
	}
	public THREAD_TYPE get_threadType() {
		return _threadType;
	}
	public int getMinThreadNum() {
		return minThreadNum;
	}
	public int getMaxThreadNum() {
		return maxThreadNum;
	}
	public int getMaxTaskNum() {
		return maxTaskNum;
	}
	public int getPriority() {
		return priority;
	}
	public boolean isDaemo() {
		return isDaemo;
	}
	
}
