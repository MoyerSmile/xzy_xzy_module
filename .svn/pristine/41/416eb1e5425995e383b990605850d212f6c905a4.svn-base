package server.xzy.diagnos;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;

import com.xzy.base_c.InfoContainer;

import server.xzy.socket.IReader;
import server.xzy.socket.XZYConnectInfo;
import server.xzy.socket.XZYSocket;

public class CmdReader implements IReader {

	private static final Object HEAD_BUFF_FLAG = new Object();
	private static final Object DATA_BUFF_FLAG = new Object();
	@Override
	public void init(XZYSocket arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public InfoContainer[] readCmd(XZYConnectInfo connInfo) throws Exception {
		ByteBuffer head = (ByteBuffer)connInfo.getInfo(HEAD_BUFF_FLAG);
		ByteBuffer data = (ByteBuffer)connInfo.getInfo(DATA_BUFF_FLAG);
		if(head == null){
			head = ByteBuffer.allocate(9);
			head.order(ByteOrder.BIG_ENDIAN);
			connInfo.setInfo(HEAD_BUFF_FLAG, head);
		}
		
		List cmdList = null;
		int dataLen,readNum;
		InfoContainer cmdInfo;
		while(true){
			cmdInfo = null;
			
			//如果信息头已经读取完整,则读数据
			if(head.remaining() == 0){
				readNum = connInfo.getChannel().read(data);
				if(readNum < 0 && cmdList == null){
					throw new ClosedChannelException();
				}
				
				//如果数据未读完整,则跳出,等待下次读.
				if(data.remaining() != 0){
					connInfo.setInfo(DATA_BUFF_FLAG, data);
					break;
				}
				
				String endFlag = new String(data.array(),data.capacity()-7,7);
				if(!endFlag.equals("SONGAID")){
					throw new Exception("Error Head: "+ endFlag);
				}
				byte enc = data.get(0);
				byte[] arr = data.array();
				for(int i=1;i<data.capacity()-7;i++){
					arr[i] = (byte)(arr[i]^enc);
				}
				
				//数据读取完整,产生命令对象.
				cmdInfo = new InfoContainer();
				cmdInfo.setInfo(XZYSocket.SOCKET_FLAG, connInfo);
				cmdInfo.setInfo(XZYSocket.DATA_FLAG, new String(data.array(),1,data.capacity()-8,"GBK"));
			}else{ //如果信息头未读完整,则读信息头
				readNum = connInfo.getChannel().read(head);
				if(readNum < 0 && cmdList == null){
					throw new ClosedChannelException();
				}
				
				//如果不能读完整信息头,意味着数据缺乏,跳出.等待下次读.
				if(head.remaining() != 0){
					break;
				}
				String headFlag = new String(head.array(),0,7);
				if(!headFlag.equals("DIAGNOS")){
					throw new Exception("Error Head: "+ headFlag);
				}
				dataLen = (head.getShort(7)&0xffff) + 7;
				
				//数据信息长度为0,直接发布命令
				if(dataLen == 0){
					
				}else{ //产生数据空间,以便向其中读取数据.
					data = ByteBuffer.allocate(dataLen);
				}
			}
			
			//附加一个信息对象.如果发布命令信息,则清除头信息,同时从连接对象中移除数据信息.
			if(cmdInfo != null){
				head.position(0);
				connInfo.removeInfo(DATA_BUFF_FLAG);
				
				if(cmdList == null){
					cmdList = new LinkedList();
				}
				cmdList.add(cmdInfo);
			}
			
			if(cmdList != null && cmdList.size() >= 100){
				break;
			}
		}
		
		//把当前组装的命令对象进行返回.
		InfoContainer[] allCmd = null;
		if(cmdList != null){
			allCmd = new InfoContainer[cmdList.size()];
			cmdList.toArray(allCmd);
		}
		
		return allCmd;
	}

}
