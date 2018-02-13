package com.xzy.base.parser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.xzy.base.Const;
import com.xzy.base_c.InfoContainer;

public class NumberItem<T> extends Item<T> {
	ByteOrder byteOrder = null;
	public NumberItem(XZYProtocolParser parser,ITEM_TYPE type){
		super(parser,type);
	}

	public void setByteOrder(ByteOrder byteOrder){
		this.byteOrder = byteOrder;
	}
	public ByteOrder getByteOrder(){
		if(this.byteOrder != null){
			return this.byteOrder;
		}
		return this.parser.getByteOrder();
	}
	
	public T createValue(InfoContainer info,ByteBuffer datas, ByteBuffer oriData, boolean isMovePosition) throws Exception{
		datas.order(this.getByteOrder());
		int realLen = 0;
		if(len instanceof Integer){
			realLen = ((Integer)len).intValue();
		}else{
			realLen = info.getInteger(len).intValue();
		}

		if(datas.remaining() < realLen){
			return null;
		}
		
		long v = Const.createLongValue(datas.array(), datas.arrayOffset()+datas.position(), realLen, this.getByteOrder(),this.type == ITEM_TYPE.INT?true:false);
		if(isMovePosition){
			datas.position(datas.position()+realLen);
		}
		if(realLen <= 4){
			return (T)new Integer((int)v);
		}else{
			return (T)new Long(v);
		}
	}
	

	public int getInfoLen(InfoContainer info) throws Exception{
		if(this.len instanceof Integer){
			return ((Integer)this.len).intValue();
		}else if(info.getInfo(this.len) != null){
			return info.getInteger(len).intValue();
		}else{
			Object val = null;
			val = info.getInfo(this.name);
			int infoLen ;
			if(val == null){
				infoLen = 0;
			}else{
				if(val instanceof byte[]){
					infoLen = ((byte[])val).length;
				}else if(val instanceof Long){
					infoLen = 8;
				}else{
					infoLen = 4;
				}
			}
			info.setInfo(len, new Integer(infoLen));
			return infoLen;
		}
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
				buffer.put(data, 0, Math.min(infoLen, data.length));
			}else{
				ByteBuffer temp = ByteBuffer.allocate(8);
				temp.order(this.getByteOrder());
				temp.putLong(Const.parseLong(val.toString()));
				if(this.getByteOrder() == ByteOrder.LITTLE_ENDIAN){
					buffer.put(temp.array(), 0, infoLen);
				}else{
					buffer.put(temp.array(), 8-infoLen, infoLen);
				}
			}
		}
		buffer.position(newPos);
	}
}
