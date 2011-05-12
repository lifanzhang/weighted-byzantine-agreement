package wbga.tester;

import java.util.StringTokenizer;

public class TesterMessage {
	private int srcId;
	private String tag;
	private String msg;
	
	public TesterMessage(int srcId, String tag, String msg) {
		this.srcId = srcId;
		this.tag = tag;
		this.msg = msg;
	}
	
	public int getSrcId() {
		return srcId;
	}
	
	public String getTag() {
		return tag;
	}
	
	public String getMessage() {
		return msg;
	}
	
	public static TesterMessage parseMessage(String s) {
		StringTokenizer st = new StringTokenizer(s);
		int srcId = Integer.parseInt(st.nextToken());
		String tag = st.nextToken();
		String msg = st.nextToken("#");
		return new TesterMessage(srcId, tag, msg);
	}
	
}
