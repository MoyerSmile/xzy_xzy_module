package server.xzy.socket;

import com.xzy.base_c.InfoContainer;

public interface IReader {
	public void init(XZYSocket caller);
	public InfoContainer[] readCmd(XZYConnectInfo connInfo) throws Exception;
}
