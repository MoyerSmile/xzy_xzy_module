package server.xzy.socket;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.xzy.base.server.pool.BasicTask;

public class XZYReadSelectorTask extends BasicTask {
	private Selector selector = null;
	private XZYSocket xzySocket = null;
	
	public XZYReadSelectorTask(Selector selector,XZYSocket xzySocket){
		this.selector = selector;
		this.xzySocket = xzySocket;
	}

	private List<RegistInfo> channelPendingList = new LinkedList<RegistInfo>();
	public void addSocketChannel(RegistInfo connInfo){
		synchronized(channelPendingList){
			channelPendingList.add(connInfo);
		}
		selector.wakeup();
	}
	
	@Override
	public void run(){
		Iterator keyIterator = null;
		SelectionKey sKey = null;
		XZYConnectInfo connInfo = null;
		while(!this.isCancel()){
			try{
				synchronized(channelPendingList){
					if(channelPendingList.size() > 0){
						RegistInfo rInfo;
						for(Iterator itr = channelPendingList.iterator();itr.hasNext();){
							rInfo = (RegistInfo)itr.next();
							itr.remove();
							this.xzySocket.registerSocket(rInfo,this.selector);
						}
					}
				}
				int readyNum = this.selector.select(10000);
				if(readyNum > 0){
					keyIterator = this.selector.selectedKeys().iterator();
					while(keyIterator.hasNext()){
						sKey = (SelectionKey)keyIterator.next();

						connInfo = null;
						try{
							if(sKey.isReadable()){
								connInfo = (XZYConnectInfo)sKey.attachment();
								if(connInfo.readCmdInfo() < 0){
									this.info("["+this.xzySocket.getServerName()+"]connect remote close!"+connInfo.getSocketDesc());
									this.xzySocket.destroySocket(connInfo,XZYSocket.SOCKET_CLOSE_CODE.CLOSE_IO_ERROR_CODE);
								}
							}
						}catch(ClosedChannelException e1){
							this.error("["+this.xzySocket.getServerName()+"]connect closed!"+connInfo.getSocketDesc(),e1);
							this.xzySocket.destroySocket(connInfo,XZYSocket.SOCKET_CLOSE_CODE.CLOSE_IO_ERROR_CODE);
						}catch(IOException e3){
							this.error("["+this.xzySocket.getServerName()+"]connect io error!"+connInfo.getSocketDesc(),e3);
							this.xzySocket.destroySocket(connInfo,XZYSocket.SOCKET_CLOSE_CODE.CLOSE_IO_ERROR_CODE);
						}catch(Exception e2){
							this.error("["+this.xzySocket.getServerName()+"]connect exception!"+connInfo.getSocketDesc(),e2);
							this.xzySocket.destroySocket(connInfo,XZYSocket.SOCKET_CLOSE_CODE.CLOSE_IO_ERROR_CODE);
						}catch(Error er){
							//错误的客户端连接或者恶意攻击，可能导致协议不正确而开启很大的空间而导致内存错误。只需关闭该Socket即可。
							this.error("["+this.xzySocket.getServerName()+"]connect Error!"+connInfo.getSocketDesc(),er);
							this.xzySocket.destroySocket(connInfo,XZYSocket.SOCKET_CLOSE_CODE.CLOSE_IO_ERROR_CODE);
						}
						
						keyIterator.remove();
					}
				}
			}catch(CancelledKeyException ce){
				this.error("["+this.xzySocket.getServerName()+"]selector cancel Error!"+connInfo.getSocketDesc(),ce);
			}catch(Exception e){
				this.error("["+this.xzySocket.getServerName()+"]selector Error!"+connInfo.getSocketDesc(),e);
			}
		}
		this.cancel();
	}

	public void cancel(){
		super.cancel();
		try{
			Iterator iterator = this.selector.keys().iterator();
			SelectionKey key;
			while(iterator.hasNext()){
				key = (SelectionKey)iterator.next();
				try{
					key.channel().close();
				}catch(Exception e){}
			}
		}catch(Exception e){}
		try{
			if(this.selector != null){
				this.selector.close();
			}
		}catch(Exception e){}
	}

	public class RegistInfo{
		public XZYConnectInfo connInfo = null;
		public SocketChannel channel = null;
		public RegistInfo(SocketChannel channel,XZYConnectInfo connInfo){
			this.channel = channel;
			this.connInfo = connInfo;
		}
	}
}
