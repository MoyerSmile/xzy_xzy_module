package server.xzy.socket;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.xzy.base.server.pool.BasicTask;

public class XZYAcceptSelectorTask extends BasicTask {
	private Selector selector;
	private XZYSocketServer server;
	public XZYAcceptSelectorTask(Selector selector,XZYSocketServer server){
		this.selector = selector;
		this.server = server;
	}
	
	@Override
	public void run() {
		Iterator keyIterator = null;
		SelectionKey sKey = null;
		SocketChannel channel = null;
		while(!this.isCancel()){
			try{
				int readyNum = this.selector.select();
				if(readyNum > 0){
					keyIterator = this.selector.selectedKeys().iterator();
					while(keyIterator.hasNext()){
						sKey = (SelectionKey)keyIterator.next();
						
						if(sKey.isAcceptable()){
							channel = ((ServerSocketChannel)sKey.channel()).accept();
							if(channel != null){
								this.server.registerSocketChannel(channel);
							}
						}

						keyIterator.remove();
					}
				}
			}catch(Exception e){
				this.error(this.server.getServerName()+":Accept Selector Error!",e);
				this.cancel();
			}
		}
	}
	
	public void cancel(){
		super.cancel();
		try{
			if(this.selector != null){
				this.selector.close();
			}
		}catch(Exception e){}
	}
}
