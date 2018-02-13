package test.xzy.base;

import server.xzy.socket.XZYConnectInfo;
import server.xzy.socket.XZYSocket;

import com.xzy.base.server.container.BasicAction;
import com.xzy.base_c.InfoContainer;

public class TestResponseAction extends BasicAction {

	@Override
	public boolean execute(InfoContainer infos) throws Exception {
		XZYConnectInfo connInfo = (XZYConnectInfo)infos.getInfo(XZYSocket.SOCKET_FLAG);
		
		InfoContainer responseInfo = new InfoContainer();
		responseInfo.setInfo("msg", 0x8001);
		connInfo.writeInfo(responseInfo);
		
		return false;
	}

}
