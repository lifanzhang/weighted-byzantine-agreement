package wbga.tester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionManager extends Thread {
	
	private int port;
	
	// Incoming message from all processes are placed in this queue
	private BlockingQueue<TesterMessage> queue;
	
	private Socket[] sockets;
	private PrintWriter[] dataOut;

	public ConnectionManager(int port, int numProcesses){
		this.port = port;
		
		queue = new LinkedBlockingQueue<TesterMessage>();
		sockets = new Socket[numProcesses];
		dataOut = new PrintWriter[numProcesses];
		
		this.start();
	}
	
	public void run(){ 
		try {
			ServerSocket serverSocket = new ServerSocket(port);		
			
			//Wait for a new connection and start a ListenerThread for it
			while(true) {
				//Get connection
				Socket s = serverSocket.accept();
				
				//Read first message to determine ID
				BufferedReader dIn = new BufferedReader(
						new InputStreamReader(s.getInputStream()));
				String getLine = dIn.readLine();
				StringTokenizer st = new StringTokenizer(getLine);
				int theirId = Integer.parseInt(st.nextToken());
				String tag = st.nextToken();
				if(tag.equals("hello")){
					// Save connection
					sockets[theirId] = s;
					dataOut[theirId] = new PrintWriter(s.getOutputStream());
					// Start listener thread
					new ListenerThread(dIn, queue).start();
				}				
			}
		} catch (IOException e) {			
			e.printStackTrace();
		}		
	}
	
	/**
	 * Sends a message to a launched process.
	 * 
	 * Message format: "<srcId> <destId> <tag> <message>#"
	 * srcId is -1, to indicate the message is from the tester process
	 */	
	public void sendMessage(int destId, String tag, String message){
		dataOut[destId].println(Integer.toString(-1) + " " + Integer.toString(destId) +
								" " + tag + " " + message + "#");
		dataOut[destId].flush();		
	}
	
	/**
	 * Attempts to retrieve message from queue. Will block for up to 1 second.
	 * 
	 * Returns a TesterMessage if successful, or null if it times out.
	 * 
	 */
	public TesterMessage getNextMessage() throws InterruptedException {
		return queue.poll(1, TimeUnit.SECONDS);	
	}
}
