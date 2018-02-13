package com.xzy.base.server.pool;

import com.xzy.base_i.IEventListener;

public class TimerPoolInfo {
	private int timerNum = 1;
	private boolean _isDaemo = true;
	//Timer³ØÊÂ¼þ¼àÌý
	private IEventListener listener = null;
	
	public TimerPoolInfo(){
		this(1,true);
	}
	public TimerPoolInfo(int timerNum){
		this(timerNum,true);
	}
	public TimerPoolInfo(int timerNum,boolean isDaemo){
		this.timerNum = timerNum;
		this._isDaemo = isDaemo;
	}
	
	public int getTimerNum(){
		return this.timerNum;
	}
	
	public boolean isDaemo(){
		return this._isDaemo;
	}

	public void setListener(IEventListener listener) {
		this.listener = listener;
	}
	public IEventListener getListener(){
		return this.listener;
	}
}
