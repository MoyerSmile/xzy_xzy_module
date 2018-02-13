package server.xzy.diagnos;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONObject;

import com.xzy.base.Const;
import com.xzy.base.Util;
import com.xzy.base.server.log.LogRecordServer;
import com.xzy.base_c.BasicServer;
import com.xzy.base_c.InfoContainer;

import server.xzy.socket.IReleaser;
import server.xzy.socket.XZYConnectInfo;
import server.xzy.socket.XZYSocket;
import server.xzy.socket.XZYSocketClient;

public class DiagnosServer extends BasicServer implements Runnable {
	public static final Object MDT_FLAG = "mdt_id";
	public static final Object IP_FLAG = "ip";
	public static final Object PORT_FLAG = "port";
	public static final Object KEY_FLAG = "key";
	public static final Object EXEC_FLAG = "exec";
	public static final Object CONNECT_CYCLE_FLAG = "connect_cycle";
	
	private XZYSocketClient client = null;
	private boolean isStop = false;
	private boolean isLogin = false;
	private int connectCycle = 10*60*1000;
	
	private IShellExec shellExec = null;
	private String mdtId = null;
	
	public DiagnosServer(){
		
	}
	
	public boolean startServer() {
		if(this.isRunning()) {
			return true;
		}
		if(this.getIntegerPara(CONNECT_CYCLE_FLAG) != null) {
			this.connectCycle = this.getIntegerPara(CONNECT_CYCLE_FLAG).intValue()*1000;
		}
		
		this.mdtId = this.getStringPara(MDT_FLAG);
		if(this.mdtId == null || this.mdtId.trim().length() == 0) {
			return false;
		}
		Object temp = this.getPara(EXEC_FLAG);
		if(temp != null) {
			if(temp instanceof IShellExec) {
				this.shellExec = (IShellExec)temp;
			}else {
				try {
					this.shellExec = (IShellExec)Class.forName(temp.toString()).newInstance();
				}catch(Exception e) {
					this.error("init failure", e);
				}
			}
		}
		if(this.shellExec == null) {
			this.shellExec = new DefaultShellExec();
		}
		this.isStop = false;
		this.isLogin = false;
		
		new Thread(this).start();
		
		this.isRun = true;
		return this.isRunning();
	}
	
	public void stopServer(){
		super.stopServer();
		
		this.isStop = true;
		if(this.client != null){
			this.client.stopServer();
		}
	}
	
	
	public void run(){
		CmdReader reader = new CmdReader();
		CmdReleaser releaser = new CmdReleaser();
		
		while(!this.isStop){
			if(this.client != null && this.client.isConnected() && this.isLogin){
				
				
			}else if(this.mdtId != null){
				if(this.client != null){
					this.client.stopServer();
				}
				
				this.isLogin = false;
				this.client = new XZYSocketClient();
				this.client.addPara(XZYSocketClient.SERVER_IP_FLAG, this.getStringPara(IP_FLAG));
				this.client.addPara(XZYSocketClient.SERVER_PORT_FLAG, this.getStringPara(PORT_FLAG));
				this.client.addPara(XZYSocketClient.CMD_READER_FLAG, reader);
				this.client.addPara(XZYSocketClient.CMD_RELEASER_FLAG, releaser);
				this.client.addPara(XZYSocketClient.MAX_CACH_SIZE_FLAG, new Integer(64));
				this.client.addPara(XZYSocketClient.AUTO_CONNECT_CYCLE_FLAG, (30*60*1000)+"");
				this.client.startServer();
			}
			
			synchronized(this){
				try {
					this.wait(this.connectCycle);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	

	
	public static ByteBuffer encodeData(JSONObject json) throws Exception{
		if(json == null){
			return null;
		}
		byte[] data = json.toString().getBytes("GBK");
		ByteBuffer buff = ByteBuffer.allocate(7 + 2 + 1 + data.length + 7);
		buff.order(ByteOrder.BIG_ENDIAN);
		
		byte enc = (byte)(1+Math.random()*255);
		for(int i=0;i<data.length;i++){
			data[i] = (byte)(data[i] ^ enc);
		}
		
		buff.put("DIAGNOS".getBytes());
		buff.putShort((short)(1+data.length));
		buff.put(enc);
		buff.put(data);
		buff.put("SONGAID".getBytes());
		
		buff.flip();
		
		return buff;
	}
	
	private class CmdReleaser implements IReleaser{

		@Override
		public void init(XZYSocket arg0) {
			
		}

		@Override
		public void execute(InfoContainer cmdInfo) {
			XZYConnectInfo connInfo = (XZYConnectInfo)cmdInfo.getInfo(XZYSocket.SOCKET_FLAG);
			Object cmd = cmdInfo.getInfo(XZYSocket.CMD_FLAG);
			if(cmd == XZYSocket.SOCKET_CONNECT_CMD){
				try {
					this.sendLoginRequest(connInfo);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(cmd == XZYSocket.SOCKET_DISCONNECT_CMD){
				DiagnosServer.this.client.stopServer();
				DiagnosServer.this.client = null;
			}else{
				String jsonStr = cmdInfo.getString(XZYSocket.DATA_FLAG);
				System.out.println("Diagnos:"+jsonStr);
				
				String id = null;
				try{
					JSONObject json = new JSONObject(jsonStr);
					JSONObject head = json.getJSONObject("head");
					JSONObject body = json.getJSONObject("body");
					
					String msg = head.getString("msg");
					
					if(msg.equals("login_res")){
						DiagnosServer.this.isLogin = true;
						if(!body.getBoolean("has_task")){
							connInfo.destroy();
						}
					}else if(msg.equals("shell_cmd")){
						id = body.getString("id");
						this.executeShellCmd(connInfo,body);
					}else if(msg.equals("file_upload")){
						id = body.getString("id");
						this.executeFileUploadCmd(connInfo,body);
					}
					
				}catch(Throwable e){
					try {
						if(id != null) {
							sendUploadStatus(connInfo,id,"error",Const.exception2Str(e),0);
						}
					}catch(Exception er) {}
					Util.sleep(2000);
					e.printStackTrace();
					connInfo.destroy();
				}
			}
		}
		
		private void sendLoginRequest(XZYConnectInfo connInfo) throws Exception{
			JSONObject json = new JSONObject();
			JSONObject head = new JSONObject();
			JSONObject body = new JSONObject();
			json.put("head", head);
			json.put("body", body);
			head.put("msg", "login");
			//TO DO
			head.put("mdtId", mdtId);
			body.put("app_version", "unknown");
			body.put("os_version", "unknown");
			body.put("key", DiagnosServer.this.getStringPara(KEY_FLAG));
			
			connInfo.writeData(DiagnosServer.encodeData(json));
		}
		
		private void executeShellCmd(XZYConnectInfo connInfo,JSONObject body) throws Exception{
			String id = body.getString("id");
			String cmd = body.getString("cmd");
			int timeout = 10000;
			if(!body.isNull("max_duration")){
				timeout = body.getInt("max_duration");
			}
			String context = null;
			if(!body.isNull("context")){
				context = body.getString("context");
			}
			
			String res = DiagnosServer.this.shellExec.execute(cmd,timeout,context);
			
			JSONObject json = new JSONObject();
			JSONObject head = new JSONObject();
			body = new JSONObject();
			json.put("head", head);
			json.put("body", body);
			head.put("msg", "shell_cmd_res");
			head.put("mdtId", mdtId);
			body.put("id", id);
			body.put("res_text", res);

			connInfo.writeData(DiagnosServer.encodeData(json));
		}
		
		private void executeFileUploadCmd(XZYConnectInfo connInfo,JSONObject body) throws Exception{
			String id = body.getString("id");
			String path = body.getString("path");
			String ip = body.getString("ip");
			int port = body.getInt("port");
			String account = body.getString("account");
			String pwd = body.getString("pwd");
			String dir = null;
			if(!body.isNull("dir")){
				dir = body.getString("dir");
			}
			
			File f = new File(path);
			if(!f.exists() || !f.isFile()){
				sendUploadStatus(connInfo,id,"error","file not exist. "+path,0);
				return ;
			}
			FTPClient ftp = null;
			try {
				ftp = new FTPClient();
			}catch(Exception e) {
				sendUploadStatus(connInfo,id,"error",Const.exception2Str(e),0);
				return ;
			}
			try{
				ftp.connect(ip, port);
				ftp.setSoTimeout(10*60*1000);
			}catch(Exception e){
				e.printStackTrace();
				sendUploadStatus(connInfo,id,"error","connect fail. "+ip+":"+port,0);
				return ;
			}
			boolean success = ftp.login(account, pwd);
			if(!success){
				sendUploadStatus(connInfo,id,"error","login fail. account="+account+" pwd="+pwd,0);
				ftp.disconnect();
				return ;
			}
			ftp.enterLocalPassiveMode();
			if(dir != null){
				if(!ftp.changeWorkingDirectory(dir)){
					sendUploadStatus(connInfo,id,"error","change work dir fail. dir=" + dir,0);
					ftp.disconnect();
					return ;
				}
			}
			
			BufferedInputStream in = null;
			try{
				sendUploadStatus(connInfo,id,"pend",null,0);
				in = new BufferedInputStream(new FileInputStream(f));
				ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
				if(!ftp.storeFile(mdtId+"-"+f.getName(), in)){
					sendUploadStatus(connInfo,id,"error","store file error."+path,0);
				}else{
					sendUploadStatus(connInfo,id,"success",null,100);
				}
			}catch(Exception e){
				e.printStackTrace();
				sendUploadStatus(connInfo,id,"error","upload file error."+path,0);
			}finally{
				if(in != null){
					in.close();
				}
				
				ftp.disconnect();
			}
		}
		
		private void sendUploadStatus(XZYConnectInfo connInfo,String id,String action,String reason,int progress) throws Exception{
			JSONObject json = new JSONObject();
			JSONObject head = new JSONObject();
			JSONObject body = new JSONObject();
			json.put("head", head);
			json.put("body", body);
			head.put("msg", "file_upload_res");
			head.put("mdtId", mdtId);
			if(action.equals("error")){
				head.put("result", false);
			}
			if(reason != null){
				head.put("reason", reason);
			}
			body.put("id", id);
			body.put("action", action);
			body.put("progress", progress);
			
			connInfo.writeData(DiagnosServer.encodeData(json));
		}
	}

	public static void main(String[] argv) throws Exception{
		LogRecordServer.getSingleInstance().startServer();
		
		DiagnosServer diag = new DiagnosServer();
		diag.addPara(IP_FLAG, "www.sanly.com.cn");
		diag.addPara(PORT_FLAG, "11792");
		diag.addPara(MDT_FLAG, "edmund");
		diag.addPara(KEY_FLAG, "cp5");
		diag.startServer();
	}
}
