package com.xzy.base.server.container.help;


import com.xzy.base.server.container.BasicAction;
import com.xzy.base.server.container.IAction;
import com.xzy.base_c.InfoContainer;

public class Help_PrintMsgAction extends BasicAction {
	protected String headStr = "MSG";
	public void init() throws Exception{
		super.init();
		
		String temp = this.getStringPara("head_str");
		if(temp != null){
			this.headStr = temp.trim();
		}

		String tempStr = this.getStringPara("is_print");
		if(tempStr != null && tempStr.trim().equalsIgnoreCase("true")){
			this.isPrint = true;
		}else{
			this.isPrint = false;
		}
	}
	
	public boolean execute(InfoContainer infos) throws Exception{
		Object msg = infos.getInfo(IAction.MSG_FLAG);
		if(isPrint()){
			this.info(headStr+":"+((msg instanceof Integer)?"0x"+Integer.toHexString((Integer)msg):msg.toString()));
		}
		return true;
	}

	private boolean isPrint = false;
	public boolean isPrint(){
		return this.isPrint;
	}
}
