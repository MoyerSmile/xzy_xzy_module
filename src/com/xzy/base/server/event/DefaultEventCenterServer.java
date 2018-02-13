package com.xzy.base.server.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.xzy.base_i.IEvent;
import com.xzy.base_i.IEventCenter;
import com.xzy.base_i.IEventListener;
import com.xzy.base_i.IEventResponse;

public class DefaultEventCenterServer implements IEventCenter {
	protected static IEventCenter singleInstance = new DefaultEventCenterServer();
	public static IEventCenter getSingleInstance(){
		return singleInstance;
	}
	
	private HashMap<Object,List<IEventListener>> listeners = new HashMap<Object,List<IEventListener>>();
	
	@Override
	public boolean notify(IEvent event) {
		if(event == null || event.getEventType() == null){
			return false;
		}
		
		IEventListener listener;
		List<IEventListener> list;
		synchronized(listeners){
			list = listeners.get(event.getEventType());
			if(list != null){
				for(Iterator<IEventListener> itr = list.iterator();itr.hasNext();){
					itr.next().dispose(event);
				}
			}
			list = listeners.get(null);
			if(list != null){
				for(Iterator<IEventListener> itr = list.iterator();itr.hasNext();){
					listener = itr.next();
					if(listener.hasInteresting(event)){
						listener.dispose(event);
					}
				}
			}
		}
		
		return true;
	}

	@Override
	public IEventResponse query(IEvent event) {
		List<IEventListener> list;
		synchronized(listeners){
			list = listeners.get(event.getEventType());
			if(list != null){
				for(Iterator<IEventListener> itr = list.iterator();itr.hasNext();){
					return itr.next().dispose(event);
				}
			}
		}
		return null;
	}

	@Override
	public void registEventListener(IEventListener listener) {
		this.registEventListener(null, listener);
	}

	@Override
	public void registEventListener(Object eventType, IEventListener listener) {
		synchronized(listeners){
			List<IEventListener> list = listeners.get(eventType);
			if(list == null){
				list = new LinkedList<IEventListener>();
				listeners.put(eventType, list);
			}
			if(!list.contains(listener)){
				list.add(listener);
			}
		}
	}

	@Override
	public void unregistEventListener(IEventListener listener) {
		unregistEventListener(null,listener);
	}
	@Override
	public void unregistEventListener(Object eventType,IEventListener listener){
		synchronized(listeners){
			List<IEventListener> list = listeners.get(eventType);
			if(list == null){
				return ;
			}
			list.remove(listener);
		}
	}



}
