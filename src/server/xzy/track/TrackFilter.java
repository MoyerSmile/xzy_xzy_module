
package server.xzy.track;

import com.xzy.base_c.InfoContainer;

public interface TrackFilter{
	/**
	 * CONTINUE放入结果集继续下一个;
	 * IGNORE不放入结果集，忽略当前这个;
	 * BREAK不放入结果集，忽略当前这个并中断后续的
	 */
	public static final int CONTINUE_FLAG = 1;
	public static final int IGNORE_FLAG = 2;
	public static final int BREAK_FLAG = 3;
	
	public int filterTrack(InfoContainer info);
}
