package com.xzy.base_c;

import java.util.ArrayList;
import java.util.HashMap;


import com.xzy.base.server.event.DefaultEventCenterServer;
import com.xzy.base.server.log.LogRecordEvent;
import com.xzy.base.server.pool.ITask;
import com.xzy.base.server.pool.ThreadPoolServer;
import com.xzy.base_i.IEvent;
import com.xzy.base_i.IEventListener;
import com.xzy.base_i.IEventResponse;
import com.xzy.base_i.IServer;

public abstract class BasicServer implements IServer,IEventListener {
	//启动本服务的服务方，放置的key名称
	public static final Object CALL_SERVER_FLAG = new Object();
	public static final String SERVER_DETECT_TIMER_FLAG = "server_detect_timer";
	private String serverName = null;
	protected boolean isRun = false;
	private HashMap paraMapping = null;
	//服务自检任务对象
	protected ITask detectTask = null;
	
	public String getServerName() {
		return this.serverName;
	}

	public void setServerName(String name) {
		this.serverName = name;
	}
	
	protected synchronized HashMap getParaMapping(){
		if(this.paraMapping == null){
			this.paraMapping = new HashMap();
		}
		return this.paraMapping;
	}

	public void addPara(Object key, Object val) {
		ServerEvent event = new ServerEvent(this,ServerEvent.SERVER_EVENT_TYPE.QUERY_VAR_PARA);
		event.addEventPara("name", val);
		IEventResponse response = DefaultEventCenterServer.getSingleInstance().query(event);
		if(response != null && response.getEventResponse() != null){
			try{
				val = response.getEventResponse().get("value");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		Object obj = this.getParaMapping().get(key);
		if(obj == null){
			this.setPara(key, val);
		}else if(obj instanceof ParaList){
			((ParaList)obj).add(val);
		}else{
			ParaList list = new ParaList();
			list.add(obj);
			list.add(val);
			this.setPara(key, list);
		}
	}

	public void setPara(Object key, Object val) {
		this.getParaMapping().put(key, val);
	}

	public Object getPara(Object key) {
		return this.getParaMapping().get(key);
	}
	public String getStringPara(Object key){
		Object obj = this.getPara(key);
		if(obj == null){
			return null;
		}
		if(obj instanceof String){
			return (String)obj;
		}
		return obj.toString();
	}
	
	public Integer getIntegerPara(Object key){
		Object obj = this.getParaMapping().get(key);
		if(obj instanceof Integer){
			return (Integer)obj;
		}
		try{
			return new Integer(obj.toString());
		}catch(Exception e){
			return null;
		}
	}
	
	public boolean isMultiplePara(Object key){
		Object obj = this.getPara(key);
		if(obj != null && (obj instanceof ParaList)){
			return true;
		}
		return false;
	}
	
	public boolean isRunning(){
		return this.isRun;
	}
	
	public boolean hasInteresting(IEvent event){
		return false;
	}
	public IEventResponse dispose(IEvent event){
		return null;
	}
	
	public void stopServer(){
		this.isRun = false;
		ServerContainer.getSingleInstance().unregistServer(this);
		if(this.detectTask != null){
			this.detectTask.cancel();
			this.detectTask = null;
		}
		
		DefaultEventCenterServer.getSingleInstance().notify(new BasicEvent(this,ServerEvent.SERVER_EVENT_TYPE.SERVER_STOP));
	}
	
	public void serverDetect(ITask detectTask,long delay,long cycle,IEventListener listener){
		if(this.detectTask != null){
			this.detectTask.cancel();
		}
		this.detectTask = detectTask;
		if(!ThreadPoolServer.getSingleInstance().isExistTimerPool(SERVER_DETECT_TIMER_FLAG)){
			ThreadPoolServer.getSingleInstance().createTimerPool(SERVER_DETECT_TIMER_FLAG);
		}
		ThreadPoolServer.getSingleInstance().schedule(SERVER_DETECT_TIMER_FLAG, detectTask, delay, cycle, listener);
	}

	public void info(String content){
		this.info(content, null);
	}
	public void info(String content,Throwable e){
		new LogRecordEvent(this).info(this.getClass(), content, e);
	}
	public void warn(String content){
		this.warn(content, null);
	}
	public void warn(String content,Throwable e){
		new LogRecordEvent(this).warn(this.getClass(), content, e);
	}
	public void error(String content){
		this.error(content, null);
	}
	public void error(String content,Throwable e){
		new LogRecordEvent(this).error(this.getClass(), content, e);
	}
	
	private class ParaList extends ArrayList {

	}
}
