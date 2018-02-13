package com.xzy.base.server.log;

import com.xzy.base.server.event.DefaultEventCenterServer;
import com.xzy.base_c.BasicEvent;

/**
 * 日记记录对象，通过此对象创建日记对象，最终调用debug、trace、info、warn、error、fatal发布不同等级的日记消息
 * @author edmund
 *
 */
public class LogRecordEvent extends BasicEvent{
	//日记对象的创建位置
	private Exception createException = new Exception();
	//日记的级别
	private LogLevel level = null;
	//日记的tag，标签
	private String tag = null;
	//日记的内容
	private String message = null;
	//异常的日记
	private Throwable e = null;
	
	public LogRecordEvent(Object source) {
		super(source,null,null);
		this.eventType = LogRecordServer.EVENT_TYPE;
	}

	public void fatal(Class tag,String message){
		this.fatal(tag.getSimpleName(), message);
	}
	public void fatal(String tag,String message){
		this.fatal(tag, message, null);
	}
	public void fatal(Class tag,Throwable e){
		this.fatal(tag.getSimpleName(), e);
	}
	public void fatal(String tag,Throwable e){
		this.fatal(tag, null, e);
	}
	public void fatal(Class tag,String message,Throwable e){
		this.fatal(tag.getSimpleName(), message, e);
	}
	public void fatal(String tag,String message,Throwable e){
		this.notifyMessage(LogLevel.fatal, tag, message, e);
	}

	public void error(Class tag,String message){
		this.error(tag.getSimpleName(), message);
	}
	public void error(String tag,String message){
		this.error(tag, message, null);
	}
	public void error(Class tag,Throwable e){
		this.error(tag.getSimpleName(), e);
	}
	public void error(String tag,Throwable e){
		this.error(tag, null, e);
	}
	public void error(Class tag,String message,Throwable e){
		this.error(tag.getSimpleName(), message, e);
	}
	public void error(String tag,String message,Throwable e){
		this.notifyMessage(LogLevel.error, tag, message, e);
	}

	public void warn(Class tag,String message){
		this.warn(tag.getSimpleName(), message);
	}
	public void warn(String tag,String message){
		this.warn(tag, message, null);
	}
	public void warn(Class tag,Throwable e){
		this.warn(tag.getSimpleName(), e);
	}
	public void warn(String tag,Throwable e){
		this.warn(tag, null, e);
	}
	public void warn(Class tag,String message,Throwable e){
		this.warn(tag.getSimpleName(), message, e);
	}
	public void warn(String tag,String message,Throwable e){
		this.notifyMessage(LogLevel.warn, tag, message, e);
	}

	public void info(Class tag,String message){
		this.info(tag.getSimpleName(), message);
	}
	public void info(String tag,String message){
		this.info(tag, message, null);
	}
	public void info(Class tag,Throwable e){
		this.info(tag.getSimpleName(), e);
	}
	public void info(String tag,Throwable e){
		this.info(tag, null, e);
	}
	public void info(Class tag,String message,Throwable e){
		this.info(tag.getSimpleName(), message, e);
	}
	public void info(String tag,String message,Throwable e){
		this.notifyMessage(LogLevel.info, tag, message, e);
	}

	public void debug(Class tag,String message){
		this.debug(tag.getSimpleName(), message);
	}
	public void debug(String tag,String message){
		this.debug(tag, message, null);
	}
	public void debug(Class tag,Throwable e){
		this.debug(tag.getSimpleName(), e);
	}
	public void debug(String tag,Throwable e){
		this.debug(tag, null, e);
	}
	public void debug(Class tag,String message,Throwable e){
		this.debug(tag.getSimpleName(), message, e);
	}
	public void debug(String tag,String message,Throwable e){
		this.notifyMessage(LogLevel.debug, tag, message, e);
	}

	public void trace(Class tag,String message){
		this.trace(tag.getSimpleName(), message);
	}
	public void trace(String tag,String message){
		this.trace(tag, message, null);
	}
	public void trace(Class tag,Throwable e){
		this.trace(tag.getSimpleName(), e);
	}
	public void trace(String tag,Throwable e){
		this.trace(tag, null, e);
	}
	public void trace(Class tag,String message,Throwable e){
		this.trace(tag.getSimpleName(), message, e);
	}
	public void trace(String tag,String message,Throwable e){
		this.notifyMessage(LogLevel.trace, tag, message, e);
	}
	
	private void notifyMessage(LogLevel level,String tag,String message,Throwable e){
		this.level = level;
		this.tag = tag;
		this.message = message;
		this.e = e;
		
		DefaultEventCenterServer.getSingleInstance().notify(this);
	}
	
	

	public Exception getCreateException() {
		return createException;
	}

	public LogLevel getLevel() {
		return level;
	}

	public String getTag() {
		return tag;
	}

	public String getMessage() {
		return message;
	}

	public Throwable getE() {
		return e;
	}



	public static enum LogLevel{
		fatal,
		error,
		warn,
		info,
		debug,
		trace
	}
}
