package com.xzy.base.server.container;

import java.util.HashMap;

import org.json.JSONObject;

import com.hp.hpl.sparta.xpath.ThisNodeTest;
import com.xzy.base.server.event.DefaultEventCenterServer;
import com.xzy.base.server.log.LogRecordEvent;
import com.xzy.base_c.InfoContainer;
import com.xzy.base_c.ServerEvent;
import com.xzy.base_i.IEventResponse;


public abstract class BasicAction implements IAction{
	/**
	 * 排除和包含某些信息，以排除所包含的信息优先
	 * 如果包含标识中不含任何信息，意味着包含所有
	 * 如果排除标识中不含任何信息，意味着不排除任何
	 */
	public static final String EXCLUDES_FLAG = "excludes";
	public static final String INCLUDES_FLAG = "includes";
	
	
	private HashMap paraInfo = new HashMap();
	
	public String getMsg(){
		return (String)this.paraInfo.get(BasicAction._MSG_FLAG);
	}
	public String getName(){
		return (String)this.paraInfo.get(BasicAction._NAME_FLAG);
	}
	public boolean isSynchronized(){
		Boolean isSync = (Boolean)this.paraInfo.get(BasicAction._SYNC_FLAG);
		if(isSync == null){
			return true;
		}
		return isSync.booleanValue();
	}
	public boolean isFilter(){
		Boolean isFilter = (Boolean)this.paraInfo.get(BasicAction._FILTER_FLAG);
		if(isFilter == null){
			return false;
		}
		return isFilter.booleanValue();
	}
	
	public void addPara(Object key,Object value){
		ServerEvent event = new ServerEvent(this,ServerEvent.SERVER_EVENT_TYPE.QUERY_VAR_PARA);
		event.addEventPara("name", value);
		IEventResponse response = DefaultEventCenterServer.getSingleInstance().query(event);
		if(response != null && response.getEventResponse() != null){
			try{
				value = response.getEventResponse().get("value");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		this.paraInfo.put(key, value);
	}
	
	public Object getPara(Object key){
		return this.paraInfo.get(key);
	}
	
	public Boolean getBooleanPara(Object key){
		if(this.paraInfo == null){
			return null;
		}
		Object obj = this.paraInfo.get(key);
		
		if(obj == null){
			return null;
		}else if(obj instanceof Boolean){
			return (Boolean)obj;
		}
		
		return new Boolean(obj.toString().equalsIgnoreCase("true"));
	}

	public String getStringPara(Object key,boolean isFilterNull){
		String para=getStringPara(key);
		if(isFilterNull)
			para=(para==null?"":para);
		return para;
	}
	
	public String getStringPara(Object key){
		if(this.paraInfo == null){
			return null;
		}
		Object obj = this.paraInfo.get(key);
		
		return obj==null?null:obj.toString();
	}
	
	public XZYActionContainerServer getActionContainer(){
		return (XZYActionContainerServer)this.getPara(IAction._ACTION_CONTAINER_FLAG);
	}

	private String includeStr = null;
	private String excludeStr = null;
	public boolean isInclude(String msg){
		if(excludeStr != null && excludeStr.indexOf(","+msg+",") >= 0){
			return false;
		}
		if(includeStr != null && includeStr.indexOf(","+msg+",") < 0){
			return false;
		}
		
		return true;
	}
	
	public void init() throws Exception{
		//初始化被排除的信息
		this.excludeStr = (String)this.getPara(EXCLUDES_FLAG);
		if(this.excludeStr != null){
			if(this.excludeStr.trim().length() == 0){
				this.excludeStr = null;
			}else{
				this.excludeStr = "," + this.excludeStr + ",";
			}
		}
		
		//初始化被包含的信息
		this.includeStr = (String)this.getPara(INCLUDES_FLAG);
		if(this.includeStr != null){
			if(this.includeStr.trim().length() == 0){
				this.includeStr = null;
			}else{
				this.includeStr = "," + this.includeStr + ",";
			}
		}
	}
	
	public void info(String content){
		this.info(content, null);
	}
	public void info(String content,Exception e){
		new LogRecordEvent(this).info(this.getClass(), content, e);
	}
	public void warn(String content){
		this.warn(content, null);
	}
	public void warn(String content,Exception e){
		new LogRecordEvent(this).warn(this.getClass(), content, e);
	}
	public void error(String content){
		this.error(content, null);
	}
	public void error(String content,Exception e){
		new LogRecordEvent(this).error(this.getClass(), content, e);
	}
}
