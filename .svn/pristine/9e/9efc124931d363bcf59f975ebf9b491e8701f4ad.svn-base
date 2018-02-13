package com.xzy.base_c;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.xzy.base.GUIDCreator;
import com.xzy.base.server.db.RedisServer;
import com.xzy.base.server.pool.BasicTask;
import com.xzy.base.server.pool.ITask;
import com.xzy.base.server.pool.ThreadPoolServer;


public abstract class BasicLockServer extends BasicServer {
	public static final String LOCK_NAME_FLAG = "lock_name";
	public static final String LOCK_SCAN_SERVER = "lock_scanner";
	
	private RedisServer redisServer = null;
	private List lockInfoList = new LinkedList();
	private ITask lockScanTask = null;
	private long scanCycle = 10*1000;
	
	private String guid = null;
	
	@Override
	public boolean startServer() {
		if(this.isRunning()){
			return false;
		}
		
		String tempStr = this.getStringPara(LOCK_NAME_FLAG);
		if(tempStr == null || tempStr.trim().length() == 0){
			return false;
		}
		String[] arr = tempStr.trim().split(";");
		for(int i=0;i<arr.length;i++){
			if(arr[i].trim().length() > 0){
				lockInfoList.add(new LockInfo(arr[i].trim()));
			}
		}
		if(lockInfoList.size() == 0){
			return false;
		}
		
		this.guid = GUIDCreator.getSingleInstance().createNewGuid();
		
		tempStr = this.getStringPara("scan_cycle");
		if(tempStr != null && tempStr.trim().length() > 0){
			scanCycle = Long.parseLong(tempStr.trim());
		}
		

		this.redisServer = (RedisServer)ServerContainer.getSingleInstance().getServer(this.getStringPara("redis_cluster_name"));
		if(this.redisServer == null){
			this.redisServer = RedisServer.getSingleInstance();
		}
		
		if(!ThreadPoolServer.getSingleInstance().createTimerPool(LOCK_SCAN_SERVER)){
			return false;
		}

		ThreadPoolServer.getSingleInstance().schedule(LOCK_SCAN_SERVER, this.lockScanTask = new BasicTask(){
			public void run(){
				LockInfo lockInfo;
				for(Iterator itr = lockInfoList.iterator();itr.hasNext();){
					lockInfo = (LockInfo)itr.next();
					requestLock(lockInfo);
				}
			}
		}, 0, this.scanCycle, null);

		this.isRun = true;
		return this.isRunning();
	}
	
	
	public void stopServer(){
		if(this.lockScanTask != null){
			this.lockScanTask.cancel();
		}
		this.lockScanTask = null;
		super.stopServer();
	}
	
	private void requestLock(LockInfo lockInfo){
		if(lockInfo.hasLock()){
			if(!lockInfo.activeLock()){
				this.warn("RealStopServer:lockName="+lockInfo.lockName);
				this.realStopServer(lockInfo.lockName);
				lockInfo.autoReleaseLock(3*this.scanCycle);
			}
		}else{
			if(lockInfo.needLock() && lockInfo.requestLock()){
				this.info("RealStartServer:lockName="+lockInfo.lockName);
				this.realStartServer(lockInfo.lockName);
			}
		}
	}
	
	private LockInfo getLockInfo(String lockName){
		if(lockName == null){
			return null;
		}
		LockInfo lockInfo;
		for(Iterator itr = this.lockInfoList.iterator();itr.hasNext();){
			lockInfo = (LockInfo)itr.next();
			if(lockInfo.lockName.equals(lockName)){
				return lockInfo;
			}
		}
		return null;
	}
	public void altuReleaseAllServer(){
		LockInfo lockInfo;
		for(Iterator itr = this.lockInfoList.iterator();itr.hasNext();){
			lockInfo = (LockInfo)itr.next();
			autoReleaseServer(lockInfo);
		}
	}
	public void autoReleaseServer(String lockName){
		LockInfo lockInfo = this.getLockInfo(lockName);
		if(lockInfo == null){
			return ;
		}
		this.realStopServer(lockName);
		this.autoReleaseServer(lockInfo);
	}
	private void autoReleaseServer(LockInfo lockInfo){
		this.realStopServer(lockInfo.lockName);
		lockInfo.autoReleaseLock(3*this.scanCycle);
	}

	public abstract boolean realStartServer(String lockName);
	public abstract void realStopServer(String lockName);

	private class LockInfo{
		public String lockName = null;
		public boolean isLock = false;
		public long allowCaptureLockTime = 0;
		
		public LockInfo(String lockName){
			this.lockName = lockName;
		}
		
		public synchronized boolean needLock(){
			return System.currentTimeMillis() > this.allowCaptureLockTime;
		}
		public synchronized boolean hasLock(){
			return this.isLock;
		}
		public synchronized boolean requestLock(){
			this.isLock = redisServer.requestLock(guid, this.lockName, 60);
			return this.isLock;
		}
		public synchronized boolean activeLock(){
			return redisServer.activeLock(guid, this.lockName);
		}
		public synchronized void releaseLock(){
			redisServer.releaseLock(guid, this.lockName);
		}
		public synchronized void autoReleaseLock(long delayTime){
			this.allowCaptureLockTime = System.currentTimeMillis()+delayTime;
			this.releaseLock();
			this.isLock = false;
		}
	}
}
