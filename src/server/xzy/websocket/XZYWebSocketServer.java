package server.xzy.websocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocketHandler;

import server.xzy.socket.IReleaser;
import server.xzy.socket.XZYConnectInfo;
import server.xzy.socket.XZYReadSelectorTask.RegistInfo;
import server.xzy.socket.XZYSocket;

import com.hp.hpl.sparta.xpath.ThisNodeTest;
import com.xzy.base_c.InfoContainer;


public class XZYWebSocketServer extends XZYSocket {
	private ArrayList<XZYWebSocket> sessionList = new ArrayList<XZYWebSocket>();

	private Server server = null;
	private IReleaser releaser = null;
	
	private static XZYWebSocketServer singleInstance = new XZYWebSocketServer();
	public static XZYWebSocketServer getSingleInstance(){
		return singleInstance;
	}
	
	
	@Override
	public boolean startServer() {
		if(this.getStringPara("ip") == null){
			this.addPara("ip", "0.0.0.0");
		}
		InetSocketAddress netAddress = new InetSocketAddress(this.getStringPara("ip"),
				this.getIntegerPara("port"));
		this.server = new Server(netAddress);
		this.server.setHandler(new WebSocketEndPointServer());
		
		Object tempObj = null;
		try{
			tempObj = this.getPara("releaser");
			if(tempObj instanceof IReleaser){
				this.releaser = (IReleaser)tempObj;
			}else{
				this.releaser = (IReleaser)Class.forName(tempObj.toString()).newInstance();
				this.releaser.init(this);
			}
			this.server.start();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}

		this.isRun = true;
		return this.isRunning();
	}
	
	public void stopServer(){
		if(this.server != null){
			try{
				this.server.stop();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		super.stopServer();
	}

	public synchronized void sendToAll(String message) throws Exception{
		XZYWebSocket socket;
		for(Iterator<XZYWebSocket> itr = this.sessionList.iterator();itr.hasNext();){
			socket = itr.next();
			socket.sendMessage(message);
		}
	}
	public synchronized void sendToAll(byte[] data,int offset,int len) throws Exception{
		XZYWebSocket socket;
		for(Iterator<XZYWebSocket> itr = this.sessionList.iterator();itr.hasNext();){
			socket = itr.next();
			socket.sendMessage(data,offset,len);
		}
	}
	
	public synchronized XZYWebSocket[] getAllConns(){
		XZYWebSocket[] arr = new XZYWebSocket[this.sessionList.size()];
		this.sessionList.toArray(arr);
		return arr;
	}

	private synchronized void addFleetyWebSocket(XZYWebSocket webSocket){
		this.sessionList.add(webSocket);
	}
	private synchronized void removeFleetyWebSocket(XZYWebSocket webSocket){
		this.sessionList.remove(webSocket);
	}
	private void triggerMessage(InfoContainer info){
		this.releaser.execute(info);
	}
	
	private class WebSocketEndPointServer extends WebSocketHandler{
		@Override
		public WebSocket doWebSocketConnect(HttpServletRequest request, String arg1) {
			return new XZYWebSocket(request.getRequestURI(),request.getRemoteHost(),request.getRemotePort());
		}
	}

	public class XZYWebSocket extends InfoContainer implements OnTextMessage, OnBinaryMessage{
		private Connection conn = null;
		private String flag = null;
		private String ip = null;
		private int port = 0;
		private String desc = null;
		
		public XZYWebSocket(String uri,String ip,int port){
			this.flag = uri;
			this.ip = ip;
			this.port = port;
			this.desc = uri+"("+ip+":"+port+")";
		}
		
		public void close(){
			if(this.conn != null){
				this.conn.disconnect();
			}
		}
		
		@Override
		public void onClose(int arg0, String arg1) {
			XZYWebSocketServer.this.removeFleetyWebSocket(this);
			InfoContainer msgPara = new InfoContainer();
			msgPara.setInfo(XZYSocket.CMD_FLAG,XZYSocket.SOCKET_DISCONNECT_CMD);
			msgPara.setInfo(XZYSocket.SOCKET_FLAG, this);
			XZYWebSocketServer.this.triggerMessage(msgPara);
		}

		@Override
		public void onOpen(Connection conn) {
			this.conn = conn;
			XZYWebSocketServer.this.addFleetyWebSocket(this);

			InfoContainer msgPara = new InfoContainer();
			msgPara.setInfo(XZYSocket.CMD_FLAG,XZYSocket.SOCKET_CONNECT_CMD);
			msgPara.setInfo(XZYSocket.SOCKET_FLAG, this);
			XZYWebSocketServer.this.triggerMessage(msgPara);
		}

		@Override
		public void onMessage(String msg) {
			InfoContainer msgPara = new InfoContainer();
			msgPara.setInfo(XZYSocket.DATA_FLAG, msg);
			msgPara.setInfo(XZYSocket.SOCKET_FLAG, this);
			XZYWebSocketServer.this.triggerMessage(msgPara);
		}

		@Override
		public void onMessage(byte[] data, int offset, int len) {
			ByteBuffer buff = ByteBuffer.wrap(data, offset, len);
			InfoContainer msgPara = new InfoContainer();
			msgPara.setInfo(XZYSocket.DATA_FLAG, buff);
			msgPara.setInfo(XZYSocket.SOCKET_FLAG, this);
			XZYWebSocketServer.this.triggerMessage(msgPara);
		}
		
		public synchronized boolean sendMessage(String msg){
			try{
				this.conn.sendMessage(msg);
			}catch(Exception e){
				e.printStackTrace();
				if(this.conn != null){
					this.conn.disconnect();
				}
				return false;
			}
			return true;
		}
		public boolean sendMessage(byte[] data,int offset,int len){
			try{
				this.conn.sendMessage(data, offset, len);
			}catch(Exception e){
				e.printStackTrace();
				if(this.conn != null){
					this.conn.disconnect();
				}
				return false;
			}
			return true;
		}
		
		public String getFlag(){
			return this.flag;
		}
		public String getIP(){
			return this.ip;
		}
		public int getPort(){
			return this.port;
		}
		public String toString(){
			return this.desc;
		}
	}
	
	
	public static void main(String[] argv) throws Exception{
		XZYWebSocketServer.getSingleInstance().addPara("port","1979");
		XZYWebSocketServer.getSingleInstance().addPara("releaser",PrintCmdReleaser.class.getName());
		XZYWebSocketServer.getSingleInstance().startServer();
	}


	@Override
	protected XZYConnectInfo registerSocketChannel(SocketChannel channel) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void registerSocket(RegistInfo rInfo, Selector selector) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void destroySocket(XZYConnectInfo connInfo, SOCKET_CLOSE_CODE code) {
		// TODO Auto-generated method stub
		
	}
}
