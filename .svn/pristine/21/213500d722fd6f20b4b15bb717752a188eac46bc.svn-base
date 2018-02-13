/**
 * 绝密 Created on 2008-4-8 by edmund
 */
package com.xzy.base_c;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.xzy.base.Util;
import com.xzy.base.XMLUtil;
import com.xzy.base_i.IServer;

public class XZYStart extends BasicServer{
	public static final String SERVER_CFG_PATH_KEY = "cfg_path";
	private String serverCfgPath = null;

	public boolean startServer(String serverPath){
		this.addPara(SERVER_CFG_PATH_KEY, serverPath);
		return this.startServer();
	}
	
	public boolean startServer(InputStream in){
		if(in == null){
			return false;
		}
		try{
			boolean isSuccess = this.loadAndRunServer(in);
			if(isSuccess){
				this.info("XML Stream Load Success!");
			}else{
				this.error("XML Stream Load Failure!");
			}
			return isSuccess;
		}catch(Exception e){
			e.printStackTrace();
			this.error("XML Stream Load Failure!",e);
			return false;
		}
	}

	public boolean startServer(){		
		String tempStr = this.getStringPara(SERVER_CFG_PATH_KEY);		
		if(tempStr != null && tempStr.trim().length() > 0){
			this.serverCfgPath = tempStr.trim();
		}		
		if(this.serverCfgPath == null){		
			return false;
		}
		this.isRun = true;
		String[] cfgArr = this.serverCfgPath.split(";");		
		for(int i = 0; i < cfgArr.length; i++){
			this.isRun |= this.loadAndRunServer(cfgArr[i]);
		}
		return this.isRun;
	}

	public void stopServer(){
		this.isRun = false;
	}

	private boolean loadAndRunServer(String serverPath){
		String realPath = null;
		try{
			URL url = null;
			if(serverPath.startsWith("!")){
				url = XZYStart.class.getClassLoader().getResource(serverPath.substring(1));
			}else{
				File f = new File(serverPath);
				if(!f.exists()){
					url = XZYStart.class.getClassLoader().getResource(f.getName());
				} else{
					url = f.toURI().toURL();
				}
			}
			realPath = url.getPath();
			InputStream in = url.openStream();
			this.loadAndRunServer(in);
			in.close();
		} catch(Exception e){
			e.printStackTrace();
			this.error("Config File Load Failure!" + serverPath+"\t"+realPath,e);
			return false;
		}
		this.info("Config File Load Success!  " + serverPath+"\t"+realPath);
		return true;
	}
	
	private boolean loadAndRunServer(InputStream in) throws Exception{		
		//加载配置文件
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = domfac.newDocumentBuilder();		
		Document document = builder.parse(in);
		in.close();		
		Element root = document.getDocumentElement();	
		NodeList allServerNodeList = root.getElementsByTagName("server");		
		int serverNum = allServerNodeList.getLength();		
		for(int i = 0; i < serverNum; i++){
			this.runServerByNode(allServerNodeList.item(i));
		}		
		return true;
	}

	private void runServerByNode(Node serverNode) throws Exception{		
		String depends = XMLUtil.getNodeAttr(serverNode, "depends");
		String serverName = XMLUtil.getNodeText(XMLUtil.getSingleElementByTagName(serverNode, "server_name"));	
		String className = XMLUtil.getNodeText(XMLUtil.getSingleElementByTagName(serverNode, "class_name"));		
		String createMethod = XMLUtil.getNodeText(XMLUtil.getSingleElementByTagName(serverNode, "create_method"));		
		String enableServer = XMLUtil.getNodeText(XMLUtil.getSingleElementByTagName(serverNode, "enable_server"));		
		String failureExit = XMLUtil.getNodeText(XMLUtil.getSingleElementByTagName(serverNode, "failure_system_exit"));		
		boolean isFailureExit = (failureExit != null && failureExit.equalsIgnoreCase("true")) ? true : false;
		if(enableServer != null && enableServer.equalsIgnoreCase("false")){
			this.warn("Server[" + serverName + "] Is Disabled!");
			return;
		}
		if(depends != null){			
			String[] allDepends = depends.trim().split(",");
			int num = allDepends.length;
			IServer dependServer;
			String dependServerName;
			for(int i = 0; i < num; i++){
				dependServerName = allDepends[i].trim();
				if(dependServerName.length() == 0){
					continue;
				}
				dependServer = ServerContainer.getSingleInstance().getServer(dependServerName);
				if(dependServer == null || !dependServer.isRunning()){
					this.error("Server[" + serverName + "] Start Failure"
							+ (isFailureExit ? ",System will be exit" : "")
							+ "!Depend's Server[" + dependServerName
							+ "] is not be running!");
					if(isFailureExit){
						System.exit(-1);
					} else{
						return;
					}
				}
			}
		}
		try{
			Class cls = Class.forName(className);
			IServer server = null;
			if(createMethod == null || createMethod.trim().length() == 0){
				server = (IServer) cls.newInstance();
			} else{
				if("getSingleInstance".equals(createMethod)){
					Method method = cls.getMethod(createMethod, new Class[0]);
					server = (IServer) method.invoke(null, new Object[0]);
				}else if("getSingleObj".equals(createMethod)){
					Method method = cls.getMethod(createMethod, cls.getClass());
					server = (IServer) method.invoke(null, cls);
				} 
			}
			//为服务类设定参数信息,具体的参数使用，则由服务类内部使用
			Node[] paraNodeArr = XMLUtil.getElementsByTagName(serverNode, "para");
			int paraNum = paraNodeArr.length;
			String key, value;
			for(int i = 0; i < paraNum; i++){
				key = XMLUtil.getNodeAttr(paraNodeArr[i], "key");
				value = XMLUtil.getNodeAttr(paraNodeArr[i], "value");
				if(key == null || value == null){
					continue;
				}
				server.addPara(key.trim(), value.trim());
			}
			server.setServerName(serverName);
			//启动服务
			if(!server.startServer()){
				server.stopServer();
				throw new Exception("false");
			}
			if(ServerContainer.getSingleInstance().getServer(serverName) != null){
				this.error("Start Warn:Same name Server is exist.serverName="
						+ serverName);
			}
			ServerContainer.getSingleInstance().registServer(server);
			this.info("Server[" + serverName + "] Start Success!");
		} catch(Throwable e){
			e.printStackTrace();
			if(isFailureExit){
				this.error("Server[" + serverName + "] Start Failure,System will be exit!");
				System.exit(-1);
			} else{
				this.error("Server[" + serverName + "] Start Failure!");
			}
		}
	}

	/**
	 * 开启一个线程运行，如果侦测不到狗，程序将退出。
	 * 该接口不够良好，最好的是同步检测，而不是异步检测。
	 */
	private boolean detectDog(){
		
		return true;
	}

	private static XZYStart instance = null;
	/**
	 * @param args
	 */
	public static void main(String[] args){
		instance = new XZYStart();
		instance.addPara(SERVER_CFG_PATH_KEY, "./conf/start_server.xml");

		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				instance.info("System will be shutdown normally.");
				String[] serverNameArr = ServerContainer.getSingleInstance().getAllServerName();
				String serverName;
				IServer server = null;
				for(int i=0;i<serverNameArr.length;i++){
					serverName = serverNameArr[i];
					server = (IServer) ServerContainer.getSingleInstance().getServer(serverName);

					instance.info("Server[" + serverName + "] Will be Exit!");
					if(server != null) {
						server.stopServer();
					}
				}
				Util.sleep(500);
			}
		});

		instance.startServer();
		
		instance.error("System start finish.");
	}
}
