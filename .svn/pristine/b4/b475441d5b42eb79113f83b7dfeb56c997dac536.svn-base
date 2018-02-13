package test.xzy.base;

import com.xzy.base.server.container.BasicAction;
import com.xzy.base.server.container.IAction;
import com.xzy.base.server.event.DefaultEventCenterServer;
import com.xzy.base_c.BasicEvent;
import com.xzy.base_c.InfoContainer;

public class TestAction extends BasicAction {
	public static final Object TEST_RESULT = new Object();
	@Override
	public boolean execute(InfoContainer infos) throws Exception {
		Object msg = infos.getInfo(IAction.MSG_FLAG);

		Integer result = (Integer)infos.getInteger(TEST_RESULT);
		if(result == null){
			result = new Integer(0);
		}
		infos.setInfo(TEST_RESULT, new Integer(result.intValue()+1));
		
		if(msg.equals(new Integer(0x8001))){
			DefaultEventCenterServer.getSingleInstance().notify(new BasicEvent(this,"8001-response"));
		}
		
		return true;
	}

}
