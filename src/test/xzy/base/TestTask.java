package test.xzy.base;

import com.xzy.base.server.pool.BasicTask;

public class TestTask extends BasicTask {
	protected Object result = null;
	
	@Override
	public void run() {

	}

	public Object getResult(){
		return this.result;
	}
}
