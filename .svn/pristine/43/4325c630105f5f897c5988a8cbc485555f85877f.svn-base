package test.xzy.base;

import org.json.JSONObject;

import com.xzy.base_i.IEvent;
import com.xzy.base_i.IEventListener;
import com.xzy.base_i.IEventResponse;

public class TestEventListener implements IEventListener {
	protected Object result = null;
	
	public void intOP(int val){
		if(this.result == null){
			this.result = new Integer(0);
		}
		this.result = ((Integer)this.result).intValue() | val;
	}
	
	public void count(){
		if(this.result == null){
			this.result = new Integer(0);
		}
		this.result = ((Integer)this.result).intValue()+1;
	}
	
	@Override
	public boolean hasInteresting(IEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public IEventResponse dispose(IEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getResult(){
		return this.result;
	}
}
