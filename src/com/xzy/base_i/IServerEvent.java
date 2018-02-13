package com.xzy.base_i;

/**
 * 服务模块之间的信息交互，避免模块间的耦合，包括分布式应用
 * @author edmund
 *
 */
public interface IServerEvent extends IEvent{
	/**
	 * 消息发布服务器
	 */
	public IServer getSourceServer();
}
