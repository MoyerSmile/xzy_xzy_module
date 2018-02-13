package server.xzy.socket;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.xzy.base.server.pool.BasicTask;
import com.xzy.base.server.pool.ThreadPoolInfo;
import com.xzy.base.server.pool.ThreadPoolServer;

public class XZYDataSendHelper{
	private static XZYDataSendHelper singleInstance = null;
	public static XZYDataSendHelper getSingleInstance(){
		if(singleInstance == null){
			synchronized(XZYDataSendHelper.class){
				if(singleInstance == null){
					singleInstance = new XZYDataSendHelper();
				}
			}
		}
		return singleInstance;
	}
	
	private boolean isStop = false;
	private SendThread sendThread = null;
	private XZYDataSendHelper(){
		this.isStop = false;
		
		sendThread = new SendThread();
		sendThread.start();
	}
	
	private Object lock = new Object();
	private List connList = new LinkedList();;
	public void add2Help(XZYConnectInfo conn){
		synchronized(lock){
			if(connList.contains(conn)){
				return ;
			}
			connList.add(conn);
			lock.notify();
		}
	}
	
	private class SendThread extends BasicTask{
		private long loopCycle = 30;
		private XZYConnectInfo curConn;
		private List tempList = new LinkedList(),swapList;
		
		private String poolName = null;
		public void start(){
			try{
				this.poolName = "Data Send Helper["+this.hashCode()+"] Send Thread";
				ThreadPoolInfo pInfo = new ThreadPoolInfo(1);
				ThreadPoolServer.getSingleInstance().createThreadPool(poolName, pInfo);
				ThreadPoolServer.getSingleInstance().addTask2ThreadPool(this.poolName,this);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public void run(){
			while(!isStop){
				synchronized(lock){
					if(connList.isEmpty()){  //如果没有待辅助的,则等待
						try{
							lock.wait();
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					
					swapList = tempList;
					tempList = connList;
					connList = swapList;
				}
					
				
				for(Iterator itr = tempList.iterator();itr.hasNext();){
					curConn = (XZYConnectInfo)itr.next();
					itr.remove();
					
					//最后一次有效写出距今已超过了60秒.意味着最近60秒都没有数据能发送出去,则不再辅助写该连接的数据
					if(curConn.getLastWriteTimeDiff() >= 60000){ 
						
					}else{
						//依然存在剩余数据
						if(curConn.flush() && curConn.hasRemainData()){
							XZYDataSendHelper.this.add2Help(curConn);
						}
					}
				}
				
				synchronized(lock){
					if(!connList.isEmpty()){  //如果存在待写的,则等待循环周期以后再进行书写.
						try{
							lock.wait(loopCycle);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}
			
			ThreadPoolServer.getSingleInstance().removeThreadPool(this.poolName);
		}
	}
}
