package server.xzy.socket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.xzy.base.Const;
import com.xzy.base.parser.IParser;
import com.xzy.base_c.InfoContainer;


public class XZYConnectInfo extends InfoContainer{
	//�Ƿ�Խ��պͷ��͵����ݽ��м�¼��֧�ֽ���ͨ����ͬʱ��¼
	public static final Object CONN_RECORD_FLAG = "conn_record_flag";

	public static final Object SOCKET_DESC_FLAG = new Object();
	
	//�����ݺͳ����ݼ�¼ֵ
	public static final int IN_DATA_RECORD = 1;
	public static final int OUT_DATA_RECORD = 2;
	
	/**
	 * ע:��ֵԽС���ȼ�Խ��
	 */
	public static final int PRIORITY_LEVEL_1 = 1;
	public static final int PRIORITY_LEVEL_15 = 15;
	
	private int recordFlag = 0;
	private OutputStream in_fout = null;
	private OutputStream out_fout = null;
	
	private SocketChannel channel = null;
	private SelectionKey sKey = null;
	private long lastActiveTime = 0;
	
	private ByteBuffer dataBuffer = null;

	private long lastWriteTime = 0;
	private Object listLock = null;
	private int maxCachSize = 64*1024;
	
	//��������������ܴ�С
	private long allSize = 0;
	//��ǰ�����͵�������
	private int curSize = 0;

	//��Ϣ����
	private List priorityList = new LinkedList();
	//���ȼ�����Ϣ����
	private int lowerPriorityNum = 0,higherPriorityNum = 0;
	
	private boolean hasThread = false;
	
	private XZYSocket server = null;
	private Date createDate = new Date();
	
	private DataPacket curPacket = null;
	private IReader reader = null;
	private IParser parser = null;
	private IReleaser releaser = null;
	

	public XZYConnectInfo(SocketChannel channel,IReader reader,IReleaser releaser,XZYSocket server){
		this(channel,reader,null,releaser,server);
	}
	public XZYConnectInfo(SocketChannel channel,IParser parser,IReleaser releaser,XZYSocket server){
		this(channel,null,parser,releaser,server);
	}
	public XZYConnectInfo(SocketChannel channel,IReader reader,IParser parser,IReleaser releaser,XZYSocket server){
		this.reader = reader;
		this.parser = parser;
		this.releaser = releaser;
		this.lastActiveTime = System.currentTimeMillis();
		this.server = server;
		this.channel = channel;
		
		if(this.parser != null) {
			this.dataBuffer = ByteBuffer.allocate(this.parser.getCapacity());
		}
		if(this.server != null){
			if(this.server.getIntegerPara(CONN_RECORD_FLAG)!=null){
				this.recordFlag = this.server.getIntegerPara(CONN_RECORD_FLAG).intValue();
			}
		}
	}
	
	private String getLogFileName(boolean isIn){
		String name ;
		SocketAddress addr = this.getRemoteSocketAddress();
		if(addr == null){
			name = "unknown";
		}else{
			name = addr.toString().replaceAll(":", "_");
		}
		if(isIn){
			return "in_"+Const.getDateFormater("yyyyMMddHHmmssSSS").format(this.createDate)+"_"+name+".log";
		}else{
			return "out_"+Const.getDateFormater("yyyyMMddHHmmssSSS").format(this.createDate)+"_"+name+".log";
		}
	}
	private void recordData(byte[] data,int offset,int len,boolean isIn){
		File dir = new File("_conn_data_record_");
		dir.mkdirs();
		OutputStream out = null;
		try{
			out = isIn?this.in_fout:this.out_fout;
			if(out == null){
				out = new FileOutputStream(new File(dir,this.getLogFileName(isIn)),true);
				if(isIn){
					this.in_fout = out;
				}else{
					this.out_fout = out;
				}
			}
			out.write(data, offset, len);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public XZYSocket getXZYSocket(){
		return this.server;
	}
	
	public void switchSendMode2Thread(int maxCachSize) throws Exception{
		this.initThread(maxCachSize);
	}
	
	private void initThread(int maxCachSize) throws Exception{
		if(this.hasThread){
			return ;
		}
		this.listLock = new Object();
		
		this.hasThread = true;
		this.maxCachSize = maxCachSize;
		if(this.maxCachSize <= 0){
			this.maxCachSize = 8*1024;
		}
	}
	
	public SocketChannel getChannel(){
		return this.channel;
	}
	
	public void setSocketDesc(String desc){
		if(desc == null){
			this.removeInfo(SOCKET_DESC_FLAG);
		}else{
			this.setInfo(SOCKET_DESC_FLAG, desc);
		}
	}
	public String getSocketDesc(){
		String desc = this.getString(SOCKET_DESC_FLAG);
		if(desc == null){
			SocketAddress addr = this.getRemoteSocketAddress();
			if(addr != null){
				desc = addr.toString();
			}
		}
		return desc;
	}
	
	public void setMinCachSize(int minCachSize){
		
	}
	public int getMinCachSize(){
		return 1024;
	}
	public void setMaxCachSize(int maxCachSize){
		this.maxCachSize = maxCachSize;
	}
	public int getMaxCachSize(){
		return this.maxCachSize;
	}
	
	public long getLastActiveTime(){
		return this.lastActiveTime;
	}
	public void updateLastActiveTime(){
		this.lastActiveTime = System.currentTimeMillis();
	}
	
	public void setSelectionKey(SelectionKey sKey){
		this.sKey = sKey;
	}
	public SelectionKey getSelectionKey(){
		return this.sKey;
	}
	public int remaining(){
		if(this.hasThread){
			return this.curSize;
		}else{
			return 0;
		}
	}
	public long getAllSize(){
		return this.allSize;
	}
	
	private SocketAddress address = null;
	public SocketAddress getRemoteSocketAddress(){
		if(this.channel == null){
			return address;
		}
		if(address==null){
			address = this.channel.socket().getRemoteSocketAddress();
		}
		return address;
	}
	private SocketAddress localAddress = null;
	public SocketAddress getLocalSocketAddress(){
		if(this.channel == null){
			return null;
		}
		if(localAddress==null){
			localAddress = this.channel.socket().getLocalSocketAddress();
		}
		return localAddress;
	}

	int localPort=0;
	public int getLocalSocketPort(){
		if(this.channel == null){
			return 0;
		}
		if(localPort==0){
			localPort = this.channel.socket().getLocalPort();
		}
		return localPort;
	}
	private InetAddress iadd = null;
	public InetAddress getInetAddress(){
		if(this.channel == null){
			return null;
		}
		if(iadd==null)
			iadd = this.channel.socket().getInetAddress();
		return iadd;
	}
	private int socketPort=0;
	public int getPort(){
		if(this.channel == null){
			return -1;
		}
		if(socketPort==0)
			socketPort= this.channel.socket().getPort();
		return socketPort;
	}

	public boolean isConnected(){
		if(this.channel == null){
			return false;
		}
		return this.channel.isConnected();
	}
	public boolean isOpen(){
		if(this.channel == null){
			return false;
		}
		return this.channel.isOpen();
	}
	
	public int readCmdInfo() throws Exception{
		if(this.reader != null) {
			InfoContainer[] cmdInfoArr = this.reader.readCmd(this);
			this.releaseCmdInfo(cmdInfoArr);
			return cmdInfoArr==null?0:cmdInfoArr.length;
		}
		
		if(this.dataBuffer.remaining() == 0){
			if(this.dataBuffer.capacity() != this.parser.getMaxCapacity()){
				ByteBuffer tempBuffer = ByteBuffer.allocate(this.parser.getMaxCapacity());
				System.arraycopy(this.dataBuffer.array(), this.dataBuffer.arrayOffset(), tempBuffer.array(), 0, this.dataBuffer.position());
				tempBuffer.position(this.dataBuffer.position());
				this.dataBuffer = tempBuffer;
			}
		}
		int num = this.channel.read(this.dataBuffer);
		if(num < 0){
			return num;
		}
		this.updateLastActiveTime();
		
		if((this.recordFlag & IN_DATA_RECORD) > 0){
			this.recordData(this.dataBuffer.array(),this.dataBuffer.position()-num,num,true);
		}
		this.dataBuffer.flip();
		this.releaseCmdInfo(this.parser.decodeData(this.dataBuffer));

		int remain = dataBuffer.remaining();
		if(this.dataBuffer.capacity() == this.parser.getMaxCapacity() && remain < this.parser.getCapacity()){
			ByteBuffer tempBuffer = ByteBuffer.allocate(this.parser.getCapacity());
			if(remain > 0){
				System.arraycopy(this.dataBuffer.array(), this.dataBuffer.arrayOffset()+this.dataBuffer.position(), tempBuffer.array(), 0, remain);
				tempBuffer.position(remain);
			}
			this.dataBuffer = tempBuffer;
		}else{
			if(dataBuffer.position() > 0){
				System.arraycopy(dataBuffer.array(), dataBuffer.position(), dataBuffer.array(), 0, remain);
			}
			dataBuffer.limit(dataBuffer.capacity());
			dataBuffer.position(remain);
		}

		return num;
	}
	public void releaseCmdInfo(InfoContainer[] cmdInfoArr){
		if(cmdInfoArr == null){
			return ;
		}
		for(int i=0;i<cmdInfoArr.length;i++){
			cmdInfoArr[i].setInfo(XZYSocket.SOCKET_FLAG, this);
			if(cmdInfoArr[i].getInfo(XZYSocket.CMD_FLAG) == null && this.parser != null){
				cmdInfoArr[i].setInfo(XZYSocket.CMD_FLAG, cmdInfoArr[i].getInfo(this.parser.getMsgName()));
				cmdInfoArr[i].setInfo(XZYSocket.DATA_FLAG, cmdInfoArr[i].removeInfo(IParser.ORI_DATA_FLAG));
				cmdInfoArr[i].setInfo(XZYSocket.SERIAL_DISPOSE_FLAG, cmdInfoArr[i].getInfo(this.parser.getSeriralDisposeFieldName()));
			}
			releaser.execute(cmdInfoArr[i]);
		}
	}
	
	public boolean writeInfo(InfoContainer info) throws Exception{
		return this.writeInfo(info, PRIORITY_LEVEL_15);
	}
	public boolean writeInfo(InfoContainer info,int priorityLevel) throws Exception{
		ByteBuffer dataBuff = this.parser.encodeData(info);
		int len = dataBuff.remaining();
		if(len == this.writeData(dataBuff, priorityLevel)){
			return true;
		}
		return false;
	}
	
	public int writeData(ByteBuffer dataBuff,int priorityLevel) throws Exception{
		if(dataBuff == null){
			throw new NullPointerException();
		}
		int len = dataBuff.remaining();
		if(len > this.maxCachSize){
			throw new Exception("data has over max capacity! len:"+len+"  this.maxCachSize:"+this.maxCachSize);
		}
		
		if(this.hasThread){
			//����д�����е�����
			if(!this.flush()){
				throw new Exception("Closed Channel!");
			}
			
			synchronized(this.listLock){
				//��¼����
				if((this.recordFlag & OUT_DATA_RECORD) > 0){
					this.recordData(dataBuff.array(),dataBuff.position(),len,false);
				}
				
				//��������е�������ʣ��,��ֱ��д��д����,���������������ʣ�� ,����д
				int num = 0;
				if(!this.hasRemainData()){
					num = this.channel.write(dataBuff);
					if(num > 0){
						this.lastWriteTime = System.currentTimeMillis();
					}
				}
				//�����ǰ��д������ʣ��
				if(dataBuff.hasRemaining()){
					int remainNum = dataBuff.remaining();
					if(remainNum + this.curSize > this.maxCachSize){
						if(priorityLevel != PRIORITY_LEVEL_1){
							this.printCachFullError();
							return 0;
						}
						//��յ����ȼ�����,����true�����ڸô����Ļ��������������,����false������������,��ֱ������������.
						if(!this.clearLowerPriority(remainNum)){
							this.printCachFullError();
							return 0;
						}
					}
					
					//�������ݰ������õ�������list��.
					DataPacket packet = new DataPacket(priorityLevel);
					packet.dataBuff = ByteBuffer.allocate(remainNum);
					System.arraycopy(dataBuff.array(), dataBuff.arrayOffset()+dataBuff.position(), packet.dataBuff.array(), 0, remainNum);
					
					if(num > 0){
						this.curPacket = packet;
					}else{
						this.addDataPacket(packet);
					}
					
					this.curSize += remainNum;
				}

				this.allSize += len;
			}
			
			//�Ѹ����Ӷ������ӵ������·���֤������.
			if(this.hasRemainData()){
				XZYDataSendHelper.getSingleInstance().add2Help(this);
			}
		}else{
			int num;
			synchronized(this.channel){
				num = this.channel.write(dataBuff);
				this.allSize += num;
			}
			return num; 
		}
		
		return len;
	}	
	private long lastPrintCachFullTime = 0;
	private void printCachFullError(){
		if(System.currentTimeMillis() - lastPrintCachFullTime > 120000){
			this.lastPrintCachFullTime = System.currentTimeMillis();
			this.server.error("%%%%%%%%%%%%%%%%%["+this.getRemoteSocketAddress()+"] has use max cache["+this.maxCachSize+"],data will be lost.");
		}
	}

	/**
	 * ��������ȼ������ݽ���д
	 * @param dataBuff
	 * @return д����ֽ���,������߳�ģʽ,�򷵻�һ��δд��ȫ��д��
	 * @throws Exception
	 */
	public int writeData(ByteBuffer dataBuff) throws Exception{
		return this.writeData(dataBuff,PRIORITY_LEVEL_15);
	}
	public int writeData(byte[] data,int offset,int len,int priorityLevel) throws Exception{
		if(data == null){
			return 0;
		}
		if(offset < 0){
			throw new Exception("������ʼλ��!");
		}
		
		int totalLen = data.length;
		if(totalLen < offset + len){
			throw new Exception("���Ȳ���!");
		}
		
		ByteBuffer dataBuff = ByteBuffer.wrap(data,offset,len);
		int num = this.writeData(dataBuff,priorityLevel);
		return num;
	}
	
	public int writeData(byte[] data,int offset,int len) throws Exception{
		return this.writeData(data, offset, len, PRIORITY_LEVEL_15);
	}
	
	/**
	 * �������ݰ���list�к��ʵ�λ��.
	 * @param packet
	 */
	private void addDataPacket(DataPacket packet){
		int index = this.priorityList.size();
		
		if(packet.priorityLevel == PRIORITY_LEVEL_1){
			index = this.higherPriorityNum;
			
			this.higherPriorityNum ++;
		}else{
			this.lowerPriorityNum ++;
		}

		this.priorityList.add(index, packet);
	}
	private boolean clearLowerPriority(int destRemainNum){
		int destCurSize = this.maxCachSize - destRemainNum;
		Iterator itr = this.priorityList.listIterator(this.higherPriorityNum);
		
		//���ڵĵ�һ�����ݿ����Ǹ����ȼ���,Ҳ�п���������д������.����0����λ�õ������Ǹ������ȼ�������д���ݵĿ���.
		DataPacket pack;
		while(this.curSize > destCurSize && itr.hasNext()){
			pack = (DataPacket)itr.next();
			itr.remove();
			
			this.curSize -= pack.remaining();
		}
		
		return this.curSize <= destCurSize;
	}
	
	public void destroy(){
		if(this.isClosed){
			return;
		}
		this.isClosed = true;
		
		try{
			if(this.in_fout != null){
				this.in_fout.close();
			}
			this.in_fout = null;
			if(this.out_fout != null){
				this.out_fout.close();
			}
			this.out_fout = null;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			if(this.channel != null){
				this.channel.close();
			}
			this.channel = null;
		}catch(Exception e){
			
		}
		try{
			if(this.sKey != null){
				this.sKey.attach(null);
				this.sKey.cancel();
				this.sKey = null;
			}
		}catch(Exception e){
			
		}
		this.clearList();
		
		if(this.server != null){
			this.server.destroySocket(this, XZYSocket.SOCKET_CLOSE_CODE.CLOSE_IO_ERROR_CODE);
		}
	}
	public void closeSocket(){
		this.destroy();
	}
	/**
	 * �ѵ�ǰӦ�ò��е�������ײ������д.д�����������.
	 * @return �Ƿ���д�ɹ�.ֻҪSocketδ�����쳣,��������Ϊ�ɹ�.
	 */
	public boolean flush(){
		if(!this.hasThread){
			return true;
		}
		
		if(this.channel == null){
			return false;
		}
		
		synchronized(this.listLock){
			try{
				this.curPacket = this.writeCurPacket();
				if(this.curPacket != null){
					return true;
				}
				
				Iterator itr;
				for(itr = this.priorityList.iterator();itr.hasNext();){
					this.curPacket = (DataPacket)itr.next();
					itr.remove();
					
					if(this.curPacket.priorityLevel == PRIORITY_LEVEL_1){
						this.higherPriorityNum --;
					}else{
						this.lowerPriorityNum --;
					}
					
					this.curPacket = this.writeCurPacket();
					if(this.curPacket != null){
						break;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				this.clearList();
				return false;
			}
		}
		
		return true;
	}
	
	private DataPacket writeCurPacket() throws Exception{
		if(this.curPacket == null){
			return this.curPacket;
		}
		
		int num = this.channel.write(this.curPacket.dataBuff);
		this.curSize -= num;
		if(num > 0){
			lastWriteTime = System.currentTimeMillis();
		}
		if(this.curPacket.hasRemaining()){
			return this.curPacket;
		}
		return null;
	}

	private void clearList(){
		if(hasThread){
			this.curSize = 0;
			this.curPacket = null;
			synchronized(this.listLock){
				this.priorityList.clear();
			}
		}
	}
	
	/**
	 * ��ǰ��Socket�����Ӧ�ó������Ƿ��д���д������
	 * @return
	 */
	public boolean hasRemainData(){
		if(!this.hasThread){
			return false;
		}

		return this.curSize > 0;
	}
	
	/**
	 * ���һ��д���뵱ǰ��ʱ���
	 * @return
	 */
	public long getLastWriteTimeDiff(){
		return System.currentTimeMillis()-this.lastWriteTime;
	}
	
	public void finalize() throws Throwable{
		super.finalize();
		this.destroy();
	}
	
	private class DataPacket{
		//����д������,���û��ռ俽����ģ��ռ�,�û���������
		public ByteBuffer dataBuff = null;
		//�������ȼ�
		public int priorityLevel = PRIORITY_LEVEL_15;
		
		public DataPacket(){
			
		}
		public DataPacket(int priorityLevel){
			this.priorityLevel = priorityLevel;
		}

		public boolean hasRemaining(){
			if(this.dataBuff == null){
				return false;
			}
			return this.dataBuff.hasRemaining();
		}
		public int remaining(){
			return this.dataBuff.remaining();
		}
	}
	private boolean isClosed = false;
	
	public boolean isClosed(){
		return this.isClosed;
	}
	public IParser getParser() {
		return parser;
	}

	public IReleaser getReleaser() {
		return releaser;
	}
	public int getCurSize(){
		return curSize;
	}
}