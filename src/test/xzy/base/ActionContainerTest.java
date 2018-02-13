package test.xzy.base;

import org.junit.Assert;
import org.junit.Test;

import com.xzy.base.Util;
import com.xzy.base.server.container.XZYActionContainerServer;
import com.xzy.base.server.container.IAction;
import com.xzy.base.server.log.LogRecordServer;
import com.xzy.base_c.InfoContainer;

public class ActionContainerTest {
	@Test
	public void actionTest(){
		LogRecordServer.getSingleInstance().startServer();
		XZYActionContainerServer server = new XZYActionContainerServer();
		server.setServerName("Test");
		server.addPara(XZYActionContainerServer.ASYNC_THREAD_NUM_FLAG, "5");
		server.addPara(XZYActionContainerServer.MULTIPLE_LIST_FLAG, "true");
		server.addPara(XZYActionContainerServer.CFG_PATH_KEY_FLAG, this.getClass().getClassLoader().getResource("test/xzy/base/action_mapping_example.xml").getFile());
		server.addPara(XZYActionContainerServer.TASK_CAPACITY_FLAG, "1000");
		server.addPara(XZYActionContainerServer.IS_PRINT_FLAG, "true");
		server.startServer();
		
		InfoContainer infos = new InfoContainer();
		infos.setInfo(IAction.MSG_FLAG, new Integer(0x0001));
		server.executeTask(infos);
		Util.sleep(100);
		if(infos.getInteger(TestAction.TEST_RESULT).intValue() != 1){
			Assert.fail("Test1 Failure."+infos.getInteger(TestAction.TEST_RESULT));
		}
		
		infos = new InfoContainer();
		infos.setInfo(IAction.MSG_FLAG, new Integer(0x0002));
		server.executeTask(infos);
		Util.sleep(100);
		if(infos.getInteger(TestAction.TEST_RESULT) != null){
			Assert.fail("Test2 Failure."+infos.getInteger(TestAction.TEST_RESULT));
		}
		
		infos = new InfoContainer();
		infos.setInfo(IAction.MSG_FLAG, new Integer(0x0003));
		server.executeTask(infos);
		Util.sleep(100);
		if(infos.getInteger(TestAction.TEST_RESULT).intValue() != 3){
			Assert.fail("Test3 Failure."+infos.getInteger(TestAction.TEST_RESULT));
		}
		
		this.print("Action Container Test Success");
		
		Util.sleep(1000);
	}

	public void print(String message){
		System.out.println(this.getClass().getName()+":"+message);
	}
}
