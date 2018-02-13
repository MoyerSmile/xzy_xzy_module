package com.xzy.base_c;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.xzy.base.Const;
import com.xzy.base_i.IServer;
import com.xzy.base_i.IThread;

public class ServerContainer extends BasicServer{
	private static ServerContainer singleInstance = new ServerContainer();
	public static ServerContainer getSingleInstance(){
		return singleInstance;
	}
	public boolean startServer(){
		return true;
	}
	
	//整个应用所有的服务管理MAP
	private HashMap<Object,WeakReference<IServer>> serverMapping = new HashMap<Object,WeakReference<IServer>>();
	//整个应用所有线程管理的LIST
	private LinkedList<WeakReference<IThread>> allThreadList = new LinkedList<WeakReference<IThread>>();
	
	
	public synchronized String[] getAllServerName(){
		String[] nameArr = new String[serverMapping.size()];
		serverMapping.keySet().toArray(nameArr);
		return nameArr;
	}
	
	/**
	 * 注册线程对象，采用弱引用方式，不影响线程对象本身的垃圾回收
	 * @param thread 线程对象
	 */
	public void registThread(IThread thread){
		if(thread == null){
			return ;
		}
		synchronized(this.allThreadList){
			if(!this.allThreadList.contains(thread)){
				this.allThreadList.add(new WeakReference(thread));
			}
		}
	}
	/**
	 * 注销线程对象的注册
	 * @param thread
	 */
	public void unregistThread(IThread thread){
		if(thread == null){
			return ;
		}
		synchronized(this.allThreadList){
			for(Iterator<WeakReference<IThread>> itr = this.allThreadList.iterator();itr.hasNext();){
				if(itr.next().get() == thread){
					itr.remove();
					break;
				}
			}
		}
	}
	/**
	 * 得到应用开启的线程总数
	 * @return 线程数量
	 */
	public int getThreadTotalNum(){
		synchronized(this.allThreadList){
			return this.allThreadList.size();
		}
	}
	
	/**
	 * 得到所有线程的打印信息
	 * @return
	 */
	public StringBuffer getThreadInfo(){
		StringBuffer buff = new StringBuffer(8*1024);
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		
		buff.append("ThreadStatInfo["+Const.getDateFormater("yyyy-MM-dd HH:mm:ss").format(new Date())+"]:\n");
		PrintStream print = null;
		synchronized(this.allThreadList){
			IThread thread;
			int index = 1;
			for(Iterator<WeakReference<IThread>> itr = this.allThreadList.iterator();itr.hasNext();){
				thread = itr.next().get();
				if(thread != null){
					print = new PrintStream(out);
					
					print.println("Thread["+thread.hashCode()+"]-" + index++ +"["+thread.getName()+"] "+Const.getDateFormater("yyyy-MM-dd HH:mm:ss").format(new Date(thread.getStartTime())));
					thread.getCreateStack().printStackTrace(print);
					print.println();
					
					buff.append(out.toString());
					out.reset();
				}
			}
		}
		return buff;
	}
	
	/**
	 * 注册服务对象
	 * @param server
	 * @return
	 */
	public synchronized boolean registServer(IServer server){
		if(server == null){
			return false;
		}
		if(this.serverMapping.containsKey(server.getServerName())){
			this.error("Repeated ServerName Regist:"+server.getServerName());
			return false;
		}
		this.serverMapping.put(server.getServerName(), new WeakReference(server));
		
		return true;
	}
	
	/**
	 * 得到对应名称的服务对象
	 * @param name 服务名称
	 * @return
	 */
	public synchronized IServer getServer(Object name){
		WeakReference<IServer> ref = this.serverMapping.get(name);
		if(ref == null){
			return null;
		}
		return ref.get();
	}
	
	/**
	 * 反注册服务对象
	 * @param server
	 * @return
	 */
	public synchronized boolean unregistServer(IServer server){
		WeakReference<IServer> ref = this.serverMapping.get(server.getServerName());
		if(ref == null){
			return false;
		}
		IServer curServer = ref.get();
		if(curServer == null){
			this.serverMapping.remove(server.getServerName());
			return true;
		}
		if(curServer == server){
			this.serverMapping.remove(server.getServerName());
			curServer.stopServer();
			return true;
		}
		return false;
	}

	/**
	 * 反注册指定名字的服务对象
	 * @param name
	 * @return
	 */
	public synchronized IServer unregistServer(Object name){
		WeakReference<IServer> ref = this.serverMapping.remove(name);
		if(ref == null){
			return null;
		}
		IServer server = ref.get();
		
		if(server == null){
			return null;
		}
		server.stopServer();
		
		return server;
	}
}
