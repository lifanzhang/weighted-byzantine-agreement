package wbga.algorithm;

public interface MessageHandler {
	public void _handleMessage(Message m, int srcId, String tag);
}
