package wbga.algorithm;
import java.util.StringTokenizer;

public class Message {
	private int srcId;
	private int destId;
	private String tag;
	private String msg;
	
	public Message(int srcId, int destId, String tag, String msg) {
		this.srcId = srcId;
		this.destId = destId;
		this.tag = tag;
		this.msg = msg;
	}
	
	public int getSrcId() {
		return srcId;
	}
	
	public int getDestId(){ 
		return destId;
	}
	
	public String getTag() {
		return tag;
	}
	
	public String getMessage() {
		return msg;
	}
	
	public static Message parseMessage(String s) {
		StringTokenizer st = new StringTokenizer(s);
		int srcId = Integer.parseInt(st.nextToken());
        int destId = Integer.parseInt(st.nextToken());
        String tag = st.nextToken();
        String msg = st.nextToken("#");
        return new Message(srcId, destId, tag, msg);	
	}
	
	public String toString() {
		String s = String.valueOf(srcId) + " " +
        			String.valueOf(destId) + " " +
        			tag + " " + msg + "#";
		return s;	
	}
}
