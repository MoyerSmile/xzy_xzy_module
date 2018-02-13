package server.xzy.socket.help;

import com.xzy.base.server.container.IAction;
import com.xzy.base.server.container.XZYActionContainerServer;
import com.xzy.base_c.InfoContainer;

import server.xzy.socket.IReleaser;
import server.xzy.socket.XZYSocket;

public class ActionContainerReleaser implements IReleaser {
	private XZYActionContainerServer actionServer = null;
	private XZYSocket socketServer = null;
	@Override
	public void init(XZYSocket socketServer) throws Exception{
		this.socketServer = socketServer;
		this.actionServer = new XZYActionContainerServer();
		this.actionServer.setServerName(socketServer.getServerName()+"-actionContainer");
		this.actionServer.addPara(XZYActionContainerServer.CALL_SERVER_FLAG, this.socketServer);
		this.actionServer.addPara(XZYActionContainerServer.CFG_PATH_KEY_FLAG,this.socketServer.getPara(XZYActionContainerServer.CFG_PATH_KEY_FLAG));
		this.actionServer.addPara(XZYActionContainerServer.ASYNC_THREAD_NUM_FLAG,this.socketServer.getPara(XZYActionContainerServer.ASYNC_THREAD_NUM_FLAG));
		this.actionServer.addPara(XZYActionContainerServer.MULTIPLE_LIST_FLAG,this.socketServer.getPara(XZYActionContainerServer.MULTIPLE_LIST_FLAG));
		this.actionServer.addPara(XZYActionContainerServer.TASK_CAPACITY_FLAG,this.socketServer.getPara(XZYActionContainerServer.TASK_CAPACITY_FLAG));
		this.actionServer.addPara(XZYActionContainerServer.IS_PRINT_FLAG,this.socketServer.getPara(XZYActionContainerServer.IS_PRINT_FLAG));
		if(!this.actionServer.startServer()){
			this.socketServer.error(this.socketServer.getServerName()+" action Container start failure!"+this.socketServer.getPara(XZYActionContainerServer.CFG_PATH_KEY_FLAG));
			throw new Exception(this.socketServer.getServerName()+" action Container start failure!");
		}
	}

	@Override
	public void execute(InfoContainer cmdInfo) {
		cmdInfo.setInfo(IAction.MSG_FLAG, cmdInfo.getInfo(XZYSocket.CMD_FLAG));
		cmdInfo.setInfo(IAction.TASK_FLAG, cmdInfo.getInfo(XZYSocket.SERIAL_DISPOSE_FLAG));
		this.actionServer.executeTask(cmdInfo);
	}

}
