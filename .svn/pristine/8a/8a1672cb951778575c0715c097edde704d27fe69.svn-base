package com.xzy.base.parser;

import java.nio.ByteBuffer;

import com.xzy.base.Const;
import com.xzy.base_c.InfoContainer;

public class BcdItem<T> extends Item<T> {
	public BcdItem(XZYProtocolParser parser,ITEM_TYPE type){
		super(parser,type);
	}
	
	public T createValue(InfoContainer info,ByteBuffer datas, ByteBuffer oriData, boolean isMovePosition) throws Exception{
		int realLen;
		
		if(len instanceof Integer){
			realLen = ((Integer)len).intValue();
			if(realLen == 0){
				realLen = datas.limit()-datas.position();
			}
		}else{
			realLen = info.getInteger(len).intValue();
		}
		if(datas.remaining() < realLen){
			return null;
		}
		T r = (T)Const.byteArr2BcdStr(datas.array(), datas.arrayOffset()+datas.position(), realLen);
		if(isMovePosition){
			datas.position(datas.position()+realLen);
		}
		return r;
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
			int infoLen = this.getDataRealLen(info);
			
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
				infoLen = Const.bcdStr2ByteArr(val.toString()).length;
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
				data = Const.bcdStr2ByteArr(val.toString());
			}
			buffer.put(data, 0, Math.min(infoLen, data.length));
		}
		buffer.position(newPos);
	}
}
