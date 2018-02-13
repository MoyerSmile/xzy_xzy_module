package com.xzy.base.server.pool;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.xzy.base_c.BasicServer;
import com.xzy.base_i.IEventListener;
import com.xzy.base_i.IPool;

public class ThreadPoolServer extends BasicServer{
	private static ThreadPoolServer singleInstance = new ThreadPoolServer();
	private HashMap<String,ThreadPool> threadPoolMapping = new HashMap<String,ThreadPool>();
	private HashMap<String,TimerPool> timerPoolMapping = new HashMap<String,TimerPool>();
	private ThreadPool dynamicThreadPool = null;
	
	public static ThreadPoolServer getSingleInstance(){
		return singleInstance;
	}
	
	@Override
	public boolean startServer() {
		int minThreadNum = 2;
		int maxThreadNum = 100;
		int maxTaskNum = 100;
		
		Integer tempInteger = this.getIntegerPara("min_thread_num");
		if(tempInteger != null){
			minThreadNum = tempInteger.intValue();
		}
		tempInteger = this.getIntegerPara("max_thread_num");
		if(tempInteger != null){
			maxThreadNum = tempInteger.intValue();
		}
		tempInteger = this.getIntegerPara("max_task_num");
		if(tempInteger != null){
			maxTaskNum = tempInteger.intValue();
		}
		maxThreadNum = Math.max(minThreadNum, maxThreadNum);
		
		ThreadPoolInfo poolInfo = new ThreadPoolInfo(minThreadNum,maxTaskNum);
		poolInfo.update2DynamicThreadNum(maxThreadNum);
		this.dynamicThreadPool = new ThreadPool("system dynamic thread pool",poolInfo);
		this.isRun = this.dynamicThreadPool.start();
		
		this.info("Start ThreadPoolServer "+(isRun?"Success":"Failure")+" minThreadNum="+minThreadNum+" maxThreadNum="+maxThreadNum+" maxTaskNum="+maxTaskNum);
		
		return this.isRun;
	}
	
	public void stopServer(){
		super.stopServer();

		this.info("Start Stop ThreadPoolServer ");
		if(this.dynamicThreadPool != null){
			this.dynamicThreadPool.stop();
			this.info("Stop ThreadPool "+this.dynamicThreadPool.getPoolName());
		}
		synchronized(this.threadPoolMapping){
			ThreadPool pool = null;
			for(Iterator<ThreadPool> itr = this.threadPoolMapping.values().iterator();itr.hasNext();){
				pool = itr.next();
				pool.stop();
				this.info("Stop ThreadPool "+pool.getPoolName());
			}
			this.threadPoolMapping.clear();
		}
		this.info("Finish Stop ThreadPoolServer ");
	}
	
	public boolean runByThread(ITask task){
		if(!this.isRunning() || this.dynamicThreadPool == null){
			return false;
		}
		
		boolean isSuccess = this.dynamicThreadPool.addTask(task);
		this.info("Create New Thread Task "+(isSuccess?"Success":"Failure")+" "+task.toString());
		
		return isSuccess;
	}
	
	public synchronized boolean createThreadPool(String poolName,ThreadPoolInfo poolInfo){
		if(poolName == null || poolInfo == null){
			return false;
		}
		if(this._isExistPoolName(poolName,IPool.POOL_TYPE.THREAD_POOL_TYPE)){
			return true;
		}
		
		ThreadPool pool = new ThreadPool(poolName,poolInfo);
		boolean isSuccess = pool.start();
		this.info("Create New ThreadPool["+poolName+"] "+(isSuccess?"Success":"Failure"));
		
		if(isSuccess){
			synchronized(this.threadPoolMapping){
				this.threadPoolMapping.put(poolName, pool);
			}
		}else{
			pool.stop();
		}
		
		return isSuccess;
	}
	
	public Exception getThreadPoolCreateStack(String poolName){
		return this._getPoolCreateStack(poolName, IPool.POOL_TYPE.THREAD_POOL_TYPE);
	}
	public Exception getTimerPoolCreateStack(String poolName){
		return this._getPoolCreateStack(poolName, IPool.POOL_TYPE.TIMER_POOL_TYPE);
	}
	private Exception _getPoolCreateStack(String poolName,IPool.POOL_TYPE type){
		IPool pool = this._getPool(poolName, type);
		if(pool == null){
			return null;
		}
		return pool.getCreateStack();
	}

	public boolean createTimerPool(String poolName){
		TimerPoolInfo poolInfo = new TimerPoolInfo();
		
		return this.createTimerPool(poolName, poolInfo);
	}
	public synchronized boolean createTimerPool(String poolName,TimerPoolInfo poolInfo){
		if(poolName == null || poolInfo == null){
			return false;
		}
		if(this._isExistPoolName(poolName,IPool.POOL_TYPE.TIMER_POOL_TYPE)){
			return true;
		}
		
		TimerPool pool = new TimerPool(poolName,poolInfo);
		boolean isSuccess = pool.start();
		this.info("Create New TimerPool["+poolName+"] "+(isSuccess?"Success":"Failure"));
		
		if(isSuccess){
			synchronized(this.timerPoolMapping){
				this.timerPoolMapping.put(poolName, pool);
			}
		}else{
			pool.stop();
		}
		
		return isSuccess;
	}

	public boolean isExistThreadPool(String poolName){
		if(poolName == null){
			return false;
		}
		return this._isExistPoolName(poolName,IPool.POOL_TYPE.THREAD_POOL_TYPE);
	}
	/**
	 * 得到线程池已经完成的任务数
	 * @param poolName 线程池名
	 * @return
	 */
	public long getThreadPoolFinishTaskNum(String poolName){
		return this._getFinishTaskNum(poolName, IPool.POOL_TYPE.THREAD_POOL_TYPE);
	}
	/**
	 * 得到定时器池已经完成过的任务数，如果一个定时任务周期性执行，每执行一次计数增加1
	 * @param poolName
	 * @return
	 */
	public long getTimerPoolFinishTaskNum(String poolName){
		return this._getFinishTaskNum(poolName, IPool.POOL_TYPE.TIMER_POOL_TYPE);
	}
	private long _getFinishTaskNum(String poolName,IPool.POOL_TYPE type){
		ThreadPool pool = (ThreadPool)this._getPool(poolName, type);
		if(pool == null){
			return -1;
		}
		return pool.getFinishTaskNum();
	}
	
	public int getThreadNum(String poolName){
		ThreadPool pool = (ThreadPool)this._getPool(poolName, IPool.POOL_TYPE.THREAD_POOL_TYPE);
		if(pool == null){
			return -1;
		}
		return pool.getThreadNum();
	}

	/**
	 * 得到线程池已经完成的任务数
	 * @param poolName 线程池名
	 * @return
	 */
	public long getThreadPoolCancelTaskNum(String poolName){
		return this._getCancelTaskNum(poolName, IPool.POOL_TYPE.THREAD_POOL_TYPE);
	}
	/**
	 * 得到定时器池已经完成过的任务数，如果一个定时任务周期性执行，每执行一次计数增加1
	 * @param poolName
	 * @return
	 */
	public long getTimerPoolCancelTaskNum(String poolName){
		return this._getFinishTaskNum(poolName, IPool.POOL_TYPE.TIMER_POOL_TYPE);
	}
	private long _getCancelTaskNum(String poolName,IPool.POOL_TYPE type){
		IPool pool = this._getPool(poolName, type);
		if(pool == null){
			return -1;
		}
		return pool.getCancelTaskNum();
	}
	

	/**
	 * 得到线程池中等待执行的任务数
	 * @param poolName 线程池名称
	 * @return
	 */
	public int getThreadPoolWaitTaskNum(String poolName){
		return this._getWaitTaskNum(poolName, IPool.POOL_TYPE.THREAD_POOL_TYPE);
	}

	/**
	 * 得到线程池中等待执行的任务数
	 * @param poolName 线程池名称
	 * @return
	 */
	public int[] getThreadPoolWaitTaskNumArr(String poolName){
		ThreadPool pool = (ThreadPool)this._getPool(poolName, IPool.POOL_TYPE.THREAD_POOL_TYPE);
		if(pool == null){
			return new int[0];
		}
		return pool.getWaitTaskNumArr();
	}
	/**
	 * 得到定时器中等待执行的任务数，通常应该为0，不至于有任务堆积等待
	 * @param poolName 定时池名称
	 * @return
	 */
	public int getTimerPoolWaitTaskNum(String poolName){
		return this._getWaitTaskNum(poolName, IPool.POOL_TYPE.TIMER_POOL_TYPE);
	}
	private int _getWaitTaskNum(String poolName,IPool.POOL_TYPE type){
		IPool pool = this._getPool(poolName, type);
		if(pool == null){
			return -1;
		}
		return pool.getWaitTaskNum();
	}

	public boolean isExistTimerPool(String poolName){
		if(poolName == null){
			return false;
		}
		return this._isExistPoolName(poolName,IPool.POOL_TYPE.TIMER_POOL_TYPE);
	}
	
	public synchronized void removeThreadPool(String poolName){
		if(poolName == null){
			return ;
		}
		ThreadPool pool = (ThreadPool)this._removeThreadPool(poolName,IPool.POOL_TYPE.THREAD_POOL_TYPE);
		if(pool == null){
			return ;
		}
		pool.stop();
		this.info("Destroy ThreadPool["+poolName+"]");
	}

	public synchronized void removeTimerPool(String poolName){
		if(poolName == null){
			return ;
		}
		TimerPool pool = (TimerPool)this._removeThreadPool(poolName,IPool.POOL_TYPE.TIMER_POOL_TYPE);
		if(pool == null){
			return ;
		}
		pool.stop();
		this.info("Destroy TimerPool["+poolName+"]");
	}
	
	public boolean addTask2ThreadPool(String poolName,ITask task){
		return this.addTask2ThreadPool(poolName, task, null);
	}
	public boolean addTask2ThreadPool(String poolName,ITask task,IEventListener listener){
		if(poolName == null || task == null){
			return false;
		}
		ThreadPool pool = (ThreadPool)this._getPool(poolName,IPool.POOL_TYPE.THREAD_POOL_TYPE);
		if(pool == null){
			return false;
		}
		return pool.addTask(task, listener);
	}
	public boolean schedule(String poolName,ITask task,Date startTime){
		if(startTime == null){
			return false;
		}
		return this.schedule(poolName, task, startTime.getTime()-System.currentTimeMillis(), -1, null);
	}
	public boolean schedule(String poolName, ITask task, Date startTime, IEventListener listener){
		if(startTime == null){
			return false;
		}
		return this.schedule(poolName, task, startTime.getTime()-System.currentTimeMillis(), -1, listener);
	}
	public boolean schedule(String poolName,ITask task,long delay){
		return this.schedule(poolName, task, delay, -1, null);
	}
	public boolean schedule(String poolName,ITask task,long delay,IEventListener listener){
		return this.schedule(poolName, task, delay, -1, listener);
	}
	public boolean schedule(String poolName,ITask task,Date startTime,long period, IEventListener listener){
		if(startTime == null){
			return false;
		}
		return this.schedule(poolName, task, (System.currentTimeMillis()-startTime.getTime()), period, listener);
	}
	public boolean schedule(String poolName,ITask task,long delay,long period,IEventListener listener){
		if(poolName == null || task == null){
			return false;
		}
		TimerPool pool = (TimerPool)this._getPool(poolName,IPool.POOL_TYPE.TIMER_POOL_TYPE);
		if(pool == null){
			return false;
		}
		return pool.schedule(task, delay, period, listener);
	}

	private boolean _isExistPoolName(String poolName,IPool.POOL_TYPE poolType){
		if(poolType.equals(IPool.POOL_TYPE.THREAD_POOL_TYPE)){
			synchronized(this.threadPoolMapping){
				return this.threadPoolMapping.containsKey(poolName);
			}
		}else if(poolType.equals(IPool.POOL_TYPE.TIMER_POOL_TYPE)){
			synchronized(this.timerPoolMapping){
				return this.timerPoolMapping.containsKey(poolName);
			}
		}
		return false;
	}
	private IPool _getPool(String poolName,IPool.POOL_TYPE poolType){
		if(poolType.equals(IPool.POOL_TYPE.THREAD_POOL_TYPE)){
			synchronized(this.threadPoolMapping){
				return this.threadPoolMapping.get(poolName);
			}
		}else if(poolType.equals(IPool.POOL_TYPE.TIMER_POOL_TYPE)){
			synchronized(this.timerPoolMapping){
				return this.timerPoolMapping.get(poolName);
			}
		}
		return null;
	}
	private IPool _removeThreadPool(String poolName,IPool.POOL_TYPE poolType){
		
		if(poolType.equals(IPool.POOL_TYPE.THREAD_POOL_TYPE)){
			synchronized(this.threadPoolMapping){
				return this.threadPoolMapping.remove(poolName);
			}
		}else if(poolType.equals(IPool.POOL_TYPE.TIMER_POOL_TYPE)){
			synchronized(this.timerPoolMapping){
				return this.timerPoolMapping.remove(poolName);
			}
		}
		return null;
	}
}
