package com.xzy.base_i;


public interface IEventListener {
	/**
	 * 是否对某个事件关注，如果返回true，dispose函数将被调用
	 * @param event 事件消息
	 * @return true感兴趣，反之不感兴趣
	 */
	public boolean hasInteresting(IEvent event);
	
	/**
	 * 对事件消息的处理，如有返回值，通过json返回，格式依据消息自行定义
	 * @param event 事件对象
	 * @return      处理结果
	 */
	public IEventResponse dispose(IEvent event);
}
