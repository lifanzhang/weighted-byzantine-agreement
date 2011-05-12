package wbga.algorithm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class WBGAInstance {
	
	//Each WBGAInstance will open a socket on (START_PORT + myId)
	private static final int START_PORT = 8000;
	private static final String LOCAL_HOST = "127.0.0.1";
	
	private static int myId;
	private static int numProcesses;
	private static int myPort; 	
	private static int testerPort;	//the port number of the tester process
	
	public static void main(String[] args) throws Exception {	
		
		myId = Integer.parseInt(args[0]);
		numProcesses = Integer.parseInt(args[1]);
		testerPort = Integer.parseInt(args[2]);
		myPort = START_PORT + myId;		
		
		System.out.println("WBGA " + myId );	
		
		// Create a server socket for myself
		ServerSocket serverSocket = new ServerSocket(myPort);
		
		// Initialize MessageHandler
		MessageManager msgManager = MessageManager.getInstance();
		msgManager.init(myId, numProcesses);				
		
		// Create instance of the Byzantine algorithm 
		// and set it to handle incoming messages
		AbstractWBGA wbga = new QueenWBGA(myId, numProcesses, msgManager);	
		msgManager.setMessageHandler(wbga);
			
		// Accept connections from all processes with a lower ID
		for(int i = 0; i < myId; i++) {
			Socket s = serverSocket.accept();
			BufferedReader dIn = new BufferedReader(
					new InputStreamReader(s.getInputStream()));
			String getLine = dIn.readLine();
			StringTokenizer st = new StringTokenizer(getLine);
			int theirId = Integer.parseInt(st.nextToken());
			st.nextToken(); //read destId, but don't use it
			String tag = st.nextToken();
			if (tag. equals("hello")) {	
				// Save connection
				msgManager.setChannelSocket(theirId, s);		
				msgManager.setChannelInput(theirId, dIn);
				msgManager.setChannelOutput(theirId, new PrintWriter(s.getOutputStream()));
				//Start thread to listen on this port
				new ListenerThread(theirId, msgManager).start();
			}				
		}
		
		// Connect to processes with a higher ID
		for (int i = myId + 1; i < numProcesses; i++) {
			Socket s = null;
			//try to connect to process i, until the connection is made
			while(s == null ){
				try{
					s = new Socket(LOCAL_HOST, START_PORT + i);
				} catch(SocketException e)
				{}
			}			
			PrintWriter pr = new PrintWriter(s.getOutputStream());
			BufferedReader br = new BufferedReader(new
					InputStreamReader(s.getInputStream()));
			// send hello message to the process
			pr.println(myId + " " + i + " " + "hello" + " " + "null");
			pr.flush();	
			//Save connection
			msgManager.setChannelSocket(i, s);
			msgManager.setChannelInput(i, br);
			msgManager.setChannelOutput(i, pr);
			//Start thread to listen on this port
			new ListenerThread(i, msgManager).start();
		}	
		
		// Create connection to the tester program
		Socket testerSocket = new Socket(LOCAL_HOST, testerPort);
		msgManager.setTesterSocket(testerSocket);
		PrintWriter testerPr = new PrintWriter(testerSocket.getOutputStream());		
		testerPr.println(myId + " " + "hello" + " " + "null");	// Send connection message		
		msgManager.setTesterInput(new BufferedReader(
				new InputStreamReader(testerSocket.getInputStream())));
		msgManager.setTesterOutput(testerPr);
		// Start thread to listen on this port
		new ListenerThread(-1, msgManager).start();
		
		// Inform tester process that we are initialized
		msgManager.sendTesterMessage("ready", null);
				
		// Wait forever, ignore new connections 
		// Change this?
		while(true) {
			serverSocket.accept();
		}		
	}	

}
