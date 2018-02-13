package com.xzy.base.server.pool;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xzy.base.Const;
import com.xzy.base.server.log.LogRecordEvent;
import com.xzy.base_c.ServerContainer;
import com.xzy.base_i.IEventListener;
import com.xzy.base_i.IPool;
import com.xzy.base_i.IThread;

/**
 * �̳߳ض���,����֧�ֶ�̬�̳߳ء�������̳߳صķ���
 * @author edmund
 *
 */
public class ThreadPool implements IPool {
	private Exception stackTrace = null;
	private String poolName = null;
	private boolean _isRunning = false;
	private long cancelTaskNum = 0;
	private long finishTaskNum = 0;
	private ThreadPoolInfo poolInfo = null;
	
	private ArrayList<LinkedList<ThreadTask>> listArr = null;
	private ArrayList<Worker> workerList = null;
	private HashMap<Object,NestList> flag2ListMapping = null;

	public ThreadPool(String poolName,ThreadPoolInfo poolInfo){
		this.poolName = poolName;
		this.poolInfo = poolInfo;
		this.stackTrace = new Exception("Stack Trace");
	}
	
	public Exception getCreateStack(){
		return this.stackTrace;
	}
	
	public int getThreadNum(){
		if(!this.isRunning()){
			return -1;
		}
		return this.workerList.size();
	}
	
	/**
	 * ����ͨ������ִ����ɵ�����������ÿ���������ʱ������ô˺���
	 */
	private synchronized void addFinishTaskNum(){
		this.finishTaskNum ++ ;
	}
	private synchronized void addCancelTaskNum(){
		this.cancelTaskNum ++;
	}
	/**
	 * �õ�������ɵ�������
	 * @return
	 */
	public long getFinishTaskNum(){
		if(!this.isRunning()){
			return -1;
		}
		return this.finishTaskNum;
	}
	public long getCancelTaskNum(){
		if(!this.isRunning()){
			return -1;
		}
		return this.cancelTaskNum;
	}
	
	public boolean isRunning(){
		return this._isRunning;
	}
	
	/**
	 * �õ��ȴ�ִ�е�������
	 */
	public synchronized int getWaitTaskNum(){
		if(!this.isRunning()){
			return -1;
		}
		
		int waitTaskNum = 0;
		for(int i=0;i<this.listArr.size();i++){
			waitTaskNum += this.listArr.get(i).size();
		}
		return waitTaskNum;
	}

	/**
	 * �õ��ȴ�ִ�е�������
	 */
	public synchronized int[] getWaitTaskNumArr(){
		if(!this.isRunning()){
			return new int[0];
		}
		
		int[] waitTaskNumArr = new int[this.listArr.size()];
		for(int i=0;i<this.listArr.size();i++){
			waitTaskNumArr[i] = this.listArr.get(i).size();
		}
		return waitTaskNumArr;
	}
	
	@Override
	public String getPoolName() {
		return this.poolName;
	}

	/**
	 * �����̳߳�
	 */
	public synchronized boolean start(){
		if(this.isRunning()){
			return true;
		}
		this.listArr = new ArrayList<LinkedList<ThreadTask>>();
		this.workerList = new ArrayList<Worker>();
		
		ThreadPoolInfo.THREAD_TYPE threadType = this.poolInfo.get_threadType();
		ThreadPoolInfo.QUEUE_TYPE queueType = this.poolInfo.get_queueType();
		
		if(threadType == ThreadPoolInfo.THREAD_TYPE.DYNAMIC_TYPE && queueType == ThreadPoolInfo.QUEUE_TYPE.MULTIPLE_QUEUE_POOL){
			new LogRecordEvent(this).error(this.getClass(), "Thread Pool["+this.poolName+"] Type Error,Can't Start. ThreadType="+threadType+" QueueType="+queueType);
			if(this.poolInfo.getListener() != null){
				this.poolInfo.getListener().dispose(new PoolEvent(IPool.POOL_TYPE.THREAD_POOL_TYPE,PoolEvent.EVENT_TYPE.CREATE_FAILURE,this.getPoolName(),null));
			}
			return false;
		}

		int minThreadNum = this.poolInfo.getMinThreadNum();
		int maxThreadNum = this.poolInfo.getMaxThreadNum();
		int maxTaskNum = this.poolInfo.getMaxTaskNum();
		
		if(maxTaskNum < 1){
			new LogRecordEvent(this).error(this.getClass(), "Thread Pool["+this.poolName+"] Type Error,Can't Start. MaxTaskNum="+maxTaskNum);
			if(this.poolInfo.getListener() != null){
				this.poolInfo.getListener().dispose(new PoolEvent(IPool.POOL_TYPE.THREAD_POOL_TYPE,PoolEvent.EVENT_TYPE.CREATE_FAILURE,this.getPoolName(),null));
			}
		}
		
		if(!this.addQueueAndThread(queueType,minThreadNum)){
			new LogRecordEvent(this).error(this.getClass(), "Thread Pool["+this.poolName+"] Create Thread Error,Can't Start. threadNum="+minThreadNum);
			if(this.poolInfo.getListener() != null){
				this.poolInfo.getListener().dispose(new PoolEvent(IPool.POOL_TYPE.THREAD_POOL_TYPE,PoolEvent.EVENT_TYPE.CREATE_FAILURE,this.getPoolName(),null));
			}
			
			return false;
		}

		if(this.poolInfo.getListener() != null){
			this.poolInfo.getListener().dispose(new PoolEvent(IPool.POOL_TYPE.THREAD_POOL_TYPE,PoolEvent.EVENT_TYPE.CREATE_SUCCESS,this.poolName,null));
		}
		
		this._isRunning = true;
		return this.isRunning();
	}
	
	/**
	 * �����̳߳صĶ��к��߳�
	 * @param queueType ���з�Ϊ�����кͶ����
	 * @param num  �������߳�����
	 * @return true����ɹ�����֮����ʧ��
	 */
	private synchronized boolean addQueueAndThread(ThreadPoolInfo.QUEUE_TYPE queueType,int num){
		Worker worker;
		LinkedList<ThreadTask> taskList;

		try{
			int oldNum = this.workerList.size();
			for(int i=0;i<num;i++){
				if(queueType == ThreadPoolInfo.QUEUE_TYPE.MULTIPLE_QUEUE_POOL){
					this.listArr.add(taskList = new LinkedList<ThreadTask>());
				}else{
					if(this.listArr.size() == 0){
						this.listArr.add(taskList = new LinkedList<ThreadTask>());
					}else{
						taskList = this.listArr.get(0);
					}
				}
				
				worker = new Worker(taskList,this.poolInfo.getPriority(),this.poolInfo.isDaemo());
				worker.setName(this.poolName+"-thread-"+(oldNum+i+1));
				worker.start();
				this.workerList.add(worker);
			}

			if(this.poolInfo.getListener() != null){
				JSONObject jsonPara = new JSONObject();
				jsonPara.put("oldNum", oldNum);
				jsonPara.put("newNum", this.workerList.size());
				this.poolInfo.getListener().dispose(new PoolEvent(IPool.POOL_TYPE.THREAD_POOL_TYPE,PoolEvent.EVENT_TYPE.THREAD_NUM_CHANGE,this.poolName,jsonPara));
			}
		}catch(Throwable e){
			e.printStackTrace();
			new LogRecordEvent(this).fatal(ThreadPool.class, this.poolName, e);
			return false;
		}
		return true;
	}
	
	/**
	 * ֹͣ�̳߳�
	 */
	public synchronized void stop(){
		this._isRunning = false;
		for(Iterator<Worker> itr = this.workerList.iterator();itr.hasNext();){
			itr.next().stopThread();
		}
		this.listArr = null;
		this.workerList = null;
		this.flag2ListMapping = null;
		
		if(this.poolInfo.getListener() != null){
			this.poolInfo.getListener().dispose(new PoolEvent(IPool.POOL_TYPE.THREAD_POOL_TYPE,PoolEvent.EVENT_TYPE.DESTROY,this.poolName,null));
		}
	}
	
	/**
	 * ��������̳߳���
	 * @param task �������
	 * @return
	 */
	public boolean addTask(ITask task){
		return this.addTask(task, null);
	}
	
	/**
	 * ��������̳߳��У�����ִ����ϣ���ͨ�����������лص�
	 * @param task ��ִ�е�����
	 * @param listener �����������
	 * @return
	 */
	public boolean addTask(ITask task,IEventListener listener){
		if(!this.isRunning()){
			return false;
		}
		
		NestList nestList = this.seletctList(task);
		
		synchronized(nestList.taskList){
			if(nestList.taskList.size() >= this.poolInfo.getMaxTaskNum()){
				if(this.poolInfo.getListener() != null || listener != null){
					PoolEvent e = new PoolEvent(IPool.POOL_TYPE.THREAD_POOL_TYPE,PoolEvent.EVENT_TYPE.TASK_OVERFLOW,this.poolName,null);
					if(this.poolInfo.getListener() != null){
						this.poolInfo.getListener().dispose(e);
					}
					if(listener != null){
						listener.dispose(e);
					}
				}
				return false;
			}
			
			//��̬�����߳�
			if(this.poolInfo.get_threadType() == ThreadPoolInfo.THREAD_TYPE.DYNAMIC_TYPE){
				boolean needAdd = true;
				int idleNum = 0;
				for(Iterator<Worker> itr = this.workerList.iterator();itr.hasNext();){
					if(itr.next().getCurTaskStartWorkTime() == 0){
						idleNum ++;
					}
				}
				if(idleNum > nestList.taskList.size()){
					needAdd = false;
				}
				
				if(needAdd){
					if(this.workerList.size() < this.poolInfo.getMaxThreadNum()){
						this.addQueueAndThread(this.poolInfo.get_queueType(), 1);
					}else{
						if(this.poolInfo.getListener() != null){
							try{
								JSONObject jsonPara = new JSONObject();
								JSONArray workerArr = null;
								jsonPara.put("threadNum", this.workerList.size());
								jsonPara.put("taskArr", workerArr = new JSONArray());
								Worker worker = null;
								ThreadTask threadTask = null;
								for(Iterator<Worker> itr = this.workerList.iterator();itr.hasNext();){
									worker = itr.next();
									threadTask = worker.getCurTask();
									if(threadTask != null){
										workerArr.put(new JSONObject().put("name", threadTask.getTask().getName()).put("startTime", Const.getDateFormater("yyyyMMddHHmmss").format(new Date(worker.getCurTaskStartWorkTime()))));
									}
								}
								
								new LogRecordEvent(this).error(ThreadPool.class, "Thread Pool["+this.poolName+"] Arrive Max Num["+this.poolInfo.getMaxThreadNum()+"]:"+jsonPara.toString());
								this.poolInfo.getListener().dispose(new PoolEvent(IPool.POOL_TYPE.THREAD_POOL_TYPE,PoolEvent.EVENT_TYPE.NO_ENOUGH_THREAD,this.poolName,jsonPara));
							}catch(Exception e){
								e.printStackTrace();
							}
						}
						
						return false;
					}
				}

				nestList.add();
			}
			nestList.taskList.add(new ThreadTask(nestList,task,listener));
			nestList.taskList.notifyAll();
		}
		
		return true;
	}
	
	private NestList singleQueueNestList = null;
	private int loopListIndex = 0;
	/**
	 * ѡ�������Ӧ��list
	 * @param task �������
	 * @return
	 */
	private synchronized NestList seletctList(ITask task){
		if(this.poolInfo.get_queueType() == ThreadPoolInfo.QUEUE_TYPE.MULTIPLE_QUEUE_POOL){
			if(this.flag2ListMapping == null){
				this.flag2ListMapping = new HashMap<Object,NestList>();
			}
			Object flag = task.getFlag();
			NestList nestList = this.flag2ListMapping.get(flag);
			if(nestList == null){
				nestList = new NestList(flag,this.listArr.get(this.loopListIndex%this.listArr.size()));
				this.loopListIndex ++;
				this.loopListIndex = this.loopListIndex%this.listArr.size();
				this.flag2ListMapping.put(flag, nestList);
			}
			
			return nestList;
		}
		if(this.singleQueueNestList == null){
			this.singleQueueNestList = new NestList(null,this.listArr.get(0));
		}
		return this.singleQueueNestList;
	}
	
	private class NestList{
		Object flag;
		int taskNum = 0;
		LinkedList<ThreadTask> taskList;
		public NestList(Object flag,LinkedList<ThreadTask> taskList){
			this.flag = flag;
			this.taskList = taskList;
		}
		public synchronized void add(){
			this.taskNum++;
		}
		public synchronized void minus(){
			this.taskNum--;
			if(this.taskNum <= 0 && ThreadPool.this.flag2ListMapping != null){
				synchronized(ThreadPool.this){
					ThreadPool.this.flag2ListMapping.remove(this.flag);
				}
			}
		}
	}
	
	/**
	 * �߳��ڵ������װ��
	 * @author edmund
	 *
	 */
	private class ThreadTask{
		private NestList nestList;
		private ITask task = null;
		private IEventListener listener = null;
		
		public ThreadTask(NestList nestList,ITask task,IEventListener listener){
			this.nestList = nestList;
			this.task = task;
			this.listener = listener;
		}

		public ITask getTask() {
			return task;
		}

		public IEventListener getListener() {
			return listener;
		}
		
		public void finish(){
			this.nestList.minus();
		}
	}
	
	/**
	 * ����ִ�е��̶߳���
	 * @author edmund
	 *
	 */
	private class Worker extends Thread implements IThread{
		private boolean isStop = false;
		private LinkedList<ThreadTask> taskList = null;
		
		private Exception startStackTrace = null;
		private long startTime = 0;
		
		private ThreadTask curThreadTask = null;
		private long startWorkTime = 0;
		
		public Worker(LinkedList<ThreadTask> taskList,int priority,boolean isDaemo){
			this.taskList = taskList;
			this.isStop = false;
			this.setPriority(priority);
			this.setDaemon(isDaemo);
			this.startStackTrace = new Exception("Stack trace");
		}
		
		public long getCurTaskStartWorkTime(){
			return this.startWorkTime;
		}
		
		public ThreadTask getCurTask(){
			return this.curThreadTask;
		}
		public Exception getCreateStack(){
			return this.startStackTrace;
		}
		public long getStartTime(){
			return this.startTime;
		}
		
		public void start(){
			this.startTime = System.currentTimeMillis();
			this.startStackTrace = new Exception("Stack trace");
			ServerContainer.getSingleInstance().registThread(this);
			super.start();
		}
		
		public void run(){
			try{
				if(this.taskList == null){
					return ;
				}
				
				while(!isStop){
					curThreadTask = null;
					if(this.taskList.isEmpty()){
						synchronized(this.taskList){
							if(isStop){
								break;
							}
							
							if(this.taskList.isEmpty()){
								this.startWorkTime = 0;
								try{
									this.taskList.wait(5000);
								}catch(Exception e){
									e.printStackTrace();
								}
							}
						}
					}else{
						synchronized(this.taskList){
							if(!this.taskList.isEmpty()){
								curThreadTask = this.taskList.remove(0);
							}
						}

						if(curThreadTask != null){
							ITask task = curThreadTask.getTask();
							this.startWorkTime = System.currentTimeMillis();
							try{
								if(!task.isCancel()){
									if(curThreadTask.listener != null){
										JSONObject jsonPara = new JSONObject();
										jsonPara.put("taskName", task.getName());
										jsonPara.put("thread", this.getName());
										curThreadTask.listener.dispose(new PoolEvent(IPool.POOL_TYPE.THREAD_POOL_TYPE,PoolEvent.EVENT_TYPE.TASK_START,poolName,jsonPara));
									}
									
									task.run();
									
									if(curThreadTask.listener != null){
										JSONObject jsonPara = new JSONObject();
										jsonPara.put("taskName", task.getName());
										jsonPara.put("thread", this.getName());
										curThreadTask.listener.dispose(new PoolEvent(IPool.POOL_TYPE.THREAD_POOL_TYPE,PoolEvent.EVENT_TYPE.TASK_FINISH,poolName,jsonPara));
									}
									ThreadPool.this.addFinishTaskNum();
								}else{
									ThreadPool.this.addCancelTaskNum();
									if(curThreadTask.listener != null){
										JSONObject jsonPara = new JSONObject();
										jsonPara.put("taskName", task.getName());
										jsonPara.put("thread", this.getName());
										curThreadTask.listener.dispose(new PoolEvent(IPool.POOL_TYPE.THREAD_POOL_TYPE,PoolEvent.EVENT_TYPE.TASK_CANCEL,poolName,jsonPara));
									}
								}
							}catch(Throwable e){
								e.printStackTrace();
							}
							
							curThreadTask.finish();
							TaskCostStatServer.getSingleInstance().addCostInfo(ThreadPool.this,task.getClass().getName()+"["+task.getName()+"]",System.currentTimeMillis()-this.startWorkTime);
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			catch(Error e)
			{
				e.printStackTrace();
			}
			finally{
				//TODO
				ServerContainer.getSingleInstance().unregistThread(this);
			}
		}
		
		public void stopThread(){
			try{
				this.isStop = true;
				ThreadTask task = this.curThreadTask;
				if(task != null){
					task.getTask().cancel();
				}
				if(this.taskList != null){
					synchronized(this.taskList){
						this.taskList.notifyAll();
					}
				}
				this.interrupt();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
