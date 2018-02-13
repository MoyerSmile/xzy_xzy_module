package server.xzy.diagnos;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DefaultShellExec implements IShellExec {

	@Override
	public String execute(String cmdStr, int timeout, String context) {
		Process p = null;
		StringBuffer res = new StringBuffer(1024);
		try {
			String [] cmds = { "sh", "-c", " " };
			cmds[2] = cmdStr;
			p = Runtime.getRuntime().exec(cmds);
			InputStream in = p.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String lineStr;
			long t = System.currentTimeMillis();
			while((System.currentTimeMillis()-t) < timeout){
				lineStr = reader.readLine();
				if(lineStr == null){
					break;
				}
				res.append(lineStr);
				res.append("\n");
			}
			in = p.getErrorStream();
			reader = new BufferedReader(new InputStreamReader(in));
			while((System.currentTimeMillis()-t) < timeout){
				lineStr = reader.readLine();
				if(lineStr == null){
					break;
				}
				res.append(lineStr);
				res.append("\n");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(p != null){
				try{
					p.destroy();
				}catch(Exception ee){}
			}
		}

		return res.toString();
	}

}
