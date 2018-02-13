package com.xzy.base_c;

import org.json.JSONException;
import org.json.JSONObject;

import com.xzy.base_i.IEvent;

public class BasicEvent implements IEvent {
	protected Object source = null;
	protected Object eventType = null;
	protected JSONObject eventPara = null;
	
	public BasicEvent(Object source,Object eventType){
		this(source,eventType,null);
	}
	public BasicEvent(Object source,Object eventType,JSONObject eventPara){
		this.source = source;
		this.eventType = eventType;
		this.eventPara = eventPara;
	}

	@Override
	public Object getSource() {
		return this.source;
	}

	@Override
	public Object getEventType() {
		return this.eventType;
	}

	@Override
	public JSONObject getEventPara() {
		return this.eventPara;
	}

	public void addEventPara(String key,Object val){
		if(this.eventPara == null){
			this.eventPara = new JSONObject();
		}
		try {
			this.eventPara.put(key, val);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
