package com.xzy.base.server.pool;

import org.json.JSONObject;

import com.xzy.base_i.IEvent;
import com.xzy.base_i.IPool;

/**
 * 线程池和定时池的事件对象
 * @author edmund
 *
 */
public class PoolEvent implements IEvent {
	public static enum EVENT_TYPE{
		CREATE_SUCCESS, 		//线程池创建成功
		CREATE_FAILURE,		//线程池创建失败
		THREAD_NUM_CHANGE,	//线程池中线程数量发生变化
		NO_ENOUGH_THREAD,	//动态变化的线程池中，没有足够的线程，也无法扩增线程
		TASK_START,      	//任务开始执行
		TASK_FINISH,     	//任务执行完毕
		TASK_CANCEL,     	//任务执行前被取消
		TASK_TIMEOUT,    	//任务执行超时
		TIMER_TASK_NO_EXECUTE,//定时任务无法执行
		TASK_OVERFLOW,			//任务数溢出
		DESTROY,         	//线程池销毁
		EVENT_TOTAL_NUM
	}
	
	private IPool.POOL_TYPE poolType = null;
	private EVENT_TYPE type = null;
	private String poolName = null;
	private JSONObject eventPara = null;
	
	public PoolEvent(IPool.POOL_TYPE poolType,EVENT_TYPE type,String poolName,JSONObject eventPara){
		this.poolType = poolType;
		this.type = type;
		this.poolName = poolName;
		this.eventPara = eventPara;
	}
	
	@Override
	public Object getSource() {

		return this.poolName;
	}
	
	public IPool.POOL_TYPE getPoolType(){
		return this.poolType;
	}

	@Override
	public EVENT_TYPE getEventType() {
		return type;
	}

	@Override
	public JSONObject getEventPara() {

		return this.eventPara;
	}

}
