package com.xzy.base.parser;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * @author Edmund
 *
 */
public interface ITransform {
	public String getName();
	public void setName(String name);
	
	public void setParas(String[] paras);
	
	public ByteBuffer encode(XZYProtocolParser parser,ByteBuffer fullData,int startOffset,int endOffset) throws Exception;
	public ByteBuffer decode(XZYProtocolParser parser,ByteBuffer fullData,int startOffset,int endOffset) throws Exception;
}
