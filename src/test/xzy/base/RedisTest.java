package test.xzy.base;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.xzy.base.GUIDCreator;
import com.xzy.base.Util;
import com.xzy.base.server.db.RedisServer;
import com.xzy.base.server.log.LogRecordServer;
import com.xzy.base_c.BasicLockServer;

public class RedisTest {
	private synchronized void init(){
		LogRecordServer.getSingleInstance().startServer();
		if(!RedisServer.getSingleInstance().isRunning()){
			RedisServer.getSingleInstance().addPara("node", "192.168.0.134:6379");
			RedisServer.getSingleInstance().startServer();
		}
	}
	private void release(){
		RedisServer.getSingleInstance().stopServer();
	}
	
	@Test
	public void saveTest(){
		this.init();
		
		String key = "test_key",val,oldVal,field;
		try{
			val = "徐新123当飞机上看";
			RedisServer.getSingleInstance().setValue(key, val);
			if(!RedisServer.getSingleInstance().getValue(key).equals(val)){
				Assert.fail("set test fail.");
			}

			RedisServer.getSingleInstance().removeKey(key);

			if(RedisServer.getSingleInstance().existKey(key)){
				Assert.fail("exist key test fail.");
			}
			
			field = "xjs";
			for(int i=0;i<10;i++){
				RedisServer.getSingleInstance().hset(key, field+i, val+i);
			}
			if(!RedisServer.getSingleInstance().hget(key,field+0).equals(val+0)){
				Assert.fail("hset test fail.");
			}
			Map<String,String> mapping = RedisServer.getSingleInstance().hgetAll(key);
			if(mapping.size() != 10){
				Assert.fail("hgetall test fail.");
			}
			if(!RedisServer.getSingleInstance().existKey(key)){
				Assert.fail("exist key test fail.");
			}
			RedisServer.getSingleInstance().expire(key, 2);
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(RedisServer.getSingleInstance().existKey(key)){
				Assert.fail("expire exist key test fail.");
			}
			
			this.print("Redis save query Test Success");
		}finally{
			
		}
	}
	
	@Test
	public void lockTest(){
		this.init();

		String guid1 = GUIDCreator.getSingleInstance().createNewGuid();
		String guid2 = GUIDCreator.getSingleInstance().createNewGuid();
		String lockName = "test_lock_Name";
		
		RedisServer.getSingleInstance().removeKey(lockName);
		
		boolean isOk = RedisServer.getSingleInstance().requestLock(guid1, lockName, 5);
		if(!isOk){
			Assert.fail("request lock test fail");
		}
		isOk = RedisServer.getSingleInstance().requestLock(guid1, lockName, 5);
		if(!isOk){
			Assert.fail("request lock test fail");
		}
		isOk = RedisServer.getSingleInstance().requestLock(guid2, lockName, 5);
		if(isOk){
			Assert.fail("request lock test fail");
		}
		isOk = RedisServer.getSingleInstance().releaseLock(guid2, lockName);
		if(isOk){
			Assert.fail("release lock test fail");
		}
		isOk = RedisServer.getSingleInstance().releaseLock(guid1, lockName);
		if(!isOk){
			Assert.fail("release lock test fail");
		}
		isOk = RedisServer.getSingleInstance().requestLock(guid2, lockName,3);
		if(!isOk){
			Assert.fail("request lock test fail");
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		isOk = RedisServer.getSingleInstance().requestLock(guid1, lockName,3);
		if(!isOk){
			Assert.fail("request lock test fail");
		}
		
		this.print("lock test success");
	}
	
	@Test
	public void serverLockTest(){
		this.init();
		BasicLockServer server1 = new BasicLockServer(){
			@Override
			public boolean realStartServer(String lockName) {
				this.addPara("start", true);
				return false;
			}

			@Override
			public void realStopServer(String lockName) {
				this.addPara("stop", true);
			}
			
		};
		BasicLockServer server2 = new BasicLockServer(){
			@Override
			public boolean realStartServer(String lockName) {
				this.addPara("start", true);
				return false;
			}

			@Override
			public void realStopServer(String lockName) {
				this.addPara("stop", true);
			}
			
		};
		BasicLockServer server3 = new BasicLockServer(){
			@Override
			public boolean realStartServer(String lockName) {
				this.addPara("start", true);
				return false;
			}

			@Override
			public void realStopServer(String lockName) {
				this.addPara("stop", true);
			}
			
		};
		

		RedisServer.getSingleInstance().removeKey("lock_xjs_lock");
		RedisServer.getSingleInstance().removeKey("lock_xjs_lock3");

		server1.addPara("lock_name", "xjs_lock");
		server1.addPara("scan_cycle","2000");
		server2.addPara("lock_name", "xjs_lock");
		server2.addPara("scan_cycle","2000");
		server3.addPara("lock_name", "xjs_lock3");
		server3.addPara("scan_cycle","2000");

		server1.startServer();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		server2.startServer();
		server3.startServer();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(server1.getPara("start") == null){
			Assert.fail("server1 lock start fail");
		}
		if(server2.getPara("start") != null){
			Assert.fail("server2 lock start fail");
		}
		if(server3.getPara("start") == null){
			Assert.fail("server3 lock start fail");
		}
		
		RedisServer.getSingleInstance().removeKey("lock_xjs_lock");
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(server1.getPara("stop") == null){
			Assert.fail("server1 lock stop fail");
		}
		if(server2.getPara("start") == null){
			Assert.fail("server2 lock start fail");
		}
		
		RedisServer.getSingleInstance().removeKey("lock_xjs_lock");

		server1.addPara("start", null);
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if(server2.getPara("stop") == null){
			Assert.fail("server2 lock stop fail");
		}

		if(server1.getPara("start") == null){
			Assert.fail("server1 lock start fail");
		}
		
		
		this.print("lock server start stop test Success");
	}
	
	@Test
	public void publishAndListenerTest(){
		LinkedList patternList = new LinkedList();
		patternList.add("*徐新*");
		TestRedisObserver observer = null;
		RedisServer.getSingleInstance().addListener(observer = new TestRedisObserver(patternList){
			public void msgArrived(String pattern, String msg, String content)
					throws Exception {
				System.out.println("Redis Listener:p="+pattern+" msg="+msg+" content="+content);
				this.result = content;
			}
		});

		Util.sleep(1000);
		String message = "徐=新测试的kjfdk2343";
		RedisServer.getSingleInstance().publish("---tet徐新(((999", message);
		
		Util.sleep(500);
		
		if(observer.result == null || !observer.result.equals(message)){
			Assert.fail("redis publish and listener fail");
		}
		this.print("redis publish and listener success");
	}

	private void print(String message){
		System.out.println(this.getClass().getName()+":"+message);
	}
}
