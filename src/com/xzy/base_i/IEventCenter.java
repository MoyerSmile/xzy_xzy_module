package com.xzy.base_i;

/**
 * 服务间数据通信的模块
 * @author edmund
 *
 */
public interface IEventCenter {
	/**
	 * 发布事件
	 * @param event 事件消息
	 */
	public boolean notify(IEvent event);
	
	/**
	 * 通过事件进行信息查询
	 * @param event 事件参数
	 * @return 事件响应，null代表无响应
	 */
	public IEventResponse query(IEvent event);

	/**
	 * 注册在所有的消息事件上，都将调用监听的hasInteresting函数以决策是否调用dispose函数
	 * @param listener 监听对象
	 */
	public void registEventListener(IEventListener listener);
	
	/**
	 * 监听在具体的消息事件上，也将调用hasInteresting函数以决策是否调用dispose函数
	 * @param eventType 事件类型
	 * @param listener  监听对象
	 */
	public void registEventListener(Object eventType,IEventListener listener);

	/**
	 * 反注册某个监听对象
	 * @param listener 监听对象
	 */
	public void unregistEventListener(IEventListener listener);
	
	public void unregistEventListener(Object eventType,IEventListener listener);
}
