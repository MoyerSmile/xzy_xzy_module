package com.xzy.base.parser.verify;

import java.nio.ByteBuffer;

import com.xzy.base.Const;
import com.xzy.base.EncryptUtil;
import com.xzy.base.parser.IVerifyFunc;
import com.xzy.base.parser.Item;
import com.xzy.base.parser.XZYProtocolParser;
import com.xzy.base_c.InfoContainer;

/**
 * CRC检验支持，支持crc8和crc16
 * 参数paraArr: 1. crc8|crc16  2. crc开始位置，负数代表从未端倒推，正数代表从头端顺推  3. crc结束位置  4.如果存在，则是crc的初始值，不配置默认为0
 * @author Edmund
 *
 */
public class CrcVerifyFunc implements IVerifyFunc {
	private static enum VERIFY_TYPE{
		crc8,
		crc16,
		total
	}
	
	private String name = null;
	private VERIFY_TYPE type = VERIFY_TYPE.crc8;
	private Item verifyItem = null;
	private int crcInitialVal = 0;
	private boolean isStartOffsetFromStart = true;
	private int startOffset = 0;
	private boolean isEndOffsetFromStart = true;
	private int endOffset = 0;
	
	public CrcVerifyFunc(){
		
	}

	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return this.name;
	}
	public void setParas(Item item,String[] paraArr){
		this.verifyItem = item;
		this.type = VERIFY_TYPE.valueOf(paraArr[0].toLowerCase());
		if(paraArr[1].startsWith("-")){
			this.isStartOffsetFromStart = false;
		}
		this.startOffset = Const.parseInt(paraArr[1]);
		if(paraArr[2].startsWith("-")){
			this.isEndOffsetFromStart = false;
		}
		this.endOffset = Const.parseInt(paraArr[2]);
		if(paraArr.length > 3 && paraArr[3].trim().length() > 0){
			this.crcInitialVal = Const.parseInt(paraArr[3]);
		}
	}
	
	@Override
	public boolean existVerify(XZYProtocolParser parser,ByteBuffer fullData,InfoContainer info){
		return true;
	}
	
	@Override
	public void createVerifyVal(XZYProtocolParser parser,ByteBuffer fullData,InfoContainer info){
		if(fullData == null){
			return ;
		}
		int crc = this.crcInitialVal;
		
		int from = fullData.arrayOffset()+startOffset,to = fullData.arrayOffset()+endOffset;
		if(!this.isStartOffsetFromStart){
			from += fullData.remaining();
		}
		if(!this.isEndOffsetFromStart){
			to += fullData.remaining();
		}
		if(type==VERIFY_TYPE.crc8){
			crc = 0xFF&EncryptUtil.crc8((byte)crc, fullData.array(), from, to-from);
		}else if(type == VERIFY_TYPE.crc16){
			crc = EncryptUtil.crc16(crc, fullData.array(), from, to-from);
		}else{
			return ;
		}
		info.setInfo(this.verifyItem.getName(), new Integer(crc));
	}
	
	@Override
	public boolean verify(XZYProtocolParser parser, ByteBuffer fullData,InfoContainer info) {
		if(fullData == null){
			return true;
		}
		int crc = this.crcInitialVal;
		
		int from = fullData.arrayOffset()+startOffset,to = fullData.arrayOffset()+endOffset;
		if(!this.isStartOffsetFromStart){
			from += fullData.remaining();
		}
		if(!this.isEndOffsetFromStart){
			to += fullData.remaining();
		}
		if(type==VERIFY_TYPE.crc8){
			crc = 0xFF&EncryptUtil.crc8((byte)crc, fullData.array(), from, to-from);
		}else if(type == VERIFY_TYPE.crc16){
			crc = EncryptUtil.crc16(crc, fullData.array(), from, to-from);
		}else{
			return false;
		}
		if(crc != info.getInteger(verifyItem.getName())){
			return false;
		}
		
		return true;
	}

}
