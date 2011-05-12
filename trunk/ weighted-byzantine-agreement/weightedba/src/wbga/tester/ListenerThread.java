package wbga.tester;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class ListenerThread extends Thread {
	
	private BufferedReader dataIn;
	private BlockingQueue<TesterMessage> queue;
	
	public ListenerThread(BufferedReader br, BlockingQueue<TesterMessage> q) {
		this.dataIn = br;
		this.queue = q;
	}
	
	public void run(){ 
		while (true) {			
			try {
				String line = dataIn.readLine();
				TesterMessage msg = TesterMessage.parseMessage(line);
				queue.put(msg);
			} catch (SocketException e) {
				// Connection is lost, probably due to the connected process terminating. 
				// No need to continue listening, so return. 
				return;			
			} catch (IOException e) {				
				e.printStackTrace();
			} catch (InterruptedException e) {			
				e.printStackTrace();
			}			
		}
	}
}
