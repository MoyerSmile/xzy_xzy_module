package com.xzy.base.server.pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.xzy.base.server.log.LogRecordEvent;
import com.xzy.base_i.IEventListener;
import com.xzy.base_i.IPool;

public class TimerPool implements IPool {
	private boolean _isRunning = false;
	private String poolName = null;
	private Exception stackTrace = null;
	private TimerPoolInfo poolInfo = null;
	private String threadPoolName = null;
	
	public TimerPool(String poolName,TimerPoolInfo poolInfo){
		this.poolName = poolName;
		this.threadPoolName = this.poolName+"-ThreadPool["+this.hashCode()+"]";
		this.poolInfo = poolInfo;
		this.stackTrace = new Exception("Stack Trace");
	}
	@Override
	public String getPoolName() {
		return this.poolName;
	}

	@Override
	public boolean start() {
		ThreadPoolInfo threadPoolInfo = new ThreadPoolInfo(this.poolInfo.getTimerNum(),100);
		this._isRunning = ThreadPoolServer.getSingleInstance().createThreadPool(this.threadPoolName, threadPoolInfo);
		if(this.poolInfo.getListener() != null){
			this.poolInfo.getListener().dispose(new PoolEvent(IPool.POOL_TYPE.TIMER_POOL_TYPE,this._isRunning?PoolEvent.EVENT_TYPE.CREATE_SUCCESS:PoolEvent.EVENT_TYPE.CREATE_FAILURE,this.poolName,null));
		}
		return this._isRunning;
	}
	
	public boolean isRunning(){
		return this._isRunning;
	}

	@Override
	public void stop() {
		this._isRunning = false;
		ThreadPoolServer.getSingleInstance().removeThreadPool(this.threadPoolName);
		if(this.poolInfo.getListener() != null){
			this.poolInfo.getListener().dispose(new PoolEvent(IPool.POOL_TYPE.TIMER_POOL_TYPE,PoolEvent.EVENT_TYPE.DESTROY,this.poolName,null));
		}
	}

	public long getFinishTaskNum(){
		if(this.threadPoolName == null || !this.isRunning()){
			return -1;
		}
		return ThreadPoolServer.getSingleInstance().getThreadPoolFinishTaskNum(this.threadPoolName);
	}
	/**
	 * 得到等待执行的任务数
	 */
	public synchronized int getWaitTaskNum(){
		if(this.threadPoolName == null || !this.isRunning()){
			return -1;
		}
		return ThreadPoolServer.getSingleInstance().getThreadPoolWaitTaskNum(this.threadPoolName);
	}
	
	public long getCancelTaskNum(){
		if(this.threadPoolName == null || !this.isRunning()){
			return -1;
		}
		return ThreadPoolServer.getSingleInstance().getThreadPoolCancelTaskNum(this.threadPoolName);
	}

	public Exception getCreateStack(){
		return this.stackTrace;
	}
	
	public boolean schedule(ITask task,long delay,long period,IEventListener listener){
		return SharedTimerPool.getSingleInstance().schedule(new XZYTimerTask(task,listener), delay, period);
	}

	private class XZYTimerTask extends TimerTask{
		private ITask task = null;
		private IEventListener listener = null;
		private ExecTask curTask = null;
		
		public XZYTimerTask(ITask task,IEventListener listener){
			this.task = task;
			this.listener = listener;
		}
		
		public void run(){
			if(this.task.isCancel()){//如果任务被取消，那么取消定时任务
				this.cancel();
			}else{
				if(ThreadPoolServer.getSingleInstance().isExistThreadPool(threadPoolName)){
					if(this.curTask != null && !this.curTask.isStarted()){
						new LogRecordEvent(this).error(TimerPool.class, "Timer Task Can't Execute because of PreTask is not started."+poolName+" "+this.task.getName()+" "+this.curTask.getCreateTime());
						if(listener != null){
							JSONObject jsonPara = new JSONObject();
							try{
								jsonPara.put("task", this.task.getName());
								jsonPara.put("class", this.task.getClass().getName());
								jsonPara.put("timer", poolName);
								jsonPara.put("preCreateTime", this.curTask.getCreateTime());
							}catch(Exception e){
								e.printStackTrace();
							}
							listener.dispose(new PoolEvent(IPool.POOL_TYPE.TIMER_POOL_TYPE,PoolEvent.EVENT_TYPE.TIMER_TASK_NO_EXECUTE,threadPoolName,jsonPara));
						}
						return;
					}
					this.curTask = new ExecTask(this.task);
					if(!ThreadPoolServer.getSingleInstance().addTask2ThreadPool(threadPoolName,this.curTask,listener)){
						if(listener != null){
							JSONObject jsonPara = new JSONObject();
							try{
								jsonPara.put("task", this.task.getName());
								jsonPara.put("class", this.task.getClass().getName());
								jsonPara.put("timer", poolName);
							}catch(Exception e){
								e.printStackTrace();
							}
							listener.dispose(new PoolEvent(IPool.POOL_TYPE.TIMER_POOL_TYPE,PoolEvent.EVENT_TYPE.TIMER_TASK_NO_EXECUTE,threadPoolName,jsonPara));
						}
						new LogRecordEvent(this).error(TimerPool.class, "Timer Task Can't Execute because of pool task overflow."+poolName+" "+this.task.getName());
					}
				}else{
					this.cancel();//线程池已经关闭，任务取消
					ThreadPoolServer.getSingleInstance().removeThreadPool(poolName);
				}
			}
		}
	}
	
	private static class ExecTask extends BasicTask{
		private long createTime = 0;
		private long startTime = 0;
		private boolean isStartTask = false;
		private ITask task = null;
		public ExecTask(ITask task){
			this.task = task;
			this.createTime = System.currentTimeMillis();
		}
		
		public long getCreateTime(){
			return this.createTime;
		}
		public long getStartTime(){
			return this.startTime;
		}
		@Override
		public void run() {
			this.startTime = System.currentTimeMillis();
			this.isStartTask = true;
			this.task.run();
		}
		
		public boolean isCancel(){
			return this.task.isCancel();
		}
		
		public boolean isStarted(){
			return this.isStartTask;
		}
		public String getName() {
			return this.task.getName();
		}
		public Object getFlag() {
			return this.task.getFlag();
		}
	}
	
	/**
	 * 全局的定时池，用于对所有的定时任务在对应的时刻产生任务，并提交给线程池进行处理。定时器本身的事情比较简单，只是进行任务的发布，所以对整个应用只开启两个定时器。
	 * @author fleety
	 *
	 */
	private static class SharedTimerPool{
		private static SharedTimerPool singleInstance = null;
		private ArrayList<Timer> timerList = null;
		private int timerIndex = 0;
		
		public synchronized static SharedTimerPool getSingleInstance(){
			if(singleInstance == null){
				singleInstance = new SharedTimerPool();
				singleInstance.start(2);
			}
			return singleInstance;
		}
		
		public synchronized boolean start(int timerNum){
			this.stop();
			
			this.timerList = new ArrayList<Timer>(4);
			for(int i=0;i<timerNum;i++){
				this.timerList.add(new Timer(true));
			}
			return true;
		}
		
		public synchronized void stop(){
			if(this.timerList == null){
				return ;
			}
			for(Iterator<Timer> itr = this.timerList.iterator();itr.hasNext();){
				itr.next().cancel();
			}
			this.timerList = null;
		}
		
		public synchronized boolean schedule(TimerTask task,long delay,long period){
			if(this.timerList == null || this.timerList.size() == 0){
				return false;
			}
			Timer timer = this.timerList.get(this.timerIndex);
			if(period > 0){
				timer.schedule(task, delay, period);
			}else{
				timer.schedule(task, delay);
			}
			this.timerIndex ++;
			this.timerIndex = this.timerIndex%this.timerList.size();
			
			return true;
		}
	}
}
