package com.xzy.base_i;

import org.json.JSONObject;

/**
 * 
 * @author edmund
 *
 */
public interface IEventResponse {
	/**
	 * 得到对应的IEvent的事件类型
	 * @return 事件类型
	 */
	public Object getEventType();
	
	/**
	 * 得到事件响应的返回信息值
	 * @return 事件响应信息值
	 */
	public JSONObject getEventResponse();
	
	/**
	 * 事件的响应目标
	 * @return 事件响应方
	 */
	public Object getResponseDest();
	
	/**
	 * 得到事件的发布源，对应IEvent中的source
	 * @return 事件源
	 */
	public Object getSource();
}
