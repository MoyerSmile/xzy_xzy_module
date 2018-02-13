package test.xzy.base;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.xzy.base.Util;
import com.xzy.base.server.log.LogRecordServer;
import com.xzy.base_c.InfoContainer;
import com.xzy.base_i.IServer;

import server.xzy.socket.XZYSocket;
import server.xzy.ui.IJSListener;
import server.xzy.ui.WebFrameServer;
import server.xzy.websocket.PrintCmdReleaser;
import server.xzy.websocket.XZYWebSocketServer;
import server.xzy.websocket.XZYWebSocketServer.XZYWebSocket;

public class WebSocketTest {
	private static final String testStr = "ÐìÐÂData.";
	@Test
	public void webSocketTest(){
		LogRecordServer.getSingleInstance().startServer();
		
		TestReleaser releaser = null;
		TestJs2JavaListener listener = null;
		XZYWebSocketServer.getSingleInstance().addPara("port","1979");
		XZYWebSocketServer.getSingleInstance().addPara("releaser",releaser = new TestReleaser(){
			public void execute(InfoContainer cmdInfo) {
				XZYWebSocket conn = (XZYWebSocket)cmdInfo.getInfo(XZYSocket.SOCKET_FLAG);
			
				if(result == null){
					result = new Integer(0);
				}
				Object cmd = cmdInfo.getInfo(XZYSocket.CMD_FLAG);
				Object data = cmdInfo.getInfo(XZYSocket.DATA_FLAG);
				if(cmd == XZYSocket.SOCKET_CONNECT_CMD){
					result = ((Integer)result).intValue()|0x01;
					JSONObject info = new JSONObject();
					try {
						info.put("data", testStr);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					((WebFrameServer)XZYWebSocketServer.getSingleInstance().getPara("server")).executeJavaScript("sendMsg", info);
				}else if(cmd == XZYSocket.SOCKET_DISCONNECT_CMD){
					result = ((Integer)result).intValue()|0x02;
				}else{
					if(data.equals(testStr)){
						result = ((Integer)result).intValue()|0x04;
						conn.sendMessage((String)data);
					}
				}
			}
		});
		XZYWebSocketServer.getSingleInstance().startServer();
		
		
		WebFrameServer server = new WebFrameServer();
		XZYWebSocketServer.getSingleInstance().addPara("server", server);
		server.setServerName("WebFrameTest");
		server.addPara("timeout", "6");
		server.addPara("listener", listener = new TestJs2JavaListener(){
			public Object js2Java(Object[] argv){
				if(argv[0].equals(testStr)){
					this.setInfo("result", argv[0]);
					((IServer)this.getInfo("server")).stopServer();
				}
				return null;
			}
		});
		listener.setInfo("server", server);
		server.addPara("url", this.getClass().getClassLoader().getResource("test/xzy/base/websocket.html").getFile());
		server.startServer();
		
		if(listener.getInfo("result") == null || ((Integer)releaser.result).intValue() != 7){
			this.print("Web Socket Test Failure."+listener.getInfo("result")+" "+((Integer)releaser.result));
			Assert.fail("Web Socket Test Failure");
		}
		this.print("Web Socket Test Success");
	}

	private void print(String message){
		System.out.println(this.getClass().getName()+":"+message);
	}
}
