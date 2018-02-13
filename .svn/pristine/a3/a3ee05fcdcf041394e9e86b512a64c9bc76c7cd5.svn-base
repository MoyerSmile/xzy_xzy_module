package com.xzy.base.server.db;

import com.xzy.base_c.BasicServer;

public class DbServer extends BasicServer {
	public static enum DB_TYPE{
		ORACLE_TYPE,
		MYSQL_TYPE,
		SQLSERVER_TYPE,
		TOTAL_NUM
	}
	
	private DB_TYPE dbType = null;
	
	@Override
	public boolean startServer() {
		
		return this.isRunning();
	}
	
	public DB_TYPE getDbType(){
		return this.dbType;
	}

}
