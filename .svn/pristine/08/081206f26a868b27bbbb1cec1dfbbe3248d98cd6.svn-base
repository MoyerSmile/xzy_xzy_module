package com.xzy.base.parser;

import java.nio.ByteBuffer;

import com.xzy.base_c.InfoContainer;

public class StringItem<T> extends Item<T> {
	String charset = null;
	public StringItem(XZYProtocolParser parser,ITEM_TYPE type){
		super(parser,type);
	}

	public void setCharset(String charset){
		this.charset = charset;
	}
	public String getCharset(){
		if(this.charset != null){
			return this.charset;
		}
		if(this.parser.getCharset() != null){
			return this.parser.getCharset();
		}
		return "GBK";
	}
	
	public T createValue(InfoContainer info,ByteBuffer datas, ByteBuffer oriData,boolean isMovePosition) throws Exception{
		int realLen;
		
		boolean needFindEndFlag = false;
		if(len instanceof Integer){
			realLen = ((Integer)len).intValue();
			if(realLen == 0){
				needFindEndFlag = true;
			}
		}else{
			realLen = info.getInteger(len).intValue();
		}

		T r = null;
		if(needFindEndFlag){
			for(int i=datas.position();i < datas.limit();i++){
				if(datas.get(i) == this.parser.getStringEndByteFlag()){
					r = (T)this.createString(datas.array(), datas.arrayOffset()+datas.position(), i-datas.position());
						
					if(isMovePosition){
						datas.position(i+1);
					}
					return r;
				}
			}

			r = (T)this.createString(datas.array(), datas.arrayOffset()+datas.position(), datas.remaining());	
			if(isMovePosition){
				datas.position(datas.limit());
			}
		}else{
			if(datas.remaining() < realLen){
				return null;
			}
			r = (T)this.createString(datas.array(), datas.arrayOffset()+datas.position(), realLen);
			if(isMovePosition){
				datas.position(datas.position()+realLen);
			}
		}
		return r;
	}
	
	private String createString(byte[] data,int offset,int len) throws Exception{
		while(len > 0 && data[offset + len - 1] == 0){
			len --;
		}
		return new String(data,offset,len,this.getCharset());
	}
	

	public int getInfoLen(InfoContainer info) throws Exception{
		if(this.len instanceof Integer){
			if(((Integer)this.len).intValue() == 0){
				return this.getDataRealLen(info)+1;
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
	private int getDataRealLen(InfoContainer info) throws Exception{
		if(info == null){
			return 0;
		}
		Object val = info.getInfo(this.name);
		int infoLen ;
		if(val == null){
			infoLen = 0;
		}else{
			if(val instanceof byte[]){
				infoLen = ((byte[])val).length;
			}else{
				infoLen = val.toString().getBytes(this.getCharset()).length;
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
		
		int infoLen = this.getInfoLen(info),fillLen = 0;
		int newPos = buffer.position() + infoLen;
		fillLen = infoLen;
		if(this.len instanceof Integer && ((Integer)this.len).intValue() == 0){
			fillLen --;
		}
		if(val != null){
			byte[] data = null;
			if(val instanceof byte[]){
				data = (byte[])val;
			}else{
				data = val.toString().getBytes(this.getCharset());
			}
			buffer.put(data, 0, Math.min(fillLen, data.length));
		}
		buffer.position(newPos);
	}
}
