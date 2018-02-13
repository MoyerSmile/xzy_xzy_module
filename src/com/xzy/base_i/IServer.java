package com.xzy.base_i;

/**
 * 基本服务类，所有的服务都可以继承此类，进行服务的启动和关闭。
 * @author edmund
 */
public interface IServer {
	/**
	 * 得到服务的服务名称
	 * @return
	 */
	public String getServerName();
	/**
	 * 设定服务的服务名称
	 * @param name 服务名称
	 */
	public void setServerName(String name);
	
	/**
	 * 添加参数，不会覆盖原来的参数，如果同key的参数存在，会形成一个值的List
	 * @param key
	 * @param val
	 */
	public void addPara(Object key,Object val);
	/**
	 * 会覆盖原来的值
	 * @param key
	 * @param val
	 */
	public void setPara(Object key,Object val);
	/**
	 * 得到当前的key对象，可能是个List
	 * @param key
	 * @return
	 */
	public Object getPara(Object key);
	
	/**
	 * 启动对应的服务
	 * @return 启动成功返回true，反之返回失败
	 */
	public boolean startServer();
	
	/**
	 * 停止对应的服务
	 */
	public void stopServer();
	
	/**
	 * 检测服务是否在运行中
	 * @return true代表运行，反之代表未运行
	 */
	public boolean isRunning();
}
