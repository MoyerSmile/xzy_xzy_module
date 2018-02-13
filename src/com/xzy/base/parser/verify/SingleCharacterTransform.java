package com.xzy.base.parser.verify;

import java.nio.ByteBuffer;
import java.util.HashMap;

import com.xzy.base.Const;
import com.xzy.base.parser.ITransform;
import com.xzy.base.parser.XZYProtocolParser;

public class SingleCharacterTransform implements ITransform {
	private String name = null;
	private int escapeCharacter = 0;
	private HashMap<Integer,Integer> encodeMapping = new HashMap<Integer,Integer>();
	private HashMap<Integer,Integer> decodeMapping = new HashMap<Integer,Integer>();
	
	public SingleCharacterTransform(){
		
	}
	
	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}

	public void setParas(String[] paraArr){
		this.escapeCharacter = Const.parseInt(paraArr[0]);
		String[] arr;
		int a,b;
		for(int i=1;i<paraArr.length;i++){
			arr = paraArr[i].split("=");
			a = Const.parseInt(arr[0]);
			b = Const.parseInt(arr[1]);
			this.encodeMapping.put(a, b);
			this.decodeMapping.put(b, a);
		}
	}
	
	@Override
	public ByteBuffer encode(XZYProtocolParser parser, ByteBuffer fullData,int startOffset,int endOffset)  throws Exception{
		ByteBuffer destBuff = ByteBuffer.allocate(fullData.remaining()*2);
		int val;
		int pos=fullData.position();
		
		for(int i=0;i<startOffset;i++){
			destBuff.put(fullData.get(pos++));
		}
		for(int i=startOffset;i<fullData.remaining()-endOffset;i++){
			val = fullData.get(pos++)&0xff;
			if(encodeMapping.containsKey(val)){
				destBuff.put((byte)escapeCharacter);
				destBuff.put((byte)encodeMapping.get(val).intValue());
			}else{
				destBuff.put((byte)val);
			}
		}

		for(int i=fullData.remaining()-endOffset;i<fullData.remaining();i++){
			destBuff.put(fullData.get(pos++));
		}
		destBuff.flip();
		return destBuff;
	}

	@Override
	public ByteBuffer decode(XZYProtocolParser parser, ByteBuffer fullData,int startOffset,int endOffset) throws Exception{
		ByteBuffer destBuff = ByteBuffer.allocate(fullData.remaining());
		int val;
		boolean isEscape = false;
		
		int pos=fullData.position();
		for(int i=0;i<startOffset;i++){
			destBuff.put(fullData.get(pos++));
		}
		for(int i=startOffset;i<fullData.remaining()-endOffset;i++){
			val = fullData.get(pos++)&0xff;
			if(isEscape){
				isEscape = false;
				if(decodeMapping.containsKey(val)){
					destBuff.put((byte)decodeMapping.get(val).intValue());
				}else{
					throw new Exception("can't unescape data:"+Integer.toHexString(escapeCharacter)+" "+Integer.toHexString(val));
				}
			}else if(val == escapeCharacter){
				isEscape = true;
			}else{
				destBuff.put((byte)val);
			}
		}
		for(int i=fullData.remaining()-endOffset;i<fullData.remaining();i++){
			destBuff.put(fullData.get(pos++));
		}
		destBuff.flip();
		return destBuff;
	}

}
