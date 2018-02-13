package com.xzy.base.server.pool;

public interface ITask extends Runnable{
	/**
	 * 得到任务的flag。对于多队列任务有效
	 * @return 任务对象标识
	 */
	public Object getFlag();
	
	/**
	 * 得到任务的名称
	 * @return 任务名称
	 */
	public String getName();
	
	/**
	 * 取消任务的执行
	 */
	public void cancel();
	
	/**
	 * 任务是否已经被取消
	 * @return true代表已经被取消，反之可执行
	 */
	public boolean isCancel();
}
