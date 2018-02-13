package test.xzy.base;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import static org.junit.Assert.*;
import org.junit.Test;

import com.xzy.base.server.log.LogRecordEvent;
import com.xzy.base.server.log.LogRecordServer;
import com.xzy.base.server.pool.BasicTask;
import com.xzy.base.server.pool.ITask;
import com.xzy.base.server.pool.PoolEvent;
import com.xzy.base.server.pool.ThreadPoolInfo;
import com.xzy.base.server.pool.ThreadPoolServer;
import com.xzy.base.server.pool.TimerPoolInfo;
import com.xzy.base_c.ServerContainer;
import com.xzy.base_i.IEvent;
import com.xzy.base_i.IEventListener;
import com.xzy.base_i.IEventResponse;
import com.xzy.base_i.IPool;

public class ThreadPoolServerTest {
	String poolName = "test";
	private static int seq = 1;
	public ThreadPoolServerTest(){
		this.poolName = "test"+seq++;
	}
	public void prepare(int threadNum){
		this.prepare(threadNum, null);
	}
	public void prepare(int threadNum,IEventListener listener){
		this.prepare(threadNum,20,listener);
	}
	public void prepare(int threadNum,int maxTaskNum,IEventListener listener){
		this.prepare(threadNum, maxTaskNum, ThreadPoolInfo.QUEUE_TYPE.SINGLE_QUEUE_POOL, listener);
	}
	public void prepare(int threadNum,int maxTaskNum,ThreadPoolInfo.QUEUE_TYPE queueType,IEventListener listener){
		this.prepare(threadNum, maxTaskNum, ThreadPoolInfo.THREAD_TYPE.STATIC_TYPE, queueType, listener);
	}
	public void prepare(int threadNum,int maxTaskNum,ThreadPoolInfo.THREAD_TYPE threadType,ThreadPoolInfo.QUEUE_TYPE queueType,IEventListener listener){
		LogRecordServer.getSingleInstance().startServer();
		this.destroy();
		ThreadPoolInfo poolInfo = new ThreadPoolInfo(threadNum,maxTaskNum,queueType);
		if(threadType == ThreadPoolInfo.THREAD_TYPE.DYNAMIC_TYPE){
			poolInfo.update2DynamicThreadNum(maxTaskNum);
		}
		poolInfo.setListener(listener);
		ThreadPoolServer.getSingleInstance().createThreadPool(poolName, poolInfo);
		
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void destroy(){
		ThreadPoolServer.getSingleInstance().removeThreadPool(poolName);
	}
	
	
	/**
	 * 测试单个任务的执行
	 */
	@Test
	public void testTaskExecute() {
		this.prepare(2);
		TestTask task = new  TestTask(){
			public void run(){
				this.result = Boolean.TRUE;
			}
		};
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(task.getResult() == null){
			this.print("ThreadPool Task Execute Failure");
			this.destroy();
			fail("ThreadPool Task Execute Failure");
		}else{
			this.print("ThreadPool Task Execute Success");
		}
		
		this.destroy();
	}


	/**
	 * 测试多个任务在单队列中的执行结果，并且取消其中的一个任务，对应取消的任务应该不执行
	 */
	@Test
	public void testMultipleTaskExecute() {
		int threadNum = 3;
		this.prepare(threadNum);
		
		TestEventListener listener = new TestEventListener(){
			private HashMap mapping = new HashMap();
			public synchronized IEventResponse dispose(IEvent e){
				if(e.getEventType() == PoolEvent.EVENT_TYPE.TASK_FINISH){
					try{
						String threadName = e.getEventPara().getString("thread");
//						System.out.println("-----"+threadName+"   "+System.currentTimeMillis());
						Integer val = (Integer)mapping.get(threadName);
						if(val == null){
							val = new Integer(1);
						}else{
							val = new Integer(val.intValue()+1);
						}
						mapping.put(threadName, val);
					}catch(Exception ee){
						ee.printStackTrace();
					}
					
					this.result = mapping;
				}
				return null;
			}
		};
		
		TestTask[] taskArr = new TestTask[10];
		final long sleepTime = 200;
		for(int i=0;i<taskArr.length;i++){
			taskArr[i] = new  TestTask(){
				public void run(){
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					this.result = Boolean.TRUE;
				}
			};
			ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, taskArr[i],listener);
		}

		try{
			Thread.sleep(sleepTime/4);
		}catch(Exception e){}
		int waitTaskNum = ThreadPoolServer.getSingleInstance().getThreadPoolWaitTaskNum(poolName);
		if(waitTaskNum != (taskArr.length-threadNum)){
			this.print("wait task num error: "+waitTaskNum+" should be "+(taskArr.length-threadNum));
			this.destroy();
			fail("wait task num error");
		}
		
		int cancelIndex = 5;
		taskArr[cancelIndex].cancel();
		
		try {
			Thread.sleep(Math.round(taskArr.length/3.0 *sleepTime) + 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		String err = "";
		boolean isOk = true;
		for(int i=0;i<taskArr.length && isOk;i++){
			if(i == cancelIndex){
				if(taskArr[i].getResult() != null){
					err = "task "+i+" "+taskArr[i].getResult()+" it should be null";
					isOk = false;
				}
			}else{
				if(taskArr[i].getResult() == null){
					err = "task "+i+" "+taskArr[i].getResult()+" it should not be null";
					isOk = false;
				}
			}
		}
		
		if(isOk){
			HashMap mapping = (HashMap)listener.getResult();
			if(mapping.size() != threadNum){
				System.out.println(ServerContainer.getSingleInstance().getThreadInfo());
				err = "执行线程不是"+threadNum+"个:num="+mapping.size()+" real="+ThreadPoolServer.getSingleInstance().getThreadNum(poolName);
				isOk = false;
			}
			if(isOk){
				int destNum = (taskArr.length-1)/threadNum;
				for(Iterator itr = mapping.values().iterator();itr.hasNext();){
					int num = ((Integer)(itr.next())).intValue();
					if(num < destNum){
						isOk = false;
						err = "单个线程执行数量小于"+destNum+"，应该大于等于"+destNum+": num = "+num;
						break;
					}
				}
			}
		}
		
		if(!isOk){
			this.print("ThreadPool Task Execute Failure:err= "+err);
			this.destroy();
			fail("ThreadPool Multiple Task Execute Failure");
		}else{
			this.print("ThreadPool Multiple Task Execute Success");
		}
		
		if(isOk){
			long finishTaskNum = ThreadPoolServer.getSingleInstance().getThreadPoolFinishTaskNum(poolName);
			long cancelTaskNum = ThreadPoolServer.getSingleInstance().getThreadPoolCancelTaskNum(poolName);
			waitTaskNum = ThreadPoolServer.getSingleInstance().getThreadPoolWaitTaskNum(poolName);
			
			if(finishTaskNum != 9 || waitTaskNum != 0 || cancelTaskNum != 1){
				this.print("ThreadPool finish and wait Task's num error: finishTaskNum="+finishTaskNum+" ~9 waitTaskNum="+waitTaskNum+" ~0 cancelTaskNum="+cancelTaskNum+" ~1");
				this.destroy();
				fail("ThreadPool finish and wait Task's num error: finishTaskNum="+finishTaskNum+" ~9 waitTaskNum="+waitTaskNum+" ~0 cancelTaskNum="+cancelTaskNum+" ~1");
			}
		}
		
		this.destroy();
	}


	@Test
	public void testEventListener() {
		TestEventListener listener = new TestEventListener(){
			public synchronized IEventResponse dispose(IEvent _e){
				PoolEvent e = (PoolEvent)_e;

				if(e.getEventType() == PoolEvent.EVENT_TYPE.CREATE_SUCCESS){
					this.intOP(1);
				}else if(e.getEventType() == PoolEvent.EVENT_TYPE.DESTROY){
					this.intOP(2);
				}else if(e.getEventType() == PoolEvent.EVENT_TYPE.THREAD_NUM_CHANGE){
					this.intOP(4);
				}else if(e.getEventType() == PoolEvent.EVENT_TYPE.TASK_OVERFLOW){
					this.intOP(8);
				}
				
				
				return null;
			}
		};
		this.prepare(2,2,listener);

		TestEventListener taskListener = new TestEventListener(){
			public synchronized IEventResponse dispose(IEvent _e){
				PoolEvent e = (PoolEvent)_e;
				if(this.result == null){
					this.result = new int[2];
				}
				int[] val = (int[])this.result;
				if(e.getEventType() == PoolEvent.EVENT_TYPE.TASK_START){
					val[0] ++;
				}else if(e.getEventType() == PoolEvent.EVENT_TYPE.TASK_FINISH){
					val[1] ++;
				}
				
				return null;
			}
		};
	
		TestTask task = new  TestTask(){
			public void run(){
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.result = Boolean.TRUE;
			}
			public String getName(){
				return this.hashCode()+"";
			}
		};
		int okNum = 0;
		for(int i=0;i<6;i++){
			boolean isOk = ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task, taskListener);
			if(isOk){
				okNum ++;
			}
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		this.destroy();
		
		int[] val = (int[])taskListener.getResult();
		if(val[0] != okNum || val[1] != okNum){
			this.print("ThreadPool Task Listener Test Failure: val="+val[0]+" okNum="+okNum);
			this.destroy();
			fail("ThreadPool Task Listener Test Failure");
		}else{
			this.print("ThreadPool Task Listener Test Success");
		}
		Integer v = (Integer)listener.getResult();
		if(v == null || v.intValue() != 0x0f){
			this.print("ThreadPool Pool Listener Test Failure. v="+ (v==null?"null":v.intValue())+" should be 0x0f");
			this.destroy();
			fail("ThreadPool Pool Listener Test Failure");
		}else{
			this.print("ThreadPool Pool Listener Test Success");
		}
	}
	
	/**
	 * 测试多队列线程池
	 */
	@Test
	public void testMultipleQueue() {
		this.prepare(4, 20,ThreadPoolInfo.QUEUE_TYPE.MULTIPLE_QUEUE_POOL,null);
		
		TestEventListener taskListener = new TestEventListener(){
			private HashMap mapping = new HashMap();
			public synchronized IEventResponse dispose(IEvent e){
				if(e.getEventType() == PoolEvent.EVENT_TYPE.TASK_FINISH){
					try{
						String threadName = e.getEventPara().getString("thread");

						Integer val = (Integer)mapping.get(threadName);
						if(val == null){
							val = new Integer(1);
						}else{
							val = new Integer(val.intValue()+1);
						}
						mapping.put(threadName, val);
					}catch(Exception ee){
						ee.printStackTrace();
					}
					
					this.result = mapping;
				}
				return null;
			}
		};
		
		TestTask task1 = new  TestTask(){
			public void run(){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.result = Boolean.TRUE;
			}
			public Object getFlag(){
				return new Integer(1);
			}
		};

		TestTask task2 = new  TestTask(){
			public void run(){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.result = Boolean.TRUE;
			}
			public Object getFlag(){
				return new Integer(2);
			}
		};

		int task1Num = 8,task2Num = 5;
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task1, taskListener);
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task1, taskListener);
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task1, taskListener);
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task1, taskListener);
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task2, taskListener);
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task2, taskListener);
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task1, taskListener);
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task1, taskListener);
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task2, taskListener);
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task2, taskListener);
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task2, taskListener);
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task1, taskListener);
		ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task1, taskListener);
		
		try{
			Thread.sleep(1000);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		HashMap mapping = (HashMap)taskListener.getResult();
		
		boolean isOk = true;
		String err = "";
		if(mapping.size() != 2){
			isOk = false;
			err = "执行线程数应该为2个，实际为"+mapping.size();
		}
		
		if(isOk){
			int num;
			for(Iterator itr = mapping.values().iterator();itr.hasNext();){
				num = ((Integer)itr.next()).intValue();
				if(num != task1Num && num != task2Num){
					isOk = false;
					err = "线程任务数应该为5或者8，实际为"+num;
					break;
				}
			}
		}
		
		if(isOk){
			this.print("ThreadPool multiple queue test success");
		}else{
			this.print("ThreadPool multiple queue test failure:"+err);
			this.destroy();
			fail("ThreadPool multiple queue test failure:"+err);
		}
		
		this.destroy();
	}
	
	@Test
	public void testDynamicThread() {
		TestEventListener listener = new TestEventListener(){
			public IEventResponse dispose(IEvent _e){
				PoolEvent e = (PoolEvent)_e;
				
				Integer val = (Integer)this.result;
				if(val == null){
					val = new Integer(0);
				}
				
				if(e.getEventType() == PoolEvent.EVENT_TYPE.THREAD_NUM_CHANGE){
					try {
						int oldNum = e.getEventPara().getInt("oldNum");
						int newNum = e.getEventPara().getInt("newNum");
						if(oldNum == 0 && newNum == 2){
							this.intOP(1);
						}
						if(newNum == 3){
							this.intOP(2);
						}
						if(newNum == 4){
							this.intOP(4);
						}
						if(newNum == 5){
							this.intOP(8);
						}
						if(newNum == 6){
							this.intOP(16);
						}
					} catch (JSONException e1) {
						e1.printStackTrace();
					}
				}
				
				return null;
			}
		};
		this.prepare(2, 10, ThreadPoolInfo.THREAD_TYPE.DYNAMIC_TYPE,ThreadPoolInfo.QUEUE_TYPE.SINGLE_QUEUE_POOL,listener);
		
		ITask task = new BasicTask(){
			public void run(){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		for(int i=0;i<3;i++){
			ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task);
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Integer val = (Integer)listener.getResult();
		boolean isOk = (val.intValue() == 0x03);
		
		if(!isOk){
			this.print("ThreadPool Dynamic Thread Test Failure."+val.intValue());
			this.destroy();
			fail("ThreadPool Dynamic Thread Test Failure."+val.intValue());
			return ;
		}
		for(int i=0;i<6;i++){
			ThreadPoolServer.getSingleInstance().addTask2ThreadPool(poolName, task);
		}

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		val = (Integer)listener.getResult();
		isOk = (val.intValue() == 0x1F);
		
		if(!isOk){
			this.print("ThreadPool Dynamic Thread Test Failure."+val.intValue());
			this.destroy();
			fail("ThreadPool Dynamic Thread Test Failure."+val.intValue());
			return ;
		}
		
		this.print("ThreadPool Dynamic Thread Test Success.");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		this.destroy();
	}
	
	@Test
	public void testThreadInfo() {
		this.prepare(2);
		
		new LogRecordEvent(this).info(this.getClass(), "徐新测试");
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//线程池的数量、任务时长统计线程、日记记录线程
		int okThreadNum = 2+1+1;
		if(ServerContainer.getSingleInstance().getThreadTotalNum() != okThreadNum){
			this.print(ServerContainer.getSingleInstance().getThreadInfo().toString());
			this.print("线程数量错误:"+ServerContainer.getSingleInstance().getThreadTotalNum() +" 应该为"+okThreadNum);
			this.destroy();
			fail("线程数量错误:"+ServerContainer.getSingleInstance().getThreadTotalNum() +" 应该为"+okThreadNum);
		}else{
			this.print("ThreadPool Thread Num Test Success!");
		}
		this.destroy();
	}
	
	private void createTimerPool(int timerNum,IEventListener listener){
		this.destroyTimerPool();
		TimerPoolInfo poolInfo = new TimerPoolInfo(timerNum);
		poolInfo.setListener(listener);
		ThreadPoolServer.getSingleInstance().createTimerPool(poolName, poolInfo);
	}
	private void destroyTimerPool(){
		ThreadPoolServer.getSingleInstance().removeTimerPool(poolName);
	}
	
	@Test
	public void timerPoolCreateDestroyTest() {
		TestEventListener listener = null;
		this.createTimerPool(2, listener = new TestEventListener(){
			public IEventResponse dispose(IEvent _event) {
				PoolEvent event = (PoolEvent)_event;
				if(event.getPoolType() == IPool.POOL_TYPE.TIMER_POOL_TYPE){
					if(event.getEventType() == PoolEvent.EVENT_TYPE.CREATE_SUCCESS){
						this.intOP(1);
					}else if(event.getEventType() == PoolEvent.EVENT_TYPE.DESTROY){
						this.intOP(2);
					}
				}
				return null;
			}
		});
		
		boolean isOk = true;
		if(((Integer)listener.getResult()).intValue() != 0x01){
			isOk |= false;
		}

		this.destroyTimerPool();

		if(((Integer)listener.getResult()).intValue() != 0x03){
			isOk |= false;
		}
		
		if(!isOk){
			this.print("Timer Pool Create Destroy Test Fail.");
			fail("Timer Pool Create Destroy Test Fail.");
		}else{
			this.print("Timer Pool Create Destroy Test Success.");
		}
	}
	
	@Test
	public void timerTaskTest() {
		this.createTimerPool(2, null);
		
		//测试任务执行偏移以及事件通知
		TestEventListener listener = null;
		Date execTime = new Date(System.currentTimeMillis()/1000*1000 + 2000);
		ThreadPoolServer.getSingleInstance().schedule(poolName, new BasicTask(){
			public void run(){
				int allowOffset = 100;
				if((System.currentTimeMillis()+allowOffset)%1000 <= 2*allowOffset){
					print("TimerTask Exec Offset Test Success!"+System.currentTimeMillis());
				}else{
					print("TimerTask Exec Offset Test Fail!" + System.currentTimeMillis());
					fail("TimerTask Exec Offset Test Fail!");
				}
			}
		}, execTime,listener = new TestEventListener(){
			public IEventResponse dispose(IEvent _event) {
				PoolEvent event = (PoolEvent)_event;
				if(event.getEventType() == PoolEvent.EVENT_TYPE.TASK_START){
					this.intOP(1);
				}else if(event.getEventType() == PoolEvent.EVENT_TYPE.TASK_FINISH){
					this.intOP(2);
				}
				return null;
			}
		});
		
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(listener.getResult().equals(new Integer(0x03))){
			this.print("Timer Task Start Finish Listen Success!");
		}else{
			this.print("Timer Task Start Finish Listen Fail!");
			fail("Timer Task Start Finish Listen Fail!");
		}

		long startTime = System.currentTimeMillis();
		BasicTask task;
		ThreadPoolServer.getSingleInstance().schedule(poolName, task = new BasicTask(){
			public void run(){
				this.flag = new Date();
			}
		}, 1000,listener = new TestEventListener(){
			public IEventResponse dispose(IEvent _event) {
				PoolEvent event = (PoolEvent)_event;
				if(event.getEventType() == PoolEvent.EVENT_TYPE.TASK_START){
					this.intOP(1);
				}else if(event.getEventType() == PoolEvent.EVENT_TYPE.TASK_FINISH){
					this.intOP(2);
				}
				return null;
			}
		});
		try {
			Thread.sleep(1200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long offset = ((Date)task.getFlag()).getTime()-startTime;
		if(offset < 1100){
			print("TimerTask Exec Offset Test2 Success!"+offset);
		}else{
			print("TimerTask Exec Offset Test2 Fail!" + offset);
			fail("TimerTask Exec Offset Test2 Fail!");
		}
		ThreadPoolServer.getSingleInstance().schedule(poolName, task = new BasicTask(){
			public void run(){
				
			}
		}, 0, 200,listener = new TestEventListener(){
			public IEventResponse dispose(IEvent event) {
				if(event.getEventType() == PoolEvent.EVENT_TYPE.TASK_START){
					
				}else if(event.getEventType() == PoolEvent.EVENT_TYPE.TASK_FINISH){
					this.count();
				}
				return null;
			}
		});
		
		try{
			Thread.sleep(1100);
		}catch(Exception e){}
		
		task.cancel();
		
		try{
			Thread.sleep(600);
		}catch(Exception e){}
		
		int count = ((Integer)listener.getResult()).intValue();
		if(count >= 5 && count < 7){
			print("TimerTask Exec Count Success!");
		}else{
			print("TimerTask Exec Count Fail!" + count);
			fail("TimerTask Exec Count Fail!" + count);
		}

		ThreadPoolServer.getSingleInstance().schedule(poolName, task = new BasicTask(){
			public void run(){
				try{
					Thread.sleep(800);
				}catch(Exception e){}
			}
		}, 0, 200,listener = new TestEventListener(){
			public IEventResponse dispose(IEvent event) {
				if(this.result == null){
					this.result = new int[]{0,0};
				}
				if(event.getEventType() == PoolEvent.EVENT_TYPE.TASK_START){
					((int[])this.result)[0]++;
				}else if(event.getEventType() == PoolEvent.EVENT_TYPE.TIMER_TASK_NO_EXECUTE){
					((int[])this.result)[1]++;
				}
				return null;
			}
		});
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		int[] a = (int[])listener.result;
		if(a[0] >= 7 && a[1] >= 5){
			print("TimerTask Exec OverFlow Test Success!" + a[0]+" "+a[1]);
		}else{
			print("TimerTask Exec OverFlow Test Failure!" + a[0]+" "+a[1]);
			fail("TimerTask Exec OverFlow Test Failure!" + a[0]+" "+a[1]);
		}
		
		this.destroyTimerPool();
	}
	
	
	private void print(String message){
		System.out.println(this.getClass().getName()+":"+message);
	}
}
