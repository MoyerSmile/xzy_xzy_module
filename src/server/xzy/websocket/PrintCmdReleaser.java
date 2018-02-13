package server.xzy.websocket;

import java.nio.ByteBuffer;

import com.xzy.base.Const;
import com.xzy.base_c.InfoContainer;

import server.xzy.socket.IReleaser;
import server.xzy.socket.XZYSocket;
import server.xzy.websocket.XZYWebSocketServer.XZYWebSocket;


public class PrintCmdReleaser implements IReleaser {
	private XZYSocket server = null;
	public void init(XZYSocket server) {
		this.server = server;
	}

	public void execute(InfoContainer cmdInfo) {
		XZYWebSocket conn = (XZYWebSocket)cmdInfo.getInfo(XZYSocket.SOCKET_FLAG);
		Object msg = cmdInfo.getInfo(XZYSocket.CMD_FLAG);
		Object data = cmdInfo.getInfo(XZYSocket.DATA_FLAG);
		if(msg == XZYSocket.SOCKET_CONNECT_CMD){
			this.server.info(msg+":"+conn);
		}else if(msg == XZYSocket.SOCKET_DISCONNECT_CMD){
			this.server.info(msg+":"+conn);
		}else{
			if(data == null){
				return ;
			}
			if(data instanceof String){
				this.server.info(conn+":text="+data);
				conn.sendMessage((String)data);
			}else{
				ByteBuffer buff = (ByteBuffer)data;
				this.server.info(conn+":data="+Const.byteArrToHexString(buff.array(),buff.arrayOffset(),buff.remaining()));
				conn.sendMessage(buff.array(), buff.arrayOffset(), buff.remaining());
			}
		}
	}

}
