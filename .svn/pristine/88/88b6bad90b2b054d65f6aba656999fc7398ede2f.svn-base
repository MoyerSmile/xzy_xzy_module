package test.xzy.base;

import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.xzy.base.Const;
import com.xzy.base.EncryptUtil;
import com.xzy.base.Util;
import com.xzy.base.parser.XZYProtocolParser;
import com.xzy.base.server.container.XZYActionContainerServer;
import com.xzy.base.server.event.DefaultEventCenterServer;
import com.xzy.base.server.log.LogRecordServer;
import com.xzy.base_c.InfoContainer;
import com.xzy.base_i.IEvent;
import com.xzy.base_i.IEventResponse;

import server.xzy.socket.DefaultReleaser;
import server.xzy.socket.XZYConnectInfo;
import server.xzy.socket.XZYSocket;
import server.xzy.socket.XZYSocketClient;
import server.xzy.socket.XZYSocketServer;
import server.xzy.socket.help.ActionContainerReleaser;

public class XZYSocketTest {
	@Test
	public void XZYSocketServerTest() throws Exception{
		LogRecordServer.getSingleInstance().startServer();
		
		TestReleaser releaser = new TestReleaser(){
			public void init(XZYSocket socketServer){
				this.result = new Boolean[]{false,false,false,false};
			}
			public void execute(InfoContainer cmdInfo) {
				XZYConnectInfo connInfo = (XZYConnectInfo)cmdInfo.getInfo(XZYSocket.SOCKET_FLAG);
				Object cmd = cmdInfo.getInfo(XZYSocket.CMD_FLAG);
				if(cmd == XZYSocket.SOCKET_CONNECT_CMD){
					((Boolean[])this.result)[0] = Boolean.TRUE;
				}else if(cmd == XZYSocket.SOCKET_DISCONNECT_CMD){
					((Boolean[])this.result)[2] = Boolean.TRUE;
				}else if(cmd.equals(new Integer(0x8001))){
					if(!cmdInfo.getInfo("name").equals("–Ï–¬≤‚ ‘")){
						print("name error"+cmdInfo);
						
						return ;
					}
					if(!cmdInfo.getInfo("responseFlowId").equals(0x7d20)){
						print("responseFlowId error"+cmdInfo);
						return ;
					}
					if(!cmdInfo.getInfo("bcd").equals("10203040")){
						print("bcd error"+cmdInfo);
						return ;
					}
					if(!cmdInfo.getInfo("deviceId").equals("102030135790")){
						print("deviceId error"+cmdInfo);
						return ;
					}
					if(!cmdInfo.getInfo("responseMessageId").equals(0x9001)){
						print("responseMessageId error"+cmdInfo);
						return ;
					}
					if(!cmdInfo.getInfo("result").equals(1)){
						print("result error"+cmdInfo);
						return ;
					}
					if(!cmdInfo.getInfo("flowId").equals(0x8010)){
						print("flowId error"+cmdInfo);
						return ;
					}
					if(!cmdInfo.getInfo("longid").equals(0x30405060l)){
						print("longid error"+cmdInfo);
						return ;
					}
					((Boolean[])this.result)[1] = Boolean.TRUE;
				}else if(cmd.equals(new Integer(0x8015))){
					String val = ((InfoContainer)para).contain(cmdInfo);
					if(val != null){
						print("0x8015 msg info error. "+val+" "+cmdInfo);
					}else{
						((Boolean[])this.result)[3] = Boolean.TRUE;
					}
				}
			}
		};

		InfoContainer sendInfo = new InfoContainer();
		releaser.para = sendInfo;
		
		XZYSocketServer server = new XZYSocketServer();
		server.setServerName("SocketServer Test");
		server.addPara(XZYSocketServer.SERVER_PORT_FLAG, "23456");
		server.addPara(XZYSocketServer.CMD_PARSER_FLAG, XZYProtocolParser.class.getName());
		server.addPara(XZYSocketServer.CMD_RELEASER_FLAG, releaser);
		server.addPara(XZYSocketServer.PROTOCOL_TEMPLATE_FLAG, this.getClass().getClassLoader().getResource("test/xzy/base/head_tail_test.xml").getPath());
		server.addPara(XZYSocketServer.PROTOCOL_TEMPLATE_DIR_FLAG,"receiver");
		server.addPara(XZYSocketServer.TIME_OUT_FLAG, "10");
		server.startServer();

		Socket socket = new Socket("127.0.0.1",23456);
		OutputStream out = socket.getOutputStream();
		
		ByteBuffer datas = ByteBuffer.allocate(1024);
		datas.order(ByteOrder.BIG_ENDIAN);
		datas.put((byte)0x7e);
		datas.putShort((short)0x8001);
		datas.putShort((short)0);
		datas.put(Const.bcdStr2ByteArr("102030135790"));
		datas.putShort((short)0x8010);

		datas.putShort((short)0x7d01);
		datas.put((byte)0x20);
		
		String name = "–Ï–¬≤‚ ‘";
		datas.put((byte)name.getBytes("GBK").length);
		datas.put(name.getBytes("GBK"));
		
		datas.putShort((short)0x9001);
		datas.put((byte)0x01);

		datas.put((byte)4);
		datas.put(Const.bcdStr2ByteArr("10203040"));
		
		datas.putLong(0x30405060);
		
		int crc = EncryptUtil.crc8(0, datas.array(), 1, datas.position() - 1)^0x01;
		datas.put((byte)crc);
		datas.put((byte)0x7e);
		
		out.write(datas.array(), 0, datas.position());
		
		Util.sleep(1000);
		socket.close();
		Util.sleep(1000);
		
		Boolean[] rr = (Boolean[])releaser.result;
		for(int i=0;i<3;i++){
			if(!rr[i].booleanValue()){
				this.print("XZYSocketServer Test Failure! "+i);
				Assert.fail("XZYSocketServer Test Failure! "+i);
			}
		}
		

		XZYSocketClient xzySocket = new XZYSocketClient();
		xzySocket.setServerName("SocketClient");
		xzySocket.addPara(XZYSocketClient.SERVER_IP_FLAG, "127.0.0.1");
		xzySocket.addPara(XZYSocketClient.SERVER_PORT_FLAG, "23456");
		xzySocket.addPara(XZYSocketClient.CMD_PARSER_FLAG, XZYProtocolParser.class.getName());
		xzySocket.addPara(XZYSocketServer.PROTOCOL_TEMPLATE_FLAG, this.getClass().getClassLoader().getResource("test/xzy/base/head_tail_test.xml").getPath());
		xzySocket.addPara(XZYSocketClient.CMD_RELEASER_FLAG, releaser = new TestReleaser(){
			public void init(XZYSocket socketServer){
				this.result = new Boolean[]{false,false,false};
			}
			public void execute(InfoContainer cmdInfo) {
					XZYConnectInfo connInfo = (XZYConnectInfo)cmdInfo.getInfo(XZYSocket.SOCKET_FLAG);
					Object cmd = cmdInfo.getInfo(XZYSocket.CMD_FLAG);
					print("SocketClient:"+cmd);
					if(cmd == XZYSocket.SOCKET_CONNECT_CMD){
						((Boolean[])this.result)[0] = Boolean.TRUE;
					}else if(cmd == XZYSocket.SOCKET_DISCONNECT_CMD){
						((Boolean[])this.result)[2] = Boolean.TRUE;
					}
				}
			});
		xzySocket.startServer();
		
		List<InfoContainer> list = new LinkedList<InfoContainer>(),tempList;
		InfoContainer tempInfo,tempSubInfo;
		sendInfo.setInfo("msg", 0x8015);
		sendInfo.setInfo("info_x", "infos");
		sendInfo.setInfo("info_num", new Integer(2));
		sendInfo.setInfo("group_1", list);
		
		for(int i=0;i<2;i++){
			tempInfo = new InfoContainer();
			tempInfo.setInfo("r", i+1);
			tempInfo.setInfo("bcd", "456"+i);
			
			tempList = new LinkedList<InfoContainer>();
			for(int j=0;j<10;j++){
				tempSubInfo = new InfoContainer();
				tempSubInfo.setInfo("info_y", "–Ï–¬≤‚ ‘_"+(j+100));
				tempList.add(tempSubInfo);
			}
			
			tempInfo.setInfo("group_1_1", tempList);
			
			list.add(tempInfo);
		}
		list = new LinkedList<InfoContainer>();
		sendInfo.setInfo("group_2", list);
		byte[] a;
		for(int i=0;i<2;i++){
			tempInfo = new InfoContainer();
			a = new byte[20];
			a[0] = a[1] = (byte)i;
			tempInfo.setInfo("r", a);
			list.add(tempInfo);
		}

		list = new LinkedList<InfoContainer>();
		sendInfo.setInfo("group_3", list);
		for(int i=0;i<5;i++){
			tempInfo = new InfoContainer();

			tempInfo.setInfo("id", new Integer(i+1));
			tempInfo.setInfo("val", ("sdsdsfds_"+i).getBytes());
			
			list.add(tempInfo);
		}
		xzySocket.sendData(sendInfo);
		
		Util.sleep(1000);
		
		if(!rr[3].booleanValue()){
			this.print("XZYSocketClient Test Failure!");
			Assert.fail("XZYSocketClient Test Failure!");
		}
		
		this.print("XZYSocketServer Test Success!");
	}
	
	@Test
	public void actionContainerTest() throws Exception{
		ActionContainerReleaser serverContainer = new ActionContainerReleaser();
		XZYSocketServer server = new XZYSocketServer();
		server.setServerName("SocketServer Test");
		server.addPara(XZYSocketServer.SERVER_PORT_FLAG, "23457");
		server.addPara(XZYSocketServer.CMD_PARSER_FLAG, XZYProtocolParser.class.getName());
		server.addPara(XZYActionContainerServer.CFG_PATH_KEY_FLAG, this.getClass().getClassLoader().getResource("test/xzy/base/action_mapping_example.xml").getPath());
		serverContainer.init(server);
		server.addPara(XZYSocketServer.CMD_RELEASER_FLAG, serverContainer);
		server.addPara(XZYSocketServer.PROTOCOL_TEMPLATE_FLAG, this.getClass().getClassLoader().getResource("test/xzy/base/head_tail_test.xml").getPath());
		server.addPara(XZYSocketServer.PROTOCOL_TEMPLATE_DIR_FLAG,"receiver");
		server.addPara(XZYSocketServer.TIME_OUT_FLAG, "10");
		server.startServer();
		

		ActionContainerReleaser clientContainer = new ActionContainerReleaser();
		XZYSocketClient xzySocket = new XZYSocketClient();
		xzySocket.setServerName("SocketClient");
		xzySocket.addPara(XZYActionContainerServer.CFG_PATH_KEY_FLAG, this.getClass().getClassLoader().getResource("test/xzy/base/action_mapping_example.xml").getPath());
		xzySocket.addPara(XZYSocketClient.SERVER_IP_FLAG, "127.0.0.1");
		xzySocket.addPara(XZYSocketClient.SERVER_PORT_FLAG, "23457");
		xzySocket.addPara(XZYSocketClient.CMD_PARSER_FLAG, XZYProtocolParser.class.getName());
		xzySocket.addPara(XZYSocketServer.PROTOCOL_TEMPLATE_FLAG, this.getClass().getClassLoader().getResource("test/xzy/base/head_tail_test.xml").getPath());
		clientContainer.init(xzySocket);
		xzySocket.addPara(XZYSocketClient.CMD_RELEASER_FLAG, clientContainer);
		xzySocket.startServer();
		

		InfoContainer sendInfo = new InfoContainer();
		List<InfoContainer> list = new LinkedList<InfoContainer>(),tempList;
		InfoContainer tempInfo,tempSubInfo;
		sendInfo.setInfo("msg", 0x8015);
		sendInfo.setInfo("info_x", "infos");
		sendInfo.setInfo("info_num", new Integer(2));
		sendInfo.setInfo("group_1", list);
		
		for(int i=0;i<2;i++){
			tempInfo = new InfoContainer();
			tempInfo.setInfo("r", i+1);
			tempInfo.setInfo("bcd", "456"+i);
			
			tempList = new LinkedList<InfoContainer>();
			for(int j=0;j<10;j++){
				tempSubInfo = new InfoContainer();
				tempSubInfo.setInfo("info_y", "–Ï–¬≤‚ ‘_"+(j+100));
				tempList.add(tempSubInfo);
			}
			
			tempInfo.setInfo("group_1_1", tempList);
			
			list.add(tempInfo);
		}
		list = new LinkedList<InfoContainer>();
		sendInfo.setInfo("group_2", list);
		byte[] a;
		for(int i=0;i<2;i++){
			tempInfo = new InfoContainer();
			a = new byte[20];
			a[0] = a[1] = (byte)i;
			tempInfo.setInfo("r", a);
			list.add(tempInfo);
		}

		list = new LinkedList<InfoContainer>();
		sendInfo.setInfo("group_3", list);
		for(int i=0;i<5;i++){
			tempInfo = new InfoContainer();

			tempInfo.setInfo("id", new Integer(i+1));
			tempInfo.setInfo("val", ("sdsdsfds_"+i).getBytes());
			
			list.add(tempInfo);
		}
		
		TestEventListener eventListener = new TestEventListener(){
			public IEventResponse dispose(IEvent event) {
				this.result = Boolean.TRUE;
				return null;
			}
		};
		DefaultEventCenterServer.getSingleInstance().registEventListener("8001-response", eventListener);
		xzySocket.sendData(sendInfo);
		
		Util.sleep(1000);
		if(eventListener.result != Boolean.TRUE){
			this.print("Socket Action Test Failure!");
			Assert.fail("Socket Action Test Failure!");
		}
		this.print("Socket Action Test Success!");
	}

	private void print(String message){
		System.out.println(this.getClass().getName()+":"+message);
	}
}
