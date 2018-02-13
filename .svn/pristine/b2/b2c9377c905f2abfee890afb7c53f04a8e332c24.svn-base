package web.xzy.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xzy.base.server.container.IAction;
import com.xzy.base_c.InfoContainer;

public class HttpInfo extends InfoContainer{
	
	public static final String INFO_NAME = "_info_";
	
	private static final Object REQUEST_OBJECT_KEY = new Object();
	private static final Object RESPONSE_OBJECT_KEY = new Object();
	private static final Object SUCCESS_KEY = new Object();
	private static final Object REASON_KEY = new Object();
	

	public static final int REQUEST_SCOPE = 1;
	public static final int SESSION_SCOPE = 2;
	public HttpInfo(){
		
	}
	public HttpInfo(HttpServletRequest request,HttpServletResponse response){
		this.setRequestResponse(request, response);
	}
	
	public static HttpInfo getHttpInfo(HttpServletRequest request){
		return (HttpInfo)request.getAttribute(INFO_NAME);
	}
	
	public void setRequestResponse(HttpServletRequest request,HttpServletResponse response){
		HttpServletRequest pRequest = this.getRequest();
		if(pRequest != null){
			pRequest.removeAttribute(INFO_NAME);
		}
		
		this.setInfo(REQUEST_OBJECT_KEY, request);
		this.setInfo(RESPONSE_OBJECT_KEY, response);
		request.setAttribute(INFO_NAME, this);
	}
	public HttpServletRequest getRequest(){
		return (HttpServletRequest)this.getInfo(REQUEST_OBJECT_KEY);
	}
	public HttpServletResponse getResponse(){
		return (HttpServletResponse)this.getInfo(RESPONSE_OBJECT_KEY);
	}
	
	public void setAttribute(String key,Object value,int scope){
		HttpServletRequest request = this.getRequest();
		if(request == null){
			return ;
		}
		
		switch(scope){
			case REQUEST_SCOPE:
				request.setAttribute(key, value);
				break;
			case SESSION_SCOPE:
				if(value == null){
					request.getSession().removeAttribute(key);
				}else{
					request.getSession().setAttribute(key, value);
				}
				break;
		}
	}
	public Object getAttribute(String key,int scope){
		HttpServletRequest request = this.getRequest();
		if(request == null){
			return null;
		}
		switch(scope){
			case REQUEST_SCOPE:
				return request.getAttribute(key);
			case SESSION_SCOPE:
				return request.getSession().getAttribute(key);
		}
		return null;
	}
	
	public String getActionMsg(){
		return this.getString(IAction.MSG_FLAG);
	}
	
	public String[] getStringArr(Object key){
		Object value = this.getInfo(key);
		if(value instanceof String[]){
			return (String[])value;
		}else if(value instanceof String){
			return new String[]{value.toString()};
		}
		return null;
	}
	public StringBuffer getAllInfoString()
	{
		StringBuffer allInfoString=new StringBuffer();
		Object[]  allKey=getAllKey();
		for(int i=0;i<allKey.length;i++)
		{
			if(allKey[i] instanceof String)
			{
				if(getInfo(allKey[i]) instanceof String)
				{
					allInfoString.append("&"+allKey[i]+"="+getString(allKey[i]));
				}
			}
		}
		
		return allInfoString;
	}
	
	public void setFaliure(String reason){
		this.setInfo(SUCCESS_KEY, Boolean.FALSE);
		this.setInfo(REASON_KEY, reason);
	}
	public boolean isSuccess(){
		Boolean b = this.getBoolean(SUCCESS_KEY);
		if(b == null){
			return true;
		}
		return b.booleanValue();
	}
	public String getReason(){
		return this.getString(REASON_KEY);
	}
	public String getTrimedString(Object obj){
		return this.getString(obj)==null?null:this.getString(obj).trim();
	}
}
