package com.xzy.base.server.pool;

import com.xzy.base.server.log.LogRecordEvent;

public abstract class BasicTask implements ITask {
	protected String taskName = null;
	protected Object flag = null;
	protected boolean isCancelled = false;

	@Override
	public Object getFlag() {
		return this.flag;
	}

	@Override
	public String getName() {
		return this.taskName;
	}

	public void cancel(){
		this.isCancelled = true;
	}
	
	@Override
	public boolean isCancel() {
		return isCancelled;
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
}
