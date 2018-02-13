package com.xzy.base.parser;

import java.nio.ByteBuffer;

import com.xzy.base_c.InfoContainer;

public interface IVerifyFunc {
	public void setName(String name);
	public String getName();
	public void setParas(Item item,String[] paraArr);
	
	public boolean existVerify(XZYProtocolParser parser,ByteBuffer fullData,InfoContainer info);
	public void createVerifyVal(XZYProtocolParser parser,ByteBuffer fullData,InfoContainer info);
	public boolean verify(XZYProtocolParser parser,ByteBuffer fullData,InfoContainer info);
}
