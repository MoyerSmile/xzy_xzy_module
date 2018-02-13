package com.xzy.base.parser;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import com.xzy.base_c.InfoContainer;

public class GroupItem<T> extends Item<T> {
	private Item[] itemArr = null;
	
	public GroupItem(XZYProtocolParser parser,ITEM_TYPE type){
		super(parser,type);
	}
	
	public Item[] getItemArr() {
		return itemArr;
	}
	public void setItemArr(Item[] itemArr) {
		this.itemArr = itemArr;
	}
	
	public T createValue(InfoContainer info,ByteBuffer data,ByteBuffer oriData,boolean isMovePosition) throws Exception{
		int loop = 0;
		boolean isFindUntilEnd = false;
		if(len instanceof Integer){
			loop = ((Integer)len).intValue();
			if(loop == 0){
				isFindUntilEnd = true;
			}
		}else{
			loop = info.getInteger(len);
		}

		LinkedList<InfoContainer> list = new LinkedList<InfoContainer>();
		InfoContainer subInfo;
		Item item;
		Object val;
		for(int i=0;i<loop || (isFindUntilEnd && i < 1000);i++){
			if(data.remaining() <= 0){
				break;
			}
			subInfo = new InfoContainer();
			for(int j = 0;j<this.itemArr.length;j++){
				item = this.itemArr[j];
				subInfo.setInfo(item.getName(), val = item.createValue(subInfo, data, oriData, true));
				if(item.getDefaultValue() != null){
					if(!item.getDefaultValue().equals(val)){
						throw new Exception(this.getName()+":Data Error.group="+this.name+" name="+item.getName()+ " val="+val+" defaultVal="+item.getDefaultValue());
					}
				}
				if(!item.verify(subInfo, oriData)){
					this.parser.error(this.name+"["+this.parser.getCallServerName()+"]"+":group="+this.name+" Field["+item.getName()+"] Verify Error");
					return null;
				}
			}
			list.add(subInfo);
		}
		
		return (T)list;
	}
	
	public int getInfoLen(InfoContainer info) throws Exception{
		int loop = 0;
		List<InfoContainer> listVal = (List<InfoContainer>)info.getInfo(this.name);
		if(this.len instanceof Integer){
			loop = ((Integer)this.len).intValue();
			if(loop == 0 && listVal != null){
				loop = listVal.size();
			}
		}else if(info.getInfo(this.len) != null){
			loop = info.getInteger(len).intValue();
		}else{
			if(listVal == null){
				loop = 0;
			}
			loop = listVal.size();

			info.setInfo(len, new Integer(loop));
		}
		
		int realLen = 0;
		InfoContainer subInfo;
		for(int i=0;i<loop;i++){
			if(i < listVal.size()){
				subInfo = listVal.get(i);
			}else{
				subInfo = null;
			}
			for(int j=0;j<this.itemArr.length;j++){
				realLen += this.itemArr[j].getInfoLen(subInfo);
			}
		}
		return realLen;
	}
	
	public void appendValue(ByteBuffer buffer,InfoContainer info) throws Exception{
		List<InfoContainer> listVal = null;
		this.createVerify(info, buffer);
		if(this.defaultVal != null){
			listVal = (List<InfoContainer>)this.defaultVal;
		}else{
			if(info != null){
				listVal = (List<InfoContainer>)info.getInfo(this.name);
			}
		}
		
		int infoLen = this.getInfoLen(info);
		int newPos = buffer.position() + infoLen;
		
		int loop;
		if(this.len instanceof Integer){
			loop = ((Integer)this.len).intValue();
			if(loop == 0 && listVal != null){
				loop = listVal.size();
			}
		}else{
			loop = info.getInteger(len).intValue();
		}
		InfoContainer subInfo;
		for(int i=0;i<loop;i++){
			if(listVal != null && i < listVal.size()){
				subInfo = listVal.get(i);
			}else{
				subInfo = null;
			}
			for(int j=0;j<this.itemArr.length;j++){
				this.itemArr[j].appendValue(buffer, subInfo);
			}
		}
		
		buffer.position(newPos);
	}
	
}
