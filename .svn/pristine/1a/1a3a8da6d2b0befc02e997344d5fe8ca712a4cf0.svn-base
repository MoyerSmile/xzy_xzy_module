package test.xzy.base;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.bson.Document;
import org.junit.Test;

import server.xzy.track.TrackServer;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.xzy.base.Util;
import com.xzy.base.server.db.MongoServer;
import com.xzy.base.server.db.MongoServer.MongoHandle;
import com.xzy.base.server.log.LogRecordServer;
import com.xzy.base_c.InfoContainer;

public class MongoTest extends BasicTest{

	private void init(){
		LogRecordServer.getSingleInstance().startServer();
		if(MongoServer.getSingleInstance().isRunning()){
			return ;
		}
		MongoServer.getSingleInstance().addPara("ip", "192.168.0.39");
		MongoServer.getSingleInstance().addPara("port", "27017");
		MongoServer.getSingleInstance().addPara("min_num", "5");
		MongoServer.getSingleInstance().addPara("default_db", "xjs_test");
		
		
		MongoServer.getSingleInstance().startServer();
	}
	
	private void destroy(){
		
	}

	@Test
	public void mongoTest(){
		this.init();
		
		MongoHandle handle = MongoServer.getSingleInstance().getMongoHandle();
		MongoDatabase db = handle.getMongoDatabase("xjs");
		db.getCollection("xjs").drop();
		
		MongoCollection collection = db.getCollection("xjs");
		Document doc = new Document();

		doc.put("f1", "v1");
		doc.put("f2", "v2");
		doc.put("f3", "v3");
		doc.put("f4", "v4");
		
		collection.insertOne(doc);
		
		boolean isOk = true;
		int count = 0;
		MongoCursor cursor = collection.find().iterator();
		while(cursor.hasNext()){
			count ++;
			doc = (Document)cursor.next();
			
			if(!doc.getString("f1").equals("v1")){
				isOk = false;
			}
			if(!doc.getString("f2").equals("v2")){
				isOk = false;
			}
			if(!doc.getString("f3").equals("v3")){
				isOk = false;
			}
			if(!doc.getString("f4").equals("v4")){
				isOk = false;
			}
		}
		
		MongoServer.getSingleInstance().releaseMongoHandle(handle);
		
		if(count != 1){
			isOk = false;
		}
		
		if(!isOk){
			this.print("MongoDb Connect Test Failure");
			Assert.fail("MongoDb Connect Test Failure");
		}else{
			this.print("MongoDb Connect Test Success");
		}
	}
	@Test
	public void testConnUsed(){
		this.init();
		
		MongoHandle[] arr = new MongoHandle[12];
		for(int i=0;i<arr.length;i++){
			arr[i] = MongoServer.getSingleInstance().getMongoHandle();
			if(i < 10){
				if(MongoServer.getSingleInstance().getIdleConnNum() != MongoServer.getSingleInstance().getConnNum()-(i+1)){
					this.print("MongoDb Connect Num Test Failure.total=" + MongoServer.getSingleInstance().getConnNum()+" used="+(i+1) +" idle="+MongoServer.getSingleInstance().getIdleConnNum());
					Assert.fail("MongoDb Connect Num Test Failure");
				}
			}
		}
		this.print("MongoDb Connect Num Test Success");
		
		for(int i=0;i<arr.length;i++){
			arr[i] = null;
		}
		
		System.gc();
		
		Util.sleep(1000);
		
		if(MongoServer.getSingleInstance().getIdleConnNum() != MongoServer.getSingleInstance().getConnNum()){
			this.print("MongoDb Connect GC Release Num Test Failure.total=" + MongoServer.getSingleInstance().getConnNum()+" idle="+MongoServer.getSingleInstance().getIdleConnNum());
			Assert.fail("MongoDb Connect GC Release Num Test Failure");
		}
		this.print("MongoDb Connect GC Release Num Test Success");
	}
	
	@Test
	public void trackTest(){
		Util.sleep(5000);
		this.init();
		
		TrackServer.getSingleInstance().addPara("mongo_server", MongoServer.getSingleInstance());
		TrackServer.getSingleInstance().startServer();
		
		InfoContainer trackInfo;
		for(int i=0;i<100;i++){
			trackInfo = new InfoContainer();
			trackInfo.setInfo(TrackServer.DEST_FLAG, "Dest_"+i);
			trackInfo.setInfo(TrackServer.LO_FLAG, new Double(121+Math.random()));
			trackInfo.setInfo(TrackServer.LA_FLAG, new Double(31+Math.random()));
			trackInfo.setInfo(TrackServer.TIME_FLAG, new Date());
			trackInfo.setInfo(TrackServer.SPEED_FLAG, i);
			TrackServer.getSingleInstance().addTrack(trackInfo);
		}
		
		Util.sleep(1000);
		
		InfoContainer queryInfo = new InfoContainer();
		queryInfo.setInfo(TrackServer.DEST_FLAG, "Dest_40");
		queryInfo.setInfo(TrackServer.Q_START_TIME_FLAG, new Date(System.currentTimeMillis()-5000));
		queryInfo.setInfo(TrackServer.Q_END_TIME_FLAG, new Date());
		List<InfoContainer> list = TrackServer.getSingleInstance().queryTrackInfo(queryInfo);
		if(list.size() != 1){
			this.print("Query Result Error");
			Assert.fail("Query Result Error");
		}
		queryInfo = list.get(0);
		if(Math.round(queryInfo.getDouble(TrackServer.SPEED_FLAG)) != 40){
			this.print("Query Result Speed Error,should 40."+queryInfo.getInteger(TrackServer.SPEED_FLAG));
			Assert.fail("Query Result Speed Error");
		}
		if(queryInfo.getDouble(TrackServer.LO_FLAG)-121 >= 1 || queryInfo.getDouble(TrackServer.LA_FLAG)-31 >= 1){
			this.print("Query Result lo,la Error,should be near 121,31."+queryInfo.getDouble(TrackServer.LO_FLAG)+","+queryInfo.getDouble(TrackServer.LA_FLAG));
			Assert.fail("Query Result lo,la Error");
		}
		this.print("Track Server Test Success");
	}

}
