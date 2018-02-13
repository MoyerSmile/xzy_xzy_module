package com.xzy.base.server.db;

import java.util.List;

public interface IRedisObserver {
	public List getPatternList();
	public boolean isObserve(String pattern);
	public void msgArrived(String pattern,String msg,String content) throws Exception;
}
