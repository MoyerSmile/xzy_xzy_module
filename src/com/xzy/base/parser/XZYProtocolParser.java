package com.xzy.base.parser;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


import com.xzy.base.Const;
import com.xzy.base.XMLUtil;
import com.xzy.base_c.InfoContainer;
import com.xzy.base_i.IServer;

/**
 * 按照配置的XML文件进行协议解析、验证、转义等功能实现的函数，如国标905、794等等
 * @author Edmund
 *
 */
public class XZYProtocolParser extends IParser{
	public static enum MATCH_MODE{
		HEAD_TAIL,
		LENGTH,
		TOTAL
	}
	
	//小端字节序
	private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
	//呼叫分析服务的服务方名称
	private String callServerName = null;
	//协议名称
	private String name = null;
	//用于并发处理的串行控制的字段信息名
	private String serialDisposeFieldName = null;
	//是否发送方，发送方使用go作为出，receiver方使用come作为出
	private boolean isSender = true;
	//数据开启空间的最大和初始化长度，对于head_tail模式，只使用初始化空间，对于长度模式，如果长度超过初始空间，会扩大到最大模式，但是不会超过最大模式
	private int initCapacity = 4*1024,maxCapacity = 64*1024;
	//应用协议的匹配模式
	private MATCH_MODE matchMode = null;
	//如果是长度模式，那么lenInfoName代表长度信息的字段名称，totalLenOffset代表应用消息的整体长度和此长度的偏差值。
	private String lenInfoName = null;
	private int totalLenOffset = 0;
	//字符串的字符集
	private String charset = null;
	//字符串的结束标识，在字符串长度为0的情况下使用
	private byte stringEndByteFlag = 0;
	//如果前面数据收到的时间距今超过下面的时长，那么前面的数据将进行抛弃，默认配置足够大，不会超时。单位秒
	private int dataTimeout = Integer.MAX_VALUE;
	
	//完整应用数据的转换器
	private LinkedList<ITransform> transformList = null;
	
	//数据格式的模版
	private Head head = null;
	private Body body = null;
	private Tail tail = null;


	public XZYProtocolParser(){
		this(null,null);
	}
	
	public XZYProtocolParser(File _xmlProtocolFile){
		this(null,_xmlProtocolFile);
	}
	public XZYProtocolParser(String callServerName,File _xmlProtocolFile){
		this.callServerName = callServerName;
		this.xmlProtocolFile = _xmlProtocolFile;
	}
	
	@Override
	public boolean startServer() {
		if(this.getStringPara("call_server_name") != null){
			this.callServerName = this.getStringPara("call_server_name");
		}
		Object obj ;
		if((obj = this.getPara("template")) != null){
			if(obj instanceof File){
				this.xmlProtocolFile = (File)obj;
			}else{
				this.xmlProtocolFile = new File(this.getStringPara("template"));
			}
		}
		IServer callServer = (IServer)this.getPara(CALL_SERVER_FLAG);
		if(callServer != null){
			if(this.xmlProtocolFile == null){
				if((obj = callServer.getPara("protocol_template")) != null){
					if(obj instanceof File){
						this.xmlProtocolFile = (File)obj;
					}else{
						this.xmlProtocolFile = new File(callServer.getPara("protocol_template").toString());
					}
				}
			}
			
			this.callServerName = callServer.getServerName();
		}
		try{
			this.analysisXml();
			
			if(callServer != null && (obj = callServer.getPara("protocol_dir")) != null){
				if(obj.equals("receiver")){
					this.isSender = false;
				}
			}
			
			this.isRun = true;
		}catch(Exception e){
			this.error(this.name+" load xml error!", e);
		}
		
		return this.isRunning();
	}
	
	public void setCallServerName(String callServerName){
		this.callServerName = callServerName;
	}
	public String getCallServerName(){
		return this.callServerName;
	}
	public String getName(){
		return this.name;
	}
	public ByteOrder getByteOrder(){
		return this.byteOrder;
	}
	public MATCH_MODE getMatchMode(){
		return this.matchMode;
	}
	public String getCharset(){
		return this.charset;
	}
	public byte getStringEndByteFlag(){
		return this.stringEndByteFlag;
	}
	public int getCapacity(){
		return this.initCapacity;
	}

	public String getMsgName(){
		if(this.body == null){
			return null;
		}
		return this.body.getMsgIDName();
	}
	public String getSeriralDisposeFieldName(){
		return this.serialDisposeFieldName;
	}
	
	public int getMaxCapacity(){
		return this.maxCapacity;
	}
	public int getDataTimeout(){
		return this.dataTimeout;
	}
	public String getLenInfoName(){
		return this.lenInfoName;
	}
	public int getTotalLenOffset(){
		return this.totalLenOffset;
	}
	public LinkedList<ITransform> getTrasformList(){
		return this.transformList;
	}

	private File xmlProtocolFile = null;
	private long lastModifyTime = 0;
	private long lastUpdateTime = 0;
	private void analysisXml() throws Exception{
		if(this.xmlProtocolFile == null){
			return ;
		}
		if(System.currentTimeMillis() - this.lastUpdateTime < 60000){
			return ;
		}
		if(this.lastModifyTime == this.xmlProtocolFile.lastModified()){
			return ;
		}
		this.lastModifyTime = this.xmlProtocolFile.lastModified();
		this.lastUpdateTime = System.currentTimeMillis();
		
		Document doc = XMLUtil.parseXml(xmlProtocolFile);
		if(doc == null){
			throw new Exception("XML Format Error:"+xmlProtocolFile.getAbsolutePath());
		}
		Element root = doc.getDocumentElement();
		
		this.name = XMLUtil.getNodeAttr(root, "name");
		this.serialDisposeFieldName = XMLUtil.getNodeAttr(root, "serial_dispose_field_name");
		this.charset = XMLUtil.getNodeAttr(root, "charset");
		String tempStr;
		
		tempStr = XMLUtil.getNodeAttr(root, "dir");
		if(tempStr != null && tempStr.equals("receiver")){
			this.isSender = false;
		}
		
		tempStr = XMLUtil.getNodeAttr(root, "string_end_byte_flag");
		if(tempStr != null && tempStr.trim().length() > 0){
			this.stringEndByteFlag = (byte)Const.parseInt(tempStr.trim());
		}
		
		tempStr = XMLUtil.getNodeAttr(root, "endian");
		if(tempStr != null && tempStr.equalsIgnoreCase("big")){
			this.byteOrder = ByteOrder.BIG_ENDIAN;
		}else{
			this.byteOrder = ByteOrder.LITTLE_ENDIAN;
		}
		
		tempStr = XMLUtil.getNodeAttr(root, "data_timeout");
		if(tempStr != null && tempStr.trim().length() > 0){
			this.dataTimeout = Const.parseInt(tempStr)*1000;
		}
		tempStr = XMLUtil.getNodeAttr(root, "capacity");
		if(tempStr != null && tempStr.trim().length() > 0){
			this.initCapacity = Const.parseInt(tempStr)*1024;
		}
		tempStr = XMLUtil.getNodeAttr(root, "max_capacity");
		if(tempStr != null && tempStr.trim().length() > 0){
			this.maxCapacity = Const.parseInt(tempStr)*1024;
		}

		tempStr = XMLUtil.getNodeAttr(root, "match");
		if(tempStr == null || tempStr.trim().length() == 0){
			throw new Exception("no match mode setting");
		}
		if(tempStr.equalsIgnoreCase("head_tail")){
			this.matchMode = MATCH_MODE.HEAD_TAIL;
		}
		if(tempStr.startsWith("length=")){
			this.matchMode = MATCH_MODE.LENGTH;
			tempStr = tempStr.substring("length=".length()).trim();
			int index = tempStr.lastIndexOf('+');
			if(index < 0){
				index = tempStr.lastIndexOf('-');
			}
			if(index < 0){
				this.lenInfoName = tempStr.substring(1);
				this.totalLenOffset = 0;
			}else{
				this.lenInfoName = tempStr.substring(1,index);
				this.totalLenOffset = Const.parseInt(tempStr.substring(index+1));
			}
		}
		this.info(this.name+"["+this.callServerName+"]"+this.toString());

		this.analysisTransform(XMLUtil.getSonSingleElementByTagName(root, "transform"));
		this.analysisHead(XMLUtil.getSonSingleElementByTagName(root, "head"));
		this.analysisBody(XMLUtil.getSonSingleElementByTagName(root, "body"));
		this.analysisTail(XMLUtil.getSonSingleElementByTagName(root, "tail"));
		
		if(this.matchMode == MATCH_MODE.HEAD_TAIL){
			if(this.head.getItemArr()[0].getDefaultValue() == null || this.tail.getItemArr()[this.tail.getItemArr().length-1] == null){
				throw new Exception(name+": can not use head-tail match mode,first item or last item has no defaultValue");
			}
		}else if(this.matchMode == MATCH_MODE.LENGTH){
			if(this.head.getItemByName(this.lenInfoName) == null){
				throw new Exception(name+": can not find the len info field from the head. "+this.lenInfoName);
			}
		}else{
			throw new Exception(this.name+" Mode Error");
		}
	}
	

	private void analysisTransform(Node transformNode) throws Exception{
		Node[] nodeArr = XMLUtil.getSonElementsByTagName(transformNode, "action");
		if(nodeArr == null || nodeArr.length == 0){
			return ;
		}

		this.transformList = new LinkedList<ITransform>();
		
		ITransform transform = null;
		String tempStr;
		Node[] paraNodeArr;
		String[] paraArr ;
		for(int i=0;i<nodeArr.length;i++){
			tempStr = XMLUtil.getNodeAttr(nodeArr[i], "cls");
			transform = (ITransform)Class.forName(tempStr.trim()).newInstance();
			transform.setName(XMLUtil.getNodeAttr(nodeArr[i], "name"));
			paraNodeArr = XMLUtil.getSonElementsByTagName(nodeArr[i], "para");
			if(paraNodeArr == null || paraNodeArr.length == 0){
				paraArr = new String[0];
			}else{
				paraArr = new String[paraNodeArr.length];
				for(int j=0;j<paraNodeArr.length;j++){
					paraArr[j] = XMLUtil.getNodeText(paraNodeArr[j]);
				}
			}
			transform.setParas(paraArr);
			this.transformList.add(transform);
		}
	}
	
	private Item[] analysisItem(String nodeName,Node node) throws Exception{
		Node[] itemNodeArr = XMLUtil.getSonElementsByTagName(node, "item"),verifyNodeArr,paraNodeArr;
		if(itemNodeArr == null){
			return new Item[0];
		}
		String tempStr,itemName=null;
		String[] paraArr;
		IVerifyFunc[] verifyArr;
		IVerifyFunc verify;
		Item testItem = new Item(this,null);
		Item[] itemArr = new Item[itemNodeArr.length];
		try{
		for(int i=0;i<itemNodeArr.length;i++){
			itemName = XMLUtil.getNodeAttr(itemNodeArr[i], "name");
			testItem.setItemTypeByString(XMLUtil.getNodeAttr(itemNodeArr[i], "type").toUpperCase());
			itemArr[i] = testItem.createItemByType();
			
			itemArr[i].setName(itemName);
			tempStr = XMLUtil.getNodeAttr(itemNodeArr[i], "len");
			if(tempStr.startsWith("$")){
				itemArr[i].setLen(tempStr.substring(1).trim());
			}else{
				itemArr[i].setLen(Const.parseInt(tempStr.trim()));
			}
			tempStr = XMLUtil.getNodeAttr(itemNodeArr[i], "maxLen");
			if(tempStr != null && tempStr.trim().length() > 0){
				itemArr[i].setMaxLen(Const.parseInt(tempStr.trim()));
			}
			tempStr = XMLUtil.getNodeAttr(itemNodeArr[i], "value");
			if(tempStr != null && tempStr.trim().length() > 0){
				itemArr[i].setDefaultValue(tempStr);
			}
			
			if(itemArr[i].isNumber()){
				if(tempStr != null && tempStr.trim().length() > 0){
					if(itemArr[i].getIntLen() <= 4){
						itemArr[i].setDefaultValue(Const.parseInt(tempStr.trim()));
					}else{
						itemArr[i].setDefaultValue(Const.parseLong(tempStr.trim()));
					}
				}
				tempStr = XMLUtil.getNodeAttr(itemNodeArr[i], "endian");
				if(tempStr != null && tempStr.trim().length() > 0){
					if(tempStr.trim().equalsIgnoreCase("big")){
						((NumberItem)itemArr[i]).setByteOrder(ByteOrder.BIG_ENDIAN);
					}else{
						((NumberItem)itemArr[i]).setByteOrder(ByteOrder.LITTLE_ENDIAN);
					}
					if(((NumberItem)itemArr[i]).getByteOrder() == this.byteOrder){
						((NumberItem)itemArr[i]).setByteOrder(null);
					}
				}
			}else if(itemArr[i].isString()){
				((StringItem)itemArr[i]).setCharset(XMLUtil.getNodeAttr(itemNodeArr[i], "charset"));
			}else if(itemArr[i].isGroup()){
				((GroupItem)itemArr[i]).setItemArr(this.analysisItem(itemArr[i].getName(),itemNodeArr[i]));
			}
			
			verifyNodeArr = XMLUtil.getSonElementsByTagName(itemNodeArr[i], "verify");
			verifyArr = new IVerifyFunc[verifyNodeArr.length];
			for(int j=0;verifyNodeArr != null && j<verifyNodeArr.length;j++){
				tempStr = XMLUtil.getNodeAttr(verifyNodeArr[j], "cls");
				verify = (IVerifyFunc)Class.forName(tempStr.trim()).newInstance();
				verify.setName(XMLUtil.getNodeAttr(verifyNodeArr[j], "name"));
				paraNodeArr = XMLUtil.getSonElementsByTagName(verifyNodeArr[j], "para");
				if(paraNodeArr == null || paraNodeArr.length == 0){
					paraArr = new String[0];
				}else{
					paraArr = new String[paraNodeArr.length];
					for(int k=0;k<paraNodeArr.length;k++){
						paraArr[k] = XMLUtil.getNodeText(paraNodeArr[k]);
					}
				}
				verify.setParas(itemArr[i],paraArr);
				verifyArr[j] = verify;
			}
			itemArr[i].setVerifyArr(verifyArr);
		}
		}catch(Exception e){
			this.error("Item Analysis Error. node="+nodeName+" item="+itemName);
			throw e;
		}
		return itemArr;
	}
	private void analysisHead(Node headNode) throws Exception{
		Item[] itemArr = this.analysisItem("head",headNode);
		this.head = new Head(itemArr);
	}
	private void analysisBody(Node bodyNode) throws Exception{
		this.body = new Body();
		
		int minLength = 0;
		String tempStr;
		
		tempStr = XMLUtil.getNodeAttr(bodyNode, "minLength");
		if(tempStr != null && tempStr.trim().length() > 0){
			minLength = Const.parseInt(tempStr.trim());
		}
		body.setMinLength(minLength);
		
		tempStr = XMLUtil.getNodeAttr(bodyNode, "msg");
		if(tempStr == null || tempStr.trim().length() == 0 || !tempStr.startsWith("$")){
			throw new Exception(this.name+",body's msg config error!");
		}
		body.setMsgIDName(tempStr.trim().substring(1));
		Item msgItem = this.head.getItemByName(body.getMsgIDName());
		if(msgItem == null){
			throw new Exception(this.name+",body's msg["+body.getMsgIDName()+"] is not exist in head, config error!");
		}
		
		Node[] msgNodeArr = XMLUtil.getSonElementsByTagName(bodyNode, "msg");
		Message msg;
		Object msgId = null;
		HashMap<Object,Message> comeMapping = new HashMap<Object,Message>();
		HashMap<Object,Message> goMapping = new HashMap<Object,Message>();
		for(int i=0;i<msgNodeArr.length;i++){
			msgId = XMLUtil.getNodeAttr(msgNodeArr[i], "id");
			if(msgItem.isNumber()){
				msgId = new Integer(Const.parseInt(msgId.toString()));
			}
			msg = new Message(msgId,this.analysisItem(msgId.toString(),msgNodeArr[i]));
			
			tempStr = XMLUtil.getNodeAttr(msgNodeArr[i], "dir");
			if(tempStr.equalsIgnoreCase("come")){
				comeMapping.put(msgId, msg);
			}else if(tempStr.equalsIgnoreCase("go")){
				goMapping.put(msgId, msg);
			}else{
				throw new Exception(this.name+" ,msgId["+XMLUtil.getNodeAttr(msgNodeArr[i], "id")+"] has error dir.");
			}
		}
		
		body.comeMessageMapping = comeMapping;
		body.goMessageMapping = goMapping;
	}
	private void analysisTail(Node tailNode) throws Exception{
		Item[] itemArr = this.analysisItem("tail",tailNode);
		this.tail = new Tail(itemArr);
	}
	
	public String toString(){
		return "name="+this.name+" endian="+this.byteOrder+" match="+this.matchMode
				+(this.matchMode==MATCH_MODE.LENGTH?"[field="+this.lenInfoName+" offset="+this.totalLenOffset+"]":"")
				+" charset="+this.charset+" timeout="+this.dataTimeout+" capacity="+(this.initCapacity/1024)+"K maxCapacity="+this.maxCapacity/1024;
	}
	
	public Head getHead(){
		return this.head;
	}
	public Body getBody(){
		return this.body;
	}
	public Tail getTail(){
		return this.tail;
	}
	
	public boolean isSender(){
		return this.isSender;
	}
	public void setSender(boolean isSender){
		this.isSender = isSender;
	}
	
	public InfoContainer[] decodeData(ByteBuffer dataBuffer) throws Exception{
		if(!this.isRunning()){
			return null;
		}
		if(dataBuffer == null){
			this.error(this.name+"["+this.callServerName+"]:Data null Exception!");
			return null;
		}
		this.analysisXml();
		
		InfoContainer[] resultArr = null;
		if(this.matchMode == MATCH_MODE.HEAD_TAIL){
			resultArr = this.parseFullDataByHeadTail(dataBuffer);
		}else if(this.matchMode == MATCH_MODE.LENGTH){
			resultArr = this.parseFullDataByLength(dataBuffer);
		}else{
			throw new Exception(this.name+"["+this.callServerName+"]:Error Mode");
		}
		
		return resultArr;
	}
	
	protected InfoContainer[] parseFullDataByHeadTail(ByteBuffer dataBuffer) throws Exception{
		Item startItem = this.head.getItemArr()[0];
		Item endItem = this.tail.getItemArr()[this.tail.getItemArr().length - 1];

		LinkedList<InfoContainer> list = null;
		int startPos = -1,dataLen;
		InfoContainer info = null;
		Object flag;
		ByteBuffer fullDataBuff,findBuff = ByteBuffer.wrap(dataBuffer.array(), dataBuffer.arrayOffset()+dataBuffer.position(), dataBuffer.remaining());
		while(findBuff.remaining() > 0){
			if(startPos >= 0){
				if(findBuff.remaining() < endItem.getIntLen()){
					break;
				}
				flag = endItem.createValue(null, findBuff, null, false);
				if(flag.equals(endItem.getDefaultValue())){
					findBuff.position(findBuff.position()+endItem.getIntLen());
					dataLen = findBuff.position()-startPos;
					dataBuffer.position(dataBuffer.position()+dataLen);
					
					fullDataBuff = ByteBuffer.wrap(dataBuffer.array(),startPos+findBuff.arrayOffset(),dataLen);
					if(list == null){
						list = new LinkedList<InfoContainer>();
					}
					info = this._decodeFullData(fullDataBuff,startItem.getIntLen(),endItem.getIntLen());
					if(info != null){
						list.add(info);
					}
					
					startPos = -1;
				}else{
					findBuff.position(findBuff.position()+1);
				}
			}else{
				if(findBuff.remaining() < startItem.getIntLen()){
					break;
				}
				flag = startItem.createValue(null, findBuff, null, false);
				if(flag.equals(startItem.getDefaultValue())){
					startPos = findBuff.position();
					findBuff.position(findBuff.position()+startItem.getIntLen());
				}else{
					findBuff.position(findBuff.position()+1);
					dataBuffer.position(dataBuffer.position()+1);
				}
			}
		}

		InfoContainer[] infoArr = null;
		if(list != null){
			infoArr = new InfoContainer[list.size()];
			list.toArray(infoArr);
		}
		return infoArr;
	}
	protected InfoContainer[] parseFullDataByLength(ByteBuffer dataBuffer) throws Exception{
		Item[] headItemArr = this.head.getItemArr();
		Item headItem;

		int startPos = -1,dataLen;
		Object flag;
		InfoContainer info = new InfoContainer();
		ByteBuffer fullDataBuff,findBuff = ByteBuffer.wrap(dataBuffer.array(), dataBuffer.arrayOffset()+dataBuffer.position(), dataBuffer.remaining());

		boolean isGoOn = true;
		int notExistByteNum = 0;
		LinkedList<InfoContainer> list = null;
		while(findBuff.hasRemaining() && isGoOn){
			startPos = findBuff.position();
			
			notExistByteNum = 0;
			for(int i=0;i<headItemArr.length;i++){
				headItem = headItemArr[i];

				if(!headItem.existVerify(info, findBuff)){
					notExistByteNum += headItem.getInfoLen(info);
					continue;
				}
				
				flag = headItem.createValue(info, findBuff, null, true);
				if(flag == null){
					isGoOn = false;
					break;
				}
				info.setInfo(headItem.getName(), flag);
				
				if(headItem.getName().equals(this.lenInfoName)){
					dataLen = ((Integer)flag).intValue() + this.totalLenOffset - notExistByteNum;

					findBuff.position(startPos + dataLen);
					dataBuffer.position(dataBuffer.position()+dataLen);
					fullDataBuff = ByteBuffer.wrap(dataBuffer.array(),startPos+findBuff.arrayOffset(),dataLen);
					
					if(list == null){
						list = new LinkedList<InfoContainer>();
					}
					info = this._decodeFullData(fullDataBuff,0,0);
					if(info != null){
						list.add(info);
					}
					break;
				}else if(headItem.getDefaultValue() != null){
					if(flag.equals(headItem.getDefaultValue())){
						continue;
					}else{
						findBuff.position(startPos+1);
						dataBuffer.position(dataBuffer.position()+1);
						break;
					}
				}
			}
		}

		InfoContainer[] infoArr = null;
		if(list != null){
			infoArr = new InfoContainer[list.size()];
			list.toArray(infoArr);
		}
		return infoArr;
	}
	
	public ByteBuffer encodeData(InfoContainer info) throws Exception{
		if(!this.isRunning()){
			return null;
		}
		this.analysisXml();
		
		Message msg = this.body.getGoMessage(info);
		Item[] itemArr = null;
		
		int totalLen = this.head.getMinTotalLen()+this.tail.getMinTotalLen();
		if(msg != null){
			itemArr = msg.getItemArr();
			for(int i=0;i<itemArr.length;i++){
				if(!itemArr[i].existVerify(info, null)){
					continue;
				}
				totalLen += itemArr[i].getInfoLen(info);
			}
		}
		int dataLen = new Integer(totalLen) - this.totalLenOffset;
		if(this.lenInfoName != null){
			info.setInfo(this.lenInfoName, dataLen);
		}
		ByteBuffer buffer = ByteBuffer.allocate(totalLen);
		buffer.order(this.getByteOrder());
		
		itemArr = this.head.getItemArr();
		for(int i=0;i<itemArr.length;i++){
			if(!itemArr[i].existVerify(info, null)){
				continue;
			}
			itemArr[i].appendValue(buffer, info);
		}
		if(msg != null){
			itemArr = msg.getItemArr();
			for(int i=0;i<itemArr.length;i++){
				if(!itemArr[i].existVerify(info, null)){
					continue;
				}
				itemArr[i].appendValue(buffer, info);
			}
		}
		itemArr = this.tail.getItemArr();
		for(int i=0;i<itemArr.length;i++){
			if(!itemArr[i].existVerify(info, null)){
				continue;
			}
			itemArr[i].appendValue(buffer, info);
		}
		
		buffer.flip();
		
		int startOffset = 0, endOffset = 0;
		if(this.matchMode == MATCH_MODE.HEAD_TAIL){
			startOffset = this.head.getItemArr()[0].getIntLen();
			endOffset = this.tail.getItemArr()[this.tail.getItemArr().length - 1].getIntLen();
		}
		buffer = this.transformFullData(buffer,startOffset,endOffset,false);
		
		return buffer;
	}
	
	protected InfoContainer _decodeFullData(ByteBuffer _data,int startOffset,int endOffset) throws Exception{
		ByteBuffer oriData = this.transformFullData(_data,startOffset,endOffset,true);
		if(oriData.remaining() < this.head.getMinTotalLen()+ this.tail.getMinTotalLen()+this.body.getMinLength()){
			this.error(this.name+"["+this.callServerName+"] Data Length is not enougth! " + oriData.remaining());
			throw new Exception(this.name+"["+this.callServerName+"] Data Length is not enougth! " + oriData.remaining());
		}
		
		ByteBuffer data = ByteBuffer.wrap(oriData.array(), oriData.arrayOffset()+oriData.position(), oriData.remaining());
		
		InfoContainer info = new InfoContainer();
		info.setInfo(ORI_DATA_FLAG, oriData);

		Item[] itemArr;
		Item item;
		Object val;
		itemArr = this.getHead().getItemArr();
		for(int i=0;i<itemArr.length;i++){
			item = itemArr[i];
			if(!item.existVerify(info, data)){
				continue;
			}
			info.setInfo(item.getName(), val = item.createValue(info, data, oriData, true));
			if(item.getDefaultValue() != null){
				if(!item.getDefaultValue().equals(val)){
					throw new Exception(this.getName()+":Data Error.name="+item.getName()+ " val="+val+" defaultVal="+item.getDefaultValue());
				}
			}
			if(!item.verify(info, oriData)){
				this.error(this.name+"["+this.callServerName+"]"+":Field["+item.getName()+"] Verify Error");
				return null;
			}
		}
		
		int limit = data.limit();
		data.limit(limit - this.tail.getMinTotalLen());
		Message message = this.getBody().getComeMessage(info);
		if(message != null){
			itemArr = message.getItemArr();
			for(int i=0;i<itemArr.length;i++){
				item = itemArr[i];
				if(!item.existVerify(info, data)){
					continue;
				}
				info.setInfo(item.getName(), val = item.createValue(info, data, oriData, true));
				if(item.getDefaultValue() != null){
					if(!item.getDefaultValue().equals(val)){
						throw new Exception("Data Error.name="+item.getName()+ " val="+val+" defaultVal="+item.getDefaultValue());
					}
				}
				if(!item.verify(info, oriData)){
					this.error(XZYProtocolParser.this.name+"["+XZYProtocolParser.this.callServerName+"]"+":Field["+item.getName()+"] Verify Error");
					return null;
				}
			}
		}
		data.limit(limit);

		data.position(data.limit()-this.tail.getMinTotalLen());
		itemArr = this.tail.getItemArr();
		for(int i=0;i<itemArr.length;i++){
			item = itemArr[i];
			if(!item.existVerify(info, data)){
				continue;
			}
			info.setInfo(item.getName(), val = item.createValue(info, data, oriData, true));
			if(item.getDefaultValue() != null){
				if(!item.getDefaultValue().equals(val)){
					throw new Exception("Data Error.name="+item.getName()+ " val="+val+" defaultVal="+item.getDefaultValue());
				}
			}
			if(!item.verify(info, oriData)){
				this.error(this.name+"["+this.callServerName+"]"+":Field["+item.getName()+"] Verify Error");
				return null;
			}
		}
		
		return info;
	}
	
	protected ByteBuffer transformFullData(ByteBuffer _data,int startOffset,int endOffset,boolean isDecode) throws Exception{
		if(this.transformList == null || this.transformList.size() == 0){
			return _data;
		}
		ByteBuffer data = _data;
		for(Iterator<ITransform> itr = this.transformList.iterator();itr.hasNext();){
			if(isDecode){
				data = itr.next().decode(this, data,startOffset,endOffset);
			}else{
				data = itr.next().encode(this, data,startOffset,endOffset);
			}
		}
		data.order(this.getByteOrder());
		return data;
	}
	
	public class Message{
		int minTotalLen = 0;
		Object msgId = null;
		Item[] msgItemArr = null;
		

		public Message(Object msgId,Item[] msgItemArr){
			this.msgId = msgId;
			this.msgItemArr = msgItemArr;
			Object lenObj;
			for(int i=0;i<this.msgItemArr.length;i++){
				lenObj = this.msgItemArr[i].getLen();
				if(lenObj instanceof Integer){
					this.minTotalLen += ((Integer)lenObj).intValue();
				}
			}
		}
		public Item[] getItemArr(){
			return this.msgItemArr;
		}
		public int getMinTotalLen(){
			return this.minTotalLen;
		}
		public void setId(Object msgId){
			this.msgId = msgId;
		}
		public Object getId(){
			return this.msgId;
		}
	}
	
	public class Head{
		int minTotalLen = 0;
		Item[] headItemArr = null;

		public Head(Item[] headItemArr){
			this.headItemArr = headItemArr;
			Object lenObj;
			for(int i=0;i<this.headItemArr.length;i++){
				lenObj = this.headItemArr[i].getLen();
				if(lenObj instanceof Integer){
					this.minTotalLen += ((Integer)lenObj).intValue();
				}
			}
		}
		
		public Item getItemByName(String name){
			for(int i=0;i<this.headItemArr.length;i++){
				if(this.headItemArr[i].getName().equals(name)){
					return this.headItemArr[i];
				}
			}
			return null;
		}
		
		public Item[] getItemArr(){
			return this.headItemArr;
		}
		public int getMinTotalLen(){
			return this.minTotalLen;
		}
	}
	
	public class Body{
		int minLength = 0;
		String msgIDName = null;
		HashMap<Object,Message> comeMessageMapping = null;
		HashMap<Object,Message> goMessageMapping = null;
		
		public int getComeMessageNum(){
			HashMap<Object,Message> mapping = null;
			if(isSender()){
				mapping = this.comeMessageMapping;
			}else{
				mapping = this.goMessageMapping;
			}
			if(mapping == null){
				return 0;
			}
			return mapping.size();
		}
		public Message getComeMessage(InfoContainer info){
			return this.getComeMessage(info.getInfo(this.msgIDName));
		}
		public Message getComeMessage(Object id){
			HashMap<Object,Message> mapping = null;
			if(isSender()){
				mapping = this.comeMessageMapping;
			}else{
				mapping = this.goMessageMapping;
			}
			if(mapping == null){
				return null;
			}
			return mapping.get(id);
		}
		public int getGoMessageNum(){
			HashMap<Object,Message> mapping = null;
			if(isSender()){
				mapping = this.goMessageMapping;
			}else{
				mapping = this.comeMessageMapping;
			}
			if(mapping == null){
				return 0;
			}
			return mapping.size();
		}
		public Message getGoMessage(InfoContainer info){
			return this.getGoMessage(info.getInfo(this.msgIDName));
		}
		public Message getGoMessage(Object id){
			HashMap<Object,Message> mapping = null;
			if(isSender()){
				mapping = this.goMessageMapping;
			}else{
				mapping = this.comeMessageMapping;
			}
			if(mapping == null){
				return null;
			}
			return mapping.get(id);
		}
		
		public void setMinLength(int minLength){
			this.minLength = minLength;
		}
		public int getMinLength(){
			return this.minLength;
		}
		public void setMsgIDName(String msgIDName){
			this.msgIDName = msgIDName;
		}
		public String getMsgIDName(){
			return this.msgIDName;
		}
	}
	
	public class Tail{
		int minTotalLen = 0;
		Item[] tailItemArr = null;
		
		public Tail(Item[] tailItemArr){
			this.tailItemArr = tailItemArr;
			Object lenObj;
			for(int i=0;i<this.tailItemArr.length;i++){
				lenObj = this.tailItemArr[i].getLen();
				if(lenObj instanceof Integer){
					this.minTotalLen += ((Integer)lenObj).intValue();
				}
			}
		}
		
		public Item[] getItemArr(){
			return this.tailItemArr;
		}
		public int getMinTotalLen(){
			return this.minTotalLen;
		}
	}
}
