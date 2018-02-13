package server.xzy.socket;

import com.xzy.base.server.log.LogRecordEvent;
import com.xzy.base_c.InfoContainer;

public class DefaultReleaser implements IReleaser {

	@Override
	public void init(XZYSocket socketServer) {

	}

	@Override
	public void execute(InfoContainer cmdInfo) {
		XZYConnectInfo connInfo = (XZYConnectInfo)cmdInfo.getInfo(XZYSocket.SOCKET_FLAG);
		
		new LogRecordEvent(this).info(this.getClass(), "cmd="+cmdInfo.getInfo(XZYSocket.CMD_FLAG)+"["+connInfo.getSocketDesc()+"]:"+cmdInfo);
	}

}
