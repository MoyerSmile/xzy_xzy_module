package server.xzy.socket;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import server.xzy.socket.XZYReadSelectorTask.RegistInfo;

import com.xzy.base_c.BasicServer;

public abstract class XZYSocket extends BasicServer {
	public static final String PROTOCOL_TEMPLATE_FLAG = "protocol_template";
	public static final String PROTOCOL_TEMPLATE_DIR_FLAG = "protocol_dir";
	
	public static final Object CMD_FLAG = new Object(){
		public String toString(){
			return "CMD_FLAG";
		}
	};
	public static final Object SOCKET_FLAG = new Object(){
		public String toString(){
			return "SOCKET_FLAG";
		}
	};
	public static final Object DATA_FLAG = new Object(){
		public String toString(){
			return "DATA_FLAG";
		}
	};
	public static final Object SERIAL_DISPOSE_FLAG = new Object(){
		public String toString(){
			return "SERIAL_DISPOSE_FLAG";
		}
	};
	public static final Object SOCKET_CLOSE_CODE_FLAG = new Object(){
		public String toString(){
			return "CLOSE_CODE";
		}
	};
	
	public static enum SOCKET_CLOSE_CODE{
		CLOSE_IO_ERROR_CODE,
		CLOSE_TIMEOUT_CODE,
		USER_CLOSE_CODE,
		SWITCH_MAIN_CODE,
		TOTAL_NUM
	} ;
	
	public static final Object SOCKET_CONNECT_CMD = new Object(){
		public String toString(){
			return "connect";
		}
	};
	public static final Object SOCKET_DISCONNECT_CMD = new Object(){
		public String toString(){
			return "disconnect";
		};
	};

	protected abstract XZYConnectInfo registerSocketChannel(SocketChannel channel);
	public abstract void registerSocket(RegistInfo rInfo,Selector selector);
	public abstract void destroySocket(XZYConnectInfo connInfo,SOCKET_CLOSE_CODE code);
}
