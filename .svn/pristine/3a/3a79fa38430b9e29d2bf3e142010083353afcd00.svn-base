package com.xzy.base.server.log;

import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;

import com.xzy.base.server.event.DefaultEventCenterServer;
import com.xzy.base.server.pool.BasicTask;
import com.xzy.base.server.pool.ThreadPoolInfo;
import com.xzy.base.server.pool.ThreadPoolServer;
import com.xzy.base_c.BasicServer;
import com.xzy.base_i.IEvent;
import com.xzy.base_i.IEventResponse;

public class LogRecordServer extends BasicServer{
	public static final String EVENT_TYPE = "_app_log_event";
	private String threadPoolName = "";
	
	private static LogRecordServer singleInstance = new LogRecordServer();
	public static LogRecordServer getSingleInstance(){
		return singleInstance;
	}
	
	private LogRecordServer(){
		
	}

	@Override
	public boolean startServer() {
		if(this.isRun){
			return true;
		}

		FileInputStream in = null;
		try{
			in = new FileInputStream("./conf/log4j.properties");
			PropertyConfigurator.configure(in);
		}catch(Throwable e){
			e.printStackTrace();
			System.out.println("Exception Start LogRecord Server");
			return false;
		}finally{
			try{
				if(in != null){
					in.close();
				}
			}catch(Exception e){}
		}
		
		
		this.threadPoolName = "_log_record_msg["+this.hashCode()+"]";
		
		this.isRun = ThreadPoolServer.getSingleInstance().createThreadPool(threadPoolName, new ThreadPoolInfo(1,1000));
		if(this.isRun){
			DefaultEventCenterServer.getSingleInstance().registEventListener(EVENT_TYPE, this);
		}
		return this.isRunning();
	}

	@Override
	public void stopServer() {
		super.stopServer();
		DefaultEventCenterServer.getSingleInstance().unregistEventListener(this);
		ThreadPoolServer.getSingleInstance().removeThreadPool(this.threadPoolName);
	}

	public boolean hasInteresting(IEvent event){
		if(event == null || event.getEventType() == null){
			return false;
		}
		return event.getEventType().equals(EVENT_TYPE);
	}
	
	public IEventResponse dispose(IEvent event){
		if(!this.isRunning()){
			return null;
		}
		
		if(event == null){
			return null;
		}
		
		LogRecordEvent info = (LogRecordEvent)event;

		if(!ThreadPoolServer.getSingleInstance().addTask2ThreadPool(this.threadPoolName,new LogTask(info))){
			System.out.println("log list overflow:"+ThreadPoolServer.getSingleInstance().getThreadPoolWaitTaskNum(this.threadPoolName));
		}
		
		return null;
	}
	
	private class LogTask extends BasicTask{
		private LogRecordEvent logInfo = null;
		public LogTask(LogRecordEvent logInfo){
			this.logInfo = logInfo;
		}
		public void run(){
			LogRecordEvent.LogLevel level = logInfo.getLevel();
			
			if(level == LogRecordEvent.LogLevel.fatal){
				Logger.getLogger(logInfo.getTag()).fatal(this.logInfo.getMessage(), this.logInfo.getE());
			}else if(level == LogRecordEvent.LogLevel.error){
				Logger.getLogger(logInfo.getTag()).error(this.logInfo.getMessage(), this.logInfo.getE());
			}else if(level == LogRecordEvent.LogLevel.warn){
				Logger.getLogger(logInfo.getTag()).warn(this.logInfo.getMessage(), this.logInfo.getE());
			}else if(level == LogRecordEvent.LogLevel.info){
				Logger.getLogger(logInfo.getTag()).info(this.logInfo.getMessage(), this.logInfo.getE());
			}else if(level == LogRecordEvent.LogLevel.debug){
				Logger.getLogger(logInfo.getTag()).debug(this.logInfo.getMessage(), this.logInfo.getE());
			}else if(level == LogRecordEvent.LogLevel.trace){
				Logger.getLogger(logInfo.getTag()).trace(this.logInfo.getMessage(), this.logInfo.getE());
			}
		}
	}
}
