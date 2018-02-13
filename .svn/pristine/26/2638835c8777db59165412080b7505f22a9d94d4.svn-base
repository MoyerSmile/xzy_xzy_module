package com.xzy.base.parser;

import java.nio.ByteBuffer;

import com.xzy.base_c.BasicServer;
import com.xzy.base_c.InfoContainer;

public abstract class IParser extends BasicServer {
	public static final Object ORI_DATA_FLAG = new Object();

	public abstract int getCapacity();
	public abstract int getMaxCapacity();

	public abstract String getMsgName();
	public abstract String getSeriralDisposeFieldName();
	
	public abstract InfoContainer[] decodeData(ByteBuffer dataBuffer) throws Exception;
	public abstract ByteBuffer encodeData(InfoContainer info) throws Exception;
}
