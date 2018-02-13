package server.xzy.ui;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONObject;

import com.xzy.base.server.pool.BasicTask;
import com.xzy.base_c.BasicServer;

public class WebFrameServer extends BasicServer {
	private Shell shell = null;
	private Display display = null;
	private Browser browser = null;
	private IJSListener listener = new JSDefaultListener();

	@Override
	public boolean startServer() {
		if (this.isRunning()) {
			return this.isRunning();
		}

		Object tempObj;
		try{
			tempObj = this.getPara("listener");
			if(tempObj != null){
				if(tempObj instanceof IJSListener){
					this.listener = (IJSListener)tempObj;
				}else{
					this.listener = (IJSListener)Class.forName(tempObj.toString()).newInstance();
				}
			}
		}catch(Exception e){
			this.error("init js2java listener error!",e);
			return false;
		}
		this.isRun = true;
		this.initFrame();

		return this.isRunning();
	}
	
	public void setListener(IJSListener listener) {
		this.listener = listener;
	}

	private void initFrame() {
		this.display = new Display();
		this.shell = new Shell(display);
		if (this.getStringPara("title") != null) {
			this.shell.setText(this.getStringPara("title"));
		}

		this.browser = new Browser(shell, SWT.FILL);
		new DefaultFunction(browser,"awt_browser_calljava");
		Dimension screenDims = Toolkit.getDefaultToolkit().getScreenSize();
		String dimStr = this.getStringPara("dimension");
		int w,h;
		if(dimStr != null){
			w = Integer.parseInt(dimStr.split(",")[0]);
			h = Integer.parseInt(dimStr.split(",")[1]);
		}else{
			w = (int)screenDims.getWidth();
			h = (int)screenDims.getHeight();
		}
		this.shell.setBounds(((int)screenDims.getWidth()-w)/2, ((int)screenDims.getHeight()-h)/2, w, h);
		this.browser.setBounds(0, 0, w, h-25);
		
		if(this.getStringPara("url") != null){
			this.updateUrl(this.getStringPara("url"));
		}

		if(this.getIntegerPara("timeout") != null){
			this.serverDetect(new BasicTask(){
				public void run(){
					stopServer();
				}
			}, this.getIntegerPara("timeout").intValue()*1000, 0, null);
		}
		
		this.shell.open();
		while (!this.shell.isDisposed()) {
			if (!this.display.readAndDispatch()) {
				this.display.sleep();
			}
		}
		this.display.dispose();
	}
	public boolean executeJavaScript(String funcName,JSONObject data){
		if(!this.isRunning()){
			return false;
		}
		JavascriptTask task = new JavascriptTask(funcName+"("+data.toString()+");");
		this.display.syncExec(task);
		return task.getResult();
	}

	public void updateUrl(String url) {
		if(this.browser != null){
			this.browser.setUrl(url);
		}
	}

	public void stopServer() {
		super.stopServer();
		
		if (this.display != null) {
			Display.getDefault().syncExec(new Runnable() {
	            public void run() {
	                shell.close();
	                shell.dispose();
	                display.close();
	    				display.dispose();
	            }
	        });
		}
	}
	
	private Object jsCall(Object[] argv){
		if(this.listener != null){
			return this.listener.js2Java(argv);
		}
		return null;
	}
	
	private class JavascriptTask implements Runnable{
		private boolean result = false;
		private String javascript = null;
		public JavascriptTask(String javascript){
			this.javascript = javascript;
		}
		public void run(){
			this.result = WebFrameServer.this.browser.execute(this.javascript);
		}
		public boolean getResult(){
			return this.result;
		}
	}

	private class JSDefaultListener implements IJSListener{
		public Object js2Java(Object[] argv){
			StringBuffer buff = new StringBuffer(1024);
			buff.append(WebFrameServer.this.getServerName()+"[js2java]:");
			for(int i=0;i<argv.length;i++){
				buff.append(argv[i]==null?"null":argv[i].toString());
			}
			WebFrameServer.this.info(buff.toString());
			return null;
		}
	}
	private class DefaultFunction extends BrowserFunction{
		public DefaultFunction(Browser browser,String funcName){
			super(browser,funcName);
		}
		public Object function(Object[] argv) {
			return WebFrameServer.this.jsCall(argv);
		}
	}
	
	public static void main(String[] argv) {
		WebFrameServer server = new WebFrameServer();
		server.addPara("title", "Test");
		server.addPara("url", "http://www.baidu.com");
		server.addPara("dimension", "800,600");
		server.addPara("timeout", "10");
		server.startServer();
	}
}
