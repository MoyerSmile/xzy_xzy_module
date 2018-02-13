package com.xzy.base.server.db;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.xzy.base.Const;
import com.xzy.base.server.pool.BasicTask;
import com.xzy.base_c.BasicServer;

public class MongoServer extends BasicServer {
	private String ip = null;
	private int port = 0;
	
	private String defaultDB = null;
	
	private List<MongoHandle> connList = new LinkedList<MongoHandle>();
	private int minConnNum = 10;
	private int maxConnNum = 10;
	
	private static MongoServer singleInstance = new MongoServer();
	public static MongoServer getSingleInstance(){
		return singleInstance;
	}
	
	public boolean startServer(){
		if(this.isRunning()){
			return true;
		}
		this.ip = this.getStringPara("ip");
		this.port = this.getIntegerPara("port").intValue();
		
		this.defaultDB = this.getStringPara("default_db");
		
		Integer tempInt = this.getIntegerPara("min_num");
		if(tempInt != null){
			this.minConnNum = tempInt.intValue();
		}
		tempInt = this.getIntegerPara("max_num");
		if(tempInt != null){
			this.maxConnNum = tempInt.intValue();
		}
		
		this.init(this.minConnNum);
		
		this.serverDetect(this.detectTask = new BasicTask(){
			public void run(){
				MongoServer.this.info("Detect Server Execute");
				MongoServer.this.checkConn();
			}
		}, 60*1000l, 5*60*1000l, null);
		
		this.isRun = true;
		return this.isRunning();
	}
	
	private void init(int initNum){
		this.info("Create New Connection,num="+initNum);
		for(int i=0;i<initNum;i++){
			this.connList.add(new MongoHandle());
		}
	}
	
	public synchronized int getConnNum(){
		return this.connList.size();
	}
	public synchronized int getIdleConnNum(){
		int count = 0;
		MongoHandle handle = null;
		for(Iterator itr = this.connList.iterator();itr.hasNext();){
			handle = (MongoHandle)itr.next();
			if(!handle.isUsed){
				count ++;
			}
		}
		return count;
	}
	
	public void checkConn(){
		MongoHandle handle = null;
		List<MongoHandle> tempList = new LinkedList<MongoHandle>();
		synchronized(this){
			for(Iterator itr = this.connList.iterator();itr.hasNext();){
				handle = (MongoHandle)itr.next();
				if(handle.isIdleTimeout() || handle.isExpired()){
					itr.remove();
					tempList.add(handle);
					this.error("idle or timeout: "+handle.toString());
				}
			}
		}

		for(Iterator itr = tempList.iterator();itr.hasNext();){
			handle = (MongoHandle)itr.next();
			handle.client.close();
		}
	}

	public synchronized MongoHandle getMongoHandle(){
		return this.getMongoHandle(60);
	}
	
	private long lastPrintTime = 0;
	public synchronized MongoHandle getMongoHandle(long useTimeWithSeconds){
		MongoHandle handle = null;
		for(Iterator itr = this.connList.iterator();itr.hasNext();){
			handle = (MongoHandle)itr.next();
			if(!handle.isUsed){
				handle.setUsed(true);
				handle.setExpiredTime(useTimeWithSeconds<=0 ? 0:(System.currentTimeMillis()+useTimeWithSeconds*1000l));
				return new MongoHandle(handle.client);
			}
		}
		
		if(this.connList.size() < this.maxConnNum){
			this.init(1);
			return this.getMongoHandle(useTimeWithSeconds);
		}
		
		if(System.currentTimeMillis() - this.lastPrintTime > 60*1000l){
			this.lastPrintTime = System.currentTimeMillis();
			StringBuffer strBuffer = new StringBuffer(1024);
			strBuffer.append("Mongo Connect Detail:\n");
			
			int count = 1;
			for(Iterator itr = this.connList.iterator();itr.hasNext();){
				handle = (MongoHandle)itr.next();
				if(handle.isUsed){
					strBuffer.append("Conn-"+count++ +" "+handle.toString() +"\n\t");
					Const.exception2Str(handle.usedStack, strBuffer);
					strBuffer.append("\n");
				}
			}
			this.error(strBuffer.toString());
		}
		
		
		return null;
	}
	
	public synchronized void releaseMongoHandle(MongoHandle releaseHandle){
		if(releaseHandle == null){
			return ;
		}
		MongoHandle handle = null;
		for(Iterator itr = this.connList.iterator();itr.hasNext();){
			handle = (MongoHandle)itr.next();
			if(handle.client == releaseHandle.client){
				handle.setUsed(false);
				releaseHandle.client = null;
				break;
			}
		}
	}

	public class MongoHandle{
		private boolean isUsed = false;
		private MongoClient client = null;
		private long lastUsedTime = System.currentTimeMillis();
		private long expiredTime = 0;
		private Exception usedStack = null;

		private MongoHandle(){
			this.client = new MongoClient(ip,port);
		}

		private MongoHandle(MongoClient client){
			this.client = client;
		}
		
		private boolean isIdleTimeout(){
			if(this.isUsed){
				return false;
			}
			
			return System.currentTimeMillis() - this.lastUsedTime > 10*60*1000l;
		}
		
		public boolean isExpired(){
			if(this.isUsed && this.expiredTime > 0 && System.currentTimeMillis() > this.expiredTime){
				return true;
			}
			return false;
		}
		public void setExpiredTime(long expiredTime){
			this.expiredTime = expiredTime;
		}
		public void setUsed(boolean _isUsed){
			this.isUsed = _isUsed;
			this.lastUsedTime = System.currentTimeMillis();
			if(this.isUsed){
				this.usedStack = new Exception();
			}else{
				this.usedStack = null;
			}
		}
		
		public MongoDatabase getMongoDatabase(){
			return this.getMongoDatabase(defaultDB);
		}
		public MongoDatabase getMongoDatabase(String db){
			return this.client.getDatabase(db);
		}
		
		public String toString(){
			return "MongoHandle["+this.client.hashCode()+"]: isUsed="+this.isUsed+" lastUsedTime="+Const.getDateFormater("yyyy-MM-dd HH:mm:ss").format(new Date(this.lastUsedTime))+" expiredTime=" +(this.expiredTime>0?Const.getDateFormater("yyyy-MM-dd HH:mm:ss").format(new Date(this.expiredTime)):"");
		}
		
		public void finalize() throws Throwable{
			super.finalize();
			MongoServer.this.releaseMongoHandle(this);
		}
	}
	
	public class DatabaseHandle{
		private MongoHandle client;
		private MongoDatabase db;
		private DatabaseHandle(MongoHandle client,MongoDatabase db){
			this.client = client;
			this.db = db;
		}
		
		public MongoHandle getMongoHandle(){
			return this.client;
		}
		
		public MongoDatabase getDb(){
			return this.db;
		}
		
		public MongoCollection createCollection(String collectionName){
			MongoCollection collection = db.getCollection(collectionName);
			if(collection == null){
				db.createCollection(collectionName);
				collection = db.getCollection(collectionName);
			}
			return collection;
		}
	}
}
