package web.xzy.base;

import java.io.*;
import java.net.URLDecoder;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xzy.base.Util;
import com.xzy.base.server.container.XZYActionContainerServer;
import com.xzy.base.server.container.IAction;
import com.xzy.base.server.log.LogRecordEvent;
import com.xzy.base_c.XZYStart;

public class EntryAction extends HttpServlet{
	private static XZYActionContainerServer actionServer = null;
	private XZYStart server = null;
	public static XZYActionContainerServer getActionServer()
    {
        return actionServer;
    }

	private static ServletContext servletContext = null;
    public void init(ServletConfig cfg) throws ServletException{
		super.init();

        String confParentPath = cfg.getInitParameter("conf_parent_path");
        if(confParentPath == null){
            EntryAction.confParentDir = EntryAction.getWebInfFile();
        }else{
            if(Util.isWindowsOS()){
                if(confParentPath.indexOf(':') > 0){//绝对路径
                    EntryAction.confParentDir = new File(confParentPath);
                }else{                              //相对WEB-INF的相对路径
                    EntryAction.confParentDir = new File(EntryAction.getWebInfFile(),confParentPath);
                }
            }else{
                if(confParentPath.startsWith("/")){//绝对路径
                    EntryAction.confParentDir = new File(confParentPath);
                }else{                              //相对WEB-INF的相对路径
                    EntryAction.confParentDir = new File(EntryAction.getWebInfFile(),confParentPath);
                }
            }
        }
        
		EntryAction.servletContext = cfg.getServletContext();
		
		File f = new File(EntryAction.getWebInfFile(),cfg.getInitParameter("server_file_path"));
		if(f.exists()){
			server = new XZYStart();
			if(server.startServer(f.getAbsolutePath())){
				this.info("WebServer Struct Start Success!");
			}else{
				this.info("WebServer Struct Start Failure!");
			}
		}
		
		f = new File(EntryAction.getWebInfFile(),cfg.getInitParameter("mapping_file_path"));
		
		String name = "Web Entry Mapping Server";
		actionServer = new XZYActionContainerServer();
		actionServer.setServerName(name);
		actionServer.addPara(XZYActionContainerServer.ASYNC_THREAD_NUM_FLAG, "1");
		actionServer.addPara(XZYActionContainerServer.CFG_PATH_KEY_FLAG, f.getAbsolutePath());
		
		if(actionServer.startServer()){
			this.info(name+" Start Success!");
		}else{
			this.info(name+" Start Failure!");
			throw new ServletException(name+" Start Failure!");
		}
	}

	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException{
		HttpServletRequest hRequest = (HttpServletRequest)request;
		HttpInfo info = new HttpInfo(hRequest,(HttpServletResponse)response);

		String name;
		String[] value;
		Enumeration enu = request.getParameterNames();
		while(enu.hasMoreElements()){
			name = (String)enu.nextElement();
			
			value = request.getParameterValues(name);
			if(value != null && value.length == 1){
				info.setInfo(name, value[0]);
			}else{
				info.setInfo(name, value);
			}
		}
		
		String msg = hRequest.getRequestURI();
		String contextPath = hRequest.getContextPath();
		if(contextPath != null){
			msg = msg.substring(contextPath.length());
		}
		msg = msg.substring(1,msg.lastIndexOf('.')).replaceAll("\\\\", "/");
		while(msg.startsWith("/")){
			msg = msg.substring(1);
		}
		
		info.setInfo(IAction.MSG_FLAG, msg);
		
		actionServer.executeTask(info);
	}
	
	public void destroy(){
		super.destroy();
		
		if(actionServer != null){
			System.out.println("入口映射行为服务开始卸载!");
			actionServer.stopServer();
		}
		if(server != null){
			System.out.println("服务框架开始卸载!");
			server.stopServer();
		}
	}

    //得到配置文件目录conf目录所在的父目录
    private static File confParentDir = null;
    public static File getConfParentDir(){
        return EntryAction.confParentDir;
    }
    
	private static File WEB_INF_FILE = null;
	public static File getWebInfFile() {
		if (WEB_INF_FILE == null) {
			try{
				WEB_INF_FILE = new File(URLDecoder.decode(EntryAction.class.getClassLoader().getResource("ApplicationResources.properties").getFile(),"GB2312")).getParentFile().getParentFile();
			}catch(Exception e){
				e.printStackTrace();
				WEB_INF_FILE = new File(EntryAction.class.getClassLoader().getResource("ApplicationResources.properties").getFile()).getParentFile().getParentFile();
			}
		}
		return WEB_INF_FILE;
	}
	public static ServletContext _getServletContext(){
		return EntryAction.servletContext;
	}
	

	public void info(String content){
		this.info(content, null);
	}
	public void info(String content,Throwable e){
		new LogRecordEvent(this).info(this.getClass(), content, e);
	}
	public void warn(String content){
		this.warn(content, null);
	}
	public void warn(String content,Throwable e){
		new LogRecordEvent(this).warn(this.getClass(), content, e);
	}
	public void error(String content){
		this.error(content, null);
	}
	public void error(String content,Throwable e){
		new LogRecordEvent(this).error(this.getClass(), content, e);
	}
}
