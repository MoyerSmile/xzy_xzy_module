package com.xzy.base.parser;

import java.nio.ByteBuffer;
import java.util.List;

import com.xzy.base_c.InfoContainer;

public class Item<T> {
	public static enum ITEM_TYPE{
		UINT,
		INT,
		BCD,
		STRING,
		BYTEA,
		GROUP,
		TOTAL
	}
	IVerifyFunc[] verifyArr = null;
	String name;
	ITEM_TYPE type;
	Object len;
	int maxLen = -1;
	Object defaultVal;

	XZYProtocolParser parser = null;
	
	public Item(XZYProtocolParser parser,ITEM_TYPE type){
		this.parser = parser;
		this.type = type;
	}
	
	public void setMaxLen(int maxLen){
		this.maxLen = maxLen;
	}
	public int getMaxLen(){
		return this.maxLen;
	}
	
	public T createValue(InfoContainer info,ByteBuffer data, ByteBuffer oriData,boolean isMovePosition) throws Exception{
		byte[] r = null;
		
		if(len instanceof Integer){
			if((Integer)this.len == 0){
				r = new byte[data.limit()-data.position()];
			}else{
				r = new byte[((Integer)len).intValue()];
			}
		}else{
			r = new byte[info.getInteger(len)];
		}
		
		data.get(r);
		if(!isMovePosition){
			data.position(data.position()-r.length);
		}
		
		return (T)r;
	}
	
	public int getInfoLen(InfoContainer info) throws Exception{
		if(this.len instanceof Integer){
			if((Integer)this.len == 0){
				return this.getDataRealLen(info);
			}
			return ((Integer)this.len).intValue();
		}else if(info.getInfo(this.len) != null){
			return info.getInteger(len).intValue();
		}else{
			int infoLen = getDataRealLen(info);
			info.setInfo(len, new Integer(infoLen));
			return infoLen;
		}
	}
	private int getDataRealLen(InfoContainer info){
		Object val = info.getInfo(this.name);
		int infoLen ;
		if(val == null){
			infoLen = 0;
		}else{
			if(val instanceof byte[]){
				infoLen = ((byte[])val).length;
			}else{
				infoLen = val.toString().getBytes().length;
			}
		}
		if(maxLen > 0){
			infoLen = Math.min(this.maxLen, infoLen);
		}
		return infoLen;
	}
	
	public void appendValue(ByteBuffer buffer,InfoContainer info) throws Exception{
		Object val = null;
		this.createVerify(info, buffer);
		if(this.defaultVal != null){
			val = this.defaultVal;
		}else{
			if(info != null){
				val = info.getInfo(this.name);
			}
		}
		
		int infoLen = this.getInfoLen(info);
		int newPos = buffer.position() + infoLen;
		if(val != null){
			byte[] data = null;
			if(val instanceof byte[]){
				data = (byte[])val;
			}else{
				data = val.toString().getBytes();
			}
			buffer.put(data, 0, Math.min(infoLen, data.length));
		}
		buffer.position(newPos);
	}
	
	public boolean isNumber(){
		return this.type == ITEM_TYPE.INT || this.type == ITEM_TYPE.UINT;
	}
	public boolean isString(){
		return this.type == ITEM_TYPE.STRING;
	}
	public boolean isBcd(){
		return this.type == ITEM_TYPE.BCD;
	}
	public boolean isGroup(){
		return this.type == ITEM_TYPE.GROUP;
	}
	
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	
	public ITEM_TYPE getItemType(){
		return this.type;
	}
	public void setItemType(ITEM_TYPE type){
		this.type = type;
	}
	public void setItemTypeByString(String type){
		this.type = ITEM_TYPE.valueOf(type);
	}
	
	public Item createItemByType(){
		if(this.isNumber()){
			return new NumberItem<Long>(this.parser,this.type);
		}else if(this.isString()){
			return new StringItem<String>(this.parser,this.type);
		}else if(this.isBcd()){
			return new BcdItem<String>(this.parser,this.type);
		}else if(this.isGroup()){
			return new GroupItem<List>(this.parser,this.type);
		}else{
			return new Item<byte[]>(this.parser,this.type);
		}
	}
	
	
	public void setVerifyArr(IVerifyFunc[] verifyArr){
		this.verifyArr = verifyArr;
	}
	public IVerifyFunc[] getVerifyArr(){
		return this.verifyArr;
	}
	
	public Object getLen(){
		return this.len;
	}
	public int getIntLen(){
		return ((Integer)this.len).intValue();
	}
	public void setLen(Object len){
		this.len = len;
	}
	public Object getDefaultValue(){
		return this.defaultVal;
	}
	public void setDefaultValue(Object val){
		this.defaultVal = val;
	}
	
	public void createVerify(InfoContainer info,ByteBuffer data){
		if(this.verifyArr == null || this.verifyArr.length == 0){
			return ;
		}
		
		for(int i=0;i<this.verifyArr.length;i++){
			this.verifyArr[i].createVerifyVal(this.parser, ByteBuffer.wrap(data.array()), info);
		}
	}
	public boolean existVerify(InfoContainer info,ByteBuffer data){
		if(this.verifyArr == null || this.verifyArr.length == 0){
			return true;
		}
		
		for(int i=0;i<this.verifyArr.length;i++){
			if(!this.verifyArr[i].existVerify(this.parser, data, info)){
				return false;
			}
		}
		
		return true;
	}
	public boolean verify(InfoContainer info,ByteBuffer data){
		if(this.verifyArr == null || this.verifyArr.length == 0){
			return true;
		}
		
		for(int i=0;i<this.verifyArr.length;i++){
			if(!this.verifyArr[i].verify(this.parser, data, info)){
				this.parser.error(this.parser.getName()+"["+this.parser.getCallServerName()+"]"+": verify["+this.verifyArr[i].getName()+"] fail.");
				return false;
			}
		}
		
		return true;
	}
}
