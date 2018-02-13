package com.xzy.base.server.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.xzy.base.Const;
import com.xzy.base.XMLUtil;
import com.xzy.base.server.pool.BasicTask;
import com.xzy.base.server.pool.TaskCostStatServer;
import com.xzy.base.server.pool.ThreadPoolInfo;
import com.xzy.base.server.pool.ThreadPoolServer;
import com.xzy.base_c.BasicServer;
import com.xzy.base_c.InfoContainer;

public class XZYActionContainerServer extends BasicServer{	
	public static Object OPTIONAL_PRINT_INFO_FLAG = new Object();
	private String ALL_MSG_FLAG = "all_msg";
	private String MSG_NODE_NAME = "msg";
	private String DEFAULT_ACTION_FLAG = "default_action";
	
	public static final String ASYNC_THREAD_NUM_FLAG = "async_thread_num";
	public static final String CFG_PATH_KEY_FLAG = "action_path";
	public static final String MULTIPLE_LIST_FLAG = "multiple_list";
	public static final String TASK_CAPACITY_FLAG = "task_capacity";
	public static final String IS_PRINT_FLAG = "is_print";
	
	
	private InfoFileObject[] cfgFile = null;
	private HashMap redirectMapping = null;
	private HashMap actionMapping = null;
	private HashMap defaultActionMapping = null;
	private HashMap appendInfoMapping = null;
	private String poolName = null;
	private boolean isPrint = true;
	
	/**
	 * ִ��ָ����Ϣ�ĵ�ȫ����Ϊ,��Ӧ��Ϊ
	 * ���ĳ����Ϣ���ص���,��ô���л����ص�����Ϣ��ִ�и���Ϣ.�ص�����Ϣֻ�ܵ��򵽾߱�������Ϊ����Ϣ.
	 * Ĭ����Ϊ,���ĳ����Ϣ�����ҵ��κο�ִ�е���Ϊ,��ô������ִ������Ϣ�����Ĭ����Ϊ����ȫ�ֵ�Ĭ����Ϣ��Ϊ,
	 */
	public XZYActionContainerServer(){
		
	}
	
	public void SetPrint(boolean isPrint){
		this.isPrint = isPrint;
	}
	
	public XZYActionContainerServer(String allMsgFlag,String msgNodeName){
		this.ALL_MSG_FLAG = allMsgFlag;
		this.MSG_NODE_NAME = msgNodeName;
	}
	public InfoContainer getAppendInfo(String msgName) {
		InfoContainer info=null;
        if(appendInfoMapping!=null)
          info=(InfoContainer)appendInfoMapping.get(msgName);
        return info;   
    }
	public List getAllAction(String msg){
		if(this.actionMapping == null){
			return null;
		}
		
		RedirectInfo rmsg = (RedirectInfo)this.redirectMapping.get(msg);
		if(rmsg != null){
			msg = rmsg.redirectName;
		}
		
		List allAction = new LinkedList();
		
		List tempList = (List)this.actionMapping.get(ALL_MSG_FLAG);
		if(tempList != null){
			allAction.addAll(tempList);
		}
		tempList = (List)this.actionMapping.get(msg);
		if(tempList != null){
			allAction.addAll(tempList);
		}
		tempList = (List)this.defaultActionMapping.get(msg);
		if(tempList != null){
			allAction.addAll(tempList);
		}
		tempList = (List)this.defaultActionMapping.get(ALL_MSG_FLAG);
		if(tempList != null){
			allAction.addAll(tempList);
		}

		return allAction;
	}
	
	/**
	 * ��ִ��������Ϣ�ϵķ�Ĭ����Ϊ,��ִ�ж�Ӧ��Ϣ�ϵķ�Ĭ����Ϊ
	 * �����������Ϊִ��,��ô���ִ��Ĭ����Ϊ.Ĭ����Ϊִֻ��һ��
	 * ���Ҷ�Ӧ��Ϣ�ϵ�Ĭ����Ϊ,����������Ϣ�ϵ�Ĭ����Ϊ.
	 * @param infos
	 */
	public void executeTask(InfoContainer infos){
		if(this.actionMapping == null){
			this.error("Server["+this.getServerName()+"] can't start up!");
			return ;
		}
		this.loadFromXml();
		
		long t = System.currentTimeMillis();

		Object oriMsg = infos.getInfo(IAction.MSG_FLAG);
		Object msg = oriMsg;
		RedirectInfo rmsg;
		StringBuffer strBuff = null;
		while((rmsg = (RedirectInfo)this.redirectMapping.get(msg)) != null){
			msg = rmsg.redirectName;
			if(rmsg.appendInfo != null){
				infos.addAll(rmsg.appendInfo);
			}
			if(strBuff == null){
				strBuff = new StringBuffer("Msg Redirect:");
				strBuff.append(this.getMsgStr(oriMsg));
			}
			strBuff.append(" -> "+this.getMsgStr(msg));
		}
		if(this.isPrint && strBuff != null){
			this.info(strBuff.toString());
		}
		
		int actionNum = 0;
		List actionList = null;
		
		//ִ��ȫ����Ϣע�����
		actionList = (List)this.actionMapping.get(ALL_MSG_FLAG);
		int returnFlag = this.executeAction(actionList,infos,msg,oriMsg); 
		if(returnFlag == BREAK_FLAG){
			if(this.isPrint){
				String infoStr = this.getServerName()+":MSG["+this.getMsgStr(oriMsg)+"]";
				if(infos.getInfo(OPTIONAL_PRINT_INFO_FLAG) != null){
					infoStr += "["+infos.getInfo(OPTIONAL_PRINT_INFO_FLAG)+"]";
				}
				infoStr += ":";
				infoStr += (System.currentTimeMillis() - t);
				this.info(infoStr);
			}
			TaskCostStatServer.getSingleInstance().addCostInfo(this, this.getServerName()+"["+oriMsg+"]", System.currentTimeMillis() - t);
			return ;
		}
		actionNum += returnFlag;

		//ִ�ж�Ӧ��Ϣע�����
		actionList = (List)this.actionMapping.get(msg);
		returnFlag = this.executeAction(actionList,infos,msg,oriMsg);
		if(returnFlag == BREAK_FLAG){
			if(this.isPrint){
				String infoStr = this.getServerName()+":MSG["+this.getMsgStr(oriMsg)+"]";
				if(infos.getInfo(OPTIONAL_PRINT_INFO_FLAG) != null){
					infoStr += "["+infos.getInfo(OPTIONAL_PRINT_INFO_FLAG)+"]";
				}
				infoStr += ":";
				infoStr += (System.currentTimeMillis() - t);
				this.info(infoStr);
			}
			TaskCostStatServer.getSingleInstance().addCostInfo(this, this.getServerName()+"["+oriMsg+"]", System.currentTimeMillis() - t);
			return ;
		}
		actionNum += returnFlag;
		
		//�����ִ����Ϊ,ִ��Ĭ����Ϊ
		if(actionNum == 0){
			actionList = (List)this.defaultActionMapping.get(msg);
			if(actionList == null || actionList.size() == 0){
				actionList = (List)this.defaultActionMapping.get(ALL_MSG_FLAG);
			}
			this.executeAction(actionList,infos,msg,oriMsg);
		}

		if(this.isPrint){
			String infoStr = this.getServerName()+":MSG["+this.getMsgStr(oriMsg)+"]";
			if(infos.getInfo(OPTIONAL_PRINT_INFO_FLAG) != null){
				infoStr += "["+infos.getInfo(OPTIONAL_PRINT_INFO_FLAG)+"]";
			}
			infoStr += ":";
			infoStr += (System.currentTimeMillis() - t);
			this.info(infoStr);
		}
		TaskCostStatServer.getSingleInstance().addCostInfo(this, this.getServerName()+"["+oriMsg+"]", System.currentTimeMillis() - t);
	}
	
	private String getMsgStr(Object msg){
		if(msg == null){
			return null;
		}
		if(msg instanceof Integer){
			return "0x"+Integer.toHexString((Integer)msg);
		}else if(msg instanceof Long){
			return "0x"+Long.toHexString((Long)msg);
		}
		return msg.toString();
	}

	private static final int BREAK_FLAG = -1;
	/**
	 * ִ�ж��е���Ϊ�б�
	 * @param actionList
	 * @param info
	 * @param msg
	 * @return ���Ϊ-1,����Ӧ����������ִ��.���Ϊ����,����ǰ��ִ�е���Ϊ��.
	 */
	private int executeAction(List actionList,InfoContainer info,Object msg,Object oriMsg){
		if(actionList == null){
			return 0;
		}
		
		int actionNum = 0;
		IAction action = null;
		for(Iterator actionIterator = actionList.iterator();actionIterator.hasNext();){
			action = (IAction)actionIterator.next();
			try{
				if(!action.isInclude(msg.toString())){
					continue;
				}
				if(action.isFilter()){
					if(!action.execute(info)){
						return BREAK_FLAG;
					}
				}else{
					actionNum++;
					if(action.isSynchronized()){
						action.execute(info);
					}else{
						//�첽ִ�У���Ҫһ���̳߳�����
						ThreadPoolServer.getSingleInstance().addTask2ThreadPool(this.poolName, new WorkTask(this.getServerName()+"-"+oriMsg,action,info));
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return actionNum;
	}
	
	public boolean startServer(){
		int threadNum = 1;
		int taskCapacity = 1000;
		
		String tempStr;
		try{
			tempStr = this.getStringPara(ASYNC_THREAD_NUM_FLAG);
			if(tempStr != null && tempStr.trim().length() > 0){
				threadNum = Integer.parseInt(tempStr.trim());
			}
		}catch(Exception e){
			this.error("ASYNC_THREAD_NUM_FLAG config error!",e);
		}
		try{
			tempStr = this.getStringPara(TASK_CAPACITY_FLAG);
			if(tempStr != null && tempStr.trim().length() > 0){
				taskCapacity = Integer.parseInt(tempStr.trim());
			}
		}catch(Exception e){
			this.error("TASK_CAPACITY_FLAG config error!",e);
		}
		
		try{
			this.poolName = this.getServerName()+"-ThreadPool["+this.hashCode()+"]";
			
			ThreadPoolInfo.QUEUE_TYPE queueType = ThreadPoolInfo.QUEUE_TYPE.SINGLE_QUEUE_POOL;
			tempStr = this.getStringPara(MULTIPLE_LIST_FLAG);
			if(tempStr != null && tempStr.trim().equalsIgnoreCase("true")){
				queueType = ThreadPoolInfo.QUEUE_TYPE.MULTIPLE_QUEUE_POOL;
			}
			ThreadPoolInfo pInfo = new ThreadPoolInfo(threadNum,taskCapacity,queueType);
			ThreadPoolServer.getSingleInstance().createThreadPool(this.poolName, pInfo);
		}catch(Exception e){
			this.error("Server["+this.getServerName()+"] ThreadPool Init Failure!",e);
			return false;
		}
		tempStr = this.getStringPara(IS_PRINT_FLAG);
		if(tempStr != null && tempStr.trim().equalsIgnoreCase("false")){
			this.isPrint = false;
		}else{
			this.isPrint = true;
		}
		
		Object cfgPath = this.getPara(CFG_PATH_KEY_FLAG);
		if(cfgPath == null){
			this.error("Config File Path null error.");
			return false;
		}
		if(cfgPath instanceof List){
			this.cfgFile = new InfoFileObject[((List)cfgPath).size()];
			int i=0;
			for(Iterator itr = ((List)cfgPath).iterator();itr.hasNext();){
				this.cfgFile[i++] = this.createObject(itr.next().toString());
			}
		}else{
			this.cfgFile = new InfoFileObject[]{this.createObject(cfgPath.toString())};
		}
		this.lastLoadTime = new long[this.cfgFile.length];
		
		boolean isSuccess =  this.loadFromXml();
		
		this.isRun = isSuccess;
		
		return this.isRunning();
	}
	
	private InfoFileObject createObject(String path){
		try{
			if(path.toUpperCase().startsWith("HTTP")){
				return new InfoFileObject(new URL(path));
			}else if(path.startsWith("!")){
				return new InfoFileObject(this.getClass().getClassLoader().getResource(path.substring(1)));
			}else{
				return new InfoFileObject(new File(path));
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	private long lastDetectTime = 0;
	private long[] lastLoadTime;
	private boolean loadFromXml(){
		if(System.currentTimeMillis()-this.lastDetectTime < 60*1000){
			return true;
		}
		if(this.cfgFile == null || this.cfgFile.length == 0){
			return false;
		}
		this.lastDetectTime = System.currentTimeMillis();
		
		synchronized(this){
			boolean isChanged = false;
			long lastModifyTime;
			for(int i=0;i<this.cfgFile.length;i++){
				lastModifyTime = this.cfgFile[i].lastModified();
				if(lastModifyTime > 0){
					isChanged |= (lastModifyTime != this.lastLoadTime[i]);
				}else{
					isChanged |= (this.lastLoadTime[i] == 0);
				}
			}
			if(!isChanged){
				return true;
			}
			
			HashMap newMapping = new HashMap();
			HashMap defaultNewMapping = new HashMap();
	        HashMap newAppendInfoMapping = new HashMap();
	        HashMap newRedirectMapping = new HashMap();
			
	        boolean isSuccess = true;
	        for(int i=0;i<this.cfgFile.length;i++){
		        	this.lastLoadTime[i] = cfgFile[i].lastModified();
		        	
		        	if(isSuccess){
		        		isSuccess &= this.loadFromXml(this.cfgFile[i], newMapping, defaultNewMapping, newAppendInfoMapping, newRedirectMapping);
		        	}
	        }
	
			if(isSuccess){
				this.actionMapping = newMapping;
				this.defaultActionMapping = defaultNewMapping;
				this.appendInfoMapping = newAppendInfoMapping;
				this.redirectMapping = newRedirectMapping;
			}
		}
		return true;
	}
	
	private boolean loadFromXml(InfoFileObject xmlFile,HashMap newMapping,HashMap defaultNewMapping,HashMap newAppendInfoMapping,HashMap newRedirectMapping){
		if(!xmlFile.exists()){
			return true;
		}
		try{
			//���������ļ�
			DocumentBuilderFactory domfac=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = domfac.newDocumentBuilder();
			Document document = builder.parse(xmlFile.getInputStream());
			
			Element root = document.getDocumentElement();
			String globalMsgType = XMLUtil.getNodeAttr(root, "msg_type"),msgType;
			if(globalMsgType == null){
				globalMsgType = "string";
			}
			NodeList allMsgNodeList = root.getElementsByTagName(MSG_NODE_NAME);
			int msgNum = allMsgNodeList.getLength();
			Element msgNode;
			String _msgName,redirectName;
			Object msgName = null;
			ArrayList actionList = null,defaultActionList = null;
			for(int i=0;i<msgNum;i++){
				msgNode = (Element)allMsgNodeList.item(i);

				msgType = XMLUtil.getNodeAttr(msgNode, "msg_type");
				if(msgType == null){
					msgType = globalMsgType;
				}
				_msgName = XMLUtil.getNodeAttr(msgNode, "name");
				if(_msgName == null || _msgName.trim().length()==0){
					continue;
				}
				_msgName = _msgName.trim();
				
				if(msgType != null && msgType.equalsIgnoreCase("int") && !_msgName.equals(ALL_MSG_FLAG)){
					msgName = new Integer(Const.parseInt(_msgName));
				}else{
					msgName = _msgName;
				}
				
				redirectName = XMLUtil.getNodeAttr(msgNode, "redirect");
				if(redirectName != null &&redirectName.trim().length() > 0){
					RedirectInfo rInfo = new RedirectInfo();
					rInfo.redirectName = redirectName.trim();
					Node[] paraArr = XMLUtil.getSonElementsByTagName(msgNode, "para");
					if(paraArr != null){
						for(int p=0;p<paraArr.length;p++){
							if(rInfo.appendInfo == null){
								rInfo.appendInfo = new InfoContainer();
							}
							rInfo.appendInfo.setInfo(XMLUtil.getNodeAttr(paraArr[p], "key"), XMLUtil.getNodeAttr(paraArr[p], "value"));
						}
					}
					newRedirectMapping.put(msgName, rInfo);
					continue;
				}
				
				actionList = new ArrayList();
				defaultActionList = new ArrayList();  
				this.loadActionFromXml(msgNode.getChildNodes(),actionList,defaultActionList);
				if(actionList.size() > 0){
					actionList.trimToSize();
					newMapping.put(msgName, actionList);
				}  
				if(defaultActionList.size() > 0){
					actionList.trimToSize();
					defaultNewMapping.put(msgName, defaultActionList);
				}  

                InfoContainer info=new InfoContainer();
                newAppendInfoMapping.put(msgName,info);
				Node[] nodes=XMLUtil.getAllAttrNode(msgNode);
				for(int k=0;k<nodes.length;k++){
					if(nodes[k].getNodeName()==null||nodes[k].getNodeName().equals(""))
						continue;
	                info.setInfo(nodes[k].getNodeName(),nodes[k].getNodeValue());
				}
                Node[] paraArr = XMLUtil.getSonElementsByTagName(msgNode, "para");
                Node paraNode;
                int paraNum = paraArr.length;
                for(int j=0;j<paraNum;j++){
                    paraNode = paraArr[j];
                    info.setInfo(XMLUtil.getNodeAttr(paraNode, "key"), XMLUtil.getNodeAttr(paraNode, "value"));
                }
			}
		}catch(Exception e){
			e.printStackTrace();
			this.error("Load Mapping Server["+this.getServerName()+"]'s Config ["+xmlFile+"] Failure!",e);
			return false;
		}
		this.info("Load Mapping Server["+this.getServerName()+"]'s Config ["+xmlFile+"] Success!");
		return true;
	}
	
	private void loadActionFromXml(NodeList actionNodeList,List actionList,List defaultActionList) throws Exception{
		int num = actionNodeList.getLength();
		Node actionNode ;
		String tagName,name,synch,className;
		IAction action;
		String enable,defaultAction;
		for(int i=0;i<num;i++){
			actionNode = actionNodeList.item(i);
			if(actionNode.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			
			tagName = ((Element)actionNode).getTagName();
			if(tagName == null){
				continue;
			}
			tagName = tagName.trim();
			if(!tagName.equalsIgnoreCase("filter") && ! tagName.equalsIgnoreCase("action")){
				continue;
			}
			name= XMLUtil.getNodeAttr(actionNode, "name");
			synch= XMLUtil.getNodeAttr(actionNode, "sync");
			defaultAction = XMLUtil.getNodeAttr(actionNode, DEFAULT_ACTION_FLAG);
			
			className = XMLUtil.getNodeText(XMLUtil.getSingleElementByTagName(actionNode, "class_name")).trim();
			enable = XMLUtil.getNodeText(XMLUtil.getSingleElementByTagName(actionNode, "enable"));
			if(enable != null  && enable.equalsIgnoreCase("false")){
				this.info("Action ["+name+"] is disabled!");
				continue;
			}
			String createMethod = XMLUtil.getNodeText(XMLUtil.getSingleElementByTagName(actionNode, "create_method"));
			try{
				Class cls = Class.forName(className);
				if(createMethod == null || createMethod.trim().length() == 0){
					action = (IAction)cls.newInstance();
				}else{
					Method method = cls.getMethod(createMethod, new Class[0]);
					action = (IAction)method.invoke(null, new Object[0]);
				}
			}catch(Exception e){
				this.error("Class ("+className+") create instance failure.",e);
				continue;
			}
			
			action.addPara(IAction._NAME_FLAG, name);
			if(synch  == null || synch.equalsIgnoreCase("true")){
				action.addPara(IAction._SYNC_FLAG, Boolean.TRUE);
			}else{
				action.addPara(IAction._SYNC_FLAG, Boolean.FALSE);
			}
			if(tagName.equalsIgnoreCase("filter")){
				action.addPara(IAction._FILTER_FLAG, Boolean.TRUE);
			}else{
				action.addPara(IAction._FILTER_FLAG, Boolean.FALSE);
			}
			
			Node[] paraArr = XMLUtil.getElementsByTagName(actionNode, "para");
			Node paraNode;
			int paraNum = paraArr.length;
			for(int j=0;j<paraNum;j++){
				paraNode = paraArr[j];
				action.addPara(XMLUtil.getNodeAttr(paraNode, "key"), XMLUtil.getNodeAttr(paraNode, "value"));
			}
			action.addPara(IAction._ACTION_CONTAINER_FLAG, this);

			try{
				action.init();
			}catch(Exception e){
				this.error("action init failure,it will not be mapped!",e);
				continue;
			}

			//�˾���������Ĭ����Ϊ,ֻ����Ϊ���п�����Ĭ����Ϊ.
			if(action.isFilter()){
				actionList.add(action);
			}else{
				if(defaultAction != null && defaultAction.equals("true")){
					defaultActionList.add(action);
				}else{
					actionList.add(action);
				}
			}
		}
	}
	
	public void stopServer(){
		super.stopServer();
		this.actionMapping = null;
		
		ThreadPoolServer.getSingleInstance().removeThreadPool(this.poolName);
	}
	
	public String getInfoDesc(){
		if(!this.isRunning()){
			return "������ֹͣ!";
		}
		StringBuffer infoDesc = new StringBuffer(128);
		infoDesc.append("threadNum=");
		infoDesc.append(ThreadPoolServer.getSingleInstance().getThreadNum(poolName));
		infoDesc.append(";");
		infoDesc.append("capacity=");
		infoDesc.append("[");
		int[] waitNumArr = ThreadPoolServer.getSingleInstance().getThreadPoolWaitTaskNumArr(poolName);
		for(int i=0;i<waitNumArr.length;i++){
			if(i>0){
				infoDesc.append(",");
			}
			infoDesc.append(waitNumArr[i]);
		}
		infoDesc.append("]");
		infoDesc.append(";totalTaskNum=");
		infoDesc.append(ThreadPoolServer.getSingleInstance().getThreadPoolFinishTaskNum(poolName));
	
		return infoDesc.toString();
	}
	
	private class RedirectInfo{
		public String redirectName = null;
		public InfoContainer appendInfo = null;
	}
	
	public class InfoFileObject{
		private Object cfgFile = null;
		private boolean isFile = false;
		
		public InfoFileObject(Object cfgFile){
			this.cfgFile = cfgFile;
			this.isFile = (this.cfgFile instanceof File);
		}
		
		public boolean isFile(){
			return this.isFile;
		}
		
		public long lastModified(){
			if(this.isFile){
				return ((File)this.cfgFile).lastModified();
			}
			return -1;
		}
		
		public boolean exists(){
			if(this.isFile){
				return ((File)this.cfgFile).exists();
			}
			return true;
		}
		
		public InputStream getInputStream() throws Exception{
			if(this.isFile){
				return new FileInputStream((File)this.cfgFile);
			}else{
				return ((URL)this.cfgFile).openStream();
			}
		}
		
		public String toString(){
			if(this.isFile){
				return ((File)this.cfgFile).getAbsolutePath();
			}else{
				return ((URL)this.cfgFile).getPath();
			}
		}
	}
	
	public static class WorkTask extends BasicTask{
		private IAction action;
		private InfoContainer info;
		public WorkTask(String name,IAction action,InfoContainer info){
			this.action = action;
			this.info = info;
		}
		
		public Object getFlag(){
			return info.getInfo(IAction.TASK_FLAG);
		}
		public String getDesc(){
			return info.getString(IAction.MSG_FLAG);
		}
		public String getMsgFlag(){
			return info.getString(IAction.MSG_FLAG);
		}

		@Override
		public void run(){
			try{
				this.action.execute(info);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}

    public HashMap getAppendInfoMapping()
    {
        return (HashMap)appendInfoMapping.clone();
    }
}
