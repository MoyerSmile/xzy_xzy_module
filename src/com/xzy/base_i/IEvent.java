package com.xzy.base_i;

import org.json.JSONObject;

/**
 * 事件对象，用于传递系统消息
 * @author edmund
 *
 */
public interface IEvent {
	/**
	 * 得到事件发布对象
	 * @return 时间发布对象
	 */
	public Object getSource();
	
	/**
	 * 得到事件类型消息
	 * @return 事件类型对象
	 */
	public Object getEventType();
	
	/**
	 * 得到事件参数
	 * @return 事件参数的JSON对象，依赖事件类型
	 */
	public JSONObject getEventPara();
}
