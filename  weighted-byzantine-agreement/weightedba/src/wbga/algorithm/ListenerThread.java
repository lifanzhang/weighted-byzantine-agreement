package wbga.algorithm;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;

public class ListenerThread extends Thread {
	
	private int channelId;
	private MessageManager msgManager;
	
	
	public ListenerThread(int channelId, MessageManager msgManager) {
		this.channelId = channelId;
		this.msgManager = msgManager;
	}
	
	public void run(){ 
		BufferedReader dataIn;
		if(channelId == -1)
			dataIn = msgManager.getTesterInput();
		else
			dataIn= msgManager.getChannelInput(channelId);		
		
		while (true) {
			try {
				String line = dataIn.readLine();			
				msgManager.receiveMessage(channelId, line);
			} catch(SocketException e) {
				//Lost connection to process, probably because process terminated. 
				//No reason to keep listening, so return, killing thread.  
				return;
			} catch (IOException e) {				
				e.printStackTrace();
			}			
		}
	}
	
}
