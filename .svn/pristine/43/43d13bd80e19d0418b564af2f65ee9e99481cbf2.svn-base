package server.xzy.track;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bson.Document;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.xzy.base.Const;
import com.xzy.base.server.db.MongoServer;
import com.xzy.base.server.db.MongoServer.MongoHandle;
import com.xzy.base.server.pool.BasicTask;
import com.xzy.base.server.pool.ThreadPoolInfo;
import com.xzy.base.server.pool.ThreadPoolServer;
import com.xzy.base_c.BasicServer;
import com.xzy.base_c.InfoContainer;
import com.xzy.base_c.ServerContainer;

public class TrackServer extends BasicServer {
	public static final Object COLLECTION_POST_FLAG = new Object();
	public static final Object Q_START_TIME_FLAG = new Object();
	public static final Object Q_END_TIME_FLAG = new Object();

	public static final String DEST_FLAG = "dest";
	public static final String LO_FLAG = "lo";
	public static final String LA_FLAG = "la";
	public static final String TIME_FLAG = "time";
	public static final String SPEED_FLAG = "speed";
	public static final String STATUS_FLAG = "status";
	public static final String ALARM_STATUS_FLAG = "alarm_status";
	public static final String KILO_FLAG = "kilo";
	public static final String OIL_FLAG = "oil";
	public static final String DIR_FLAG = "dir";
	public static final String RECORD_FLAG = "record_time";
	
	
	private MongoServer mongoServer = null;
	private String dbName = null;
	private String threadPoolName = null;
	private int saveThreadNum = 2;
	
	
	private static TrackServer singleInstance = new TrackServer();
	public static TrackServer getSingleInstance(){
		return singleInstance;
	}
	
	@Override
	public boolean startServer() {
		this.dbName = this.getStringPara("db_name");
		Object obj = this.getPara("mongo_server");
		if(obj instanceof MongoServer){
			this.mongoServer = (MongoServer)obj;
		}else if(obj != null){
			this.mongoServer = (MongoServer)ServerContainer.getSingleInstance().getServer(obj.toString());
		}
		if(this.mongoServer == null){
			return false;
		}

		if (this.getIntegerPara("save_thread_num") != null) {
			this.saveThreadNum = this.getIntegerPara("save_thread_num").intValue();
		}
		
		this.threadPoolName = this.getServerName()+"-TrackSaver["+this.hashCode()+"]";
		ThreadPoolInfo poolInfo = new ThreadPoolInfo(this.saveThreadNum,5000);
		this.isRun = ThreadPoolServer.getSingleInstance().createThreadPool(this.threadPoolName, poolInfo);
		
		return this.isRunning();
	}
	
	public void stopServer(){
		super.stopServer();
		ThreadPoolServer.getSingleInstance().removeThreadPool(this.threadPoolName);
	}

	public boolean addTrack(InfoContainer trackInfo){
		if(!this.isRunning()){
			return false;
		}
		return ThreadPoolServer.getSingleInstance().addTask2ThreadPool(this.threadPoolName, new TrackTask(trackInfo));
	}
	
	
	public List<InfoContainer> queryTrackInfo(InfoContainer queryInfo) {
		return this.queryTrackInfo(queryInfo, null);
	}
	public List<InfoContainer> queryTrackInfo(InfoContainer queryInfo,TrackFilter filter) {
		if(!this.isRunning()){
			return null;
		}

		Date sTime = queryInfo.getDate(Q_START_TIME_FLAG);
		Date eTime = queryInfo.getDate(Q_END_TIME_FLAG);
		String dest = queryInfo.getString(DEST_FLAG);
		String post = queryInfo.getString(COLLECTION_POST_FLAG);

		List<InfoContainer> resultList = new LinkedList<InfoContainer>();
		MongoHandle handle = null;
		MongoDatabase db = null;
		MongoCollection collection = null;
		try{
			handle = mongoServer.getMongoHandle();
			if(handle == null){
				return null;
			}
			if(dbName == null){
				db = handle.getMongoDatabase();
			}else{
				db = handle.getMongoDatabase(dbName);
			}
			if(db == null){
				return null;
			}

			Calendar cal = Calendar.getInstance();
			cal.setTime(sTime);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			
			InfoContainer trackInfo;
			Iterator<String> fieldItr;
			Document doc;
			String key;
			Date tempStartTime = sTime,tempEndTime = cal.getTime();
			if(tempEndTime.after(eTime)){
				tempEndTime = eTime;
			}
			while(tempStartTime.before(eTime)){
				collection = db.getCollection(getCollectionName(tempStartTime,post));
				if(collection == null){
					
				}else{
					FindIterable able = collection.find(new BasicDBObject(DEST_FLAG,dest)
						.append("time", new BasicDBObject("$gt",tempStartTime).append("$lt",tempEndTime)))
						.sort(new BasicDBObject("time",1));
					able.batchSize(1000);
					MongoCursor<Document> itr = able.iterator();
					while(itr.hasNext()){
						doc = itr.next();
						trackInfo = new InfoContainer();
						fieldItr = doc.keySet().iterator();
						while(fieldItr.hasNext()){
							key = fieldItr.next();
							
							if(key.equals("loc")){
								ArrayList loc = (ArrayList)((Document)doc.get(key)).get("coordinates");
								trackInfo.setInfo(LO_FLAG, (Double)loc.get(0));
								trackInfo.setInfo(LA_FLAG, (Double)loc.get(1));
							}else{
								trackInfo.setInfo(key,doc.get(key));
							}
						}

						if(filter != null){
							int result = filter.filterTrack(trackInfo);
							if(result == TrackFilter.CONTINUE_FLAG){
								resultList.add(trackInfo);
							}else if(result == TrackFilter.BREAK_FLAG){
								resultList.add(trackInfo);
								return resultList;
							}
						}else{
							resultList.add(trackInfo);
						}
					}
				}
				
				tempStartTime = cal.getTime();
				cal.add(Calendar.DAY_OF_MONTH, 1);
				tempEndTime = cal.getTime();
				if(tempEndTime.after(eTime)){
					tempEndTime = eTime;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			this.mongoServer.releaseMongoHandle(handle);
		}
		return resultList;
	}
	
	

	private String collectionNamePrefix = "X_TRK_";
	private String getCollectionName(Date date,String post){
		if(post == null || post.length() == 0){
			post = "";
		}else if(!post.startsWith("_")){
			post = "_"+post;
		}
		String curCollectionName = this.collectionNamePrefix+Const.getDateFormater("yyyyMMdd").format(date) + post;
		return curCollectionName;
	}
	
	private class TrackTask extends BasicTask{
		private InfoContainer trackInfo = null;
		public TrackTask(InfoContainer info){
			this.trackInfo = info;
		}
		
		public String getName(){
			return "TrackRecord";
		}
		
		@Override
		public void run() {
			if(!isRunning()){
				return ;
			}
			
			Document doc = new Document();

			doc.put(DEST_FLAG, trackInfo.getInfo(DEST_FLAG));

			BasicDBList coordinates = new BasicDBList();
			coordinates.put(0, trackInfo.getDouble(LO_FLAG));
			coordinates.put(1, trackInfo.getDouble(LA_FLAG));
			doc.put("loc", new BasicDBObject("type","Point").append("coordinates", coordinates));
			
			doc.put(TIME_FLAG, trackInfo.getDate(TIME_FLAG));
			doc.put(SPEED_FLAG, trackInfo.getDouble(SPEED_FLAG));
			doc.put(STATUS_FLAG, trackInfo.getInteger(STATUS_FLAG));
			doc.put(ALARM_STATUS_FLAG, trackInfo.getInteger(ALARM_STATUS_FLAG));
			if(trackInfo.getInfo(KILO_FLAG) != null){
				doc.put(KILO_FLAG, trackInfo.getDouble(KILO_FLAG));
			}
			if(trackInfo.getInfo(OIL_FLAG) != null){
				doc.put(OIL_FLAG, trackInfo.getDouble(OIL_FLAG));
			}
			doc.put(DIR_FLAG, trackInfo.getInteger(DIR_FLAG));
			doc.put(RECORD_FLAG, new Date());
			
		
			String curCollectionName = getCollectionName((Date)doc.get(TIME_FLAG),this.trackInfo.getString(COLLECTION_POST_FLAG));
			
			MongoHandle handle = null;
			MongoDatabase db = null;
			MongoCollection collection = null;
			
			try{
				handle = mongoServer.getMongoHandle();
				if(dbName == null){
					db = handle.getMongoDatabase();
				}else{
					db = handle.getMongoDatabase(dbName);
				}
				if(db == null){
					TrackServer.this.error("TrackServer can't find db. "+dbName);
					return ;
				}
				
				collection = db.getCollection(curCollectionName);
				if(collection == null || collection.count() == 0){
					if(collection == null){
						db.createCollection(curCollectionName);
					}
					collection = db.getCollection(curCollectionName);
					collection.createIndex(new BasicDBObject("loc", "2dsphere"));
					collection.createIndex(new BasicDBObject(DEST_FLAG, 1));
					collection.createIndex(new BasicDBObject(TIME_FLAG, 1));
				}
				
				if(collection != null){
					collection.insertOne(doc);
				}
			}catch(Exception e){
				e.printStackTrace();
				TrackServer.this.error("TrackServer Insert Error.", e);
				return ;
			}finally{
				mongoServer.releaseMongoHandle(handle);
			}
		}
		
	}
}
